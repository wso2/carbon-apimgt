package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;
import org.wso2.carbon.apimgt.governance.rest.api.util.APIMGovernanceAPIUtil;
import org.wso2.carbon.apimgt.governance.rest.api.util.ComplianceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import java.util.List;
import java.io.InputStream;


import org.wso2.carbon.apimgt.governance.api.model.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

        ArtifactComplianceDetailsDTO detailsDTO = ComplianceAPIUtil.getArtifactComplianceDetailsDTO
                (apiId, artifactType, username, organization);

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
        ArtifactComplianceListDTO complianceListDTO = ComplianceAPIUtil
                .getArtifactComplianceListDTO(artifactType, username, organization, limit, offset);

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

        ArtifactComplianceSummaryDTO summaryDTO = ComplianceAPIUtil.getArtifactComplianceSummary(artifactType,
                username, organization);

        return Response.ok().entity(summaryDTO).build();
    }

    /**
     * Get governance rule violation results for a specific API
     *
     * @param messageContext message context
     * @return Response
     * @throws RuntimeException if an error occurs while getting the rule violation results
     */
    public Response getRuleViolationResultsByAPI(String artifactType, List<String> governableStates,
                                                 InputStream apiSchemaInputStream, Attachment apiSchemaDetail,
                                                 String label, MessageContext messageContext) {
        String apiSchemaCode = null;

        try {
            // Get the file name from the attachment header to determine the type
            String fileName = apiSchemaDetail.getContentDisposition().getParameter("filename");

            if (fileName != null && fileName.endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(apiSchemaInputStream)) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().endsWith("swagger.yaml")) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.copy(zis, baos);
                            apiSchemaCode = baos.toString(StandardCharsets.UTF_8);
                            break;
                        }
                    }

                    if (apiSchemaCode == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("swagger.yaml not found in the uploaded zip file").build();
                    }
                }
            } else if (fileName != null && fileName.endsWith(".txt")) {
                apiSchemaCode = new String(apiSchemaInputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unsupported file type. Only .zip or .txt are accepted.").build();
            }

            // Construct the rule map
            Map<RuleType, String> apiDefinition = new HashMap<>();
            apiDefinition.put(RuleType.API_DEFINITION, apiSchemaCode);

            // Convert artifactType string to ExtendedArtifactType enum
            ExtendedArtifactType artifactEnum;
            try {
                artifactEnum = ExtendedArtifactType.valueOf(artifactType);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid artifactType: " + artifactType).build();
            }

            // Convert governableStates strings to APIMGovernableState enums
            List<APIMGovernableState> stateEnums = new ArrayList<>();
            if (governableStates != null) {
                for (String state : governableStates) {
                    try {
                        stateEnums.add(APIMGovernableState.valueOf(state));
                    } catch (IllegalArgumentException e) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("Invalid governable state: " + state).build();
                    }
                }
            }

            // If multiple states are provided, handle appropriately (only using the first for now)
            APIMGovernableState selectedState = stateEnums.isEmpty() ? APIMGovernableState.API_CREATE : stateEnums.get(0);

            ArtifactComplianceInfo complianceResult = PublisherCommonUtils.checkGovernanceComplianceGenAI(
                    selectedState,
                    artifactEnum,
                    RestApiCommonUtil.getLoggedInUserTenantDomain(),
                    apiDefinition
            );

            List<RuleViolation> violatedRulesOnly = complianceResult.getBlockingRuleViolations();
            List<Map<String, String>> simplifiedViolations = new ArrayList<>();

            for (RuleViolation violation : violatedRulesOnly) {
                Map<String, String> violationMap = new HashMap<>();
                violationMap.put("violatedPath", violation.getViolatedPath());
                violationMap.put("ruleMessage", violation.getRuleMessage());
                simplifiedViolations.add(violationMap);
            }

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("violatedRules", simplifiedViolations);

            return Response.ok(responseMap).build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing the uploaded file: " + e.getMessage()).build();
        } catch (APIManagementException e) {
            throw new RuntimeException(e);
        }
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

        RulesetValidationResultDTO rulesetValidationResultDTO = ComplianceAPIUtil
                .getRulesetValidationResultDTO(apiId, artifactType, rulesetId, username, organization);

        return Response.ok().entity(rulesetValidationResultDTO).build();
    }
}
