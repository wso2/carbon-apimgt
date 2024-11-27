package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.manager.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManagerImpl;
import org.wso2.carbon.apimgt.governance.rest.api.AssessComplianceApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.rest.api.dto.AssessAPIComplianceRequestDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * This is the implementation class for the Assess Compliance API.
 */
public class AssessComplianceApiServiceImpl implements AssessComplianceApiService {
    private static final Log log = LogFactory.getLog(AssessComplianceApiServiceImpl.class);

    private final ExecutorService complianceExecutorService = Executors.newFixedThreadPool(10,
            new ComplianceAssessmentThreadFactory());

    /**
     * Assess the compliance of an API
     *
     * @param apiId                         API ID
     * @param organization                  Organization Name
     * @param assessAPIComplianceRequestDTO Assess API Compliance Request DTO
     * @param messageContext                Message Context
     * @return Response
     * @throws GovernanceException If an error occurs while assessing the compliance
     */
    public Response assessAPICompliance(String apiId, String organization,
                                        AssessAPIComplianceRequestDTO assessAPIComplianceRequestDTO,
                                        MessageContext messageContext) throws GovernanceException {
        ComplianceManager complianceManager = new ComplianceManagerImpl();

        // Retrieve policies and their associated rulesets
        Map<String, Map<String, String>> policyToRulesetToContentMap = complianceManager.
                getAssociatedRulesetsByPolicy(organization);

        String authorizationHeader = messageContext.getHttpHeaders().
                getHeaderString(HttpHeaders.AUTHORIZATION);

        // Submit the compliance assessment task to the executor service
        complianceExecutorService.submit(() -> {
            log.info("Assessing compliance for API: " + apiId + " asynchronously.");
            complianceManager.assessAPICompliance(apiId, organization,
                    policyToRulesetToContentMap, authorizationHeader);
        });
        return Response.accepted().build();
    }

    public Response deleteAPI(String apiId, String organization, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * Thread factory to create compliance assessment threads.
     */
    private static class ComplianceAssessmentThreadFactory implements ThreadFactory {
        private int count = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ComplianceAssessmentThread-thread-" + count++);
        }
    }
}
