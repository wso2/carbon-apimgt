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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
import org.apache.http.nio.entity.ContentOutputStream;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleOutputBuffer;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.jmx.LatencyView;
import org.apache.synapse.transport.passthru.jmx.PassThroughTransportMetricsCollector;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is the class where transport interacts with the client. This class
 * receives events for a particular connection. These events give information
 * about the message and its various states.
 */
public class SourceHandler implements NHttpServerEventHandler {
    private static Log log = LogFactory.getLog(SourceHandler.class);

    private final SourceConfiguration sourceConfiguration;

    private PassThroughTransportMetricsCollector metrics = null;
    
    private LatencyView latencyView = null;
    
    private LatencyView s2sLatencyView = null;

    public SourceHandler(SourceConfiguration sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
        this.metrics = sourceConfiguration.getMetrics();
        
		try {
		    Scheme scheme = sourceConfiguration.getScheme();
			if (!scheme.isSSL()) {
				this.latencyView = new LatencyView(scheme.isSSL());
			} else {
				this.s2sLatencyView = new LatencyView(scheme.isSSL());
			}
		} catch (AxisFault e) {
			log.error(e.getMessage(), e);
		}
    }

    public void connected(NHttpServerConnection conn) {
        // we have to have these two operations in order
        sourceConfiguration.getSourceConnections().addConnection(conn);
        SourceContext.create(conn, ProtocolState.REQUEST_READY, sourceConfiguration);

        metrics.connected();
    }

    public void requestReceived(NHttpServerConnection conn) {
        try {
        	
        	HttpContext _context = conn.getContext();
        	_context.setAttribute(PassThroughConstants.REQ_ARRIVAL_TIME, System.currentTimeMillis());
        	 
            if (!SourceContext.assertState(conn, ProtocolState.REQUEST_READY) && !SourceContext.assertState(conn, ProtocolState.WSDL_RESPONSE_DONE)) {
                handleInvalidState(conn, "Request received");
                return;
            }
            // we have received a message over this connection. So we must inform the pool
            sourceConfiguration.getSourceConnections().useConnection(conn);

            // at this point we have read the HTTP Headers
            SourceContext.updateState(conn, ProtocolState.REQUEST_HEAD);

            SourceRequest request = new SourceRequest(
                    sourceConfiguration, conn.getHttpRequest(), conn);

            SourceContext.setRequest(conn, request);

            request.start(conn);

            metrics.incrementMessagesReceived();
            
            /******/
            String method = request.getRequest() != null ? request.getRequest().getRequestLine().getMethod().toUpperCase():"";
            OutputStream os = null;
            if ("GET".equals(method) || "HEAD".equals(method)) {
				HttpContext context = request.getConnection().getContext();
				ContentOutputBuffer outputBuffer = new SimpleOutputBuffer(8192,	new HeapByteBufferAllocator());
				// ContentOutputBuffer outputBuffer
				// = new SharedOutputBuffer(8192, conn, new
				// HeapByteBufferAllocator());
				context.setAttribute("synapse.response-source-buffer",outputBuffer);
				os = new ContentOutputStream(outputBuffer);
			} 

            sourceConfiguration.getWorkerPool().execute(
                    new ServerWorker(request, sourceConfiguration,os));
        } catch (HttpException e) {
            log.error(e.getMessage(), e);

            informReaderError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        } catch (IOException e) {
            logIOException(conn, e);

            informReaderError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    public void inputReady(NHttpServerConnection conn,
                           ContentDecoder decoder) {
        try {
            ProtocolState protocolState = SourceContext.getState(conn);

            if (protocolState != ProtocolState.REQUEST_HEAD
                    && protocolState != ProtocolState.REQUEST_BODY) {
                handleInvalidState(conn, "Request message body data received");
                return;
            }

            SourceContext.updateState(conn, ProtocolState.REQUEST_BODY);

            SourceRequest request = SourceContext.getRequest(conn);

            int readBytes = request.read(conn, decoder);
            if (readBytes > 0) {
                metrics.incrementBytesReceived(readBytes);
            }
        } catch (IOException e) {
            logIOException(conn, e);

            informReaderError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    public void responseReady(NHttpServerConnection conn) {
        try {
            ProtocolState protocolState = SourceContext.getState(conn);
            if (protocolState.compareTo(ProtocolState.REQUEST_DONE) < 0) {                
                return;
            }

            if (protocolState.compareTo(ProtocolState.CLOSING) >= 0) {
                return;
            }

            if (protocolState != ProtocolState.REQUEST_DONE) {
                handleInvalidState(conn, "Writing a response");
                return;
            }

            // because the duplex nature of http core we can reach hear without a actual response
            SourceResponse response = SourceContext.getResponse(conn);
            if (response != null) {
                response.start(conn);

                metrics.incrementMessagesSent();
                if (!response.hasEntity()) {
                   // Update stats as outputReady will not be triggered for no entity responses
                   updateStatistics(conn);
                }
            }
        } catch (IOException e) {
            logIOException(conn, e);

            informWriterError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSING);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        } catch (HttpException e) {
            log.error(e.getMessage(), e);

            informWriterError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSING);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    public void outputReady(NHttpServerConnection conn,
                            ContentEncoder encoder) {
        try {
            ProtocolState protocolState = SourceContext.getState(conn);
            
            //special case to handle WSDLs
            if(protocolState == ProtocolState.WSDL_RESPONSE_DONE){
            	// we need to shut down if the shutdown flag is set
            	 HttpContext context = conn.getContext();
            	 ContentOutputBuffer outBuf = (ContentOutputBuffer) context.getAttribute(
                         "synapse.response-source-buffer");
            	  int bytesWritten = outBuf.produceContent(encoder);
                  if (metrics != null && bytesWritten > 0) {
                      metrics.incrementBytesSent(bytesWritten);
                  }
                
                  conn.requestInput();
                  if(outBuf instanceof SimpleOutputBuffer && !((SimpleOutputBuffer)outBuf).hasData()){
                	  sourceConfiguration.getSourceConnections().releaseConnection(conn);
                  }
                  
            	return;
            }
            
                        
            if (protocolState != ProtocolState.RESPONSE_HEAD
                    && protocolState != ProtocolState.RESPONSE_BODY) {
                log.warn("Illegal incoming connection state: "
                        + protocolState + " . Possibly two send backs " +
                        "are happening for the same request");

                handleInvalidState(conn, "Trying to write response body");
                return;
            }

            SourceContext.updateState(conn, ProtocolState.RESPONSE_BODY);

            SourceResponse response = SourceContext.getResponse(conn);

            int bytesSent = response.write(conn, encoder);
            
			if (encoder.isCompleted()) {
                updateStatistics(conn);
			}
            
            metrics.incrementBytesSent(bytesSent);
        } catch (IOException e) {
            logIOException(conn, e);

            informWriterError(conn);

            SourceContext.updateState(conn, ProtocolState.CLOSING);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    private void updateStatistics(NHttpServerConnection conn) {
        HttpContext context = conn.getContext();
        if (context.getAttribute(PassThroughConstants.REQ_ARRIVAL_TIME) != null &&
            context.getAttribute(PassThroughConstants.REQ_DEPARTURE_TIME) != null &&
            context.getAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME) != null) {

            if (latencyView != null) {
                latencyView.notifyTimes((Long) context.getAttribute(PassThroughConstants.REQ_ARRIVAL_TIME),
                                        (Long) context.getAttribute(PassThroughConstants.REQ_DEPARTURE_TIME),
                                        (Long) context.getAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME),
                                        System.currentTimeMillis());
            } else if (s2sLatencyView != null) {
                s2sLatencyView.notifyTimes((Long) context.getAttribute(PassThroughConstants.REQ_ARRIVAL_TIME),
                                           (Long) context.getAttribute(PassThroughConstants.REQ_DEPARTURE_TIME),
                                           (Long) context.getAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME),
                                           System.currentTimeMillis());
            }
        }

        context.removeAttribute(PassThroughConstants.REQ_ARRIVAL_TIME);
        context.removeAttribute(PassThroughConstants.REQ_DEPARTURE_TIME);
        context.removeAttribute(PassThroughConstants.RES_HEADER_ARRIVAL_TIME);
    }

    private void logIOException(NHttpServerConnection conn, IOException e) {
        // this check feels like crazy! But weird things happened, when load testing.
        if (e == null) {
            return;
        }
        if (e instanceof ConnectionClosedException || (e.getMessage() != null && (
                e.getMessage().toLowerCase().contains("connection reset by peer") ||
                e.getMessage().toLowerCase().contains("forcibly closed")))) {
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

            metrics.incrementFaultsReceiving();
        } else {
            log.error("Unexpected I/O error: " + e.getClass().getName(), e);

            metrics.incrementFaultsReceiving();
        }
    }

    public void timeout(NHttpServerConnection conn) {
        ProtocolState state = SourceContext.getState(conn);

        if (state == ProtocolState.REQUEST_READY || state == ProtocolState.RESPONSE_DONE) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Keep-Alive connection was time out: " + conn);
            }
        } else if (state == ProtocolState.REQUEST_BODY ||
                state == ProtocolState.REQUEST_HEAD) {

            metrics.incrementTimeoutsReceiving();

            informReaderError(conn);
            log.warn("Connection time out while reading the request: " + conn);
        } else if (state == ProtocolState.RESPONSE_BODY ||
                state == ProtocolState.RESPONSE_HEAD) {
            informWriterError(conn);
            log.warn("Connection time out while writing the response: " + conn);
        } else if (state == ProtocolState.REQUEST_DONE){
            log.warn("Connection time out after request is read: " + conn);
        }

        SourceContext.updateState(conn, ProtocolState.CLOSED);
       
        sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
    }

    public void closed(NHttpServerConnection conn) {
        ProtocolState state = SourceContext.getState(conn);
        boolean isFault = false;
        if (state == ProtocolState.REQUEST_READY || state == ProtocolState.RESPONSE_DONE) {
            if (log.isDebugEnabled()) {
                log.debug(conn + ": Keep-Alive connection was closed: " + conn);
            }
        } else if (state == ProtocolState.REQUEST_BODY ||
                state == ProtocolState.REQUEST_HEAD) {
        	isFault = true;
            informReaderError(conn);
            log.warn("Connection closed while reading the request: " + conn);
        } else if (state == ProtocolState.RESPONSE_BODY ||
                state == ProtocolState.RESPONSE_HEAD) {
        	isFault = true;
            informWriterError(conn);
            log.warn("Connection closed while writing the response: " + conn);
        } else if (state == ProtocolState.REQUEST_DONE) {
        	isFault = true;
            log.warn("Connection closed by the client after request is read: " + conn);
        }

        metrics.disconnected();

        SourceContext.updateState(conn, ProtocolState.CLOSED);
        sourceConfiguration.getSourceConnections().shutDownConnection(conn, isFault);
    }

    public void endOfInput(NHttpServerConnection conn) throws IOException {
        conn.close();
    }

    public void exception(NHttpServerConnection conn, Exception ex) {
        if (ex instanceof IOException) {
            logIOException(conn, (IOException) ex);

            metrics.incrementFaultsReceiving();

            ProtocolState state = SourceContext.getState(conn);
            if (state == ProtocolState.REQUEST_BODY ||
                    state == ProtocolState.REQUEST_HEAD) {
                informReaderError(conn);
            } else if (state == ProtocolState.RESPONSE_BODY ||
                    state == ProtocolState.RESPONSE_HEAD) {
                informWriterError(conn);
            } else if (state == ProtocolState.REQUEST_DONE) {
                informWriterError(conn);
            } else if (state == ProtocolState.RESPONSE_DONE) {
                informWriterError(conn);
            }
            
            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        } else if (ex instanceof HttpException) {
            try {
                if (conn.isResponseSubmitted()) {
                    sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
                    return;
                }
                HttpContext httpContext = conn.getContext();

                HttpResponse response = new BasicHttpResponse(
                        HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "Bad request");
                response.setParams(
                        new DefaultedHttpParams(sourceConfiguration.getHttpParams(),
                                response.getParams()));
                response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);

                // Pre-process HTTP request
                httpContext.setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
                httpContext.setAttribute(ExecutionContext.HTTP_REQUEST, null);
                httpContext.setAttribute(ExecutionContext.HTTP_RESPONSE, response);

                sourceConfiguration.getHttpProcessor().process(response, httpContext);

                conn.submitResponse(response);            
                SourceContext.updateState(conn, ProtocolState.CLOSED);
                conn.close();
            } catch (Exception ex1) {
                log.error(ex.getMessage(), ex);
                SourceContext.updateState(conn, ProtocolState.CLOSED);
                sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
            }
        } else {
            log.error("Unexpected error: " + ex.getMessage(), ex);
            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    private void handleInvalidState(NHttpServerConnection conn, String action) {
        log.warn(action + " while the handler is in an inconsistent state " +
                SourceContext.getState(conn));
        SourceContext.updateState(conn, ProtocolState.CLOSED);
        sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
    }

    private void informReaderError(NHttpServerConnection conn) {
        Pipe reader = SourceContext.get(conn).getReader();

        metrics.incrementFaultsReceiving();

        if (reader != null) {
            reader.producerError();
        }
    }

    private void informWriterError(NHttpServerConnection conn) {
        Pipe writer = SourceContext.get(conn).getWriter();

        metrics.incrementFaultsSending();

        if (writer != null) {
            writer.consumerError();
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
            sourceConfiguration.getHttpProcessor().process(response, conn.getContext());
            conn.submitResponse(response);
        } catch (HttpException e) {
            handleException("Unexpected HTTP protocol error : " + e.getMessage(), e, conn);
        } catch (IOException e) {
            handleException("IO error submiting response : " + e.getMessage(), e, conn);
        }
    }
    
    
    // ----------- utility methods -----------

    private void handleException(String msg, Exception e, NHttpServerConnection conn) {
        log.error(msg, e);
        if (conn != null) {
            //shutdownConnection(conn);
        }
    }
    
    
    
}
