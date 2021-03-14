package org.wso2.carbon.graphql.api.devportal.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.PersistenceManager;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl;
import org.wso2.carbon.graphql.api.devportal.security.JwtUser;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

public class PersistenceService {

    //protected String username;
    APIPersistence apiPersistenceInstance;

    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    public AccessTokenInfo getAccessTokenData(String token) throws APIManagementException {
        WebAppAuthenticatorImpl webAppAuthenticator = new WebAppAuthenticatorImpl();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo = webAppAuthenticator.getTokenMetaData(token);

        return accessTokenInfo;
    }


    private String secret = "Graphql";

    public JwtUser validate(String token) {

        JwtUser jwtUser = null;
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            jwtUser = new JwtUser();

            jwtUser.setUserName(body.getSubject());
            jwtUser.setPassword((String) body.get("userId"));
            jwtUser.setRole((String) body.get("role"));
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return jwtUser;
    }
    public Map<String, Object> getDevportalAPIS(int start, int offset, String token,String oauth) throws APIPersistenceException, APIManagementException {


        Map<String, Object> result = new HashMap<>();


        AccessTokenInfo accessTokenInfo = getAccessTokenData(oauth);


        String tenantDomain = MultitenantUtils.getTenantDomain(accessTokenInfo.getEndUserName());

        String username = accessTokenInfo.getEndUserName();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            //when the username is an email in supertenant, it has at least 2 occurrences of '@'
            long count = username.chars().filter(ch -> ch == '@').count();
            //in the case of email, there will be more than one '@'
            boolean isEmailUsernameEnabled = Boolean.parseBoolean(CarbonUtils.getServerConfiguration().
                    getFirstProperty("EnableEmailUserName"));
            if (isEmailUsernameEnabled || (username.endsWith(SUPER_TENANT_SUFFIX) && count <= 1)) {
                username = MultitenantUtils.getTenantAwareUsername(username);
            }
        }
        Organization org = new Organization("carbon.super");



        String x = username;

        JwtUser jwtUser = validate(token);

        String userName = jwtUser.getUserName();
        //String role = jwtUser.getRole();

        String[] roles = new String[1];//APIUtil.getListOfRoles(userName);
        roles[0] = "system/wso2.anonymous.role";
        //roles[0] = role;
        Map<String, Object> properties = APIUtil.getUserProperties("wso2.anonymous.user");

        UserContext userCtx = new UserContext(userName, org, properties, roles);

        String searchQuery = "";

        Properties propertiesforPersostence = new Properties();
        propertiesforPersostence.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());

        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(propertiesforPersostence); //PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
        DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
                start, offset, userCtx);

        List<DevPortalAPI> list = searchAPIs.getDevPortalAPIList();

        result.put("apis", list);
        result.put("count", list.size());

        return result;
        //return list;
    }

//    public List<DevPortalAPI> getDevportalAPIS(int start, int offset) throws APIPersistenceException, APIManagementException {
//
//        Organization org = new Organization("carbon.super");
//
//        String[] roles = new String[1];
//        roles[0] = "system/wso2.anonymous.role";
//
//        Map<String, Object> properties = APIUtil.getUserProperties("wso2.anonymous.user");
//
//        UserContext userCtx = new UserContext("wso2.anonymous.user", org, properties, roles);
//
//        String searchQuery = "";
//
//        Properties propertiesforPersostence = new Properties();
//        propertiesforPersostence.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
//
//        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(propertiesforPersostence); //PersistenceManager.getPersistenceInstance("wso2.anonymous.user");
//        DevPortalAPISearchResult searchAPIs = apiPersistenceInstance.searchAPIsForDevPortal(org, searchQuery,
//                start, offset, userCtx);
//
//        List<DevPortalAPI> list = searchAPIs.getDevPortalAPIList();
//
//        return list;
//    }

    public DevPortalAPI getApiFromUUID(String uuid) throws APIPersistenceException {
        Organization org = new Organization("carbon.super");
        Properties properties = new Properties();
        properties.put(APIConstants.ALLOW_MULTIPLE_STATUS, APIUtil.isAllowDisplayAPIsWithMultipleStatus());
        apiPersistenceInstance = PersistenceManager.getPersistenceInstance(properties);

        DevPortalAPI devPortalApi = apiPersistenceInstance.getDevPortalAPI(org , uuid);

        return devPortalApi;
    }

//    public int apiCount(int start,int offset) throws APIPersistenceException, APIManagementException {
//        List<DevPortalAPI> list = getDevportalAPIS(start,offset);
//        return list.size();
//    }

}
