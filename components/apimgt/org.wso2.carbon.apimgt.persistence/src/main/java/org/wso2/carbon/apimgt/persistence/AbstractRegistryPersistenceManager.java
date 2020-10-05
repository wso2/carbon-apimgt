package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.persistence.PersistenceUtil;

public abstract class AbstractRegistryPersistenceManager {
    protected Registry registry;
    protected UserRegistry configRegistry;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected String tenantDomain;
    protected String username;

    public AbstractRegistryPersistenceManager(String username) throws APIManagementException {
        try {
            if (username == null) {

                this.registry = getRegistryService().getGovernanceUserRegistry();
                this.configRegistry = getRegistryService().getConfigSystemRegistry();

                this.username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealm()));
            } else {
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String tenantUserName = getTenantAwareUsername(username);
                int tenantId = getTenantManager().getTenantId(tenantDomainName);
                this.tenantId = tenantId;
                this.tenantDomain = tenantDomainName;
                this.username = tenantUserName;

                loadTenantRegistry(tenantId);

                this.registry = getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);

//                //load resources for each tenants.
//                PersistenceUtil.loadloadTenantAPIRXT(tenantUserName, tenantId);
//                APIUtil.loadTenantAPIPolicy(tenantUserName, tenantId);
//
//                //Check whether GatewayType is "Synapse" before attempting to load Custom-Sequences into registry
//                APIManagerConfiguration configuration = getAPIManagerConfiguration();
//
//                String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
//
//                if (APIConstants.API_GATEWAY_TYPE_SYNAPSE.equalsIgnoreCase(gatewayType)) {
//                    APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
//                }

                ServiceReferenceHolder.setUserRealm((UserRealm) (ServiceReferenceHolder.getInstance().
                                                getRealmService().getTenantUserRealm(tenantId)));
            }
            ServiceReferenceHolder.setUserRealm(getRegistryService().getConfigSystemRegistry().getUserRealm());
            registerCustomQueries(configRegistry, username);
        } catch (RegistryException e) {
            String msg = "Error while obtaining registry objects";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Error while getting user registry for user:" + username;
            throw new APIManagementException(msg, e);
        }
    }

    protected void loadTenantRegistry(int apiTenantId) throws RegistryException {
        RegistryPersistenceUtil.loadTenantRegistry(apiTenantId);
    }
    // can send this to a super abstract class
    public API getAPI(APIIdentifier identifier) {

//        String apiPath = RegistryPersistenceUtil.getAPIPath(identifier);
//        Registry registry;
//        try {
//            String apiTenantDomain = RegistryPersistenceUtil.getTenantDomain(identifier);
//            int apiTenantId = getTenantManager().getTenantId(apiTenantDomain);
//            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(apiTenantDomain)) {
//                RegistryPersistenceUtil.loadTenantRegistry(apiTenantId);
//            }
//
//            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) { //cross tenant scenario
//                registry = getRegistryService().getGovernanceUserRegistry(
//                                                getTenantAwareUsername(RegistryPersistenceUtil.replaceEmailDomainBack(identifier.getProviderName())), apiTenantId);
//            } else {
//                registry = this.registry;
//            }
//            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
//                                            APIConstants.API_KEY);
//            Resource apiResource = registry.get(apiPath);
//            String artifactId = apiResource.getUUID();
//            if (artifactId == null) {
//                throw new APIManagementException("artifact id is null for : " + apiPath);
//            }
//            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
//
//            API api = RegistryPersistenceUtil.getAPIForPublishing(apiArtifact, registry);
//            RegistryPersistenceUtil.updateAPIProductDependencies(api, registry); //USE REG >> NO functionality ATM
//
//            //check for API visibility
//            if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) { //global api
//                return api;
//            }
//            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
//                throw new APIManagementException("User " + username + " does not have permission to view API : "
//                                                + api.getId().getApiName());
//            }
//
//            return api;
//
//        } catch (RegistryException e) {
//            String msg = "Failed to get API from : " + apiPath;
//            throw new APIManagementException(msg, e);
//        } catch (org.wso2.carbon.user.api.UserStoreException e) {
//            String msg = "Failed to get API from : " + apiPath;
//            throw new APIManagementException(msg, e);
//        }

        return null;
    }

    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    protected String getTenantAwareUsername(String username) {
        return MultitenantUtils.getTenantAwareUsername(username);
    }

    /**
     * method to register custom registry queries
     *
     * @param registry Registry instance to use
     * @throws RegistryException n error
     */
    protected void registerCustomQueries(UserRegistry registry, String username)
                                    throws RegistryException, APIManagementException {
        String tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
        String latestAPIsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/latest-apis";
        String resourcesByTag = RegistryConstants.QUERIES_COLLECTION_PATH + "/resource-by-tag";
        String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                        RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                        APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION);
        if (username == null) {
            try {
                UserRealm realm = ServiceReferenceHolder.getUserRealm();
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(realm);
                authorizationManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);

            } catch (UserStoreException e) {
                String msg = "Error while setting the permissions";
                throw new APIManagementException(msg, e);
            }
        } else if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            int tenantId;
            try {
                tenantId = getTenantManager().getTenantId(tenantDomain);
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                                                getTenantUserRealm(tenantId).getAuthorizationManager();
                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                String msg = "Error while setting the permissions";
                throw new APIManagementException(msg, e);
            }

        }

        if (!registry.resourceExists(tagsQueryPath)) {
            Resource resource = registry.newResource();

            //Tag Search Query
            //'MOCK_PATH' used to bypass ChrootWrapper -> filterSearchResult. A valid registry path is
            // a must for executeQuery results to be passed to client side
            String sql1 =
                                            "SELECT '" + RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                            APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION + "' AS MOCK_PATH, " +
                                                                            "   RT.REG_TAG_NAME AS TAG_NAME, " +
                                                                            "   COUNT(RT.REG_TAG_NAME) AS USED_COUNT " +
                                                                            "FROM " +
                                                                            "   REG_RESOURCE_TAG RRT, " +
                                                                            "   REG_TAG RT, " +
                                                                            "   REG_RESOURCE R, " +
                                                                            "   REG_RESOURCE_PROPERTY RRP, " +
                                                                            "   REG_PROPERTY RP " +
                                                                            "WHERE " +
                                                                            "   RT.REG_ID = RRT.REG_TAG_ID  " +
                                                                            "   AND R.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                                                                            "   AND RRT.REG_VERSION = R.REG_VERSION " +
                                                                            "   AND RRP.REG_VERSION = R.REG_VERSION " +
                                                                            "   AND RP.REG_NAME = 'STATUS' " +
                                                                            "   AND RRP.REG_PROPERTY_ID = RP.REG_ID " +
                                                                            "   AND (RP.REG_VALUE !='DEPRECATED' AND RP.REG_VALUE !='CREATED' AND RP.REG_VALUE !='BLOCKED' AND RP.REG_VALUE !='RETIRED') " +
                                                                            "GROUP BY " +
                                                                            "   RT.REG_TAG_NAME";
            resource.setContent(sql1);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                                            RegistryConstants.TAG_SUMMARY_RESULT_TYPE);
            registry.put(tagsQueryPath, resource);
        }
        if (!registry.resourceExists(latestAPIsQueryPath)) {
            //Recently added APIs
            Resource resource = registry.newResource();
            String sql =
                                            "SELECT " +
                                                                            "   RR.REG_PATH_ID AS REG_PATH_ID, " +
                                                                            "   RR.REG_NAME AS REG_NAME " +
                                                                            "FROM " +
                                                                            "   REG_RESOURCE RR, " +
                                                                            "   REG_RESOURCE_PROPERTY RRP, " +
                                                                            "   REG_PROPERTY RP " +
                                                                            "WHERE " +
                                                                            "   RR.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                                                                            "   AND RRP.REG_VERSION = RR.REG_VERSION " +
                                                                            "   AND RP.REG_NAME = 'STATUS' " +
                                                                            "   AND RRP.REG_PROPERTY_ID = RP.REG_ID " +
                                                                            "   AND (RP.REG_VALUE !='DEPRECATED' AND RP.REG_VALUE !='CREATED') " +
                                                                            "ORDER BY " +
                                                                            "   RR.REG_LAST_UPDATED_TIME " +
                                                                            "DESC ";
            resource.setContent(sql);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                                            RegistryConstants.RESOURCES_RESULT_TYPE);
            registry.put(latestAPIsQueryPath, resource);
        }
        if (!registry.resourceExists(resourcesByTag)) {
            Resource resource = registry.newResource();
            String sql =
                                            "SELECT '" + RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                            APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION + "' AS MOCK_PATH, " +
                                                                            "   R.REG_UUID AS REG_UUID " +
                                                                            "FROM " +
                                                                            "   REG_RESOURCE_TAG RRT, " +
                                                                            "   REG_TAG RT, " +
                                                                            "   REG_RESOURCE R, " +
                                                                            "   REG_PATH RP " +
                                                                            "WHERE " +
                                                                            "   RT.REG_TAG_NAME = ? " +
                                                                            "   AND R.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
                                                                            "   AND RP.REG_PATH_ID = R.REG_PATH_ID " +
                                                                            "   AND RT.REG_ID = RRT.REG_TAG_ID " +
                                                                            "   AND RRT.REG_VERSION = R.REG_VERSION ";

            resource.setContent(sql);
            resource.setMediaType(RegistryConstants.SQL_QUERY_MEDIA_TYPE);
            resource.addProperty(RegistryConstants.RESULT_TYPE_PROPERTY_NAME,
                                            RegistryConstants.RESOURCE_UUID_RESULT_TYPE);
            registry.put(resourcesByTag, resource);
        }
    }

    protected GenericArtifactManager getAPIGenericArtifactManagerFromUtil(Registry registry, String keyType)
                                    throws APIManagementException {
        return  null; // return  PersistenceUtil.getArtifactManager(registry, keyType);
    }
}
