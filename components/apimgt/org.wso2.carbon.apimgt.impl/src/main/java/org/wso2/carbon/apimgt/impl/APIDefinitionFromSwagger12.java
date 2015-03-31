package org.wso2.carbon.apimgt.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIDefinitionFromSwagger12 extends APIDefinition {
    @Override
    public Set<URITemplate> getURITemplatesFromDefinition(APIIdentifier apiIdentifier, String resourceConfigsJSON, API api, APIProvider apiProvider) throws APIManagementException {
        JSONParser parser = new JSONParser();
        JSONObject resourceConfigs;
        JSONObject api_doc;
        Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
        boolean isTenantFlowStarted = false;

        try {
            resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            api_doc = (JSONObject) resourceConfigs.get("api_doc");
            String apiJSON = api_doc.toJSONString();

            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            //apiProvider.updateSwagger12Definition(apiId, APIConstants.API_DOC_1_2_RESOURCE_NAME, apiJSON);
            JSONArray resources = (JSONArray) resourceConfigs.get("resources");

            //Iterating each resourcePath config
            for (Object resource : resources) {
                JSONObject resourceConfig = (JSONObject) resource;
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

                Map<String, Environment> environments = config.getApiGatewayEnvironments();
                Environment environment = null;
                String endpoint = null;
                if (environments != null) {
                    Set<String> publishedEnvironments = api.getEnvironments();
                    if (publishedEnvironments.isEmpty() || publishedEnvironments.contains("none")) {
                        environment = environments.values()
                                .toArray(new Environment[environments.size()])[0];
                        String gatewayEndpoint = environment.getApiGatewayEndpoint();
                        if (gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    } else {
                        for (String environmentName : publishedEnvironments) {
                            // find environment that has hybrid type
                            if (APIConstants.GATEWAY_ENV_TYPE_HYBRID
                                    .equals(environments.get(environmentName).getType())) {
                                environment = environments.get(environmentName);
                                break;
                            }
                        }
                        //if not having any hybrid environment give 1st environment in api published list
                        if (environment == null) {
                            environment = environments.get(publishedEnvironments.toArray()[0]);
                        }
                        String gatewayEndpoint = environment.getApiGatewayEndpoint();
                        if (gatewayEndpoint != null && gatewayEndpoint.contains(",")) {
                            endpoint = gatewayEndpoint.split(",")[0];
                        } else {
                            endpoint = gatewayEndpoint;
                        }
                    }
                }
                String apiPath = APIUtil.getAPIPath(apiIdentifier);
                if (endpoint.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    endpoint.substring(0, endpoint.length() - 1);
                }
                // We do not need the version in the base path since with the context version strategy, the version is
                // embedded in the context
                String basePath = endpoint + api.getContext();
                resourceConfig.put("basePath", basePath);
                String resourceJSON = resourceConfig.toJSONString();

                String resourcePath = (String) resourceConfig.get("resourcePath");

                apiProvider.updateSwagger12Definition(apiIdentifier, resourcePath, resourceConfig.toJSONString());

                JSONArray resource_configs = (JSONArray) resourceConfig.get("apis");

                //Iterating each Sub resourcePath config
                int subResCount = 0;
                while (subResCount < resource_configs.size()) {
                    JSONObject subResource = (JSONObject) resource_configs.get(subResCount);
                    String uriTempVal = (String) subResource.get("path");
                    uriTempVal = uriTempVal.startsWith("/") ? uriTempVal : ("/" + uriTempVal);

                    JSONArray operations = (JSONArray) subResource.get("operations");
                    //Iterating each operation config
                    for (Object operation1 : operations) {
                        JSONObject operation = (JSONObject) operation1;
                        String httpVerb = (String) operation.get("method");
                        /* Right Now PATCH is not supported. Need to remove this check when PATCH is supported*/
                        if (!"PATCH".equals(httpVerb)) {
                            URITemplate template = new URITemplate();
                            Scope scope = APIUtil.findScopeByKey(getScopeFromDefinition(resourceConfigsJSON), (String) operation.get("scope"));

                            String authType = (String) operation.get("auth_type");
                            if (authType != null) {
                                if (authType.equals("Application & Application User")) {
                                    authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                                }
                                if (authType.equals("Application User")) {
                                    authType = "Application_User";
                                }
                            } else {
                                authType = APIConstants.AUTH_NO_AUTHENTICATION;
                            }
                            template.setThrottlingTier((String) operation.get("throttling_tier"));
                            template.setMediationScript((String) operation.get("mediation_script"));
                            template.setUriTemplate(uriTempVal);
                            template.setHTTPVerb(httpVerb);
                            template.setAuthType(authType);
                            template.setScope(scope);

                            uriTemplates.add(template);
                        }
                    }

                    subResCount++;
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }
        return uriTemplates;
    }

    @Override
    public Set<Scope> getScopeFromDefinition(String resourceConfigsJSON) throws APIManagementException {
        Set<Scope> scopeList = new LinkedHashSet<Scope>();
        JSONObject resourceConfigs;
        JSONParser parser = new JSONParser();
        try {
            resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            JSONObject api_doc = (JSONObject) resourceConfigs.get("api_doc");
            if (api_doc.get("authorizations") != null) {
                JSONObject authorizations = (JSONObject) api_doc.get("authorizations");
                if (authorizations.get("oauth2") != null) {
                    JSONObject oauth2 = (JSONObject) authorizations.get("oauth2");
                    if (oauth2.get("scopes") != null) {
                        JSONArray scopes = (JSONArray) oauth2.get("scopes");

                        if (scopes != null) {
                            for (Object scopeObj : scopes) {
                                Map scopeMap = (Map) scopeObj;
                                if (scopeMap.get("key") != null) {
                                    Scope scope = new Scope();
                                    scope.setKey((String) scopeMap.get("key"));
                                    scope.setName((String) scopeMap.get("name"));
                                    scope.setRoles((String) scopeMap.get("roles"));
                                    scope.setDescription((String) scopeMap.get("description"));
                                    scopeList.add(scope);
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }

        return scopeList;
    }

    @Override
    public void saveAPIDefinition(APIIdentifier apiIdentifier, String resourceConfigsJSON, API api, APIProvider apiProvider) throws APIManagementException {
        JSONParser parser = new JSONParser();
        //boolean isTenantFlowStarted;
        try {
            JSONObject resourceConfigs = (JSONObject) parser.parse(resourceConfigsJSON);
            JSONObject api_doc = (JSONObject) resourceConfigs.get("api_doc");
            String apiJSON = api_doc.toJSONString();

            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                //isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            apiProvider.updateSwagger12Definition(apiIdentifier, APIConstants.API_DOC_1_2_RESOURCE_NAME, apiJSON);
        } catch (ParseException e) {
            handleException("Invalid resource configuration ", e);
        }

    }
}
