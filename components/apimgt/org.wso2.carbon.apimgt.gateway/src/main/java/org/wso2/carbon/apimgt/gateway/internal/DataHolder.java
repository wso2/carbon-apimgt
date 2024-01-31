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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.gateway.webhooks.SubscriptionDataStore;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataLoader;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;

import java.util.*;

public class DataHolder {
    private static final Log log  = LogFactory.getLog(DataHolder.class);
    private static final DataHolder Instance = new DataHolder();
    private Map<String, List<String>> apiToCertificatesMap = new HashMap();
    private Map<String, String> googleAnalyticsConfigMap = new HashMap<>();
    private Map<String, GraphQLSchemaDTO> apiToGraphQLSchemaDTOMap = new HashMap<>();
    private Map<String, List<String>> apiToKeyManagersMap = new HashMap<>();
    private Map<String,Map<String, API>> tenantAPIMap  = new HashMap<>();
    private boolean isAllApisDeployed = false;
    private boolean isAllGatewayPoliciesDeployed = false;

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

    public boolean isAllGatewayPoliciesDeployed() {
        return isAllGatewayPoliciesDeployed;
    }

    public void setAllGatewayPoliciesDeployed(boolean allGatewayPoliciesDeployed) {
        isAllGatewayPoliciesDeployed = allGatewayPoliciesDeployed;
    }

    public void addAPIMetaData(API api) {
        if (log.isDebugEnabled()) {
            log.debug("Adding meta data of API : " + api.getApiName());
        }
        String context = api.getContext();
        String defaultContext = context;
        if (!api.getApiType().equals("APIProduct")) {
            int index = context.lastIndexOf("/" + api.getApiVersion());
            defaultContext = context.substring(0, index);
        }
        Map<String, API> apiMap;
        if (tenantAPIMap.containsKey(api.getOrganization())) {
            apiMap = tenantAPIMap.get(api.getOrganization());
        } else {
            apiMap = new HashMap<>();
        }
        API oldAPI = apiMap.get(api.getContext());
        if (oldAPI != null) {
            apiMap.remove(api.getContext());
            if (oldAPI.isDefaultVersion()) {
                apiMap.remove(defaultContext);
            }
        }
        apiMap.put(api.getContext(), api);
        if (api.isDefaultVersion()) {
            apiMap.put(defaultContext, api);
        }
        tenantAPIMap.put(api.getOrganization(), apiMap);
    }

    public void markAPIAsDeployed(GatewayAPIDTO gatewayAPIDTO) {
        Map<String, API> apiMap = tenantAPIMap.get(gatewayAPIDTO.getTenantDomain());
        if (apiMap != null) {
            API api = apiMap.get(gatewayAPIDTO.getApiContext());
            if (api != null) {
                api.setDeployed(true);
                if (log.isDebugEnabled()) {
                    log.debug("API : " + api.getApiName() + "is deployed successfully");
                }
                api.setRevisionId(gatewayAPIDTO.getRevision());
            }
        }
    }

    public Map<String, Map<String, API>> getTenantAPIMap() {
        return tenantAPIMap;
    }

    public void removeAPIFromAllTenantMap(String apiContext, String tenantDomain) {
        Map<String, API> apiMap = tenantAPIMap.get(tenantDomain);
        if (apiMap != null) {
            API api = apiMap.get(apiContext);
            if (api != null) {
                apiMap.remove(apiContext);
                if (api.isDefaultVersion()) {
                    if (api.isDefaultVersion()) {
                        String context = api.getContext();
                        int index = context.lastIndexOf("/" + api.getApiVersion());
                        apiMap.remove(context.substring(0, index));
                    }

                }
            }
        }
    }

    public void addAPIMetaData(DeployAPIInGatewayEvent gatewayEvent) {
        SubscriptionDataLoader subscriptionDataLoader = new SubscriptionDataLoaderImpl();
        try {
            API api = subscriptionDataLoader.getApi(gatewayEvent.getContext(), gatewayEvent.getVersion());
            if (api != null){
                addAPIMetaData(api);
            }
        } catch (DataLoadingException e) {
            log.error("Error while loading API Metadata", e);
        }
    }

    public void addAPIMetaData(APIEvent event) {
        SubscriptionDataLoader subscriptionDataLoader = new SubscriptionDataLoaderImpl();
        try {
            API api = subscriptionDataLoader.getApi(event.getApiContext(), event.getApiVersion());
            if (api != null){
                addAPIMetaData(api);
            }
        } catch (DataLoadingException e) {
            log.error("Error while loading API Metadata", e);
        }
    }

    public void markApisAsUnDeployedInTenant(String tenantDomain) {
        if (tenantAPIMap.containsKey(tenantDomain)) {
            Map<String, API> apiMap = tenantAPIMap.get(tenantDomain);
            apiMap.values().forEach(api -> api.setDeployed(false));
        }
    }
}
