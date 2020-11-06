/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.CORSConfiguration;
import org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent.getRegistryService;

public class RegistryPersistenceUtil {
    private static final Log log = LogFactory.getLog(RegistryPersistenceUtil.class);

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT, APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    /**
     * Create Governance artifact from given attributes
     *
     * @param artifact initial governance artifact
     * @param api      API object with the attributes value
     * @return GenericArtifact
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to create API
     */
    public static GenericArtifact createAPIArtifactContent(GenericArtifact artifact, API api)
                                    throws APIManagementException {

        try {
            String apiStatus = api.getStatus();
            artifact.setAttribute(APIConstants.API_OVERVIEW_NAME, api.getId().getApiName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, api.getId().getVersion());
            artifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, String.valueOf(api.isDefaultVersion()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT, api.getContext());
            artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, api.getId().getProviderName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WADL, api.getWadlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, api.getThumbnailUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, apiStatus);
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER, api.getTechnicalOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL, api.getTechnicalOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER, api.getBusinessOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, api.getBusinessOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBILITY, api.getVisibility());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES, api.getVisibleRoles());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS, api.getVisibleTenants());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED,
                                            Boolean.toString(api.isEndpointSecured()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST,
                                            Boolean.toString(api.isEndpointAuthDigest()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME, api.getEndpointUTUsername());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD, api.getEndpointUTPassword());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TRANSPORTS, api.getTransports());
            artifact.setAttribute(APIConstants.API_OVERVIEW_INSEQUENCE, api.getInSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE, api.getOutSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE, api.getFaultSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING, api.getResponseCache());
            artifact.setAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT, Integer.toString(api.getCacheTimeout()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL, api.getRedirectURL());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OWNER, api.getApiOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY, Boolean.toString(api.isAdvertiseOnly()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG, api.getEndpointConfig());

            artifact.setAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY,
                                            api.getSubscriptionAvailability());
            artifact.setAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS,
                                            api.getSubscriptionAvailableTenants());

            artifact.setAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION, api.getImplementation());

            artifact.setAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS, api.getProductionMaxTps());
            artifact.setAttribute(APIConstants.API_SANDBOX_THROTTLE_MAXTPS, api.getSandboxMaxTps());
            artifact.setAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER, api.getAuthorizationHeader());
            artifact.setAttribute(APIConstants.API_OVERVIEW_API_SECURITY, api.getApiSecurity());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA,
                                            Boolean.toString(api.isEnabledSchemaValidation()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE, Boolean.toString(api.isEnableStore()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_TESTKEY, api.getTestKey());

            //Validate if the API has an unsupported context before setting it in the artifact
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                String invalidContext = File.separator + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(api.getContextTemplate())) {
                    throw new APIManagementException("API : " + api.getId() + " has an unsupported context : " + api
                                                    .getContextTemplate());
                }
            } else {
                String invalidContext = APIConstants.TENANT_PREFIX + tenantDomain + File.separator
                                                + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(api.getContextTemplate())) {
                    throw new APIManagementException("API : " + api.getId() + " has an unsupported context : " + api
                                                    .getContextTemplate());
                }
            }
            // This is to support the pluggable version strategy.
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE, api.getContextTemplate());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context");
            artifact.setAttribute(APIConstants.API_OVERVIEW_TYPE, api.getType());

            StringBuilder policyBuilder = new StringBuilder();
            for (Tier tier : api.getAvailableTiers()) {
                policyBuilder.append(tier.getName());
                policyBuilder.append("||");
            }

            String policies = policyBuilder.toString();

            if (!"".equals(policies)) {
                policies = policies.substring(0, policies.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, policies);
            }

            StringBuilder tiersBuilder = new StringBuilder();
            for (Tier tier : api.getAvailableTiers()) {
                tiersBuilder.append(tier.getName());
                tiersBuilder.append("||");
            }

            String tiers = tiersBuilder.toString();

            if (!"".equals(tiers)) {
                tiers = tiers.substring(0, tiers.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, tiers);
            } else {
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, tiers);
            }

            if (APIConstants.PUBLISHED.equals(apiStatus)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true");
            }
            String[] keys = artifact.getAttributeKeys();
            for (String key : keys) {
                if (key.contains("URITemplate")) {
                    artifact.removeAttribute(key);
                }
            }

            Set<URITemplate> uriTemplateSet = api.getUriTemplates();
            int i = 0;
            for (URITemplate uriTemplate : uriTemplateSet) {
                artifact.addAttribute(APIConstants.API_URI_PATTERN + i, uriTemplate.getUriTemplate());
                artifact.addAttribute(APIConstants.API_URI_HTTP_METHOD + i, uriTemplate.getHTTPVerb());
                artifact.addAttribute(APIConstants.API_URI_AUTH_TYPE + i, uriTemplate.getAuthType());

                i++;

            }
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS, writeEnvironmentsToArtifact(api));

            artifact.setAttribute(APIConstants.API_OVERVIEW_CORS_CONFIGURATION,
                                            RegistryPersistenceUtil.getCorsConfigurationJsonFromDto(
                                                                            api.getCorsConfiguration()));

            //attaching micro-gateway labels to the API
            attachLabelsToAPIArtifact(artifact, api, tenantDomain);

            //attaching api categories to the API
            List<APICategory> attachedApiCategories = api.getApiCategories();
            artifact.removeAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME);
            if (attachedApiCategories != null) {
                for (APICategory category : attachedApiCategories) {
                    artifact.addAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME, category.getName());
                }
            }

            //set monetization status (i.e - enabled or disabled)
            artifact.setAttribute(APIConstants.Monetization.API_MONETIZATION_STATUS,
                                            Boolean.toString(api.getMonetizationStatus()));
            //set additional monetization data
            if (api.getMonetizationProperties() != null) {
                artifact.setAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES,
                                                api.getMonetizationProperties().toJSONString());
            }
            if (api.getKeyManagers() != null) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS, new Gson().toJson(api.getKeyManagers()));
            }

            //check in github code to see this method was removed
            String apiSecurity = artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);
            if (apiSecurity != null && !apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) && !apiSecurity
                                            .contains(APIConstants.API_SECURITY_API_KEY)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, "");
            }

            //          set deployments selected
            Set<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> deploymentEnvironments = api.getDeploymentEnvironments();
            String json = new Gson().toJson(deploymentEnvironments);
            artifact.setAttribute(APIConstants.API_OVERVIEW_DEPLOYMENTS, json);

        } catch (GovernanceException e) {
            String msg = "Failed to create API for : " + api.getId().getApiName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * This method is used to attach micro-gateway labels to the given API
     *
     * @param artifact     genereic artifact
     * @param api          API
     * @param tenantDomain domain name of the tenant
     * @throws APIManagementException if failed to attach micro-gateway labels
     */
    public static void attachLabelsToAPIArtifact(GenericArtifact artifact, API api, String tenantDomain)
                                    throws APIManagementException {

        //get all labels in the tenant
        List<Label> gatewayLabelList = RegistryPersistenceUtil.getAllLabels(tenantDomain);
        //validation is performed here to cover all actions related to API artifact updates
        if (!gatewayLabelList.isEmpty()) {
            //put available gateway labels to a list for validation purpose
            List<String> availableGatewayLabelListNames = new ArrayList<>();
            for (Label x : gatewayLabelList) {
                availableGatewayLabelListNames.add(x.getName());
            }
            try {
                //clear all the existing labels first
                artifact.removeAttribute(APIConstants.API_LABELS_GATEWAY_LABELS);
                //if there are labels attached to the API object, add them to the artifact
                if (api.getGatewayLabels() != null) {
                    //validate and add each label to the artifact
                    List<Label> candidateLabelsList = api.getGatewayLabels();
                    for (Label label : candidateLabelsList) {
                        String candidateLabel = label.getName();
                        //validation step, add the label only if it exists in the available gateway labels
                        if (availableGatewayLabelListNames.contains(candidateLabel)) {
                            artifact.addAttribute(APIConstants.API_LABELS_GATEWAY_LABELS, candidateLabel);
                        } else {
                            log.warn("Label name : " + candidateLabel + " does not exist in the tenant : "
                                                            + tenantDomain + ", hence skipping it.");
                        }
                    }
                }
            } catch (GovernanceException e) {
                String msg = "Failed to add labels for API : " + api.getId().getApiName();
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No predefined labels in the tenant : " + tenantDomain + " . Skipped adding all labels");
            }
        }
    }

    /**
     * This method is used to get the labels in a given tenant space
     *
     * @param tenantDomain tenant domain name
     * @return micro gateway labels in a given tenant space
     * @throws APIManagementException if failed to fetch micro gateway labels
     */
    public static List<Label> getAllLabels(String tenantDomain) throws APIManagementException {

        //        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        //        return apiMgtDAO.getAllLabels(tenantDomain);
        return null; //
    }

    /**
     * Used to generate Json string from CORS Configuration object
     *
     * @param corsConfiguration CORSConfiguration Object
     * @return Json string according to CORSConfiguration Object
     */
    public static String getCorsConfigurationJsonFromDto(org.wso2.carbon.apimgt.api.model.CORSConfiguration corsConfiguration) {

        return new Gson().toJson(corsConfiguration);
    }

    /**
     * This method used to set environment values to governance artifact of API .
     *
     * @param api API object with the attributes value
     */
    public static String writeEnvironmentsToArtifact(API api) {

        StringBuilder publishedEnvironments = new StringBuilder();
        Set<String> apiEnvironments = api.getEnvironments();
        if (apiEnvironments != null) {
            for (String environmentName : apiEnvironments) {
                publishedEnvironments.append(environmentName).append(',');
            }

            if (apiEnvironments.isEmpty()) {
                publishedEnvironments.append("none,");
            }

            if (!publishedEnvironments.toString().isEmpty()) {
                publishedEnvironments.deleteCharAt(publishedEnvironments.length() - 1);
            }
        }
        return publishedEnvironments.toString();
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key)
            throws APIManagementException {

        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, key);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + key
                        + ". Tenant id set in registry : " + ((UserRegistry) registry).getTenantId()
                        + ", Tenant domain set in PrivilegedCarbonContext: "
                        + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
    }

    /**
     * Utility method to get API provider path
     *
     * @param identifier APIIdentifier
     * @return API provider path
     */
    public static String getAPIProviderPath(APIIdentifier identifier) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName();
    }

    /**
     * Utility method to get API provider path
     *
     * @param api provider
     * @return API provider path
     */
    public static String getAPIProviderPath(String provider) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + provider;
    }
    /**
     * Utility method to get api path from APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return API path
     */
    // HAS REG USAGE
    public static String getAPIPath(APIIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + replaceEmailDomain(
                                        identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR + identifier
                                        .getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion()
                                        + APIConstants.API_RESOURCE_NAME;
    }

    /**
     * When an input is having '@',replace it with '-AT-' [This is required to persist API data in registry,as registry
     * paths don't allow '@' sign.]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomain(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR, APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return input;
    }

    public static String getTenantDomain(Identifier identifier) {
        return MultitenantUtils.getTenantDomain(replaceEmailDomainBack(identifier.getProviderName()));
    }

    public static void loadTenantRegistry(int tenantId) throws RegistryException {

        TenantRegistryLoader tenantRegistryLoader = PersistenceManagerComponent.getTenantRegistryLoader();
        ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
        tenantRegistryLoader.loadTenantRegistry(tenantId);
    }

    public static void loadloadTenantAPIRXT(String tenant, int tenantID) throws APIManagementException {

        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry registry = null;
        try {

            registry = registryService.getGovernanceSystemRegistry(tenantID);
        } catch (RegistryException e) {
            throw new APIManagementException("Error when create registry instance ", e);
        }

        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                                        + File.separator + "rxts";
        File file = new File(rxtDir);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                // if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);

        if (rxtFilePaths == null) {
            throw new APIManagementException("rxt files not found in directory " + rxtDir);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;

            //This is  "registry" is a governance registry instance, therefore calculate the relative path to governance.
            String govRelativePath = RegistryUtils.getRelativePathToOriginal(resourcePath,
                                            RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH));
            try {
                // calculate resource path
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(
                                                ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);

                org.wso2.carbon.user.api.AuthorizationManager authManager = ServiceReferenceHolder.getInstance()
                                                .getRealmService().
                                                                                getTenantUserRealm(tenantID)
                                                .getAuthorizationManager();

                if (registry.resourceExists(govRelativePath)) {
                    // set anonymous user permission to RXTs
                    authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    continue;
                }

                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes(Charset.defaultCharset()));
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                registry.put(govRelativePath, resource);

                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);

            } catch (UserStoreException e) {
                throw new APIManagementException("Error while adding role permissions to API", e);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }

    }
    
    public static void loadTenantAPIPolicy(String tenant, int tenantID) throws APIManagementException {

        String tierBasePath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                                        + File.separator + "default-tiers" + File.separator;

        String apiTierFilePath = tierBasePath + APIConstants.DEFAULT_API_TIER_FILE_NAME;
        String appTierFilePath = tierBasePath + APIConstants.DEFAULT_APP_TIER_FILE_NAME;
        String resTierFilePath = tierBasePath + APIConstants.DEFAULT_RES_TIER_FILE_NAME;

        loadTenantAPIPolicy(tenantID, APIConstants.API_TIER_LOCATION, apiTierFilePath);
        loadTenantAPIPolicy(tenantID, APIConstants.APP_TIER_LOCATION, appTierFilePath);
        loadTenantAPIPolicy(tenantID, APIConstants.RES_TIER_LOCATION, resTierFilePath);
    }
    /**
     * Load the throttling policy  to the registry for tenants
     *
     * @param tenantID
     * @param location
     * @param fileName
     * @throws APIManagementException
     */
    private static void loadTenantAPIPolicy(int tenantID, String location, String fileName)
                                    throws APIManagementException {

        InputStream inputStream = null;

        try {
            RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();

            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(location)) {
                if (log.isDebugEnabled()) {
                    log.debug("Tier policies already uploaded to the tenant's registry space");
                }
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding API tier policies to the tenant's registry");
            }
            File defaultTiers = new File(fileName);
            if (!defaultTiers.exists()) {
                log.info("Default tier policies not found in : " + fileName);
                return;
            }
            inputStream = FileUtils.openInputStream(defaultTiers);
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            govRegistry.put(location, resource);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving policy information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading policy file content", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error when closing input stream", e);
                }
            }
        }
    }

    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     *
     * @param registryContext Registry Context instance which holds path mappings
     * @param path            default path of the registry
     * @return mounted path or path
     */
    public static String getMountedPath(RegistryContext registryContext, String path) {

        if (registryContext != null && path != null) {
            List<Mount> mounts = registryContext.getMounts();
            if (mounts != null) {
                for (Mount mount : mounts) {
                    if (path.equals(mount.getPath())) {
                        return mount.getTargetPath();
                    }
                }
            }
        }
        return path;
    }

    /**
     * This Method is different from getAPI method, as this one returns
     * URLTemplates without aggregating duplicates. This is to be used for building synapse config.
     *
     * @param artifact
     * @param registry
     * @return API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static API getAPIForPublishing(GovernanceArtifact artifact, Registry registry)
                                    throws APIManagementException {
        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);

            api = new API(apiIdentifier);
            //set uuid
            api.setUUID(artifact.getId());
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            api = setResourceProperties(api, registry, artifactPath);
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            // set url
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                    .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
            }
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setProductionMaxTps(artifact.getAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS));
            api.setSandboxMaxTps(artifact.getAttribute(APIConstants.API_SANDBOX_THROTTLE_MAXTPS));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                String strCacheTimeout = artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT);
                if (strCacheTimeout != null && !strCacheTimeout.isEmpty()) {
                    cacheTimeout = Integer.parseInt(strCacheTimeout);
                }
            } catch (NumberFormatException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error while retrieving cache timeout from the registry for " + apiIdentifier);
                }
                // ignore the exception and use default cache timeout value
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            Set<Tier> availableTiers = new HashSet<Tier>();
            if(tiers != null) {
                String[] tiersArray = tiers.split("\\|\\|");
                for(String tierName : tiersArray) {
                    availableTiers.add(new Tier(tierName));
                }
            }
            api.addAvailableTiers(availableTiers );
            ////// TODO set this tiers in the obj/////////////////////////////////

            // This contains the resolved context
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
            api.setEnableSchemaValidation(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));
            api.setEnableStore(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
            api.setTestKey(artifact.getAttribute(APIConstants.API_OVERVIEW_TESTKEY));

            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setCreatedTime(String.valueOf(registry.get(artifactPath).getCreatedTime().getTime()));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));

            api.setEnvironments(getEnvironments(artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS)));
            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
            //set data and status related to monetization
            api.setMonetizationStatus(Boolean.parseBoolean(artifact.getAttribute
                    (APIConstants.Monetization.API_MONETIZATION_STATUS)));
            String monetizationInfo = artifact.getAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES);

            //set selected clusters which API needs to be deployed
            String deployments = artifact.getAttribute(APIConstants.API_OVERVIEW_DEPLOYMENTS);
            
            Set<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> deploymentEnvironments = extractDeploymentsForAPI(deployments);
            if (deploymentEnvironments != null && !deploymentEnvironments.isEmpty()) {
                api.setDeploymentEnvironments(deploymentEnvironments);
            }

            if (StringUtils.isNotBlank(monetizationInfo)) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObj = (JSONObject) parser.parse(monetizationInfo);
                api.setMonetizationProperties(jsonObj);
            }
            api.setGatewayLabels(getLabelsFromAPIGovernanceArtifact(artifact, api.getId().getProviderName()));
            api.setApiCategories(getAPICategoriesFromAPIGovernanceArtifact(artifact, tenantId));
            //get endpoint config string from artifact, parse it as a json and set the environment list configured with
            //non empty URLs to API object
            String keyManagers = artifact.getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
            if (StringUtils.isNotEmpty(keyManagers)) {
                api.setKeyManagers(new Gson().fromJson(keyManagers, List.class));
            } else {
                api.setKeyManagers(Arrays.asList(APIConstants.API_LEVEL_ALL_KEY_MANAGERS));
            }
            try {
                api.setEnvironmentList(extractEnvironmentListForAPI(
                        artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)));
            } catch (ParseException e) {
                String msg = "Failed to parse endpoint config JSON of API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (ClassCastException e) {
                String msg = "Invalid endpoint config JSON found in API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Provider";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Failed to get parse monetization information.";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    private static Set<String> getEnvironments(String environments) {
        if(environments != null) {
            String[] publishEnvironmentArray = environments.split(",");
            return new HashSet<String>(Arrays.asList(publishEnvironmentArray));
        }
        return null;
    }

    /**
     * To set the resource properties to the API.
     *
     * @param api          API that need to set the resource properties.
     * @param registry     Registry to get the resource from.
     * @param artifactPath Path of the API artifact.
     * @return Updated API.
     * @throws RegistryException Registry Exception.
     */
    private static API setResourceProperties(API api, Registry registry, String artifactPath) throws RegistryException {

        Resource apiResource = registry.get(artifactPath);
        Properties properties = apiResource.getProperties();
        if (properties != null) {
            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug("API '" + api.getId().toString() + "' " + "has the property " + propertyName);
                }
                if (propertyName.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
                    api.addProperty(propertyName.substring(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX.length()),
                            apiResource.getProperty(propertyName));
                }
            }
        }
        api.setAccessControl(apiResource.getProperty(APIConstants.ACCESS_CONTROL));

        String accessControlRoles = null;

        String displayPublisherRoles = apiResource.getProperty(APIConstants.DISPLAY_PUBLISHER_ROLES);
        if (displayPublisherRoles == null) {

            String publisherRoles = apiResource.getProperty(APIConstants.PUBLISHER_ROLES);

            if (publisherRoles != null) {
                accessControlRoles = APIConstants.NULL_USER_ROLE_LIST.equals(
                        apiResource.getProperty(APIConstants.PUBLISHER_ROLES)) ?
                        null : apiResource.getProperty(APIConstants.PUBLISHER_ROLES);
            }
        } else {
            accessControlRoles = APIConstants.NULL_USER_ROLE_LIST.equals(displayPublisherRoles) ?
                    null : displayPublisherRoles;
        }

        api.setAccessControlRoles(accessControlRoles);
        return api;
    }

    protected GenericArtifactManager getAPIGenericArtifactManagerFromUtil(Registry registry, String keyType)
                                    throws APIManagementException {
        return getArtifactManager(registry, keyType);
    }

//    /**
//     * Crate an WSDL from given wsdl url. Reset the endpoint details to gateway node
//     * *
//     *
//     * @param registry - Governance Registry space to save the WSDL
//     * @param api      -API instance
//     * @return Path of the created resource
//     * @throws APIManagementException If an error occurs while adding the WSDL
//     */
//
//    public static String createWSDL(Registry registry, API api) throws RegistryException, APIManagementException {
//
//        try {
//            GenericArtifactManager apiArtifactManager = getArtifactManager(registry, APIConstants.API_KEY);
//
//            String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
//                                            .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
//                                                                            api.getId().getVersion());
//
//            String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
//                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;
//
//            //            APIMWSDLReader wsdlReader = new APIMWSDLReader();
//            //            OMElement wsdlContentEle;
//            String wsdlRegistryPath;
//
//            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
//            if (org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
//                                            .equalsIgnoreCase(tenantDomain)) {
//                wsdlRegistryPath = RegistryConstants.PATH_SEPARATOR + "registry" + RegistryConstants.PATH_SEPARATOR
//                                                + "resource" + absoluteWSDLResourcePath;
//            } else {
//                wsdlRegistryPath = "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
//                                                + RegistryConstants.PATH_SEPARATOR + "resource"
//                                                + absoluteWSDLResourcePath;
//            }
//
//            Resource wsdlResource = registry.newResource();
//            // isWSDL2Document(api.getWsdlUrl()) method only understands http or file system urls.
//            // Hence if this is a registry url, should not go in to the following if block
//
//            registry.put(wsdlResourcePath, wsdlResource);
//            //set the anonymous role for wsld resource to avoid basicauth security.
//            String[] visibleRoles = null;
//            if (api.getVisibleRoles() != null) {
//                visibleRoles = api.getVisibleRoles().split(",");
//            }
//            setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, wsdlResourcePath);
//
//            //Delete any WSDL archives if exists
//            String wsdlArchivePath = getWsdlArchivePath(api.getId());
//            if (registry.resourceExists(wsdlArchivePath)) {
//                registry.delete(wsdlArchivePath);
//            }
//
//            //set the wsdl resource permlink as the wsdlURL.
//            api.setWsdlUrl(getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));
//
//            if (PersistenceUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
//                //String path = APIUtil.createWSDL(api);
//                 updateWSDLUriInAPIArtifact(wsdlRegistryPath, artifactManager, artifact,artifactPath);
//                //
//            }
//
//            if (api.getWsdlResource() != null) {
//                String path = saveWSDLResource(registry, api);
//                updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
//            }
//
//            return wsdlRegistryPath;
//
//        } catch (RegistryException e) {
//            String msg = "Failed to add WSDL " + api.getWsdlUrl() + " to the registry";
//            log.error(msg, e);
//            throw new RegistryException(msg, e);
//        } catch (APIManagementException e) {
//            String msg = "Failed to process the WSDL : " + api.getWsdlUrl();
//            log.error(msg, e);
//            throw new APIManagementException(msg, e);
//        }
//    }

    public static void startTenantFlow(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    public static API getApiForPublishing(Registry registry, GovernanceArtifact apiArtifact)
                                    throws APIManagementException {
        API api = getAPIForPublishing(apiArtifact, registry);
        updateAPIProductDependencies(api, registry);
        return api;
    }

    public static void updateAPIProductDependencies(API api, Registry registry) throws APIManagementException {

        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Set<APIProductIdentifier> usedByProducts = uriTemplate.retrieveUsedByProducts();
            for (APIProductIdentifier usedByProduct : usedByProducts) {
                //TODO : removed registry call until find a proper fix
                String apiProductPath = RegistryPersistenceUtil.getAPIProductPath(usedByProduct);
                usedByProduct.setUUID(apiProductPath);
            }
        }
    }

    /**
     * Utility method to get api product path from APIProductIdentifier
     *
     * @param identifier APIProductIdentifier
     * @return APIProduct path
     */
    public static String getAPIProductPath(APIProductIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + replaceEmailDomain(
                                        identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR + identifier
                                        .getName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion()
                                        + APIConstants.API_RESOURCE_NAME;
    }

    public static void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param username     Username
     * @param visibility   API visibility
     * @param roles        Authorized roles
     * @param artifactPath API resource path
     * @throws APIManagementException Throwing exception
     */
    public static void setResourcePermissions(String username, String visibility, String[] roles, String artifactPath)
                                    throws APIManagementException {

        setResourcePermissions(username, visibility, roles, artifactPath, null);
    }

    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param visibility   API/Product visibility
     * @param roles        Authorized roles
     * @param artifactPath API/Product resource path
     * @param registry     Registry
     * @throws APIManagementException Throwing exception
     */
    public static void setResourcePermissions(String username, String visibility, String[] roles, String artifactPath,
                                    Registry registry) throws APIManagementException {

        try {
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                            getMountedPath(RegistryContext.getBaseInstance(),
                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                                                            + artifactPath);
            Resource registryResource = null;

            if (registry != null && registry.resourceExists(artifactPath)) {
                registryResource = registry.get(artifactPath);
            }
            StringBuilder publisherAccessRoles = new StringBuilder(APIConstants.NULL_USER_ROLE_LIST);

            if (registryResource != null) {
                String publisherRole = registryResource.getProperty(APIConstants.PUBLISHER_ROLES);
                if (publisherRole != null) {
                    publisherAccessRoles = new StringBuilder(publisherRole);
                }
                if (StringUtils.isEmpty(publisherAccessRoles.toString())) {
                    publisherAccessRoles = new StringBuilder(APIConstants.NULL_USER_ROLE_LIST);
                }

                if (APIConstants.API_GLOBAL_VISIBILITY.equalsIgnoreCase(visibility)
                                                || APIConstants.API_PRIVATE_VISIBILITY.equalsIgnoreCase(visibility)) {
                    registryResource.setProperty(APIConstants.STORE_VIEW_ROLES, APIConstants.NULL_USER_ROLE_LIST);
                    publisherAccessRoles = new StringBuilder(APIConstants.NULL_USER_ROLE_LIST); // set publisher
                    // access roles null since store visibility is global. We do not need to add any roles to
                    // store_view_role property.
                } else {
                    registryResource.setProperty(APIConstants.STORE_VIEW_ROLES, publisherAccessRoles.toString());
                }
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(PersistenceUtil.replaceEmailDomainBack(username));
            if (!org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(tenantDomain)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                                                getTenantManager().getTenantId(tenantDomain);
                // calculate resource path
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(
                                                ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);
                org.wso2.carbon.user.api.AuthorizationManager authManager = ServiceReferenceHolder.getInstance()
                                                .getRealmService().
                                                                                getTenantUserRealm(tenantId)
                                                .getAuthorizationManager();
                if (visibility != null && APIConstants.API_RESTRICTED_VISIBILITY.equalsIgnoreCase(visibility)) {
                    boolean isRoleEveryOne = false;
                    /*If no roles have defined, authorize for everyone role */
                    if (roles != null) {
                        if (roles.length == 1 && "".equals(roles[0])) {
                            authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                            isRoleEveryOne = true;
                        } else {
                            for (String role : roles) {
                                if (APIConstants.EVERYONE_ROLE.equalsIgnoreCase(role.trim())) {
                                    isRoleEveryOne = true;
                                }
                                authManager.authorizeRole(role.trim(), resourcePath, ActionConstants.GET);
                                publisherAccessRoles.append(",").append(role.trim().toLowerCase());
                            }
                        }
                    }
                    if (!isRoleEveryOne) {
                        authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    }
                    authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && APIConstants.API_PRIVATE_VISIBILITY.equalsIgnoreCase(visibility)) {
                    authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(visibility)) {

                    /*If no roles have defined, deny access for everyone & anonymous role */
                    if (roles == null) {
                        authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                        authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    } else {
                        for (String role : roles) {
                            authManager.denyRole(role.trim(), resourcePath, ActionConstants.GET);

                        }
                    }
                } else {
                    authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                }
            } else {
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(
                                                ServiceReferenceHolder.getUserRealm());

                if (visibility != null && APIConstants.API_RESTRICTED_VISIBILITY.equalsIgnoreCase(visibility)) {
                    boolean isRoleEveryOne = false;
                    if (roles != null) {
                        for (String role : roles) {
                            if (APIConstants.EVERYONE_ROLE.equalsIgnoreCase(role.trim())) {
                                isRoleEveryOne = true;
                            }
                            authorizationManager.authorizeRole(role.trim(), resourcePath, ActionConstants.GET);
                            publisherAccessRoles.append(",").append(role.toLowerCase());
                        }
                    }
                    if (!isRoleEveryOne) {
                        authorizationManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    }
                    authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);

                } else if (visibility != null && APIConstants.API_PRIVATE_VISIBILITY.equalsIgnoreCase(visibility)) {
                    authorizationManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(visibility)) {
                    /*If no roles have defined, deny access for everyone & anonymous role */
                    if (roles == null) {
                        authorizationManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                        authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    } else {
                        for (String role : roles) {
                            authorizationManager.denyRole(role.trim(), resourcePath, ActionConstants.GET);

                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Store view roles for " + artifactPath + " : " + publisherAccessRoles.toString());
                    }
                    authorizationManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authorizationManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                }
            }
            if (registryResource != null) {
                registryResource.setProperty(APIConstants.STORE_VIEW_ROLES, publisherAccessRoles.toString());
                registry.put(artifactPath, registryResource);
            }

        } catch (UserStoreException e) {
            throw new APIManagementException("Error while adding role permissions to API", e);
        } catch (RegistryException e) {
            throw new APIManagementException("Registry exception while adding role permissions to API", e);
        }
    }

    /**
     * Utility method for get registry path for wsdl archive.
     *
     * @param identifier APIIdentifier
     * @return wsdl archive path
     */
    public static String getWsdlArchivePath(APIIdentifier identifier) {

        return APIConstants.API_WSDL_RESOURCE_LOCATION + APIConstants.API_WSDL_ARCHIVE_LOCATION + identifier
                                        .getProviderName() + APIConstants.WSDL_PROVIDER_SEPERATOR + identifier
                                        .getApiName() + identifier.getVersion() + APIConstants.ZIP_FILE_EXTENSION;
    }

    /**
     * This is to get the registry resource's HTTP permlink path.
     * Once this issue is fixed (https://wso2.org/jira/browse/REGISTRY-2110),
     * we can remove this method, and get permlink from the resource.
     *
     * @param path - Registry resource path
     * @return {@link String} -HTTP permlink
     */
    public static String getRegistryResourceHTTPPermlink(String path) {

        String schemeHttp = APIConstants.HTTP_PROTOCOL;
        String schemeHttps = APIConstants.HTTPS_PROTOCOL;

        ConfigurationContextService contetxservice = ServiceReferenceHolder.getContextService();
        //First we will try to generate http permalink and if its disabled then only we will consider https
        int port = CarbonUtils.getTransportProxyPort(contetxservice.getServerConfigContext(), schemeHttp);
        if (port == -1) {
            port = CarbonUtils.getTransportPort(contetxservice.getServerConfigContext(), schemeHttp);
        }
        //getting https parameters if http is disabled. If proxy port is not present we will go for default port
        if (port == -1) {
            port = CarbonUtils.getTransportProxyPort(contetxservice.getServerConfigContext(), schemeHttps);
        }
        if (port == -1) {
            port = CarbonUtils.getTransportPort(contetxservice.getServerConfigContext(), schemeHttps);
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");

        if (webContext == null || "/".equals(webContext)) {
            webContext = "";
        }
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        String version = "";
        if (registryService == null) {
            log.error("Registry Service has not been set.");
        } else if (path != null) {
            try {
                String[] versions = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME,
                                                CarbonContext.getThreadLocalCarbonContext().getTenantId())
                                                .getVersions(path);
                if (versions != null && versions.length > 0) {
                    version = versions[0].substring(versions[0].lastIndexOf(";version:"));
                }
            } catch (RegistryException e) {
                log.error("An error occurred while determining the latest version of the "
                                                + "resource at the given path: " + path, e);
            }
        }
        if (port != -1 && path != null) {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            return webContext + ((tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(tenantDomain)) ?
                                            "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain :
                                            "") + "/registry/resource" + org.wso2.carbon.registry.app.Utils
                                            .encodeRegistryPath(path) + version;
        }
        return null;
    }

    /**
     * Update WSDLUri in the API Registry artifact
     *
     * @param wsdlPath WSDL Registry Path
     * @param artifactManager Artifact Manager
     * @param artifact API Artifact
     * @param artifactPath API Artifact path
     * @throws RegistryException when error occurred while updating WSDL path
     */
    //  WILL BE ONLY CALLED FROM REGISTRY INTERFACE  METHOD
    public static void updateWSDLUriInAPIArtifact(String wsdlPath, GenericArtifactManager artifactManager,
                                    GenericArtifact artifact, String artifactPath) throws RegistryException {
        Registry registry = getRegistryService().getGovernanceUserRegistry();

        if (wsdlPath != null) {
            registry.addAssociation(artifactPath, wsdlPath, CommonConstants.ASSOCIATION_TYPE01);
            artifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, wsdlPath);
            artifactManager.updateGenericArtifact(artifact); //update the  artifact
        }
    }

    /**
     * Save the provided wsdl archive file to the registry for the api
     *
     * @param registry Governance Registry space to save the WSDL
     * @param api      API instance
     * @return
     * @throws RegistryException
     * @throws APIManagementException
     */
    public static String saveWSDLResource(Registry registry, API api) throws RegistryException, APIManagementException {

        ResourceFile wsdlResource = api.getWsdlResource();
        String wsdlResourcePath;
        boolean isZip = false;
        String wsdlResourcePathArchive =
                                        APIConstants.API_WSDL_RESOURCE_LOCATION + APIConstants.API_WSDL_ARCHIVE_LOCATION + api.getId()
                                                                        .getProviderName() + APIConstants.WSDL_PROVIDER_SEPERATOR + api.getId().getApiName() +
                                                                        api.getId().getVersion() + APIConstants.ZIP_FILE_EXTENSION;
        String wsdlResourcePathFile = APIConstants.API_WSDL_RESOURCE_LOCATION +
                                        PersistenceUtil.createWsdlFileName(api.getId().getProviderName(),
                                                                        api.getId().getApiName(),
                                                                        api.getId().getVersion());

        if (wsdlResource.getContentType().equals(APIConstants.APPLICATION_ZIP)) {
            wsdlResourcePath = wsdlResourcePathArchive;
            isZip = true;
        } else {
            wsdlResourcePath = wsdlResourcePathFile;
        }

        String absoluteWSDLResourcePath = RegistryUtils
                                        .getAbsolutePath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                        + wsdlResourcePath;
        try {
            Resource wsdlResourceToUpdate = registry.newResource();
            wsdlResourceToUpdate.setContentStream(api.getWsdlResource().getContent());
            wsdlResourceToUpdate.setMediaType(api.getWsdlResource().getContentType());
            registry.put(wsdlResourcePath, wsdlResourceToUpdate);
            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }
            setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                                            wsdlResourcePath);

            if (isZip) {
                //Delete any WSDL file if exists
                if (registry.resourceExists(wsdlResourcePathFile)) {
                    registry.delete(wsdlResourcePathFile);
                }
            } else {
                //Delete any WSDL archives if exists
                if (registry.resourceExists(wsdlResourcePathArchive)) {
                    registry.delete(wsdlResourcePathArchive);
                }
            }

            api.setWsdlUrl(getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));
        } catch (RegistryException e) {
            String msg = "Failed to add WSDL Archive " + api.getWsdlUrl() + " to the registry";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to process the WSDL Archive: " + api.getWsdlUrl();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return wsdlResourcePath;
    }

    /**
     * This method is used to attach micro-gateway labels to the given API
     *
     * @param artifact     genereic artifact
     * @param api          API
     * @param tenantDomain domain name of the tenant
     * @throws APIManagementException if failed to attach micro-gateway labels
     */
    // HAS REG USAGE
    public static void attachLabelsToAPIArtifact(GenericArtifact artifact, API api, String tenantDomain, List<Label> gatewayLabelList)
                                    throws APIManagementException {

        //get all labels in the tenant
        //List<Label> gatewayLabelList = APIUtil.getAllLabels(tenantDomain);
        //validation is performed here to cover all actions related to API artifact updates
        if (!gatewayLabelList.isEmpty()) {
            //put available gateway labels to a list for validation purpose
            List<String> availableGatewayLabelListNames = new ArrayList<>();
            for (Label x : gatewayLabelList) {
                availableGatewayLabelListNames.add(x.getName());
            }
            try {
                //clear all the existing labels first
                artifact.removeAttribute(APIConstants.API_LABELS_GATEWAY_LABELS);
                //if there are labels attached to the API object, add them to the artifact
                if (api.getGatewayLabels() != null) {
                    //validate and add each label to the artifact
                    List<Label> candidateLabelsList = api.getGatewayLabels();
                    for (Label label : candidateLabelsList) {
                        String candidateLabel = label.getName();
                        //validation step, add the label only if it exists in the available gateway labels
                        if (availableGatewayLabelListNames.contains(candidateLabel)) {
                            artifact.addAttribute(APIConstants.API_LABELS_GATEWAY_LABELS, candidateLabel);
                        } else {
                            log.warn("Label name : " + candidateLabel + " does not exist in the tenant : " +
                                                            tenantDomain + ", hence skipping it.");
                        }
                    }
                }
            } catch (GovernanceException e) {
                String msg = "Failed to add labels for API : " + api.getId().getApiName();
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No predefined labels in the tenant : " + tenantDomain + " . Skipped adding all labels");
            }
        }
    }

    /**
     * Used to reconstruct the input get APIs query as sub context and doc content doesn't support AND search
     *
     * @param query Input apis get query
     * @return Reconstructed new apis get query
     * @throws APIManagementException If there is an error in the search query
     */
    public static String constructApisGetQuery(String query) throws APIManagementException {

        String newSearchQuery = constructQueryWithProvidedCriterias(query.trim());
        // below section should only be called in apisGet
//        if (!query.contains(APIConstants.TYPE)) {
//            String typeCriteria = APIConstants.TYPE_SEARCH_TYPE_KEY + RegistryPersistenceUtil.getORBasedSearchCriteria
//                                            (APIConstants.API_SUPPORTED_TYPE_LIST);
//            newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + typeCriteria;
//        }
        if (query.isEmpty()) {
            String typeCriteria = APIConstants.TYPE_SEARCH_TYPE_KEY + RegistryPersistenceUtil.getORBasedSearchCriteria
                                            (APIConstants.API_SUPPORTED_TYPE_LIST);
            newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + typeCriteria;
        }
        return newSearchQuery;
    }

    /**
     * @param inputSearchQuery search Query
     * @return Reconstructed new search query
     * @throws APIManagementException If there is an error in the search query
     */
    private static String constructQueryWithProvidedCriterias(String inputSearchQuery) throws APIManagementException {

        String newSearchQuery = "";
        // sub context and doc content doesn't support AND search
        if (inputSearchQuery != null && inputSearchQuery.contains(" ") && !inputSearchQuery
                                        .contains(APIConstants.TAG_COLON_SEARCH_TYPE_PREFIX) && (!inputSearchQuery
                                        .contains(APIConstants.CONTENT_SEARCH_TYPE_PREFIX) || inputSearchQuery.split(":").length > 2)) {
            if (inputSearchQuery.split(" ").length > 1) {
                String[] searchCriterias = inputSearchQuery.split(" ");
                for (int i = 0; i < searchCriterias.length; i++) {
                    if (searchCriterias[i].contains(":") && searchCriterias[i].split(":").length > 1) {
                        if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX
                                                        .equalsIgnoreCase(searchCriterias[i].split(":")[0])
                                                        || APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX
                                                        .equalsIgnoreCase(searchCriterias[i].split(":")[0])) {
                            throw new APIManagementException("Invalid query. AND based search is not supported for "
                                                            + "doc and subcontext prefixes");
                        }
                    }
                    if (i == 0) {
                        newSearchQuery = getSingleSearchCriteria(searchCriterias[i]);
                    } else {
                        newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + getSingleSearchCriteria(searchCriterias[i]);
                    }
                }
            }
        } else {
            newSearchQuery = getSingleSearchCriteria(inputSearchQuery);
        }
        return newSearchQuery;
    }

    /**
     * Generates solr compatible search criteria synatax from user entered query criteria.
     * Ex: From version:1.0.0, this returns version=*1.0.0*
     *
     * @param criteria
     * @return solar compatible criteria
     * @throws APIManagementException
     */
    public static String getSingleSearchCriteria(String criteria) throws APIManagementException {

        criteria = criteria.trim();
        String searchValue = criteria;
        String searchKey = APIConstants.NAME_TYPE_PREFIX;

        if (criteria.contains(":")) {
            if (criteria.split(":").length > 1) {
                String[] splitValues = criteria.split(":");
                searchKey = splitValues[0].trim();
                searchValue = splitValues[1];
                //if search key is 'tag' instead of 'tags', allow it as well since rest api document says query
                // param to use for tag search is 'tag'

                if (APIConstants.TAG_SEARCH_TYPE_PREFIX.equals(searchKey)) {
                    searchKey = APIConstants.TAGS_SEARCH_TYPE_PREFIX;
                    searchValue = searchValue.replace(" ", "\\ ");
                }

                if (!APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey) &&
                                                !APIConstants.TAGS_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey)) {
                    if (APIConstants.API_STATUS.equalsIgnoreCase(searchKey)) {
                        searchValue = searchValue.toLowerCase();
                    }
                    if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                        if (!searchValue.endsWith("*")) {
                            searchValue = searchValue + "*";
                        }
                        if (!searchValue.startsWith("*")) {
                            searchValue = "*" + searchValue;
                        }
                    }
                }

            } else {
                throw new APIManagementException("Search term is missing. Try again with valid search query.");
            }
        } else {
            if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                if (!searchValue.endsWith("*")) {
                    searchValue = searchValue + "*";
                }
                if (!searchValue.startsWith("*")) {
                    searchValue = "*" + searchValue;
                }
            }
        }
        if (APIConstants.API_PROVIDER.equalsIgnoreCase(searchKey)) {
            searchValue = searchValue.replaceAll("@", "-AT-");
        }
        return searchKey + "=" + searchValue;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact) throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setId(artifact.getId());
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));
            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

            if (visibilityAttr != null) {
                if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                    documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                    documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                    documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.URL;
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.FILE;
                documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(APIConstants.DOC_FILE_PATH)));
            } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.MARKDOWN;
            }
            documentation.setSourceType(docSourceType);
            if (documentation.getType() == DocumentationType.OTHER) {
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }
    /**
     * Prepends the webcontextroot to a registry path.
     *
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he WebContext root.
     */
    public static String prependWebContextRoot(String postfixUrl) {

        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (webContext != null && !"/".equals(webContext)) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }

    /**
     * Composes OR based search criteria from provided array of values
     *
     * @param values
     * @return
     */
    public static String getORBasedSearchCriteria(String[] values) {

        String criteria = "(";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                criteria = criteria + values[i];
                if (i != values.length - 1) {
                    criteria = criteria + " OR ";
                } else {
                    criteria = criteria + ")";
                }
            }
            return criteria;
        }
        return null;
    }

    /**
     * This method used to get API from governance artifact
     *
     * @param artifact API artifact
     * @param registry Registry
     * @return API
     * @throws APIManagementException if failed to get API from artifact
     */
    public static API getAPI(GovernanceArtifact artifact, Registry registry)
                                    throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
//            int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
//
//            if (apiId == -1) {
//                return null;
//            }
            api = new API(apiIdentifier);
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
//            api = setResourceProperties(api, registry, artifactPath);
//            api.setRating(getAverageRating(apiId));
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            //set uuid
            api.setUUID(artifact.getId());
            //setting api ID for scope retrieval
//            api.getId().setApplicationId(Integer.toString(apiId));
            // set url
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                                            .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
            }
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setProductionMaxTps(artifact.getAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                            .getTenantId(tenantDomainName);

//            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
//            api.setApiLevelPolicy(apiLevelTier);

            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//            Map<String, Tier> definedTiers = getTiers(tenantId);
//            Set<Tier> availableTier = getAvailableTiers(definedTiers, tiers, apiName);
//            api.addAvailableTiers(availableTier);
//            api.setMonetizationCategory(getAPIMonetizationCategory(availableTier, tenantDomainName));

            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
            api.setEnableSchemaValidation(Boolean.parseBoolean(
                                            artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));

//            Map<String, Scope> scopeToKeyMapping = getAPIScopes(api.getId(), tenantDomainName);
//            api.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));

//            Set<URITemplate> uriTemplates = ApiMgtDAO.getInstance().getURITemplatesOfAPI(api.getId());
//
//            for (URITemplate uriTemplate : uriTemplates) {
//                List<Scope> oldTemplateScopes = uriTemplate.retrieveAllScopes();
//                List<Scope> newTemplateScopes = new ArrayList<>();
//                if (!oldTemplateScopes.isEmpty()) {
//                    for (Scope templateScope : oldTemplateScopes) {
//                        Scope scope = scopeToKeyMapping.get(templateScope.getKey());
//                        newTemplateScopes.add(scope);
//                    }
//                }
//                uriTemplate.addAllScopes(newTemplateScopes);
//                uriTemplate.setResourceURI(api.getUrl());
//                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
//            }
//            api.setUriTemplates(uriTemplates);
            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
//            api.setEnvironments(extractEnvironmentsForAPI(environments));
//            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
//            api.setApiCategories(getAPICategoriesFromAPIGovernanceArtifact(artifact, tenantId));

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Provider";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * Retrieves api product artifact from registry
     *
     * @param artifact
     * @param registry
     * @return APIProduct
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static APIProduct getAPIProduct(GovernanceArtifact artifact, Registry registry)
                                    throws APIManagementException {

        APIProduct apiProduct;
        try {
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String productName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String productVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(providerName, productName,
                                            productVersion);
            apiProduct = new APIProduct(apiProductIdentifier);
//            apiProduct.setRating(Float.toString(getAverageRating(apiProductIdentifier)));
//            ApiMgtDAO.getInstance().setAPIProductFromDB(apiProduct);

//            setResourceProperties(apiProduct, registry, artifactPath);

            //set uuid
            apiProduct.setUuid(artifact.getId());
            apiProduct.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            apiProduct.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            apiProduct.setState(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
            apiProduct.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            apiProduct.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            apiProduct.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            apiProduct.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            apiProduct.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            apiProduct.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            apiProduct.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            apiProduct.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            apiProduct.setSubscriptionAvailability(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            apiProduct.setSubscriptionAvailableTenants(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
//            apiProduct.setEnvironments(extractEnvironmentsForAPI(environments));
            apiProduct.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            apiProduct.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
            apiProduct.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
//            apiProduct.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            apiProduct.setCreatedTime(registry.get(artifactPath).getCreatedTime());
            apiProduct.setLastUpdated(registry.get(artifactPath).getLastModified());
            apiProduct.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            apiProduct.setTenantDomain(tenantDomainName);
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                            .getTenantId(tenantDomainName);

            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//            Map<String, Tier> definedTiers = getTiers(tenantId);
//            Set<Tier> availableTier = getAvailableTiers(definedTiers, tiers, productName);
//            apiProduct.setAvailableTiers(availableTier);

            // We set the context template here
            apiProduct.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            apiProduct.setEnableSchemaValidation(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));
            apiProduct.setEnableStore(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENABLE_STORE)));
            apiProduct.setTestKey(artifact.getAttribute(APIConstants.API_OVERVIEW_TESTKEY));
            apiProduct.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error in converting cache time out due to " + e.getMessage());
                }
            }
            apiProduct.setCacheTimeout(cacheTimeout);

//            List<APIProductResource> resources = ApiMgtDAO.getInstance().
//                                            getAPIProductResourceMappings(apiProductIdentifier);

//            Map<String, Scope> uniqueAPIProductScopeKeyMappings = new LinkedHashMap<>();
//            for (APIProductResource resource : resources) {
//                List<Scope> resourceScopes = resource.getUriTemplate().retrieveAllScopes();
//                ListIterator it = resourceScopes.listIterator();
//                while (it.hasNext()) {
//                    Scope resourceScope = (Scope) it.next();
//                    String scopeKey = resourceScope.getKey();
//                    if (!uniqueAPIProductScopeKeyMappings.containsKey(scopeKey)) {
//                        resourceScope = getScopeByName(scopeKey, tenantDomainName);
//                        uniqueAPIProductScopeKeyMappings.put(scopeKey, resourceScope);
//                    } else {
//                        resourceScope = uniqueAPIProductScopeKeyMappings.get(scopeKey);
//                    }
//                    it.set(resourceScope);
//                }
//            }

            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            apiProduct.addTags(tags);

//            for (APIProductResource resource : resources) {
//                String apiPath = APIUtil.getAPIPath(resource.getApiIdentifier());
//
//                Resource productResource = null;
//                try {
//                    // Handles store and publisher visibility issue when associated apis have different visibility
//                    // restrictions.
//                    productResource = registry.get(apiPath);
//                } catch (RegistryException e) {
//                    if (e.getClass().equals(AuthorizationFailedException.class)) {
//                        if (log.isDebugEnabled()) {
//                            log.debug("User is not authorized to access the resource " + apiPath);
//                        }
//                        continue;
//                    } else {
//                        String msg = "Failed to get product resource";
//                        throw new APIManagementException(msg, e);
//                    }
//                }
//                String artifactId = productResource.getUUID();
//                resource.setApiId(artifactId);
//
//                GenericArtifactManager artifactManager = getArtifactManager(registry,
//                                                APIConstants.API_KEY);
//
//                GenericArtifact apiArtifact = artifactManager.getGenericArtifact(resource.getApiId());
//                API api = getAPI(apiArtifact, registry);
//
//                resource.setEndpointConfig(api.getEndpointConfig());
//                resource.setEndpointSecurityMap(setEndpointSecurityForAPIProduct(api));
//            }

//            apiProduct.setProductResources(resources);
            //set data and status related to monetization
            apiProduct.setMonetizationStatus(Boolean.parseBoolean(artifact.getAttribute
                                            (APIConstants.Monetization.API_MONETIZATION_STATUS)));
            String monetizationInfo = artifact.getAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES);
            if (StringUtils.isNotBlank(monetizationInfo)) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObj = (JSONObject) parser.parse(monetizationInfo);
                apiProduct.setMonetizationProperties(jsonObj);
            }
            apiProduct.setApiCategories(getAPICategoriesFromAPIGovernanceArtifact(artifact, tenantId));
        } catch (GovernanceException e) {
            String msg = "Failed to get API Product for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Product Provider";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Failed to get parse monetization information.";
            throw new APIManagementException(msg, e);
        }
        return apiProduct;
    }

    public static String getLcStateFromArtifact(GovernanceArtifact artifact) throws GovernanceException {
        String lcState = artifact.getLifecycleState();
        String state = (lcState != null) ? lcState : artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
        return (state != null) ? state.toUpperCase() : null;
    }


    /**
     * This method is used to get the actual endpoint password of an API from the hidden property
     * in the case where the handler APIEndpointPasswordRegistryHandler is enabled in registry.xml
     *
     * @param api      The API
     * @param registry The registry object
     * @return The actual password of the endpoint if exists
     * @throws RegistryException Throws if the api resource doesn't exist
     */
    private static String getActualEpPswdFromHiddenProperty(API api, Registry registry) throws RegistryException {

        String apiPath = getAPIPath(api.getId());
        Resource apiResource = registry.get(apiPath);
        return apiResource.getProperty(APIConstants.REGISTRY_HIDDEN_ENDPOINT_PROPERTY);
    }

    /**
     * This method returns the categories attached to the API
     *
     * @param artifact API artifact
     * @param tenantID tenant ID of API Provider
     * @return List<APICategory> list of categories
     */
    private static List<APICategory> getAPICategoriesFromAPIGovernanceArtifact(GovernanceArtifact artifact, int tenantID)
                                    throws GovernanceException, APIManagementException {

        String[] categoriesOfAPI = artifact.getAttributes(APIConstants.API_CATEGORIES_CATEGORY_NAME);

        List<APICategory> categoryList = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(categoriesOfAPI)) {
            //category array retrieved from artifact has only the category name, therefore we need to fetch categories
            //and fill out missing attributes before attaching the list to the api
            String tenantDomain = getTenantDomainFromTenantId(tenantID);
//            List<APICategory> allCategories = getAllAPICategoriesOfTenant(tenantDomain);

            //todo-category: optimize this loop with breaks
//            for (String categoryName : categoriesOfAPI) {
//                for (APICategory category : allCategories) {
//                    if (categoryName.equals(category.getName())) {
//                        categoryList.add(category);
//                        break;
//                    }
//                }
//            }
        }
        return categoryList;
    }



    /**
     * Helper method to get tenantDomain from tenantId
     *
     * @param tenantId tenant Id
     * @return tenantId
     */
    public static String getTenantDomainFromTenantId(int tenantId) {

        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if (realmService == null) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            return realmService.getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static API getAPI(GovernanceArtifact artifact)
                                    throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
//            int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
//            if (apiId == -1) {
//                return null;
//            }
            //set uuid
            api.setUUID(artifact.getId());
//            api.setRating(getAverageRating(apiId));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setEnableStore(Boolean.parseBoolean(
                                            artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
            api.setTestKey(artifact.getAttribute(APIConstants.API_OVERVIEW_TESTKEY));
            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }
            api.setCacheTimeout(cacheTimeout);

//            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
//            api.setApiLevelPolicy(apiLevelTier);

//            Set<Tier> availablePolicy = new HashSet<Tier>();
//            String[] subscriptionPolicy = ApiMgtDAO.getInstance().getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, replaceEmailDomainBack(providerName));
//            List<String> definedPolicyNames = Arrays.asList(subscriptionPolicy);
//            String policies = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//            if (policies != null && !"".equals(policies)) {
//                String[] policyNames = policies.split("\\|\\|");
//                for (String policyName : policyNames) {
//                    if (definedPolicyNames.contains(policyName) || APIConstants.UNLIMITED_TIER.equals(policyName)) {
//                        Tier p = new Tier(policyName);
//                        availablePolicy.add(p);
//                    } else {
//                        log.warn("Unknown policy: " + policyName + " found on API: " + apiName);
//                    }
//                }
//            }
//
//            api.addAvailableTiers(availablePolicy);
            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
//            api.setMonetizationCategory(getAPIMonetizationCategory(availablePolicy, tenantDomainName));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));

//            ArrayList<URITemplate> urlPatternsList;
//            urlPatternsList = ApiMgtDAO.getInstance().getAllURITemplates(api.getContext(), api.getId().getVersion());
//            Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);
//
//            for (URITemplate uriTemplate : uriTemplates) {
//                uriTemplate.setResourceURI(api.getUrl());
//                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
//
//            }
//            api.setUriTemplates(uriTemplates);
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
//            api.setEnvironments(extractEnvironmentsForAPI(environments));
//            api.setCorsConfiguration(RegistryPersistenceUtil.getCorsConfigurationFromArtifact(artifact));
            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
            String deployments = artifact.getAttribute(APIConstants.API_OVERVIEW_DEPLOYMENTS);
            Set<DeploymentEnvironments> deploymentEnvironments = PersistenceUtil.extractDeploymentsForAPI(deployments);
            if (deploymentEnvironments != null && !deploymentEnvironments.isEmpty()) {
               // api.setDeploymentEnvironments(deploymentEnvironments); ///// TODO commented out
            }

            //get endpoint config string from artifact, parse it as a json and set the environment list configured with
            //non empty URLs to API object
            try {
                api.setEnvironmentList(PersistenceUtil.extractEnvironmentListForAPI(
                                                artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)));
            } catch (ParseException e) {
                String msg = "Failed to parse endpoint config JSON of API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (ClassCastException e) {
                String msg = "Invalid endpoint config JSON found in API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        } catch (GovernanceException e) {
            String msg = "Failed to get API from artifact ";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * Copy of the getAPI(GovernanceArtifact artifact, Registry registry) method with reduced DB calls for api
     * publisher list view listing.
     * @param artifact
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public static API getReducedPublisherAPIForListing(GovernanceArtifact artifact, Registry registry)
                                    throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set uuid
            api.setUUID(artifact.getId());

            // set url
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                                            .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
            }
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setProductionMaxTps(artifact.getAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
            api.setEnableSchemaValidation(Boolean.parseBoolean(
                                            artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));
            api.setEnableStore(Boolean.parseBoolean(
                                            artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                                            APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));

            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));

            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        }
        return api;
    }


    /**
     * Used to generate CORS Configuration object from CORS Configuration Json
     *
     * @param jsonString json representation of CORS configuration
     * @return CORSConfiguration Object
     */
    public static CORSConfiguration getCorsConfigurationDtoFromJson(String jsonString) {

        return new Gson().fromJson(jsonString, CORSConfiguration.class);

    }

    public static org.wso2.carbon.apimgt.api.model.CORSConfiguration getCorsConfigurationFromArtifact(
            GovernanceArtifact artifact) throws GovernanceException {

        org.wso2.carbon.apimgt.api.model.CORSConfiguration corsConfiguration = new Gson().fromJson(
                artifact.getAttribute(APIConstants.API_OVERVIEW_CORS_CONFIGURATION),
                org.wso2.carbon.apimgt.api.model.CORSConfiguration.class);
        return corsConfiguration;
    }
    
    /**
     * This method used to set selected deployment environment values to governance artifact of API .
     *
     * @param deployments DeploymentEnvironments attributes value
     */
    public static Set<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> extractDeploymentsForAPI(String deployments) {

        HashSet<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> deploymentEnvironmentsSet = new HashSet<>();
        if (deployments != null && !"null".equals(deployments)) {
            Type deploymentEnvironmentsSetType = new TypeToken<HashSet<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments>>() {
            }.getType();
            deploymentEnvironmentsSet = new Gson().fromJson(deployments, deploymentEnvironmentsSetType);
            return deploymentEnvironmentsSet;
        }
        return deploymentEnvironmentsSet;
    }
    
    public static List<Label> getLabelsFromAPIGovernanceArtifact(GovernanceArtifact artifact, String apiProviderName)
            throws GovernanceException, APIManagementException {

        String[] labelArray = artifact.getAttributes(APIConstants.API_LABELS_GATEWAY_LABELS);
        List<Label> gatewayLabelListForAPI = new ArrayList<>();

        if (labelArray != null && labelArray.length > 0) {
            //String tenantDomain = MultitenantUtils.getTenantDomain
            //        (replaceEmailDomainBack(apiProviderName));
            //List<Label> allLabelList = APIUtil.getAllLabels(tenantDomain);
            for (String labelName : labelArray) {
                Label label = new Label();
                //set the name
                label.setName(labelName);
                //set the description and access URLs
                /*for (Label currentLabel : allLabelList) {
                    if (labelName.equalsIgnoreCase(currentLabel.getName())) {
                        label.setDescription(currentLabel.getDescription());
                        label.setAccessUrls(currentLabel.getAccessUrls());
                    }
                }*/
                gatewayLabelListForAPI.add(label);
            }
        }
        return gatewayLabelListForAPI;
    }
    
    /**
     * This method used to extract environment list configured with non empty URLs.
     *
     * @param endpointConfigs (Eg: {"production_endpoints":{"url":"http://www.test.com/v1/xxx","config":null,
     *                        "template_not_supported":false},"endpoint_type":"http"})
     * @return Set<String>
     */
    public static Set<String> extractEnvironmentListForAPI(String endpointConfigs)
            throws ParseException, ClassCastException {

        Set<String> environmentList = new HashSet<String>();
        if (StringUtils.isNotBlank(endpointConfigs) && !"null".equals(endpointConfigs)) {
            JSONParser parser = new JSONParser();
            JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfigs);
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_PRODUCTION_ENDPOINTS) &&
                    isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_PRODUCTION_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_PRODUCTION);
            }
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_SANDBOX_ENDPOINTS) &&
                    isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_SANDBOX_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_SANDBOX);
            }
        }
        return environmentList;
    }
    /**
     * This method used to check whether the endpoints JSON object has a non empty URL.
     *
     * @param endpoints (Eg: {"url":"http://www.test.com/v1/xxx","config":null,"template_not_supported":false})
     * @return boolean
     */
    public static boolean isEndpointURLNonEmpty(Object endpoints) {

        if (endpoints instanceof JSONObject) {
            JSONObject endpointJson = (JSONObject) endpoints;
            if (endpointJson.containsKey(APIConstants.API_DATA_URL) &&
                    endpointJson.get(APIConstants.API_DATA_URL) != null) {
                String url = (endpointJson.get(APIConstants.API_DATA_URL)).toString();
                if (StringUtils.isNotBlank(url)) {
                    return true;
                }
            }
        } else if (endpoints instanceof JSONArray) {
            JSONArray endpointsJson = (JSONArray) endpoints;
            for (int i = 0; i < endpointsJson.size(); i++) {
                if (isEndpointURLNonEmpty(endpointsJson.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }
    

    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry      user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = RegistryPersistenceUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                    apiIdentifier.getProviderName());
        } else if (apiIdentifier instanceof APIProductIdentifier) {
            resourcePath =
                    RegistryPersistenceUtil.getAPIProductOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
        }

        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {

            String msg = "Error while retrieving OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                    + apiIdentifier.getVersion();
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Error while parsing OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                    + apiIdentifier.getVersion() + " in " + resourcePath;
            throw new APIManagementException(msg, e);
        }
        return apiDocContent;
    }

    public static String getOpenAPIDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider
                + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion
                + RegistryConstants.PATH_SEPARATOR;
    }
    
    public static String getAPIProductOpenAPIDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR;
    }
}
