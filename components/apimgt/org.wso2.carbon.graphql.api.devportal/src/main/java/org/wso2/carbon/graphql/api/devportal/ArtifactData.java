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
import org.wso2.carbon.apimgt.persistence.dto.*;
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

import java.util.*;

public class ArtifactData {

    protected Registry registry;
    protected String username;
    protected String tenantDomain;
    //private final String userNameWithoutChange;
    APIPersistence apiPersistenceInstance;




    public List<DevPortalAPI> getDevportalAPIS() throws APIManagementException, UserStoreException, RegistryException, APIPersistenceException {

        Organization org = new Organization("carbon.super");

        //final String userNameWithoutChange = "wso2.anonymous.user";
        //String userame = (userNameWithoutChange != null)? userNameWithoutChange: username;

        //String[] roles = APIUtil.getListOfRoles("wso2.anonymous.user");
        String[] roles = new String[1];
        roles[0] = "system/wso2.anonymous.role";
        Map<String, Object> properties = null;

        UserContext userCtx = new UserContext("wso2.anonymous.user", org, properties,  roles);

        String searchQuery = "";
        String modifiedQuery = RegistrySearchUtil.getDevPortalSearchQuery(searchQuery, userCtx);


        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
                0, 25, userCtx);

        List<DevPortalAPI> list = searchAPIs.getDevPortalAPIList();


        return list;
    }

    public DevPortalAPI getApiFromUUID(String Id) throws APIPersistenceException {
        Organization org = new Organization("carbon.super");
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance("wso2.anonymous.user");

        DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org , Id);

        return devPortalApi;
    }


}
