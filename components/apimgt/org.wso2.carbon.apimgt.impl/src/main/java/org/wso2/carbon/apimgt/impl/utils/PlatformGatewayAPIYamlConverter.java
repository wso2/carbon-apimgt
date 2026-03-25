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

package org.wso2.carbon.apimgt.impl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Converts on-prem APIM API model to API Platform Gateway readable YAML format.
 * Output conforms to gateway.api-platform.wso2.com/v1alpha1 RestApi spec.
 * API-level and operation-level policies are included so the platform gateway can enforce them.
 */
public final class PlatformGatewayAPIYamlConverter {

    private static final String API_VERSION = "gateway.api-platform.wso2.com/v1alpha1";
    private static final String KIND = "RestApi";
    /** Pattern for metadata.name: URL-safe, 3-63 chars. */
    private static final Pattern NAME_SAFE = Pattern.compile("^[a-z0-9][a-z0-9\\-]*[a-z0-9]$");

    private PlatformGatewayAPIYamlConverter() {
    }

    /**
     * Build API Platform format YAML string from on-prem API (context from API definition).
     * API-level and operation-level policies are included for gateway enforcement.
     *
     * @param api on-prem API (must have endpoint config and context)
     * @return YAML string (apiVersion, kind, metadata, spec with displayName, version, context, upstream, operations)
     * @throws APIManagementException if API is invalid or endpoint URL cannot be resolved
     */
    public static String toPlatformGatewayYaml(API api) throws APIManagementException {
        String displayName = api.getDisplayName();
        String version = api.getId().getVersion();
        String context = api.getContext();
        String metadataName = toMetadataName(displayName, version);
        return buildYaml(api, displayName, version, context, metadataName);
    }

    private static String buildYaml(API api, String displayName, String version, String context, String metadataName)
            throws APIManagementException {
        String productionUrl = extractProductionEndpointUrl(api.getEndpointConfig());
        String sandboxUrl = extractSandboxEndpointUrl(api.getEndpointConfig());

        // Build a DTO structure compatible with API Platform APIDeploymentYAML (apiVersion/kind/metadata/spec)
        DeploymentYaml deployment = new DeploymentYaml();
        deployment.apiVersion = API_VERSION;
        deployment.kind = KIND;

        DeploymentMetadata metadata = new DeploymentMetadata();
        metadata.name = metadataName;
        deployment.metadata = metadata;

        ApiSpec spec = new ApiSpec();
        spec.displayName = displayName;
        spec.version = version;
        spec.context = context;

        UpstreamYaml upstream = new UpstreamYaml();
        UpstreamTarget main = new UpstreamTarget();
        main.url = productionUrl;
        upstream.main = main;
        if (StringUtils.isNotBlank(sandboxUrl)) {
            UpstreamTarget sandbox = new UpstreamTarget();
            sandbox.url = sandboxUrl;
            upstream.sandbox = sandbox;
        }
        spec.upstream = upstream;

        // API-level policies (request/response/fault from DB)
        List<OperationPolicy> apiPolicies = api.getApiPolicies();
        if (apiPolicies != null && !apiPolicies.isEmpty()) {
            for (OperationPolicy policy : apiPolicies) {
                spec.policies.add(toPolicyDto(policy));
            }
        }
        // API-level Policy Hub policies (Separate property, not in AM_API_OPERATION_POLICY_MAPPING)
        List<OperationPolicy> apiHubPolicies = api.getHubPolicies();
        if (apiHubPolicies != null && !apiHubPolicies.isEmpty()) {
            for (OperationPolicy policy : apiHubPolicies) {
                spec.policies.add(toPolicyDto(policy));
            }
        }

        // Operation-level policies
        appendOperations(api, spec);

        deployment.spec = spec;

        ObjectMapper yamlMapper = new ObjectMapper(
                new YAMLFactory()
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                        .disable(YAMLGenerator.Feature.SPLIT_LINES)
        );
        try {
            return yamlMapper.writeValueAsString(deployment);
        } catch (JsonProcessingException e) {
            throw new APIManagementException("Failed to serialize platform gateway YAML", e);
        }
    }

    /**
     * Convert an APIM OperationPolicy into a generic policy DTO for platform gateway.
     * Uses major-only policy version (v0, v1, ...) and retains parameter types for Policy Hub validation.
     */
    private static PolicyDto toPolicyDto(OperationPolicy policy) {
        if (policy == null) {
            return null;
        }
        PolicyDto dto = new PolicyDto();
        String name = policy.getPolicyName();
        if (StringUtils.isBlank(name)) {
            name = "policy";
        }
        dto.name = name;
        dto.version = toPolicyVersionMajorOnly(policy.getPolicyVersion());
        // Leave executionCondition unset so the gateway executes the policy unconditionally.
        // Direction (request/response) is determined by the chain (request vs response) the policy belongs to.
        Map<String, Object> params = policy.getParameters();
        if (params != null && !params.isEmpty()) {
            dto.params = params;
        }
        return dto;
    }

    /** Gateway expects major-only version (e.g. v0, v1). Converts 0.2 -> v0, 1.0 -> v1. */
    private static String toPolicyVersionMajorOnly(String version) {
        if (StringUtils.isBlank(version)) return "v1";
        String v = version.trim();
        if (v.startsWith("v") && v.length() > 1 && Character.isDigit(v.charAt(1))) {
            int major = 1;
            try {
                int end = 1;
                while (end < v.length() && Character.isDigit(v.charAt(end))) end++;
                major = Integer.parseInt(v.substring(1, end));
            } catch (NumberFormatException ignored) { }
            return "v" + major;
        }
        try {
            int dot = v.indexOf('.');
            int major = dot > 0 ? Integer.parseInt(v.substring(0, dot).trim()) : Integer.parseInt(v.trim());
            return "v" + major;
        } catch (NumberFormatException e) {
            return "v1";
        }
    }

    private static String toMetadataName(String displayName, String version) {
        String base = (displayName + "-" + version).toLowerCase().replaceAll("[^a-z0-9\\-]", "-")
                .replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (base.length() > 63) base = base.substring(0, 63);
        if (!NAME_SAFE.matcher(base).matches()) base = "api-" + base;
        return base;
    }

    private static void appendOperations(API api, ApiSpec spec) {
        Set<URITemplate> templates = api.getUriTemplates();
        if (templates == null || templates.isEmpty()) {
            OperationYaml op = new OperationYaml();
            op.method = "GET";
            op.path = "/*";
            spec.operations.add(op);
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
                if (verb != null && !verb.isEmpty()) {
                    verbs.add(verb);
                } else {
                    verbs.add("GET");
                }
            }
            List<OperationPolicy> opPolicies = template.getOperationPolicies();
            List<OperationPolicy> opHubPolicies = template.getHubPolicies();
            for (String method : verbs) {
                String m = (method == null || method.isEmpty()) ? "GET" : method.toUpperCase();
                OperationYaml op = new OperationYaml();
                op.method = m;
                op.path = path;
                if (opPolicies != null && !opPolicies.isEmpty()) {
                    for (OperationPolicy policy : opPolicies) {
                        PolicyDto dto = toPolicyDto(policy);
                        if (dto != null) {
                            op.policies.add(dto);
                        }
                    }
                }
                if (opHubPolicies != null && !opHubPolicies.isEmpty()) {
                    for (OperationPolicy policy : opHubPolicies) {
                        PolicyDto dto = toPolicyDto(policy);
                        if (dto != null) {
                            op.policies.add(dto);
                        }
                    }
                }
                spec.operations.add(op);
            }
        }
    }

    /**
     * Extract production endpoint URL from endpointConfig JSON.
     * Handles production_endpoints.url, production_endpoints.list[0].url, or list[0] as string URL.
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
                JSONObject prodObj = (JSONObject) prod;
                Object url = prodObj.get(APIConstants.API_DATA_URL);
                if (url != null && StringUtils.isNotBlank(url.toString())) {
                    return url.toString().trim();
                }
                Object list = prodObj.get("list");
                if (list instanceof JSONArray && !((JSONArray) list).isEmpty()) {
                    Object first = ((JSONArray) list).get(0);
                    if (first instanceof JSONObject) {
                        Object listUrl = ((JSONObject) first).get(APIConstants.API_DATA_URL);
                        if (listUrl != null && StringUtils.isNotBlank(listUrl.toString())) {
                            return listUrl.toString().trim();
                        }
                    } else if (first != null && StringUtils.isNotBlank(first.toString())) {
                        return first.toString().trim();
                    }
                }
            }
            if (prod instanceof JSONArray) {
                JSONArray list = (JSONArray) prod;
                if (!list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof JSONObject) {
                        Object url = ((JSONObject) first).get(APIConstants.API_DATA_URL);
                        if (url != null && StringUtils.isNotBlank(url.toString())) {
                            return url.toString().trim();
                        }
                    } else if (first != null && StringUtils.isNotBlank(first.toString())) {
                        return first.toString().trim();
                    }
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

    /**
     * Top-level deployment YAML DTO (apiVersion/kind/metadata/spec).
     * Shape is compatible with API Platform APIDeploymentYAML so the same gateway binary can consume it.
     */
    private static class DeploymentYaml {
        public String apiVersion;
        public String kind;
        public DeploymentMetadata metadata;
        public ApiSpec spec;
    }

    private static class DeploymentMetadata {
        public String name;
    }

    /**
     * spec section DTO: displayName, version, context, upstream, policies, operations.
     */
    private static class ApiSpec {
        public String displayName;
        public String version;
        public String context;
        public UpstreamYaml upstream;
        public List<PolicyDto> policies = new java.util.ArrayList<>();
        public List<OperationYaml> operations = new java.util.ArrayList<>();
    }

    private static class UpstreamYaml {
        public UpstreamTarget main;
        public UpstreamTarget sandbox;
    }

    private static class UpstreamTarget {
        public String url;
        public String ref;
    }

    /**
     * Generic policy DTO aligned with api-platform's Policy:
     * name, version (major-only), executionCondition, params (arbitrary JSON).
     */
    private static class PolicyDto {
        public String name;
        public String version;
        public String executionCondition;
        public Map<String, Object> params;
    }

    /**
     * Operation DTO: method, path, and attached policies.
     */
    private static class OperationYaml {
        public String method;
        public String path;
        public List<PolicyDto> policies = new java.util.ArrayList<>();
    }
}
