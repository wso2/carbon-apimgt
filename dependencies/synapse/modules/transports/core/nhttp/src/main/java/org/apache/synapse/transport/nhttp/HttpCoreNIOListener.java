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
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.base.TransportMBeanSupport;
import org.apache.axis2.transport.base.threads.NativeThreadFactory;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.http.conn.Scheme;
import org.apache.synapse.transport.http.conn.ServerConnFactory;
import org.apache.synapse.transport.nhttp.config.ServerConnFactoryBuilder;
import org.apache.synapse.transport.nhttp.util.NhttpMetricsCollector;
import org.apache.synapse.transport.nhttp.util.ActiveConnectionMonitor;

/**
 * NIO transport listener for Axis2 based on HttpCore and NIO extensions
 */
public class HttpCoreNIOListener implements TransportListener, ManagementSupport {

    private static final Log log = LogFactory.getLog(HttpCoreNIOListener.class);
    /** The Axis2 configuration context */
    private volatile ConfigurationContext cfgCtx;
    /** The IOReactor */
    private volatile DefaultListeningIOReactor ioReactor;
    /** The I/O dispatch */
    private volatile ServerIODispatch iodispatch;
    /** Protocol scheme used by this listener **/
    private volatile Scheme scheme;
    /** Connection factory used by this listener **/
    private volatile ServerConnFactory connFactory;
    /** The component name (to be used in logs)*/
    private volatile String name;
    /** HTTP parameters */
    private volatile HttpParams params;
    /** The ServerHandler */
    private volatile ServerHandler handler;
    /** JMX support */
    private volatile TransportMBeanSupport mbeanSupport;
    /** Listener configurations */
    private volatile ListenerContext listenerContext;
    /** Metrics */
    private volatile NhttpMetricsCollector metrics;
    /** state of the listener */
    private volatile int state = BaseConstants.STOPPED;
    /** Checks wheather WSDLEPRPrefix is set*/
    private boolean EPRPrefixCheck = true;
    
    /** The EPR prefix for services available over this transport */
    private volatile String serviceEPRPrefix;
    /** The EPR prefix for services with custom URI available over this transport */
    private volatile String customEPRPrefix;
    /** The custom URI map for the services if there are any */
    private final Map<String, String> serviceNameToEPRMap = new HashMap<String, String>();
    /** The servicename map for the custom URI if there are any */
    private Map<String, String> eprToServiceNameMap = new HashMap<String, String>();
    /** the axis observer that gets notified of service life cycle events*/
    private final AxisObserver axisObserver = new GenericAxisObserver();
    /** Active Connection Monitor Scheduler  */
    private final ScheduledExecutorService activeConnectionMonitorScheduler = Executors.newSingleThreadScheduledExecutor();
    /** Delay for ActiveConnectionMonitor */
    public static final long ACTIVE_CONNECTION_MONITOR_DELAY = 1000;

    protected Scheme initScheme() {
        return new Scheme("http", 80, false);
    }
    
    protected ServerConnFactoryBuilder initConnFactoryBuilder(
            final TransportInDescription transportIn, final HttpHost host) throws AxisFault {
        return new ServerConnFactoryBuilder(transportIn, host);
    }    

    /**
     * Initialize the transport listener, and execute reactor in new separate thread
     * @param "cfgCtx" the Axis2 configuration context
     * @param transportIn the description of the http/s transport from Axis2 configuration
     * @throws AxisFault on error
     */
    public void init(ConfigurationContext ctx, TransportInDescription transportIn)
            throws AxisFault {

        cfgCtx = ctx;
        Map<String, String> o = (Map<String, String>) cfgCtx.getProperty(NhttpConstants.EPR_TO_SERVICE_NAME_MAP);
        if (o != null) {
            this.eprToServiceNameMap = o;
        } else {
            eprToServiceNameMap = new HashMap<String, String>();
            cfgCtx.setProperty(NhttpConstants.EPR_TO_SERVICE_NAME_MAP, eprToServiceNameMap);
        }

        NHttpConfiguration cfg = NHttpConfiguration.getInstance();

        // Initialize connection factory
        params = new BasicHttpParams();
        params
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                        cfg.getProperty(NhttpConstants.SO_TIMEOUT_RECEIVER, 60000))
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
                        cfg.getProperty(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024))
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "Synapse-HttpComponents-NIO")
                .setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET,
                        cfg.getStringValue(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, HTTP.DEFAULT_PROTOCOL_CHARSET)); //TODO:This does not works with HTTPCore 4.3

        name = transportIn.getName().toUpperCase(Locale.US) + " Listener";
        scheme = initScheme();
        
        // Setup listener context
        listenerContext = new ListenerContextBuilder(transportIn).parse().build();

        System.setProperty(transportIn.getName() + ".nio.port", String.valueOf(listenerContext.getPort()));
        
        // Setup connection factory
        HttpHost host = new HttpHost(
            listenerContext.getHostname(), 
            listenerContext.getPort(), 
            scheme.getName());
        connFactory = initConnFactoryBuilder(transportIn, host).build(params);

        // configure the IO reactor on the specified port
        try {
            String prefix = name + " I/O dispatcher";
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(
                    cfg.getServerIOWorkers());
            ioReactorConfig.setSoTimeout(
                    cfg.getProperty(NhttpConstants.SO_TIMEOUT_RECEIVER, 60000));
            ioReactorConfig.setTcpNoDelay(
                    cfg.getProperty(CoreConnectionPNames.TCP_NODELAY, 1) == 1);
            if (cfg.getBooleanValue("http.nio.interest-ops-queueing", false)) {
                ioReactorConfig.setInterestOpQueued(true);
            }
            ioReactor = new DefaultListeningIOReactor(
                    ioReactorConfig,
                    new NativeThreadFactory(new ThreadGroup(prefix + " thread group"), prefix));

            ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
                public boolean handle(IOException ioException) {
                    log.warn("System may be unstable: IOReactor encountered a checked exception : "
                            + ioException.getMessage(), ioException);
                    return true;
                }

                public boolean handle(RuntimeException runtimeException) {
                    log.warn("System may be unstable: IOReactor encountered a runtime exception : "
                            + runtimeException.getMessage(), runtimeException);
                    return true;
                }
            });
        } catch (IOException e) {
            handleException("Error creating IOReactor", e);
        }

        metrics = new NhttpMetricsCollector(true, transportIn.getName());
        
        handler = new ServerHandler(cfgCtx, scheme, listenerContext, metrics);
        iodispatch = new ServerIODispatch(handler, connFactory);
        
        Parameter param = transportIn.getParameter(NhttpConstants.WSDL_EPR_PREFIX);
        if (param != null) {
            serviceEPRPrefix = getServiceEPRPrefix(cfgCtx, (String) param.getValue());
            customEPRPrefix = (String) param.getValue();
            EPRPrefixCheck = false;
        } else {
            serviceEPRPrefix = getServiceEPRPrefix(cfgCtx, listenerContext.getHostname(), listenerContext.getPort());
            customEPRPrefix = scheme.getName() + "://" + listenerContext.getHostname() +
                    ":" + (listenerContext.getPort() == scheme.getDefaultPort() ? "" : listenerContext.getPort()) + "/";
        }

        // register to receive updates on services for lifetime management
        cfgCtx.getAxisConfiguration().addObservers(axisObserver);

        // register with JMX
        mbeanSupport = new TransportMBeanSupport(this, "nio-" + transportIn.getName());
        mbeanSupport.register();
    }

    public int getActiveConnectionsSize() {
        return handler.getActiveConnectionsSize();
    }

    /**
     * Return the EPR prefix for services made available over this transport
     * @param cfgCtx configuration context to retrieve the service context path
     * @param host name of the host
     * @param port listening port
     * @return wsdlEPRPrefix for the listener
     */
    protected String getServiceEPRPrefix(ConfigurationContext cfgCtx, String host, int port) {
        return scheme.getName() + "://" +
                host + (port == scheme.getDefaultPort() ? "" : ":" + port) +
                (!cfgCtx.getServiceContextPath().startsWith("/") ? "/" : "") +
                cfgCtx.getServiceContextPath() +
                (!cfgCtx.getServiceContextPath().endsWith("/") ? "/" : "");
    }

    /**
     * Return the EPR prefix for services made available over this transport
     * @param cfgCtx configuration context to retrieve the service context path
     * @param wsdlEPRPrefix specified wsdlPrefix
     * @return wsdlEPRPrefix for the listener
     */
    protected String getServiceEPRPrefix(ConfigurationContext cfgCtx, String wsdlEPRPrefix) {
        return wsdlEPRPrefix +
                (!cfgCtx.getServiceContextPath().startsWith("/") ? "/" : "") +
                cfgCtx.getServiceContextPath() +
                (!cfgCtx.getServiceContextPath().endsWith("/") ? "/" : "");
    }



    /**
     * Start the transport listener. This method returns when the listener is ready to
     * accept connections.
     * @throws AxisFault
     */
    public void start() throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Starting Listener...");
        }

        for (Object obj : cfgCtx.getAxisConfiguration().getServices().values()) {
            addToServiceURIMap((AxisService) obj);
        }

        state = BaseConstants.STARTED;
        
        // start the IO reactor in a new separate thread
        final IOEventDispatch ioEventDispatch = iodispatch;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    ioReactor.execute(ioEventDispatch);
                } catch (InterruptedIOException ex) {
                    log.fatal("Reactor Interrupted", ex);
                } catch (IOException e) {
                    log.fatal("Encountered an I/O error: " + e.getMessage(), e);
                } catch (Exception e) {
                    log.fatal("Unexpected exception in I/O reactor", e);
                }
                log.info(name + " Shutdown");
            }
        }, "HttpCoreNIOListener");

        t.start();

        listenerContext.getHttpGetRequestProcessor().init(cfgCtx, handler);
        startEndpoints();
    }

    private void startEndpoints() throws AxisFault {
        Queue<ListenerEndpoint> endpoints = new LinkedList<ListenerEndpoint>();
        
        Set<InetSocketAddress> addressSet = new HashSet<InetSocketAddress>();
        addressSet.addAll(connFactory.getBindAddresses());
        if (NHttpConfiguration.getInstance().getMaxActiveConnections() != -1) {
            addMaxConnectionCountController(NHttpConfiguration.getInstance().getMaxActiveConnections());
        }
        if (listenerContext.getBindAddress() != null) {
            addressSet.add(new InetSocketAddress(listenerContext.getBindAddress(), listenerContext.getPort()));
        }
        if (addressSet.isEmpty()) {
            addressSet.add(new InetSocketAddress(listenerContext.getPort()));
        }
        
        // Ensure simple but stable order
        List<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>(addressSet);
        Collections.sort(addressList, new Comparator<InetSocketAddress>() {

            public int compare(InetSocketAddress a1, InetSocketAddress a2) {
                String s1 = a1.toString();
                String s2 = a2.toString();
                return s1.compareTo(s2);
            }
            
        });
        for (InetSocketAddress address: addressList) {
            endpoints.add(ioReactor.listen(address));
        }

        // Wait for the endpoint to become ready, i.e. for the listener to start accepting
        // requests.
        while (!endpoints.isEmpty()) {
            ListenerEndpoint endpoint = endpoints.remove();
            try {
                endpoint.waitFor();
                if (log.isInfoEnabled()) {
                    InetSocketAddress address = (InetSocketAddress) endpoint.getAddress();
                    if (!address.isUnresolved()) {
                        log.info(name + " started on " + address.getHostName() + ":" + address.getPort());
                    } else {
                        log.info(name + " started on " + address);
                    }
                }
            } catch (InterruptedException e) {
                log.warn("Listener startup was interrupted");
                break;
            }
        }
    }
    
    private void addToServiceURIMap(AxisService service) {
        Parameter param = service.getParameter(NhttpConstants.SERVICE_URI_LOCATION);
        if (param != null) {
            String uriLocation = param.getValue().toString();
            if (uriLocation.startsWith("/")) {
                uriLocation = uriLocation.substring(1);
            }
            serviceNameToEPRMap.put(service.getName(), uriLocation);
            eprToServiceNameMap.put(uriLocation, service.getName());
        }
    }

    private void removeServiceFfromURIMap(AxisService service) {
        eprToServiceNameMap.remove(serviceNameToEPRMap.get(service.getName()));
        serviceNameToEPRMap.remove(service.getName());
    }

    /**
     * Stop the listener
     * @throws AxisFault on error
     */
    public void stop() throws AxisFault {
        if (state == BaseConstants.STOPPED) return;
        try {
            int wait = NHttpConfiguration.getInstance().getListenerShutdownWaitTime();
            if (wait > 0) {
                ioReactor.pause();
                log.info("Waiting " + wait/1000 + " seconds to cleanup active connections...");
                Thread.sleep(wait);
                ioReactor.shutdown(wait);
            }  else {
                ioReactor.shutdown();
            }
            handler.stop();
            state = BaseConstants.STOPPED;
            for (Object obj : cfgCtx.getAxisConfiguration().getServices().values()) {
                removeServiceFfromURIMap((AxisService) obj);
            }
        } catch (IOException e) {
            handleException("Error shutting down IOReactor", e);
        } catch (InterruptedException e) {
            handleException("Error waiting for connection drain", e);
        }
    }

    /**
     * Pause the listener - Stops accepting new connections, but continues processing existing
     * connections until they complete. This helps bring an instance into a maintenence mode
     * @throws AxisFault
     */
    public void pause() throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        try {
            ioReactor.pause();
            handler.markActiveConnectionsToBeClosed();
            state = BaseConstants.PAUSED;
            log.info(name + " Paused");
        } catch (IOException e) {
            handleException("Error pausing IOReactor", e);
        }
    }

    /**
     * Resume the lister - Brings the lister into active mode back from a paused state
     * @throws AxisFault
     */
    public void resume() throws AxisFault {
        if (state != BaseConstants.PAUSED) return;
        try {
            ioReactor.resume();
            state = BaseConstants.STARTED;
            log.info(name + " Resumed");
        } catch (IOException e) {
            handleException("Error resuming IOReactor", e);
        }
    }

    public void reload(final TransportInDescription transportIn) throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        
        // Close all listener endpoints and stop accepting new connections
        Set<ListenerEndpoint> endpoints = ioReactor.getEndpoints();
        for (ListenerEndpoint endpoint: endpoints) {
            endpoint.close();
        }
        
        // Rebuild connection factory
        HttpHost host = new HttpHost(
            listenerContext.getHostname(), 
            listenerContext.getPort(), 
            scheme.getName());
        ServerConnFactoryBuilder connFactoryBuilder = initConnFactoryBuilder(transportIn, host);
        connFactory = connFactoryBuilder.build(params);
        iodispatch.update(connFactory);
        
        startEndpoints();
        
        log.info(name + " Reloaded");
    }
    
    /**
     * Returns the number of active threads processing messages
     * @return number of active threads processing messages
     */
    public int getActiveThreadCount() {
        return handler.getActiveCount();
    }

    /**
     * Returns the number of requestes queued in the thread pool
     * @return queue size
     */
    public int getQueueSize() {
        return handler.getQueueSize();
    }

    /**
     * Stop accepting new connections, and wait the maximum specified time for in-flight
     * requests to complete before a controlled shutdown for maintenence
     *
     * @param millis a number of milliseconds to wait until pending requests are allowed to complete
     * @throws AxisFault
     */
    public void maintenenceShutdown(long millis) throws AxisFault {
        if (state != BaseConstants.STARTED) return;
        try {
            long start = System.currentTimeMillis();
            ioReactor.pause();
            ioReactor.shutdown(millis);
            state = BaseConstants.STOPPED;
            log.info("Listener shutdown in : " + (System.currentTimeMillis() - start) / 1000 + "s");
        } catch (IOException e) {
            handleException("Error shutting down the IOReactor for maintenence", e);
        }
    }


    /**
     * Return the EPR for the given service (implements deprecated method temporarily)
     */
    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {

        EndpointReference ep = getEPRsForService(serviceName, ip)[0];
        return ep;
//        String trailler = "";
//        //Strip out the operation name
//        if (serviceName.indexOf('/') != -1) {
//            trailler += serviceName.substring(serviceName.indexOf("/"));
//            serviceName = serviceName.substring(0, serviceName.indexOf('/'));
//        }
//        // strip out the endpoint name if present
//        if (serviceName.indexOf('.') != -1) {
//            trailler += serviceName.substring(serviceName.indexOf("."));
//            serviceName = serviceName.substring(0, serviceName.indexOf('.'));
//        }
//
//        if (serviceNameToEPRMap.containsKey(serviceName)) {
//            return new EndpointReference(
//                    customEPRPrefix + serviceNameToEPRMap.get(serviceName));
//        } else {
//            return new EndpointReference(serviceEPRPrefix + serviceName + trailler);
//        }
    }

    /**
     * Return the EPRs for the given service over this transport
     * @param serviceName name of the service
     * @param ip IP address
     * @return the EndpointReferences for this service over the transport
     * @throws AxisFault on error
     */
    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {

        String trailer = "";
        boolean isServiceWithCustomURI = isServiceWithCustomURI(serviceName);
        //Strip out the operation name
        if (serviceName.indexOf('/') != -1) {
            trailer += serviceName.substring(serviceName.indexOf("/"));
            serviceName = serviceName.substring(0, serviceName.indexOf('/'));
        }
        // strip out the endpoint name if present
        if (serviceName.indexOf('.') != -1 && !isServiceWithCustomURI) {
            trailer += serviceName.substring(serviceName.indexOf("."));
            serviceName = serviceName.substring(0, serviceName.indexOf('.'));
        }else if(isServiceWithCustomURI){
            serviceName = getServiceNameFromServiceWithCustomURI(serviceName);
        }

        EndpointReference[] endpointReferences = new EndpointReference[1];
        String service = serviceEPRPrefix;
        if (ip != null && !"".equals(ip) && EPRPrefixCheck) {
            service = replaceHostname(service, ip);
        }
        if (serviceNameToEPRMap.containsKey(serviceName)) {
            endpointReferences[0] = new EndpointReference(
                    customEPRPrefix + serviceNameToEPRMap.get(serviceName));
        } else {
            if (service == null || "".equals(service)) {
                return null;
            }
            endpointReferences[0]
                    = new EndpointReference(service + serviceName + trailer);
        }
        return endpointReferences;
    }

    private boolean isServiceWithCustomURI(String serviceName){

        if(serviceNameToEPRMap.containsKey(serviceName)){
            return true;
        }
        //if map is not containing service name and there is no dot in service name
        //service is not have a custom URI
        if(!serviceName.contains(".")){
            return false;
        }
        serviceName = serviceName.substring(0,serviceName.lastIndexOf("."));
        return isServiceWithCustomURI(serviceName);
    }

    /** Only call this service IFF isServiceWithCustomURI(serviceName) is true
     * Else some nasty things can happen
     * @param serviceName
     * @return serviceName without endpoint part
     */
    private String getServiceNameFromServiceWithCustomURI(String serviceName){

        if(serviceNameToEPRMap.containsKey(serviceName)){
            return serviceName;
        }
        serviceName = serviceName.substring(0,serviceName.lastIndexOf("."));
        return getServiceNameFromServiceWithCustomURI(serviceName);
    }


    /**
     * Pause the IOReactor if the maxActive listener connection is
     * exceed the configured value.
     */
    private void addMaxConnectionCountController(int maxActiveConnections) {
        ActiveConnectionMonitor activeConnectionMonitor =
                new ActiveConnectionMonitor(metrics, ioReactor, maxActiveConnections);
        activeConnectionMonitorScheduler.scheduleWithFixedDelay(
                activeConnectionMonitor, 0, ACTIVE_CONNECTION_MONITOR_DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * TODO: Return session context from transport, this is an improvement in axis2 1.2 and
     * is not currently supported
     * @param messageContext context to be used
     * @return always null
     */
    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    public void destroy() {
        ioReactor = null;
        cfgCtx.getAxisConfiguration().getObserversList().remove(axisObserver);
        mbeanSupport.unregister();
        metrics.destroy();
    }

    /**
     * An AxisObserver which will start listening for newly deployed or started services,
     * and stop listening when services are undeployed or stopped.
     */
    class GenericAxisObserver implements AxisObserver {

        // The initilization code will go here
        public void init(AxisConfiguration axisConfig) {
        }

        public void serviceUpdate(AxisEvent event, AxisService service) {

            if (!ignoreService(service)
                    && BaseUtils.isUsingTransport(service, listenerContext.getTransportIn().getName())) {
                switch (event.getEventType()) {
                    case AxisEvent.SERVICE_DEPLOY :
                        addToServiceURIMap(service);
                        break;
                    case AxisEvent.SERVICE_REMOVE :
                        removeServiceFfromURIMap(service);
                        break;
                    case AxisEvent.SERVICE_START  :
                        addToServiceURIMap(service);
                        break;
                    case AxisEvent.SERVICE_STOP   :
                        removeServiceFfromURIMap(service);
                        break;
                }
            }
        }

        public void moduleUpdate(AxisEvent event, AxisModule module) {}
        public void addParameter(Parameter param) throws AxisFault {}
        public void removeParameter(Parameter param) throws AxisFault {}
        public void deserializeParameters(OMElement parameterElement) throws AxisFault {}
        public Parameter getParameter(String name) { return null; }
        public ArrayList<Parameter> getParameters() { return null; }
        public boolean isParameterLocked(String parameterName) { return false; }
        public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {}
    }

    private boolean ignoreService(AxisService service) {
        // these are "private" services
        return service.getName().startsWith("__") || JavaUtils.isTrueExplicitly(
                service.getParameter(NhttpConstants.HIDDEN_SERVICE_PARAM_NAME));
    }

    // -------------- utility methods -------------
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    // -- jmx/management methods--
    public long getMessagesReceived() {
        if (metrics != null) {
            return metrics.getMessagesReceived();
        }
        return -1;
    }

    public long getFaultsReceiving() {
        if (metrics != null) {
            return metrics.getFaultsReceiving();
        }
        return -1;
    }

    public long getBytesReceived() {
        if (metrics != null) {
            return metrics.getBytesReceived();
        }
        return -1;
    }

    public long getMessagesSent() {
        if (metrics != null) {
            return metrics.getMessagesSent();
        }
        return -1;
    }

    public long getFaultsSending() {
        if (metrics != null) {
            return metrics.getFaultsSending();
        }
        return -1;
    }

    public long getBytesSent() {
        if (metrics != null) {
            return metrics.getBytesSent();
        }
        return -1;
    }

    public long getTimeoutsReceiving() {
        if (metrics != null) {
            return metrics.getTimeoutsReceiving();
        }
        return -1;
    }

    public long getTimeoutsSending() {
        if (metrics != null) {
            return metrics.getTimeoutsSending();
        }
        return -1;
    }

    public long getMinSizeReceived() {
        if (metrics != null) {
            return metrics.getMinSizeReceived();
        }
        return -1;
    }

    public long getMaxSizeReceived() {
        if (metrics != null) {
            return metrics.getMaxSizeReceived();
        }
        return -1;
    }

    public double getAvgSizeReceived() {
        if (metrics != null) {
            return metrics.getAvgSizeReceived();
        }
        return -1;
    }

    public long getMinSizeSent() {
        if (metrics != null) {
            return metrics.getMinSizeSent();
        }
        return -1;
    }

    public long getMaxSizeSent() {
        if (metrics != null) {
            return metrics.getMaxSizeSent();
        }
        return -1;
    }

    public double getAvgSizeSent() {
        if (metrics != null) {
            return metrics.getAvgSizeSent();
        }
        return -1;
    }

    public Map getResponseCodeTable() {
        if (metrics != null) {
            return metrics.getResponseCodeTable();
        }
        return null;
    }

    public void resetStatistics() {
        if (metrics != null) {
            metrics.reset();
        }
    }

    public long getLastResetTime() {
        if (metrics != null) {
            return metrics.getLastResetTime();
        }
        return -1;
    }

    public long getMetricsWindow() {
        if (metrics != null) {
            return System.currentTimeMillis() - metrics.getLastResetTime();
        }
        return -1;
    }

    private String replaceHostname(String url, String hostName) {
        if (url == null) {
            return "";
        }
        boolean valid;
		int s = url.indexOf("://") + 3;
		valid = s > 3 ? true : false;
		int e = url.lastIndexOf(":");
		valid = e == url.indexOf(":") ? false : true;
		if (valid) {
			return url.substring(0, s) + hostName + url.substring(e);
		}
        return "";
    }
}
