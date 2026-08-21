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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * Exercises the policy aware compliance queries against a real database.
 * <p>
 * The joins are the risky part of per policy severity filtering: a ruleset run has to be tied back to the policy it
 * was run under before its severities can be applied. These tests seed a small governance schema and assert what the
 * constant queries actually return, rather than trusting the SQL by inspection.
 */
public class PolicySeverityQueryTest {

    private static final String ORGANIZATION = "carbon.super";
    private static final String ARTIFACT_KEY = "artifact-key-1";
    private static final String ARTIFACT_REF_ID = "d090cf7c-d1ab-491c-9357-b55a47e49ef2";
    private static final String API = "API";
    private static final String RULESET_ID = "7ca1bcae-6feb-4c4b-9252-78e034b8b89e";
    private static final String POLICY_ID = "e5c3d190-413a-4e58-9b44-0a3b1bb741d5";
    private static final String SECOND_POLICY_ID = "b1f0c0de-2b3c-4a5d-8e6f-7a8b9c0d1e2f";
    private static final String RUN_ID = "run-1";

    /**
     * Build a governance schema holding one artifact governed by one policy, with one ruleset carrying an error
     * rule and an info rule
     *
     * @param name               Database name, so each test gets its own schema
     * @param policySeverities   Value of the optional column on the policy, may be null
     * @param violatedRuleNames  Rules the artifact violated
     * @return Connection to the prepared database
     * @throws Exception If the database cannot be prepared
     */
    private Connection database(String name, String policySeverities, String... violatedRuleNames) throws Exception {

        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE GOV_ARTIFACT (ARTIFACT_KEY VARCHAR(36) NOT NULL, "
                    + "ARTIFACT_REF_ID VARCHAR(36) NOT NULL, ARTIFACT_TYPE VARCHAR(32) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL)");
            statement.execute("CREATE TABLE GOV_POLICY (POLICY_ID VARCHAR(36) NOT NULL, NAME VARCHAR(256) NOT NULL, "
                    + "ORGANIZATION VARCHAR(128) NOT NULL, "
                    + SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN + " VARCHAR(64))");
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

            statement.execute("INSERT INTO GOV_ARTIFACT VALUES ('" + ARTIFACT_KEY + "', '" + ARTIFACT_REF_ID
                    + "', '" + API + "', '" + ORGANIZATION + "')");
            statement.execute("INSERT INTO GOV_POLICY VALUES ('" + POLICY_ID + "', 'Severity Test Policy', '"
                    + ORGANIZATION + "', " + (policySeverities == null ? "NULL" : "'" + policySeverities + "'") + ")");
            statement.execute("INSERT INTO GOV_POLICY_RUN VALUES ('" + ARTIFACT_KEY + "', '" + POLICY_ID + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET VALUES ('" + POLICY_ID + "', '" + RULESET_ID + "')");
            // The stored flag says the run was not completely clean, which is what the old queries keyed off
            statement.execute("INSERT INTO GOV_RULESET_RUN VALUES ('" + RUN_ID + "', '" + ARTIFACT_KEY + "', '"
                    + RULESET_ID + "', 0)");
            statement.execute("INSERT INTO GOV_RULESET_RULE VALUES ('rule-1', '" + RULESET_ID
                    + "', 'api-version-prefix', 'ERROR')");
            statement.execute("INSERT INTO GOV_RULESET_RULE VALUES ('rule-2', '" + RULESET_ID
                    + "', 'api-description-check', 'INFO')");

            int id = 0;
            for (String ruleName : violatedRuleNames) {
                id++;
                statement.execute("INSERT INTO GOV_RULE_VIOLATION VALUES ('violation-" + id + "', '" + RUN_ID
                        + "', '" + RULESET_ID + "', '" + ruleName + "')");
            }
        }
        return connection;
    }

    /**
     * Run a policy aware query and collect the rows whose violation affects compliance, the way the DAO does
     *
     * @param connection Connection to the prepared database
     * @param query      Query to run
     * @param column     Column to collect
     * @param parameters Query parameters
     * @return Collected values
     * @throws Exception If the query fails
     */
    private Set<String> collectAffecting(Connection connection, String query, String column, String... parameters)
            throws Exception {

        Set<String> collected = new HashSet<>();
        try (PreparedStatement prepStmnt = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                prepStmnt.setString(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = prepStmnt.executeQuery()) {
                while (resultSet.next()) {
                    RuleSeverity severity = RuleSeverity.fromString(resultSet.getString("SEVERITY"));
                    String configured = resultSet.getString(SQLConstants.COMPLIANCE_AFFECTING_SEVERITIES_COLUMN);
                    if (APIMGovernanceUtil.isComplianceAffectingSeverity(severity,
                            APIMGovernanceUtil.resolveComplianceAffectingSeverities(configured))) {
                        collected.add(resultSet.getString(column));
                    }
                }
            }
        }
        return collected;
    }

    @Test
    public void testRulesetIsNotViolatedWhenOnlyAnExcludedSeverityIsViolated() throws Exception {

        try (Connection connection = database("policy_query_info_only", "ERROR,WARN", "api-description-check")) {
            Assert.assertTrue("A policy judged on ERROR and WARN must not report the ruleset as violated for an "
                            + "info violation",
                    collectAffecting(connection, SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                            "RULESET_ID", ARTIFACT_REF_ID, API, ORGANIZATION).isEmpty());
        }
    }

    @Test
    public void testRulesetIsViolatedWhenACountedSeverityIsViolated() throws Exception {

        try (Connection connection = database("policy_query_error", "ERROR,WARN", "api-version-prefix",
                "api-description-check")) {
            Set<String> violated = collectAffecting(connection,
                    SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY, "RULESET_ID",
                    ARTIFACT_REF_ID, API, ORGANIZATION);

            Assert.assertEquals("The error violation must still report the ruleset as violated",
                    new HashSet<>(java.util.Collections.singletonList(RULESET_ID)), violated);
        }
    }

    @Test
    public void testEverySeverityCountsWhenThePolicyConfiguresNothing() throws Exception {

        try (Connection connection = database("policy_query_unset", null, "api-description-check")) {
            Assert.assertEquals("A policy with nothing configured must keep counting every severity",
                    new HashSet<>(java.util.Collections.singletonList(RULESET_ID)),
                    collectAffecting(connection, SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                            "RULESET_ID", ARTIFACT_REF_ID, API, ORGANIZATION));
        }
    }

    @Test
    public void testOrganizationWideQueryAppliesThePolicySeverities() throws Exception {

        try (Connection connection = database("policy_query_org", "ERROR", "api-description-check")) {
            Assert.assertTrue("The organization wide listing must apply the same severities",
                    collectAffecting(connection, SQLConstants.GET_FAILED_RULESET_RUNS_WITH_SEVERITY, "RULESET_ID",
                            ORGANIZATION).isEmpty());
        }
    }

    @Test
    public void testArtifactIsNotNonCompliantWhenOnlyAnExcludedSeverityIsViolated() throws Exception {

        try (Connection connection = database("policy_query_artifact_info", "ERROR,WARN", "api-description-check")) {
            Assert.assertTrue("An info violation must not make the artifact non compliant under this policy",
                    collectAffecting(connection, SQLConstants.GET_NON_COMPLIANT_ARTIFACTS_WITH_SEVERITY,
                            "ARTIFACT_REF_ID", API, ORGANIZATION).isEmpty());
        }
    }

    @Test
    public void testArtifactIsNonCompliantWhenACountedSeverityIsViolated() throws Exception {

        try (Connection connection = database("policy_query_artifact_error", "ERROR,WARN", "api-version-prefix")) {
            Assert.assertEquals("An error violation must still make the artifact non compliant",
                    new HashSet<>(java.util.Collections.singletonList(ARTIFACT_REF_ID)),
                    collectAffecting(connection, SQLConstants.GET_NON_COMPLIANT_ARTIFACTS_WITH_SEVERITY,
                            "ARTIFACT_REF_ID", API, ORGANIZATION));
        }
    }

    /**
     * Add a second policy which governs the same artifact through the same ruleset
     * <p>
     * A ruleset belonging to more than one policy is the case per-policy severities exist for, and the case a
     * single stored pass or fail flag on the ruleset run could never express.
     *
     * @param connection Connection to the prepared database
     * @param policyId   Id of the policy to add
     * @param severities Value of the optional column on that policy, may be null
     * @throws Exception If the rows cannot be inserted
     */
    private void addGoverningPolicy(Connection connection, String policyId, String severities) throws Exception {

        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO GOV_POLICY VALUES ('" + policyId + "', 'Second Policy', '"
                    + ORGANIZATION + "', " + (severities == null ? "NULL" : "'" + severities + "'") + ")");
            statement.execute("INSERT INTO GOV_POLICY_RUN VALUES ('" + ARTIFACT_KEY + "', '" + policyId + "')");
            statement.execute("INSERT INTO GOV_POLICY_RULESET VALUES ('" + policyId + "', '" + RULESET_ID + "')");
        }
    }

    @Test
    public void testSharedRulesetIsViolatedWhenAnyGoverningPolicyCountsTheSeverity() throws Exception {

        // The shared ruleset produces one row per governing policy. The listing has no policy in its path, so it
        // must report the ruleset as violated while any policy governing the artifact still counts the severity,
        // otherwise a lenient policy would silently suppress a strict one's finding.
        try (Connection connection = database("shared_ruleset_union", "ERROR,WARN", "api-description-check")) {
            addGoverningPolicy(connection, SECOND_POLICY_ID, null);

            Assert.assertEquals("A policy still counting info must keep the shared ruleset violated, even though "
                            + "the other policy excludes it",
                    new HashSet<>(java.util.Collections.singletonList(RULESET_ID)),
                    collectAffecting(connection, SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                            "RULESET_ID", ARTIFACT_REF_ID, API, ORGANIZATION));
        }
    }

    @Test
    public void testSharedRulesetIsNotViolatedWhenEveryGoverningPolicyExcludesTheSeverity() throws Exception {

        try (Connection connection = database("shared_ruleset_all_exclude", "ERROR,WARN", "api-description-check")) {
            addGoverningPolicy(connection, SECOND_POLICY_ID, "ERROR");

            Assert.assertTrue("With every governing policy excluding info the shared ruleset must not be violated",
                    collectAffecting(connection, SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT_WITH_SEVERITY,
                            "RULESET_ID", ARTIFACT_REF_ID, API, ORGANIZATION).isEmpty());
        }
    }

    @Test
    public void testSharedRulesetMakesTheArtifactNonCompliantForAnyCountingPolicy() throws Exception {

        try (Connection connection = database("shared_artifact_union", "ERROR,WARN", "api-description-check")) {
            addGoverningPolicy(connection, SECOND_POLICY_ID, null);

            Assert.assertEquals("The artifact must stay non compliant while any governing policy counts the "
                            + "violated severity",
                    new HashSet<>(java.util.Collections.singletonList(ARTIFACT_REF_ID)),
                    collectAffecting(connection, SQLConstants.GET_NON_COMPLIANT_ARTIFACTS_WITH_SEVERITY,
                            "ARTIFACT_REF_ID", API, ORGANIZATION));
        }
    }

    @Test
    public void testSeveritiesAreStoredAgainstOnePolicyOnly() throws Exception {

        // Two policies sharing a ruleset must be configurable independently. Storing against one must not be
        // visible through the other, which is the whole difference between per-policy and per-ruleset severities.
        try (Connection connection = database("store_per_policy", null)) {
            addGoverningPolicy(connection, SECOND_POLICY_ID, null);

            store(connection, POLICY_ID, "ERROR,WARN");

            Assert.assertEquals("The configured policy must read back what was stored",
                    "ERROR,WARN", read(connection, POLICY_ID));
            Assert.assertNull("The policy sharing the ruleset must stay unconfigured",
                    read(connection, SECOND_POLICY_ID));
        }
    }

    @Test
    public void testStoringNullClearsTheSeverities() throws Exception {

        // Clearing is how the portal returns a policy to counting every severity, so it has to reach null rather
        // than an empty string: resolveComplianceAffectingSeverities treats both as unconfigured, but only null
        // leaves the column in the state a deployment which never used the feature would have.
        try (Connection connection = database("clear_severities", "ERROR,WARN")) {
            Assert.assertEquals("ERROR,WARN", read(connection, POLICY_ID));

            store(connection, POLICY_ID, null);

            Assert.assertNull("Clearing must return the policy to the unconfigured state",
                    read(connection, POLICY_ID));
        }
    }

    @Test
    public void testStoringSeveritiesLeavesOtherOrganizationsAlone() throws Exception {

        // Both statements are scoped by organization as well as policy id, so a policy id colliding across
        // tenants cannot be written or read across the boundary.
        try (Connection connection = database("store_other_org", null)) {
            try (PreparedStatement prepStmnt = connection
                    .prepareStatement(SQLConstants.UPDATE_POLICY_COMPLIANCE_AFFECTING_SEVERITIES)) {
                prepStmnt.setString(1, "ERROR");
                prepStmnt.setString(2, POLICY_ID);
                prepStmnt.setString(3, "another.org");
                Assert.assertEquals("A write scoped to another organization must not match this policy",
                        0, prepStmnt.executeUpdate());
            }

            Assert.assertNull("The policy must be untouched by a write for another organization",
                    read(connection, POLICY_ID));
        }
    }

    /**
     * Store a severity selection against a policy through the statement the DAO uses
     *
     * @param connection Connection to the prepared database
     * @param policyId   Policy to write to
     * @param severities Comma separated severities, null to clear the setting
     * @throws Exception If the write fails
     */
    private void store(Connection connection, String policyId, String severities) throws Exception {

        try (PreparedStatement prepStmnt = connection
                .prepareStatement(SQLConstants.UPDATE_POLICY_COMPLIANCE_AFFECTING_SEVERITIES)) {
            prepStmnt.setString(1, severities);
            prepStmnt.setString(2, policyId);
            prepStmnt.setString(3, ORGANIZATION);
            Assert.assertEquals("The write must match exactly the policy it was aimed at",
                    1, prepStmnt.executeUpdate());
        }
    }

    /**
     * Read the severity selection of a policy through the statement the DAO uses
     *
     * @param connection Connection to the prepared database
     * @param policyId   Policy to read
     * @return Stored value, null when the policy is unconfigured
     * @throws Exception If the read fails
     */
    private String read(Connection connection, String policyId) throws Exception {

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
}
