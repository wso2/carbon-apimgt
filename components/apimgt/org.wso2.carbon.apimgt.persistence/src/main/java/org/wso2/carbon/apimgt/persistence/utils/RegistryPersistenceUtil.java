package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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
            Set<DeploymentEnvironments> deploymentEnvironments = api.getDeploymentEnvironments();
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
    public static String getCorsConfigurationJsonFromDto(CORSConfiguration corsConfiguration) {

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
                                                + ". Tenant id set in registry : " + ((UserRegistry) registry)
                                                .getTenantId() + ", Tenant domain set in PrivilegedCarbonContext: "
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

        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
                                        File.separator + "rxts";
        File file = new File(rxtDir);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
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
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                                                (ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);

                org.wso2.carbon.user.api.AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                                                getTenantUserRealm(tenantID).getAuthorizationManager();

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

//        API api;
//        try {
//            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
//            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
//            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
//            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
//            int apiId = ApiMgtDAO.getInstance().getAPIID(apiIdentifier, null);
//
//            if (apiId == -1) {
//                return null;
//            }
//
//            api = new API(apiIdentifier);
//            //set uuid
//            api.setUUID(artifact.getId());
//            // set rating
//            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
//            api = setResourceProperties(api, registry, artifactPath);
//            api.setRating(getAverageRating(apiId));
//            //set description
//            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
//            //set last access time
//            api.setLastUpdated(registry.get(artifactPath).getLastModified());
//            // set url
//            api.setStatus(getLcStateFromArtifact(artifact));
//            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
//            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
//            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
//            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
//            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
//            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
//            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
//            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
//            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
//            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
//            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
//                                            APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
//            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
//                                            APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
//            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
//            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
//                                            .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
//                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
//            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
//                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
//            }
//            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
//            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
//            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
//            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
//            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
//            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
//            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
//            api.setProductionMaxTps(artifact.getAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS));
//            api.setSandboxMaxTps(artifact.getAttribute(APIConstants.API_SANDBOX_THROTTLE_MAXTPS));
//
//            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
//            try {
//                String strCacheTimeout = artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT);
//                if (strCacheTimeout != null && !strCacheTimeout.isEmpty()) {
//                    cacheTimeout = Integer.parseInt(strCacheTimeout);
//                }
//            } catch (NumberFormatException e) {
//                if (log.isWarnEnabled()) {
//                    log.warn("Error while retrieving cache timeout from the registry for " + apiIdentifier);
//                }
//                // ignore the exception and use default cache timeout value
//            }
//
//            api.setCacheTimeout(cacheTimeout);
//
//            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));
//
//            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
//            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
//            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
//            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
//            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
//            api.setSubscriptionAvailableTenants(artifact.getAttribute(
//                                            APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));
//
//            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
//            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
//                                            .getTenantId(tenantDomainName);
//
//            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
//                                            .getAPIManagerConfiguration();
//
//            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
//            api.setApiLevelPolicy(apiLevelTier);
//
//            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
//            Map<String, Tier> definedTiers = getTiers(tenantId);
//            Set<Tier> availableTier = getAvailableTiers(definedTiers, tiers, apiName);
//            api.addAvailableTiers(availableTier);
//
//            // This contains the resolved context
//            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
//            // We set the context template here
//            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
//            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
//            api.setEnableSchemaValidation(Boolean.parseBoolean(artifact.getAttribute(
//                                            APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));
//            api.setEnableStore(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
//            api.setTestKey(artifact.getAttribute(APIConstants.API_OVERVIEW_TESTKEY));
//
//            Map<String, Scope> scopeToKeyMapping = getAPIScopes(api.getId(), tenantDomainName);
//            api.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));
//
//            Set<URITemplate> uriTemplates = ApiMgtDAO.getInstance().getURITemplatesOfAPI(api.getId());
//
//            // AWS Lambda: get paths
//            OASParserUtil oasParserUtil = new OASParserUtil();
//            String resourceConfigsString = oasParserUtil.getAPIDefinition(apiIdentifier, registry);
//            JSONParser jsonParser = new JSONParser();
//            JSONObject paths = null;
//            if (resourceConfigsString != null) {
//                JSONObject resourceConfigsJSON = (JSONObject) jsonParser.parse(resourceConfigsString);
//                paths = (JSONObject) resourceConfigsJSON.get(APIConstants.SWAGGER_PATHS);
//            }
//
//            for (URITemplate uriTemplate : uriTemplates) {
//                String uTemplate = uriTemplate.getUriTemplate();
//                String method = uriTemplate.getHTTPVerb();
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
//                // AWS Lambda: set arn & timeout to URI template
//                if (paths != null) {
//                    JSONObject path = (JSONObject) paths.get(uTemplate);
//                    if (path != null) {
//                        JSONObject operation = (JSONObject) path.get(method.toLowerCase());
//                        if (operation != null) {
//                            if (operation.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME)) {
//                                uriTemplate.setAmznResourceName((String)
//                                                                operation.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_NAME));
//                            }
//                            if (operation.containsKey(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)) {
//                                uriTemplate.setAmznResourceTimeout(((Long)
//                                                                operation.get(APIConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT)).intValue());
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (APIConstants.IMPLEMENTATION_TYPE_INLINE.equalsIgnoreCase(api.getImplementation())) {
//                for (URITemplate template : uriTemplates) {
//                    template.setMediationScript(template.getAggregatedMediationScript());
//                }
//            }
//
//            api.setUriTemplates(uriTemplates);
//            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
//                                            APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
//            Set<String> tags = new HashSet<String>();
//            org.wso2.carbon.registry.core.Tag[] tag = registry.getTags(artifactPath);
//            for (Tag tag1 : tag) {
//                tags.add(tag1.getTagName());
//            }
//            api.addTags(tags);
//            api.setLastUpdated(registry.get(artifactPath).getLastModified());
//            api.setCreatedTime(String.valueOf(registry.get(artifactPath).getCreatedTime().getTime()));
//            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
//            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
//            api.setEnvironments(extractEnvironmentsForAPI(environments));
//            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
//            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
//            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
//            //set data and status related to monetization
//            api.setMonetizationStatus(Boolean.parseBoolean(artifact.getAttribute
//                                            (APIConstants.Monetization.API_MONETIZATION_STATUS)));
//            String monetizationInfo = artifact.getAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES);
//
//            //set selected clusters which API needs to be deployed
//            String deployments = artifact.getAttribute(APIConstants.API_OVERVIEW_DEPLOYMENTS);
//            Set<DeploymentEnvironments> deploymentEnvironments = extractDeploymentsForAPI(deployments);
//            if (deploymentEnvironments != null && !deploymentEnvironments.isEmpty()) {
//                api.setDeploymentEnvironments(deploymentEnvironments);
//            }
//
//            if (StringUtils.isNotBlank(monetizationInfo)) {
//                JSONParser parser = new JSONParser();
//                JSONObject jsonObj = (JSONObject) parser.parse(monetizationInfo);
//                api.setMonetizationProperties(jsonObj);
//            }
//            api.setGatewayLabels(getLabelsFromAPIGovernanceArtifact(artifact, api.getId().getProviderName()));
//            api.setApiCategories(getAPICategoriesFromAPIGovernanceArtifact(artifact, tenantId));
//            //get endpoint config string from artifact, parse it as a json and set the environment list configured with
//            //non empty URLs to API object
//            String keyManagers = artifact.getAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS);
//            if (StringUtils.isNotEmpty(keyManagers)) {
//                api.setKeyManagers(new Gson().fromJson(keyManagers, List.class));
//            } else {
//                api.setKeyManagers(Arrays.asList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
//            }
//            try {
//                api.setEnvironmentList(extractEnvironmentListForAPI(
//                                                artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)));
//            } catch (ParseException e) {
//                String msg = "Failed to parse endpoint config JSON of API: " + apiName + " " + apiVersion;
//                log.error(msg, e);
//                throw new APIManagementException(msg, e);
//            } catch (ClassCastException e) {
//                String msg = "Invalid endpoint config JSON found in API: " + apiName + " " + apiVersion;
//                log.error(msg, e);
//                throw new APIManagementException(msg, e);
//            }
//
//        } catch (GovernanceException e) {
//            String msg = "Failed to get API for artifact ";
//            throw new APIManagementException(msg, e);
//        } catch (RegistryException e) {
//            String msg = "Failed to get LastAccess time or Rating";
//            throw new APIManagementException(msg, e);
//        } catch (UserStoreException e) {
//            String msg = "Failed to get User Realm of API Provider";
//            throw new APIManagementException(msg, e);
//        } catch (ParseException e) {
//            String msg = "Failed to get parse monetization information.";
//            throw new APIManagementException(msg, e);
//        }
//        return api;
        return null;
    }
}
