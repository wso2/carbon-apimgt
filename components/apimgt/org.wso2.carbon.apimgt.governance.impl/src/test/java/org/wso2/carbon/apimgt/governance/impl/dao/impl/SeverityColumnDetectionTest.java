/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.dao.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Tests detection of the optional compliance affecting severity column against a real database.
 * <p>
 * The column is not created by the product. A deployment opts in to per-policy severity filtering by adding it, so
 * detection has to be correct on a schema that does not have it as well as one that does. It also has to cope with
 * databases storing identifiers in different cases, which is why the check compares column names ignoring case.
 */
public class SeverityColumnDetectionTest {

    private static final String COLUMN = "COMPLIANCE_AFFECTING_SEVERITIES";

    /**
     * Point the governance component at a configuration which enables or disables per policy severity filtering
     *
     * @param enabled Whether the feature is enabled in the configuration
     */
    private void configureFeature(boolean enabled) {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setPerPolicySeverityFilteringEnabled(enabled);

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Open a connection to a uniquely named in-memory database holding a GOV_POLICY table
     *
     * @param name              Database name, so each test gets its own schema
     * @param withColumn        Whether to create the optional column
     * @param lowerCaseIdentifiers Whether identifiers should be stored in lower case
     * @return Connection to the prepared database
     * @throws Exception If the database cannot be prepared
     */
    private Connection database(String name, boolean withColumn, boolean lowerCaseIdentifiers) throws Exception {

        // The H2 bundle used here does not always register itself through the JDBC service loader
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1"
                + (lowerCaseIdentifiers ? ";DATABASE_TO_LOWER=TRUE" : "");
        Connection connection = DriverManager.getConnection(url, "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS GOV_POLICY ("
                    + "POLICY_ID VARCHAR(36) NOT NULL, NAME VARCHAR(256) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL, PRIMARY KEY (POLICY_ID))");
            if (withColumn) {
                statement.execute("ALTER TABLE GOV_POLICY ADD " + COLUMN + " VARCHAR(64)");
            }
        }
        return connection;
    }

    @After
    public void clearCachedDetection() throws Exception {

        Field cached = GovernancePolicyMgtDAOImpl.class.getDeclaredField("policySeverityColumnPresent");
        cached.setAccessible(true);
        cached.set(null, null);
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Test
    public void testColumnIsNotDetectedOnAnUntouchedSchema() throws Exception {

        configureFeature(true);
        try (Connection connection = database("detect_absent", false, false)) {
            Assert.assertFalse("A deployment which has not opted in must report the feature as unavailable",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testColumnIsDetectedOnceAdded() throws Exception {

        configureFeature(true);
        try (Connection connection = database("detect_present", true, false)) {
            Assert.assertTrue("Adding the column must make the feature available",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testColumnIsDetectedWhenIdentifiersAreStoredInLowerCase() throws Exception {

        // Oracle, DB2 and H2 fold unquoted identifiers to upper case while PostgreSQL folds them to lower case. A
        // case sensitive lookup would succeed on one and silently fail on the other.
        configureFeature(true);
        try (Connection connection = database("detect_lower", true, true)) {
            Assert.assertTrue("Detection must not depend on the case the database stores identifiers in",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testDetectionIsCached() throws Exception {

        configureFeature(true);
        try (Connection connection = database("detect_cached", false, false)) {
            Assert.assertFalse(GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));

            // Adding the column to a running deployment must not change the answer until the cache is cleared,
            // which is why a restart is required after opting in.
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE GOV_POLICY ADD " + COLUMN + " VARCHAR(64)");
            }
            Assert.assertFalse("The cached answer must be reused rather than re-read on every call",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testColumnInAnotherSchemaIsNotDetected() throws Exception {

        // Oracle and DB2 instances commonly hold several schemas, and MySQL treats every database as a catalog. A
        // lookup which is not narrowed to the schema the connection writes to would find a table of the same name
        // belonging to another deployment, report the feature as available, and then fail on every query.
        configureFeature(true);
        try (Connection connection = database("detect_other_schema", false, false)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA OTHER_DEPLOYMENT");
                statement.execute("CREATE TABLE OTHER_DEPLOYMENT.GOV_POLICY ("
                        + "POLICY_ID VARCHAR(36) NOT NULL, " + COLUMN + " VARCHAR(64), PRIMARY KEY (POLICY_ID))");
            }

            Assert.assertFalse("The column must be looked for in the schema this connection writes to, not in any "
                            + "schema which happens to hold a table of the same name",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testFeatureIsUnavailableWhenTheConfigurationIsDisabled() throws Exception {

        configureFeature(false);
        try (Connection connection = database("gate_disabled", true, false)) {
            Assert.assertFalse("The column alone must not enable the feature while the configuration is off",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testFeatureIsUnavailableWhenTheConfigurationServiceIsMissing() throws Exception {

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
        try (Connection connection = database("gate_noconfig", true, false)) {
            Assert.assertFalse("An unavailable configuration must be treated as the feature being disabled",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testFeatureNeedsBothTheConfigurationAndTheColumn() throws Exception {

        configureFeature(true);
        try (Connection connection = database("gate_nocolumn", false, false)) {
            Assert.assertFalse("Enabling the configuration alone must not make the feature available",
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }
}
