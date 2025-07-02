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
import org.wso2.carbon.apimgt.governance.api.ArtifactGovernanceHandler;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.APIMDefaultGovPolicy;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.DefaultRuleset;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetContent;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.APIMGovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.ArtifactGovernanceFactory;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
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
import java.util.Collections;
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
     * @throws APIMGovernanceException if an error occurs while parsing YAML content
     */
    public static Map<String, Object> getMapFromYAMLStringContent(String content) throws APIMGovernanceException {
        // Parse YAML content
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map<String, Object> rulesetMap;
        try {
            rulesetMap = yamlReader.readValue(content, Map.class);
        } catch (JsonProcessingException e) {
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_FAILED_TO_PARSE_RULESET_CONTENT, e);
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
                                    getRulesetFromDefaultRuleset(defaultRuleset, file.getName()), organization);
                        } else {
                            log.info("Ruleset " + defaultRuleset.getName() + " already exists in organization: "
                                    + organization + "; skipping.");
                        }
                    } catch (IOException e) {
                        log.error("Error while loading default ruleset from file: " + file.getName(), e);
                    } catch (APIMGovernanceException e) {
                        log.error("Error while adding default ruleset: " + file.getName(), e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error while accessing default ruleset directory", e);
        } catch (APIMGovernanceException e) {
            log.error("Error while retrieving existing rulesets for organization: " + organization, e);
        }
    }

    /**
     * Load default policies from the default policy directory
     *
     * @param organization Organization
     */
    public static void loadDefaultPolicies(String organization) {
        PolicyManager policyManager = new PolicyManager();
        try {
            // Fetch existing policies for the organization
            List<APIMGovernancePolicy> existingPolicies = policyManager.getGovernancePolicies(organization)
                    .getGovernancePolicyList();

            // Define the path to default policies
            String pathToPolicies = CarbonUtils.getCarbonHome() + File.separator
                    + APIMGovernanceConstants.DEFAULT_POLICY_LOCATION;
            Path pathToDefaultPolicies = Paths.get(pathToPolicies);

            // Iterate through default policy files
            Files.list(pathToDefaultPolicies).forEach(path -> {
                File file = path.toFile();
                if (file.isFile() && (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                    try {
                        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                        APIMDefaultGovPolicy defaultPolicy = mapper.readValue(file, APIMDefaultGovPolicy.class);

                        // Add policy if it doesn't already exist
                        boolean policyExists = existingPolicies.stream()
                                .anyMatch(policy -> defaultPolicy.getName().equals(policy.getName()));
                        if (!policyExists) {
                            log.info("Adding default policy: " + defaultPolicy.getName());
                            APIMGovernancePolicy policy = getGovPolicyFromDefaultGovPolicy(defaultPolicy, organization);
                            APIMGovernancePolicy createdPolicy = policyManager.createGovernancePolicy(organization,
                                    policy);
                            if (createdPolicy != null) {
                                new ComplianceManager().handlePolicyChangeEvent(createdPolicy.getId(), organization);
                            }
                        } else {
                            log.info("Policy " + defaultPolicy.getName() + " already exists in organization: "
                                    + organization + "; skipping.");
                        }
                    } catch (IOException e) {
                        log.error("Error while loading default policy from file: " + file.getName(), e);
                    } catch (APIMGovernanceException e) {
                        log.error("Error while adding default policy: " + file.getName(), e);
                    }
                }
            });
        } catch (IOException e) {
            log.error("Error while accessing default policy directory", e);
        } catch (APIMGovernanceException e) {
            log.error("Error while retrieving existing policies for organization: " + organization, e);
        }
    }

    /**
     * Get Ruleset from DefaultRuleset
     *
     * @param defaultRuleset DefaultRuleset
     * @param fileName       File name
     * @return Ruleset
     * @throws APIMGovernanceException if an error occurs while loading default ruleset content
     */
    public static Ruleset getRulesetFromDefaultRuleset(DefaultRuleset defaultRuleset,
                                                       String fileName) throws APIMGovernanceException {
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
     * Get APIMGovernancePolicy from APIMDefaultGovPolicy
     *
     * @param defaultPolicy Default Policy
     * @param organization  Organization
     * @return APIMGovernancePolicy object
     */
    public static APIMGovernancePolicy getGovPolicyFromDefaultGovPolicy(APIMDefaultGovPolicy defaultPolicy,
                                                                        String organization) {
        APIMGovernancePolicy policy = new APIMGovernancePolicy();
        RulesetManager rulesetManager = new RulesetManager();

        policy.setName(defaultPolicy.getName());
        policy.setDescription(defaultPolicy.getDescription());
        List<String> labels = defaultPolicy.getLabels();
        if (labels != null && labels.stream().anyMatch(label -> label
                .equalsIgnoreCase(APIMGovernanceConstants.GLOBAL_LABEL))) {
            policy.setGlobal(true);
            policy.setLabels(Collections.emptyList());
        } else {
            policy.setLabels(labels);
        }
        policy.setGovernableStates(defaultPolicy.getGovernableStates().stream()
                .map(APIMGovernableState::fromString)
                .collect(Collectors.toList()));
        List<String> rulesetIds = new ArrayList<>();
        for (String rulesetName : defaultPolicy.getRulesetNames()) {
            try {
                RulesetInfo ruleset = rulesetManager.getRulesetByName(rulesetName, organization);
                if (ruleset != null) {
                    rulesetIds.add(ruleset.getId());
                } else {
                    log.warn("Provided ruleset name: " + rulesetName + " does not exist in organization: "
                            + organization + ". Skipping ruleset while creating policy: " + defaultPolicy.getName());
                }
            } catch (APIMGovernanceException e) {
                log.error("Error while getting ruleset ID for ruleset name: " + rulesetName, e);
            }
        }
        policy.setRulesetIds(rulesetIds);
        // For now actions are empty, notify actions are set by default by the policy manager down the line
        policy.setActions(Collections.emptyList());

        return policy;
    }

    /**
     * Get all artifacts for a given artifact type
     *
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return List of artifact IDs
     * @throws APIMGovernanceException If an error occurs while getting the list of artifacts
     */
    public static List<String> getAllArtifacts(ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getAllArtifacts(organization);
    }

    /**
     * Get all artifacts for a given artifact type visible to a given user
     *
     * @param artifactType Artifact Type
     * @param username     Username
     * @param organization Organization
     * @return List of artifact IDs
     * @throws APIMGovernanceException If an error occurs while getting the list of artifacts
     */
    public static List<String> getAllArtifacts(ArtifactType artifactType, String username, String organization)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getAllArtifacts(username, organization);
    }

    /**
     * Get all artifacts as a map of Artifact Type, List of Artifact Reference IDs
     *
     * @param organization Organization
     * @return Map of Artifact Type, List of Artifact Reference IDs
     * @throws APIMGovernanceException If an error occurs while getting the list of artifacts
     */
    public static Map<ArtifactType, List<String>> getAllArtifacts(String organization) throws APIMGovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();

        for (ArtifactType artifactType : ArtifactType.values()) {
            List<String> artifactRefIds = getAllArtifacts(artifactType, organization);
            artifacts.put(artifactType, artifactRefIds);
        }

        return artifacts;
    }

    /**
     * Get artifacts for a label as a map of Artifact Type, List of Artifact Reference IDs
     *
     * @param label Label ID
     * @return Map of Artifact Type, List of Artifact Reference IDs
     */
    public static Map<ArtifactType, List<String>> getArtifactsForLabel(String label) throws APIMGovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();
        ArtifactGovernanceFactory artifactGovernanceFactory = ArtifactGovernanceFactory.getInstance();
        for (ArtifactType artifactType : ArtifactType.values()) {
            List<String> artifactRefIds = artifactGovernanceFactory.getHandler(artifactType)
                    .getArtifactsByLabel(label);
            artifacts.put(artifactType, artifactRefIds);
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
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getLabelsForArtifact(artifactRefId);
    }

    /**
     * Get applicable policies for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return Map of Policy IDs, Policy Names
     */
    public static Map<String, String> getApplicablePoliciesForArtifact(String artifactRefId, ArtifactType artifactType,
                                                                       String organization)
            throws APIMGovernanceException {

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
     * @param artifactRefId       Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType        Artifact Type
     * @param apimGovernableState Governable state (The state at which the artifact should be governed)
     * @param organization        Organization
     * @return List of applicable policy IDs
     * @throws APIMGovernanceException if an error occurs while checking for applicable policies
     */
    public static List<String> getApplicablePoliciesForArtifactWithState(String artifactRefId,
                                                                         ArtifactType artifactType,
                                                                         APIMGovernableState apimGovernableState,
                                                                         String organization)
            throws APIMGovernanceException {

        List<String> labels = APIMGovernanceUtil.getLabelsForArtifact(artifactRefId, artifactType);
        PolicyManager policyManager = new PolicyManager();

        // Check for policies using labels and the state
        Set<String> policies = new HashSet<>();
        for (String label : labels) {
            // Get policies for the label and state
            List<String> policiesForLabel = policyManager
                    .getPoliciesByLabelAndState(label, apimGovernableState, organization);
            if (policiesForLabel != null) {
                policies.addAll(policiesForLabel);
            }
        }

        policies.addAll(policyManager.getOrganizationWidePoliciesByState(apimGovernableState,
                organization));

        return new ArrayList<>(policies);
    }

    /**
     * Check for blocking actions in policies
     *
     * @param policyIds           List of policy IDs
     * @param apimGovernableState Governable state
     * @param organization        Organization
     * @return boolean
     * @throws APIMGovernanceException if an error occurs while checking for blocking actions
     */
    public static boolean isBlockingActionsPresent(List<String> policyIds, APIMGovernableState apimGovernableState,
                                                   String organization)
            throws APIMGovernanceException {
        PolicyManager policyManager = new PolicyManager();
        boolean isBlocking = false;
        for (String policyId : policyIds) {
            if (policyManager.isBlockingActionPresentForState(policyId, apimGovernableState, organization)) {
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
     * @param organization  Organization
     * @return boolean
     */
    public static boolean isArtifactAvailable(String artifactRefId, ArtifactType artifactType,
                                              String organization) throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.isArtifactAvailable(artifactRefId, organization);
    }

    /**
     * Get artifact name
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return String
     * @throws APIMGovernanceException If an error occurs while getting the artifact name
     */
    public static String getArtifactName(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getName(artifactRefId, organization);
    }

    /**
     * Get artifact version
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return String
     * @throws APIMGovernanceException If an error occurs while getting the artifact version
     */
    public static String getArtifactVersion(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getVersion(artifactRefId, organization);
    }

    /**
     * Get artifact owner
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return String
     * @throws APIMGovernanceException If an error occurs while getting the artifact owner
     */
    public static String getArtifactOwner(String artifactRefId, ArtifactType artifactType, String organization)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getOwner(artifactRefId, organization);
    }

    /**
     * Get extended artifact type for an artifact
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException If an error occurs while getting the extended artifact type
     */
    public static ExtendedArtifactType getExtendedArtifactTypeForArtifact(String artifactRefId,
                                                                          ArtifactType artifactType)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getExtendedArtifactType(artifactRefId);
    }

    /**
     * Get artifact project
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param revisionId    Revision Number
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return byte[]
     * @throws APIMGovernanceException If an error occurs while getting the artifact project
     */
    public static byte[] getArtifactProjectWithRevision(String artifactRefId, String revisionId,
                                                        ArtifactType artifactType,
                                                        String organization) throws APIMGovernanceException {

        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getArtifactProject(artifactRefId, revisionId, organization);
    }

    /**
     * Get artifact project
     *
     * @param artifactRefId Artifact Reference ID (ID of the artifact on APIM side)
     * @param artifactType  Artifact Type
     * @param organization  Organization
     * @return byte[]
     * @throws APIMGovernanceException If an error occurs while getting the artifact project
     */
    public static byte[] getArtifactProject(String artifactRefId, ArtifactType artifactType,
                                            String organization) throws APIMGovernanceException {

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
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.extractArtifactProject(project);
    }

    /**
     * Check if an artifact is governable based on the governable states provided
     *
     * @param artifactId       Artifact ID
     * @param artifactType     Artifact Type
     * @param governableStates List of governable states
     * @return boolean         True if the artifact is governable, false otherwise
     * @throws APIMGovernanceException If an error occurs while checking if the artifact is governable
     */
    public static boolean isArtifactGovernable(String artifactId,
                                               ArtifactType artifactType, List<APIMGovernableState> governableStates)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.isArtifactGovernable(artifactId, governableStates);
    }

    /**
     * Check if an artifact is governable
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @return boolean True if the artifact is governable, false otherwise
     * @throws APIMGovernanceException If an error occurs while checking if the artifact is governable
     */
    public static boolean isArtifactGovernable(String artifactId, ArtifactType artifactType)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.isArtifactGovernable(artifactId);
    }

    /**
     * Get the extended artifact type from the artifact project
     *
     * @param artifactProject Artifact project zip
     * @return ExtendedArtifactType
     * @throws APIMGovernanceException If an error occurs while getting the extended artifact type
     */
    public static ExtendedArtifactType getExtendedArtifactTypeFromProject(byte[] artifactProject,
                                                                          ArtifactType artifactType)
            throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.getExtendedArtifactTypeFromProject(artifactProject);
    }

    /**
     * Check if an artifact is visible to a user
     *
     * @param artifactRefId Artifact Reference ID
     * @param artifactType  Artifact Type
     * @param username      Username
     * @param organization  Organization
     * @return boolean
     * @throws APIMGovernanceException If an error occurs while checking if the artifact is visible to the user
     */
    public static boolean isArtifactVisibleToUser(String artifactRefId, ArtifactType artifactType, String username,
                                                  String organization) throws APIMGovernanceException {
        ArtifactGovernanceHandler handler = ArtifactGovernanceFactory.getInstance().getHandler(artifactType);
        return handler.isArtifactVisibleToUser(artifactRefId, username, organization);
    }


}
