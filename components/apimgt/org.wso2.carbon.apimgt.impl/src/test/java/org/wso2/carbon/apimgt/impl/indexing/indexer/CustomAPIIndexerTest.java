/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;

/**
 * This is the test case related with {@link CustomAPIIndexer}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GovernanceUtils.class, IndexingManager.class, APIUtil.class})
public class CustomAPIIndexerTest {
    private CustomAPIIndexer indexer;
    private AsyncIndexer.File2Index file2Index;
    private UserRegistry userRegistry;

    @Before
    public void init() throws RegistryException {
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(IndexingManager.class);
        IndexingManager indexingManager = Mockito.mock(IndexingManager.class);
        PowerMockito.when(IndexingManager.getInstance()).thenReturn(indexingManager);
        userRegistry = Mockito.mock(UserRegistry.class);
        Mockito.doReturn(userRegistry).when(indexingManager).getRegistry(Mockito.anyInt());
        Mockito.doReturn(true).when(userRegistry).resourceExists(Mockito.anyString());
        PowerMockito.when(GovernanceUtils.getGovernanceSystemRegistry(userRegistry)).thenReturn(userRegistry);
        String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/api";
        file2Index = new AsyncIndexer.File2Index("".getBytes(), null, path, -1234, "");
        indexer = new CustomAPIIndexer();
    }

    /**
     * This method checks the indexer's behaviour for APIs which does not have the relevant custom properties.
     *
     * @throws RegistryException Registry Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testIndexingCustomProperties() throws RegistryException, APIManagementException {
        Resource resource = new ResourceImpl();
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.doReturn(resource).when(userRegistry).get(Mockito.anyString());
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY)).thenReturn("public");
        PowerMockito.when(APIUtil.getAPI(genericArtifact, userRegistry))
                .thenReturn(Mockito.mock(API.class));
        resource.setProperty(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX + APIConstants.
                CUSTOM_API_INDEXER_PROPERTY, APIConstants.CUSTOM_API_INDEXER_PROPERTY);
        Assert.assertEquals(APIConstants.OVERVIEW_PREFIX + APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX +
                APIConstants.CUSTOM_API_INDEXER_PROPERTY, indexer.getIndexedDocument(file2Index).getFields().keySet().
                toArray()[0].toString());
    }

    /**
     * This method checks the indexer's behaviour for new APIs which does not have the relevant properties.
     *
     * @throws RegistryException Registry Exception.
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testIndexDocumentForNewAPI() throws APIManagementException, RegistryException {
        Resource resource = new ResourceImpl();
        PowerMockito.mockStatic(APIUtil.class);
        GenericArtifactManager artifactManager = Mockito.mock(GenericArtifactManager.class);
        PowerMockito.when(APIUtil.getArtifactManager((UserRegistry)(Mockito.anyObject()), Mockito.anyString())).
                thenReturn(artifactManager);
        GenericArtifact genericArtifact = Mockito.mock(GenericArtifact.class);
        Mockito.when(artifactManager.getGenericArtifact(Mockito.anyString())).thenReturn(genericArtifact);
        Mockito.when(genericArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY)).thenReturn("public");
        PowerMockito.when(APIUtil.getAPI(genericArtifact, userRegistry))
                .thenReturn(Mockito.mock(API.class));
        resource.setProperty(APIConstants.ACCESS_CONTROL, APIConstants.NO_ACCESS_CONTROL);
        resource.setProperty(APIConstants.PUBLISHER_ROLES, APIConstants.NULL_USER_ROLE_LIST);
        resource.setProperty(APIConstants.STORE_VIEW_ROLES, APIConstants.NULL_USER_ROLE_LIST);
        Mockito.doReturn(resource).when(userRegistry).get(Mockito.anyString());
        indexer.getIndexedDocument(file2Index);
        Assert.assertNull(APIConstants.CUSTOM_API_INDEXER_PROPERTY + " property was set for the API which does not "
                + "require migration", resource.getProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY));
    }
}