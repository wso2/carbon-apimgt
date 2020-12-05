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
package org.wso2.carbon.apimgt.persistence;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent.ContentSourceType;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.ThumbnailPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.WSDLPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistanceDocUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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
        this.registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        this.username = username;
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
                this.organization = new Organization(Integer.toString(tenantId), tenantDomain, "registry");
                this.username = tenantUserName;

                loadTenantRegistry(tenantId);

                this.registry = getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);

                this.configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);

                //load resources for each tenants.
                RegistryPersistenceUtil.loadloadTenantAPIRXT(tenantUserName, tenantId);
                RegistryPersistenceUtil.loadTenantAPIPolicy(tenantUserName, tenantId);

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
        } catch (RegistryException e) { //TODO fix these

        } catch (UserStoreException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        } catch (APIPersistenceException e) {
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
    
    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        
        API api = APIMapper.INSTANCE.toApi(publisherAPI);
        if (apiGenericArtifactManager == null) {
            String errorMessage = "Failed to retrieve artifact manager when creating API " + api.getId().getApiName();
            log.error(errorMessage);
            throw new APIPersistenceException(errorMessage);
        }
        boolean transactionCommitted = false;
        try {
            
            registry.beginTransaction();
            GenericArtifact genericArtifact =
                    apiGenericArtifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
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
            
            List<Label> candidateLabelsList = api.getGatewayLabels();
            if (candidateLabelsList != null) {
                for (Label label : candidateLabelsList) {
                    artifact.addAttribute(APIConstants.API_LABELS_GATEWAY_LABELS, label.getName());
                }
            }

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

            if (api.getSwaggerDefinition() != null) {
                String resourcePath = RegistryPersistenceUtil.getOpenAPIDefinitionFilePath(api.getId().getName(),
                        api.getId().getVersion(), api.getId().getProviderName());
                resourcePath = resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
                Resource resource;
                if (!registry.resourceExists(resourcePath)) {
                    resource = registry.newResource();
                } else {
                    resource = registry.get(resourcePath);
                }
                resource.setContent(api.getSwaggerDefinition());
                resource.setMediaType("application/json");
                registry.put(resourcePath, resource);
                //Need to set anonymous if the visibility is public
                RegistryPersistenceUtil.clearResourcePermissions(resourcePath, api.getId(),
                        ((UserRegistry) registry).getTenantId());
                RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                        visibleRoles, resourcePath);
            }
            
            //Set permissions to doc path
            String docLocation = RegistryPersistanceDocUtil.getDocumentPath(api.getId().getProviderName(),
                    api.getId().getApiName(), api.getId().getVersion());
            RegistryPersistenceUtil.clearResourcePermissions(docLocation, api.getId(),
                    ((UserRegistry) registry).getTenantId());
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                    visibleRoles, docLocation);
            
            registry.commitTransaction();
            api.setUuid(artifact.getId());
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                log.debug("API details successfully added to the registry. API Name: " + api.getId().getApiName()
                        + ", API Version : " + api.getId().getVersion() + ", API context : " + api.getContext());
            }
            api.setCreatedTime(String.valueOf(new Date().getTime()));// set current time as created time for returning api.
            PublisherAPI returnAPI = APIMapper.INSTANCE.toPublisherApi(api);
            if (log.isDebugEnabled()) {
                log.debug("Created API :" + returnAPI.toString());
            }
            return returnAPI;
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName(), re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while creating API", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                throw new APIPersistenceException(
                        "Error while rolling back the transaction for API: " + api.getId().getApiName(), ex);
            }
        }
    }

    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        API api = APIMapper.INSTANCE.toApi(publisherAPI);

        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            String apiArtifactId = registry.get(RegistryPersistenceUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when updating API artifact ID " + api.getId();
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);

            boolean isSecured = Boolean.parseBoolean(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED));
            boolean isDigestSecured = Boolean.parseBoolean(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST));
            String userName = artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME);
            String password = artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD);
   
            if (!isSecured && !isDigestSecured && userName != null) {
                api.setEndpointUTUsername(userName);
                api.setEndpointUTPassword(password);
            }

            String oldStatus = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            Resource apiResource = registry.get(artifact.getPath());
            String oldAccessControlRoles = api.getAccessControlRoles();
            if (apiResource != null) {
                oldAccessControlRoles = registry.get(artifact.getPath()).getProperty(APIConstants.PUBLISHER_ROLES);
            }
            GenericArtifact updateApiArtifact = RegistryPersistenceUtil.createAPIArtifactContent(artifact, api);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, updateApiArtifact.getId());
            org.wso2.carbon.registry.core.Tag[] oldTags = registry.getTags(artifactPath);
            if (oldTags != null) {
                for (org.wso2.carbon.registry.core.Tag tag : oldTags) {
                    registry.removeTag(artifactPath, tag.getTagName());
                }
            }
            Set<String> tagSet = api.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            if (api.isDefaultVersion()) {
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "true");
            } else {
                updateApiArtifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, "false");
            }


            artifactManager.updateGenericArtifact(updateApiArtifact);

            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            String apiStatus = api.getStatus().toUpperCase();
            //saveAPIStatus(artifactPath, apiStatus);
            String[] visibleRoles = new String[0];
            String publisherAccessControlRoles = api.getAccessControlRoles();

            updateRegistryResources(artifactPath, publisherAccessControlRoles, api.getAccessControl(),
                    api.getAdditionalProperties());

            //propagate api status change and access control roles change to document artifact
            String newStatus = updateApiArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (!StringUtils.equals(oldStatus, newStatus) || !StringUtils.equals(oldAccessControlRoles, publisherAccessControlRoles)) {
                RegistryPersistenceUtil.notifyAPIStateChangeToAssociatedDocuments(artifact, registry);
            }

            // TODO Improve: add a visibility change check and only update if needed
            RegistryPersistenceUtil.clearResourcePermissions(artifactPath, api.getId(),
                    ((UserRegistry) registry).getTenantId());
            String visibleRolesList = api.getVisibleRoles();

            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                    visibleRoles, artifactPath, registry);
            
            //attaching api categories to the API
            List<APICategory> attachedApiCategories = api.getApiCategories();
            artifact.removeAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME);
            if (attachedApiCategories != null) {
                for (APICategory category : attachedApiCategories) {
                    artifact.addAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME, category.getName());
                }
            }
            
            if (api.getSwaggerDefinition() != null) {
                String resourcePath = RegistryPersistenceUtil.getOpenAPIDefinitionFilePath(api.getId().getName(),
                        api.getId().getVersion(), api.getId().getProviderName());
                resourcePath = resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
                Resource resource;
                if (!registry.resourceExists(resourcePath)) {
                    resource = registry.newResource();
                } else {
                    resource = registry.get(resourcePath);
                }
                resource.setContent(api.getSwaggerDefinition());
                resource.setMediaType("application/json");
                registry.put(resourcePath, resource);
                //Need to set anonymous if the visibility is public
                RegistryPersistenceUtil.clearResourcePermissions(resourcePath, api.getId(),
                        ((UserRegistry) registry).getTenantId());
                RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                        visibleRoles, resourcePath);
            }
            registry.commitTransaction();
            transactionCommitted = true;
            return APIMapper.INSTANCE.toPublisherApi(api);
        } catch (Exception e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error from this level will mask the original exception
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName(), re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation ", e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                throw new APIPersistenceException("Error occurred while rolling back the transaction. ", ex);
            }
        }
    }
    
    

    /**
     * Things to populate manually -
     *      apistatus: check getAPIbyUUID() in abstractapimanager
     *      apiid: getAPIForPublishing(GovernanceArtifact artifact, Registry registry) in apiutil
     *      api.setRating ---
     *      api.setApiLevelPolicy --
     *       api.addAvailableTiers --
     *       api.setScopes --
     *       api.setUriTemplates --
     *       api.setEnvironments == read from config---
     *       api.setCorsConfiguration = if null get from configs
     *       api.setGatewayLabels == label name is set. other stuff seems not needed. if needed set them
     *      
     */
    @Override
    public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            String requestedTenantDomain = org.getName();
            if (requestedTenantDomain  != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
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

            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                                            APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            if (apiArtifact != null) {
                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                api.setSwaggerDefinition(this.getOASDefinition(org, apiId));
                //TODO directly map to PublisherAPI from the registry
                PublisherAPI pubApi = APIMapper.INSTANCE.toPublisherApi(api) ; 
                if (log.isDebugEnabled()) {
                    log.debug("API for id " + apiId + " : " + pubApi.toString());
                }
                return pubApi;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + apiId + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } catch (OASPersistenceException e) {
            String msg = "Failed to retrieve OpenAPI definition for the API";
            throw new APIPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }
    
    @Override
    public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            Registry registry;
            String requestedTenantDomain = org.getName();
            if (requestedTenantDomain  != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                if (APIConstants.WSO2_ANONYMOUS_USER.equals(this.username)) {
                    registry = getRegistryService().getGovernanceUserRegistry(this.username, id);
                } else if (this.tenantDomain != null && !this.tenantDomain.equals(requestedTenantDomain)) {
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                } else {
                    registry = this.registry;
                }
            } else {
                registry = this.registry;
            }


            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            if (apiArtifact != null) {
                String type = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE);

                if (APIConstants.API_PRODUCT.equals(type)) {
                    /*
                    APIProduct apiProduct = getApiProduct(registry, apiArtifact);
                    String productTenantDomain = MultitenantUtils.getTenantDomain(
                            RegistryPersistenceUtil.replaceEmailDomainBack(apiProduct.getId().getProviderName()));
                    if (APIConstants.API_GLOBAL_VISIBILITY.equals(apiProduct.getVisibility())) {
                        return new ApiTypeWrapper(apiProduct);
                    }

                    if (this.tenantDomain == null || !this.tenantDomain.equals(productTenantDomain)) {
                        throw new APIManagementException(
                                "User " + username + " does not have permission to view API Product : " + apiProduct
                                        .getId().getName());
                    }

                    return new ApiTypeWrapper(apiProduct);
                    */
                    
                    //TODO previously there was a seperate method to get products. validate whether we could use api one instead
                    API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                    String apiTenantDomain = MultitenantUtils.getTenantDomain(
                            RegistryPersistenceUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                    if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) {
                        return APIMapper.INSTANCE.toDevPortalApi(api);
                    }

                    if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
                        throw new APIPersistenceException(
                                "User " + username + " does not have permission to view API : " + api.getId()
                                        .getApiName());
                    }
                    return APIMapper.INSTANCE.toDevPortalApi(api);
                } else {
                    API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                    String apiTenantDomain = MultitenantUtils.getTenantDomain(
                            RegistryPersistenceUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                    if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) {
                        //return new ApiTypeWrapper(api);
                        return APIMapper.INSTANCE.toDevPortalApi(api);
                    }

                    if (this.tenantDomain == null || !this.tenantDomain.equals(apiTenantDomain)) {
                        throw new APIPersistenceException(
                                "User " + username + " does not have permission to view API : " + api.getId()
                                        .getApiName());
                    }

                    return APIMapper.INSTANCE.toDevPortalApi(api);
                }
            } else {
                return null;
            }
        } catch (RegistryException | UserStoreException | APIManagementException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override
    public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {

        boolean transactionCommitted = false;
        try {
            registry.beginTransaction();
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when deleting API " + apiId;
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            APIIdentifier identifier = new APIIdentifier(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                    apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));

            //Delete the dependencies associated  with the api artifact
            GovernanceArtifact[] dependenciesArray = apiArtifact.getDependencies();

            if (dependenciesArray.length > 0) {
                for (GovernanceArtifact artifact : dependenciesArray) {
                    registry.delete(artifact.getPath());
                }
            }
            
            artifactManager.removeGenericArtifact(apiArtifact);
            
            String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                    identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
            Resource apiResource = registry.get(path);
            String artifactId = apiResource.getUUID();
            artifactManager.removeGenericArtifact(artifactId);

            String thumbPath = RegistryPersistenceUtil.getIconPath(identifier);
            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }

            String wsdlArchivePath = RegistryPersistenceUtil.getWsdlArchivePath(identifier);
            if (registry.resourceExists(wsdlArchivePath)) {
                registry.delete(wsdlArchivePath);
            }

            /*Remove API Definition Resource - swagger*/
            String apiDefinitionFilePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getApiName() + '-' + identifier.getVersion() + '-' + identifier.getProviderName();
            if (registry.resourceExists(apiDefinitionFilePath)) {
                registry.delete(apiDefinitionFilePath);
            }

            /*remove empty directories*/
            String apiCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getApiName();
            if (registry.resourceExists(apiCollectionPath)) {
                Resource apiCollection = registry.get(apiCollectionPath);
                CollectionImpl collection = (CollectionImpl) apiCollection;
                //if there is no other versions of apis delete the directory of the api
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more versions of the API found, removing API collection from registry");
                    }
                    registry.delete(apiCollectionPath);
                }
            }

            String apiProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    identifier.getProviderName();

            if (registry.resourceExists(apiProviderPath)) {
                Resource providerCollection = registry.get(apiProviderPath);
                CollectionImpl collection = (CollectionImpl) providerCollection;
                //if there is no api for given provider delete the provider directory
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more APIs from the provider " + identifier.getProviderName() + " found. " +
                                "Removing provider collection from registry");
                    }
                    registry.delete(apiProviderPath);
                }
            }
            registry.commitTransaction();
            transactionCommitted  = true;
        } catch (RegistryException e) {
            throw new APIPersistenceException("Failed to remove the API : " + apiId, e);
        } finally {
            try {
                if (!transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                throw new APIPersistenceException("Error occurred while rolling back the transaction. ", ex);
            }
        }
    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start, int offset,
            UserContext ctx) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();
        boolean isTenantMode = (requestedTenantDomain != null);
        boolean isTenantFlowStarted = false;
        PublisherAPISearchResult result = null;
        try {

            if (isTenantMode && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                requestedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain == null)
                    || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {// Tenant store anonymous
                                                                                            // mode
                tenantIDLocal = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.loadTenantRegistry(tenantIDLocal);
                userRegistry = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                if (!requestedTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    RegistryPersistenceUtil.loadTenantConfigBlockingMode(requestedTenantDomain);
                }
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (searchQuery != null && searchQuery.contains(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else if (searchQuery != null && searchQuery.contains(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else if (searchQuery != null && searchQuery.contains(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else {
                result = searchPaginatedPublisherAPIs(userRegistry, tenantIDLocal, searchQuery, start, offset);
            }
        } catch (UserStoreException | RegistryException | APIManagementException e) {
            throw new APIPersistenceException("Error while searching APIs " , e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIs(Registry userRegistry, int tenantIDLocal, String searchQuery,
            int start, int offset) throws APIManagementException {
        int totalLength = 0;
        boolean isMore = false;
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        try {

            final int maxPaginationLimit = getMaxPaginationLimit();

            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(searchQuery, registry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, registry,
                            APIConstants.API_RXT_MEDIA_TYPE, true);
                    if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                        isFound = false;
                    }
                } else {
                    isFound = false;
                }
            }

            if (!isFound) {
                return searchResults;
            }

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }
            List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();
            int tempLength = 0;
            for (GovernanceArtifact artifact : governanceArtifacts) {

                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                apiInfo.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                apiInfo.setId(artifact.getId());
                apiInfo.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                apiInfo.setStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                publisherAPIInfoList.add(apiInfo);

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }

            searchResults.setPublisherAPIInfoList(publisherAPIInfoList);
            searchResults.setReturnedAPIsCount(publisherAPIInfoList.size());
            searchResults.setTotalAPIsCount(totalLength);
        } catch (RegistryException e) {
            String msg = "Failed to search APIs with type";
            throw new APIManagementException(msg, e);
        } finally {
            PaginationContext.destroy();
        }

        return searchResults;
    }

    @Override
    public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start, int offset,
            UserContext ctx) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();
        boolean isTenantMode = (requestedTenantDomain != null);
        boolean isTenantFlowStarted = false;
        DevPortalAPISearchResult result = null;
        try {

            if (isTenantMode && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            } else {
                requestedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            }

            Registry userRegistry;
            int tenantIDLocal = 0;
            String userNameLocal = this.username;
            if ((isTenantMode && this.tenantDomain == null)
                    || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {// Tenant store anonymous
                                                                                            // mode
                tenantIDLocal = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.loadTenantRegistry(tenantIDLocal);
                userRegistry = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantIDLocal);
                userNameLocal = CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                if (!requestedTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    RegistryPersistenceUtil.loadTenantConfigBlockingMode(requestedTenantDomain);
                }
            } else {
                userRegistry = this.registry;
                tenantIDLocal = tenantId;
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (searchQuery != null && searchQuery.contains(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else if (searchQuery != null && searchQuery.contains(APIConstants.SUBCONTEXT_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else if (searchQuery != null && searchQuery.contains(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
                // TODO
            } else {
                result = searchPaginatedDevPortalAPIs(userRegistry, tenantIDLocal, searchQuery, start, offset);
            }
        } catch (UserStoreException | RegistryException | APIManagementException e) {
            throw new APIPersistenceException("Error while searching APIs " , e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    private DevPortalAPISearchResult searchPaginatedDevPortalAPIs(Registry userRegistry, int tenantIDLocal,
            String searchQuery, int start, int offset) throws APIManagementException {
        int totalLength = 0;
        boolean isMore = false;
        DevPortalAPISearchResult searchResults = new DevPortalAPISearchResult();
        try {

            final int maxPaginationLimit = getMaxPaginationLimit();

            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(searchQuery, userRegistry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, registry,
                            APIConstants.API_RXT_MEDIA_TYPE, true);
                    if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                        isFound = false;
                    }
                } else {
                    isFound = false;
                }
            }

            if (!isFound) {
                return searchResults;
            }

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                isMore = true;  // More APIs exist, cannot determine total API count without incurring perf hit
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }
            List<DevPortalAPIInfo> devPortalAPIInfoList = new ArrayList<DevPortalAPIInfo>();
            int tempLength = 0;
            for (GovernanceArtifact artifact : governanceArtifacts) {

                DevPortalAPIInfo apiInfo = new DevPortalAPIInfo();
                //devPortalAPIInfoList apiInfo = new devPortalAPIInfoList();
                apiInfo.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                apiInfo.setId(artifact.getId());
                apiInfo.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                apiInfo.setStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                apiInfo.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                devPortalAPIInfoList.add(apiInfo);

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }

            searchResults.setDevPortalAPIInfoList(devPortalAPIInfoList);
            searchResults.setReturnedAPIsCount(devPortalAPIInfoList.size());
            searchResults.setTotalAPIsCount(totalLength);
        } catch (RegistryException e) {
            String msg = "Failed to search APIs with type";
            throw new APIManagementException(msg, e);
        } finally {
            PaginationContext.destroy();
        }

        return searchResults;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status) throws APIPersistenceException {
        GenericArtifactManager artifactManager = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            //PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(this.username);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(org.getName(), true);
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(APIConstants.API_KEY, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
                GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
                String action = LCManagerFactory.getInstance().getLCManager()
                        .getTransitionAction(apiArtifact.getLifecycleState().toUpperCase(), status.toUpperCase());
                apiArtifact.invokeAction(action, APIConstants.API_LIFE_CYCLE);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + APIConstants.API_KEY +
                        ". Tenant id set in registry : " + ((UserRegistry) registry).getTenantId() +
                        ", Tenant domain set in PrivilegedCarbonContext: " +
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }

        } catch (GovernanceException e) {
            throw new APIPersistenceException("Error while changing the lifecycle. ", e);
        } catch (RegistryException e) {
            throw new APIPersistenceException("Error while accessing the registry. ", e);
        } catch (PersistenceException e) {
            throw new APIPersistenceException("Error while accessing the lifecycle. ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        
    }

    @Override
    public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile)
            throws WSDLPersistenceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveOASDefinition(Organization org, String apiId, String apiDefinition) throws OASPersistenceException {

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when deleting API " + apiId;
                log.error(errorMessage);
                throw new OASPersistenceException(errorMessage);
            }

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);

            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            String visibleRoles = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
            String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
            String resourcePath = RegistryPersistenceUtil.getOpenAPIDefinitionFilePath(apiName, apiVersion,
                    apiProviderName);
            resourcePath = resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinition);
            resource.setMediaType("application/json");
            registry.put(resourcePath, resource);

            String[] visibleRolesArr = null;
            if (visibleRoles != null) {
                visibleRolesArr = visibleRoles.split(",");
            }

            // Need to set anonymous if the visibility is public
            RegistryPersistenceUtil.clearResourcePermissions(resourcePath,
                    new APIIdentifier(apiProviderName, apiName, apiVersion), ((UserRegistry) registry).getTenantId());
            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, visibleRolesArr, resourcePath);

        } catch (RegistryException | APIPersistenceException| APIManagementException e) {
            throw new OASPersistenceException("Error while adding OSA Definition for " + apiId, e);
        } 
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        String apiTenantDomain = org.getName();
        String definition = null;
        boolean tenantFlowStarted = false;
        try {
            Registry registryType;
            //Tenant store anonymous mode if current tenant and the required tenant is not matching
            if (this.tenantDomain == null || isTenantDomainNotMatching(apiTenantDomain)) {
                if (apiTenantDomain != null) {
                    RegistryPersistenceUtil.startTenantFlow(apiTenantDomain);
                    tenantFlowStarted = true;
                }
                int tenantId = getTenantManager().getTenantId(
                        apiTenantDomain);
                registryType = getRegistryService().getGovernanceUserRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            Identifier id = null;
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            if (apiArtifact != null) {

                String type = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE);
                if ("APIProduct".equals(type)) {
                    id = new APIProductIdentifier(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                            apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                            apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                } else {
                    id = new APIIdentifier(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                            apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                            apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                }

            }
            definition = RegistryPersistenceUtil.getAPIDefinition(id, registryType);
            if (log.isDebugEnabled()) {
                log.debug("Definition for " + apiId + " : " +  definition);
            }
        } catch (UserStoreException | RegistryException | APIManagementException | APIPersistenceException e) {
            String msg = "Failed to get swagger documentation of API : " + apiId;
            throw new OASPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return definition;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }
    @Override
    public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition)
            throws GraphQLPersistenceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Documentation addDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        try {
            String tenantDomain = org.getName();
            Registry registry = this.registry; // for future impl
            
            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            
            GenericArtifactManager docArtifactManager = new GenericArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            GenericArtifact docArtifact = docArtifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            docArtifactManager.addGenericArtifact(RegistryPersistanceDocUtil.createDocArtifactContent(docArtifact,
                    apiName, apiVersion, apiProviderName, documentation));           
            
            String apiPath = RegistryPersistenceUtil.getAPIPath(apiName, apiVersion, apiProviderName);
            String docVisibility = documentation.getVisibility().name();
            String[] authorizedRoles = RegistryPersistenceUtil.getAuthorizedRoles(apiPath, tenantDomain);
            String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
            if (docVisibility != null) {
                if (APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_SHARED_VISIBILITY;
                } else if (APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_OWNER_VISIBILITY;
                }
            }
            RegistryPersistenceUtil.setResourcePermissions(apiProviderName,visibility, authorizedRoles, docArtifact
                    .getPath(), registry);
            String docFilePath = docArtifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if (docFilePath != null && !"".equals(docFilePath)) {
                // The docFilePatch comes as
                // /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                // We need to remove the /t/tenanatdoman/registry/resource/_system/governance section to set
                // permissions.
                int startIndex = docFilePath.indexOf(APIConstants.GOVERNANCE) + (APIConstants.GOVERNANCE).length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, authorizedRoles, filePath,
                        registry);
            }
            documentation.setId(docArtifact.getId());
            return documentation;
        } catch (RegistryException | APIManagementException | UserStoreException | APIPersistenceException e) {
            throw new DocumentationPersistenceException("Failed to add documentation", e);
        } 
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        try {
            String tenantDomain = org.getName();
            Registry registry = this.registry; // for future impl
            
            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            GenericArtifactManager artifactManager = RegistryPersistanceDocUtil.getDocumentArtifactManager(registry);
            GenericArtifact artifact = artifactManager.getGenericArtifact(documentation.getId());
            String docVisibility = documentation.getVisibility().name();
            String[] authorizedRoles = new String[0];
            String visibleRolesList = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
            if (visibleRolesList != null) {
                authorizedRoles = visibleRolesList.split(",");
            }
            String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
            if (docVisibility != null) {
                if (APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_SHARED_VISIBILITY;
                } else if (APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                    authorizedRoles = null;
                    visibility = APIConstants.DOC_OWNER_VISIBILITY;
                }
            }

            GenericArtifact updateApiArtifact = RegistryPersistanceDocUtil.createDocArtifactContent(artifact,
                    apiProviderName, apiName, apiVersion, documentation);
            artifactManager.updateGenericArtifact(updateApiArtifact);
            RegistryPersistenceUtil.clearResourcePermissions(updateApiArtifact.getPath(),
                    new APIIdentifier(apiProviderName, apiName, apiVersion), ((UserRegistry) registry).getTenantId());

            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, authorizedRoles,
                    artifact.getPath(), registry);

            String docFilePath = artifact.getAttribute(APIConstants.DOC_FILE_PATH);
            if (docFilePath != null && !"".equals(docFilePath)) {
                // The docFilePatch comes as
                // /t/tenanatdoman/registry/resource/_system/governance/apimgt/applicationdata..
                // We need to remove the
                // /t/tenanatdoman/registry/resource/_system/governance section
                // to set permissions.
                int startIndex = docFilePath.indexOf(APIConstants.GOVERNANCE) + (APIConstants.GOVERNANCE).length();
                String filePath = docFilePath.substring(startIndex, docFilePath.length());
                RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, authorizedRoles, filePath,
                        registry);
            }
            return documentation;
        } catch (RegistryException | APIManagementException | APIPersistenceException e) {
            throw new DocumentationPersistenceException("Failed to update documentation", e);
        }
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        Documentation documentation = null;
        try {
            Registry registryType;
            String requestedTenantDomain = org.getName();
            boolean isTenantMode = (requestedTenantDomain != null);
            // Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null)
                    || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = RegistryPersistanceDocUtil
                    .getDocumentArtifactManager(registryType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            
            if (artifact == null) {
                return documentation;
            }
            if (null != artifact) {
                documentation = RegistryPersistanceDocUtil.getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException | UserStoreException e) {
            String msg = "Failed to get documentation details";
            throw new DocumentationPersistenceException(msg, e);
        }
        return documentation;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        DocumentContent documentContent = null;
        try {
            Registry registryType;
            String requestedTenantDomain = org.getName();
            boolean isTenantMode = (requestedTenantDomain != null);
            // Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null)
                    || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            GenericArtifactManager artifactManager = RegistryPersistanceDocUtil
                    .getDocumentArtifactManager(registryType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            
            if (artifact == null) {
                return null;
            }
            if (artifact != null) {
                Documentation documentation = RegistryPersistanceDocUtil.getDocumentation(artifact);
                if (documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    String resource = documentation.getFilePath();
                    String[] resourceSplitPath =
                            resource.split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
                    if (resourceSplitPath.length == 2) {
                        resource = resourceSplitPath[1];
                    } else {
                        throw new DocumentationPersistenceException("Invalid resource Path " + resource);
                    }
                    if (registryType.resourceExists(resource)) {
                        documentContent = new DocumentContent();
                        Resource apiDocResource = registryType.get(resource);
                        String[] content = apiDocResource.getPath().split("/");
                        String name = content[content.length - 1];

                        documentContent.setSourceType(ContentSourceType.FILE);
                        ResourceFile resourceFile = new ResourceFile(
                                apiDocResource.getContentStream(), apiDocResource.getMediaType());
                        resourceFile.setName(name);
                        documentContent.setResourceFile(resourceFile);
                    }

                } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)
                        || documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                    
                    String contentPath = artifact.getPath()
                            .replace(RegistryConstants.PATH_SEPARATOR + documentation.getName(), "")
                            + RegistryConstants.PATH_SEPARATOR + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                            + RegistryConstants.PATH_SEPARATOR + documentation.getName();
                    if (registryType.resourceExists(contentPath)) {
                        documentContent = new DocumentContent();
                        Resource docContent = registryType.get(contentPath);
                        Object content = docContent.getContent();
                        if (content != null) {
                            String contentStr = new String((byte[]) docContent.getContent(), Charset.defaultCharset());
                            documentContent.setTextContent(contentStr);
                            documentContent
                                    .setSourceType(ContentSourceType.valueOf(documentation.getSourceType().toString()));
                        }
                    }
                } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.URL)) {
                    documentContent = new DocumentContent();
                    String sourceUrl = documentation.getSourceUrl();
                    documentContent.setTextContent(sourceUrl);
                    documentContent
                            .setSourceType(ContentSourceType.valueOf(documentation.getSourceType().toString()));
                }
            }
        } catch (RegistryException | UserStoreException e) {
            String msg = "Failed to get documentation details";
            throw new DocumentationPersistenceException(msg, e);
        } 
        return documentContent;
    }
    
    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId,
            DocumentContent content) throws DocumentationPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            boolean isTenantMode = (tenantDomain != null);
            if (isTenantMode && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            Registry registry = this.registry; // for future impl

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            
            GenericArtifactManager docArtifactManager = RegistryPersistanceDocUtil
                    .getDocumentArtifactManager(registry);
            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docId);
            Documentation doc = RegistryPersistanceDocUtil.getDocumentation(docArtifact);

            if (DocumentContent.ContentSourceType.FILE.equals(content.getSourceType())) {
                ResourceFile resource = content.getResourceFile();
                String filePath = RegistryPersistanceDocUtil.getDocumentFilePath(apiProviderName, apiName, apiVersion,
                        resource.getName());
                String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
                String visibleRolesList = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                RegistryPersistenceUtil.setResourcePermissions(RegistryPersistenceUtil.replaceEmailDomain(apiProviderName), visibility, visibleRoles,
                        filePath, registry);
                //documentation.setFilePath(addResourceFile(apiId, filePath, icon));
                String savedFilePath = addResourceFile(filePath, resource, registry, tenantDomain);
                //doc.setFilePath(savedFilePath);
                docArtifact.setAttribute(APIConstants.DOC_FILE_PATH, savedFilePath);
                docArtifactManager.updateGenericArtifact(docArtifact);
                RegistryPersistenceUtil.setFilePermission(filePath);
            } else {
                String contentPath = RegistryPersistanceDocUtil.getDocumentContentPath(apiProviderName, apiName,
                        apiVersion, doc.getName());
                Resource docContent;

                if (!registry.resourceExists(contentPath)) {
                    docContent = registry.newResource();
                } else {
                    docContent = registry.get(contentPath);
                }
                String text = content.getTextContent();
                if (!APIConstants.NO_CONTENT_UPDATE.equals(text )) {
                    docContent.setContent(text);
                }
                docContent.setMediaType(APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE);
                registry.put(contentPath, docContent);            
                
                // Set resource permission
                String apiPath = RegistryPersistenceUtil.getAPIPath(apiName, apiVersion, apiProviderName);
                String docVisibility = doc.getVisibility().name();
                String[] authorizedRoles = RegistryPersistenceUtil.getAuthorizedRoles(apiPath, tenantDomain);
                String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
                if (docVisibility != null) {
                    if (APIConstants.DOC_SHARED_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                        authorizedRoles = null;
                        visibility = APIConstants.DOC_SHARED_VISIBILITY;
                    } else if (APIConstants.DOC_OWNER_VISIBILITY.equalsIgnoreCase(docVisibility)) {
                        authorizedRoles = null;
                        visibility = APIConstants.DOC_OWNER_VISIBILITY;
                    }
                }
                RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, authorizedRoles,
                        contentPath, registry);
            } 
        } catch (APIPersistenceException | RegistryException | APIManagementException | PersistenceException
                | UserStoreException e) {
            throw new DocumentationPersistenceException("Error while adding document content", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }

    @Override
    public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset,
            String searchQuery, UserContext ctx) throws DocumentationPersistenceException {

        DocumentSearchResult result = null;
        Registry registryType;
        String requestedTenantDomain = org.getName();
        boolean isTenantMode = (requestedTenantDomain != null);
        boolean isTenantFlowStarted = false;
        try {
            if (isTenantMode && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(requestedTenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            } else {
                requestedTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(requestedTenantDomain, true);
            }

            // Tenant store anonymous mode if current tenant and the required tenant is not matching
            if ((isTenantMode && this.tenantDomain == null)
                    || (isTenantMode && isTenantDomainNotMatching(requestedTenantDomain))) {
                int tenantId = getTenantManager().getTenantId(requestedTenantDomain);
                registryType = getRegistryService()
                        .getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registryType,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String apiOrAPIProductDocPath = RegistryPersistanceDocUtil.getDocumentPath(apiProviderName, apiName,
                    apiVersion);
            String pathToContent = apiOrAPIProductDocPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR;
            String pathToDocFile = apiOrAPIProductDocPath + APIConstants.DOCUMENT_FILE_DIR;

            if (registryType.resourceExists(apiOrAPIProductDocPath)) {
                List<Documentation> documentationList = new ArrayList<Documentation>();
                Resource resource = registryType.get(apiOrAPIProductDocPath);
                if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                    String[] docsPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();
                    for (String docPath : docsPaths) {
                        if (!(docPath.equalsIgnoreCase(pathToContent) || docPath.equalsIgnoreCase(pathToDocFile))) {
                            Resource docResource = registryType.get(docPath);
                            GenericArtifactManager artifactManager = RegistryPersistanceDocUtil
                                    .getDocumentArtifactManager(registryType);
                            GenericArtifact docArtifact = artifactManager.getGenericArtifact(docResource.getUUID());
                            Documentation doc = RegistryPersistanceDocUtil.getDocumentation(docArtifact);
                            if (searchQuery != null) {
                                if (searchQuery.toLowerCase().startsWith("name:")) {
                                    String requestedDocName = searchQuery.split(":")[1];
                                    if (doc.getName().equalsIgnoreCase(requestedDocName)) {
                                        documentationList.add(doc);
                                    }
                                } else {
                                    log.warn("Document search not implemented for the query " + searchQuery);
                                }
                            } else {
                                documentationList.add(doc);
                            }
                            
                        }
                    }
                }
                result = new DocumentSearchResult();
                result.setDocumentationList(documentationList);
            }
        } catch (RegistryException | UserStoreException | APIPersistenceException e) {
            String msg = "Failed to get documentations for api/product " + apiId;
            throw new DocumentationPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    @Override
    public void deleteDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        try {
            GenericArtifactManager artifactManager = RegistryPersistanceDocUtil.getDocumentArtifactManager(registry);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when removing documentation of " + apiId
                        + " Document ID " + docId;
                log.error(errorMessage);
                throw new DocumentationPersistenceException(errorMessage);
            }
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);
            String docPath = artifact.getPath();
            if (docPath != null) {
                if (registry.resourceExists(docPath)) {
                    registry.delete(docPath);
                }
            }

        } catch (RegistryException e) {
            throw new DocumentationPersistenceException("Failed to delete documentation", e);
        }
    }

    @Override
    public Mediation addMediationPolicy(Organization org, String apiId, Mediation mediation)
            throws MediationPolicyPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mediation updateMediationPolicy(Organization org, String apiId, Mediation mediation)
            throws MediationPolicyPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId)
            throws MediationPolicyPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MediationInfo> getAllMediationPolicies(Organization org, String apiId)
            throws MediationPolicyPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteMediationPolicy(Organization org, String apiId, String mediationPolicyId)
            throws MediationPolicyPersistenceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile)
            throws ThumbnailPersistenceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus  Current status of the API
     * @throws APIManagementException on error
     */
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
    
    protected int getMaxPaginationLimit() {
        // TODO fix this
        /*
        String paginationLimit = getAPIManagerConfiguration()
                .getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);
        // If the Config exists use it to set the pagination limit
        final int maxPaginationLimit;
        if (paginationLimit != null) {
            // The additional 1 added to the maxPaginationLimit is to help us determine if more
            // APIs may exist so that we know that we are unable to determine the actual total
            // API count. We will subtract this 1 later on so that it does not interfere with
            // the logic of the rest of the application
            int pagination = Integer.parseInt(paginationLimit);
            // Because the store jaggery pagination logic is 10 results per a page we need to set pagination
            // limit to at least 11 or the pagination done at this level will conflict with the store pagination
            // leading to some of the APIs not being displayed
            if (pagination < 11) {
                pagination = 11;
                log.warn("Value of '" + APIConstants.API_STORE_APIS_PER_PAGE + "' is too low, defaulting to 11");
            }
            maxPaginationLimit = start + pagination + 1;
        }
        // Else if the config is not specified we go with default functionality and load all
        else {
            maxPaginationLimit = Integer.MAX_VALUE;
        }*/

        return Integer.MAX_VALUE;
    }

    protected String addResourceFile(String resourcePath, ResourceFile resourceFile,
            Registry registry, String tenantDomain) throws PersistenceException {
        try {
            Resource thumb = registry.newResource();
            thumb.setContentStream(resourceFile.getContent());
            thumb.setMediaType(resourceFile.getContentType());
            registry.put(resourcePath, thumb);
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
                return RegistryConstants.PATH_SEPARATOR + "registry" + RegistryConstants.PATH_SEPARATOR + "resource"
                        + RegistryConstants.PATH_SEPARATOR + "_system" + RegistryConstants.PATH_SEPARATOR + "governance"
                        + resourcePath;
            } else {
                return "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource" + RegistryConstants.PATH_SEPARATOR + "_system"
                        + RegistryConstants.PATH_SEPARATOR + "governance" + resourcePath;
            }
        } catch (RegistryException e) {
            String msg = "Error while adding the resource to the registry";
            throw new PersistenceException(msg, e);
        }
    }
}
