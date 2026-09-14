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
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * Runs the compliance affecting severity writes and queries against a real database of any vendor.
 * <p>
 * Nothing probes the schema any more: the configuration alone decides whether a statement names the optional
 * column, and a deployment which turned the configuration on without adding the column finds out when a statement
 * fails. That makes a real vendor the only place the design can be checked, because the three things under test
 * are all answers the database gives — that the documented {@code ALTER TABLE} is accepted, that a failing write
 * rolls its whole transaction back, and that the failure is recognisable as a missing column rather than reported
 * as a server fault.
 * <p>
 * It is skipped unless a database is supplied, so an ordinary build is unaffected. Point it at one with:
 * <pre>
 * mvn test -Dtest=SeverityColumnLiveDatabaseTest \
 *          -Dgov.test.db.url=jdbc:postgresql://localhost:5432/apimdb \
 *          -Dgov.test.db.username=wso2carbon -Dgov.test.db.password=wso2carbon \
 *          -Dgov.test.db.disposable=true
 * </pre>
 * The driver has to be on the test classpath. The {@code gov-vendor-driver} profile in this module's pom puts it
 * there from a path, which is how all six vendors are covered without redistributing a driver:
 * {@code -Pgov-vendor-driver -Dgov.test.db.driverJar=$HOME/drivers/postgresql-42.7.4.jar}.
 * <p>
 * This creates and drops the real {@code GOV_} tables, because the queries under test name them, so it must only
 * ever be pointed at a disposable schema. Saying so is required but is never taken as proof: without
 * {@code -Dgov.test.db.disposable=true} it refuses to run, and past that flag the schema still has to show for
 * itself that it is disposable. It is rejected if it holds any table only a deployment creates, and rejected again
 * if anything this test would drop holds a row. The flag is the caller's assertion, and a mistyped URL is precisely
 * the case where that assertion is wrong, so nothing is dropped on the strength of it alone.
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
    private static final String RULESET_ID = "7ca1bcae-6feb-4c4b-9252-78e034b8b89e";

    /**
     * The tables this test drops, in an order which clears dependents before what they reference
     * <p>
     * The guard reads this same list, so whatever is dropped is necessarily also what was checked. Adding a table
     * to the drop without extending the check is the mistake this cannot make.
     */
    private static final List<String> DROPPED_TABLES = Collections.unmodifiableList(Arrays.asList(
            "GOV_RULE_VIOLATION", "GOV_RULESET_RULE", "GOV_RULESET_RUN", "GOV_POLICY_ACTION",
            "GOV_POLICY_GOVERNABLE_STATE", "GOV_POLICY_LABEL", "GOV_POLICY_RULESET", "GOV_POLICY_RUN",
            "GOV_POLICY", "GOV_ARTIFACT"));

    /**
     * Tables which exist only in a product schema and which this test never creates
     * <p>
     * Emptiness cannot prove a schema is disposable: an apim_db whose DDL has been loaded but which has never been
     * started holds every governance table with nothing in them, and would pass a row count. What separates it from
     * a scratch schema is everything else the product ships, none of which this test would ever have created, so
     * finding any one of these is proof the schema belongs to a deployment.
     */
    private static final List<String> PRODUCT_TABLES = Collections.unmodifiableList(Arrays.asList(
            "AM_API", "AM_APPLICATION", "AM_SUBSCRIPTION", "AM_POLICY_SUBSCRIPTION", "UM_USER", "REG_RESOURCE"));

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
                + "severity writes and queries against a real vendor.", url != null && !url.trim().isEmpty());

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
        enableFeature(true);
        handOutConnectionsWithAutoCommitOn();
    }

    /**
     * Point the governance component at a configuration which does or does not enable per policy severity filtering
     *
     * @param enabled Value of the configuration key
     */
    private void enableFeature(boolean enabled) {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setPerPolicySeverityFilteringEnabled(enabled);

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Point the governance component at a data source which hands out connections the way a pooled deployment
     * does, with auto commit left on.
     * <p>
     * Every DAO write here arrives on such a connection, because that is what the pool gives it in production.
     * The DAO has to turn auto commit off before committing: committing a connection which still has it on is an
     * error rather than a no-op on PostgreSQL and MySQL, and it is also what makes the rollback these tests rely
     * on possible at all.
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
     * Stop before dropping anything unless the schema is independently shown to be disposable
     * <p>
     * A URL typed one character wrong is all it takes to point this at a real deployment, and the tables it drops
     * are the ones that deployment stores its policies in. {@code -Dgov.test.db.disposable=true} is the caller
     * asserting it is safe, which is exactly the assertion a mistyped URL invalidates, so it is not evidence of
     * anything and the schema has to prove it for itself. Two independent things are required, because neither
     * alone is sufficient: no table the product ships may be present, and nothing this test would drop may hold a
     * row.
     *
     * @throws SQLException If a check itself fails, which stops the run
     */
    private void refuseToTouchARealDeployment() throws SQLException {

        refuseIfTheProductOwnsThisSchema();
        refuseIfAnyTableToBeDroppedHoldsData();
    }

    /**
     * Refuse a schema which holds tables belonging to a deployment
     * <p>
     * This is the check a row count cannot make. A loaded but never started apim_db has empty governance tables
     * and would pass every count, while dropping its GOV_ tables both destroys them and leaves the rest of the
     * deployment referencing a reduced set this test recreates without their constraints.
     *
     * @throws SQLException If the metadata cannot be read, which stops the run
     */
    private void refuseIfTheProductOwnsThisSchema() throws SQLException {

        for (String table : PRODUCT_TABLES) {
            if (tableExists(table)) {
                throw new IllegalStateException("Refusing to run: the supplied schema holds " + table + ", which "
                        + "only a product deployment creates, so it is not disposable no matter what -D"
                        + DISPOSABLE_PROPERTY + " says. Point -D" + URL_PROPERTY + " at an empty database.");
            }
        }
    }

    /**
     * Refuse a schema where anything this test would drop still holds data
     * <p>
     * Every table in the drop list is checked rather than the policy table alone: governance data lives across all
     * of them, and rulesets, artifacts and recorded violations are no less real for a policy table that happens to
     * be empty.
     *
     * @throws SQLException If a count fails on a table the driver says is present, which stops the run
     */
    private void refuseIfAnyTableToBeDroppedHoldsData() throws SQLException {

        for (String table : DROPPED_TABLES) {
            // Which SQLException means "no such table" differs on every vendor, so the driver is asked whether the
            // table is there rather than matching error codes. Absent is the expected state for a fresh scratch
            // schema and is not a reason to stop; a count which fails on a table that does exist is, because a
            // check that could not be completed must stop the run rather than wave it through.
            if (!tableExists(table)) {
                continue;
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    throw new IllegalStateException("Refusing to run: " + table + " in the supplied schema already "
                            + "holds " + resultSet.getInt(1) + " rows, so this looks like a real deployment rather "
                            + "than a disposable schema. Point -D" + URL_PROPERTY + " at an empty database.");
                }
            }
        }
    }

    /**
     * Ask the driver whether a table exists, so a count which failed can be told apart from a table which is not
     * there
     *
     * @param table Table to look for
     * @return True when the supplied schema holds it
     * @throws SQLException If the metadata cannot be read, which also stops the run
     */
    private boolean tableExists(String table) throws SQLException {

        DatabaseMetaData metaData = connection.getMetaData();
        // PostgreSQL folds unquoted identifiers to lower case, so the name has to be asked for the way that
        // vendor stores it.
        String tableName = metaData.storesLowerCaseIdentifiers()
                ? table.toLowerCase(Locale.ENGLISH)
                : table;
        try (ResultSet tables = metaData.getTables(connection.getCatalog(), connection.getSchema(),
                tableName, null)) {
            return tables.next();
        }
    }

    private void dropSchema() {

        for (String table : DROPPED_TABLES) {
            attempt("DROP TABLE " + table);
        }
    }

    /**
     * Create the governance tables the severity writes and queries touch, using only types every supported vendor
     * accepts
     * <p>
     * GOV_POLICY carries every column the policy insert names, because the writes under test go through the DAO
     * rather than through a statement written here. The optional severity column is deliberately absent: adding it
     * is what each test does when it wants the opted in state.
     *
     * @throws SQLException If the schema cannot be created
     */
    private void createSchema() throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE GOV_ARTIFACT (ARTIFACT_KEY VARCHAR(36) NOT NULL, "
                    + "ARTIFACT_REF_ID VARCHAR(36) NOT NULL, ARTIFACT_TYPE VARCHAR(32) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY (POLICY_ID VARCHAR(36) NOT NULL, NAME VARCHAR(256) NOT NULL, "
                    + "DESCRIPTION VARCHAR(1024), ORGANIZATION VARCHAR(128) NOT NULL, CREATED_BY VARCHAR(128), "
                    + "CREATED_TIME " + timestampType() + ", UPDATED_BY VARCHAR(128), "
                    + "LAST_UPDATED_TIME " + timestampType() + ", IS_GLOBAL INT)");
            statement.execute("CREATE TABLE GOV_POLICY_RUN (ARTIFACT_KEY VARCHAR(36) NOT NULL, "
                    + "POLICY_ID VARCHAR(36) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_RULESET (POLICY_ID VARCHAR(36) NOT NULL, "
                    + "RULESET_ID VARCHAR(36) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_LABEL (POLICY_ID VARCHAR(36) NOT NULL, "
                    + "LABEL VARCHAR(256) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_GOVERNABLE_STATE (POLICY_ID VARCHAR(36) NOT NULL, "
                    + "STATE VARCHAR(64) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY_ACTION (POLICY_ID VARCHAR(36) NOT NULL, "
                    + "STATE VARCHAR(64) NOT NULL, SEVERITY VARCHAR(32) NOT NULL, TYPE VARCHAR(32) NOT NULL)");
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
     * The type to declare a point in time with on the connected vendor
     * <p>
     * TIMESTAMP is not portable the way the rest of these declarations are. In T-SQL it is a row version rather
     * than a point in time, and a table is allowed only one, so declaring both policy time columns that way made
     * the schema uncreatable on SQL Server and the whole class unable to run there. Every other supported vendor
     * spells it TIMESTAMP, which is what their shipped scripts use, and SQL Server's uses DATETIME.
     *
     * @return Type name this vendor accepts for the policy time columns
     * @throws SQLException If the product name cannot be read
     */
    private String timestampType() throws SQLException {

        String product = connection.getMetaData().getDatabaseProductName();
        return product != null && product.toLowerCase(Locale.ENGLISH).contains("sql server")
                ? "DATETIME"
                : "TIMESTAMP";
    }

    /**
     * Add the optional column using exactly the statement the product reports as the remediation
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
     * Build a policy the DAO will accept, carrying the given severity selection
     *
     * @param policyId   Policy ID
     * @param severities Selection to carry, null when the field was not sent
     * @return Policy to write
     */
    private APIMGovernancePolicy policy(String policyId, String severities) {

        APIMGovernancePolicy governancePolicy = new APIMGovernancePolicy();
        governancePolicy.setId(policyId);
        governancePolicy.setName("Severity_Test_Policy");
        governancePolicy.setDescription("Written by SeverityColumnLiveDatabaseTest");
        governancePolicy.setCreatedBy("admin");
        governancePolicy.setUpdatedBy("admin");
        governancePolicy.setRulesetIds(Collections.singletonList(RULESET_ID));
        governancePolicy.setLabels(Collections.emptyList());
        governancePolicy.setActions(Collections.emptyList());
        governancePolicy.setGovernableStates(Collections.emptyList());
        governancePolicy.setComplianceAffectingSeverities(severities);
        return governancePolicy;
    }

    /**
     * Seed one artifact governed by two policies through one shared ruleset, violating only its info rule
     *
     * @throws SQLException If the rows cannot be inserted
     */
    private void seedAnInfoOnlyViolation() throws SQLException {

        seedThePolicies();
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO GOV_ARTIFACT (ARTIFACT_KEY, ARTIFACT_REF_ID, ARTIFACT_TYPE, "
                    + "ORGANIZATION) VALUES ('artifact-key-1', '" + ARTIFACT_REF_ID + "', 'API', '"
                    + ORGANIZATION + "')");
            statement.execute("INSERT INTO GOV_POLICY_RUN (ARTIFACT_KEY, POLICY_ID) VALUES ('artifact-key-1', '"
                    + POLICY_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RUN (ARTIFACT_KEY, POLICY_ID) VALUES ('artifact-key-1', '"
                    + OTHER_POLICY_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET (POLICY_ID, RULESET_ID) VALUES ('" + POLICY_ID
                    + "', '" + RULESET_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET (POLICY_ID, RULESET_ID) VALUES ('" + OTHER_POLICY_ID
                    + "', '" + RULESET_ID + "')");
            statement.execute("INSERT INTO GOV_RULESET_RUN (RULESET_RUN_ID, ARTIFACT_KEY, RULESET_ID, RESULT) "
                    + "VALUES ('run-1', 'artifact-key-1', '" + RULESET_ID + "', 0)");
            statement.execute("INSERT INTO GOV_RULESET_RULE (RULESET_RULE_ID, RULESET_ID, RULE_NAME, SEVERITY) "
                    + "VALUES ('rule-1', '" + RULESET_ID + "', 'api-description-check', 'INFO')");
            statement.execute("INSERT INTO GOV_RULE_VIOLATION (ID, RULESET_RUN_ID, RULESET_ID, RULE_NAME) "
                    + "VALUES ('violation-1', 'run-1', '" + RULESET_ID + "', 'api-description-check')");
        }
    }

    /**
     * Insert the two policies the query tests read
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
     * Set the column directly, to seed a state for the query tests rather than to exercise a write path
     *
     * @param policyId   Policy to write to
     * @param severities Comma separated severities, null to clear the setting
     * @throws SQLException If the write fails
     */
    private void storeDirectly(String policyId, String severities) throws SQLException {

        try (PreparedStatement prepStmnt = connection.prepareStatement("UPDATE "
                + SQLConstants.GOV_POLICY_TABLE + " SET " + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN
                + " = ? WHERE POLICY_ID = ? AND ORGANIZATION = ?")) {
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
     * Count the policy rows in the schema, so a rolled back create can be told from a committed one
     *
     * @return Number of rows in the policy table
     * @throws SQLException If the count fails
     */
    private int policyRowCount() throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM "
                     + SQLConstants.GOV_POLICY_TABLE)) {
            Assert.assertTrue(resultSet.next());
            return resultSet.getInt(1);
        }
    }

    /**
     * Read the name of a policy, which is what an update changes alongside the severity
     *
     * @param policyId Policy to read
     * @return Stored name
     * @throws SQLException If the read fails
     */
    private String nameOf(String policyId) throws SQLException {

        try (PreparedStatement prepStmnt = connection.prepareStatement("SELECT NAME FROM "
                + SQLConstants.GOV_POLICY_TABLE + " WHERE POLICY_ID = ? AND ORGANIZATION = ?")) {
            prepStmnt.setString(1, policyId);
            prepStmnt.setString(2, ORGANIZATION);
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                Assert.assertTrue("The policy must exist", resultSet.next());
                return resultSet.getString("NAME");
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
    public void testTheDocumentedAlterTableIsAcceptedAndTheSelectionRoundTrips() throws Exception {

        // The statement used here is the one the server reports as the remediation, so this is the only test that
        // can show the instruction given to an operator actually works on their vendor.
        addTheOptionalColumn();

        GovernancePolicyMgtDAOImpl.getInstance()
                .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);

        Assert.assertEquals("The selection must be written by the policy insert and read back unchanged on this "
                + "vendor", "ERROR,WARN", read(POLICY_ID));
    }

    @Test
    public void testAFailedSeverityWriteLeavesNoPolicyBehind() throws Exception {

        // The point of folding the severity into the policy insert. With the configuration on and the column
        // absent the insert itself is refused, so there is no committed policy for the failed request to leave
        // behind. The row count is checked rather than the status code, because the status code is what would look
        // right while a policy quietly survived.
        try {
            GovernancePolicyMgtDAOImpl.getInstance()
                    .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);
            Assert.fail("A create naming a column the table does not have must fail on this vendor");
        } catch (APIMGovernanceException expected) {
            Assert.assertEquals("A missing column is a deployment step the caller can act on, so it must be "
                            + "reported as a request error rather than a server fault",
                    APIMGovExceptionCodes.PER_POLICY_SEVERITY_FILTERING_UNAVAILABLE.getErrorCode(),
                    expected.getErrorHandler().getErrorCode());
            Assert.assertEquals(400, expected.getErrorHandler().getHttpStatusCode());
            Assert.assertTrue("The failure must name the statement to run, since that is the whole remedy",
                    String.valueOf(expected.getMessage()).contains("ALTER TABLE"));
        }

        Assert.assertEquals("The transaction must roll back, leaving no policy at all", 0, policyRowCount());
    }

    @Test
    public void testReadsCountEverySeverityWhileTheColumnIsMissing() throws Exception {

        // The configuration documents that every severity affects compliance while either half of the feature is
        // missing, and the reads honour that by answering rather than failing. Nothing asks this vendor whether
        // the column exists: the statement is issued and its failure classified, so this is the only place the
        // classification can be shown to match what the vendor's own driver actually raises on a read. The write
        // path proves the same thing for its statements, but a write failure is a different code path.
        seedAnInfoOnlyViolation();

        Assert.assertTrue("A listing read must answer with no policy having narrowed its severities, rather than "
                        + "failing, while the column is absent on this vendor",
                GovernancePolicyMgtDAOImpl.getInstance().getComplianceAffectingSeverities(ORGANIZATION).isEmpty());

        Assert.assertNull("A single policy read must answer as an unconfigured policy does on this vendor",
                GovernancePolicyMgtDAOImpl.getInstance().getComplianceAffectingSeverities(POLICY_ID, ORGANIZATION));

        // The compliance screens fall back to the legacy query, which is the every severity answer, so the INFO
        // only violation counts and the ruleset reads as violated again.
        Assert.assertEquals("With nowhere to store a selection every severity counts, so the INFO violation must "
                        + "still violate the ruleset",
                Collections.singletonList(RULESET_ID),
                ComplianceMgtDAOImpl.getInstance().getViolatedRulesets(ORGANIZATION));
    }

    @Test
    public void testAFailedSeverityUpdateChangesNothing() throws Exception {

        // Same guarantee on the update path: the severity and the rest of the policy are one statement, so a
        // refused severity cannot leave the name changed.
        seedThePolicies();

        try {
            GovernancePolicyMgtDAOImpl.getInstance()
                    .updateGovernancePolicy(POLICY_ID, policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);
            Assert.fail("An update naming a column the table does not have must fail on this vendor");
        } catch (APIMGovernanceException expected) {
            Assert.assertEquals(APIMGovExceptionCodes.PER_POLICY_SEVERITY_FILTERING_UNAVAILABLE.getErrorCode(),
                    expected.getErrorHandler().getErrorCode());
        }

        Assert.assertEquals("The rest of the policy must be rolled back with the severity", "Strict",
                nameOf(POLICY_ID));
    }

    @Test
    public void testWithTheConfigurationOffNoStatementNamesTheColumn() throws Exception {

        // A deployment which takes the release and does nothing has to behave exactly as it did before. The
        // column is absent here, so any statement naming it would fail and this test would not pass at all.
        enableFeature(false);

        GovernancePolicyMgtDAOImpl.getInstance().createGovernancePolicy(policy(POLICY_ID, null), ORGANIZATION);
        GovernancePolicyMgtDAOImpl.getInstance()
                .updateGovernancePolicy(POLICY_ID, policy(POLICY_ID, null), ORGANIZATION);

        Assert.assertEquals("The policy must be written on a schema which never opted in", 1, policyRowCount());
        Assert.assertNull("The feature must report nothing at all while the configuration is off",
                GovernancePolicyMgtDAOImpl.getInstance().getComplianceAffectingSeverities(POLICY_ID, ORGANIZATION));
        Assert.assertTrue(GovernancePolicyMgtDAOImpl.getInstance()
                .getComplianceAffectingSeverities(ORGANIZATION).isEmpty());
    }

    @Test
    public void testAnOmittedSelectionPreservesTheStoredValue() throws Exception {

        // Absent means preserve, which is carried entirely by the choice of statement: a request without the
        // field runs the update that does not name the column, so the stored value is not in the statement to be
        // overwritten.
        addTheOptionalColumn();
        GovernancePolicyMgtDAOImpl.getInstance()
                .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);

        GovernancePolicyMgtDAOImpl.getInstance()
                .updateGovernancePolicy(POLICY_ID, policy(POLICY_ID, null), ORGANIZATION);

        Assert.assertEquals("An update which did not send the field must leave the selection alone", "ERROR,WARN",
                read(POLICY_ID));
    }

    @Test
    public void testABlankSelectionClearsTheStoredValue() throws Exception {

        // Blank means clear, and it has to reach the column as null rather than as an empty string, so the policy
        // ends up indistinguishable from one that never used the feature.
        addTheOptionalColumn();
        GovernancePolicyMgtDAOImpl.getInstance()
                .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);

        GovernancePolicyMgtDAOImpl.getInstance()
                .updateGovernancePolicy(POLICY_ID, policy(POLICY_ID, ""), ORGANIZATION);

        Assert.assertNull("Clearing must return the policy to the unconfigured state on this vendor",
                read(POLICY_ID));
    }

    @Test
    public void testTheColumnAddedWhileRunningTakesEffectOnTheNextRequest() throws Exception {

        // Nothing is cached, so an operator who runs the ALTER TABLE on a server that is already configured does
        // not have to restart it. This is the one thing the design gains by not probing the schema.
        try {
            GovernancePolicyMgtDAOImpl.getInstance()
                    .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);
            Assert.fail("The create must fail before the column exists");
        } catch (APIMGovernanceException expected) {
            Assert.assertEquals(APIMGovExceptionCodes.PER_POLICY_SEVERITY_FILTERING_UNAVAILABLE.getErrorCode(),
                    expected.getErrorHandler().getErrorCode());
        }

        addTheOptionalColumn();

        GovernancePolicyMgtDAOImpl.getInstance()
                .createGovernancePolicy(policy(POLICY_ID, "ERROR,WARN"), ORGANIZATION);

        Assert.assertEquals("The next request must succeed with nothing restarted and no cache to clear",
                "ERROR,WARN", read(POLICY_ID));
    }

    @Test
    public void testTheSeverityQueriesRunOnThisDialect() throws Exception {

        // The three policy aware queries are the ones that could fail to parse on a dialect. Running them here is
        // what turns portability by inspection into portability observed.
        addTheOptionalColumn();
        seedAnInfoOnlyViolation();
        storeDirectly(POLICY_ID, "ERROR,WARN");
        storeDirectly(OTHER_POLICY_ID, null);

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
        storeDirectly(POLICY_ID, "ERROR,WARN");
        storeDirectly(OTHER_POLICY_ID, "ERROR");

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
            // The injected data source outlives this test otherwise, and a later one in the same JVM would then
            // reach a database it was never given.
            setDataSource(null);
            connection.close();
        }
    }
}
