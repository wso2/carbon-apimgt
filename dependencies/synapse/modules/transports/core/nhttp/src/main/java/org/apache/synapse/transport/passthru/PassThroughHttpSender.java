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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.threads.NativeThreadFactory;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.http.conn.ClientConnFactory;
import org.apache.synapse.transport.http.conn.ProxyAuthenticator;
import org.apache.synapse.transport.http.conn.ProxyConfig;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.config.ClientConnFactoryBuilder;
import org.apache.synapse.transport.nhttp.config.ProxyConfigBuilder;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.apache.synapse.transport.passthru.connections.TargetConnections;
import org.apache.synapse.transport.passthru.jmx.MBeanRegistrar;
import org.apache.synapse.transport.passthru.jmx.PassThroughTransportMetricsCollector;
import org.apache.synapse.transport.passthru.jmx.TransportView;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;
import org.apache.synapse.transport.passthru.util.SourceResponseFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * PassThroughHttpSender for Synapse based on HttpCore and NIO extensions
 */
public class PassThroughHttpSender extends AbstractHandler implements TransportSender {

    protected Log log;

    /** IOReactor used to create connections and manage them */
    private DefaultConnectingIOReactor ioReactor;
    /** Protocol handler */
    private TargetHandler handler;
    /** I/O dispatcher */
    private IOEventDispatch ioEventDispatch;
    /** The connection factory */
    private ClientConnFactory connFactory;
    
    /** Delivery agent used for delivering the messages to the servers */
    private DeliveryAgent deliveryAgent;

    /** The protocol scheme of the sender */
    private Scheme scheme;
    /** The configuration of the sender */
    private TargetConfiguration targetConfiguration;

    /** Proxy config */
    private ProxyConfig proxyConfig;
    
    /** state of the sender */
    private volatile int state = BaseConstants.STOPPED;

    private String namePrefix;

    public PassThroughHttpSender() {
        log = LogFactory.getLog(this.getClass().getName());
    }

    protected Scheme getScheme() {
        return new Scheme("http", 80, false);
    }
    
    protected ClientConnFactoryBuilder initConnFactoryBuilder(
            final TransportOutDescription transportOut) throws AxisFault {
        return new ClientConnFactoryBuilder(transportOut);
    }
    
    public void init(ConfigurationContext configurationContext,
                     TransportOutDescription transportOutDescription) throws AxisFault {
        log.info("Initializing Pass-through HTTP/S Sender...");

        namePrefix = transportOutDescription.getName().toUpperCase(Locale.US);
        scheme = getScheme();
        
        WorkerPool workerPool = null;
        Object obj = configurationContext.getProperty(
                PassThroughConstants.PASS_THROUGH_TRANSPORT_WORKER_POOL);
        if (obj != null) {
            workerPool = (WorkerPool) obj;                                   
        }

        PassThroughTransportMetricsCollector metrics = new PassThroughTransportMetricsCollector(
            false, scheme.getName());
        TransportView view = new TransportView(null, this, metrics, null);
        MBeanRegistrar.getInstance().registerMBean(view, "Transport",
                 "passthru-" + namePrefix.toLowerCase() + "-sender");
        
        proxyConfig = new ProxyConfigBuilder().parse(transportOutDescription).build();
        if (log.isInfoEnabled() && proxyConfig.getProxy() != null) {
            log.info("HTTP Sender using Proxy " + proxyConfig.getProxy() + " bypassing " + 
                proxyConfig.getProxyBypass());
        }
        
        targetConfiguration = new TargetConfiguration(configurationContext,
                transportOutDescription, workerPool, metrics, 
                proxyConfig.getCreds() != null ? new ProxyAuthenticator(proxyConfig.getCreds()) : null);
        targetConfiguration.build();
        configurationContext.setProperty(PassThroughConstants.PASS_THROUGH_TRANSPORT_WORKER_POOL,
                targetConfiguration.getWorkerPool());
        
        ClientConnFactoryBuilder connFactoryBuilder = initConnFactoryBuilder(transportOutDescription);
        connFactory = connFactoryBuilder.createConnFactory(targetConfiguration.getHttpParams());
        
        try {
            String prefix = namePrefix + "-Sender I/O dispatcher";

            ioReactor = new DefaultConnectingIOReactor(
                            targetConfiguration.getIOReactorConfig(),
                            new NativeThreadFactory(new ThreadGroup(prefix + " Thread Group"), prefix));

            ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {

                public boolean handle(IOException ioException) {
                    log.warn("System may be unstable: " + namePrefix +
                            " ConnectingIOReactor encountered a checked exception : " +
                            ioException.getMessage(), ioException);
                    return true;
                }

                public boolean handle(RuntimeException runtimeException) {
                    log.warn("System may be unstable: " + namePrefix +
                            " ConnectingIOReactor encountered a runtime exception : "
                            + runtimeException.getMessage(), runtimeException);
                    return true;
                }
            });
        } catch (IOReactorException e) {
            handleException("Error starting " + namePrefix + " ConnectingIOReactor", e);
        }

        ConnectCallback connectCallback = new ConnectCallback();
        // manage target connections
        TargetConnections targetConnections =
                new TargetConnections(ioReactor, targetConfiguration, connectCallback);
        targetConfiguration.setConnections(targetConnections);

        // create the delivery agent to hand over messages
        deliveryAgent = new DeliveryAgent(targetConfiguration, targetConnections, proxyConfig);
        // we need to set the delivery agent
        connectCallback.setDeliveryAgent(deliveryAgent);        

        handler = new TargetHandler(deliveryAgent, connFactory, targetConfiguration);
        ioEventDispatch = new ClientIODispatch(handler, connFactory);
        
        // start the sender in a separate thread
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    ioReactor.execute(ioEventDispatch);
                } catch (Exception ex) {
                   log.fatal("Exception encountered in the " + namePrefix + " Sender. " +
                            "No more connections will be initiated by this transport", ex);
                }
                log.info(namePrefix + " Sender shutdown");
            }
        }, "PassThrough" + namePrefix + "Sender");
        t.start();

        state = BaseConstants.STARTED;

        log.info("Pass-through " + namePrefix + " Sender started...");
    }

    public void cleanup(org.apache.axis2.context.MessageContext messageContext) throws AxisFault {

    }

    public void stop() {
        try {
            ioReactor.shutdown();
        } catch (IOException e) {
            log.error("Error shutting down the PassThroughHttpSender", e);
        }
    }


    public InvocationResponse invoke(org.apache.axis2.context.MessageContext msgContext) throws AxisFault {
        // remove unwanted HTTP headers (if any from the current message)
    	
        PassThroughTransportUtils.removeUnwantedHeaders(msgContext,
                targetConfiguration.isPreserveServerHeader(),
                targetConfiguration.isPreserveUserAgentHeader());

        if (AddressingHelper.isReplyRedirected(msgContext)
                && !msgContext.getReplyTo().hasNoneAddress()) {

            msgContext.setProperty(PassThroughConstants.IGNORE_SC_ACCEPTED, Constants.VALUE_TRUE);
        }

        EndpointReference epr = PassThroughTransportUtils.getDestinationEPR(msgContext);
        if (epr != null) {
            if (!epr.hasNoneAddress()) {
                if (msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE) == null) {
                    Pipe pipe = new Pipe(targetConfiguration.getBufferFactory().getBuffer(),
                            "Test", targetConfiguration);
                    msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, pipe);
                    msgContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
                }
                deliveryAgent.submit(msgContext, epr);
                sendRequestContent(msgContext);
            } else {
                handleException("Cannot send message to " + AddressingConstants.Final.WSA_NONE_URI);
            }
        } else {
            if (msgContext.getProperty(Constants.OUT_TRANSPORT_INFO) != null) {
                if (msgContext.getProperty(Constants.OUT_TRANSPORT_INFO) instanceof ServerWorker) {
                    try {
                        submitResponse(msgContext);
                    } catch (Exception e) {
                        handleException("Failed to submit the response", e);
                    }
                }else {
                    //handleException("No valid destination EPR to send message");
                	//should be able to handle sendUsingOutputStream  Ref NHTTP_NIO
                	sendUsingOutputStream(msgContext);
                }
            } else {
                handleException("No valid destination EPR to send message");
            }
        }

        if (msgContext.getOperationContext() != null) {
            msgContext.getOperationContext().setProperty(
                Constants.RESPONSE_WRITTEN, Constants.VALUE_TRUE);
        }

        return InvocationResponse.CONTINUE;
    }
    
    
    private void sendUsingOutputStream(MessageContext msgContext) throws AxisFault {

        OMOutputFormat format = NhttpUtil.getOMOutputFormat(msgContext);
        MessageFormatter messageFormatter =
                MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
        OutputStream out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_OUT);

        if (msgContext.isServerSide()) {
            OutTransportInfo transportInfo =
                (OutTransportInfo) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO);

            if (transportInfo != null) {
                transportInfo.setContentType(
                messageFormatter.getContentType(msgContext, format, msgContext.getSoapAction()));
            } else {
                throw new AxisFault(Constants.OUT_TRANSPORT_INFO + " has not been set");
            }
        }

        try {
            messageFormatter.writeTo(msgContext, format, out, false);
            out.close();
        } catch (IOException e) {
            handleException("IO Error sending response message", e);
        }
    }


	private void sendRequestContent(final MessageContext msgContext) throws AxisFault {
		
		//NOTE:this a special case where, when the backend service expects content-length but,there is no desire that the message
		//should be build, if FORCE_HTTP_CONTENT_LENGTH and COPY_CONTENT_LENGTH_FROM_INCOMING, we assume that the content
		//comming from the client side has not been changed
		boolean forceContentLength = msgContext.isPropertyTrue(NhttpConstants.FORCE_HTTP_CONTENT_LENGTH);
	    boolean forceContentLengthCopy = msgContext.isPropertyTrue(PassThroughConstants.COPY_CONTENT_LENGTH_FROM_INCOMING);
	            
		if (forceContentLength && forceContentLengthCopy && msgContext.getProperty(PassThroughConstants.ORGINAL_CONTEN_LENGTH) != null) {
			 msgContext.setProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH, Long.parseLong((String)msgContext.getProperty(PassThroughConstants.ORGINAL_CONTEN_LENGTH) ));
		}
		
		if (Boolean.TRUE.equals(msgContext.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED))) {
			synchronized (msgContext) {
				while (!Boolean.TRUE.equals(msgContext.getProperty(PassThroughConstants.WAIT_BUILDER_IN_STREAM_COMPLETE)) &&
				       !Boolean.TRUE.equals(msgContext.getProperty("PASSTHRU_CONNECT_ERROR"))) {
					try {
						msgContext.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			if (Boolean.TRUE.equals(msgContext.getProperty("PASSTHRU_CONNECT_ERROR"))) {
				return;
			}
			
			
			OutputStream out = (OutputStream) msgContext.getProperty(PassThroughConstants.BUILDER_OUTPUT_STREAM);
			if (out != null) {
				String disableChunking = (String) msgContext.getProperty(PassThroughConstants.DISABLE_CHUNKING);
				String forceHttp10 = (String) msgContext.getProperty(PassThroughConstants.FORCE_HTTP_1_0);
				Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
				
				if("true".equals(disableChunking) || "true".equals(forceHttp10) ){
					ByteArrayOutputStream _out = new ByteArrayOutputStream();
					MessageFormatter formatter =  MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
					OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);
                    if(null == msgContext.getProperty(PassThroughConstants.FORMATTER_PRESERVE) || msgContext.isPropertyTrue(PassThroughConstants.FORMATTER_PRESERVE)) {
                        formatter.writeTo(msgContext, format, _out, true);
                    } else {
                        formatter.writeTo(msgContext, format, _out, false);
                    }

					try {
	                    long messageSize =setStreamAsTempData(formatter,msgContext,format);
	                    msgContext.setProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH,messageSize);
	                    formatter.writeTo(msgContext, format, out, false);
                    } catch (IOException e) {
	                    // TODO Auto-generated catch block
                    	 handleException("IO while building message", e);
                    }
                    //if HTTP MEHOD = GET we need to write down the HEADER information to the wire and need
                    //to ignore any entity enclosed methods available.
                    if (("GET").equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD)) || ("DELETE").equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD))) {
                        pipe.setSerializationCompleteWithoutData(true);
                    } else {
                        pipe.setSerializationComplete(true);
                    }

				}else {
					//if HTTP MEHOD = GET we need to write down the HEADER information to the wire and need
					//to ignore any entity enclosed methods available.
                    if (("GET").equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD)) || ("DELETE").equals(msgContext.getProperty(Constants.Configuration.HTTP_METHOD))) {
						pipe.setSerializationCompleteWithoutData(true);
						return;
					}

					if ((disableChunking == null || !"true".equals(disableChunking)) ||
					    (forceHttp10 == null || !"true".equals(forceHttp10))) {
						MessageFormatter formatter = MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
						OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);
						formatter.writeTo(msgContext, format, out, false);
					}

                    if ((msgContext.getProperty(PassThroughConstants.REST_GET_DELETE_INVOKE) != null &&
                         (Boolean) msgContext.getProperty(PassThroughConstants.REST_GET_DELETE_INVOKE))) {
                        pipe.setSerializationCompleteWithoutData(true);
                    } else if ((msgContext.getProperty(PassThroughConstants.FORCE_POST_PUT_NOBODY) != null &&
                                (Boolean) msgContext.getProperty(PassThroughConstants.FORCE_POST_PUT_NOBODY))) {
                        pipe.setSerializationCompleteWithoutData(true);
                    } else {
                        pipe.setSerializationComplete(true);
                    }

                }
			}
		}
	}
	
	
	/**
     * Write the stream to a temporary storage and calculate the content length
     *
     * @param entity HTTPEntity
     * @throws IOException if an exception occurred while writing data
     */
    private long setStreamAsTempData(MessageFormatter messageFormatter,MessageContext msgContext,OMOutputFormat format) throws IOException {
        OverflowBlob serialized = new OverflowBlob(256, 4096, "http-nio_", ".dat");
        OutputStream out = serialized.getOutputStream();
        try {
            messageFormatter.writeTo(msgContext, format, out, true);
        } finally {
            out.close();
        }
       // msgContext.setProperty(NhttpConstants.SERIALIZED_BYTES, serialized);
       return serialized.getLength();
    }

    public void submitResponse(MessageContext msgContext)
            throws IOException, HttpException {
        SourceConfiguration sourceConfiguration = (SourceConfiguration) msgContext.getProperty(
                        PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION);

        NHttpServerConnection conn = (NHttpServerConnection) msgContext.getProperty(
                PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION);
        if (conn == null) {
            ServerWorker serverWorker = (ServerWorker) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO);
            if (serverWorker != null) {
                MessageContext requestContext = serverWorker.getRequestContext();
                conn = (NHttpServerConnection) requestContext.getProperty(
                        PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION);
                sourceConfiguration = (SourceConfiguration) requestContext.getProperty(
                        PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION);
            } else {
                throw new IllegalStateException("Unable to correlate the response to a request");
            }
        }

        SourceRequest sourceRequest = SourceContext.getRequest(conn);

        SourceResponse sourceResponse = SourceResponseFactory.create(msgContext,
                sourceRequest, sourceConfiguration);
        sourceResponse.checkResponseChunkDisable(msgContext);

        SourceContext.setResponse(conn, sourceResponse);

        Boolean noEntityBody = (Boolean) msgContext.getProperty(PassThroughConstants.NO_ENTITY_BODY);
        Pipe pipe = (Pipe) msgContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        if ((noEntityBody == null || !noEntityBody) || pipe != null) {
            if (pipe == null) {
                pipe = new Pipe(sourceConfiguration.getBufferFactory().getBuffer(),
                        "Test", sourceConfiguration);
                msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, pipe);
                msgContext.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            }

            pipe.attachConsumer(conn);
            sourceResponse.connect(pipe);
        }

        Integer errorCode = (Integer) msgContext.getProperty(PassThroughConstants.ERROR_CODE);
        if (errorCode != null) {
            sourceResponse.setStatus(HttpStatus.SC_BAD_GATEWAY);
            SourceContext.get(conn).setShutDown(true);
        }

        ProtocolState state = SourceContext.getState(conn);
        if (state != null && state.compareTo(ProtocolState.REQUEST_DONE) <= 0) {
            // start sending the response if we
            if (msgContext.isPropertyTrue(PassThroughConstants.MESSAGE_BUILDER_INVOKED) && pipe != null) {
                OutputStream out = pipe.getOutputStream();
                /*if (msgContext.isPropertyTrue(NhttpConstants.SC_ACCEPTED)) {
                    out.write(new byte[0]);
                }else {*/
                MessageFormatter formatter = MessageFormatterDecoratorFactory.createMessageFormatterDecorator(msgContext);
                OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(msgContext);

                Object contentTypeInMsgCtx =
                        msgContext.getProperty(org.apache.axis2.Constants.Configuration.CONTENT_TYPE);
                boolean isContentTypeSetFromMsgCtx = false;

                // If ContentType header is set in the axis2 message context, use it.
                if (contentTypeInMsgCtx != null) {
                   String contentTypeValueInMsgCtx = contentTypeInMsgCtx.toString();
                   // Skip multipart/related as it should be taken from formatter.
                   if (!contentTypeValueInMsgCtx.contains(
                           PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)) {
                       sourceResponse.addHeader(HTTP.CONTENT_TYPE, contentTypeValueInMsgCtx);
                       isContentTypeSetFromMsgCtx = true;
                   }
                }

                // If ContentType is not set from msg context, get the formatter ContentType
                if (!isContentTypeSetFromMsgCtx) {
                    sourceResponse.removeHeader(HTTP.CONTENT_TYPE);
                    sourceResponse.addHeader(HTTP.CONTENT_TYPE,
                                             formatter.getContentType(
                                                     msgContext, format, msgContext.getSoapAction()));
                }

                formatter.writeTo(msgContext, format, out, false);
                /*}*/
                pipe.setSerializationComplete(true);
                out.close();
            }
            
            if(noEntityBody != null && Boolean.TRUE == noEntityBody && pipe != null){
                OutputStream out = pipe.getOutputStream();
            	out.write(new byte[0]);
            	pipe.setRawSerializationComplete(true);
                out.close();
            }
            conn.requestOutput();
        } else {
            // nothing much to do as we have started the response already
            if (errorCode != null) {
                if (log.isDebugEnabled()) {
                    log.warn("A Source connection is closed because of an " +
                            "error in target: " + conn);
                }
            } else {
                log.debug("A Source Connection is closed, because source handler " +
                          "is already in the process of writing a response while " +
                          "another response is submitted: " + conn);
            }

            SourceContext.updateState(conn, ProtocolState.CLOSED);
            sourceConfiguration.getSourceConnections().shutDownConnection(conn, true);
        }
    }

    public void pause() throws AxisFault {
        if (state != BaseConstants.STARTED) {
            return;
        }
        state = BaseConstants.PAUSED;
        log.info(namePrefix + " Sender Paused");
    }

    public void resume() throws AxisFault {
        if (state != BaseConstants.PAUSED) {
            return;
        }
        state = BaseConstants.STARTED;
        log.info(namePrefix + " Sender Resumed");
    }

    public void maintenanceShutdown(long millis) throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        try {
            long start = System.currentTimeMillis();
            ioReactor.shutdown(millis);
            state = BaseConstants.STOPPED;
            log.info("Sender shutdown in : " + (System.currentTimeMillis() - start) / 1000 + "s");
        } catch (IOException e) {
            handleException("Error shutting down the IOReactor for maintenence", e);
        }
    }

    private void handleException(String s, Exception e) throws AxisFault {
        log.error(s, e);
        throw new AxisFault(s, e);
    }

    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

}
