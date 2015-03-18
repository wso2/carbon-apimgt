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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.client.SandeshaClient;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.policy.SandeshaPolicyBean;
import org.apache.sandesha2.policy.builders.RMAssertionBuilder;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.MessageHelper;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * This is a simple client that handles both in only and in out
 */
public class Axis2FlexibleMEPClient {

    private static final Log log = LogFactory.getLog(Axis2FlexibleMEPClient.class);

    /**
     * Based on the Axis2 client code. Sends the Axis2 Message context out and returns
     * the Axis2 message context for the response.
     * <p/>
     * Here Synapse works as a Client to the service. It would expect 200 ok, 202 ok and
     * 500 internal server error as possible responses.
     *
     * @param endpoint                 the endpoint being sent to, maybe null
     * @param synapseOutMessageContext the outgoing synapse message
     * @throws AxisFault on errors
     */
    public static void send(

            EndpointDefinition endpoint,
            org.apache.synapse.MessageContext synapseOutMessageContext) throws AxisFault {

        boolean separateListener = false;
        boolean wsSecurityEnabled = false;
        String wsSecPolicyKey = null;
        String inboundWsSecPolicyKey = null;
        String outboundWsSecPolicyKey = null;
        boolean wsRMEnabled = false;
        String wsRMPolicyKey = null;
        boolean wsAddressingEnabled = false;
        String wsAddressingVersion = null;

        if (endpoint != null) {
            separateListener = endpoint.isUseSeparateListener();
            wsSecurityEnabled = endpoint.isSecurityOn();
            wsSecPolicyKey = endpoint.getWsSecPolicyKey();
            inboundWsSecPolicyKey = endpoint.getInboundWsSecPolicyKey();
            outboundWsSecPolicyKey = endpoint.getOutboundWsSecPolicyKey();
            wsRMEnabled = endpoint.isReliableMessagingOn();
            wsRMPolicyKey = endpoint.getWsRMPolicyKey();
            wsAddressingEnabled = endpoint.isAddressingOn() || wsRMEnabled;
            wsAddressingVersion = endpoint.getAddressingVersion();
        }

        if (log.isDebugEnabled()) {
            String to;
            if (endpoint != null && endpoint.getAddress() != null) {
                to = endpoint.getAddress(synapseOutMessageContext);
            } else {
                to = synapseOutMessageContext.getTo().toString();
            }

            log.debug(
                    "Sending [add = " + wsAddressingEnabled +
                            "] [sec = " + wsSecurityEnabled +
                            "] [rm = " + wsRMEnabled +
                            (endpoint != null ?
                                    "] [mtom = " + endpoint.isUseMTOM() +
                                            "] [swa = " + endpoint.isUseSwa() +
                                            "] [format = " + endpoint.getFormat() +
                                            "] [force soap11=" + endpoint.isForceSOAP11() +
                                            "] [force soap12=" + endpoint.isForceSOAP12() +
                                            "] [pox=" + endpoint.isForcePOX() +
                                            "] [get=" + endpoint.isForceGET() +
                                            "] [encoding=" + endpoint.getCharSetEncoding() : "") +
                            "] [to=" + to + "]");
        }

        // save the original message context without altering it, so we can tie the response
        MessageContext originalInMsgCtx
                = ((Axis2MessageContext) synapseOutMessageContext).getAxis2MessageContext();

        //TODO Temp hack: ESB removes the session id from request in a random manner.
        Map headers = (Map) originalInMsgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        String session = (String) synapseOutMessageContext.getProperty("LB_COOKIE_HEADER");
        if (session != null) {
            headers.put("Cookie", session);
        }

        // create a new MessageContext to be sent out as this should not corrupt the original
        // we need to create the response to the original message later on
        String preserveAddressingProperty = (String) synapseOutMessageContext.getProperty(
                SynapseConstants.PRESERVE_WS_ADDRESSING);
        MessageContext axisOutMsgCtx = cloneForSend(originalInMsgCtx, preserveAddressingProperty);


        if (log.isDebugEnabled()) {
            log.debug("Message [Original Request Message ID : "
                    + synapseOutMessageContext.getMessageID() + "]"
                    + " [New Cloned Request Message ID : " + axisOutMsgCtx.getMessageID() + "]");
        }
        // set all the details of the endpoint only to the cloned message context
        // so that we can use the original message context for resending through different endpoints
        if (endpoint != null) {

            if (SynapseConstants.FORMAT_POX.equals(endpoint.getFormat())) {
                axisOutMsgCtx.setDoingREST(true);
                axisOutMsgCtx.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                axisOutMsgCtx.setProperty(Constants.Configuration.CONTENT_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                
                Object o = axisOutMsgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    			Map _headers = (Map) o;
    			if (_headers != null) {
    				_headers.remove(HTTP.CONTENT_TYPE);
    				_headers.put(HTTP.CONTENT_TYPE, org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
    			}

            } else if (SynapseConstants.FORMAT_GET.equals(endpoint.getFormat())) {
                axisOutMsgCtx.setDoingREST(true);
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_GET);
                axisOutMsgCtx.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE,
                        org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_X_WWW_FORM);

            } else if (SynapseConstants.FORMAT_SOAP11.equals(endpoint.getFormat())) {
                axisOutMsgCtx.setDoingREST(false);
                axisOutMsgCtx.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                // We need to set this explicitly here in case the request was not a POST
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);
                if (axisOutMsgCtx.getSoapAction() == null && axisOutMsgCtx.getWSAAction() != null) {
                    axisOutMsgCtx.setSoapAction(axisOutMsgCtx.getWSAAction());
                }
                if (!axisOutMsgCtx.isSOAP11()) {
                    SOAPUtils.convertSOAP12toSOAP11(axisOutMsgCtx);
                }
                Object o = axisOutMsgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    			Map _headers = (Map) o;
    			if (_headers != null) {
    				_headers.remove(HTTP.CONTENT_TYPE);
    				_headers.put(HTTP.CONTENT_TYPE,  org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_TEXT_XML);
    			}

            } else if (SynapseConstants.FORMAT_SOAP12.equals(endpoint.getFormat())) {
                axisOutMsgCtx.setDoingREST(false);
                axisOutMsgCtx.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                // We need to set this explicitly here in case the request was not a POST
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);
                if (axisOutMsgCtx.getSoapAction() == null && axisOutMsgCtx.getWSAAction() != null) {
                    axisOutMsgCtx.setSoapAction(axisOutMsgCtx.getWSAAction());
                }
                if (axisOutMsgCtx.isSOAP11()) {
                    SOAPUtils.convertSOAP11toSOAP12(axisOutMsgCtx);
                }
                Object o = axisOutMsgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    			Map _headers = (Map) o;
    			if (_headers != null) {
                    _headers.remove(HTTP.CONTENT_TYPE);
                    
                if ( axisOutMsgCtx.getSoapAction() != null){
                         String actionHeaderPrefix = ";action=\""; 
                         String contentTypeWithAction = 
                                            new StringBuilder(org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML.length() 
                                            + axisOutMsgCtx.getSoapAction().length() + actionHeaderPrefix.length() + 1)
                                              .append(org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML)
                                              .append(actionHeaderPrefix)
                                              .append(axisOutMsgCtx.getSoapAction())
                                              .append('\"')
                                              .toString();
                         _headers.put(HTTP.CONTENT_TYPE, contentTypeWithAction);
                     }else{
                         _headers.put(HTTP.CONTENT_TYPE, org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML);
                     }
               }

            } else if (SynapseConstants.FORMAT_REST.equals(endpoint.getFormat())) {
                /*format=rest is kept only backword compatibility. We no longer needed that.*/
                /* Remove Message Type  for GET and DELETE Request */
                if (originalInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD) != null) {
                    if (originalInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD).
                            toString().equals(Constants.Configuration.HTTP_METHOD_GET)
                            || originalInMsgCtx.getProperty(Constants.Configuration.HTTP_METHOD).
                            toString().equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
                        axisOutMsgCtx.removeProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE);
                    }
                }
                axisOutMsgCtx.setDoingREST(true);
            } else {
                processWSDL2RESTRequestMessageType(originalInMsgCtx, axisOutMsgCtx);
            }

            if (endpoint.isUseMTOM()) {
                axisOutMsgCtx.setDoingMTOM(true);
                // fix / workaround for AXIS2-1798
                axisOutMsgCtx.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_MTOM,
                        org.apache.axis2.Constants.VALUE_TRUE);
                axisOutMsgCtx.setDoingMTOM(true);

            } else if (endpoint.isUseSwa()) {
                axisOutMsgCtx.setDoingSwA(true);
                // fix / workaround for AXIS2-1798
                axisOutMsgCtx.setProperty(
                        org.apache.axis2.Constants.Configuration.ENABLE_SWA,
                        org.apache.axis2.Constants.VALUE_TRUE);
                axisOutMsgCtx.setDoingSwA(true);
            }

            if (endpoint.getCharSetEncoding() != null) {
                axisOutMsgCtx.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                        endpoint.getCharSetEncoding());
            }

            // HTTP Endpoint : use the specified HTTP method and remove REST_URL_POSTFIX, it's not supported in HTTP Endpoint
            if (endpoint.isHTTPEndpoint()) {
                axisOutMsgCtx.setProperty(Constants.Configuration.HTTP_METHOD, synapseOutMessageContext.getProperty(Constants.Configuration.HTTP_METHOD));
                axisOutMsgCtx.removeProperty(NhttpConstants.REST_URL_POSTFIX);
            }

            // add rest request' suffix URI
            String restSuffix = (String) axisOutMsgCtx.getProperty(NhttpConstants.REST_URL_POSTFIX);
            boolean isRest = SynapseConstants.FORMAT_REST.equals(endpoint.getFormat());

            if (!isRest && !endpoint.isForceSOAP11() && !endpoint.isForceSOAP12()) {
                isRest = isRequestRest(originalInMsgCtx);
            }

            if (endpoint.getAddress() != null) {
                String address = endpoint.getAddress(synapseOutMessageContext);
                if (isRest && restSuffix != null && !"".equals(restSuffix)) {

                    String url;
                    if (!address.endsWith("/") && !restSuffix.startsWith("/") &&
                            !restSuffix.startsWith("?")) {
                        url = address + "/" + restSuffix;
                    } else if (address.endsWith("/") && restSuffix.startsWith("/")) {
                        url = address + restSuffix.substring(1);
                    } else if (address.endsWith("/") && restSuffix.startsWith("?")) {
                        url = address.substring(0, address.length() - 1) + restSuffix;
                    } else {
                        url = address + restSuffix;
                    }
                    axisOutMsgCtx.setTo(new EndpointReference(url));

                } else {
                    axisOutMsgCtx.setTo(
                            new EndpointReference(address));
                }
                axisOutMsgCtx.setProperty(NhttpConstants.ENDPOINT_PREFIX, address);
                synapseOutMessageContext.setProperty(SynapseConstants.ENDPOINT_PREFIX, address);
            } else {
                // Supporting RESTful invocation
                if (isRest && restSuffix != null && !"".equals(restSuffix)) {
                    EndpointReference epr = axisOutMsgCtx.getTo();
                    if (epr != null) {
                        String address = epr.getAddress();
                        String url;
                        if (!address.endsWith("/") && !restSuffix.startsWith("/") &&
                                !restSuffix.startsWith("?")) {
                            url = address + "/" + restSuffix;
                        } else {
                            url = address + restSuffix;
                        }
                        axisOutMsgCtx.setTo(new EndpointReference(url));
                    }
                }
            }

            if (endpoint.isUseSeparateListener()) {
                axisOutMsgCtx.getOptions().setUseSeparateListener(true);
            }
        } else {
            processWSDL2RESTRequestMessageType(originalInMsgCtx, axisOutMsgCtx);
        }

        // only put whttp:location for the REST (GET) requests, otherwise causes issues for POX messages
        if (axisOutMsgCtx.isDoingREST() && HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(
                axisOutMsgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE))) {
            if (axisOutMsgCtx.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION) == null
                    && axisOutMsgCtx.getEnvelope().getBody().getFirstElement() != null) {
                axisOutMsgCtx.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,
                        axisOutMsgCtx.getEnvelope().getBody().getFirstElement()
                                .getQName().getLocalPart());
            }
        }

        if (wsAddressingEnabled) {

            if (wsAddressingVersion != null &&
                    SynapseConstants.ADDRESSING_VERSION_SUBMISSION.equals(wsAddressingVersion)) {

                axisOutMsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Submission.WSA_NAMESPACE);

            } else if (wsAddressingVersion != null &&
                    SynapseConstants.ADDRESSING_VERSION_FINAL.equals(wsAddressingVersion)) {

                axisOutMsgCtx.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                        AddressingConstants.Final.WSA_NAMESPACE);
            }

            axisOutMsgCtx.setProperty
                    (AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
        } else {
            axisOutMsgCtx.setProperty
                    (AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
        }

        // remove the headers if we don't need to preserve them.
        // determine weather we need to preserve the processed headers
        String preserveHeaderProperty =
                (String) synapseOutMessageContext.getProperty(
                        SynapseConstants.PRESERVE_PROCESSED_HEADERS);
        if (preserveHeaderProperty == null || !Boolean.parseBoolean(preserveHeaderProperty)) {
            // default behaviour is to remove the headers
            MessageHelper.removeProcessedHeaders(axisOutMsgCtx,
                    (preserveAddressingProperty != null &&
                            Boolean.parseBoolean(preserveAddressingProperty)));
        }


        ConfigurationContext axisCfgCtx = axisOutMsgCtx.getConfigurationContext();
        AxisConfiguration axisCfg = axisCfgCtx.getAxisConfiguration();

        AxisService anoymousService =
                AnonymousServiceFactory.getAnonymousService(synapseOutMessageContext.getConfiguration(),
                        axisCfg, wsAddressingEnabled, wsRMEnabled, wsSecurityEnabled);
        // mark the anon services created to be used in the client side of synapse as hidden
        // from the server side of synapse point of view
        anoymousService.getParent().addParameter(SynapseConstants.HIDDEN_SERVICE_PARAM, "true");
        ServiceGroupContext sgc = new ServiceGroupContext(
                axisCfgCtx, (AxisServiceGroup) anoymousService.getParent());
        ServiceContext serviceCtx = sgc.getServiceContext(anoymousService);

        boolean outOnlyMessage = "true".equals(synapseOutMessageContext.getProperty(
                SynapseConstants.OUT_ONLY));

        // get a reference to the DYNAMIC operation of the Anonymous Axis2 service
        AxisOperation axisAnonymousOperation = anoymousService.getOperation(
                outOnlyMessage ?
                        new QName(AnonymousServiceFactory.OUT_ONLY_OPERATION) :
                        new QName(AnonymousServiceFactory.OUT_IN_OPERATION));

        Options clientOptions = MessageHelper.cloneOptions(originalInMsgCtx.getOptions());
        clientOptions.setUseSeparateListener(separateListener);
        // if RM is requested,
        if (wsRMEnabled) {
            // if a WS-RM policy is specified, use it
            if (wsRMPolicyKey != null) {
                Object property = synapseOutMessageContext.getEntry(wsRMPolicyKey);
                if (property instanceof OMElement) {
                    OMElement policyOMElement = (OMElement) property;
                    RMAssertionBuilder builder = new RMAssertionBuilder();
                    SandeshaPolicyBean sandeshaPolicyBean = (SandeshaPolicyBean) builder.build(policyOMElement, null);
                    Parameter policyParam = new Parameter(Sandesha2Constants.SANDESHA_PROPERTY_BEAN, sandeshaPolicyBean);
                    anoymousService.addParameter(policyParam);
                }
            }
        }

        // if security is enabled,
        if (wsSecurityEnabled) {
            // if a WS-Sec policy is specified, use it
            if (wsSecPolicyKey != null) {
                clientOptions.setProperty(
                        SynapseConstants.RAMPART_POLICY,
                        MessageHelper.getPolicy(synapseOutMessageContext, wsSecPolicyKey));
            } else {
                if (inboundWsSecPolicyKey != null) {
                    clientOptions.setProperty(SynapseConstants.RAMPART_IN_POLICY,
                            MessageHelper.getPolicy(
                                    synapseOutMessageContext, inboundWsSecPolicyKey));
                }
                if (outboundWsSecPolicyKey != null) {
                    clientOptions.setProperty(SynapseConstants.RAMPART_OUT_POLICY,
                            MessageHelper.getPolicy(
                                    synapseOutMessageContext, outboundWsSecPolicyKey));
                }
            }
            // temporary workaround for https://issues.apache.org/jira/browse/WSCOMMONS-197
            if (axisOutMsgCtx.getEnvelope().getHeader() == null) {
                SOAPFactory fac = axisOutMsgCtx.isSOAP11() ?
                        OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();
                fac.createSOAPHeader(axisOutMsgCtx.getEnvelope());
            }
        }

        OperationClient mepClient = axisAnonymousOperation.createClient(serviceCtx, clientOptions);
        mepClient.addMessageContext(axisOutMsgCtx);
        axisOutMsgCtx.setAxisMessage(
                axisAnonymousOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));

        // set the SEND_TIMEOUT for transport sender
        if (endpoint != null && endpoint.getTimeoutDuration() > 0) {
            axisOutMsgCtx.setProperty(SynapseConstants.SEND_TIMEOUT, endpoint.getTimeoutDuration());
        }


        // always set a callback as we decide if the send it blocking or non blocking within
        // the MEP client. This does not cause an overhead, as we simply create a 'holder'
        // object with a reference to the outgoing synapse message context
        // synapseOutMessageContext
        AsyncCallback callback = new AsyncCallback(axisOutMsgCtx, synapseOutMessageContext);
        if (!outOnlyMessage) {
            if (endpoint != null) {
                // set the timeout time and the timeout action to the callback, so that the
                // TimeoutHandler can detect timed out callbacks and take approprite action.
                callback.setTimeOutOn(System.currentTimeMillis() + endpoint.getTimeoutDuration());
                callback.setTimeOutAction(endpoint.getTimeoutAction());
            } else {
                callback.setTimeOutOn(System.currentTimeMillis());
            }

        }
        mepClient.setCallback(callback);
//
//        if (Utils.isClientThreadNonBlockingPropertySet(axisOutMsgCtx)) {
//            SynapseCallbackReceiver synapseCallbackReceiver = (SynapseCallbackReceiver) axisOutMsgCtx.getAxisOperation().getMessageReceiver();
//            synapseCallbackReceiver.addCallback(axisOutMsgCtx.getMessageID(), new FaultCallback(axisOutMsgCtx, synapseOutMessageContext));
//        }

        // this is a temporary fix for converting messages from HTTP 1.1 chunking to HTTP 1.0.
        // Without this HTTP transport can block & become unresponsive because we are streaming
        // HTTP 1.1 messages and HTTP 1.0 require the whole message to caculate the content length
        if (originalInMsgCtx.isPropertyTrue(NhttpConstants.FORCE_HTTP_1_0)) {
            synapseOutMessageContext.getEnvelope().toString();
        }

        // with the nio transport, this causes the listener not to write a 202
        // Accepted response, as this implies that Synapse does not yet know if
        // a 202 or 200 response would be written back.
        originalInMsgCtx.getOperationContext().setProperty(
                org.apache.axis2.Constants.RESPONSE_WRITTEN, "SKIP");

        // if the transport out is explicitly set use it
        Object o = originalInMsgCtx.getProperty("TRANSPORT_OUT_DESCRIPTION");
        if (o != null && o instanceof TransportOutDescription) {
            axisOutMsgCtx.setTransportOut((TransportOutDescription) o);
            clientOptions.setTransportOut((TransportOutDescription) o);
            clientOptions.setProperty("TRANSPORT_OUT_DESCRIPTION", o);
        }

        mepClient.execute(true);
        if (wsRMEnabled) {
            Object rm11 = clientOptions.getProperty(SandeshaClientConstants.RM_SPEC_VERSION);
            if ((rm11 != null) && rm11.equals(Sandesha2Constants.SPEC_VERSIONS.v1_1)) {
                ServiceClient serviceClient = new ServiceClient(
                        axisOutMsgCtx.getConfigurationContext(), axisOutMsgCtx.getAxisService());
                serviceClient.setTargetEPR(
                        new EndpointReference(endpoint.getAddress(synapseOutMessageContext)));
                serviceClient.setOptions(clientOptions);
                serviceClient.getOptions().setTo(
                        new EndpointReference(endpoint.getAddress(synapseOutMessageContext)));
                SandeshaClient.terminateSequence(serviceClient);
            }
        }
    }

    private static MessageContext cloneForSend(MessageContext ori, String preserveAddressing)
            throws AxisFault {

        MessageContext newMC = MessageHelper.clonePartially(ori);

        newMC.setEnvelope(ori.getEnvelope());
        if (preserveAddressing != null && Boolean.parseBoolean(preserveAddressing)) {
            newMC.setMessageID(ori.getMessageID());
        } else {
            MessageHelper.removeAddressingHeaders(newMC);
        }

        newMC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                ori.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));

        return newMC;
    }

    public static void clearSecurtityProperties(Options options) {

        Options current = options;
        while (current != null && current.getProperty(SynapseConstants.RAMPART_POLICY) != null) {
            current.setProperty(SynapseConstants.RAMPART_POLICY, null);
            current = current.getParent();
        }
    }

    /**
     * This is is a workaround for axis2 RestUtils behaviour
     * Based on an internal property and the http method, we set the message type
     *
     * @param originalInMsgCtx IN message
     * @param axisOutMsgCtx    Out message
     */
    private static void processWSDL2RESTRequestMessageType(MessageContext originalInMsgCtx,
                                                           MessageContext axisOutMsgCtx) {

        // TODO - this is a workaround for axis2 RestUtils behaviour
        Object restContentType =
                originalInMsgCtx.getProperty(NhttpConstants.REST_REQUEST_CONTENT_TYPE);

        if (restContentType == null) {

            String httpMethod = (String) originalInMsgCtx.getProperty(
                    Constants.Configuration.HTTP_METHOD);
            if (Constants.Configuration.HTTP_METHOD_GET.equals(httpMethod)
                    || Constants.Configuration.HTTP_METHOD_DELETE.equals(httpMethod)) {
                restContentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM;
            }
        }
          //Removed ESB 4.7.0 PPT 2013-06-28
//        if (restContentType != null && restContentType instanceof String) {
//            String contentType = TransportUtils.getContentType((String) restContentType, originalInMsgCtx);
//            axisOutMsgCtx.setProperty(
//                    org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, contentType);
//            originalInMsgCtx.setProperty(
//                    org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, contentType);
//        }
    }

    /**
     * Whether the original request received by the synapse is REST
     *
     * @param originalInMsgCtx request message
     * @return <code>true</code> if the request was a REST request
     */
    private static boolean isRequestRest(MessageContext originalInMsgCtx) {

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

                if(!isRestRequest) {
                    isRestRequest = (String.valueOf(originalInMsgCtx.getProperty(Constants.Configuration.MESSAGE_TYPE))
                        .equals(HTTPConstants.MEDIA_TYPE_TEXT_XML) && originalInMsgCtx.getSoapAction() == null);
                }
            }
        }
        return isRestRequest;
    }
}

// if(Utils.isClientThreadNonBlockingPropertySet(axisOutMsgCtx) ){
//SynapseCallbackReceiver synapseCallbackReceiver=(SynapseCallbackReceiver)  axisOutMsgCtx.getAxisOperation().getMessageReceiver();
//synapseCallbackReceiver.addCallback(synapseOutMessageContext.getMessageID(),new AsyncCallback(synapseOutMessageContext));
//
//}