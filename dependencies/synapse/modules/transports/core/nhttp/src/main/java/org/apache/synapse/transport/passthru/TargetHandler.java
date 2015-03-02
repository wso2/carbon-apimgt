/*
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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.synapse.transport.http.conn.ClientConnFactory;
import org.apache.synapse.transport.http.conn.ProxyTunnelHandler;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.apache.synapse.transport.passthru.connections.HostConnections;
import org.apache.synapse.transport.passthru.jmx.PassThroughTransportMetricsCollector;

import java.io.IOException;


/**
 * This class is handling events from the transport -- > client.
 */
public class TargetHandler implements NHttpClientEventHandler {
    private static Log log = LogFactory.getLog(TargetHandler.class);

    /** Delivery agent */
    private final DeliveryAgent deliveryAgent;

    /** Connection factory */
    private final ClientConnFactory connFactory;
    
    /** Configuration used by the sender */
    private final TargetConfiguration targetConfiguration;

    /** Error handler for injecting faults */
    private final TargetErrorHandler targetErrorHandler;

    private PassThroughTransportMetricsCollector metrics = null;

    public TargetHandler(DeliveryAgent deliveryAgent,
                         ClientConnFactory connFactory,
                         TargetConfiguration configuration) {
        this.deliveryAgent = deliveryAgent;
        this.connFactory = connFactory;
        this.targetConfiguration = configuration;
        this.targetErrorHandler = new TargetErrorHandler(targetConfiguration);
        this.metrics = targetConfiguration.getMetrics();
    }

    public void connected(NHttpClientConnection conn, Object o) {
        assert o instanceof HostConnections : "Attachment should be a HostConnections";

        HostConnections pool = (HostConnections) o;
        conn.getContext().setAttribute(PassThroughConstants.CONNECTION_POOL, pool);
        HttpRoute route = pool.getRoute();
          
        // create the connection information and set it to request ready
        TargetContext.create(conn, ProtocolState.REQUEST_READY, targetConfiguration);

        // notify the pool about the new connection
        targetConfiguration.getConnections().addConnection(conn);

        // notify about the new connection
        deliveryAgent.connected(pool.getRoute());
        
        HttpContext context = conn.getContext();
        context.setAttribute(PassThroughConstants.REQ_DEPARTURE_TIME, System.currentTimeMillis());

        metrics.connected();
        
        if (route.isTunnelled()) {
            // Requires a proxy tunnel
            ProxyTunnelHandler tunnelHandler = new ProxyTunnelHandler(route, connFactory);
            context.setAttribute(PassThroughConstants.TUNNEL_HANDLER, tunnelHandler);
        }
    }

    public void requestReady(NHttpClientConnection conn) {
        HttpContext context = conn.getContext();
        ProtocolState connState = null;
        try {
            
            connState = TargetContext.getState(conn);

            if (connState == ProtocolState.REQUEST_DONE || connState == ProtocolState.RESPONSE_BODY) {
                return;
            }

            if (connState != ProtocolState.REQUEST_READY) {
                handleInvalidState(conn, "Request not started");
                return;
            }

            ProxyTunnelHandler tunnelHandler = (ProxyTunnelHandler) context.getAttribute(PassThroughConstants.TUNNEL_HANDLER);
            if (tunnelHandler != null && !tunnelHandler.isCompleted()) {
                if (!tunnelHandler.isRequested()) {
                    HttpRequest request = tunnelHandler.generateRequest(context);
                    if (targetConfiguration.getProxyAuthenticator() != null) {
                        targetConfiguration.getProxyAuthenticator().authenticatePreemptively(request, context);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(conn + ": Sending CONNECT request to " + tunnelHandler.getProxy());
                    }
                    conn.submitRequest(request);
                    tunnelHandler.setRequested();
                }
                return;
            }
            
            TargetRequest request = TargetContext.getRequest(conn);
            if (request != null) {
                request.start(conn);
                targetConfiguration.getMetrics().incrementMessagesSent();
            }
            context.setAttribute(PassThroughConstants.REQ_DEPARTURE_TIME, System.currentTimeMillis());
        } catch (IOException e) {
            logIOException(conn, e);
            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);

            MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.SND_IO_ERROR,
                        "Error in Sender",
                        null,
                        connState);
            }
        } catch (HttpException e) {
            log.error(e.getMessage(), e);
            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);

            MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.SND_HTTP_ERROR,
                        "Error in Sender",
                        null,
                        connState);
            }
        }
    }

    public void outputReady(NHttpClientConnection conn, ContentEncoder encoder) {
        ProtocolState connState = null;
        try {
            connState = TargetContext.getState(conn);
            if (connState != ProtocolState.REQUEST_HEAD &&
                    connState != ProtocolState.REQUEST_DONE) {
                handleInvalidState(conn, "Writing message body");
                return;
            }

            TargetRequest request = TargetContext.getRequest(conn);
            if (request.hasEntityBody()) {
                int bytesWritten = request.write(conn, encoder);
                metrics.incrementBytesSent(bytesWritten);
            }
        } catch (IOException ex) {
            logIOException(conn, ex);
            TargetContext.updateState(conn, ProtocolState.CLOSING);
            targetConfiguration.getConnections().shutdownConnection(conn, true);

            informWriterError(conn);

            MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.SND_HTTP_ERROR,
                        "Error in Sender",
                        null,
                        connState);
            }
        } catch (Exception e) {
            log.error("Error occurred while writing data to the target", e);
            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);

            informWriterError(conn);

            MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.SND_HTTP_ERROR,
                        "Error in Sender",
                        null,
                        connState);
            }
        }
    }

    public void responseReceived(NHttpClientConnection conn) {
        HttpContext context = conn.getContext();
        HttpResponse response = conn.getHttpResponse();
        ProtocolState connState;
        try {
            String method = null;
            ProxyTunnelHandler tunnelHandler = (ProxyTunnelHandler) context.getAttribute(PassThroughConstants.TUNNEL_HANDLER);
            if (tunnelHandler != null && !tunnelHandler.isCompleted()) {
                method = "CONNECT";
                context.removeAttribute(PassThroughConstants.TUNNEL_HANDLER);
                tunnelHandler.handleResponse(response, conn);
                if (tunnelHandler.isSuccessful()) {
                    log.debug(conn + ": Tunnel established");
                    conn.resetInput();
                    conn.requestOutput();
                    return;
                } else {
                    TargetContext.updateState(conn, ProtocolState.REQUEST_DONE);
                }
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < HttpStatus.SC_OK) {
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Received a 100 Continue response");
                }
                // Ignore 1xx response
                return;
            }
            
        	context.setAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME, System.currentTimeMillis());
            connState = TargetContext.getState(conn);
            if (connState != ProtocolState.REQUEST_DONE) {
                StatusLine errorStatus = response.getStatusLine();
                /* We might receive a 404 or a similar type, even before we write the request body. */
                if (errorStatus != null &&
                        errorStatus.getStatusCode() >= HttpStatus.SC_NOT_FOUND) {
                    TargetContext.updateState(conn, ProtocolState.REQUEST_DONE);
                    conn.resetOutput();
                    if (log.isDebugEnabled()) {
                        log.debug(conn + ": Received response with status code : " +
                                response.getStatusLine().getStatusCode() + " in invalid state : " + connState.name());
                    }
                } else {
                    handleInvalidState(conn, "Receiving response");
                    return;
                }
            }
            TargetRequest targetRequest = TargetContext.getRequest(conn);

            if (targetRequest != null) {
                method = targetRequest.getMethod();
            }
            if (method == null) {
                method = "POST";
            }
            boolean canResponseHaveBody =
                    isResponseHaveBodyExpected(method, response);
            if (!canResponseHaveBody) {
                if (log.isDebugEnabled()) {
                    log.debug(conn + ": Received no-content response " +
                            response.getStatusLine().getStatusCode());
                }
                conn.resetInput();
            }
            TargetResponse targetResponse = new TargetResponse(
                    targetConfiguration, response, conn, canResponseHaveBody);
            TargetContext.setResponse(conn, targetResponse);
            targetResponse.start(conn);

            MessageContext requestMsgContext = TargetContext.get(conn).getRequestMsgCtx();
            if (statusCode == HttpStatus.SC_ACCEPTED && handle202(requestMsgContext)) {
                return;
            }
                       
            targetConfiguration.getWorkerPool().execute(
                    new ClientWorker(targetConfiguration.getConfigurationContext(),
                            requestMsgContext, targetResponse));

            targetConfiguration.getMetrics().incrementMessagesReceived();
            
			NHttpServerConnection sourceConn =
			                                   (NHttpServerConnection) requestMsgContext.getProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION);
			if (sourceConn != null) {
				sourceConn.getContext().setAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME,
				                                     conn.getContext()
				                                         .getAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME));
				sourceConn.getContext().setAttribute(PassThroughConstants.REQ_DEPARTURE_TIME,
				                                     conn.getContext()
				                                         .getAttribute(PassThroughConstants.REQ_DEPARTURE_TIME));

			}
                                                                                      
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            informReaderError(conn);

            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);
        }
    }

    private boolean handle202(MessageContext requestMsgContext) throws AxisFault {
        if (requestMsgContext.isPropertyTrue(NhttpConstants.IGNORE_SC_ACCEPTED)) {
            // We should not further process this 202 response - Ignore it
            return true;
        }

        MessageReceiver mr = requestMsgContext.getAxisOperation().getMessageReceiver();
        MessageContext responseMsgCtx = requestMsgContext.getOperationContext().
                        getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        if (responseMsgCtx == null || requestMsgContext.getOptions().isUseSeparateListener()) {
            // Most probably a response from a dual channel invocation
            // Inject directly into the SynapseCallbackReceiver
            requestMsgContext.setProperty(NhttpConstants.HTTP_202_RECEIVED, "true");
            mr.receive(requestMsgContext);
            return true;
        }

        return false;
    }

    public void inputReady(NHttpClientConnection conn, ContentDecoder decoder) {
        ProtocolState connState;
        try {
            connState = TargetContext.getState(conn);
            if (connState.compareTo(ProtocolState.RESPONSE_HEAD) < 0) {
                return;
            }
            if (connState != ProtocolState.RESPONSE_HEAD &&
                    connState != ProtocolState.RESPONSE_BODY) {
                handleInvalidState(conn, "Response received");
                return;
            }

            TargetContext.updateState(conn, ProtocolState.RESPONSE_BODY);

            TargetResponse response = TargetContext.getResponse(conn);

			if (response != null) {
				int responseRead = response.read(conn, decoder);
				metrics.incrementBytesReceived(responseRead);
			}
        } catch (IOException e) {
            logIOException(conn, e);
            informReaderError(conn);

            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            informReaderError(conn);

            TargetContext.updateState(conn, ProtocolState.CLOSED);
            targetConfiguration.getConnections().shutdownConnection(conn, true);
        }
    }

    public void closed(NHttpClientConnection conn) {
        ProtocolState state = TargetContext.getState(conn);
        
        boolean isFault = false;

        if (state == ProtocolState.REQUEST_READY || state == ProtocolState.RESPONSE_DONE) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Keep-Alive Connection closed");
            }
        } else if (state == ProtocolState.REQUEST_HEAD || state == ProtocolState.REQUEST_BODY) {
            informWriterError(conn);
            log.warn("Connection closed by target host while sending the request");
            isFault = true;
        } else if (state == ProtocolState.RESPONSE_HEAD || state == ProtocolState.RESPONSE_BODY) {
            informReaderError(conn);
            log.warn("Connection closed by target host while receiving the response");
            isFault = true;
        } else if (state == ProtocolState.REQUEST_DONE) {
            informWriterError(conn);
            log.warn("Connection closed by target host before receiving the response");
            isFault = true;
        }

        if (isFault) {
            MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.CONNECTION_CLOSED,
                        "Error in Sender",
                        null,
                        state);
            }
        }

        metrics.disconnected();

        TargetContext.updateState(conn, ProtocolState.CLOSED);
        targetConfiguration.getConnections().shutdownConnection(conn, isFault);

    }

    private void logIOException(NHttpClientConnection conn, IOException e) {
        String message = getErrorMessage("I/O error : " + e.getMessage(), conn);

        if (e instanceof ConnectionClosedException || (e.getMessage() != null &&
                e.getMessage().toLowerCase().contains("connection reset by peer") ||
                e.getMessage().toLowerCase().contains("forcibly closed"))) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": I/O error (Probably the keep-alive connection " +
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
        } else {
            log.error(message, e);
        }
    }

    public void timeout(NHttpClientConnection conn) {
        ProtocolState state = TargetContext.getState(conn);

        String message = getErrorMessage("Connection timeout", conn);
        if (log.isDebugEnabled()) {
            log.debug(conn + ": " + message);
        }

        if (state != null &&
                (state == ProtocolState.REQUEST_READY || state == ProtocolState.RESPONSE_DONE)) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": " + getErrorMessage("Keep-alive connection timed out", conn));
            }
        } else if (state != null ) {
            if (state == ProtocolState.REQUEST_BODY) {
                metrics.incrementTimeoutsSending();
                informWriterError(conn);
            }

            if (state == ProtocolState.RESPONSE_BODY || state == ProtocolState.REQUEST_HEAD) {
                metrics.incrementTimeoutsReceiving();
                informReaderError(conn);
            }

            if (state.compareTo(ProtocolState.REQUEST_DONE) <= 0) {
                MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();

                log.warn(conn + ": Connection time out while in state: " + state);
                if (requestMsgCtx != null) {
                    targetErrorHandler.handleError(requestMsgCtx,
                            ErrorCodes.CONNECTION_TIMEOUT,
                            "Error in Sender",
                            null,
                            state);
                }
            }
        }

        TargetContext.updateState(conn, ProtocolState.CLOSED);
        targetConfiguration.getConnections().shutdownConnection(conn, true);
    }

    private boolean isResponseHaveBodyExpected(
            final String method, final HttpResponse response) {

        if ("HEAD".equalsIgnoreCase(method)) {
            return false;
        }

        int status = response.getStatusLine().getStatusCode();
        return status >= HttpStatus.SC_OK
            && status != HttpStatus.SC_NO_CONTENT
            && status != HttpStatus.SC_NOT_MODIFIED
            && status != HttpStatus.SC_RESET_CONTENT;
    }

    /**
     * Include remote host and port information to an error message
     *
     * @param message the initial message
     * @param conn    the connection encountering the error
     * @return the updated error message
     */
    private String getErrorMessage(String message, NHttpClientConnection conn) {
        if (conn != null && conn instanceof DefaultNHttpClientConnection) {
            DefaultNHttpClientConnection c = ((DefaultNHttpClientConnection) conn);

            if (c.getRemoteAddress() != null) {
                return message + " For : " + c.getRemoteAddress().getHostAddress() + ":" +
                        c.getRemotePort();
            }
        }
        return message;
    }

    private void handleInvalidState(NHttpClientConnection conn, String action) {
        ProtocolState state = TargetContext.getState(conn);

        if (log.isWarnEnabled()) {
            log.warn(conn + ": " + action + " while the handler is in an inconsistent state " +
                TargetContext.getState(conn));
        }
        MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
        TargetContext.updateState(conn, ProtocolState.CLOSED);
        targetConfiguration.getConnections().shutdownConnection(conn, true);
        if (requestMsgCtx != null) {
            targetErrorHandler.handleError(requestMsgCtx,
                    ErrorCodes.SND_INVALID_STATE,
                    "Error in Sender",
                    null,
                    state);
        }
    }

    private void informReaderError(NHttpClientConnection conn) {
        Pipe reader = TargetContext.get(conn).getReader();

        metrics.incrementFaultsReceiving();

        if (reader != null) {
            reader.producerError();
        }
    }

    private void informWriterError(NHttpClientConnection conn) {
        Pipe writer = TargetContext.get(conn).getWriter();

        metrics.incrementFaultsReceiving();

        if (writer != null) {
            writer.consumerError();
        }
    }

    public void endOfInput(NHttpClientConnection conn) throws IOException {
        conn.close();
    }
    
    public void exception(NHttpClientConnection conn, Exception ex) {
        ProtocolState state = TargetContext.getState(conn);
        MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
        if (ex instanceof IOException) {

            logIOException(conn, (IOException) ex);

            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.SND_IO_ERROR,
                        "Error in Sender",
                        ex,
                        state);
            }

            TargetContext.updateState(conn, ProtocolState.CLOSING);
        } else if (ex instanceof HttpException) {
            String message = getErrorMessage("HTTP protocol violation : " + ex.getMessage(), conn);
            log.error(message, ex);

            if (requestMsgCtx != null) {
                targetErrorHandler.handleError(requestMsgCtx,
                        ErrorCodes.PROTOCOL_VIOLATION,
                        "Error in Sender",
                        null,
                        state);
            }

            TargetContext.updateState(conn, ProtocolState.CLOSED);
        } else {
            if(null != ex && null != ex.getMessage()) {
                log.error("Unexpected error: " + ex.getMessage(), ex);
            } else {
                log.error("Unexpected error.");
            }
            TargetContext.updateState(conn, ProtocolState.CLOSED);
        }
        targetConfiguration.getConnections().shutdownConnection(conn, true);
    }

}
