/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.rateLimit;

import org.apache.cxf.common.util.UrlUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.util.exception.RateLimitExceedException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Map;

/**
 * This class will make sure the number of APIs can be created is limited to a preconfigure number.
 * If the total API count exceeds that number 403 will be returned
 */
public class ApiRateLimitInterceptor extends AbstractPhaseInterceptor {
    public ApiRateLimitInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean rateLimitEnabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_RATE_LIMIT_ENABLE));
        int apiLimit = Integer.parseInt(config.getFirstProperty(APIConstants.API_RATE_LIMIT_API_LIMIT));

        if (rateLimitEnabled && getToBeRateLimited(message)) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(RateLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(RateLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID)) {
                String organizationId = queryParamsMap.get(RateLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID);
                int apiCount = RestApiUtil.getRegularAPICount(organizationId);
                if (apiCount >= apiLimit) {
                    throw new RateLimitExceedException();
                }
            }
        }

        return;
    }

    private boolean getToBeRateLimited(Message message) {
        String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String matchingPath = (String) message.get(RateLimitInterceptorConstants.PATH_TO_MATCH_SLASH);

        boolean isCreateAPI = RateLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                RateLimitInterceptorConstants.API_FROM_SCRATCH_PATH.equals(matchingPath);
        boolean isCreateVersion = RateLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                RateLimitInterceptorConstants.NEW_API_VERSION_PATH.equals(matchingPath);
        boolean isImportAPI = RateLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                RateLimitInterceptorConstants.IMPORT_OPENAPI_PATH.equals(matchingPath);
        boolean isRegularType = false;

        if (isImportAPI) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(RateLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(RateLimitInterceptorConstants.QUERY_PARAM_API_TYPE)) {
                String type = queryParamsMap.get(RateLimitInterceptorConstants.QUERY_PARAM_API_TYPE);
                isRegularType = RateLimitInterceptorConstants.API_TYPE_REGULAR.equals(type);
            }
        }

        return isCreateAPI || isCreateVersion || (isImportAPI && isRegularType);
    }
}
