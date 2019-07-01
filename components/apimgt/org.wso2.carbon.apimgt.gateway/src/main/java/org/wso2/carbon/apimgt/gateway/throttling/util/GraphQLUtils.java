/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.throttling.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.ThrottleConditionEvaluator;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.ThrottleHandler;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.metrics.manager.MetricManager;

import java.util.List;
import java.util.Map;

public class GraphQLUtils {

    protected ThrottleDataHolder getThrottleDataHolder() {
        return ServiceReferenceHolder.getInstance().getThrottleDataHolder();
    }

    protected ThrottleConditionEvaluator getThrottleConditionEvaluator() {
        return ThrottleConditionEvaluator.getInstance();
    }

    protected Timer getTimer(String name) {
        return MetricManager.timer(Level.INFO, name);
    }

    public boolean getThrottlingForGraphQLOperations(String operationLevelThrottleKey, ConditionGroupDTO[]
            conditionGroupDTOs, MessageContext synCtx, AuthenticationContext authContext) {

        boolean isThrottled = false;
        final String RESOURCE_THROTTLE = "RESOURCE_THROTTLE";
        final Log log = LogFactory.getLog(ThrottleHandler.class);

        //If tier is not unlimited only throttling will apply.
        Timer timer1 = getTimer(MetricManager.name( APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), RESOURCE_THROTTLE));
        Timer.Context
                context1 = timer1.start();

        if (conditionGroupDTOs != null && conditionGroupDTOs.length > 0) {
            // Checking Applicability of Conditions is a relatively expensive operation. So we are
            // going to check it only if the API/Operation is throttled out.
            if (getThrottleDataHolder().isAPIThrottled
                    (operationLevelThrottleKey)) {
                if (getThrottleDataHolder().isConditionsAvailable(operationLevelThrottleKey)) {
                    Map<String, List<ConditionDto>> conditionDtoMap = getThrottleDataHolder()
                            .getConditionDtoMap(operationLevelThrottleKey);
                    if (log.isDebugEnabled()) {
                        log.debug("conditions available" + conditionDtoMap.size());
                    }
                    String throttledCondition = getThrottleConditionEvaluator().getThrottledInCondition
                            (synCtx, authContext, conditionDtoMap);
                    if (StringUtils.isNotEmpty(throttledCondition)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Throttled with Condition :" + throttledCondition);
                        }
                        String combinedOperationLevelThrottleKey = operationLevelThrottleKey + "_" +
                                throttledCondition;

                        if (log.isDebugEnabled()) {
                            log.debug("Checking condition : " + combinedOperationLevelThrottleKey);
                        }

                        if (getThrottleDataHolder().
                                isThrottled(combinedOperationLevelThrottleKey)) {

                            isThrottled = true;
                            long timestamp = getThrottleDataHolder().
                                    getThrottleNextAccessTimestamp(combinedOperationLevelThrottleKey);
                            synCtx.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP,
                                    timestamp);
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Evaluating Conditional Groups");
                    }
                    //Then we will apply resource level throttling
                    List<ConditionGroupDTO> applicableConditions = getThrottleConditionEvaluator()
                            .getApplicableConditions(synCtx, authContext, conditionGroupDTOs);
                    for (ConditionGroupDTO conditionGroup : applicableConditions) {
                        String combinedOperationLevelThrottleKey = operationLevelThrottleKey + conditionGroup.getConditionGroupId();
                        if (log.isDebugEnabled()) {
                            log.debug("Checking condition : " + combinedOperationLevelThrottleKey);
                        }

                        if (getThrottleDataHolder().
                                isThrottled(combinedOperationLevelThrottleKey)) {
                            isThrottled = true;
                            long timestamp = getThrottleDataHolder().
                                    getThrottleNextAccessTimestamp(combinedOperationLevelThrottleKey);
                            synCtx.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                            break;
                        }
                    }
                }
            }

        } else {
            log.warn("Unable to find throttling information for resource and http verb. Throttling "
                    + "will not apply");
        }
        context1.stop();
        return isThrottled;
    }


}


