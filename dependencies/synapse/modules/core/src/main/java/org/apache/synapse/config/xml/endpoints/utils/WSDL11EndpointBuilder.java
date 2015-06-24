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

package org.apache.synapse.config.xml.endpoints.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.util.resolver.CustomWSDLLocator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Builds the EndpointDefinition containing the details for an epr using a WSDL 1.1 document.
 */
public class WSDL11EndpointBuilder {

    private static Log log = LogFactory.getLog(WSDL11EndpointBuilder.class);

    /**
     * Creates an EndpointDefinition for WSDL endpoint from an inline WSDL supplied in the WSDL
     * endpoint configuration.
     *
     * @param endpointDefinition the endpoint definition to populate
     * @param baseUri base uri of the wsdl
     * @param wsdl    OMElement representing the inline WSDL
     * @param service Service of the endpoint
     * @param port    Port of the endpoint
     * @return EndpointDefinition containing the information retrieved from the WSDL
     */
    public EndpointDefinition populateEndpointDefinitionFromWSDL(
            EndpointDefinition endpointDefinition,
            String baseUri, OMElement wsdl,
            String service, String port) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            wsdl.serialize(baos);
            InputStream in = new ByteArrayInputStream(baos.toByteArray());
            InputSource inputSource = new InputSource(in);
            WSDLLocator wsdlLocator = new CustomWSDLLocator(inputSource, baseUri);
            Document doc = null;
            try {
                doc = XMLUtils.newDocument(inputSource);
            } catch (ParserConfigurationException e) {
                handleException("Parser Configuration Error", e);
            } catch (SAXException e) {
                handleException("Parser SAX Error", e);
            } catch (IOException e) {
                handleException(WSDLException.INVALID_WSDL + "IO Error", e);
            }
            if (doc != null) {
                WSDLFactory fac = WSDLFactory.newInstance();
                WSDLReader reader = fac.newWSDLReader();
                Definition definition = reader.readWSDL(wsdlLocator, doc.getDocumentElement());
                return createEndpointDefinitionFromWSDL(
                        endpointDefinition, definition, service, port);
            }
        } catch (XMLStreamException e) {
            handleException("Error retrieving the WSDL definition from the inline WSDL.", e);
        } catch (WSDLException e) {
            handleException("Error retrieving the WSDL definition from the inline WSDL.", e);
        }

        return null;
    }

    private EndpointDefinition createEndpointDefinitionFromWSDL(
            EndpointDefinition endpointDefinition,
            Definition definition,
            String serviceName, String portName) {

        if (definition == null) {
            handleException("WSDL document is not specified.");
        }

        if (serviceName == null) {
            handleException("Service name of the WSDL document is not specified.");
        }

        if (portName == null) {
            handleException("Port name of the WSDL document is not specified.");
        }


        String serviceURL = null;
        // get soap version from wsdl port and update endpoint definition below
        // so that correct soap version is used when endpoint is called
        String format = null;
        assert definition != null;
        String tns = definition.getTargetNamespace();
        Service service = definition.getService(new QName(tns, serviceName));
        if (service != null) {
            Port port = service.getPort(portName);
            if (port != null) {
                for (Object obj : port.getExtensibilityElements()) {
                    if (obj instanceof SOAPAddress) {
                        SOAPAddress address = (SOAPAddress) obj;
                        serviceURL = address.getLocationURI();
                        format = SynapseConstants.FORMAT_SOAP11;
                        break;
                    } else if (obj instanceof SOAP12Address) {
                        SOAP12Address address = (SOAP12Address) obj;
                        serviceURL = address.getLocationURI();
                        format = SynapseConstants.FORMAT_SOAP12;
                        break;
                    } else if (obj instanceof HTTPAddress) {
                        HTTPAddress address = (HTTPAddress) obj;
                        serviceURL = address.getLocationURI();
                        format = SynapseConstants.FORMAT_REST;
                        Binding binding = port.getBinding();
                        if (binding == null) {
                            continue;
                        }
                        for (Object portElement : binding.getExtensibilityElements()) {
                            if (portElement instanceof HTTPBinding) {
                                HTTPBinding httpBinding = (HTTPBinding) portElement;
                                String verb = httpBinding.getVerb();
                                if (verb == null || "".equals(verb)) {
                                    continue;
                                }
                                if ("POST".equals(verb.toUpperCase())) {
                                    format = SynapseConstants.FORMAT_REST;
                                } else if ("GET".equals(verb.toUpperCase())) {
                                    format = SynapseConstants.FORMAT_GET;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (serviceURL != null) {
            endpointDefinition.setAddress(serviceURL);
            if (SynapseConstants.FORMAT_SOAP11.equals(format)) {
                endpointDefinition.setForceSOAP11(true);
            } else if (SynapseConstants.FORMAT_SOAP12.equals(format)) {
                endpointDefinition.setForceSOAP12(true);
            } else if (SynapseConstants.FORMAT_REST.equals(format)){
                endpointDefinition.setForceREST(true);
            } else if (SynapseConstants.FORMAT_GET.equals(format)){
                endpointDefinition.setForceGET(true);
            } else {
                handleException("format value '" + format + "' not yet implemented");
            }
            endpointDefinition.setFormat(format);

            // todo: determine this using wsdl and policy                                    

            return endpointDefinition;

        } else {
            handleException("Couldn't retrieve endpoint information from the WSDL.");
        }

        return null;
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
