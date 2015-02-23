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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SequenceType;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.config.xml.endpoints.EndpointFactory;
import org.apache.synapse.core.axis2.ProxyService;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.util.PolicyInfo;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Factory for {@link ProxyService} instances.
 * <p/>
 * Configuration syntax:
 * <pre>
 * &lt;proxy name="string" [transports="(http |https |jms )+|all"] [pinnedServers="(serverName )+" [trace="enable|disable"]&gt;
 *    &lt;description&gt;..&lt;/description&gt;?
 *    &lt;target [inSequence="name"] [outSequence="name"] [faultSequence="name"] [endpoint="name"]&gt;
 *       &lt;endpoint&gt;...&lt;/endpoint&gt;?
 *       &lt;inSequence&gt;...&lt;/inSequence&gt;?
 *       &lt;outSequence&gt;...&lt;/outSequence&gt;?
 *       &lt;faultSequence&gt;...&lt;/faultSequence&gt;?
 *    &lt;/target&gt;?
 *    &lt;publishWSDL uri=".." key="string"&gt;
 *       ( &lt;wsdl:definition&gt;...&lt;/wsdl:definition&gt; | &lt;wsdl20:description&gt;...&lt;/wsdl20:description&gt; )?
 *       &lt;resource location="..." key="..."/&gt;*
 *    &lt;/publishWSDL&gt;?
 *    &lt;enableAddressing/&gt;?
 *    &lt;enableSec/&gt;?
 *    &lt;enableRM/&gt;?
 *    &lt;policy key="string"/&gt;?
 *    &lt;policy key="string" type=(in | out)/&gt;?
 *       // optional service parameters
 *    &lt;parameter name="string"&gt;
 *       text | xml
 *    &lt;/parameter&gt;?
 * &lt;/proxy&gt;
 * </pre>
 */
public class ProxyServiceFactory {

    private static final Log log = LogFactory.getLog(ProxyServiceFactory.class);

    public static ProxyService createProxy(OMElement elem, Properties properties) {

        ProxyService proxy = null;

        OMAttribute name = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
        if (name == null) {
            handleException("The 'name' attribute is required for a Proxy service definition");
        } else {
            proxy = new ProxyService(name.getAttributeValue());
        }

        OMAttribute trans = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "transports"));
        if (trans != null) {
            String transports = trans.getAttributeValue();
            if (transports == null || ProxyService.ALL_TRANSPORTS.equals(transports)) {
                // default to all transports using service name as destination
            } else {
                StringTokenizer st = new StringTokenizer(transports, " ,");
                ArrayList<String> transportList = new ArrayList<String>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.length() != 0) {
                        transportList.add(token);
                    }
                }
                proxy.setTransports(transportList);
            }
        }

        OMAttribute pinnedServers = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "pinnedServers"));
        if (pinnedServers != null) {
            String pinnedServersValue = pinnedServers.getAttributeValue();
            if (pinnedServersValue == null) {
                // default to all servers
            } else {
                StringTokenizer st = new StringTokenizer(pinnedServersValue, " ,");
                List<String> pinnedServersList = new ArrayList<String>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.length() != 0) {
                      pinnedServersList.add(token);
                    }
                }
                proxy.setPinnedServers(pinnedServersList);
            }
        }
        
        OMAttribute trace = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.TRACE_ATTRIB_NAME));
        if (trace != null) {
            String traceValue = trace.getAttributeValue();
            if (traceValue != null) {
                if (traceValue.equals(XMLConfigConstants.TRACE_ENABLE)) {
                    proxy.setTraceState(org.apache.synapse.SynapseConstants.TRACING_ON);
                } else if (traceValue.equals(XMLConfigConstants.TRACE_DISABLE)) {
                    proxy.setTraceState(org.apache.synapse.SynapseConstants.TRACING_OFF);
                }
            }
        }
        OMAttribute startOnLoad = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "startOnLoad"));
        if (startOnLoad != null) {
            proxy.setStartOnLoad(Boolean.valueOf(startOnLoad.getAttributeValue()));
        } else {
            proxy.setStartOnLoad(true);
        }

        OMAttribute serviceGroup = elem.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "serviceGroup"));
        if (serviceGroup != null) {
            proxy.setServiceGroup(serviceGroup.getAttributeValue());
        }

        // setting the description of the proxy service
        OMElement descriptionElement = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "description"));
        if (descriptionElement != null) {
            proxy.setDescription(descriptionElement.getText().trim());
        }

        // read definition of the target of this proxy service. The target could be an 'endpoint'
        // or a named sequence. If none of these are specified, the messages would be mediated
        // by the Synapse main mediator
        OMElement target = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "target"));
        if (target != null) {
            boolean isTargetOk = false;
            SequenceMediatorFactory mediatorFactory = new SequenceMediatorFactory();
            OMAttribute inSequence = target.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "inSequence"));
            if (inSequence != null) {
                proxy.setTargetInSequence(inSequence.getAttributeValue());
                isTargetOk = true;
            } else {
                OMElement inSequenceElement = target.getFirstChildWithName(
                        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "inSequence"));
                if (inSequenceElement != null) {
                    SequenceMediator inSequenceMediator =
                            mediatorFactory.createAnonymousSequence(inSequenceElement, properties);
                    inSequenceMediator.setSequenceType(SequenceType.PROXY_INSEQ);
                    proxy.setTargetInLineInSequence(inSequenceMediator);
                    isTargetOk = true;
                }
            }
            OMAttribute outSequence = target.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "outSequence"));
            if (outSequence != null) {
                proxy.setTargetOutSequence(outSequence.getAttributeValue());
            } else {
                OMElement outSequenceElement = target.getFirstChildWithName(
                        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "outSequence"));
                if (outSequenceElement != null) {
                    SequenceMediator outSequenceMediator =
                            mediatorFactory.createAnonymousSequence(outSequenceElement, properties);
                    outSequenceMediator.setSequenceType(SequenceType.PROXY_OUTSEQ);
                    proxy.setTargetInLineOutSequence(outSequenceMediator);
                }
            }
            OMAttribute faultSequence = target.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "faultSequence"));
            if (faultSequence != null) {
                proxy.setTargetFaultSequence(faultSequence.getAttributeValue());
            } else {
                OMElement faultSequenceElement = target.getFirstChildWithName(
                        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "faultSequence"));
                if (faultSequenceElement != null) {
                    SequenceMediator faultSequenceMediator =
                            mediatorFactory.createAnonymousSequence(faultSequenceElement, properties);
                    faultSequenceMediator.setSequenceType(SequenceType.PROXY_FAULTSEQ);
                    proxy.setTargetInLineFaultSequence(faultSequenceMediator);
                }
            }
            OMAttribute tgtEndpt = target.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "endpoint"));
            if (tgtEndpt != null) {
                proxy.setTargetEndpoint(tgtEndpt.getAttributeValue());
                isTargetOk = true;
            } else {
                OMElement endpointElement = target.getFirstChildWithName(
                        new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "endpoint"));
                if (endpointElement != null) {
                    proxy.setTargetInLineEndpoint(EndpointFactory.getEndpointFromElement(
                            endpointElement, true, properties));
                    isTargetOk = true;
                }
            }
            if(!isTargetOk) {
                handleException("Target of the proxy service must declare " +
                        "either an inSequence or endpoint or both");
            }
        } else {
            handleException("Target is required for a Proxy service definition");
        }

        // read the WSDL, Schemas and Policies and set to the proxy service
        OMElement wsdl = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "publishWSDL"));
        if (wsdl != null) {
            OMAttribute wsdlEndpoint = wsdl.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "endpoint"));
            OMAttribute wsdlKey = wsdl.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
            if (wsdlEndpoint != null) {
                proxy.setPublishWSDLEndpoint(wsdlEndpoint.getAttributeValue());
            } else if (wsdlKey != null) {
                proxy.setWSDLKey(wsdlKey.getAttributeValue());
            } else {
                OMAttribute wsdlURI = wsdl.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "uri"));
                if (wsdlURI != null) {
                    try {
                        proxy.setWsdlURI(new URI(wsdlURI.getAttributeValue()));
                    } catch (URISyntaxException e) {
                        String msg = "Error creating uri for proxy service wsdl";
                        log.error(msg);
                        handleException(msg, e);
                    }
                } else {
                    OMElement wsdl11 = wsdl.getFirstChildWithName(
                            new QName(WSDLConstants.WSDL1_1_NAMESPACE, "definitions"));
                    if (wsdl11 != null) {
                        proxy.setInLineWSDL(wsdl11);
                    } else {
                        OMElement wsdl20 = wsdl.getFirstChildWithName(
                                new QName(WSDL2Constants.WSDL_NAMESPACE, "description"));
                        if (wsdl20 != null) {
                            proxy.setInLineWSDL(wsdl20);
                        }
                    }
                }
            }
            proxy.setResourceMap(ResourceMapFactory.createResourceMap(wsdl));
        }

        Iterator policies = elem.getChildrenWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "policy"));
        while (policies.hasNext()) {
            Object o = policies.next();
            if (o instanceof OMElement) {
                OMElement policy = (OMElement) o;
                OMAttribute key = policy.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "key"));
                OMAttribute type = policy.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "type"));
                OMAttribute operationName = policy.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "operationName"));
                OMAttribute operationNS = policy.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "operationNamespace"));

                if (key != null) {

                    PolicyInfo pi = new PolicyInfo(key.getAttributeValue());

                    if (type != null && type.getAttributeValue() != null) {
                        if ("in".equals(type.getAttributeValue())) {
                            pi.setType(PolicyInfo.MESSAGE_TYPE_IN);
                        } else if ("out".equals(type.getAttributeValue())) {
                            pi.setType(PolicyInfo.MESSAGE_TYPE_OUT);
                        } else {
                            handleException("Undefined policy type for the policy with key : "
                                    + key.getAttributeValue());
                        }
                    }

                    if (operationName != null && operationName.getAttributeValue() != null) {
                        if (operationNS != null && operationNS.getAttributeValue() != null) {
                            pi.setOperation(new QName(operationNS.getAttributeValue(),
                                    operationName.getAttributeValue()));
                        } else {
                            pi.setOperation(new QName(operationName.getAttributeValue()));
                        }
                    }

                    proxy.addPolicyInfo(pi);

                } else {
                    handleException("Policy element does not specify the policy key");
                }
            } else {
                handleException("Invalid 'policy' element found under element 'policies'");
            }
        }

        String nameString = proxy.getName();
        if (nameString == null || "".equals(nameString)) {
            nameString = SynapseConstants.ANONYMOUS_PROXYSERVICE;
        }
        AspectConfiguration aspectConfiguration = new AspectConfiguration(nameString);
        proxy.configure(aspectConfiguration);

        OMAttribute statistics = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE,
                XMLConfigConstants.STATISTICS_ATTRIB_NAME));
        if (statistics != null) {
            String statisticsValue = statistics.getAttributeValue();
            if (statisticsValue != null) {
                if (XMLConfigConstants.STATISTICS_ENABLE.equals(statisticsValue)) {
                    aspectConfiguration.enableStatistics();
                }
            }
        }

        Iterator props = elem.getChildrenWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "parameter"));
        while (props.hasNext()) {
            Object o = props.next();
            if (o instanceof OMElement) {
                OMElement prop = (OMElement) o;
                OMAttribute pname = prop.getAttribute(
                        new QName(XMLConfigConstants.NULL_NAMESPACE, "name"));
                OMElement propertyValue = prop.getFirstElement();
                if (pname != null) {
                    if (propertyValue != null) {
                        proxy.addParameter(pname.getAttributeValue(), propertyValue);
                    } else {
                        proxy.addParameter(pname.getAttributeValue(), prop.getText().trim());
                    }
                } else {
                    handleException("Invalid property specified for proxy service : " + name);
                }
            } else {
                handleException("Invalid property specified for proxy service : " + name);
            }
        }

        if (elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableAddressing")) != null) {
            proxy.setWsAddrEnabled(true);
        }

        if (elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableRM")) != null) {
            proxy.setWsRMEnabled(true);
        }

        if (elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableSec")) != null) {
            proxy.setWsSecEnabled(true);
        }

        return proxy;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

}
