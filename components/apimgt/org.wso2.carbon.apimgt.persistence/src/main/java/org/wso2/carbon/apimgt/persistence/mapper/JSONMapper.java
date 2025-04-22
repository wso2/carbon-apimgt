package org.wso2.carbon.apimgt.persistence.mapper;

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class JSONMapper {
    private static final Log log = LogFactory.getLog(JSONMapper.class);
    private static final Gson gson = new Gson();

    public JsonObject mapApiToJson(API api) {
        JsonObject json = new JsonObject();
        json.addProperty("id", api.getId().toString());
        json.addProperty("uuid", api.getUuid());
        json.addProperty("url", api.getUrl());
        json.addProperty("sandboxUrl", api.getSandboxUrl());
        json.addProperty("wsdlUrl", api.getWsdlUrl());
        json.addProperty("wadlUrl", api.getWadlUrl());
        json.addProperty("swaggerDefinition", api.getSwaggerDefinition());
        json.addProperty("graphQLSchema", api.getGraphQLSchema());
        json.addProperty("asyncAPIDefinition", api.getAsyncApiDefinition());
        json.addProperty("type", api.getType());
        json.addProperty("subType", api.getSubtype());
        json.addProperty("context",  api.getContext());
        json.addProperty("contextTemplate", api.getContextTemplate());
        json.addProperty("thumbnailUrl", api.getThumbnailUrl());
        json.add("tags", setToJSONArray(api.getTags()));
        json.add("documents", setToJSONArray(api.getDocuments()));
        json.addProperty("httpVerb", api.getHttpVerb());
        json.addProperty("lastUpdated", dateToString(api.getLastUpdated()));
        json.addProperty("updatedBy", api.getUpdatedBy());
        json.addProperty("organization", api.getOrganization());
        json.addProperty("status", api.getStatus());
        json.addProperty("technicalOwner", api.getTechnicalOwner());
        json.addProperty("technicalOwnerEmail", api.getTechnicalOwnerEmail());
        json.addProperty("businessOwner", api.getBusinessOwner());
        json.addProperty("businessOwnerEmail", api.getBusinessOwnerEmail());
        json.addProperty("visibility", api.getVisibility());
        json.addProperty("visibilityRoles", api.getVisibleRoles());
        json.addProperty("visibleTenants", api.getVisibleTenants());
        json.addProperty("apiOwner", api.getApiOwner());
        json.addProperty("redirectUrl", api.getRedirectURL());
        json.addProperty("implementation", api.getImplementation());
        json.addProperty("monetizationCategory", api.getMonetizationCategory());
        json.addProperty("isEgress", api.isEgress());
        return json;
    }

    private static <T> JsonArray setToJSONArray(Set<T> items) {
        JsonArray jsonArray = new JsonArray();
        for (T item: items) {
            JsonElement jsonElement = gson.toJsonTree(item);
            jsonArray.add(jsonElement);
        }
        return jsonArray;
    }

    public String formatJSONString(String definition) {
        if (definition == null) return null;
        String formattedString = "";
        try {
            JsonElement jsonElement = JsonParser.parseString(definition);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            formattedString = gson.toJson(jsonElement);
        } catch (Exception e) {
            log.error("Failed to parse Swagger Definition!, " + e);
        }
        return formattedString;
    }

    private static String dateToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
