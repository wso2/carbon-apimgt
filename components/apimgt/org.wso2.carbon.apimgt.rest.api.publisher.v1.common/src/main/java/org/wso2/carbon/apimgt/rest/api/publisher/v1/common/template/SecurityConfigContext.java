/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;

import java.util.HashMap;
import java.util.Map;

/**
 * Set the parameters for secured endpoints.
 */
public class SecurityConfigContext extends ConfigContextDecorator {

    private API api;
    private APIProduct apiProduct;
    private JSONObject productionEndpointSecurity;
    private JSONObject sandboxEndpointSecurity;
    private Map<String, APIDTO> associatedAPIMap;

    public SecurityConfigContext(ConfigContext context, API api) {

        super(context);
        this.api = api;
    }

    public SecurityConfigContext(ConfigContext context, APIProduct apiProduct,
                                 Map<String, APIDTO> associatedAPIMap) {

        super(context);
        this.apiProduct = apiProduct;
        this.associatedAPIMap = associatedAPIMap;
    }

    @Override
    public void validate() throws APITemplateException, APIManagementException {

        super.validate();
        if (api != null) {
            JSONParser parser = new JSONParser();
            //check if endpoint config exists
            String apiEndpointConfig = api.getEndpointConfig();

            if (StringUtils.isNotEmpty(apiEndpointConfig)) {
                try {
                    Object config = parser.parse(apiEndpointConfig);
                    JSONObject endpointConfig = (JSONObject) config;
                    if (endpointConfig.get(APIConstants.ENDPOINT_SECURITY) != null) {
                        JSONObject endpointSecurity = (JSONObject) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
                        if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                            productionEndpointSecurity =
                                    (JSONObject) endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION);
                        }
                        if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                            sandboxEndpointSecurity =
                                    (JSONObject) endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX);
                        }
                    }
                } catch (ParseException e) {
                    this.handleException("Unable to pass the endpoint JSON config");
                }
            }
        }
    }

    public VelocityContext getContext() {

        VelocityContext context = super.getContext();
        boolean isSecureVaultEnabled = Boolean.parseBoolean(getApiManagerConfiguration().
                getFirstProperty(APIConstants.API_SECUREVAULT_ENABLE));
        if (api != null) {
            Map<String, EndpointSecurityModel> endpointSecurityModelMap = new HashMap<>();
            endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, new EndpointSecurityModel());
            endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, new EndpointSecurityModel());

            if (StringUtils.isNotEmpty(api.getEndpointConfig())) {
                if (productionEndpointSecurity != null) {
                    EndpointSecurityModel endpointSecurityModel =
                            new ObjectMapper().convertValue(productionEndpointSecurity,
                                    EndpointSecurityModel.class);
                    endpointSecurityModel = retrieveEndpointSecurityModel(endpointSecurityModel,
                            api.getId().getApiName(), api.getId().getVersion(), api.getUuid(),
                            APIConstants.ENDPOINT_SECURITY_PRODUCTION, null);
                    if (endpointSecurityModel != null) {
                        endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION,
                                endpointSecurityModel);
                    }
                }

                if (sandboxEndpointSecurity != null) {
                    EndpointSecurityModel endpointSecurityModel =
                            new ObjectMapper().convertValue(sandboxEndpointSecurity,
                                    EndpointSecurityModel.class);
                    endpointSecurityModel = retrieveEndpointSecurityModel(endpointSecurityModel,
                            api.getId().getApiName(), api.getId().getVersion(), api.getUuid(),
                            APIConstants.ENDPOINT_SECURITY_SANDBOX, null);
                    if (endpointSecurityModel != null) {
                        endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX,
                                endpointSecurityModel);
                    }
                }
            }
            context.put("endpoint_security", endpointSecurityModelMap);
        } else if (apiProduct != null) {
            Map<String, Map<String, EndpointSecurityModel>> endpointSecurityModelMap = new HashMap<>();
            for (APIProductResource apiProductResource : apiProduct.getProductResources()) {
                APIDTO apidto = associatedAPIMap.get(apiProductResource.getApiId());
                String alias = apiProduct.getId().getName() + "--v" + apiProduct.getId().getVersion();

                Map<String, EndpointSecurityModel> stringEndpointSecurityModelMap = new HashMap<>();
                Map<String, EndpointSecurity> endpointSecurityMap = apiProductResource.getEndpointSecurityMap();
                for (Map.Entry<String, EndpointSecurity> endpointSecurityEntry : endpointSecurityMap.entrySet()) {
                    EndpointSecurityModel endpointSecurityModel =
                            new EndpointSecurityModel(endpointSecurityEntry.getValue());
                    endpointSecurityModel = retrieveEndpointSecurityModel(endpointSecurityModel, apidto.getName(),
                            apidto.getVersion(), apidto.getId(), endpointSecurityEntry.getKey(), alias);
                    stringEndpointSecurityModelMap.put(endpointSecurityEntry.getKey(), endpointSecurityModel);
                }
                endpointSecurityModelMap.put(apiProductResource.getApiId(), stringEndpointSecurityModelMap);
            }
            context.put("endpoint_security", endpointSecurityModelMap);
        }
        context.put("isSecureVaultEnabled", isSecureVaultEnabled);
        return context;
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    private EndpointSecurityModel retrieveEndpointSecurityModel(EndpointSecurityModel endpointSecurityModel,
                                                                String apiName, String version, String apiId,
                                                                String type, String prefix) {

        if (endpointSecurityModel != null && endpointSecurityModel.isEnabled()) {
            if (APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH
                    .equalsIgnoreCase(endpointSecurityModel.getType())) {
                if (StringUtils.isNotEmpty(prefix)) {
                    endpointSecurityModel.setUniqueIdentifier(prefix.concat("--")
                            .concat(GatewayUtils.retrieveUniqueIdentifier(apiId, type)));
                } else {
                    endpointSecurityModel.setUniqueIdentifier(GatewayUtils.retrieveUniqueIdentifier(apiId, type));
                }
                if (StringUtils.isNotEmpty(prefix)) {
                    endpointSecurityModel.setClientSecretAlias(prefix.concat("--")
                            .concat(GatewayUtils.retrieveOauthClientSecretAlias(apiName, version, type)));
                } else {
                    endpointSecurityModel.setClientSecretAlias(GatewayUtils.retrieveOauthClientSecretAlias(apiName,
                            version, type));
                }
                if (StringUtils.isNotEmpty(prefix)) {
                    endpointSecurityModel.setPasswordAlias(prefix.concat("--")
                            .concat(GatewayUtils.retrieveOAuthPasswordAlias(apiName, version, type)));
                } else {
                    endpointSecurityModel.setPasswordAlias(GatewayUtils.retrieveOAuthPasswordAlias(apiName,
                            version, type));
                }
            }
            if (StringUtils.isNotBlank(endpointSecurityModel.getUsername())
                    && StringUtils.isNotBlank(endpointSecurityModel.getPassword())) {
                endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(
                        endpointSecurityModel.getUsername().concat(":")
                                .concat(endpointSecurityModel.getPassword()).getBytes())));
            }
            if (StringUtils.isNotEmpty(prefix)) {
                endpointSecurityModel.setAlias(prefix.concat("--")
                        .concat(GatewayUtils.retrieveBasicAuthAlias(apiName, version, type)));
            } else {
                endpointSecurityModel.setAlias(GatewayUtils.retrieveBasicAuthAlias(apiName, version, type));
            }
            return endpointSecurityModel;
        }
        return null;
    }
}
