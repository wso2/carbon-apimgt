/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.impl.definitions;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.io.IOException;

/**
 * @deprecated use org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition instead
 */
@Deprecated
public class GraphQLSchemaDefinition extends org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition {

    /**
     * Returns the graphQL content in registry specified by the wsdl name
     *
     * @param apiId Api Identifier
     * @return graphQL content matching name if exist else null
     */
    @Deprecated
    public String getGraphqlSchemaDefinition(APIIdentifier apiId, Registry registry) throws APIManagementException {
        String apiName = apiId.getApiName();
        String apiVersion = apiId.getVersion();
        String apiProviderName = apiId.getProviderName();
        APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId.getUUID());
        String resourcePath;
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            resourcePath = APIUtil.getRevisionPath(apiRevision.getApiUUID(), apiRevision.getId());
        } else {
            resourcePath = APIUtil.getGraphqlDefinitionFilePath(apiName, apiVersion, apiProviderName);
        }

        String schemaDoc = null;
        String schemaName = apiId.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                apiId.getApiName() + apiId.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
        String schemaResourePath = resourcePath + schemaName;
        try {
            if (registry.resourceExists(schemaResourePath)) {
                Resource schemaResource = registry.get(schemaResourePath);
                schemaDoc = IOUtils.toString(schemaResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
            }
        } catch (RegistryException e) {
            String msg = "Error while getting schema file from the registry " + schemaResourePath;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (IOException e) {
            String error = "Error occurred while getting the content of schema: " + schemaName;
            log.error(error);
            throw new APIManagementException(error, e);
        }
        return schemaDoc;
    }
}
