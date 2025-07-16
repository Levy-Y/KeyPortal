package io.levysworks.configs;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "servers-wrapper")
public interface AgentsConfig {
    List<ServerConfig> servers();

    interface ServerConfig {
        String name();

        String poll_key();
    }
}