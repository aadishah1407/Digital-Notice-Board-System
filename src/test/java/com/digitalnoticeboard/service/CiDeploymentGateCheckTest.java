package com.digitalnoticeboard.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * TEMPORARY: Deliberately fails to verify the CI deployment gate on main.
 * Remove this test immediately after confirming the GitHub Actions run skips deployment.
 */
class CiDeploymentGateCheckTest {

    @Test
    void deliberateFailureShouldBlockDeployment() {
        fail("Temporary CI deployment-gate check: this failure is intentional.");
    }
}
