package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {
    public static String safeGetAsString(JsonObject obj, String memberName) {
        JsonElement element = obj.get(memberName);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
    }
}
