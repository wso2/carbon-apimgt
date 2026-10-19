/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.LLMProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * BuiltInLLMProviderService handles the common logic for Built In LLM provider services.
 */
public abstract class BuiltInLLMProviderService implements LLMProviderService {


    @Override
    public Map<String, String> getResponseMetadata(LLMResponseMetaData responseMetadata,
                                                   List<LLMProviderMetadata> metadataList,
                                                   Map<String, String> metadataMap) throws APIManagementException {
        if (metadataList == null || metadataList.isEmpty()) {
            log.debug("Metadata list is null or empty.");
            return metadataMap;
        }
        for (LLMProviderMetadata metadata : metadataList) {
            // Each attribute is extracted independently so that a failure (or a JSON null) in one attribute
            // never prevents the remaining attributes from being collected.
            try {
                extractResponseMetadata(responseMetadata, metadata, metadataMap);
            } catch (Exception e) {
                log.warn("Error extracting response metadata attribute: "
                        + (metadata != null ? metadata.getAttributeName() : null) + ". Skipping the attribute.", e);
            }
        }
        return metadataMap;
    }

    private void extractResponseMetadata(LLMResponseMetaData responseMetadata, LLMProviderMetadata metadata,
                                         Map<String, String> metadataMap) {

        String attributeName = metadata.getAttributeName();
        String inputSource = metadata.getInputSource();
        String attributeIdentifier = metadata.getAttributeIdentifier();
        if (StringUtils.isBlank(attributeIdentifier)) {
            // Optional attributes may be configured without an identifier (e.g. a custom provider that does not
            // report prompt-cache tokens).
            log.debug("No identifier configured for metadata attribute: " + attributeName);
            return;
        }
        if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(inputSource)) {
            extractFromPayload(responseMetadata.getPayload(), attributeName, attributeIdentifier, metadataMap);
        } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(inputSource)) {
            extractFromPath(responseMetadata.getRequestPath(), attributeName, attributeIdentifier, metadataMap);
        } else {
            log.debug("Unsupported input source: " + inputSource + " for attribute: " + attributeName);
        }
    }

    private void extractFromPayload(String payload, String attributeName, String attributeIdentifier,
                                    Map<String, String> metadataMap) {

        if (payload == null) {
            log.debug("Payload is null, cannot extract metadata for attribute: " + attributeName);
            return;
        }
        try {
            Object value = JsonPath.read(payload, attributeIdentifier);
            // A JSON null is returned as Java null, or as JsonNull when JsonPath uses the Gson provider (as it does
            // in the gateway). Treat both as "not reported".
            if (value == null || value instanceof JsonNull) {
                log.debug("Attribute is null in the payload for identifier: " + attributeIdentifier);
                return;
            }
            // JsonPath is configured with the Gson provider, so unwrap primitives to avoid the JSON-quoted form
            // returned by JsonPrimitive#toString().
            String extractedValue = (value instanceof JsonPrimitive)
                    ? ((JsonPrimitive) value).getAsString()
                    : String.valueOf(value);
            metadataMap.put(attributeName, extractedValue);
        } catch (PathNotFoundException e) {
            log.debug("Attribute not found in the payload for identifier: " + attributeIdentifier);
        }
    }

    private void extractFromPath(String requestPath, String attributeName, String attributeIdentifier,
                                 Map<String, String> metadataMap) {

        if (StringUtils.isEmpty(requestPath)) {
            return;
        }
        Pattern pattern = Pattern.compile(attributeIdentifier);
        Matcher matcher = pattern.matcher(URLDecoder.decode(requestPath, StandardCharsets.UTF_8));
        String matchedValue = matcher.find() ? matcher.group(0) : null;
        if (matchedValue != null) {
            metadataMap.put(attributeName, matchedValue);
        } else {
            log.debug("No match found for the path identifier: " + attributeIdentifier);
        }
    }

    /**
     * Returns metadata attribute identifiers that were shipped as defaults in earlier versions and have since been
     * replaced. When a stored built-in provider still uses one of these identifiers, it is updated to the current
     * default on startup. Identifiers customised by other means are never overwritten.
     *
     * @return Map of attribute name to the list of superseded default identifiers.
     */
    public Map<String, List<String>> getSupersededMetadataIdentifiers() {

        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getRequestMetadata(String payload, Map<String, String> headers,
                                                  Map<String, String> queryParams,
                                                  List<LLMProviderMetadata> metadataList,
                                                  Map<String, String> metadataMap)
            throws APIManagementException {

        return metadataMap;
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
    public abstract LLMProvider getLLMProvider()
            throws APIManagementException;
}
