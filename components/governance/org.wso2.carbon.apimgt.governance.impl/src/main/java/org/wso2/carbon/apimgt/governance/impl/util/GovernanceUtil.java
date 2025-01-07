/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import feign.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.manager.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.model.DefaultRuleset;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains utility methods for Governance
 */
public class GovernanceUtil {
    private static final Log log = LogFactory.getLog(GovernanceUtil.class);


    /**
     * Generates a UUID
     *
     * @return UUID
     */
    public static String generateUUID() {

        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    /**
     * Resolves system properties and replaces in given in text
     *
     * @param text
     * @return System properties resolved text
     */
    public static String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${")
                && (indexOfStartingChars = textBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = textBuilder.toString().indexOf('}')) != -1) {

            String sysProp = textBuilder.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);

            //Derive original text value with resolved system property value
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1));
            }
            if ("carbon.home".equals(sysProp) && ".".equals(propValue)) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
            }
        }
        text = textBuilder.toString();
        return text;
    }


    /**
     * Load default rulesets from the default ruleset directory
     *
     * @param organization Organization
     */
    public static void loadDefaultRulesets(String organization) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        RulesetManager rulesetManager = new RulesetManagerImpl();
        try {
            // Fetch existing rulesets for the organization
            RulesetList existingRulesets = rulesetManager.getRulesets(organization);
            List<RulesetInfo> rulesetInfos = existingRulesets.getRulesetList();
            List<String> existingRuleNames = rulesetInfos.stream()
                    .map(RulesetInfo::getName)
                    .collect(Collectors.toList());

            // Define the path to default rulesets
            String pathToRulesets = CarbonUtils.getCarbonHome() + File.separator
                    + GovernanceConstants.DEFAULT_RULESET_LOCATION;
            Path pathToDefaultRulesets = Paths.get(pathToRulesets);

            // Iterate through default ruleset files
            Files.list(pathToDefaultRulesets).forEach(path -> {
                File file = path.toFile();
                if (file.isFile() && file.getName().endsWith(GovernanceConstants.YAML_FILE_TYPE)) {
                    try {

                        DefaultRuleset defaultRuleset = mapper.readValue(file, DefaultRuleset.class);

                        // Add ruleset if it doesn't already exist
                        if (!existingRuleNames.contains(defaultRuleset.getName())) {
                            log.info("Adding default ruleset: " + defaultRuleset.getName());
                            rulesetManager.createNewRuleset(organization,
                                    getRulesetFromDefaultRuleset(defaultRuleset));
                        } else {
                            log.info("Ruleset " + defaultRuleset.getName() + " already exists in organization: "
                                    + organization + "; skipping.");
                        }
                    } catch (IOException e) {
                        log.error("Error while loading default ruleset from file: " + file.getName(), e);
                    } catch (GovernanceException e) {
                        log.error("Error while adding default ruleset: " + file.getName(), e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error while accessing default ruleset directory", e);
        } catch (GovernanceException e) {
            log.error("Error while retrieving existing rulesets for organization: " + organization, e);
        }
    }

    /**
     * Get Ruleset from DefaultRuleset
     *
     * @param defaultRuleset DefaultRuleset
     * @return Ruleset
     * @throws GovernanceException if an error occurs while loading default ruleset content
     */
    public static Ruleset getRulesetFromDefaultRuleset(DefaultRuleset defaultRuleset) throws GovernanceException {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(defaultRuleset.getId());
        ruleset.setName(defaultRuleset.getName());
        ruleset.setDescription(defaultRuleset.getDescription());
        ruleset.setRuleType(defaultRuleset.getAppliesTo());
        ruleset.setArtifactType(defaultRuleset.getArtifactType());
        ruleset.setProvider(defaultRuleset.getProvider());
        ruleset.setRulesetContent(defaultRuleset.getRulesetContentAsString());
        ruleset.setDocumentationLink(defaultRuleset.getDocumentationLink());
        return ruleset;
    }

    /**
     * Get response bytes if present
     *
     * @param response Response
     * @return byte[]
     */
    public static byte[] getResponseBytesIfPresent(Response response) {
        byte[] responseBytes = new byte[0];
        try {
            if (response.body() != null) {
                return IOUtils.toByteArray(response.body().asInputStream());
            }
        } catch (IOException e) {
            //Log and continue. Response is read only for logging purposes. We don't need to break the flow.
            log.error("Error while reading response from dependent component", e);
        }
        return responseBytes;
    }

    /**
     * Get encoded log
     *
     * @param requestBody    Request body
     * @param requestMethod  Request method
     * @param requestUrl     Request URL
     * @param responseStatus Response status
     * @param responseBody   Response body
     * @param traceId        Trace ID
     * @return Encoded log
     */
    public static String getEncodedLog(byte[] requestBody, String requestMethod, String requestUrl,
                                       int responseStatus, byte[] responseBody, String traceId) {
        String requestStrBase64 = "<empty>";
        String responseStrBase64 = "<empty>";
        if (requestBody != null && requestBody.length != 0) {
            requestStrBase64 = new String(Base64.encodeBase64(requestBody));
        }
        if (responseBody != null && responseBody.length != 0) {
            responseStrBase64 = new String(Base64.encodeBase64(responseBody));
        }
        return "{" +
                "\"status\": \"" + responseStatus + "\", " +
                "\"responseBody\": \"" + responseStrBase64 + "\", " +
                "\"requestUrl\": \"" + requestMethod + " " + requestUrl + "\", " +
                "\"requestBody\": \"" + requestStrBase64 + "\", " +
                "\"traceId\": \"" + traceId + "\"" +
                "}";
    }

    /**
     * Get Swagger file from zip
     *
     * @param zipContent Zip content
     * @param apiId      api ID
     * @return Swagger file content
     * @throws GovernanceException if an error occurs while extracting swagger content
     */
    public static String getSwaggerFileFromZip(byte[] zipContent, String apiId) throws GovernanceException {
        String swaggerContent = null;
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().contains(GovernanceConstants.DEFINITIONS_FOLDER
                        + GovernanceConstants.SWAGGER_FILE_NAME)) {
                    // Read all bytes from the ZipInputStream
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }
                    // Convert the byte array to string
                    swaggerContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    return swaggerContent;
                }
            }
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.
                    ERROR_WHILE_EXTRACTING_SWAGGER_CONTENT, apiId);
        }
        return null;
    }

    /**
     * Get byte array from input stream
     *
     * @param is InputStream
     * @return byte[]
     * @throws IOException if an error occurs while converting input stream to byte array
     */
    public static byte[] toByteArray(InputStream is) throws IOException {

        return IOUtils.toByteArray(is);
    }

    /**
     * Get ruleset content map
     *
     * @param rulesetContent Ruleset content string
     * @return Map
     * @throws GovernanceException if an error occurs while parsing ruleset content
     */
    public static Map<String, Object> getRulesetConetentMap(String rulesetContent) throws GovernanceException {
        // Parse YAML content
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map<String, Object> rulesetMap;
        try {
            rulesetMap = yamlReader.readValue(rulesetContent, Map.class);
        } catch (JsonProcessingException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_FAILED_TO_PARSE_RULESET_CONETENT, e);
        }
        return rulesetMap;
    }
}

