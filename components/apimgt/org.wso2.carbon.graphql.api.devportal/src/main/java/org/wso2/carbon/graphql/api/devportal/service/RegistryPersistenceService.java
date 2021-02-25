package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.util.*;

public class RegistryPersistenceService {

    //protected String username;
    APIPersistence apiPersistenceInstance;


    public List<DevPortalAPI> getDevportalAPIS(int start, int offset) throws APIPersistenceException, APIManagementException {

        Organization org = new Organization("carbon.super");

        String[] roles = new String[1];
        roles[0] = "system/wso2.anonymous.role";

        Map<String, Object> properties = APIUtil.getUserProperties("wso2.anonymous.user");

        UserContext userCtx = new UserContext("wso2.anonymous.user", org, properties, roles);

        String searchQuery = "";

        Properties propertiesforPersostence = new Properties();
        propertiesforPersostence.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());

        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(propertiesforPersostence); //PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
                start, offset, userCtx);

        List<DevPortalAPI> list = searchAPIs.getDevPortalAPIList();

        return list;
    }

    public DevPortalAPI getApiFromUUID(String Id) throws APIPersistenceException {
        Organization org = new Organization("carbon.super");
        Properties properties = new Properties();
        properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(properties);

        DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org , Id);

        return devPortalApi;
    }

    public int apiCount(int start,int offset) throws APIPersistenceException, APIManagementException {
        List<DevPortalAPI> list = getDevportalAPIS(start,offset);
        return list.size();
    }

}
