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
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.DefaultRuleset;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
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
        ruleset.setRuleCategory(RuleCategory.fromString(defaultRuleset.getRuleCategory()));
        ruleset.setRuleType(RuleType.fromString(defaultRuleset.getRuleType()));
        ruleset.setArtifactType(ArtifactType.fromString(defaultRuleset.getArtifactType()));
        ruleset.setProvider(defaultRuleset.getProvider());
        ruleset.setRulesetContent(defaultRuleset.getRulesetContentString());
        ruleset.setDocumentationLink(defaultRuleset.getDocumentationLink());
        return ruleset;
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
     * Get labels for an artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @return List of label IDs
     */
    public static List<String> getLabelsForArtifact(String artifactId, ArtifactType artifactType)
            throws GovernanceException {
        List<String> labels = new ArrayList<>();
        if (ArtifactType.isArtifactAPI(artifactType)) {
            labels = APIMUtil.getLabelIDsForAPI(artifactId);
        }
        return labels;
    }

    /**
     * Get artifacts for a label
     *
     * @param labelId Label ID
     * @return Map of Artifact Type, List of Artifact IDs
     */
    public static Map<ArtifactType, List<String>> getArtifactsForLabel(String labelId) throws GovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();
        List<String> apiIds = APIMUtil.getAPIsByLabel(labelId);
        if (apiIds != null && !apiIds.isEmpty()) {
            artifacts.put(ArtifactType.API, apiIds);
        }
        return artifacts;
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
        if (ArtifactType.isArtifactAPI(artifactType)) {
            return APIMUtil.getAllAPIs(organization);
        }
        return new ArrayList<>();
    }

    /**
     * Get all artifacts for a given artifact type
     *
     * @param organization Organization
     * @return Map of Artifact Type, List of Artifact IDs
     * @throws GovernanceException If an error occurs while getting the list of artifacts
     */
    public static Map<ArtifactType, List<String>> getAllArtifactsMap(String organization)
            throws GovernanceException {
        Map<ArtifactType, List<String>> artifacts = new HashMap<>();
        artifacts.put(ArtifactType.API, getAllArtifacts(ArtifactType.API, organization));
        return artifacts;
    }

    /**
     * Get applicable policies for an artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return Map of Policy IDs, Policy Names
     */
    public static Map<String, String> getApplicablePoliciesForArtifact(String artifactId,
                                                                       ArtifactType artifactType,
                                                                       String organization) throws GovernanceException {

        List<String> labels = GovernanceUtil.getLabelsForArtifact(artifactId, artifactType);
        PolicyManager policyManager = new PolicyManagerImpl();

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
     * @param artifactId      Artifact ID
     * @param artifactType    Artifact Type
     * @param governableState Governable state (The state at which the artifact should be governed)
     * @param organization    Organization
     * @return List of applicable policy IDs
     * @throws GovernanceException if an error occurs while checking for applicable policies
     */
    public static List<String> getApplicablePoliciesForArtifactWithState(String artifactId,
                                                                         ArtifactType artifactType,
                                                                         GovernableState governableState,
                                                                         String organization)
            throws GovernanceException {

        List<String> labels = GovernanceUtil.getLabelsForArtifact(artifactId, artifactType);
        PolicyManager policyManager = new PolicyManagerImpl();

        // Check for policies using labels and the state
        Set<String> policies = new HashSet<>();
        for (String label : labels) {
            // Get policies for the label and state
            List<String> policiesForLabel = policyManager.getPoliciesByLabelAndState(label,
                    governableState, organization);
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
        log.info("Checking for blocking actions in policies for state: " + governableState);
        PolicyManager policyManager = new PolicyManagerImpl();
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
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @return boolean
     */
    public static boolean isArtifactAvailable(String artifactId, ArtifactType artifactType) {
        boolean isArtifactAPI = ArtifactType.isArtifactAPI(artifactType);

        // Check if artifact exists in APIM
        boolean artifactExists = false;
        if (isArtifactAPI) {
            artifactExists = APIMUtil.isAPIExist(artifactId);
        }
        return artifactExists;
    }

    /**
     * Get artifact type
     *
     * @param artifactId Artifact ID
     * @return ArtifactType
     */
    public static ArtifactType getArtifactType(String artifactId) throws GovernanceException {
        ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        return complianceMgtDAO.getArtifactInfo(artifactId).getArtifactType();
    }

    /**
     * Get artifact name
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @return String
     * @throws GovernanceException If an error occurs while getting the artifact name
     */
    public static String getArtifactName(String artifactId, ArtifactType artifactType)
            throws GovernanceException {
        String artifactName = null;
        if (ArtifactType.isArtifactAPI(artifactType)) {
            artifactName = APIMUtil.getAPINameCombinedWithVersion(artifactId);
        }
        return artifactName;
    }

    /**
     * Get artifact project
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @param organization Organization
     * @return byte[]
     * @throws GovernanceException If an error occurs while getting the artifact project
     */
    public static byte[] getArtifactProject(String artifactId, ArtifactType artifactType,
                                            String organization) throws GovernanceException {
        boolean isArtifactAPI = ArtifactType.isArtifactAPI(artifactType);

        // Get artifact project from APIM
        byte[] artifactProject = null;
        if (isArtifactAPI) {
            artifactProject =
                    APIMUtil.getAPIProject(artifactId, organization);
        }
        return artifactProject;
    }


    /**
     * Get governable states for artifact
     *
     * @param artifactId   Artifact ID
     * @param artifactType Artifact Type
     * @return List of Governable States
     * @throws GovernanceException If an exception occurs
     */
    public List<GovernableState> getGovernableStatesForArtifact(String artifactId,
                                                                ArtifactType artifactType)
            throws GovernanceException {
        List<GovernableState> governableStates = new ArrayList<>();
        String artifactTypeStr = String.valueOf(artifactType);

        if (ArtifactType.isArtifactAPI(artifactType)) {

            // Every created api should be governed with create and update policies
            governableStates.add(GovernableState.API_CREATE);
            governableStates.add(GovernableState.API_UPDATE);

            if (APIMUtil.isAPIDeployed(artifactId)) {
                // If the API is deployed, it should be governed with the deploy policy
                governableStates.add(GovernableState.API_DEPLOY);
            }

            String status = APIMUtil.getAPIStatus(artifactId);
            if (APIStatus.PUBLISHED.getStatus().equals(status)
                    || APIStatus.DEPRECATED.getStatus().equals(status)
                    || APIStatus.BLOCKED.getStatus().equals(status)) {
                // If the API is published, deprecated or blocked, it should be governed with the publish policy
                // as API has already been published
                governableStates.add(GovernableState.API_PUBLISH);
            }
        }

        return governableStates;
    }

}



