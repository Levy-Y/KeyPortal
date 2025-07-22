package io.levysworks.beans;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.sql.SQLException;

/**
 * Bean for managing application health status.
 * Provides health check methods used for internal monitoring.
 */
@ApplicationScoped
public class HealthManager {
    @Inject
    @Readiness
    Instance<HealthCheck> readinessChecks;

    /**
     * Indicates whether all systems are healthy.
     */
    private boolean systemsHealthy;

    /**
     * Returns whether all systems are currently healthy.
     * @return {@code true} if all systems are healthy, {@code false} otherwise
     */
    public boolean isSystemHealthy() {
        return systemsHealthy;
    }

    /**
     * Scheduled method for checking system health
     * <p>
     * Runs every {@code 10} seconds
     * <br>
     * Iterates over all readiness checks and updates the internal health status.
     */
    @Scheduled(every = "10s")
    public void checkSystemHealth() {
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
