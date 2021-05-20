/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtBadRequestException;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

public class OrganizationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(OrganizationInterceptor.class);

    public OrganizationInterceptor() {
        // We will use PRE_INVOKE phase as we need to process message before hit actual
        // service
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        try {
            OrganizationResolver resolver = APIUtil.getOrganizationResolver();
            
            // populate properties needed for the resolver.
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(APIConstants.PROPERTY_HEADERS_KEY, message.get(Message.PROTOCOL_HEADERS));
            properties.put(APIConstants.PROPERTY_QUERY_KEY, message.get(Message.QUERY_STRING));
            
            String organization = resolver.resolve(properties);
            message.put(RestApiConstants.ORGANIZATION, organization);
        } catch (APIManagementException e) {
            if (e instanceof APIMgtBadRequestException) {
                RestApiUtil.handleBadRequest(e.getMessage(), 901300L, logger);
            } else {
                RestApiUtil.handleInternalServerError("Error while resolving the organization resolver", e, logger);
            }
        }
        logger.debug("Organization :" + message.get(RestApiConstants.ORGANIZATION));
    }

}
