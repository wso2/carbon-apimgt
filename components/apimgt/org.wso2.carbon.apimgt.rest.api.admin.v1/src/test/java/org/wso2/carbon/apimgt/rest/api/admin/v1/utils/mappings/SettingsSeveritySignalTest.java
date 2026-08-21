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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

/**
 * Tests the capability signal the admin portal reads before any policy exists.
 * <p>
 * The tri-state {@code complianceAffectingSeverities} field of a policy payload tells a client whether to offer the
 * severity control, but the policy create form has no policy to read it from. That form relies on this flag in the
 * settings payload instead. If it reports true on a deployment which cannot store severities, an administrator is
 * offered a control whose every save is rejected; if it reports false on one which can, a working feature is
 * invisible.
 */
public class SettingsSeveritySignalTest {

    /**
     * Read the flag the settings payload carries, with the given governance configuration in place
     *
     * @param governanceConfig Governance configuration to report, null when the configuration holds none
     * @return Value of the flag
     * @throws Exception If the helper cannot be invoked
     */
    private boolean signalWith(APIMGovernanceConfigDTO governanceConfig) throws Exception {

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
        return Whitebox.invokeMethod(SettingsMappingUtil.class, "isPerPolicySeverityFilteringEnabled");
    }

    /**
     * Build a governance configuration with the feature in the given state
     *
     * @param enabled Whether per policy severity filtering is enabled
     * @return Governance configuration
     */
    private APIMGovernanceConfigDTO governanceConfig(boolean enabled) {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setPerPolicySeverityFilteringEnabled(enabled);
        return governanceConfig;
    }

    @Test
    public void testTheSignalIsOnWhenTheConfigurationEnablesTheFeature() throws Exception {

        Assert.assertTrue("A deployment which has enabled the feature must advertise it, so the create form offers "
                + "the control", signalWith(governanceConfig(true)));
    }

    @Test
    public void testTheSignalIsOffWhenTheConfigurationDisablesTheFeature() throws Exception {

        Assert.assertFalse("A deployment which has not enabled the feature must not advertise it",
                signalWith(governanceConfig(false)));
    }

    @Test
    public void testTheSignalIsOffWhenTheConfigurationHoldsNoGovernanceSection() throws Exception {

        // An api-manager.xml written before governance configuration existed reports no section at all, and the
        // flag has to survive that rather than throwing while the settings payload is being built.
        Assert.assertFalse("A configuration with no governance section must read as the feature being off",
                signalWith(null));
    }

    @Test
    public void testTheSignalIsOffWhenTheConfigurationServiceIsUnavailable() throws Exception {

        // The settings endpoint is reachable during startup, before the configuration service is registered.
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);

        Assert.assertFalse("An unavailable configuration service must read as the feature being off",
                (boolean) Whitebox.invokeMethod(SettingsMappingUtil.class,
                        "isPerPolicySeverityFilteringEnabled"));
    }

    @Test
    public void testTheSignalIsOffWhenTheConfigurationItselfIsUnavailable() throws Exception {

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(null);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);

        Assert.assertFalse("A registered service holding no configuration must read as the feature being off",
                (boolean) Whitebox.invokeMethod(SettingsMappingUtil.class,
                        "isPerPolicySeverityFilteringEnabled"));
    }
}
