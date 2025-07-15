package io.levysworks.profiles;

import java.util.Map;
import io.quarkus.test.junit.QuarkusTestProfile;

public class NoRateLimitTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("io.levysworks.endpoints.RegisterEndpoint/RateLimit/value", "10");
    }
}