/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru;

import org.apache.axis2.context.MessageContext;
import org.apache.http.nio.NHttpConnection;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;

import java.nio.ByteBuffer;

/**
 * When a connection is created, an object of this class is stored in the Connection Context.
 * It is used as a holder for information required during the life-cycle of this connection.
 */
public class TargetContext {
    private TargetConfiguration targetConfiguration = null;

    public static final String CONNECTION_INFORMATION = "CONNECTION_INFORMATION";
    /** The request for this connection */
    private TargetRequest request;
    /** The response for this connection */
    private TargetResponse response;
    /** State of the connection */
    private ProtocolState state;
    /** The request message context */
    private MessageContext requestMsgCtx;
    /** The current reader */
    private Pipe reader;
    /** The current writer */
    private Pipe writer;

    public TargetContext(TargetConfiguration targetConfiguration) {
        this.targetConfiguration = targetConfiguration;
    }

    public ProtocolState getState() {
        return state;
    }

    public void setState(ProtocolState state) {
        this.state = state;
    }

    public TargetRequest getRequest() {
        return request;
    }

    public void setRequest(TargetRequest request) {
        this.request = request;
    }

    public TargetResponse getResponse() {
        return response;
    }

    public void setResponse(TargetResponse response) {
        this.response = response;
    }

    public MessageContext getRequestMsgCtx() {
        return requestMsgCtx;
    }

    public void setRequestMsgCtx(MessageContext requestMsgCtx) {
        this.requestMsgCtx = requestMsgCtx;
    }

    public Pipe getReader() {
        return reader;
    }

    public Pipe getWriter() {
        return writer;
    }

    public void setReader(Pipe reader) {
        this.reader = reader;
    }

    public void setWriter(Pipe writer) {
        this.writer = writer;
    }

    /**
     * Reset the resources associated with this context
     */
    public void reset() {
       reset(false);
    }

    /**
     * Reset the resources associated with this context
     *
     * @param isError whether an error is causing this shutdown of the connection.
     *                It is very important to set this flag correctly.
     *                When an error causing the shutdown of the connections we should not
     *                release associated writer buffer to the pool as it might lead into
     *                situations like same buffer is getting released to both source and target
     *                buffer factories
     */
    public void reset(boolean isError) {
        request = null;
        response = null;
        state = ProtocolState.REQUEST_READY;

        if (writer != null) {
            if (!isError) {      // If there is an error we do not release the buffer to the factory
                ByteBuffer buffer = writer.getBuffer();
                targetConfiguration.getBufferFactory().release(buffer);
            }
        }

        reader = null;
        writer = null;       
    }

    public static void create(NHttpConnection conn, ProtocolState state, 
                              TargetConfiguration configuration) {
        TargetContext info = new TargetContext(configuration);

        conn.getContext().setAttribute(CONNECTION_INFORMATION, info);

        info.setState(state);
    }

    public static void updateState(NHttpConnection conn, ProtocolState state) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setState(state);
        }  else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static boolean assertState(NHttpConnection conn, ProtocolState state) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null && info.getState() == state;

    }

    public static ProtocolState getState(NHttpConnection conn) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getState() : null;
    }

    public static void setRequest(NHttpConnection conn, TargetRequest request) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setRequest(request);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static void setResponse(NHttpConnection conn, TargetResponse response) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setResponse(response);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static TargetRequest getRequest(NHttpConnection conn) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getRequest() : null;
    }

    public static TargetResponse getResponse(NHttpConnection conn) {
        TargetContext info = (TargetContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getResponse() : null;
    }

    public static TargetContext get(NHttpConnection conn) {
        return (TargetContext) conn.getContext().getAttribute(CONNECTION_INFORMATION);
    }

}
