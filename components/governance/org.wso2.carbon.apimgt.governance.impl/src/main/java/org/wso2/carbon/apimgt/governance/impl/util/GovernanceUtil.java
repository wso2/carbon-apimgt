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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.manager.RulesetManager;
import org.wso2.carbon.apimgt.governance.api.model.DefaultRuleset;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.api.model.RulesetInfo;
import org.wso2.carbon.apimgt.governance.api.model.RulesetList;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.RulesetManagerImpl;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        ruleset.setMessage(defaultRuleset.getMessage());
        ruleset.setDescription(defaultRuleset.getDescription());
        ruleset.setAppliesTo(defaultRuleset.getAppliesTo());
        ruleset.setProvider(defaultRuleset.getProvider());
        ruleset.setRulesetContent(defaultRuleset.getRulesetContentAsString());
        ruleset.setDocumentationLink(defaultRuleset.getDocumentationLink());
        ruleset.setIsDefault(1);
        return ruleset;
    }

}

