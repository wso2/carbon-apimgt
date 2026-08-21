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

package org.wso2.carbon.apimgt.governance.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Tests how the severities configured against a ruleset are resolved, and how rule violations are filtered by them.
 * <p>
 * Violations of a severity outside the configured set are still evaluated and reported, but must not fail a ruleset,
 * violate a policy or make an artifact non-compliant.
 */
public class ComplianceAffectingSeverityTest {

    private static final Set<RuleSeverity> ALL_SEVERITIES = EnumSet.allOf(RuleSeverity.class);

    /**
     * Build a rule violation carrying the given severity
     *
     * @param ruleName Name of the violated rule
     * @param severity Severity of the violated rule, may be null
     * @return RuleViolation
     */
    private RuleViolation violation(String ruleName, RuleSeverity severity) {

        RuleViolation ruleViolation = new RuleViolation();
        ruleViolation.setRuleName(ruleName);
        ruleViolation.setRulesetId("ruleset-1");
        ruleViolation.setSeverity(severity);
        return ruleViolation;
    }

    // Resolving the configured value

    @Test
    public void testEverySeverityAffectsComplianceWhenNotConfigured() {

        Assert.assertEquals("A ruleset with nothing configured must keep its existing behaviour",
                ALL_SEVERITIES, APIMGovernanceUtil.resolveComplianceAffectingSeverities(null));
    }

    @Test
    public void testEverySeverityAffectsComplianceWhenBlank() {

        Assert.assertEquals(ALL_SEVERITIES, APIMGovernanceUtil.resolveComplianceAffectingSeverities("   "));
        Assert.assertEquals(ALL_SEVERITIES, APIMGovernanceUtil.resolveComplianceAffectingSeverities(""));
    }

    @Test
    public void testConfiguredSeveritiesAreResolved() {

        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,WARN"));
    }

    @Test
    public void testConfiguredSeveritiesAreCaseInsensitiveAndTrimmed() {

        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                APIMGovernanceUtil.resolveComplianceAffectingSeverities(" error , Warn "));
    }

    @Test
    public void testUnknownSeverityTokensAreIgnored() {

        Assert.assertEquals("An unknown token must be skipped without discarding the valid ones",
                EnumSet.of(RuleSeverity.ERROR),
                APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,BLOCKER"));
    }

    @Test
    public void testEverySeverityAffectsComplianceWhenNoTokenIsValid() {

        Assert.assertEquals("Falling back to every severity keeps governance strict rather than silently lenient",
                ALL_SEVERITIES, APIMGovernanceUtil.resolveComplianceAffectingSeverities("BLOCKER,ERR0R"));
    }

    @Test
    public void testEmptyTokensAreIgnored() {

        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,,WARN,"));
        Assert.assertEquals(ALL_SEVERITIES, APIMGovernanceUtil.resolveComplianceAffectingSeverities(","));
    }

    @Test
    public void testResolvedSetIsNeverEmpty() {

        for (String configured : new String[]{null, "", "   ", ",", "UNKNOWN"}) {
            Assert.assertFalse("An empty set would mean nothing ever fails, which is the worst failure direction",
                    APIMGovernanceUtil.resolveComplianceAffectingSeverities(configured).isEmpty());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testResolvedSetIsImmutable() {

        APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR").add(RuleSeverity.INFO);
    }

    // Severity checks

    @Test
    public void testOnlyConfiguredSeveritiesAffectCompliance() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,WARN");
        Assert.assertTrue(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.ERROR, affecting));
        Assert.assertTrue(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.WARN, affecting));
        Assert.assertFalse(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.INFO, affecting));
    }

    @Test
    public void testUnresolvedSeverityAffectsCompliance() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR");
        Assert.assertTrue("A violation whose severity could not be resolved must not silently stop being enforced",
                APIMGovernanceUtil.isComplianceAffectingSeverity(null, affecting));
    }

    @Test
    public void testNullAffectingSetTreatsEverySeverityAsAffecting() {

        Assert.assertTrue(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.INFO, null));
    }

    // Violation filtering

    @Test
    public void testInfoViolationsAloneDoNotAffectCompliance() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,WARN");
        List<RuleViolation> ruleViolations = Arrays.asList(
                violation("api-description-check", RuleSeverity.INFO),
                violation("api-contact-check", RuleSeverity.INFO));

        Assert.assertTrue("A ruleset failing only info level rules must pass",
                APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations, affecting).isEmpty());
    }

    @Test
    public void testErrorViolationStillAffectsComplianceAlongsideInfoViolations() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,WARN");
        List<RuleViolation> ruleViolations = Arrays.asList(
                violation("api-version-prefix", RuleSeverity.ERROR),
                violation("api-description-check", RuleSeverity.INFO));

        List<RuleViolation> affectingViolations =
                APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations, affecting);

        Assert.assertEquals(1, affectingViolations.size());
        Assert.assertEquals("api-version-prefix", affectingViolations.get(0).getRuleName());
    }

    @Test
    public void testInfoViolationsAffectComplianceWhenNotConfigured() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities(null);
        List<RuleViolation> ruleViolations =
                Collections.singletonList(violation("api-description-check", RuleSeverity.INFO));

        Assert.assertEquals("Without a configured value an info violation must keep failing the ruleset",
                1, APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations, affecting).size());
    }

    @Test
    public void testFilteringDoesNotModifyTheGivenViolations() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR");
        List<RuleViolation> ruleViolations = Arrays.asList(
                violation("api-version-prefix", RuleSeverity.ERROR),
                violation("api-description-check", RuleSeverity.INFO));

        APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations, affecting);

        Assert.assertEquals("Every violation must still be reported to the user", 2, ruleViolations.size());
    }

    @Test
    public void testFilteringHandlesEmptyAndNullViolations() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR,WARN");
        Assert.assertTrue(APIMGovernanceUtil.filterComplianceAffectingViolations(null, affecting).isEmpty());
        Assert.assertTrue(APIMGovernanceUtil
                .filterComplianceAffectingViolations(Collections.emptyList(), affecting).isEmpty());
    }

    @Test
    public void testViolationWithUnresolvedSeverityIsKept() {

        Set<RuleSeverity> affecting = APIMGovernanceUtil.resolveComplianceAffectingSeverities("ERROR");
        List<RuleViolation> ruleViolations =
                Collections.singletonList(violation("malformed-severity-rule", null));

        Assert.assertEquals(1,
                APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations, affecting).size());
    }
}
