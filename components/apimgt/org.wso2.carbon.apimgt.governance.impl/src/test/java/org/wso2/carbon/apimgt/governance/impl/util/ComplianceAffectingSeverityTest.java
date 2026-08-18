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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.governance.api.model.RuleSeverity;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Tests the resolution of compliance affecting rule severities and the filtering of rule violations based on it.
 * <p>
 * Rules of a severity that does not affect compliance are still evaluated and reported, but they must not fail a
 * ruleset, violate a policy or make an artifact non-compliant.
 */
public class ComplianceAffectingSeverityTest {

    private static final Set<RuleSeverity> ALL_SEVERITIES = EnumSet.allOf(RuleSeverity.class);

    /**
     * Point the governance component at a configuration holding the given compliance affecting severities
     *
     * @param configuredSeverities Value as it would be read from api-manager.xml, may be null
     */
    private void configureSeverities(String configuredSeverities) {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setComplianceAffectingSeverities(configuredSeverities);

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

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

    @After
    public void resetConfiguration() throws Exception {

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
        setCachedConfig(null);
    }

    /**
     * Overwrite the cached raw configuration value so each test resolves the configuration from scratch
     *
     * @param value Raw configuration value to seed the cache with, may be null to clear it
     * @throws Exception If the field cannot be accessed
     */
    private void setCachedConfig(String value) throws Exception {

        Field cachedConfig = APIMGovernanceUtil.class.getDeclaredField("resolvedSeverityConfig");
        cachedConfig.setAccessible(true);
        cachedConfig.set(null, value);
    }

    /**
     * Read the cached raw configuration value
     *
     * @return Cached configuration value, null when nothing has been cached
     * @throws Exception If the field cannot be accessed
     */
    private String getCachedConfig() throws Exception {

        Field cachedConfig = APIMGovernanceUtil.class.getDeclaredField("resolvedSeverityConfig");
        cachedConfig.setAccessible(true);
        return (String) cachedConfig.get(null);
    }

    // Configuration resolution

    @Test
    public void testEverySeverityAffectsComplianceWhenNotConfigured() {

        configureSeverities(null);
        Assert.assertEquals("An unconfigured deployment must keep its existing behaviour",
                ALL_SEVERITIES, EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testEverySeverityAffectsComplianceWhenConfigurationIsBlank() {

        configureSeverities("   ");
        Assert.assertEquals(ALL_SEVERITIES,
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testEverySeverityAffectsComplianceWhenConfigurationServiceIsUnavailable() {

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
        Assert.assertEquals(ALL_SEVERITIES,
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testConfiguredSeveritiesAreResolved() {

        configureSeverities("ERROR,WARN");
        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testConfiguredSeveritiesAreCaseInsensitiveAndTrimmed() {

        configureSeverities(" error , Warn ");
        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testUnknownSeverityTokensAreIgnored() {

        configureSeverities("ERROR,BLOCKER");
        Assert.assertEquals("An unknown token must be skipped without discarding the valid ones",
                EnumSet.of(RuleSeverity.ERROR),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testEverySeverityAffectsComplianceWhenNoTokenIsValid() {

        configureSeverities("BLOCKER,ERR0R");
        Assert.assertEquals("Falling back to every severity keeps governance strict rather than silently lenient",
                ALL_SEVERITIES, EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testEmptyTokensAreIgnored() {

        configureSeverities("ERROR,,WARN,");
        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    @Test
    public void testSeverityListIsNeverEmpty() {

        for (String configuredSeverities : new String[]{null, "", "   ", ",", "UNKNOWN"}) {
            configureSeverities(configuredSeverities);
            Assert.assertFalse("A query filtered by severity always needs at least one severity to bind",
                    APIMGovernanceUtil.getComplianceAffectingSeverityList().isEmpty());
        }
    }

    @Test
    public void testSeverityListIsADefensiveCopy() {

        configureSeverities("ERROR,WARN");
        List<RuleSeverity> severities = APIMGovernanceUtil.getComplianceAffectingSeverityList();
        severities.clear();
        Assert.assertEquals("Mutating the returned list must not affect the cached configuration",
                EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }

    // Severity checks

    @Test
    public void testOnlyConfiguredSeveritiesAffectCompliance() {

        configureSeverities("ERROR,WARN");
        Assert.assertTrue(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.ERROR));
        Assert.assertTrue(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.WARN));
        Assert.assertFalse(APIMGovernanceUtil.isComplianceAffectingSeverity(RuleSeverity.INFO));
    }

    @Test
    public void testUnresolvedSeverityAffectsCompliance() {

        configureSeverities("ERROR");
        Assert.assertTrue("A violation whose severity could not be resolved must not silently stop being enforced",
                APIMGovernanceUtil.isComplianceAffectingSeverity(null));
    }

    // Violation filtering

    @Test
    public void testInfoViolationsAloneDoNotAffectCompliance() {

        configureSeverities("ERROR,WARN");
        List<RuleViolation> ruleViolations = Arrays.asList(
                violation("api-description-check", RuleSeverity.INFO),
                violation("api-contact-check", RuleSeverity.INFO));

        Assert.assertTrue("A ruleset failing only info level rules must pass",
                APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations).isEmpty());
    }

    @Test
    public void testErrorViolationStillAffectsComplianceAlongsideInfoViolations() {

        configureSeverities("ERROR,WARN");
        List<RuleViolation> ruleViolations = Arrays.asList(
                violation("api-version-prefix", RuleSeverity.ERROR),
                violation("api-description-check", RuleSeverity.INFO));

        List<RuleViolation> complianceAffecting =
                APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations);

        Assert.assertEquals(1, complianceAffecting.size());
        Assert.assertEquals("api-version-prefix", complianceAffecting.get(0).getRuleName());
    }

    @Test
    public void testWarnViolationAffectsCompliance() {

        configureSeverities("ERROR,WARN");
        List<RuleViolation> ruleViolations =
                Collections.singletonList(violation("api-description-length", RuleSeverity.WARN));

        Assert.assertEquals(1, APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations).size());
    }

    @Test
    public void testInfoViolationsAffectComplianceWhenNotConfigured() {

        configureSeverities(null);
        List<RuleViolation> ruleViolations =
                Collections.singletonList(violation("api-description-check", RuleSeverity.INFO));

        Assert.assertEquals("Without the configuration an info violation must keep failing the ruleset",
                1, APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations).size());
    }

    @Test
    public void testFilteringDoesNotModifyTheGivenViolations() {

        configureSeverities("ERROR");
        List<RuleViolation> ruleViolations = new ArrayList<>(Arrays.asList(
                violation("api-version-prefix", RuleSeverity.ERROR),
                violation("api-description-check", RuleSeverity.INFO)));

        APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations);

        Assert.assertEquals("Every violation must still be reported to the user", 2, ruleViolations.size());
    }

    @Test
    public void testFilteringHandlesEmptyAndNullViolations() {

        configureSeverities("ERROR,WARN");
        Assert.assertTrue(APIMGovernanceUtil.filterComplianceAffectingViolations(null).isEmpty());
        Assert.assertTrue(APIMGovernanceUtil
                .filterComplianceAffectingViolations(Collections.emptyList()).isEmpty());
    }

    @Test
    public void testViolationWithUnresolvedSeverityIsKept() {

        configureSeverities("ERROR");
        List<RuleViolation> ruleViolations =
                Collections.singletonList(violation("malformed-severity-rule", null));

        Assert.assertEquals(1, APIMGovernanceUtil.filterComplianceAffectingViolations(ruleViolations).size());
    }

    @Test
    public void testFallbackForAnEntirelyInvalidConfigurationIsCached() throws Exception {

        configureSeverities("NOPE,ALSO_NOPE");
        Assert.assertEquals(ALL_SEVERITIES,
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));

        Assert.assertEquals("The fallback must be cached, otherwise the invalid value is re-parsed and the warning "
                        + "re-logged on every call",
                "NOPE,ALSO_NOPE", getCachedConfig());
    }

    @Test
    public void testCacheIsInvalidatedWhenTheConfigurationChanges() throws Exception {

        configureSeverities("ERROR,WARN");
        Assert.assertEquals(EnumSet.of(RuleSeverity.ERROR, RuleSeverity.WARN),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));

        configureSeverities("ERROR");
        Assert.assertEquals("A changed configuration must not keep serving the previously cached value",
                EnumSet.of(RuleSeverity.ERROR),
                EnumSet.copyOf(APIMGovernanceUtil.getComplianceAffectingSeverityList()));
    }
}
