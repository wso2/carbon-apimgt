package org.wso2.carbon.apimgt.persistence.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;

public class JsonUtils {
    private static final Log log = LogFactory.getLog(JsonUtils.class);

    public static String safeGetAsString(JsonObject obj, String memberName) {
        JsonElement element = obj.get(memberName);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
    }

    public static String getFormattedJsonString(String jsonString) {
        String formattedString = null;
        try {
            if (jsonString != null && jsonString.length() > 2) {
                String jsonStringWithoutQuotes = jsonString.substring(1, jsonString.length() - 1);
                formattedString = unescapeJson(jsonStringWithoutQuotes);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(formattedString);
                cleanJson(root);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to format JSON string", e);
        }
        return formattedString;
    }

    private static String unescapeJson(String input) {
        return input.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static void cleanJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode child = entry.getValue();
                if (child.isTextual()) {
                    String cleaned = child.asText().replace("\n", " ");
                    objNode.set(entry.getKey(), new TextNode(cleaned));
                } else {
                    cleanJson(child); // Recursive call
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                cleanJson(item); // Recursive call for each array item
            }
        }
    }
}
