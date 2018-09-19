/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.SchemaCacheUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.EnumMap;
import java.util.Iterator;

/**
 * This mediator validate the JSON request and response messages against JSON schema which is defined in the swagger
 * file.
 */
public class SwaggerSchemaValidator extends AbstractMediator {

    private String swagger = null;
    private JSONObject paths;
    private JSONObject definition = null;
    private JSONObject schemas = null;
    private JSONObject jsonSchemaSetter;
    private static boolean cached = false;
    private String apiId;

    private static final Log logger = LogFactory.getLog(SwaggerSchemaValidator.class);

    @Override
    public boolean mediate(MessageContext messageContext) {

        Entry localEntryObj = null;
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String contentType = axis2MC.getProperty(ThreatProtectorConstants.REST_CONTENT_TYPE).toString();

        if (contentType != null && ThreatProtectorConstants.APPLICATION_JSON.equals(contentType)) {
            apiId = messageContext.getProperty(ThreatProtectorConstants.LOCALENTRY_ID).toString();
            if (apiId != null) {
                localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(apiId);
            }
            if (localEntryObj != null) {
                swagger = localEntryObj.getValue().toString();
            }
            if (swagger != null) {
                jsonSchemaSetter = new JSONObject(swagger);
                paths = jsonSchemaSetter.getJSONObject(APIMgtGatewayConstants.SWAGGER_PATH);

                if (jsonSchemaSetter.has(APIMgtGatewayConstants.DEFINITIONS)) {
                    definition = jsonSchemaSetter.getJSONObject(APIMgtGatewayConstants.DEFINITIONS);
                } else if (jsonSchemaSetter.has(APIMgtGatewayConstants.COMPONENTS)) {
                    JSONObject component = jsonSchemaSetter.getJSONObject(APIMgtGatewayConstants.COMPONENTS);
                    if (component != null) {
                        schemas = component.getJSONObject(APIMgtGatewayConstants.SCHEMAS);
                    }
                }
                if (!cached) {
                    cacheSchemas();
                }
                if (messageContext.isResponse()) {
                    String reqMethod = messageContext.getProperty(
                            APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
                    if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(reqMethod)) {
                        validateResponse(messageContext);
                    }
                } else {
                    String requestMethod = messageContext.getProperty(
                            APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
                    if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(requestMethod)) {
                        validateRequest(messageContext);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isContentAware() {
        return true;
    }

    /**
     * Validate the Response Message which comes from the BE.
     *
     * @param messageContext Message context
     */
    private void validateResponse(MessageContext messageContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Validating API Response message of API: " + apiId);
        }
        String electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String reqMethod = messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();

        Iterator swaggerPaths = null;
        JSONObject response = null;
        if (paths != null) {
            swaggerPaths = jsonSchemaSetter.getJSONObject(APIMgtGatewayConstants.SWAGGER_PATH).keys();
        }
        String resourceKey;
        String schema;
        String schemaReference;

        if (swaggerPaths != null) {
            while (swaggerPaths.hasNext()) {
                resourceKey = (String) swaggerPaths.next();
                if (resourceKey.equals(electedResource)) {
                    JSONObject resource = paths.getJSONObject(resourceKey);
                    if (resource != null) {
                        response = resource.getJSONObject(
                                reqMethod.toLowerCase()).getJSONObject(APIMgtGatewayConstants.RESPONSE);
                    }
                    EnumMap<APIMgtGatewayConstants.HttpStatusCode, String> httpStatuses = generateHTTPStatuses();
                    if (response != null) {
                        JSONObject schemaValue = null;
                        for (int z = 0; z < response.names().length(); z++) {
                            if (httpStatuses.containsValue(response.names().get(z))) {
                                if (response.getJSONObject(response.names().get(z).toString()).
                                        has(APIMgtGatewayConstants.SCHEMA)) {
                                    schemaReference = response.getJSONObject(response.names().get(z).toString()).
                                            get(APIMgtGatewayConstants.SCHEMA).
                                            toString();
                                    JSONObject refSchema = new JSONObject(schemaReference);
                                    schema = refSchema.get(APIMgtGatewayConstants.SCHEMA_REFERENCE).toString();
                                    int referenceSchemaObject = schema != null ? schema.
                                            lastIndexOf(APIMgtGatewayConstants.LAST_INDEX) : 0;
                                    String schemaObject = schema != null ? schema.
                                            substring(referenceSchemaObject + 1) : null;

                                    String defString = SchemaCacheUtils.getCacheSchema(schemaObject);
                                    JSONObject payloadObject = getMessageContent(messageContext);
                                    String schemaString = defString != null ? defString : null;
                                    validateContent(payloadObject, schemaString, messageContext);
                                } else if (response.getJSONObject(response.names().get(z).toString()).
                                        has(APIMgtGatewayConstants.CONTENT)) {
                                    JSONObject content = response.getJSONObject((String) response.names().get(z)).
                                            getJSONObject(APIMgtGatewayConstants.CONTENT);
                                    if (content != null) {
                                        if (content.has(APIMgtGatewayConstants.JSON_CONTENT_TYPE)) {
                                            JSONObject jsonObject = content.getJSONObject(
                                                    APIMgtGatewayConstants.JSON_CONTENT_TYPE);
                                            if (jsonObject != null) {
                                                schemaValue = jsonObject.getJSONObject(APIMgtGatewayConstants.SCHEMA);
                                            }
                                            if (schemaValue != null) {
                                                JSONObject scObject = jsonObject.getJSONObject(APIMgtGatewayConstants.SCHEMA);
                                                if (scObject != null) {
                                                    schema = jsonObject.getJSONObject(APIMgtGatewayConstants.SCHEMA).
                                                            get(APIMgtGatewayConstants.
                                                                    SCHEMA_REFERENCE).toString();
                                                    int referenceSchemaObject = schema != null ? schema.lastIndexOf(
                                                            APIMgtGatewayConstants.LAST_INDEX) : 0;
                                                    String schemaObject = schema != null ? schema.substring(
                                                            referenceSchemaObject + 1) : null;

                                                    String defString = SchemaCacheUtils.getCacheSchema(schemaObject);
                                                    JSONObject payloadObject = getMessageContent(messageContext);
                                                    String schemaString = defString != null ? defString : null;
                                                    validateContent(payloadObject, schemaString, messageContext);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Validate the Request/response content.
     *
     * @param payloadObject  Request/response payload
     * @param schemaString   Schema which is uses to validate request/response messages
     * @param messageContext Message context.
     */
    private void validateContent(JSONObject payloadObject, String schemaString, MessageContext messageContext) {

        JSONObject jsonSchema = new JSONObject(schemaString);
        Schema schema = SchemaLoader.load(jsonSchema);
        try {
            schema.validate(payloadObject);
        } catch (ValidationException e) {
            if (messageContext.isResponse()) {
                logger.error("Schema validation failed in Response :" + e.getMessage(), e);
            } else {
                logger.error("Schema validation failed in Request :" + e.getMessage(), e);
            }
            GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
        }
    }

    /**
     * Validate the Request Message.
     *
     * @param messageContext Message context.
     */
    private void validateRequest(MessageContext messageContext) {

        if (logger.isDebugEnabled()) {
            logger.debug("Validating API Request message of API: " + apiId);
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();

        String path = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();
        String schemaReference;

        String pathKey;
        JSONArray parameters;
        JSONObject requestBody;
        Iterator resourcePaths = paths.keys();
        while (resourcePaths.hasNext()) {
            pathKey = (String) resourcePaths.next();
            if (pathKey.matches(path)) {

                JSONArray jsonArray = paths.getJSONObject(pathKey).names();
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (jsonArray.get(i).equals(requestMethod.toLowerCase())) {

                        JSONObject parameterObject = paths.getJSONObject(pathKey).getJSONObject(
                                requestMethod.toLowerCase());
                        if (parameterObject != null) {
                            if (parameterObject.has(APIMgtGatewayConstants.PARAMETERS)) {
                                parameters = parameterObject.getJSONArray(APIMgtGatewayConstants.PARAMETERS);
                                if (parameters != null) {
                                    for (int x = 0; x < parameters.length(); x++) {
                                        JSONObject jsonobject = parameters.getJSONObject(i);
                                        if (jsonobject.has(APIMgtGatewayConstants.SCHEMA)) {
                                            if (jsonobject.getJSONObject(APIMgtGatewayConstants.SCHEMA).
                                                    has(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                                                schemaReference = jsonobject.getJSONObject(APIMgtGatewayConstants.SCHEMA
                                                ).get(APIMgtGatewayConstants.SCHEMA_REFERENCE).toString();
                                                int index = schemaReference != null ? schemaReference.
                                                        lastIndexOf(APIMgtGatewayConstants.LAST_INDEX) : 0;
                                                String schemaObject = schemaReference != null ? schemaReference.
                                                        substring(index + 1) : null;
                                                String schema = SchemaCacheUtils.getCacheSchema(schemaObject);
                                                JSONObject payloadObject = getMessageContent(messageContext);
                                                String refSchema = schema != null ? schema : null;
                                                if (refSchema != null)
                                                    validateContent(payloadObject, refSchema, messageContext);
                                            } else {
                                                JSONObject payloadObject = getMessageContent(messageContext);
                                                String refSchema = jsonobject.getJSONObject(
                                                        APIMgtGatewayConstants.SCHEMA).toString();
                                                validateContent(payloadObject, refSchema, messageContext);
                                            }
                                        }

                                    }
                                }
                            } else if (parameterObject.has(APIMgtGatewayConstants.REQUEST_BODY)) {
                                JSONObject schema = null;
                                requestBody = parameterObject.getJSONObject(APIMgtGatewayConstants.REQUEST_BODY);

                                if (requestBody != null) {
                                    JSONObject schemaReferenceObject = (JSONObject) requestBody.getJSONObject(
                                            APIMgtGatewayConstants.CONTENT).get(APIMgtGatewayConstants.JSON_CONTENT_TYPE);
                                    if (schemaReferenceObject != null) {
                                        schema = (JSONObject) schemaReferenceObject.get(APIMgtGatewayConstants.SCHEMA);

                                    }
                                    if (schema != null) {
                                        if (schema.has(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                                            schemaReference = schema.get(APIMgtGatewayConstants.
                                                    SCHEMA_REFERENCE).toString();
                                            int index = schemaReference != null ? schemaReference.lastIndexOf
                                                    (APIMgtGatewayConstants.LAST_INDEX) : 0;
                                            String schemaObject = schemaReference != null ? schemaReference.
                                                    substring(index + 1) : null;
                                            String cacheSchema = SchemaCacheUtils.getCacheSchema(schemaObject);
                                            JSONObject payloadObject = getMessageContent(messageContext);
                                            String refSchema = cacheSchema != null ? cacheSchema : null;
                                            if (refSchema != null)
                                                validateContent(payloadObject, refSchema, messageContext);
                                        } else {
                                            JSONObject payloadObject = getMessageContent(messageContext);
                                            String refSchema = requestBody.getJSONObject(APIMgtGatewayConstants.
                                                    SCHEMA).toString();
                                            validateContent(payloadObject, refSchema, messageContext);
                                        }
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    }

    /**
     * Get the Request/Response messageContent as a JsonObject
     *
     * @param messageContext Message context
     * @return JsonObject which contains the request/response message content
     */
    private JSONObject getMessageContent(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String requestMethod;
        if (messageContext.isResponse()) {
            requestMethod = messageContext.getProperty(ThreatProtectorConstants.HTTP_RESPONSE_METHOD).toString();
        } else {
            requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();

        }
        JSONObject payloadObject = null;
        if (!APIConstants.SupportedHTTPVerbs.GET.name().equalsIgnoreCase(requestMethod) && messageContext.getEnvelope().
                getBody().getFirstElement() != null) {
            OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
            try {
                payloadObject = new JSONObject(JsonUtil.toJsonString(xmlResponse).toString());
            } catch (AxisFault axisFault) {
                logger.error(" Error occurred while converting the String payload to Json");
            }
        }
        return payloadObject;
    }

    private void extractSchemaReference(JSONArray combineKeys) {
        JSONArray definitions = null;
        String schemaObj = null;
        String combineString;
        JSONObject defString;
        for (int n = 0; n < combineKeys.length(); n++) {
            if (combineKeys.isNull(n)) {
                combineKeys.remove(n);
                n -= 1;
            } else {
                String allOfSchema = combineKeys.get(n).toString();
                if (allOfSchema.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                    JSONObject allOfObject = new JSONObject(allOfSchema);
                    combineString = allOfObject.get(APIMgtGatewayConstants.SCHEMA_REFERENCE).toString();
                    for (int l = 0; l <= combineKeys.length(); l++) {

                        String allOfSchemaComKey = combineKeys.get(n).toString();
                        if (allOfSchemaComKey.contains(APIMgtGatewayConstants.SCHEMA_REFERENCE)) {
                            combineKeys.remove(l);
                        }
                    }
                    int index = 0;
                    if (combineString != null) {
                        index = combineString.lastIndexOf(APIMgtGatewayConstants.LAST_INDEX);
                    }
                    if (combineString != null) {
                        schemaObj = combineString.substring(index + 1);
                    }
                    String swaggerSchema;
                    if ((definition != null) || (schemas != null)) {
                        if (definitions == null) {
                            swaggerSchema = APIMgtGatewayConstants.DEFINITION;
                        } else {
                            swaggerSchema = APIMgtGatewayConstants.SCHEMA;
                        }
                        definitions = definition.names();
                        if (definitions != null) {
                            for (int k = 0; k < definitions.length(); k++) {
                                String schemaDef = definitions.get(k).toString();
                                if (schemaObj != null && schemaObj.matches(schemaDef)) {
                                    if (swaggerSchema.equals(APIMgtGatewayConstants.DEFINITION)) {
                                        defString = definition.getJSONObject(schemaObj);

                                    } else {
                                        defString = schemas.getJSONObject(schemaObj);
                                    }
                                    combineKeys.put(k, defString);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void cacheSchemas() {
        cached = true;
        if (jsonSchemaSetter.has(APIMgtGatewayConstants.DEFINITIONS)) {
            JSONArray definitionNames = definition.names();
            if (definitionNames != null) {
                pushToCache(definitionNames, definition);
            }
        } else if (jsonSchemaSetter.has(APIMgtGatewayConstants.COMPONENTS)) {
            JSONObject jsonSchemaSetterComp = jsonSchemaSetter.getJSONObject(APIMgtGatewayConstants.COMPONENTS);
            if (jsonSchemaSetterComp.getJSONObject(APIMgtGatewayConstants.SCHEMAS) != null) {
                schemas = jsonSchemaSetterComp.getJSONObject(APIMgtGatewayConstants.SCHEMAS);
                JSONArray schemaNames = schemas.names();
                if (schemaNames != null) {
                    pushToCache(schemaNames, schemas);
                }
            }
        }
    }

    private EnumMap<APIMgtGatewayConstants.HttpStatusCode, String> generateHTTPStatuses() {
        EnumMap<APIMgtGatewayConstants.HttpStatusCode, String> enumMap =
                new EnumMap<>(APIMgtGatewayConstants.HttpStatusCode.class);

        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.OK, "200");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.ACCEPTED, "202 ");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.CREATED, "201");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.NON_AUTHORITATIVE_INFORMATION, "203");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.NO_CONTENT, "204");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.RESET_CONTENT, "205");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.PARTIAL_CONTENT, "206");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.MULTI_STATUS, "207");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.ALREADY_REPORTED, "208");
        enumMap.put(APIMgtGatewayConstants.HttpStatusCode.IM_USED, "226");
        return enumMap;
    }

    private EnumMap<APIMgtGatewayConstants.CombineSchema, String> getCombineSchema() {
        EnumMap<APIMgtGatewayConstants.CombineSchema, String> combineSchema =
                new EnumMap<>(APIMgtGatewayConstants.CombineSchema.class);
        combineSchema.put(APIMgtGatewayConstants.CombineSchema.ALL_OF, "allOf");
        combineSchema.put(APIMgtGatewayConstants.CombineSchema.ANY_OF, "anyOf");
        combineSchema.put(APIMgtGatewayConstants.CombineSchema.ONE_OF, "oneOf");
        return combineSchema;
    }

    private void pushToCache(JSONArray jsonSchemaSetter, JSONObject schemas) {
        EnumMap<APIMgtGatewayConstants.CombineSchema, String> combineSchema = getCombineSchema();
        JSONArray combineSchemas;
        for (int y = 0; y < jsonSchemaSetter.length(); y++) {
            String obj = jsonSchemaSetter.get(y).toString();

            for (int z = 0; z < schemas.getJSONObject(obj).names().length(); z++) {

                if (combineSchema.containsValue(schemas.getJSONObject(obj).names().get(z))) {
                    if (schemas.getJSONObject(obj).has("allOf")) {
                        combineSchemas = schemas.getJSONObject(obj).getJSONArray(
                                APIMgtGatewayConstants.KEY_WORD_ALLOF);
                        extractSchemaReference(combineSchemas);
                    } else if (schemas.getJSONObject(obj).has("oneOf")) {
                        combineSchemas = schemas.getJSONObject(obj).getJSONArray(
                                APIMgtGatewayConstants.KEY_WORD_ONEOF);
                        extractSchemaReference(combineSchemas);
                    } else if (schemas.getJSONObject(obj).has("anyOf")) {
                        combineSchemas = schemas.getJSONObject(obj).getJSONArray(
                                APIMgtGatewayConstants.KEY_WORD_ANYOF);
                        extractSchemaReference(combineSchemas);
                    }
                }
            }
            if (SchemaCacheUtils.getCacheSchema(obj) == null) {
                SchemaCacheUtils.putCache(obj, schemas.getJSONObject(obj).toString());
            }
        }
    }
}
