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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.base.MetricsCollector;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
import org.apache.http.nio.entity.ContentInputStream;
import org.apache.http.nio.entity.ContentOutputStream;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SharedInputBuffer;
import org.apache.http.nio.util.SharedOutputBuffer;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EncodingUtils;
import org.apache.synapse.commons.evaluators.EvaluatorContext;
import org.apache.synapse.commons.evaluators.Parser;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.commons.jmx.ThreadingView;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.nhttp.debug.ServerConnectionDebug;
import org.apache.synapse.transport.nhttp.util.LatencyView;
import org.apache.synapse.transport.nhttp.util.NhttpMetricsCollector;

/**
 * The server connection handler. An instance of this class is used by each IOReactor, to
 * process every connection. Hence this class should not store any data related to a single
 * connection - as this is being shared.
 */
public class ServerHandler implements NHttpServerEventHandler {

    private static final Log log = LogFactory.getLog(ServerHandler.class);

    /** the Axis2 configuration context */
    private final ConfigurationContext cfgCtx;
    /** the protocol scheme */
    private final Scheme scheme;
    /** the listener context */
    private final ListenerContext listenerContext;
    /** the factory to create HTTP responses */
    private final HttpResponseFactory responseFactory;
    /** the HTTP response processor */
    private final HttpProcessor httpProcessor;
    /** the strategy to re-use connections */
    private final ConnectionReuseStrategy connStrategy;
    /** the buffer allocator */
    private final ByteBufferAllocator allocator;
    /** the thread pool to process requests */
    private final WorkerPool workerPool;
    /** the metrics collector */
    private final NhttpMetricsCollector metrics;
    /**
     * This parset is used by the priority executor to parse a given HTTP message and
     * determine the priority of the message
     */
    private final Parser parser;
    /**
     * An executor capable of exucuting the Server Worker according the priority assigned
     * to a particular message
     */
    private final PriorityExecutor executor;
    /** keeps track of the connection that are alive in the system */
    private final List<NHttpServerConnection> activeConnections;

    private final LatencyView latencyView;
    private final LatencyView s2sLatencyView;
    private final ThreadingView threadingView;
    

    public static final String REQUEST_SINK_BUFFER = "synapse.request-sink-buffer";
    public static final String RESPONSE_SOURCE_BUFFER = "synapse.response-source-buffer";
    public static final String CONNECTION_CREATION_TIME = "synapse.connectionCreationTime";
    public static final String SERVER_CONNECTION_DEBUG = "synapse.server-connection-debug";

    public ServerHandler(
            final ConfigurationContext cfgCtx,
            final Scheme scheme,
            final ListenerContext listenerContext,
            final NhttpMetricsCollector metrics) {
        super();
        this.cfgCtx = cfgCtx;
        this.scheme = scheme;
        this.listenerContext = listenerContext;
        this.metrics = metrics;
        this.responseFactory = new DefaultHttpResponseFactory();
        this.httpProcessor = getHttpProcessor();
        this.connStrategy = new DefaultConnectionReuseStrategy();
        this.allocator = new HeapByteBufferAllocator();
        this.activeConnections = new ArrayList<NHttpServerConnection>();
		String strNamePostfix = "";
		if (listenerContext.getTransportIn() != null &&
		    listenerContext.getTransportIn().getName() != null) {
			strNamePostfix = "-" + listenerContext.getTransportIn().getName();
		}      
        this.latencyView = new LatencyView("NHTTPLatencyView", scheme.isSSL(), strNamePostfix);
        this.s2sLatencyView = new LatencyView("NHTTPS2SLatencyView", scheme.isSSL(), strNamePostfix);
        this.threadingView = new ThreadingView("HttpServerWorker", true, 50);

        NHttpConfiguration cfg = NHttpConfiguration.getInstance();
        if (listenerContext.getExecutor() == null)  {
            this.workerPool = WorkerPoolFactory.getWorkerPool(
                cfg.getServerCoreThreads(),
                cfg.getServerMaxThreads(),
                cfg.getServerKeepalive(),
                cfg.getServerQueueLen(),
                "Server Worker thread group", "HttpServerWorker");
            this.executor = null;
            this.parser = null;
        } else {
            this.workerPool = null;
            this.executor = listenerContext.getExecutor();
            this.parser = listenerContext.getParser();
        }
    }

    /**
     * Process a new incoming request
     * @param conn the connection
     */
    public void requestReceived(final NHttpServerConnection conn) {

        HttpContext context = conn.getContext();
        context.setAttribute(NhttpConstants.REQ_ARRIVAL_TIME, System.currentTimeMillis());
        HttpRequest request = conn.getHttpRequest();
        context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
        context.setAttribute(NhttpConstants.MESSAGE_IN_FLIGHT, "true");

        // prepare to collect debug information
        conn.getContext().setAttribute(
                ServerHandler.SERVER_CONNECTION_DEBUG, new ServerConnectionDebug(conn));

        NHttpConfiguration cfg = NHttpConfiguration.getInstance();
        try {
            InputStream is;
            // Only create an input buffer and ContentInputStream if the request has content
            if (request instanceof HttpEntityEnclosingRequest) {
                // Mark request as not yet fully read, to detect timeouts from harmless keepalive deaths
                conn.getContext().setAttribute(NhttpConstants.REQUEST_READ, Boolean.FALSE);
                
                ContentInputBuffer inputBuffer
                        = new SharedInputBuffer(cfg.getBufferSize(), conn, allocator);
                context.setAttribute(REQUEST_SINK_BUFFER, inputBuffer);
                is = new ContentInputStream(inputBuffer);
            } else {
                is = null;
                conn.getContext().removeAttribute(NhttpConstants.REQUEST_READ);
            }
            
            ContentOutputBuffer outputBuffer
                    = new SharedOutputBuffer(cfg.getBufferSize(), conn, allocator);
            context.setAttribute(RESPONSE_SOURCE_BUFFER, outputBuffer);
            OutputStream os = new ContentOutputStream(outputBuffer);

            // create the default response to this request
            ProtocolVersion httpVersion = request.getRequestLine().getProtocolVersion();
            HttpResponse response = responseFactory.newHttpResponse(
                httpVersion, HttpStatus.SC_OK, context);

            // create a basic HttpEntity using the source channel of the response pipe
            BasicHttpEntity entity = new BasicHttpEntity();
            if (httpVersion.greaterEquals(HttpVersion.HTTP_1_1)) {
                entity.setChunked(true);
            }
            response.setEntity(entity);

            if (metrics != null) {
                metrics.incrementMessagesReceived();
            }
            // hand off processing of the request to a thread off the pool
            ServerWorker worker = new ServerWorker(
                    cfgCtx, 
                    scheme.getName(),
                    metrics, conn, this, request, is, response, os,
                    listenerContext.isRestDispatching(),
                    listenerContext.getHttpGetRequestProcessor());

            if (workerPool != null) {
                workerPool.execute(worker);
            } else if (executor != null) {
                Map<String, String> headers = new HashMap<String, String>();
                for (Header header : request.getAllHeaders()) {
                    headers.put(header.getName(), header.getValue());
                }

                EvaluatorContext evaluatorContext =
                        new EvaluatorContext(request.getRequestLine().getUri(), headers);
                int priority = parser.parse(evaluatorContext);
                executor.execute(worker, priority);
            }
            
			// See if the client expects a 100-Continue
			Header expect = request.getFirstHeader(HTTP.EXPECT_DIRECTIVE);
			if (expect != null && HTTP.EXPECT_CONTINUE.equalsIgnoreCase(expect.getValue())) {
				HttpResponse ack = new BasicHttpResponse(request.getProtocolVersion(), HttpStatus.SC_CONTINUE, "Continue");
				conn.submitResponse(ack);
				if (log.isDebugEnabled()) {
					log.debug(conn + ": Expect :100 Continue hit, sending ack back to the server");
				}
				return;
			}

        } catch (Exception e) {
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
            handleException("Error processing request received for : " +
                request.getRequestLine().getUri(), e, conn);
        }
    }

    /**
     * Process ready input by writing it into the Pipe
     * @param conn the connection being processed
     * @param decoder the content decoder in use
     */
    public void inputReady(final NHttpServerConnection conn, final ContentDecoder decoder) {

        HttpContext context = conn.getContext();
        ContentInputBuffer inBuf
                = (ContentInputBuffer) context.getAttribute(REQUEST_SINK_BUFFER);

        try {
            int bytesRead = inBuf.consumeContent(decoder);
            if (metrics != null && bytesRead > 0) {
                metrics.incrementBytesReceived(bytesRead);
            }

            if (decoder.isCompleted()) {
                
                ((ServerConnectionDebug) conn.getContext().getAttribute(
                        SERVER_CONNECTION_DEBUG)).recordRequestCompletionTime();
                // remove the request we have fully read, to detect harmless keepalive timeouts from
                // real timeouts while reading requests
                context.setAttribute(NhttpConstants.REQUEST_READ, Boolean.TRUE);
            }

        } catch (IOException e) {
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
            handleException("I/O Error at inputReady : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Process ready output by writing into the channel
     * @param conn the connection being processed
     * @param encoder the content encoder in use
     */
    public void outputReady(final NHttpServerConnection conn, final ContentEncoder encoder) {

        HttpContext context = conn.getContext();
        HttpResponse response = conn.getHttpResponse();
        ContentOutputBuffer outBuf = (ContentOutputBuffer) context.getAttribute(
                RESPONSE_SOURCE_BUFFER);

        if (outBuf == null) {
            // fix for SYNAPSE 584. This is a temporaly fix becuase of HTTPCORE-208
            shutdownConnection(conn);
            return;
        }

        try {
            int bytesWritten = outBuf.produceContent(encoder);
            if (metrics != null && bytesWritten > 0) {
                metrics.incrementBytesSent(bytesWritten);
            }

            if (encoder.isCompleted()) {

                if (context.getAttribute(NhttpConstants.REQ_ARRIVAL_TIME) != null &&
                    context.getAttribute(NhttpConstants.REQ_DEPARTURE_TIME) != null &&
                    context.getAttribute(NhttpConstants.RES_ARRIVAL_TIME) != null) {

                    latencyView.notifyTimes(
                        (Long) context.getAttribute(NhttpConstants.REQ_ARRIVAL_TIME),
                        (Long) context.getAttribute(NhttpConstants.REQ_DEPARTURE_TIME),
                        (Long) context.getAttribute(NhttpConstants.RES_ARRIVAL_TIME),
                        System.currentTimeMillis());
                }

                context.removeAttribute(NhttpConstants.REQ_ARRIVAL_TIME);
                context.removeAttribute(NhttpConstants.REQ_DEPARTURE_TIME);
                context.removeAttribute(NhttpConstants.RES_ARRIVAL_TIME);
                
                ((ServerConnectionDebug) conn.getContext().getAttribute(
                        SERVER_CONNECTION_DEBUG)).recordResponseCompletionTime();

                Boolean reqRead = (Boolean) conn.getContext().getAttribute(
                        NhttpConstants.REQUEST_READ);
                Boolean forceConnectionClose = (Boolean) conn.getContext().getAttribute(
                        NhttpConstants.FORCE_CONNECTION_CLOSE);
                if (reqRead != null && !reqRead) {
                    try {
                        // this is a connection we should not re-use
                        conn.close();
                    } catch (Exception ignore) {}
                } else if (!connStrategy.keepAlive(response, context)) {
                    conn.close();
                } else if (forceConnectionClose != null && forceConnectionClose) {
                    conn.close();
                } else {
                    conn.requestInput();
                }
            }

        } catch (IOException e) {
            if (metrics != null) {
                metrics.incrementFaultsSending();
            }
            handleException("I/O Error at outputReady : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Commit the response to the connection. Processes the response through the configured
     * HttpProcessor and submits it to be sent out. This method hides any exceptions and is targetted
     * for non critical (i.e. browser requests etc) requests, which are not core messages
     * @param conn the connection being processed
     * @param response the response to commit over the connection
     */
    public void commitResponseHideExceptions(
            final NHttpServerConnection conn, final HttpResponse response) {
        try {
            conn.suspendInput();
            httpProcessor.process(response, conn.getContext());
            conn.submitResponse(response);
        } catch (HttpException e) {
            handleException("Unexpected HTTP protocol error : " + e.getMessage(), e, conn);
        } catch (IOException e) {
            handleException("IO error submiting response : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Commit the response to the connection. Processes the response through the configured
     * HttpProcessor and submits it to be sent out. Re-Throws exceptions, after closing connections
     * @param conn the connection being processed
     * @param response the response to commit over the connection
     * @throws IOException if an IO error occurs while sending the response
     * @throws HttpException if a HTTP protocol violation occurs while sending the response
     */
    public void commitResponse(final NHttpServerConnection conn,
        final HttpResponse response) throws IOException, HttpException {
        try {
            conn.suspendInput();
            HttpContext context = conn.getContext();
            httpProcessor.process(response, context);

            if (context.getAttribute(NhttpConstants.REQ_ARRIVAL_TIME) != null &&
                context.getAttribute(NhttpConstants.REQ_DEPARTURE_TIME) != null &&
                context.getAttribute(NhttpConstants.RES_HEADER_ARRIVAL_TIME) != null) {

                s2sLatencyView.notifyTimes(
                    (Long) context.getAttribute(NhttpConstants.REQ_ARRIVAL_TIME),
                    (Long) context.getAttribute(NhttpConstants.REQ_DEPARTURE_TIME),
                    (Long) context.getAttribute(NhttpConstants.RES_HEADER_ARRIVAL_TIME),
                    System.currentTimeMillis());
            }

            conn.submitResponse(response);
        } catch (HttpException e) {
            shutdownConnection(conn);
            throw e;
        } catch (IOException e) {
            shutdownConnection(conn);
            throw e;
        }
    }

    /**
     * Handle connection timeouts by shutting down the connections
     * @param conn the connection being processed
     */
    public void timeout(final NHttpServerConnection conn) {
        HttpContext context = conn.getContext();
        Boolean read = (Boolean) context.getAttribute(NhttpConstants.REQUEST_READ);

        if (read == null || read) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Keepalive connection was closed");
            }
        } else {
            log.error("Connection Timeout - before message body was fully read : " + conn);
            if (metrics != null) {
                metrics.incrementTimeoutsReceiving();
            }
        }
        shutdownConnection(conn);
    }

    public void endOfInput(final NHttpServerConnection conn) throws IOException {
        conn.close();
    }

    public void connected(final NHttpServerConnection conn) {
        if (log.isTraceEnabled()) {
            log.trace(conn + ": New incoming connection");
        }

        metrics.connected();

        // record connection creation time for debug logging
        conn.getContext().setAttribute(CONNECTION_CREATION_TIME, System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug(conn + ": Adding a connection : "
                + conn + " to the pool, existing pool size : " + activeConnections.size());
        }
        synchronized (this) {
            activeConnections.add(conn);
        }
    }

    public void responseReady(NHttpServerConnection conn) {

        if (JavaUtils.isTrueExplicitly(conn.getContext().getAttribute(NhttpConstants.FORCE_CLOSING))
                && !JavaUtils.isTrueExplicitly(conn.getContext().getAttribute(
                NhttpConstants.MESSAGE_IN_FLIGHT))) {

            try {
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Closing a persisted connection since it is forced : " + conn);
                }
                conn.close();
            } catch (IOException ignore) {}
            
            return;
        }

        metrics.notifyReceivedMessageSize(conn.getMetrics().getReceivedBytesCount());
        metrics.notifySentMessageSize(conn.getMetrics().getSentBytesCount());
        conn.getMetrics().reset();
        conn.getContext().removeAttribute(NhttpConstants.MESSAGE_IN_FLIGHT);

        if (log.isTraceEnabled()) {
            log.trace(conn + ": Ready to send response");
        }
    }

    public void closed(final NHttpServerConnection conn) {

        HttpContext context = conn.getContext();
        shutdownConnection(conn);
        context.removeAttribute(REQUEST_SINK_BUFFER);
        context.removeAttribute(RESPONSE_SOURCE_BUFFER);
        context.removeAttribute(CONNECTION_CREATION_TIME);
        context.removeAttribute(SERVER_CONNECTION_DEBUG);

        if (log.isTraceEnabled()) {
            log.trace(conn + ": Connection closed");
        }
        metrics.disconnected();
    }

    public void markActiveConnectionsToBeClosed() {
        log.info("Marking the closing signal on the connection pool of size : "
                + activeConnections.size());
        synchronized (this) {
            for (NHttpServerConnection conn : activeConnections) {
                conn.getContext().setAttribute(NhttpConstants.FORCE_CLOSING, "true");
                conn.requestOutput();
            }
        }
    }

    /**
     * Handle  errors while reading or writing to underlying channels
     * @param conn the connection being processed
     * @param e the exception encountered
     */
    public void exception(NHttpServerConnection conn, Exception e) {
        if (e instanceof HttpException) {
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }

            HttpContext context = conn.getContext();
            HttpRequest request = conn.getHttpRequest();
            ProtocolVersion ver = HttpVersion.HTTP_1_0;
            if (request != null && request.getRequestLine() != null) {
                ver = request.getRequestLine().getProtocolVersion();
            }
            HttpResponse response = responseFactory.newHttpResponse(
                    ver, HttpStatus.SC_BAD_REQUEST, context);

            byte[] msg = EncodingUtils.getAsciiBytes("Malformed HTTP request: " + e.getMessage());
            ByteArrayEntity entity = new ByteArrayEntity(msg);
            entity.setContentType("text/plain; charset=US-ASCII");
            response.setEntity(entity);
            try {
                commitResponseHideExceptions(conn, response);
            } catch (Exception ignore) {}
        } else if (e instanceof ConnectionClosedException || (e.getMessage() != null &&
                e.getMessage().contains("Connection reset by peer") ||
                e.getMessage().contains("forcibly closed"))) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": I/O error (Probably the keepalive connection " +
                        "was closed):" + e.getMessage());
            }
            shutdownConnection(conn);
        } else if (e instanceof IOException && e.getMessage() != null) {
            String msg = e.getMessage().toLowerCase();
            if (msg.indexOf("broken") != -1) {
                log.warn("I/O error (Probably the connection " +
                        "was closed by the remote party):" + e.getMessage());
            } else {
                log.error("I/O error: " + e.getMessage(), e);
            }
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
            shutdownConnection(conn);
        } else {
            log.error("Unexpected I/O error: " + e.getClass().getName(), e);
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
            shutdownConnection(conn);
        }
    }

    /**
     * Handle IO errors while reading or writing to underlying channels
     * @param conn the connection being processed
     * @param e the exception encountered
     */
    public void exception(NHttpServerConnection conn, IOException e) {
        if (e instanceof ConnectionClosedException || (e.getMessage() != null &&
                e.getMessage().contains("Connection reset by peer") ||
                e.getMessage().contains("forcibly closed"))) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": I/O error (Probably the keepalive connection " +
                        "was closed):" + e.getMessage());
            }
        } else if (e.getMessage() != null) {
            String msg = e.getMessage().toLowerCase();
            if (msg.indexOf("broken") != -1) {
                log.warn("I/O error (Probably the connection " +
                        "was closed by the remote party):" + e.getMessage());
            } else {
                log.error("I/O error: " + e.getMessage(), e);
            }
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
        } else {
            log.error("Unexpected I/O error: " + e.getClass().getName(), e);
            if (metrics != null) {
                metrics.incrementFaultsReceiving();
            }
        }
        shutdownConnection(conn);
    }

    // ----------- utility methods -----------

    private void handleException(String msg, Exception e, NHttpServerConnection conn) {
        log.error(msg, e);
        if (conn != null) {
            shutdownConnection(conn);
        }
    }

    /**
     * Shutdown the connection ignoring any IO errors during the process
     * @param conn the connection to be shutdown
     */
    private void shutdownConnection(final NHttpServerConnection conn) {
        SharedOutputBuffer outputBuffer = (SharedOutputBuffer)
            conn.getContext().getAttribute(RESPONSE_SOURCE_BUFFER);
        if (outputBuffer != null) {
            outputBuffer.close();
        }
        SharedInputBuffer inputBuffer = (SharedInputBuffer)
            conn.getContext().getAttribute(REQUEST_SINK_BUFFER);
        if (inputBuffer != null) {
            inputBuffer.close();
        }

        synchronized (this) {
            if (!activeConnections.isEmpty() && activeConnections.remove(conn) && log.isDebugEnabled()) {
                log.debug(conn + ": Removing the connection : " + conn
                        + " from pool of size : " + activeConnections.size());
            }
        }

        try {
            conn.shutdown();
        } catch (IOException ignore) {}
    }

    public int getActiveConnectionsSize() {
        return activeConnections.size();
    }

    /**
     * Return the HttpProcessor for responses
     * @return the HttpProcessor that processes HttpResponses of this server
     */
    private HttpProcessor getHttpProcessor() {
        BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
        httpProcessor.addInterceptor(new ResponseDate());
        httpProcessor.addInterceptor(new ResponseServer());
        httpProcessor.addInterceptor(new ResponseContent());
        httpProcessor.addInterceptor(new ResponseConnControl());
        return httpProcessor;
    }

    public int getActiveCount() {
        return workerPool.getActiveCount();
    }

    public int getQueueSize() {
        return workerPool.getQueueSize();
    }
    
    public MetricsCollector getMetrics() {
        return metrics;
    }

    public void stop() {
        latencyView.destroy();
        s2sLatencyView.destroy();
        threadingView.destroy();

        try {
            if (workerPool != null) {
                workerPool.shutdown(1000);
            } else if (executor != null) {
                executor.destroy();
            }
        } catch (InterruptedException ignore) {}
    }
}
