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
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.RefPath;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.UUID;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

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

    private static final String REF_PREFIX = "#/components/";
    private static final String ARRAY_DATA_TYPE = "array";

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

        throw new APIManagementException("Invalid OAS definition provided.");
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
            throw new APIManagementException("Cannot update destination swagger because it is not in OpenAPI format");
        }

        SwaggerUpdateContext context = new SwaggerUpdateContext();

        extractRelevantSourceData(apiToProductResourceMapping, context);

        // Update paths
        destOpenAPI.setPaths(context.getPaths());

        // Update Scopes
        setScopes(destOpenAPI, context.getAggregatedScopes());

        // Update reference definitions
        setReferenceObjectDefinitions(destOpenAPI, context);

        return Json.pretty(destOpenAPI);
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
            extensions.put(APIConstants.SWAGGER_X_SCOPES_BINDINGS, scopeBindings);
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

                    for (String refKey : refCategoryEntry.getValue()) {
                        RequestBody requestBody = sourceRequestBodies.get(refKey);
                        setRefOfRequestBody(requestBody, context);
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
                            Content content = parameter.getContent();
                            if (content != null) {
                                extractReferenceFromContent(content, context);
                            } else {
                                String ref = parameter.get$ref();
                                if (ref != null) {
                                    extractReferenceWithoutSchema(ref, context);
                                }
                            }
                        }
                    }
                }

                if (RESPONSES.equalsIgnoreCase(category)) {
                    Map<String, ApiResponse> responses = sourceComponents.getResponses();

                    if (responses != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            ApiResponse response = responses.get(refKey);
                            Content content = response.getContent();
                            extractReferenceFromContent(content, context);
                        }
                    }
                }

                if (HEADERS.equalsIgnoreCase(category)) {
                    Map<String, Header> headers = sourceComponents.getHeaders();

                    if (headers != null) {
                        for (String refKey : refCategoryEntry.getValue()) {
                            Header header = headers.get(refKey);
                            Content content = header.getContent();
                            extractReferenceFromContent(content, context);
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

            extractReferenceFromContent(content, context);
        }
    }

    private static void setRefOfApiResponses(ApiResponses responses, SwaggerUpdateContext context) {
        if (responses != null) {
            for (ApiResponse response : responses.values()) {
                Content content = response.getContent();

                extractReferenceFromContent(content, context);
            }
        }
    }

    private static void setRefOfApiResponseHeaders(ApiResponses responses, SwaggerUpdateContext context) {
        if (responses != null) {
            for (ApiResponse response : responses.values()) {
                Map<String, Header> headers = response.getHeaders();

                if (headers != null) {
                    for (Header header : headers.values()) {
                        Content content = header.getContent();

                        extractReferenceFromContent(content, context);
                    }
                }
            }
        }
    }

    private static void setRefOfParameters(List<Parameter> parameters, SwaggerUpdateContext context) {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
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

    private static void extractReferenceFromContent(Content content, SwaggerUpdateContext context) {
        if (content != null) {
            for (MediaType mediaType : content.values()) {
                Schema schema = mediaType.getSchema();

                extractReferenceFromSchema(schema, context);
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
            if (ref == null) {
                if (ARRAY_DATA_TYPE.equalsIgnoreCase(schema.getType())) {
                    ArraySchema arraySchema = (ArraySchema) schema;
                    ref = arraySchema.getItems().get$ref();
                }
            }

            if (ref != null) {
                addToReferenceObjectMap(ref, context);
            }

            // Process schema properties if present
            Map properties = schema.getProperties();

            if (properties != null) {
                for (Object propertySchema : properties.values()) {
                    extractReferenceFromSchema((Schema) propertySchema, context);
                }
            }
        }
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
        if ((new File(archiveDirectory + "/" + APIConstants.OPENAPI_MASTER_JSON)).exists()) {
            masterSwagger = new File(archiveDirectory + "/" + APIConstants.OPENAPI_MASTER_JSON);
            return masterSwagger;
        } else if ((new File(archiveDirectory + "/" + APIConstants.OPENAPI_MASTER_YAML)).exists()) {
            masterSwagger = new File(archiveDirectory + "/" + APIConstants.OPENAPI_MASTER_YAML);
            return masterSwagger;
        } else {
            throw new APIManagementException("Could not find a master swagger file with the name of swagger.json " +
                    "/swagger.yaml");
        }
    }

    /**
     * Extract the archive file and validates the openAPI definition
     *
     * @param inputStream   file as input stream
     * @param returnContent whether to return the content of the definition in the response DTO
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse extractAndValidateOpenAPIArchive(InputStream inputStream,
            boolean returnContent) throws APIManagementException {
        String path = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator +
                APIConstants.OPENAPI_ARCHIVES_TEMP_FOLDER + File.separator + UUID.randomUUID().toString();
        String archivePath = path + File.separator + APIConstants.OPENAPI_ARCHIVE_ZIP_FILE;
        String extractedLocation = APIFileUtil
                .extractUploadedArchive(inputStream, APIConstants.OPENAPI_EXTRACTED_DIRECTORY, archivePath, path);
        File[] listOfFiles = new File(extractedLocation).listFiles();
        File archiveDirectory = null;
        if (listOfFiles != null) {
            if (listOfFiles.length > 1) {
                throw new APIManagementException("Swagger Definitions should be placed under one root folder.");
            }
            for (File file: listOfFiles) {
                if (file.isDirectory()) {
                    archiveDirectory = file.getAbsoluteFile();
                    break;
                }
            }
        }
        //Verify whether the zipped input is archive or file.
        //If it is a single  swagger file without remote references it can be imported directly, without zipping.
        if (archiveDirectory == null) {
            throw new APIManagementException("Could not find an archive in the given ZIP file.");
        }
        File masterSwagger = checkMasterSwagger(archiveDirectory);
        String content;
        try {
            InputStream masterInputStream = new FileInputStream(masterSwagger);
            content = IOUtils.toString(masterInputStream, APIConstants.DigestAuthConstants.CHARSET);
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
            openAPIContent = Json.pretty(openAPI);
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
     * Try to validate a give openAPI definition using OpenAPI 3 parser
     *
     * @param apiDefinition     definition
     * @param returnJsonContent whether to return definition as a json content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException if error occurred while parsing definition
     */
    public static APIDefinitionValidationResponse validateAPIDefinition(String apiDefinition, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse =
                oas3Parser.validateAPIDefinition(apiDefinition, returnJsonContent);
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
     * Creates a json string using the swagger object.
     *
     * @param swaggerObj swagger object
     * @return json string using the swagger object
     * @throws APIManagementException error while creating swagger json
     */
    public static String getSwaggerJsonString(Swagger swaggerObj) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        //this is to ignore "originalRef" in schema objects
        mapper.addMixIn(RefModel.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefProperty.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefPath.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefParameter.class, IgnoreOriginalRefMixin.class);
        mapper.addMixIn(RefResponse.class, IgnoreOriginalRefMixin.class);

        //this is to ignore "responseSchema" in response schema objects
        mapper.addMixIn(Response.class, ResponseSchemaMixin.class);
        try {
            return new String(mapper.writeValueAsBytes(swaggerObj));
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Error while generating Swagger json from model", e);
        }
    }

    /**
     * This method validates the given OpenAPI definition by URL
     *
     * @param url               URL of the API definition
     * @param returnJsonContent whether to return the converted json form of the
     * @return APIDefinitionValidationResponse object with validation information
     */
    public static APIDefinitionValidationResponse validateAPIDefinitionByURL(String url, boolean returnJsonContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        try {
            URL urlObj = new URL(url);
            HttpClient httpClient = APIUtil.getHttpClient(urlObj.getPort(), urlObj.getProtocol());
            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                validationResponse = validateAPIDefinition(responseStr, returnJsonContent);
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
     * This method returns the timestamps for a given API
     *
     * @param apiIdentifier
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public static Map<String, String> getAPIOpenAPIDefinitionTimeStamps(APIIdentifier apiIdentifier, Registry registry)
            throws APIManagementException {
        Map<String, String> timeStampMap = new HashMap<String, String>();
        String resourcePath =
                APIUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                        apiIdentifier.getProviderName());
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                Date lastModified = apiDocResource.getLastModified();
                Date createdTime = apiDocResource.getCreatedTime();
                if (lastModified != null) {
                    timeStampMap.put("UPDATED_TIME", String.valueOf(lastModified.getTime()));
                } else {
                    timeStampMap.put("CREATED_TIME", String.valueOf(createdTime.getTime()));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 updated time for " + apiIdentifier.getApiName() + '-'
                            + apiIdentifier.getVersion(), e);
        }
        return timeStampMap;
    }

    /**
     * This method saves api definition json in the registry
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     * @param registry          user registry
     * @throws APIManagementException
     */
    public static void saveAPIDefinition(API api, String apiDefinitionJSON, Registry registry)
            throws APIManagementException {
        String apiName = api.getId().getApiName();
        String apiVersion = api.getId().getVersion();
        String apiProviderName = api.getId().getProviderName();

        try {
            String resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiName, apiVersion, apiProviderName);
            resourcePath = resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME;
            Resource resource;
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
            } else {
                resource = registry.get(resourcePath);
            }
            resource.setContent(apiDefinitionJSON);
            resource.setMediaType("application/json");
            registry.put(resourcePath, resource);

            String[] visibleRoles = null;
            if (api.getVisibleRoles() != null) {
                visibleRoles = api.getVisibleRoles().split(",");
            }

            //Need to set anonymous if the visibility is public
            APIUtil.clearResourcePermissions(resourcePath, api.getId(), ((UserRegistry) registry).getTenantId());
            APIUtil.setResourcePermissions(apiProviderName, api.getVisibility(), visibleRoles, resourcePath);

        } catch (RegistryException e) {
            handleException("Error while adding Swagger Definition for " + apiName + '-' + apiVersion, e);
        }
    }

    /**
     * This method returns api definition json for given api
     *
     * @param apiIdentifier api identifier
     * @param registry      user registry
     * @return api definition json as json string
     * @throws APIManagementException
     */
    public static String getAPIDefinition(Identifier apiIdentifier, Registry registry) throws APIManagementException {
        String resourcePath = "";

        if (apiIdentifier instanceof APIIdentifier) {
            resourcePath = APIUtil.getOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                    apiIdentifier.getProviderName());
        } else if (apiIdentifier instanceof APIProductIdentifier) {
            resourcePath =
                    APIUtil.getAPIProductOpenAPIDefinitionFilePath(apiIdentifier.getName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
        }

        JSONParser parser = new JSONParser();
        String apiDocContent = null;
        try {
            if (registry.resourceExists(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME)) {
                Resource apiDocResource = registry.get(resourcePath + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME);
                apiDocContent = new String((byte[]) apiDocResource.getContent(), Charset.defaultCharset());
                parser.parse(apiDocContent);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Resource " + APIConstants.API_OAS_DEFINITION_RESOURCE_NAME + " not found at "
                            + resourcePath);
                }
            }
        } catch (RegistryException e) {
            handleException(
                    "Error while retrieving OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                            + apiIdentifier.getVersion(), e);
        } catch (ParseException e) {
            handleException("Error while parsing OpenAPI v2.0 or v3.0.0 Definition for " + apiIdentifier.getName() + '-'
                    + apiIdentifier.getVersion() + " in " + resourcePath, e);
        }
        return apiDocContent;
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
            Scope scope = APIUtil.findScopeByKey(apiScopes, scopeName);
            if (scope == null) {
                throw new APIManagementException("Resource Scope '" + scopeName + "' not found.");
            }
            template.setScopes(scope);
        }
        return template;
    }

    /**
     * generate endpoint information for OAS definition
     *
     * @param api          API
     * @param isProduction is production endpoints
     * @return JsonNode
     */
    public static JsonNode generateOASConfigForEndpoints(API api, boolean isProduction) {
        if (api.getEndpointConfig() == null || api.getEndpointConfig().trim().isEmpty()) {
            return null;
        }
        JSONObject endpointConfig = new JSONObject(api.getEndpointConfig());
        if (endpointConfig.has(APIConstants.IMPLEMENTATION_STATUS)) {
            // no need to populate if it is prototype API
            return null;
        }
        ObjectNode endpointResult;
        String type = endpointConfig.getString(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE);
        if (APIConstants.ENDPOINT_TYPE_DEFAULT.equalsIgnoreCase(type)) {
            endpointResult = objectMapper.createObjectNode();
            endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_DEFAULT);
        } else if (APIConstants.ENDPOINT_TYPE_FAILOVER.equalsIgnoreCase(type)) {
            endpointResult = populateFailoverConfig(endpointConfig, isProduction);
        } else if (APIConstants.ENDPOINT_TYPE_LOADBALANCE.equalsIgnoreCase(type)) {
            endpointResult = populateLoadBalanceConfig(endpointConfig, isProduction);
        } else if (APIConstants.ENDPOINT_TYPE_HTTP.equalsIgnoreCase(type)) {
            endpointResult = setPrimaryConfig(endpointConfig, isProduction, APIConstants.ENDPOINT_TYPE_HTTP);
        } else if (APIConstants.ENDPOINT_TYPE_ADDRESS.equalsIgnoreCase(type)) {
            endpointResult = setPrimaryConfig(endpointConfig, isProduction, APIConstants.ENDPOINT_TYPE_ADDRESS);
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
                securityConfigObj.put(APIConstants.ENDPOINT_SECURITY_TYPE, APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST);
            } else {
                securityConfigObj.put(APIConstants.ENDPOINT_SECURITY_TYPE, APIConstants.ENDPOINT_SECURITY_TYPE_BASIC);
            }
            if (!StringUtils.isEmpty(api.getEndpointUTUsername())) {
                securityConfigObj.put(APIConstants.ENDPOINT_SECURITY_USERNAME, api.getEndpointUTUsername());
            }
            endpointResult.set(APIConstants.ENDPOINT_SECURITY_CONFIG, securityConfigObj);
        }
    }

    /**
     * Set failover configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static ObjectNode populateFailoverConfig(JSONObject endpointConfig, boolean isProd) {
        JSONArray endpointsURLs = null;
        JSONObject primaryEndpoints = null;
        if (isProd) {
            if (endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_FAILOVERS)) {
                endpointsURLs = endpointConfig.getJSONArray(APIConstants.ENDPOINT_PRODUCTION_FAILOVERS);
            }
            if (endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_FAILOVERS)) {
                endpointsURLs = endpointConfig.getJSONArray(APIConstants.ENDPOINT_SANDBOX_FAILOVERS);
            }
            if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (endpointsURLs != null) {
            for (int i = 0; i < endpointsURLs.length(); i++) {
                JSONObject obj = endpointsURLs.getJSONObject(i);
                endpointsArray.add(obj.getString(APIConstants.ENDPOINT_URL));
            }
        }
        if (primaryEndpoints != null && primaryEndpoints.has(APIConstants.ENDPOINT_URL)) {
            endpointsArray.add(primaryEndpoints.getString(APIConstants.ENDPOINT_URL));
        }
        if (endpointsArray.size() < 1) {
            return null;
        }
        ObjectNode endpointResult = objectMapper.createObjectNode();
        endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_FAILOVER);
        return endpointResult;
    }

    /**
     * Set load balance configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     */
    private static ObjectNode populateLoadBalanceConfig(JSONObject endpointConfig, boolean isProd) {
        JSONArray primaryProdEndpoints = new JSONArray();
        if (isProd) {
            if (endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS) && endpointConfig
                    .get(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS) instanceof JSONArray) {
                primaryProdEndpoints = endpointConfig.getJSONArray(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS) && endpointConfig
                    .get(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS) instanceof JSONArray) {
                primaryProdEndpoints = endpointConfig.getJSONArray(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }

        ArrayNode endpointsArray = objectMapper.createArrayNode();
        if (primaryProdEndpoints != null) {
            for (int i = 0; i < primaryProdEndpoints.length(); i++) {
                JSONObject obj = primaryProdEndpoints.getJSONObject(i);
                endpointsArray.add(obj.getString(APIConstants.ENDPOINT_URL));
            }
        }
        if (endpointsArray.size() < 1) {
            return null;
        }
        ObjectNode endpointResult = objectMapper.createObjectNode();
        endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
        endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, APIConstants.ENDPOINT_TYPE_LOADBALANCE);
        return endpointResult;
    }

    /**
     * Set baisc configuration
     *
     * @param endpointConfig endpoint configuration json string
     * @param isProd         endpoint type
     * @param type           endpoint type
     */
    private static ObjectNode setPrimaryConfig(JSONObject endpointConfig, boolean isProd, String type) {
        JSONObject primaryEndpoints = new JSONObject();
        if (isProd) {
            if (endpointConfig.has(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_PRODUCTION_ENDPOINTS);
            }
        } else {
            if (endpointConfig.has(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS)) {
                primaryEndpoints = endpointConfig.getJSONObject(APIConstants.ENDPOINT_SANDBOX_ENDPOINTS);
            }
        }
        if (primaryEndpoints != null && primaryEndpoints.has(APIConstants.ENDPOINT_URL)) {
            ArrayNode endpointsArray = objectMapper.createArrayNode();
            endpointsArray.add(primaryEndpoints.getString(APIConstants.ENDPOINT_URL));
            ObjectNode endpointResult = objectMapper.createObjectNode();
            endpointResult.set(APIConstants.ENDPOINT_URLS, endpointsArray);
            endpointResult.put(APIConstants.X_WSO2_ENDPOINT_TYPE, type);
            return endpointResult;
        }
        return null;
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
        extensions.remove(APIConstants.X_WSO2_CORS);
        extensions.remove(APIConstants.X_WSO2_AUTH_HEADER);
        extensions.remove(APIConstants.X_WSO2_THROTTLING_TIER);
        extensions.remove(APIConstants.X_THROTTLING_TIER);
        extensions.remove(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS);
        extensions.remove(APIConstants.X_WSO2_SANDBOX_ENDPOINTS);
        extensions.remove(APIConstants.X_WSO2_BASEPATH);
        extensions.remove(APIConstants.X_WSO2_TRANSPORTS);
        extensions.remove(APIConstants.X_WSO2_APP_SECURITY);
        extensions.remove(APIConstants.X_WSO2_RESPONSE_CACHE);
        extensions.remove(APIConstants.X_WSO2_MUTUAL_SSL);
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
        extensions.remove(APIConstants.X_WSO2_APP_SECURITY);
        extensions.remove(APIConstants.X_WSO2_SANDBOX_ENDPOINTS);
        extensions.remove(APIConstants.X_WSO2_PRODUCTION_ENDPOINTS);
        extensions.remove(APIConstants.X_WSO2_DISABLE_SECURITY);
        extensions.remove(APIConstants.X_WSO2_THROTTLING_TIER);
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
            if (APIConstants.APPLICATION_LEVEL_SECURITY.contains(securityType)) {
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
            appSecurityOptional = !securityList.contains(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
        }
        ArrayNode appSecurityTypes = objectMapper.valueToTree(appSecurityList);
        endpointResult.set(APIConstants.WSO2_APP_SECURITY_TYPES, appSecurityTypes);
        endpointResult.put(APIConstants.OPTIONAL, appSecurityOptional);
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
        boolean enabled = APIConstants.ENABLED.equalsIgnoreCase(responseCache);
        responseCacheConfig.put(APIConstants.RESPONSE_CACHING_ENABLED, enabled);
        responseCacheConfig.put(APIConstants.RESPONSE_CACHING_TIMEOUT, cacheTimeout);
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
     * @return String
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
     * @return String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getThrottleTierFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object throttleTier = extensions.get(APIConstants.X_WSO2_THROTTLING_TIER);
        return throttleTier == null ? null : throttleTier.toString();
    }

    /**
     * This method returns extension of transports(http,https) related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getTransportsFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        String transports = null;
        ObjectMapper mapper = new ObjectMapper();
        if (extensions.containsKey(APIConstants.X_WSO2_TRANSPORTS)) {
            Object object = extensions.get(APIConstants.X_WSO2_TRANSPORTS).toString();
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
     * @return String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getMutualSSLEnabledFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object mutualSSl = extensions.get(APIConstants.X_WSO2_MUTUAL_SSL);
        return mutualSSl == null ? null : mutualSSl.toString();
    }

    /**
     * This method returns extension of CORS config related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return CORSConfiguration
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

        if (extensions.containsKey(APIConstants.X_WSO2_CORS)) {
            Object corsConfigObject = extensions.get(APIConstants.X_WSO2_CORS);
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
     * @return boolean
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getResponseCacheFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        boolean responseCache = false;
        if (extensions.containsKey(APIConstants.X_WSO2_RESPONSE_CACHE)) {
            Object responseCacheConfig = extensions.get(APIConstants.X_WSO2_RESPONSE_CACHE);
            ObjectNode cacheConfigNode = mapper.convertValue(responseCacheConfig, ObjectNode.class);
            responseCache = Boolean.parseBoolean(String.valueOf(cacheConfigNode.get(APIConstants.RESPONSE_CACHING_ENABLED)));
        }
        return responseCache;
    }

    /**
     * This method returns extension of cache timeout related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return int
     * @throws APIManagementException throws if an error occurred
     */
    public static int getCacheTimeOutFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        int timeOut = 0;
        if (extensions.containsKey(APIConstants.X_WSO2_RESPONSE_CACHE)) {
            Object responseCacheConfig = extensions.get(APIConstants.X_WSO2_RESPONSE_CACHE);
            ObjectNode cacheConfigNode = mapper.convertValue(responseCacheConfig, ObjectNode.class);
            timeOut = Integer.parseInt(String.valueOf(cacheConfigNode.get(APIConstants.RESPONSE_CACHING_TIMEOUT)));
        }
        return timeOut;
    }

    /**
     * This method returns extension of custom authorization Header related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return String
     * @throws APIManagementException throws if an error occurred
     */
    public static String getAuthorizationHeaderFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        Object authorizationHeader = extensions.get(APIConstants.X_WSO2_AUTH_HEADER);
        return authorizationHeader == null ? null : authorizationHeader.toString();
    }

    /**
     * This method returns extension of disable security related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return boolean
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getDisableSecurity(Map<String, Object> extensions) throws APIManagementException {
        boolean disableSecurity = false;
        if (extensions.containsKey(APIConstants.X_WSO2_DISABLE_SECURITY)) {
            disableSecurity = Boolean.parseBoolean(String.valueOf(extensions.get(APIConstants.X_WSO2_DISABLE_SECURITY)));
        }
        return disableSecurity;
    }

    /**
     * This method returns extension of application security types related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return String
     * @throws APIManagementException throws if an error occurred
     */
    public static List<String> getApplicationSecurityTypes(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> appSecurityTypes = new ArrayList<>();
        if (extensions.containsKey(APIConstants.X_WSO2_APP_SECURITY)) {
            Object applicationSecurityTypes = extensions.get(APIConstants.X_WSO2_APP_SECURITY);
            ObjectNode appSecurityTypesNode = mapper.convertValue(applicationSecurityTypes, ObjectNode.class);
            appSecurityTypes = mapper.convertValue(appSecurityTypesNode.get("security-types"), ArrayList.class);
        }
        return appSecurityTypes;
    }

    /**
     * This method returns extension of application security types state related to micro-gw
     *
     * @param extensions Map<String, Object>
     * @return boolean
     * @throws APIManagementException throws if an error occurred
     */
    public static boolean getAppSecurityStateFromSwagger(Map<String, Object> extensions) throws APIManagementException {
        ObjectMapper mapper = new ObjectMapper();
        boolean appSecurityState = false;
        if (extensions.containsKey(APIConstants.X_WSO2_APP_SECURITY)) {
            Object applicationSecurityTypes = extensions.get(APIConstants.X_WSO2_APP_SECURITY);
            ObjectNode appSecurityTypesNode = mapper.convertValue(applicationSecurityTypes, ObjectNode.class);
            appSecurityState = Boolean.parseBoolean(String.valueOf(appSecurityTypesNode.get("optional")));
        }
        return appSecurityState;
    }

}
