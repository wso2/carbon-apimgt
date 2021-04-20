package org.wso2.carbon.graphql.api.devportal.service;



import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.security.AuthenticationContext;

import java.util.*;


public class PersistenceService {

    APIPersistence apiPersistenceInstance;

    public Map<String, Object> getDevportalAPIS(int start, int offset) throws APIPersistenceException, APIManagementException {
        Map<String, Object> result = new HashMap<>();


        String loggedInUserName= AuthenticationContext.getLoggedInUserName();
        String loggedInTenanDomain = AuthenticationContext.getLoggedInTenanDomain();

        if(loggedInTenanDomain!=null & loggedInTenanDomain!=null){
            Organization org = new Organization(loggedInTenanDomain);

            String[] roles  = APIUtil.getListOfRoles(loggedInUserName);
//            String[] roles = new String[1];
//            roles[0] = "system/wso2.anonymous.role";

            Map<String, Object> properties = APIUtil.getUserProperties(loggedInUserName);
            UserContext userCtx = new UserContext(loggedInUserName, org, properties, roles);
            String searchQuery = "";
            Properties propertiesforPersostence = new Properties();
            propertiesforPersostence.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
            apiPersistenceInstance = PersistenceManager.getPersistenceInstance(propertiesforPersostence); //PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
            DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery, start, offset, userCtx);
            List<DevPortalAPI> list = searchAPIs.getDevPortalAPIList();

            result.put("apis", list);
            result.put("count", list.size());

        }
        return result;
    }

    public DevPortalAPI getApiFromUUID(String uuid) throws APIPersistenceException, APIManagementException {
        DevPortalAPI devPortalApi = null;
        String loggedInTenanDomain = AuthenticationContext.getLoggedInTenanDomain();
        if (loggedInTenanDomain!=null){
            Organization org = new Organization(loggedInTenanDomain);
            Properties properties = new Properties();
            properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
            apiPersistenceInstance = PersistenceManager.getPersistenceInstance(properties);
            devPortalApi = apiPersistenceInstance.getDevPortalAPI(org , uuid);
        }


        return devPortalApi;
    }


}
