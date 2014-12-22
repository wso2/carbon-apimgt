/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

/**
 * The basic abstract implementation of the core APIManager interface. This implementation uses
 * the governance system registry for storing APIs and related metadata.
 */
public abstract class AbstractAPIManager implements APIManager {

    protected Log log = LogFactory.getLog(getClass());

    protected Registry registry;
    protected ApiMgtDAO apiMgtDAO;
    protected int tenantId;
    protected String tenantDomain;
    protected String username;

    public AbstractAPIManager() throws APIManagementException {
    }

    public AbstractAPIManager(String username) throws APIManagementException {
        apiMgtDAO = new ApiMgtDAO();
        UserRegistry configRegistry;
        try {
            if (username == null) {
                this.registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry();
                configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getConfigSystemRegistry();
                this.username= CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME;
                ServiceReferenceHolder.setUserRealm((UserRealm)(ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealm()));
            } else {
                String tenantDomainName = MultitenantUtils.getTenantDomain(username);
                String tenantUserName = MultitenantUtils.getTenantAwareUsername(username);
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomainName);
                this.tenantId=tenantId;
                this.tenantDomain=tenantDomainName;
                this.username=tenantUserName;

                APIUtil.loadTenantRegistry(tenantId);

                this.registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(tenantUserName, tenantId);
                configRegistry = ServiceReferenceHolder.getInstance().getRegistryService().
                        getConfigSystemRegistry(tenantId);
                //load resources for each tenants.
                APIUtil.loadloadTenantAPIRXT( tenantUserName, tenantId);
                APIUtil.loadTenantAPIPolicy( tenantUserName, tenantId);
                APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
                ServiceReferenceHolder.setUserRealm((UserRealm)(ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(tenantId)));
            }
            ServiceReferenceHolder.setUserRealm(ServiceReferenceHolder.getInstance().
                    getRegistryService().getConfigSystemRegistry().getUserRealm());
            registerCustomQueries(configRegistry, username);
        } catch (RegistryException e) {
            handleException("Error while obtaining registry objects", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Error while getting user registry for user:"+username, e);
        }

    }

    /**
     * method to register custom registry queries
     * @param registry  Registry instance to use
     * @throws RegistryException n error
     */
    private void registerCustomQueries(UserRegistry registry, String username)
            throws RegistryException, APIManagementException {
        String tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
        String latestAPIsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/latest-apis";
        String resourcesByTag = RegistryConstants.QUERIES_COLLECTION_PATH + "/resource-by-tag";
        String path =
                      RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                    APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                           RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                            "/repository/components/org.wso2.carbon.governance");
        if (username == null) {
            try {
                UserRealm realm = ServiceReferenceHolder.getUserRealm();
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager(realm);
                authorizationManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);

            } catch (UserStoreException e) {
                handleException("Error while setting the permissions", e);
            }
        }else if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            int tenantId = 0;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(tenantId).getAuthorizationManager();
                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                handleException("Error while setting the permissions", e);
            }

        }

        if (!registry.resourceExists(tagsQueryPath)) {
            Resource resource = registry.newResource();

            //Tag Search Query
            //'MOCK_PATH' used to bypass ChrootWrapper -> filterSearchResult. A valid registry path is
            // a must for executeQuery results to be passed to client side
            String sql1 =
                    "SELECT  " +
                                  "   '" +
                                  APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                         RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                  "/repository/components/org.wso2.carbon.governance' AS MOCK_PATH, " +
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
//            String sql =
//                    "SELECT " +
//                    "   RR.REG_PATH_ID," +
//                    "   RR.REG_NAME " +
//                    "FROM " +
//                    "   REG_RESOURCE RR " +
//                    "WHERE " +
//                    "   RR.REG_MEDIA_TYPE = 'application/vnd.wso2-api+xml' " +
//                    "ORDER BY " +
//                    "   RR.REG_LAST_UPDATED_TIME DESC ";
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
        if(!registry.resourceExists(resourcesByTag)){
            Resource resource = registry.newResource();
            String sql =
                    "SELECT " +
                                 "   '" +
                                 APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                 "/repository/components/org.wso2.carbon.governance' AS MOCK_PATH, " +
                    "   R.REG_UUID AS REG_UUID " +
                    "FROM " +
                    "   REG_RESOURCE_TAG RRT, " +
                    "   REG_TAG RT, " +
                    "   REG_RESOURCE R, " +
                    "   REG_PATH RP " +
                    "WHERE " +
                    "   RT.REG_TAG_NAME = ? "+
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

    public void cleanup() {

    }

    public List<API> getAllAPIs() throws APIManagementException {
        List<API> apiSortedList = new ArrayList<API>();
        boolean isTenantFlowStarted = false;
        try {
        	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain))	{
        		isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        	}
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                apiSortedList.add(APIUtil.getAPI(artifact));
            }

        } catch (RegistryException e) {
            handleException("Failed to get APIs from the registry", e);
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }

        Collections.sort(apiSortedList, new APINameComparator());
        return apiSortedList;
    }

    public API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
            Registry registry;
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !this.tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPIForPublishing(apiArtifact, registry);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    public API getAPI(String apiPath) throws APIManagementException {
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    public boolean isAPIAvailable(APIIdentifier identifier) throws APIManagementException {
        String path = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                      identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                      identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        try {
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            handleException("Failed to check availability of api :" + path, e);
            return false;
        }
    }

    public Set<String> getAPIVersions(String providerName, String apiName)
            throws APIManagementException {

        Set<String> versionSet = new HashSet<String>();
        String apiPath = APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                         providerName + RegistryConstants.PATH_SEPARATOR + apiName;
        try {
            Resource resource = registry.get(apiPath);
            if (resource instanceof Collection) {
                Collection collection = (Collection) resource;
                String[] versionPaths = collection.getChildren();
                if (versionPaths == null || versionPaths.length == 0) {
                    return versionSet;
                }
                for (String path : versionPaths) {
                    versionSet.add(path.substring(apiPath.length() + 1));
                }
            } else {
                throw new APIManagementException("API version must be a collection " + apiName);
            }
        } catch (RegistryException e) {
            handleException("Failed to get versions for API: " + apiName, e);
        }
        return versionSet;
    }

    public String addIcon(String resourcePath, Icon icon) throws APIManagementException {
        try {
            Resource thumb = registry.newResource();
            thumb.setContentStream(icon.getContent());
            thumb.setMediaType(icon.getContentType());
            registry.put(resourcePath, thumb);
            if(tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
            return RegistryConstants.PATH_SEPARATOR + "registry"
                   + RegistryConstants.PATH_SEPARATOR + "resource"
                   + RegistryConstants.PATH_SEPARATOR + "_system"
                   + RegistryConstants.PATH_SEPARATOR + "governance"
                   + resourcePath;
            }
            else{
                return "/t/"+tenantDomain+ RegistryConstants.PATH_SEPARATOR + "registry"
                        + RegistryConstants.PATH_SEPARATOR + "resource"
                        + RegistryConstants.PATH_SEPARATOR + "_system"
                        + RegistryConstants.PATH_SEPARATOR + "governance"
                        + resourcePath;
            }
        } catch (RegistryException e) {
            handleException("Error while adding the icon image to the registry", e);
        }
        return null;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath = APIUtil.getAPIPath(apiId);
        try {
        	Association[] docAssociations = registry.getAssociations(apiResourcePath,
                                                                     APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();

                Resource docResource = registry.get(docPath);
                GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                    APIConstants.DOCUMENTATION_KEY);
                GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                        docResource.getUUID());
                Documentation doc = APIUtil.getDocumentation(docArtifact);
                Date contentLastModifiedDate;
                Date docLastModifiedDate = docResource.getLastModified();
                if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                    String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                    contentLastModifiedDate = registry.get(contentPath).getLastModified();
                    doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                        contentLastModifiedDate : docLastModifiedDate));
                }else{
                    doc.setLastUpdated(docLastModifiedDate);
                }


                documentationList.add(doc);
            }
            /* Document for loading API definition Content - Swagger*/
            Documentation documentation = new Documentation(DocumentationType.SWAGGER_DOC, APIConstants.API_DEFINITION_DOC_NAME);
            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            documentation.setSourceType(docSourceType);
            documentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);

            String swaggerDocPath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR + 
            		apiId.getApiName() +"-"  + apiId.getVersion() +'-'+apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_DOC_RESOURCE_NAME;
            if (registry.resourceExists(swaggerDocPath)) {
            	Resource docResource = registry.get(swaggerDocPath);
            	documentation.setLastUpdated(docResource.getLastModified());
                String visibility=docResource.getProperty(APIConstants.VISIBILITY);
                if(visibility==null){visibility=APIConstants.DOC_API_BASED_VISIBILITY;}
                if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.API_LEVEL.toString())) {
                    documentation.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
                } else if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.PRIVATE.toString())) {
                    documentation.setVisibility(Documentation.DocumentVisibility.PRIVATE);
                } else {
                    documentation.setVisibility(Documentation.DocumentVisibility.OWNER_ONLY);
                }
            	documentationList.add(documentation);
            }

        } catch (RegistryException e) {
            handleException("Failed to get documentations for api " + apiId.getApiName(), e);
        }
        return documentationList;
    }

    public List<Documentation> getAllDocumentation(APIIdentifier apiId,String loggedUsername) throws APIManagementException {
        List<Documentation> documentationList = new ArrayList<Documentation>();
        String apiResourcePath = APIUtil.getAPIPath(apiId);
        try {
        	String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            Registry registryType;
            /* If the API provider is a tenant, load tenant registry*/
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                registryType = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            Association[] docAssociations = registryType.getAssociations(apiResourcePath,
                    APIConstants.DOCUMENTATION_ASSOCIATION);
            for (Association association : docAssociations) {
                String docPath = association.getDestinationPath();
                Resource docResource = null;
                try {
                    docResource = registryType.get(docPath);
                } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                    //do nothing. Permission not allowed to access the doc.
                }catch (RegistryException e){e.printStackTrace();}
                if (docResource != null) {
                    GenericArtifactManager artifactManager = new GenericArtifactManager(registryType,
                            APIConstants.DOCUMENTATION_KEY);
                    GenericArtifact docArtifact = artifactManager.getGenericArtifact(
                            docResource.getUUID());
                    Documentation doc = APIUtil.getDocumentation(docArtifact, apiId.getProviderName());
                    Date contentLastModifiedDate;
                    Date docLastModifiedDate = docResource.getLastModified();
                    if (Documentation.DocumentSourceType.INLINE.equals(doc.getSourceType())) {
                        String contentPath = APIUtil.getAPIDocContentPath(apiId, doc.getName());
                         try{
                        contentLastModifiedDate = registryType.get(contentPath).getLastModified();
                        doc.setLastUpdated((contentLastModifiedDate.after(docLastModifiedDate) ?
                                     contentLastModifiedDate : docLastModifiedDate));
                         } catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
                        //do nothing. Permission not allowed to access the doc.
                         }

                    }else{
                        doc.setLastUpdated(docLastModifiedDate);
                    }
                    documentationList.add(doc);
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to get documentations for api " + apiId.getApiName(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
        	handleException("Failed to get documentations for api " + apiId.getApiName(), e);
		}
        return documentationList;
    }

    private boolean isTenantDomainNotMatching(String tenantDomain) {
        if (this.tenantDomain != null) {
            return !(this.tenantDomain.equals(tenantDomain));
        }
        return true;
    }

    public Documentation getDocumentation(APIIdentifier apiId, DocumentationType docType,
                                          String docName) throws APIManagementException {
        Documentation documentation = null;
        String docPath = APIUtil.getAPIDocPath(apiId) + docName;
        GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                            APIConstants.DOCUMENTATION_KEY);
        try {
            Resource docResource = registry.get(docPath);
            GenericArtifact artifact = artifactManager.getGenericArtifact(docResource.getUUID());
            documentation = APIUtil.getDocumentation(artifact);
        } catch (RegistryException e) {
            handleException("Failed to get documentation details", e);
        }
        return documentation;
    }

    public String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        String contentPath = APIUtil.getAPIDocPath(identifier) +
                             APIConstants.INLINE_DOCUMENT_CONTENT_DIR + RegistryConstants.PATH_SEPARATOR +
                             documentationName;
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(identifier.getProviderName()));
        Registry registry;
        try {
	        /* If the API provider is a tenant, load tenant registry*/
	        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
	            int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
	            registry = ServiceReferenceHolder.getInstance().
	                    getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !this.tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceUserRegistry(identifier.getProviderName(), MultitenantConstants.SUPER_TENANT_ID);
                } else {
                    registry = this.registry;
                }
            }

            if (registry.resourceExists(contentPath)) {
                Resource docContent = registry.get(contentPath);
                Object content = docContent.getContent();
                if (content != null) {
                    return new String((byte[]) docContent.getContent());
                }
            }
            /* Loading API definition Content - Swagger*/
            if(documentationName != null && documentationName.equals(APIConstants.API_DEFINITION_DOC_NAME))
            {
                String swaggerDocPath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                        identifier.getApiName() +"-"  + identifier.getVersion() + "-" + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_DOC_RESOURCE_NAME;
                /* API Definition content will be loaded only in API Provider. Hence globally initialized
           * registry can be used here.*/
                if (this.registry.resourceExists(swaggerDocPath)) {
                    Resource docContent = registry.get(swaggerDocPath);
                    Object content = docContent.getContent();
                    if (content != null) {
                        return new String((byte[]) docContent.getContent());
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "No document content found for documentation: "
                         + documentationName + " of API: "+identifier.getApiName();
            handleException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
        	handleException("Failed to get ddocument content found for documentation: "
        				 + documentationName + " of API: "+identifier.getApiName(), e);
		}
        return null;
    }

    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        return apiMgtDAO.getSubscriberById(accessToken);
    }

    public boolean isContextExist(String context) throws APIManagementException {
    	boolean isTenantFlowStarted = false;
        try {
        	if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
        		isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        	}
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                String artifactContext = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT);
                artifactContext=artifactContext.substring(artifactContext.lastIndexOf("/"));
                if (artifactContext.equalsIgnoreCase(context)) {
                    return true;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to check context availability : " + context, e);
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        return false;
    }


    public boolean isApiNameExist(String apiName) throws APIManagementException {
        boolean isTenantFlowStarted = false;
        try {
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
            for (GenericArtifact artifact : artifacts) {
                String artifactName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
                if (artifactName.equalsIgnoreCase(apiName)) {
                    return true;
                }
            }
        } catch (RegistryException e) {
            handleException("Failed to check api name availability : " + apiName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return false;
    }

    public void addSubscriber(Subscriber subscriber)
            throws APIManagementException {
        apiMgtDAO.addSubscriber(subscriber);
    }

    public void updateSubscriber(Subscriber subscriber)
            throws APIManagementException {
        apiMgtDAO.updateSubscriber(subscriber);
    }

    public Subscriber getSubscriber(int subscriberId)
            throws APIManagementException {
        return apiMgtDAO.getSubscriber(subscriberId);
    }

    public Icon getIcon(APIIdentifier identifier) throws APIManagementException {
        String artifactPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                              identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                              identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();

        String thumbPath = artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        try {
            if (registry.resourceExists(thumbPath)) {
                Resource res = registry.get(thumbPath);
                Icon icon = new Icon(res.getContentStream(), res.getMediaType());
                return icon;
            }
        } catch (RegistryException e) {
            handleException("Error while loading API icon from the registry", e);
        }
        return null;
    }

    public Set<API> getSubscriberAPIs(Subscriber subscriber) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Set<SubscribedAPI> subscribedAPIs = apiMgtDAO.getSubscribedAPIs(subscriber);
        boolean isTenantFlowStarted = false;
        try {
	        if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
	        	isTenantFlowStarted = true;
	            PrivilegedCarbonContext.startTenantFlow();
	            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
	        }
	        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
	            String apiPath = APIUtil.getAPIPath(subscribedAPI.getApiId());
	            Resource resource;
	            try {
	                resource = registry.get(apiPath);
	                GenericArtifactManager artifactManager = new GenericArtifactManager(registry, APIConstants.API_KEY);
	                GenericArtifact artifact = artifactManager.getGenericArtifact(
	                        resource.getUUID());
	                API api = APIUtil.getAPI(artifact, registry);
	                apiSortedSet.add(api);
	            } catch (RegistryException e) {
	                handleException("Failed to get APIs for subscriber: " + subscriber.getName(), e);
	            }
	        }
        } finally {
        	if (isTenantFlowStarted) {
        		PrivilegedCarbonContext.endTenantFlow();
        	}
        }
        return apiSortedSet;
    }

    protected void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }

    protected void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }


    public boolean isApplicationTokenExists(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenExists(accessToken);
    }

    public boolean isApplicationTokenRevoked(String accessToken) throws APIManagementException {
        return apiMgtDAO.isAccessTokenRevoked(accessToken);
    }


    public APIKey getAccessTokenData(String accessToken) throws APIManagementException {
        return apiMgtDAO.getAccessTokenData(accessToken);
    }

    public Map<Integer, APIKey> searchAccessToken(String searchType, String searchTerm, String loggedInUser)
            throws APIManagementException {
        if (searchType == null) {
            return apiMgtDAO.getAccessTokens(searchTerm);
        } else {
            if (searchType.equalsIgnoreCase("User")) {
                return apiMgtDAO.getAccessTokensByUser(searchTerm, loggedInUser);
            } else if (searchType.equalsIgnoreCase("Before")) {
                return apiMgtDAO.getAccessTokensByDate(searchTerm, false, loggedInUser);
            }  else if (searchType.equalsIgnoreCase("After")) {
                return apiMgtDAO.getAccessTokensByDate(searchTerm, true, loggedInUser);
            } else {
                return apiMgtDAO.getAccessTokens(searchTerm);
            }
        }

    }
    public Set<APIIdentifier> getAPIByAccessToken(String accessToken) throws APIManagementException{
        return apiMgtDAO.getAPIByAccessToken(accessToken);
    }
    public API getAPI(APIIdentifier identifier,APIIdentifier oldIdentifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                                                                                APIConstants.API_KEY);
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry,oldIdentifier);

        } catch (RegistryException e) {
            handleException("Failed to get API from : " + apiPath, e);
            return null;
        }
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers() throws APIManagementException {


        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());

        Map<String, Tier> tierMap;
        if (tenantId == 0) {
            tierMap = APIUtil.getTiers();
        } else {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            tierMap = APIUtil.getTiers(tenantId);
            PrivilegedCarbonContext.endTenantFlow();
        }
        tiers.addAll(tierMap.values());

        return tiers;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public Set<Tier> getTiers(String tenantDomain) throws APIManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());

        Map<String, Tier> tierMap;
        int requestedTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (requestedTenantId == 0) {
            tierMap = APIUtil.getTiers();
        } else {
            tierMap = APIUtil.getTiers(requestedTenantId);
        }
        tiers.addAll(tierMap.values());
        PrivilegedCarbonContext.endTenantFlow();
        return tiers;
    }

    @Override
    public String getSwaggerDefinition(APIIdentifier apiId) throws APIManagementException {
        String resourcePath = APIUtil.getAPIDefinitionFilePath(apiId.getApiName(),
                apiId.getVersion(), apiId.getProviderName());

        JSONParser parser = new JSONParser();
        JSONObject apiJSON = null;
        try {
            Resource apiDocResource;
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
            Registry registryType;
            /* If the API provider is a tenant, load tenant registry*/
            boolean isTenantMode=(tenantDomain != null);
            if ((isTenantMode && this.tenantDomain==null) || (isTenantMode && isTenantDomainNotMatching(tenantDomain))) {//Tenant store anonymous mode
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                registryType = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceUserRegistry(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, tenantId);
            } else {
                registryType = registry;
            }
            if (registryType.resourceExists(resourcePath)) {
            try{
            apiDocResource= registryType.get(resourcePath);
            String apiDocContent = new String((byte []) apiDocResource.getContent());
            apiJSON = (JSONObject) parser.parse(apiDocContent);
            }catch (org.wso2.carbon.registry.core.secure.AuthorizationFailedException e) {
            //Permission not allowed to access the doc.
            return  APIConstants.NO_PERMISSION_ERROR;
            }

            }else{
            return  APIConstants.NO_PERMISSION_ERROR;
            }


        } catch (RegistryException e) {
            log.error("Error while retrieving Swagger Definition for " + apiId.getApiName() + "-" +
                    apiId.getVersion(), e);
            return  APIConstants.NO_PERMISSION_ERROR;
        } catch (ParseException e) {
            log.error("Error while parsing Swagger Definition for " + apiId.getApiName() + "-" +
                    apiId.getVersion() + " in " + resourcePath, e);
            return  APIConstants.JSON_PARSE_ERROR;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while parsing Swagger Definition for " + apiId.getApiName() + "-" +
                    apiId.getVersion() + " in " + resourcePath, e);
            return  APIConstants.JSON_PARSE_ERROR;
        }
        return apiJSON.toJSONString();
    }
}
