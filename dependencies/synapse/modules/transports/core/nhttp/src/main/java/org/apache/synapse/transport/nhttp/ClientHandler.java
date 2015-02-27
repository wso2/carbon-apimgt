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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.base.MetricsCollector;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.Credentials;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.entity.ContentInputStream;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.ContentInputBuffer;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SharedInputBuffer;
import org.apache.http.nio.util.SharedOutputBuffer;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.synapse.commons.jmx.ThreadingView;
import org.apache.synapse.transport.http.conn.ClientConnFactory;
import org.apache.synapse.transport.http.conn.ProxyAuthenticator;
import org.apache.synapse.transport.http.conn.ProxyTunnelHandler;
import org.apache.synapse.transport.nhttp.debug.ClientConnectionDebug;
import org.apache.synapse.transport.nhttp.util.NhttpMetricsCollector;

/**
 * The client connection handler. An instance of this class is used by each IOReactor, to
 * process every connection. Hence this class should not store any data related to a single
 * connection - as this is being shared.
 */
public class ClientHandler implements NHttpClientEventHandler {

    private static final Log log = LogFactory.getLog(ClientHandler.class);

    /** the HTTP protocol parameters to adhere to for outgoing messages */
    private final HttpParams params;
    /** the HttpProcessor for response messages received */
    private final HttpProcessor httpProcessor;
    /** the connection re-use strategy */
    private final ConnectionReuseStrategy connStrategy;
    /** the buffer allocator */
    private final ByteBufferAllocator allocator;

    /** Connection factory used by this sender **/
    private ClientConnFactory connFactory;
    /** the connection pool */
    private final ConnectionPool connpool;
    /** the proxy authenticator */
    private final ProxyAuthenticator proxyauthenticator;

    /** the Axis2 configuration context */
    private ConfigurationContext cfgCtx = null;

    /** the nhttp configuration */
    private NHttpConfiguration cfg = null;

    private WorkerPool workerPool = null;
    /** the metrics collector */
    private NhttpMetricsCollector metrics = null;

    /** Array of content types for which warnings are logged if HTTP status code is 500. */
    private String[] warnOnHttp500;

    /** weather we are counting the connections to the back end servers */
    private boolean countConnections = false;
    /** lock to update the connection counts in a thread safe way */
    private Lock lock = new ReentrantLock();

    private ThreadingView threadingView = null;

    /** A map for holding the number of open connections for a host:port pair */
    private Map<String, AtomicInteger> openConnections = new HashMap<String, AtomicInteger>();

    public static final String ATTACHMENT_KEY = "synapse.axis2-attachment";
    public static final String TUNNEL_HANDLER = "synapse.tunnel-handler";

    public static final String OUTGOING_MESSAGE_CONTEXT = "synapse.axis2_message_context";
    public static final String AXIS2_HTTP_REQUEST = "synapse.axis2-http-request";
    public static final String CLIENT_CONNECTION_DEBUG = "synapse.client-connection-debug";
    public static final String CONNECTION_CREATION_TIME = "synapse.connectionCreationTime";

    public static final String REQUEST_SOURCE_BUFFER = "synapse.request-source-buffer";
    public static final String RESPONSE_SINK_BUFFER = "synapse.response-sink-buffer";

    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Create an instance of this client connection handler using the Axis2 configuration
     * context and Http protocol parameters given
     * 
     * @param cfgCtx the Axis2 configuration context
     * @param params the Http protocol parameters to adhere to
     * @param metrics statistics collection metrics
     */
    public ClientHandler(
            final ConnectionPool connpool,
            final ClientConnFactory connFactory,
            final Credentials proxycreds,
            final ConfigurationContext cfgCtx,
            final HttpParams params,
            final NhttpMetricsCollector metrics) {
        super();
        this.connpool = connpool;
        this.connFactory = connFactory;
        this.proxyauthenticator = proxycreds != null ? new ProxyAuthenticator(proxycreds) : null;
        this.cfgCtx = cfgCtx;
        this.params = params;
        this.httpProcessor = getHttpProcessor();
        this.connStrategy = new DefaultConnectionReuseStrategy();
        this.metrics = metrics;
        this.allocator = new HeapByteBufferAllocator();
        this.threadingView = new ThreadingView("HttpClientWorker", true, 50);

        this.cfg = NHttpConfiguration.getInstance();
        workerPool = WorkerPoolFactory.getWorkerPool(
            cfg.getClientCoreThreads(),
            cfg.getClientMaxThreads(),
            cfg.getClientKeepalive(),
            cfg.getClientQueueLen(),
            "Client Worker thread group", "HttpClientWorker");

        Object contentTypeList = cfgCtx.getLocalProperty("warnOnHTTP500");
        if (contentTypeList != null) {
            warnOnHttp500 = (String[]) contentTypeList;
        }
        // check weather we count the connections
        this.countConnections = NHttpConfiguration.getInstance().isCountConnections();
        // set the connection map to the configuration context
        cfgCtx.setProperty(NhttpConstants.OPEN_CONNNECTIONS_MAP, openConnections);

        // set the latest openConnections map to MBean data during connection creation
        metrics.setConnectionsPerHosts(openConnections);
    }

    public void requestReady(final NHttpClientConnection conn) throws IOException, HttpException{
        // The connection is ready for submission of a new request
        HttpContext context = conn.getContext();
        ProxyTunnelHandler tunnelHandler = (ProxyTunnelHandler) context.getAttribute(TUNNEL_HANDLER);
        if (tunnelHandler != null && !tunnelHandler.isCompleted()) {
            if (!tunnelHandler.isRequested()) {
                HttpRequest request = tunnelHandler.generateRequest(context);
                if (proxyauthenticator != null) {
                    proxyauthenticator.authenticatePreemptively(request, context);
                }
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Sending CONNECT request to " + tunnelHandler.getProxy());
                }
                conn.submitRequest(request);
                tunnelHandler.setRequested();
            }
            return;
        }

        Axis2HttpRequest axis2Req = (Axis2HttpRequest) context.removeAttribute(ATTACHMENT_KEY);
        if (axis2Req == null || axis2Req.isCompleted()) {
            return;
        }
        try {
            processConnection(conn,  axis2Req);
        } catch (ConnectionClosedException e) {
            metrics.incrementFaultsSending();
            handleException("I/O Error submitting request : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Submit a new request over an already established connection, which has been
     * 'kept alive'
     *
     * @param conn the connection to use to send the request, which has been kept open
     * @param axis2Req the new request
     * @throws ConnectionClosedException if the connection is closed by the other party
     */
    public void submitRequest(final NHttpClientConnection conn, final Axis2HttpRequest axis2Req)
        throws ConnectionClosedException {
        conn.getContext().setAttribute(ATTACHMENT_KEY, axis2Req);
        conn.requestOutput();
    }

    /**
     * Invoked when the destination is connected
     * 
     * @param conn the connection being processed
     * @param attachment the attachment set previously
     */
    public void connected(final NHttpClientConnection conn, final Object attachment) {

        if (log.isDebugEnabled() ) {
            log.debug(conn + ": ClientHandler connected : " + conn);
        }
        HttpContext context = conn.getContext();
        metrics.connected();

        // record connection creation time for debug logging
        context.setAttribute(CONNECTION_CREATION_TIME, System.currentTimeMillis());

        if (countConnections) {
            recordConnection(conn);
        }

        Axis2HttpRequest axis2Req = (Axis2HttpRequest) attachment;
        HttpRoute route = axis2Req.getRoute();
        if (route.isTunnelled()) {
            // Requires a proxy tunnel
            ProxyTunnelHandler tunnelHandler = new ProxyTunnelHandler(route, connFactory);
            context.setAttribute(TUNNEL_HANDLER, tunnelHandler);
        }
        context.setAttribute(ATTACHMENT_KEY, axis2Req);
        conn.requestOutput();

    }

    /**
     * Process a new connection over an existing TCP connection or new
     * 
     * @param conn HTTP connection to be processed
     * @param axis2Req axis2 representation of the message in the connection
     * @throws ConnectionClosedException if the connection is closed 
     */
    private void processConnection(final NHttpClientConnection conn,
        final Axis2HttpRequest axis2Req) throws ConnectionClosedException {

        // record start time of request
        ClientConnectionDebug cd = (ClientConnectionDebug)
                axis2Req.getMsgContext().getProperty(CLIENT_CONNECTION_DEBUG);
        if (cd != null) {
            cd.recordRequestStartTime(conn, axis2Req);
            conn.getContext().setAttribute(CLIENT_CONNECTION_DEBUG, cd);
        }

        try {
            // Reset connection metrics
            conn.getMetrics().reset();

            HttpContext context = conn.getContext();
            ContentOutputBuffer outputBuffer
                    = new SharedOutputBuffer(cfg.getBufferSize(), conn, allocator);
            axis2Req.setOutputBuffer(outputBuffer);
            context.setAttribute(REQUEST_SOURCE_BUFFER, outputBuffer);

            HttpRoute route = axis2Req.getRoute();
            context.setAttribute(AXIS2_HTTP_REQUEST, axis2Req);
            context.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
            context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, route.getTargetHost());
            context.setAttribute(OUTGOING_MESSAGE_CONTEXT, axis2Req.getMsgContext());

            HttpRequest request = axis2Req.getRequest();
            request.setParams(new DefaultedHttpParams(request.getParams(), this.params));

            /*
             * Remove Content-Length and Transfer-Encoding headers, if already present.
             * */
            request.removeHeaders(HTTP.TRANSFER_ENCODING);
            request.removeHeaders(HTTP.CONTENT_LEN);

            this.httpProcessor.process(request, context);
            if (proxyauthenticator != null && route.getProxyHost() != null && !route.isTunnelled()) {
                proxyauthenticator.authenticatePreemptively(request, context);
            }
            if (axis2Req.getTimeout() > 0) {
                conn.setSocketTimeout(axis2Req.getTimeout());
            }

            context.setAttribute(NhttpConstants.ENDPOINT_PREFIX, axis2Req.getEndpointURLPrefix());
            context.setAttribute(NhttpConstants.HTTP_REQ_METHOD, request.getRequestLine().getMethod());
            context.setAttribute(ExecutionContext.HTTP_REQUEST, request);
            setServerContextAttribute(NhttpConstants.REQ_DEPARTURE_TIME,
                        System.currentTimeMillis(), conn);
            conn.submitRequest(request);
        } catch (ConnectionClosedException e) {
            throw e;
        } catch (IOException e) {
            if (metrics != null) {
                metrics.incrementFaultsSending();
            }
            handleException("I/O Error submitting request : " + e.getMessage(), e, conn);
        } catch (HttpException e) {
            if (metrics != null) {
                metrics.incrementFaultsSending();
            }
            handleException("HTTP protocol error submitting request : " + e.getMessage(), e, conn);
        } finally {
            synchronized(axis2Req) {
                axis2Req.setReadyToStream(true);
                axis2Req.notifyAll();
            }
        }
    }

    /**
     * Handle connection close events
     * 
     * @param conn HTTP connection to be closed
     */
    public void closed(final NHttpClientConnection conn) {
        connpool.forget(conn);
        String message = getErrorMessage("Connection close", conn);
        if (log.isTraceEnabled()) {
            log.trace(conn + ": " + message);
        }
        Axis2HttpRequest axis2Request = (Axis2HttpRequest)
            conn.getContext().getAttribute(AXIS2_HTTP_REQUEST);

        if (axis2Request != null && !axis2Request.isCompleted()) {
            checkAxisRequestComplete(conn, NhttpConstants.CONNECTION_CLOSED, message, null);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": " + getErrorMessage("Keep-alive connection closed", conn));
            }
        }

        HttpContext context = conn.getContext();
        shutdownConnection(conn);
        context.removeAttribute(RESPONSE_SINK_BUFFER);
        context.removeAttribute(REQUEST_SOURCE_BUFFER);
        metrics.disconnected();
    }

    /**
     * Handle connection timeouts by shutting down the connections. These are established
     * that have reached the SO_TIMEOUT of the socket
     * 
     * @param conn the connection being processed
     */
    public void timeout(final NHttpClientConnection conn) {
        String message = getErrorMessage("Connection timeout", conn);
        if (log.isDebugEnabled()) {
            log.debug(conn + ": " + message);
        }

        Axis2HttpRequest axis2Request = (Axis2HttpRequest)
            conn.getContext().getAttribute(AXIS2_HTTP_REQUEST);

        if (axis2Request != null && !axis2Request.isCompleted()) {
            checkAxisRequestComplete(conn, NhttpConstants.CONNECTION_TIMEOUT, message, null);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": " + getErrorMessage("Keep-alive connection timed out", conn));
            }
        }

        HttpContext context = conn.getContext();
        shutdownConnection(conn);
        context.removeAttribute(RESPONSE_SINK_BUFFER);
        context.removeAttribute(REQUEST_SOURCE_BUFFER);
    }

    /**
     * Handle Http protocol violations encountered while reading from underlying channels
     * 
     * @param conn the connection being processed
     * @param e the exception encountered
     */
    public void exception(final NHttpClientConnection conn, final HttpException e) {
        String message = getErrorMessage("HTTP protocol violation : " + e.getMessage(), conn);
        log.error(message, e);
    	checkAxisRequestComplete(conn, NhttpConstants.PROTOCOL_VIOLATION, message, e);
        shutdownConnection(conn);
    }

    /**
     * Handle IO errors while reading or writing to underlying channels
     * 
     * @param conn the connection being processed
     * @param ex the exception encountered
     */
    public void exception(final NHttpClientConnection conn, final Exception ex) {

        if (ex instanceof HttpException) {
            String message = getErrorMessage("HTTP protocol violation : " + ex.getMessage(), conn);
            log.error(message, ex);
            checkAxisRequestComplete(conn, NhttpConstants.PROTOCOL_VIOLATION, message, ex);
        } if (ex instanceof IOException) {
            String message = getErrorMessage("I/O error : " + ex.getMessage(), conn);
            if (message.toLowerCase().indexOf("reset") != -1) {
                log.warn(message);
            } else {
                log.error(message, ex);
            }
            checkAxisRequestComplete(conn, NhttpConstants.SND_IO_ERROR_SENDING, message, ex);
        } else {
            log.error(ex.getMessage(), ex);
        }
        shutdownConnection(conn);


        String message = getErrorMessage("I/O error : " + ex.getMessage(), conn);
        if (message.toLowerCase().indexOf("reset") != -1) {
            log.warn(message);
        } else {
            log.error(message, ex);
        }
        checkAxisRequestComplete(conn, NhttpConstants.SND_IO_ERROR_SENDING, message, ex);
        shutdownConnection(conn);
    }

    public void endOfInput(NHttpClientConnection conn) throws IOException {
        conn.close();
    }

    /**
     * Include remote host and port information to an error message
     * 
     * @param message the initial message
     * @param conn the connection encountering the error
     * @return the updated error message
     */
       private String getErrorMessage(String message, NHttpClientConnection conn) {
           if (conn != null && conn instanceof DefaultNHttpClientConnection) {
               DefaultNHttpClientConnection c = ((DefaultNHttpClientConnection) conn);
               Axis2HttpRequest axis2Request = (Axis2HttpRequest)
                       conn.getContext().getAttribute(AXIS2_HTTP_REQUEST);

               if (c.getRemoteAddress() != null) {
                   return message + " For : " + c.getRemoteAddress().getHostAddress() + ":" +
                           c.getRemotePort() + (axis2Request != null ? " For Request : "
                           + axis2Request : "");
               }
           }
           return message;
       }

    /**
     * check to see if http request-response has completed, if not completed yet,
     * notify an exception to the message-receiver
     *
     * @param conn the connection being checked for completion
     * @param errorCode the error code to raise
     * @param errorMessage the text for an error message to be returned to the MR on failure
     * @param exceptionToRaise an Exception to be returned to the MR on failure
     */
    private void checkAxisRequestComplete(NHttpClientConnection conn,
        final int errorCode, final String errorMessage, final Exception exceptionToRaise) {

        Axis2HttpRequest axis2Request = (Axis2HttpRequest)
                conn.getContext().getAttribute(AXIS2_HTTP_REQUEST);
        if (axis2Request != null && !axis2Request.isCompleted()) {
            markRequestCompletedWithError(axis2Request, errorCode, errorMessage, exceptionToRaise);
        }
    }

    /**
     * Mark request to send failed with error
     *
     * @param axis2Request the Axis2HttpRequest to be marked as completed with an error
     * @param errorCode the error code to raise
     * @param errorMessage the text for an error message to be returned to the MR on failure
     * @param exceptionToRaise an Exception to be returned to the MR on failure
     */
    protected void markRequestCompletedWithError(
        final Axis2HttpRequest axis2Request, 
        final int errorCode, final String errorMessage, final Exception exceptionToRaise) {

        axis2Request.setCompleted(true);
        if (errorCode == -1 && errorMessage == null && exceptionToRaise == null) {
            return; // no need to continue
        }

        final MessageContext mc = axis2Request.getMsgContext();

        // if the request message is a sandesha messag we ignore the
        // exception handling
        // we cannot use the declared sandesha2 constant since
        // nhttp transport shouldn't take a sandesha2 dependency
        String done = (String) mc.getProperty("Sandesha2AppProcessingDone");
		if (JavaUtils.isTrueExplicitly(done)) {
			return;
		}

        if (mc.getAxisOperation() != null &&
                mc.getAxisOperation().getMessageReceiver() != null) {

            if (metrics != null) {
                if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                    if (errorCode == NhttpConstants.CONNECTION_TIMEOUT) {
                        metrics.incrementTimeoutsReceiving(mc);
                    } else {
                        metrics.incrementFaultsSending(errorCode, mc);
                    }
                } else {
                    if (errorCode == NhttpConstants.CONNECTION_TIMEOUT) {
                        metrics.incrementTimeoutsReceiving();
                    } else {
                        metrics.incrementFaultsSending();
                    }
                }
            }

            workerPool.execute( new Runnable() {
                public void run() {
                    MessageReceiver mr = mc.getAxisOperation().getMessageReceiver();
                    try {
                        // This AxisFault is created to create the fault message context
                        // noinspection ThrowableInstanceNeverThrown
                        AxisFault axisFault = exceptionToRaise != null ?
                                new AxisFault(errorMessage, exceptionToRaise) :
                                new AxisFault(errorMessage);

                        MessageContext nioFaultMessageContext =
                            MessageContextBuilder.createFaultMessageContext(mc, axisFault);

                        SOAPEnvelope envelope = nioFaultMessageContext.getEnvelope();

                        if (log.isDebugEnabled()) {
                            log.debug("Sending Fault for Request with Message ID : "
                                    + mc.getMessageID());
                        }
                        
                        nioFaultMessageContext.setProperty(
                            NhttpConstants.SENDING_FAULT, Boolean.TRUE);
                        nioFaultMessageContext.setProperty(
                                NhttpConstants.ERROR_MESSAGE, errorMessage);
                        if (errorCode != -1) {
                            nioFaultMessageContext.setProperty(
                                NhttpConstants.ERROR_CODE, errorCode);
                        }
                        if (exceptionToRaise != null) {
                            nioFaultMessageContext.setProperty(
                                NhttpConstants.ERROR_DETAIL, exceptionToRaise.toString());
                            nioFaultMessageContext.setProperty(
                                NhttpConstants.ERROR_EXCEPTION, exceptionToRaise);
                            envelope.getBody().getFault().getDetail().setText(
                                exceptionToRaise.toString());
                        } else {
                            nioFaultMessageContext.setProperty(
                                NhttpConstants.ERROR_DETAIL, errorMessage);
                            envelope.getBody().getFault().getDetail().setText(errorMessage);
                        }
                        nioFaultMessageContext.setProperty(CLIENT_CONNECTION_DEBUG,
                            mc.getProperty(CLIENT_CONNECTION_DEBUG));
                        mr.receive(nioFaultMessageContext);

                    } catch (AxisFault af) {
                        log.error("Unable to report back failure to the message receiver", af);
                    }
                }
            });
        }
    }

    /**
     * Process ready input (i.e. response from remote server)
     * 
     * @param conn connection being processed
     * @param decoder the content decoder in use
     */
    public void inputReady(final NHttpClientConnection conn, final ContentDecoder decoder) {
        HttpContext context = conn.getContext();
        HttpResponse response = conn.getHttpResponse();
        ContentInputBuffer inBuf = (ContentInputBuffer) context.getAttribute(RESPONSE_SINK_BUFFER);

        try {
            int bytesRead = inBuf.consumeContent(decoder);
            if (metrics != null && bytesRead > 0) {
                if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                    metrics.incrementBytesReceived(getMessageContext(conn), bytesRead);
                } else {
                    metrics.incrementBytesReceived(bytesRead);
                }
            }

            if (decoder.isCompleted()) {
                setServerContextAttribute(NhttpConstants.RES_ARRIVAL_TIME,
                        System.currentTimeMillis(), conn);
                ClientConnectionDebug ccd = (ClientConnectionDebug)
                        conn.getContext().getAttribute(CLIENT_CONNECTION_DEBUG);
                if (ccd != null) {
                    ccd.recordResponseCompletionTime();
                }
                
                if (metrics != null) {
                    if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                        MessageContext mc = getMessageContext(conn);
                        metrics.incrementMessagesReceived(mc);
                        metrics.notifyReceivedMessageSize(
                                mc, conn.getMetrics().getReceivedBytesCount());
                        metrics.notifySentMessageSize(mc, conn.getMetrics().getSentBytesCount());
                        metrics.reportResponseCode(mc, response.getStatusLine().getStatusCode());
                    } else {
                        metrics.incrementMessagesReceived();
                        metrics.notifyReceivedMessageSize(
                                conn.getMetrics().getReceivedBytesCount());
                        metrics.notifySentMessageSize(conn.getMetrics().getSentBytesCount());
                    }
                }
                // reset metrics on connection
                conn.getMetrics().reset();
                if (context.getAttribute(NhttpConstants.DISCARD_ON_COMPLETE) != null) {
                    try {
                        // this is a connection we should not re-use
                        connpool.forget(conn);
                        shutdownConnection(conn);
                        context.removeAttribute(RESPONSE_SINK_BUFFER);
                        context.removeAttribute(REQUEST_SOURCE_BUFFER);
                    } catch (Exception ignore) {}
                } else if (!connStrategy.keepAlive(response, context)) {
                    shutdownConnection(conn);
                    context.removeAttribute(RESPONSE_SINK_BUFFER);
                    context.removeAttribute(REQUEST_SOURCE_BUFFER);
                } else {
                    connpool.release(conn);
                }
            }

        } catch (IOException e) {
            if (metrics != null) {
                if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                    metrics.incrementFaultsReceiving(
                        NhttpConstants.SND_IO_ERROR_RECEIVING, getMessageContext(conn));
                } else {
                    metrics.incrementFaultsReceiving();
                }
            }
            handleException("I/O Error at inputReady : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Process ready output (i.e. write request to remote server)
     * 
     * @param conn the connection being processed
     * @param encoder the encoder in use
     */
    public void outputReady(final NHttpClientConnection conn, final ContentEncoder encoder) {
        HttpContext context = conn.getContext();

        ContentOutputBuffer outBuf
                = (ContentOutputBuffer) context.getAttribute(REQUEST_SOURCE_BUFFER);
        if (outBuf == null) return;

        try {
            int bytesWritten = outBuf.produceContent(encoder);
            if (metrics != null) {
                if (bytesWritten > 0) {
                    if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                        metrics.incrementBytesSent(getMessageContext(conn), bytesWritten);
                    } else {
                        metrics.incrementBytesSent(bytesWritten);
                    }
                }
                
                if (encoder.isCompleted()) {
                    if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                        metrics.incrementMessagesSent(getMessageContext(conn));
                    } else {
                        metrics.incrementMessagesSent();
                    }
                }
            }

            if (encoder.isCompleted()) {
                ClientConnectionDebug ccd = (ClientConnectionDebug)
                        context.getAttribute(CLIENT_CONNECTION_DEBUG);
                if (ccd != null) {
                    ccd.recordRequestCompletionTime();
                }
            }

        } catch (IOException e) {
            if (metrics != null) {
                if (metrics.getLevel() == MetricsCollector.LEVEL_FULL) {
                    metrics.incrementFaultsSending(
                        NhttpConstants.SND_IO_ERROR_SENDING, getMessageContext(conn));
                } else {
                    metrics.incrementFaultsSending();
                }
            }
            handleException("I/O Error at outputReady : " + e.getMessage(), e, conn);
        }
    }

    /**
     * Process a response received for the request sent out
     * 
     * @param conn the connection being processed
     */
    public void responseReceived(final NHttpClientConnection conn) {

        HttpContext context = conn.getContext();
        HttpResponse response = conn.getHttpResponse();

        ProxyTunnelHandler tunnelHandler = (ProxyTunnelHandler) context.getAttribute(TUNNEL_HANDLER);
        if (tunnelHandler != null && !tunnelHandler.isCompleted()) {
            context.removeAttribute(TUNNEL_HANDLER);
            tunnelHandler.handleResponse(response, conn);
            if (tunnelHandler.isSuccessful()) {
                log.debug(conn + ": Tunnel established");
                conn.resetInput();
                conn.requestOutput();
                return;
            } else {
                Axis2HttpRequest axis2Req = (Axis2HttpRequest) context.getAttribute(ATTACHMENT_KEY);
                context.setAttribute(AXIS2_HTTP_REQUEST, axis2Req);
                context.setAttribute(OUTGOING_MESSAGE_CONTEXT, axis2Req.getMsgContext());
                ContentOutputBuffer outputBuffer
                        = new SharedOutputBuffer(cfg.getBufferSize(), conn, allocator);
                axis2Req.setOutputBuffer(outputBuffer);
                context.setAttribute(REQUEST_SOURCE_BUFFER, outputBuffer);
                context.setAttribute(NhttpConstants.DISCARD_ON_COMPLETE, Boolean.TRUE);
            }
        }

        setServerContextAttribute(NhttpConstants.RES_HEADER_ARRIVAL_TIME,
                System.currentTimeMillis(), conn);


        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CONTINUE) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Received a 100 Continue response");
            }
            // according to the HTTP 1.1 specification HTTP status 100 continue implies that
            // the response will be followed, and the client should just ignore the 100 Continue
            // and wait for the response
            return;
        }

        ClientConnectionDebug ccd = (ClientConnectionDebug) 
                conn.getContext().getAttribute(CLIENT_CONNECTION_DEBUG);
        if (ccd != null) {
            ccd.recordResponseStartTime(response.getStatusLine().toString());
        }

        // Have we sent out our request fully in the first place? if not, forget about it now..
        Axis2HttpRequest req
                = (Axis2HttpRequest) conn.getContext().getAttribute(AXIS2_HTTP_REQUEST);

        if (req != null) {
            req.setCompleted(true);

            if (log.isDebugEnabled()) {
                log.debug(conn + ": Response Received for Request : " + req);
            }
            if (!req.isSendingCompleted()) {
                req.getMsgContext().setProperty(
                        NhttpConstants.ERROR_CODE, NhttpConstants.SEND_ABORT);
                SharedOutputBuffer outputBuffer = (SharedOutputBuffer)
                        conn.getContext().getAttribute(REQUEST_SOURCE_BUFFER);
                if (outputBuffer != null) {
                    outputBuffer.shutdown();
                }
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Remote server aborted request being sent and replied : " + conn
                        + " for request : " + conn.getContext().getAttribute(
                        NhttpConstants.HTTP_REQ_METHOD));
                }
                context.setAttribute(NhttpConstants.DISCARD_ON_COMPLETE, Boolean.TRUE);
                if (metrics != null) {
                    metrics.incrementFaultsSending(NhttpConstants.SEND_ABORT, req.getMsgContext());
                }
            }
        }


        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_ACCEPTED : {
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Received a 202 Accepted response");
                }

                // sometimes, some http clients sends an "\r\n" as the content body with a
                // HTTP 202 OK.. we will just get it into this temp buffer and ignore it..
                ContentInputBuffer inputBuffer = new SharedInputBuffer(8, conn, allocator);
                context.setAttribute(RESPONSE_SINK_BUFFER, inputBuffer);

                // create a dummy message with an empty SOAP envelope and a property
                // NhttpConstants.SC_ACCEPTED set to Boolean.TRUE to indicate this is a
                // placeholder message for the transport to send a HTTP 202 to the
                // client. Should / would be ignored by any transport other than
                // nhttp. For example, JMS would not send a reply message for one-way
                // operations.
                MessageContext outMsgCtx =
                        (MessageContext) context.getAttribute(OUTGOING_MESSAGE_CONTEXT);
                MessageReceiver mr = outMsgCtx.getAxisOperation().getMessageReceiver();

                // the following check is to support the dual channel invocation. Hence the
                // response will be sent as a new request to the client over a different channel
                // client sends back a 202 Accepted response to synapse and we need to neglect that 
                // 202 Accepted message
                if (!outMsgCtx.isPropertyTrue(NhttpConstants.IGNORE_SC_ACCEPTED)) {

                    try {
                        MessageContext responseMsgCtx = outMsgCtx.getOperationContext().
                                getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
                        if (responseMsgCtx == null ||
                                outMsgCtx.getOptions().isUseSeparateListener() ||
                                outMsgCtx.getOperationContext().isComplete()) {
                            if (responseMsgCtx != null &&
                                    responseMsgCtx.getProperty("synapse.send") == null) {
                                return;
                            }
                        } else if (responseMsgCtx == null ||
                                outMsgCtx.getOptions().isUseSeparateListener()) {
                            // Since we need to notify the SynapseCallback receiver to remove the
                            // call backs registered  we set a custom property
                            setHeaders(context, response, outMsgCtx, responseMsgCtx);
                            outMsgCtx.setProperty(NhttpConstants.HTTP_202_RECEIVED, "true");
                            mr.receive(outMsgCtx);
                            return;
                        }

                        if (responseMsgCtx == null) {
                            return;
                        }

                        setHeaders(context, response, outMsgCtx, responseMsgCtx);
                        responseMsgCtx.setServerSide(true);
                        responseMsgCtx.setDoingREST(outMsgCtx.isDoingREST());
                        responseMsgCtx.setProperty(MessageContext.TRANSPORT_IN,
                                outMsgCtx.getProperty(MessageContext.TRANSPORT_IN));
                        responseMsgCtx.setTransportIn(outMsgCtx.getTransportIn());
                        responseMsgCtx.setTransportOut(outMsgCtx.getTransportOut());

                        responseMsgCtx.setAxisMessage(outMsgCtx.getAxisOperation().
                                getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
                        responseMsgCtx.setOperationContext(outMsgCtx.getOperationContext());
                        responseMsgCtx.setConfigurationContext(outMsgCtx.getConfigurationContext());
                        responseMsgCtx.setTo(null);

                        if (!outMsgCtx.isDoingREST() && !outMsgCtx.isSOAP11()) {
                            responseMsgCtx.setEnvelope(new SOAP12Factory().getDefaultEnvelope());
                        } else {
                            responseMsgCtx.setEnvelope(new SOAP11Factory().getDefaultEnvelope());
                        }
                        responseMsgCtx.setProperty(AddressingConstants.
                                DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
                        responseMsgCtx.setProperty(NhttpConstants.SC_ACCEPTED, Boolean.TRUE);
                        mr.receive(responseMsgCtx);

                    } catch (org.apache.axis2.AxisFault af) {
                        log.debug(conn + ": Unable to report back " +
                                "202 Accepted state to the message receiver");
                    }
                }

                return;
            }

            case HttpStatus.SC_OK : {
                processResponse(conn, context, response);
                return;
            }
            case HttpStatus.SC_INTERNAL_SERVER_ERROR: {
                if (warnOnHttp500(response)) {
                    log.warn(getErrorMessage("Received an internal server error : "
                        + response.getStatusLine().getReasonPhrase(), conn));
                }
                processResponse(conn, context, response);
                return;
            }
            default : {
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": " + getErrorMessage("HTTP status code received : " +
                        response.getStatusLine().getStatusCode() + " :: " +
                        response.getStatusLine().getReasonPhrase(), conn));
                }

                Header contentType = response.getFirstHeader(HTTP.CONTENT_TYPE);
                if (contentType != null) {
                    if ((contentType.getValue().indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                            || contentType.getValue().indexOf(
                            SOAP12Constants.SOAP_12_CONTENT_TYPE) >=0) {

                        if (log.isDebugEnabled()) {
                            log.debug(conn + ": Received an unexpected response with a SOAP payload");
                        }

                    } else if (contentType.getValue().indexOf("html") == -1) {
                        if (log.isDebugEnabled()) {
                            log.debug(conn + ": Received an unexpected response with a POX/REST payload");
                        }
                    } else {
                        log.warn(getErrorMessage("Received an unexpected response - " +
                                "of content type : " + contentType.getValue() +
                                " and status code : " + response.getStatusLine().getStatusCode() +
                                " with reason : " +
                                response.getStatusLine().getReasonPhrase(), conn));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(conn + ": " + getErrorMessage("Received a response - " +
                            "without a content type with status code : " +
                            response.getStatusLine().getStatusCode() + " and reason : " +
                            response.getStatusLine().getReasonPhrase(), conn));
                    }
                }
                
                processResponse(conn, context, response);
            }
        }
    }

    private void setHeaders(HttpContext context, HttpResponse response,
                            MessageContext outMsgCtx, MessageContext responseMsgCtx) {
        Header[] headers = response.getAllHeaders();
        if (headers != null && headers.length > 0) {

            Map<String, String> headerMap
                    = new TreeMap<String, String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });
            String endpointURLPrefix = (String) context.getAttribute(NhttpConstants.ENDPOINT_PREFIX);

            String servicePrefix = (String) outMsgCtx.getProperty(NhttpConstants.SERVICE_PREFIX);
			for (int i = 0; i < headers.length; i++) {
				Header header = headers[i];

				// if this header is already added
				if (headerMap.containsKey(header.getName())) {
					/* this is a multi-value header */
					// generate the key
					String key = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
					// get the old value
					String oldValue = headerMap.get(header.getName());
					// adds additional values to a list in a property of message
					// context
					Map map;
					if (responseMsgCtx.getProperty(key) != null) {
						map = (Map) responseMsgCtx.getProperty(key);
						map.put(header.getName(), oldValue);
					} else {
						map = new MultiValueMap();
						map.put(header.getName(), oldValue);
						// set as a property in message context
						responseMsgCtx.setProperty(key, map);
					}

				}

				if ("Location".equals(header.getName())
						&& endpointURLPrefix != null && servicePrefix != null) {
					// Here, we are changing only the host name and the port of
					// the new URI - value of the Location
					// header.
					// If the new URI is again referring to a resource in the
					// server to which the original request
					// is sent, then replace the hostname and port of the URI
					// with the hostname and port of synapse
					// We are not changing the request url here, only the host
					// name and the port.
					try {
						URI serviceURI = new URI(servicePrefix);
						URI endpointURI = new URI(endpointURLPrefix);
						URI locationURI = new URI(header.getValue());

						if (locationURI.getHost().equalsIgnoreCase(
								endpointURI.getHost())) {
							URI newURI = new URI(locationURI.getScheme(),
									locationURI.getUserInfo(),
									serviceURI.getHost(), serviceURI.getPort(),
									locationURI.getPath(),
									locationURI.getQuery(),
									locationURI.getFragment());
							headerMap.put(header.getName(), newURI.toString());
							responseMsgCtx
									.setProperty(
											NhttpConstants.SERVICE_PREFIX,
											outMsgCtx
													.getProperty(NhttpConstants.SERVICE_PREFIX));
						}
					} catch (URISyntaxException e) {
						log.error(e.getMessage(), e);
					}
				} else {
					headerMap.put(header.getName(), header.getValue());
				}
			}
            responseMsgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
        }
    }

    /**
     * Checks whether the provided 500 response shall be logged as a warning.
     * The behavior can be configured based on the content type of the message via a transport 
     * parameter in axis2.xml named <code>warnOnHTTP500</code>.
     *
     * @param response an http 500 response
     * 
     * @return true, if a warning shall be logged, otherwise false
     */
    private boolean warnOnHttp500(final HttpResponse response) {
        if (warnOnHttp500 == null || warnOnHttp500.length == 0) {
            return true;
        }

        for (String contentType : warnOnHttp500) {
            if (contentType == null || contentType.trim().equals("*")) {
                return true;
            }
        }

        // determine content type of the response message
        Header contentTypeHeader = response.getFirstHeader(CONTENT_TYPE);
        String messageContentType;
        if (contentTypeHeader == null) {
            messageContentType = "none";
        } else {
            messageContentType = contentTypeHeader.getValue();
            if (messageContentType == null || messageContentType.trim().length() == 0) {
                messageContentType = "none";
            }
        }

        // test if one of the content types matches
        for (String contentType : warnOnHttp500) {
            if (messageContentType.startsWith(contentType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Perform processing of the received response though Axis2
     *
     * @param conn HTTP connection to be processed
     * @param context HTTP context associated with the connection
     * @param response HTTP response associated with the connection
     */
    private void processResponse(final NHttpClientConnection conn, HttpContext context,
        HttpResponse response) {

        ContentInputBuffer inputBuffer = null;
        MessageContext outMsgContext = (MessageContext) context.getAttribute(OUTGOING_MESSAGE_CONTEXT);
        String endptPrefix = (String) context.getAttribute(NhttpConstants.ENDPOINT_PREFIX);
        String requestMethod = (String) context.getAttribute(NhttpConstants.HTTP_REQ_METHOD);
        int statusCode = response.getStatusLine().getStatusCode();

        boolean expectEntityBody = false;
        if (!"HEAD".equals(requestMethod) && !"OPTIONS".equals(requestMethod) &&
            statusCode >= HttpStatus.SC_OK
                && statusCode != HttpStatus.SC_NO_CONTENT
                && statusCode != HttpStatus.SC_NOT_MODIFIED
                && statusCode != HttpStatus.SC_RESET_CONTENT) {
            expectEntityBody = true;
        }

        if (expectEntityBody) {
            inputBuffer
                = new SharedInputBuffer(cfg.getBufferSize(), conn, allocator);
            context.setAttribute(RESPONSE_SINK_BUFFER, inputBuffer);

            BasicHttpEntity entity = new BasicHttpEntity();
            if (response.getStatusLine().getProtocolVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                entity.setChunked(true);
            }
            response.setEntity(entity);
            context.setAttribute(ExecutionContext.HTTP_RESPONSE, response);
            
        } else {
            conn.resetInput();
            conn.resetOutput();

            if (context.getAttribute(NhttpConstants.DISCARD_ON_COMPLETE) != null ||
                !connStrategy.keepAlive(response, context)) {
                try {
                    // this is a connection we should not re-use
                    connpool.forget(conn);
                    shutdownConnection(conn);
                    context.removeAttribute(RESPONSE_SINK_BUFFER);
                    context.removeAttribute(REQUEST_SOURCE_BUFFER);
                } catch (Exception ignore) {}
            } else {
                connpool.release(conn);
            }
        }

        workerPool.execute(
            new ClientWorker(cfgCtx,
                inputBuffer == null ? null : new ContentInputStream(inputBuffer),
                response, outMsgContext, endptPrefix));
    }

    public void execute(Runnable task) {
        workerPool.execute(task);        
    }

    /**
     * Shutdown the connection ignoring any IO errors during the process
     * 
     * @param conn the connection to be shutdown
     */
    private void shutdownConnection(final NHttpClientConnection conn) {
        if (conn instanceof HttpInetConnection) {
            HttpInetConnection inetConnection = (HttpInetConnection) conn;
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Connection to remote address : " + inetConnection.getRemoteAddress()
                        + ":" + inetConnection.getRemotePort() + " from local address : "
                        + inetConnection.getLocalAddress() + ":" + inetConnection.getLocalPort() +
                        " is closed!");
            }
            
            if (countConnections) {
                removeConnectionRecord(inetConnection);
            }
        }

        HttpContext context = conn.getContext();
        SharedOutputBuffer outputBuffer = (SharedOutputBuffer)
                context.getAttribute(REQUEST_SOURCE_BUFFER);
        if (outputBuffer != null) {
            outputBuffer.close();
        }
        SharedInputBuffer inputBuffer = (SharedInputBuffer)
            context.getAttribute(RESPONSE_SINK_BUFFER);
        if (inputBuffer != null) {
            inputBuffer.close();
        }
        try {
            conn.shutdown();
        } catch (IOException ignore) {}

        context.removeAttribute(RESPONSE_SINK_BUFFER);
        context.removeAttribute(REQUEST_SOURCE_BUFFER);
        context.removeAttribute(CLIENT_CONNECTION_DEBUG);
        context.removeAttribute(CONNECTION_CREATION_TIME);
    }

    /**
     * Remove a connection record for this host:port pair from active connections records.
     *
     * @param inetConnection connection that need to be removed from the active connections records
     */
    private void removeConnectionRecord(HttpInetConnection inetConnection) {
        AtomicInteger connections = openConnections.get(
                inetConnection.getRemoteAddress().getHostName() + ":"
                        + inetConnection.getRemotePort());
        if (connections == null) {
            connections = openConnections.get(
                    inetConnection.getRemoteAddress().getHostAddress() + ":"
                            + inetConnection.getRemotePort());
        }

        if (connections != null) {
            int no = connections.getAndDecrement();
            lock.lock();
            try {
                if (no == 0) {
                    if (null == openConnections.remove(
                            inetConnection.getRemoteAddress().getHostName()
                            + ":" + inetConnection.getRemotePort())) {

                    } else {
                        openConnections.remove(
                                inetConnection.getRemoteAddress().getHostAddress()
                                + ":" + inetConnection.getRemotePort());
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Record a connection in the active connection records.
     *
     * @param conn connection to be recorded.
     */
    private void recordConnection(NHttpClientConnection conn) {
        if (conn instanceof HttpInetConnection) {
            HttpInetConnection inetConnection = (HttpInetConnection) conn;
            // first we try to get the connection with host_addrss:port key
            AtomicInteger connections = openConnections.get(
                    inetConnection.getRemoteAddress().getHostName() + ":"
                            + inetConnection.getRemotePort());
            // if we fail try to get the connection with ip_address:port key
            if (connections == null) {
                connections = openConnections.get(
                        inetConnection.getRemoteAddress().getHostAddress() + ":"
                                + inetConnection.getRemotePort());
            }

            lock.lock();
            try {
                if (connections == null) {
                    connections = new AtomicInteger();
                    if (inetConnection.getRemoteAddress().getHostName() != null) {
                        openConnections.put(
                                inetConnection.getRemoteAddress().getHostName() + ":"
                                + inetConnection.getRemotePort(), connections);
                    } else {
                        openConnections.put(
                                inetConnection.getRemoteAddress().getHostAddress() + ":"
                                + inetConnection.getRemotePort(), connections);
                    }
                }
            } finally {
                lock.unlock();
            }
            connections.getAndIncrement();
        }
    }

    /**
     * Return the HttpProcessor for requests
     *
     * @return the HttpProcessor that processes requests
     */
    private HttpProcessor getHttpProcessor() {
        BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
        httpProcessor.addInterceptor(new RequestContent());
        httpProcessor.addInterceptor(new RequestTargetHost());
        httpProcessor.addInterceptor(new RequestConnControl());
        httpProcessor.addInterceptor(new RequestUserAgent());
        httpProcessor.addInterceptor(new RequestExpectContinue());
        return httpProcessor;
    }

    public int getActiveCount() {
        return workerPool.getActiveCount();
    }

    public int getQueueSize() {
        return workerPool.getQueueSize();
    }
        
    public void stop() {
        threadingView.destroy();

        try {
            workerPool.shutdown(1000);
        } catch (InterruptedException ignore) {}
    }

    // ----------- utility methods -----------

    private void handleException(String msg, Exception e, NHttpClientConnection conn) {
        if (msg.toLowerCase().indexOf("reset") != -1) {
            log.warn(msg);
        } else {
            log.error(msg, e);
        }
        if (conn != null) {
            shutdownConnection(conn);
        }
    }

    private MessageContext getMessageContext(final NHttpClientConnection conn) {
        HttpContext context = conn.getContext();
        Axis2HttpRequest axis2Req = (Axis2HttpRequest) context.getAttribute(AXIS2_HTTP_REQUEST);
        if (axis2Req != null) {
            return axis2Req.getMsgContext();
        }
        return null;
    }

    private void setServerContextAttribute(String key, Object value, NHttpClientConnection conn) {
        MessageContext msgCtx = getMessageContext(conn);
        if (msgCtx != null) {
            Object outTransport = msgCtx.getProperty(Constants.OUT_TRANSPORT_INFO);
            if (outTransport != null && outTransport instanceof ServerWorker) {
                HttpContext context = ((ServerWorker) outTransport).getConn().getContext();
                context.setAttribute(key, value);
            }
        }
    }
}
