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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.balana.XACMLConstants;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.EntitlementServiceClient;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import org.wso2.carbon.identity.entitlement.proxy.PEPProxy;


public class XACMLAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(XACMLAuthenticationInterceptor.class);
    public XACMLAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }
                
        handleRequest(inMessage, null);
    }


    /**
     * isUserPermitted requests received at the ml endpoint, using HTTP basic-auth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed.
     */
    public boolean handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Authenticating request: " + message.getId()));
        }
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy == null) {
            logger.error("Authentication failed: Basic authentication header is missing");
            return false;
        }
        Object certObject = null;
        String username = StringUtils.trim(policy.getUserName());
        if (StringUtils.isEmpty(username)) {
            logger.error("Username cannot be null/empty.");
            return false;
        }
        return isUserPermitted(username, (String) message.get(Message.REQUEST_URI),
                (String) message.get(Message.HTTP_REQUEST_METHOD), null);
    }


    private boolean isUserPermitted(String userName, String resource, String httpMethod, String[] arr) {
        try {
            String status;
            EntitlementServiceClient client = new EntitlementServiceClient();
            status = client.validateAction(userName, resource, httpMethod, arr);
            //TODO this permit need to be replaced with XACML constant for permitted.
            if(status.equalsIgnoreCase("Permit")){
                return true;
            }
        } catch (Exception e) {
            logger.error("Error while validating XACML request" + e);
        }
        return false;
    }
}