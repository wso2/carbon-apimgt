package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.ComplianceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.Response;

/**
 * This class implements the ArtifactComplianceApiService interface.
 */
public class ArtifactComplianceApiServiceImpl implements ArtifactComplianceApiService {

    private static final Log log = LogFactory.getLog(ArtifactComplianceApiServiceImpl.class);

    /**
     * This method retrieves compliance details for a specific API.
     *
     * @param apiId          The UUID of the API.
     * @param messageContext The message context.
     * @return The compliance details for the specified API.
     */
    public Response getComplianceByAPIId(String apiId, MessageContext messageContext) throws APIMGovernanceException {

        ArtifactType artifactType = ArtifactType.API;
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving compliance details for API: " + apiId + " in organization: " + organization);
        }

        ArtifactComplianceDetailsDTO detailsDTO = ComplianceAPIUtil.getArtifactComplianceDetailsDTO
                (apiId, artifactType, username, organization);

        log.info("Successfully retrieved compliance details for API: " + apiId);
        return Response.ok().entity(detailsDTO).build();
    }

    /**
     * This method retrieves compliance status for a list of APIs.
     *
     * @param limit          The maximum size of the resource array to return.
     * @param offset         The starting point within the complete list of items qualified.
     * @param messageContext The message context.
     * @return The compliance status for the list of APIs.
     */
    public Response getComplianceStatusListOfAPIs(Integer limit, Integer offset, MessageContext messageContext)
            throws APIMGovernanceException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        ArtifactType artifactType = ArtifactType.API;
        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving compliance status list for APIs with limit: " + limit + ", offset: " + offset + 
                    " in organization: " + organization);
        }

        ArtifactComplianceListDTO complianceListDTO = ComplianceAPIUtil
                .getArtifactComplianceListDTO(artifactType, username, organization, limit, offset);

        log.info("Successfully retrieved compliance status list for APIs");
        return Response.ok().entity(complianceListDTO).build();
    }

    /**
     * Get organizational artifact compliance summary for APIs
     *
     * @param messageContext message context
     * @return Response
     * @throws APIMGovernanceException if an error occurs while getting the artifact compliance summary
     */
    public Response getComplianceSummaryForAPIs(MessageContext messageContext) throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();
        ArtifactType artifactType = ArtifactType.API;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving compliance summary for APIs in organization: " + organization);
        }

        ArtifactComplianceSummaryDTO summaryDTO = ComplianceAPIUtil.getArtifactComplianceSummary(artifactType,
                username, organization);

        log.info("Successfully retrieved compliance summary for APIs");
        return Response.ok().entity(summaryDTO).build();
    }

    /**
     * Get ruleset validation results by Artifact Reference Id
     *
     * @param apiId          API ID
     * @param rulesetId      ruleset ID
     * @param messageContext message context
     * @return Response
     * @throws APIMGovernanceException if an error occurs while getting the ruleset validation results
     */
    public Response getRulesetValidationResultsByAPIId(String apiId, String rulesetId, MessageContext messageContext)
            throws APIMGovernanceException {

        String organization = APIMGovernanceAPIUtil.getValidatedOrganization(messageContext);
        String username = APIMGovernanceAPIUtil.getLoggedInUsername();
        ArtifactType artifactType = ArtifactType.API;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving ruleset validation results for API: " + apiId + " and ruleset: " + rulesetId + 
                    " in organization: " + organization);
        }

        RulesetValidationResultDTO rulesetValidationResultDTO = ComplianceAPIUtil
                .getRulesetValidationResultDTO(apiId, artifactType, rulesetId, username, organization);

        log.info("Successfully retrieved ruleset validation results for API: " + apiId + " and ruleset: " + rulesetId);
        return Response.ok().entity(rulesetValidationResultDTO).build();
    }
}
