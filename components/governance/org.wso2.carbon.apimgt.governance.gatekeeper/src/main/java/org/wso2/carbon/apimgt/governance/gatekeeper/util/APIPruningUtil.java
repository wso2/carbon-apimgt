/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.gatekeeper.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class for pruning API definitions by removing boilerplate fields
 * and extracting meaningful content for deduplication analysis.
 */
public final class APIPruningUtil {

    private static final Log log = LogFactory.getLog(APIPruningUtil.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private APIPruningUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Prunes an OpenAPI definition by removing boilerplate fields.
     * Removes: info.contact, info.license, info.termsOfService, servers, externalDocs, security
     *
     * @param apiDefinition The original API definition (YAML or JSON)
     * @return The pruned API definition as a normalized string
     * @throws APIMGovernanceException If parsing fails
     */
    public static String pruneAPIDefinition(String apiDefinition) throws APIMGovernanceException {
        if (apiDefinition == null || apiDefinition.trim().isEmpty()) {
            throw new APIMGovernanceException("API definition cannot be null or empty");
        }

        try {
            // Determine if YAML or JSON
            JsonNode rootNode;
            boolean isYaml = !apiDefinition.trim().startsWith("{");

            if (isYaml) {
                rootNode = yamlMapper.readTree(apiDefinition);
            } else {
                rootNode = jsonMapper.readTree(apiDefinition);
            }

            if (!(rootNode instanceof ObjectNode)) {
                throw new APIMGovernanceException("API definition must be a valid JSON/YAML object");
            }

            ObjectNode prunedNode = pruneJsonNode((ObjectNode) rootNode);

            // Return as normalized JSON for consistent comparison
            return jsonMapper.writeValueAsString(prunedNode);

        } catch (APIMGovernanceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error pruning API definition", e);
            throw new APIMGovernanceException("Failed to prune API definition: " + e.getMessage(), e);
        }
    }

    /**
     * Prunes the JSON node by removing boilerplate fields.
     *
     * @param rootNode The root JSON node
     * @return The pruned JSON node
     */
    private static ObjectNode pruneJsonNode(ObjectNode rootNode) {
        ObjectNode prunedNode = rootNode.deepCopy();

        // Remove servers array
        prunedNode.remove("servers");

        // Remove externalDocs
        prunedNode.remove("externalDocs");

        // Remove top-level security
        prunedNode.remove("security");

        // Prune info object
        if (prunedNode.has("info") && prunedNode.get("info").isObject()) {
            ObjectNode infoNode = (ObjectNode) prunedNode.get("info");
            infoNode.remove("contact");
            infoNode.remove("license");
            infoNode.remove("termsOfService");
            // Keep title, description, version as they are semantically meaningful
        }

        return prunedNode;
    }

    /**
     * Extracts normalized paths from an OpenAPI definition.
     * Paths are normalized by removing path parameters and sorting.
     *
     * @param apiDefinition The API definition string
     * @return Set of normalized path strings
     * @throws APIMGovernanceException If parsing fails
     */
    public static Set<String> extractNormalizedPaths(String apiDefinition) throws APIMGovernanceException {
        Set<String> normalizedPaths = new TreeSet<>();

        try {
            ParseOptions parseOptions = new ParseOptions();
            parseOptions.setResolve(true);
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(apiDefinition, null, parseOptions);

            if (result.getOpenAPI() == null) {
                log.warn("Could not parse OpenAPI definition. Parse messages: " + result.getMessages());
                return normalizedPaths;
            }

            OpenAPI openAPI = result.getOpenAPI();
            if (openAPI.getPaths() != null) {
                for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
                    String path = entry.getKey();
                    PathItem pathItem = entry.getValue();

                    // Normalize path by replacing path parameters with placeholder
                    String normalizedPath = normalizePath(path);

                    // Add path with HTTP methods
                    addPathOperations(normalizedPaths, normalizedPath, pathItem);
                }
            }

        } catch (Exception e) {
            log.error("Error extracting paths from API definition", e);
            throw new APIMGovernanceException("Failed to extract paths: " + e.getMessage(), e);
        }

        return normalizedPaths;
    }

    /**
     * Normalizes a path by replacing path parameters with a generic placeholder.
     *
     * @param path The original path
     * @return The normalized path
     */
    private static String normalizePath(String path) {
        // Replace path parameters like {id}, {userId} with {param}
        return path.replaceAll("\\{[^}]+\\}", "{param}").toLowerCase();
    }

    /**
     * Adds path operations to the set.
     *
     * @param paths     The set to add to
     * @param path      The normalized path
     * @param pathItem  The path item containing operations
     */
    private static void addPathOperations(Set<String> paths, String path, PathItem pathItem) {
        if (pathItem.getGet() != null) {
            paths.add("GET " + path);
            addOperationDetails(paths, "GET", path, pathItem.getGet());
        }
        if (pathItem.getPost() != null) {
            paths.add("POST " + path);
            addOperationDetails(paths, "POST", path, pathItem.getPost());
        }
        if (pathItem.getPut() != null) {
            paths.add("PUT " + path);
            addOperationDetails(paths, "PUT", path, pathItem.getPut());
        }
        if (pathItem.getDelete() != null) {
            paths.add("DELETE " + path);
            addOperationDetails(paths, "DELETE", path, pathItem.getDelete());
        }
        if (pathItem.getPatch() != null) {
            paths.add("PATCH " + path);
            addOperationDetails(paths, "PATCH", path, pathItem.getPatch());
        }
        if (pathItem.getHead() != null) {
            paths.add("HEAD " + path);
        }
        if (pathItem.getOptions() != null) {
            paths.add("OPTIONS " + path);
        }
    }

    /**
     * Adds operation details like operationId and tags.
     */
    private static void addOperationDetails(Set<String> paths, String method, String path, Operation operation) {
        if (operation.getOperationId() != null) {
            paths.add(method + " " + path + " operationId:" + operation.getOperationId().toLowerCase());
        }
        if (operation.getTags() != null) {
            for (String tag : operation.getTags()) {
                paths.add(method + " " + path + " tag:" + tag.toLowerCase());
            }
        }
    }

    /**
     * Extracts schema definitions from an OpenAPI definition.
     *
     * @param apiDefinition The API definition string
     * @return Set of normalized schema strings
     * @throws APIMGovernanceException If parsing fails
     */
    public static Set<String> extractNormalizedSchemas(String apiDefinition) throws APIMGovernanceException {
        Set<String> normalizedSchemas = new TreeSet<>();

        try {
            ParseOptions parseOptions = new ParseOptions();
            parseOptions.setResolve(true);
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(apiDefinition, null, parseOptions);

            if (result.getOpenAPI() == null) {
                return normalizedSchemas;
            }

            OpenAPI openAPI = result.getOpenAPI();
            if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
                for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
                    String schemaName = entry.getKey().toLowerCase();
                    Schema schema = entry.getValue();

                    normalizedSchemas.add("schema:" + schemaName);

                    // Add property information
                    if (schema.getProperties() != null) {
                        Map<String, Schema> properties = schema.getProperties();
                        for (String propName : properties.keySet()) {
                            Schema propSchema = properties.get(propName);
                            String propType = propSchema.getType() != null ? propSchema.getType() : "object";
                            normalizedSchemas.add("schema:" + schemaName + "." + propName.toLowerCase() + 
                                                  ":" + propType.toLowerCase());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error extracting schemas from API definition", e);
            throw new APIMGovernanceException("Failed to extract schemas: " + e.getMessage(), e);
        }

        return normalizedSchemas;
    }

    /**
     * Creates a combined feature set from paths and schemas for MinHash computation.
     *
     * @param apiDefinition The API definition string
     * @return List of feature strings
     * @throws APIMGovernanceException If extraction fails
     */
    public static List<String> extractFeatures(String apiDefinition) throws APIMGovernanceException {
        List<String> features = new ArrayList<>();

        // Add normalized paths
        features.addAll(extractNormalizedPaths(apiDefinition));

        // Add normalized schemas
        features.addAll(extractNormalizedSchemas(apiDefinition));

        // Sort for consistency
        Collections.sort(features);

        return features;
    }
}
