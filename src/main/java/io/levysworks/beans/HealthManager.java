package io.levysworks.beans;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.sql.SQLException;

@ApplicationScoped
public class HealthManager {
    @Inject
    @Readiness
    Instance<HealthCheck> readinessChecks;

    private boolean systemsHealthy;

    public boolean isSystemHealthy() {
        return systemsHealthy;
    }

    @Scheduled(every = "10s")
    public void checkSystemHealth() throws SQLException {
        for (HealthCheck check : readinessChecks) {
            HealthCheckResponse response = check.call();
            if (response.getStatus() != HealthCheckResponse.Status.UP) {
                systemsHealthy = false;
                return;
            }
        }
        systemsHealthy = true;

    }
}
