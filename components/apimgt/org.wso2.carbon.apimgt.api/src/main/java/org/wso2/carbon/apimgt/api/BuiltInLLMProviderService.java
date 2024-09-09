package org.wso2.carbon.apimgt.api;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InBuiltLlmProviderService handles the common logic for Built In LLM provider services.
 */
public abstract class BuiltInLLMProviderService implements LLMProviderService {

    /**
     * Extracts metadata from the payload, headers, or query params.
     *
     * @param payload      The request payload.
     * @param headers      The request headers (not used).
     * @param queryParams  The request query parameters (not used).
     * @param metadataList List of metadata to extract.
     * @return Map of extracted metadata.
     * @throws APIManagementException If extraction fails.
     */
    @Override
    public Map<String, String> getResponseMetadata(String payload, Map<String, String> headers,
                                                   Map<String, String> queryParams,
                                                   List<LLMProviderMetadata> metadataList)
            throws APIManagementException {

        Map<String, String> extractedMetadata = new HashMap<>();
        if (metadataList == null || metadataList.isEmpty()) {
            log.warn("Metadata list is null or empty.");
            return extractedMetadata;
        }
        try {
            for (LLMProviderMetadata metadata : metadataList) {
                String attributeName = metadata.getAttributeName();
                String inputSource = metadata.getInputSource();
                String attributeIdentifier = metadata.getAttributeIdentifier();
                if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(inputSource)) {
                    if (payload != null) {
                        try {
                            String extractedValue = JsonPath.read(payload, attributeIdentifier).toString();
                            extractedMetadata.put(attributeName, extractedValue);
                        } catch (PathNotFoundException e) {
                            log.warn("Attribute not found in the payload for identifier: " + attributeIdentifier);
                        }
                    } else {
                        log.warn("Payload is null, cannot extract metadata for attribute: " + attributeName);
                    }
                } else {
                    log.warn("Unsupported input source: " + inputSource + " for attribute: " + attributeName);
                }
            }
        } catch (PathNotFoundException e) {
            throw new APIManagementException("Error extracting metadata: Attribute not found in payload", e);
        } catch (Exception e) {
            throw new APIManagementException("Error extracting metadata from the payload", e);
        }
        return extractedMetadata;
    }

    @Override
    public Map<String, String> getRequestMetadata(String payload, Map<String, String> headers,
                                                  Map<String, String> queryParams,
                                                  List<LLMProviderMetadata> metadataList)
            throws APIManagementException {
        return null;
    }

    /**
     * Reads the API definition from the specified file path.
     *
     * @param filePath The path to the API definition file.
     * @return The API definition as a string.
     * @throws APIManagementException if an error occurs while reading the file.
     */
    protected String readApiDefinition(String filePath) throws APIManagementException {

        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new APIManagementException("Error reading API definition", e);
        }
    }

    @Override
    public abstract String getType();

    @Override
    public abstract LLMProvider registerLlmProvider(String organization, String apiDefinitionFilePath)
            throws APIManagementException;
}
