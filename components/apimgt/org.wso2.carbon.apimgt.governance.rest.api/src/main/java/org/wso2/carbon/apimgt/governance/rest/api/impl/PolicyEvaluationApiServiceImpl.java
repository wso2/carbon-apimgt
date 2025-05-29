package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.impl.APIGovernanceHandler;
import org.wso2.carbon.apimgt.governance.impl.service.APIMGovernanceServiceImpl;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyEvaluationApiService;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import java.io.InputStream;
import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.governance.api.model.ExtendedArtifactType;
import org.wso2.carbon.apimgt.governance.api.model.RuleType;
import org.wso2.carbon.apimgt.governance.api.model.RuleCategory;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceInfo;
import org.wso2.carbon.apimgt.governance.api.model.Ruleset;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PolicyEvaluationApiServiceImpl implements PolicyEvaluationApiService {

    private static final Log log = LogFactory.getLog(PolicyEvaluationApiServiceImpl.class);

    public Response getPolicyEvaluationByAPI(String artifactType, String ruleCategory, String ruleType,
                                             InputStream fileInputStream, Attachment fileDetail,
                                             String label, MessageContext messageContext) throws APIMGovernanceException {

        ValidationEngine validationEngine = org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        ExtendedArtifactType extendedArtifactType;
        RuleType parsedRuleType;
        RuleCategory parsedRuleCategory;
        try {
            extendedArtifactType = ExtendedArtifactType.valueOf(artifactType);
            parsedRuleType = RuleType.valueOf(ruleType);
            parsedRuleCategory = RuleCategory.valueOf(ruleCategory);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid ruleType or ruleCategory: " + e.getMessage()).build();
        }

        Map<RuleType, String> apiProjectContentMap = new HashMap<>();
        String fileName = fileDetail.getContentDisposition().getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

        try {
            if (fileName != null && fileName.endsWith(APIConstants.ZIP_FILE_EXTENSION)) {
                byte[] zipBytes = fileInputStream.readAllBytes();
                APIGovernanceHandler governanceHandler = new APIGovernanceHandler();
                apiProjectContentMap = governanceHandler.extractArtifactProject(zipBytes);

            } else if (fileName != null && (fileName.endsWith(APIConstants.YAML_FILE_EXTENSION)
                    || fileName.endsWith(APIConstants.YML_FILE_EXTENSION)
                    || fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)
                    || fileName.endsWith(APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION)
                    || fileName.endsWith(".txt"))) {

                String apiProjectContent = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
                apiProjectContentMap.put(parsedRuleType, apiProjectContent);

            } else {
                APIUtil.handleException(
                        "Unsupported file is uploaded : file name : " + fileName);
            }

            APIMGovernanceService governanceService = new APIMGovernanceServiceImpl();
            ArtifactComplianceInfo artifactComplianceInfo = new ArtifactComplianceInfo();
            List<RuleViolation> ruleViolationsList = new ArrayList<>();

            for (Map.Entry<RuleType, String> entry : apiProjectContentMap.entrySet()) {
                RuleType currentRuleType = entry.getKey();
                String fileContent = entry.getValue();

                List<Ruleset> rulesets = governanceService.getApplicableRulesetsByExtendedArtifactType(
                        extendedArtifactType,
                        currentRuleType,
                        parsedRuleCategory,
                        RestApiCommonUtil.getLoggedInUserTenantDomain()
                );

                for (Ruleset ruleset : rulesets) {
                    List<RuleViolation> ruleViolations = validationEngine.validate(fileContent, ruleset);
                    ruleViolationsList.addAll(ruleViolations);
                }
            }

            artifactComplianceInfo.addNonBlockingViolations(ruleViolationsList);
            return Response.ok(artifactComplianceInfo).build();

        } catch (IOException e) {
            log.error("Error processing uploaded file: " + fileName, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing the uploaded file: " + e.getMessage()).build();
        } catch (APIManagementException e) {
            log.error("API Management error during policy evaluation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error occurred during policy evaluation").build();
        } catch (APIMGovernanceException e) {
            log.error("Governance service error", e);
            throw new APIMGovernanceException(APIMGovExceptionCodes.ERROR_WHILE_GETTING_RULE_VIOLATIONS);
        }
    }
}