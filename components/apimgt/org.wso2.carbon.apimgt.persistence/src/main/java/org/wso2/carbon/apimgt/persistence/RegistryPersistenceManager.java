package org.wso2.carbon.apimgt.persistence;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PersistenceManager;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.persistence.utils.PersistenceUtils;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.util.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.apimgt.api.model.API;

public class RegistryPersistenceManager implements PersistenceManager {
    private static final Log log = LogFactory.getLog(RegistryPersistenceManager.class);
    private static PersistenceManager instance;
    protected int tenantId = MultitenantConstants.INVALID_TENANT_ID; //-1 the issue does not occur.;
    protected Registry registry;

    public RegistryPersistenceManager() {
        try {
            this.registry = getRegistryService().getGovernanceUserRegistry();
        } catch (RegistryException e) {

        }
    }

    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    public static PersistenceManager getInstance() {
        if (instance == null) {
            synchronized (RegistryPersistenceManager.class) {
                if (instance == null) {
                    instance = new RegistryPersistenceManager();
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

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        /*
        String apiPath = APIUtil.getAPIPath(identifier);
        Registry registry;
        try {
            String apiTenantDomain = getTenantDomain(identifier);
            int apiTenantId = getTenantManager()
                                            .getTenantId(apiTenantDomain);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(apiTenantDomain)) {
                APIUtil.loadTenantRegistry(apiTenantId);
            }

            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) { //cross tenant scenario
                registry = getRegistryService().getGovernanceUserRegistry(
                                                getTenantAwareUsername(APIUtil.replaceEmailDomainBack(identifier.getProviderName())), apiTenantId);
            } else {
                registry = this.registry;
            }
            GenericArtifactManager artifactManager = getAPIGenericArtifactManagerFromUtil(registry,
                                            APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);

            API api = APIUtil.getAPIForPublishing(apiArtifact, registry);
            APIUtil.updateAPIProductDependencies(api, registry); //USE REG >> NO functionality ATM

            //check for API visibility
            if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) { //global api
                return api;
            }
            if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
                throw new APIManagementException("User " + username + " does not have permission to view API : "
                                                + api.getId().getApiName());
            }

            return api;

        } catch (RegistryException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Failed to get API from : " + apiPath;
            throw new APIManagementException(msg, e);
        }
        */
         return null;
    }

    @Override public void updateApi(API api) {

    }

    @Override public void updateWsdl() {

    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public void createAPI(API api) {

    }
}
