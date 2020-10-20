package org.wso2.carbon.apimgt.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;
import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.attachLabelsToAPIArtifact;
import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.updateWSDLUriInAPIArtifact;

public class RegistryPersistenceImpl implements APIPersistence {
    private static final Log log = LogFactory.getLog(RegistryPersistenceImpl.class);
    private static APIPersistence instance;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected Registry registry;
    protected String tenantDomain;
    protected UserRegistry configRegistry;
    protected String username;
    protected Organization organization;
    private RegistryService registryService;
    private GenericArtifactManager apiGenericArtifactManager;

    public RegistryPersistenceImpl(String username) {
        //super(username);
        this.registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            // is it ok to reuse artifactManager object TODO : resolve this concern
            // this.registry = getRegistryService().getGovernanceUserRegistry();


            if (username == null) {

                this.registry = getRegistryService().getGovernanceUserRegistry();
                this.configRegistry = getRegistryService().getConfigSystemRegistry();

                this.username = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((ServiceReferenceHolder.getInstance().getRealmService()
                                                .getBootstrapRealm()));
                this.apiGenericArtifactManager = RegistryPersistenceUtil.getArtifactManager(this.registry,
                                                APIConstants.API_KEY);
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
                this.apiGenericArtifactManager = RegistryPersistenceUtil.getArtifactManager(this.registry,
                                                APIConstants.API_KEY);
            }
        } catch (RegistryException e) {

        } catch (UserStoreException e) {
            e.printStackTrace();
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
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
                                            getMountedPath(RegistryContext.getBaseInstance(),
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

    //    @Override public API getAPI(String apiUUID) {
    //        return null;
    //    }

    /* Setting WorkflowStatus property to the API object returned by this methods will be useful in some occations. Set
     it when required on the value returned from this method */
    @Override
    public API getAPIbyId(String apiUuid, String requestedTenantDomain) throws APIManagementException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            if (requestedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(requestedTenantDomain)) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                registry = getRegistryService().getGovernanceSystemRegistry(tenantId);
            } else {
                if (this.tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                                .equals(this.tenantDomain)) {
                    // at this point, requested tenant = carbon.super but logged in user is anonymous or tenant
                    registry = getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    // both requested tenant and logged in user's tenant are carbon.super
                    registry = this.registry;
                }
            }

            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                                            APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiUuid);
            if (apiArtifact != null) {
                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);

                /* Setting this  property will be useful in some occations. Set it whe required on the value returned
                  from this method API getAPIbyId(..)
                APIIdentifier apiIdentifier = api.getId();
                WorkflowDTO workflowDTO = APIUtil.getAPIWorkflowStatus(apiIdentifier, WF_TYPE_AM_API_STATE);
                if (workflowDTO != null) {
                    WorkflowStatus status = workflowDTO.getStatus();
                    api.setWorkflowStatus(status.toString());
                }*/
                return api;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + apiUuid + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API";
            throw new APIManagementException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
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

    @Override public void saveGraphqlSchemaDefinition(String apiId, String schemaDefinition) {

    }

    @Override public void deleteAPI(String apiId) {

    }

    @Override public Documentation getDocumentation(String apiId, String docId, Organization requestedOrg)
                                    throws APIManagementException {
        String requestedTenantDomain = requestedOrg.getName();
        Documentation documentation = null;
        try {
            Registry registryType;
            boolean isTenantMode = (requestedOrg.getName() != null);
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null) || (isTenantMode && isTenantDomainNotMatching(
                                            requestedTenantDomain))) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                                                .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME,
                                                                                tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registryType,
                                            APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            if (artifact == null) {
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

    @Override public void removeDocumentation(String apiOrProductId, String docId) {

    }

    @Override public void updateDocumentation(String apiId, Documentation documentation) {

    }

    @Override public List<Documentation> getAllDocumentation(String apiOrProductId) {
        return null;
    }

    @Override public void addDocumentation(String apiId, Documentation documentation) {

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

    @Override public boolean isMediationPolicyExists(String mediationPolicyId) {
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

    @Override public void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail) {

    }

    @Override public boolean isDocumentationExists(String apiOrProductId, String docName) {
        return false;
    }

    @Override public void changeAPILifeCycle(String apiId, String status) {

    }

    @Override public int createNewAPIVersion(API api, String newVersion) {
        return 0;
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

    @Override public Map<String, Object> getDocumentContent(String apiId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public boolean isApiExists(APIIdentifier apiIdentifier) {
        return false;
    }

//    @Override public void createAPI(API api) throws APIManagementException {
//    }

    @Override public API createAPI(API api) throws APIManagementException {
        //Add default API LC if it is not there
        try {
            if (!CommonUtil.lifeCycleExists(APIConstants.API_LIFE_CYCLE,
                                            registryService.getConfigSystemRegistry(tenantId))) {
                String defaultLifecyclePath = CommonUtil.getDefaltLifecycleConfigLocation() + File.separator
                                                + APIConstants.API_LIFE_CYCLE + APIConstants.XML_EXTENSION;
                File file = new File(defaultLifecyclePath);
                String content = null;
                if (file != null && file.exists()) {
                    content = FileUtils.readFileToString(file);
                }
                if (content != null) {
                    CommonUtil.addLifecycle(content, registryService.getConfigSystemRegistry(tenantId),
                                                    CommonUtil.getRootSystemRegistry(tenantId));
                }
            }
        } catch (RegistryException e) {
            handleException("Error occurred while adding default APILifeCycle.", e);
        } catch (IOException e) {
            handleException("Error occurred while loading APILifeCycle.xml.", e);
        } catch (XMLStreamException e) {
            handleException("Error occurred while adding default API LifeCycle.", e);
        }

        if (apiGenericArtifactManager == null) {
            String errorMessage = "Failed to retrieve artifact manager when creating API " + api.getId().getApiName();
            log.error(errorMessage);
            throw new APIManagementException(errorMessage);
        }

        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact = apiGenericArtifactManager
                                            .newGovernanceArtifact(new QName(api.getId().getApiName()));
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact artifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            apiGenericArtifactManager.addGenericArtifact(artifact);
            //Attach the API lifecycle
            artifact.attachLifecycle(APIConstants.API_LIFE_CYCLE);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = RegistryPersistenceUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            //            if (PersistenceUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
            //                String path = RegistryPersistenceUtil.createWSDL(registry, api);
            //                //updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }
            //
            //            if (api.getWsdlResource() != null) {
            //                String path = APIUtil.saveWSDLResource(registry, api);
            //                //updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }
           // // RegistryPersistenceUtil.attachLabelsToAPIArtifact(artifact, api, tenantDomain, gatewayLabelList);
            String apiStatus = api.getStatus();
            saveAPIStatus(artifactPath, apiStatus);

            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }

            String publisherAccessControlRoles = api.getAccessControlRoles();
            updateRegistryResources(artifactPath, publisherAccessControlRoles, api.getAccessControl(),
                                            api.getAdditionalProperties());
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, artifactPath, registry);

            registry.commitTransaction();
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                String logMessage = "API Name: " + api.getId().getApiName() + ", API Version " + api.getId()
                                                .getVersion() + " created";
                log.debug(logMessage);
            }
            if (log.isDebugEnabled()) {
                log.debug("API details successfully added to the registry. API Name: " + api.getId().getApiName()
                                                + ", API Version : " + api.getId().getVersion() + ", API context : "
                                                + api.getContext());
            }
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName(), re);
            }
            handleException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            handleException("Error while creating API", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                handleException("Error while rolling back the transaction for API: " + api.getId().getApiName(), ex);
            }
        }
        //Need to call get api and return the created api
        return null;
    }

    /**
     * To add API/Product roles restrictions and add additional properties.
     *
     * @param artifactPath                Path of the API/Product artifact.
     * @param publisherAccessControlRoles Role specified for the publisher access control.
     * @param publisherAccessControl      Publisher Access Control restriction.
     * @param additionalProperties        Additional properties that is related with an API/Product.
     * @throws RegistryException Registry Exception.
     */
    private void updateRegistryResources(String artifactPath, String publisherAccessControlRoles,
                                    String publisherAccessControl, Map<String, String> additionalProperties)
                                    throws RegistryException {
        publisherAccessControlRoles = (publisherAccessControlRoles == null || publisherAccessControlRoles.trim()
                                        .isEmpty()) ? APIConstants.NULL_USER_ROLE_LIST : publisherAccessControlRoles;
        if (publisherAccessControlRoles.equalsIgnoreCase(APIConstants.NULL_USER_ROLE_LIST)) {
            publisherAccessControl = APIConstants.NO_ACCESS_CONTROL;
        }
        if (!registry.resourceExists(artifactPath)) {
            return;
        }

        Resource apiResource = registry.get(artifactPath);
        if (apiResource != null) {
            if (additionalProperties != null) {
                // Removing all the properties, before updating new properties.
                Properties properties = apiResource.getProperties();
                if (properties != null) {
                    Enumeration propertyNames = properties.propertyNames();
                    while (propertyNames.hasMoreElements()) {
                        String propertyName = (String) propertyNames.nextElement();
                        if (propertyName.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
                            apiResource.removeProperty(propertyName);
                        }
                    }
                }
            }
            // We are changing to lowercase, as registry search only supports lower-case characters.
            apiResource.setProperty(APIConstants.PUBLISHER_ROLES, publisherAccessControlRoles.toLowerCase());

            // This property will be only used for display proposes in the Publisher UI so that the original case of
            // the roles that were specified can be maintained.
            apiResource.setProperty(APIConstants.DISPLAY_PUBLISHER_ROLES, publisherAccessControlRoles);
            apiResource.setProperty(APIConstants.ACCESS_CONTROL, publisherAccessControl);
            apiResource.removeProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY);
            if (additionalProperties != null && additionalProperties.size() != 0) {
                for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                    apiResource.setProperty((APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX + entry.getKey()),
                                                    entry.getValue());
                }
            }
            registry.put(artifactPath, apiResource);
        }
    }

    @Override public String createWsdl(API api, InputStream wsdlContent, OMElement wsdlContentEle) {
        String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                        .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                        api.getId().getVersion());

        String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;

        //        APIMWSDLReader wsdlReader = new APIMWSDLReader();
        //        OMElement wsdlContentEle;
        String wsdlRegistryPath;

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                        .equalsIgnoreCase(tenantDomain)) {
            wsdlRegistryPath = RegistryConstants.PATH_SEPARATOR + "registry" + RegistryConstants.PATH_SEPARATOR
                                            + "resource" + absoluteWSDLResourcePath;
        } else {
            wsdlRegistryPath = "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                                            + RegistryConstants.PATH_SEPARATOR + "resource" + absoluteWSDLResourcePath;
        }

        try {
            Resource wsdlResource = registry.newResource();
            // isWSDL2Document(api.getWsdlUrl()) method only understands http or file system urls.
            // Hence if this is a registry url, should not go in to the following if block
            if (!api.getWsdlUrl().matches(wsdlRegistryPath) && (api.getWsdlUrl().startsWith("http:") || api.getWsdlUrl()
                                            .startsWith("https:") || api.getWsdlUrl().startsWith("file:") || api
                                            .getWsdlUrl().startsWith("/t"))) {
                // Get the WSDL 1.1 or 2.0 processor and process the content based on the version
                wsdlResource.setContentStream(wsdlContent);

            } else {
                byte[] wsdl = (byte[]) registry.get(wsdlResourcePath).getContent();
                if (PersistenceUtil.isWSDL2Resource(wsdl)) {
                    wsdlResource.setContent(wsdlContentEle.toString());
                } else {
                    wsdlResource.setContent(wsdlContentEle.toString());
                }
            }
            registry.put(wsdlResourcePath, wsdlResource);
            //set the anonymous role for wsld resource to avoid basicauth security.
            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, wsdlResourcePath);

            //Delete any WSDL archives if exists
            String wsdlArchivePath = RegistryPersistenceUtil.getWsdlArchivePath(api.getId());
            if (registry.resourceExists(wsdlArchivePath)) {
                registry.delete(wsdlArchivePath);
            }

            //check availability of below 2 lines
            //set the wsdl resource permlink as the wsdlURL.
            api.setWsdlUrl(RegistryPersistenceUtil.getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));

            GenericArtifactManager artifactManager = RegistryPersistenceUtil
                                            .getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "";
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }

            GenericArtifact genericArtifact = artifactManager
                                            .newGovernanceArtifact(new QName(api.getId().getApiName()));

            GenericArtifact artifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());

            if (PersistenceUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
                updateWSDLUriInAPIArtifact(wsdlRegistryPath, artifactManager, artifact, artifactPath);
            }

            //            if (api.getWsdlResource() != null) {
            //                String path = APIUtil.saveWSDLResource(registry, api);
            //                updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            //            }

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public ResourceFile getWSDL(String apiId) {
        try {
            API api = getAPIbyId(apiId, tenantDomain);
            String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                            .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                            api.getId().getVersion());

            Resource wsdlResource = registry.get(wsdlResourcePath);
            return new ResourceFile(wsdlResource.getContentStream(), wsdlResource.getMediaType());
        } catch (APIManagementException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public String updateWsdlFromWsdlFile(String apiId, ResourceFile wsdlResourceFile)
                                    throws APIManagementException {
        API api = null;
        try {
            // GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);

            api = getAPIbyId(apiId, tenantDomain);

            GenericArtifact genericArtifact = null;
            GenericArtifact apiArtifact = null;
            String apiArtifactPath = null;
            genericArtifact = apiGenericArtifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));

            if (genericArtifact == null) {
                String errorMessage = "";
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            apiArtifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            apiArtifactPath = GovernanceUtils.getArtifactPath(registry, apiArtifact.getId());

            String wsdlResourcePath;
            boolean isZip = false;

            String wsdlResourcePathArchive = APIConstants.API_WSDL_RESOURCE_LOCATION
                                            + APIConstants.API_WSDL_ARCHIVE_LOCATION + api.getId().getProviderName()
                                            + APIConstants.WSDL_PROVIDER_SEPERATOR + api.getId().getApiName() + api
                                            .getId().getVersion() + APIConstants.ZIP_FILE_EXTENSION;
            String wsdlResourcePathFile = APIConstants.API_WSDL_RESOURCE_LOCATION + PersistenceUtil
                                            .createWsdlFileName(api.getId().getProviderName(), api.getId().getApiName(),
                                                                            api.getId().getVersion());

            if (wsdlResourceFile.getContentType().equals(APIConstants.APPLICATION_ZIP)) {
                wsdlResourcePath = wsdlResourcePathArchive;
                isZip = true;
            } else {
                wsdlResourcePath = wsdlResourcePathFile;
            }

            String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                            RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + wsdlResourcePath;

            Resource wsdlResourceToUpdate = registry.newResource();
            wsdlResourceToUpdate.setContentStream(api.getWsdlResource().getContent());
            wsdlResourceToUpdate.setMediaType(api.getWsdlResource().getContentType());
            registry.put(wsdlResourcePath, wsdlResourceToUpdate);
            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                                            visibleRoles, wsdlResourcePath);

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

            api.setWsdlUrl(RegistryPersistenceUtil.getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));

            RegistryPersistenceUtil.updateWSDLUriInAPIArtifact(wsdlResourcePath, apiGenericArtifactManager, apiArtifact,
                                            apiArtifactPath);
            return wsdlResourcePath;

        } catch (RegistryException e) {
            String msg = "Failed to add WSDL Archive " + api.getWsdlUrl() + " to the registry";
            log.error(msg, e);
            //throw new RegistryException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to process the WSDL Archive: " + api.getWsdlUrl();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus  Current status of the API
     * @throws APIManagementException on error
     */
    // HAS REG USAGE
    private void saveAPIStatus(String artifactId, String apiStatus) throws APIManagementException {
        try {
            Resource resource = registry.get(artifactId);
            if (resource != null) {
                String propValue = resource.getProperty(APIConstants.API_STATUS);
                if (propValue == null) {
                    resource.addProperty(APIConstants.API_STATUS, apiStatus);
                } else {
                    resource.setProperty(APIConstants.API_STATUS, apiStatus);
                }
                registry.put(artifactId, resource);
            }
        } catch (RegistryException e) {
            handleException("Error while adding API", e);
        }
    }
}
