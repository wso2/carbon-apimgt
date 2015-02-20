/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.message.senders.blocking;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.commons.httpclient.Header;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.SOAPUtils;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.MessageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockingMsgSenderUtils {

    /**
     * Fill client options extracting properties from the original message context and endpoint
     * definition
     *
     * @param endpoint endpoint definition
     * @param clientOptions target client options
     * @param synapseInMsgCtx original message context
     */
    public static void fillClientOptions(EndpointDefinition endpoint, Options clientOptions,
                                         MessageContext synapseInMsgCtx) {

        org.apache.axis2.context.MessageContext axisInMsgCtx
                = ((Axis2MessageContext) synapseInMsgCtx).getAxis2MessageContext();

        if (endpoint != null) {

            // if RM is enabled
            boolean wsRMEnabled = endpoint.isReliableMessagingOn();
            if (wsRMEnabled) {
                String wsRMPolicyKey = endpoint.getWsRMPolicyKey();
                if (wsRMPolicyKey != null) {
                    clientOptions.setProperty(
                            SynapseConstants.SANDESHA_POLICY,
                            MessageHelper.getPolicy(synapseInMsgCtx, wsRMPolicyKey));
                }
            }

            // if security is enabled
            boolean wsSecurityEnabled = endpoint.isSecurityOn();
            if (wsSecurityEnabled) {
                String wsSecPolicyKey = endpoint.getWsSecPolicyKey();
                if (wsSecPolicyKey != null) {
                    clientOptions.setProperty(
                            SynapseConstants.RAMPART_POLICY,
                            MessageHelper.getPolicy(synapseInMsgCtx, wsSecPolicyKey));
                } else {
                    String inboundWsSecPolicyKey = endpoint.getInboundWsSecPolicyKey();
                    String outboundWsSecPolicyKey = endpoint.getOutboundWsSecPolicyKey();
                    if (inboundWsSecPolicyKey != null) {
                        clientOptions.setProperty(SynapseConstants.RAMPART_IN_POLICY,
                                                  MessageHelper.getPolicy(
                                                          synapseInMsgCtx, inboundWsSecPolicyKey));
                    }
                    if (outboundWsSecPolicyKey != null) {
                        clientOptions.setProperty(SynapseConstants.RAMPART_OUT_POLICY,
                                                  MessageHelper.getPolicy(
                                                          synapseInMsgCtx, outboundWsSecPolicyKey));
                    }
                }
            }

            clientOptions.setUseSeparateListener(endpoint.isUseSeparateListener());
        }

        if (axisInMsgCtx.getSoapAction() != null) {
            clientOptions.setAction(axisInMsgCtx.getSoapAction());
        }

        clientOptions.setExceptionToBeThrownOnSOAPFault(
                "true".equals(synapseInMsgCtx.getProperty(SynapseConstants.FORCE_ERROR_PROPERTY)));
    }

    /**
     * Fill the target message context extracting the required properties of the original message
     * context and the endpoint
     *
     * @param endpoint endpoint definition
     * @param axisOutMsgCtx target message axis2 context
     * @param synapseInMsgCtx original synapse message context
     * @throws org.apache.axis2.AxisFault
     */
    public static void fillMessageContext(EndpointDefinition endpoint,
                                          org.apache.axis2.context.MessageContext axisOutMsgCtx,
                                          MessageContext synapseInMsgCtx)
            throws AxisFault {

        org.apache.axis2.context.MessageContext axisInMsgCtx
                = ((Axis2MessageContext) synapseInMsgCtx).getAxis2MessageContext();

        // Copy properties
        setProperties(axisInMsgCtx, axisOutMsgCtx);

        // Copy Transport headers
        setTransportHeaders(axisInMsgCtx, axisOutMsgCtx);

        // Endpoint format
        if (endpoint.getFormat() != null) {
            String format = endpoint.getFormat();
            if (SynapseConstants.FORMAT_POX.equals(format)) {
                axisOutMsgCtx.setDoingREST(true);
                axisOutMsgCtx.setProperty(
                        Constants.Configuration.MESSAGE_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                axisOutMsgCtx.setProperty(
                        Constants.Configuration.CONTENT_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);

            } else if (SynapseConstants.FORMAT_GET.equals(format)) {
                axisOutMsgCtx.setDoingREST(true);
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                                          Constants.Configuration.HTTP_METHOD_GET);
                axisOutMsgCtx.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                          org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_X_WWW_FORM);

            } else if (SynapseConstants.FORMAT_SOAP11.equals(format)) {
                axisOutMsgCtx.setDoingREST(false);
                axisOutMsgCtx.removeProperty(Constants.Configuration.MESSAGE_TYPE);
                // We need to set this explicitly here in case the request was not a POST
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                                          Constants.Configuration.HTTP_METHOD_POST);
                if (axisOutMsgCtx.getSoapAction() == null && axisOutMsgCtx.getWSAAction() != null) {
                    axisOutMsgCtx.setSoapAction(axisOutMsgCtx.getWSAAction());
                }
                if (!axisOutMsgCtx.isSOAP11()) {
                    SOAPUtils.convertSOAP12toSOAP11(axisOutMsgCtx);
                }
            } else if (SynapseConstants.FORMAT_SOAP12.equals(format)) {
                axisOutMsgCtx.setDoingREST(false);
                axisOutMsgCtx.removeProperty(Constants.Configuration.MESSAGE_TYPE);
                // We need to set this explicitly here in case the request was not a POST
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                                          Constants.Configuration.HTTP_METHOD_POST);
                if (axisOutMsgCtx.getSoapAction() == null && axisOutMsgCtx.getWSAAction() != null) {
                    axisOutMsgCtx.setSoapAction(axisOutMsgCtx.getWSAAction());
                }
                if (axisOutMsgCtx.isSOAP11()) {
                    SOAPUtils.convertSOAP11toSOAP12(axisOutMsgCtx);
                }
            } else if (SynapseConstants.FORMAT_REST.equals(format)) {
                if (axisInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD) != null) {
                    if (axisInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD).
                            toString().equals(Constants.Configuration.HTTP_METHOD_GET)
                        || axisInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD).
                            toString().equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
                        axisOutMsgCtx.removeProperty(Constants.Configuration.MESSAGE_TYPE);
                    }
                }
                axisOutMsgCtx.setDoingREST(true);
            }
        }

        // MTOM/SWA
        if (endpoint.isUseMTOM()) {
            axisOutMsgCtx.setDoingMTOM(true);
            // fix / workaround for AXIS2-1798
            axisOutMsgCtx.setProperty(
                    Constants.Configuration.ENABLE_MTOM,
                    Constants.VALUE_TRUE);
            axisOutMsgCtx.setDoingMTOM(true);

        } else if (endpoint.isUseSwa()) {
            axisOutMsgCtx.setDoingSwA(true);
            // fix / workaround for AXIS2-1798
            axisOutMsgCtx.setProperty(
                    Constants.Configuration.ENABLE_SWA,
                    Constants.VALUE_TRUE);
            axisOutMsgCtx.setDoingSwA(true);
        }

        if (endpoint.getCharSetEncoding() != null) {
            axisOutMsgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                                      endpoint.getCharSetEncoding());
        }

        // HTTP Endpoint : use the specified HTTP method TODO: Remove this after refactoring Http Endpoint logic
        if (endpoint.isHTTPEndpoint()) {
            axisOutMsgCtx.setProperty(
                    Constants.Configuration.HTTP_METHOD,
                    synapseInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD));
        }

        boolean isRest = SynapseConstants.FORMAT_REST.equals(endpoint.getFormat()) |
                         axisInMsgCtx.isDoingREST();
        if (!isRest && !endpoint.isForceSOAP11() && !endpoint.isForceSOAP12()) {
            isRest = isRequestRest(axisInMsgCtx);
        }
        String restURLPostfix = (String) axisOutMsgCtx.getProperty(NhttpConstants.REST_URL_POSTFIX);

        if (endpoint.getAddress() != null) {
            String address = endpoint.getAddress(synapseInMsgCtx);
            if (isRest && restURLPostfix != null && !"".equals(restURLPostfix)) {
                address = getEPRWithRestURLPostfix(restURLPostfix, address);
            }
            axisOutMsgCtx.setTo(new EndpointReference(address));
        } else {
            EndpointReference endpointReference = axisOutMsgCtx.getTo();
            if (endpointReference != null) {
                if (isRest && restURLPostfix != null && !"".equals(restURLPostfix)) {
                    String address = endpointReference.getAddress();
                    address = getEPRWithRestURLPostfix(restURLPostfix, address);
                    axisOutMsgCtx.setTo(new EndpointReference(address));
                } else {
                    axisInMsgCtx.setTo(endpointReference);
                }
            }
        }

        if (endpoint.isUseSeparateListener()) {
            axisOutMsgCtx.getOptions().setUseSeparateListener(true);
        }

        // set the SEND_TIMEOUT for transport sender
        if (endpoint.getTimeoutDuration() > 0) {
            axisOutMsgCtx.setProperty(SynapseConstants.SEND_TIMEOUT, endpoint.getTimeoutDuration());
        }

        // Check for preserve WS-Addressing
        String preserveAddressingProperty = (String) synapseInMsgCtx.getProperty(
                SynapseConstants.PRESERVE_WS_ADDRESSING);
        if (preserveAddressingProperty != null && Boolean.parseBoolean(preserveAddressingProperty)) {
            axisOutMsgCtx.setMessageID(axisInMsgCtx.getMessageID());
        } else {
            MessageHelper.removeAddressingHeaders(axisOutMsgCtx);
            axisOutMsgCtx.setProperty(
                                AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
        }

    }

    /**
     * Get the modified EPR with rest url postfix
     * @param restURLPostfix Rest URL postfix
     * @param address original EPR
    */
    private static String getEPRWithRestURLPostfix(String restURLPostfix, String address) {
        String url;
        if (!address.endsWith("/") && !restURLPostfix.startsWith("/") &&
            !restURLPostfix.startsWith("?")) {
            url = address + "/" + restURLPostfix;
        } else if (address.endsWith("/") && restURLPostfix.startsWith("/")) {
            url = address + restURLPostfix.substring(1);
        } else if (address.endsWith("/") && restURLPostfix.startsWith("?")) {
            url = address.substring(0, address.length() - 1) + restURLPostfix;
        } else {
            url = address + restURLPostfix;
        }
        return url;
    }

    /**
     * Whether the original request received by the synapse is REST
     *
     * @param originalInMsgCtx request message
     * @return <code>true</code> if the request was a REST request
     */
    private static boolean isRequestRest(org.apache.axis2.context.MessageContext originalInMsgCtx) {

        boolean isRestRequest =
                originalInMsgCtx.getProperty(NhttpConstants.REST_REQUEST_CONTENT_TYPE) != null;

        if (!isRestRequest) {

            String httpMethod = (String) originalInMsgCtx.getProperty(
                    Constants.Configuration.HTTP_METHOD);

            isRestRequest = Constants.Configuration.HTTP_METHOD_GET.equals(httpMethod)
                            || Constants.Configuration.HTTP_METHOD_DELETE.equals(httpMethod)
                            || Constants.Configuration.HTTP_METHOD_PUT.equals(httpMethod)
                            || RESTConstants.METHOD_OPTIONS.equals(httpMethod)
                            || Constants.Configuration.HTTP_METHOD_HEAD.equals(httpMethod);

            if (!isRestRequest) {

                isRestRequest = Constants.Configuration.HTTP_METHOD_POST.equals(httpMethod)
                                && HTTPTransportUtils.isRESTRequest(
                        String.valueOf(originalInMsgCtx.getProperty(
                                Constants.Configuration.MESSAGE_TYPE)));
            }
        }
        return isRestRequest;
    }

    /**
     * Set message context properties extracting from the original message context
     * @param axisInMsgCtx original message context
     * @param axisOutMsgCtx target message context
     */
    private static void setProperties(org.apache.axis2.context.MessageContext axisInMsgCtx,
                                      org.apache.axis2.context.MessageContext axisOutMsgCtx) {
        for (String propertyName : allowedProperties) {
            Object property = axisInMsgCtx.getProperty(propertyName);
            if (property != null) {
                axisOutMsgCtx.setProperty(propertyName, property);
            }
        }
    }

    private static String[] allowedProperties = {"JSON_OBJECT",
                                                 "JSON_STRING",
                                                 Constants.Configuration.HTTP_METHOD,
                                                 Constants.Configuration.MESSAGE_TYPE,
                                                 Constants.Configuration.CONTENT_TYPE,
                                                 NhttpConstants.REST_URL_POSTFIX};

    /**
     * Set transport header extracting headers from the original message context
     *
     * @param axisInMsgCtx original message context
     * @param axisOutMsgCtx target message context
     */
    private static void setTransportHeaders(org.apache.axis2.context.MessageContext axisInMsgCtx,
                                            org.apache.axis2.context.MessageContext axisOutMsgCtx) {

        Object headers = axisInMsgCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        List<Header> list = new ArrayList<Header>();
        if (headers != null && headers instanceof Map) {
            Map headersMap = (Map) headers;
            for (Object next : headersMap.keySet()) {
                if (isSkipTransportHeader(next.toString())) {
                    continue;
                }
                Object value = headersMap.get(next);
                if (next instanceof String && value instanceof String) {
                    Header header = new Header(next.toString(), value.toString());
                    list.add(header);
                }
            }
        }
        axisOutMsgCtx.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, list);
    }

    private static boolean isSkipTransportHeader(String headerName) {

        return HTTP.CONN_DIRECTIVE.equalsIgnoreCase(headerName) ||
               HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName) ||
               HTTP.DATE_HEADER.equalsIgnoreCase(headerName) ||
               HTTP.CONTENT_TYPE.equalsIgnoreCase(headerName) ||
               HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) ||
               HTTP.SERVER_HEADER.equalsIgnoreCase(headerName) ||
               HTTP.USER_AGENT.equalsIgnoreCase(headerName) ||
               "SOAPAction".equalsIgnoreCase(headerName);

    }

}
