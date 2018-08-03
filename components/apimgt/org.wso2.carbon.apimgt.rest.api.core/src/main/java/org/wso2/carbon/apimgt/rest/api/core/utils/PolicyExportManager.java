/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.template.APITemplateException;
import org.wso2.carbon.apimgt.core.template.APIThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.ApplicationThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.CustomThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.template.SubscriptionThrottlePolicyTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Policy export manager handles creating execution plans and creating and archive.
 */
public class PolicyExportManager {

    private static final Logger log = LoggerFactory.getLogger(PolicyExportManager.class);
    private static final String APPLICATION = "application_";
    private static final String SUBSCRIPTION = "subscription_";
    private static final String RESOURCE = "resource_";
    private static final String DEFAULT = "_default";
    private static final String CUSTOM = "custom_";
    private static final String ZIP = ".zip";
    private static final String EXPORT_POLICIES = "ExportPolicies";
    private static final String SIDDHI_EXTENSION = ".siddhi";
    private APIMgtAdminService apiMgtAdminService;

    public PolicyExportManager(APIMgtAdminService adminService) {
        apiMgtAdminService = adminService;
    }

    public String createArchiveFromExecutionPlans(String exportedPoliciesDirName, String archiveDir, String archiveName)
            throws APIManagementException {
        //retrieve all policies under each policy level
        List<APIPolicy> apiPolicies = apiMgtAdminService.getApiPolicies();
        List<ApplicationPolicy> applicationPolicies = apiMgtAdminService.getApplicationPolicies();
        List<SubscriptionPolicy> subscriptionPolicies = apiMgtAdminService.getSubscriptionPolicies();
        List<CustomPolicy> customPolicies = apiMgtAdminService.getCustomRules();
        //write all execution Plans/Siddhi Apps to exportPoliciesDirName directory
        String dirLocation = exportedPoliciesDirName + File.separator + EXPORT_POLICIES;
        APIFileUtils.createDirectory(dirLocation);
        if (!apiPolicies.isEmpty()) {
            for (Map<String, String> map : getApiPolicySiddhiApps(apiPolicies)) {
                prepareFile(dirLocation, map);
            }
        }
        if (!applicationPolicies.isEmpty()) {
            prepareFile(dirLocation, getAppPolicySiddhiApps(applicationPolicies));
        }
        if (!subscriptionPolicies.isEmpty()) {
            prepareFile(dirLocation, getSubscriptionPolicySiddhiApps(subscriptionPolicies));
        }
        if (!customPolicies.isEmpty()) {
            prepareFile(dirLocation, getCustomPolicySiddhiApps(customPolicies));
        }
        //create archive and get the archive location
        String zippedFilePath = createArchiveFromPolicies(exportedPoliciesDirName, archiveDir, archiveName);
        APIFileUtils.deleteDirectory(exportedPoliciesDirName);
        return zippedFilePath;
    }

    /**
     * Create Archive from policies.
     *
     * @param sourceDir   Source directory containing execution plans
     * @param archiveDir  Archive containing directory
     * @param archiveName name of the archive
     * @return String object of the file path
     * @throws APIMgtDAOException if creating archive failed
     */
    private String createArchiveFromPolicies(String sourceDir, String archiveDir, String archiveName)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating archive for execution plans.");
        }
        try {
            APIFileUtils.archiveDirectory(sourceDir, archiveDir, archiveName);
        } catch (APIMgtDAOException e) {
            String errorMessage = "Error occurred creating archive :" + archiveName + " at " + archiveDir;
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        }
        String archivePath = archiveDir + File.separator + archiveName + ZIP;
        log.info("Created archive for execution plans: " + archivePath);
        return archivePath;
    }

    /**
     * write execution plan to a file.
     *
     * @param exportDir location contains the files
     * @param policies  Map of execution plan names and execution plans
     */
    private void prepareFile(String exportDir, Map<String, String> policies) throws APIMgtDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing files for execution plans.");
        }

        for (Map.Entry<String, String> entry : policies.entrySet())
        {
            APIFileUtils.createFile(exportDir + File.separator + entry.getKey() + SIDDHI_EXTENSION);
            APIFileUtils.writeToFile(exportDir + File.separator + entry.getKey() + SIDDHI_EXTENSION, entry.getValue());
        }
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param apiPolicies APIPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private List<Map<String, String>> getApiPolicySiddhiApps(List<APIPolicy> apiPolicies) throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for API policies.");
        }
        List<Map<String, String>> policies = new ArrayList<>();
        Map<String, String> siddhiApps = new HashMap<>();
        APIThrottlePolicyTemplateBuilder templateBuilder;
        for (APIPolicy apiPolicy : apiPolicies) {
            templateBuilder = new APIThrottlePolicyTemplateBuilder(apiPolicy);
            if (apiPolicy.getPipelines() != null) {
                siddhiApps = templateBuilder.getThrottlePolicyTemplateForPipelines();
            }
            siddhiApps.put(RESOURCE + apiPolicy.getPolicyName() + DEFAULT,
                    templateBuilder.getThrottlePolicyTemplateForAPILevelDefaultCondition());
            policies.add(siddhiApps);
        }
        return policies;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param applicationPolicies ApplicationPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getAppPolicySiddhiApps(List<ApplicationPolicy> applicationPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for application policy.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        ApplicationThrottlePolicyTemplateBuilder templateBuilder;
        for (ApplicationPolicy policy : applicationPolicies) {
            templateBuilder = new ApplicationThrottlePolicyTemplateBuilder(policy);
            name = APPLICATION + policy.getPolicyName();
            executionPlan = templateBuilder.getThrottlePolicyForAppLevel();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param subscriptionPolicies SubscriptionPolicy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getSubscriptionPolicySiddhiApps(List<SubscriptionPolicy> subscriptionPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for Subscription policies.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        SubscriptionThrottlePolicyTemplateBuilder templateBuilder;
        for (SubscriptionPolicy policy : subscriptionPolicies) {
            name = SUBSCRIPTION + policy.getPolicyName();
            templateBuilder = new SubscriptionThrottlePolicyTemplateBuilder(policy);
            executionPlan = templateBuilder.getThrottlePolicyForSubscriptionLevel();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }

    /**
     * Get execution plan/ siddhi apps for custom policies.
     *
     * @param customPolicies custom policy object list
     * @return Map<String, String> containing execution plan name and execution plans.
     * @throws APITemplateException If template generating fails
     */
    private Map<String, String> getCustomPolicySiddhiApps(List<CustomPolicy> customPolicies)
            throws APITemplateException {
        if (log.isDebugEnabled()) {
            log.debug("Get execution plans for custom policies.");
        }
        Map<String, String> siddhiApps = new HashMap<>();
        String name;
        String executionPlan;
        CustomThrottlePolicyTemplateBuilder templateBuilder;
        for (CustomPolicy policy : customPolicies) {
            templateBuilder = new CustomThrottlePolicyTemplateBuilder(policy);
            name = CUSTOM + policy.getPolicyName();
            executionPlan = templateBuilder.getThrottlePolicyTemplateForCustomPolicy();
            siddhiApps.put(name, executionPlan);
        }
        return siddhiApps;
    }
}
