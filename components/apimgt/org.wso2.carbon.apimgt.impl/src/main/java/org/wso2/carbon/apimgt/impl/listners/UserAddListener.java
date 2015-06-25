/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.listners;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManagerListener;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;

/*
*This class is use to listen user adding event. When new user added to system we can trigger
* new workflow using this class
*/
public class UserAddListener extends AbstractUserStoreManagerListener {
    private static final Log log = LogFactory.getLog(UserAddListener.class);

    public int getExecutionOrderId() {
        return AuthorizationManagerListener.MULTITENANCY_USER_RESTRICTION_HANDLER;
    }

    @Override
    public boolean addUser(String userName, Object credential, String[] roleList,
                           Map<String, String> claims, String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String processServerURL = config.getFirstProperty("WorkFlowExtensions.SelfSignIn.ServerURL");
        String processServerUsername = config.getFirstProperty("WorkFlowExtensions.SelfSignIn.UserName");
        String processServerPassword = config.getFirstProperty("WorkFlowExtensions.SelfSignIn.Password");
        try {
            ServiceClient client = new ServiceClient(ServiceReferenceHolder.getInstance()
                    .getContextService().getClientConfigContext(), null);
            Options options = new Options();
            options.setAction("http://wso2.org/bps/sample/process");
            options.setTo(new EndpointReference(processServerURL + "UserCreationProcess"));
            String configs = CarbonUtils.getCarbonConfigDirPath() + "/apimanager/bpel-policy.xml";
            //options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy(configs));
            // String cookie = loginBPS();
            // options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            client.setOptions(options);
            //client.engageModule("addressing");
            //client.engageModule("rampart");
            String temp = client.sendReceive(getPayload(userName)).toString();
        } catch (Exception e) {
            String message = "Error while invoking external business process";
            log.error(message + e.toString());
        }
        return true;
    }

    private static Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    private static OMElement getPayload(String userName) throws XMLStreamException, javax.xml.stream.XMLStreamException {
        String payload = "   <p:UserCreationProcessRequest xmlns:p=\"http://wso2.org/bps/sample\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <p:input>" + userName + "</p:input>\n" +
                "   </p:UserCreationProcessRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }


    private String loginBPS() throws AxisFault {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String user = "admin";
        String password = "admin";
        String url = "https://localhost:9444/services/";

        if (url == null || user == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("API gateway URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(ServiceReferenceHolder.getInstance()
                .getContextService().getClientConfigContext(), url + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return sessionCookie;
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }
}