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
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.ComplianceEvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;
import org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler to process pending compliance evaluation requests.
 */
public class ComplianceEvaluationScheduler {

    private static final Log log = LogFactory.getLog(ComplianceEvaluationScheduler.class);
    private static int threadPoolSize;
    private static int queueSize;
    private static int checkIntervalMinutes;
    private static int cleanupIntervalMinutes;
    private static ScheduledExecutorService scheduler;
    private static ThreadPoolExecutor processorPool;
    private static final ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();

    /**
     * Initialize the evaluation request scheduler.
     */
    public static void initialize() {
        log.info("Initializing Evaluation Request Scheduler...");

        APIMGovernanceConfigDTO apimGovernanceConfigDTO = ServiceReferenceHolder.getInstance()
                .getAPIMConfigurationService().getAPIManagerConfiguration().getAPIMGovernanceConfigurationDto();

        threadPoolSize = apimGovernanceConfigDTO.getSchedulerThreadPoolSize();
        queueSize = apimGovernanceConfigDTO.getSchedulerQueueSize();
        checkIntervalMinutes = apimGovernanceConfigDTO.getSchedulerTaskCheckInterval();
        cleanupIntervalMinutes = apimGovernanceConfigDTO.getSchedulerTaskCleanupInterval();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        processorPool = createProcessorPool();

        scheduler.scheduleAtFixedRate(
                ComplianceEvaluationScheduler::processPendingRequests,
                0, checkIntervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * Create a thread pool for processing requests in parallel.
     *
     * @return Configured ThreadPoolExecutor.
     */
    private static ThreadPoolExecutor createProcessorPool() {
        return new ThreadPoolExecutor(
                threadPoolSize, threadPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize),
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
        logProcessorPoolStatus();
        deleteLongLastingProcessingReqs(); // Clear long-lasting processing requests
        List<ComplianceEvaluationRequest> pendingRequests = fetchPendingRequests(queueSize);

        if (pendingRequests == null || pendingRequests.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No pending evaluation requests found.");
            }
            return;
        }

        List<Future<?>> futures = new ArrayList<>();

        for (ComplianceEvaluationRequest request : pendingRequests) {
            Future<?> future = processorPool.submit(() -> {

                String organization = request.getOrganization();

                // Start tenant flow for this thread, need to get the project from APIM
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(organization, true);
                    String tenantAdminUsername = MultitenantUtils.getTenantAwareUsername(
                            RegistryPersistenceUtil.getTenantAdminUserName(organization));
                    carbonContext.setUsername(tenantAdminUsername);
                    processRequest(request);
                } catch (Throwable e) {
                    log.error("Unhandled exception/error during request processing: " + request.getId(), e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            });
            futures.add(future);
        }

        for (int i = 0; i < futures.size(); i++) {
            Future<?> future = futures.get(i);
            ComplianceEvaluationRequest request = pendingRequests.get(i);

            try {
                future.get();
            } catch (InterruptedException e) {
                log.error("Task interrupted for request " + request.getId() + "for artifact " +
                        request.getArtifactRefId(), e.getCause());
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("Execution error for request " + request.getId() + "for artifact " +
                        request.getArtifactRefId(), e.getCause());
            } catch (Throwable t) {
                log.error("Error processing request " + request.getId() + "for artifact " +
                        request.getArtifactRefId(), t.getCause());
            }
        }


    }

    /**
     * Fetch pending requests from the database.
     *
     * @param limit Maximum number of requests to fetch.
     * @return List of pending requests.
     */
    private static List<ComplianceEvaluationRequest> fetchPendingRequests(Integer limit) {
        try {
            List<ComplianceEvaluationRequest> reqs = complianceMgtDAO
                    .getPendingComplianceEvalRequests();
            AuditLogger.log("Fetch Requests", "Scheduler fetched %s requests with IDs: %s",
                    reqs.size(), Arrays.toString(reqs.stream().map(ComplianceEvaluationRequest::getId).toArray()));
            if (reqs.size() > limit) {
                return reqs.subList(0, limit);
            } else {
                return reqs;
            }
        } catch (APIMGovernanceException e) {
            log.error("Error fetching pending requests: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Process evaluation requests for a given artifact.
     *
     * @param request Evaluation request.
     */
    private static void processRequest(ComplianceEvaluationRequest request) {

        String requestId = request.getId();
        String artifactRefId = request.getArtifactRefId();
        ArtifactType artifactType = request.getArtifactType();
        String organization = request.getOrganization();

        try {
            AuditLogger.log("Async Eval Request", "Processing request %s for artifact %s in %s " +
                    "organization", requestId, artifactRefId, organization);
            // Attempt to process the evaluation request
            boolean isUpdated = complianceMgtDAO.updatePendingRequestToProcessing(request);
            if (!isUpdated) {
                String logMessage = String.format(
                        "Skipping governance evaluation for artifact %s in " +
                                "organization %s as there are processing requests for the same artifact.",
                        artifactRefId, organization);
                log.debug(logMessage);
                AuditLogger.log("Async Eval Request", logMessage);
                complianceMgtDAO.deleteComplianceEvalRequest(requestId);
                return;
            }

            // Check if artifact exists
            if (!APIMGovernanceUtil.isArtifactAvailable(artifactRefId, artifactType, organization)) {
                String logMessage = String.format("Artifact not found for artifact %s in organization %s. " +
                        "Skipping governance evaluation.", artifactRefId, organization);
                log.warn(logMessage);
                AuditLogger.log(AuditLogger.LogLevel.WARN, "Async Eval Request", logMessage);
                complianceMgtDAO.deleteComplianceEvalReqsForArtifact(artifactRefId, artifactType, organization);
                return;
            }

            // Get artifact project
            byte[] artifactProject = APIMGovernanceUtil.getArtifactProject(artifactRefId, artifactType, organization);

            // If artifact project does not exist, skip evaluation
            if (artifactProject == null) {
                String logMessage = String.format("Artifact project not found for artifact %s in organization %s. " +
                        "Skipping governance evaluation.", artifactRefId, organization);
                log.warn(logMessage);
                AuditLogger.log(AuditLogger.LogLevel.WARN, "Async Eval Request", logMessage);
                complianceMgtDAO.deleteComplianceEvalReqsForArtifact(artifactRefId, artifactType, organization);
                return;
            }

            // Extract artifact project content to map
            Map<RuleType, String> artifactProjectContentMap = APIMGovernanceUtil.extractArtifactProjectContent
                    (artifactProject, artifactType);

            // Evaluate the artifact against each policy
            for (String policyId : request.getPolicyIds()) {
                evaluateArtifactWithPolicy(artifactRefId, artifactType, policyId, artifactProjectContentMap,
                        organization);
                AuditLogger.log("Async Eval Request", "Artifact %s evaluated against policy %s " +
                        "in organization %s", artifactRefId, policyId, organization);
            }

            // Delete the evaluation request after processing completes
            complianceMgtDAO.deleteComplianceEvalRequest(requestId);
            AuditLogger.log("Async Eval Request", "Request %s processed successfully for artifact %s " +
                    "in organization %s", requestId, artifactRefId, organization);
        } catch (APIMGovernanceException e) {
            log.error("Error processing evaluation request for artifact " + artifactRefId, e);
        }

    }

    /**
     * Evaluate an artifact against a policy.
     *
     * @param artifactRefId             ID of the artifact.
     * @param artifactType              Type of the artifact.
     * @param policyId                  ID of the policy.
     * @param artifactProjectContentMap Content of the artifact project.
     * @param organization              Organization of the artifact.
     * @throws APIMGovernanceException If an error occurs while evaluating the artifact.
     */
    private static void evaluateArtifactWithPolicy(String artifactRefId, ArtifactType artifactType, String policyId,
                                                   Map<RuleType, String> artifactProjectContentMap, String organization)
            throws APIMGovernanceException {

        ValidationEngine validationEngine = ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        // Validate the artifact against each ruleset
        List<Ruleset> rulesets = GovernancePolicyMgtDAOImpl.getInstance()
                .getRulesetsWithContentByPolicyId(policyId, organization);

        Map<String, List<RuleViolation>> rulesetViolationsMap = new HashMap<>();
        int skippedRulesets = 0;

        for (Ruleset ruleset : rulesets) {
            List<RuleViolation> ruleViolations = new ArrayList<>();

            // Check if ruleset's artifact type matches with the artifact's type
            ExtendedArtifactType extendedArtifactType = ruleset.getArtifactType();
            if (extendedArtifactType.equals(APIMGovernanceUtil
                    .getExtendedArtifactTypeForArtifact(artifactRefId, artifactType))) {

                // Get target file content from artifact project based on ruleType
                RuleType ruleType = ruleset.getRuleType();
                String contentToValidate = artifactProjectContentMap.get(ruleType);

                if (contentToValidate == null) {
                    log.warn(ruleType + " content not found in artifact project for artifact " +
                            artifactRefId + ". Skipping governance evaluation for ruleset " + ruleset.getId());
                    continue;
                }

                // Send target content and ruleset for validation
                List<RuleViolation> violations = validationEngine.validate(contentToValidate, ruleset);
                AuditLogger.log("Async Eval Request", "Validated artifact %s " +
                                "in organization %s against ruleset %s", artifactRefId,
                        organization, ruleset.getId());
                ruleViolations.addAll(violations);
                rulesetViolationsMap.put(ruleset.getId(), ruleViolations);

            } else {
                skippedRulesets++;
                String logMessage = String.format("Skipping governance evaluation for artifact %s in organization %s " +
                                "against ruleset %s as the artifact type does not match",
                        artifactRefId, organization, ruleset.getId());
                log.debug(logMessage);
                AuditLogger.log("Async Eval Request", logMessage);
            }
        }
        if (skippedRulesets == rulesets.size()) {
            String logMessage = String.format("All rulesets in policy %s are skipped for artifact %s in organization " +
                    "%s as the artifact type does not match", policyId, artifactRefId, organization);
            log.debug(logMessage);
            AuditLogger.log("Async Eval Request", logMessage);
            return;
        }
        savePolicyEvaluationResults(artifactRefId, artifactType, policyId, rulesetViolationsMap,
                organization);
    }

    /**
     * Save compliance evaluation results of the policy.
     *
     * @param artifactRefId        ID of the artifact.
     * @param artifactType         Type of the artifact.
     * @param policyId             ID of the policy.
     * @param rulesetViolationsMap Map of rule violations for each ruleset.
     * @param organization         Organization of the artifact.
     */
    private static void savePolicyEvaluationResults(String artifactRefId, ArtifactType artifactType, String policyId,
                                                    Map<String, List<RuleViolation>> rulesetViolationsMap,
                                                    String organization) {
        try {
            complianceMgtDAO.addComplianceEvalResults(artifactRefId, artifactType, policyId, rulesetViolationsMap,
                    organization);
        } catch (APIMGovernanceException e) {
            log.error("Error saving governance results for artifact " + artifactRefId, e);
        }
    }

    /**
     * Delete long-lasting processing requests.
     */
    private static void deleteLongLastingProcessingReqs() {
        try {
            List<String> delIds = complianceMgtDAO.deleteLongLastingProcessingReqs(cleanupIntervalMinutes);
            AuditLogger.log("Delete Processing Requests", "Deleted %s long-lasting processing requests with IDs: %s",
                    delIds.size(), Arrays.toString(delIds.toArray()));
        } catch (APIMGovernanceException e) {
            log.error("Error resetting long lasting processing requests: " + e.getMessage(), e);
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

    /**
     * Log the status of the processor pool.
     */
    private static void logProcessorPoolStatus() {
        if (processorPool != null) {
            AuditLogger.log("Scheduler Thread Pool Status",
                    "Active Thread Count: %s -- | -- " +
                            "Completed Task Count: %s -- | -- " +
                            "Submitted Task Count: %s -- | -- " +
                            "Queue Size: %s -- | -- " +
                            "Core Pool Size: %s -- | -- " +
                            "Maximum Pool Size: %s -- | -- " +
                            "Current Pool Size: %s",
                    processorPool.getActiveCount(),
                    processorPool.getCompletedTaskCount(),
                    processorPool.getTaskCount(),
                    processorPool.getQueue().size(),
                    processorPool.getCorePoolSize(),
                    processorPool.getMaximumPoolSize(),
                    processorPool.getPoolSize());
        }
    }
}
