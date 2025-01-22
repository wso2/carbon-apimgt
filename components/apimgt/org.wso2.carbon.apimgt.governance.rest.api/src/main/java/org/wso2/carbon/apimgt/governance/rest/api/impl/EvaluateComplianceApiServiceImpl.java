package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.GovernableState;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.apimgt.governance.rest.api.EvaluateComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;


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
        List<String> policyIds = GovernanceUtil.getApplicableGovPoliciesForArtifact(artifactId,
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
}
