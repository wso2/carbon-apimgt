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

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.http.*;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a Http Request.
 */
public class SourceRequest {
    // private Log log = LogFactory.getLog(SourceRequest.class);

    private Pipe pipe = null;
    /** HTTP Headers */
    private Map<String, String> headers =  new TreeMap<String, String>(new Comparator<String>() {
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    });
    /** HTTP URL */
    private String url;
    /** HTTP Method */
    private String method;
    /** Weather reqyest has a body */
    private boolean entityEnclosing;
    /** The http request */
    private HttpRequest request = null;
    /** Configuration of the receiver */
    private SourceConfiguration sourceConfiguration;
    /** HTTP Version */
    private ProtocolVersion version = null;
    /** The connection from the client */
    private NHttpServerConnection connection = null;
   
    /** Excess headers of the request */
    private Map excessHeaders = new MultiValueMap();


    public SourceRequest(SourceConfiguration sourceConfiguration,
                         HttpRequest request,
                         NHttpServerConnection conn) {
        this.sourceConfiguration = sourceConfiguration;
        this.request = request;
        this.connection = conn;

        this.url = request.getRequestLine().getUri();
        this.method = request.getRequestLine().getMethod();
        this.entityEnclosing = request instanceof HttpEntityEnclosingRequest;
        this.version = request.getProtocolVersion();

        this.version = request.getRequestLine().getProtocolVersion();
        if (!version.lessEquals(HttpVersion.HTTP_1_1)) {
            // Downgrade protocol version if greater than HTTP/1.1
            this.version = HttpVersion.HTTP_1_1;
        }

        Header[] headers = request.getAllHeaders();
        if (headers != null) {
            for (Header header : headers) {
                if(this.headers.containsKey(header.getName())) {
	                 addExcessHeader(header);
	            } else {
	                 this.headers.put(header.getName(), header.getValue());
	            }
	        }
	    }
    }

    /**
     * Start processing the request by connecting the pipe if this request has an entity body.
     * @param conn connection
     * @throws IOException if an error occurs
     * @throws HttpException if an error occurs
     */
    public void start(NHttpServerConnection conn) throws IOException, HttpException {
        if (entityEnclosing) {
            pipe = new Pipe(conn, sourceConfiguration.getBufferFactory().getBuffer(), "source", sourceConfiguration);

            SourceContext.get(conn).setReader(pipe);

            // See if the client expects a 100-Continue
            if (((HttpEntityEnclosingRequest) request).expectContinue()) {
                HttpResponse ack = new BasicHttpResponse(version, HttpStatus.SC_CONTINUE, "Continue");
                conn.submitResponse(ack);
            }
        } else {
            // this request is completed, there is nothing more to read
            SourceContext.updateState(conn, ProtocolState.REQUEST_DONE);
            // No httpRequest content expected. Suspend client input
            conn.suspendInput();
        }
    }

    /**
     * Produce the content in to the pipe.
     * @param conn the connection
     * @param decoder content decoder
     *
     * @throws java.io.IOException if an error occurs
     * @return number of bytes read
     */
    public int read(NHttpServerConnection conn, ContentDecoder decoder) throws IOException {
        if (pipe == null) {
            throw new IllegalStateException("A Pipe must be connected before calling read");
        }

        if (entityEnclosing) {
            int bytes = pipe.produce(decoder);

            if (decoder.isCompleted()) {
                sourceConfiguration.getMetrics().
                        notifyReceivedMessageSize(conn.getMetrics().getReceivedBytesCount());

                // Update connection state
                SourceContext.updateState(conn, ProtocolState.REQUEST_DONE);
                // Suspend client input
                conn.suspendInput();
            }
            return bytes;
        } else {
            throw new IllegalStateException("Only Entity Enclosing Requests " +
                    "can read content in to the pipe");
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getUri() {
        return url;
    }

    public String getMethod() {
        return method;
    }
    
    public Map getExcessHeaders() {
         return this.excessHeaders;
    }
  
    public void addExcessHeader(Header h) {
         this.excessHeaders.put(h.getName(), h.getValue());
    }

    public Pipe getPipe() {
        return pipe;
    }

    public NHttpServerConnection getConnection() {
        return connection;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public boolean isEntityEnclosing() {
        return entityEnclosing;
    }
}
