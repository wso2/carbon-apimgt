/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.store.v1.WebhooksApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.WebhookServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;


public class WebhooksApiServiceImpl implements WebhooksApiService {

    private static final Log log = LogFactory.getLog(WebhooksApiServiceImpl.class);

    public Response webhooksSubscriptionsGet(String applicationId, String apiId, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        if (StringUtils.isNotEmpty(applicationId)) {
            WebhookSubscriptionListDTO webhookSubscriptionListDTO = WebhookServiceImpl.getWebhooksSubscriptions(
                    applicationId, apiId);
            return Response.ok().entity(webhookSubscriptionListDTO).build();
        } else {
            throw new APIManagementException("Application Id cannot be empty",
                    ExceptionCodes.from(ExceptionCodes.CERT_BAD_REQUEST, "Application Id cannot be empty"));
        }
    }
}
