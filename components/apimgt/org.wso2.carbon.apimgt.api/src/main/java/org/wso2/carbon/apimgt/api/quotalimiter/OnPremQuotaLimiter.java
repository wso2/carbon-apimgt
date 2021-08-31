/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.quotalimiter;

import java.util.Map;

/**
 * This class handles API creation if API Quota Limit Extension not available.
 */
public class OnPremQuotaLimiter implements ResourceQuotaLimiter {

    /**
     * @return Returns false since product-apim should allow for API creations since quota-limit extension not available there.
     */
    @Override
    public boolean getQuotaLimitStatus(String orgID, String httpMethod, String pathToMatch, Map<String, Object> payload) {
        return false;
    }
}
