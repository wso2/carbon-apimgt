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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.DefaultStreamInterceptor;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.GenericRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.ByteBuffer;

import static org.wso2.carbon.apimgt.gateway.handlers.streaming.sse.SseApiConstants.SSE_THROTTLE_DTO;

/**
 * This is used for handling throttling, and analytics event publishing of sse apis (subset of streaming apis).
 */
public class SseStreamInterceptor extends DefaultStreamInterceptor {

    private static final Log log = LogFactory.getLog(SseStreamInterceptor.class);
    private GenericRequestDataCollector dataCollector = new GenericRequestDataCollector();

    @Override
    public boolean interceptTargetResponse(MessageContext axisCtx) {
        Object artifactType = axisCtx.getProperty(PassThroughConstants.SYNAPSE_ARTIFACT_TYPE);
        return APIConstants.API_TYPE_SSE.equals(artifactType);
    }

    @Override
    public boolean targetResponse(ByteBuffer buffer, MessageContext axis2Ctx) {
        int eventCount = getEventCount(buffer);
        return handleThrottlingAndAnalytics(eventCount, axis2Ctx);
    }

    private int getEventCount(ByteBuffer buffer) {
        int count = 0;
        // do process
        return count;
    }

    private boolean handleThrottlingAndAnalytics(int eventCount, MessageContext axi2Ctx) {

        boolean throttleResult = false;
        Object throttleObject = axi2Ctx.getProperty(SSE_THROTTLE_DTO);
        if (throttleObject != null) {

            ThrottleDTO throttleDTO = (ThrottleDTO) throttleObject;
            APIKeyValidationInfoDTO infoDTO = new APIKeyValidationInfoDTO();

            String tenantDomain = infoDTO.getSubscriberTenantDomain();
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                boolean isThrottled = WebsocketUtil.isThrottled(null, null, null);
                if (isThrottled) {
                    if (APIUtil.isAnalyticsEnabled()) {
                        //  dataCollector.collectData();
                    }
                    return false;
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            Object[] objects =
                    new Object[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                            null, null, null };
            org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(
                    "org.wso2.throttle.request.stream:1.0.0", System.currentTimeMillis(), null, null, objects);

            ThrottleDataPublisher throttleDataPublisher =
                    ServiceReferenceHolder.getInstance().getThrottleDataPublisher();
            if (throttleDataPublisher != null) {
                // todo need to publish per number of events
                throttleDataPublisher.getDataPublisher().tryPublish(event);
            } else {
                log.error("Cannot publish events to traffic manager because ThrottleDataPublisher "
                                  + "has not been initialised");
            }
            return true;
        } else {
            log.error("Throttle object cannot be null.");
        }
        return throttleResult;
    }
}
