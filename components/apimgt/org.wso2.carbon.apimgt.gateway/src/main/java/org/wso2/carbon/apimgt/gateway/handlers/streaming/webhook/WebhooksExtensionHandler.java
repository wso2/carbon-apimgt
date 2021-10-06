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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.APIManagerExtensionHandler;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Handler used for webhooks apis to execute extensions in the mediation flow.
 * <pre>
 * {@code
 * <handler class="org.wso2.carbon.apimgt.gateway.handlers.streaming.webhook.WebhooksExtensionHandler">
 * </handler>
 * }
 * </pre>
 */
public class WebhooksExtensionHandler extends APIManagerExtensionHandler {

    private String eventReceiverResourcePath = APIConstants.WebHookProperties.DEFAULT_SUBSCRIPTION_RESOURCE_PATH;

    public boolean handleRequest(MessageContext synCtx) {

        String requestSubPath = getRequestSubPath(synCtx);
        if (requestSubPath.startsWith(eventReceiverResourcePath)) {
            return super.handleRequest(synCtx);
        }
        return true;
    }

    private String getRequestSubPath(MessageContext synCtx) {

        Object requestSubPath = synCtx.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
        if (requestSubPath != null) {
            return requestSubPath.toString();
        }
        return Utils.getSubRequestPath(Utils.getSelectedAPI(synCtx), synCtx);
    }
}
