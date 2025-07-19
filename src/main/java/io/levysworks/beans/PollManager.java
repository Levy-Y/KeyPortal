package io.levysworks.beans;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.levysworks.configs.AgentsConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class PollManager {
    @Inject
    AgentsConfig agents;

    @Inject
    DatabaseManager dbManager;

    @Inject
    RabbitMQClient rabbitMQClient;

    private Connection connection;

    @PostConstruct
    public void init() {
        try {
            connection = rabbitMQClient.connect();

            try (Channel setupChannel = connection.createChannel()) {
                setupChannel.exchangeDeclare("pollexchange", BuiltinExchangeType.TOPIC, true);

                agents.servers().forEach(server -> {
                    String agentName = server.name();
                    try {
                        setupChannel.queueDeclare(agentName, true, false, false, null);
                        setupChannel.queueBind(agentName, "pollexchange", agentName + ".add");
                        setupChannel.queueBind(agentName, "pollexchange", agentName + ".remove");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void addKeyForAgent(String agentName, String key) throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish("pollexchange", agentName + ".add", null, key.getBytes());
        }
    }

    public void removeKeyFromAgent(String agentName, String uid) throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish("pollexchange", agentName + ".remove", null, uid.getBytes());
        }
    }

    public boolean checkAgentExists(String agentName) {
        for (AgentsConfig.ServerConfig entry : agents.servers()) {
            if (entry.name().equals(agentName)) {
                return true;
            }
        }
        return false;
    }

    public PollManager() {}
}