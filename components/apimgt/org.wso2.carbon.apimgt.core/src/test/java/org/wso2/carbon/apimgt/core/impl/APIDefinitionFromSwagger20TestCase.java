/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class APIDefinitionFromSwagger20TestCase {

    private static final String API_VIEW = "apim:api_view";
    private static final String API_CREATE = "apim:api_create";
    private static final String API_UPDATE = "apim:api_update";
    private static final String API_DELETE = "apim:api_delete";
    private static final String VIEW = "View API";
    private static final String CREATE = "Create API";
    private static final String UPDATE = "Update API";
    private static final String DELETE = "Delete API";

    @Test
    public void testGetScope() throws IOException, APIManagementException {
        Map<String, String> defMap = new HashMap<>();
        defMap.put(API_VIEW, VIEW);
        defMap.put(API_CREATE, CREATE);
        defMap.put(API_UPDATE, UPDATE);
        defMap.put(API_DELETE, DELETE);

        APIDefinitionFromSwagger20 apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
        String sampleApi = SampleTestObjectCreator.getSampleApiSwagger();
        Map<String, String> scopes = apiDefinitionFromSwagger20.getScope(sampleApi);
        Assert.assertEquals(scopes, defMap);
    }
}

