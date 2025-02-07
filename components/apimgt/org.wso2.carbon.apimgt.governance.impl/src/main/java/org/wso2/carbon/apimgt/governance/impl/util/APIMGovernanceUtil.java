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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.DefaultRuleset;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.governance.impl.RulesetManager;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class contains utility methods for Governance
 */
public class APIMGovernanceUtil {
    private static final Log log = LogFactory.getLog(APIMGovernanceUtil.class);

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
     * Get map from YAML string content
     *
     * @param content String content
     * @return Map
     * @throws GovernanceException if an error occurs while parsing YAML content
     */
    public static Map<String, Object> getMapFromYAMLStringContent(String content) throws GovernanceException {
        // Parse YAML content
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map<String, Object> rulesetMap;
        try {
            rulesetMap = yamlReader.readValue(content, Map.class);
        } catch (JsonProcessingException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_FAILED_TO_PARSE_RULESET_CONTENT, e);
        }
        return rulesetMap;
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
        RulesetManager rulesetManager = new RulesetManager();
        try {
            // Fetch existing rulesets for the organization
            RulesetList existingRulesets = rulesetManager.getRulesets(organization);
            List<RulesetInfo> rulesetInfos = existingRulesets.getRulesetList();
            List<String> existingRuleNames = rulesetInfos.stream()
                    .map(RulesetInfo::getName)
                    .collect(Collectors.toList());

            // Define the path to default rulesets
            String pathToRulesets = CarbonUtils.getCarbonHome() + File.separator
                    + APIMGovernanceConstants.DEFAULT_RULESET_LOCATION;
            Path pathToDefaultRulesets = Paths.get(pathToRulesets);

            // Iterate through default ruleset files
            Files.list(pathToDefaultRulesets).forEach(path -> {
                File file = path.toFile();
                if (file.isFile() && (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                    try {
                        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                        DefaultRuleset defaultRuleset = mapper.readValue(file, DefaultRuleset.class);

                        // Add ruleset if it doesn't already exist
                        if (!existingRuleNames.contains(defaultRuleset.getName())) {
                            log.info("Adding default ruleset: " + defaultRuleset.getName());
                            rulesetManager.createNewRuleset(
                                    getRulesetFromDefaultRuleset(defaultRuleset,
                                            file.getName().replaceAll("/", "_")), organization);
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
     * @param fileName       File name
     * @return Ruleset
     * @throws GovernanceException if an error occurs while loading default ruleset content
     */
    public static Ruleset getRulesetFromDefaultRuleset(DefaultRuleset defaultRuleset,
                                                       String fileName) throws GovernanceException {
        Ruleset ruleset = new Ruleset();
        ruleset.setName(defaultRuleset.getName());
        ruleset.setDescription(defaultRuleset.getDescription());
        ruleset.setRuleCategory(RuleCategory.fromString(defaultRuleset.getRuleCategory()));
        ruleset.setRuleType(RuleType.fromString(defaultRuleset.getRuleType()));
        ruleset.setArtifactType(ExtendedArtifactType.fromString(defaultRuleset.getArtifactType()));
        ruleset.setProvider(defaultRuleset.getProvider());
        ruleset.setDocumentationLink(defaultRuleset.getDocumentationLink());

        RulesetContent rulesetContent = new RulesetContent();
        rulesetContent.setFileName(fileName);
        rulesetContent.setContent(defaultRuleset.getRulesetContentString().getBytes(StandardCharsets.UTF_8));
        ruleset.setRulesetContent(rulesetContent);

        return ruleset;
    }

    /**
     * Get all artifacts for a given artifact type
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of artifact IDs
     * @throws GovernanceException If an error occurs while getting the list of artifacts
     */
    public static List<String> getAllArtifacts(ArtifactType artifactType, String organization)
            throws GovernanceException {
        if (ArtifactType.API.equals(artifactType)) {
            return APIMUtil.getAllAPIs(organization);
        }
        return new ArrayList<>();
    }

    /**
     * Get all artifacts as a map of Artifact Type, List of Artifact Reference IDs
     *
     * @param organization Organization
     * @return Map of Artifact Type, List of Artifact Reference IDs
     * @throws GovernanceException If an error occurs while getting the list of artifacts
     */
    public static Map<ArtifactType, List<String>> getAllArtifacts(String organization) throws GovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();

        for (ArtifactType artifactType : ArtifactType.values()) {
            if (ArtifactType.API.equals(artifactType)) {
                List<String> artifactRefIds = APIMUtil.getAllAPIs(organization);
                artifacts.put(artifactType, artifactRefIds);
            }
        }

        return artifacts;
    }

    /**
     * Get artifacts for a label as a map of Artifact Type,
     * List of Artifact Reference IDs
     *
     * @param labelId Label ID
     * @return Map of Artifact Type, List of Artifact Reference IDs
     */
    public static Map<ArtifactType, List<String>> getArtifactsForLabel(String labelId) throws GovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();
        for (ArtifactType artifactType : ArtifactType.values()) {
            if (ArtifactType.API.equals(artifactType)) {
                List<String> artifactRefIds = APIMUtil.getAPIsByLabel(labelId);
                artifacts.put(artifactType, artifactRefIds);
            }
        }
        return artifacts;
    }

    /**
     * Get labels for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @return List of label IDs
     */
    public static List<String> getLabelsForArtifact(String artifactRefId, ArtifactType artifactType)
            throws GovernanceException {
        List<String> labels = new ArrayList<>();
        if (ArtifactType.API.equals(artifactType)) {
            labels = APIMUtil.getLabelsForAPI(artifactRefId);
        }
        return labels;
    }

    /**
     * Get applicable policies for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Map of Policy IDs, Policy Names
     */
    public static Map<String, String> getApplicablePoliciesForArtifact(String artifactRefId,
                                                                       ArtifactType artifactType,
                                                                       String organization) throws GovernanceException {

        List<String> labels = APIMGovernanceUtil.getLabelsForArtifact(artifactRefId, artifactType);
        PolicyManager policyManager = new PolicyManager();

        Map<String, String> policies = new HashMap<>();
        for (String label : labels) {
            Map<String, String> policiesForLabel = policyManager.getPoliciesByLabel(label, organization);
            if (policiesForLabel != null) {
                policies.putAll(policiesForLabel);
            }
        }

        policies.putAll(policyManager.getOrganizationWidePolicies(organization));

        return policies; // Return a map of policy IDs and policy names
    }

    /**
     * Get all applicable policy IDs for an artifact given a specific state at which
     * the artifact should be governed
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType    Artifact Type
     * @param governableState Governable state (The state at which the artifact should be governed)
     * @param organization    Organization
     * @return List of applicable policy IDs
     * @throws GovernanceException if an error occurs while checking for applicable policies
     */
    public static List<String> getApplicablePoliciesForArtifactWithState(String artifactRefId,
                                                                         ArtifactType artifactType,
                                                                         GovernableState governableState,
                                                                         String organization)
            throws GovernanceException {

        List<String> labels = APIMGovernanceUtil.getLabelsForArtifact(artifactRefId, artifactType);
        PolicyManager policyManager = new PolicyManager();

        // Check for policies using labels and the state
        Set<String> policies = new HashSet<>();
        for (String label : labels) {
            // Get policies for the label and state
            List<String> policiesForLabel = policyManager
                    .getPoliciesByLabelAndState(label, governableState, organization);
            if (policiesForLabel != null) {
                policies.addAll(policiesForLabel);
            }
        }

        policies.addAll(policyManager.getOrganizationWidePoliciesByState(governableState,
                organization));

        return new ArrayList<>(policies);
    }

    /**
     * Check for blocking actions in policies
     *
     * @param policyIds       List of policy IDs
     * @param governableState Governable state
     * @return boolean
     * @throws GovernanceException if an error occurs while checking for blocking actions
     */
    public static boolean isBlockingActionsPresent(List<String> policyIds, GovernableState governableState)
            throws GovernanceException {
        PolicyManager policyManager = new PolicyManager();
        boolean isBlocking = false;
        for (String policyId : policyIds) {
            if (policyManager.isBlockingActionPresentForState(policyId, governableState)) {
                isBlocking = true;
                break;
            }
        }
        return isBlocking;
    }

    /**
     * Check if an artifact is available
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @return boolean
     */
    public static boolean isArtifactAvailable(String artifactRefId, ArtifactType artifactType) {
        artifactType = artifactType != null ? artifactType : ArtifactType.API;

        // Check if artifact exists in APIM
        boolean artifactExists = false;
        if (ArtifactType.API.equals(artifactType)) {
            artifactExists = APIMUtil.isAPIExist(artifactRefId);
        }
        return artifactExists;
    }

    /**
     * Get artifact name
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @return String
     * @throws GovernanceException If an error occurs while getting the artifact name
     */
    public static String getArtifactName(String artifactRefId, ArtifactType artifactType)
            throws GovernanceException {

        String artifactName = null;
        if (ArtifactType.API.equals(artifactType)) {
            artifactName = APIMUtil.getAPIName(artifactRefId);
        }
        return artifactName;
    }

    /**
     * Get artifact version
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @return String
     * @throws GovernanceException If an error occurs while getting the artifact version
     */
    public static String getArtifactVersion(String artifactRefId, ArtifactType artifactType)
            throws GovernanceException {

        String artifactVersion = null;
        if (ArtifactType.API.equals(artifactType)) {
            artifactVersion = APIMUtil.getAPIVersion(artifactRefId);
        }
        return artifactVersion;
    }

    /**
     * Get artifact id from artifact name, version, type and organization
     *
     * @param artifactName    Artifact name
     * @param artifactVersion Artifact version
     * @param artifactType    Artifact type
     * @param organization    Organization
     * @return Artifact Reference ID (ID of the artifact on APIM side)
     * @throws GovernanceException If an error occurs while getting the artifact ID
     */
    public static String getArtifactRefId(String artifactName, String artifactVersion, ArtifactType artifactType,
                                          String organization) throws GovernanceException {

        if (ArtifactType.API.equals(artifactType)) {
            return APIMUtil.getApiUUID(artifactName, artifactVersion, organization);
        }
        return null;
    }

    /**
     * Get extended artifact type for an artifact
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @return ExtendedArtifactType
     * @throws GovernanceException If an error occurs while getting the extended artifact type
     */
    public static ExtendedArtifactType getExtendedArtifactTypeForArtifact
    (String artifactRefId, ArtifactType artifactType)
            throws GovernanceException {
        if (ArtifactType.API.equals(artifactType)) {
            return APIMUtil.getExtendedArtifactTypeForAPI(APIMUtil.getAPIType(artifactRefId));
        }
        return null;
    }

    /**
     * Get artifact project
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param revisionNo   Revision Number
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return byte[]
     * @throws GovernanceException If an error occurs while getting the artifact project
     */
    public static byte[] getArtifactProjectWithRevision(String artifactRefId, String revisionNo,
                                                        ArtifactType artifactType,
                                                        String organization) throws GovernanceException {

        // Get artifact project from APIM
        byte[] artifactProject = null;
        if (ArtifactType.API.equals(artifactType)) {
            artifactProject =
                    APIMUtil.getAPIProject(artifactRefId, revisionNo, organization);
        }
        return artifactProject;
    }

    /**
     * Get artifact project
     *
     * @param artifactRefId   Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return byte[]
     * @throws GovernanceException If an error occurs while getting the artifact project
     */
    public static byte[] getArtifactProject(String artifactRefId, ArtifactType artifactType,
                                            String organization) throws GovernanceException {

        return getArtifactProjectWithRevision(artifactRefId, null, artifactType, organization);
    }

    /**
     * Extract project content into a map of RuleType and String
     *
     * @param project      Project
     * @param artifactType Artifact Type
     * @return Map of RuleType and String
     */
    public static Map<RuleType, String> extractArtifactProjectContent(byte[] project, ArtifactType artifactType)
            throws GovernanceException {
        if (ArtifactType.API.equals(artifactType)) {
            return APIMUtil.extractAPIProjectContent(project);
        }
        return null;
    }

    /**
     * Read artifact project content from a file path
     *
     * @param filePath File path
     * @return byte[]
     * @throws GovernanceException If an error occurs while reading the file
     */
    public static byte[] readArtifactProjectContent(String filePath) throws GovernanceException {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_FAILED_TO_READ_ARTIFACT_PROJECT, e);
        }
    }

}
