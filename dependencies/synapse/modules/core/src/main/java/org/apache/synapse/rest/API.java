/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.synapse.rest;

import org.apache.axis2.Constants;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.apache.synapse.rest.version.VersionStrategy;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.util.*;

public class API extends AbstractRESTProcessor implements ManagedLifecycle {

    private String host;
    private int port = -1;
    private String context;
    private Map<String,Resource> resources = new LinkedHashMap<String,Resource>();
    private List<Handler> handlers = new ArrayList<Handler>();

    private int protocol = RESTConstants.PROTOCOL_HTTP_AND_HTTPS;

    private VersionStrategy versionStrategy = new DefaultStrategy(this);

    private String fileName;

    public API(String name, String context) {
        super(name);
        setContext(context);
    }

    public void setContext(String context) {
        if (!context.startsWith("/")) {
            handleException("API context must begin with '/' character");
        }
        this.context = RESTUtils.trimTrailingSlashes(context);
    }

    /**
     * Get the fully qualified name of this API
     *
     * @return returns the key combination for API NAME + VERSION
     */
    public String getName() {
        // check if a versioning strategy exists
        if (versionStrategy.getVersion() != null && !"".equals(versionStrategy.getVersion()) ) {
            return name + ":v" +versionStrategy.getVersion();
        }
        return name;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getAPIName() {
        return name;
    }

    public String getVersion(){
        return versionStrategy.getVersion();
    }

    public String getContext() {
        return context;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addResource(Resource resource) {
        DispatcherHelper dispatcherHelper = resource.getDispatcherHelper();
        if (dispatcherHelper != null) {
            String mapping = dispatcherHelper.getString();
            for (Resource r : resources.values()) {
                DispatcherHelper helper = r.getDispatcherHelper();
                if (helper != null && helper.getString().equals(mapping) &&
                        resourceMatches(resource, r)) {
                    handleException("Two resources cannot have the same path mapping and methods");
                }
            }
        } else {
            for (Resource r : resources.values()) {
                DispatcherHelper helper = r.getDispatcherHelper();
                if (helper == null) {
                    handleException("Only one resource can be designated as default");
                }
            }
        }
        resources.put(resource.getName(), resource);
    }

    private boolean resourceMatches(Resource r1, Resource r2) {
        String[] methods1 = r1.getMethods();
        String[] methods2 = r2.getMethods();
        for (String m1 : methods1) {
            for (String m2 : methods2) {
                if (m1.equals(m2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Resource[] getResources() {
        return resources.values().toArray(new Resource[resources.size()]);
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    public Handler[] getHandlers() {
        return handlers.toArray(new Handler[handlers.size()]);
    }

    boolean canProcess(MessageContext synCtx) {
        if (synCtx.isResponse()) {
            String apiName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API);
            String version = synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION) == null ?
                             "": (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            //if api name is not matching OR versions are different
            if (!getName().equals(apiName) || !versionStrategy.getVersion().equals(version)) {
                return false;
            }
        } else {
            String path = RESTUtils.getFullRequestPath(synCtx);
            if (!path.startsWith(context + "/") && !path.startsWith(context + "?") &&
                    !context.equals(path) && !"/".equals(context)) {
                if (log.isDebugEnabled()) {
                    log.debug("API context: " + context + " does not match request URI: " + path);
                }
                return false;
            }

            if(!versionStrategy.isMatchingVersion(synCtx)){
                return false;
            }

            org.apache.axis2.context.MessageContext msgCtx =
                    ((Axis2MessageContext) synCtx).getAxis2MessageContext();

            if (host != null || port != -1) {
                String hostHeader = getHostHeader(msgCtx);
                if (hostHeader != null) {
                    if (host != null && !host.equals(extractHostName(hostHeader))) {
                        if (log.isDebugEnabled()) {
                            log.debug("API host: " + host + " does not match host information " +
                                    "in the request: " + hostHeader);
                        }
                        return false;
                    }

                    if (port != -1 && port != extractPortNumber(hostHeader,
                            msgCtx.getIncomingTransportName())) {
                        if (log.isDebugEnabled()) {
                            log.debug("API port: " + port + " does not match port information " +
                                    "in the request: " + hostHeader);
                        }
                        return false;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Host information not available on the message");
                    }
                    return false;
                }
            }
            if (protocol == RESTConstants.PROTOCOL_HTTP_ONLY &&
                    !Constants.TRANSPORT_HTTP.equals(msgCtx.getIncomingTransportName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Protocol information does not match - Expected HTTP");
                }
                synCtx.setProperty(SynapseConstants.TRANSPORT_DENIED,new Boolean(true));
                synCtx.setProperty(SynapseConstants.IN_TRANSPORT,msgCtx.getTransportIn().getName());
                log.warn("Trying to access API : "+name+" on restricted transport chanel ["+msgCtx.getTransportIn().getName()+"]");
                return false;
            }

            if (protocol == RESTConstants.PROTOCOL_HTTPS_ONLY &&
                    !Constants.TRANSPORT_HTTPS.equals(msgCtx.getIncomingTransportName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Protocol information does not match - Expected HTTPS");
                }
                synCtx.setProperty(SynapseConstants.TRANSPORT_DENIED,new Boolean(true));
                synCtx.setProperty(SynapseConstants.IN_TRANSPORT,msgCtx.getTransportIn().getName());
                log.warn("Trying to access API : "+name+" on restricted transport chanel ["+msgCtx.getTransportIn().getName()+"]");
                return false;
            }
        }

        return true;
    }

    void process(MessageContext synCtx) {
        if (log.isDebugEnabled()) {
            log.debug("Processing message with ID: " + synCtx.getMessageID() + " through the " +
                    "API: " + name);
        }

        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API, getName());
        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API_VERSION, versionStrategy.getVersion());
        synCtx.setProperty(RESTConstants.REST_API_CONTEXT, context);
        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY, versionStrategy.getVersionType());

        // Calculate REST_URL_POSTFIX from full request path
        String restURLPostfix = (String) synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (!synCtx.isResponse() && restURLPostfix != null) {  // Skip for response path
            if (!restURLPostfix.startsWith("/")) {
				restURLPostfix = "/" + restURLPostfix;
			} 
			if (restURLPostfix.startsWith(context)) {
				restURLPostfix = restURLPostfix.substring(context.length());				
			}
			if (versionStrategy instanceof URLBasedVersionStrategy) {
				String version = versionStrategy.getVersion();
				if (restURLPostfix.startsWith(version)) {
					restURLPostfix = restURLPostfix.substring(version.length());
				} else if (restURLPostfix.startsWith("/" + version)) {
					restURLPostfix = restURLPostfix.substring(version.length() + 1);
				}
			}
			((Axis2MessageContext) synCtx).getAxis2MessageContext().
							setProperty(NhttpConstants.REST_URL_POSTFIX,restURLPostfix);
		}

        for (Handler handler : handlers) {
            if (log.isDebugEnabled()) {
                log.debug("Processing message with ID: " + synCtx.getMessageID() + " through " +
                        "handler: " + handler.getClass().getName());
            }

            boolean proceed;
            if (synCtx.isResponse()) {
                proceed = handler.handleResponse(synCtx);
            } else {
                proceed = handler.handleRequest(synCtx);
            }

            if (!proceed) {
                return;
            }
        }

        if (synCtx.isResponse()) {
            String resourceName = (String) synCtx.getProperty(RESTConstants.SYNAPSE_RESOURCE);
            if (resourceName != null) {
                Resource resource = resources.get(resourceName);
                if (resource != null) {
                    resource.process(synCtx);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("No resource information on the response: " + synCtx.getMessageID());
            }
            return;
        }


        String path = RESTUtils.getFullRequestPath(synCtx);
        String subPath;
        if (versionStrategy.getVersionType().equals(VersionStrategyFactory.TYPE_URL)) {
            //for URL based
            //request --> http://{host:port}/context/version/path/to/resource
            subPath = path.substring(context.length() + versionStrategy.getVersion().length() + 1);
        } else {
            subPath = path.substring(context.length());
        }
        if ("".equals(subPath)) {
            subPath = "/";
        }
        synCtx.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, subPath);

        org.apache.axis2.context.MessageContext msgCtx =
                        ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String hostHeader = getHostHeader(msgCtx);
        if (hostHeader != null) {
            synCtx.setProperty(RESTConstants.REST_URL_PREFIX,
                    msgCtx.getIncomingTransportName() + "://" + hostHeader);
        }

        Set<Resource> acceptableResources = new HashSet<Resource>();
        for (Resource r : resources.values()) {
            if (r.canProcess(synCtx)) {
                acceptableResources.add(r);
            }
        }

        boolean processed = false;
        if (!acceptableResources.isEmpty()) {
            for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                Resource resource = dispatcher.findResource(synCtx, acceptableResources);
                if (resource != null) {
                    resource.process(synCtx);
                    processed = true;
                    break;
                }
            }
        }

        if (!processed) {
            if (log.isDebugEnabled()) {
                log.debug("No matching resource was found for the request: " + synCtx.getMessageID());
            }

            Mediator sequence = synCtx.getSequence(RESTConstants.NO_MATCHING_RESOURCE_HANDLER);
            if (sequence != null) {
                sequence.mediate(synCtx);
            }
        }
    }

    private String getHostHeader(org.apache.axis2.context.MessageContext msgCtx) {
        Map transportHeaders = (Map) msgCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String hostHeader = null;
        if (transportHeaders != null) {
            hostHeader = (String) transportHeaders.get(HTTP.TARGET_HOST);
        }

        if (hostHeader == null) {
            hostHeader = (String) msgCtx.getProperty(NhttpConstants.SERVICE_PREFIX);
        }
        return hostHeader;
    }

    private String extractHostName(String hostHeader) {
        int index = hostHeader.indexOf(':');
        if (index != -1) {
            return hostHeader.substring(0, index);
        } else {
            return hostHeader;
        }
    }

    private int extractPortNumber(String hostHeader, String transport) {
        int index = hostHeader.indexOf(':');
        if (index != -1) {
            return Integer.parseInt(hostHeader.substring(index + 1));
        } else if (Constants.TRANSPORT_HTTP.equals(transport)) {
            return 80;
        } else {
            return 443;
        }
    }

    public void init(SynapseEnvironment se) {
        if (resources.isEmpty()) {
            handleException("The API: " + getName() + " has been configured without " +
                    "any resource definitions");
        }

        log.info("Initializing API: " + getName());
        for (Resource resource : resources.values()) {
            resource.init(se);
        }
        
        for (Handler handler : handlers) {
            if (handler instanceof ManagedLifecycle) {
                ((ManagedLifecycle) handler).init(se);
            }
        }
    }

    public void destroy() {
        log.info("Destroying API: " + getName());
        for (Resource resource : resources.values()) {
            resource.destroy();
        }

        for (Handler handler : handlers) {
            if (handler instanceof ManagedLifecycle) {
                ((ManagedLifecycle) handler).destroy();
            }
        }
    }

    public VersionStrategy getVersionStrategy() {
        return versionStrategy;
    }

    public void setVersionStrategy(VersionStrategy versionStrategy) {
        this.versionStrategy = versionStrategy;
    }

    public Resource getResource(String resourceName) {
        return resources.get(resourceName);
    }
}
