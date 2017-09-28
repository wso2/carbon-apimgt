/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APIExecutorTestCase {

    private final String ARTIFACT_ID = "abc123";

    private RequestContext requestContext = Mockito.mock(RequestContext.class);
    private Resource resource = Mockito.mock(Resource.class);
    private GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
    private GenericArtifactManager genericArtifactManager = Mockito.mock(GenericArtifactManager.class);
    private UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
    private APIProvider apiProvider = Mockito.mock(APIProvider.class);
    private APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);
    private API api = Mockito.mock(API.class);

    @Before
    public void setup() throws Exception{
        Mockito.when(resource.getUUID()).thenReturn(ARTIFACT_ID);
        Mockito.when(requestContext.getResource()).thenReturn(resource);
        Mockito.when(genericArtifactManager.getGenericArtifact(ARTIFACT_ID)).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getLifecycleState()).thenReturn("CREATED");
        Mockito.when(api.getEndpointConfig()).thenReturn("http://foo.com/api");
        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(apiProvider.propergateAPIStatusChangeToGateways(apiIdentifier, APIStatus.PUBLISHED)).thenReturn(null);
        Mockito.when(apiProvider.updateAPIforStateChange(apiIdentifier, APIStatus.PUBLISHED, null)).thenReturn(true);
        Mockito.when(userRegistry.get("/apimgt/applicationdata/provider/john/pizza-shack/1.0.0/api")).thenReturn(resource);
        Mockito.when(api.getId().getProviderName()).thenReturn("john");
        Mockito.when(api.getId().getApiName()).thenReturn("pizza-shack");
        Mockito.when(api.getId().getVersion()).thenReturn("1.0.0");
    }

    @Test
    public void testExecute() throws Exception{

        Tier tier1 = new Tier("GOLD");
        Tier tier2 = new Tier("SILVER");
        Set<Tier> hashSet = new HashSet<Tier>();
        hashSet.add(tier1);
        hashSet.add(tier2);
        Mockito.when(api.getAvailableTiers()).thenReturn(hashSet);
        APIExecutor apiExecutor = new APIExecutorWrapper(genericArtifactManager, userRegistry, apiProvider, api);
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Assert.assertTrue(isExecuted);
    }

    @Test
    public void testExecuteWithDeprecated() throws Exception {
        Tier tier1 = new Tier("GOLD");
        Tier tier2 = new Tier("SILVER");
        Set<Tier> hashSet = new HashSet<Tier>();
        hashSet.add(tier1);
        hashSet.add(tier2);
        Mockito.when(api.getAvailableTiers()).thenReturn(hashSet);
        Mockito.when(genericArtifact.isLCItemChecked(0, APIConstants.API_LIFE_CYCLE)).thenReturn(true);
        List<API> apiList = new ArrayList<API>();
        APIIdentifier apiIdTemp = Mockito.mock(APIIdentifier.class);
        API apiTemp = new API(apiIdTemp);
        apiTemp.setStatus(APIStatus.PUBLISHED);
        apiList.add(apiTemp);
        Mockito.when(apiTemp.getId().getProviderName()).thenReturn("john");
        Mockito.when(apiTemp.getId().getApiName()).thenReturn("pizza-shack");
        Mockito.when(apiTemp.getId().getVersion()).thenReturn("1.0.0");
        Mockito.when(apiProvider.getAPIsByProvider("john")).thenReturn(apiList);
        APIExecutor apiExecutor = new APIExecutorWrapper(genericArtifactManager, userRegistry, apiProvider, api);
        boolean isExecuted = apiExecutor.execute(requestContext, "CREATED", "PUBLISHED");
        Mockito.verify(apiProvider, Mockito.times(1)).getAPIsByProvider("john");
        Assert.assertTrue(isExecuted);
    }

}
