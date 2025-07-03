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
        try {
            for (LLMProviderMetadata metadata : metadataList) {
                String attributeName = metadata.getAttributeName();
                String inputSource = metadata.getInputSource();
                String attributeIdentifier = metadata.getAttributeIdentifier();
                if (APIConstants.AIAPIConstants.INPUT_SOURCE_PAYLOAD.equalsIgnoreCase(inputSource)) {
                    if (responseMetadata.getPayload() != null) {
                        try {
                            String extractedValue =
                                    JsonPath.read(responseMetadata.getPayload(), attributeIdentifier).toString();
                            metadataMap.put(attributeName, extractedValue);
                        } catch (PathNotFoundException e) {
                            log.debug("Attribute not found in the payload for identifier: " + attributeIdentifier);
                        }
                    } else {
                        log.debug("Payload is null, cannot extract metadata for attribute: " + attributeName);
                    }
                } else if (APIConstants.AIAPIConstants.INPUT_SOURCE_PATH.equalsIgnoreCase(inputSource)) {
                    if (StringUtils.isNotEmpty(responseMetadata.getRequestPath())) {
                        Pattern pattern = Pattern.compile(attributeIdentifier);
                        Matcher matcher = pattern.matcher(
                                URLDecoder.decode(responseMetadata.getRequestPath(), StandardCharsets.UTF_8));
                        String matchedValue = matcher.find() ? matcher.group(0) : null;
                        if (matchedValue != null) {
                            metadataMap.put(attributeName, matchedValue);
                        } else {
                            log.debug("No match found for the path identifier: " + attributeIdentifier);
                        }
                    }
                } else {
                    log.debug("Unsupported input source: " + inputSource + " for attribute: " + attributeName);
                }
            }
        } catch (PathNotFoundException e) {
            throw new APIManagementException("Error extracting metadata: Attribute not found in payload", e);
        } catch (Exception e) {
            throw new APIManagementException("Error extracting metadata from the payload", e);
        }
        return metadataMap;
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
