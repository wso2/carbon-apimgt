/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OperationPoliciesApiServiceImplUtils {

    private OperationPoliciesApiServiceImplUtils() {
    }

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
    public static void preparePolicyDefinition(OperationPolicyData policyData,
                                               OperationPolicyDefinition policyDefinition, String definition,
                                               OperationPolicyDefinition.GatewayType gatewayType)
            throws APIManagementException {
        policyDefinition.setContent(definition);
        policyDefinition.setGatewayType(gatewayType);
        policyDefinition.setMd5Hash(APIUtil.getHashOfOperationPolicyDefinition(policyDefinition));

        if (OperationPolicyDefinition.GatewayType.Synapse.equals(gatewayType)) {
            policyData.setSynapsePolicyDefinition(policyDefinition);
        } else if (OperationPolicyDefinition.GatewayType.ChoreoConnect.equals(gatewayType)) {
            policyData.setCcPolicyDefinition(policyDefinition);
        }

        policyData.setMd5Hash(APIUtil.getHashOfOperationPolicy(policyData));
    }

    /**
     * Get query parameters from the string
     *
     * @param query Request query
     * @return Map of query params
     */
    public static Map<String, String> getQueryParams(String query) {
        if (query == null || StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, String> queryParamMap = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                queryParamMap.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParamMap;
    }

}
