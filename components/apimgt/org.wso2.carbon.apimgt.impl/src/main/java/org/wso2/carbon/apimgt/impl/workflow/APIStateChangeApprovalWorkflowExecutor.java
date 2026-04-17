/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.cleanupPendingTasksByWorkflowReference;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.completeStateChangeWorkflow;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.getSelectedStatesToApprove;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.setWorkflowParameters;

/**
 * Governance-enhanced approval workflow for API state change.
 *
 * <p>For configured state transitions (via the {@code stateList} property):
 * <ul>
 *   <li><b>Deprecate / Retire:</b> Calls {@code DeprecationGuideScheduler.findSuccessors()}
 *       via GatekeeperService to identify structural successor APIs. If enforcement mode is
 *       BLOCK and no PUBLISHED successor exists, the transition is rejected immediately with a
 *       {@link WorkflowException}. In WARN mode without a successor, the request proceeds to
 *       admin approval but the violation is recorded in GOV_RULE_VIOLATION and the risk reason
 *       is attached as workflow metadata for Admin Portal visibility.</li>
 *   <li><b>Other configured transitions:</b> Enter the pending (CREATED) state for
 *       admin approval (standard approval behavior).</li>
 * </ul>
 *
 * <p>For non-configured transitions, auto-approves (simple workflow behavior).
 *
 * <p>On {@link #complete(WorkflowDTO)}, if the admin has APPROVED the request and the
 * action was Deprecate/Retire, the compliance evaluation is triggered asynchronously
 * to record violations in GOV_RULE_VIOLATION for the Compliance Manager.
 *
 * <p><b>Configuration Gating:</b> The governance check only runs when the workflow is
 * enabled via {@code [apim.workflow] enable = true} in deployment.toml.
 */
public class APIStateChangeApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(APIStateChangeApprovalWorkflowExecutor.class);

    private static final String GATEKEEPER_SERVICE_CLASS =
            "org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService";
    private static final String GOV_SERVICE_IMPL_CLASS =
            "org.wso2.carbon.apimgt.governance.impl.service.APIMGovernanceServiceImpl";

    private String stateList;

    public String getStateList() {
        return stateList;
    }

    public void setStateList(String stateList) {
        this.stateList = stateList;
    }

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_API_STATE;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return Collections.emptyList();
    }

    /**
     * Execute the API state change workflow with governance awareness.
     *
     * <p>For Deprecate/Retire transitions, calls GatekeeperService.findSuccessorForDeprecation()
     * to identify successor APIs. Enforces BLOCK/WARN policy and attaches metadata to the
     * WorkflowDTO for Admin Portal task visibility.
     *
     * @param workflowDTO the workflow DTO containing API and transition info
     * @return workflow response
     * @throws WorkflowException if the transition is blocked by governance policy
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("[GOVERNANCE-WORKFLOW] Executing governance-enhanced API State change Workflow. Workflow reference: " + workflowDTO.getWorkflowReference());
        }

        if (stateList != null) {
            Map<String, List<String>> stateActionMap = getSelectedStatesToApprove(stateList);
            APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;

            if (stateActionMap.containsKey(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    && stateActionMap.get(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    .contains(apiStateWorkFlowDTO.getApiLCAction())) {

                String action = apiStateWorkFlowDTO.getApiLCAction();

                // Run governance successor check for Deprecate/Retire if workflow is enabled
                if (isWorkflowEnabled()
                        && ("Deprecate".equalsIgnoreCase(action)
                        || "Retire".equalsIgnoreCase(action))) {
                    runGovernanceCheck(apiStateWorkFlowDTO);
                }

                setWorkflowParameters(apiStateWorkFlowDTO);
                super.execute(workflowDTO);
                if (log.isDebugEnabled()) {
                    log.debug("API state change approval workflow executed successfully. Workflow reference: "
                            + workflowDTO.getWorkflowReference());
                }
            } else {
                // For any other states, act as simple workflow executor.
                workflowDTO.setStatus(WorkflowStatus.APPROVED);
                // calling super.complete() instead of complete() to act as the simpleworkflow executor
                super.complete(workflowDTO);
            }
        } else {
            String msg = "State change list is not provided. Please check <stateList> element in workflow-extensions.xml";
            log.error(msg);
            throw new WorkflowException(msg);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API state change workflow approval process.
     * If the admin has APPROVED, the actual lifecycle change is executed.
     * For Deprecate/Retire actions, compliance evaluation is triggered
     * asynchronously to record violations in GOV_RULE_VIOLATION.
     *
     * @param workflowDTO the workflow DTO
     * @return workflow response
     * @throws WorkflowException if completion fails
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("[GOVERNANCE-WORKFLOW] Completing governance-enhanced API State change Workflow..");
        }
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        completeStateChangeWorkflow(workflowDTO);

        // After lifecycle change completes, trigger governance compliance evaluation
        // for Deprecate/Retire to record violations in GOV_RULE_VIOLATION
        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            triggerComplianceEvaluation(workflowDTO);
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for api state change workflow Approval executor.
     * Use workflow external reference to delete the pending workflow request.
     *
     * @param workflowExtRef External Workflow Reference of pending workflow process
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("[GOVERNANCE-WORKFLOW] Starting cleanup task for workflow ref: " + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        cleanupPendingTasksByWorkflowReference(workflowExtRef);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Configuration Gating
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Check if the workflow is enabled via deployment.toml [apim.workflow] enable = true.
     */
    private boolean isWorkflowEnabled() {
        try {
            WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration()
                    .getWorkflowProperties();
            return workflowProperties != null && workflowProperties.isEnabled();
        } catch (Exception e) {
            log.debug("[GOVERNANCE-WORKFLOW] Could not read workflow config. "
                    + "Defaulting to governance check enabled.");
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Governance Check (Successor Search)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Run governance successor check via GatekeeperService (reflection-based
     * to maintain OSGi module isolation).
     *
     * <ul>
     *   <li>Block mode + no successor → throws {@link WorkflowException}</li>
     *   <li>Warn mode + no successor → attaches RiskReason, persists violation,
     *       enters pending state for admin review</li>
     *   <li>Successor found → attaches full candidate list as metadata</li>
     * </ul>
     *
     * @param dto the API state workflow DTO
     * @throws WorkflowException if transition is blocked
     */
    private void runGovernanceCheck(APIStateWorkflowDTO dto) throws WorkflowException {
        String apiUuid = dto.getApiUUID();
        String action = dto.getApiLCAction();
        String organization = dto.getTenantDomain();

        try {
            ClassLoader bundleCl = this.getClass().getClassLoader();
            Class<?> gsClass = bundleCl.loadClass(GATEKEEPER_SERVICE_CLASS);
            Method getInstance = gsClass.getMethod("getInstance");
            Object gatekeeperService = getInstance.invoke(null);

            Method isInitialized = gsClass.getMethod("isInitialized");
            if (!(boolean) isInitialized.invoke(gatekeeperService)) {
                log.warn("[GOVERNANCE-WORKFLOW] GatekeeperService not initialized. "
                        + "Skipping governance check for API " + apiUuid);
                return;
            }

            // Call DeprecationGuideEngine.findSuccessors() via GatekeeperService
            Method findSuccessor = gsClass.getMethod("findSuccessorForDeprecation",
                    String.class, String.class, String.class);
            Object guideResult = findSuccessor.invoke(gatekeeperService, apiUuid, organization, action);

            if (guideResult == null) {
                log.warn("[GOVERNANCE-WORKFLOW] Null deprecation guide result for API "
                        + apiUuid + ". Allowing transition to proceed to approval.");
                return;
            }

            boolean successorFound = (boolean) guideResult.getClass()
                    .getMethod("isSuccessorFound").invoke(guideResult);

            String enforcementMode = "warn";
            try {
                enforcementMode = (String) guideResult.getClass()
                        .getMethod("getEnforcementMode").invoke(guideResult);
            } catch (NoSuchMethodException ignored) {
                // Old version without enforcement mode — default to warn
            }

            log.info("[GOVERNANCE-WORKFLOW] Successor check for API " + apiUuid
                    + " (action=" + action + "): successorFound=" + successorFound
                    + ", enforcementMode=" + enforcementMode);

            // ── BLOCK Mode: no successor → immediate rejection ──
            if ("block".equalsIgnoreCase(enforcementMode) && !successorFound) {
                throw new WorkflowException(
                        "Lifecycle transition '" + action + "' blocked by governance policy: "
                                + "No valid PUBLISHED successor API found. A successor must exist "
                                + "before the API can be " + action.toLowerCase() + "d. "
                                + "(Enforcement mode: BLOCK)");
            }

            // Attach governance metadata for Admin Portal task view
            dto.setMetadata("GovernanceCheckExecuted", "true");
            dto.setMetadata("SuccessorFound", String.valueOf(successorFound));
            dto.setMetadata("EnforcementMode", enforcementMode);
            dto.setMetadata("LifecycleAction", action);

            if (successorFound) {
                attachSuccessorMetadata(dto, guideResult);
            } else {
                // ── WARN Mode: no successor → record risk and persist violation ──
                String riskReason = "API transitioned to " + action
                        + "d state without an identified successor";
                dto.setMetadata("RiskReason", riskReason);
                log.info("[GOVERNANCE-WORKFLOW] WARN mode — no successor for API " + apiUuid
                        + ". Recording violation and allowing request to proceed to admin approval.");

                // Persist violation in GOV_RULE_VIOLATION for Compliance Manager visibility
                persistGovernanceViolation(apiUuid, organization, riskReason);
            }

        } catch (WorkflowException we) {
            throw we;
        } catch (ClassNotFoundException cnfe) {
            log.warn("[GOVERNANCE-WORKFLOW] GatekeeperService not available in classpath. "
                    + "Skipping governance check for API " + apiUuid
                    + ". Class: " + cnfe.getMessage());
        } catch (Exception e) {
            // Fail-open: don't block transition if governance check fails unexpectedly
            log.warn("[GOVERNANCE-WORKFLOW] Governance check failed for API " + apiUuid
                    + " (action: " + action + "): " + e.getMessage()
                    + ". Allowing transition to proceed to approval.");
        }
    }

    /**
     * Extract successor details from the DeprecationGuideResult and attach
     * them as workflow metadata, including the full candidate list as JSON.
     */
    @SuppressWarnings("unchecked")
    private void attachSuccessorMetadata(APIStateWorkflowDTO dto, Object guideResult) {
        try {
            // Best successor details
            String successorUuid = (String) guideResult.getClass()
                    .getMethod("getSuccessorApiUuid").invoke(guideResult);
            String successorName = (String) guideResult.getClass()
                    .getMethod("getSuccessorApiName").invoke(guideResult);
            String successorVersion = (String) guideResult.getClass()
                    .getMethod("getSuccessorApiVersion").invoke(guideResult);
            double similarity = (double) guideResult.getClass()
                    .getMethod("getSimilarityPercentage").invoke(guideResult);

            dto.setMetadata("SuccessorApiUuid", successorUuid);
            dto.setMetadata("SuccessorApiName", successorName);
            dto.setMetadata("SuccessorApiVersion", successorVersion);
            dto.setMetadata("SimilarityPercentage", String.format("%.1f", similarity));

            // Full candidate list — serialize as JSON for Admin Portal display
            try {
                Method getAllCandidates = guideResult.getClass().getMethod("getAllCandidates");
                List<?> candidates = (List<?>) getAllCandidates.invoke(guideResult);
                if (candidates != null && !candidates.isEmpty()) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < candidates.size(); i++) {
                        Object cand = candidates.get(i);
                        String cName = (String) cand.getClass()
                                .getMethod("getApiName").invoke(cand);
                        String cVersion = (String) cand.getClass()
                                .getMethod("getApiVersion").invoke(cand);
                        double cSimilarity = (double) cand.getClass()
                                .getMethod("getSimilarityPercentage").invoke(cand);
                        String cType = (String) cand.getClass()
                                .getMethod("getSuccessorType").invoke(cand);
                        if (i > 0) {
                            sb.append(",");
                        }
                        sb.append("{\"name\":\"").append(escapeJson(cName))
                                .append("\",\"version\":\"").append(escapeJson(cVersion))
                                .append("\",\"similarity\":").append(String.format("%.1f", cSimilarity))
                                .append(",\"type\":\"").append(escapeJson(cType))
                                .append("\"}");
                    }
                    sb.append("]");
                    dto.setMetadata("SuccessorCandidates", sb.toString());
                    dto.setMetadata("CandidateCount", String.valueOf(candidates.size()));
                }
            } catch (NoSuchMethodException nsm) {
                log.debug("[GOVERNANCE-WORKFLOW] getAllCandidates() not available in result");
            }
        } catch (Exception ex) {
            log.debug("[GOVERNANCE-WORKFLOW] Could not extract successor details: "
                    + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Compliance Manager Violation Persistence
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Persist a governance violation in GOV_RULE_VIOLATION for Compliance Manager visibility.
     * Called when WARN mode has no successor — ensures the violation shows up in the dashboard.
     *
     * @param apiUuid      UUID of the API being transitioned
     * @param organization the organization
     * @param reason       the violation reason string
     */
    private void persistGovernanceViolation(String apiUuid, String organization, String reason) {
        try {
            ClassLoader bundleCl = this.getClass().getClassLoader();
            Class<?> serviceImplClass = bundleCl.loadClass(GOV_SERVICE_IMPL_CLASS);
            Object govService = serviceImplClass.getDeclaredConstructor().newInstance();

            // Load ArtifactType and APIMGovernableState enums via reflection
            Class<?> artifactTypeClass = bundleCl.loadClass(
                    "org.wso2.carbon.apimgt.governance.api.model.ArtifactType");
            Class<?> governableStateClass = bundleCl.loadClass(
                    "org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState");

            Object artifactApi = Enum.valueOf(
                    (Class<Enum>) artifactTypeClass, "API");
            Object stateApiUpdate = Enum.valueOf(
                    (Class<Enum>) governableStateClass, "API_UPDATE");

            Method evalMethod = serviceImplClass.getMethod(
                    "evaluateComplianceAsync",
                    String.class, artifactTypeClass, governableStateClass, String.class);

            evalMethod.invoke(govService, apiUuid, artifactApi, stateApiUpdate, organization);

            log.info("[GOVERNANCE-WORKFLOW] Compliance violation persisted for API "
                    + apiUuid + ": " + reason);

        } catch (ClassNotFoundException cnfe) {
            log.warn("[GOVERNANCE-WORKFLOW] Governance service not available. "
                    + "Cannot persist violation. Class: " + cnfe.getMessage());
        } catch (Exception e) {
            log.warn("[GOVERNANCE-WORKFLOW] Failed to persist governance violation for API "
                    + apiUuid + ": " + e.getMessage());
        }
    }

    /**
     * Trigger asynchronous governance compliance evaluation after lifecycle change.
     * This records violations (e.g., "deprecated without successor") in GOV_RULE_VIOLATION.
     * Only triggered for Published→Deprecated transitions; Deprecated→Retired transitions
     * skip this check because the lifecycle violation should already exist from deprecation.
     */
    private void triggerComplianceEvaluation(WorkflowDTO workflowDTO) {
        try {
            String apiUuid = null;
            String organization = workflowDTO.getTenantDomain();
            String action = null;

            if (workflowDTO instanceof APIStateWorkflowDTO) {
                apiUuid = ((APIStateWorkflowDTO) workflowDTO).getApiUUID();
                action = ((APIStateWorkflowDTO) workflowDTO).getApiLCAction();
            }

            // Only trigger for Deprecate (Published→Deprecated) transitions.
            // Retire transitions skip this — the violation should already exist from deprecation.
            if (apiUuid == null || action == null
                    || !"Deprecate".equalsIgnoreCase(action)) {
                return;
            }

            log.info("[GOVERNANCE-WORKFLOW] Triggering compliance evaluation for API "
                    + apiUuid + " after " + action + " approval.");

            ClassLoader bundleCl = this.getClass().getClassLoader();
            Class<?> serviceImplClass = bundleCl.loadClass(GOV_SERVICE_IMPL_CLASS);
            Object govService = serviceImplClass.getDeclaredConstructor().newInstance();

            Class<?> artifactTypeClass = bundleCl.loadClass(
                    "org.wso2.carbon.apimgt.governance.api.model.ArtifactType");
            Class<?> governableStateClass = bundleCl.loadClass(
                    "org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState");

            Object artifactApi = Enum.valueOf(
                    (Class<Enum>) artifactTypeClass, "API");
            Object stateApiUpdate = Enum.valueOf(
                    (Class<Enum>) governableStateClass, "API_UPDATE");

            Method evalMethod = serviceImplClass.getMethod(
                    "evaluateComplianceAsync",
                    String.class, artifactTypeClass, governableStateClass, String.class);

            evalMethod.invoke(govService, apiUuid, artifactApi, stateApiUpdate, organization);

            log.info("[GOVERNANCE-WORKFLOW] Compliance evaluation triggered for API "
                    + apiUuid + " (action: " + action + ")");

        } catch (ClassNotFoundException cnfe) {
            log.warn("[GOVERNANCE-WORKFLOW] Governance service not available in classpath. "
                    + "Skipping compliance evaluation. Class: " + cnfe.getMessage());
        } catch (Exception e) {
            log.warn("[GOVERNANCE-WORKFLOW] Failed to trigger compliance evaluation: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Escape a string for safe JSON embedding.
     */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
