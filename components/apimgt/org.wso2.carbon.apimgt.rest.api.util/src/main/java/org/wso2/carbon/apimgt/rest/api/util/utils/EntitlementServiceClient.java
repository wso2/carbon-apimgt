/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.rmi.RemoteException;

/**
 * This class will be used to initiate connection with PDP and get decision based on request
 */
public class EntitlementServiceClient {
    EntitlementServiceStub entitlementServiceStub;
    private static final Log logger = LogFactory.getLog(EntitlementServiceClient.class);

    /**
     * This method will initiate entitlement service client which calls PDP
     *
     * @throws Exception whenever if failed to initiate client properly.
     */
    public EntitlementServiceClient() throws Exception {
        ConfigurationContext configContext;
        try {
            String repositoryBasePath = CarbonUtils.getCarbonHome() + File.separator + "repository";
            String clientRepo = repositoryBasePath +
                    File.separator + "deployment" + File.separator + "client";
            String clientAxisConf = repositoryBasePath +
                    File.separator + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";

            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientRepo, clientAxisConf);
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
            option.setProperty(HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
        } catch (Exception e) {
            logger.error("Error while initiating entitlement service client ", e);
        }
    }

    /**
     * @param subject     subject to be check with PDP
     * @param resource    resource name to be checked with PDP
     * @param action      action to be check with PDP
     * @param environment environment to be check with PDP
     * @return Allow if resource can be accessible
     *         Deny if resource forbidden
     *         Not Applicable if cannot find matched policy
     */
    public String validateAction(String subject, String resource, String action, String[] environment) {
        String decision = "DENY";
        try {
            decision = entitlementServiceStub.getDecisionByAttributes(subject, resource, action, environment);
            System.out.println("\nXACML Decision is received : " + decision);
            String authCookie = (String) entitlementServiceStub._getServiceClient().getServiceContext()
                    .getProperty(HTTPConstants.COOKIE_STRING);
            System.out.println("\nCookie is received for subsequent communication :  " + authCookie);
        } catch (RemoteException e) {
            logger.error("Error while connecting PDP ", e);
        } catch (EntitlementServiceException e) {
            logger.error("Error while validating XACML policy for given request ", e);
        }
        return decision;
    }
}