package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.governance.api.ValidationEngine;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.impl.service.APIMGovernanceServiceImpl;
import org.wso2.carbon.apimgt.governance.impl.util.AuditLogger;
import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.governance.api.model.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import java.util.List;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PolicyEvaluationApiServiceImpl implements PolicyEvaluationApiService {

    private static final Log log = LogFactory.getLog(PolicyEvaluationApiServiceImpl.class);

    public Response getPolicyEvaluationByAPI(String artifactType, String ruleCategory, String ruleType,
                                             InputStream fileInputStream, Attachment fileDetail,
                                             String label, MessageContext messageContext) {

        ValidationEngine validationEngine = org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder.getInstance()
                .getValidationEngineService().getValidationEngine();

        String apiSchemaCode = null;

        try {
            String fileName = fileDetail.getContentDisposition().getParameter("filename");

            if (fileName != null && fileName.endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(fileInputStream)) {
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
                apiSchemaCode = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unsupported file type. Only .zip or .txt are accepted.").build();
            }

            ExtendedArtifactType extendedArtifactType;
            try {
                extendedArtifactType = ExtendedArtifactType.valueOf(artifactType);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid artifactType: " + artifactType).build();
            }

            RuleType parsedRuleType = RuleType.valueOf(ruleType);
            RuleCategory parsedRuleCategory = RuleCategory.valueOf(ruleCategory);

            APIMGovernanceService governanceService = new APIMGovernanceServiceImpl();
            List<Ruleset> rulesets = governanceService.getApplicableRulesetsByExtendedArtifactType(
                    extendedArtifactType,
                    parsedRuleType,
                    parsedRuleCategory,
                    RestApiCommonUtil.getLoggedInUserTenantDomain()
            );

            ArtifactComplianceInfo artifactComplianceInfo = new ArtifactComplianceInfo();
            List<RuleViolation> allRuleViolations = new ArrayList<>();

            for (Ruleset ruleset : rulesets) {
                List<RuleViolation> ruleViolations = validationEngine.validate(apiSchemaCode, ruleset);
                allRuleViolations.addAll(ruleViolations);
            }

            artifactComplianceInfo.addBlockingViolations(allRuleViolations);

            List<Map<String, String>> simplifiedViolations = artifactComplianceInfo.getBlockingRuleViolations().stream()
                    .map(violation -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("violatedPath", violation.getViolatedPath());
                        map.put("ruleMessage", violation.getRuleMessage());
                        map.put("severity", String.valueOf(violation.getSeverity()));
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("violatedRules", simplifiedViolations);

            return Response.ok(responseMap).build();

        } catch (IOException e) {
            log.error("Error processing uploaded file", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing the uploaded file.").build();

        }
        catch (APIMGovernanceException e) {
            log.error("Error retrieving governance rulesets", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving governance rulesets.").build();

        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred: " + e.getMessage()).build();
        }
    }
}