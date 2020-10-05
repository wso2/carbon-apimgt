package org.wso2.carbon.apimgt.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PersistenceManager;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.apimgt.api.model.API;

import java.util.Map;

public class RegistryPersistenceManager extends AbstractRegistryPersistenceManager implements PersistenceManager {
    private static final Log log = LogFactory.getLog(RegistryPersistenceManager.class);
    private static PersistenceManager instance;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected Registry registry;
    protected String tenantDomain;

    public RegistryPersistenceManager(String username) throws APIManagementException {
        super(username);
        try {
            this.registry = getRegistryService().getGovernanceUserRegistry();
        } catch (RegistryException e) {

        }
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    public static PersistenceManager getInstance(String username) throws APIManagementException {
        if (instance == null) {
            synchronized (RegistryPersistenceManager.class) {
                if (instance == null) {
                    instance = new RegistryPersistenceManager(username);
                }
            }
        }
        return instance;
    }

    /*
    @Override
    public void addLifeCycle(API api) {
        log.info("In RegistryPersistenceManager");
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();

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
            //handleException("Error occurred while adding default APILifeCycle.", e);
        } catch (IOException e) {
           // handleException("Error occurred while loading APILifeCycle.xml.", e);
        } catch (XMLStreamException e) {
            //handleException("Error occurred while adding default API LifeCycle.", e);
        }
        String tenantDomain = MultitenantUtils
                                        .getTenantDomain(PersistenceUtils.replaceEmailDomainBack(api.getId().getProviderName()));
    }
*/
    /*
    @Override public void createAPI(API api) { //throws APIManagementException
        GenericArtifactManager artifactManager = PersistenceUtils.getArtifactManager(registry, APIConstants.API_KEY);
        if (artifactManager == null) {
            String errorMessage = "Failed to retrieve artifact manager when creating API " + api.getId().getApiName();
            log.error(errorMessage);
           // throw new APIManagementException(errorMessage);
        }
        try {
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                                            artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact artifact = PersistenceUtils.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
            //Attach the API lifecycle
            artifact.attachLifecycle(APIConstants.API_LIFE_CYCLE);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIUtil.getAPIProviderPath(api.getId());
            //provider ------provides----> API
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);
            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            if (APIUtil.isValidWSDLURL(api.getWsdlUrl(), false)) {
                String path = APIUtil.createWSDL(registry, api);
                updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            }

            if (api.getWsdlResource() != null) {
                String path = APIUtil.saveWSDLResource(registry, api);
                updateWSDLUriInAPIArtifact(path, artifactManager, artifact, artifactPath);
            }

            //attaching micro-gateway labels to the API
            APIUtil.attachLabelsToAPIArtifact(artifact, api, tenantDomain);

            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
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
            APIUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles,
                                            artifactPath, registry);

            registry.commitTransaction();
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                String logMessage =
                                                "API Name: " + api.getId().getApiName() + ", API Version " + api.getId().getVersion()
                                                                                + " created";
                log.debug(logMessage);
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
    }
    */

    @Override public void updateWsdlFromResourceFile() {

    }

    @Override public API getAPI(String apiUUID) {
        return null;
    }

    @Override public void updateApi(API api) {

    }

    @Override public void updateWsdlFromUrl(APIIdentifier apiIdentifier, String wsdlUrl) {

    }

    @Override public void updateWsdlFromWsdlFile(APIIdentifier apiIdentifier, ResourceFile wsdlResourceFile) {

    }

    @Override public void updateWsdlFromUrl(API api) {

    }

    @Override public void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile) {

    }

    @Override public void updateDocVisibility(APIIdentifier apiIdentifier, String visibility, String visibleRoles,
                                    Documentation documentation) {

    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public void createAPI(API api) {

    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, String orgName, int start, int end,
                                    boolean limitAttributes) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, String tenantDomain, int start,
                                    int end, boolean limitAttributes, boolean isPublisherListing) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIsByContent(int tenantId, String searchQuery, int start,
                                    int end, boolean limitAttributes) {
        return null;
    }
}
