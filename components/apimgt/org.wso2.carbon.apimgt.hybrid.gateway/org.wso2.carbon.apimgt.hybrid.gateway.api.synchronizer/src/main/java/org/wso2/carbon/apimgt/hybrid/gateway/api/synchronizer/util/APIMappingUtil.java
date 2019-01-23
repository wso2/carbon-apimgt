/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIEndpointSecurityDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.dto.APIMaxTpsDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.exceptions.APISynchronizationException;
import org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.internal.ServiceDataHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Util class for API creation
 */
public class APIMappingUtil {

    private static final Log log = LogFactory.getLog(APIMappingUtil.class);

    /**
     * Method to update an existing API
     *
     * @param body DTO model of new API to be created
     */
    public static void apisUpdate(APIDTO body) throws APISynchronizationException {
        try {
            APIManagerConfiguration config = ServiceDataHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String adminUsername = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
            String tenantDomain = MultitenantUtils.getTenantDomain(adminUsername);

            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(adminUsername);
            boolean isWSApi = APIDTO.TypeEnum.WS == body.getType();

            //Set admin as the API provider since admin is the only user in this tenant space
            API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, adminUsername);
            APIIdentifier apiId = apiToAdd.getId();

            // Delete the API if it already exists
            if (apiProvider.checkIfAPIExists(apiId)) {
                apiProvider.deleteAPI(apiId);
            }
            apiToAdd.setApiOwner(adminUsername);
            String initialState = apiToAdd.getStatus().toString();
            // PROTOTYPED APIs need to be created in CREATED state if they are to be deployed as prototypes
            // to the gateway (This is the same with PUBLISHED APIs; they are created in CREATED state before getting
            // published to the gateway)
            apiToAdd.setStatus(APIConstants.CREATED);

            // Adding the api
            apiProvider.addAPI(apiToAdd);
            if (!isWSApi) {
                apiProvider.saveSwagger20Definition(apiId, body.getApiDefinition());
            }
            log.info("Successfully created API " + apiId);
            // Publishing the API
            if (APIConstants.PUBLISHED.toString().equals(initialState)) {
                apiProvider.changeLifeCycleStatus(apiToAdd.getId(), "Publish");
                log.info("Successfully published API with identifier " + apiId);
            }
            if (APIConstants.PROTOTYPED.toString().equals(initialState)) {
                apiProvider.changeLifeCycleStatus(apiToAdd.getId(), "Deploy as a Prototype");
                log.info("Successfully published API with identifier " + apiId);
            }
        } catch (APIManagementException e) {
            String errorMessage = "An error occurred while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            throw new APISynchronizationException(errorMessage, e);
        } catch (FaultGatewaysException e) {
            String errorMessage = "An error occurred while publishing API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " to gateway.";
            throw new APISynchronizationException(errorMessage, e);
        }
    }

    /**
     * Method to convert an APIDTO object to an API
     *
     * @param dto      DTO model of the API
     * @param provider API provider
     */
    private static API fromDTOtoAPI(APIDTO dto, String provider) throws APIManagementException {
        APIDefinition apiDefinitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();
        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);

        // The provider name that is coming from the body is not honored for now.
        // Later we can use it by checking admin privileges of the user.
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        API model = new API(apiId);

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith("/" + APISynchronizationConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + APISynchronizationConstants.API_VERSION_PARAM, "");
        }

        context = context.startsWith("/") ? context : ("/" + context);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the plug-able version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        model.setContextTemplate(context);

        context = updateContextWithVersion(dto.getVersion(), originalContext, context);
        model.setContext(context);
        model.setDescription(dto.getDescription());
        model.setEndpointConfig(dto.getEndpointConfig());
        model.setWsdlUrl(dto.getWsdlUri());
        model.setType(dto.getType().toString());
        model.setThumbnailUrl(dto.getThumbnailUri());

        if (dto.getStatus() != null) {
            model.setStatus((dto.getStatus() != null) ? dto.getStatus().toUpperCase() : null);
        }
        model.setAsDefaultVersion(dto.getIsDefaultVersion());
        model.setResponseCache(dto.getResponseCaching());
        if (dto.getCacheTimeout() != null) {
            model.setCacheTimeout(dto.getCacheTimeout());
        } else {
            model.setCacheTimeout(APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

        if (dto.getApiDefinition() != null) {
            String apiSwaggerDefinition = dto.getApiDefinition();
            //URI Templates
            Set<URITemplate> uriTemplates = apiDefinitionFromOpenAPISpec.getURITemplates(model, apiSwaggerDefinition);
            model.setUriTemplates(uriTemplates);

            // scopes
            Set<Scope> scopes = apiDefinitionFromOpenAPISpec.getScopes(apiSwaggerDefinition);
            model.setScopes(scopes);

        }

        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getTiers();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);

        String transports = StringUtils.join(dto.getTransport(), ',');
        model.setTransports(transports);
        model.setVisibility(mapVisibilityFromDTOtoAPI(dto.getVisibility()));
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            model.setVisibleTenants(visibleTenants);
        }

        if (!StringUtils.isBlank(dto.getGatewayEnvironments())) {
            String gatewaysString = dto.getGatewayEnvironments();
            model.setEnvironments(APIUtil.extractEnvironmentsForAPI(gatewaysString));
        } else if (dto.getGatewayEnvironments() != null) {
            //this means the provided gatewayEnvironments is "" (empty)
            model.setEnvironments(APIUtil.extractEnvironmentsForAPI(APIConstants.API_GATEWAY_NONE));
        }
        APICorsConfigurationDTO apiCorsConfigurationDTO = dto.getCorsConfiguration();
        CORSConfiguration corsConfiguration;
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration =
                    new CORSConfiguration(apiCorsConfigurationDTO.getCorsConfigurationEnabled(),
                            apiCorsConfigurationDTO.getAccessControlAllowOrigins(),
                            apiCorsConfigurationDTO.getAccessControlAllowCredentials(),
                            apiCorsConfigurationDTO.getAccessControlAllowHeaders(),
                            apiCorsConfigurationDTO.getAccessControlAllowMethods());

        } else {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        model.setCorsConfiguration(corsConfiguration);
        setEndpointSecurityFromApiDTOToModel(dto, model);
        setMaxTpsFromApiDTOToModel(dto, model);

        return model;
    }

    /**
     * Method to set Endpoint Security From APIDTO To API Model
     *
     * @param dto DTO model of the API
     * @param api API
     */
    private static void setEndpointSecurityFromApiDTOToModel(APIDTO dto, API api) {
        APIEndpointSecurityDTO securityDTO = dto.getEndpointSecurity();
        if (dto.getEndpointSecurity() != null && securityDTO.getType() != null) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(securityDTO.getUsername());
            api.setEndpointUTPassword(securityDTO.getPassword());
            if (APIEndpointSecurityDTO.TypeEnum.digest.equals(securityDTO.getType())) {
                api.setEndpointAuthDigest(true);
            }
        }
    }

    /**
     * Method to set Max TPs From APIDTO To API Model
     *
     * @param dto DTO model of the API
     * @param api API
     */
    private static void setMaxTpsFromApiDTOToModel(APIDTO dto, API api) {
        APIMaxTpsDTO maxTpsDTO = dto.getMaxTps();
        if (maxTpsDTO != null) {
            if (maxTpsDTO.getProduction() != null) {
                api.setProductionMaxTps(maxTpsDTO.getProduction().toString());
            }
            if (maxTpsDTO.getSandbox() != null) {
                api.setSandboxMaxTps(maxTpsDTO.getSandbox().toString());
            }
        }
    }

    /**
     * Method to map Visibility APIDTO To API Model
     *
     * @param visibility visibility of the API in APIDTO model
     * @return API visibility
     */
    private static String mapVisibilityFromDTOtoAPI(APIDTO.VisibilityEnum visibility) {
        switch (visibility) {
            case PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
            case CONTROLLED:
                return APIConstants.API_CONTROLLED_VISIBILITY;
            default:
                return null;
        }
    }

    /**
     * Method to update API context with version
     *
     * @param version    API version
     * @param contextVal API context value
     * @param context    API context
     * @return API context with version
     */
    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace("/" + APISynchronizationConstants.API_VERSION_PARAM,
                    "");
        } else {
            context = context.replace(APISynchronizationConstants.API_VERSION_PARAM, version);
        }
        return context;
    }

    /**
     * Method to check and set API version parameters
     *
     * @param context API context
     * @return API context
     */
    private static String checkAndSetVersionParam(String context) {
        // This is to support the new plug-able version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(APISynchronizationConstants.API_VERSION_PARAM)) {
            if (!context.endsWith("/")) {
                context = context + "/";
            }
            context = context + APISynchronizationConstants.API_VERSION_PARAM;
        }
        return context;
    }
}
