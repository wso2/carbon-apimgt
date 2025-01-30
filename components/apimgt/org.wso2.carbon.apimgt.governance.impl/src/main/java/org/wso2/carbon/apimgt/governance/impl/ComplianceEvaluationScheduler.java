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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationResult;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationStatus;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMUtil;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler to process pending compliance evaluation requests.
 */
public class ComplianceEvaluationScheduler {

    private static final Log log = LogFactory.getLog(ComplianceEvaluationScheduler.class);
    private static final int THREAD_POOL_SIZE = 10;
    private static final int QUEUE_SIZE = 255;
    private static final int CHECK_INTERVAL_MINUTES = 2;
    private static ScheduledExecutorService scheduler;
    private static ThreadPoolExecutor processorPool;
    private static final ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();

    /**
     * Initialize the evaluation request scheduler.
     */
    public static void initialize() throws GovernanceException {
        log.info("Initializing Evaluation Request Scheduler...");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        processorPool = createProcessorPool();

        complianceMgtDAO.updateProcessingRequestToPending();

        scheduler.scheduleAtFixedRate(
                ComplianceEvaluationScheduler::processPendingRequests,
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
     *
     */
    private static void processPendingRequests() {
        if (log.isDebugEnabled()) {
            log.debug("Checking for pending evaluation requests...");
        }

        List<ComplianceEvaluationRequest> pendingRequests = fetchPendingRequests();

        if (pendingRequests == null || pendingRequests.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No pending evaluation requests found.");
            }
            return;
        }

        // Group pending requests by artifact ID
        Map<String, List<ComplianceEvaluationRequest>> groupedRequests = new HashMap<>();
        for (ComplianceEvaluationRequest request : pendingRequests) {
            String artifactId = request.getArtifactId();
            if (groupedRequests.containsKey(artifactId)) {
                groupedRequests.get(artifactId).add(request);
            } else {
                List<ComplianceEvaluationRequest> requests = new ArrayList<>();
                requests.add(request);
                groupedRequests.put(artifactId, requests);
            }
        }

        // Process requests for each artifact in parallel
        for (String artifactId : groupedRequests.keySet()) {
            processorPool.submit(() -> {
                List<ComplianceEvaluationRequest> requests = groupedRequests.get(artifactId);

                String organization = requests.get(0).getOrganization(); // Can get organization from any request
                ArtifactType artifactType = requests.get(0)
                        .getArtifactType(); // Can get artifact type from any request

                // Start tenant flow for this thread
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(organization, true);

                    // Process the requests for an artifact
                    processRequestsForArtifact(artifactId, artifactType, organization, requests);
                } finally {
                    // End the tenant flow to avoid memory leaks
                    PrivilegedCarbonContext.endTenantFlow();
                }
            });
        }

    }

    /**
     * Fetch pending requests from the database.
     *
     * @return List of pending requests.
     */
    private static List<ComplianceEvaluationRequest> fetchPendingRequests() {
        try {
            return complianceMgtDAO.getPendingComplianceEvaluationRequests();
        } catch (GovernanceException e) {
            log.error("Error fetching pending requests: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Process evaluation requests for a given artifact.
     *
     * @param artifactId   ID of the artifact.
     * @param artifactType Type of the artifact.
     * @param organization Organization of the artifact.
     * @param requests     List of evaluation requests.
     */
    private static void processRequestsForArtifact(String artifactId, ArtifactType artifactType, String organization,
                                                   List<ComplianceEvaluationRequest> requests) {

        try {

            // Check if artifact exists
            if (!GovernanceUtil.isArtifactAvailable(artifactId, artifactType)) {
                log.warn("Artifact not found for artifact ID: " + artifactId + " " +
                        ". Skipping governance evaluation");
                complianceMgtDAO.deleteComplianceEvaluationRequestByArtifactId(artifactId);
                return;
            }

            // Check if artifact is SOAP or GRAPHQL TODO: Support SOAP and GraphQL
            if (ArtifactType.SOAP_API.equals(artifactType) || ArtifactType.GRAPHQL_API.equals(artifactType)) {
                log.warn("Artifact type " + artifactType + " not supported for artifact ID: " + artifactId + " " +
                        ". Skipping governance evaluation");
                complianceMgtDAO.deleteComplianceEvaluationRequestByArtifactId(artifactId);
                return;
            }

            // Get artifact project
            byte[] artifactProject = GovernanceUtil.getArtifactProject(artifactId, artifactType, organization);

            // If artifact project does not exist, skip evaluation
            if (artifactProject == null) {
                log.warn("Artifact project not found for artifact ID: " +
                        artifactId + " .Skipping governance evaluation");
                complianceMgtDAO.deleteComplianceEvaluationRequestByArtifactId(artifactId);
                return;
            }

            // Extract artifact project content to map
            Map<RuleType, String> artifactProjectContentMap = new HashMap<>();
            if (ArtifactType.isArtifactAPI(artifactType)) {
                artifactProjectContentMap = APIMUtil.extractAPIProjectContent(artifactProject,
                        artifactId, artifactType);
            }

            for (ComplianceEvaluationRequest request : requests) {
                processEvaluationRequest(request.getId(), artifactId, artifactType,
                        request.getPolicyId(), artifactProjectContentMap);
            }

        } catch (GovernanceException e) {
            log.error("Error processing evaluation requests for artifact ID: " + artifactId, e);
        }

    }

    /**
     * Process a single evaluation request.
     *
     * @param requestId                 ID of the artifact.
     * @param artifactId                ID of the artifact.
     * @param artifactType              Type of the artifact.
     * @param policyId                  ID of the policy.
     * @param artifactProjectContentMap Map of rule type to content.
     * @throws GovernanceException If an error occurs while processing the evaluation request.
     */
    private static void processEvaluationRequest(String requestId,
                                                 String artifactId, ArtifactType artifactType, String policyId,
                                                 Map<RuleType, String> artifactProjectContentMap)
            throws GovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        // Check if there are processing requests for the same artifact, policy pair. If so, skip evaluation
        if (complianceMgtDAO.getProcessingComplianceEvaluationRequest(artifactId, policyId) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping governance evaluation for artifact ID: " + artifactId + " " +
                        "and policy ID: " + policyId +
                        " as there are processing requests for the same pair.");
            }
            return;
        }

        // Update evaluation status to PROCESSING
        complianceMgtDAO.updateComplianceEvaluationStatus(requestId, ComplianceEvaluationStatus.PROCESSING);

        // Get Rulesets related to the policy
        GovernancePolicyMgtDAO policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();

        List<Ruleset> rulesets = policyMgtDAO.getRulesetsByPolicyId(policyId);

        // Validate the artifact against each ruleset
        for (Ruleset ruleset : rulesets) {
            ArtifactType rulesetArtifactType = ruleset.getArtifactType();

            // Check if ruleset's artifact type matches with the artifact's type
            if ((ArtifactType.isArtifactAPI(artifactType) &&
                    ArtifactType.API.equals(rulesetArtifactType)) ||
                    (rulesetArtifactType.equals(artifactType))) {

                // Get target file content from artifact project based on ruleType
                RuleType ruleType = ruleset.getRuleType();
                String contentToValidate = artifactProjectContentMap.get(ruleType);

                if (contentToValidate == null) {
                    log.warn(ruleType + " content not found in artifact project for artifact ID: " +
                            artifactId + ". Skipping governance evaluation for ruleset ID: " + ruleset.getId());
                    continue;
                }
                // Send target content and ruleset for validation
                List<RuleViolation> ruleViolations = validationEngine.validate(
                        contentToValidate, ruleset);

                saveEvaluationResults(artifactId, policyId, ruleset.getId(),
                        ruleViolations);

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Ruleset artifact type does not match with the artifact's type. Skipping " +
                            "governance evaluation for ruleset ID: " + ruleset.getId());
                }
            }
        }

        // Delete the evaluation request after processing completes
        complianceMgtDAO.deleteComplianceEvaluationRequest(requestId);

    }

    /**
     * Save compliance evaluation results to the database.
     *
     * @param artifactId     ID of the artifact.
     * @param policyId       ID of the policy.
     * @param rulesetId      ID of the ruleset.
     * @param ruleViolations List of rule violations.
     */
    private static void saveEvaluationResults(String artifactId, String policyId,
                                              String rulesetId,
                                              List<RuleViolation> ruleViolations) {
        try {
            ComplianceEvaluationResult result = new ComplianceEvaluationResult(artifactId,
                    policyId, rulesetId, ruleViolations.isEmpty());

            for (RuleViolation violation : ruleViolations) {
                violation.setPolicyId(policyId);
                violation.setArtifactId(artifactId);
            }

            complianceMgtDAO.addComplianceEvaluationResult(result, ruleViolations);

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
