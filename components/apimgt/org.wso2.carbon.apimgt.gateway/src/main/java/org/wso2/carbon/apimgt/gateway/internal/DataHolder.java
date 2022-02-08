/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.internal;

import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHolder {

    private static final DataHolder Instance = new DataHolder();
    private Map<String, List<String>> apiToCertificatesMap = new HashMap();
    private Map<String, String> googleAnalyticsConfigMap = new HashMap<>();
    private Map<String, GraphQLSchemaDTO> apiToGraphQLSchemaDTOMap = new HashMap<>();
    private Map<String, List<String>> apiToKeyManagersMap = new HashMap<>();
    private boolean isAllApisDeployed = false;

    private DataHolder() {

    }

    public Map<String, List<String>> getApiToCertificatesMap() {

        return apiToCertificatesMap;
    }

    public void setApiToCertificatesMap(Map<String, List<String>> apiToCertificatesMap) {

        this.apiToCertificatesMap = apiToCertificatesMap;
    }

    public static DataHolder getInstance() {

        return Instance;
    }

    public void addApiToAliasList(String apiId, List<String> aliasList) {

        apiToCertificatesMap.put(apiId, aliasList);
    }

    public List<String> getCertificateAliasListForAPI(String apiId) {

        return apiToCertificatesMap.getOrDefault(apiId, Collections.emptyList());
    }

    public void addGoogleAnalyticsConfig(String tenantDomain, String config) {

        googleAnalyticsConfigMap.put(tenantDomain, config);
    }

    public void removeGoogleAnalyticsConfig(String tenantDomain) {

        googleAnalyticsConfigMap.remove(tenantDomain);
    }

    public String getGoogleAnalyticsConfig(String tenantDomain) {

        return googleAnalyticsConfigMap.get(tenantDomain);
    }

    public Map<String, GraphQLSchemaDTO> getApiToGraphQLSchemaDTOMap() {

        return apiToGraphQLSchemaDTOMap;
    }

    public GraphQLSchemaDTO getGraphQLSchemaDTOForAPI(String apiId) {

        return apiToGraphQLSchemaDTOMap.get(apiId);
    }

    public void addApiToGraphQLSchemaDTO(String apiId, GraphQLSchemaDTO graphQLSchemaDTO) {

        apiToGraphQLSchemaDTOMap.put(apiId, graphQLSchemaDTO);
    }

    public boolean isAllApisDeployed() {

        return isAllApisDeployed;
    }

    public void setAllApisDeployed(boolean allApisDeployed) {

        isAllApisDeployed = allApisDeployed;
    }

    public void addKeyManagerToAPIMapping(String uuid, List<String> keyManagers) {

        apiToKeyManagersMap.put(uuid, keyManagers);
    }
    public void removeKeyManagerToAPIMapping(String uuid) {

        apiToKeyManagersMap.remove(uuid);
    }
    public List<String> getKeyManagersFromUUID(String apiUUID) {

        return apiToKeyManagersMap.get(apiUUID);
    }
}
