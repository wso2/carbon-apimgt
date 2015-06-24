/**
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

package org.apache.synapse.transport.nhttp.debug;

import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.synapse.transport.nhttp.ServerHandler;

import java.net.InetAddress;

/**
 * A connection debug object would be accumulated during request processing, but only made use
 * of if the connection encounters issues during processing.
 */
public class ServerConnectionDebug extends AbstractConnectionDebug {

    private long connectionCreationTime;
    private long requestStartTime;
    private long requestCompletionTime;
    private long responseStartTime;
    private long responseCompletionTime;

    private String remoteClientIP;
    private String requestURLPAth;
    private String requestHTTPMethod;
    private String requestHTTPProtocol;

    private ClientConnectionDebug clientConnectionDebug = null;

    public ServerConnectionDebug(NHttpServerConnection conn) {

        super();
        this.connectionCreationTime = (Long) conn.getContext().getAttribute(
                ServerHandler.CONNECTION_CREATION_TIME);
        this.requestStartTime = System.currentTimeMillis();

        // assume an entity body is not present. If present this would be overwritten
        this.requestCompletionTime = System.currentTimeMillis();

        RequestLine reqLine = conn.getHttpRequest().getRequestLine();
        this.requestURLPAth = reqLine.getUri();
        this.requestHTTPMethod = reqLine.getMethod();
        this.requestHTTPProtocol = reqLine.getProtocolVersion().toString();

        if (conn instanceof HttpInetConnection) {
            HttpInetConnection inetConn = (HttpInetConnection) conn;
            InetAddress remoteAddr = inetConn.getRemoteAddress();
            if (remoteAddr != null) {
                this.remoteClientIP = remoteAddr.getHostAddress();
            }
        }

        HttpRequest req = conn.getHttpRequest();
        this.headers = req.getAllHeaders();
    }

    public void recordResponseStartTime() {
        this.responseStartTime = System.currentTimeMillis();
    }

    public void recordRequestCompletionTime() {
        this.requestCompletionTime = System.currentTimeMillis();
    }

    public void recordResponseCompletionTime() {
        this.responseCompletionTime = System.currentTimeMillis();
    }

    public void setClientConnectionDebug(ClientConnectionDebug clientConnectionDebug) {
        this.clientConnectionDebug = clientConnectionDebug;
    }

    public ClientConnectionDebug getClientConnectionDebug() {
        return clientConnectionDebug;
    }

    public String dump() {
        StringBuffer sb = new StringBuffer(50);
        responseCompletionTime = responseCompletionTime == 0 ?
                System.currentTimeMillis() : responseCompletionTime;
        long totalTime = responseCompletionTime - requestStartTime;

        sb.append("C2E-Req-StartTime").append(keyValueSeparator).append(format(requestStartTime));
        sb.append(fieldSeparator);
        sb.append("C2E-Req-EndTime").append(keyValueSeparator).append(format(requestCompletionTime));
        sb.append(fieldSeparator);
        sb.append("C2E-Req-ConnCreateTime").append(keyValueSeparator).append(format(connectionCreationTime));
        sb.append(statementSeparator);

        sb.append("C2E-Req-URL").append(keyValueSeparator).append(requestURLPAth);
        sb.append(fieldSeparator);
        sb.append("C2E-Req-Protocol").append(keyValueSeparator).append(requestHTTPProtocol);
        sb.append(fieldSeparator);
        sb.append("C2E-Req-Method").append(keyValueSeparator).append(requestHTTPMethod);
        sb.append(statementSeparator);

        sb.append("C2E-Req-IP").append(keyValueSeparator).append(remoteClientIP);
        if (!printNoHeaders) {
            sb.append(fieldSeparator);
            sb.append("C2E-Req-Info").append("{").append(headersToString()).append("}");
        }
        sb.append(statementSeparator);

        if (clientConnectionDebug != null) {
            sb.append(clientConnectionDebug.dump());
        }

        sb.append("E2C-Resp-Start").append(keyValueSeparator).append(format(responseStartTime));
        sb.append(fieldSeparator);
        sb.append("E2C-Resp-End").append(keyValueSeparator).append(format(responseCompletionTime));
        sb.append(statementSeparator);

        sb.append("Total-Time").append(keyValueSeparator).append(totalTime).append("ms");
        if (clientConnectionDebug != null) {
            long svcTime = clientConnectionDebug.getResponseCompletionTime()
                    - clientConnectionDebug.getLastRequestStartTime();
            svcTime = svcTime < 0 ? clientConnectionDebug.getResponseStartTime()
                    - clientConnectionDebug.getLastRequestStartTime() : svcTime;
            sb.append(fieldSeparator);
            sb.append("Svc-Time").append(keyValueSeparator).append(svcTime > 0 ?
                    Long.toString(svcTime) + "ms" : "UNDETERMINED");
            sb.append(fieldSeparator);
            sb.append("ESB-Time").append(keyValueSeparator).append(svcTime > 0 ?
                    Long.toString(totalTime - svcTime) + "ms" : "UNDETERMINED");
        }

        return sb.toString();
    }

}