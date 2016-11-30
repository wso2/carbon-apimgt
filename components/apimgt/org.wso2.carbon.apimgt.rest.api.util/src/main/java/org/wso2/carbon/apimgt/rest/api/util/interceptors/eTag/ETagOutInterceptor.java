/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.eTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.ws.rs.core.MultivaluedMap;

import static org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil.checkETagSkipList;

public class ETagOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Log log = LogFactory.getLog(ETagOutInterceptor.class);
    private static final String ETAG = "ETag";

    public ETagOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (checkETagSkipList(message.getExchange().getInMessage().get(Message.PATH_INFO).toString(),
                message.getExchange().getInMessage().get(Message.HTTP_REQUEST_METHOD).toString())) {
            if (log.isDebugEnabled()){
                log.debug("Skipping ETagOutInterceptor for URI : " + message.getExchange().getInMessage().get(Message.PATH_INFO).toString());
            }
            return;

        }
        MultivaluedMap<String, Object> headers = (MetadataMap<String, Object>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new MetadataMap<>();
        }
        if (message.getExchange().containsKey(ETAG)) {
            String eTag = (String) message.getExchange().get(ETAG);
            setOutBoundHeaders(message, headers, eTag);
        }
    }

    private void setOutBoundHeaders(Message message, MultivaluedMap<String, Object> headers, String eTag) {
        headers.add(ETAG, "\"" + eTag + "\"");
        message.put(Message.PROTOCOL_HEADERS, headers);
    }
}
