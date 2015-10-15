/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.utils;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.rmi.RemoteException;

public class EntitlementServiceClient {
    EntitlementServiceStub entitlementServiceStub;
    public EntitlementServiceClient(String[] args) throws Exception {
        //EntitlementClientUtils.loadConfigProperties();
        //String trustStore = EntitlementClientUtils.getTrustStore();
        //System.setProperty("javax.net.ssl.trustStore",  trustStore );
        //System.setProperty("javax.net.ssl.trustStorePassword", EntitlementClientUtils.getTrustStorePassword());
        ConfigurationContext configContext;
        boolean isTenantFlowStarted;

        try {

            String clientRepo = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "deployment" + File.separator + "client";
            String clientAxisConf = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "axis2"+ File.separator +"axis2_client.xml";

            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
            String serviceEndPoint = EntitlementClientUtils.getServerUrl() + "EntitlementService";
            entitlementServiceStub =
                    new EntitlementServiceStub(configContext, serviceEndPoint);
            ServiceClient client = entitlementServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setProperty(HTTPConstants.COOKIE_STRING, null);
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(EntitlementClientUtils.getServerUsername());
            auth.setPassword(EntitlementClientUtils.getServerPassword());
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
        } catch (Exception e) {

        }
    }

    public String validateAction(String subject, String resource, String action, String[] environment){
        String decision = "DENY";
        try {
            decision =entitlementServiceStub.getDecisionByAttributes(subject,resource,action,environment);
            System.out.println("\nXACML Decision is received : " + decision);
            String authCookie = (String) entitlementServiceStub._getServiceClient().getServiceContext()
                    .getProperty(HTTPConstants.COOKIE_STRING);
            System.out.println("\nCookie is received for subsequent communication :  " + authCookie);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (EntitlementServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return decision;
    }
}