package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper Class utilities for AsyncApiV3Parser to keep parsing and template building logic reusable.
 */
public class AsyncApiV3ParserUtil {
    private static final Log log = LogFactory.getLog(AsyncApiV3ParserUtil.class);

    private AsyncApiV3ParserUtil() {
        // static util
    }

    /**
     * This method was creaet to parse YAML/JSON into JsonNode (YAMLFactory handles JSON too)
     */
    public static JsonNode parseToJsonNode(String apiDefinition) throws APIManagementException {
        if (apiDefinition == null || apiDefinition.trim().isEmpty()) {
            throw new APIManagementException("AsyncAPI definition is empty or null.");
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readTree(apiDefinition);
        } catch (IOException e) {
            String msg = "Failed to parse AsyncAPI definition: " + e.getMessage();
            log.debug("[AsyncApiV3ParserHelper] parseToJsonNode failed: " + msg);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Return a map of name -> JsonNode for either root.mapKey or root.parentKey.mapKey.
     * If parentKey is null, reads root.mapKey.
     */
    public static Map<String, JsonNode> getMap(JsonNode root, String parentKey, String mapKey) {
        Map<String, JsonNode> map = new LinkedHashMap<>();
        if (root == null || root.isMissingNode()) {
            return map;
        }
        JsonNode node = (parentKey != null) ? root.path(parentKey).path(mapKey) : root.path(mapKey);
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            it.forEachRemaining(entry -> {
                if (entry.getValue() != null && !entry.getValue().isNull()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            });
        }
        return map;
    }

    public static String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) return null;
        JsonNode n = node.path(field);
        return n.isTextual() ? n.asText() : null;
    }

    /**
     * Extract channel name from a $ref like "#/channels/myChannel" (returns "myChannel").
     * If format differs, returns last path segment as fallback.
     */
    public static String extractChannelNameFromRef(String ref) {
        if (ref == null) return null;
        final String prefix = "#/channels/";
        if (ref.startsWith(prefix)) {
            return ref.substring(prefix.length());
        }
        int lastSlash = ref.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < ref.length() - 1) {
            return ref.substring(lastSlash + 1);
        }
        return ref;
    }

    /**
     * Build a URITemplate by reading vendor extensions and x-scopes from operation/channel JSON nodes.
     */
    public static URITemplate buildURITemplate(
            String target,
            String verb,
            JsonNode operationNode,
            JsonNode channelNode,
            Set<Scope> scopes) throws APIManagementException {

        URITemplate template = new URITemplate();
        template.setHTTPVerb(verb);
        template.setHttpVerbs(verb);
        template.setUriTemplate(target);

        // auth type: channel-level override -> operation-level
        String authType = null;
        JsonNode chAuth = channelNode.path("x-auth-type");
        if (chAuth.isTextual()) {
            authType = chAuth.asText();
        } else {
            JsonNode opAuth = operationNode.path("x-auth-type");
            if (opAuth.isTextual()) {
                authType = opAuth.asText();
            }
        }
        if (StringUtils.isNotBlank(authType)) {
            template.setAuthType(authType);
        }

        // x-scopes extraction (array | object | string)
        java.util.List<String> opScopes = new java.util.ArrayList<>();
        JsonNode xScopes = operationNode.path("x-scopes");
        if (xScopes != null && !xScopes.isMissingNode()) {
            if (xScopes.isArray()) {
                xScopes.forEach(n -> { if (n.isTextual()) opScopes.add(n.asText()); });
            } else if (xScopes.isObject()) {
                xScopes.fields().forEachRemaining(f -> {
                    JsonNode v = f.getValue();
                    if (v.isTextual()) opScopes.add(v.asText());
                });
            } else if (xScopes.isTextual()) {
                opScopes.add(xScopes.asText());
            }
        }

        if (!opScopes.isEmpty()) {
            if (opScopes.size() == 1) {
                Scope scope = APISpecParserUtil.findScopeByKey(scopes, opScopes.get(0));
                if (scope == null) {
                    throw new APIManagementException("Scope '" + opScopes.get(0) + "' not found.");
                }
                template.setScope(scope);
                template.setScopes(scope);
            } else {
                for (String sName : opScopes) {
                    Scope scope = APISpecParserUtil.findScopeByKey(scopes, sName);
                    if (scope == null) {
                        throw new APIManagementException("Resource Scope '" + sName + "' not found.");
                    }
                    template.setScopes(scope);
                }
            }
        }
        return template;
    }

    public static Map<String, String> getWSUriMapping(JsonNode root) {
        Map<String, String> wsUriMapping = new LinkedHashMap<>();
        if (root == null || root.isMissingNode()) {
            return wsUriMapping;
        }

        // Prefer components.channels, otherwise root.channels
        Map<String, JsonNode> channels = getMap(root, "components", "channels");
        if (channels.isEmpty()) {
            channels = getMap(root, null, "channels");
        }

        if (channels.isEmpty()) {
            return wsUriMapping;
        }

        for (Map.Entry<String, JsonNode> entry : channels.entrySet()) {
            String channelKey = entry.getKey();
            JsonNode channel = entry.getValue();
            if (channel == null || channel.isNull()) {
                continue;
            }

            // helper to resolve x-uri-mapping from an operation node (publish/subscribe)
            java.util.function.Function<JsonNode, String> resolveXUri = (opNode) -> {
                if (opNode == null || opNode.isMissingNode() || opNode.isNull()) {
                    return null;
                }
                JsonNode xUri = opNode.path("x-uri-mapping");
                if (xUri != null && xUri.isTextual()) {
                    return xUri.asText();
                }
                // Some specs put vendor extensions under "extensions" or nested locations; attempt a conservative fallback:
                JsonNode extensions = opNode.path("extensions");
                if (extensions != null && extensions.isObject()) {
                    JsonNode alt = extensions.path("x-uri-mapping");
                    if (alt != null && alt.isTextual()) {
                        return alt.asText();
                    }
                }
                // Also check within "bindings" extension or other common vendor spots if necessary (kept small on purpose).
                return null;
            };

            // publish
            String publishUri = resolveXUri.apply(channel.path("publish"));
            if (publishUri != null) {
                wsUriMapping.put("PUBLISH_" + channelKey, publishUri);
            }

            // subscribe
            String subscribeUri = resolveXUri.apply(channel.path("subscribe"));
            if (subscribeUri != null) {
                wsUriMapping.put("SUBSCRIBE_" + channelKey, subscribeUri);
            }
        }

        return wsUriMapping;
    }
}
