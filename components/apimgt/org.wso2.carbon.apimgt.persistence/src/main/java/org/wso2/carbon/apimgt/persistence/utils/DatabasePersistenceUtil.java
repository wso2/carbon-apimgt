package org.wso2.carbon.apimgt.persistence.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabasePersistenceUtil {
    private static final Log log = LogFactory.getLog(DatabasePersistenceUtil.class);
    private static final Gson gson = new Gson();

    public static JsonObject mapApiToJson(API api) {
        JsonObject jsonObject = gson.toJsonTree(api).getAsJsonObject();
        jsonObject.remove("swaggerDefinition");
        return jsonObject;
    }

    public static JsonObject mapOrgToJson(Organization org) {
        return gson.toJsonTree(org).getAsJsonObject();
    }

    public static JsonObject mapDocumentToJson(org.wso2.carbon.apimgt.persistence.dto.Documentation documentation) {
        return gson.toJsonTree(documentation).getAsJsonObject();
    }

    public static JsonObject stringTojsonObject(String jsonString) {
        return JsonParser.parseString(jsonString).getAsJsonObject();
    }

    public static API jsonToApi(JsonObject jsonObject) {
        return gson.fromJson(jsonObject, API.class);
    }

    private static JsonObject getIdObject(APIIdentifier apiId) {
        JsonObject id = new JsonObject();
        id.addProperty("providerName", apiId.getProviderName());
        id.addProperty("apiName", apiId.getApiName());
        id.addProperty("version", apiId.getVersion());
        id.addProperty("tier", apiId.getTier());
        id.addProperty("applicationId", apiId.getApplicationId());
        id.addProperty("uuid", apiId.getUUID());
        id.addProperty("id", apiId.getId());
        id.addProperty("organization", apiId.getOrganization());
        return id;
    }

    public static <T> JsonArray setToJSONArray(Set<T> items) {
        JsonArray jsonArray = new JsonArray();
        for (T item: items) {
            JsonElement jsonElement = gson.toJsonTree(item);
            jsonArray.add(jsonElement);
        }
        return jsonArray;
    }

    public static <T> JsonArray listToJSONArray(List<T> items) {
        JsonArray jsonArray = new JsonArray();
        for (T item: items) {
            JsonElement jsonElement = gson.toJsonTree(item);
            jsonArray.add(jsonElement);
        }
        return jsonArray;
    }

    public static JsonElement corsConfigToJson(CORSConfiguration corsConfiguration) {
        JsonElement element = new JsonObject();
        element = gson.toJsonTree(corsConfiguration);

        return element;
    }

    public static String dateToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public static Date stringToDate(String dateString) {
        if (dateString == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return formatter.parse(dateString);
        } catch (Exception e) {
            log.error("Failed to parse date string: " + dateString, e);
            return null;
        }
    }

    public static Set<String> jsonArrayToSet(JsonArray jsonArray) {
        Set<String> resultSet = new HashSet<>();
        for (JsonElement element: jsonArray) {
            resultSet.add(element.getAsString());
        }
        return resultSet;
    }

    public static String getFormattedJsonString(String jsonString) {
        String formattedString = null;
        try {
            if (jsonString != null && jsonString.length() > 2) {
                if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                    formattedString = unescapeJson(jsonString);
                } else if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                    formattedString = unescapeJson(jsonString);
                } else if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                    // Handle JSON string with quotes
                    String jsonStringWithoutQuotes = jsonString.substring(1, jsonString.length() - 1);
                    formattedString = unescapeJson(jsonStringWithoutQuotes);
                }
                ObjectMapper mapper = new ObjectMapper();
//                formattedString = fixDoubleQuotes(jsonString);
                JsonNode root = mapper.readTree(jsonString);
                cleanJson(root);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to format JSON string", e);
        }
        return formattedString;
    }

    public static String getFormattedJsonStringToSave(JsonObject json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyjson = gson.toJson(json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prettyjson);
            cleanJson(root);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to format JSON string", e);
        }
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

    private static String fixDoubleQuotes(String json) {
        Pattern pattern = Pattern.compile("\"\"([^\"]*?)\"\"");
        Matcher matcher = pattern.matcher(json);

        return matcher.replaceAll("\"$1\"");
    }

    public static String safeGetAsString(JsonObject obj, String memberName) {
        JsonElement element = obj.get(memberName);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
    }

    public API mapJsonStringToAPI(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        String providerName = DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "providerName");
        String apiName = DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "apiName");
        String version = DatabasePersistenceUtil.safeGetAsString(jsonObject.getAsJsonObject("id"), "version");
        String uuid = DatabasePersistenceUtil.safeGetAsString(jsonObject, "uuid");

        APIIdentifier id = new APIIdentifier(providerName, apiName, version, uuid);

        API api = new API(id);
        api.setUuid(uuid);
        api.setDescription(DatabasePersistenceUtil.safeGetAsString(jsonObject, "description"));
        api.setStatus(DatabasePersistenceUtil.safeGetAsString(jsonObject, "status"));
        api.setThumbnailUrl(DatabasePersistenceUtil.safeGetAsString(jsonObject, "thumbnailUrl"));
        api.setWsdlUrl(DatabasePersistenceUtil.safeGetAsString(jsonObject, "wsdlUrl"));
        api.setWadlUrl(DatabasePersistenceUtil.safeGetAsString(jsonObject, "wadlUrl"));
        api.setTechnicalOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwner"));
        api.setTechnicalOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "technicalOwnerEmail"));
        api.setBusinessOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwner"));
        api.setBusinessOwnerEmail(DatabasePersistenceUtil.safeGetAsString(jsonObject, "businessOwnerEmail"));
        api.setVisibility(DatabasePersistenceUtil.safeGetAsString(jsonObject, "visibility"));
        api.setVisibleRoles(DatabasePersistenceUtil.safeGetAsString(jsonObject, "visibilityRoles"));
        api.setVisibleTenants(DatabasePersistenceUtil.safeGetAsString(jsonObject, "visibleTenants"));
        api.setEndpointSecured(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "endpointSecured")));
//        api.setEndpointUTUsername();
//        api.setEndpointUTPassword(JsonUtils.safeGetAsString(jsonObject, "endpointUTPassword"));
        api.setTransports(DatabasePersistenceUtil.safeGetAsString(jsonObject, "transports"));
//        api.setInSequence();
//        api.setOutSequence();
//        api.setFaultSequence();
        api.setResponseCache(DatabasePersistenceUtil.safeGetAsString(jsonObject, "responseCache"));
        api.setImplementation(DatabasePersistenceUtil.safeGetAsString(jsonObject, "implementation"));
        api.setType(DatabasePersistenceUtil.safeGetAsString(jsonObject, "type"));
        api.setProductionMaxTps(DatabasePersistenceUtil.safeGetAsString(jsonObject, "productionMaxTps"));
        api.setProductionTimeUnit(DatabasePersistenceUtil.safeGetAsString(jsonObject, "productionTimeUnit"));
        api.setSandboxMaxTps(DatabasePersistenceUtil.safeGetAsString(jsonObject, "sandboxMaxTps"));
        api.setSandboxTimeUnit(DatabasePersistenceUtil.safeGetAsString(jsonObject, "sandboxTimeUnit"));

        BackendThrottlingConfiguration backendThrottlingConfiguration = new BackendThrottlingConfiguration();
        backendThrottlingConfiguration.setProductionMaxTps(DatabasePersistenceUtil.safeGetAsString(jsonObject, "backendProductionMaxTps"));
        backendThrottlingConfiguration.setProductionTimeUnit(DatabasePersistenceUtil.safeGetAsString(jsonObject, "backendProductionTimeUnit"));
        backendThrottlingConfiguration.setSandboxMaxTps(DatabasePersistenceUtil.safeGetAsString(jsonObject, "backendSandboxMaxTps"));
        backendThrottlingConfiguration.setSandboxTimeUnit(DatabasePersistenceUtil.safeGetAsString(jsonObject, "backendSandboxTimeUnit"));

        api.setBackendThrottlingConfiguration(backendThrottlingConfiguration);
        api.setGatewayVendor(DatabasePersistenceUtil.safeGetAsString(jsonObject, "gatewayVendor"));
        api.setAsyncTransportProtocols(DatabasePersistenceUtil.safeGetAsString(jsonObject, "asyncTransportProtocols"));

        int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
        try {
            String cacheTimeoutString = DatabasePersistenceUtil.safeGetAsString(jsonObject, "cacheTimeout");
            if (cacheTimeoutString == null && !cacheTimeoutString.isEmpty()) {
                cacheTimeout = Integer.parseInt(DatabasePersistenceUtil.safeGetAsString(jsonObject, "cacheTimeout"));
            }
        } catch (NumberFormatException e) {
            log.error("Invalid cache timeout value. Using default value: " + APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

        api.setCacheTimeout(cacheTimeout);
        api.setEndpointConfig(DatabasePersistenceUtil.safeGetAsString(jsonObject, "endpointConfig"));
        api.setRedirectURL(DatabasePersistenceUtil.safeGetAsString(jsonObject, "redirectUrl"));
        api.setApiExternalProductionEndpoint(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiExternalProductionEndpoint"));
        api.setApiExternalSandboxEndpoint(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiExternalSandboxEndpoint"));
        api.setApiOwner(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiOwner"));
        api.setAdvertiseOnly(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "advertiseOnly")));
        api.setSubscriptionAvailability(DatabasePersistenceUtil.safeGetAsString(jsonObject, "subscriptionAvailability"));
        api.setSubscriptionAvailableTenants(DatabasePersistenceUtil.safeGetAsString(jsonObject, "subscriptionAvailableTenants"));

        String tenantDomainName = MultitenantUtils.getTenantDomain((providerName));
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);
        } catch (UserStoreException e) {
            throw new RuntimeException(e);
        }

        String tiers = DatabasePersistenceUtil.safeGetAsString(jsonObject, "tiers");
        Set<Tier> availableTiers = new HashSet<Tier>();
        if(tiers != null) {
            String[] tiersArray = tiers.split("\\|\\|");
            for(String tierName : tiersArray) {
                availableTiers.add(new Tier(tierName));
            }
        }
        api.setAvailableTiers(availableTiers);

        String organizationTiers =DatabasePersistenceUtil.safeGetAsString(jsonObject, "availableTiersForOrganizations");
        api.setAvailableTiersForOrganizations(getAvailableTiersForOrganizationsFromString(organizationTiers));

        api.setContext(DatabasePersistenceUtil.safeGetAsString(jsonObject, "context"));
        api.setContextTemplate(DatabasePersistenceUtil.safeGetAsString(jsonObject, "contextTemplate"));
        api.setLatest(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "isLatest")));
        api.setEnableSchemaValidation(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "enableSchemaValidation")));
        api.setEnableSubscriberVerification(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "enableSubscriberVerification")));
        api.setEnableStore(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "enableStore")));
        api.setTestKey(DatabasePersistenceUtil.safeGetAsString(jsonObject, "testKey"));
        api.setTags(jsonArrayToSet(jsonObject.getAsJsonArray("tags")));
        api.setLastUpdated(new Date());
        api.setCreatedTime(DatabasePersistenceUtil.safeGetAsString(jsonObject, "createdTime"));
        api.setImplementation(DatabasePersistenceUtil.safeGetAsString(jsonObject, "implementation"));
        api.setEnvironments(getEnvironments(DatabasePersistenceUtil.safeGetAsString(jsonObject, "environments")));
        api.setAuthorizationHeader(DatabasePersistenceUtil.safeGetAsString(jsonObject, "authorizationHeader"));
        api.setApiKeyHeader(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiKeyHeader"));
        api.setApiSecurity(DatabasePersistenceUtil.safeGetAsString(jsonObject, "apiSecurity"));
        api.setMonetizationEnabled(getAsBoolean(DatabasePersistenceUtil.safeGetAsString(jsonObject, "isMonetizationEnabled")));
        api.setAudiences(jsonArrayToSet(jsonObject.getAsJsonArray("audiences")));
        api.setAudience(DatabasePersistenceUtil.safeGetAsString(jsonObject, "audience"));
        api.setVersionTimestamp(DatabasePersistenceUtil.safeGetAsString(jsonObject, "versionTimestamp"));

        String monetizationInfo = DatabasePersistenceUtil.safeGetAsString(jsonObject, "monetizationProperties");

        if (StringUtils.isNotBlank(monetizationInfo)) {
            JSONParser parser = new JSONParser();
            JSONObject monetizationInfoJson = null;
            try {
                monetizationInfoJson = (JSONObject) parser.parse(monetizationInfo);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            api.setMonetizationProperties(monetizationInfoJson);
        }
//        api.setApiCategories(JsonUtils.safeGetAsString(jsonObject, "apiCategories"));


        return api;
    }

    private static boolean getAsBoolean(String value) {
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT, APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    public static Set<org.wso2.carbon.apimgt.api.model.OrganizationTiers> getAvailableTiersForOrganizationsFromString(
            String tiersString) {

        if (tiersString == null || tiersString.isEmpty()) {
            return new LinkedHashSet<>();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            org.wso2.carbon.apimgt.api.model.OrganizationTiers[] tiersArray = objectMapper.readValue(tiersString,
                    org.wso2.carbon.apimgt.api.model.OrganizationTiers[].class);
            return new LinkedHashSet<>(Arrays.asList(tiersArray));
        } catch (Exception e) {
            log.error("Error while converting string to availableTiersForOrganizations object", e);
            return new LinkedHashSet<>();
        }
    }

    private static Set<String> getEnvironments(String environments) {
        if(environments != null) {
            String[] publishEnvironmentArray = environments.split(",");
            return new HashSet<String>(Arrays.asList(publishEnvironmentArray));
        }
        return null;
    }

    public static PublisherAPI getAPIForSearch(JsonObject json) {
        PublisherAPI api = new PublisherAPI();

      try {
          api.setContext(safeGetAsString(json, "context"));
          api.setDescription(safeGetAsString(json, "description"));
          api.setId(safeGetAsString(json.get("id").getAsJsonObject(), "uuid"));
          api.setStatus(safeGetAsString(json, "status"));
          api.setApiName(safeGetAsString(json.get("id").getAsJsonObject(), "apiName"));
          api.setProviderName(safeGetAsString(json.get("id").getAsJsonObject(), "providerName"));
          api.setVersion(safeGetAsString(json.get("id").getAsJsonObject(), "version"));
          api.setAdvertiseOnly(getAsBoolean(safeGetAsString(json, "advertiseOnly")));
          api.setThumbnail(safeGetAsString(json, "thumbnailUrl"));
          api.setBusinessOwner(safeGetAsString(json, "businessOwner"));
          api.setBusinessOwnerEmail(safeGetAsString(json, "businessOwnerEmail"));
          api.setTechnicalOwner(safeGetAsString(json, "technicalOwner"));
          api.setTechnicalOwnerEmail(safeGetAsString(json, "technicalOwnerEmail"));
          api.setMonetizationStatus(getAsBoolean(safeGetAsString(json, "monetizationStatus")));
      } catch (Exception e) {
          throw new RuntimeException("Error while converting JSON to PublisherAPI object", e);
      }

      return api;
    }

    public static org.wso2.carbon.apimgt.persistence.dto.Documentation jsonToDocument(JsonObject json) {
        try {
            String docName = safeGetAsString(json, "name");
            org.wso2.carbon.apimgt.persistence.dto.DocumentationType docType = org.wso2.carbon.apimgt.persistence.dto.DocumentationType.valueOf(Objects.requireNonNull(safeGetAsString(json, "type")).toUpperCase());

            org.wso2.carbon.apimgt.persistence.dto.Documentation documentation = new org.wso2.carbon.apimgt.persistence.dto.Documentation(docType, docName);

            documentation.setId(safeGetAsString(json, "id"));
            documentation.setSummary(safeGetAsString(json, "summary"));
            documentation.setSourceType(DocumentationInfo.DocumentSourceType.valueOf(Objects.requireNonNull(safeGetAsString(json, "sourceType")).toUpperCase()));
            documentation.setSourceUrl(safeGetAsString(json, "sourceUrl"));
            documentation.setVisibility(org.wso2.carbon.apimgt.persistence.dto.Documentation.DocumentVisibility.valueOf(Objects.requireNonNull(safeGetAsString(json, "visibility")).toUpperCase()));
            documentation.setCreatedDate(stringToDate(safeGetAsString(json, "createdTime")));
            documentation.setLastUpdated(stringToDate(safeGetAsString(json, "lastUpdatedTime")));
            return documentation;
        } catch (Exception e) {
            throw new RuntimeException("Error while converting JSON to Documentation object", e);
        }
    }

    public static DocumentContent jsonToDocumentationContent(JsonObject json) {
        DocumentContent documentContent = new DocumentContent();
        try {
           documentContent.setTextContent(safeGetAsString(json, "textContent"));
           documentContent.setSourceType(DocumentContent.ContentSourceType.valueOf(Objects.requireNonNull(safeGetAsString(json, "sourceType")).toUpperCase()));

           if (documentContent.getSourceType().equals(DocumentContent.ContentSourceType.FILE)) {
               String fileType = safeGetAsString(json, "fileType");
               String fileName = safeGetAsString(json, "fileName");
               org.wso2.carbon.apimgt.persistence.dto.ResourceFile resourceFile = new org.wso2.carbon.apimgt.persistence.dto.ResourceFile(null, fileType);
               resourceFile.setName(fileName);
               documentContent.setResourceFile(resourceFile);
           }
        } catch (Exception e) {
            throw new RuntimeException("Error while converting JSON to DocumentationContent object", e);
        }
        return documentContent;
    }

    public static long getDocumentFileLength(InputStream inputStream) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[4096];
        int nRead;
        while (true) {
            try {
                if ((nRead = inputStream.read(data, 0, data.length)) == -1) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            buffer.write(data, 0, nRead);
        }

        byte[] byteArray = buffer.toByteArray();
        return byteArray.length;
    }

    public static String getSearchQuery(String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return searchQuery.split(":", 2)[1].trim();
        }
        return null;
    }

    public static String convertToBase64(InputStream inputStream, String contentType) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] fileBytes = outputStream.toByteArray();
        String base64Encoded = Base64.getEncoder().encodeToString(fileBytes);

        return "data:" + contentType + ";base64," + base64Encoded;
    }

}
