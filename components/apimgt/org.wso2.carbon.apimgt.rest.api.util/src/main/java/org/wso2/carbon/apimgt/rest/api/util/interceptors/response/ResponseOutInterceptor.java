/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.ws.rs.core.MultivaluedMap;

public class ResponseOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Log log = LogFactory.getLog(ResponseOutInterceptor.class);
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    public ResponseOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        MultivaluedMap<String, Object> headers = (MetadataMap<String, Object>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new MetadataMap<>();
        }
        setOutBoundHeaders(message, headers);
    }

    private void setOutBoundHeaders(Message message, MultivaluedMap<String, Object> headers) {
        headers.add(X_CONTENT_TYPE_OPTIONS, "nosniff");
        message.put(Message.PROTOCOL_HEADERS, headers);
    }
}
