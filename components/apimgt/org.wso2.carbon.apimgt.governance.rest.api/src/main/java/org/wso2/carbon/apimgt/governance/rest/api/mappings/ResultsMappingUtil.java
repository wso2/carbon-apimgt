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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.rest.api.mappings;

import org.wso2.carbon.apimgt.governance.api.GovernanceAPIConstants;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactComplianceState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactInfo;
import org.wso2.carbon.apimgt.governance.api.model.GovernancePolicy;
import org.wso2.carbon.apimgt.governance.api.model.Rule;
import org.wso2.carbon.apimgt.governance.api.model.RuleViolation;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceForPolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryForPolicyDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PaginationDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceStatusDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.util.GovernanceAPIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Results Mapping Utility
 */
public class ResultsMappingUtil {

    /**
     * Converts a RuleViolation object to a RuleValidationResultDTO object
     *
     * @param rule          Rule object
     * @param ruleViolation RuleViolation object
     * @return RuleValidationResultDTO object
     */
    public static RuleValidationResultDTO getRuleValidationResultDTO(Rule rule, RuleViolation ruleViolation) {
        RuleValidationResultDTO ruleValidationResultDTO = new RuleValidationResultDTO();
        ruleValidationResultDTO.setId(rule.getId());
        ruleValidationResultDTO.setName(rule.getCode());
        ruleValidationResultDTO.setDescription(rule.getDescription());
        if (ruleViolation != null) {
            ruleValidationResultDTO.setMessage(rule.getMessageOnFailure());
            ruleValidationResultDTO.setStatus(RuleValidationResultDTO.StatusEnum.FAILED);
            ruleValidationResultDTO.setSeverity(RuleValidationResultDTO.SeverityEnum.valueOf(
                    String.valueOf(rule.getSeverity())));
        } else {
            ruleValidationResultDTO.setStatus(RuleValidationResultDTO.StatusEnum.PASSED);
        }

        return ruleValidationResultDTO;
    }

    /**
     * Set pagination details for the artifact compliance list
     *
     * @param complianceListDTO ArtifactComplianceListDTO object
     * @param limit             max number of objects returned
     * @param offset            starting index
     * @param size              total number of objects
     * @param artifactType      artifact type
     */
    public static void setPaginationDetailsForArtifactCompliance(ArtifactComplianceListDTO complianceListDTO,
                                                                 int limit, int offset,
                                                                 int size, String artifactType) {
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = GovernanceAPIUtil.getArtifactCompliancePageURL(
                    GovernanceAPIConstants.ARTIFACT_COMPLIANCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), artifactType);
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = GovernanceAPIUtil.getArtifactCompliancePageURL(GovernanceAPIConstants.ARTIFACT_COMPLIANCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), artifactType);
        }
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);

        complianceListDTO.setPagination(paginationDTO);
    }


    /**
     * Set pagination details for the policy adherence list
     *
     * @param listDTO PolicyAdherenceListDTO object
     * @param limit   max number of objects returned
     * @param offset  starting index
     * @param size    total number of objects
     */
    public static void setPaginationDetailsForPolicyAdherence(PolicyAdherenceListDTO listDTO, int limit,
                                                              int offset, int size) {
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        // Set previous and next URLs for pagination
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = GovernanceAPIUtil.getPaginatedURL(
                    GovernanceAPIConstants.POLICY_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }
        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = GovernanceAPIUtil.getPaginatedURL(GovernanceAPIConstants.POLICY_ADHERENCE_GET_URL,
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);

        listDTO.setPagination(paginationDTO);
    }


    /**
     * Get the policy adherence summary
     *
     * @param totalPoliciesCount     total number of policies
     * @param evaluatedPoliciesCount total number of evaluated policies
     * @param violatedPoliciesCount  total number of violated policies
     * @return PolicyAdherenceSummaryDTO object
     */
    public static PolicyAdherenceSummaryDTO getPolicyAdherenceSummary(int totalPoliciesCount,
                                                                      int evaluatedPoliciesCount,
                                                                      int violatedPoliciesCount) {
        PolicyAdherenceSummaryDTO policyAdherenceSummaryDTO = new PolicyAdherenceSummaryDTO();
        policyAdherenceSummaryDTO.setTotalPolicies(totalPoliciesCount);
        policyAdherenceSummaryDTO.setViolatedPolicies(violatedPoliciesCount);
        policyAdherenceSummaryDTO.setFollowedPolicies(evaluatedPoliciesCount - violatedPoliciesCount);
        policyAdherenceSummaryDTO.setUnAppliedPolicies(totalPoliciesCount - evaluatedPoliciesCount);
        return policyAdherenceSummaryDTO;
    }

    /**
     * Get the artifact compliance summary
     *
     * @param totalArtifactsCount        total number of artifacts
     * @param compliantArtifactsCount    total number of compliant artifacts
     * @param nonCompliantArtifactsCount total number of non-compliant artifacts
     * @return ArtifactComplianceSummaryDTO object
     */
    public static ArtifactComplianceSummaryDTO getArtifactComplianceSummary(int totalArtifactsCount,
                                                                            int compliantArtifactsCount,
                                                                            int nonCompliantArtifactsCount) {
        ArtifactComplianceSummaryDTO artifactComplianceSummaryDTO = new ArtifactComplianceSummaryDTO();
        artifactComplianceSummaryDTO.setTotalArtifacts(totalArtifactsCount);
        artifactComplianceSummaryDTO.setCompliantArtifacts(compliantArtifactsCount);
        artifactComplianceSummaryDTO.setNonCompliantArtifacts(nonCompliantArtifactsCount);
        artifactComplianceSummaryDTO.setNotApplicableArtifacts(totalArtifactsCount - compliantArtifactsCount -
                nonCompliantArtifactsCount);
        return artifactComplianceSummaryDTO;
    }

    /**
     * Get the policy adherence details.
     *
     * @param policy             GovernancePolicy object
     * @param evaluatedArtifacts Artifacts evaluated against the policy separated by compliance state
     * @return PolicyAdherenceDetailsDTO object
     */
    public static PolicyAdherenceDetailsDTO getPolicyAdherenceDetailsDTO(GovernancePolicy policy,
                                                                         Map<ArtifactComplianceState,
                                                                                 List<ArtifactInfo>>
                                                                                 evaluatedArtifacts) {

        PolicyAdherenceDetailsDTO policyAdherenceDetailsDTO = new PolicyAdherenceDetailsDTO();
        policyAdherenceDetailsDTO.setPolicyName(policy.getName());
        policyAdherenceDetailsDTO.setPolicyId(policy.getId());
        List<ArtifactComplianceForPolicyDTO> artifactComplianceForPolicyDTOList = new ArrayList<>();


        // Set the status of the policy adherence
        boolean isCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.COMPLIANT).isEmpty();

        boolean isNonCompliantArtifactsExist = evaluatedArtifacts.containsKey(ArtifactComplianceState.NON_COMPLIANT) &&
                !evaluatedArtifacts.get(ArtifactComplianceState.NON_COMPLIANT).isEmpty();

        if (!isNonCompliantArtifactsExist && !isCompliantArtifactsExist) {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.UNAPPLIED);
        } else if (isNonCompliantArtifactsExist) {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.VIOLATED);
        } else {
            policyAdherenceDetailsDTO.setStatus(PolicyAdherenceDetailsDTO.StatusEnum.FOLLOWED);
        }

        // Set the compliance status of the artifacts attached to the policy
        for (Map.Entry<ArtifactComplianceState, List<ArtifactInfo>> entry : evaluatedArtifacts.entrySet()) {
            ArtifactComplianceState complianceState = entry.getKey();
            List<ArtifactInfo> artifactInfoList = entry.getValue();

            for (ArtifactInfo artifactInfo : artifactInfoList) {
                ArtifactComplianceForPolicyDTO artifactComplianceForPolicyDTO = new ArtifactComplianceForPolicyDTO();
                artifactComplianceForPolicyDTO.setArtifactId(artifactInfo.getArtifactId());
                artifactComplianceForPolicyDTO.setArtifactName(artifactInfo.getDisplayName());

                String artifactType = String.valueOf(artifactInfo.getArtifactType());
                artifactComplianceForPolicyDTO.setArtifactType(ArtifactComplianceForPolicyDTO
                        .ArtifactTypeEnum.valueOf(artifactType));

                artifactComplianceForPolicyDTO.setStatus(ArtifactComplianceForPolicyDTO
                        .StatusEnum.valueOf(String.valueOf(complianceState)));

                artifactComplianceForPolicyDTOList.add(artifactComplianceForPolicyDTO);
            }
        }

        policyAdherenceDetailsDTO.setEvaluatedArtifacts(artifactComplianceForPolicyDTOList);

        return policyAdherenceDetailsDTO;
    }

    /**
     * Get the policy adherence status
     *
     * @param policy                    GovernancePolicy object
     * @param compliantArtifactCount    total number of compliant artifacts
     * @param nonCompliantArtifactCount total number of non-compliant artifacts
     * @return PolicyAdherenceStatusDTO object
     */
    public static PolicyAdherenceStatusDTO getPolicyAdherenceStatusDTO(GovernancePolicy policy,
                                                                       int compliantArtifactCount,
                                                                       int nonCompliantArtifactCount) {
        ArtifactComplianceSummaryForPolicyDTO summaryDTO = new ArtifactComplianceSummaryForPolicyDTO();
        summaryDTO.setCompliantArtifacts(compliantArtifactCount);
        summaryDTO.setNonCompliantArtifacts(nonCompliantArtifactCount);

        PolicyAdherenceStatusDTO statusDTO = new PolicyAdherenceStatusDTO();
        statusDTO.setPolicyId(policy.getId());
        statusDTO.setPolicyName(policy.getName());
        statusDTO.setArtifactComplianceSummary(summaryDTO);
        statusDTO.setStatus(nonCompliantArtifactCount > 0 ? PolicyAdherenceStatusDTO.StatusEnum.VIOLATED :
                PolicyAdherenceStatusDTO.StatusEnum.FOLLOWED);

        return statusDTO;
    }

}
