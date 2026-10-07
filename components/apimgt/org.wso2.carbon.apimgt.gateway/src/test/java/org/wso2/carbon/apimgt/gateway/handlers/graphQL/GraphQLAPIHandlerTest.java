/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.graphQL;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.axis2.Constants.Configuration.HTTP_METHOD;
import static org.apache.synapse.rest.RESTConstants.REST_SUB_REQUEST_PATH;

/**
 * Unit test cases related GraphQLAPIHandler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DataHolder.class, Utils.class })
public class GraphQLAPIHandlerTest {

    Axis2MessageContext messageContext;
    org.apache.axis2.context.MessageContext axis2MessageContext;
    Map<String, GraphQLSchemaDTO> schemaDTOMap;
    OMElement omElement;
    DataHolder dataHolder;

    @Before
    public void setup() throws IOException {

        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MessageContext = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        omElement = Mockito.mock(OMElement.class);
        dataHolder = Mockito.mock(DataHolder.class);
        SOAPEnvelope soapEnvelope = Mockito.mock(SOAPEnvelope.class);
        SOAPBody soapBody = Mockito.mock(SOAPBody.class);
        PowerMockito.mockStatic(DataHolder.class);
        // Partial static spy: every Utils method keeps its real behaviour (notably
        // isGraphQLSubscriptionRequest, which handleRequest short-circuits on), while sendFault is
        // suppressed so the fault raised for a blocked request can be verified without a transport.
        PowerMockito.spy(Utils.class);
        OMElement body = Mockito.mock(OMElement.class);
        Map propertyList = Mockito.mock(Map.class);

        Mockito.when(messageContext.getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("ws");
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(true);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("wss");
        Mockito.when(axis2MessageContext.getEnvelope()).thenReturn(soapEnvelope);
        Mockito.when(soapEnvelope.getBody()).thenReturn(soapBody);
        Mockito.when(soapBody.getFirstElement()).thenReturn(body);
        Mockito.when(body.getFirstChildWithName(QName.valueOf("query"))).thenReturn(omElement);

        Mockito.when(messageContext.getProperties()).thenReturn(propertyList);
        Mockito.when(messageContext.getProperty(REST_SUB_REQUEST_PATH)).thenReturn("/");
        Mockito.when(propertyList.get(REST_SUB_REQUEST_PATH)).thenReturn("/");
        Mockito.when(propertyList.get(REST_SUB_REQUEST_PATH).toString().split("/?query=")).
                thenReturn(new String[0]);
        Mockito.when(DataHolder.getInstance()).thenReturn(dataHolder);

        // Get schema and parse
        schemaDTOMap = new HashMap<>();
        String graphqlDirPath = "graphQL" + File.separator;
        String relativePath = graphqlDirPath + "schema_with_additional_props.graphql";
        String schemaString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(relativePath));
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry registry = schemaParser.parse(schemaString);
        GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
        GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
        schemaDTOMap.put("12345", schemaDTO);

        Mockito.when(dataHolder.getApiToGraphQLSchemaDTOMap()).thenReturn(schemaDTOMap);

        try {
            PowerMockito.doNothing().when(Utils.class, "sendFault", Mockito.any(MessageContext.class),
                    Mockito.anyInt());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to suppress Utils.sendFault", e);
        }
    }

    /**
     * This method will test request flow when "isGraphqlSubscriptionRequest" property is set in axis2 message context
     * when incoming transport is websocket. This occurs during Graphql Subscription request flow.
     */
    @Test
    public void testHandleRequestForGraphQLSubscriptions() {
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(true);
        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        Assert.assertTrue(graphQLAPIHandler.handleRequest(messageContext));
        Assert.assertTrue(graphQLAPIHandler.handleRequest(messageContext));
    }

    /**
     * This method will test Graphql Query request flow.
     */
    @Test
    public void testHandleRequestForGraphQLQueries() {
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn(("{allLifts{name}}"));
        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");
        Assert.assertTrue(graphQLAPIHandler.handleRequest(messageContext));
    }

    /**
     * A pure introspection query ({@code __schema}) must be rejected by the handler itself with the dedicated
     * GraphQL introspection error, instead of being allowed through with an empty elected resource and later
     * surfacing as the generic REST-shaped "no matching resource" authentication failure.
     */
    @Test
    public void testHandleRequestForIntrospectionQuery() {

        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn("{__schema{queryType{name}}}");

        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");

        Assert.assertFalse("Introspection query should be rejected by the GraphQL handler",
                graphQLAPIHandler.handleRequest(messageContext));
        assertIntrospectionRejection("__schema");
    }

    /**
     * {@code __type} is the other query-root introspection meta-field and must be rejected the same way.
     */
    @Test
    public void testHandleRequestForTypeIntrospectionQuery() {

        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn("{__type(name:\"Lift\"){fields{name}}}");

        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");

        Assert.assertFalse("__type introspection query should be rejected by the GraphQL handler",
                graphQLAPIHandler.handleRequest(messageContext));
        assertIntrospectionRejection("__type");
    }

    /**
     * A query that mixes a schema-defined field with an introspection field elects the schema-defined field, so
     * it is routed exactly as it was before this change. Such a request does not hit the misleading "no matching
     * resource" error this change addresses, and altering it would change the behaviour of a request that
     * currently succeeds - deliberately out of scope.
     */
    @Test
    public void testHandleRequestForMixedIntrospectionAndFieldQueryIsUnchanged() {

        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn("{allLifts{name} __schema{queryType{name}}}");

        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");

        Assert.assertTrue("A query that elects an operation must keep its existing behaviour",
                graphQLAPIHandler.handleRequest(messageContext));
        Mockito.verify(messageContext).setProperty(APIConstants.API_ELECTED_RESOURCE, "allLifts");
    }

    /**
     * The same holds when a root-level {@code __typename} accompanies a schema-defined field: an operation is
     * elected, so the request is routed unchanged.
     */
    @Test
    public void testHandleRequestForFieldPlusRootTypeNameIsUnchanged() {

        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn("{allLifts{name} __typename}");

        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");

        Assert.assertTrue("__typename alongside a schema-defined field must keep its existing behaviour",
                graphQLAPIHandler.handleRequest(messageContext));
        Mockito.verify(messageContext).setProperty(APIConstants.API_ELECTED_RESOURCE, "allLifts");
    }

    /**
     * Introspection meta-fields below the top level (notably {@code __typename}, which GraphQL clients such as
     * Apollo add to every nested selection set automatically) are ordinary response data and must keep working.
     */
    @Test
    public void testHandleRequestForNestedTypeNameIsAllowed() {

        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getProperty(HTTP_METHOD)).thenReturn("QUERY");
        Mockito.when(omElement.getText()).thenReturn("{allLifts{__typename name}}");

        GraphQLAPIHandler graphQLAPIHandler = new GraphQLAPIHandler();
        graphQLAPIHandler.setApiUUID("12345");

        Assert.assertTrue("A nested __typename must not be treated as an introspection request",
                graphQLAPIHandler.handleRequest(messageContext));
        Mockito.verify(messageContext).setProperty(APIConstants.API_ELECTED_RESOURCE, "allLifts");
    }

    /**
     * Asserts the fault the handler raises for a blocked introspection request: the dedicated error code,
     * message and a detail naming the offending field, plus the 400 status.
     *
     * @param expectedField the introspection field expected to be named in the error detail
     */
    private void assertIntrospectionRejection(String expectedField) {

        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_CODE,
                GraphQLConstants.GRAPHQL_INTROSPECTION_NOT_SUPPORTED);
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_MESSAGE,
                GraphQLConstants.GRAPHQL_INTROSPECTION_NOT_SUPPORTED_MESSAGE);

        ArgumentCaptor<String> detail = ArgumentCaptor.forClass(String.class);
        Mockito.verify(messageContext).setProperty(Mockito.eq(SynapseConstants.ERROR_DETAIL), detail.capture());
        Assert.assertTrue("Error detail should name the rejected introspection field but was: "
                + detail.getValue(), detail.getValue().contains(expectedField));

        PowerMockito.verifyStatic(Utils.class);
        Utils.sendFault(messageContext, HttpStatus.SC_BAD_REQUEST);
    }
}
