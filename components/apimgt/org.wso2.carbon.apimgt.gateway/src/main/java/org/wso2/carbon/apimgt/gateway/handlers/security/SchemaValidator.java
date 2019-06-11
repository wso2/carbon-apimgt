package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.mediators.RegularExpressionProtector;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.SchemaCacheUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.exception.APIManagementException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.JsonPath;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

public class SchemaValidator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SchemaValidator.class);
    private String uuid;
    private String swagger = null;
    private JsonNode rootNode;
    private JSONObject definition = null;
    private JSONObject jsonSchemaSetter;

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String contentType = axis2MC.getProperty(ThreatProtectorConstants.REST_CONTENT_TYPE).toString();
        String requestMethod;
        try {
            RelayUtils.buildMessage(axis2MC);
            if (contentType != null && ThreatProtectorConstants.APPLICATION_JSON.equals(contentType)) {
                try {
                    initialize(messageContext);
                    pushToCache();
                } catch (APIManagementException e) {
                    logger.error("Error occurred while initializing the swagger elements", e);
                }
                if (swagger != null) {
                    requestMethod = messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
                    if (!APIConstants.SupportedHTTPVerbs.GET.name().equals(requestMethod)) {
                        try {
                            validateRequest(messageContext);
                        } catch (APIManagementException e) {
                            logger.error("Error occurred while validating the API request", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return false;
    }

    /**
     * Initialize the swagger from Local Entry.
     *
     * @param messageContext Content of the message context
     */
    private void initialize(MessageContext messageContext) throws APIManagementException {

        if (logger.isDebugEnabled()) {
            logger.info("Initializing the swagger from localEntry");
        }
        Entry localEntryObj;
        ObjectMapper objectMapper = new ObjectMapper();
        uuid = messageContext.getProperty(ThreatProtectorConstants.LOCALENTRY).toString();

        if (uuid != null) {
            localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(uuid);
            if ((!messageContext.isResponse()) && (localEntryObj != null)) {
                swagger = localEntryObj.getValue().toString();
                if (swagger != null) {
                    try {
                        rootNode = objectMapper.readTree(swagger.getBytes());
                    } catch (IOException e) {
                        throw new APIManagementException("Error Occurred while converting the Swagger" +
                                " into JsonNode", e);
                    }
                    jsonSchemaSetter = new JSONObject(swagger);
                    if (jsonSchemaSetter.has(ThreatProtectorConstants.DEFINITIONS)) {
                        definition = jsonSchemaSetter.getJSONObject(ThreatProtectorConstants.DEFINITIONS);
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
        if ( logger.isDebugEnabled() ) {
            logger.debug("Validating JSON content against the schema");
        }
        JSONObject jsonSchema = new JSONObject(schemaString);
        Schema schema = SchemaLoader.load(jsonSchema);
        if (schema != null) {
            try {
                schema.validate(payloadObject);
            } catch (ValidationException e) {
                if ( messageContext.isResponse() ) {
                    logger.error("Schema validation failed in Response :" + e.getMessage(), e);
                } else {
                    logger.error("Schema validation failed in Request :" + e.getMessage(), e);
                }
                GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
            }
        }
    }

    /**
     * Validate the Request JSON Message.
     * @param messageContext Message context.
     */
    private void validateRequest(MessageContext messageContext) throws APIManagementException {


        Map<String, String> map = getSchemaContent(messageContext);
        String schema = null;
        String key;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if ( entry.getKey().equals(ThreatProtectorConstants.KEY) ) {
                String keyValue = entry.getValue();
                if (keyValue != null) {
                    key = generateCacheKey(keyValue, messageContext);
                    schema = SchemaCacheUtils.getCacheSchema(key);
                }
            } else {
                schema = entry.getValue();
            }
        }
        JSONObject payloadObject = getMessageContent(messageContext);
        if (schema != null) {
            if ( schema != null )
                validateContent(payloadObject, schema, messageContext);
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
        if ( messageContext.isResponse() ) {
            requestMethod = messageContext.getProperty(ThreatProtectorConstants.HTTP_RESPONSE_METHOD).toString();
        } else {
            requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();

        }
        JSONObject payloadObject = null;
        if ( !APIConstants.SupportedHTTPVerbs.GET.name().equalsIgnoreCase(requestMethod) && messageContext.getEnvelope().
                getBody() != null ) {
            OMElement xmlResponse = messageContext.getEnvelope().getBody().getFirstElement();
            try {
                payloadObject = new JSONObject(JsonUtil.toJsonString(xmlResponse).toString());
            } catch (AxisFault axisFault) {
                logger.error(" Error occurred while converting the String payload to Json");
            }
        }
        return payloadObject;
    }

    /**
     * Put schema definitions into cache.
     * @throws APIManagementException
     */
    private void pushToCache() throws APIManagementException {
        if ( logger.isDebugEnabled() ) {
            logger.debug("Adding schema to cache");
        }
        if ( jsonSchemaSetter.has(ThreatProtectorConstants.DEFINITIONS) ) {
            JSONArray jsonSchemaSetter = definition.names();
            if ( jsonSchemaSetter != null ) {
                JsonNode schemas = rootNode.at("/" + ThreatProtectorConstants.DEFINITIONS);
                JsonNode schemaValue;
                for (int y = 0; y < jsonSchemaSetter.length(); y++) {
                    String schemaName = jsonSchemaSetter.get(y).toString();
                    schemaValue = schemas.at(ThreatProtectorConstants.LAST_INDEX + schemaName);
                    generateSchema(schemaValue);
                    if ( SchemaCacheUtils.getCacheSchema(schemaName) == null ) {
                        SchemaCacheUtils.putCache(schemaName + "-" + uuid, schemaValue.toString());
                    }
                }
            }
        }
    }

    /**
     * Replace $ref references with relevant schemas and recreate swagger definition
     * @param parent Swagger definition parent Node
     * @throws APIManagementException
     */
    private void generateSchema(JsonNode parent) throws APIManagementException {
        JsonNode schemaProperty;
        Iterator<Map.Entry<String, JsonNode>> schemaNode = parent.fields();
        while (schemaNode.hasNext()) {
            Map.Entry<String, JsonNode> entry = schemaNode.next();
            if ( entry.getValue().has(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                JsonNode refNode = entry.getValue();
                Iterator<Map.Entry<String, JsonNode>> refItems = refNode.fields();
                while (refItems.hasNext()) {
                    Map.Entry<String, JsonNode> entryRef = refItems.next();
                    if ( entryRef.getKey().equals(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                        JsonNode schemaObject = extractSchemaObject(entryRef.getValue());
                        if (schemaObject != null) {
                            entry.setValue(schemaObject);
                        }
                    }
                }
            }
            schemaProperty = entry.getValue();
            if ( JsonNodeType.OBJECT == schemaProperty.getNodeType() ) {
                generateSchema(schemaProperty);
            }
            if ( JsonNodeType.ARRAY == schemaProperty.getNodeType() ) {
                generateArraySchemas(entry);
            }
        }
    }

    private JsonNode extractSchemaObject(JsonNode refNode) {
        String[] val = refNode.toString().split("#");
        String path = val[1].replace("\\{^\"|\"}", "").replaceAll("\"", "");
        return rootNode.at(path);
    }

    /**
     * Get the relevant schema content to particular request
     * @param messageContext Message content.
     * @return particular schema content with its schema initialization pattern(key/schema)
     */
    private Map getSchemaContent(MessageContext messageContext) throws APIManagementException {
        Map<String, String> map;
        Map<String, String> schemaMap = new HashMap<>();
        String schemaKey;
        String schema;
        if ( !messageContext.isResponse() ) {
            map = extractSchemaFromRequest(messageContext);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if ( entry.getKey().equals(ThreatProtectorConstants.KEY) ) {
                    schemaKey = entry.getValue();
                    schemaMap.put(ThreatProtectorConstants.KEY, schemaKey);
                    return schemaMap;
                } else {
                    schema = entry.getValue();
                    schemaMap.put(ThreatProtectorConstants.SCHEMA, schema);
                    return schemaMap;
                }
            }
        } else {
            schemaKey = extractResponse(messageContext);
            if (schemaKey != null) {
                schemaMap.put(ThreatProtectorConstants.KEY, schemaKey);
                return schemaMap;
            }
        }
        return schemaMap;
    }

    /**
     * Generate a key for each swagger reference: $ref
     * @param schemaKey $ref value
     * @param messageContext Mediation message context
     * @return generated cache ket to be persist
     */
    private String generateCacheKey(String schemaKey, MessageContext messageContext) {
        if (schemaKey != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(schemaKey);
            stringBuilder.append("-");
            stringBuilder.append(messageContext.getProperty(ThreatProtectorConstants.LOCALENTRY).toString());
            return  stringBuilder.toString();
        } else {
            return null;
        }
    }

    /**
     * Extract key/schema from the swagger.(Swagger request methods contains either reference for the schema or flat
     * schema. key - schema reference key, schema - flat schema)
     * @param messageContext
     * @return key and schema map
     */
    private Map<String, String> extractSchemaFromRequest(MessageContext messageContext) throws APIManagementException {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();

        String resourcePath = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();
        Map<String, String> references = new HashMap<>();
        String Swagger = swagger;

        String schema = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS + resourcePath +
                ThreatProtectorConstants.JSONPATH_SEPRATER + requestMethod.toLowerCase()
                + ".parameters..schema").toString();
        if ( schema.contains(ThreatProtectorConstants.SCHEMA_REFERENCE) && schema != null ) {
            byte[] bytes = schema.getBytes();
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(bytes);
                Iterator<JsonNode> schemaNode = node.findParent(ThreatProtectorConstants.SCHEMA_REFERENCE).elements();
                while (schemaNode.hasNext()) {
                    String keyValue = extractRef(schemaNode);
                    if (keyValue != null) {
                        references.put(ThreatProtectorConstants.KEY, keyValue);
                        return references;
                    }
                }
            } catch (IOException e) {
                throw new APIManagementException("Error occurred while retrieving the value from json path", e);
            }
        } else {
            // remove open and ending square brackets
            references.put("schema", schema.substring(1, schema.length()-1));
            return references;

        }
        return references;
    }

    /**
     * Extract the response schema from swagger according to the response code.
     * @param messageContext message content
     * @return response schema
     * @throws APIManagementException wrap and throw IOException
     */
    private String extractResponse(MessageContext messageContext) throws APIManagementException {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        String electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE).toString();
        String reqMethod = messageContext.getProperty(APIMgtGatewayConstants.ELECTED_REQUEST_METHOD).toString();
        String responseStatus = axis2MC.getProperty(APIMgtGatewayConstants.HTTP_SC).toString();
        String Swagger = swagger;
        String resourceSchema;
        String defaultSchema;
        String nonSchema = "NonSchema";

        resourceSchema = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS +
                electedResource + ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                ThreatProtectorConstants.JSONPATH_RESPONSES + responseStatus).toString();
        if ( resourceSchema != null ) {
            defaultSchema = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS + electedResource +
                    ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                    ThreatProtectorConstants.JSONPATH_RESPONSES +
                    responseStatus + ".schema").toString();
            if ( defaultSchema != null ) {
                if ( defaultSchema.contains(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                    byte[] bytes = defaultSchema.getBytes();
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        JsonNode node = mapper.readTree(bytes);
                        Iterator<JsonNode> schemaNode = node.findParent(
                                ThreatProtectorConstants.SCHEMA_REFERENCE).elements();
                        return extractRef(schemaNode);

                    } catch (IOException e) {
                        throw new APIManagementException("Error occurred while converting bytes from json node");
                    }
                } else {
                    return defaultSchema;
                }

            } else {
                resourceSchema = JsonPath.read(Swagger, ThreatProtectorConstants.JSONPATH_PATHS
                        + electedResource + ThreatProtectorConstants.JSONPATH_SEPRATER + reqMethod.toLowerCase() +
                        ".responses.default").toString();
                if ( resourceSchema != null ) {
                    if ( resourceSchema.contains(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                        byte[] bytes = resourceSchema.getBytes();
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            JsonNode node = mapper.readTree(bytes);
                            if (node != null) {
                                Iterator<JsonNode> schemaNode = node.findParent(
                                        ThreatProtectorConstants.SCHEMA_REFERENCE).elements();
                                return extractRef(schemaNode);
                            }

                        } catch (IOException e) {
                            throw new APIManagementException(e);
                        }
                    } else {
                        return resourceSchema;
                    }
                } else {
                    return defaultSchema;
                }
            }
        }
        return nonSchema;
    }

    /**
     * Replace $ref array elements
     *
     * @param entry Swagger content
     * @throws APIManagementException
     */
    private void generateArraySchemas(Map.Entry<String, JsonNode> entry) throws APIManagementException {
        JsonNode entryRef;
        JsonNode schemaProperty;
        if ( entry.getValue() != null ) {
            schemaProperty = entry.getValue();
            if ( schemaProperty != null ) {
                Iterator<JsonNode> arrayElements = schemaProperty.elements();
                while (arrayElements.hasNext()) {
                    entryRef = arrayElements.next();
                    if ( entryRef != null ) {
                        if ( entryRef.has(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                            entryRef = extractSchemaObject(entryRef);
                            ObjectMapper mapper = new ObjectMapper();
                            String str[] = schemaProperty.toString().split(",");
                            if ( str != null && str.length > 0 ) {
                                List<String> schemaItems = Arrays.asList(str);
                                ArrayList<String> convertedSchemaItems = new ArrayList(schemaItems);
                                for (int x = 0; x < convertedSchemaItems.size(); x++) {
                                    String refItem = convertedSchemaItems.get(x);
                                    if ( refItem.contains(ThreatProtectorConstants.SCHEMA_REFERENCE) ) {
                                        convertedSchemaItems.remove(refItem);
                                        convertedSchemaItems.add(entryRef.toString());
                                    }
                                }
                                try {
                                    JsonNode actualObj = mapper.readTree(convertedSchemaItems.toString());
                                    entry.setValue(actualObj);
                                } catch (IOException e) {
                                    throw new APIManagementException(
                                            "Error occurred while converting string to json elements", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Schema path from $ref
     * @param schemaNode Swagger schema content
     * @return $ref path
     */
    private String extractRef(Iterator<JsonNode> schemaNode) {
        while (schemaNode.hasNext()) {
            String nodeVal = schemaNode.next().toString();
            String[] val = nodeVal.split("#");
            if (val.length> 0) {
                String path = val[1].replaceAll("^\"|\"$", "");
                if (StringUtils.isNotEmpty(path)) {
                    int c = path.lastIndexOf('/');
                    return path.substring(c + 1);
                }
            }
            return null;
        }
        return null;
    }

}
