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
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Icon;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APINameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIStoreNameComparator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException n error
     */
    private void registerCustomQueries(UserRegistry registry, String username)
            throws RegistryException, APIManagementException {
        String tagsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/tag-summary";
        String latestAPIsQueryPath = RegistryConstants.QUERIES_COLLECTION_PATH + "/latest-apis";
        String resourcesByTag = RegistryConstants.QUERIES_COLLECTION_PATH + "/resource-by-tag";
        String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                    APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                           RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                    APIConstants.GOVERNANCE_COMPONENT_REGISTRY_LOCATION);
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
                    "SELECT '" + APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
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
                    "SELECT '" + APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
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

    /**
     * Returns details of an API
     *
     * @param idObj APIIdentifier
     * @return An API object related to the given identifier or null
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed get API from APIIdentifier
     */
    public JSONObject getAPI(JSONObject idObj) throws APIManagementException {

        String providerName = (String) idObj.get("provider");
        String apiName = (String) idObj.get("name");
        String version = (String) idObj.get("version");
        APIIdentifier identifier = new APIIdentifier(providerName, apiName, version);
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            return getJSONfyAPIData(getAPI(identifier));

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    protected API getAPI(APIIdentifier identifier) throws APIManagementException {
        Registry registry;
        String apiPath = APIUtil.getAPIPath(identifier);
        try {
        	//adding tenantDomain check to provide access to the API details page to the anonymous user
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
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

    public JSONArray getAllDocumentation(JSONObject apiObj) throws APIManagementException {
        JSONArray docArr = new JSONArray();
        String providerName = (String) apiObj.get("provider");
        String apiName = (String) apiObj.get("name");
        String version = (String) apiObj.get("version");
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            APIIdentifier apiId = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName, version);
            List<Documentation> docs = getAllDocumentation(apiId);
            return getJSONfyDocumentationList(docs);


        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    protected List<Documentation> getAllDocumentation(APIIdentifier apiId) throws APIManagementException {

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

    private JSONArray getJSONfyDocumentationList(List<Documentation> docs) {
        JSONArray docsArr = new JSONArray();
        Iterator it = docs.iterator();
        int i = 0;
        while (it.hasNext()) {
            JSONObject row = new JSONObject();
            Object docsObject = it.next();
            Documentation doc = (Documentation) docsObject;
            Object objectSourceType = doc.getSourceType();
            String strSourceType = objectSourceType.toString();
            row.put("docName", doc.getName());
            row.put("docType", doc.getType().getType());
            row.put("sourceType", strSourceType);
            row.put("visibility", doc.getVisibility().name());
            row.put("docLastUpdated", (Long.valueOf(doc.getLastUpdated().getTime()).toString()));
            //row.put("sourceType", row, doc.getSourceType());
            if (Documentation.DocumentSourceType.URL.equals(doc.getSourceType())) {
                row.put("sourceUrl", doc.getSourceUrl());
            }

            if (Documentation.DocumentSourceType.FILE.equals(doc.getSourceType())) {
                row.put("filePath", doc.getFilePath());
            }

            if (doc.getType() == DocumentationType.OTHER) {
                row.put("otherTypeName", doc.getOtherTypeName());
            }

            row.put("summary", doc.getSummary());
            docsArr.add(i, row);
            i++;

        }
        return docsArr;
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

    public JSONObject getDocumentationContent(JSONObject idObj, String documentationName)
            throws APIManagementException {
        String providerName = (String) idObj.get("provider");
        String apiName = (String) idObj.get("name");
        String version = (String) idObj.get("version");
        APIIdentifier identifier = new APIIdentifier(APIUtil.replaceEmailDomain(providerName), apiName,
                                                     version);
        return getJSONfyDocContent(identifier, documentationName, getDocumentationContent(identifier, documentationName));


    }

    protected String getDocumentationContent(APIIdentifier identifier, String documentationName)
            throws APIManagementException {
        boolean isTenantFlowStarted = false;
        Registry registry;
        try {

            String contentPath = APIUtil.getAPIDocPath(identifier) +
                                 APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
                                 RegistryConstants.PATH_SEPARATOR +
                                 documentationName;
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.
                    replaceEmailDomainBack(identifier.getProviderName()));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.
                    equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            /* If the API provider is a tenant, load tenant registry*/
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int id = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getTenantId(tenantDomain);
                registry = ServiceReferenceHolder.getInstance().
                        getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                if (this.tenantDomain != null && !this.tenantDomain.
                        equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    registry = ServiceReferenceHolder.getInstance().
                            getRegistryService().getGovernanceUserRegistry(identifier.getProviderName()
                            , MultitenantConstants.SUPER_TENANT_ID);
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
            if (documentationName != null && documentationName.equals(APIConstants.API_DEFINITION_DOC_NAME)) {
                String swaggerDocPath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
                                        identifier.getApiName() + "-" + identifier.getVersion()
                                        + "-" + identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR
                                        + APIConstants.API_DOC_RESOURCE_NAME;
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
                         + documentationName + " of API: " + identifier.getApiName();
            handleException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "No document content found for documentation: "
                         + documentationName + " of API: " + identifier.getApiName();
            handleException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return null;
    }

    private JSONObject getJSONfyDocContent(APIIdentifier id, String docName, String content) {
        JSONObject row = new JSONObject();
        row.put("providerName", APIUtil.replaceEmailDomainBack(id.getProviderName()));
        row.put("apiName", id.getApiName());
        row.put("apiVersion", id.getVersion());
        row.put("docName", docName);
        row.put("content", content);
        return row;
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
                String artifactContext = artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE);
                // With context version strategy we have to check endswith first
                // ex: /{version}/foo/ --> /{version}/foo
                if(artifactContext.endsWith("/")){
                    artifactContext=artifactContext.substring(artifactContext.lastIndexOf("/"));
                }
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

    public Set<API> getSubscriberAPIs(String userName) throws APIManagementException {
        SortedSet<API> apiSortedSet = new TreeSet<API>(new APINameComparator());
        Subscriber subscriber = new Subscriber(userName);
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
    public API getAPI(APIIdentifier identifier,APIIdentifier oldIdentifier, String oldContext) throws
                                                                                          APIManagementException {
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
            return APIUtil.getAPI(apiArtifact, registry,oldIdentifier, oldContext);

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

    private JSONArray getTiers(Set<Tier> tiers) {
        JSONArray tiersArr = new JSONArray();
        int i = 0;
        for (Tier tier : tiers) {
            JSONObject row = new JSONObject();
            row.put("tierName", tier.getName());
            row.put("tierDisplayName", tier.getDisplayName());
            row.put("tierDescription", tier.getDescription() != null ? tier.getDescription() : "");
            tiersArr.add(i, row);
            i++;
        }
        return tiersArr;
    }

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Set<Tier>
     */
    public JSONArray getTiers(String tenantDomain) throws APIManagementException {
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Map<String, Tier> tierMap;
        if (tenantDomain == null) {
            if (tenantId == 0) {
                tierMap = APIUtil.getTiers();
            } else {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
                tierMap = APIUtil.getTiers(tenantId);
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            int requestedTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (requestedTenantId == 0) {
                tierMap = APIUtil.getTiers();
            } else {
                tierMap = APIUtil.getTiers(requestedTenantId);
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
        tiers.addAll(tierMap.values());
        return getTiers(tiers);
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

    /**
     * Returns a list of pre-defined # {@link org.wso2.carbon.apimgt.api.model.Tier} in the system.
     *
     * @return Map<String, String>
     */
    public Map<String,String> getTenantDomainMappings(String tenantDomain) throws APIManagementException {
        boolean isTenantFlowStarted = false;
        Map<String,String> domains;
        try {
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            int requestedTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            domains = APIUtil.getDomainMappings(requestedTenantId);

        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return domains;
    }

    private JSONObject getJSONfyAPIData(API api) throws APIManagementException {
        JSONObject apiObj = new JSONObject();
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        apiObj.put("name", api.getId().getApiName());
        apiObj.put("description", api.getDescription());
        apiObj.put("url", api.getUrl());
        apiObj.put("wsdlUrl", api.getWsdlUrl());
        apiObj.put("rates", api.getRating());
        apiObj.put("version", api.getId().getVersion());
        StringBuilder tagsSet = new StringBuilder("");
        for (int k = 0; k < api.getTags().toArray().length; k++) {
            tagsSet.append(api.getTags().toArray()[k].toString());
            if (k != api.getTags().toArray().length - 1) {
                tagsSet.append(",");
            }
        }
        apiObj.put("tags", APIUtil.checkValue(tagsSet.toString()));
        StringBuilder tiersSet = new StringBuilder("");
        StringBuilder tiersDisplayNamesSet = new StringBuilder("");
        StringBuilder tiersDescSet = new StringBuilder("");
        Set<Tier> tierSet = api.getAvailableTiers();
        Iterator it = tierSet.iterator();
        int j = 0;
        while (it.hasNext()) {
            Object tierObject = it.next();
            Tier tier = (Tier) tierObject;
            tiersSet.append(tier.getName());
            tiersDisplayNamesSet.append(tier.getDisplayName());
            tiersDescSet.append(tier.getDescription());
            if (j != tierSet.size() - 1) {
                tiersSet.append(",");
                tiersDisplayNamesSet.append(",");
                tiersDescSet.append(",");
            }
            j++;
        }

        apiObj.put("tiers", APIUtil.checkValue(tiersSet.toString()));
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        StringBuilder envDetails = new StringBuilder();
        Set<String> environmentsPublishedByAPI =
                new HashSet<String>(api.getEnvironments());
        environmentsPublishedByAPI.remove("none");
        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            envDetails.append(environment.getName() + ",");
            envDetails.append(APIUtil.filterUrls(environment.getApiGatewayEndpoint(),
                                                 api.getTransports()) + "|");
        }
        if (!envDetails.toString().isEmpty()) {
            //removig last seperator mark
            envDetails = envDetails.deleteCharAt(envDetails.length() - 1);

        }
        apiObj.put("serverURL", envDetails.toString());
        apiObj.put("status", APIUtil.checkValue(api.getStatus().toString()));
        if (api.getThumbnailUrl() == null) {
            apiObj.put("thumbnailUrl", "images/api-default.png");
        } else {
            apiObj.put("thumbnailUrl", APIUtil.prependWebContextRoot(api.getThumbnailUrl()));
        }
        apiObj.put("context", api.getContext());
        //String updateTime=APIUtil.checkValue(Long.valueOf(api.getLastUpdated().getTime()).toString());
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss a z");
        String dateFormatted = dateFormat.format(api.getLastUpdated());
        apiObj.put("lastUpdatedTime",dateFormatted );
        apiObj.put("subscribersCount", getSubscriberCount(api.getId()));
        String apiOwner = api.getApiOwner();
        if (apiOwner == null) {
            apiOwner = APIUtil.replaceEmailDomainBack(api.getId().getProviderName());
        }
        apiObj.put("apiOwner", apiOwner);
        apiObj.put("isAdvertiseOnly", api.isAdvertiseOnly());
        apiObj.put("redirectURL", api.getRedirectURL());
        /*--if (uriTemplates.size() != 0) {
            JSONArray uriTempArr = new JSONArray();
            Iterator i = uriTemplates.iterator();
            List<JSONArray> uriTemplatesArr = new ArrayList<JSONArray>();
            while (i.hasNext()) {
                List<String> utArr = new ArrayList<String>();
                URITemplate ut = (URITemplate) i.next();
                utArr.add(ut.getUriTemplate());
                utArr.add(ut.getMethodsAsString().replaceAll("\\s", ","));
                utArr.add(ut.getAuthTypeAsString().replaceAll("\\s", ","));
                utArr.add(ut.getThrottlingTiersAsString().replaceAll("\\s", ","));
                JSONArray utNArr = new JSONArray();
                for (int p = 0; p < utArr.size(); p++) {
                    utNArr.add(utArr.get(p));
                }
                uriTemplatesArr.add(utNArr);
            }

            for (int c = 0; c < uriTemplatesArr.size(); c++) {
                uriTempArr.add(uriTemplatesArr.get(c));
            }

           apiObj.put("apiResources", uriTempArr);
        }  --*/

        apiObj.put("sandboxUrl", APIUtil.checkValue(api.getSandboxUrl()));
        apiObj.put("tierDescriptions", APIUtil.checkValue(tiersDescSet.toString()));
        apiObj.put("businessOwner", APIUtil.checkValue(api.getBusinessOwner()));
        apiObj.put("businessOwnerMail", APIUtil.checkValue(api.getBusinessOwnerEmail()));
        apiObj.put("techOwner", APIUtil.checkValue(api.getTechnicalOwner()));
        apiObj.put("techOwnerMail", APIUtil.checkValue(api.getTechnicalOwnerEmail()));
        apiObj.put("wadlUrl", APIUtil.checkValue(api.getWadlUrl()));
        apiObj.put("visibility", APIUtil.checkValue(api.getVisibility()));
        apiObj.put("visibleRoles", APIUtil.checkValue(api.getVisibleRoles()));
        apiObj.put("visibleTenants", APIUtil.checkValue(api.getVisibleTenants()));
        apiObj.put("UTUsername", APIUtil.checkValue(api.getEndpointUTUsername()));
        apiObj.put("UTPassword", APIUtil.checkValue(api.getEndpointUTPassword()));
        apiObj.put("isEndpointSecured", APIUtil.checkValue(Boolean.toString(api.isEndpointSecured())));
        apiObj.put("provider", APIUtil.replaceEmailDomainBack(APIUtil.checkValue(api.getId().getProviderName())));
        apiObj.put("httpTransport", APIUtil.checkTransport("http", api.getTransports()));
        apiObj.put("httpsTransport", APIUtil.checkTransport("https", api.getTransports()));
        Set<APIStore> storesSet = getExternalAPIStores(api.getId());
        if (storesSet != null && storesSet.size() != 0) {
            JSONArray apiStoresArray = new JSONArray();
            int i = 0;
            for (APIStore store : storesSet) {
                JSONObject storeObject = new JSONObject();
                storeObject.put("name", store.getName());
                storeObject.put("displayName", store.getDisplayName());
                storeObject.put("published", store.isPublished());
                apiStoresArray.add(storeObject);
                i++;
            }
            apiObj.put("externalAPIStores", apiStoresArray);
        }
        apiObj.put("insequence", APIUtil.checkValue(api.getInSequence()));
        apiObj.put("outsequence", APIUtil.checkValue(api.getOutSequence()));

        apiObj.put("subscriptionAvailability", APIUtil.checkValue(api.getSubscriptionAvailability()));
        apiObj.put("subscriptionAvailableTenants", APIUtil.checkValue(api.getSubscriptionAvailableTenants()));

        //@todo need to handle backword compatibility
        apiObj.put("endpointConfig", APIUtil.checkValue(api.getEndpointConfig()));

        apiObj.put("responseCache", APIUtil.checkValue(api.getResponseCache()));
        apiObj.put("cacheTimeout", APIUtil.checkValue(Integer.toString(api.getCacheTimeout())));
        apiObj.put("tierDislayNames", APIUtil.checkValue(tiersDisplayNamesSet.toString()));

        apiObj.put("faultsequence", APIUtil.checkValue(api.getFaultSequence()));
        apiObj.put("destinationStatsEnabled", APIUtil.checkValue(api.getDestinationStatsEnabled()));

        //todo implement resource load

        if (uriTemplates.size() != 0) {
            JSONArray resourceArray = new JSONArray();
            Iterator i = uriTemplates.iterator();
            while (i.hasNext()) {
                JSONObject resourceObj = new JSONObject();
                URITemplate ut = (URITemplate) i.next();

                resourceObj.put("url_pattern", ut.getUriTemplate());
                resourceObj.put("http_verbs", JSONValue.parse(ut.getResourceMap()));

                resourceArray.add(resourceObj);
            }

            apiObj.put("apiResources", JSONValue.toJSONString(resourceArray));
        }


        Set<Scope> scopes = api.getScopes();
        JSONArray scopesNative = new JSONArray();
        for (Scope scope : scopes) {
            JSONObject scopeNative = new JSONObject();
            scopeNative.put("id", scope.getId());
            scopeNative.put("key", scope.getKey());
            scopeNative.put("name", scope.getName());
            scopeNative.put("roles", scope.getRoles());
            scopeNative.put("description", scope.getDescription());
            scopesNative.add(scopeNative);
        }
        apiObj.put("scopes", scopesNative.toJSONString());
        apiObj.put("defaultVersion", APIUtil.checkValue(Boolean.toString(api.isDefaultVersion())));
        apiObj.put("implementation", api.getImplementation());
        apiObj.put("publishedEnvironments", APIUtil.writeEnvironmentsToArtifact(api));
        return apiObj;

    }

    private int getSubscriberCount(APIIdentifier id)
            throws APIManagementException {

        Set<Subscriber> subs = getSubscribersOfAPI(id);
        Set<String> subscriberNames = new HashSet<String>();
        if (subs != null) {
            for (Subscriber sub : subs) {
                subscriberNames.add(sub.getName());
            }
            return subscriberNames.size();
        } else {
            return 0;
        }
    }

    /**
     * Returns full list of Subscribers of an API
     *
     * @param identifier APIIdentifier
     * @return Set<Subscriber>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get Subscribers
     */
    public JSONArray getSubscribersOfAPI(JSONObject identifier)
            throws APIManagementException {
        JSONArray subscribersArr;
        String providerName = (String) identifier.get("provider");
        String apiName = (String) identifier.get("name");
        String version = (String) identifier.get("version");
        APIIdentifier apiId = new APIIdentifier(providerName, apiName, version);
        subscribersArr=getJSONfySubscribersSet(getSubscribersOfAPI(apiId));
        return subscribersArr ;
    }

    protected Set<Subscriber> getSubscribersOfAPI(APIIdentifier id)
            throws APIManagementException {
        Set<Subscriber> subscriberSet = null;
        try {
            subscriberSet = apiMgtDAO.getSubscribersOfAPI(id);
        } catch (APIManagementException e) {
            handleException("Failed to get subscribers for API : " + id.getApiName(), e);
        }
        return subscriberSet ;
    }

    private JSONArray getJSONfySubscribersSet(Set<Subscriber> subscribers){
        JSONArray subscribersArr=   new JSONArray();
        Iterator it = subscribers.iterator();
        int i = 0;
        while (it.hasNext()) {
            JSONObject row = new JSONObject();
            Object subscriberObject = it.next();
            Subscriber user = (Subscriber) subscriberObject;
            row.put("userName",  user.getName());
            row.put("subscribedDate", APIUtil.checkValue(Long.valueOf(user.getSubscribedDate().getTime()).toString()));
            subscribersArr.add(i,row);
            i++;
        }
        return subscribersArr;
    }

    /**
     * When enabled publishing to external APIStores support,get all the external apistore details which are
     * published and stored in db and which are not unpublished
     *
     * @param apiId The API Identifier which need to update in db
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          If failed to update subscription status
     */
    @Override
    public Set<APIStore> getExternalAPIStores(APIIdentifier apiId)
            throws APIManagementException {
    	//adding tenantDomain check to provide access to the API details page to the anonymous user
        if (tenantDomain != null && APIUtil.isAPIsPublishToExternalAPIStores(tenantId)) {
            SortedSet<APIStore> sortedApiStores = new TreeSet<APIStore>(new APIStoreNameComparator());
            Set<APIStore> publishedStores = apiMgtDAO.getExternalAPIStoresDetails(apiId);
            sortedApiStores.addAll(publishedStores);
            return APIUtil.getExternalAPIStores(sortedApiStores, tenantId);
        } else {
            return null;
        }
    }

}
