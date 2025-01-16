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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.EvaluationRequest;
import org.wso2.carbon.apimgt.governance.api.model.EvaluationStatus;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.governance.impl.dao.ComplianceMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.GovernancePolicyMgtDAO;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.ComplianceMgtDAOImpl;
import org.wso2.carbon.apimgt.governance.impl.dao.impl.GovernancePolicyMgtDAOImpl;

/**
 * This class represents the scheduler to process pending governance evaluation requests
 */
public class EvaluationRequestScheduler {

    private static final Log log = LogFactory.getLog(EvaluationRequestScheduler.class);

    private static final int EVALUATION_THREAD_POOL_SIZE = 10;
    private static final int EVALUATION_THREAD_POOL_QUEUE_SIZE = 255;
    private static final int CHECK_INTERVAL_MINUTES = 5;

    private static ScheduledExecutorService requestScheduler;
    private static ThreadPoolExecutor requestProcessorPool;

    /**
     * Initialize the evaluation request scheduler
     */
    public static void initialize() {
        log.info("Initializing Evaluation Request Scheduler...");

        requestScheduler = Executors.newSingleThreadScheduledExecutor();

        // Thread pool to process requests in parallel
        requestProcessorPool = createRequestProcessorPool();
        log.info("Request Processor Pool with size " + EVALUATION_THREAD_POOL_SIZE + " created.");

        scheduleNextCheck();
    }

    /**
     * Create a thread pool to process requests in parallel
     *
     * @return Request processor thread pool
     */
    private static ThreadPoolExecutor createRequestProcessorPool() {
        return new ThreadPoolExecutor(
                EVALUATION_THREAD_POOL_SIZE,
                EVALUATION_THREAD_POOL_SIZE,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(
                        EVALUATION_THREAD_POOL_QUEUE_SIZE), // Store pending requests in a queue
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy() // Discard new requests if queue is full
        );
    }

    /**
     * Schedule the next request check
     */
    private static void scheduleNextCheck() {
        requestScheduler.schedule(() -> {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Checking for pending evaluation requests...");
                }
                List<EvaluationRequest> pendingEvaluationRequests = fetchPendingRequests();

                if (pendingEvaluationRequests == null ||
                        pendingEvaluationRequests.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("No pending evaluation requests found. Waiting for next check...");
                    }
                    waitAndScheduleNextCheck(CHECK_INTERVAL_MINUTES);
                    return;
                }

                // Submit all pending requests to the thread pool
                for (EvaluationRequest request : pendingEvaluationRequests) {
                    requestProcessorPool.submit(() -> processRequest(request));
                }

                // Wait for all submitted requests to complete
                requestProcessorPool.shutdown();
                if (requestProcessorPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                    if (log.isDebugEnabled()) {
                        log.debug("All requests processed. Waiting for next check...");
                    }
                    resetRequestProcessorPool();
                    waitAndScheduleNextCheck(0);
                }
            } catch (Exception e) {
                log.error("Error while processing requests: ", e);
                waitAndScheduleNextCheck(CHECK_INTERVAL_MINUTES);
            }
        }, 0, TimeUnit.SECONDS);
    }

    /**
     * Wait for the specified time and schedule the next request check
     *
     * @param checkIntervalMinutes Check interval in minutes
     */
    private static void waitAndScheduleNextCheck(int checkIntervalMinutes) {
        requestScheduler.schedule(() -> {
            if (log.isDebugEnabled()) {
                log.debug("Starting next check after " + CHECK_INTERVAL_MINUTES + " minutes...");
            }
            scheduleNextCheck();
        }, checkIntervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * Reset the request processor pool
     */
    private static void resetRequestProcessorPool() {
        requestProcessorPool = createRequestProcessorPool();
    }

    /**
     * Fetch pending requests from the database or queue
     *
     * @return List of pending requests
     */
    private static List<EvaluationRequest> fetchPendingRequests() {
        try {
            ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
            return complianceMgtDAO.getPendingEvaluationRequests();
        } catch (GovernanceException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Process an evaluation request
     *
     * @param request Evaluation request to process
     */
    private static void processRequest(EvaluationRequest request) {
        //TODO: Add missing pieces of impl
        try {
            ComplianceMgtDAO complianceMgtDAO = ComplianceMgtDAOImpl.getInstance();
            complianceMgtDAO.updateEvaluationStatus(request.getId(), EvaluationStatus.PROCESSING);

            // Check presence of artifact on APIM and get Artifact Type
            String apimArtifactType = "";

            // Get artifact project from APIM

            // Get Rulesets for Policy
            GovernancePolicyMgtDAO policyMgtDAO = GovernancePolicyMgtDAOImpl.getInstance();
            List<Ruleset> rulesets = policyMgtDAO.getRulesetsByPolicyId(request.getPolicyId());

            for (Ruleset ruleset : rulesets) {
                ArtifactType artifactType = request.getArtifactType();

                // Check if artifact type matches
                if (!ArtifactType.ALL_API.equals(artifactType)) {
                    if (!artifactType.equals(ArtifactType.fromAPIMArtifactType(apimArtifactType))) {
                        continue;
                    }
                }

                // Get target file content from APIM project based on ruleType
                RuleType ruleType = ruleset.getRuleType();

                // Send target file and ruleset content for spectral for validation
                InputStream content = ruleset.getRulesetContent();


            }

        } catch (GovernanceException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shutdown the evaluation request scheduler
     */
    public static void shutdown() {
        log.info("Shutting down Evaluation Request Scheduler...");

        // Shutdown the request scheduler
        if (requestScheduler != null) {
            requestScheduler.shutdown();
            try {
                if (!requestScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Forcing request scheduler shutdown...");
                    requestScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Request scheduler shutdown interrupted: ", e);
                Thread.currentThread().interrupt();
            }
        }

        // Shutdown the request processor pool
        if (requestProcessorPool != null) {
            requestProcessorPool.shutdown();
            try {
                if (!requestProcessorPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Forcing request processor pool shutdown...");
                    requestProcessorPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Request processor pool shutdown interrupted: ", e);
                Thread.currentThread().interrupt();
            }
        }

        log.info("Evaluation Request Scheduler shut down.");
    }

}
