package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.RegistryPersistenceImpl;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.apimgt.persistence.utils.RegistrySearchUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;
import static org.wso2.carbon.utils.multitenancy.MultitenantUtils.getTenantAwareUsername;

public class ArtifactData {

    protected Registry registry;
    protected String username;
    protected String tenantDomain;
    //private final String userNameWithoutChange;
    APIPersistence apiPersistenceInstance;



    public GenericArtifact getDevportalApis(String apiId) throws APIPersistenceException, RegistryException, UserStoreException {
        boolean tenantFlowStarted = false;
        //String providername = null;
        GenericArtifact apiArtifact = null;
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);

        try{
            Registry registry;
            String requestedTenantDomain = org.getName();
            if (requestedTenantDomain  != null) {
                int id = getTenantManager().getTenantId(requestedTenantDomain);
                RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
                tenantFlowStarted = true;
                if (APIConstants.WSO2_ANONYMOUS_USER.equals("wso2.anonymous.user")) {
                    registry = getRegistryService().getGovernanceUserRegistry("wso2.anonymous.user", id);
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


            apiArtifact = artifactManager.getGenericArtifact(apiId);
            //providername = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        }catch (RegistryException e){

        }finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return apiArtifact;


    }


    protected TenantManager getTenantManager() {
        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager();
    }
    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    public Registry getRegistry() throws UserStoreException, RegistryException {
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        //boolean tenantFlowStarted = false;
        Registry registry;
        String requestedTenantDomain = org.getName();
        if (requestedTenantDomain  != null) {
            int id = getTenantManager().getTenantId(requestedTenantDomain);
            RegistryPersistenceUtil.startTenantFlow(requestedTenantDomain);
            //tenantFlowStarted = true;
            if (APIConstants.WSO2_ANONYMOUS_USER.equals("wso2.anonymous.user")) {
                registry = getRegistryService().getGovernanceUserRegistry("wso2.anonymous.user", id);
            } else if (this.tenantDomain != null && !this.tenantDomain.equals(requestedTenantDomain)) {
                registry = getRegistryService().getGovernanceSystemRegistry(id);
            } else {
                registry = this.registry;
            }
        } else {
            registry = this.registry;
        }
        return registry;
    }

    public GenericArtifact[] getAllApis() throws APIPersistenceException,APIManagementException, RegistryException, UserStoreException {
        //ArtifactData artifactData = new ArtifactData();

        boolean tenantFlowStarted = false;
        GenericArtifact apiArtifact = null;
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String[] allGenericArtifact=null;
        GenericArtifact[] artifacts = null;

        try {
            Registry registry = getRegistry();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            allGenericArtifact = artifactManager.getAllGenericArtifactIds();
            artifacts = artifactManager.getAllGenericArtifacts();


        } catch (RegistryException e) {

        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return artifacts;
    }
//    public static final String GET_API_VERSIONS = "SELECT API.API_VERSION FROM AM_API API WHERE API.API_PROVIDER = ? AND API.API_NAME = ? ";
//    public Set<String> getAPIVersions(String apiName, String apiProvider) throws APIManagementException {
//        Set<String> versions = new HashSet<String>();
//
//        try (Connection connection = APIMgtDBUtil.getConnection();
//             PreparedStatement statement = connection.prepareStatement(SQLConstants.GET_API_VERSIONS)) {
//            statement.setString(1, APIUtil.replaceEmailDomainBack(apiProvider));
//            statement.setString(2, apiName);
//            ResultSet resultSet = statement.executeQuery();
//            while (resultSet.next()) {
//                versions.add(resultSet.getString("API_VERSION"));
//            }
//        } catch (SQLException e) {
//            handleException("Error while retrieving versions for api " + apiName + " for the provider " + apiProvider,
//                    e);
//        }
//        return versions;
//    }
    public static final String GET_API_IDENTIFIRE_PARAMS = "SELECT API.API_NAME,API.API_PROVIDER,API.API_VERSION,API.API_TYPE FROM AM_API API WHERE API.API_UUID = ? ";

    public List<String> getApiIdentifireParams(String Id){
        List<String> IdentifireParams = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_API_IDENTIFIRE_PARAMS)) {
            statement.setString(1, Id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                IdentifireParams.add(resultSet.getString("API_NAME"));
                IdentifireParams.add(resultSet.getString("API_PROVIDER"));
                IdentifireParams.add(resultSet.getString("API_VERSION"));
                IdentifireParams.add(resultSet.getString("API_TYPE"));

            }
        } catch (SQLException e) {

        }

        return IdentifireParams;

    }


    public int getDevportalAPIS() throws APIManagementException, UserStoreException, RegistryException, APIPersistenceException {

        Organization org = new Organization("carbon.super");

        //final String userNameWithoutChange = "wso2.anonymous.user";
        //String userame = (userNameWithoutChange != null)? userNameWithoutChange: username;

        //String[] roles = APIUtil.getListOfRoles("wso2.anonymous.user");
//
        String[] roles = new String[1];
        roles[0] = "system/wso2.anonymous.role";
        Map<String, Object> properties = null;
//        //UserContext userCtx = new UserContext("",org,properties, roles);
        UserContext userCtx = new UserContext("wso2.anonymous.user", org, properties,  roles);
//
//        String requestedTenantDomain = org.getName();
//        //RegistryPersistenceImpl.RegistryHolder holder = getRegistry(requestedTenantDomain);

        String searchQuery = "";
        String modifiedQuery = RegistrySearchUtil.getDevPortalSearchQuery(searchQuery, userCtx);


        //String searchQuery = "store_view_roles=(null OR system\\/wso2.anonymous.role)&name=*&enableStore=(true OR null)&lcState=(PUBLISHED OR PROTOTYPED)";

        Registry userRegistry = getRegistry();
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
                0, 25, userCtx);

        List<DevPortalAPIInfo> list = searchAPIs.getDevPortalAPIInfoList();

        return searchAPIs.getTotalAPIsCount();
    }

}
