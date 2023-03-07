/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.api.util;

import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.model.ThrottlingLimit;

/**
 * utility methods shared across the classes inside the model package.
 */
public class ModelUtil {

    /**
     * To maintain backward compatibility, there is a requirement to convert throttlingLimit to throttlingPolicy.
     *
     * @param throttlingLimit {@link ThrottlingLimit} object
     * @return API Throttle Policy ex: 10KPerMin, 100KPerHour etc
     */
    public static String generateThrottlePolicyFromThrottleLimit(ThrottlingLimit throttlingLimit) {
        String requestCount;
        String shortenerSuffix = "";
        if (throttlingLimit.getRequestCount() == -1)
            return APIConstants.UNLIMITED_TIER;
        if (throttlingLimit.getRequestCount() % 100000 == 0) {
            requestCount = Integer.toString(throttlingLimit.getRequestCount() / 1000000);
            shortenerSuffix = "M";
        } else if (throttlingLimit.getRequestCount() % 1000 == 0) {
            requestCount = Integer.toString(throttlingLimit.getRequestCount() / 1000);
            shortenerSuffix = "K";
        } else {
            requestCount = Integer.toString(throttlingLimit.getRequestCount());
        }
        // To make it compatible with the previously existing throttling policy
        StringBuilder sb = new StringBuilder();
        if ("MINUTE".equalsIgnoreCase(throttlingLimit.getUnit())) {
            sb.append(requestCount).append(shortenerSuffix).append("PerMin");
        } else {
            sb.append(requestCount).append(shortenerSuffix).append("Per").append(throttlingLimit.getUnit());
        }
        return sb.toString();
    }

    /**
     * Existing APIs contains the throttling tier value as a string. This method assigns the throttling limit in
     * SwaggerData object's throttleLimit field considering the throttling tier string value.
     *
     * @param throttlingTierStr String value relevant to the throttling tier
     * @return ThrottleLimit object from throttling tier String value
     */
    public static ThrottlingLimit generateThrottlingLimitFromThrottlingTier(String throttlingTierStr) {
        ThrottlingLimit throttlingLimit = new ThrottlingLimit();
        throttlingLimit.setUnit("MINUTE");
        switch (throttlingTierStr) {
            case "10KPerMin":
                throttlingLimit.setRequestCount(10000);
                break;
            case "20KPerMin":
                throttlingLimit.setRequestCount(20000);
                break;
            case "50KPerMin":
                throttlingLimit.setRequestCount(50000);
                break;
            case "Unlimited":
            default:
                throttlingLimit.setRequestCount(-1);
                break;
        }
        return throttlingLimit;
    }
}
