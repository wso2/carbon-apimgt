/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.nhttp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.nio.NHttpServerConnection;

import java.io.OutputStream;

/**
 * This interface is used for plugging in different implementations for special processing of some
 * HTTP GET requests.
 * <p/>
 * e.g. ?wsdl, ?wsdl2 etc.
 * <p/>
 * If you need to handle a special HTTP GET request, you have to write an implementation of this
 * interface.
 */
public interface HttpGetRequestProcessor {
    /**
     * Initialize the HttpGetProcessor
     * @param cfgCtx servers configuration context
     * @param serverHandler dispatching handler
     * @throws AxisFault if an error occurs
     */
    void init(ConfigurationContext cfgCtx, ServerHandler serverHandler) throws AxisFault;

    /**
     * Process the HTTP GET request.
     *
     * @param request       The HttpRequest
     * @param response      The HttpResponse
     * @param msgContext    The MessageContext
     * @param conn          The NHttpServerConnection
     * @param os            The OutputStream
     * @param isRestDispatching Rest dispatching
     */
    void process(HttpRequest request,
                 HttpResponse response,
                 MessageContext msgContext,
                 NHttpServerConnection conn,
                 OutputStream os,
                 boolean isRestDispatching);

}
