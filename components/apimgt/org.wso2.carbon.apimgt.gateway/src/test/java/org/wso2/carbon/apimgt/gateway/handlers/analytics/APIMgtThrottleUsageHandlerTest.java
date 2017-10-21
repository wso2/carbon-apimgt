/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;

import static org.apache.synapse.rest.RESTConstants.SYNAPSE_REST_API;


public class APIMgtThrottleUsageHandlerTest {
    @Test
    public void mediateWhileEventReceiverIsSkipped() throws Exception {
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito.mock
                (APIManagerAnalyticsConfiguration.class);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        APIMgtThrottleUsageHandler apiMgtThrottleUsageHandler = new APIMgtThrottleUsageHandlerWrapper
                (apiMgtUsageDataPublisher, true, false, apiManagerAnalyticsConfiguration);
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setApiKey("ac-def");
        authContext.setUsername("admin");
        Mockito.when(messageContext.getProperty("__API_AUTH_CONTEXT")).thenReturn(authContext);
        Mockito.when(messageContext.getProperty(SYNAPSE_REST_API)).thenReturn("admin--api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)).thenReturn("admin");
        Mockito.when(messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH)).thenReturn("/api1/aa?vc=1");
        Mockito.when(messageContext.getProperty(APIConstants.THROTTLE_OUT_REASON_KEY)).thenReturn("application level " +
                "throttled out");
        apiMgtThrottleUsageHandler.mediate(messageContext);
    }

}