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

package org.wso2.carbon.apimgt.impl.template;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set the parameters for secured endpoints
 */
public class SecurityConfigContext extends ConfigContextDecorator {

    private API api;
    private APIProduct apiProduct;
    private JSONObject productionEndpointSecurity;
    private JSONObject sandboxEndpointSecurity;

    public SecurityConfigContext(ConfigContext context, API api) {

        super(context);
        this.api = api;
    }

    public SecurityConfigContext(ConfigContext context, APIProduct apiProduct) {

        super(context);
        this.apiProduct = apiProduct;
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
            String alias = api.getId().getProviderName() + "--" + api.getId().getApiName()
                    + api.getId().getVersion();
            endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, new EndpointSecurityModel());
            endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, new EndpointSecurityModel());
            if (api.isEndpointSecured()) {
                EndpointSecurityModel endpointSecurityModel = new EndpointSecurityModel();
                endpointSecurityModel.setEnabled(true);
                endpointSecurityModel.setUsername(api.getEndpointUTUsername());
                endpointSecurityModel.setPassword(api.getEndpointUTPassword());
                if (api.isEndpointAuthDigest()) {
                    endpointSecurityModel.setType(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST);
                } else {
                    endpointSecurityModel.setType(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC);
                }
                endpointSecurityModel.setAlias(alias);
                String unpw = api.getEndpointUTUsername() + ":" + api.getEndpointUTPassword();
                endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(unpw.getBytes())));
                endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityModel);
                endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecurityModel);
            }
            if (StringUtils.isNotEmpty(api.getEndpointConfig())) {
                if (productionEndpointSecurity != null) {
                    EndpointSecurityModel endpointSecurityModel = new ObjectMapper()
                            .convertValue(productionEndpointSecurity, EndpointSecurityModel.class);
                    if (endpointSecurityModel != null) {
                        if (endpointSecurityModel.isEnabled()) {
                            if (StringUtils.isNotBlank(endpointSecurityModel.getUsername())
                                    && StringUtils.isNotBlank(endpointSecurityModel.getPassword())) {
                                endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(
                                        endpointSecurityModel.getUsername().concat(":")
                                                .concat(endpointSecurityModel.getPassword()).getBytes())));
                            }
                            endpointSecurityModel.setUniqueIdentifier(api.getId() + "-" + UUID.randomUUID().toString());
                            endpointSecurityModel.setAlias(
                                    alias.concat("--").concat(APIConstants.ENDPOINT_SECURITY_PRODUCTION));
                        }
                        endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityModel);
                    }
                }
                if (sandboxEndpointSecurity != null) {
                    EndpointSecurityModel endpointSecurityModel = new ObjectMapper()
                            .convertValue(sandboxEndpointSecurity, EndpointSecurityModel.class);
                    if (endpointSecurityModel != null) {
                        if (endpointSecurityModel.isEnabled()) {
                            if (StringUtils.isNotBlank(endpointSecurityModel.getUsername()) &&
                                    StringUtils.isNotBlank(endpointSecurityModel.getPassword())) {
                                endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(
                                        endpointSecurityModel.getUsername().concat(":")
                                                .concat(endpointSecurityModel.getPassword()).getBytes())));
                            }
                            endpointSecurityModel.setUniqueIdentifier(api.getId() + "-" + UUID.randomUUID().toString());
                            endpointSecurityModel.setAlias(
                                    alias.concat("--").concat(APIConstants.ENDPOINT_SECURITY_SANDBOX));
                        }
                        endpointSecurityModelMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecurityModel);
                    }
                }
            }
            context.put("endpoint_security", endpointSecurityModelMap);
        } else if (apiProduct != null) {
            Map<String, Map<String, EndpointSecurityModel>> endpointSecurityModelMap = new HashMap<>();
            for (APIProductResource apiProductResource : apiProduct.getProductResources()) {
                String alias = apiProductResource.getApiIdentifier().getProviderName() + "--" +
                        apiProductResource.getApiIdentifier().getApiName() +
                        apiProductResource.getApiIdentifier().getVersion();

                Map<String, EndpointSecurityModel> stringEndpointSecurityModelMap = new HashMap<>();
                Map<String, EndpointSecurity> endpointSecurityMap = apiProductResource.getEndpointSecurityMap();
                for (Map.Entry<String, EndpointSecurity> endpointSecurityEntry : endpointSecurityMap.entrySet()) {
                    EndpointSecurityModel endpointSecurityModel = new EndpointSecurityModel();
                    if (endpointSecurityEntry.getValue().isEnabled()) {
                        endpointSecurityModel.setEnabled(endpointSecurityEntry.getValue().isEnabled());
                        endpointSecurityModel.setUsername(endpointSecurityEntry.getValue().getUsername());
                        endpointSecurityModel.setPassword(endpointSecurityEntry.getValue().getPassword());
                        endpointSecurityModel.setType(endpointSecurityEntry.getValue().getType());
                        endpointSecurityModel
                                .setAdditionalProperties(endpointSecurityEntry.getValue().getAdditionalProperties());
                        if (StringUtils.isNotBlank(endpointSecurityModel.getUsername())
                                && StringUtils.isNotBlank(endpointSecurityModel.getPassword())) {
                            endpointSecurityModel.setBase64EncodedPassword(new String(Base64.encodeBase64(
                                    endpointSecurityModel.getUsername().concat(":")
                                            .concat(endpointSecurityModel.getPassword())
                                            .getBytes())));
                        }
                        endpointSecurityModel.setAlias(alias.concat("--").concat(endpointSecurityEntry.getKey()));

                        if (APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH.equalsIgnoreCase(endpointSecurityModel.getType())) {
                            endpointSecurityModel.setUniqueIdentifier(apiProduct.getId() + "-" + UUID.randomUUID().toString());
                            endpointSecurityModel.setGrantType(endpointSecurityEntry.getValue().getGrantType());
                            endpointSecurityModel.setTokenUrl(endpointSecurityEntry.getValue().getTokenUrl());
                            endpointSecurityModel.setClientId(endpointSecurityEntry.getValue().getClientId());
                            endpointSecurityModel.setClientSecret(endpointSecurityEntry.getValue().getClientSecret());
                            if (endpointSecurityEntry.getValue().getCustomParameters() != null) {
                                endpointSecurityModel.setCustomParameters(
                                        endpointSecurityEntry.getValue().getCustomParameters());
                            }
                        }
                    }
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
}
