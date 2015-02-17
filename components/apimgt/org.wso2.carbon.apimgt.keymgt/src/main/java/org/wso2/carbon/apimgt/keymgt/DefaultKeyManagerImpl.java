/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.OauthAppRequest;
import org.wso2.carbon.apimgt.impl.AbstractKeyManager;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import java.util.Map;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class DefaultKeyManagerImpl extends AbstractKeyManager {

    private static final Log log = LogFactory.getLog(DefaultKeyManagerImpl.class);

    @Override
    public OAuthApplicationInfo createApplication(OauthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();

        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getoAuthApplicationInfo();
        oAuthConsumerAppDTO.setApplicationName((String) oAuthApplicationInfo.getParameter("client_name"));

        if(oAuthApplicationInfo.getParameter("callback_url") != null){
            JSONArray jsonArray = (JSONArray) oAuthApplicationInfo.getParameter("callback_url");
            String callbackUrl = null;

            for (Object callbackUrlObject : jsonArray) {
                callbackUrl = (String) callbackUrlObject;
            }

            oAuthConsumerAppDTO.setCallbackUrl(callbackUrl);
        }

        try {
            oAuthAdminClient.registerOAuthApplicationData(oAuthConsumerAppDTO);

        } catch (Exception e) {
            handleException("OAuth application registration failed", e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName((String) oAuthApplicationInfo.getParameter("client_name"));
        } catch (Exception e) {
            handleException("Can not retrieve registered OAuth application information ", e);
        }

        oAuthApplicationInfo = createOAuthAppFromResponse(oAuthConsumerAppDTO);

        return oAuthApplicationInfo;

    }

    @Override
    public OAuthApplicationInfo updateApplication(OauthAppRequest appInfoDTO) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = getOAuthConsumerAppDTOFromAppInfo(appInfoDTO);
        String oAuthAppName = (String) appInfoDTO.getoAuthApplicationInfo().getParameter(ApplicationConstants.
                OAUTH_CLIENT_NAME);

        try {
            oAuthAdminClient.updateOAuthApplicationData(oAuthConsumerAppDTO);
        } catch (Exception e) {
            handleException("Can not update OAuth application : " + oAuthAppName, e);
        }

        try {
            oAuthConsumerAppDTO = oAuthAdminClient.
                    getOAuthApplicationDataByAppName(oAuthAppName);
        } catch (Exception e) {
            handleException("Can not retrieve updated OAuth application : " + oAuthAppName, e);
        }

        return createOAuthAppFromResponse(oAuthConsumerAppDTO);

    }


    @Override
    public void deleteApplication(String consumerKey) throws APIManagementException {

        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        try {
            oAuthAdminClient.removeOAuthApplicationData(consumerKey);
        } catch (Exception e) {
            handleException("Can not remove OAuth application for the given consumer key : " + consumerKey, e);
        }

    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {
        OAuthAdminClient oAuthAdminClient = APIUtil.getOauthAdminClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        try {
            oAuthConsumerAppDTO = oAuthAdminClient.getOAuthApplicationData(consumerKey);
        } catch (Exception e) {
            handleException("Can not retrieve OAuth application information from given key: " + consumerKey, e);
        }

        return createOAuthAppFromResponse(oAuthConsumerAppDTO);
    }

    @Override
    public Map getTokenMetaData(String accessToken) throws APIManagementException {
        return null;
    }

    @Override
    public String getKeyManagerMetaData() throws APIManagementException {
        return null;
    }

    @Override
    public OAuthApplicationInfo createSemiManualAuthApplication(OauthAppRequest appInfoRequest) throws APIManagementException {
        return null;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
     */
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }


    /**
     * Converts OAuthConsumerAppDTO to a OAuthApplicationInfo object
     *
     * @param oAuthConsumerAppDTO - OAuthApplicationInfo
     * @return OAuthApplicationInfo object
     */
    private OAuthApplicationInfo createOAuthAppFromResponse(OAuthConsumerAppDTO oAuthConsumerAppDTO) {
        OAuthApplicationInfo info = new OAuthApplicationInfo();
        //set client ID.
        Object clientId = oAuthConsumerAppDTO.getOauthConsumerKey();
        info.setClientId((String) clientId);

        Object clientSecret = oAuthConsumerAppDTO.getOauthConsumerSecret();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_SECRET, clientSecret);

        //set client Name.
        Object clientName = oAuthConsumerAppDTO.getApplicationName();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_NAME, clientName);

        Object redirectURI = oAuthConsumerAppDTO.getCallbackUrl();
        info.addParameter(ApplicationConstants.OAUTH_REDIRECT_URIS, redirectURI);

        Object grantType = oAuthConsumerAppDTO.getGrantTypes();
        info.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, grantType);


        return info;
    }


    /**
     * Converts AppInfoDTO object into a OAuthConsumerAppDTO object
     *
     * @param appInfoDTO - AppInfoDTO
     * @return OAuthConsumerAppDTO object
     */
    private OAuthConsumerAppDTO getOAuthConsumerAppDTOFromAppInfo(OauthAppRequest appInfoDTO) {

        OAuthConsumerAppDTO oAuthConsumerAppDTO = new OAuthConsumerAppDTO();
        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getoAuthApplicationInfo();

        oAuthConsumerAppDTO.setApplicationName((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_NAME));
        oAuthConsumerAppDTO.setOauthConsumerKey(oAuthApplicationInfo.getClientId());
        oAuthConsumerAppDTO.setOauthConsumerSecret((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_SECRET));
        oAuthConsumerAppDTO.setCallbackUrl((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_REDIRECT_URIS));
        oAuthConsumerAppDTO.setGrantTypes((String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT));

        return oAuthConsumerAppDTO;
    }


}
