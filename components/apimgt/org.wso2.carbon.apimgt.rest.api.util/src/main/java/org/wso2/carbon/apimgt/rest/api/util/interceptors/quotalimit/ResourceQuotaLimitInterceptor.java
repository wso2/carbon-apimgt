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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.quotalimit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.quotalimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles API creations considering specified quotas.
 */
public class ResourceQuotaLimitInterceptor extends AbstractPhaseInterceptor {

    private static final String PATH_TO_MATCH_SLASH = "path_to_match_slash";
    private static final String ORGANIZATION = "organization";
    private static final Log log = LogFactory.getLog(ResourceQuotaLimiter.class);

    public ResourceQuotaLimitInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    /**
     * Limits to create APIs.
     *
     * @param message cxf message
     */
    @Override
    public void handleMessage(Message message) {
        log.debug("Handling the request from quota limit interceptor");
        if (getQuotaLimitEnabled()) {
            try {
                String pathToMatch = (String) message.get(PATH_TO_MATCH_SLASH);
                String orgID = (String) message.get(ORGANIZATION);
                String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
                List payloadData = message.getContent(List.class);
                Map<String, Object> properties = new HashMap<>();
                properties.put("requestPayload", payloadData);
                ResourceQuotaLimiter quotaLimiter = APIUtil.getResourceQuotaLimiter();
                boolean isQuotaLimited = quotaLimiter.getQuotaLimitStatus(orgID, httpMethod, pathToMatch, properties);
                if (log.isDebugEnabled()) {
                    log.debug("Quota limit status:" + isQuotaLimited + " returned by limiter "
                            + quotaLimiter.getClass().getName());
                }
                if (isQuotaLimited) {
                    Response response = Response.status(Response.Status.TOO_MANY_REQUESTS).build();
                    message.getExchange().put(Response.class, response);
                }
            } catch (APIManagementException e) {
                log.error("Error occurred while getting response from ResourceQuotaLimiter. " + e.getMessage());
            }
        }
    }

    private boolean getQuotaLimitEnabled() {
        APIManagerConfiguration configurations = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        return Boolean.parseBoolean(configurations.getFirstProperty(APIConstants.API_QUOTA_LIMIT_ENABLE));
    }
}
