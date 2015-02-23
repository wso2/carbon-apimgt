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

package org.apache.synapse.core.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.SynapseArtifact;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.AspectConfigurable;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.PolicyInfo;
import org.apache.synapse.util.resolver.CustomWSDLLocator;
import org.apache.synapse.util.resolver.CustomXmlSchemaURIResolver;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.util.resolver.UserDefinedWSDLLocator;
import org.apache.synapse.util.resolver.UserDefinedXmlSchemaURIResolver;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * <proxy-service name="string" [transports="(http |https |jms )+|all"] [trace="enable|disable"]>
 *    <description>..</description>?
 *    <target [inSequence="name"] [outSequence="name"] [faultSequence="name"] [endpoint="name"]>
 *       <endpoint>...</endpoint>
 *       <inSequence>...</inSequence>
 *       <outSequence>...</outSequence>
 *       <faultSequence>...</faultSequence>
 *    </target>?
 *    <publishWSDL uri=".." key="string" endpoint="string">
 *       <wsdl:definition>...</wsdl:definition>?
 *       <wsdl20:description>...</wsdl20:description>?
 *       <resource location="..." key="..."/>*
 *    </publishWSDL>?
 *    <enableSec/>?
 *    <enableRM/>?
 *    <policy key="string" [type=("in" |"out")] [operationName="string"]
 *      [operationNamespace="string"]>?
 *       // optional service parameters
 *    <parameter name="string">
 *       text | xml
 *    </parameter>?
 * </proxy-service>
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ProxyService implements AspectConfigurable, SynapseArtifact {

    private static final Log log = LogFactory.getLog(ProxyService.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);
    private final Log serviceLog;

    public static final String ABSOLUTE_SCHEMA_URL_PARAM = "showAbsoluteSchemaURL";
    
    /**
     * The name of the proxy service
     */
    private String name;
    /**
     * The proxy service description. This could be optional informative text about the service
     */
    private String description;
    /**
     * The transport/s over which this service should be exposed, or defaults to all available
     */
    private ArrayList transports;
    /**
     * Server names for which this service should be exposed
     */
    private List pinnedServers = new ArrayList();
    /**
     * The target endpoint key
     */
    private String targetEndpoint = null;
    /**
     * The target inSequence key
     */
    private String targetInSequence = null;
    /**
     * The target outSequence key
     */
    private String targetOutSequence = null;
    /**
     * The target faultSequence key
     */
    private String targetFaultSequence = null;
    /**
     * The inlined definition of the target endpoint, if defined
     */
    private Endpoint targetInLineEndpoint = null;
    /**
     * The inlined definition of the target in-sequence, if defined
     */
    private SequenceMediator targetInLineInSequence = null;
    /**
     * The in-lined definition of the target out-sequence, if defined
     */
    private SequenceMediator targetInLineOutSequence = null;
    /**
     * The in-lined definition of the target fault-sequence, if defined
     */
    private SequenceMediator targetInLineFaultSequence = null;
    /**
     * A list of any service parameters (e.g. JMS parameters etc)
     */
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    /**
     * The key for the base WSDL
     */
    private String wsdlKey;
    /**
     * The URI for the base WSDL, if defined as a URL
     */
    private URI wsdlURI;
    /**
     * The in-lined representation of the service WSDL, if defined inline
     */
    private Object inLineWSDL;
    /**
     * A ResourceMap object allowing to locate artifacts (WSDL and XSD) imported
     * by the service WSDL to be located in the registry.
     */

    /**
     * Endpoint which used to resolve WSDL url
     * If address endpoint pointed, url+"?wsdl" will be use as WSDL url
     * If wsdl endpoint,  wsdl url of endpoint will be use to fetch wsdl
     */
    private String publishWSDLEndpoint;

    private ResourceMap resourceMap;
    /**
     * Policies to be set to the service, this can include service level, operation level,
     * message level or hybrid level policies as well.
     */
    private List<PolicyInfo> policies = new ArrayList<PolicyInfo>();
    /**
     * The keys for any supplied policies that would apply at the service level
     */
    private final List<String> serviceLevelPolicies = new ArrayList<String>();
    /**
     * The keys for any supplied policies that would apply at the in message level
     */
    private List<String> inMessagePolicies = new ArrayList<String>();
    /**
     * The keys for any supplied policies that would apply at the out message level
     */
    private List<String> outMessagePolicies = new ArrayList<String>();
    /**
     * Should WS Addressing be engaged on this service
     */
    private boolean wsAddrEnabled = false;
    /**
     * Should WS RM be engaged on this service
     */
    private boolean wsRMEnabled = false;
    /**
     * Should WS Sec be engaged on this service
     */
    private boolean wsSecEnabled = false;
    /**
     * Should this service be started by default on initialization?
     */
    private boolean startOnLoad = true;
    /**
     * Is this service running now?
     */
    private boolean running = false;

    public static final String ALL_TRANSPORTS = "all";
   
    /**
     * The variable that indicate tracing on or off for the current mediator
     */
    private int traceState = SynapseConstants.TRACING_UNSET;

    private AspectConfiguration aspectConfiguration;

    private String fileName;

    private String serviceGroup;

    private boolean moduleEngaged;

    private boolean wsdlPublished;

    /**
     * Constructor
     *
     * @param name the name of the Proxy service
     */
    public ProxyService(String name) {
        this.name = name;
        serviceLog = LogFactory.getLog(SynapseConstants.SERVICE_LOGGER_PREFIX + name);
    }

    /**
     * Build the underlying Axis2 service from the Proxy service definition
     *
     * @param synCfg  the Synapse configuration
     * @param axisCfg the Axis2 configuration
     * @return the Axis2 service for the Proxy
     */
    public AxisService buildAxisService(SynapseConfiguration synCfg, AxisConfiguration axisCfg) {

        auditInfo("Building Axis service for Proxy service : " + name);
        AxisService proxyService = null;

        // get the wsdlElement as an OMElement
        if (trace()) {
            trace.info("Loading the WSDL : " +
                (publishWSDLEndpoint != null ? " endpoint = " + publishWSDLEndpoint :
                (wsdlKey != null ? " key = " + wsdlKey :
                (wsdlURI != null ? " URI = " + wsdlURI : " <Inlined>"))));
        }

        InputStream wsdlInputStream = null;
        OMElement wsdlElement = null;
        boolean wsdlFound = false;
        String publishWSDL = null;

        SynapseEnvironment synEnv = SynapseConfigUtils.getSynapseEnvironment(axisCfg);
        String synapseHome = synEnv != null ? synEnv.getServerContextInformation()
                .getServerConfigurationInformation().getSynapseHome() : "";

        if (wsdlKey != null) {
            synCfg.getEntryDefinition(wsdlKey);
            Object keyObject = synCfg.getEntry(wsdlKey);
            if (keyObject instanceof OMElement) {
                wsdlElement = (OMElement) keyObject;
            }
            wsdlFound = true;
        } else if (inLineWSDL != null) {
            wsdlElement = (OMElement) inLineWSDL;
            wsdlFound = true;
        } else if (wsdlURI != null) {
            try {
            	URL url = wsdlURI.toURL();
                publishWSDL = url.toString();

                OMNode node = SynapseConfigUtils.getOMElementFromURL(publishWSDL, synapseHome);
                if (node instanceof OMElement) {
                    wsdlElement = (OMElement) node;
                }
                wsdlFound = true;
            } catch (MalformedURLException e) {
                handleException("Malformed URI for wsdl", e);
            } catch (IOException e) {
                //handleException("Error reading from wsdl URI", e);

                boolean enablePublishWSDLSafeMode = false;
                Map proxyParameters= this.getParameterMap();
                if (!proxyParameters.isEmpty()) {
                    if (proxyParameters.containsKey("enablePublishWSDLSafeMode")) {
                        enablePublishWSDLSafeMode =
                                Boolean.parseBoolean(
                                        proxyParameters.get("enablePublishWSDLSafeMode").
                                                toString().toLowerCase());
                    } else {
                        if (trace()) {
                            trace.info("WSDL was unable to load for: " + publishWSDL);
                            trace.info("Please add <syn:parameter name=\"enableURISafeMode\">true" +
                                    "</syn:parameter> to proxy service.");
                        }
                        handleException("Error reading from wsdl URI", e);
                    }
                }

                if (enablePublishWSDLSafeMode) {
                    // this is if the wsdl cannot be loaded... create a dummy service and an operation for which
                    // our SynapseDispatcher will properly dispatch to

                    //!!!Need to add a reload function... And display that the wsdl/service is offline!!!
                    if (trace()) {
                        trace.info("WSDL was unable to load for: " + publishWSDL);
                        trace.info("enableURISafeMode: true");
                    }

                    log.warn("Unable to load the WSDL for : " + name, e);
                    return null;
                } else {
                    if (trace()) {
                        trace.info("WSDL was unable to load for: " + publishWSDL);
                        trace.info("enableURISafeMode: false");
                    }

                    handleException("Error reading from wsdl URI", e);
                }
            }
        } else if (publishWSDLEndpoint != null) {
            try {
                URL url = null;
                Endpoint ep = synCfg.getEndpoint(publishWSDLEndpoint);
                if (ep == null) {
                    handleException("Unable to resolve WSDL url. " + publishWSDLEndpoint + " is null");
                }

                if (ep instanceof AddressEndpoint) {
                    url = new URL(((AddressEndpoint) (ep)).getDefinition().getAddress() + "?wsdl");
                } else if (ep instanceof WSDLEndpoint) {
                    url = new URL(((WSDLEndpoint) (ep)).getWsdlURI());
                } else {
                    handleException("Unable to resolve WSDL url. " + publishWSDLEndpoint +
                            " is not a AddressEndpoint or WSDLEndpoint");
                }
                publishWSDL = url.toString();

                OMNode node = SynapseConfigUtils.getOMElementFromURL(publishWSDL, synapseHome);
                if (node instanceof OMElement) {
                    wsdlElement = (OMElement) node;
                }
                wsdlFound = true;
            } catch (MalformedURLException e) {
                handleException("Malformed URI for wsdl", e);
            } catch (IOException e) {
                //handleException("Error reading from wsdl URI", e);

                boolean enablePublishWSDLSafeMode = false;
                Map proxyParameters= this.getParameterMap();
                if (!proxyParameters.isEmpty()) {
                    if (proxyParameters.containsKey("enablePublishWSDLSafeMode")) {
                        enablePublishWSDLSafeMode =
                                Boolean.parseBoolean(
                                        proxyParameters.get("enablePublishWSDLSafeMode").
                                                toString().toLowerCase());
                    } else {
                        if (trace()) {
                            trace.info("WSDL was unable to load for: " + publishWSDL);
                            trace.info("Please add <syn:parameter name=\"enableURISafeMode\">true" +
                                    "</syn:parameter> to proxy service.");
                        }
                        handleException("Error reading from wsdl URI " + publishWSDL, e);
                    }
                }

                if (enablePublishWSDLSafeMode) {
                    // this is if the wsdl cannot be loaded... create a dummy service and an operation for which
                    // our SynapseDispatcher will properly dispatch to

                    //!!!Need to add a reload function... And display that the wsdl/service is offline!!!
                    if (trace()) {
                        trace.info("WSDL was unable to load for: " + publishWSDL);
                        trace.info("enableURISafeMode: true");
                    }

                    log.warn("Unable to load the WSDL for : " + name, e);
                    return null;
                } else {
                    if (trace()) {
                        trace.info("WSDL was unable to load for: " + publishWSDL);
                        trace.info("enableURISafeMode: false");
                    }

                    handleException("Error reading from wsdl URI " + publishWSDL, e);
                }
            }
        } else {
            // this is for POX... create a dummy service and an operation for which
            // our SynapseDispatcher will properly dispatch to
            if (trace()) trace.info("Did not find a WSDL. Assuming a POX or Legacy service");
            proxyService = new AxisService();
            AxisOperation mediateOperation = new InOutAxisOperation(
                    SynapseConstants.SYNAPSE_OPERATION_NAME);
            // Set the names of the two messages so that Axis2 is able to produce a WSDL (see SYNAPSE-366):
            mediateOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).setName("in");
            mediateOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE).setName("out");
            proxyService.addOperation(mediateOperation);
        }

        // if a WSDL was found
        if (wsdlElement != null) {
            OMNamespace wsdlNamespace = wsdlElement.getNamespace();

            // serialize and create an input stream to read WSDL
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                if (trace()) trace.info("Serializing wsdlElement found to build an Axis2 service");
                wsdlElement.serialize(baos);
                wsdlInputStream = new ByteArrayInputStream(baos.toByteArray());
            } catch (XMLStreamException e) {
                handleException("Error converting to a StreamSource", e);
            }

            if (wsdlInputStream != null) {

                try {
                    // detect version of the WSDL 1.1 or 2.0
                    if (trace()) trace.info("WSDL Namespace is : "
                        + wsdlNamespace.getNamespaceURI());

                    if (wsdlNamespace != null) {
                        WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder = null;

                        if (WSDL2Constants.WSDL_NAMESPACE.
                                equals(wsdlNamespace.getNamespaceURI())) {
                            wsdlToAxisServiceBuilder =
                                    new WSDL20ToAxisServiceBuilder(wsdlInputStream, null, null);

                        } else if (org.apache.axis2.namespace.Constants.NS_URI_WSDL11.
                                equals(wsdlNamespace.getNamespaceURI())) {
                            wsdlToAxisServiceBuilder =
                                    new WSDL11ToAxisServiceBuilder(wsdlInputStream);
                        } else {
                            handleException("Unknown WSDL format.. not WSDL 1.1 or WSDL 2.0");
                        }

                        if (wsdlToAxisServiceBuilder == null) {
                            throw new SynapseException(
                                    "Could not get the WSDL to Axis Service Builder");
                        }

                        wsdlToAxisServiceBuilder.setBaseUri(wsdlURI != null ?
                                wsdlURI.toString() : synapseHome);

                        if (trace()) {
                            trace.info("Setting up custom resolvers");
                        }
                        
                        // load the UserDefined WSDLResolver and SchemaURIResolver implementations
                        if (synCfg.getProperty(SynapseConstants.SYNAPSE_WSDL_RESOLVER) != null &&
                                synCfg.getProperty(SynapseConstants.SYNAPSE_SCHEMA_RESOLVER) != null) {
                            setUserDefinedResourceResolvers(synCfg, wsdlInputStream,
                                    wsdlToAxisServiceBuilder);
                        } else {
                            //Use the Custom Resolvers
                            // Set up the URIResolver

                            if (resourceMap != null) {
                                // if the resource map is available use it
                                wsdlToAxisServiceBuilder.setCustomResolver(
                                        new CustomXmlSchemaURIResolver(resourceMap, synCfg));
                                // Axis 2 also needs a WSDLLocator for WSDL 1.1 documents
                                if (wsdlToAxisServiceBuilder instanceof WSDL11ToAxisServiceBuilder) {
                                    ((WSDL11ToAxisServiceBuilder)
                                            wsdlToAxisServiceBuilder).setCustomWSDLResolver(
                                            new CustomWSDLLocator(new InputSource(wsdlInputStream),
                                                    wsdlURI != null ? wsdlURI.toString() : "",
                                                    resourceMap, synCfg));
                                }
                            } else {
                                //if the resource map isn't available ,
                                //then each import URIs will be resolved using base URI
                                wsdlToAxisServiceBuilder.setCustomResolver(
                                        new CustomXmlSchemaURIResolver());
                                // Axis 2 also needs a WSDLLocator for WSDL 1.1 documents
                                if (wsdlToAxisServiceBuilder instanceof WSDL11ToAxisServiceBuilder) {
                                    ((WSDL11ToAxisServiceBuilder)
                                            wsdlToAxisServiceBuilder).setCustomWSDLResolver(
                                            new CustomWSDLLocator(new InputSource(wsdlInputStream),
                                                    wsdlURI != null ? wsdlURI.toString() : ""));
                                }
                            }
                        }
                        if (trace()) {
                            trace.info("Populating Axis2 service using WSDL");
                            if (trace.isTraceEnabled()) {
                                trace.trace("WSDL : " + wsdlElement.toString());
                            }
                        }
                        proxyService = wsdlToAxisServiceBuilder.populateService();

                        // this is to clear the bindings and ports already in the WSDL so that the
                        // service will generate the bindings on calling the printWSDL otherwise
                        // the WSDL which will be shown is same as the original WSDL except for the
                        // service name
                        proxyService.getEndpoints().clear();

                    } else {
                        handleException("Unknown WSDL format.. not WSDL 1.1 or WSDL 2.0");
                    }

                } catch (AxisFault af) {
                    handleException("Error building service from WSDL", af);
                } catch (IOException ioe) {
                    handleException("Error reading WSDL", ioe);
                }
            }
        } else if (wsdlFound) {
            handleException("Couldn't build the proxy service : " + name
                    + ". Unable to locate the specified WSDL to build the service");
        }

        // Set the name and description. Currently Axis2 uses the name as the
        // default Service destination
        if (proxyService == null) {
            throw new SynapseException("Could not create a proxy service");
        }
        proxyService.setName(name);
        if (description != null) {
            proxyService.setDocumentation(description);
        }

        // process transports and expose over requested transports. If none
        // is specified, default to all transports using service name as
        // destination
        if (transports == null || transports.size() == 0) {
            // default to all transports using service name as destination
        } else {
            if (trace()) trace.info("Exposing transports : " + transports);
            proxyService.setExposedTransports(transports);
        }

        // process parameters
        if (trace() && parameters.size() > 0) {
            trace.info("Setting service parameters : " + parameters);
        }
        for (Object o : parameters.keySet()) {
            String name = (String) o;
            Object value = parameters.get(name);

            Parameter p = new Parameter();
            p.setName(name);
            p.setValue(value);

            try {
                proxyService.addParameter(p);
            } catch (AxisFault af) {
                handleException("Error setting parameter : " + name + "" +
                    "to proxy service as a Parameter", af);
            }
        }

        if (JavaUtils.isTrueExplicitly(proxyService.getParameterValue(ABSOLUTE_SCHEMA_URL_PARAM))) {
            proxyService.setCustomSchemaNamePrefix("");
        }

        if (JavaUtils.isTrueExplicitly(proxyService.getParameterValue("disableOperationValidation"))){
            try {
                AxisOperation defaultOp = processOperationValidation(proxyService);
                //proxyServiceGroup.setParent(axisCfg);
            } catch (AxisFault axisFault) {
                // ignore
            }
        }

        if (!policies.isEmpty()) {

            for (PolicyInfo pi : policies) {
                if (getPolicyFromKey(pi.getPolicyKey(), synCfg) == null) {
                    handleException("Cannot find Policy from the key");
                }

                if (pi.isServicePolicy()) {

                    proxyService.getPolicySubject().attachPolicy(
                            getPolicyFromKey(pi.getPolicyKey(), synCfg));

                } else if (pi.isOperationPolicy()) {

                    AxisOperation op = proxyService.getOperation(pi.getOperation());
                    if (op != null) {
                        op.getPolicySubject().attachPolicy(
                                getPolicyFromKey(pi.getPolicyKey(), synCfg));

                    } else {
                        handleException("Couldn't find the operation specified " +
                                "by the QName : " + pi.getOperation());
                    }

                } else if (pi.isMessagePolicy()) {

                    if (pi.getOperation() != null) {

                        AxisOperation op = proxyService.getOperation(pi.getOperation());
                        if (op != null) {
                            op.getMessage(pi.getMessageLable()).getPolicySubject().attachPolicy(
                                    getPolicyFromKey(pi.getPolicyKey(), synCfg));
                        } else {
                            handleException("Couldn't find the operation " +
                                    "specified by the QName : " + pi.getOperation());
                        }

                    } else {
                        // operation is not specified and hence apply to all the applicable messages
                        for (Iterator itr = proxyService.getOperations(); itr.hasNext();) {
                            Object obj = itr.next();
                            if (obj instanceof AxisOperation) {
                                // check whether the policy is applicable
                                if (!((obj instanceof OutOnlyAxisOperation && pi.getType()
                                        == PolicyInfo.MESSAGE_TYPE_IN) ||
                                        (obj instanceof InOnlyAxisOperation
                                        && pi.getType() == PolicyInfo.MESSAGE_TYPE_OUT))) {

                                    AxisMessage message = ((AxisOperation)
                                            obj).getMessage(pi.getMessageLable());
                                    message.getPolicySubject().attachPolicy(
                                            getPolicyFromKey(pi.getPolicyKey(), synCfg));
                                }
                            }
                        }
                    }
                } else {
                    handleException("Undefined Policy type");
                }
            }
        }

        // create a custom message receiver for this proxy service 
        ProxyServiceMessageReceiver msgRcvr = new ProxyServiceMessageReceiver();
        msgRcvr.setName(name);
        msgRcvr.setProxy(this);

        Iterator iter = proxyService.getOperations();
        while (iter.hasNext()) {
            AxisOperation op = (AxisOperation) iter.next();
            op.setMessageReceiver(msgRcvr);
        }

        try {
            proxyService.addParameter(
                    SynapseConstants.SERVICE_TYPE_PARAM_NAME, SynapseConstants.PROXY_SERVICE_TYPE);
            if (serviceGroup == null) {
                auditInfo("Adding service " + name + " to the Axis2 configuration");
                axisCfg.addService(proxyService);
            } else {
                auditInfo("Adding service " + name + " to the service group " + serviceGroup);
                if (axisCfg.getServiceGroup(serviceGroup) == null) {
                    // If the specified group does not exist we should create it
                    AxisServiceGroup proxyServiceGroup = new AxisServiceGroup();
                    proxyServiceGroup.setServiceGroupName(serviceGroup);
                    proxyServiceGroup.setParent(axisCfg);
                    // Add  the service to the new group and add the group the AxisConfiguration
                    proxyServiceGroup.addService(proxyService);
                    axisCfg.addServiceGroup(proxyServiceGroup);
                } else {
                    // Simply add the service to the existing group
                    proxyService.setParent(axisCfg.getServiceGroup(serviceGroup));
                    axisCfg.addServiceToExistingServiceGroup(proxyService, serviceGroup);
                }
            }
            this.setRunning(true);
        } catch (AxisFault axisFault) {
            try {
                if (axisCfg.getService(proxyService.getName()) != null) {
                    if (trace()) trace.info("Removing service " + name + " due to error : "
                        + axisFault.getMessage());
                    axisCfg.removeService(proxyService.getName());
                }
            } catch (AxisFault ignore) {}
            handleException("Error adding Proxy service to the Axis2 engine", axisFault);
        }

        // should Addressing be engaged on this service?
        if (wsAddrEnabled) {
            auditInfo("WS-Addressing is enabled for service : " + name);
            try {
                proxyService.engageModule(axisCfg.getModule(
                    SynapseConstants.ADDRESSING_MODULE_NAME), axisCfg);
            } catch (AxisFault axisFault) {
                handleException("Error loading WS Addressing module on proxy service : " + name, axisFault);
            }
        }

        // should RM be engaged on this service?
        if (wsRMEnabled) {
            auditInfo("WS-Reliable messaging is enabled for service : " + name);
            try {
                proxyService.engageModule(axisCfg.getModule(
                    SynapseConstants.RM_MODULE_NAME), axisCfg);
            } catch (AxisFault axisFault) {
                handleException("Error loading WS RM module on proxy service : " + name, axisFault);
            }
        }

        // should Security be engaged on this service?
        if (wsSecEnabled) {
            auditInfo("WS-Security is enabled for service : " + name);
            try {
                proxyService.engageModule(axisCfg.getModule(
                    SynapseConstants.SECURITY_MODULE_NAME), axisCfg);
            } catch (AxisFault axisFault) {
                handleException("Error loading WS Sec module on proxy service : "
                        + name, axisFault);
            }
        }

        moduleEngaged = wsSecEnabled || wsRMEnabled || wsAddrEnabled;
        wsdlPublished = wsdlFound;

        auditInfo("Successfully created the Axis2 service for Proxy service : " + name);
        return proxyService;
    }

    private void setUserDefinedResourceResolvers(SynapseConfiguration synCfg,
                                                 InputStream wsdlInputStream,
                                                 WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder) {
        String wsdlResolverName = synCfg.getProperty(SynapseConstants.SYNAPSE_WSDL_RESOLVER);
        String schemaResolverName = synCfg.getProperty(SynapseConstants.SYNAPSE_SCHEMA_RESOLVER);
        Class wsdlClazz, schemaClazz;
        Object wsdlClzzObject, schemaClazzObject;
        try {
            wsdlClazz = Class.forName(wsdlResolverName);
            schemaClazz = Class.forName(schemaResolverName);
        } catch (ClassNotFoundException e) {
            String msg =
                    "System could not find the class defined for the specific properties" +
                            " \n WSDLResolverImplementation:" + wsdlResolverName +
                            "\n SchemaResolverImplementation:" + schemaResolverName;
            handleException(msg, e);
            return;
        }

        try {
            wsdlClzzObject = wsdlClazz.newInstance();
            schemaClazzObject = schemaClazz.newInstance();
        } catch (Exception e) {
            String msg = "Could not create an instance from the class";
            handleException(msg, e);
            return;
        }

        UserDefinedXmlSchemaURIResolver userDefSchemaResolver =
                (UserDefinedXmlSchemaURIResolver) schemaClazzObject;
        userDefSchemaResolver.init(resourceMap, synCfg, wsdlKey);

        wsdlToAxisServiceBuilder.setCustomResolver(userDefSchemaResolver);
        if (wsdlToAxisServiceBuilder instanceof WSDL11ToAxisServiceBuilder) {
            UserDefinedWSDLLocator userDefWSDLLocator = (UserDefinedWSDLLocator) wsdlClzzObject;
            userDefWSDLLocator.init(new InputSource(wsdlInputStream),
                    wsdlURI != null ? wsdlURI.toString() : "", resourceMap, synCfg,
                    wsdlKey);
            ((WSDL11ToAxisServiceBuilder) wsdlToAxisServiceBuilder).
                    setCustomWSDLResolver(userDefWSDLLocator);
        }
    }

    private Policy getPolicyFromKey(String key, SynapseConfiguration synCfg) {

        synCfg.getEntryDefinition(key);
        return PolicyEngine.getPolicy(
                SynapseConfigUtils.getStreamSource(synCfg.getEntry(key)).getInputStream());
    }

    /**
     * Start the proxy service
     * @param synCfg the synapse configuration
     */
    public void start(SynapseConfiguration synCfg) {
        AxisConfiguration axisConfig = synCfg.getAxisConfiguration();
        if (axisConfig != null) {

            Parameter param = axisConfig.getParameter(SynapseConstants.SYNAPSE_ENV);
            if (param != null && param.getValue() instanceof SynapseEnvironment)  {
                SynapseEnvironment env = (SynapseEnvironment) param.getValue();
                if (targetInLineInSequence != null) {
                    targetInLineInSequence.init(env);
                }
                if (targetInLineOutSequence != null) {
                    targetInLineOutSequence.init(env);
                }
                if (targetInLineFaultSequence != null) {
                    targetInLineFaultSequence.init(env);
                }
            } else {
                auditWarn("Unable to find the SynapseEnvironment. " +
                    "Components of the proxy service may not be initialized");
            }

            AxisService as = axisConfig.getServiceForActivation(this.getName());
            as.setActive(true);
            axisConfig.notifyObservers(new AxisEvent(AxisEvent.SERVICE_START, as), as);
            this.setRunning(true);
            auditInfo("Started the proxy service : " + name);
        } else {
            auditWarn("Unable to start proxy service : " + name +
                ". Couldn't access Axis configuration");
        }
    }

    /**
     * Stop the proxy service
     * @param synCfg the synapse configuration
     */
    public void stop(SynapseConfiguration synCfg) {
        AxisConfiguration axisConfig = synCfg.getAxisConfiguration();
        if (axisConfig != null) {

            if (targetInLineInSequence != null) {
                targetInLineInSequence.destroy();
            }
            if (targetInLineOutSequence != null) {
                targetInLineOutSequence.destroy();
            }
            if (targetInLineFaultSequence != null) {
                targetInLineFaultSequence.destroy();
            }

            AxisService as = axisConfig.getServiceForActivation(this.getName());
            //If an active AxisService is found
            if (as != null && as.isActive()) {
                as.setActive(false);
                axisConfig.notifyObservers(new AxisEvent(AxisEvent.SERVICE_STOP, as), as);
            }
            this.setRunning(false);
            auditInfo("Stopped the proxy service : " + name);
        } else {
            auditWarn("Unable to stop proxy service : " + name +
                ". Couldn't access Axis configuration");
        }
    }

    private void handleException(String msg) {
        serviceLog.error(msg);
        log.error(msg);
        if (trace()) trace.error(msg);
        throw new SynapseException(msg);
    }

    private void handleException(String msg, Exception e) {
        serviceLog.error(msg);
        log.error(msg, e);
        if (trace()) trace.error(msg + " :: " + e.getMessage());
        throw new SynapseException(msg, e);
    }

    /**
     * Write to the general log, as well as any service specific logs the audit message at INFO
     * @param message the INFO level audit message
     */
    private void auditInfo(String message) {
        log.info(message);
        serviceLog.info(message);
        if (trace()) {
            trace.info(message);
        }
    }

    /**
     * Write to the general log, as well as any service specific logs the audit message at WARN
     * @param message the WARN level audit message
     */
    private void auditWarn(String message) {
        log.warn(message);
        serviceLog.warn(message);
        if (trace()) {
            trace.warn(message);
        }
    }

    /**
     * Return true if tracing should be enabled
     * @return true if tracing is enabled for this service
     */
    private boolean trace() {
        return traceState == SynapseConstants.TRACING_ON;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList getTransports() {
        return transports;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public Map<String, Object> getParameterMap() {
        return this.parameters;
    }

    public void setTransports(ArrayList transports) {
        this.transports = transports;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    public String getTargetInSequence() {
        return targetInSequence;
    }

    public void setTargetInSequence(String targetInSequence) {
        this.targetInSequence = targetInSequence;
    }

    public String getTargetOutSequence() {
        return targetOutSequence;
    }

    public void setTargetOutSequence(String targetOutSequence) {
        this.targetOutSequence = targetOutSequence;
    }

    public String getWSDLKey() {
        return wsdlKey;
    }

    public void setWSDLKey(String wsdlKey) {
        this.wsdlKey = wsdlKey;
    }

    public List<String> getServiceLevelPolicies() {
        return serviceLevelPolicies;
    }

    public void addServiceLevelPolicy(String serviceLevelPolicy) {
        this.serviceLevelPolicies.add(serviceLevelPolicy);
    }

    public boolean isWsAddrEnabled() {
        return wsAddrEnabled;
    }

    public void setWsAddrEnabled(boolean wsAddrEnabled) {
        this.wsAddrEnabled = wsAddrEnabled;
    }

    public boolean isWsRMEnabled() {
        return wsRMEnabled;
    }

    public void setWsRMEnabled(boolean wsRMEnabled) {
        this.wsRMEnabled = wsRMEnabled;
    }

    public boolean isWsSecEnabled() {
        return wsSecEnabled;
    }

    public void setWsSecEnabled(boolean wsSecEnabled) {
        this.wsSecEnabled = wsSecEnabled;
    }

    public boolean isStartOnLoad() {
        return startOnLoad;
    }

    public void setStartOnLoad(boolean startOnLoad) {
        this.startOnLoad = startOnLoad;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
   
    /**
     * Returns the int value that indicate the tracing state
     *
     * @return Returns the int value that indicate the tracing state
     */
    public int getTraceState() {
        return traceState;
    }

    /**
     * Set the tracing State variable
     *
     * @param traceState tracing state
     */
    public void setTraceState(int traceState) {
        this.traceState = traceState;
    }

    public String getTargetFaultSequence() {
        return targetFaultSequence;
    }

    public void setTargetFaultSequence(String targetFaultSequence) {
        this.targetFaultSequence = targetFaultSequence;
    }

    public Object getInLineWSDL() {
        return inLineWSDL;
    }

    public void setInLineWSDL(Object inLineWSDL) {
        this.inLineWSDL = inLineWSDL;
    }

    public URI getWsdlURI() {
        return wsdlURI;
    }

    public void setWsdlURI(URI wsdlURI) {
        this.wsdlURI = wsdlURI;
    }

    public Endpoint getTargetInLineEndpoint() {
        return targetInLineEndpoint;
    }

    public void setTargetInLineEndpoint(Endpoint targetInLineEndpoint) {
        this.targetInLineEndpoint = targetInLineEndpoint;
    }

    public SequenceMediator getTargetInLineInSequence() {
        return targetInLineInSequence;
    }

    public void setTargetInLineInSequence(SequenceMediator targetInLineInSequence) {
        this.targetInLineInSequence = targetInLineInSequence;
    }

    public SequenceMediator getTargetInLineOutSequence() {
        return targetInLineOutSequence;
    }

    public void setTargetInLineOutSequence(SequenceMediator targetInLineOutSequence) {
        this.targetInLineOutSequence = targetInLineOutSequence;
    }

    public SequenceMediator getTargetInLineFaultSequence() {
        return targetInLineFaultSequence;
    }

    public void setTargetInLineFaultSequence(SequenceMediator targetInLineFaultSequence) {
        this.targetInLineFaultSequence = targetInLineFaultSequence;
    }

    public List getPinnedServers() {
        return pinnedServers;
    }

    public void setPinnedServers(List pinnedServers) {
        this.pinnedServers = pinnedServers;
    }

    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
    }

    public List<String> getInMessagePolicies() {
        return inMessagePolicies;
    }

    public void setInMessagePolicies(List<String> inMessagePolicies) {
        this.inMessagePolicies = inMessagePolicies;
    }

    public void addInMessagePolicy(String messagePolicy) {
        this.inMessagePolicies.add(messagePolicy);
    }

    public List<String> getOutMessagePolicies() {
        return outMessagePolicies;
    }

    public void setOutMessagePolicies(List<String> outMessagePolicies) {
        this.outMessagePolicies = outMessagePolicies;
    }

    public void addOutMessagePolicy(String messagePolicy) {
        this.outMessagePolicies.add(messagePolicy);
    }

    public List<PolicyInfo> getPolicies() {
        return policies;
    }

    public void setPolicies(List<PolicyInfo> policies) {
        this.policies = policies;
    }

    public void addPolicyInfo(PolicyInfo pi) {
        this.policies.add(pi);
    }

    public void configure(AspectConfiguration aspectConfiguration) {
        this.aspectConfiguration = aspectConfiguration;
    }

    public AspectConfiguration getAspectConfiguration() {
        return aspectConfiguration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public boolean isModuleEngaged() {
        return moduleEngaged;
    }
       

    public void setModuleEngaged(boolean moduleEngaged) {
    	this.moduleEngaged = moduleEngaged;
    }

	public boolean isWsdlPublished() {
        return wsdlPublished;
    }

    private AxisOperation processOperationValidation(AxisService proxyService) throws AxisFault {
    	  AxisOperation mediateOperation = new InOutAxisOperation(
                  SynapseConstants.SYNAPSE_OPERATION_NAME);
          // Set the names of the two messages so that Axis2 is able to produce a WSDL (see SYNAPSE-366):
          mediateOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).setName("in");
          mediateOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE).setName("out");
       // create a custom message receiver for this proxy service 
          ProxyServiceMessageReceiver msgRcvr = new ProxyServiceMessageReceiver();
          msgRcvr.setName(name);
          msgRcvr.setProxy(this);
          mediateOperation.setMessageReceiver(msgRcvr);
          mediateOperation.setParent(proxyService);
          proxyService.addParameter("_default_mediate_operation_", mediateOperation);
          return mediateOperation;
		
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ Proxy Service [ Name : ").append(name).append(" ] ]");
        return sb.toString();
    }

    public String getPublishWSDLEndpoint() {
        return publishWSDLEndpoint;
    }

    public void setPublishWSDLEndpoint(String publishWSDLEndpoint) {
        this.publishWSDLEndpoint = publishWSDLEndpoint;
    }
}
