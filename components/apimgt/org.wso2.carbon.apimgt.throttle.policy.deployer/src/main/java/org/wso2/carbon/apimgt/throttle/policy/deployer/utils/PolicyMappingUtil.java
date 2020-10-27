/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttle.policy.deployer.utils;

import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.HeaderCondition;
import org.wso2.carbon.apimgt.api.model.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.api.model.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;

/**
 * An Utility class for policy mapping operations.
 */
public class PolicyMappingUtil {

    /**
     * Map a org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Condition to a
     * org.wso2.carbon.apimgt.api.model.policy.Condition
     *
     * @param conditionDTO org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Condition
     * @return org.wso2.carbon.apimgt.api.model.policy.Condition object
     */
    public static Condition mapCondition(
            org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Condition conditionDTO) {

        switch (conditionDTO.getConditionType()) {
            case PolicyConstants.IP_RANGE_TYPE:
                IPCondition ipRangeCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
                ipRangeCondition.setInvertCondition(conditionDTO.isInverted());
                ipRangeCondition.setStartingIP(conditionDTO.getName());
                ipRangeCondition.setEndingIP(conditionDTO.getValue());
                return ipRangeCondition;
            case PolicyConstants.IP_SPECIFIC_TYPE:
                IPCondition ipSpecificCondition = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
                ipSpecificCondition.setInvertCondition(conditionDTO.isInverted());
                ipSpecificCondition.setSpecificIP(conditionDTO.getValue());
                return ipSpecificCondition;
            case PolicyConstants.HEADER_TYPE:
                HeaderCondition headerCondition = new HeaderCondition();
                headerCondition.setInvertCondition(conditionDTO.isInverted());
                headerCondition.setHeader(conditionDTO.getName());
                headerCondition.setValue(conditionDTO.getValue());
                return headerCondition;
            case PolicyConstants.JWT_CLAIMS_TYPE:
                JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
                jwtClaimsCondition.setInvertCondition(conditionDTO.isInverted());
                jwtClaimsCondition.setAttribute(conditionDTO.getName());
                jwtClaimsCondition.setClaimUrl(conditionDTO.getValue());
                return jwtClaimsCondition;
            case PolicyConstants.QUERY_PARAMETER_TYPE:
                QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
                queryParameterCondition.setInvertCondition(conditionDTO.isInverted());
                queryParameterCondition.setParameter(conditionDTO.getName());
                queryParameterCondition.setValue(conditionDTO.getValue());
                return queryParameterCondition;
            default:
                return null;
        }
    }
}
