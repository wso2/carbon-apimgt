package org.wso2.carbon.apimgt.persistence;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIPersistence;

import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RegistryPersistenceImpl implements APIPersistence {
    private static final Log log = LogFactory.getLog(RegistryPersistenceImpl.class);
    private static APIPersistence instance;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected Registry registry;
    protected String tenantDomain;
    protected UserRegistry configRegistry;
    protected String username;
    protected Organization organization;
    public static int myname = 0;

    public RegistryPersistenceImpl(String username) throws APIManagementException {
        //super(username);
        try {
            this.registry = getRegistryService().getGovernanceUserRegistry();

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
                this.organization = new Organization(tenantDomain, tenantId, "registry");
                this.username = tenantUserName;

                loadTenantRegistry(tenantId);

                this.registry = getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);

                //load resources for each tenants.
                loadloadTenantAPIRXT(tenantUserName, tenantId);
                loadTenantAPIPolicy(tenantUserName, tenantId);

// ===== Below  calls should be called at impls module
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
        } catch (RegistryException e) {

        } catch (UserStoreException e) {
            e.printStackTrace();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            e.printStackTrace();
        }
    }
    protected String getTenantAwareUsername(String username) {
        return MultitenantUtils.getTenantAwareUsername(username);
    }

    protected void loadTenantRegistry(int apiTenantId) throws RegistryException {
        TenantRegistryLoader tenantRegistryLoader = PersistenceManagerComponent.getTenantRegistryLoader();
        ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(apiTenantId);
        tenantRegistryLoader.loadTenantRegistry(apiTenantId);
    }

    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    /**
     * Load the  API RXT to the registry for tenants
     *
     * @param tenant
     * @param tenantID
     * @throws APIManagementException
     */

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
                                            getMountedPath(RegistryContext.getBaseInstance(),
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

            } catch (org.wso2.carbon.user.api.UserStoreException e) {
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

    @Override public API getAPI(String apiUUID) {
        return null;
    }

    @Override public API updateApi(API api) {
        return null;
    }

    @Override public void updateWsdlFromUrl(String apiId, String wsdlUrl) {

    }

    @Override public void updateWsdlFromUrl(API api) {

    }

    @Override public void updateDocVisibility(String apiId, String visibility, String visibleRoles,
                                    Documentation documentation) {

    }

    @Override public void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile) {

    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public void createAPI(API api) {

    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                    int end, boolean limitAttributes) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                    int end, boolean limitAttributes, boolean isPublisherListing) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery,
                                    int start, int end, boolean limitAttributes) {
        return null;
    }

    @Override public String getGraphqlSchema(String apiId) {
        return null;
    }

    @Override public void saveGraphqlSchemaDefinition(API api, String schemaDefinition) {

    }

    @Override public void saveGraphqlSchemaDefinition(String apiId, String visibleRoles, String schemaDefinition) {

    }

    @Override public void deleteAPI(String apiId) {

    }

    @Override public Documentation getDocumentation(String docId, Organization requestedOrg) throws APIManagementException {
        String requestedTenantDomain = requestedOrg.getName();
        Documentation documentation = null;
        try {
            Registry registryType;
            boolean isTenantMode = (requestedOrg.getName() != null);
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                                            requestedTenantDomain))) {
                int tenantId = getTenantManager()
                                                .getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                                                .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registryType, APIConstants
                                            .DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            if(artifact == null) {
                return documentation;
            }
            if (null != artifact) {
                documentation = getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get documentation details";
            throw new APIManagementException(msg, e);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return documentation;
    }

    // this method should be at somewhere else
    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    protected GenericArtifactManager getAPIGenericArtifactManagerFromUtil(Registry registry, String keyType)
                                    throws APIManagementException {
        return getArtifactManager(registry, keyType);
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key) throws APIManagementException {

        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, key);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + key +
                                                ". Tenant id set in registry : " + ((UserRegistry) registry).getTenantId() +
                                                ", Tenant domain set in PrivilegedCarbonContext: " +
                                                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
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

    @Override public Map<String, Object> getDocumentContent(String userName, Organization requestedOrg) {
        return null;
    }

    @Override public void removeDocumentation(String apiOrProductId, String docId) {

    }

    @Override public void updateDocumentation(String apiId, Documentation documentation) {

    }

    @Override public List<Documentation> getAllDocumentation(String apiOrProductId) {
        return null;
    }

    @Override public void addDocumentation(API api, Documentation documentation) {

    }

    @Override public API getLightweightAPIByUUID(String uuid, String requestedOrg) {
        return null;
    }

    @Override public Map<String, Object> getAPILifeCycleData(String apiId) {
        return null;
    }

    @Override public List<Mediation> getAllApiSpecificMediationPolicies(String apiId) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String apiOrProductId,
                                    String mediationPolicyUUID) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String mediationPolicyUUID) {
        return null;
    }

    @Override public void updateApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public void deleteApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public boolean checkIfMediationPolicyExists(String mediationPolicyId) {
        return false;
    }

    @Override public void addApiSpecificMediationPolicy(String apiOrProductId, String type, ResourceFile contentFile) {

    }

    @Override public void configureMonetizationInAPI(API api) {

    }

    @Override public void configureMonetizationInAPI(String apiId, JSONObject monetizationProperties,
                                    boolean isMonetizationEnabled) {

    }

    @Override public boolean isSOAPToRESTApi(String apiOrProductId) {
        return false;
    }

    @Override public String getRestToSoapConvertedSequence(String apiOrProductId, String seqType) {
        return null;
    }

    @Override public String getResourcePolicyFromResourceId(String apiId, String resourceId) {
        return null;
    }

    @Override public void updateResourcePolicyFromResourceId(String apiId, String resourceId, String content) {

    }

    @Override public String getOASDefinitionOfAPI(String apiOrProductId) {
        return null;
    }

    @Override public void saveOASAPIDefinition(String apiId, String apiDefinitionJSON) {

    }

    @Override public ResourceFile getIcon(String apiId) {
        return null;
    }

    @Override
    public void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail) {

    }

    @Override public boolean isDocumentationExist(String apiOrProductId, String docName) {
        return false;
    }

    @Override public ResourceFile getWSDL(String apiId) {
        return null;
    }

    @Override public void changeAPILifeCycle(String apiId, String status) {

    }

    @Override public int createNewAPIVersion(API api, String newVersion) {
        return 0;
    }

    @Override public void saveGraphQLSchemaDefinition(API api, String schemaDefinition) {

    }

    @Override public boolean isMediationPolicyExists(APIProvider apiProvider, String mediationPolicyUUID) {
        return false;
    }

    @Override public APIProduct getAPIProductbyUUID(String uuid, Organization requestedOrg) {
        return null;
    }

    @Override public APIProduct getAPIProduct(String apiProductId) {
        return null;
    }

    @Override public void deleteAPIProduct(String apiProductId) {

    }

    @Override public Documentation getProductDocumentation(String productId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public boolean isApiExists(APIIdentifier apiIdentifier) {
        return false;
    }
}
