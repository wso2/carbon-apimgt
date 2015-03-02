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

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.xml.endpoints.utils.WSDL11EndpointBuilder;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.WSDLEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.Properties;

/**
 * Creates an {@link WSDLEndpoint} based endpoint from a XML configuration.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;endpoint [name="<em>name</em>"]&gt;
 *   &lt;wsdl [uri="<em>WSDL location</em>"]
 *         service="<em>qualified name</em>" port="<em>qualified name</em>"
 *         [format="soap11|soap12|pox|get"] [optimize="mtom|swa"]
 *         [encoding="<em>charset encoding</em>"]
 *         [statistics="enable|disable"] [trace="enable|disable"]&gt;
 *     &lt;wsdl:definition&gt;...&lt;/wsdl:definition&gt;?
 *     &lt;wsdl20:description&gt;...&lt;/wsdl20:description&gt;?
 *
 *     &lt;enableRM [policy="<em>key</em>"]/&gt;?
 *     &lt;enableSec [policy="<em>key</em>"]/&gt;?
 *     &lt;enableAddressing [version="final|submission"] [separateListener="true|false"]/&gt;?
 *
 *     &lt;timeout&gt;
 *       &lt;duration&gt;<em>timeout duration in seconds</em>&lt;/duration&gt;
 *       &lt;responseAction&gt;discard|fault&lt;/responseAction&gt;
 *     &lt;/timeout&gt;?
 *
 *     &lt;suspendDurationOnFailure&gt;
 *       <em>suspend duration in seconds</em>
 *     &lt;/suspendDurationOnFailure&gt;?
 *   &lt;/wsdl&gt;
 * &lt;/endpoint&gt;
 * </pre>
 */
public class WSDLEndpointFactory extends DefaultEndpointFactory {

    public static final String SKIP_WSDL_PARSING = "skip.wsdl.parsing";

    private static WSDLEndpointFactory instance = new WSDLEndpointFactory();

    private WSDLEndpointFactory() {
    }

    public static WSDLEndpointFactory getInstance() {
        return instance;
    }

    protected Endpoint createEndpoint(OMElement epConfig, boolean anonymousEndpoint,
                                      Properties properties) {

        WSDLEndpoint wsdlEndpoint = new WSDLEndpoint();
        OMAttribute name = epConfig.getAttribute(new QName(
                org.apache.synapse.config.xml.XMLConfigConstants.NULL_NAMESPACE, "name"));

        if (name != null) {
            wsdlEndpoint.setName(name.getAttributeValue());
        }

        OMElement wsdlElement = epConfig.getFirstChildWithName
                (new QName(SynapseConstants.SYNAPSE_NAMESPACE, "wsdl"));
        if (wsdlElement != null) {

            DefinitionFactory fac = getEndpointDefinitionFactory();
            EndpointDefinition endpoint;
            if (fac == null) {
                fac = new EndpointDefinitionFactory();
                endpoint = fac.createDefinition(wsdlElement);
            } else {
                endpoint = fac.createDefinition(wsdlElement);
            }

            // for now, QOS information has to be provided explicitly.
            extractSpecificEndpointProperties(endpoint, wsdlElement);
            wsdlEndpoint.setDefinition(endpoint);
            processAuditStatus(endpoint, wsdlEndpoint.getName(), wsdlElement);

            // get the service name and port name. at this point we should not worry about
            // the presence of those parameters. they are handled by corresponding WSDL builders.
            String serviceName = wsdlElement.getAttributeValue(new QName("service"));
            String portName = wsdlElement.getAttributeValue(new QName("port"));
            // check if wsdl is supplied as a URI
            String wsdlURI = wsdlElement.getAttributeValue(new QName("uri"));

            // set serviceName and portName in the endpoint. it does not matter if these are
            // null at this point. we are setting them only for serialization purpose.
            wsdlEndpoint.setServiceName(serviceName);
            wsdlEndpoint.setPortName(portName);

            String noParsing = properties.getProperty(SKIP_WSDL_PARSING);

            if (wsdlURI != null) {
                wsdlEndpoint.setWsdlURI(wsdlURI.trim());
                if (noParsing == null || !JavaUtils.isTrueExplicitly(noParsing)) {
                    try {
                        OMNode wsdlOM = SynapseConfigUtils.getOMElementFromURL(new URL(wsdlURI)
                                .toString(), properties.get(SynapseConstants.SYNAPSE_HOME) != null ?
                                properties.get(SynapseConstants.SYNAPSE_HOME).toString() : "");
                        if (wsdlOM != null && wsdlOM instanceof OMElement) {
                            OMElement omElement = (OMElement) wsdlOM;
                            OMNamespace ns = omElement.getNamespace();
                            if (ns != null) {
                                String nsUri = omElement.getNamespace().getNamespaceURI();
                                if (org.apache.axis2.namespace.Constants.NS_URI_WSDL11.equals(nsUri)) {

                                    new WSDL11EndpointBuilder().
                                            populateEndpointDefinitionFromWSDL(endpoint,
                                                    wsdlURI.trim(), omElement, serviceName, portName);

                                } else if (WSDL2Constants.WSDL_NAMESPACE.equals(nsUri)) {
                                    //endpoint = new WSDL20EndpointBuilder().
                                    // createEndpointDefinitionFromWSDL(wsdlURI, serviceName, portName);

                                    handleException("WSDL 2.0 Endpoints are currently not supported");
                                }
                            }
                        }
                    } catch (ConnectException e) {
                        log.warn("Could not connect to the WSDL endpoint " + wsdlURI.trim(), e);
                    } catch (IOException e) {
                        log.warn("Could not read the WSDL endpoint " + wsdlURI.trim(), e);
                    } catch (Exception e) {
                        handleException("Couldn't create endpoint from the given WSDL URI : "
                                + e.getMessage(), e);
                    }
                }
            }

            // check if the wsdl 1.1 document is supplied inline
            OMElement definitionElement = wsdlElement.getFirstChildWithName
                    (new QName(org.apache.axis2.namespace.Constants.NS_URI_WSDL11, "definitions"));
            if (endpoint == null && definitionElement != null) {
                wsdlEndpoint.setWsdlDoc(definitionElement);

                if (noParsing == null || !JavaUtils.isTrueExplicitly(noParsing)) {
                    String resolveRoot = properties.get(SynapseConstants.RESOLVE_ROOT).toString();
                    String baseUri = "file:./";
                    if (resolveRoot != null) {
                        baseUri = resolveRoot.trim();
                    }
                    if (!baseUri.endsWith(File.separator)) {
                        baseUri = baseUri + File.separator;
                    }
                    new WSDL11EndpointBuilder().populateEndpointDefinitionFromWSDL(endpoint,
                            baseUri, definitionElement, serviceName, portName);
                } else {
                    endpoint = new EndpointDefinition();
                }
            }

            // check if a wsdl 2.0 document is supplied inline
            OMElement descriptionElement = wsdlElement.getFirstChildWithName
                    (new QName(WSDL2Constants.WSDL_NAMESPACE, "description"));
            if (endpoint == null && descriptionElement != null) {
                handleException("WSDL 2.0 Endpoints are currently not supported.");
            }
        }

        // process the parameters
        processProperties(wsdlEndpoint, epConfig);

        return wsdlEndpoint;
    }

}
