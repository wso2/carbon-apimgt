/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.notifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;

import java.util.Set;

/**
 * Notifier for API deploy/undeploy events targeting API Platform (Envoy) gateways.
 * Registered under {@link APIConstants.NotifierType#GATEWAY_PUBLISHED_API}; acts only when the event
 * has non-empty {@link DeployAPIInGatewayEvent#getPlatformGatewayIds()}. Delegates to
 * {@link PlatformGatewayDeploymentDispatcher} when set (e.g. by internal service or bridge); otherwise no-ops.
 */
public class PlatformGatewayDeployNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(PlatformGatewayDeployNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (!(event instanceof DeployAPIInGatewayEvent)) {
            return true;
        }
        DeployAPIInGatewayEvent deployEvent = (DeployAPIInGatewayEvent) event;
        Set<String> platformGatewayIds = deployEvent.getPlatformGatewayIds();
        if (platformGatewayIds == null || platformGatewayIds.isEmpty()) {
            return true;
        }
        PlatformGatewayDeploymentDispatcher dispatcher =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentDispatcher();
        if (dispatcher == null) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deploy notifier: no dispatcher set; skipping event for API "
                        + deployEvent.getUuid() + " and " + platformGatewayIds.size() + " platform gateway(s)");
            }
            return true;
        }
        try {
            if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(event.getType())) {
                dispatcher.dispatchDeploy(deployEvent, platformGatewayIds);
            } else if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(event.getType())) {
                if (deployEvent.isDeleted()) {
                    dispatcher.dispatchDelete(deployEvent, platformGatewayIds);
                } else {
                    dispatcher.dispatchUndeploy(deployEvent, platformGatewayIds);
                }
            }
        } catch (Exception e) {
            log.warn("Platform gateway deploy notifier failed to dispatch event for API " + deployEvent.getUuid()
                    + ": " + e.getMessage(), e);
            throw new NotifierException(e);
        }
        return true;
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name();
    }
}
