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

package org.wso2.carbon.apimgt.gateway.handlers.streaming;

import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Include common functionality for websub and sse protocol.
 */
public class StreamingApiHanlder extends AbstractHandler {

    @Override
    public boolean handleRequest(MessageContext synCtx) {

        Object apiType = synCtx.getProperty(APIConstants.API_TYPE);
        if (APIConstants.API_TYPE_SSE.equals(apiType) || APIConstants.API_TYPE_WEBSUB.equals(apiType)) {
            ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    setProperty(Constants.Configuration.HTTP_METHOD, synCtx.getProperty(APIConstants.HTTP_VERB));
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}
