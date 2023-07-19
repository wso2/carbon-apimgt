/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecAttribute;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.OperationPolicyComparator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicySpecAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for mapping Operation Policy Objects into REST API Operation Policy related DTOs
 * and vice versa.
 */
public class OperationPolicyMappingUtil {

    public static List<OperationPolicy> fromDTOListToOperationPolicyList(
            List<OperationPolicyDTO> operationPolicyDTOList) {

        List<OperationPolicy> operationPolicyList = new ArrayList<>();
        for (OperationPolicyDTO operationPolicyDto : operationPolicyDTOList) {
            OperationPolicy operationPolicy = fromDTOToOperationPolicy(operationPolicyDto);
            operationPolicyList.add(operationPolicy);
        }
        return operationPolicyList;
    }

    public static OperationPolicy fromDTOToOperationPolicy(OperationPolicyDTO operationPolicyDTO) {

        OperationPolicy operationPolicy = new OperationPolicy();
        operationPolicy.setPolicyName(operationPolicyDTO.getPolicyName());
        operationPolicy.setPolicyVersion(operationPolicyDTO.getPolicyVersion());
        operationPolicy.setPolicyId(operationPolicyDTO.getPolicyId());
        operationPolicy.setParameters(operationPolicyDTO.getParameters());
        return operationPolicy;
    }

    public static OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {

        OperationPolicyDTO dto = new OperationPolicyDTO();
        dto.setPolicyName(operationPolicy.getPolicyName());
        dto.setPolicyVersion(operationPolicy.getPolicyVersion());
        dto.setPolicyId(operationPolicy.getPolicyId());
        dto.setParameters(operationPolicy.getParameters());
        return dto;
    }

    public static APIOperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {

        APIOperationPoliciesDTO dto = new APIOperationPoliciesDTO();
        List<OperationPolicyDTO> request = new ArrayList<>();
        List<OperationPolicyDTO> response = new ArrayList<>();
        List<OperationPolicyDTO> fault = new ArrayList<>();

        if (operationPolicyList != null) {
            Collections.sort(operationPolicyList, new OperationPolicyComparator());
            for (OperationPolicy op : operationPolicyList) {
                OperationPolicyDTO policyDTO = fromOperationPolicyToDTO(op);
                if (APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST.equals(op.getDirection())) {
                    request.add(policyDTO);
                } else if (APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE.equals(op.getDirection())) {
                    response.add(policyDTO);
                } else if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(op.getDirection())) {
                    fault.add(policyDTO);
                }
            }
        }

        dto.setRequest(request);
        dto.setResponse(response);
        dto.setFault(fault);
        return dto;
    }

    public static List<OperationPolicy> fromDTOToAPIOperationPoliciesList(
            APIOperationPoliciesDTO apiOperationPoliciesDTO) {

        List<OperationPolicy> operationPoliciesList = new ArrayList<>();

        if (apiOperationPoliciesDTO != null) {
            List<OperationPolicyDTO> request = apiOperationPoliciesDTO.getRequest();
            List<OperationPolicyDTO> response = apiOperationPoliciesDTO.getResponse();
            List<OperationPolicyDTO> fault = apiOperationPoliciesDTO.getFault();
            int requestCount = 1;
            int responseCount = 1;
            int faultCount = 1;
            for (OperationPolicyDTO op : request) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);
                operationPolicy.setOrder(requestCount);
                operationPoliciesList.add(operationPolicy);
                requestCount += 1;
            }

            for (OperationPolicyDTO op : response) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE);
                operationPolicy.setOrder(responseCount);
                operationPoliciesList.add(operationPolicy);
                responseCount += 1;
            }

            for (OperationPolicyDTO op : fault) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_FAULT);
                operationPolicy.setOrder(faultCount);
                operationPoliciesList.add(operationPolicy);
                faultCount += 1;
            }
        }
        return operationPoliciesList;
    }

    public static OperationPolicyDataListDTO fromOperationPolicyDataListToDTO(
            List<OperationPolicyData> policyDataList, int offset, int limit) {

        List<OperationPolicyDataDTO> operationPolicyList = new ArrayList<>();

        if (policyDataList == null) {
            policyDataList = new ArrayList<>();
        }

        int size = policyDataList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);
        for (int i = start; i <= end; i++) {
            operationPolicyList.add(fromOperationPolicyDataToDTO(policyDataList.get(i)));
        }

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        OperationPolicyDataListDTO dataListDTO = new OperationPolicyDataListDTO();
        dataListDTO.setList(operationPolicyList);
        dataListDTO.setCount(operationPolicyList.size());
        dataListDTO.setPagination(paginationDTO);

        return dataListDTO;
    }

    public static OperationPolicyDataDTO fromOperationPolicyDataToDTO(OperationPolicyData policyData) {

        OperationPolicyDataDTO policyDataDTO = new OperationPolicyDataDTO();
        OperationPolicySpecification policySpecification = policyData.getSpecification();
        policyDataDTO.setId(policyData.getPolicyId());
        policyDataDTO.setMd5(policyData.getMd5Hash());
        policyDataDTO.setIsAPISpecific(policyData.isApiSpecificPolicy());
        policyDataDTO.setName(policySpecification.getName());
        policyDataDTO.setVersion(policySpecification.getVersion());
        policyDataDTO.setDisplayName(policySpecification.getDisplayName());
        policyDataDTO.setDescription(policySpecification.getDescription());
        policyDataDTO.setSupportedGateways(policySpecification.getSupportedGateways());
        policyDataDTO.setSupportedApiTypes(policySpecification.getSupportedApiTypes());
        policyDataDTO.setApplicableFlows(policySpecification.getApplicableFlows());
        policyDataDTO.setCategory(policySpecification.getCategory().toString());

        if (policySpecification.getPolicyAttributes() != null) {
            List<OperationPolicySpecAttributeDTO> specAttributeDtoList = new ArrayList<>();
            for (OperationPolicySpecAttribute specAttribute : policySpecification.getPolicyAttributes()) {
                OperationPolicySpecAttributeDTO specAttributeDTO =
                        fromOperationPolicySpecAttributesToDTO(specAttribute);
                specAttributeDtoList.add(specAttributeDTO);
            }
            policyDataDTO.setPolicyAttributes(specAttributeDtoList);
        }
        return policyDataDTO;
    }

    public static OperationPolicySpecAttributeDTO fromOperationPolicySpecAttributesToDTO(
            OperationPolicySpecAttribute specAttribute) {

        OperationPolicySpecAttributeDTO specAttributeDTO = new OperationPolicySpecAttributeDTO();
        specAttributeDTO.setName(specAttribute.getName());
        specAttributeDTO.setDisplayName(specAttribute.getDisplayName());
        specAttributeDTO.setDescription(specAttribute.getDescription());
        specAttributeDTO.setType(specAttribute.getType().toString());
        specAttributeDTO.setValidationRegex(specAttribute.getValidationRegex());
        specAttributeDTO.setRequired(specAttribute.isRequired());
        specAttributeDTO.setDefaultValue(specAttribute.getDefaultValue());
        if (specAttribute.getType().equals(OperationPolicySpecAttribute.AttributeType.Enum)) {
            specAttributeDTO.setAllowedValues(specAttribute.getAllowedValues());
        }
        return specAttributeDTO;
    }
}
