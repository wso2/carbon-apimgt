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

package org.wso2.carbon.apimgt.governance.impl.dao.constants;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;

/**
 * Guards the contract between the compliance queries and the bind indexes used by ComplianceMgtDAOImpl.
 * <p>
 * The severity placeholders are bound last, one per severity {@link RuleSeverity} defines. Adding a condition to any
 * of these queries without moving the severity bind index would bind the severities over the wrong parameters, which
 * these tests are meant to catch at build time.
 */
public class ComplianceSQLConstantsTest {

    private static final int SEVERITY_PARAM_COUNT = RuleSeverity.values().length;

    /**
     * Count the bind parameters of a query
     *
     * @param query SQL query
     * @return Number of bind parameters
     */
    private int countBindParams(String query) {

        int count = 0;
        for (char character : query.toCharArray()) {
            if (character == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * Assert that the severities are bound over the trailing parameters of the query
     *
     * @param query              SQL query
     * @param severityStartIndex Index the DAO starts binding severities at
     */
    private void assertSeveritiesAreBoundLast(String query, int severityStartIndex) {

        Assert.assertEquals("The severity bind index must line up with the trailing placeholders of the query",
                countBindParams(query), severityStartIndex + SEVERITY_PARAM_COUNT - 1);
    }

    @Test
    public void testFailedRulesetRunsBindsOrganizationThenSeverities() {

        // ComplianceMgtDAOImpl.getViolatedRulesets binds the organization at 1 and the severities from 2
        Assert.assertEquals(1 + SEVERITY_PARAM_COUNT,
                countBindParams(SQLConstants.GET_FAILED_RULESET_RUNS));
        assertSeveritiesAreBoundLast(SQLConstants.GET_FAILED_RULESET_RUNS, 2);
    }

    @Test
    public void testFailedRulesetRunsForArtifactBindsArtifactThenSeverities() {

        // ComplianceMgtDAOImpl.getViolatedRulesetsForArtifact binds three parameters before the severities
        Assert.assertEquals(3 + SEVERITY_PARAM_COUNT,
                countBindParams(SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT));
        assertSeveritiesAreBoundLast(SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT, 4);
    }

    @Test
    public void testNonCompliantArtifactsBindsArtifactTypeThenSeverities() {

        // ComplianceMgtDAOImpl.getNonCompliantArtifacts binds two parameters before the severities
        Assert.assertEquals(2 + SEVERITY_PARAM_COUNT,
                countBindParams(SQLConstants.GET_NON_COMPLIANT_ARTIFACTS));
        assertSeveritiesAreBoundLast(SQLConstants.GET_NON_COMPLIANT_ARTIFACTS, 3);
    }

    @Test
    public void testComplianceQueriesResolveSeverityInsteadOfTheRulesetRunResultFlag() {

        // GOV_RULESET_RUN.RESULT only records whether a run was completely clean, so it cannot tell an informational
        // violation apart from a blocking one. Reverting to it would reintroduce the defect these queries fix.
        for (String query : new String[]{
                SQLConstants.GET_FAILED_RULESET_RUNS,
                SQLConstants.GET_FAILED_RULESET_RUNS_FOR_ARTIFACT,
                SQLConstants.GET_NON_COMPLIANT_ARTIFACTS}) {
            Assert.assertFalse("Compliance must not be decided by the ruleset run result flag",
                    query.contains("GRR.RESULT"));
            Assert.assertTrue("Compliance must be filtered by rule severity",
                    query.contains("GRULE.SEVERITY IN"));
            Assert.assertTrue("The severity filter must be correlated with the ruleset run being evaluated",
                    query.contains("GV.RULESET_RUN_ID = GRR.RULESET_RUN_ID"));
        }
    }
}
