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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.nhttp.util.MessageFormatterDecoratorFactory;
import org.apache.synapse.transport.passthru.config.TargetConfiguration;
import org.apache.synapse.transport.passthru.util.PassThroughTransportUtils;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is a class for representing a request to be sent to a target.
 */
public class TargetRequest {
    /** Configuration of the sender */
    private TargetConfiguration targetConfiguration;
    private HttpRoute route;
    private Pipe pipe = null;
    /** Headers map */
    private Map<String, TreeSet<String>> headers = new HashMap<String, TreeSet<String>>();
    /** URL */
    private URL url;
    /** HTTP Method */
    private String method;
    /** HTTP request created for sending the message */
    private HttpRequest request = null;
    /** Weather chunk encoding should be used */
    private boolean chunk = true;
    /** HTTP version that should be used */
    private ProtocolVersion version = null;
    /** Weather full url is used for the request */
    private boolean fullUrl = false;
    /** Port to be used for the request */
    private int port = 80;
    /** Weather this request has a body */
    private boolean hasEntityBody = true;
    /** Keep alive request */
    private boolean keepAlive = true;
    
    /**
     * Create a target request.
     *
     * @param targetConfiguration the configuration of the sender
     * @param url the url to be used
     * @param method the HTTP method
     * @param hasEntityBody weather request has an entity body
     */
    public TargetRequest(TargetConfiguration targetConfiguration, HttpRoute route, URL url,
                         String method, boolean hasEntityBody) {
        this(targetConfiguration, route, method, url, hasEntityBody);
    }

    public TargetRequest(TargetConfiguration targetConfiguration, HttpRoute route, String method,
                         URL url, boolean hasEntityBody) {
        this.route = route;
        this.method = method;
        this.url = url;
        this.targetConfiguration = targetConfiguration;
        this.hasEntityBody = hasEntityBody;
    }

    public void connect(Pipe pipe) {
        this.pipe = pipe;
    }

    public void start(NHttpClientConnection conn) throws IOException, HttpException {
        if (pipe != null) {
            TargetContext.get(conn).setWriter(pipe);
        }

        String path = fullUrl || (route.getProxyHost() != null && !route.isTunnelled()) ?
                    url.toString() : url.getPath() +
                    (url.getQuery() != null ? "?" + url.getQuery() : "");

        long contentLength = -1;
        String contentLengthHeader = null;
	    if(headers.get(HTTP.CONTENT_LEN) != null && headers.get(HTTP.CONTENT_LEN).size() > 0) {
	        contentLengthHeader = headers.get(HTTP.CONTENT_LEN).first();
	    }
         
        if (contentLengthHeader != null) {
            contentLength = Integer.parseInt(contentLengthHeader);
            headers.remove(HTTP.CONTENT_LEN);
        }
        
        MessageContext requestMsgCtx = TargetContext.get(conn).getRequestMsgCtx();
        
        
        if(requestMsgCtx.getProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH) != null){
        	contentLength = (Long)requestMsgCtx.getProperty(PassThroughConstants.PASSTROUGH_MESSAGE_LENGTH);
        }
        
       
        //fix for  POST_TO_URI
        if(requestMsgCtx.isPropertyTrue(NhttpConstants.POST_TO_URI)){
        	path = url.toString();
        }

     
        
        
        //fix GET request empty body
		if ((("GET").equals(requestMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD)))
				|| (("DELETE").equals(requestMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD)))) {
			hasEntityBody = false;
			MessageFormatter formatter = MessageProcessorSelector.getMessageFormatter(requestMsgCtx);
			OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(requestMsgCtx);
			if (formatter != null && format != null) {
				URL _url = formatter.getTargetAddress(requestMsgCtx, format, url);
				if (_url != null && !_url.toString().isEmpty()) {
					if (requestMsgCtx.getProperty(NhttpConstants.POST_TO_URI) != null
							&& Boolean.TRUE.toString().equals(requestMsgCtx.getProperty(NhttpConstants.POST_TO_URI))) {
						path = _url.toString();
					} else {
						path = _url.getPath()
								+ ((_url.getQuery() != null && !_url.getQuery().isEmpty())
										? ("?" + _url.getQuery())
										: "");
					}

				}
				headers.remove(HTTP.CONTENT_TYPE);
			}
		}        
		// The content type in the transport headers is used to over write the Content-Type returned from the message
        // formatter. This will support <property name="Content-Type" value="foo"/> so that the final Content-Type
        // will be "foo" irrespective of what the message formatter returned.
        // To support this, the property mediator changes Content-Type header when messageType axis2 property is used
        // which is wrong. This is a hack we need to properly fix.        
        
        Object o = requestMsgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
		if (o != null && o instanceof TreeMap) {
			Map _headers = (Map) o;
			String trpContentType = (String) _headers.get(HTTP.CONTENT_TYPE);
			if (trpContentType != null && !trpContentType.equals("")) {
                if (!trpContentType.contains(PassThroughConstants.CONTENT_TYPE_MULTIPART_RELATED)) {
					addHeader(HTTP.CONTENT_TYPE, trpContentType);
				}

			}

		}
        
        
  
                                                            
        if (hasEntityBody) {
            request = new BasicHttpEntityEnclosingRequest(method, path,
                    version != null ? version : HttpVersion.HTTP_1_1);

            BasicHttpEntity entity = new BasicHttpEntity();


            boolean forceContentLength = requestMsgCtx.isPropertyTrue(
                                                                   NhttpConstants.FORCE_HTTP_CONTENT_LENGTH);
            boolean forceContentLengthCopy = requestMsgCtx.isPropertyTrue(
                                                                   PassThroughConstants.COPY_CONTENT_LENGTH_FROM_INCOMING);
                                
            if (forceContentLength) {
                entity.setChunked(false);
                if (forceContentLengthCopy && contentLength > 0) {
                    entity.setContentLength(contentLength);
                } 
            }else{
             if (contentLength != -1) {
                entity.setChunked(false);
                entity.setContentLength(contentLength);
            } else {
                entity.setChunked(chunk);
            }
            }
            
            
            ((BasicHttpEntityEnclosingRequest) request).setEntity(entity);
           
        } else {
            request = new BasicHttpRequest(method, path,
                    version != null ? version : HttpVersion.HTTP_1_1);
        }

        Set<Map.Entry<String, TreeSet<String>>> entries = headers.entrySet();
        for (Map.Entry<String, TreeSet<String>> entry : entries) {
             if (entry.getKey() != null) {
                Iterator<String> i = entry.getValue().iterator();
                 while(i.hasNext()) {
                        request.addHeader(entry.getKey(), i.next());
                 }
             }
         }
        
        //setup wsa action..
        if(request != null){
        	
    		String soapAction = requestMsgCtx.getSoapAction();
            if (soapAction == null) {
                soapAction = requestMsgCtx.getWSAAction();
            }
            if (soapAction == null) {
            	requestMsgCtx.getAxisOperation().getInputAction();
            }

            if (requestMsgCtx.isSOAP11() && soapAction != null &&
                    soapAction.length() > 0) {
                Header existingHeader =
                	request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION);
                if (existingHeader != null) {
                	request.removeHeader(existingHeader);
                }
                MessageFormatter messageFormatter =
                    MessageFormatterDecoratorFactory.createMessageFormatterDecorator(requestMsgCtx);
                request.setHeader(HTTPConstants.HEADER_SOAP_ACTION,
                        messageFormatter.formatSOAPAction(requestMsgCtx, null, soapAction));
                //request.setHeader(HTTPConstants.USER_AGENT,"Synapse-PT-HttpComponents-NIO");
            }
    	}

        request.setParams(new DefaultedHttpParams(request.getParams(),
                targetConfiguration.getHttpParams()));

        //Chucking is not performed for request has "http 1.0" and "GET" http method
       if (!((request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)) ||
                              (("GET").equals(requestMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD))) || (("DELETE").equals(requestMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD))))) {
            this.processChunking(conn, requestMsgCtx);
        }

        if (!keepAlive) {
            request.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
        }
        
       

        // Pre-process HTTP request
        conn.getContext().setAttribute(ExecutionContext.HTTP_CONNECTION, conn);
        conn.getContext().setAttribute(ExecutionContext.HTTP_TARGET_HOST,
                new HttpHost(url.getHost(), port));
        conn.getContext().setAttribute(ExecutionContext.HTTP_REQUEST, request);

        // start the request
        targetConfiguration.getHttpProcessor().process(request, conn.getContext());
        
        if (targetConfiguration.getProxyAuthenticator() != null 
                && route.getProxyHost() != null && !route.isTunnelled()) {
            targetConfiguration.getProxyAuthenticator().authenticatePreemptively(request, conn.getContext());
        }
        
        conn.submitRequest(request);

        if (hasEntityBody) {
            TargetContext.updateState(conn, ProtocolState.REQUEST_HEAD);
        } else {
            TargetContext.updateState(conn, ProtocolState.REQUEST_DONE);
        }
    }
    
    
   
    

	/**
	 * Handles the chuking messages in Passthough context, create a temporary buffer and calculate the message
	 * size before writing to the external buffer, which is required the context of handling DISABLED chunking 
	 * messages
	 * 
	 * @param conn
	 * @param requestMsgCtx
	 * @throws IOException
	 * @throws AxisFault
	 */
	private void processChunking(NHttpClientConnection conn, MessageContext requestMsgCtx) throws IOException,
	                                                                                                        AxisFault {
		String disableChunking = (String) requestMsgCtx.getProperty(PassThroughConstants.DISABLE_CHUNKING);
		String forceHttp10 = (String) requestMsgCtx.getProperty(PassThroughConstants.FORCE_HTTP_1_0);
	    if ("true".equals(disableChunking) || "true".equals(forceHttp10)) {
	    	if (requestMsgCtx.getEnvelope().getBody().getFirstElement() == null) {
				BasicHttpEntity entity = (BasicHttpEntity) ((BasicHttpEntityEnclosingRequest) request).getEntity();    
				try {
					RelayUtils.buildMessage(requestMsgCtx);
					this.hasEntityBody = true;
					Pipe pipe = (Pipe) requestMsgCtx.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
					if (pipe != null) {
						pipe.attachConsumer(conn);
						this.connect(pipe);
						if (Boolean.TRUE.equals(requestMsgCtx.getProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED))) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							MessageFormatter formatter =  MessageProcessorSelector.getMessageFormatter(requestMsgCtx);
							OMOutputFormat format = PassThroughTransportUtils.getOMOutputFormat(requestMsgCtx);
							formatter.writeTo(requestMsgCtx, format, out, false);
							OutputStream _out = pipe.getOutputStream();
							IOUtils.write(out.toByteArray(), _out);
						
							entity.setContentLength(new Long(out.toByteArray().length));
							entity.setChunked(false);
						}
					}
					// pipe.setSerializationComplete(true);
				} catch (XMLStreamException e) {
					 e.printStackTrace();
				
				}
			}

		}  
    }

    /**
     * Consume the data from the pipe and write it to the wire.
     *
     * @param conn the connection to the target
     * @param encoder encoder for writing the message through
     * @throws java.io.IOException if an error occurs
     * @return number of bytes written
     */
    public int write(NHttpClientConnection conn, ContentEncoder encoder) throws IOException {
        int bytes = 0;
        if (pipe != null) {
            bytes = pipe.consume(encoder);
        }

        if (encoder.isCompleted()) {
            targetConfiguration.getMetrics().
                    notifySentMessageSize(conn.getMetrics().getSentBytesCount());

            TargetContext.updateState(conn, ProtocolState.REQUEST_DONE);
        }
        
        return bytes;

    }

    public boolean hasEntityBody() {
        return hasEntityBody;
    }
    
    
    public void setHasEntityBody(boolean hasEntityBody) {
		this.hasEntityBody = hasEntityBody;
	}

	public void addHeader(String name, String value) {
		if (headers.get(name) == null) {
			TreeSet<String> values = new TreeSet<String>();
			values.add(value);
			if (HTTP.CONTENT_TYPE.equalsIgnoreCase(name)) {
				headers.put(HTTP.CONTENT_TYPE, values);
			} else {
				headers.put(name, values);
			}
		} else {
			if (HTTP.CONTENT_TYPE.equalsIgnoreCase(name)) {
				headers.remove(HTTP.CONTENT_TYPE);
				TreeSet<String> values = new TreeSet<String>();
				values.add(value);
				headers.put(HTTP.CONTENT_TYPE, values);
			} else {
				TreeSet<String> values = headers.get(name);
				values.add(value);
			}
		}
	}

    public String getMethod() {
        return method;
    }

    public void setChunk(boolean chunk) {
        this.chunk = chunk;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setFullUrl(boolean fullUrl) {
        this.fullUrl = fullUrl;
    }

    public void setVersion(ProtocolVersion version) {
        this.version = version;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

	public HttpRequest getRequest() {
		return request;
	}
    
    
}
