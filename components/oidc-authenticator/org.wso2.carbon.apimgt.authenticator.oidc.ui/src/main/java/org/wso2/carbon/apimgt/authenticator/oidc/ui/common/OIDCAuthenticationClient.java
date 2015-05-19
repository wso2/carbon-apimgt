/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.authenticator.oidc.ui.common;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.apimgt.authenticator.oidc.stub.OIDCAuthenticationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

public class OIDCAuthenticationClient {

    private OIDCAuthenticationServiceStub stub;
    private static final Log log = LogFactory.getLog(OIDCAuthenticationClient.class);
    private HttpSession session;

    public OIDCAuthenticationClient(ConfigurationContext ctx, String serverURL, String cookie,
                                        HttpSession session) throws Exception {
        this.session = session;
        String serviceEPR = serverURL + "OIDCAuthenticationService";
        stub = new OIDCAuthenticationServiceStub(ctx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        if (cookie != null) {
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
    }

    public String login(String code, String nonce) throws AuthenticationException {

        try {
            String  username = stub.login(code, nonce);
            if(username == null || username.equals("")){
                setAdminCookie(false);
            }else{
                setAdminCookie(true);
            }

            return username;
        } catch (RemoteException e) {
            log.error("Error when sign-in for the user " , e);
            throw new AuthenticationException("Error when sign-in for the user " , e);
        }
    }

    public void logout(HttpSession session) throws AuthenticationException {
        try {
            if(!CarbonUtils.isRunningOnLocalTransportMode()){
                stub.logout();
            }
            session.removeAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
            session.invalidate();
        } catch (java.lang.Exception e) {
            String msg = "Error occurred while logging out";
            log.error(msg, e);
            throw new AuthenticationException(msg, e);
        }
    }

    private void setAdminCookie(boolean result) {
        if (result) {
            String cookie = (String) stub._getServiceClient().getServiceContext().getProperty(
                    HTTPConstants.COOKIE_STRING);
            session.setAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN, cookie);
        }
    }

}
