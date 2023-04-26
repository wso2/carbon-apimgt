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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence.Direction;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent.ContentSourceType;
import org.wso2.carbon.apimgt.persistence.exceptions.*;
import org.wso2.carbon.apimgt.persistence.internal.PersistenceManagerComponent;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mapper.APIProductMapper;
import org.wso2.carbon.apimgt.persistence.utils.PublisherAPISearchResultComparator;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceDocUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistrySearchUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifactImpl;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.ResourceData;
import org.wso2.carbon.registry.common.TermData;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.service.ContentBasedSearchService;
import org.wso2.carbon.registry.indexing.service.SearchResultsBean;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;

public class RegistryPersistenceImpl implements APIPersistence {

    private static final Log log = LogFactory.getLog(RegistryPersistenceImpl.class);
    private Properties properties;

    public RegistryPersistenceImpl() {
    }

    public RegistryPersistenceImpl(Properties properties) {
        this.properties = properties;
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

    @SuppressWarnings("unchecked")
    @Override
    public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {

        API api = APIMapper.INSTANCE.toApi(publisherAPI);
        boolean transactionCommitted = false;
        boolean tenantFlowStarted = false;
        Registry registry = null;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(api.getId().getApiName()));
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API " + api.getId().getApiName();
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifact artifact = RegistryPersistenceUtil.createAPIArtifactContent(genericArtifact, api);
            artifactManager.addGenericArtifact(artifact);
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

            String apiStatus = api.getStatus();
            saveAPIStatus(registry, artifactPath, apiStatus);

            String visibleRolesList = api.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }

            String publisherAccessControlRoles = api.getAccessControlRoles();
            updateRegistryResources(registry, artifactPath, publisherAccessControlRoles, api.getAccessControl(),
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
            } else if (api.getAsyncApiDefinition() != null) {
                String resourcePath = RegistryPersistenceUtil
                        .getOpenAPIDefinitionFilePath(api.getId().getName(), api.getId().getVersion(),
                                api.getId().getProviderName());
                resourcePath = resourcePath + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;
                Resource resource;
                if (!registry.resourceExists(resourcePath)) {
                    resource = registry.newResource();
                } else {
                    resource = registry.get(resourcePath);
                }
                resource.setContent(api.getAsyncApiDefinition());
                resource.setMediaType(APIConstants.APPLICATION_JSON_MEDIA_TYPE);          //add a constant for app.json
                registry.put(resourcePath, resource);
                RegistryPersistenceUtil.clearResourcePermissions(resourcePath, api.getId(),
                        ((UserRegistry) registry).getTenantId());
                RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                        visibleRoles, resourcePath);
            }

            //Set permissions to doc path
            String docLocation = RegistryPersistenceDocUtil.getDocumentPath(api.getId().getProviderName(),
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
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
            try {
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error while rolling back the transaction for API: " + api.getId().getApiName());
            }
        }
    }

    @Override
    public String addAPIRevision(Organization org, String apiUUID, int revisionId) throws APIPersistenceException {
        String revisionUUID;
        boolean transactionCommitted = false;
        Registry registry = null;
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiUUID);
            if (apiArtifact != null) {
                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                APIIdentifier apiId = api.getId();
                String apiPath = RegistryPersistenceUtil.getAPIPath(apiId);
                int prependIndex = apiPath.lastIndexOf("/api");
                String apiSourcePath = apiPath.substring(0, prependIndex);
                String revisionTargetPath = RegistryPersistenceUtil.getRevisionPath(apiId.getUUID(), revisionId);
                if (registry.resourceExists(revisionTargetPath)) {
                    throw new APIManagementException("API revision already exists with id: " + revisionId,
                            ExceptionCodes.from(ExceptionCodes.EXISTING_API_REVISION_FOUND, String.valueOf(revisionId)));
                }
                registry.copy(apiSourcePath, revisionTargetPath);
                Resource apiRevisionArtifact = registry.get(revisionTargetPath + "api");
                registry.commitTransaction();
                transactionCommitted = true;
                if (log.isDebugEnabled()) {
                    String logMessage =
                            "Revision for API Name: " + apiId.getApiName() + ", API Version " + apiId.getVersion()
                                    + " created";
                    log.debug(logMessage);
                }
                revisionUUID = apiRevisionArtifact.getUUID();
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + apiUUID + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API Revision create for API: "
                        + apiUUID, re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while creating API Revision", e);
        } finally {
            try {
                if (tenantFlowStarted) {
                    RegistryPersistenceUtil.endTenantFlow();
                }
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error while rolling back the transaction for API Revision create for API: " + apiUUID);
            }
        }
        return revisionUUID;
    }

    @Override
    public void restoreAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId)
            throws APIPersistenceException {

        boolean transactionCommitted = false;
        Registry registry = null;
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiUUID);
            String lcState = ((GenericArtifactImpl) apiArtifact).getLcState();
            if (apiArtifact != null) {
                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                String visibleRolesList = api.getVisibleRoles();
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                String apiPath = GovernanceUtils.getArtifactPath(registry, apiUUID);
                int prependIndex = apiPath.lastIndexOf("/api");
                String apiSourcePath = apiPath.substring(0, prependIndex);
                String revisionTargetPath = RegistryPersistenceUtil.getRevisionPath(apiUUID, revisionId);
                registry.delete(apiSourcePath);
                registry.copy(revisionTargetPath, apiSourcePath);
                Resource newAPIArtifact = registry.get(apiPath);
                newAPIArtifact.setUUID(apiUUID);
                newAPIArtifact.setProperty("registry.lifecycle.APILifeCycle.state", java.util.Arrays.asList((lcState)));
                registry.put(apiPath, newAPIArtifact);
                RegistryPersistenceUtil.clearResourcePermissions(apiPath, api.getId(),
                        ((UserRegistry) registry).getTenantId());
                RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(), api.getVisibility(),
                        visibleRoles, apiPath);
            }
            registry.commitTransaction();
            transactionCommitted = true;
            if (log.isDebugEnabled()) {
                String logMessage =
                        "Revision ID" + revisionId + " for API UUID: " + apiUUID + " restored";
                log.debug(logMessage);
            }
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API Revision restore for API: "
                        + apiUUID, re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while restoring revision", e);
        } finally {
            try {
                if (tenantFlowStarted) {
                    RegistryPersistenceUtil.endTenantFlow();
                }
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error while rolling back the transaction for API Revision restore for API: " + apiUUID);
            }
        }
    }

    @Override
    public void deleteAPIRevision(Organization org, String apiUUID, String revisionUUID, int revisionId)
            throws APIPersistenceException {
        String revisionTargetPath = APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiUUID +
                RegistryConstants.PATH_SEPARATOR + revisionId;
        boolean transactionCommitted = false;
        Registry registry = null;
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            registry.delete(revisionTargetPath);
            registry.commitTransaction();
            transactionCommitted = true;
            if (log.isDebugEnabled()) {
                String logMessage =
                        "Revision ID:" + revisionId + " for API : " + apiUUID + " deleted";
                log.debug(logMessage);
            }
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API Revision delete for API: "
                        + apiUUID, re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } finally {
            try {
                if (tenantFlowStarted) {
                    RegistryPersistenceUtil.endTenantFlow();
                }
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error while rolling back the transaction for API Revision delete for API: " + apiUUID);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        API api = APIMapper.INSTANCE.toApi(publisherAPI);

        boolean transactionCommitted = false;
        boolean tenantFlowStarted = false;
        Registry registry = null;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();

            registry.beginTransaction();
            String apiArtifactId = registry.get(RegistryPersistenceUtil.getAPIPath(api.getId())).getUUID();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when updating API artifact ID " + api.getId();
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifact artifact = getAPIArtifact(apiArtifactId, registry);

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
            artifactManager.updateGenericArtifact(updateApiArtifact);

            //write API Status to a separate property. This is done to support querying APIs using custom query (SQL)
            //to gain performance
            //String apiStatus = api.getStatus().toUpperCase();
            //saveAPIStatus(artifactPath, apiStatus);
            String[] visibleRoles = new String[0];
            String publisherAccessControlRoles = api.getAccessControlRoles();

            updateRegistryResources(registry, artifactPath, publisherAccessControlRoles, api.getAccessControl(),
                    api.getAdditionalProperties());

            //propagate api status change and access control roles change to document artifact
            String newStatus = updateApiArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (!StringUtils.equals(oldStatus, newStatus) || !StringUtils.equals(oldAccessControlRoles, publisherAccessControlRoles)) {
                RegistryPersistenceUtil.notifyAPIStateChangeToAssociatedDocuments(artifact, registry);
            }

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

            // doc visibility change
            String apiOrAPIProductDocPath = RegistryPersistenceDocUtil.getDocumentPath(api.getId().getProviderName(),
                    api.getId().getApiName(), api.getId().getVersion());
            String pathToContent = apiOrAPIProductDocPath + APIConstants.INLINE_DOCUMENT_CONTENT_DIR;
            String pathToDocFile = apiOrAPIProductDocPath + APIConstants.DOCUMENT_FILE_DIR;

            if (registry.resourceExists(apiOrAPIProductDocPath)) {
                Resource resource = registry.get(apiOrAPIProductDocPath);
                if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                    String[] docsPaths = ((org.wso2.carbon.registry.core.Collection) resource).getChildren();
                    for (String docPath : docsPaths) {
                        if (!(docPath.equalsIgnoreCase(pathToContent) || docPath.equalsIgnoreCase(pathToDocFile))) {
                            Resource docResource = registry.get(docPath);
                            GenericArtifactManager docArtifactManager = RegistryPersistenceDocUtil
                                    .getDocumentArtifactManager(registry);
                            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docResource.getUUID());
                            Documentation doc = RegistryPersistenceDocUtil.getDocumentation(docArtifact);

                            if ((APIConstants.DOC_API_BASED_VISIBILITY).equalsIgnoreCase(doc.getVisibility().name())) {
                                String documentationPath = RegistryPersistenceDocUtil.getAPIDocPath(api.getId())
                                        + doc.getName();
                                RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(),
                                        api.getVisibility(), visibleRoles, documentationPath, registry);
                                if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())
                                        || Documentation.DocumentSourceType.MARKDOWN.equals(doc.getSourceType())) {

                                    String contentPath = RegistryPersistenceDocUtil.getAPIDocPath(api.getId())
                                            + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                                            + RegistryConstants.PATH_SEPARATOR + doc.getName();
                                    RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(),
                                            api.getVisibility(), visibleRoles, contentPath, registry);
                                } else if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType())
                                        && doc.getFilePath() != null) {
                                    String filePath = RegistryPersistenceDocUtil.getDocumentationFilePath(api.getId(),
                                            doc.getFilePath().split("files" + RegistryConstants.PATH_SEPARATOR)[1]);
                                    RegistryPersistenceUtil.setResourcePermissions(api.getId().getProviderName(),
                                            api.getVisibility(), visibleRoles, filePath, registry);
                                }
                            }
                        }
                    }
                }
            }

            setSoapToRestSequences(publisherAPI, registry);
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
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
            try {
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error occurred while rolling back the transaction.", ex);
            }
        }
    }

    @Override
    public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {

        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            tenantFlowStarted = holder.isTenantFlowStarted();
            Registry registry = holder.getRegistry();

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registry);
            if (apiArtifact != null) {

                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
                int prependIndex = apiPath.lastIndexOf("/api");
                String apiSourcePath = apiPath.substring(0, prependIndex);
                String definitionPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
                String asyncApiDefinitionPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

                if (registry.resourceExists(definitionPath)) {
                    Resource apiDocResource = registry.get(definitionPath);
                    String apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                    api.setSwaggerDefinition(apiDocContent);
                }

                if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                    List<SOAPToRestSequence> list = getSoapToRestSequences(registry, api, Direction.IN);
                    list.addAll(getSoapToRestSequences(registry, api, Direction.OUT));
                    api.setSoapToRestSequences(list);
                } else if (APIConstants.API_TYPE_WEBSUB.equals(api.getType()) ||
                        APIConstants.API_TYPE_WS.equals(api.getType()) ||
                        APIConstants.API_TYPE_SSE.equals(api.getType()) ||
                        APIConstants.API_TYPE_WEBHOOK.equals(api.getType())) {
                    if (asyncApiDefinitionPath != null) {
                        if (registry.resourceExists(asyncApiDefinitionPath)) {
                            Resource apiDocResource = registry.get(asyncApiDefinitionPath);
                            String apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                            api.setAsyncApiDefinition(apiDocContent);
                        }
                    }
                }

                PublisherAPI pubApi = APIMapper.INSTANCE.toPublisherApi(api);
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
        } catch (APIManagementException e) {
            String msg = "Failed to get API";
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
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registry);
            if (apiArtifact != null) {

                API api = RegistryPersistenceUtil.getApiForPublishing(registry, apiArtifact);
                String definitionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                        + RegistryPersistenceUtil.replaceEmailDomain(api.getId().getProviderName())
                        + RegistryConstants.PATH_SEPARATOR + api.getId().getName() + RegistryConstants.PATH_SEPARATOR
                        + api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

                if (registry.resourceExists(definitionPath)) {
                    Resource apiDocResource = registry.get(definitionPath);
                    String apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                    api.setSwaggerDefinition(apiDocContent);
                }
                String apiTenantDomain = MultitenantUtils
                        .getTenantDomain(RegistryPersistenceUtil.replaceEmailDomainBack(api.getId().getProviderName()));
                if (APIConstants.API_GLOBAL_VISIBILITY.equals(api.getVisibility())) {
                    // return new ApiTypeWrapper(api);
                    return APIMapper.INSTANCE.toDevPortalApi(api);
                }

                if (tenantDomain == null || !tenantDomain.equals(apiTenantDomain)) {
                    throw new APIPersistenceException(
                            "User does not have permission to view API : " + api.getId().getApiName());
                }

                return APIMapper.INSTANCE.toDevPortalApi(api);

            } else {
                return null;
            }
        } catch (RegistryException | APIManagementException e) {
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
        boolean tenantFlowStarted = false;
        Registry registry = null;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
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

            /*remove revision directory with UUID*/
            String revisionDirectoryPath = APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    apiId;
            if (registry.resourceExists(revisionDirectoryPath)) {
                registry.delete(revisionDirectoryPath);
            }

            registry.commitTransaction();
            transactionCommitted = true;
        } catch (RegistryException e) {
            throw new APIPersistenceException("Failed to remove the API : " + apiId, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
            try {
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error occurred while rolling back the transaction.", ex);
            }
        }
    }

    @Override
    public void deleteAllAPIs(Organization org) throws APIPersistenceException {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    @Override
    public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start, int offset,
                                                           UserContext ctx, String sortBy, String sortOrder) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();

        boolean isTenantFlowStarted = false;
        PublisherAPISearchResult result = null;
        try {
            RegistryHolder holder = getRegistry(requestedTenantDomain);
            Registry sysRegistry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            int tenantIDLocal = holder.getTenantId();
            log.debug("Requested query for publisher search: " + searchQuery);

            String modifiedQuery = RegistrySearchUtil.getPublisherSearchQuery(searchQuery, ctx);

            log.debug("Modified query for publisher search: " + modifiedQuery);

            String tenantAdminUsername = getTenantAwareUsername(
                    RegistryPersistenceUtil.getTenantAdminUserName(requestedTenantDomain));
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAdminUsername);

            if (searchQuery != null && searchQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)
                    && searchQuery.split(":").length > 1) {
                    result = searchPaginatedPublisherAPIsByDoc(sysRegistry, tenantIDLocal, searchQuery.split(":")[1],
                        tenantAdminUsername, start, offset);
            } else {
                result = searchPaginatedPublisherAPIs(sysRegistry, tenantIDLocal, modifiedQuery, start, offset);
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while searching APIs ", e);
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
        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
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
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, userRegistry,
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
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }
            List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();
            int tempLength = 0;
            for (GovernanceArtifact artifact : governanceArtifacts) {

                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                String artifactPath = GovernanceUtils.getArtifactPath(userRegistry, artifact.getId());
                Resource apiResource = userRegistry.get(artifactPath);
                apiInfo.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                apiInfo.setId(artifact.getId());
                apiInfo.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                apiInfo.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                apiInfo.setStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                apiInfo.setAudience(artifact.getAttribute(APIConstants.API_OVERVIEW_AUDIENCE));
                apiInfo.setCreatedTime(String.valueOf(apiResource.getCreatedTime().getTime()));
                apiInfo.setUpdatedTime(apiResource.getLastModified());
                apiInfo.setUpdatedBy(apiResource.getLastUpdaterUserName());
                apiInfo.setGatewayVendor(String.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_GATEWAY_VENDOR)));
                apiInfo.setAdvertiseOnly(Boolean.parseBoolean(artifact
                        .getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
                publisherAPIInfoList.add(apiInfo);

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }
            // Sort the publisherAPIInfoList according to the API name.
            Collections.sort(publisherAPIInfoList, new PublisherAPISearchResultComparator());
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
        boolean isTenantFlowStarted = false;
        DevPortalAPISearchResult result = null;
        try {
            RegistryHolder holder = getRegistry(ctx.getUserame(), requestedTenantDomain);
            Registry userRegistry = holder.getRegistry();
            int tenantIDLocal = holder.getTenantId();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            log.debug("Requested query for devportal search: " + searchQuery);
            String modifiedQuery = RegistrySearchUtil.getDevPortalSearchQuery(searchQuery, ctx,
                    isAllowDisplayAPIsWithMultipleStatus(), isAllowDisplayAPIsWithMultipleVersions());
            log.debug("Modified query for devportal search: " + modifiedQuery);

            String userNameLocal;
            if (holder.isAnonymousMode()) {
                userNameLocal = APIConstants.WSO2_ANONYMOUS_USER;
            } else {
                userNameLocal = getTenantAwareUsername(ctx.getUserame());
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            if (searchQuery != null && searchQuery.startsWith(APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX)) {
                result = searchPaginatedDevPortalAPIsByDoc(userRegistry, tenantIDLocal, searchQuery.split(":")[1],
                        userNameLocal, start, offset);
            } else {
                result = searchPaginatedDevPortalAPIs(userRegistry, tenantIDLocal, modifiedQuery, start, offset);
            }
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while searching APIs ", e);
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
        DevPortalAPISearchResult searchResults = new DevPortalAPISearchResult();
        try {

            final int maxPaginationLimit = getMaxPaginationLimit();

            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);
            log.debug("Dev portal list apis query " + searchQuery);
            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(searchQuery, userRegistry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            totalLength = PaginationContext.getInstance().getLength();
            boolean isFound = true;
            if (governanceArtifacts == null || governanceArtifacts.size() == 0) {
                if (searchQuery.contains(APIConstants.API_OVERVIEW_PROVIDER)) {
                    searchQuery = searchQuery.replaceAll(APIConstants.API_OVERVIEW_PROVIDER, APIConstants.API_OVERVIEW_OWNER);
                    governanceArtifacts = GovernanceUtils.findGovernanceArtifacts(searchQuery, userRegistry,
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
                apiInfo.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                apiInfo.setStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                apiInfo.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
                Set<String> availableTiers = new HashSet<String>();
                if (tiers != null) {
                    String[] tiersArray = tiers.split("\\|\\|");
                    for (String tierName : tiersArray) {
                        availableTiers.add(tierName);
                    }
                }
                apiInfo.setAvailableTierNames(availableTiers);
                apiInfo.setSubscriptionAvailability(
                        artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
                apiInfo.setSubscriptionAvailableOrgs(
                        artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));
                apiInfo.setGatewayVendor(artifact.getAttribute(APIConstants.API_OVERVIEW_GATEWAY_VENDOR));
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

    private DevPortalAPISearchResult searchPaginatedDevPortalAPIsByDoc(Registry registry, int tenantID,
                                                                       String searchQuery, String username, int start, int offset) throws APIPersistenceException {
        DevPortalAPISearchResult searchResults = new DevPortalAPISearchResult();
        try {

            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when searching APIs by docs in tenant ID " + tenantID;
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            if (docArtifactManager == null) {
                String errorMessage = "Doc artifact manager is null when searching APIs by docs in tenant ID " +
                        tenantID;
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            SolrClient client = SolrClient.getInstance();
            Map<String, String> fields = new HashMap<String, String>();
            fields.put(APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, "*" + APIConstants.API_ROOT_LOCATION + "*");
            fields.put(APIConstants.DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD, "*");

            if (tenantID == -1) {
                tenantID = MultitenantConstants.SUPER_TENANT_ID;
            }
            //PaginationContext.init(0, 10000, "ASC", APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, Integer.MAX_VALUE);
            SolrDocumentList documentList = client.query(searchQuery, tenantID, fields);

            org.wso2.carbon.user.api.AuthorizationManager manager = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantID).
                    getAuthorizationManager();

            username = MultitenantUtils.getTenantAwareUsername(username);
            List<DevPortalAPIInfo> devPortalAPIInfoList = new ArrayList<DevPortalAPIInfo>();
            for (SolrDocument document : documentList) {
                DevPortalAPIInfo apiInfo = new DevPortalAPIInfo();
                String filePath = (String) document.getFieldValue("path_s");
                String fileName = (String) document.getFieldValue("resourceName_s");
                int index = filePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                filePath = filePath.substring(index);
                boolean isAuthorized;
                int indexOfContents = filePath.indexOf(APIConstants.INLINE_DOCUMENT_CONTENT_DIR);
                String documentationPath = filePath.substring(0, indexOfContents) + fileName;
                String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                        RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + documentationPath);
                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                    isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                } else {
                    isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                }
                if (isAuthorized) {
                    int indexOfDocumentation = filePath.indexOf(APIConstants.DOCUMENTATION_KEY);
                    String apiPath = documentationPath.substring(0, indexOfDocumentation) + APIConstants.API_KEY;
                    path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + apiPath);
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                        isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                    } else {
                        isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                    }

                    if (isAuthorized) {
                        Resource resource = registry.get(apiPath);
                        String apiArtifactId = resource.getUUID();
                        if (apiArtifactId != null) {
                            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
                            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                            if (APIConstants.PUBLISHED.equals(status) ||
                                    APIConstants.PROTOTYPED.equals(status)) {

                                apiInfo.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                                apiInfo.setId(artifact.getId());
                                apiInfo.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                                apiInfo.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
                                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                                apiInfo.setStatus(status);
                                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                                apiInfo.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
                                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                                apiInfo.setSubscriptionAvailability(
                                        artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
                                apiInfo.setSubscriptionAvailableOrgs(artifact
                                        .getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));
                                apiInfo.setGatewayVendor(artifact.getAttribute(APIConstants.API_OVERVIEW_GATEWAY_VENDOR));
                                devPortalAPIInfoList.add(apiInfo);
                            }

                        } else {
                            throw new GovernanceException("artifact id is null of " + apiPath);
                        }
                    }
                }
            }
            searchResults.setDevPortalAPIInfoList(devPortalAPIInfoList);
            searchResults.setTotalAPIsCount(devPortalAPIInfoList.size());
            searchResults.setReturnedAPIsCount(devPortalAPIInfoList.size());
        } catch (RegistryException | UserStoreException | APIPersistenceException | IndexerException e) {
            String msg = "Failed to search APIs with type";
            throw new APIPersistenceException(msg, e);
        } finally {
            PaginationContext.destroy();
        }

        return searchResults;
    }

    private PublisherAPISearchResult searchPaginatedPublisherAPIsByDoc(Registry registry, int tenantID,
                                                                       String searchQuery, String username, int start, int offset) throws APIPersistenceException {

        PublisherAPISearchResult searchResults = new PublisherAPISearchResult();
        try {

            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when searching APIs by docs in tenant ID " + tenantID;
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            if (docArtifactManager == null) {
                String errorMessage = "Doc artifact manager is null when searching APIs by docs in tenant ID " +
                        tenantID;
                log.error(errorMessage);
                throw new APIPersistenceException(errorMessage);
            }
            SolrClient client = SolrClient.getInstance();
            Map<String, String> fields = new HashMap<String, String>();
            fields.put(APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, "*" + APIConstants.API_ROOT_LOCATION + "*");
            fields.put(APIConstants.DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD, "*");

            if (tenantID == -1) {
                tenantID = MultitenantConstants.SUPER_TENANT_ID;
            }
            //PaginationContext.init(0, 10000, "ASC", APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, Integer.MAX_VALUE);
            SolrDocumentList documentList = client.query(searchQuery, tenantID, fields);

            org.wso2.carbon.user.api.AuthorizationManager manager = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantID).
                    getAuthorizationManager();

            username = MultitenantUtils.getTenantAwareUsername(username);
            List<PublisherAPIInfo> publisherAPIInfoList = new ArrayList<PublisherAPIInfo>();
            for (SolrDocument document : documentList) {
                PublisherAPIInfo apiInfo = new PublisherAPIInfo();
                String filePath = (String) document.getFieldValue("path_s");
                String fileName = (String) document.getFieldValue("resourceName_s");
                int index = filePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                filePath = filePath.substring(index);
                boolean isAuthorized;
                int indexOfContents = filePath.indexOf(APIConstants.INLINE_DOCUMENT_CONTENT_DIR);
                String documentationPath = filePath.substring(0, indexOfContents) + fileName;
                String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                        RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + documentationPath);
                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                    isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                } else {
                    isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                }
                if (isAuthorized) {
                    int indexOfDocumentation = filePath.indexOf(APIConstants.DOCUMENTATION_KEY);
                    String apiPath = documentationPath.substring(0, indexOfDocumentation) + APIConstants.API_KEY;
                    path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            RegistryPersistenceUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + apiPath);
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
                        isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
                    } else {
                        isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
                    }

                    if (isAuthorized) {
                        Resource resource = registry.get(apiPath);
                        String apiArtifactId = resource.getUUID();
                        if (apiArtifactId != null) {
                            GenericArtifact artifact = artifactManager.getGenericArtifact(apiArtifactId);
                            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
                            if (APIConstants.PUBLISHED.equals(status) ||
                                    APIConstants.PROTOTYPED.equals(status)) {

                                apiInfo.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                                apiInfo.setId(artifact.getId());
                                apiInfo.setApiName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                                apiInfo.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
                                apiInfo.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
                                apiInfo.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                                apiInfo.setStatus(status);
                                apiInfo.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
                                apiInfo.setCreatedTime(String.valueOf(resource.getCreatedTime().getTime()));
                                apiInfo.setUpdatedTime(resource.getLastModified());
                                apiInfo.setUpdatedBy(resource.getLastUpdaterUserName());
                                apiInfo.setGatewayVendor(String.valueOf(
                                        artifact.getAttribute(APIConstants.API_OVERVIEW_GATEWAY_VENDOR)));
                                //apiInfo.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
                                apiInfo.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                                apiInfo.setAdvertiseOnly(Boolean.parseBoolean(artifact
                                        .getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
                                publisherAPIInfoList.add(apiInfo);
                            }

                        } else {
                            throw new GovernanceException("artifact id is null of " + apiPath);
                        }
                    }
                }
            }
            // Sort the publisherAPIInfoList according to the API name.
            Collections.sort(publisherAPIInfoList, new PublisherAPISearchResultComparator());
            searchResults.setPublisherAPIInfoList(publisherAPIInfoList);
            searchResults.setTotalAPIsCount(publisherAPIInfoList.size());
            searchResults.setReturnedAPIsCount(publisherAPIInfoList.size());
        } catch (RegistryException | UserStoreException | APIPersistenceException | IndexerException e) {
            String msg = "Failed to search APIs with type";
            throw new APIPersistenceException(msg, e);
        } finally {
            PaginationContext.destroy();
        }

        return searchResults;
    }

    private boolean isAllowDisplayAPIsWithMultipleStatus() {
        if (properties != null) {
            return (boolean) properties.get(APIConstants.ALLOW_MULTIPLE_STATUS);
        }
        return false;
    }

    private boolean isAllowDisplayAPIsWithMultipleVersions() {
        if (properties != null) {
            return (boolean) properties.get(APIConstants.ALLOW_MULTIPLE_VERSIONS);
        }
        return false;
    }

    @Override
    public PublisherContentSearchResult searchContentForPublisher(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        log.debug("Requested query for publisher content search: " + searchQuery);
        Map<String, String> attributes = RegistrySearchUtil.getPublisherSearchAttributes(searchQuery, ctx);
        if (log.isDebugEnabled()) {
            log.debug("Search attributes : " + attributes);
        }
        boolean isTenantFlowStarted = false;
        PublisherContentSearchResult result = null;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            String requestedTenantDomain = org.getName();
            String tenantAwareUsername = getTenantAwareUsername(RegistryPersistenceUtil.getTenantAdminUserName(requestedTenantDomain));
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAwareUsername);

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            int maxPaginationLimit = getMaxPaginationLimit();
            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            int tenantId = holder.getTenantId();
            if (tenantId == -1) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }

            UserRegistry systemUserRegistry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
            ContentBasedSearchService contentBasedSearchService = new ContentBasedSearchService();

            SearchResultsBean resultsBean = contentBasedSearchService.searchByAttribute(attributes, systemUserRegistry);
            String errorMsg = resultsBean.getErrorMessage();
            if (errorMsg != null) {
                throw new APIPersistenceException("Error while searching " + errorMsg);
            }
            ResourceData[] resourceData = resultsBean.getResourceDataList();
            int totalLength = PaginationContext.getInstance().getLength();

            if (resourceData != null) {
                result = new PublisherContentSearchResult();
                List<SearchContent> contentData = new ArrayList<SearchContent>();
                if (log.isDebugEnabled()) {
                    log.debug("Number of records Found: " + resourceData.length);
                }

                for (ResourceData data : resourceData) {

                    String resourcePath = data.getResourcePath();
                    if (resourcePath.contains(APIConstants.APIMGT_REGISTRY_LOCATION)) {
                        int index = resourcePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                        resourcePath = resourcePath.substring(index);
                        Resource resource = registry.get(resourcePath);
                        if (APIConstants.DOCUMENT_RXT_MEDIA_TYPE.equals(resource.getMediaType()) ||
                                APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE.equals(resource.getMediaType())) {
                            if (resourcePath.contains(APIConstants.INLINE_DOCUMENT_CONTENT_DIR)) {
                                int indexOfContents = resourcePath.indexOf(APIConstants.INLINE_DOCUMENT_CONTENT_DIR);
                                resourcePath = resourcePath.substring(0, indexOfContents) + data.getName();
                            }
                            DocumentSearchContent docSearch = new DocumentSearchContent();
                            Resource docResource = registry.get(resourcePath);
                            String docArtifactId = docResource.getUUID();
                            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docArtifactId);
                            Documentation doc = RegistryPersistenceDocUtil.getDocumentation(docArtifact);
                            //API associatedAPI = null;
                            //APIProduct associatedAPIProduct = null;
                            int indexOfDocumentation = resourcePath.indexOf(APIConstants.DOCUMENTATION_KEY);
                            String apiPath = resourcePath.substring(0, indexOfDocumentation) + APIConstants.API_KEY;
                            Resource apiResource = registry.get(apiPath);
                            String apiArtifactId = apiResource.getUUID();
                            PublisherAPI pubAPI;
                            if (apiArtifactId != null) {
                                GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                                String accociatedType;
                                if (apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE).
                                        equals(APIConstants.AuditLogConstants.API_PRODUCT)) {
                                    //associatedAPIProduct = APIUtil.getAPIProduct(apiArtifact, registry);
                                    accociatedType = APIConstants.API_PRODUCT;
                                } else {
                                    //associatedAPI = APIUtil.getAPI(apiArtifact, registry);
                                    accociatedType = APIConstants.API;
                                }
                                pubAPI = RegistryPersistenceUtil.getAPIForSearch(apiArtifact);
                                docSearch.setApiName(pubAPI.getApiName());
                                docSearch.setApiProvider(pubAPI.getProviderName());
                                docSearch.setApiVersion(pubAPI.getVersion());
                                docSearch.setApiUUID(pubAPI.getId());
                                docSearch.setAssociatedType(accociatedType);
                                docSearch.setDocType(doc.getType());
                                docSearch.setId(doc.getId());
                                docSearch.setSourceType(doc.getSourceType());
                                docSearch.setVisibility(doc.getVisibility());
                                docSearch.setName(doc.getName());
                                contentData.add(docSearch);
                            } else {
                                throw new GovernanceException("artifact id is null of " + apiPath);
                            }

                        } else {
                            String apiArtifactId = resource.getUUID();
                            //API api;
                            //APIProduct apiProduct;
                            String type;
                            if (apiArtifactId != null) {
                                GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                                if (apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TYPE).
                                        equals(APIConstants.API_PRODUCT)) {
                                    //apiProduct = APIUtil.getAPIProduct(apiArtifact, registry);
                                    //apiProductSet.add(apiProduct);
                                    type = APIConstants.API_PRODUCT;
                                } else {
                                    //api = APIUtil.getAPI(apiArtifact, registry);
                                    //apiSet.add(api);
                                    type = APIConstants.API;
                                }
                                PublisherAPI pubAPI = RegistryPersistenceUtil.getAPIForSearch(apiArtifact);
                                PublisherSearchContent content = new PublisherSearchContent();
                                content.setContext(pubAPI.getContext());
                                content.setDescription(pubAPI.getDescription());
                                content.setId(pubAPI.getId());
                                content.setName(pubAPI.getApiName());
                                content.setProvider(
                                        RegistryPersistenceUtil.replaceEmailDomainBack(pubAPI.getProviderName()));
                                content.setType(type);
                                content.setVersion(pubAPI.getVersion());
                                content.setStatus(pubAPI.getStatus());
                                content.setAdvertiseOnly(pubAPI.isAdvertiseOnly());
                                content.setThumbnailUri(pubAPI.getThumbnail());
                                contentData.add(content);
                            } else {
                                throw new GovernanceException("artifact id is null for " + resourcePath);
                            }
                        }
                    }

                }
                result.setTotalCount(totalLength);
                result.setReturnedCount(contentData.size());
                result.setResults(contentData);
            }

        } catch (RegistryException | IndexerException | DocumentationPersistenceException | APIManagementException e) {
            throw new APIPersistenceException("Error while searching for content ", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    @Override
    public DevPortalContentSearchResult searchContentForDevPortal(Organization org, String searchQuery, int start,
                                                                  int offset, UserContext ctx) throws APIPersistenceException {
        log.debug("Requested query for devportal content search: " + searchQuery);
        Map<String, String> attributes = RegistrySearchUtil.getDevPortalSearchAttributes(searchQuery, ctx,
                isAllowDisplayAPIsWithMultipleStatus());

        if (log.isDebugEnabled()) {
            log.debug("Search attributes : " + attributes);
        }
        DevPortalContentSearchResult result = null;
        boolean isTenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(ctx.getUserame(), org.getName());
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            String tenantAwareUsername = getTenantAwareUsername(ctx.getUserame());

            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAwareUsername);

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifactManager docArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.DOCUMENTATION_KEY);
            int maxPaginationLimit = getMaxPaginationLimit();
            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            int tenantId = holder.getTenantId();
            if (tenantId == -1) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }

            UserRegistry systemUserRegistry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
            ContentBasedSearchService contentBasedSearchService = new ContentBasedSearchService();

            SearchResultsBean resultsBean = contentBasedSearchService.searchByAttribute(attributes, systemUserRegistry);
            String errorMsg = resultsBean.getErrorMessage();
            if (errorMsg != null) {
                throw new APIPersistenceException("Error while searching " + errorMsg);
            }
            ResourceData[] resourceData = resultsBean.getResourceDataList();
            int totalLength = PaginationContext.getInstance().getLength();

            if (resourceData != null) {
                result = new DevPortalContentSearchResult();
                List<SearchContent> contentData = new ArrayList<SearchContent>();
                if (log.isDebugEnabled()) {
                    log.debug("Number of records Found: " + resourceData.length);
                }

                for (ResourceData data : resourceData) {

                    String resourcePath = data.getResourcePath();
                    if (resourcePath.contains(APIConstants.APIMGT_REGISTRY_LOCATION)) {
                        int index = resourcePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
                        resourcePath = resourcePath.substring(index);
                        Resource resource = registry.get(resourcePath);
                        if (APIConstants.DOCUMENT_RXT_MEDIA_TYPE.equals(resource.getMediaType()) ||
                                APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE.equals(resource.getMediaType())) {
                            if (resourcePath.contains(APIConstants.INLINE_DOCUMENT_CONTENT_DIR)) {
                                int indexOfContents = resourcePath.indexOf(APIConstants.INLINE_DOCUMENT_CONTENT_DIR);
                                resourcePath = resourcePath.substring(0, indexOfContents) + data.getName();
                            }
                            DocumentSearchContent docSearch = new DocumentSearchContent();
                            Resource docResource = registry.get(resourcePath);
                            String docArtifactId = docResource.getUUID();
                            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docArtifactId);
                            Documentation doc = RegistryPersistenceDocUtil.getDocumentation(docArtifact);
                            int indexOfDocumentation = resourcePath.indexOf(APIConstants.DOCUMENTATION_KEY);
                            String apiPath = resourcePath.substring(0, indexOfDocumentation) + APIConstants.API_KEY;
                            Resource apiResource = registry.get(apiPath);
                            String apiArtifactId = apiResource.getUUID();
                            DevPortalAPI devAPI;
                            if (apiArtifactId != null) {
                                GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                                devAPI = RegistryPersistenceUtil.getDevPortalAPIForSearch(apiArtifact);
                                docSearch.setApiName(devAPI.getApiName());
                                docSearch.setApiProvider(devAPI.getProviderName());
                                docSearch.setApiVersion(devAPI.getVersion());
                                docSearch.setApiUUID(devAPI.getId());
                                docSearch.setDocType(doc.getType());
                                docSearch.setId(doc.getId());
                                docSearch.setSourceType(doc.getSourceType());
                                docSearch.setVisibility(doc.getVisibility());
                                docSearch.setName(doc.getName());
                                contentData.add(docSearch);
                            } else {
                                throw new GovernanceException("artifact id is null of " + apiPath);
                            }

                        } else {
                            String apiArtifactId = resource.getUUID();
                            if (apiArtifactId != null) {
                                GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiArtifactId);
                                DevPortalAPI devAPI = RegistryPersistenceUtil.getDevPortalAPIForSearch(apiArtifact);
                                DevPortalSearchContent content = new DevPortalSearchContent();
                                content.setContext(devAPI.getContext());
                                content.setDescription(devAPI.getDescription());
                                content.setId(devAPI.getId());
                                content.setName(devAPI.getApiName());
                                content.setProvider(
                                        RegistryPersistenceUtil.replaceEmailDomainBack(devAPI.getProviderName()));
                                content.setVersion(devAPI.getVersion());
                                content.setStatus(devAPI.getStatus());
                                content.setBusinessOwner(devAPI.getBusinessOwner());
                                content.setBusinessOwnerEmail(devAPI.getBusinessOwnerEmail());

                                contentData.add(content);
                            } else {
                                throw new GovernanceException("artifact id is null for " + resourcePath);
                            }
                        }
                    }

                }
                result.setTotalCount(totalLength);
                result.setReturnedCount(contentData.size());
                result.setResults(contentData);
            }

        } catch (RegistryException | IndexerException | DocumentationPersistenceException e) {
            throw new APIPersistenceException("Error while searching for content ", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    @Override
    public void changeAPILifeCycle(Organization org, String apiId, String status) throws APIPersistenceException {
        //Unused method
    }

    @Override
    public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile)
            throws WSDLPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String apiSourcePath = RegistryPersistenceUtil.getAPIBasePath(apiProviderName, apiName, apiVersion);
            String wsdlResourcePath = null;
            boolean isZip = false;
            String wsdlResourcePathArchive = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_WSDL_ARCHIVE_LOCATION + apiProviderName + APIConstants.WSDL_PROVIDER_SEPERATOR
                    + apiName + apiVersion + APIConstants.ZIP_FILE_EXTENSION;
            String wsdlResourcePathFile = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.createWsdlFileName(apiProviderName, apiName, apiVersion);
            if (APIConstants.APPLICATION_ZIP.equals(wsdlResourceFile.getContentType())) {
                wsdlResourcePath = wsdlResourcePathArchive;
                isZip = true;
            } else {
                wsdlResourcePath = wsdlResourcePathFile;
            }

            String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
            String visibleRolesList = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);

            Resource wsdlResource = registry.newResource();

            wsdlResource.setContentStream(wsdlResourceFile.getContent());
            if (wsdlResourceFile.getContentType() != null) {
                wsdlResource.setMediaType(wsdlResourceFile.getContentType());
            }
            registry.put(wsdlResourcePath, wsdlResource);
            //set the anonymous role for wsld resource to avoid basicauth security.
            String[] visibleRoles = null;
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, visibleRoles, wsdlResourcePath);

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
            String absoluteWSDLResourcePath = RegistryUtils
                    .getAbsolutePath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                    + wsdlResourcePath;
            String wsdlRegistryPath;
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                    .equalsIgnoreCase(tenantDomain)) {
                wsdlRegistryPath =
                        RegistryConstants.PATH_SEPARATOR + "registry" + RegistryConstants.PATH_SEPARATOR + "resource"
                                + absoluteWSDLResourcePath;
            } else {
                wsdlRegistryPath = "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource" + absoluteWSDLResourcePath;
            }
            apiArtifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, wsdlRegistryPath);
            apiArtifactManager.updateGenericArtifact(apiArtifact);
        } catch (APIPersistenceException | APIManagementException | RegistryException e) {
            throw new WSDLPersistenceException("Error while saving the wsdl for api " + apiId, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    @Override
    public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registry);
            if (apiArtifact == null) {
                return null;
            }
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiSourcePath = apiPath.substring(0, prependIndex);
            String wsdlResourcePath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.createWsdlFileName(apiProviderName, apiName, apiVersion);
            String wsdlResourcePathOld = APIConstants.API_WSDL_RESOURCE_LOCATION
                    + RegistryPersistenceUtil.createWsdlFileName(apiProviderName, apiName, apiVersion);
            String resourceFileName = apiProviderName + "-" + apiName + "-" + apiVersion;
            if (registry.resourceExists(wsdlResourcePath)) {
                Resource resource = registry.get(wsdlResourcePath);
                ResourceFile returnResource = new ResourceFile(resource.getContentStream(), resource.getMediaType());
                returnResource.setName(resourceFileName);
                return returnResource;
            } else if (registry.resourceExists(wsdlResourcePathOld)) {
                Resource resource = registry.get(wsdlResourcePathOld);
                ResourceFile returnResource = new ResourceFile(resource.getContentStream(), resource.getMediaType());
                returnResource.setName(resourceFileName);
                return returnResource;
            } else {
                wsdlResourcePath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_WSDL_ARCHIVE_LOCATION + apiProviderName
                        + APIConstants.WSDL_PROVIDER_SEPERATOR + apiName + apiVersion + APIConstants.ZIP_FILE_EXTENSION;
                wsdlResourcePathOld = APIConstants.API_WSDL_RESOURCE_LOCATION + APIConstants.API_WSDL_ARCHIVE_LOCATION
                        + apiProviderName + APIConstants.WSDL_PROVIDER_SEPERATOR + apiName + apiVersion
                        + APIConstants.ZIP_FILE_EXTENSION;
                if (registry.resourceExists(wsdlResourcePath)) {
                    Resource resource = registry.get(wsdlResourcePath);
                    ResourceFile returnResource = new ResourceFile(resource.getContentStream(), resource.getMediaType());
                    returnResource.setName(resourceFileName);
                    return returnResource;
                } else if (registry.resourceExists(wsdlResourcePathOld)) {
                    Resource resource = registry.get(wsdlResourcePathOld);
                    ResourceFile returnResource = new ResourceFile(resource.getContentStream(), resource.getMediaType());
                    returnResource.setName(resourceFileName);
                    return returnResource;
                } else {
                    throw new WSDLPersistenceException("No WSDL found for the API: " + apiId,
                            ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API, apiName, apiVersion));
                }
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Error while getting wsdl file from the registry for API: " + apiId.toString();
            throw new WSDLPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void saveOASDefinition(Organization org, String apiId, String apiDefinition) throws OASPersistenceException {

        boolean isTenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

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

        } catch (RegistryException | APIPersistenceException | APIManagementException e) {
            throw new OASPersistenceException("Error while adding OSA Definition for " + apiId, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        String apiTenantDomain = org.getName();
        String definition = null;
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(apiTenantDomain);
            Registry registryType = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted;

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registryType);
            if (apiArtifact != null) {
                String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
                String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
                String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
                String apiPath = GovernanceUtils.getArtifactPath(registryType, apiId);
                int prependIndex = apiPath.lastIndexOf("/api");
                String apiSourcePath = apiPath.substring(0, prependIndex);
                String definitionPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

                if (registryType.resourceExists(definitionPath)) {
                    Resource apiDocResource = registryType.get(definitionPath);
                    definition = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                    return definition;
                }
            }

        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Failed to get swagger documentation of API : " + apiId;
            throw new OASPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return definition;
    }

    @Override
    public void saveAsyncDefinition(Organization org, String apiId, String apiDefinition)
            throws AsyncSpecPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager artifactManager = RegistryPersistenceUtil
                    .getArtifactManager(registry, APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when deleting API " + apiId;
                log.error(errorMessage);
                throw new AsyncSpecPersistenceException(errorMessage);
            }

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
            String visibleRoles = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiSourcePath = apiPath.substring(0, prependIndex);
            String resourcePath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                    + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinition);
            resource.setMediaType(APIConstants.APPLICATION_JSON_MEDIA_TYPE);          //add a constant for app.json
            registry.put(resourcePath, resource);

            String[] visibleRolesArr = null;
            if (visibleRoles != null) {
                visibleRolesArr = visibleRoles.split(",");
            }

            RegistryPersistenceUtil
                    .clearResourcePermissions(resourcePath, new APIIdentifier(apiProviderName, apiName, apiVersion),
                            ((UserRegistry) registry).getTenantId());
            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, visibleRolesArr, resourcePath);

        } catch (RegistryException | APIPersistenceException | APIManagementException e) {
            throw new AsyncSpecPersistenceException("Error while adding AsyncApi Definition for " + apiId, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public String getAsyncDefinition(Organization org, String apiId) throws AsyncSpecPersistenceException {
        String apiTenantDomain = org.getName();
        String definition = null;
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(apiTenantDomain);
            Registry registryType = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted;

            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registryType,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
            if (apiArtifact != null) {
                String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
                String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
                String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

                String apiPath = GovernanceUtils.getArtifactPath(registryType, apiId);
                int prependIndex = apiPath.lastIndexOf("/api");
                String apiSourcePath = apiPath.substring(0, prependIndex);
                String definitionPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR
                        + APIConstants.API_ASYNC_API_DEFINITION_RESOURCE_NAME;

                if (registryType.resourceExists(definitionPath)) {
                    Resource apiDocResource = registryType.get(definitionPath);
                    definition = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                    return definition;
                }
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Failed to get specification of API : " + apiId;
            throw new AsyncSpecPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return definition;
    }

    @Override
    public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition)
            throws GraphQLPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            BasicAPI api = getbasicAPIInfo(apiId, registry);
            if (api == null) {
                throw new GraphQLPersistenceException("API not foud ", ExceptionCodes.API_NOT_FOUND);
            }
            String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + api.apiProvider
                    + RegistryConstants.PATH_SEPARATOR + api.apiName + RegistryConstants.PATH_SEPARATOR + api.apiVersion
                    + RegistryConstants.PATH_SEPARATOR;

            String saveResourcePath = path + api.apiProvider + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR
                    + api.apiName + api.apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
            Resource resource;
            if (!registry.resourceExists(saveResourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(saveResourcePath);
            }

            resource.setContent(schemaDefinition);
            resource.setMediaType(String.valueOf(ContentType.TEXT_PLAIN));
            registry.put(saveResourcePath, resource);
            if (log.isDebugEnabled()) {
                log.debug("Successfully imported the schema: " + schemaDefinition);
            }

            // Need to set anonymous if the visibility is public
            RegistryPersistenceUtil.clearResourcePermissions(saveResourcePath,
                    new APIIdentifier(api.apiProvider, api.apiName, api.apiVersion),
                    ((UserRegistry) registry).getTenantId());
            RegistryPersistenceUtil.setResourcePermissions(api.apiProvider, api.visibility, api.visibleRoles,
                    saveResourcePath);

        } catch (RegistryException | APIManagementException | APIPersistenceException e) {
            throw new GraphQLPersistenceException("Error while adding Graphql Definition for api " + apiId, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override
    public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        boolean tenantFlowStarted = false;
        String schemaDoc = null;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
            BasicAPI api = getbasicAPIInfo(apiId, registry);
            if (api == null) {
                throw new GraphQLPersistenceException("API not foud ", ExceptionCodes.API_NOT_FOUND);
            }
            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiSourcePath = apiPath.substring(0, prependIndex);
            String schemaName = api.apiProvider + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR + api.apiName
                    + api.apiVersion + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION;
            String schemaResourcePath = apiSourcePath + RegistryConstants.PATH_SEPARATOR + schemaName;
            if (registry.resourceExists(schemaResourcePath)) {
                Resource schemaResource = registry.get(schemaResourcePath);
                schemaDoc = IOUtils.toString(schemaResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
            }
        } catch (APIPersistenceException | RegistryException | IOException e) {
            throw new GraphQLPersistenceException("Error while accessing graphql schema definition ", e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return schemaDoc;
    }

    @Override
    public Documentation addDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();
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
            docArtifactManager.addGenericArtifact(RegistryPersistenceDocUtil.createDocArtifactContent(docArtifact,
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
            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, visibility, authorizedRoles, docArtifact
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
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override
    public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
            throws DocumentationPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            GenericArtifactManager artifactManager = RegistryPersistenceDocUtil.getDocumentArtifactManager(registry);
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

            GenericArtifact updateApiArtifact = RegistryPersistenceDocUtil.createDocArtifactContent(artifact,
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
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override
    public Documentation getDocumentation(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        Documentation documentation = null;
        boolean tenantFlowStarted = false;
        try {
            String requestedTenantDomain = org.getName();
            RegistryHolder holder = getRegistry(requestedTenantDomain);
            Registry registryType = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager artifactManager = RegistryPersistenceDocUtil
                    .getDocumentArtifactManager(registryType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);

            if (artifact == null) {
                return documentation;
            }
            if (null != artifact) {
                documentation = RegistryPersistenceDocUtil.getDocumentation(artifact);
                documentation.setCreatedDate(registryType.get(artifact.getPath()).getCreatedTime());
                Date lastModified = registryType.get(artifact.getPath()).getLastModified();
                if (lastModified != null) {
                    documentation.setLastUpdated(registryType.get(artifact.getPath()).getLastModified());
                }
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Failed to get documentation details";
            throw new DocumentationPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return documentation;
    }

    @Override
    public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
            throws DocumentationPersistenceException {
        DocumentContent documentContent = null;
        boolean tenantFlowStarted = false;
        try {
            String requestedTenantDomain = org.getName();
            RegistryHolder holder = getRegistry(requestedTenantDomain);
            Registry registryType = holder.getRegistry();
            tenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager artifactManager = RegistryPersistenceDocUtil
                    .getDocumentArtifactManager(registryType);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docId);

            if (artifact == null) {
                return null;
            }
            if (artifact != null) {
                Documentation documentation = RegistryPersistenceDocUtil.getDocumentation(artifact);
                if (documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    String resource = documentation.getFilePath();

                    if (resource == null) {
                        return null;
                    }

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
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Failed to get documentation details";
            throw new DocumentationPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return documentContent;
    }

    @Override
    public DocumentContent addDocumentationContent(Organization org, String apiId, String docId,
                                                   DocumentContent content) throws DocumentationPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            GenericArtifactManager docArtifactManager = RegistryPersistenceDocUtil
                    .getDocumentArtifactManager(registry);
            GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docId);
            Documentation doc = RegistryPersistenceDocUtil.getDocumentation(docArtifact);

            if (DocumentContent.ContentSourceType.FILE.equals(content.getSourceType())) {
                ResourceFile resource = content.getResourceFile();
                String filePath = RegistryPersistenceDocUtil.getDocumentFilePath(apiProviderName, apiName, apiVersion,
                        resource.getName());
                String visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);
                String visibleRolesList = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
                String[] visibleRoles = new String[0];
                if (visibleRolesList != null) {
                    visibleRoles = visibleRolesList.split(",");
                }
                RegistryPersistenceUtil.setResourcePermissions(
                        RegistryPersistenceUtil.replaceEmailDomain(apiProviderName), visibility, visibleRoles, filePath,
                        registry);
                //documentation.setFilePath(addResourceFile(apiId, filePath, icon));
                String savedFilePath = addResourceFile(filePath, resource, registry, tenantDomain);
                //doc.setFilePath(savedFilePath);
                docArtifact.setAttribute(APIConstants.DOC_FILE_PATH, savedFilePath);
                docArtifactManager.updateGenericArtifact(docArtifact);
                RegistryPersistenceUtil.setFilePermission(filePath);
            } else {
                String contentPath = RegistryPersistenceDocUtil.getDocumentContentPath(apiProviderName, apiName,
                        apiVersion, doc.getName());
                Resource docContent;

                if (!registry.resourceExists(contentPath)) {
                    docContent = registry.newResource();
                } else {
                    docContent = registry.get(contentPath);
                }
                String text = content.getTextContent();
                if (!APIConstants.NO_CONTENT_UPDATE.equals(text)) {
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
                GenericArtifact updateDocArtifact = RegistryPersistenceDocUtil.createDocArtifactContent(docArtifact,
                        apiProviderName, apiName, apiVersion, doc);
                Boolean toggle = Boolean.parseBoolean(updateDocArtifact.getAttribute("toggle"));
                updateDocArtifact.setAttribute("toggle", Boolean.toString(!toggle));
                docArtifactManager.updateGenericArtifact(updateDocArtifact);
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
        boolean isTenantFlowStarted = false;
        try {

            RegistryHolder holder = getRegistry(requestedTenantDomain);
            registryType = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registryType,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String apiPath = GovernanceUtils.getArtifactPath(registryType, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiSourcePath = apiPath.substring(0, prependIndex);
            String apiOrAPIProductDocPath = apiSourcePath + RegistryConstants.PATH_SEPARATOR +
                    APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;

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
                            GenericArtifactManager artifactManager = RegistryPersistenceDocUtil
                                    .getDocumentArtifactManager(registryType);
                            GenericArtifact docArtifact = artifactManager.getGenericArtifact(docResource.getUUID());
                            Documentation doc = RegistryPersistenceDocUtil.getDocumentation(docArtifact);
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
        } catch (RegistryException | APIPersistenceException e) {
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
        boolean isTenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            GenericArtifactManager artifactManager = RegistryPersistenceDocUtil.getDocumentArtifactManager(registry);
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

        } catch (RegistryException | APIPersistenceException e) {
            throw new DocumentationPersistenceException("Failed to delete documentation", e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId)
            throws MediationPolicyPersistenceException {
        boolean isTenantFlowStarted = false;
        Mediation mediation = null;

        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            BasicAPI api = getbasicAPIInfo(apiId, registry);
            if (api == null) {
                throw new MediationPolicyPersistenceException("API not foud ", ExceptionCodes.API_NOT_FOUND);
            }
            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiResourcePath = apiPath.substring(0, prependIndex);
            String policyPath = GovernanceUtils.getArtifactPath(registry, mediationPolicyId);
            if (!policyPath.startsWith(apiResourcePath)) {
                throw new MediationPolicyPersistenceException("Policy not foud ", ExceptionCodes.POLICY_NOT_FOUND);
            }
            Resource mediationResource = registry.get(policyPath);
            if (mediationResource != null) {
                String contentString = IOUtils.toString(mediationResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                // Extracting name specified in the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName("name"));
                String mediationPolicyName = attribute.getAttributeValue();
                String[] path = policyPath.split(RegistryConstants.PATH_SEPARATOR);
                String resourceType = path[(path.length - 2)];
                mediation = new Mediation();
                mediation.setConfig(contentString);
                mediation.setType(resourceType);
                mediation.setId(mediationResource.getUUID());
                mediation.setName(mediationPolicyName);
            }
        } catch (RegistryException | APIPersistenceException | IOException | XMLStreamException e) {
            String msg = "Error occurred  while getting Api Specific mediation policies ";
            throw new MediationPolicyPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return mediation;
    }

    @Override
    public List<MediationInfo> getAllMediationPolicies(Organization org, String apiId)
            throws MediationPolicyPersistenceException {
        boolean isTenantFlowStarted = false;
        List<MediationInfo> mediationList = new ArrayList<MediationInfo>();
        MediationInfo mediation;

        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            BasicAPI api = getbasicAPIInfo(apiId, registry);
            if (api == null) {
                throw new MediationPolicyPersistenceException("API not foud ", ExceptionCodes.API_NOT_FOUND);
            }
            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String apiResourcePath = apiPath.substring(0, prependIndex);

            // apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            // Getting API registry resource
            Resource resource = registry.get(apiResourcePath);
            // resource eg: /_system/governance/apimgt/applicationdata/provider/admin/calculatorAPI/2.0
            if (resource instanceof Collection) {
                Collection typeCollection = (Collection) resource;
                String[] typeArray = typeCollection.getChildren();
                for (String type : typeArray) {
                    // Check for mediation policy sequences
                    if ((type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR
                            + APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN))
                            || (type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR
                            + APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT))
                            || (type.equalsIgnoreCase(apiResourcePath + RegistryConstants.PATH_SEPARATOR
                            + APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT))) {
                        Resource typeResource = registry.get(type);
                        // typeResource : in / out / fault
                        if (typeResource instanceof Collection) {
                            String[] mediationPolicyArr = ((Collection) typeResource).getChildren();
                            if (mediationPolicyArr.length > 0) {
                                for (String mediationPolicy : mediationPolicyArr) {
                                    Resource policyResource = registry.get(mediationPolicy);
                                    // policyResource eg: custom_in_message

                                    // Get uuid of the registry resource
                                    String resourceId = policyResource.getUUID();

                                    // Get mediation policy config
                                    try {
                                        String contentString = IOUtils.toString(policyResource.getContentStream(),
                                                RegistryConstants.DEFAULT_CHARSET_ENCODING);
                                        // Extract name from the policy config
                                        OMElement omElement = AXIOMUtil.stringToOM(contentString);
                                        OMAttribute attribute = omElement.getAttribute(new QName("name"));
                                        String mediationPolicyName = attribute.getAttributeValue();
                                        mediation = new MediationInfo();
                                        mediation.setId(resourceId);
                                        mediation.setName(mediationPolicyName);
                                        // Extracting mediation policy type from the registry resource path
                                        String resourceType = type.substring(type.lastIndexOf("/") + 1);
                                        mediation.setType(resourceType);
                                        mediationList.add(mediation);
                                    } catch (XMLStreamException e) {
                                        // If exception been caught flow will continue with next mediation policy
                                        log.error(
                                                "Error occurred while getting omElement out of" + " mediation content",
                                                e);
                                    } catch (IOException e) {
                                        log.error("Error occurred while converting the content "
                                                + "stream of mediation " + mediationPolicy + " to string", e);
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Error occurred  while getting Api Specific mediation policies ";
            throw new MediationPolicyPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return mediationList;
    }

    @Override
    public void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile)
            throws ThumbnailPersistenceException {
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            Registry registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifactManager apiArtifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiId);
            if (apiArtifact == null) {
                throw new ThumbnailPersistenceException("API not found. ", ExceptionCodes.API_NOT_FOUND);
            }
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    apiProviderName + RegistryConstants.PATH_SEPARATOR +
                    apiName + RegistryConstants.PATH_SEPARATOR + apiVersion;
            String filePath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

            String savedFilePath = addResourceFile(filePath, resourceFile, registry, tenantDomain);

            RegistryPersistenceUtil.setResourcePermissions(apiProviderName, null, null, filePath);

            apiArtifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, savedFilePath);
            apiArtifactManager.updateGenericArtifact(apiArtifact);
        } catch (APIPersistenceException | GovernanceException | PersistenceException | APIManagementException e) {
            throw new ThumbnailPersistenceException("Error while saving thumbnail for api " + apiId, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {

        Registry registry;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registry);
            if (apiArtifact == null) {
                return null;
            }
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String artifactOldPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + apiProviderName + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR
                    + apiVersion;
            String apiPath = GovernanceUtils.getArtifactPath(registry, apiId);
            int prependIndex = apiPath.lastIndexOf("/api");
            String artifactPath = apiPath.substring(0, prependIndex);
            String oldThumbPath = artifactOldPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
            String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                return new ResourceFile(res.getContentStream(), res.getMediaType());
            } else if (registry.resourceExists(oldThumbPath)) {
                Resource res = registry.get(oldThumbPath);
                return new ResourceFile(res.getContentStream(), res.getMediaType());
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Error while loading API icon of API " + apiId + " from the registry";
            throw new ThumbnailPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }

    @Override
    public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        Registry registry;
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            GenericArtifact apiArtifact = getAPIArtifact(apiId, registry);
            if (apiArtifact == null) {
                throw new ThumbnailPersistenceException("API not found for id " + apiId, ExceptionCodes.API_NOT_FOUND);
            }
            String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            apiProviderName = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
            String apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);

            String artifactOldPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + apiProviderName + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR
                    + apiVersion;
            String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProviderName
                    + RegistryConstants.PATH_SEPARATOR + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion;

            String oldThumbPath = artifactOldPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
            String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;

            if (registry.resourceExists(thumbPath)) {
                registry.delete(thumbPath);
            }
            if (registry.resourceExists(oldThumbPath)) {
                registry.delete(oldThumbPath);
            }
        } catch (RegistryException | APIPersistenceException e) {
            String msg = "Error while loading API icon of API " + apiId + " from the registry";
            throw new ThumbnailPersistenceException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Persist API Status into a property of API Registry resource
     *
     * @param artifactId API artifact ID
     * @param apiStatus  Current status of the API
     * @throws APIManagementException on error
     */
    private void saveAPIStatus(Registry registry, String artifactId, String apiStatus) throws APIManagementException {
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
    private void updateRegistryResources(Registry registry, String artifactPath, String publisherAccessControlRoles,
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

    class RegistryHolder {
        private Registry registry;
        private boolean isTenantFlowStarted;
        private int tenantId;
        private boolean isAnonymousMode;

        public boolean isAnonymousMode() {
            return isAnonymousMode;
        }

        public void setAnonymousMode(boolean anonymousMode) {
            isAnonymousMode = anonymousMode;
        }

        public Registry getRegistry() {
            return registry;
        }

        public void setRegistry(Registry registry) {
            this.registry = registry;
        }

        public boolean isTenantFlowStarted() {
            return isTenantFlowStarted;
        }

        public void setTenantFlowStarted(boolean isTenantFlowStarted) {
            this.isTenantFlowStarted = isTenantFlowStarted;
        }

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }
    }

    protected RegistryHolder getRegistry(String requestedTenantDomain) throws APIPersistenceException {

        String userTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.debug("Accessing system registry in tenant domain " + userTenantDomain + ". Requested tenant domain: "
                + requestedTenantDomain);
        boolean tenantFlowStarted = false;
        Registry registry;
        RegistryHolder holder = new RegistryHolder();

        try {
            if (requestedTenantDomain != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;

                if (userTenantDomain != null && !userTenantDomain.equals(requestedTenantDomain)) { // cross tenant
                    log.debug("Cross tenant user from tenant " + userTenantDomain + " accessing "
                            + requestedTenantDomain + " registry");
                    loadTenantRegistry(id);
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                    holder.setTenantId(id);
                } else {
                    log.debug("Same tenant accessing registry of tenant " + userTenantDomain + ":" + tenantId);
                    loadTenantRegistry(tenantId);
                    registry = getRegistryService().getGovernanceSystemRegistry(tenantId);
                    RegistryPersistenceUtil.loadloadTenantAPIRXT(null, tenantId);
                    RegistryPersistenceUtil.addLifecycleIfNotExists(tenantId);
                    RegistryPersistenceUtil.registerCustomQueries(registry, null, userTenantDomain);
                    holder.setTenantId(tenantId);
                }
            } else {
                log.debug("Same tenant user accessing registry of tenant " + userTenantDomain + ":" + tenantId);
                loadTenantRegistry(tenantId);
                registry = getRegistryService().getGovernanceSystemRegistry(tenantId);
                RegistryPersistenceUtil.loadloadTenantAPIRXT(null, tenantId);
                RegistryPersistenceUtil.addLifecycleIfNotExists(tenantId);
                RegistryPersistenceUtil.registerCustomQueries(registry, null, userTenantDomain);
                holder.setTenantId(tenantId);
            }
        } catch (RegistryException | UserStoreException | PersistenceException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        }
        holder.setRegistry(registry);
        holder.setTenantFlowStarted(tenantFlowStarted);
        return holder;
    }

    protected RegistryHolder getRegistry(String username, String requestedTenantDomain) throws APIPersistenceException {

        String tenantAwareUserName = getTenantAwareUsername(username);
        String userTenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.debug("Accessing registry for user:" + tenantAwareUserName + " in tenant domain " + userTenantDomain
                + ". Requested tenant domain: " + requestedTenantDomain);
        boolean tenantFlowStarted = false;
        Registry registry;
        Registry configRegistry;
        RegistryHolder holder = new RegistryHolder();
        try {
            if (requestedTenantDomain != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                if (APIConstants.WSO2_ANONYMOUS_USER.equals(tenantAwareUserName)) { // annonymous
                    log.debug("Annonymous user from tenant " + userTenantDomain + " accessing the registry");
                    loadTenantRegistry(id);
                    registry = getRegistryService().getGovernanceUserRegistry(tenantAwareUserName, id);
                    configRegistry = getRegistryService().getConfigSystemRegistry();
                    holder.setTenantId(id);
                } else if (userTenantDomain != null && !userTenantDomain.equals(requestedTenantDomain)) { // cross tenant
                    holder.setAnonymousMode(true);
                    log.debug("Cross tenant user from tenant " + userTenantDomain + " accessing "
                            + requestedTenantDomain + " registry");
                    loadTenantRegistry(id);
                    registry = getRegistryService().getGovernanceSystemRegistry(id);
                    configRegistry = getRegistryService().getConfigSystemRegistry(id);
                    holder.setTenantId(id);
                } else {
                    log.debug("Same tenant user : " + tenantAwareUserName + " accessing registry of tenant "
                            + userTenantDomain + ":" + tenantId);
                    loadTenantRegistry(tenantId);
                    registry = getRegistryService().getGovernanceUserRegistry(tenantAwareUserName, tenantId);
                    configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);
                    RegistryPersistenceUtil.loadloadTenantAPIRXT(tenantAwareUserName, tenantId);
                    holder.setTenantId(tenantId);
                }
            } else {
                log.debug("Same tenant user : " + tenantAwareUserName + " accessing registry of tenant "
                        + userTenantDomain + ":" + tenantId);
                loadTenantRegistry(tenantId);
                registry = getRegistryService().getGovernanceUserRegistry(tenantAwareUserName, tenantId);
                configRegistry = getRegistryService().getConfigSystemRegistry(tenantId);
                RegistryPersistenceUtil.loadloadTenantAPIRXT(tenantAwareUserName, tenantId);
                holder.setTenantId(tenantId);
            }
            RegistryPersistenceUtil.registerCustomQueries(configRegistry, username, userTenantDomain);
            RegistryPersistenceUtil.addLifecycleIfNotExists(tenantId);
        } catch (RegistryException | UserStoreException | PersistenceException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        }
        holder.setRegistry(registry);
        holder.setTenantFlowStarted(tenantFlowStarted);
        return holder;
    }

    private BasicAPI getbasicAPIInfo(String uuid, Registry registry)
            throws APIPersistenceException, GovernanceException {
        BasicAPI api = new BasicAPI();
        GenericArtifact apiArtifact = getAPIArtifact(uuid, registry);
        if (apiArtifact == null) {
            return null;
        }
        String apiProviderName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        api.apiProvider = RegistryPersistenceUtil.replaceEmailDomain(apiProviderName);
        api.apiName = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        api.apiVersion = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        String visibleRolesList = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES);
        if (visibleRolesList != null) {
            api.visibleRoles = visibleRolesList.split(",");
        }
        api.visibility = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY);

        return api;
    }

    private class BasicAPI {
        String apiName;
        String apiVersion;
        String apiProvider;
        String visibility;
        String[] visibleRoles;
    }

    @Override
    public PublisherAPIProduct addAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException {

        Registry registry = null;
        boolean isTenantFlowStarted = false;
        boolean transactionCommitted = false;
        APIProduct apiProduct;
        try {
            String tenantDomain = org.getName();
            RegistryHolder holder = getRegistry(tenantDomain);
            registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            GenericArtifact genericArtifact =
                    artifactManager.newGovernanceArtifact(new QName(publisherAPIProduct.getApiProductName()));
            apiProduct = APIProductMapper.INSTANCE.toApiProduct(publisherAPIProduct);
            APIProductIdentifier id = new APIProductIdentifier(publisherAPIProduct.getProviderName(),
                    publisherAPIProduct.getApiProductName(), publisherAPIProduct.getVersion());
            apiProduct.setID(id);
            if (genericArtifact == null) {
                String errorMessage = "Generic artifact is null when creating API Product" + apiProduct.getId().getName();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact artifact = RegistryPersistenceUtil.createAPIProductArtifactContent(genericArtifact, apiProduct);
            artifactManager.addGenericArtifact(artifact);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            String providerPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + id.getProviderName();
            //provider ------provides----> APIProduct
            registry.addAssociation(providerPath, artifactPath, APIConstants.PROVIDER_ASSOCIATION);

            String apiProductStatus = apiProduct.getState();
            saveAPIStatus(registry, artifactPath, apiProductStatus);

            Set<String> tagSet = apiProduct.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }

            String visibleRolesList = apiProduct.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }

            String publisherAccessControlRoles = apiProduct.getAccessControlRoles();
            updateRegistryResources(registry, artifactPath, publisherAccessControlRoles, apiProduct.getAccessControl(),
                    apiProduct.getAdditionalProperties());
            RegistryPersistenceUtil.setResourcePermissions(apiProduct.getId().getProviderName(),
                    apiProduct.getVisibility(), visibleRoles, artifactPath, registry);

            registry.commitTransaction();
            transactionCommitted = true;

            if (log.isDebugEnabled()) {
                String logMessage =
                        "API Product Name: " + apiProduct.getId().getName() + ", API Product Version "
                                + apiProduct.getId().getVersion() + " created";
                log.debug(logMessage);
            }

            publisherAPIProduct.setCreatedTime(String.valueOf(new Date().getTime()));
            publisherAPIProduct.setId(artifact.getId());
            return publisherAPIProduct;
        } catch (RegistryException e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error here would mask the original exception
                log.error("Error while rolling back the transaction for API Product : "
                        + publisherAPIProduct.getApiProductName(), re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } catch (APIManagementException e) {
            throw new APIPersistenceException("Error while creating API Product", e);
        } finally {
            try {
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error while rolling back the transaction for API Product : "
                        + publisherAPIProduct.getApiProductName());
            }
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public PublisherAPIProduct getPublisherAPIProduct(Organization org, String apiProductId)
            throws APIPersistenceException {
        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            tenantFlowStarted = holder.isTenantFlowStarted();
            Registry registry = holder.getRegistry();

            GenericArtifact apiArtifact = getAPIArtifact(apiProductId, registry);
            if (apiArtifact != null) {
                APIProduct apiProduct = RegistryPersistenceUtil.getAPIProduct(apiArtifact, registry);
                String definitionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                        + RegistryPersistenceUtil.replaceEmailDomain(apiProduct.getId().getProviderName())
                        + RegistryConstants.PATH_SEPARATOR + apiProduct.getId().getName()
                        + RegistryConstants.PATH_SEPARATOR + apiProduct.getId().getVersion()
                        + RegistryConstants.PATH_SEPARATOR + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;

                if (registry.resourceExists(definitionPath)) {
                    Resource apiDocResource = registry.get(definitionPath);
                    String apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                    apiProduct.setDefinition(apiDocContent);
                }
                PublisherAPIProduct pubApi = APIProductMapper.INSTANCE.toPublisherApiProduct(apiProduct);
                pubApi.setApiProductName(apiProduct.getId().getName());
                pubApi.setProviderName(apiProduct.getId().getProviderName());
                pubApi.setVersion(apiProduct.getId().getVersion());

                if (log.isDebugEnabled()) {
                    log.debug("API Product for id " + apiProductId + " : " + pubApi.toString());
                }
                return pubApi;
            } else {
                String msg = "Failed to get API. API artifact corresponding to artifactId " + apiProductId
                        + " does not exist";
                throw new APIMgtResourceNotFoundException(msg);
            }
        } catch (RegistryException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }

    @Override
    public PublisherAPIProductSearchResult searchAPIProductsForPublisher(Organization org, String searchQuery,
                                                                         int start, int offset, UserContext ctx) throws APIPersistenceException {
        String requestedTenantDomain = org.getName();

        boolean isTenantFlowStarted = false;
        PublisherAPIProductSearchResult result = new PublisherAPIProductSearchResult();
        try {
            RegistryHolder holder = getRegistry(ctx.getUserame(), requestedTenantDomain);
            Registry userRegistry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();

            log.debug("Requested query for publisher product search: " + searchQuery);

            String modifiedQuery = RegistrySearchUtil.getPublisherProductSearchQuery(searchQuery, ctx);

            log.debug("Modified query for publisher product search: " + modifiedQuery);

            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(ctx.getUserame());

            final int maxPaginationLimit = getMaxPaginationLimit();

            PaginationContext.init(start, offset, "ASC", APIConstants.API_OVERVIEW_NAME, maxPaginationLimit);

            List<GovernanceArtifact> governanceArtifacts = GovernanceUtils
                    .findGovernanceArtifacts(modifiedQuery, userRegistry, APIConstants.API_RXT_MEDIA_TYPE,
                            true);
            int totalLength = PaginationContext.getInstance().getLength();

            // Check to see if we can speculate that there are more APIs to be loaded
            if (maxPaginationLimit == totalLength) {
                --totalLength; // Remove the additional 1 added earlier when setting max pagination limit
            }

            int tempLength = 0;
            List<PublisherAPIProductInfo> publisherAPIProductInfoList = new ArrayList<PublisherAPIProductInfo>();
            for (GovernanceArtifact artifact : governanceArtifacts) {

                PublisherAPIProductInfo info = new PublisherAPIProductInfo();
                info.setProviderName(artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER));
                info.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
                info.setId(artifact.getId());
                info.setApiProductName(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME));
                info.setState(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS));
                info.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
                info.setVersion(artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
                info.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));
                info.setThumbnail(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));

                publisherAPIProductInfoList.add(info);

                // Ensure the APIs returned matches the length, there could be an additional API
                // returned due incrementing the pagination limit when getting from registry
                tempLength++;
                if (tempLength >= totalLength) {
                    break;
                }
            }

            result.setPublisherAPIProductInfoList(publisherAPIProductInfoList);
            result.setReturnedAPIsCount(publisherAPIProductInfoList.size());
            result.setTotalAPIsCount(totalLength);

        } catch (GovernanceException e) {
            throw new APIPersistenceException("Error while searching APIs ", e);
        } finally {
            PaginationContext.destroy();
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return result;
    }

    @Override
    public PublisherAPIProduct updateAPIProduct(Organization org, PublisherAPIProduct publisherAPIProduct)
            throws APIPersistenceException {
        String requestedTenantDomain = org.getName();
        boolean isTenantFlowStarted = false;
        boolean transactionCommitted = false;
        APIProduct apiProduct;
        Registry registry = null;
        try {
            RegistryHolder holder = getRegistry(requestedTenantDomain);
            registry = holder.getRegistry();
            isTenantFlowStarted = holder.isTenantFlowStarted();
            registry.beginTransaction();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when updating API Product with artifact ID "
                        + publisherAPIProduct.getId();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact artifact = artifactManager.getGenericArtifact(publisherAPIProduct.getId());

            apiProduct = APIProductMapper.INSTANCE.toApiProduct(publisherAPIProduct);
            APIProductIdentifier id = new APIProductIdentifier(publisherAPIProduct.getProviderName(),
                    publisherAPIProduct.getApiProductName(), publisherAPIProduct.getVersion());
            apiProduct.setID(id);
            GenericArtifact updateApiProductArtifact = RegistryPersistenceUtil.createAPIProductArtifactContent(artifact,
                    apiProduct);
            String artifactPath = GovernanceUtils.getArtifactPath(registry, updateApiProductArtifact.getId());

            artifactManager.updateGenericArtifact(updateApiProductArtifact);

            String visibleRolesList = apiProduct.getVisibleRoles();
            String[] visibleRoles = new String[0];
            if (visibleRolesList != null) {
                visibleRoles = visibleRolesList.split(",");
            }
            org.wso2.carbon.registry.core.Tag[] oldTags = registry.getTags(artifactPath);
            if (oldTags != null) {
                for (org.wso2.carbon.registry.core.Tag tag : oldTags) {
                    registry.removeTag(artifactPath, tag.getTagName());
                }
            }
            Set<String> tagSet = apiProduct.getTags();
            if (tagSet != null) {
                for (String tag : tagSet) {
                    registry.applyTag(artifactPath, tag);
                }
            }
            String publisherAccessControlRoles = apiProduct.getAccessControlRoles();

            updateRegistryResources(registry, artifactPath, publisherAccessControlRoles, apiProduct.getAccessControl(),
                    apiProduct.getAdditionalProperties());
            RegistryPersistenceUtil.setResourcePermissions(apiProduct.getId().getProviderName(),
                    apiProduct.getVisibility(), visibleRoles, artifactPath, registry);
            registry.commitTransaction();
            transactionCommitted = true;
            return publisherAPIProduct;
        } catch (Exception e) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException re) {
                // Throwing an error from this level will mask the original exception
                log.error("Error while rolling back the transaction for API Product: "
                        + publisherAPIProduct.getApiProductName(), re);
            }
            throw new APIPersistenceException("Error while performing registry transaction operation", e);
        } finally {
            try {
                if (registry != null && !transactionCommitted) {
                    registry.rollbackTransaction();
                }
            } catch (RegistryException ex) {
                log.error("Error occurred while rolling back the transaction.", ex);
            }
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    public void deleteAPIProduct(Organization org, String apiId) throws APIPersistenceException {

        boolean tenantFlowStarted = false;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            tenantFlowStarted = holder.isTenantFlowStarted();
            Registry registry = holder.getRegistry();
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Failed to retrieve artifact manager when deleting API Product" + apiId;
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            GenericArtifact apiProductArtifact = artifactManager.getGenericArtifact(apiId);

            APIProductIdentifier identifier = new APIProductIdentifier(
                    apiProductArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER),
                    apiProductArtifact.getAttribute(APIConstants.API_OVERVIEW_NAME),
                    apiProductArtifact.getAttribute(APIConstants.API_OVERVIEW_VERSION));
            // this is the product resource collection path
            String productResourcePath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.replaceEmailDomain(identifier.getProviderName())
                    + RegistryConstants.PATH_SEPARATOR + identifier.getName() + RegistryConstants.PATH_SEPARATOR
                    + identifier.getVersion();

            // this is the product rxt instance path
            String apiProductArtifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + RegistryPersistenceUtil.replaceEmailDomain(identifier.getProviderName())
                    + RegistryConstants.PATH_SEPARATOR + identifier.getName() + RegistryConstants.PATH_SEPARATOR
                    + identifier.getVersion() + APIConstants.API_RESOURCE_NAME;

            Resource apiProductResource = registry.get(productResourcePath);
            String productResourceUUID = apiProductResource.getUUID();

            if (productResourceUUID == null) {
                throw new APIManagementException("artifact id is null for : " + productResourcePath);
            }

            Resource apiArtifactResource = registry.get(apiProductArtifactPath);
            String apiArtifactResourceUUID = apiArtifactResource.getUUID();

            if (apiArtifactResourceUUID == null) {
                throw new APIManagementException("artifact id is null for : " + apiProductArtifactPath);
            }

            // Delete the dependencies associated with the api product artifact
            GovernanceArtifact[] dependenciesArray = apiProductArtifact.getDependencies();
            if (dependenciesArray.length > 0) {
                for (GovernanceArtifact artifact : dependenciesArray) {
                    registry.delete(artifact.getPath());
                }
            }

            // delete registry resources
            artifactManager.removeGenericArtifact(apiProductArtifact);
            artifactManager.removeGenericArtifact(productResourceUUID);

            /* remove empty directories */
            String apiProductCollectionPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getName();
            if (registry.resourceExists(apiProductCollectionPath)) {
                // at the moment product versioning is not supported so we are directly deleting this collection as
                // this is known to be empty
                registry.delete(apiProductCollectionPath);
            }

            String productProviderPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                    + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + identifier.getName();

            if (registry.resourceExists(productProviderPath)) {
                Resource providerCollection = registry.get(productProviderPath);
                CollectionImpl collection = (CollectionImpl) providerCollection;
                // if there is no api product for given provider delete the provider directory
                if (collection.getChildCount() == 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("No more API Products from the provider " + identifier.getProviderName() + " found. "
                                + "Removing provider collection from registry");
                    }
                    registry.delete(productProviderPath);
                }
            }
            /*remove revision directory with UUID*/
            String revisionDirectoryPath = APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR +
                    apiId;
            if (registry.resourceExists(revisionDirectoryPath)) {
                registry.delete(revisionDirectoryPath);
            }

        } catch (RegistryException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } catch (APIManagementException e) {
            String msg = "Failed to get API";
            throw new APIPersistenceException(msg, e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }

    }

    protected GenericArtifact getAPIArtifact(String apiId, Registry registry)
            throws APIPersistenceException, GovernanceException {
        GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                APIConstants.API_KEY);
        GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiId);
        return apiArtifact;
    }

    protected List<SOAPToRestSequence> getSoapToRestSequences(Registry registry, API api, Direction direction)
            throws RegistryException, APIPersistenceException {
        String resourcePath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR
                + RegistryPersistenceUtil.replaceEmailDomain(api.getId().getProviderName())
                + RegistryConstants.PATH_SEPARATOR + api.getId().getName() + RegistryConstants.PATH_SEPARATOR
                + api.getId().getVersion() + RegistryConstants.PATH_SEPARATOR + "soap_to_rest"
                + RegistryConstants.PATH_SEPARATOR;
        if (direction == Direction.IN) {
            resourcePath = resourcePath + "in";
        } else if (direction == Direction.OUT) {
            resourcePath = resourcePath + "out";
        } else {
            throw new APIPersistenceException("Invalid sequence type");
        }

        List<SOAPToRestSequence> sequences = new ArrayList<SOAPToRestSequence>();
        if (registry.resourceExists(resourcePath)) {
            Collection collection = (Collection) registry.get(resourcePath);
            String[] resources = collection.getChildren();
            for (String path : resources) {
                Resource resource = registry.get(path);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                String resourceName;
                if (resource.getProperty("resourcePath") != null) {
                    resourceName = resource.getProperty("resourcePath");
                } else {
                    resourceName = ((ResourceImpl) resource).getName();
                }
                resourceName = resourceName.replaceAll("\\.xml", "");
                resourceName = resourceName.split("_")[0];
                String httpMethod = resource.getProperty("method");

                SOAPToRestSequence seq = new SOAPToRestSequence(httpMethod, resourceName, content, direction);
                seq.setUuid(resource.getUUID());
                sequences.add(seq);
            }
        }
        return sequences;
    }

    protected void setSoapToRestSequences(PublisherAPI publisherAPI, Registry registry) throws RegistryException {
        if (publisherAPI.getSoapToRestSequences() != null && !publisherAPI.getSoapToRestSequences().isEmpty()) {
            List<SOAPToRestSequence> sequence = publisherAPI.getSoapToRestSequences();
            for (SOAPToRestSequence soapToRestSequence : sequence) {

                String apiResourceName = soapToRestSequence.getPath();
                if (apiResourceName.startsWith("/")) {
                    apiResourceName = apiResourceName.substring(1);
                }
                String resourcePath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR
                        + RegistryPersistenceUtil.replaceEmailDomain(publisherAPI.getProviderName())
                        + RegistryConstants.PATH_SEPARATOR + publisherAPI.getApiName()
                        + RegistryConstants.PATH_SEPARATOR + publisherAPI.getVersion()
                        + RegistryConstants.PATH_SEPARATOR;
                if (soapToRestSequence.getDirection() == Direction.OUT) {
                    resourcePath = resourcePath + "soap_to_rest" + RegistryConstants.PATH_SEPARATOR + "out"
                            + RegistryConstants.PATH_SEPARATOR;
                } else {
                    resourcePath = resourcePath + "soap_to_rest" + RegistryConstants.PATH_SEPARATOR + "in"
                            + RegistryConstants.PATH_SEPARATOR;
                }

                resourcePath = resourcePath + apiResourceName + "_" + soapToRestSequence.getMethod() + ".xml";

                Resource regResource;
                if (!registry.resourceExists(resourcePath)) {
                    regResource = registry.newResource();
                    regResource.setContent(soapToRestSequence.getContent());
                    regResource.addProperty("method", soapToRestSequence.getMethod());
                    if (regResource.getProperty("resourcePath") != null) {
                        regResource.removeProperty("resourcePath");
                    }
                    regResource.addProperty("resourcePath", apiResourceName);
                    regResource.setMediaType("text/xml");
                    registry.put(resourcePath, regResource);
                }
            }

        }

    }

    @Override
    public Set<Tag> getAllTags(Organization org, UserContext ctx) throws APIPersistenceException {
        TreeSet<Tag> tempTagSet = new TreeSet<Tag>(new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Registry userRegistry = null;
        boolean tenantFlowStarted = false;
        String tagsQueryPath = null;
        try {
            RegistryHolder holder = getRegistry(org.getName());
            tenantFlowStarted = holder.isTenantFlowStarted();
            userRegistry = holder.getRegistry();

            tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
            Map<String, String> params = new HashMap<String, String>();
            params.put(RegistryConstants.RESULT_TYPE_PROPERTY_NAME, RegistryConstants.TAG_SUMMARY_RESULT_TYPE);
            String userNameLocal;
            if (holder.isAnonymousMode()) {
                userNameLocal = APIConstants.WSO2_ANONYMOUS_USER;
            } else {
                userNameLocal = getTenantAwareUsername(ctx.getUserame());
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userNameLocal);

            Map<String, Tag> tagsData = new HashMap<String, Tag>();

            Map<String, List<String>> criteriaPublished = new HashMap<String, List<String>>();
            criteriaPublished.put(APIConstants.API_OVERVIEW_STATUS_SEARCH_KEY, new ArrayList<String>() {
                {
                    add(APIConstants.PUBLISHED);
                }
            });
            // rxt api media type
            List<TermData> termsPublished = GovernanceUtils.getTermDataList(criteriaPublished,
                    APIConstants.API_OVERVIEW_TAG, APIConstants.API_RXT_MEDIA_TYPE, true);

            if (termsPublished != null) {
                for (TermData data : termsPublished) {
                    tempTagSet.add(new Tag(data.getTerm(), (int) data.getFrequency()));
                }
            }

            Map<String, List<String>> criteriaPrototyped = new HashMap<String, List<String>>();
            criteriaPrototyped.put(APIConstants.API_OVERVIEW_STATUS_SEARCH_KEY, new ArrayList<String>() {
                {
                    add(APIConstants.PROTOTYPED);
                }
            });
            // rxt api media type
            List<TermData> termsPrototyped = GovernanceUtils.getTermDataList(criteriaPrototyped,
                    APIConstants.API_OVERVIEW_TAG, APIConstants.API_RXT_MEDIA_TYPE, true);

            if (termsPrototyped != null) {
                for (TermData data : termsPrototyped) {
                    tempTagSet.add(new Tag(data.getTerm(), (int) data.getFrequency()));
                }
            }
            return tempTagSet;

        } catch (RegistryException e) {
            try {
                // Before a tenant login to the store or publisher at least one time,
                // a registry exception is thrown when the tenant store is accessed in anonymous mode.
                // This fix checks whether query resource available in the registry. If not
                // give a warn.
                if (userRegistry != null && !userRegistry.resourceExists(tagsQueryPath)) {
                    log.warn("Failed to retrieve tags query resource at " + tagsQueryPath);
                    return Collections.EMPTY_SET;
                }
            } catch (RegistryException e1) {
                // Even if we should ignore this exception, we are logging this as a warn log.
                // The reason is that, this error happens when we try to add some additional logs in an error
                // scenario and it does not affect the execution path.
                log.warn("Unable to execute the resource exist method for tags query resource path : " + tagsQueryPath,
                        e1);
            }
            throw new APIPersistenceException("Failed to get all the tags", e);
        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
    }
}
