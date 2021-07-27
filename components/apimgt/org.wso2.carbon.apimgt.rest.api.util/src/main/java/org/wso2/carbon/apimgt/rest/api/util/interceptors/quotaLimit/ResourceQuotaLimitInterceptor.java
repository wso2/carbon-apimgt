/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.quotaLimit;

import org.apache.cxf.common.util.UrlUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.quotaLimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ResourceQuotaLimitInterceptor extends AbstractPhaseInterceptor {

    public ResourceQuotaLimitInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    /**
     * Limits to create APIs if below conditions are satisfied.
     * > ResourceQuotaLimit enabled
     * > Return value as "true" from the getToBeQuotaLimited method.
     *
     * @param message cxf message
     */
    @Override
    public void handleMessage(Message message) {
        if (getQuotaLimitEnabled() && getToBeQuotaLimited(message)) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(QuotaLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(QuotaLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID)) {
                String organizationId = queryParamsMap.get(QuotaLimitInterceptorConstants.QUERY_PARAM_ORGANIZATION_ID);
                String userId = QuotaLimitInterceptorConstants.QUOTA_LIMIT_USERID;
                String resourceType = QuotaLimitInterceptorConstants.QUOTA_LIMIT_RESOURCE_TYPE;
                try {
                    ResourceQuotaLimiter quotaLimiter = APIUtil.getResourceQuotaLimiter();
                    boolean extensionReturnedValue = quotaLimiter.getAPIRateLimitStatus(organizationId, userId, resourceType);
                    if (quotaLimiter.getAPIRateLimitStatus(organizationId, userId, resourceType)) {
                        Response response = Response.status(Response.Status.TOO_MANY_REQUESTS).build();
                        message.getExchange().put(Response.class, response);
                    }
                } catch (APIManagementException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
    }

    private boolean getQuotaLimitEnabled() {
        APIManagerConfiguration configurations = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        boolean isRateLimitEnabled = Boolean.parseBoolean(configurations.getFirstProperty(APIConstants.API_QUOTA_LIMIT_ENABLE));
        return isRateLimitEnabled;
    }

    private boolean getToBeQuotaLimited(Message message) {
        String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String matchingPath = (String) message.get(QuotaLimitInterceptorConstants.PATH_TO_MATCH_SLASH);

        boolean isCreateAPI = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.API_FROM_SCRATCH_PATH.equals(matchingPath);
        boolean isCreateVersion = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.NEW_API_VERSION_PATH.equals(matchingPath);
        boolean isImportAPI = QuotaLimitInterceptorConstants.HTTP_POST.equals(httpMethod) &&
                QuotaLimitInterceptorConstants.IMPORT_OPENAPI_PATH.equals(matchingPath);

        boolean isRegularType = false;
        if (isImportAPI) {
            Map<String, String> queryParamsMap = UrlUtils.parseQueryString(
                    (String) message.get(QuotaLimitInterceptorConstants.QUERY_PARAM_STRING));
            if (queryParamsMap.containsKey(QuotaLimitInterceptorConstants.QUERY_PARAM_API_TYPE)) {
                String type = queryParamsMap.get(QuotaLimitInterceptorConstants.QUERY_PARAM_API_TYPE);
                isRegularType = QuotaLimitInterceptorConstants.API_TYPE_REGULAR.equals(type);
            }
        }

        return isCreateAPI || isCreateVersion || (isImportAPI && isRegularType);
    }
}
