package org.wso2.carbon.graphql.api.devportal;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.handleException;
import static org.wso2.carbon.utils.multitenancy.MultitenantUtils.getTenantAwareUsername;

public class ArtifactData {

    protected Registry registry;
    protected String username;
    protected String tenantDomain;



    public GenericArtifact getDevportalApis(String apiId) throws GovernanceException {
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
        }catch (RegistryException | org.wso2.carbon.user.api.UserStoreException | APIManagementException e){

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

    public String[] getTotalApisCount() throws APIManagementException, RegistryException, UserStoreException {
        //ArtifactData artifactData = new ArtifactData();

        boolean tenantFlowStarted = false;
        GenericArtifact apiArtifact = null;
        String TenantDomain = RestApiUtil.getRequestedTenantDomain(null);
        Organization org = new Organization(TenantDomain);
        String[] allGenericArtifact=null;

        try {
            Registry registry = getRegistry();
            GenericArtifactManager artifactManager = RegistryPersistenceUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);

            allGenericArtifact = artifactManager.getAllGenericArtifactIds();


        } catch (RegistryException | org.wso2.carbon.user.api.UserStoreException | APIManagementException e) {

        } finally {
            if (tenantFlowStarted) {
                RegistryPersistenceUtil.endTenantFlow();
            }
        }
        return allGenericArtifact;
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
    public static final String GET_API_IDENTIFIRE_PARAMS = "SELECT API.API_NAME,API.API_PROVIDER,API.API_VERSION FROM AM_API API WHERE API.API_UUID = ? ";

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
            }
        } catch (SQLException e) {

        }

        return IdentifireParams;

    }

}
