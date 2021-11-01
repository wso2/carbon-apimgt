/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Unit test cases related GraphQLQueryAnalysisHandler.
 */
public class GraphQLQueryAnalysisHandlerTest {

    /**
     * This method will test request flow when "isGraphqlSubscriptionRequest" property is set in axis2 message context
     * when incoming transport is websocket. This occurs during Graphql Subscription request flow.
     */
    @Test
    public void testHandleRequestForGraphQLSubscriptions() {
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("ws");
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(true);
        GraphQLQueryAnalysisHandler graphQLQueryAnalysisHandler = new GraphQLQueryAnalysisHandler();
        Assert.assertTrue(graphQLQueryAnalysisHandler.handleRequest(messageContext));

        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("wss");
        Assert.assertTrue(graphQLQueryAnalysisHandler.handleRequest(messageContext));
    }
}
