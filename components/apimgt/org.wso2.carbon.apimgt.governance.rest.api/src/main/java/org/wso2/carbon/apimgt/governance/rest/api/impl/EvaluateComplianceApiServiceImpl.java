package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.ComplianceManager;
import org.wso2.carbon.apimgt.governance.api.PolicyManager;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.impl.PolicyManagerImpl;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.rest.api.EvaluateComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;

import java.util.ArrayList;
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
     * @param artifactId              Artifact ID
     * @param artifactType            Artifact Type
     * @param artifactEvaluationState Artifact Evaluation State
     * @param artifactZipInputStream  Artifact ZIP InputStream
     * @param artifactZipDetail       Artifact ZIP Detail
     * @param messageContext          Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while evaluating the compliance of the artifact
     */
    public Response evaluateCompliance(String artifactId, String artifactType,
                                       String artifactEvaluationState, InputStream artifactZipInputStream,
                                       Attachment artifactZipDetail, MessageContext messageContext)
            throws GovernanceException {

        List<String> labels = new ArrayList<>(); // TODO: Get labels from APIM
        PolicyManager policyManager = new PolicyManagerImpl();
        ComplianceManager complianceManager = new ComplianceManagerImpl();
        String organization = GovernanceAPIUtil.getValidatedOrganization(messageContext);

        // Check for policies using labels and the state
        List<String> policies = new ArrayList<>();
        for (String label : labels) {
            // Get policies for the label and state
            List<String> policiesForLabel = policyManager.getPoliciesByLabelAndState(label,
                    artifactEvaluationState, organization);
            if (policiesForLabel != null) {
                policies.addAll(policiesForLabel);
            }
        }

        policies.addAll(policyManager.getOrganizationWidePoliciesByState(artifactEvaluationState,
                organization));

        // Skip compliance evaluation if no policies are found for the particular set of labels and state
        if (policies.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No applicable governance policies found,  hence skipping compliance evaluation for " +
                        "artifact: " + artifactId);
            }
            return Response.ok().build();
        }

        // Check for presence of blocking actions
        boolean isBlockingPolicyPresent = false;
        for (String policyId : policies) {
            if (policyManager.isBlockingActionPresentForState(policyId, artifactEvaluationState)) {
                isBlockingPolicyPresent = true;
                break;
            }
        }

        if (isBlockingPolicyPresent) {
            //TODO: Handle evaluation synchronously
            return Response.ok().build();
        } else {
            //TODO: Handle evaluation asynchronously
//            complianceManager.handle(artifactId, artifactType,
//                    artifactFileType, artifactEvaluationState, artifactFileInputStream, organization);
            return Response.accepted().build();
        }

    }
}
