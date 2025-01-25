package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.EvaluateComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;


/**
 * This class implements the Compliance Evaluation
 */
public class EvaluateComplianceApiServiceImpl implements EvaluateComplianceApiService {

    private static final Log log = LogFactory.getLog(EvaluateComplianceApiServiceImpl.class);

    /**
     * Evaluate compliance of an artifact
     *
     * @param artifactId            Artifact ID
     * @param artifactType          Artifact Type
     * @param governableState       The state at which the artifact should be governed
     * @param targetFileInputStream Target File Input Stream
     * @param targetFileDetail      Target File Detail
     * @param messageContext        Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while evaluating the compliance of the artifact
     */
    public Response evaluateCompliance(String artifactId, String artifactType,
                                       String governableState, InputStream targetFileInputStream,
                                       Attachment targetFileDetail, MessageContext messageContext)
    throws GovernanceException {


        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        // Evaluate compliance for dependent states asynchronously
        evaluateComplianceForDependentStates(artifactId, artifactType, governableState, organization);

        List<String> policyIds = GovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactId,
                ArtifactType.fromString(artifactType),
                GovernableState.fromString(governableState), organization);

        // Skip compliance evaluation if no policies are found for the particular set of labels and state
        if (policyIds.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No applicable governance policies found,  hence skipping compliance evaluation for " +
                        "artifact: " + artifactId);
            }
            return Response.ok().build();
        }
        boolean isBlocking = GovernanceUtil.isBlockingActionsPresent(policyIds,
                GovernableState.fromString(governableState));

        if (isBlocking) {
            //TODO: Handle evaluation synchronously
            return Response.ok().build();
        } else {
            new ComplianceManagerImpl().handleComplianceEvaluationAsync(artifactId,
                    ArtifactType.fromString(artifactType),
                    policyIds, organization);
            return Response.accepted().build();
        }

    }

    /**
     * Evaluate compliance for dependent states.
     * That is when a request is made to govern an artifact at a particular state, we need to evaluate the compliance
     * for all the states before that state as well.
     *
     * @param artifactId      Artifact ID
     * @param artifactType    Artifact Type
     * @param governableState The state at which the artifact should be governed
     * @param organization    Organization
     * @throws GovernanceException If an error occurs while evaluating the compliance for dependent states
     */
    private void evaluateComplianceForDependentStates(String artifactId, String artifactType,
                                                      String governableState, String organization)
            throws GovernanceException {
        GovernableState state = GovernableState.fromString(governableState);
        if (state == null) {
            throw new GovernanceException("Invalid governable state: " + governableState);
        }
        List<GovernableState> dependentStates = GovernableState.getDependentGovernableStates(state);
        for (GovernableState dependentState : dependentStates) {
            List<String> policyIds = GovernanceUtil.getApplicablePoliciesForArtifactWithState(artifactId,
                    ArtifactType.fromString(artifactType), dependentState, organization);
            new ComplianceManagerImpl().handleComplianceEvaluationAsync(artifactId,
                    ArtifactType.fromString(artifactType), policyIds, organization);
        }
    }
}
