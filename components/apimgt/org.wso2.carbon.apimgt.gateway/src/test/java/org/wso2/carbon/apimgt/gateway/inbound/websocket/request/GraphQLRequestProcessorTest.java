/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.t
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound.websocket.request;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketUtils;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for GraphQLRequestProcessor.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({InboundWebsocketProcessorUtil.class, ServiceReferenceHolder.class, WebSocketUtils.class,
        WebsocketUtil.class, APIUtil.class})
public class GraphQLRequestProcessorTest {

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.getOAuthConfigurationFromAPIMConfig(Mockito.anyString())).thenReturn("");

        PowerMockito.mockStatic(WebsocketUtil.class);
    }

    @Test
    public void testHandleRequestSuccess() throws Exception {
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n    }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);

        PowerMockito.when(InboundWebsocketProcessorUtil
                .validateScopes(inboundMessageContext, "liftStatusChange", "1")).thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        PowerMockito.when(InboundWebsocketProcessorUtil.findMatchingVerb("liftStatusChange", inboundMessageContext))
                .thenReturn(verbInfoDTO);
        APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
        infoDTO.setGraphQLMaxComplexity(4);
        infoDTO.setGraphQLMaxDepth(3);
        inboundMessageContext.setInfoDTO(infoDTO);

        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertEquals(inboundMessageContext.getVerbInfoForGraphQLMsgId("1").getOperation(), "liftStatusChange");
        Assert.assertEquals(inboundMessageContext.getVerbInfoForGraphQLMsgId("1").getVerbInfoDTO().getHttpVerb(),
                "SUBSCRIPTION");
        Assert.assertEquals(inboundMessageContext.getVerbInfoForGraphQLMsgId("1").getVerbInfoDTO().getThrottling(),
                "Unlimited");
    }

    @Test
    public void testHandleRequestNonSubscribeMessage() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"connection_init\",\"payload\":{}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    @Test
    public void testHandleRequestAuthError() throws APISecurityException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"type\":\"connection_init\",\"payload\":{}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        responseDTO.setError(true);
        responseDTO.setErrorMessage("Invalid authentication");
        responseDTO.setCloseConnection(true);
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        setChannelAttributeMap(inboundMessageContext);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), "Invalid authentication");
        Assert.assertTrue(responseDTO.isCloseConnection());
    }

    @Test
    public void testHandleRequestInvalidPayload() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        GraphQLProcessorResponseDTO inboundProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        inboundProcessorResponseDTO.setErrorMessage("Invalid operation payload");
        inboundProcessorResponseDTO.setId("1");
        PowerMockito.when(InboundWebsocketProcessorUtil
                        .getBadRequestGraphQLFrameErrorDTO("Invalid operation payload", "1"))
                .thenReturn(inboundProcessorResponseDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        setChannelAttributeMap(inboundMessageContext);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), "Invalid operation payload");
        Assert.assertEquals(processorResponseDTO.getErrorCode(), WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertTrue(errorJson.containsKey(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE));
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertTrue(errorJson.containsKey(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID));
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        Assert.assertTrue(errorJson.containsKey(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD));
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertTrue(payload.containsKey(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE));
        Assert.assertTrue(payload.containsKey(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE));
        Assert.assertEquals(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE),
                "Invalid operation payload");
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST));

        msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"mutation {\\n  "
                + "changeLiftStatusChange {\\n    id\\n    name\\n    }\\n}\\n\"}}";
        inboundProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        inboundProcessorResponseDTO.setErrorMessage("Invalid operation. Only allowed Subscription type operations");
        inboundProcessorResponseDTO.setId("1");
        PowerMockito.when(InboundWebsocketProcessorUtil.getBadRequestGraphQLFrameErrorDTO(
                        "Invalid operation. Only allowed Subscription type operations", "1"))
                .thenReturn(inboundProcessorResponseDTO);
        processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(),
                "Invalid operation. Only allowed Subscription type operations");
        Assert.assertEquals(processorResponseDTO.getErrorCode(), WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertEquals(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE),
                "Invalid operation. Only allowed Subscription type operations");
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST));
    }

    @Test
    public void testHandleRequestInvalidQueryPayload() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n invalidField\\n }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);
        setChannelAttributeMap(inboundMessageContext);
        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertTrue(processorResponseDTO.getErrorMessage()
                .contains(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY_MESSAGE));
        Assert.assertEquals(processorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertTrue(((String) payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE))
                .contains(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY_MESSAGE));
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_INVALID_QUERY));
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    @Test
    public void testHandleRequestInvalidScope() throws Exception  {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);

        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        verbInfoDTO.setAuthType("Any");
        PowerMockito.when(InboundWebsocketProcessorUtil.findMatchingVerb("liftStatusChange",
                inboundMessageContext)).thenReturn(verbInfoDTO);

        GraphQLProcessorResponseDTO graphQLProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        graphQLProcessorResponseDTO.setError(true);
        graphQLProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR);
        graphQLProcessorResponseDTO.setErrorMessage("User is NOT authorized to access the Resource");
        graphQLProcessorResponseDTO.setCloseConnection(false);
        graphQLProcessorResponseDTO.setId("1");
        PowerMockito.when(InboundWebsocketProcessorUtil.validateScopes(inboundMessageContext, "liftStatusChange", "1"))
                .thenReturn(graphQLProcessorResponseDTO);

        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertEquals(processorResponseDTO.getErrorMessage(), "User is NOT authorized to access the Resource");
        Assert.assertEquals(processorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertEquals(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE),
                "User is NOT authorized to access the Resource");
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR));
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    private void setChannelAttributeMap(InboundMessageContext inboundMessageContext) {
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        inboundMessageContext.setCtx(ctx);
        Channel channel = Mockito.mock(Channel.class);
        Mockito.when(ctx.channel()).thenReturn(channel);
        PowerMockito.mockStatic(WebSocketUtils.class);
        Mockito.when(channel.attr(WebSocketUtils.WSO2_PROPERTIES)).thenReturn(getChannelAttributeMap());
        PowerMockito.when(WebSocketUtils.getApiProperties(ctx)).thenReturn(new HashMap<>());
    }

    @Test
    public void testHandleRequestScopeValidationSkipWhenSecurityDisabled() throws Exception  {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);

        // VerbInfoDTO with security disabled
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        verbInfoDTO.setAuthType("None");
        PowerMockito.when(InboundWebsocketProcessorUtil.findMatchingVerb("liftStatusChange",
                inboundMessageContext)).thenReturn(verbInfoDTO);

        // Creating response for scope validation
        GraphQLProcessorResponseDTO graphQLProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        graphQLProcessorResponseDTO.setError(true);
        graphQLProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR);
        graphQLProcessorResponseDTO.setErrorMessage("User is NOT authorized to access the Resource");
        graphQLProcessorResponseDTO.setCloseConnection(false);
        graphQLProcessorResponseDTO.setId("1");

        PowerMockito.when(InboundWebsocketProcessorUtil.validateScopes(inboundMessageContext,
                "liftStatusChange", "1")).thenReturn(graphQLProcessorResponseDTO);
        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(responseDTO);

        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertFalse(processorResponseDTO.isError());
        Assert.assertNull(processorResponseDTO.getErrorMessage());
        Assert.assertNotEquals(processorResponseDTO.getErrorMessage(),
                "User is NOT authorized to access the Resource");
    }

    @Test
    public void testHandleRequestTooDeep() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n    }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);

        PowerMockito.when(InboundWebsocketProcessorUtil
                .validateScopes(inboundMessageContext, "liftStatusChange", "1")).thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        PowerMockito.when(InboundWebsocketProcessorUtil.findMatchingVerb("liftStatusChange", inboundMessageContext))
                .thenReturn(verbInfoDTO);
        APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
        infoDTO.setGraphQLMaxComplexity(4);
        infoDTO.setGraphQLMaxDepth(1);
        inboundMessageContext.setInfoDTO(infoDTO);

        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertTrue(processorResponseDTO.getErrorMessage().contains(
                WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE));
        Assert.assertEquals(processorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertTrue(((String) payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE))
                .contains(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP_MESSAGE));
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.GRAPHQL_QUERY_TOO_DEEP));
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    @Test
    public void testHandleRequestThrottle() throws Exception {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        String msgText = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},"
                + "\"operationName\":null,\"query\":\"subscription {\\n  "
                + "liftStatusChange {\\n    id\\n    name\\n    }\\n}\\n\"}}";
        PowerMockito.mockStatic(InboundWebsocketProcessorUtil.class);
        InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
        PowerMockito.when(InboundWebsocketProcessorUtil.authenticateToken(inboundMessageContext))
                .thenReturn(responseDTO);

        // Get schema and parse
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        inboundMessageContext.setGraphQLSchemaDTO(schemaDTO);

        PowerMockito.when(InboundWebsocketProcessorUtil
                .validateScopes(inboundMessageContext, "liftStatusChange", "1")).thenReturn(responseDTO);
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb("SUBSCRIPTION");
        verbInfoDTO.setThrottling("Unlimited");
        PowerMockito.when(InboundWebsocketProcessorUtil.findMatchingVerb("liftStatusChange", inboundMessageContext))
                .thenReturn(verbInfoDTO);
        APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();
        infoDTO.setGraphQLMaxComplexity(4);
        infoDTO.setGraphQLMaxDepth(3);
        inboundMessageContext.setInfoDTO(infoDTO);

        GraphQLProcessorResponseDTO throttleResponseDTO = new GraphQLProcessorResponseDTO();
        throttleResponseDTO.setError(true);
        throttleResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
        throttleResponseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        throttleResponseDTO.setId("1");

        PowerMockito.when(InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO,
                inboundMessageContext, "1")).thenReturn(throttleResponseDTO);
        GraphQLRequestProcessor graphQLRequestProcessor = new GraphQLRequestProcessor();
        setChannelAttributeMap(inboundMessageContext);
        PowerMockito.when(WebsocketUtil.validateDenyPolicies(Mockito.anyObject())).thenReturn(responseDTO);
        InboundProcessorResponseDTO processorResponseDTO =
                graphQLRequestProcessor.handleRequest(msgSize, msgText, inboundMessageContext);
        Assert.assertTrue(processorResponseDTO.isError());
        Assert.assertTrue(processorResponseDTO.getErrorMessage().contains(
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE));
        Assert.assertEquals(processorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
        Assert.assertNotNull(processorResponseDTO.getErrorResponseString());
        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(processorResponseDTO.getErrorResponseString());
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        Assert.assertTrue(((String) payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE))
                .contains(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE));
        Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR));
        Assert.assertFalse(processorResponseDTO.isCloseConnection());
    }

    private Attribute<Map<String, Object>> getChannelAttributeMap() {
        return new Attribute<Map<String, Object>>() {
            @Override
            public AttributeKey<Map<String, Object>> key() {
                return null;
            }

            @Override
            public Map<String, Object> get() {
                return null;
            }

            @Override
            public void set(Map<String, Object> stringObjectMap) {

            }

            @Override
            public Map<String, Object> getAndSet(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> setIfAbsent(Map<String, Object> stringObjectMap) {
                return null;
            }

            @Override
            public Map<String, Object> getAndRemove() {
                return null;
            }

            @Override
            public boolean compareAndSet(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                return false;
            }

            @Override
            public void remove() {

            }
        };
    }
}
