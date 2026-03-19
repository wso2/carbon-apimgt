/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

/**
 * Wraps platform gateway wire JSON for storage in {@code AM_GW_PLATFORM_EVENT.PAYLOAD} and extracts
 * the wire message again for WebSocket delivery.
 *
 * <p>Envelope shape: {@code { metadata?, message } }. {@code metadata} holds arbitrary key/value pairs
 * (strings, numbers, booleans, or nested structures via Gson). It is omitted when empty.</p>
 *
 * <p>Detection: a stored object is treated as an envelope when it has a top-level {@code message} and
 * no top-level {@code type} (platform gateway wire messages always include {@code type} at the root).</p>
 */
public final class PlatformGatewayEventEnvelopeUtil {

    private static final Log log = LogFactory.getLog(PlatformGatewayEventEnvelopeUtil.class);

    public static final String JSON_KEY_METADATA = "metadata";
    public static final String JSON_KEY_MESSAGE = "message";
    /** Present on all gateway wire JSON at the root; used to distinguish envelope wrapper from raw wire. */
    public static final String JSON_KEY_WIRE_TYPE = "type";

    private static final Gson GSON = new Gson();

    private PlatformGatewayEventEnvelopeUtil() {
    }

    /**
     * Build a stored payload: {@code { metadata?, message } }.
     * {@code metadata} is built from the map (null keys skipped; null values skipped; empty strings skipped).
     *
     * @param gatewayWireJson JSON the gateway expects on the wire (parsed into {@code message})
     * @param metadata        optional attributes for storage, routing, or auditing (any event type)
     */
    public static String wrapForStorage(String gatewayWireJson, Map<String, ?> metadata) {
        if (gatewayWireJson == null) {
            throw new IllegalArgumentException("gatewayWireJson is required");
        }
        if (log.isDebugEnabled()) {
            log.debug("Wrapping gateway wire JSON for storage; metadata entry count: "
                    + (metadata == null ? 0 : metadata.size()));
        }
        JsonObject root = new JsonObject();
        addMetadataIfAny(root, metadata);
        try {
            root.add(JSON_KEY_MESSAGE, JsonParser.parseString(gatewayWireJson));
        } catch (JsonSyntaxException e) {
            root.addProperty(JSON_KEY_MESSAGE, gatewayWireJson);
        }
        return root.toString();
    }

    /**
     * Same as {@link #wrapForStorage(String, Map)} with no metadata (omits {@code metadata} key).
     */
    public static String wrapForStorage(String gatewayWireJson) {
        return wrapForStorage(gatewayWireJson, null);
    }

    private static void addMetadataIfAny(JsonObject root, Map<String, ?> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        JsonObject meta = new JsonObject();
        for (Map.Entry<String, ?> e : metadata.entrySet()) {
            String key = e.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }
            Object value = e.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                continue;
            }
            meta.add(key, GSON.toJsonTree(value));
        }
        if (meta.size() > 0) {
            root.add(JSON_KEY_METADATA, meta);
        }
    }

    /**
     * If {@code storedPayload} is an envelope ({@code message} at root, no root {@code type}), return
     * {@code message} serialized for the gateway; otherwise return {@code storedPayload} unchanged
     * (legacy raw wire JSON or rows that only look like wire documents).
     */
    public static String toWirePayload(String storedPayload) {
        if (storedPayload == null || storedPayload.isEmpty()) {
            return storedPayload;
        }
        try {
            JsonElement root = JsonParser.parseString(storedPayload);
            if (!root.isJsonObject()) {
                return storedPayload;
            }
            JsonObject obj = root.getAsJsonObject();
            if (!obj.has(JSON_KEY_MESSAGE) || obj.has(JSON_KEY_WIRE_TYPE)) {
                return storedPayload;
            }
            JsonElement msg = obj.get(JSON_KEY_MESSAGE);
            if (msg.isJsonNull()) {
                return storedPayload;
            }
            if (msg.isJsonPrimitive() && msg.getAsJsonPrimitive().isString()) {
                return msg.getAsString();
            }
            return msg.toString();
        } catch (JsonSyntaxException e) {
            return storedPayload;
        }
    }
}
