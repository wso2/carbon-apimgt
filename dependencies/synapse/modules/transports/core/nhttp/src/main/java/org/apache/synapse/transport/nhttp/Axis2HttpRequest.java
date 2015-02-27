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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.entity.ContentOutputStream;
import org.apache.http.nio.util.ContentOutputBuffer;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;

/**
 * Represents an outgoing Axis2 HTTP/s request. It holds the EPR of the destination, the
 * Axis2 MessageContext to be sent, an HttpHost object which captures information about the
 * destination, and a Pipe used to write the message stream to the destination
 */
public class Axis2HttpRequest {

    private static final Log log = LogFactory.getLog(Axis2HttpRequest.class);

    /**
     * the EPR of the destination
     */
    private EndpointReference epr = null;
    /**
     * The [socket | connect] timeout
     */
    private int timeout = -1;
    /**
     * the message context being sent
     */
    private MessageContext msgContext = null;
    /**
     * The Axis2 MessageFormatter that will ensure proper serialization as per Axis2 semantics
     */
    MessageFormatter messageFormatter = null;
    /**
     * The OM Output format holder
     */
    OMOutputFormat format = null;
    private ContentOutputBuffer outputBuffer = null;
    /**
     * ready to begin streaming?
     */
    private volatile boolean readyToStream = false;
    /**
     * The sending of this request has fully completed
     */
    private volatile boolean sendingCompleted = false;
    /**
     * for request complete checking - request complete means the request has been fully sent
     * and the response it fully received
     */
    private volatile boolean completed = false;
    /**
     * The URL prefix of the endpoint (to be used for Location header re-writing in the response)
     */
    private String endpointURLPrefix = null;

    /**
     * the HttpHost that contains the HTTP connection information
     */
    private HttpRoute route;
    /**
     * weather chunking is enabled or not
     */
    private boolean chunked = true;

    public Axis2HttpRequest(EndpointReference epr, HttpRoute route, MessageContext msgContext) {
        this.epr = epr;
        this.route = route;
        this.msgContext = msgContext;
        this.format = NhttpUtil.getOMOutputFormat(msgContext);
        this.messageFormatter =
                MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
        this.chunked = !msgContext.isPropertyTrue(NhttpConstants.DISABLE_CHUNKING);
    }

    public void setReadyToStream(boolean readyToStream) {
        this.readyToStream = readyToStream;
    }

    public void setOutputBuffer(ContentOutputBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
    }

    public void clear() {
        this.epr = null;
        this.route = null;
        this.msgContext = null;
        this.format = null;
        this.messageFormatter = null;
        this.outputBuffer = null;
    }

    public EndpointReference getEpr() {
        return epr;
    }

    public HttpRoute getRoute() {
        return route;
    }

    public MessageContext getMsgContext() {
        return msgContext;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getEndpointURLPrefix() {
        return endpointURLPrefix;
    }

    public void setEndpointURLPrefix(String endpointURLPrefix) {
        this.endpointURLPrefix = endpointURLPrefix;
    }

    /**
     * Create and return a new HttpPost request to the destination EPR
     *
     * @return the HttpRequest to be sent out
     * @throws IOException in error retrieving the <code>HttpRequest</code>
     */
    public HttpRequest getRequest() throws IOException, HttpException {
        String httpMethod = (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if (httpMethod == null) {
            httpMethod = "POST";
        }
        endpointURLPrefix = (String) msgContext.getProperty(NhttpConstants.ENDPOINT_PREFIX);

        boolean forceHTTP10 = msgContext.isPropertyTrue(NhttpConstants.FORCE_HTTP_1_0);
        HttpVersion httpver = forceHTTP10 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1;

        HttpRequest httpRequest;

        try {
            if ("POST".equals(httpMethod) || "PUT".equals(httpMethod)) {

                URI uri = rewriteRequestURI(new URI(epr.getAddress()));
                BasicHttpEntityEnclosingRequest requestWithEntity = new BasicHttpEntityEnclosingRequest(
                        httpMethod, uri.toASCIIString(), httpver);

                BasicHttpEntity entity = new BasicHttpEntity();

                if (forceHTTP10) {
                    setStreamAsTempData(entity);
                } else {
                    entity.setChunked(chunked);
                    if (!chunked) {
                        setStreamAsTempData(entity);
                    }
                }
                requestWithEntity.setEntity(entity);
                requestWithEntity.setHeader(
                        HTTP.CONTENT_TYPE,
                        messageFormatter.getContentType(msgContext, format, msgContext.getSoapAction()));
                httpRequest = requestWithEntity;

            } else if ("GET".equals(httpMethod) || "DELETE".equals(httpMethod)) {

                URL url = messageFormatter.getTargetAddress(
                        msgContext, format, new URL(epr.getAddress()));

                URI uri = rewriteRequestURI(url.toURI());
                httpRequest = new BasicHttpRequest(httpMethod, uri.toASCIIString(), httpver);

                httpRequest.setHeader(HTTP.CONTENT_TYPE, messageFormatter.getContentType(
                        msgContext, format, msgContext.getSoapAction()));

            } else {
                URI uri = rewriteRequestURI(new URI(epr.getAddress()));
                httpRequest = new BasicHttpRequest(httpMethod, uri.toASCIIString(), httpver);
            }

        } catch (URISyntaxException ex) {
            throw new HttpException(ex.getMessage(), ex);
        }

        // set any transport headers
        Object o = msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (o != null && o instanceof Map) {
            Map headers = (Map) o;
            for (Object header : headers.keySet()) {
                Object value = headers.get(header);
                if (header instanceof String && value != null && value instanceof String) {
                    if (!HTTPConstants.HEADER_HOST.equalsIgnoreCase((String) header)) {
                        httpRequest.setHeader((String) header, (String) value);
                        
                        String excessProp = NhttpConstants.EXCESS_TRANSPORT_HEADERS;
                        
                        Map map = (Map) msgContext.getProperty(excessProp);
						if (map != null && map.get(header) != null) {
							log.debug("Number of excess values for " + header
									+ " header is : "
									+ ((Collection) (map.get(header))).size());

							for (Iterator iterator = map.keySet().iterator(); iterator
									.hasNext();) {
								String key = (String) iterator.next();

								for (String excessVal : (Collection<String>) map
										.get(key)) {
									httpRequest.addHeader((String) header,
											(String) excessVal);
								}

							}
						}
                        
                    } else {
                        if (msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER) != null) {
                            httpRequest.setHeader((String) header,
                                    (String) msgContext.getProperty(NhttpConstants.REQUEST_HOST_HEADER));
                        }
                    }

                }
            }
        }

        // if the message is SOAP 11 (for which a SOAPAction is *required*), and
        // the msg context has a SOAPAction or a WSA-Action (give pref to SOAPAction)
        // use that over any transport header that may be available
        String soapAction = msgContext.getSoapAction();
        if (soapAction == null) {
            soapAction = msgContext.getWSAAction();
        }
        if (soapAction == null) {
            msgContext.getAxisOperation().getInputAction();
        }

        if (msgContext.isSOAP11() && soapAction != null &&
                soapAction.length() >= 0) {
            Header existingHeader =
                    httpRequest.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION);
            if (existingHeader != null) {
                httpRequest.removeHeader(existingHeader);
            }
            httpRequest.setHeader(HTTPConstants.HEADER_SOAP_ACTION,
                    messageFormatter.formatSOAPAction(msgContext, null, soapAction));
        }

        if (NHttpConfiguration.getInstance().isKeepAliveDisabled() ||
                msgContext.isPropertyTrue(NhttpConstants.NO_KEEPALIVE)) {
            httpRequest.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        }

        return httpRequest;
    }

    private URI rewriteRequestURI(final URI uri) throws URISyntaxException {
        if (msgContext.isPropertyTrue(NhttpConstants.POST_TO_URI) ||
                (route.getProxyHost() != null && !route.isTunnelled())) {
            // Must be absolute
            return uri;
        } else {
            // Make relative
            return URIUtils.rewriteURI(uri, null, true);
        }
    }


    /**
     * Start streaming the message into the Pipe, so that the contents could be read off the source
     * channel returned by getSourceChannel()
     *
     * @throws AxisFault on error
     */
    public void streamMessageContents() throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Start streaming outgoing http request : [Message ID : " + msgContext.getMessageID() + "]");
            if (log.isTraceEnabled()) {
                log.trace("Message [Request Message ID : " + msgContext.getMessageID() + "] " +
                        "[Request Message Payload : [ " + msgContext.getEnvelope() + "]");
            }
        }

        NHttpConfiguration cfg = NHttpConfiguration.getInstance();
        int senderTimeout = cfg.getProperty(NhttpConstants.SO_TIMEOUT_SENDER, 60000);
        long startTime = System.currentTimeMillis();

        synchronized (this) {
            while (!readyToStream && !completed) {
                try {
                    this.wait(senderTimeout + 10000);
                    if ((startTime + senderTimeout + 5000) < System.currentTimeMillis()) {
                        handleException("Thread " + Thread.currentThread().getName() +
                                " is blocked longer than the send timeout when trying to send the message :" +
                                msgContext.getMessageID() + ". Releasing thread",
                                new AxisFault("Sender thread was not notified within send timeout"));
                    }
                } catch (InterruptedException ignore) {
                }
            }
        }

        if (!completed) {
            OutputStream out = new ContentOutputStream(outputBuffer);
            try {
                if (msgContext.isPropertyTrue(NhttpConstants.FORCE_HTTP_1_0)) {
                    writeMessageFromTempData(out);
                } else {
                    if (chunked) {
                        messageFormatter.writeTo(msgContext, format, out, false);
                    } else {
                        writeMessageFromTempData(out);
                    }
                }
            } catch (Exception e) {
                Throwable t = e.getCause();
                if (t != null && t.getCause() != null && t.getCause() instanceof ClosedChannelException) {
                    if (log.isDebugEnabled()) {
                        log.debug("Ignore closed channel exception, as the " +
                                "SessionRequestCallback handles this exception");
                    }
                } else {
                    Integer errorCode = msgContext == null ? null :
                            (Integer) msgContext.getProperty(NhttpConstants.ERROR_CODE);
                    if (errorCode == null || errorCode == NhttpConstants.SEND_ABORT) {
                        if (log.isDebugEnabled()) {
                            log.debug("Remote server aborted request being sent, and responded");
                        }
                    } else {
                        if (e instanceof AxisFault) {
                            throw (AxisFault) e;
                        } else {
                            handleException("Error streaming message context", e);
                        }
                    }
                }
            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    handleException("Error closing outgoing message stream", e);
                }
                setSendingCompleted(true);
            }
        }
    }

    /**
     * Write the stream to a temporary storage and calculate the content length
     *
     * @param entity HTTPEntity
     * @throws IOException if an exception occurred while writing data
     */
    private void setStreamAsTempData(BasicHttpEntity entity) throws IOException {
        OverflowBlob serialized = new OverflowBlob(256, 4096, "http-nio_", ".dat");
        OutputStream out = serialized.getOutputStream();
        try {
            messageFormatter.writeTo(msgContext, format, out, true);
        } finally {
            out.close();
        }
        msgContext.setProperty(NhttpConstants.SERIALIZED_BYTES, serialized);
        entity.setContentLength(serialized.getLength());
    }

    /**
     * Take the data from temporary storage and write it to the output stream
     *
     * @param out output stream
     * @throws IOException if an exception occurred while writing data
     */
    private void writeMessageFromTempData(OutputStream out) throws IOException {
        OverflowBlob serialized =
                (OverflowBlob) msgContext.getProperty(NhttpConstants.SERIALIZED_BYTES);
        try {
            serialized.writeTo(out);
        } finally {
            serialized.release();
        }
    }

    // -------------- utility methods -------------
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public boolean isSendingCompleted() {
        return sendingCompleted;
    }

    public void setSendingCompleted(boolean sendingCompleted) {
        this.sendingCompleted = sendingCompleted;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Axis2Request [Message ID : ").append(msgContext.getMessageID()).append("] ");
        sb.append("[Status Completed : ").append(isCompleted() ? "true" : "false").append("] ");
        sb.append("[Status SendingCompleted : ").append(
                isSendingCompleted() ? "true" : "false").append("]");
        return sb.toString();
    }
}
