package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDTO;

import java.util.ArrayList;
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
        operationPolicy.setTemplateName(operationPolicyDTO.getTemplateName());
        operationPolicy.setOrder((operationPolicyDTO.getOrder()) != null ? operationPolicyDTO.getOrder() : 1);
        operationPolicy.setParameters(operationPolicyDTO.getParameters());
        return  operationPolicy;
    }

    public static OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {
        OperationPolicyDTO dto = new OperationPolicyDTO();
        dto.setPolicyName(operationPolicy.getPolicyName());
        dto.setTemplateName(operationPolicy.getTemplateName());
        dto.setOrder(operationPolicy.getOrder());
        dto.setParameters(operationPolicy.getParameters());
        return dto;
    }

    public static APIOperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {
        APIOperationPoliciesDTO dto = new APIOperationPoliciesDTO();
        List<OperationPolicyDTO> in = new ArrayList<>();
        List<OperationPolicyDTO> out = new ArrayList<>();
        List<OperationPolicyDTO> fault = new ArrayList<>();

        for (OperationPolicy op : operationPolicyList) {
            OperationPolicyDTO policyDTO = fromOperationPolicyToDTO(op);
            if (APIConstants.OPERATION_SEQUENCE_TYPE_IN.equals(op.getDirection())) {
                in.add(policyDTO);
            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_OUT.equals(op.getDirection())) {
                out.add(policyDTO);
            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_FAULT.equals(op.getDirection())) {
                fault.add(policyDTO);
            }
        }
        dto.setIn(in);
        dto.setOut(out);
        dto.setFault(fault);
        return dto;
    }

    public static List<OperationPolicy> fromDTOToAPIOperationPoliciesList(
            APIOperationPoliciesDTO apiOperationPoliciesDTO) {
        List<OperationPolicy> operationPoliciesList = new ArrayList<>();

        if (apiOperationPoliciesDTO != null) {
            List<OperationPolicyDTO> in = apiOperationPoliciesDTO.getIn();
            List<OperationPolicyDTO> out = apiOperationPoliciesDTO.getOut();
            List<OperationPolicyDTO> fault = apiOperationPoliciesDTO.getFault();
            for (OperationPolicyDTO op : in) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_IN);
                operationPoliciesList.add(operationPolicy);
            }

            for (OperationPolicyDTO op : out) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_OUT);
                operationPoliciesList.add(operationPolicy);
            }

            for (OperationPolicyDTO op : fault) {
                OperationPolicy operationPolicy = fromDTOToOperationPolicy(op);
                operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_FAULT);
                operationPoliciesList.add(operationPolicy);
            }
        }
        return operationPoliciesList;
    }
}
