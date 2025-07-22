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

/**
 * Bean used for sending {@code add} and {@code remove} key messages to the agent servers
 */
@ApplicationScoped
public class PollManager {
    @Inject
    AgentsConfig agents;

    @Inject
    RabbitMQClient rabbitMQClient;

    private Connection connection;

    /**
     * Initializes the RabbitMQ connection and declares necessary messaging infrastructure.
     * <p>
     * Connects to the configured RabbitMQ server, declares the {@code pollexchange} topic exchange,
     * and creates {@code .add} and {@code .remove} queues for each agent defined in {@link AgentsConfig}.
     * This method is called automatically after bean construction.
     */
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

    /**
     * Publishes a new key to the specified agent's {@code .add} queue on the {@code pollexchange} exchange.
     *
     * @param agentName the target agent's name
     * @param key the key to be added
     * @throws IOException if a RabbitMQ I/O error occurs
     * @throws TimeoutException if the operation times out
     */
    public void addKeyForAgent(String agentName, String key) throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish("pollexchange", agentName + ".add", null, key.getBytes());
        }
    }

    /**
     * Publishes a key removal request to the specified agent's {@code .remove} queue on the {@code pollexchange} exchange.
     *
     * @param agentName the target agent's name
     * @param uid the UID of the key to be removed
     * @throws IOException if a RabbitMQ I/O error occurs
     * @throws TimeoutException if the operation times out
     */
    public void removeKeyFromAgent(String agentName, String uid) throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish("pollexchange", agentName + ".remove", null, uid.getBytes());
        }
    }

    /**
     * Checks whether an agent exists in the {@link AgentsConfig}
     * @param agentName The agent to look for
     * @return {@code true} if the agent exists, {@code false} otherwise
     */
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