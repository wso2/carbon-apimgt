/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.NHttpConnection;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.SourceContext;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.wso2.carbon.apimgt.gateway.utils.TransportHeaderUtil;

import java.util.ArrayList;
import java.util.List;

public class TransportHeaderHandler extends AbstractSynapseHandler {
    private static final Log log = LogFactory.getLog(TransportHeaderHandler.class);

    private boolean isRemoveRequestHeadersOnFaultEnabled = false;

    /** Well-known request headers to be removed in a response */
    private List<String> standardRequestHeaders = new ArrayList<String>();
    /** Well-known response headers to be removed in a request */
    private List<String> standardResponseHeaders = new ArrayList<String>();
    /** Request headers to be preserved in response if request has not reached backend */
    private List<String> preserveRequestHeaders = new ArrayList<String>();

    /**
     * Processes the request as it is.
     *
     * @param synCtx Synapse Message Context
     * @return true
     */
    public boolean handleRequestInFlow(MessageContext synCtx) {
        return true;
    }

    /**
     * Removes any standard well known response headers present in the request outgoing.
     *
     * @param synCtx Synapse Message Context
     * @return true
     */
    public boolean handleRequestOutFlow(MessageContext synCtx) {
	if (log.isDebugEnabled()) {
            log.debug("Starting to remove standard Response headers defined from the request out flow.");
	}
        //Remove all the standard well known response headers from the request outgoing
        TransportHeaderUtil.removeTransportHeadersFromList(synCtx, this.standardResponseHeaders);
        TransportHeaderUtil.removeExcessTransportHeadersFromList(synCtx, this.standardResponseHeaders);
	if (log.isDebugEnabled()) {
            log.debug("Removing headers completed in request out flow");
	}
        return true;
    }

    /**
     * Sets a flag in the Message Context to identify that the message has gone to Response Inflow
     *
     * @param synCtx Synapse Message Context
     * @return true
     */
    public boolean handleResponseInFlow(MessageContext synCtx) {
        synCtx.setProperty(TransportHeaderUtil.RESPONSE_INFLOW_INVOKED, Boolean.TRUE);
        return true;
    }

    /**
     * Removes standard well known request headers from the final response.
     * Removes all the headers present in the request if the request has not reached the backend.
     *
     * @param synCtx Synapse Message Context
     * @return true
     */
    public boolean handleResponseOutFlow(MessageContext synCtx) {
	if (log.isDebugEnabled()) {
            log.debug("Starting to remove standard Request headers defined from the response out flow.");
	}
        //Remove all the standard well known request headers from the final response
        TransportHeaderUtil.removeTransportHeadersFromList(synCtx, this.standardRequestHeaders);
        TransportHeaderUtil.removeExcessTransportHeadersFromList(synCtx, this.standardRequestHeaders);
	if (log.isDebugEnabled()) {
            log.debug("Removing headers completed in response out flow");
	}
        if (this.isRemoveRequestHeadersOnFaultEnabled) {
            //Remove all the headers present in the request if the request has not reached the backend.
            NHttpConnection sourceHttpConnection =
                    (NHttpConnection)((Axis2MessageContext)synCtx).getAxis2MessageContext().
                            getProperty(TransportHeaderUtil.PASSTHROUGH_SOURCE_CONNECTION);
            if (sourceHttpConnection != null) {
                SourceRequest sourceRequest = SourceContext.getRequest(sourceHttpConnection);
                if (TransportHeaderUtil.isRemovingRequestHeadersInResponseRequired(synCtx, sourceRequest)) {
                    TransportHeaderUtil.removeRequestHeadersFromResponseHeaders(sourceRequest.getHeaders(),
                                                    TransportHeaderUtil.getTransportHeaders(synCtx),
                                                    this.preserveRequestHeaders);
                    TransportHeaderUtil.removeRequestHeadersFromResponseHeaders(sourceRequest.getExcessHeaders(),
                                                    TransportHeaderUtil.getExcessTransportHeaders(synCtx),
                                                    this.preserveRequestHeaders);
                }
            }
        }
        return true;
    }

    /**
     * Populates the standard request headers  to be excluded.
     * By default list is empty
     *
     * @param excludeHeaders Comma separated header list
     */
    public void setExcludeRequestHeaders(String excludeHeaders) {
        this.standardRequestHeaders = TransportHeaderUtil.populateStandardHeaders(excludeHeaders);
    }

    /**
     * Populates the standard response headers  to be excluded.
     * By default list is empty
     *
     * @param excludeHeaders Comma separated header list
     */
    public void setExcludeResponseHeaders(String excludeHeaders) {
        this.standardResponseHeaders = TransportHeaderUtil.populateStandardHeaders(excludeHeaders);
    }

    /**
     * Populates headers to be preserved when removing request headers in response.
     * By default list is empty
     *
     * @param preserveHeaders Comma separated header list
     */
    public void setPreserveRequestHeaders(String preserveHeaders) {
        this.preserveRequestHeaders = TransportHeaderUtil.populateStandardHeaders(preserveHeaders);
    }

    /**
     * Enables/Disables removing request headers in response. By default it is disabled.
     *
     * @param isEnable Boolean flag
     */
    public void setRemoveRequestHeaders(boolean isEnable) {
        this.isRemoveRequestHeadersOnFaultEnabled = isEnable;
    }
}

