package com.spotlight.platform.userprofile.api.web.healthchecks;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.health.HealthCheck;

import org.junit.jupiter.api.Test;

class PreventStartupWarningHealthCheckTest {

    private final PreventStartupWarningHealthCheck healthCheck =
            new PreventStartupWarningHealthCheck();

    @Test
    void healthcheckName_isCorrect() {
        assertThat(PreventStartupWarningHealthCheck.NAME)
                .isEqualTo("preventing-startup-warning-healthcheck");
    }

    @Test
    void healthCheckCalled_returnsHealthy() {
        assertThat(healthCheck.check()).isEqualTo(HealthCheck.Result.healthy());
    }
}
