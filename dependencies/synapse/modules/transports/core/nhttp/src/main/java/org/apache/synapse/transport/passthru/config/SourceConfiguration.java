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

package org.apache.synapse.transport.passthru.config;

import java.net.UnknownHostException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.HttpGetRequestProcessor;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.connections.SourceConnections;
import org.apache.synapse.transport.passthru.jmx.PassThroughTransportMetricsCollector;

/**
 * This class stores configurations specific to the Listeners
 */
public class SourceConfiguration extends BaseConfiguration {

    private Log log = LogFactory.getLog(SourceConfiguration.class);

    /** This is used to process HTTP responses */
    private HttpProcessor httpProcessor = null;

    /** Response factory used for creating HTTP Responses */
    private HttpResponseFactory responseFactory = null;

    /** port of the listener */
    private int port = 8280;

    /** Object to manage the source connections */
    private SourceConnections sourceConnections = null;

    private TransportInDescription inDescription;
    private Scheme scheme;
    private String host;

    /** The EPR prefix for services available over this transport */
    private String serviceEPRPrefix;
    /** The EPR prefix for services with custom URI available over this transport */
    private String customEPRPrefix;
    
    /** WSDL processor for Get requests*/
    private HttpGetRequestProcessor httpGetRequestProcessor = null;


    public SourceConfiguration(ConfigurationContext configurationContext,
                               TransportInDescription description,
                               Scheme scheme,
                               WorkerPool pool,
                               PassThroughTransportMetricsCollector metrics) {
        super(configurationContext, description, pool, metrics);
        this.inDescription = description;
        this.scheme = scheme;
        httpProcessor = new ImmutableHttpProcessor(
                new HttpResponseInterceptor[]{
                        new ResponseDate(),
                        new ResponseServer(),
                        new ResponseContent(),
                        new ResponseConnControl()});

        responseFactory = new DefaultHttpResponseFactory();

        sourceConnections = new SourceConnections();
    }

    public void build() throws AxisFault {
        super.build();

        port = ParamUtils.getRequiredParamInt(parameters, "port");

        Parameter hostParameter = inDescription.getParameter(TransportListener.HOST_ADDRESS);
        if (hostParameter != null) {
            host = ((String) hostParameter.getValue()).trim();
        } else {
            try {
                host = java.net.InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("Unable to lookup local host name, using 'localhost'");
            }
        }

        Parameter param = inDescription.getParameter(PassThroughConstants.WSDL_EPR_PREFIX);
        if (param != null) {
            serviceEPRPrefix = getServiceEPRPrefix(configurationContext, (String) param.getValue());
            customEPRPrefix = (String) param.getValue();
        } else {
            serviceEPRPrefix = getServiceEPRPrefix(configurationContext, host, port);
            customEPRPrefix = scheme.getName() + "://" + host + ":" +
                    (port == scheme.getDefaultPort() ? "" : port) + "/";
        }
        
        // create http Get processor
        param = inDescription.getParameter(NhttpConstants.HTTP_GET_PROCESSOR);
        if (param != null && param.getValue() != null) {
            httpGetRequestProcessor = createHttpGetProcessor(param.getValue().toString());
            if (httpGetRequestProcessor == null) {
                handleException("Cannot create HttpGetRequestProcessor");
            }
        } 
    }

    public HttpParams getHttpParams() {
        return httpParams;
    }

    public IOReactorConfig getIOReactorConfig() {
        return ioReactorConfig;
    }

    public HttpProcessor getHttpProcessor() {
        return httpProcessor;
    }

    public HttpResponseFactory getResponseFactory() {
        return responseFactory;
    }
    
    public String getHostname() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public SourceConnections getSourceConnections() {
        return sourceConnections;
    }

    public TransportInDescription getInDescription() {
        return inDescription;
    }

    public Scheme getScheme() {
        return scheme;
    }

    public String getServiceEPRPrefix() {
        return serviceEPRPrefix;
    }

    public String getCustomEPRPrefix() {
        return customEPRPrefix;
    }
    
	public HttpGetRequestProcessor getHttpGetRequestProcessor() {
		return httpGetRequestProcessor;
	}

	/**
     * Return the EPR prefix for services made available over this transport
     * @param cfgCtx configuration context to retrieve the service context path
     * @param wsdlEPRPrefix specified wsdlPrefix
     *
     * @return wsdlEPRPrefix for the listener
     */
    protected String getServiceEPRPrefix(ConfigurationContext cfgCtx, String wsdlEPRPrefix) {
        return wsdlEPRPrefix +
            (!cfgCtx.getServiceContextPath().startsWith("/") ? "/" : "") +
            cfgCtx.getServiceContextPath() +
            (!cfgCtx.getServiceContextPath().endsWith("/") ? "/" : "");
    }

    /**
     * Return the EPR prefix for services made available over this transport
     * @param cfgCtx configuration context to retrieve the service context path
     * @param host name of the host
     * @param port listening port
     * @return wsdlEPRPrefix for the listener
     */
	protected String getServiceEPRPrefix(ConfigurationContext cfgCtx,
			String host, int port) {
        return scheme.getName() + "://"
            + host
            + (port == scheme.getDefaultPort() ? "" : ":" + port)
            + (!cfgCtx.getServiceContextPath().startsWith("/") ? "/" : "")
            + cfgCtx.getServiceContextPath()
            + (!cfgCtx.getServiceContextPath().endsWith("/") ? "/" : "");
	}
    
    private HttpGetRequestProcessor createHttpGetProcessor(String str) throws AxisFault {
        Object obj = null;
        try {
            obj = Class.forName(str).newInstance();
        } catch (ClassNotFoundException e) {
            handleException("Error creating WSDL processor", e);
        } catch (InstantiationException e) {
            handleException("Error creating WSDL processor", e);
        } catch (IllegalAccessException e) {
            handleException("Error creating WSDL processor", e);
        }

        if (obj instanceof HttpGetRequestProcessor) {
            return (HttpGetRequestProcessor) obj;
        } else {
            handleException("Error creating WSDL processor. The HttpProcessor should be of type " +
                    "org.apache.synapse.transport.nhttp.HttpGetRequestProcessor");
        }

        return null;
    }
    
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
    
    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }
}
