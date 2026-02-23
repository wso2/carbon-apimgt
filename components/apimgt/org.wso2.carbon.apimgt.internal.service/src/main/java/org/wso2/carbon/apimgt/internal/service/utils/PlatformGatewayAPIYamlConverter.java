/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.internal.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Converts on-prem APIM API model to API Platform Gateway readable YAML format.
 * Output conforms to gateway.api-platform.wso2.com/v1alpha1 RestApi spec.
 * Policies are omitted (policy hub is used separately).
 */
public final class PlatformGatewayAPIYamlConverter {

    private static final String API_VERSION = "gateway.api-platform.wso2.com/v1alpha1";
    private static final String KIND = "RestApi";
    /** Pattern for metadata.name: URL-safe, 3-63 chars. */
    private static final Pattern NAME_SAFE = Pattern.compile("^[a-z0-9][a-z0-9\\-]*[a-z0-9]$");

    private PlatformGatewayAPIYamlConverter() {
    }

    /**
     * Build API Platform format YAML string from on-prem API for platform-gateway deployment.
     * Uses platform-style context so invocation URL matches: /{orgId}/{environment}/{apiName}/{version}/{resource}.
     * No policies are included (policy hub).
     *
     * @param api on-prem API (must have endpoint config)
     * @param organization organization (tenant domain, e.g. carbon.super); if blank, uses API context
     * @param environment environment segment (e.g. "default"); if blank, uses API context
     * @return YAML string (apiVersion, kind, metadata, spec with displayName, version, context, upstream, operations)
     * @throws APIManagementException if API is invalid or endpoint URL cannot be resolved
     */
    public static String toPlatformGatewayYaml(API api, String organization, String environment)
            throws APIManagementException {
        String displayName = sanitizeDisplayName(
                api.getDisplayName() != null ? api.getDisplayName() : (api.getId() != null ? api.getId().getApiName() : null));
        String version = normalizeVersion(api.getId() != null ? api.getId().getVersion() : null);
        String context = buildPlatformStyleContext(organization, environment, displayName, version, api);
        String metadataName = toMetadataName(displayName, version);
        return buildYaml(api, displayName, version, context, metadataName);
    }

    /**
     * Build API Platform format YAML string from on-prem API (context from API definition).
     * No policies are included (policy hub).
     *
     * @param api on-prem API (must have endpoint config and context)
     * @return YAML string (apiVersion, kind, metadata, spec with displayName, version, context, upstream, operations)
     * @throws APIManagementException if API is invalid or endpoint URL cannot be resolved
     */
    public static String toPlatformGatewayYaml(API api) throws APIManagementException {
        String displayName = sanitizeDisplayName(
                api.getDisplayName() != null ? api.getDisplayName() : (api.getId() != null ? api.getId().getApiName() : null));
        String version = normalizeVersion(api.getId() != null ? api.getId().getVersion() : null);
        String context = normalizeContext(api.getContextTemplate() != null ? api.getContextTemplate() : api.getContext());
        String metadataName = toMetadataName(displayName, version);
        return buildYaml(api, displayName, version, context, metadataName);
    }

    private static String buildPlatformStyleContext(String organization, String environment, String displayName,
            String version, API api) {
        if (StringUtils.isBlank(organization) || StringUtils.isBlank(environment)) {
            return normalizeContext(api.getContextTemplate() != null ? api.getContextTemplate() : api.getContext());
        }
        String orgSegment = organization.trim().toLowerCase().replaceAll("[^a-z0-9\\-_.]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (orgSegment.isEmpty()) orgSegment = "default";
        String envSegment = environment.trim().toLowerCase().replaceAll("[^a-z0-9\\-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (envSegment.isEmpty()) envSegment = "default";
        String apiNamePath = toApiNamePath(displayName);
        return "/" + orgSegment + "/" + envSegment + "/" + apiNamePath + "/" + version;
    }

    /** URL path segment from display name (e.g. "Reading List API 1" -> "reading-list-api-1"). */
    private static String toApiNamePath(String displayName) {
        if (displayName == null || displayName.isEmpty()) return "api";
        return displayName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static String buildYaml(API api, String displayName, String version, String context, String metadataName)
            throws APIManagementException {
        String productionUrl = extractProductionEndpointUrl(api.getEndpointConfig());
        String sandboxUrl = extractSandboxEndpointUrl(api.getEndpointConfig());

        StringBuilder yaml = new StringBuilder();
        yaml.append("apiVersion: ").append(quote(API_VERSION)).append("\n");
        yaml.append("kind: ").append(quote(KIND)).append("\n");
        yaml.append("metadata:\n");
        yaml.append("  name: ").append(quote(metadataName)).append("\n");
        yaml.append("spec:\n");
        yaml.append("  displayName: ").append(quote(displayName)).append("\n");
        yaml.append("  version: ").append(quote(version)).append("\n");
        yaml.append("  context: ").append(quote(context)).append("\n");
        yaml.append("  upstream:\n");
        yaml.append("    main:\n");
        yaml.append("      url: ").append(quote(productionUrl)).append("\n");
        if (StringUtils.isNotBlank(sandboxUrl)) {
            yaml.append("    sandbox:\n");
            yaml.append("      url: ").append(quote(sandboxUrl)).append("\n");
        }
        yaml.append("  operations:\n");
        appendOperations(api, yaml);
        return yaml.toString();
    }

    private static String quote(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static String sanitizeDisplayName(String name) {
        if (name == null || name.isEmpty()) return "api";
        return name.replaceAll("[^a-zA-Z0-9\\-_ .]", "-").trim();
    }

    private static String normalizeVersion(String v) {
        if (v == null || v.isEmpty()) return "v1.0";
        if (v.matches("^v\\d+\\.\\d+$")) return v;
        if (v.matches("^\\d+\\.\\d+$")) return "v" + v;
        return "v1.0";
    }

    private static String normalizeContext(String ctx) {
        if (ctx == null || ctx.isEmpty()) return "/api";
        String c = ctx.trim();
        if (!c.startsWith("/")) c = "/" + c;
        if (c.length() > 1 && c.endsWith("/")) c = c.substring(0, c.length() - 1);
        return c;
    }

    private static String toMetadataName(String displayName, String version) {
        String base = (displayName + "-" + version).toLowerCase().replaceAll("[^a-z0-9\\-]", "-")
                .replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (base.length() > 63) base = base.substring(0, 63);
        if (!NAME_SAFE.matcher(base).matches()) base = "api-" + base;
        return base;
    }

    private static void appendOperations(API api, StringBuilder yaml) {
        Set<URITemplate> templates = api.getUriTemplates();
        if (templates == null || templates.isEmpty()) {
            yaml.append("  - method: GET\n");
            yaml.append("    path: \"/*\"\n");
            return;
        }
        for (URITemplate template : templates) {
            String path = template.getUriTemplate();
            if (path == null || path.isEmpty()) path = "/*";
            if (!path.startsWith("/")) path = "/" + path;
            Set<String> verbs = template.getHttpVerbs();
            if (verbs == null || verbs.isEmpty()) {
                String verb = template.getHTTPVerb();
                verbs = new LinkedHashSet<>();
                if (verb != null && !verb.isEmpty()) verbs.add(verb);
                else verbs.add("GET");
            }
            for (String method : verbs) {
                String m = (method == null || method.isEmpty()) ? "GET" : method.toUpperCase();
                yaml.append("  - method: ").append(m).append("\n");
                yaml.append("    path: ").append(quote(path)).append("\n");
            }
        }
    }

    /**
     * Extract production endpoint URL from endpointConfig JSON.
     * Handles production_endpoints.url or production_endpoints.list[0].url.
     */
    public static String extractProductionEndpointUrl(String endpointConfig) throws APIManagementException {
        if (StringUtils.isBlank(endpointConfig)) {
            throw new APIManagementException("API has no endpoint configuration");
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(endpointConfig);
            Object prod = root.get(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            if (prod == null) {
                throw new APIManagementException("API has no production_endpoints");
            }
            if (prod instanceof JSONObject) {
                Object url = ((JSONObject) prod).get(APIConstants.API_DATA_URL);
                if (url != null && StringUtils.isNotBlank(url.toString())) {
                    return url.toString().trim();
                }
            }
            throw new APIManagementException("production_endpoints has no url");
        } catch (ParseException e) {
            throw new APIManagementException("Invalid endpoint configuration JSON", e);
        } catch (ClassCastException e) {
            throw new APIManagementException("Invalid endpoint configuration structure", e);
        }
    }

    /**
     * Extract sandbox endpoint URL if present.
     */
    public static String extractSandboxEndpointUrl(String endpointConfig) {
        if (StringUtils.isBlank(endpointConfig)) return null;
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(endpointConfig);
            Object sandbox = root.get(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
            if (sandbox instanceof JSONObject) {
                Object url = ((JSONObject) sandbox).get(APIConstants.API_DATA_URL);
                if (url != null && StringUtils.isNotBlank(url.toString())) {
                    return url.toString().trim();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
