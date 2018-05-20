/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.client;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceAPIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceIdentityException;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

public class SubscriberKeyMgtClient {

    private static Log log = LogFactory.getLog(SubscriberKeyMgtClient.class);

    private APIKeyMgtSubscriberServiceStub subscriberServiceStub;
    private volatile String cookie;

    public SubscriberKeyMgtClient(String backendServerURL, String username, String password)
            throws Exception {
        try {
            subscriberServiceStub = new APIKeyMgtSubscriberServiceStub(
                    null, backendServerURL + "APIKeyMgtSubscriberService");
            ServiceClient client = subscriberServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, subscriberServiceStub._getServiceClient());

        } catch (Exception e) {
            String errorMsg = "Error when instantiating SubscriberKeyMgtClient.";
            log.error(errorMsg, e);
            throw e;
        }
    }

    public OAuthApplicationInfo createOAuthApplicationbyApplicationInfo(OAuthApplicationInfo oauthAppInfo) throws Exception {
        //setCookie(subscriberServiceStub);
        OAuthApplicationInfo oAuthApplicationInfo = subscriberServiceStub.createOAuthApplicationByApplicationInfo(oauthAppInfo);
        //updateCookie(subscriberServiceStub);
        return oAuthApplicationInfo;
    }

    public OAuthApplicationInfo createOAuthApplication(String userId, String applicationName, String callbackUrl, String tokenType) throws Exception {
        //setCookie(subscriberServiceStub);
        OAuthApplicationInfo oAuthApplicationInfo = subscriberServiceStub
                .createOAuthApplication(userId, applicationName, callbackUrl, tokenType);
        //updateCookie(subscriberServiceStub);
        return oAuthApplicationInfo;
    }

    private void updateCookie(APIKeyMgtSubscriberServiceStub subscriberServiceStub) {
        Object cookie = subscriberServiceStub._getServiceClient().getOptions().getProperty(HTTPConstants.COOKIE_STRING);
        if(cookie != null){
            this.cookie = (String) cookie;
        }
    }

    private void setCookie(APIKeyMgtSubscriberServiceStub subscriberServiceStub) {
        if (cookie != null) {
            subscriberServiceStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
    }


    public OAuthApplicationInfo getOAuthApplication(String consumerKey) throws Exception {
        OAuthApplicationInfo oAuthApplicationInfo = subscriberServiceStub.retrieveOAuthApplication(consumerKey);
        return oAuthApplicationInfo;
    }

    public OAuthApplicationInfo updateOAuthApplication(String userId, String applicationName, String callbackUrl,
                                                       String consumerKey, String[] grantTypes)
            throws RemoteException, APIKeyMgtSubscriberServiceAPIManagementException,
                   APIKeyMgtSubscriberServiceAPIKeyMgtException, APIKeyMgtSubscriberServiceIdentityException {
        return subscriberServiceStub.updateOAuthApplication(userId, applicationName, callbackUrl, consumerKey, 
                                                            grantTypes);
    }

    public void deleteOAuthApplication(String consumerKey) throws Exception {
        subscriberServiceStub.deleteOAuthApplication(consumerKey);
    }

    public String regenerateApplicationAccessKey(String keyType, String oldAccessToken, String[] allowedDomains,
                                                 String clientId, String clientSecret, String validityTime)
            throws Exception {
        return subscriberServiceStub.renewAccessToken(keyType, oldAccessToken, allowedDomains, clientId, clientSecret, validityTime);

    }

    public void revokeAccessToken(String token,String consumerKey,String authUser) throws Exception {
       subscriberServiceStub.revokeAccessToken(token,consumerKey,authUser);

    }

}
