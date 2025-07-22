package io.levysworks.configs;

import io.smallrye.config.ConfigMapping;

import java.util.List;

/**
 * Configuration mapping interface for agent server definitions, using the {@code servers-wrapper} prefix.
 */
@ConfigMapping(prefix = "servers-wrapper")
public interface AgentsConfig {

    /**
     * Returns the list of configured agent servers.
     *
     * @return list of {@link ServerConfig} entries
     */
    List<ServerConfig> servers();

    /**
     * Represents a single agent server's configuration.
     */
    interface ServerConfig {

        /**
         * Returns the name of the agent server.
         *
         * @return the server name
         */
        String name();
    }
}