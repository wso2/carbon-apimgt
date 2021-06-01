/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.throttling.publisher;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.util.ArrayList;
import java.util.TreeMap;


public class DataProcessAndPublishingAgentTest {
    String applicationLevelThrottleKey = "Gold";
    String applicationLevelTier = "Gold";
    String apiLevelThrottleKey = "Gold";
    String apiLevelTier = "Gold";
    String subscriptionLevelThrottleKey = "/abc/1.0.0:abcde:fgh";
    String subscriptionLevelTier = "Gold";
    String resourceLevelThrottleKey = "/abc/1.0.0/*:GET";
    String authorizedUser = "admin@carbon.super";
    String resourceLevelTier = "Gold";
    String apiContext = "/api1";
    String apiVersion = "1.0.0";
    String appTenant = "carbon.super";
    String apiTenant = "carbon.super";
    String apiName = "API1";
    String appId = "1";

    @Test
    public void setDataReference() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataPublisher dataPublisher = Mockito.mock(DataPublisher.class);
        Mockito.when(dataPublisher.tryPublish(Mockito.any(Event.class))).thenReturn(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(new TreeMap<>());
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, apiLevelTier, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }

    @Test
    public void setDataReferenceWithoutApiLevelTier() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
    }

    @Test
    public void setDataReferenceWithHeaderConditionEnableWithNullHeaderMap() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn
                (null);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }

    @Test
    public void setDataReferenceWithHeaderConditionEnable() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(new TreeMap<>());
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }

    @Test
    public void setIPCondition() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIMgtGatewayConstants.X_FORWARDED_FOR, "192.168.1.1");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }

    @Test
    public void testEnableQueryParamCondition() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(NhttpConstants.REST_URL_POSTFIX)).thenReturn("?a=1&b=2");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIMgtGatewayConstants.X_FORWARDED_FOR, "192.168.1.1");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }


    @Test
    public void testEnableQueryParamConditionWithoutQueryParams() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        throttleProperties.setEnableJwtConditions(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(axis2MsgCntxt.getProperty(NhttpConstants.REST_URL_POSTFIX)).thenReturn("");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIMgtGatewayConstants.X_FORWARDED_FOR, "192.168.1.1");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }
    @Test
    public void testEnableQueryParamConditionWithJwtToken() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableJwtConditions(true);
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken
                ("eyJ4NXQiOiJObUptT0dVeE16WmxZak0yWkRSaE5UWmxZVEExWXpkaFpUUmlPV0UwTldJMk0ySm1PVGMxWkEiLCJraWQiOiJkMGVjN" +
                        "TE0YTMyYjZmODhjMGFiZDEyYTI4NDA2OTliZGQzZGViYTlkIiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoicVI2QjRu" +
                        "YlhEVS13ajNEeFFJVEc0ZyIsInN1YiI6ImFkbWluIiwiYXVkIjpbIlRLZFZTVk5uWVhUeXFQOGhrS0xvZmJmRXExd2EiXS" +
                        "wiYXpwIjoiVEtkVlNWTm5ZWFR5cVA4aGtLTG9mYmZFcTF3YSIsImF1dGhfdGltZSI6MTUwNjA2MzE1OCwiaXNzIjoiaHR0c" +
                        "HM6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2VuIiwiZXhwIjoxNTA2MDY2NzU4LCJpYXQiOjE1MDYwNjMxNTh9" +
                        ".bQ4smuaTczBhDhd68eh1DDJ2aXgsvjiesWzJd3aYo31_1-yAQg6a21ARi3hCqbozgQdkvobv7tuK7EJ5LfkDMgrhGJP9w" +
                        "SPSqIbOpPIJKfjXq--j6fh51gxUaaWz-I7EXYQtVm3ygJsF__a6O-NSrLK07Pqw79f9TgFYZoni1iQ");
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
        dataProcessAndPublishingAgent.clearDataReference();
    }
    @Test
    public void testContentAwareTierPresent() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setIsContentAware(true);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIThrottleConstants.CONTENT_LENGTH,123);
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }
    @Test
    public void testContentAwareTierPresentAndContentLengthNotPresent() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setIsContentAware(true);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope env = fac.createSOAPEnvelope();
        fac.createSOAPBody(env);
        env.getBody().addChild(fac.createOMElement("test", "http://t", "t"));
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(messageContext.getEnvelope()).thenReturn(env);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }
    @Test
    public void testIgnoreClientPortFromXForwardedForHeader() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIMgtGatewayConstants.X_FORWARDED_FOR, "192.168.1.1:80");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }
    @Test
    public void testXForwardedForHeaderIPV6() throws Exception {
        ThrottleProperties throttleProperties = new ThrottleProperties();
        DataProcessAndPublishingAgent dataProcessAndPublishingAgent = new DataProcessAndPublishingAgentWrapper
                (throttleProperties);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext
                .class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).thenReturn("admin--PizzaShackAPI");
        TreeMap headers = new TreeMap();
        headers.put(APIMgtGatewayConstants.X_FORWARDED_FOR, "0:0:0:0:0:0:0:1");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setContentAware(false);
        ArrayList<VerbInfoDTO> list = new ArrayList<VerbInfoDTO>();
        list.add(verbInfoDTO);
        Mockito.when(messageContext.getProperty(APIConstants.VERB_INFO_DTO)).thenReturn(list);
        dataProcessAndPublishingAgent.setDataReference(applicationLevelThrottleKey, applicationLevelTier,
                apiLevelThrottleKey, null, subscriptionLevelThrottleKey, subscriptionLevelTier,
                resourceLevelThrottleKey, resourceLevelTier, authorizedUser, apiContext, apiVersion, appTenant,
                apiTenant, appId, messageContext, authenticationContext);
        dataProcessAndPublishingAgent.run();
    }

}