/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.handlers;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

/**
 * This is the test class for {@link CustomAPIIndexHandler}.
 */
public class CustomAPIIndexHandlerTest {

    /**
     * This method tests whether the CustomAPIIndexer works correctly under different circumstances without throwing
     * Exception.
     *
     * @throws RegistryException Registry Exception.
     */
    @Test
    public void testPut() throws RegistryException {
        // Resource without property.
        Resource resource = new ResourceImpl();
        RequestContext requestContext = Mockito.mock(RequestContext.class);
        Mockito.doReturn(Mockito.mock(Registry.class)).when(requestContext).getRegistry();
        ResourcePath resourcePath = Mockito.mock(ResourcePath.class);
        Mockito.doReturn(resource).when(requestContext).getResource();
        Mockito.doReturn(resourcePath).when(requestContext).getResourcePath();
        CustomAPIIndexHandler customAPIIndexHandler = new CustomAPIIndexHandler();
        customAPIIndexHandler.put(requestContext);

        // Resource with property.
        resource.setProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY, "true");
        Mockito.doReturn(resource).when(requestContext).getResource();
        customAPIIndexHandler.put(requestContext);
    }
}