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
package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import javax.xml.stream.XMLStreamException;

/**
 * Tests how the per-policy severity filtering setting is read out of the governance configuration.
 * <p>
 * Enabling the feature is one half of a deliberate opt-in, so the setting has to be off for every deployment that
 * has not asked for it. That includes an upgraded deployment whose api-manager.xml predates the element entirely,
 * which is the case these tests exist to protect.
 */
public class APIMGovernanceSeverityConfigTest {

    /**
     * Read the setting after parsing a governance configuration element
     *
     * @param governanceConfig APIMGovernance XML element
     * @return True when the parsed configuration enables per-policy severity filtering
     * @throws XMLStreamException If the XML cannot be parsed
     */
    private boolean parse(String governanceConfig) throws XMLStreamException {

        APIManagerConfiguration config = new APIManagerConfiguration();
        config.setAPIMGovernanceConfigurations(AXIOMUtil.stringToOM(governanceConfig));
        return config.getAPIMGovernanceConfigurationDto().isPerPolicySeverityFilteringEnabled();
    }

    @Before
    public void resetTheSetting() {

        // The configuration DTO is shared statically across every APIManagerConfiguration, so a value left behind
        // by an earlier test would otherwise decide the next one.
        new APIManagerConfiguration().getAPIMGovernanceConfigurationDto()
                .setPerPolicySeverityFilteringEnabled(false);
    }

    @Test
    public void testTheFeatureIsOffOnAFreshConfiguration() {

        Assert.assertFalse(new APIMGovernanceConfigDTO().isPerPolicySeverityFilteringEnabled(),
                "The feature must default to off, so that adding the optional column alone never changes how a "
                        + "deployment judges compliance");
    }

    @Test
    public void testTheFeatureStaysOffWhenTheElementIsAbsent() throws XMLStreamException {

        // An api-manager.xml written before this feature existed has no such element at all. Upgrading must not
        // change the compliance posture of that deployment.
        Assert.assertFalse(parse("<APIMGovernance><DataSourceName>jdbc/WSO2AM_DB</DataSourceName></APIMGovernance>"),
                "A configuration which predates the element must leave the feature off");
    }

    @Test
    public void testTheFeatureIsOnWhenTheElementIsTrue() throws XMLStreamException {

        Assert.assertTrue(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>true"
                        + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"),
                "Asking for the feature must enable the configuration half of the opt-in");
    }

    @Test
    public void testTheElementIsReadWithoutRegardToCase() throws XMLStreamException {

        Assert.assertTrue(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>TRUE"
                        + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"),
                "The value must be read the way every other boolean in this file is read");
    }

    @Test
    public void testAnythingOtherThanTrueLeavesTheFeatureOff() throws XMLStreamException {

        // The value is parsed strictly, so yes, on and 1 all leave the feature off. That is the safe direction,
        // and it is asserted here so the strictness is a documented decision rather than a surprise in support.
        for (String value : new String[]{"yes", "on", "1", "false", "", "  "}) {
            Assert.assertFalse(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>" + value
                            + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"),
                    "Only true may enable the feature, but '" + value + "' did");
        }
    }

    @Test
    public void testAConfigurationWithoutTheElementTurnsAPreviouslyEnabledFlagOff() throws Exception {

        // The governance configuration DTO is shared statically across every APIManagerConfiguration, so a parse
        // that only wrote the flag when the element was present would leave an earlier true standing. Parsing has
        // to describe the file it just read, not the last one.
        Assert.assertTrue(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>true"
                + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"));

        Assert.assertFalse(parse("<APIMGovernance><DataSourceName>jdbc/WSO2AM_DB</DataSourceName></APIMGovernance>"),
                "A configuration without the element must leave the feature off, even after an earlier "
                        + "configuration enabled it");
    }

    @Test
    public void testTheFlagFollowsTheLastConfigurationParsed() throws Exception {

        Assert.assertTrue(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>true"
                + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"));
        Assert.assertFalse(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>false"
                + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"));
        Assert.assertTrue(parse("<APIMGovernance><PerPolicySeverityFilteringEnabled>true"
                        + "</PerPolicySeverityFilteringEnabled></APIMGovernance>"),
                "Parsing must be repeatable in both directions");
    }
}
