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
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.xsd.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.subscriber.APIKeyMgtSubscriberServiceStub;
import org.wso2.carbon.apimgt.keymgt.stub.types.carbon.ApplicationKeysDTO;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.URL;

public class SubscriberKeyMgtClient {

    private static Log log = LogFactory.getLog(SubscriberKeyMgtClient.class);

    private APIKeyMgtSubscriberServiceStub subscriberServiceStub;

    public SubscriberKeyMgtClient(String backendServerURL, String username, String password)
            throws Exception {
        try {
            subscriberServiceStub = new APIKeyMgtSubscriberServiceStub(
                    null, backendServerURL + "APIKeyMgtSubscriberService");
            ServiceClient client = subscriberServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                                                      true, subscriberServiceStub._getServiceClient());

        } catch (Exception e) {
            String errorMsg = "Error when instantiating SubscriberKeyMgtClient.";
            log.error(errorMsg, e);
            throw e;
        }
    }


    public OAuthApplicationInfo createOAuthApplication(String userId, String applicationName, String callbackUrl) throws Exception {
        OAuthApplicationInfo oAuthApplicationInfo = subscriberServiceStub.createOAuthApplication(userId, applicationName, callbackUrl);
        return oAuthApplicationInfo;
    }


    public OAuthApplicationInfo getOAuthApplication(String consumerKey) throws Exception {
        OAuthApplicationInfo oAuthApplicationInfo = subscriberServiceStub.retrieveOAuthApplication(consumerKey);
        return oAuthApplicationInfo;
    }

    public void deleteOAuthApplication(String consumerKey) throws Exception {
        subscriberServiceStub.deleteOAuthApplication(consumerKey);
    }

    public String getAccessKey(String userId, APIInfoDTO apiInfoDTO,
                               String applicationName, String keyType, String callbackUrl) throws Exception {
        return subscriberServiceStub.getAccessToken(userId, apiInfoDTO, applicationName, keyType, callbackUrl);
    }

    public ApplicationKeysDTO getApplicationAccessKey(String userId, String applicationName,
                                                      String keyType, String callbackUrl, String[] allowedDomains,String validityTime, String tokenScope) throws Exception {
        ApplicationKeysDTO keys = subscriberServiceStub.getApplicationAccessToken(userId, applicationName, keyType, callbackUrl, allowedDomains, validityTime, tokenScope);
        return keys;
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
