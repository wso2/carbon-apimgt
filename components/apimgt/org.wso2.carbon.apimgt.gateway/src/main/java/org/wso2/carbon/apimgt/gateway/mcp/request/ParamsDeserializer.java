package org.wso2.carbon.apimgt.gateway.mcp.request;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ParamsDeserializer implements JsonDeserializer<Params>  {
    private static final Log log = LogFactory.getLog(ParamsDeserializer.class);

    @Override
    public Params deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws
            JsonParseException {
        Params params = new Params();

        if (json != null && !json.isJsonNull()) {
            JsonObject obj = json.getAsJsonObject();
            JsonObject args = obj.getAsJsonObject("arguments");

            //iterate through the json object and print key value pairs
            if (args == null || args.isJsonNull()) {
                params.setArguments(new HashMap<>());
            } else {
                Map<String, Object> arguments = new HashMap<>();
                for (String key : args.keySet()) {
                    JsonElement value = args.get(key);
                    if (value != null && value.isJsonPrimitive()) {
                        JsonPrimitive prim = value.getAsJsonPrimitive();
                        if (prim.isString()) {
                            arguments.put(key, prim.getAsString());
                        } else if (prim.isNumber()) {
                            // Distinguish between int and double
                            Number num = prim.getAsNumber();
                            if (num.doubleValue() == num.longValue()) {
                                arguments.put(key, num.longValue());
                            } else {
                                arguments.put(key, num.doubleValue());
                            }
                        } else {
                            arguments.put(key, prim);
                        }
                    } else if (value != null && (value.isJsonObject() || value.isJsonArray() || value.isJsonNull())) {
                        // For complex types, store the JsonElement itself
                        arguments.put(key, value);
                    }
                    params.setArguments(arguments);
                }
            }

            // Let Gson handle the rest
            params.setProtocolVersion(context.deserialize(obj.get("protocolVersion"), String.class));
            params.setCapabilities(context.deserialize(obj.get("capabilities"), Params.Capabilities.class));
            params.setClientInfo(context.deserialize(obj.get("clientInfo"), Params.ClientInfo.class));
            params.setToolName(context.deserialize(obj.get("name"), String.class));
            params.setCursor(context.deserialize(obj.get("cursor"), String.class));
        }
        return params;
    }
}
