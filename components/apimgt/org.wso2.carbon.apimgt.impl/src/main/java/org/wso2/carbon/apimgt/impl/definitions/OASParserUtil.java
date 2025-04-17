/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.impl.definitions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Yaml;
import io.swagger.parser.util.DeserializationUtils;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.parser.ObjectMapperFactory;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.definitions.APIParserConstants;
import org.wso2.carbon.apimgt.impl.definitions.mixin.License31Mixin;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provide common functions related to OAS
 */
public class OASParserUtil {
    private static final Log log = LogFactory.getLog(OASParserUtil.class);
    private static APIDefinition oas2Parser = new OAS2Parser();
    private static APIDefinition oas3Parser = new OAS3Parser();
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static SwaggerConverter swaggerConverter = new SwaggerConverter();

    public enum SwaggerVersion {
        SWAGGER,
        OPEN_API,
    }

    private static final String REQUEST_BODIES = "requestBodies";
    private static final String SCHEMAS = "schemas";
    private static final String PARAMETERS = "parameters";
    private static final String RESPONSES = "responses";
    private static final String HEADERS = "headers";
    private static final String EXAMPLES = "examples";

    private static final String REF_PREFIX = "#/components/";
    private static final String OBJECT_DATA_TYPE = "object";
    private static final String OPENAPI_RESOURCE_KEY = "paths";
    private static final String[] UNSUPPORTED_RESOURCE_BLOCKS = new String[]{"servers"};

    static class SwaggerUpdateContext {
        private final Paths paths = new Paths();
        private final Set<Scope> aggregatedScopes = new HashSet<>();
        private final Map<String, Set<String>> referenceObjectMap = new HashMap<>();
        private final Set<Components> aggregatedComponents = new HashSet<>();

        SwaggerUpdateContext() {
            referenceObjectMap.put(REQUEST_BODIES, new HashSet<>());
            referenceObjectMap.put(SCHEMAS, new HashSet<>());
            referenceObjectMap.put(PARAMETERS, new HashSet<>());
            referenceObjectMap.put(RESPONSES, new HashSet<>());
            referenceObjectMap.put(HEADERS, new HashSet<>());
            referenceObjectMap.put(EXAMPLES, new HashSet<>());
        }


        Paths getPaths() {
            return paths;
        }

        Set<Scope> getAggregatedScopes() {
            return aggregatedScopes;
        }

        Map<String, Set<String>> getReferenceObjectMapping() {
            return referenceObjectMap;
        }

        public Set<Components> getAggregatedComponents() {
            return aggregatedComponents;
        }
    }

    /**
     * Map<String, Object>
     * Return correct OAS parser by validating give definition with OAS 2/3 parsers.
     *
     * @param apiDefinition OAS definition
     * @return APIDefinition APIDefinition parser
     * @throws APIManagementException If error occurred while parsing definition.
     */
    public static APIDefinition getOASParser(String apiDefinition) throws APIManagementException {

        SwaggerVersion swaggerVersion = getSwaggerVersion(apiDefinition);

        if (swaggerVersion == SwaggerVersion.SWAGGER) {
            return oas2Parser;
        }

        return oas3Parser;
    }

    public static SwaggerVersion getSwaggerVersion(String apiDefinition) throws APIManagementException {
        ObjectMapper mapper;
        if (apiDefinition.trim().startsWith("{")) {
            mapper = ObjectMapperFactory.createJson();
        } else {
            mapper = ObjectMapperFactory.createYaml();
        }
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(apiDefinition.getBytes());
        } catch (IOException e) {
            throw new APIManagementException("Error occurred while parsing OAS definition", e);
        }
        ObjectNode node = (ObjectNode) rootNode;
        JsonNode openapi = node.get("openapi");
        if (openapi != null && openapi.asText().startsWith("3.")) {
            return SwaggerVersion.OPEN_API;
        }
        JsonNode swagger = node.get("swagger");
        if (swagger != null) {
            return SwaggerVersion.SWAGGER;
        }

        String errMsg = "Could not determine the OAS version as the version element of the definition is not found.";
        ExceptionCodes errorHandler = ExceptionCodes.OAS_DEFINITION_VERSION_NOT_FOUND;
        throw new APIManagementException(errMsg, errorHandler);
    }

    public static Map<String, Object> generateExamples(String apiDefinition) throws APIManagementException {
        SwaggerVersion destinationSwaggerVersion = getSwaggerVersion(apiDefinition);

        if (destinationSwaggerVersion == SwaggerVersion.OPEN_API) {
            return oas3Parser.generateExample(apiDefinition);
        } else if (destinationSwaggerVersion == SwaggerVersion.SWAGGER) {
            return oas2Parser.generateExample(apiDefinition);
        } else {
            throw new APIManagementException("Cannot update destination swagger because it is not in OpenAPI format");
        }
    }

    public static String getOASDefinitionWithTierContentAwareProperty(String apiDefinition,
                                                                      List<String> contentAwareTiersList, String apiLevelTier) throws APIManagementException {
        if (contentAwareTiersList == null || contentAwareTiersList.isEmpty()) {
            // no modifications if the list is empty
            return apiDefinition;
        }
        SwaggerVersion destinationSwaggerVersion = getSwaggerVersion(apiDefinition);

        if (destinationSwaggerVersion == SwaggerVersion.OPEN_API) {
            return oas3Parser.getOASDefinitionWithTierContentAwareProperty(apiDefinition, contentAwareTiersList,
                    apiLevelTier);
        } else if (destinationSwaggerVersion == SwaggerVersion.SWAGGER) {
            return oas2Parser.getOASDefinitionWithTierContentAwareProperty(apiDefinition, contentAwareTiersList,
                    apiLevelTier);
        } else {
            throw new APIManagementException("Cannot update destination swagger because it is not in OpenAPI format");
        }
    }

    public static String updateAPIProductSwaggerOperations(Map<API, List<APIProductResource>> apiToProductResourceMapping,
                                                           String destinationSwagger)
            throws APIManagementException {
        SwaggerVersion destinationSwaggerVersion = getSwaggerVersion(destinationSwagger);
        OpenAPI destOpenAPI;

        if (destinationSwaggerVersion == SwaggerVersion.OPEN_API) {
            destOpenAPI = ((OAS3Parser) oas3Parser).getOpenAPI(destinationSwagger);
        } else {
            String errorMessage = "Cannot update destination swagger because it is not in OpenAPI format";
            throw new APIManagementException(errorMessage, ExceptionCodes.NOT_IN_OPEN_API_FORMAT);
        }

        SwaggerUpdateContext context = new SwaggerUpdateContext();

        extractRelevantSourceData(apiToProductResourceMapping, context);

        // Update paths
        destOpenAPI.setPaths(context.getPaths());

        // Update Scopes
        setScopes(destOpenAPI, context.getAggregatedScopes());

        // Update reference definitions
        setReferenceObjectDefinitions(destOpenAPI, context);

        return convertOAStoJSON(destOpenAPI);
    }

    private static void setScopes(final OpenAPI destOpenAPI, final Set<Scope> aggregatedScopes) {
        Map<String, SecurityScheme> securitySchemes;
        SecurityScheme securityScheme;
        OAuthFlow oAuthFlow;
        Scopes scopes = new Scopes();
        if (destOpenAPI.getComponents() != null &&
                (securitySchemes = destOpenAPI.getComponents().getSecuritySchemes()) != null &&
                (securityScheme = securitySchemes.get(OAS3Parser.OPENAPI_SECURITY_SCHEMA_KEY)) != null &&
                (oAuthFlow = securityScheme.getFlows().getImplicit()) != null) {

            Map<String, String> scopeBindings = new HashMap<>();

            for (Scope scope : aggregatedScopes) {
                scopes.addString(scope.getKey(), scope.getDescription());
                scopeBindings.put(scope.getKey(), scope.getRoles());
            }

            oAuthFlow.setScopes(scopes);

            Map<String, Object> extensions = new HashMap<>();
            extensions.put(APIParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            oAuthFlow.setExtensions(extensions);
        }
    }

    private static void setReferenceObjectDefinitions(final OpenAPI destOpenAPI, SwaggerUpdateContext context) {
        processReferenceObjectMap(context);

        Components components = destOpenAPI.getComponents();
        Set<Components> aggregatedComponents = context.getAggregatedComponents();

        for (Components sourceComponents : aggregatedComponents) {
            Map<String, Set<String>> referenceObjectMap = context.getReferenceObjectMapping();
            for (Map.Entry<String, Set<String>> refCategoryEntry : referenceObjectMap.entrySet()) {
                String category = refCategoryEntry.getKey();

                if (REQUEST_BODIES.equalsIgnoreCase(category)) {
                    Map<String, RequestBody> sourceRequestBodies = sourceComponents.getRequestBodies();

                    if (sourceRequestBodies != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            RequestBody requestBody = sourceRequestBodies.get(refKey);
                            if (requestBody != null) {
                                components.addRequestBodies(refKey, requestBody);
                            }
                        }
                    }
                }

                if (SCHEMAS.equalsIgnoreCase(category)) {
                    Map<String, Schema> sourceSchemas = sourceComponents.getSchemas();

                    if (sourceSchemas != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Schema schema = sourceSchemas.get(refKey);
                            if (schema != null) {
                                components.addSchemas(refKey, schema);
                            }
                        }
                    }
                }

                if (PARAMETERS.equalsIgnoreCase(category)) {
                    Map<String, Parameter> parameters = sourceComponents.getParameters();

                    if (parameters != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Parameter parameter = parameters.get(refKey);
                            if (parameter != null) {
                                components.addParameters(refKey, parameter);
                            }
                        }
                    }
                }

                if (RESPONSES.equalsIgnoreCase(category)) {
                    Map<String, ApiResponse> responses = sourceComponents.getResponses();

                    if (responses != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            ApiResponse response = responses.get(refKey);
                            if (response != null) {
                                components.addResponses(refKey, response);
                            }
                        }
                    }
                }

                if (HEADERS.equalsIgnoreCase(category)) {
                    Map<String, Header> headers = sourceComponents.getHeaders();

                    if (headers != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Header header = headers.get(refKey);
                            if (header != null) {
                                components.addHeaders(refKey, header);
                            }
                        }
                    }
                }

                if (EXAMPLES.equalsIgnoreCase(category)) {
                    Map<String, Example> examples = sourceComponents.getExamples();

                    if (examples != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Example example = examples.get(refKey);
                            if (example != null) {
                                components.addExamples(refKey, example);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void processReferenceObjectMap(SwaggerUpdateContext context) {
        // Get a deep copy of the reference objects in order to prevent Concurrent modification exception
        // since we may need to update the reference object mapping while iterating through it
        Map<String, Set<String>> referenceObjectsMappingCopy = getReferenceObjectsCopy(context.getReferenceObjectMapping());

        int preRefObjectCount = getReferenceObjectCount(context.getReferenceObjectMapping());

        Set<Components> aggregatedComponents = context.getAggregatedComponents();
        for (Components sourceComponents : aggregatedComponents) {

            for (Map.Entry<String, Set<String>> refCategoryEntry : referenceObjectsMappingCopy.entrySet()) {
                String category = refCategoryEntry.getKey();

                if (REQUEST_BODIES.equalsIgnoreCase(category)) {
                    Map<String, RequestBody> sourceRequestBodies = sourceComponents.getRequestBodies();

                    if (sourceRequestBodies != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            RequestBody requestBody = sourceRequestBodies.get(refKey);
                            setRefOfRequestBody(requestBody, context);
                        }
                    }
                }

                if (SCHEMAS.equalsIgnoreCase(category)) {
                    Map<String, Schema> sourceSchemas = sourceComponents.getSchemas();

                    if (sourceSchemas != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Schema schema = sourceSchemas.get(refKey);
                            extractReferenceFromSchema(schema, context);
                        }
                    }
                }

                if (PARAMETERS.equalsIgnoreCase(category)) {
                    Map<String, Parameter> parameters = sourceComponents.getParameters();

                    if (parameters != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Parameter parameter = parameters.get(refKey);
                            setRefOfParameter(parameter, context);
                        }
                    }
                }

                if (RESPONSES.equalsIgnoreCase(category)) {
                    Map<String, ApiResponse> responses = sourceComponents.getResponses();

                    if (responses != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            ApiResponse response = responses.get(refKey);
                            setRefOfApiResponse(response, context);
                        }
                    }
                }

                if (HEADERS.equalsIgnoreCase(category)) {
                    Map<String, Header> headers = sourceComponents.getHeaders();

                    if (headers != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Header header = headers.get(refKey);
                            setRefOfApiResponseHeader(header, context);
                        }
                    }
                }

                if (EXAMPLES.equalsIgnoreCase(category)) {
                    Map<String, Example> examples = sourceComponents.getExamples();

                    if (examples != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Example example = examples.get(refKey);
                            setRefOfExample(example, context);
                        }
                    }
                }
            }

            int postRefObjectCount = getReferenceObjectCount(context.getReferenceObjectMapping());

            if (postRefObjectCount > preRefObjectCount) {
                processReferenceObjectMap(context);
            }
        }
    }

    private static int getReferenceObjectCount(Map<String, Set<String>> referenceObjectMap) {
        int total = 0;

        for (Set<String> refKeys : referenceObjectMap.values()) {
            total += refKeys.size();
        }

        return total;
    }

    private static Map<String, Set<String>> getReferenceObjectsCopy(Map<String, Set<String>> referenceObject) {
        return referenceObject.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));
    }

    private static void extractRelevantSourceData(Map<API, List<APIProductResource>> apiToProductResourceMapping,
                                                  SwaggerUpdateContext context) throws APIManagementException {
        // Extract Paths that exist in the destination swagger from the source swagger
        for (Map.Entry<API, List<APIProductResource>> mappingEntry : apiToProductResourceMapping.entrySet()) {
            String sourceSwagger = mappingEntry.getKey().getSwaggerDefinition();
            SwaggerVersion sourceSwaggerVersion = getSwaggerVersion(sourceSwagger);

            if (sourceSwaggerVersion == SwaggerVersion.OPEN_API) {
                OpenAPI srcOpenAPI = ((OAS3Parser) oas3Parser).getOpenAPI(sourceSwagger);

                Set<Components> aggregatedComponents = context.getAggregatedComponents();
                Components components = srcOpenAPI.getComponents();

                if (components != null) {
                    aggregatedComponents.add(components);
                }

                Set<Scope> allScopes = oas3Parser.getScopes(sourceSwagger);

                Paths srcPaths = srcOpenAPI.getPaths();
                List<APIProductResource> apiProductResources = mappingEntry.getValue();

                for (APIProductResource apiProductResource : apiProductResources) {
                    URITemplate uriTemplate = apiProductResource.getUriTemplate();
                    PathItem srcPathItem = srcPaths.get(uriTemplate.getUriTemplate());
                    readPathsAndScopes(srcPathItem, uriTemplate, allScopes, context);
                }
            } else if (sourceSwaggerVersion == SwaggerVersion.SWAGGER) {
                Swagger srcSwagger = ((OAS2Parser) oas2Parser).getSwagger(sourceSwagger);

                Set<Components> aggregatedComponents = context.getAggregatedComponents();
                Components components = swaggerConverter.readContents(sourceSwagger, null, null).
                        getOpenAPI().getComponents();

                if (components != null) {
                    aggregatedComponents.add(components);
                }

                Set<Scope> allScopes = oas2Parser.getScopes(sourceSwagger);
                Map<String, Path> srcPaths = srcSwagger.getPaths();
                List<APIProductResource> apiProductResources = mappingEntry.getValue();

                for (APIProductResource apiProductResource : apiProductResources) {
                    URITemplate uriTemplate = apiProductResource.getUriTemplate();
                    Path srcPath = srcPaths.get(uriTemplate.getUriTemplate());
                    readPathsAndScopes(swaggerConverter.convert(srcPath), uriTemplate, allScopes, context);
                }
            }
        }
    }

    private static void readPathsAndScopes(PathItem srcPathItem, URITemplate uriTemplate,
                                           final Set<Scope> allScopes, SwaggerUpdateContext context) {
        Map<PathItem.HttpMethod, Operation> srcOperations = srcPathItem.readOperationsMap();

        PathItem.HttpMethod httpMethod = PathItem.HttpMethod.valueOf(uriTemplate.getHTTPVerb().toUpperCase());
        Operation srcOperation = srcOperations.get(httpMethod);

        Paths paths = context.getPaths();
        Set<Scope> aggregatedScopes = context.getAggregatedScopes();

        if (!paths.containsKey(uriTemplate.getUriTemplate())) {
            paths.put(uriTemplate.getUriTemplate(), new PathItem());
        }

        PathItem pathItem = paths.get(uriTemplate.getUriTemplate());
        pathItem.operation(httpMethod, srcOperation);
        if (pathItem.getParameters() == null && srcPathItem.getParameters() != null) {
            pathItem.setParameters(srcPathItem.getParameters());
            setRefOfParameters(srcPathItem.getParameters(), context);
        }

        readReferenceObjects(srcOperation, context);

        List<SecurityRequirement> srcOperationSecurity = srcOperation.getSecurity();
        if (srcOperationSecurity != null) {
            for (SecurityRequirement requirement : srcOperationSecurity) {
                List<String> scopes = requirement.get(OAS3Parser.OPENAPI_SECURITY_SCHEMA_KEY);
                if (scopes != null) {
                    for (String scopeKey : scopes) {
                        for (Scope scope : allScopes) {
                            if (scope.getKey().equals(scopeKey)) {
                                aggregatedScopes.add(scope);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void readReferenceObjects(Operation srcOperation, SwaggerUpdateContext context) {
        setRefOfRequestBody(srcOperation.getRequestBody(), context);

        setRefOfApiResponses(srcOperation.getResponses(), context);

        setRefOfApiResponseHeaders(srcOperation.getResponses(), context);

        setRefOfParameters(srcOperation.getParameters(), context);
    }

    private static void setRefOfRequestBody(RequestBody requestBody, SwaggerUpdateContext context) {
        if (requestBody != null) {
            Content content = requestBody.getContent();
            if (content != null) {
                extractReferenceFromContent(content, context);
            } else {
                String ref = requestBody.get$ref();
                if (ref != null) {
                    addToReferenceObjectMap(ref, context);
                }
            }
        }
    }

    private static void setRefOfApiResponses(ApiResponses responses, SwaggerUpdateContext context) {
        if (responses != null) {
            for (ApiResponse response : responses.values()) {
                setRefOfApiResponse(response, context);
            }
        }
    }

    /**
     * Process a given response entry of the API definition.
     *
     * @param response  The response object which needs to be processed.
     * @param context The SwaggerUpdateContext object containing the context of the API definition.
     */
    private static void setRefOfApiResponse(ApiResponse response, SwaggerUpdateContext context) {
        if (response != null) {
            Content content = response.getContent();
            if (content != null) {
                extractReferenceFromContent(content, context);
            } else {
                String ref = response.get$ref();
                if (ref != null) {
                    addToReferenceObjectMap(ref, context);
                }
            }

            Map<String, Header> headers = response.getHeaders();
            if (headers != null) {
                for (Header header : headers.values()) {
                    setRefOfApiResponseHeader(header, context);
                }
            }
        }
    }

    private static void setRefOfApiResponseHeaders(ApiResponses responses, SwaggerUpdateContext context) {
        if (responses != null) {
            for (ApiResponse response : responses.values()) {
                Map<String, Header> headers = response.getHeaders();

                if (headers != null) {
                    for (Header header : headers.values()) {
                        setRefOfApiResponseHeader(header, context);
                    }
                }
            }
        }
    }

    /**
     * Process a given response header entry of the API definition.
     *
     * @param header  The header object which needs to be processed.
     * @param context The SwaggerUpdateContext object containing the context of the API definition.
     */
    private static void setRefOfApiResponseHeader(Header header, SwaggerUpdateContext context) {
        if (header != null) {
            Content content = header.getContent();
            if (content != null) {
                extractReferenceFromContent(content, context);
            } else {
                Schema schema = header.getSchema();
                if (schema != null) {
                    extractReferenceFromSchema(schema, context);
                } else {
                    String ref = header.get$ref();
                    if (ref != null) {
                        addToReferenceObjectMap(ref, context);
                    }
                }
            }
        }
    }

    private static void setRefOfParameters(List<Parameter> parameters, SwaggerUpdateContext context) {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                setRefOfParameter(parameter, context);
            }
        }
    }

    /**
     * Process a given parameter entry of the API definition.
     *
     * @param parameter  The parameter object which needs to be processed.
     * @param context The SwaggerUpdateContext object containing the context of the API definition.
     */
    private static void setRefOfParameter(Parameter parameter, SwaggerUpdateContext context) {
        if (parameter != null) {
            Content content = parameter.getContent();
            if (content != null) {
                extractReferenceFromContent(content, context);
            } else {
                Schema schema = parameter.getSchema();
                if (schema != null) {
                    String ref = schema.get$ref();
                    if (ref != null) {
                        addToReferenceObjectMap(ref, context);
                    }
                } else {
                    String ref = parameter.get$ref();
                    if (ref != null) {
                        extractReferenceWithoutSchema(ref, context);
                    }
                }
            }
        }
    }

    /**
     * Process a given example entry of the API definition.
     *
     * @param example  The example object which needs to be processed.
     * @param context The SwaggerUpdateContext object containing the context of the API definition.
     */
    private static void setRefOfExample(Example example, SwaggerUpdateContext context) {
        if (example != null) {
            String ref = example.get$ref();
            if (ref != null) {
                addToReferenceObjectMap(ref, context);
            }
        }
    }

    private static void extractReferenceFromContent(Content content, SwaggerUpdateContext context) {
        if (content != null) {
            for (MediaType mediaType : content.values()) {
                Schema schema = mediaType.getSchema();

                extractReferenceFromSchema(schema, context);

                Map<String, Example> examples = mediaType.getExamples();
                if (examples != null) {
                    for (Map.Entry<String, Example> exampleEntry : examples.entrySet()) {
                        Example example = exampleEntry.getValue();
                        setRefOfExample(example, context);
                    }
                }
            }
        }
    }

    private static void extractReferenceWithoutSchema(String reference, SwaggerUpdateContext context) {
        if (reference != null) {
            addToReferenceObjectMap(reference, context);
        }
    }

    private static void extractReferenceFromSchema(Schema schema, SwaggerUpdateContext context) {
        if (schema != null) {
            String ref = schema.get$ref();
            List<String> references = new ArrayList<String>();
            if (ref == null) {
                if (schema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) schema;
                    Schema itemsSchema = arraySchema.getItems();
                    // Process $ref items
                    ref = itemsSchema.get$ref();
                    if (ref == null) {
                        // Process items in the form of Composed Schema such as allOf, oneOf, anyOf
                        extractReferenceFromSchema(itemsSchema, context);
                    }
                } else if (schema instanceof ObjectSchema) {
                    references = addSchemaOfSchema(schema, context);
                } else if (schema instanceof MapSchema) {
                    Schema additionalPropertiesSchema = (Schema) schema.getAdditionalProperties();
                    extractReferenceFromSchema(additionalPropertiesSchema, context);
                } else if (schema instanceof ComposedSchema) {
                    if (((ComposedSchema) schema).getAllOf() != null) {
                        for (Schema sc : ((ComposedSchema) schema).getAllOf()) {
                            if (OBJECT_DATA_TYPE.equalsIgnoreCase(sc.getType())) {
                                references.addAll(addSchemaOfSchema(sc, context));
                            } else {
                                String schemaRef = sc.get$ref();
                                if (schemaRef != null) {
                                    references.add(sc.get$ref());
                                } else {
                                    processSchemaProperties(sc, context);
                                }
                            }
                        }
                    }
                    if (((ComposedSchema) schema).getAnyOf() != null) {
                        for (Schema sc : ((ComposedSchema) schema).getAnyOf()) {
                            if (OBJECT_DATA_TYPE.equalsIgnoreCase(sc.getType())) {
                                references.addAll(addSchemaOfSchema(sc, context));
                            } else {
                                String schemaRef = sc.get$ref();
                                if (schemaRef != null) {
                                    references.add(sc.get$ref());
                                } else {
                                    processSchemaProperties(sc, context);
                                }
                            }
                        }
                    }
                    if (((ComposedSchema) schema).getOneOf() != null) {
                        for (Schema sc : ((ComposedSchema) schema).getOneOf()) {
                            if (OBJECT_DATA_TYPE.equalsIgnoreCase(sc.getType())) {
                                references.addAll(addSchemaOfSchema(sc, context));
                            } else {
                                String schemaRef = sc.get$ref();
                                if (schemaRef != null) {
                                    references.add(sc.get$ref());
                                } else {
                                    processSchemaProperties(sc, context);
                                }
                            }
                        }
                    }
                    if (((ComposedSchema) schema).getAllOf() == null &&
                            ((ComposedSchema) schema).getAnyOf() == null &&
                            ((ComposedSchema) schema).getOneOf() == null) {
                        log.error("Unidentified schema. The schema is not available in the API definition.");
                    }
                }
            }

            if (ref != null) {
                addToReferenceObjectMap(ref, context);
            } else if (!references.isEmpty() && references.size() != 0) {
                for (String reference : references) {
                    if (reference != null) {
                        addToReferenceObjectMap(reference, context);
                    }
                }
            }

            processSchemaProperties(schema, context);
        }
    }

    /**
     * Process properties of a schema object of the API definition.
     *
     * @param schema  The schema object which contains the properties which needs to be processed.
     * @param context The SwaggerUpdateContext object containing the context of the API definition.
     */
    private static void processSchemaProperties(Schema schema, SwaggerUpdateContext context) {
        // Process schema properties if present
        Map properties = schema.getProperties();
        if (properties != null) {
            for (Object propertySchema : properties.values()) {
                extractReferenceFromSchema((Schema) propertySchema, context);
            }
        }
    }

    private static List<String> addSchemaOfSchema(Schema schema, SwaggerUpdateContext context) {
        List<String> references = new ArrayList<String>();
        ObjectSchema os = (ObjectSchema) schema;
        if (os.getProperties() != null) {
            for (String propertyName : os.getProperties().keySet()) {
                Schema propertySchema = os.getProperties().get(propertyName);

                if (propertySchema.get$ref() != null) {
                    references.add(propertySchema.get$ref());
                }

                if (propertySchema instanceof ComposedSchema) {
                    ComposedSchema cs = (ComposedSchema) propertySchema;
                    if (cs.getAllOf() != null) {
                        for (Schema sc : cs.getAllOf()) {
                            references.add(sc.get$ref());
                        }
                    } else if (cs.getAnyOf() != null) {
                        for (Schema sc : cs.getAnyOf()) {
                            references.add(sc.get$ref());
                        }
                    } else if (cs.getOneOf() != null) {
                        for (Schema sc : cs.getOneOf()) {
                            references.add(sc.get$ref());
                        }
                    } else {
                        log.error("Unidentified schema. The schema is not available in the API definition.");
                    }
                } else if (propertySchema instanceof ObjectSchema ||
                        propertySchema instanceof ArraySchema) {
                    extractReferenceFromSchema(propertySchema, context);
                }
            }
        }
        return references;
    }

    private static void addToReferenceObjectMap(String ref, SwaggerUpdateContext context) {
        Map<String, Set<String>> referenceObjectMap = context.getReferenceObjectMapping();
        final String category = getComponentCategory(ref);
        if (referenceObjectMap.containsKey(category)) {
            Set<String> refObjects = referenceObjectMap.get(category);
            refObjects.add(getRefKey(ref));
        }
    }

    private static String getRefKey(String ref) {
        String[] split = ref.split("/");
        return split[split.length - 1];
    }

    private static String getComponentCategory(String ref) {
        String[] remainder = ref.split(REF_PREFIX);

        if (remainder.length == 2) {
            String[] split = remainder[1].split("/");

            if (split.length == 2) {
                return split[0];
            }
        }

        return "";
    }

    public static File checkMasterSwagger(File archiveDirectory) throws APIManagementException {
        File masterSwagger = null;
        if ((new File(archiveDirectory + "/" + APIParserConstants.OPENAPI_MASTER_JSON)).exists()) {
            masterSwagger = new File(archiveDirectory + "/" + APIParserConstants.OPENAPI_MASTER_JSON);
            return masterSwagger;
        } else if ((new File(archiveDirectory + "/" + APIParserConstants.OPENAPI_MASTER_YAML)).exists()) {
            masterSwagger = new File(archiveDirectory + "/" + APIParserConstants.OPENAPI_MASTER_YAML);
            return masterSwagger;
        } else {
            throw new APIManagementException("Could not find a master swagger file with the name of swagger.json " +
                    "/swagger.yaml");
        }
    }

    /**
     * Extract the archive file and validates the openAPI definition
     *
     * @param apiDefinitionDirectory    The directory containing the API definition files, including the master Swagger file.
     * @param returnContent             Flag indicating whether to return the content of the definition in the response DTO
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse validateAPIDefinitionFromDirectory(File apiDefinitionDirectory,
            boolean returnContent) throws APIManagementException {
        File masterSwagger = checkMasterSwagger(apiDefinitionDirectory);
        String content;
        try {
            InputStream masterInputStream = new FileInputStream(masterSwagger);
            content = IOUtils.toString(masterInputStream, APIParserConstants.CHARSET);
        } catch (IOException e) {
            throw new APIManagementException("Error reading master swagger file" + e);
        }
        String openAPIContent = "";
        SwaggerVersion version;
        version = getSwaggerVersion(content);
        String filePath = masterSwagger.getAbsolutePath();
        if (SwaggerVersion.OPEN_API.equals(version)) {
            OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
            ParseOptions options = new ParseOptions();
            options.setResolve(true);
            OpenAPI openAPI = openAPIV3Parser.read(filePath, null, options);
            openAPIContent = convertOAStoJSON(openAPI);
        } else if (SwaggerVersion.SWAGGER.equals(version)) {
            SwaggerParser parser = new SwaggerParser();
            Swagger swagger = parser.read(filePath, null, true);
            try {
                openAPIContent = Yaml.pretty().writeValueAsString(swagger);
            } catch (IOException e) {
                throw new APIManagementException("Error in converting swagger to openAPI content. " + e);
            }
        }
        APIDefinitionValidationResponse apiDefinitionValidationResponse;
        apiDefinitionValidationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
        return apiDefinitionValidationResponse;
    }

    /**
     * Convert OAS definition to JSON
     *
     * @param oasDefinition
     * @return
     */
    public static String convertOAStoJSON(OpenAPI oasDefinition) {

        String jsonString = null;
        //Custom json mapper to parse OAS 3.1 definitions as the default parser drops mandatory licence.identifier field
        if (isOpenAPIVersion31(oasDefinition)) {
            ObjectMapper mapper = Json31.mapper().copy();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            //Custom Mixin for License object in OAS 3.1
            mapper.addMixIn(License.class, License31Mixin.class);
            try {
                jsonString = mapper.writeValueAsString(oasDefinition);
            } catch (JsonProcessingException e) {
                log.error("Error while converting OAS definition to JSON", e);
            }
        } else {
            jsonString = Json.pretty(oasDefinition);
        }
        return jsonString;
    }

    /**
     * Check whether the given openAPI definition is OAS 3.1
     * @param oasDefinition
     * @return
     */
    public static boolean isOpenAPIVersion31(OpenAPI oasDefinition) {
        return APIParserConstants.OAS_V31.equalsIgnoreCase(oasDefinition.getSpecVersion().name());
    }

    /**
     * Try to validate a give openAPI definition using OpenAPI 3 parser
     *
     * @param apiDefinition     definition
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        String apiDefinitionProcessed = apiDefinition;
        if (!apiDefinition.trim().startsWith("{")) {
            try {
                JsonNode jsonNode = DeserializationUtils.readYamlTree(apiDefinition, new SwaggerDeserializationResult());
                apiDefinitionProcessed = jsonNode.toString();
            } catch (IOException e) {
                throw new APIManagementException("Error while reading API definition yaml", e);
            }
        }
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        try {
            new Gson().fromJson(apiDefinitionProcessed, JsonObject.class); // Parsing the json content to validate parsing errors
            apiDefinitionProcessed = removeUnsupportedBlocksFromResources(apiDefinitionProcessed);
            if (apiDefinitionProcessed != null) {
                apiDefinition = apiDefinitionProcessed;
            }
            validationResponse = oas3Parser.validateAPIDefinition(apiDefinition, returnJsonContent);
            if (!validationResponse.isValid()) {
                for (ErrorHandler handler : validationResponse.getErrorItems()) {
                    if (ExceptionCodes.INVALID_OAS3_FOUND.getErrorCode() == handler.getErrorCode()) {
                        return tryOAS2Validation(apiDefinition, returnJsonContent);
                    }
                }
            }
        } catch (Exception e) {
            //catching a generic exception as there can be runtime exceptions when parsing happens
            addErrorToValidationResponse(validationResponse, e);
        }
        return validationResponse;
    }

    /**
     * Add error item with the thrown error message to the provided validation response object
     *
     * @param validationResponse APIDefinitionValidationResponse object
     * @param e         error object
     * @return added ErrorItem object
     */
    public static ErrorItem addErrorToValidationResponse(APIDefinitionValidationResponse validationResponse,
                                                         Exception e) {
        validationResponse.setValid(false);
        ErrorItem errorItem = new ErrorItem();
        errorItem.setErrorCode(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode());
        errorItem.setMessage(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorMessage());
        errorItem.setDescription(e.toString());
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }


    /**
     * Try to validate a give openAPI definition using OpenAPI 3 parser
     *
     * @param apiDefinition     definition
     * @param url OpenAPI definition url
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, String url ,
                                                                        boolean returnJsonContent)
            throws APIManagementException {
        String apiDefinitionProcessed = apiDefinition;
        if (!apiDefinition.trim().startsWith("{")) {
            try {
                JsonNode jsonNode = DeserializationUtils.readYamlTree(apiDefinition, new SwaggerDeserializationResult());
                apiDefinitionProcessed = jsonNode.toString();
            } catch (IOException e) {
                throw new APIManagementException("Error while reading API definition yaml", e);
            }
        }
        apiDefinitionProcessed = removeUnsupportedBlocksFromResources(apiDefinitionProcessed);
        if (apiDefinitionProcessed != null) {
            apiDefinition = apiDefinitionProcessed;
        }
        APIDefinitionValidationResponse validationResponse =
                oas3Parser.validateAPIDefinition(apiDefinition, url, returnJsonContent);
        if (!validationResponse.isValid()) {
            for (ErrorHandler handler : validationResponse.getErrorItems()) {
                if (ExceptionCodes.INVALID_OAS3_FOUND.getErrorCode() == handler.getErrorCode()) {
                    return tryOAS2Validation(apiDefinition, returnJsonContent);
                }
            }
        }
        return validationResponse;
    }
    /**
     * Try to validate a give openAPI definition using swagger parser
     *
     * @param apiDefinition     definition
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    private static APIDefinitionValidationResponse tryOAS2Validation(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse =
                oas2Parser.validateAPIDefinition(apiDefinition, returnJsonContent);
        if (!validationResponse.isValid()) {
            for (ErrorHandler handler : validationResponse.getErrorItems()) {
                if (ExceptionCodes.INVALID_OAS2_FOUND.getErrorCode() == handler.getErrorCode()) {
                    addErrorToValidationResponse(validationResponse, "attribute swagger or openapi should present");
                    return validationResponse;
                }
            }
        }
        return validationResponse;
    }

    /**
     * Update the APIDefinitionValidationResponse object with success state using the values given
     *
     * @param validationResponse    APIDefinitionValidationResponse object to be updated
     * @param originalAPIDefinition original API Definition
     * @param openAPIVersion        version of OpenAPI Spec (2.0 or 3.0.0)
     * @param title                 title of the OpenAPI Definition
     * @param version               version of the OpenAPI Definition
     * @param context               base path of the OpenAPI Definition
     * @param description           description of the OpenAPI Definition
     */
    public static void updateValidationResponseAsSuccess(APIDefinitionValidationResponse validationResponse,
                                                         String originalAPIDefinition, String openAPIVersion, String title, String version, String context,
                                                         String description, List<String> endpoints) {
        validationResponse.setValid(true);
        validationResponse.setContent(originalAPIDefinition);
        APIDefinitionValidationResponse.Info info = new APIDefinitionValidationResponse.Info();
        info.setOpenAPIVersion(openAPIVersion);
        info.setName(title);
        info.setVersion(version);
        info.setContext(context);
        info.setDescription(description);
        info.setEndpoints(endpoints);
        validationResponse.setInfo(info);
    }

    /**
     * Add error item with the provided message to the provided validation response object
     *
     * @param validationResponse APIDefinitionValidationResponse object
     * @param errMessage         error message
     * @return added ErrorItem object
     */
    public static ErrorItem addErrorToValidationResponse(APIDefinitionValidationResponse validationResponse,
                                                         String errMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setErrorCode(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorCode());
        errorItem.setMessage(ExceptionCodes.OPENAPI_PARSE_EXCEPTION.getErrorMessage());
        errorItem.setDescription(errMessage);
        validationResponse.getErrorItems().add(errorItem);
        return errorItem;
    }

    /**
     * This method validates the given OpenAPI definition by URL
     *
     * @param url               URL of the API definition
     * @param returnJsonContent whether to return the converted json form of the
     * @return APIDefinitionValidationResponse object with validation information
     */
    public static APIDefinitionValidationResponse validateAPIDefinitionByURL(String url, HttpClient httpClient,
                                                                             boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                String responseStrProcessed = responseStr;
                if (!responseStr.trim().startsWith("{")) {
                    try {
                        JsonNode jsonNode = DeserializationUtils.readYamlTree(responseStr, new SwaggerDeserializationResult());
                        responseStrProcessed = jsonNode.toString();
                    } catch (IOException e) {
                        throw new APIManagementException("Error while reading API definition yaml", e);
                    }
                }
                responseStrProcessed = removeUnsupportedBlocksFromResources(responseStrProcessed);
                if (responseStrProcessed != null) {
                    responseStr = responseStrProcessed;
                }
                validationResponse = validateAPIDefinition(responseStr, new URL(url).getHost(), returnJsonContent);
            } else {
                validationResponse.setValid(false);
                validationResponse.getErrorItems().add(ExceptionCodes.OPENAPI_URL_NO_200);
            }
        } catch (IOException e) {
            ErrorHandler errorHandler = ExceptionCodes.OPENAPI_URL_MALFORMED;
            //Log the error and continue since this method is only intended to validate a definition
            log.error(errorHandler.getErrorDescription(), e);

            validationResponse.setValid(false);
            validationResponse.getErrorItems().add(errorHandler);
        }
        return validationResponse;
    }

    /**
     * Sets the scopes to the URL template object using the given list of scopes
     *
     * @param template URL template
     * @param resourceScopes   list of scopes of the resource
     * @param apiScopes set of scopes defined for the API
     * @return URL template after setting the scopes
     */
    public static URITemplate setScopesToTemplate(URITemplate template, List<String> resourceScopes,
                                                  Set<Scope> apiScopes) throws APIManagementException {

        for (String scopeName : resourceScopes) {
            if (StringUtils.isNotBlank(scopeName)) {
                Scope scope = findScopeByKey(apiScopes, scopeName);
                if (scope == null) {
                    throw new APIManagementException("Resource Scope '" + scopeName + "' not found.");
                }
                template.setScopes(scope);
            }
        }
        return template;
    }

    /**
     * Find scope object in a set based on the key
     *
     * @param scopes - Set of scopes
     * @param key    - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes, String key) {

        for (Scope scope : scopes) {
            if (scope.getKey().equals(key)) {
                return scope;
            }
        }
        return null;
    }

    /**
     * generate endpoint information for OAS definition
     *
     * @param api          API
     * @param isProduction is production endpoints
     * @return JsonNode
     */
    public static JsonNode generateOASConfigForEndpoints(API api, boolean isProduction)
            throws APIManagementException {
        if (api.getEndpointConfig() == null || api.getEndpointConfig().trim().isEmpty()) {
            return null;
        }
        JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
        if (endpointConfig.has(APIParserConstants.IMPLEMENTATION_STATUS)) {
            // no need to populate if it is prototype API
            return null;
        }
        ObjectNode endpointResult;
        String type = endpointConfig.getString(APIParserConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE);
        if (APIParserConstants.ENDPOINT_TYPE_DEFAULT.equalsIgnoreCase(type)) {
            endpointResult = objectMapper.createObjectNode();
            endpointResult.put(APIParserConstants.X_WSO2_ENDPOINT_TYPE, APIParserConstants.ENDPOINT_TYPE_DEFAULT);
        } else if (APIParserConstants.ENDPOINT_TYPE_FAILOVER.equalsIgnoreCase(type)) {
            endpointResult = populateFailoverConfig(endpointConfig, isProduction);
        } else if (APIParserConstants.ENDPOINT_TYPE_LOADBALANCE.equalsIgnoreCase(type)) {
            endpointResult = populateLoadBalanceConfig(endpointConfig, isProduction);
        } else if (APIParserConstants.ENDPOINT_TYPE_HTTP.equalsIgnoreCase(type)) {
            endpointResult = setPrimaryConfig(endpointConfig, isProduction, APIParserConstants.ENDPOINT_TYPE_HTTP);
        } else if (APIParserConstants.ENDPOINT_TYPE_SERVICE.equalsIgnoreCase(type)) {
            endpointResult = setPrimaryConfig(endpointConfig, isProduction, APIParserConstants.ENDPOINT_TYPE_SERVICE);
        } else if (APIParserConstants.ENDPOINT_TYPE_ADDRESS.equalsIgnoreCase(type)) {
            endpointResult = setPrimaryConfig(endpointConfig, isProduction, APIParserConstants.ENDPOINT_TYPE_ADDRESS);
        } else {
            return null;
        }
        if (endpointResult != null) {
            populateEndpointSecurity(api, endpointResult);
        }
        return endpointResult;
    }

    private static void populateEndpointSecurity(API api, ObjectNode endpointResult) {
        if (api.isEndpointSecured()) {
            ObjectNode securityConfigObj = objectMapper.createObjectNode();
            if (api.isEndpointAuthDigest()) {
                securityConfigObj.put(APIParserConstants.ENDPOINT_SECURITY_TYPE,
                        APIParserConstants.ENDPOINT_SECURITY_TYPE_DIGEST.toUpperCase());
            } else {
                securityConfigObj.put(APIParserConstants.ENDPOINT_SECURITY_TYPE,
                        APIParserConstants.ENDPOINT_SECURITY_TYPE_BASIC.toUpperCase());
            }
            if (!StringUtils.isEmpty(api.getEndpointUTUsername())) {
                securityConfigObj.put(APIParserConstants.ENDPOINT_SECURITY_USERNAME, api.getEndpointUTUsername());
            }
            endpointResult.set(APIParserConstants.ENDPOINT_SECURITY_CONFIG, securityConfigObj);
        }
    }

    /**
     * Set failover configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static ObjectNode populateFailoverConfig(JSONObject endpointConfig, boolean isProd)
            throws APIManagementException {
        JSONArray endpointsURLs = null;
        JSONObject primaryEndpoints = null;
        if (isProd) {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_PRODUCTION_FAILOVERS)) {
                endpointsURLs = endpointConfig.getJSONArray(APIParserConstants.ENDPOINT_PRODUCTION_FAILOVERS);
            }
            if (endpointConfig.has(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_SANDBOX_FAILOVERS)) {
                endpointsURLs = endpointConfig.getJSONArray(APIParserConstants.ENDPOINT_SANDBOX_FAILOVERS);
            }
            if (endpointConfig.has(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (primaryEndpoints != null && primaryEndpoints.has(APIParserConstants.ENDPOINT_URL)) {
            endpointsArray.add(primaryEndpoints.getString(APIParserConstants.ENDPOINT_URL));
        }
        if (endpointsURLs != null) {
            for (int i = 0; i < endpointsURLs.length(); i++) {
                JSONObject obj = endpointsURLs.getJSONObject(i);
                endpointsArray.add(obj.getString(APIParserConstants.ENDPOINT_URL));
            }
        }
        if (endpointsArray.size() < 1) {
            return null;
        }
        ObjectNode endpointResult = objectMapper.createObjectNode();
        endpointResult.set(APIParserConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIParserConstants.X_WSO2_ENDPOINT_TYPE, APIParserConstants.ENDPOINT_TYPE_FAILOVER);
        return updateEndpointResult(primaryEndpoints, endpointResult);
    }

    /**
     * Set load balance configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static ObjectNode populateLoadBalanceConfig(JSONObject endpointConfig, boolean isProd)
            throws APIManagementException {
        JSONArray primaryProdEndpoints = new JSONArray();
        if (isProd) {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS) && endpointConfig
                    .get(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS) instanceof JSONArray) {
                primaryProdEndpoints = endpointConfig.getJSONArray(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS) && endpointConfig
                    .get(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS) instanceof JSONArray) {
                primaryProdEndpoints = endpointConfig.getJSONArray(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (primaryProdEndpoints != null) {
            for (int i = 0; i < primaryProdEndpoints.length(); i++) {
                JSONObject obj = primaryProdEndpoints.getJSONObject(i);
                endpointsArray.add(obj.getString(APIParserConstants.ENDPOINT_URL));
            }
        }
        if (endpointsArray.size() < 1) {
            return null;
        }
        ObjectNode endpointResult = objectMapper.createObjectNode();
        endpointResult.set(APIParserConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIParserConstants.X_WSO2_ENDPOINT_TYPE, APIParserConstants.ENDPOINT_TYPE_LOADBALANCE);

        if (primaryProdEndpoints != null) {
            for (int i = 0; i < primaryProdEndpoints.length(); i++) {
                if (primaryProdEndpoints.getJSONObject(i).has(APIParserConstants.ADVANCE_ENDPOINT_CONFIG)) {
                    return updateEndpointResult(primaryProdEndpoints.getJSONObject(i), endpointResult);
                }
            }
        }

        return endpointResult;
    }

    /**
     * Set baisc configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     * @param type           endpoint type
     */
    private static ObjectNode setPrimaryConfig(JSONObject endpointConfig, boolean isProd, String type)
            throws APIManagementException {
        JSONObject primaryEndpoints = new JSONObject();
        if (isProd) {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIParserConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIParserConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }
        if (primaryEndpoints != null && primaryEndpoints.has(APIParserConstants.ENDPOINT_URL)) {
            ArrayNode endpointsArray = objectMapper.createArrayNode();
            endpointsArray.add(primaryEndpoints.getString(APIParserConstants.ENDPOINT_URL));
            ObjectNode endpointResult = objectMapper.createObjectNode();
            endpointResult.set(APIParserConstants.ENDPOINT_URLS, endpointsArray);
            endpointResult.put(APIParserConstants.X_WSO2_ENDPOINT_TYPE, type);
            return updateEndpointResult(primaryEndpoints, endpointResult);
        }
        return null;
    }

    /**
     * Add advance configuration to the endpointResult object
     *
     * @param primaryEndpoints production and sandbox endpoint configuration Json object
     * @param endpointResult         endpoint result ObjectNode
     */
    private static ObjectNode updateEndpointResult(JSONObject primaryEndpoints, ObjectNode endpointResult)
            throws APIManagementException {
        if (primaryEndpoints.has(APIParserConstants.ADVANCE_ENDPOINT_CONFIG)) {
            try {
                endpointResult.put(APIParserConstants.ADVANCE_ENDPOINT_CONFIG, objectMapper
                        .readTree(primaryEndpoints.get(APIParserConstants.ADVANCE_ENDPOINT_CONFIG).toString()));
            } catch (JsonProcessingException e) {
                throw new APIManagementException(
                        "Error while setting the advance endpoint configs ", e);
            }
        } else {
            //When user removes existing advancedConfigurations section.Returns null if key was not an existing
            endpointResult.remove(APIParserConstants.ADVANCE_ENDPOINT_CONFIG);
        }
        return endpointResult;
    }

    /**
     * remove publisher/MG related extension from OAS
     *
     * @param extensions extensions
     */
    public static void removePublisherSpecificInfo(Map<String, Object> extensions) {
        if (extensions == null) {
            return;
        }
        extensions.remove(APIParserConstants.X_WSO2_CORS);
        extensions.remove(APIParserConstants.X_WSO2_AUTH_HEADER);
        extensions.remove(APIParserConstants.X_WSO2_THROTTLING_TIER);
        extensions.remove(APIParserConstants.X_THROTTLING_TIER);
        extensions.remove(APIParserConstants.X_WSO2_PRODUCTION_ENDPOINTS);
        extensions.remove(APIParserConstants.X_WSO2_SANDBOX_ENDPOINTS);
        extensions.remove(APIParserConstants.X_WSO2_BASEPATH);
        extensions.remove(APIParserConstants.X_WSO2_TRANSPORTS);
        extensions.remove(APIParserConstants.X_WSO2_APP_SECURITY);
        extensions.remove(APIParserConstants.X_WSO2_RESPONSE_CACHE);
        extensions.remove(APIParserConstants.X_WSO2_MUTUAL_SSL);
    }

    /**
     * remove publisher/MG related extension from OAS
     *
     * @param extensions extensions
     */
    public static void removePublisherSpecificInfofromOperation(Map<String, Object> extensions) {
        if (extensions == null) {
            return;
        }
        extensions.remove(APIParserConstants.X_WSO2_APP_SECURITY);
        extensions.remove(APIParserConstants.X_WSO2_SANDBOX_ENDPOINTS);
        extensions.remove(APIParserConstants.X_WSO2_PRODUCTION_ENDPOINTS);
        extensions.remove(APIParserConstants.X_WSO2_DISABLE_SECURITY);
        extensions.remove(APIParserConstants.X_WSO2_THROTTLING_TIER);
    }

    /**
     * Get Application level security types
     *
     * @param security list of security types
     * @return List of api security
     */
    private static List<String> getAPISecurity(List<String> security) {
        List<String> apiSecurityList = new ArrayList<>();
        for (String securityType : security) {
            if (APIParserConstants.APPLICATION_LEVEL_SECURITY.contains(securityType)) {
                apiSecurityList.add(securityType);
            }
        }
        return apiSecurityList;
    }

    /**
     * generate app security information for OAS definition
     *
     * @param security application security
     * @return JsonNode
     */
    static JsonNode getAppSecurity(String security) {
        List<String> appSecurityList = new ArrayList<>();
        ObjectNode endpointResult = objectMapper.createObjectNode();
        boolean appSecurityOptional = false;
        if (security != null) {
            List<String> securityList = Arrays.asList(security.split(","));
            appSecurityList = getAPISecurity(securityList);
            appSecurityOptional = !securityList.contains(APIParserConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
        }
        ArrayNode appSecurityTypes = objectMapper.valueToTree(appSecurityList);
        endpointResult.set(APIParserConstants.WSO2_APP_SECURITY_TYPES, appSecurityTypes);
        endpointResult.put(APIParserConstants.OPTIONAL, appSecurityOptional);
        return endpointResult;
    }

    /**
     * generate response cache configuration for OAS definition.
     *
     * @param responseCache response cache Enabled/Disabled
     * @param cacheTimeout  cache timeout in seconds
     * @return JsonNode
     */
    static JsonNode getResponseCacheConfig(String responseCache, int cacheTimeout) {
        ObjectNode responseCacheConfig = objectMapper.createObjectNode();
        boolean enabled = APIParserConstants.ENABLED.equalsIgnoreCase(responseCache);
        responseCacheConfig.put(APIParserConstants.RESPONSE_CACHING_ENABLED, enabled);
        responseCacheConfig.put(APIParserConstants.RESPONSE_CACHING_TIMEOUT, cacheTimeout);
        return responseCacheConfig;
    }

    /**
     * Sort scopes by name.
     * This method was added to display scopes in publisher in a sorted manner.
     *
     * @param scopeSet
     * @return Scope set
     */
    static Set<Scope> sortScopes(Set<Scope> scopeSet) {
        List<Scope> scopesSortedlist = new ArrayList<>(scopeSet);
        scopesSortedlist.sort(Comparator.comparing(Scope::getKey));
        return new LinkedHashSet<>(scopesSortedlist);
    }

    /**
     * Preprocessing of scopes schemes to support multiple schemes other than 'default' type
     * This method will change the given definition
     *
     * @param swaggerContent String
     * @return swagger definition as String
     */
    public static String preProcess(String swaggerContent) throws APIManagementException {
        //Load required properties from swagger to the API
        APIDefinition apiDefinition = getOASParser(swaggerContent);
        //Inject and map mgw throttling extensions to default type
        swaggerContent = apiDefinition.injectMgwThrottlingExtensionsToDefault(swaggerContent);
        //Process mgw disable security extension
        swaggerContent = apiDefinition.processDisableSecurityExtension(swaggerContent);
        return apiDefinition.processOtherSchemeScopes(swaggerContent);
    }

    /**
     * This method returns api that is attached with api extensions related to micro-gw
     *
     * @param swaggerContent String
     * @param api            API
     * @return API
     */
    public static API setExtensionsToAPI(String swaggerContent, API api) throws APIManagementException {
        APIDefinition apiDefinition = getOASParser(swaggerContent);
        return apiDefinition.setExtensionsToAPI(swaggerContent, api);
    }

    /**
     * This method returns extension of throttling tier related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return throttling tier as String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getThrottleTierFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object throttleTier = extensions.get(APIParserConstants.X_WSO2_THROTTLING_TIER);
        return throttleTier == null ? null : throttleTier.toString();
    }

    /**
     * This method returns extension of transports(http,https) related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return transport type as String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getTransportsFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        String transports = null;
        ObjectMapper mapper = new ObjectMapper();
        if (extensions.containsKey(APIParserConstants.X_WSO2_TRANSPORTS)) {
            Object object = extensions.get(APIParserConstants.X_WSO2_TRANSPORTS).toString();
            transports = mapper.convertValue(object, String.class);
            transports = transports.replace("[", "");
            transports = transports.replace("]", "");
            transports = transports.replace(" ", "");
        }
        return transports;
    }

    /**
     * This method returns extension of mutualSSL related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return mutualSSL value as String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getMutualSSLEnabledFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object mutualSSl = extensions.get(APIParserConstants.X_WSO2_MUTUAL_SSL);
        return mutualSSl == null ? null : mutualSSl.toString();
    }

    /**
     * This method returns extension of CORS config related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return CORSConfiguration object with configurations
     * @throws APIManagementException throws if an error occurred
     */
    public static CORSConfiguration getCorsConfigFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        boolean corsConfigurationEnabled = false;
        boolean accessControlAllowCredentials = false;
        List<String> accessControlAllowOrigins = new ArrayList<>();
        List<String> accessControlAllowHeaders = new ArrayList<>();
        List<String> accessControlAllowMethods = new ArrayList<>();
        CORSConfiguration corsConfig = new CORSConfiguration(corsConfigurationEnabled,
                accessControlAllowOrigins, accessControlAllowCredentials, accessControlAllowHeaders,
                accessControlAllowMethods);
        ObjectMapper mapper = new ObjectMapper();

        if (extensions.containsKey(APIParserConstants.X_WSO2_CORS)) {
            Object corsConfigObject = extensions.get(APIParserConstants.X_WSO2_CORS);
            JsonNode objectNode = mapper.convertValue(corsConfigObject, JsonNode.class);
            corsConfigurationEnabled = Boolean.parseBoolean(String.valueOf(objectNode.get("corsConfigurationEnabled")));
            accessControlAllowCredentials = Boolean.parseBoolean(String.valueOf(objectNode.get("accessControlAllowCredentials")));
            accessControlAllowHeaders = mapper.convertValue(objectNode.get("accessControlAllowHeaders"), ArrayList.class);
            accessControlAllowOrigins = mapper.convertValue(objectNode.get("accessControlAllowOrigins"), ArrayList.class);
            accessControlAllowMethods = mapper.convertValue(objectNode.get("accessControlAllowMethods"), ArrayList.class);
            corsConfig.setCorsConfigurationEnabled(corsConfigurationEnabled);
            corsConfig.setAccessControlAllowCredentials(accessControlAllowCredentials);
            corsConfig.setAccessControlAllowHeaders(accessControlAllowHeaders);
            corsConfig.setAccessControlAllowOrigins(accessControlAllowOrigins);
            corsConfig.setAccessControlAllowMethods(accessControlAllowMethods);
        }
        return corsConfig;
    }

    /**
     * This method returns extension of responseCache enabling check related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return response cache enable or disable as boolean
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getResponseCacheFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        boolean responseCache = false;
        if (extensions.containsKey(APIParserConstants.X_WSO2_RESPONSE_CACHE)) {
            Object responseCacheConfig = extensions.get(APIParserConstants.X_WSO2_RESPONSE_CACHE);
            ObjectNode cacheConfigNode = mapper.convertValue(responseCacheConfig, ObjectNode.class);
            responseCache = Boolean.parseBoolean(String.valueOf(cacheConfigNode.get(APIParserConstants.RESPONSE_CACHING_ENABLED)));
        }
        return responseCache;
    }

    /**
     * This method returns extension of cache timeout related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return cache timeout value as int
     * @throws APIManagementException throws if an error occurred
     */
    public static int getCacheTimeOutFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        int timeOut = 0;
        if (extensions.containsKey(APIParserConstants.X_WSO2_RESPONSE_CACHE)) {
            Object responseCacheConfig = extensions.get(APIParserConstants.X_WSO2_RESPONSE_CACHE);
            ObjectNode cacheConfigNode = mapper.convertValue(responseCacheConfig, ObjectNode.class);
            timeOut = Integer.parseInt(String.valueOf(cacheConfigNode.get(APIParserConstants.RESPONSE_CACHING_TIMEOUT)));
        }
        return timeOut;
    }

    /**
     * This method returns extension of custom authorization Header related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return authorization header value as String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getAuthorizationHeaderFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object authorizationHeader = extensions.get(APIParserConstants.X_WSO2_AUTH_HEADER);
        return authorizationHeader == null ? null : authorizationHeader.toString();
    }

    /**
     * This method returns extension of custom API key Header related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return API key header header value as String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getApiKeyHeaderFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object apiKeyHeader = extensions.get(APIParserConstants.X_WSO2_API_KEY_HEADER);
        return apiKeyHeader == null ? null : apiKeyHeader.toString();
    }

    /**
     * This method returns extension of custom authorization Header related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return security disable or enable value as String
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getDisableSecurity(Map<String, Object> extensions) throws APIManagementException {
        boolean disableSecurity = false;
        if (extensions.containsKey(APIParserConstants.X_WSO2_DISABLE_SECURITY)) {
            disableSecurity = Boolean.parseBoolean(String.valueOf(extensions.get(APIParserConstants.X_WSO2_DISABLE_SECURITY)));
        }
        return disableSecurity;
    }

    /**
     * This method returns extension of application security types related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return application security types as String
     * @throws APIManagementException throws if an error occurred
     */
    public static List<String> getApplicationSecurityTypes(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> appSecurityTypes = new ArrayList<>();
        if (extensions.containsKey(APIParserConstants.X_WSO2_APP_SECURITY)) {
            Object applicationSecurityTypes = extensions.get(APIParserConstants.X_WSO2_APP_SECURITY);
            ObjectNode appSecurityTypesNode = mapper.convertValue(applicationSecurityTypes, ObjectNode.class);
            appSecurityTypes = mapper.convertValue(appSecurityTypesNode.get("security-types"), ArrayList.class);
        }
        return appSecurityTypes;
    }

    /**
     * This method returns extension of application security types state related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return application security state as boolean
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getAppSecurityStateFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        boolean appSecurityState = false;
        if (extensions.containsKey(APIParserConstants.X_WSO2_APP_SECURITY)) {
            Object applicationSecurityTypes = extensions.get(APIParserConstants.X_WSO2_APP_SECURITY);
            ObjectNode appSecurityTypesNode = mapper.convertValue(applicationSecurityTypes, ObjectNode.class);
            appSecurityState = Boolean.parseBoolean(String.valueOf(appSecurityTypesNode.get("optional")));
        }
        return appSecurityState;
    }

    public static void copyOperationVendorExtensions(Map<String, Object> existingExtensions,
                                                     Map<String, Object> updatedVendorExtensions) {
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_AUTH_TYPE) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_AUTH_TYPE, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_AUTH_TYPE));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_THROTTLING_TIER) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_THROTTLING_TIER, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_THROTTLING_TIER));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_THROTTLING_BANDWIDTH) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_THROTTLING_BANDWIDTH, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_THROTTLING_BANDWIDTH));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_MEDIATION_SCRIPT) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_MEDIATION_SCRIPT, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_MEDIATION_SCRIPT));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_WSO2_SECURITY) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_WSO2_SECURITY, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_WSO2_SECURITY));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_SCOPE) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_SCOPE, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_SCOPE));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_NAME));
        }
        if (existingExtensions.get(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT) != null) {
            updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT, existingExtensions
                    .get(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_TIMEOUT));
        }
        if (existingExtensions.get(APIParserConstants.X_WSO2_APP_SECURITY) != null) {
            updatedVendorExtensions.put(APIParserConstants.X_WSO2_APP_SECURITY, existingExtensions
                    .get(APIParserConstants.X_WSO2_APP_SECURITY));
        }
        updatedVendorExtensions.put(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED, existingExtensions
                .get(APIParserConstants.SWAGGER_X_AMZN_RESOURCE_CONTNET_ENCODED));
    }

    /**
     * This method removes the unsupported json blocks from the given json string.
     *
     * @param jsonString Open api specification from which unsupported blocks must be removed.
     * @return String open api specification without unsupported blocks. Null value if there is no unsupported blocks.
     */
    public static String removeUnsupportedBlocksFromResources(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        boolean definitionUpdated = false;
        if (jsonObject.has(OPENAPI_RESOURCE_KEY)) {
            JSONObject paths = jsonObject.optJSONObject(OPENAPI_RESOURCE_KEY);
            if (paths != null ) {
                for (String unsupportedBlockKey : UNSUPPORTED_RESOURCE_BLOCKS) {
                    boolean result = removeBlocksRecursivelyFromJsonObject(unsupportedBlockKey, paths, false);
                    definitionUpdated = definitionUpdated  || result;
                }
            }
        }
        if (definitionUpdated) {
            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            try {
                Map<String, Object> map = om.readValue(jsonObject.toString(), HashMap.class);
                String json = om.writeValueAsString(map);
                return json;
            } catch (JsonProcessingException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * This method removes provided key from the json object recursively.
     *
     * @param keyToBeRemoved, Key to remove from open api spec.
     * @param jsonObject, Open api spec as json object.
     */
    private static boolean removeBlocksRecursivelyFromJsonObject(String keyToBeRemoved, JSONObject jsonObject, boolean definitionUpdated) {
        if (jsonObject == null) {
            return definitionUpdated;
        }
        if (jsonObject.has(keyToBeRemoved)) {
            jsonObject.remove(keyToBeRemoved);
            definitionUpdated = true;
        }
        for (Object key : jsonObject.keySet()) {
            JSONObject subObj = jsonObject.optJSONObject(key.toString());
            if (subObj != null) {
                boolean result = removeBlocksRecursivelyFromJsonObject(keyToBeRemoved, subObj, definitionUpdated);
                definitionUpdated = definitionUpdated || result;
            }
        }
        return definitionUpdated;
    }

    /**
     * This method will set the scopes defined in the API to the security scheme in swagger3.
     *
     * @param swaggerData    SwaggerData object which contains the API data.
     * @param securityScheme SecurityScheme object which contains the security scheme.
     */
    public static void setScopesFromAPIToSecurityScheme(SwaggerData swaggerData, SecurityScheme securityScheme) {

        Map<String, String> scopeBindings = new LinkedHashMap<>();
        Scopes oas3Scopes = new Scopes();
        Set<Scope> scopes = swaggerData.getScopes(); // Get the scopes defined in the API.
        if (scopes != null && !scopes.isEmpty()) {  // If scopes defined, add them to the OAS definition.
            populateScopesFromAPI(scopes, oas3Scopes, scopeBindings);
            // replace the scope bindings if the scopes are not empty.
            if (SecurityScheme.Type.OAUTH2.toString().equals(securityScheme.getType().toString())) {
                securityScheme.getFlows().getImplicit()
                        .addExtension(APIParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            } else if (SecurityScheme.Type.HTTP.toString().equals(securityScheme.getType().toString()) &&
                    APIParserConstants.SWAGGER_API_SECURITY_BASIC_AUTH_TYPE.equals(securityScheme.getScheme())) {
                securityScheme.addExtension(APIParserConstants.SWAGGER_X_BASIC_AUTH_SCOPES, oas3Scopes);
                securityScheme.addExtension(APIParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            }
        }
        if (SecurityScheme.Type.OAUTH2.toString().equals(securityScheme.getType().toString())) {
            securityScheme.getFlows().getImplicit().setScopes(oas3Scopes);
        }
    }

    /**
     * This method will set the scopes defined in the API to the security scheme in swagger2.
     *
     * @param swaggerData              SwaggerData object which contains the API data.
     * @param securitySchemeDefinition SecuritySchemeDefinition object which contains the security scheme.
     */
    public static void setScopesFromAPIToSecurityScheme(SwaggerData swaggerData,
                                                        SecuritySchemeDefinition securitySchemeDefinition) {

        Map<String, String> swaggerScopes = new LinkedHashMap<>();
        Map<String, String> scopeBindings = new LinkedHashMap<>();
        Set<Scope> scopes = swaggerData.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            populateScopesFromAPI(scopes, swaggerScopes, scopeBindings);
            if (StringUtils.equals(APIParserConstants.DEFAULT_API_SECURITY_OAUTH2, securitySchemeDefinition.getType())) {
                securitySchemeDefinition.setVendorExtension(APIParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            } else if (StringUtils.equals(APIParserConstants.SWAGGER_API_SECURITY_BASIC_AUTH_TYPE,
                    securitySchemeDefinition.getType())) {
                securitySchemeDefinition.setVendorExtension(APIParserConstants.SWAGGER_X_BASIC_AUTH_SCOPES, swaggerScopes);
                securitySchemeDefinition.setVendorExtension(APIParserConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
            }
        }
        if (StringUtils.equals(APIParserConstants.DEFAULT_API_SECURITY_OAUTH2, securitySchemeDefinition.getType())) {
            ((OAuth2Definition) securitySchemeDefinition).setScopes(swaggerScopes);
        }
    }

    private static void populateScopesFromAPI(Set<Scope> apiScopes, Map<String, String> scopes,
                                              Map<String, String> scopeBindings) {

        if (apiScopes != null && !apiScopes.isEmpty()) {
            apiScopes.forEach(scope -> {
                String description = scope.getDescription() != null ? scope.getDescription() : StringUtils.EMPTY;
                scopes.put(scope.getKey(), description);
                // If roles are defined for the scope, add them to the scope bindings.
                String roles = (StringUtils.isNotBlank(scope.getRoles())
                        && scope.getRoles().trim().split(",").length > 0) ? scope.getRoles() : StringUtils.EMPTY;
                scopeBindings.put(scope.getKey(), roles);
            });
        }
    }

    /**
     * Add security requirement to swagger2.
     *
     * @param swagger         Swagger2 object
     * @param securityReqName SecurityRequirement name (Eg: default, basic_auth etc).
     */
    public static void addSecurityRequirementToSwagger(Swagger swagger, String securityReqName) {

        io.swagger.models.SecurityRequirement securityRequirement = new io.swagger.models.SecurityRequirement();
        securityRequirement.setRequirements(securityReqName, new ArrayList<>());
        if (swagger.getSecurity() == null || !swagger.getSecurity().contains(securityRequirement)) {
            swagger.addSecurity(securityRequirement);
        }
    }

    /**
     * Add security requirement to OAS definition.
     *
     * @param openAPI         OAS Definition object
     * @param securityReqName SecurityRequirement name (Eg: default, basic_auth etc).
     */
    public static void addSecurityRequirementToSwagger(OpenAPI openAPI, String securityReqName) {

        SecurityRequirement secReq = new SecurityRequirement();
        secReq.addList(securityReqName, new ArrayList<>());
        openAPI.addSecurityItem(secReq);
    }

    /**
     * Add operation level security requirements from the API to OAS definition.
     *
     * @param operationSecurities Existing operation level security requirements
     * @param apiSecurities       Security defined for API
     * @param securityReqName     Specific security name (Eg: basic_auth, default etc)
     * @param operationScopes     Operation specific scopes for the security requirement
     */
    public static void addOASOperationSecurityReqFromAPI(List<SecurityRequirement> operationSecurities,
                                                         List<String> apiSecurities, String securityReqName,
                                                         List<String> operationScopes) {

        if (apiSecurities.contains(securityReqName)) {
            boolean isSecurityExists = operationSecurities.stream().anyMatch(
                    securityRequirement -> securityRequirement.containsKey(securityReqName));
            if (!isSecurityExists) {
                SecurityRequirement securityRequirement = new SecurityRequirement();
                securityRequirement.addList(securityReqName, operationScopes);
                operationSecurities.add(securityRequirement);
            } else {
                operationSecurities.stream().filter
                                (securityRequirement -> securityRequirement.containsKey(securityReqName))
                        .findFirst().ifPresent(securityRequirement -> securityRequirement
                                .addList(securityReqName, operationScopes));
            }
        }
    }

    /**
     * Set Basic Auth Scopes for API resources in OAS definition.
     *
     * @param operationScopes Operation specific scopes for the security requirement
     * @param apiSecurities   Security defined for API
     * @param operation       Existing operation
     */
    public static void addOASBasicAuthResourceScopesFromAPI(List<String> operationScopes, List<String> apiSecurities,
                                                            Operation operation) {

        if (!operationScopes.isEmpty() && apiSecurities.contains(APIParserConstants.API_SECURITY_BASIC_AUTH)) {
            operation.addExtension(APIParserConstants.SWAGGER_X_BASIC_AUTH_RESOURCE_SCOPES, operationScopes);
        }
    }

    /**
     * Add operation level security requirements from the API to Swagger2.
     *
     * @param operationSecurities Existing operation level security requirements
     * @param apiSecurities       Security defined for API
     * @param securityReqName     Specific security name (Eg: basic_auth, default etc)
     * @param operationScopes     Operation specific scopes for the security requirement
     */
    public static void addSwaggerOperationSecurityReqFromAPI(List<Map<String, List<String>>> operationSecurities,
                                                             List<String> apiSecurities, String securityReqName,
                                                             List<String> operationScopes) {

        if (apiSecurities.contains(securityReqName)) {
            // If security requirement is set for the API.
            boolean isSecurityExists = operationSecurities.stream().anyMatch(
                    securityRequirement -> securityRequirement.containsKey(securityReqName));
            if (!isSecurityExists) {
                // If security not defined in the swagger definition, add new.
                Map<String, List<String>> securityRequirement = new HashMap<>();
                securityRequirement.put(securityReqName, operationScopes);
                operationSecurities.add(securityRequirement);
            } else {
                // If security already defined in the swagger definition, update the scope list.
                operationSecurities.stream().filter
                                (securityRequirement -> securityRequirement.containsKey(securityReqName))
                        .findFirst().ifPresent(securityRequirement -> securityRequirement
                                .put(securityReqName, operationScopes));
            }
        }
    }

    /**
     * Set Basic Auth Scopes for API resources in Swagger2 definition.
     *
     * @param operationScopes Operation specific scopes for the security requirement
     * @param apiSecurities   Security defined for API
     * @param operation       Existing operation
     */
    public static void addSwaggerBasicAuthResourceScopesFromAPI(List<String> operationScopes,
                                                                List<String> apiSecurities,
                                                                io.swagger.models.Operation operation) {

        if (!operationScopes.isEmpty() && apiSecurities.contains(APIParserConstants.API_SECURITY_BASIC_AUTH)) {
            operation.setVendorExtension(APIParserConstants.SWAGGER_X_BASIC_AUTH_RESOURCE_SCOPES, operationScopes);
        }
    }

    /**
     * This method will validate the OAS definition against the resource paths with trailing slashes.
     *
     * @param openAPI            OpenAPI object
     * @param swagger         Swagger object
     * @param validationResponse validation response
     * @return isSwaggerValid boolean
     */
    public static boolean isValidWithPathsWithTrailingSlashes(OpenAPI openAPI, Swagger swagger,
                                                              APIDefinitionValidationResponse validationResponse) {
        Map<String, ?> pathItems = null;
        if (openAPI != null) {
            pathItems = openAPI.getPaths();
        } else if (swagger != null) {
            pathItems = swagger.getPaths();
        }
        if (pathItems != null) {
            for (String path : pathItems.keySet()) {
                if (path.endsWith("/")) {
                    String newPath = path.substring(0, path.length() - 1);
                    if (pathItems.containsKey(newPath)) {
                        Object pathItem = pathItems.get(newPath);
                        Object newPathItem = pathItems.get(path);

                        if (pathItem instanceof PathItem && newPathItem instanceof PathItem) {
                            if (!validateOAS3Paths((PathItem) pathItem, (PathItem) newPathItem, newPath, validationResponse)) {
                                return false;
                            }
                        } else if (pathItem instanceof Path && newPathItem instanceof Path) {
                            if (!validateOAS2Paths((Path) pathItem, (Path) newPathItem, newPath, validationResponse)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean validateOAS3Paths(PathItem pathItem, PathItem newPathItem, String newPath,
                                             APIDefinitionValidationResponse validationResponse) {
        if (pathItem.getGet() != null && newPathItem.getGet() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.GET.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getPost() != null && newPathItem.getPost() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.POST.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getPut() != null && newPathItem.getPut() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.PUT.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getPatch() != null && newPathItem.getPatch() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.PATCH.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getDelete() != null && newPathItem.getDelete() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.DELETE.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getHead() != null && newPathItem.getHead() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.HEAD.name(), APIParserConstants.OPEN_API);
            return false;
        }
        if (pathItem.getOptions() != null && newPathItem.getOptions() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.OPTIONS.name(),
                    APIParserConstants.OPEN_API);
            return false;
        }
        return true;
    }

    private static boolean validateOAS2Paths(Path pathItem, Path newPathItem, String newPath,
                                             APIDefinitionValidationResponse validationResponse) {
        if (pathItem.getGet() != null && newPathItem.getGet() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.GET.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getPost() != null && newPathItem.getPost() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.POST.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getPut() != null && newPathItem.getPut() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.PUT.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getPatch() != null && newPathItem.getPatch() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.PATCH.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getDelete() != null && newPathItem.getDelete() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.DELETE.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getHead() != null && newPathItem.getHead() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.HEAD.name(), APIParserConstants.SWAGGER);
            return false;
        }
        if (pathItem.getOptions() != null && newPathItem.getOptions() != null) {
            addError(validationResponse, newPath, APIParserConstants.SupportedHTTPVerbs.OPTIONS.name(), APIParserConstants.SWAGGER);
            return false;
        }
        return true;
    }

    private static void addError(APIDefinitionValidationResponse validationResponse, String path, String operation,
                                 String definitionType) {
        OASParserUtil.addErrorToValidationResponse(validationResponse,
                "Multiple " + operation + " operations with the same resource path " + path +
                        " found in the " + definitionType + " definition");
    }
}
