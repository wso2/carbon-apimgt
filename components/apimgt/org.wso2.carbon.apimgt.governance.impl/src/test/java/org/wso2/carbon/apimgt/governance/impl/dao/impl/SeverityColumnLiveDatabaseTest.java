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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * Runs the compliance affecting severity detection and queries against a real database of any vendor.
 * <p>
 * {@link SeverityColumnVendorLookupTest} pins the metadata contract each vendor's driver documents, and that runs
 * everywhere. This is how the contract is confirmed against the vendor itself, and it is the only test here that
 * can prove the documented {@code ALTER TABLE} works and that the queries parse on that dialect.
 * <p>
 * It is skipped unless a database is supplied, so an ordinary build is unaffected. Point it at one with:
 * <pre>
 * mvn test -Dtest=SeverityColumnLiveDatabaseTest \
 *          -Dgov.test.db.url=jdbc:postgresql://localhost:5432/apimdb \
 *          -Dgov.test.db.username=wso2carbon -Dgov.test.db.password=wso2carbon
 * </pre>
 * The driver has to be on the test classpath. The {@code gov-vendor-driver} profile in this module's pom puts it
 * there from a path, which is how all six vendors are covered without redistributing a driver:
 * {@code -Pgov-vendor-driver -Dgov.test.db.driverJar=$HOME/drivers/postgresql-42.7.4.jar}.
 * <p>
 * This creates and drops the real {@code GOV_} tables, because the queries under test name them, so it must only
 * ever be pointed at a disposable schema. Saying so is required rather than assumed: without
 * {@code -Dgov.test.db.disposable=true} it refuses to run, and it refuses again if the schema it was given already
 * holds governance data.
 */
public class SeverityColumnLiveDatabaseTest {

    private static final String URL_PROPERTY = "gov.test.db.url";
    private static final String USERNAME_PROPERTY = "gov.test.db.username";
    private static final String PASSWORD_PROPERTY = "gov.test.db.password";
    private static final String DRIVER_PROPERTY = "gov.test.db.driver";
    private static final String DISPOSABLE_PROPERTY = "gov.test.db.disposable";

    private static final String ORGANIZATION = "carbon.super";
    private static final String POLICY_ID = "e5c3d190-413a-4e58-9b44-0a3b1bb741d5";
    private static final String OTHER_POLICY_ID = "b1f0c0de-2b3c-4a5d-8e6f-7a8b9c0d1e2f";
    private static final String ARTIFACT_REF_ID = "d090cf7c-d1ab-491c-9357-b55a47e49ef2";

    private static final Log log = LogFactory.getLog(SeverityColumnLiveDatabaseTest.class);

    private Connection connection;

    /**
     * Whether the supplied schema has been confirmed disposable, which is the only state in which this test is
     * allowed to drop tables.
     * <p>
     * The safety check runs after the connection is opened, and JUnit runs the teardown even when the setup
     * throws. Without this flag the teardown would drop the very tables the check had just refused to touch.
     */
    private boolean schemaIsDisposable;

    @Before
    public void openTheSuppliedDatabase() throws Exception {

        String url = System.getProperty(URL_PROPERTY);
        Assume.assumeTrue("Skipped because no database was supplied. Set -D" + URL_PROPERTY + " to run the "
                + "severity detection and queries against a real vendor.", url != null && !url.trim().isEmpty());

        String driver = System.getProperty(DRIVER_PROPERTY);
        if (driver != null && !driver.trim().isEmpty()) {
            Class.forName(driver.trim());
        }

        Assert.assertEquals("This test creates and drops the GOV_ tables named by the queries under test, so it "
                        + "must only be pointed at a disposable schema. Pass -D" + DISPOSABLE_PROPERTY + "=true to "
                        + "confirm the schema given in -D" + URL_PROPERTY + " can be destroyed.",
                "true", System.getProperty(DISPOSABLE_PROPERTY));

        connection = DriverManager.getConnection(url, System.getProperty(USERNAME_PROPERTY),
                System.getProperty(PASSWORD_PROPERTY));
        connection.setAutoCommit(true);

        refuseToTouchARealDeployment();

        // Only past the check is dropping anything permitted, and the teardown reads this before it drops.
        schemaIsDisposable = true;

        dropSchema();
        createSchema();
        enableFeature();
        clearCache();
    }

    /**
     * Point the governance component at a configuration which enables per policy severity filtering
     */
    private void enableFeature() {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setPerPolicySeverityFilteringEnabled(true);

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Forget the cached detection result, which is held for the life of the JVM in production
     */
    private void clearCache() throws Exception {

        Field cached = GovernancePolicyMgtDAOImpl.class.getDeclaredField("policySeverityColumnPresent");
        cached.setAccessible(true);
        cached.set(null, null);
    }

    /**
     * Point the governance component at a data source which hands out connections the way a pooled deployment
     * does, with auto commit left on.
     * <p>
     * Every other test here drives the SQL on this class's own connection, so none of them reaches the DAO's
     * transaction handling. That handling is what has to turn auto commit off before committing, because
     * committing a connection which still has it on is an error rather than a no-op on PostgreSQL and MySQL.
     *
     * @throws Exception If the data source cannot be injected
     */
    private void handOutConnectionsWithAutoCommitOn() throws Exception {

        DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenAnswer(invocation -> {
            // A fresh connection each time, so the DAO closing its own cannot close the one this test reads with.
            Connection pooled = DriverManager.getConnection(System.getProperty(URL_PROPERTY),
                    System.getProperty(USERNAME_PROPERTY), System.getProperty(PASSWORD_PROPERTY));
            pooled.setAutoCommit(true);
            return pooled;
        });

        setDataSource(dataSource);
    }

    /**
     * Replace the data source the governance component reads through
     *
     * @param dataSource Data source to install, null to leave the component without one
     * @throws Exception If the field cannot be written
     */
    private void setDataSource(DataSource dataSource) throws Exception {

        Field field = APIMGovernanceDBUtil.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        field.set(null, dataSource);
    }

    /**
     * Run a statement, ignoring the failure. Used for cleanup, where the object may not exist.
     *
     * @param sql Statement to attempt
     */
    private void attempt(String sql) {

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ignored) {
            // The object does not exist, which is the state this is trying to reach
        }
    }

    /**
     * Stop before dropping anything if the schema already holds governance data
     * <p>
     * A URL typed one character wrong is all it takes to point this at a real deployment, and the tables it drops
     * are the ones that deployment stores its policies in. An empty GOV_POLICY is allowed through, because a
     * freshly created scratch schema legitimately has one.
     *
     * @throws SQLException If the check itself fails for a reason other than the table being absent
     */
    private void refuseToTouchARealDeployment() throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM "
                     + SQLConstants.GOV_POLICY_TABLE)) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                throw new IllegalStateException("Refusing to run: " + SQLConstants.GOV_POLICY_TABLE + " in the "
                        + "supplied schema already holds " + resultSet.getInt(1) + " policies, so this looks like a "
                        + "real deployment rather than a disposable schema. Point -D" + URL_PROPERTY + " at an empty "
                        + "database.");
            }
        } catch (SQLException e) {
            // The table not existing is the expected state for a fresh scratch schema, and is not a reason to
            // stop. Anything else -- a denied permission, a lock timeout, a dropped connection -- must not be
            // read as absence. This method is the only thing standing between the test and a real deployment's
            // data, so a check which could not be completed has to stop the run rather than wave it through.
            //
            // Which SQLException means "no such table" differs on every vendor, so the driver is asked whether
            // the table is there instead of matching error codes.
            if (govPolicyTableExists()) {
                throw e;
            }
            log.debug("No existing " + SQLConstants.GOV_POLICY_TABLE + " to inspect in the supplied schema", e);
        }
    }

    /**
     * Ask the driver whether the policy table exists, so a count which failed can be told apart from a table
     * which is not there
     *
     * @return True when the supplied schema holds the policy table
     * @throws SQLException If the metadata cannot be read, which also stops the run
     */
    private boolean govPolicyTableExists() throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        // PostgreSQL folds unquoted identifiers to lower case, so the name has to be asked for the way that
        // vendor stores it, exactly as the detection under test does.
        String tableName = metaData.storesLowerCaseIdentifiers()
                ? SQLConstants.GOV_POLICY_TABLE.toLowerCase(Locale.ENGLISH)
                : SQLConstants.GOV_POLICY_TABLE;
        try (ResultSet tables = metaData.getTables(connection.getCatalog(), connection.getSchema(),
                tableName, null)) {
            return tables.next();
        }
    }

    private void dropSchema() {

        for (String table : new String[]{"GOV_RULE_VIOLATION", "GOV_RULESET_RULE", "GOV_RULESET_RUN",
                "GOV_POLICY_RULESET", "GOV_POLICY_RUN", "GOV_POLICY", "GOV_ARTIFACT"}) {
            attempt("DROP TABLE " + table);
        }
    }

    /**
     * Create the governance tables the severity queries join, using only types every supported vendor accepts
     *
     * @throws SQLException If the schema cannot be created
     */
    private void createSchema() throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE GOV_ARTIFACT (ARTIFACT_KEY VARCHAR(36) NOT NULL, "
                    + "ARTIFACT_REF_ID VARCHAR(36) NOT NULL, ARTIFACT_TYPE VARCHAR(32) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY (POLICY_ID VARCHAR(36) NOT NULL, NAME VARCHAR(256) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_RUN (ARTIFACT_KEY VARCHAR(36) NOT NULL, "
                    + "POLICY_ID VARCHAR(36) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_RULESET (POLICY_ID VARCHAR(36) NOT NULL, "
                    + "RULESET_ID VARCHAR(36) NOT NULL)");
            statement.execute("CREATE TABLE GOV_RULESET_RUN (RULESET_RUN_ID VARCHAR(36) NOT NULL, "
                    + "ARTIFACT_KEY VARCHAR(36) NOT NULL, RULESET_ID VARCHAR(36) NOT NULL, RESULT INT NOT NULL)");
            statement.execute("CREATE TABLE GOV_RULE_VIOLATION (ID VARCHAR(36) NOT NULL, "
                    + "RULESET_RUN_ID VARCHAR(36) NOT NULL, RULESET_ID VARCHAR(36) NOT NULL, "
                    + "RULE_NAME VARCHAR(256) NOT NULL)");
            statement.execute("CREATE TABLE GOV_RULESET_RULE (RULESET_RULE_ID VARCHAR(36) NOT NULL, "
                    + "RULESET_ID VARCHAR(36) NOT NULL, RULE_NAME VARCHAR(256) NOT NULL, "
                    + "SEVERITY VARCHAR(32) NOT NULL)");
        }
    }

    /**
     * Add the optional column using exactly the statement the product logs as the remediation
     *
     * @throws SQLException If the column cannot be added
     */
    private void addTheOptionalColumn() throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + SQLConstants.GOV_POLICY_TABLE + " ADD "
                    + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN + " VARCHAR(64)");
        }
    }

    /**
     * Seed one artifact governed by two policies through one shared ruleset, violating only its info rule
     *
     * @throws SQLException If the rows cannot be inserted
     */
    private void seedAnInfoOnlyViolation() throws SQLException {

        seedThePolicies();
        String rulesetId = "7ca1bcae-6feb-4c4b-9252-78e034b8b89e";
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO GOV_ARTIFACT (ARTIFACT_KEY, ARTIFACT_REF_ID, ARTIFACT_TYPE, "
                    + "ORGANIZATION) VALUES ('artifact-key-1', '" + ARTIFACT_REF_ID + "', 'API', '"
                    + ORGANIZATION + "')");
            statement.execute("INSERT INTO GOV_POLICY_RUN (ARTIFACT_KEY, POLICY_ID) VALUES ('artifact-key-1', '"
                    + POLICY_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RUN (ARTIFACT_KEY, POLICY_ID) VALUES ('artifact-key-1', '"
                    + OTHER_POLICY_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET (POLICY_ID, RULESET_ID) VALUES ('" + POLICY_ID
                    + "', '" + rulesetId + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET (POLICY_ID, RULESET_ID) VALUES ('" + OTHER_POLICY_ID
                    + "', '" + rulesetId + "')");
            statement.execute("INSERT INTO GOV_RULESET_RUN (RULESET_RUN_ID, ARTIFACT_KEY, RULESET_ID, RESULT) "
                    + "VALUES ('run-1', 'artifact-key-1', '" + rulesetId + "', 0)");
            statement.execute("INSERT INTO GOV_RULESET_RULE (RULESET_RULE_ID, RULESET_ID, RULE_NAME, SEVERITY) "
                    + "VALUES ('rule-1', '" + rulesetId + "', 'api-description-check', 'INFO')");
            statement.execute("INSERT INTO GOV_RULE_VIOLATION (ID, RULESET_RUN_ID, RULESET_ID, RULE_NAME) "
                    + "VALUES ('violation-1', 'run-1', '" + rulesetId + "', 'api-description-check')");
        }
    }

    /**
     * Insert the two policies the severity tests write to
     * <p>
     * Every column is named rather than relying on their order, because the optional column is added before these
     * rows exist and a positional insert would then no longer match the table.
     *
     * @throws SQLException If the rows cannot be inserted
     */
    private void seedThePolicies() throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO GOV_POLICY (POLICY_ID, NAME, ORGANIZATION) VALUES ('" + POLICY_ID
                    + "', 'Strict', '" + ORGANIZATION + "')");
            statement.execute("INSERT INTO GOV_POLICY (POLICY_ID, NAME, ORGANIZATION) VALUES ('" + OTHER_POLICY_ID
                    + "', 'Lenient', '" + ORGANIZATION + "')");
        }
    }

    /**
     * Store a severity selection against a policy through the statement the DAO uses
     *
     * @param policyId   Policy to write to
     * @param severities Comma separated severities, null to clear the setting
     * @throws SQLException If the write fails
     */
    private void store(String policyId, String severities) throws SQLException {

        try (PreparedStatement prepStmnt = connection
                .prepareStatement(SQLConstants.UPDATE_POLICY_COMPLIANCE_AFFECTING_SEVERITIES)) {
            prepStmnt.setString(1, severities);
            prepStmnt.setString(2, policyId);
            prepStmnt.setString(3, ORGANIZATION);
            Assert.assertEquals("The write must match exactly the policy it was aimed at", 1,
                    prepStmnt.executeUpdate());
        }
    }

    /**
     * Read the severity selection of a policy through the statement the DAO uses
     *
     * @param policyId Policy to read
     * @return Stored value, null when the policy is unconfigured
     * @throws SQLException If the read fails
     */
    private String read(String policyId) throws SQLException {

        try (PreparedStatement prepStmnt = connection
                .prepareStatement(SQLConstants.GET_POLICY_COMPLIANCE_AFFECTING_SEVERITIES)) {
            prepStmnt.setString(1, policyId);
            prepStmnt.setString(2, ORGANIZATION);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                Assert.assertTrue("The policy must exist", resultSet.next());
                return resultSet.getString(SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN);
            }
        }
    }

    /**
     * Count the rows a policy aware query returns whose violated severity affects compliance, the way the DAO does
     *
     * @param query      Query to run
     * @param parameters Query parameters
     * @return Number of rows which affect compliance
     * @throws SQLException If the query fails
     */
    private int affectingRows(String query, String... parameters) throws SQLException {

        int affecting = 0;
        try (PreparedStatement prepStmnt = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                prepStmnt.setString(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    String configured =
                            resultSet.getString(SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN);
                    if (APIMGovernanceUtil.isComplianceAffectingSeverity(
                            RuleSeverity.fromString(resultSet.getString("SEVERITY")),
                            APIMGovernanceUtil.resolveComplianceAffectingSeverities(configured))) {
                        affecting++;
                    }
                }
            }
        }
        return affecting;
    }

    @Test
    public void testTheColumnIsNotDetectedBeforeItIsAdded() throws Exception {

        Assert.assertFalse("A schema which has not opted in must report the feature as unavailable on this vendor",
                GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
    }

    @Test
    public void testTheDocumentedAlterTableIsAcceptedAndDetected() throws Exception {

        // The statement used here is the one the server logs as the remediation, so this is the only test that can
        // show the instruction given to an operator actually works on their vendor.
        addTheOptionalColumn();
        clearCache();

        Assert.assertTrue("The documented ALTER TABLE must be accepted and the column then detected on this vendor",
                GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
    }

    @Test
    public void testSeveritiesRoundTripThroughTheColumn() throws Exception {

        addTheOptionalColumn();
        seedThePolicies();

        store(POLICY_ID, "ERROR,WARN");
        Assert.assertEquals("The stored value must read back unchanged on this vendor", "ERROR,WARN",
                read(POLICY_ID));

        store(POLICY_ID, null);
        Assert.assertNull("Clearing must return the policy to the unconfigured state on this vendor",
                read(POLICY_ID));
    }

    @Test
    public void testTheDaoWriteCommitsOnAConnectionWhichArrivesWithAutoCommitOn() throws Exception {

        addTheOptionalColumn();
        seedThePolicies();
        clearCache();
        handOutConnectionsWithAutoCommitOn();

        // This goes through the DAO rather than the statement, so the transaction handling is what is under test.
        // A write which leaves auto commit on fails here on the vendors which reject the commit, and a write which
        // never commits fails on the read instead, so both halves of the handling are covered.
        GovernancePolicyMgtDAOImpl.getInstance()
                .updateComplianceAffectingSeverities(POLICY_ID, ORGANIZATION, "ERROR,WARN");

        Assert.assertEquals("The DAO write must commit, and report success, on a connection which arrived with "
                + "auto commit on", "ERROR,WARN", read(POLICY_ID));
    }

    @Test
    public void testTheSeverityQueriesRunOnThisDialect() throws Exception {

        // The three policy aware queries are the ones that could fail to parse on a dialect. Running them here is
        // what turns portability by inspection into portability observed.
        addTheOptionalColumn();
        seedAnInfoOnlyViolation();
        store(POLICY_ID, "ERROR,WARN");
        store(OTHER_POLICY_ID, null);

        Assert.assertEquals("A policy still counting info must keep the shared ruleset violated on this vendor",
                1, affectingRows(SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                        ARTIFACT_REF_ID, "API", ORGANIZATION));
        Assert.assertEquals(1, affectingRows(SQLConstants.GET_FAILED_RULESET_RUNS_WITH_SEVERITY, ORGANIZATION));
        Assert.assertEquals(1, affectingRows(SQLConstants.GET_NON_COMPLIANT_ARTIFACTS_WITH_SEVERITY, "API",
                ORGANIZATION));
    }

    @Test
    public void testTheRulesetIsNotViolatedWhenEveryPolicyExcludesTheSeverity() throws Exception {

        addTheOptionalColumn();
        seedAnInfoOnlyViolation();
        store(POLICY_ID, "ERROR,WARN");
        store(OTHER_POLICY_ID, "ERROR");

        Assert.assertEquals("With every governing policy excluding info nothing must affect compliance on this "
                        + "vendor", 0,
                affectingRows(SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                        ARTIFACT_REF_ID, "API", ORGANIZATION));
    }

    @After
    public void closeTheSuppliedDatabase() throws Exception {

        if (connection == null) {
            return;
        }
        try {
            // A setup which failed the safety check leaves this false, and the supplied schema is left untouched.
            if (schemaIsDisposable) {
                dropSchema();
            }
        } finally {
            ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
            clearCache();
            // The injected data source outlives this test otherwise, and a later one in the same JVM would then
            // reach a database it was never given.
            setDataSource(null);
            connection.close();
        }
    }
}
