package org.wso2.carbon.apimgt.persistence.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.*;
import org.wso2.carbon.apimgt.persistence.dto.OrganizationTiers;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    public static Date stringTimestampToDate(String timestamp) {
        if (timestamp == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            try {
                return formatter.parse(new Date(Long.parseLong(timestamp)).toString());
            } catch (NumberFormatException e) {
                // If the timestamp is not a long, try parsing it as a date string
                return formatter.parse(timestamp);
            }
        } catch (Exception e) {
            log.error("Failed to parse timestamp string: " + timestamp, e);
            return null;
        }
    }

    public static String dateToTimestampString(Date date) {
        return String.valueOf(date.getTime());
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

    public static String safeGetAsString(JsonObject obj, String memberName) {
        JsonElement element = obj.get(memberName);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
    }

    public static JsonObject mapApiProductToJson(APIProduct apiProduct) {
        JsonObject jsonObject = gson.toJsonTree(apiProduct).getAsJsonObject();
        jsonObject.remove("definition");
        return jsonObject;
    }

    public static APIProduct jsonToApiProduct(JsonObject jsonObject) {
       return gson.fromJson(jsonObject, APIProduct.class);
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

    public static Set<OrganizationTiers> getAvailableTiersForOrganizationsFromString(
            String tiersString) {

        if (tiersString == null || tiersString.isEmpty()) {
            return new LinkedHashSet<>();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            OrganizationTiers[] tiersArray = objectMapper.readValue(tiersString,OrganizationTiers[].class);
            return new LinkedHashSet<>(Arrays.asList(tiersArray));
        } catch (Exception e) {
            log.error("Error while converting string to availableTiersForOrganizations object", e);
            return new LinkedHashSet<>();
        }
    }

    public static PublisherAPI getAPIForSearch(JsonObject json) {
        PublisherAPI api = new PublisherAPI();

      try {
          api.setContext(safeGetAsString(json, "context"));
          api.setDescription(safeGetAsString(json, "description"));
          api.setId(safeGetAsString(json, "uuid"));
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

    public static PublisherAPIProduct getAPIProductForSearch(JsonObject json) {
        PublisherAPIProduct apiProduct = new PublisherAPIProduct();

        try {
            apiProduct.setContext(safeGetAsString(json, "context"));
            apiProduct.setDescription(safeGetAsString(json, "description"));
            apiProduct.setId(safeGetAsString(json, "uuid"));
            apiProduct.setState(safeGetAsString(json, "state"));
            apiProduct.setApiProductName(safeGetAsString(json.get("id").getAsJsonObject(), "apiProductName"));
            apiProduct.setProviderName(safeGetAsString(json.get("id").getAsJsonObject(), "providerName"));
            apiProduct.setVersion(safeGetAsString(json.get("id").getAsJsonObject(), "version"));
            apiProduct.setThumbnail(safeGetAsString(json, "thumbnailUrl"));
            apiProduct.setBusinessOwner(safeGetAsString(json, "businessOwner"));
            apiProduct.setBusinessOwnerEmail(safeGetAsString(json, "businessOwnerEmail"));
            apiProduct.setTechnicalOwner(safeGetAsString(json, "technicalOwner"));
            apiProduct.setTechnicalOwnerEmail(safeGetAsString(json, "technicalOwnerEmail"));
            apiProduct.setMonetizationStatus(getAsBoolean(safeGetAsString(json, "isMonetizationEnabled")));
        } catch (Exception e) {
            throw new RuntimeException("Error while converting JSON to PublisherAPIProduct object", e);
        }

        return apiProduct;
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

    private static String stripSpecialCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Pattern to match special characters at the start and end
        // This matches any non-alphanumeric character (except spaces) at beginning or end
        String pattern = "^[^a-zA-Z0-9/\\s]+|[^a-zA-Z0-9\\s]+$";

        // Keep applying the pattern until no more special characters at start/end
        String result = input;
        String previous;
        do {
            previous = result;
            result = result.replaceAll(pattern, "");
        } while (!result.equals(previous));

        return result.trim();
    }

    public static SearchQuery getSearchQuery(String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String[] query = searchQuery.split(APIConstants.CHAR_COLON, 2);

            if (query.length == 2) {
                String content = stripSpecialCharacters(query[1].trim());
                String type = query[0].trim();
                return new SearchQuery(content, type);
            } else {
                if (searchQuery.equals(APIConstants.CHAR_ASTERIX)) {
                    return null;
                }
                // If the search query does not contain a type, treat it as a general search
                return new SearchQuery(searchQuery.trim(), APIConstants.CONTENT_SEARCH_TYPE_PREFIX);
            }
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

    public static String createWsdlFileName(String provider, String apiName, String apiVersion) {

        return provider + "--" + apiName + apiVersion;
    }

    public static DevPortalAPIInfo mapAPItoAPIInfo(API api) {
        if (api == null) {
            return null;
        }

        DevPortalAPIInfo apiInfo = new DevPortalAPIInfo();
        apiInfo.setId(api.getUuid());
        apiInfo.setApiName(api.getId().getApiName());
        apiInfo.setVersion(api.getId().getVersion());
        apiInfo.setProviderName(api.getId().getProviderName());
        apiInfo.setContext(api.getContext());
        apiInfo.setType(api.getType());
        apiInfo.setThumbnail(api.getThumbnailUrl());
        apiInfo.setBusinessOwner(api.getBusinessOwner());
        apiInfo.setStatus(api.getStatus());
        apiInfo.setAvailableTierNames(api.getAvailableTiers().stream().map(Tier::getName).collect(Collectors.toSet()));
        apiInfo.setAvailableTiersForOrganizations(api.getAvailableTiersForOrganizations().stream().map(
                tier -> {
                    OrganizationTiers organizationTiers = new OrganizationTiers();
                    organizationTiers.setTiers(tier.getTiers().stream().map(Tier::getName).collect(Collectors.toSet()));
                    return organizationTiers;
                }
        ).collect(Collectors.toSet()));
        apiInfo.setSubscriptionAvailability(api.getSubscriptionAvailability());
        apiInfo.setSubscriptionAvailableOrgs(api.getSubscriptionAvailableTenants());
        apiInfo.setCreatedTime(dateToString(stringTimestampToDate(api.getCreatedTime())));
        apiInfo.setDescription(api.getDescription());
        apiInfo.setGatewayVendor(api.getGatewayVendor());
        apiInfo.setAdditionalProperties(api.getAdditionalProperties());
        apiInfo.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        apiInfo.setTechnicalOwner(api.getTechnicalOwner());
        apiInfo.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        apiInfo.setAdvertiseOnly(api.isAdvertiseOnly());

        return apiInfo;
    }

    public static DevPortalAPIInfo mapAPIProductToAPIInfo(APIProduct apiProduct) {
        if (apiProduct == null) {
            return null;
        }

        DevPortalAPIInfo apiInfo = new DevPortalAPIInfo();
        apiInfo.setId(apiProduct.getUuid());
        apiInfo.setApiName(apiProduct.getId().getName());
        apiInfo.setVersion(apiProduct.getId().getVersion());
        apiInfo.setProviderName(apiProduct.getId().getProviderName());
        apiInfo.setContext(apiProduct.getContext());
        apiInfo.setType(apiProduct.getType());
        apiInfo.setThumbnail(apiProduct.getThumbnailUrl());
        apiInfo.setBusinessOwner(apiProduct.getBusinessOwner());
        apiInfo.setStatus(apiProduct.getState());
        apiInfo.setAvailableTierNames(apiProduct.getAvailableTiers().stream().map(Tier::getName).collect(Collectors.toSet()));
        apiInfo.setSubscriptionAvailability(apiProduct.getSubscriptionAvailability());
        apiInfo.setSubscriptionAvailableOrgs(apiProduct.getSubscriptionAvailableTenants());
        apiInfo.setCreatedTime(dateToString(apiProduct.getCreatedTime()));
        apiInfo.setDescription(apiProduct.getDescription());
        apiInfo.setGatewayVendor(apiProduct.getGatewayVendor());
        apiInfo.setAdditionalProperties(apiProduct.getAdditionalProperties());

        return apiInfo;
    }

    public static PublisherAPI convertToPublisherAPI(PublisherAPIProduct publisherAPIProduct) {
        PublisherAPI publisherAPI = new PublisherAPI();
        publisherAPI.setId(publisherAPIProduct.getId());
        publisherAPI.setApiName(publisherAPIProduct.getApiProductName());
        publisherAPI.setProviderName(publisherAPIProduct.getProviderName());
        publisherAPI.setVersion(publisherAPIProduct.getVersion());
        publisherAPI.setContext(publisherAPIProduct.getContext());
        publisherAPI.setStatus(publisherAPIProduct.getState());
        publisherAPI.setThumbnail(publisherAPIProduct.getThumbnail());
        publisherAPI.setDescription(publisherAPIProduct.getDescription());
        publisherAPI.setBusinessOwner(publisherAPIProduct.getBusinessOwner());
        publisherAPI.setBusinessOwnerEmail(publisherAPIProduct.getBusinessOwnerEmail());
        publisherAPI.setTechnicalOwner(publisherAPIProduct.getTechnicalOwner());
        publisherAPI.setTechnicalOwnerEmail(publisherAPIProduct.getTechnicalOwnerEmail());
        publisherAPI.setAvailableTierNames(publisherAPIProduct.getAvailableTierNames());
        publisherAPI.setVisibleOrganizations(publisherAPIProduct.getVisibleOrganizations());
        publisherAPI.setSubscriptionAvailability(publisherAPIProduct.getSubscriptionAvailableOrgs());
        publisherAPI.setCreatedTime(publisherAPIProduct.getCreatedTime());
        publisherAPI.setLastUpdated(publisherAPIProduct.getLastUpdated());
        publisherAPI.setGatewayVendor(publisherAPIProduct.getGatewayVendor());
        publisherAPI.setAdditionalProperties(publisherAPIProduct.getAdditionalProperties());
        return publisherAPI;
    }

    public static DevPortalAPI mapAPIProductToDevPortalAPI(APIProduct apiProduct) {
        if (apiProduct == null) {
            return null;
        }

    DevPortalAPI devPortalAPI = new DevPortalAPI();
    devPortalAPI.setId(apiProduct.getUuid());
    devPortalAPI.setApiName(apiProduct.getId().getName());
    devPortalAPI.setVersion(apiProduct.getId().getVersion());
    devPortalAPI.setProviderName(apiProduct.getId().getProviderName());
    devPortalAPI.setContext(apiProduct.getContext());
    devPortalAPI.setType(apiProduct.getType());
    devPortalAPI.setThumbnail(apiProduct.getThumbnailUrl());
    devPortalAPI.setBusinessOwner(apiProduct.getBusinessOwner());
    devPortalAPI.setBusinessOwnerEmail(apiProduct.getBusinessOwnerEmail());
    devPortalAPI.setTechnicalOwner(apiProduct.getTechnicalOwner());
    devPortalAPI.setTechnicalOwnerEmail(apiProduct.getTechnicalOwnerEmail());
    devPortalAPI.setStatus(apiProduct.getState());
    devPortalAPI.setAvailableTierNames(apiProduct.getAvailableTiers().stream().map(Tier::getName).collect(Collectors.toSet()));
    devPortalAPI.setSubscriptionAvailability(apiProduct.getSubscriptionAvailability());
    devPortalAPI.setSubscriptionAvailableOrgs(apiProduct.getSubscriptionAvailableTenants());
    devPortalAPI.setCreatedTime(dateToTimestampString(apiProduct.getCreatedTime()));
    devPortalAPI.setDescription(apiProduct.getDescription());
    devPortalAPI.setGatewayVendor(apiProduct.getGatewayVendor());
    devPortalAPI.setAdditionalProperties(apiProduct.getAdditionalProperties());
    devPortalAPI.setTransports(apiProduct.getTransports());
    devPortalAPI.setVisibleOrganizations(apiProduct.getVisibleTenants());
    devPortalAPI.setVisibleRoles(apiProduct.getVisibleRoles());
    devPortalAPI.setTags(new ArrayList<>(apiProduct.getTags()));
    devPortalAPI.setEnvironments(apiProduct.getEnvironments());
    devPortalAPI.setApiSecurity(apiProduct.getApiSecurity());
    devPortalAPI.setMonetizationEnabled(apiProduct.isMonetizationEnabled());
    devPortalAPI.setApiCategories(apiProduct.getApiCategories().stream().map(APICategory::getName).collect(Collectors.toSet()));
    devPortalAPI.setAdvertiseOnly(apiProduct.isEnableStore());
    devPortalAPI.setDefaultVersion(apiProduct.isDefaultVersion());
    devPortalAPI.setDeploymentEnvironments(apiProduct.getEnvironments().stream()
            .map(env -> {
                DeploymentEnvironments deploymentEnvironments = new DeploymentEnvironments();
                deploymentEnvironments.setType(env);
                return deploymentEnvironments;
            }).collect(Collectors.toSet()));
    devPortalAPI.setAuthorizationHeader(apiProduct.getAuthorizationHeader());
    devPortalAPI.setApiKeyHeader(apiProduct.getApiKeyHeader());
    devPortalAPI.setVisibility(apiProduct.getVisibility());
    devPortalAPI.setAsyncTransportProtocols(apiProduct.isAsync() ? "async" : null);

        return devPortalAPI;
    }

    public static String getTenantAdminUserName(String tenantDomain) throws APIManagementException {
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminUserName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
            if (!tenantDomain.contentEquals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                return adminUserName.concat("@").concat(tenantDomain);
            }
            return adminUserName;
        } catch (UserStoreException e) {
            throw new APIManagementException("Error in getting tenant admin username", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static String getTextFromInputStream(ResourceFile resourceFile) throws IOException {
        InputStream inputStream = resourceFile.getContent();
        String fileType = resourceFile.getContentType();

        switch (fileType.toLowerCase()) {
            case APIConstants.DOCUMENTATION_PDF_CONTENT_TYPE:
                return extractTextFromPdf(inputStream);
            case APIConstants.DOCUMENTATION_DOCX_CONTENT_TYPE:
                return extractTextFromDocx(inputStream);
            case APIConstants.DOCUMENTATION_DOC_CONTENT_TYPE:
                return extractTextFromDoc(inputStream);
            case APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE:
                return extractTextFromPlainText(inputStream);
            default:
                throw new UnsupportedOperationException("Unsupported file type: " + fileType);
        }
    }

    private static String extractTextFromPdf(InputStream inputStream) throws IOException {
        // You'll need Apache PDFBox dependency
        try {
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (Exception e) {
            throw new IOException("Failed to extract text from PDF", e);
        }
    }

    private static String extractTextFromDocx(InputStream inputStream) throws IOException {
        // You'll need Apache POI dependency
        try {
            XWPFDocument document = new XWPFDocument(inputStream);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            String text = extractor.getText();
            document.close();
            return text;
        } catch (Exception e) {
            throw new IOException("Failed to extract text from DOCX", e);
        }
    }

    private static String extractTextFromDoc(InputStream inputStream) throws IOException {
        // You'll need Apache POI-HWPF dependency
        try {
            HWPFDocument document = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(document);
            String text = extractor.getText();
            document.close();
            return text;
        } catch (Exception e) {
            throw new IOException("Failed to extract text from DOC", e);
        }
    }

    private static String extractTextFromPlainText(InputStream inputStream) {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(inputStream, Charset.forName("UTF-8"))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
                if (scanner.hasNextLine()) {
                    content.append(System.lineSeparator());
                }
            }
        }
        return content.toString();
    }
}



