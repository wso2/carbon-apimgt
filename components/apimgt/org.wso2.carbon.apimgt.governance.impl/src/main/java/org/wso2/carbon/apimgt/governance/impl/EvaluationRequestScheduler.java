/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.*;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;

/**
 * Scheduler to process pending governance evaluation requests.
 */
public class EvaluationRequestScheduler {

    private static final Log log = LogFactory.getLog(EvaluationRequestScheduler.class);
    private static final int THREAD_POOL_SIZE = 10;
    private static final int QUEUE_SIZE = 255;
    private static final int CHECK_INTERVAL_MINUTES = 5;
    private static ScheduledExecutorService scheduler;
    private static ThreadPoolExecutor processorPool;

    /**
     * Initialize the evaluation request scheduler.
     */
    public static void initialize() {
        log.info("Initializing Evaluation Request Scheduler...");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        processorPool = createProcessorPool();

        scheduler.scheduleAtFixedRate(
                EvaluationRequestScheduler::processPendingRequests,
                0, CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Create a thread pool for processing requests in parallel.
     *
     * @return Configured ThreadPoolExecutor.
     */
    private static ThreadPoolExecutor createProcessorPool() {
        return new ThreadPoolExecutor(
                THREAD_POOL_SIZE, THREAD_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(QUEUE_SIZE),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    /**
     * Process pending evaluation requests.
     */
    private static void processPendingRequests() {
        if (log.isDebugEnabled()) {
            log.debug("Checking for pending evaluation requests...");
        }

        List<EvaluationRequest> pendingRequests = fetchPendingRequests();

        if (pendingRequests == null || pendingRequests.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No pending evaluation requests found.");
            }
            return;
        }

        for (EvaluationRequest request : pendingRequests) {
            processorPool.submit(() -> processRequest(request));
        }
    }

    /**
     * Fetch pending requests from the database.
     *
     * @return List of pending requests.
     */
    private static List<EvaluationRequest> fetchPendingRequests() {
        try {
            ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
            return complianceMgtDAO.getPendingEvaluationRequests();
        } catch (GovernanceException e) {
            log.error("Error fetching pending requests: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Process a single evaluation request.
     *
     * @param request The evaluation request to process.
     */
    private static void processRequest(EvaluationRequest request) {

        String artifactId = request.getArtifactId();
        ArtifactType artifactType = request.getArtifactType();
        String policyId = request.getPolicyId();
        String organization = request.getOrganization();
        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        try {
            ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
            complianceMgtDAO.updateEvaluationStatus(request.getId(), EvaluationStatus.PROCESSING);

            // If artifact does not exist, skip evaluation
            if (!GovernanceUtil.isArtifactAvailable(artifactId, artifactType)) {
                log.warn("Artifact not found for artifact ID: " + request.getArtifactId() + " . Skipping governance " +
                        "evaluation");
                return;
            }

            // Get artifact project
            byte[] artifactProject = GovernanceUtil.getArtifactProject(artifactId, artifactType, organization);

            // If artifact project does not exist, skip evaluation
            if (artifactProject == null) {
                log.warn("Artifact project not found for artifact ID: " +
                        request.getArtifactId() + " .Skipping governance evaluation");
                return;
            }

            // Extract artifact project content
            Map<RuleType, String> ruleTypeToContentMap = new HashMap<>();
            if (ArtifactType.isArtifactAPI(artifactType)) {
                ruleTypeToContentMap = APIMUtil.extractAPIProjectContent(artifactProject, artifactId);
            }

            // Get Rulesets related to the policy
            GovernancePolicyMgtDAO policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
            List<Ruleset> rulesets = policyMgtDAO.getRulesetsByPolicyId(policyId);

            // Validate the artifact against each ruleset
            for (Ruleset ruleset : rulesets) {
                ArtifactType rulesetArtifactType = request.getArtifactType();

                // Check if ruleset's artifact type matches with the artifact's type
                if ((ArtifactType.isArtifactAPI(artifactType) &&
                        ArtifactType.ALL_API.equals(rulesetArtifactType)) ||
                        (rulesetArtifactType.equals(request.getArtifactType()))) {

                    // Get target file content from artifact project based on ruleType
                    RuleType ruleType = ruleset.getRuleType();
                    String contentToValidate = ruleTypeToContentMap.get(ruleType);

                    // Send target content and ruleset for validation
                    List<RuleViolation> ruleViolations = validationEngine.validate(
                            contentToValidate, ruleset);

                    saveGovernanceResults(artifactId, artifactType, policyId, ruleViolations, organization);

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Ruleset artifact type does not match with the artifact's type. Skipping " +
                                "governance evaluation for ruleset ID: " + ruleset.getId());
                    }
                }
            }

            // Delete the evaluation request after processing completes
            complianceMgtDAO.deleteEvaluationRequest(request.getId());

        } catch (GovernanceException e) {
            log.error("Error processing evaluation request: " + request.getId(), e);
        }
    }

    /**
     * Save governance evaluation results to the database.
     *
     * @param artifactId     ID of the artifact.
     * @param artifactType   Type of the artifact.
     * @param policyId       ID of the policy.
     * @param ruleViolations List of rule violations.
     * @param organization   Organization
     */
    private static void saveGovernanceResults(String artifactId, ArtifactType artifactType, String policyId,
                                              List<RuleViolation> ruleViolations, String organization) {
        ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
        try {
            complianceMgtDAO.addGovernanceResult(artifactId, artifactType, policyId, organization,
                    ruleViolations.isEmpty());

            for (RuleViolation violation : ruleViolations) {
                violation.setOrganization(organization);
                violation.setPolicyId(policyId);
                violation.setArtifactId(artifactId);
                complianceMgtDAO.addRuleViolation(violation);
            }
        } catch (GovernanceException e) {
            log.error("Error saving governance result for artifact ID: " + artifactId, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("New governance result saved for artifact ID: " + artifactId);
        }
    }

    /**
     * Shutdown the evaluation request scheduler.
     */
    public static void shutdown() {
        log.info("Shutting down Evaluation Request Scheduler...");

        shutdownExecutor(scheduler, "request scheduler");
        shutdownExecutor(processorPool, "request processor pool");

        log.info("Evaluation Request Scheduler shut down.");
    }

    /**
     * Shutdown an executor service.
     *
     * @param executor The executor to shut down.
     * @param name     Name of the executor for logging.
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown of " + name + "...");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Shutdown interrupted for " + name, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
