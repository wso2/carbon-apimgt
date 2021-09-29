package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        operationPolicy
                .setPolicyType(OperationPolicy.PolicyType.valueOf(operationPolicyDTO.getPolicyType().toString()));
        operationPolicy.setParameters(operationPolicyDTO.getParameters());
        return  operationPolicy;
    }

    public static OperationPolicyDTO fromOperationPolicyToDTO(OperationPolicy operationPolicy) {
        OperationPolicyDTO dto = new OperationPolicyDTO();
        dto.setPolicyType(OperationPolicyDTO.PolicyTypeEnum.fromValue(operationPolicy.getPolicyType().toString()));
        dto.setParameters(operationPolicy.getParameters());
        return dto;
    }

    public static APIOperationPoliciesDTO fromOperationPolicyListToDTO(List<OperationPolicy> operationPolicyList) {
        APIOperationPoliciesDTO dto = new APIOperationPoliciesDTO();
        List<OperationPolicyDTO> in = new ArrayList<>();
        List<OperationPolicyDTO> out = new ArrayList<>();

        for (OperationPolicy op : operationPolicyList) {
            OperationPolicyDTO policyDTO = fromOperationPolicyToDTO(op);
            if (APIConstants.OPERATION_SEQUENCE_TYPE_IN.equals(op.getDirection())) {
                in.add(policyDTO);
            } else if (APIConstants.OPERATION_SEQUENCE_TYPE_OUT.equals(op.getDirection())) {
                out.add(policyDTO);
            }
        }
        dto.setIn(in);
        dto.setOut(out);

        return dto;
    }

    public static List<OperationPolicy> fromDTOToAPIOpeationPoliciesList(
            APIOperationPoliciesDTO apiOperationPoliciesDTO) {
        List<OperationPolicy> operationPoliciesList = new ArrayList<>();

        List<OperationPolicyDTO> in = apiOperationPoliciesDTO.getIn();
        List<OperationPolicyDTO> out = apiOperationPoliciesDTO.getOut();
        for (OperationPolicyDTO op : in) {
            OperationPolicy operationPolicy =  fromDTOToOperationPolicy(op);
            operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_IN);
            operationPoliciesList.add(operationPolicy);
        }

        for (OperationPolicyDTO op : out) {
            OperationPolicy operationPolicy =  fromDTOToOperationPolicy(op);
            operationPolicy.setDirection(APIConstants.OPERATION_SEQUENCE_TYPE_OUT);
            operationPoliciesList.add(operationPolicy);
        }
        return operationPoliciesList;
    }

    public static void setOperationPoliciesToOperationsDTO(String apiId, List<APIOperationsDTO> apiOperationsDTO)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Set<URITemplate> uriTemplatesWithOperationPolicies = apiProvider.getURITemplatesWithOperationPolicies(apiId);
        Map<String, List<OperationPolicy>> operationPoliciesPerURITemplate = new HashMap<>();
        if (!uriTemplatesWithOperationPolicies.isEmpty()) {
            for (URITemplate uriTemplate : uriTemplatesWithOperationPolicies) {
                String uriTemplateKey = uriTemplate.getHTTPVerb() + ":" + uriTemplate.getUriTemplate();
                operationPoliciesPerURITemplate.put(uriTemplateKey, uriTemplate.getOperationPolicies());
            }
        }

        for (APIOperationsDTO operationsDTO : apiOperationsDTO) {
            String uriTemplateKey = operationsDTO.getVerb() + ":" + operationsDTO.getTarget();
            if (operationPoliciesPerURITemplate.containsKey(uriTemplateKey)) {
                List<OperationPolicy> operationPolicies = operationPoliciesPerURITemplate.get(uriTemplateKey);
                operationsDTO.setOperationPolicies(fromOperationPolicyListToDTO(operationPolicies));
            }
        }
    }
}
