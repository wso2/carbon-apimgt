/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.nio.charset.Charset;

public class AsyncApiUtil {

    private static final Log log = LogFactory.getLog(AsyncApiUtil.class);

    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry) throws APIManagementException {

        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                    apiIdentifier.getProviderName());
        } else if (apiIdentifier instanceof APIProductIdentifier) {
            resourcePath =
                    APIUtil.getAPIProductOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
        }

        String asyncApiSpec = null;
        String asyncApiSpecPath = resourcePath + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;
        try {
            if (registry.resourceExists(asyncApiSpecPath)) {
                Resource apiDocResource = registry.get(asyncApiSpecPath);
                asyncApiSpec = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Async API specification for " + apiIdentifier.getName() + '-' + apiIdentifier.getVersion());
                }
            }
        } catch (RegistryException e) {
            APIUtil.handleException("Error while retrieving Async API specification from " + asyncApiSpecPath, e);
        }
        return asyncApiSpec;
    }
}
