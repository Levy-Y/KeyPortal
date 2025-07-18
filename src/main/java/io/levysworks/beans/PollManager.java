package io.levysworks.beans;

import io.levysworks.configs.AgentsConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class PollManager {
    @Inject
    AgentsConfig agents;

    private final ConcurrentMap<String, List<String>> agentKeys;

    public List<String> pollKeys(String agentName) {
        List<String> keys = agentKeys.get(agentName);
        if (keys == null) {
            return Collections.emptyList();
        }

        List<String> copy = new ArrayList<>(keys);
        keys.clear();
        return copy;
    }

    public void addKeyForAgent(String user_uuid, String agentName, String key) {
        agentKeys.computeIfAbsent(agentName, k -> new CopyOnWriteArrayList<>()).add(key);
    }

    public void addAgent(String agentName) {
        if (!agentKeys.containsKey(agentName)) {
            agentKeys.put(agentName, new CopyOnWriteArrayList<>());
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

    public boolean checkAgentKeyMatch(String agentName, String key) {
        for (AgentsConfig.ServerConfig entry : agents.servers()) {
            if (entry.name().equals(agentName) && entry.poll_key().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public PollManager() {
        agentKeys = new ConcurrentHashMap<>();
    }

    @PostConstruct
    void init() {
        for (AgentsConfig.ServerConfig agent : agents.servers()) {
            addAgent(agent.name());
        }
    }
}