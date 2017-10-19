/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.keymgt.internal;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.keymgt.issuers.AbstractScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;


public class APIKeyMgtServiceComponentTest {

    @Test
    public void testSetUnsetRegistryService() throws Exception {

        RegistryService registryService = Mockito.mock(RegistryService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setRegistryService(registryService);
        Assert.assertEquals(registryService, APIKeyMgtDataHolder.getRegistryService());

        apiKeyMgtServiceComponent.unsetRegistryService(registryService);
        Assert.assertEquals(null, APIKeyMgtDataHolder.getRegistryService());
    }

    @Test
    public void testSetUnsetRealmService() throws Exception {

        RealmService realmService = Mockito.mock(RealmService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setRealmService(realmService);
        Assert.assertEquals(realmService, APIKeyMgtDataHolder.getRealmService());

        apiKeyMgtServiceComponent.unsetRealmService(realmService);
        Assert.assertEquals(null, APIKeyMgtDataHolder.getRealmService());
    }

    @Test
    public void testSetUnsetAPIManagerConfigurationService() throws Exception {

        APIManagerConfigurationService amConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.setAPIManagerConfigurationService(amConfigurationService);
        Assert.assertEquals(amConfigurationService, APIKeyMgtDataHolder.getAmConfigService());
        Assert.assertEquals(amConfigurationService, ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService());

        apiKeyMgtServiceComponent.unsetAPIManagerConfigurationService(amConfigurationService);
        Assert.assertEquals(null, APIKeyMgtDataHolder.getAmConfigService());
        Assert.assertEquals(null, ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService());
    }

    @Test
    public void testAddRemoveScopeIssuer() throws Exception {

        AbstractScopesIssuer abstractScopesIssuer = Mockito.mock(AbstractScopesIssuer.class);
        APIKeyMgtServiceComponent apiKeyMgtServiceComponent = new APIKeyMgtServiceComponent();
        apiKeyMgtServiceComponent.addScopeIssuer(abstractScopesIssuer);
        Assert.assertEquals(abstractScopesIssuer, APIKeyMgtDataHolder.getScopesIssuers()
                .get(abstractScopesIssuer.getPrefix()));

        apiKeyMgtServiceComponent.removeScopeIssuers(abstractScopesIssuer);
        Assert.assertEquals(null, APIKeyMgtDataHolder.getScopesIssuers());
    }

}