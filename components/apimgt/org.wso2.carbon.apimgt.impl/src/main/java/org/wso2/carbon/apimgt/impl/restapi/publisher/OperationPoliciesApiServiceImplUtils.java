package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashMap;
import java.util.Map;

public class OperationPoliciesApiServiceImplUtils {

    /**
     * @param policySpecification Operation policy spec
     * @param organization        Validated organization
     * @return OperationPolicyData object
     */
    public static OperationPolicyData prepareOperationPolicyData(OperationPolicySpecification policySpecification,
                                                                 String organization) {
        OperationPolicyData operationPolicyData = new OperationPolicyData();
        operationPolicyData.setOrganization(organization);
        operationPolicyData.setSpecification(policySpecification);

        return operationPolicyData;
    }

    /**
     * @param policySpecification Operation policy spec
     * @param organization        Validated organization
     * @param apiId               API UUID
     * @return OperationPolicyData object
     */
    public static OperationPolicyData prepareOperationPolicyData(OperationPolicySpecification policySpecification,
                                                                 String organization, String apiId) {
        OperationPolicyData operationPolicyData = new OperationPolicyData();
        operationPolicyData.setOrganization(organization);
        operationPolicyData.setApiUUID(apiId);
        operationPolicyData.setSpecification(policySpecification);

        return operationPolicyData;
    }

    /**
     * @param policyData       Operation policy data
     * @param policyDefinition Operation policy definition object
     * @param definition       Policy definition
     * @param gatewayType      Policy gateway type
     */
    public static void preparePolicyDefinition(
            OperationPolicyData policyData, OperationPolicyDefinition policyDefinition,
            String definition, OperationPolicyDefinition.GatewayType gatewayType) {
        policyDefinition.setContent(definition);
        policyDefinition.setGatewayType(gatewayType);
        policyDefinition.setMd5Hash(APIUtil.getMd5OfOperationPolicyDefinition(policyDefinition));

        if (OperationPolicyDefinition.GatewayType.Synapse.equals(gatewayType)) {
            policyData.setSynapsePolicyDefinition(policyDefinition);
        } else if (OperationPolicyDefinition.GatewayType.ChoreoConnect.equals(gatewayType)) {
            policyData.setCcPolicyDefinition(policyDefinition);
        }

        policyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(policyData));
    }

    /**
     * @param query Request query
     * @return Map of query params
     */
    public static Map<String, String> getQueryParams(String query) {
        Map<String, String> queryParamMap = new HashMap<String, String>();
        String[] queryParams = query.split(" ");
        for (String param : queryParams) {
            String[] keyVal = param.split(":");
            if (keyVal.length == 2) {
                queryParamMap.put(keyVal[0], keyVal[1]);
            }
        }

        return queryParamMap;
    }

}
