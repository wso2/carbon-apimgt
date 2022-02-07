/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.ext.listener;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.payloadhandler.SynapsePayloadHandler;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.X509Certificate;
import javax.xml.stream.XMLStreamException;

/**
 * This util class is handling pre-process request, post-process request, pre-process response and post-process
 * utility operations. In simple terms, this class acts as an intermediate component between a handler and extension
 * listener implementation of that handler type.
 */
public class ExtensionListenerUtil {

    private static final Log log = LogFactory.getLog(ExtensionListenerUtil.class);

    private ExtensionListenerUtil() {

        throw new IllegalStateException("Utility class");
    }

    /**
     * Handles pre-process request by constructing the request context DTO, invoking the matching extension listener
     * implementation and processing the extension listener response.
     *
     * @param messageContext Synapse Message Context
     * @param type           Extension type
     * @return boolean indicating to continue normal handler response flow or respond back immediately
     */
    public static boolean preProcessRequest(MessageContext messageContext, String type) {

        ExtensionListener extensionListener = getExtensionListener(type);
        if (extensionListener != null) {
            RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
            ExtensionResponseDTO responseDTO = extensionListener.preProcessRequest(requestContextDTO);
            return handleExtensionResponse(messageContext, responseDTO);
        }
        return true;
    }

    /**
     * Handles post-process request by constructing the request context DTO, invoking the matching extension listener
     * implementation and processing the extension listener response.
     *
     * @param messageContext Synapse Message Context
     * @param type           Extension type
     * @return boolean indicating to continue normal handler response flow or respond back immediately
     */
    public static boolean postProcessRequest(MessageContext messageContext, String type) {

        ExtensionListener extensionListener = getExtensionListener(type);
        if (extensionListener != null) {
            RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
            ExtensionResponseDTO responseDTO = extensionListener.postProcessRequest(requestContextDTO);
            return handleExtensionResponse(messageContext, responseDTO);
        }
        return true;
    }

    /**
     * Handles pre-process response by constructing the response context DTO, invoking the matching extension listener
     * implementation and processing the extension listener response.
     *
     * @param messageContext Synapse Message Context
     * @param type           Extension type
     * @return boolean indicating to continue normal handler response flow or respond back immediately
     */
    public static boolean preProcessResponse(MessageContext messageContext, String type) {

        ExtensionListener extensionListener = getExtensionListener(type);
        if (extensionListener != null) {
            ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
            ExtensionResponseDTO responseDTO = extensionListener.preProcessResponse(responseContextDTO);
            return handleExtensionResponse(messageContext, responseDTO);
        }
        return true;
    }

    /**
     * Handles post-process response by constructing the response context DTO, invoking the matching extension listener
     * implementation and processing the extension listener response.
     *
     * @param messageContext Synapse Message Context
     * @param type           Extension type
     * @return boolean indicating to continue normal handler response flow or respond back immediately
     */
    public static boolean postProcessResponse(MessageContext messageContext, String type) {

        ExtensionListener extensionListener = getExtensionListener(type);
        if (extensionListener != null) {
            ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
            ExtensionResponseDTO responseDTO = extensionListener.postProcessResponse(responseContextDTO);
            return handleExtensionResponse(messageContext, responseDTO);
        }
        return true;
    }

    /**
     * Generates RequestContextDTO object using Synapse MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return RequestContextDTO
     */
    private static RequestContextDTO generateRequestContextDTO(MessageContext messageContext) {

        RequestContextDTO requestDTO = new RequestContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfo(messageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(messageContext);
        requestDTO.setApiRequestInfo(apiRequestInfoDTO);
        requestDTO.setMsgInfo(msgInfoDTO);
        requestDTO.setCustomProperty(getCustomPropertyMapFromMsgContext(messageContext));

        javax.security.cert.X509Certificate[] clientCerts = null;

        try {
            X509Certificate clientCertificate = Utils.getClientCertificate(
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext());

            if (clientCertificate != null) {
                clientCerts = new X509Certificate[]{clientCertificate};
            }
        } catch (APIManagementException e) {
            log.error("Error when getting client certificate", e);
        }
        requestDTO.setClientCerts(clientCerts);
        return requestDTO;
    }

    /**
     * Generates ResponseContextDTO object using Synapse MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return ResponseContextDTO
     */
    private static ResponseContextDTO generateResponseContextDTO(MessageContext messageContext) {

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfo(messageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(messageContext);
        responseContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        responseContextDTO.setMsgInfo(msgInfoDTO);
        responseContextDTO.setStatusCode((int) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(NhttpConstants.HTTP_SC));
        return responseContextDTO;
    }

    /**
     * Generates APIRequestInfoDTO object using Synapse MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return APIRequestInfoDTO
     */
    private static APIRequestInfoDTO generateAPIInfoDTO(MessageContext messageContext) {

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setContext((String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT));
        apiRequestInfoDTO.setVersion((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        apiRequestInfoDTO.setApiId((String) messageContext.getProperty(APIMgtGatewayConstants.API_UUID_PROPERTY));
        AuthenticationContext authenticationContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authenticationContext != null) {
            apiRequestInfoDTO.setUsername(authenticationContext.getUsername());
            apiRequestInfoDTO.setConsumerKey(authenticationContext.getConsumerKey());
        }
        return apiRequestInfoDTO;
    }

    /**
     * Populate common MsgInfoDTO properties for both Request and Response from MessageContext.
     *
     * @param messageContext Synapse MessageContext
     */
    private static MsgInfoDTO generateMessageInfo(MessageContext messageContext) {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        msgInfoDTO.setHeaders(getAxis2TransportHeaders(axis2MC));
        msgInfoDTO.setResource(GatewayUtils.extractResource(messageContext));
        msgInfoDTO.setElectedResource((String) messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE));
        //Add a payload handler instance for the current message context to consume the payload later
        msgInfoDTO.setPayloadHandler(new SynapsePayloadHandler(messageContext));
        Object correlationId = axis2MC.getProperty(CorrelationConstants.CORRELATION_ID);
        if (correlationId instanceof String){
            msgInfoDTO.setMessageId((String) correlationId);
        }
        msgInfoDTO.setHttpMethod((String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
        return msgInfoDTO;
    }

    /**
     * Evaluate and process ExtensionResponseDTO. Set transport headers, payload, custom property map, status code etc.
     * to message context. If an error response type, handle mediation via a given custom mediation handler.
     *
     * @param messageContext       Synapse Message Context
     * @param extensionResponseDTO ExtensionResponseDTO
     * @return true or false indicating continue or return response
     */
    private static boolean handleExtensionResponse(MessageContext messageContext,
                                                   ExtensionResponseDTO extensionResponseDTO) {

        if (extensionResponseDTO == null) {
            return true;    // if responseDTO is null, nothing to do hence continuing the normal flow
        }
        Map<String, String> headers = extensionResponseDTO.getHeaders();
        if (headers != null) {
            // if headers not sent back, the existing headers will not be changed
            ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        }
        // handle payload
        processResponsePayload(extensionResponseDTO, messageContext);
        // set customProperty map to send in throttle stream
        if (extensionResponseDTO.getCustomProperty() != null) {
            messageContext
                    .setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY, extensionResponseDTO.getCustomProperty());
        }
        // set Http Response status code
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE,
                extensionResponseDTO.getStatusCode());
        return evaluateExtensionResponseStatus(extensionResponseDTO, messageContext);
    }

    /**
     * Process Extension Response Payload in DTO and set it to the message context.
     *
     * @param extensionResponseDTO ExtensionResponseDTO
     * @param messageContext       Synapse MessageContext
     */
    private static void processResponsePayload(ExtensionResponseDTO extensionResponseDTO,
                                               MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        String contentType = getAxis2TransportHeaders(axis2MC).get(APIConstants.HEADER_CONTENT_TYPE);
        if (extensionResponseDTO.getPayload() != null && contentType != null) {
            // if payload null, not modifying existing payload
            try {
                String payload = IOUtils.toString(extensionResponseDTO.getPayload());
                // based on Content-Type header reset the payload
                if (StringUtils.equals(contentType, APIConstants.APPLICATION_JSON_MEDIA_TYPE)) {
                    JsonUtil.removeJsonPayload(axis2MC);
                    JsonUtil.getNewJsonPayload(axis2MC, payload, true, true);
                    axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    axis2MC.setProperty(Constants.Configuration.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                } else {
                    // by default treat payload in well formed xml format
                    OMElement omElement = AXIOMUtil.stringToOM(payload);
                    SOAPEnvelope env = TransportUtils.createSOAPEnvelope(omElement);
                    messageContext.setEnvelope(env);
                    axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            APIConstants.APPLICATION_XML_SOAP_MEDIA_TYPE);
                }
                axis2MC.removeProperty(APIConstants.NO_ENTITY_BODY);
            } catch (IOException | XMLStreamException e) {
                log.error("Error while setting payload " + axis2MC.getLogIDString(), e);
            }
        } else if (extensionResponseDTO.getPayload() == null && contentType == null &&
                ExtensionResponseStatus.RETURN_ERROR.toString().equals(extensionResponseDTO.getResponseStatus())) {
            axis2MC.setProperty(APIConstants.NO_ENTITY_BODY, true);
        }
    }

    /**
     * Evaluate Extension Response Status and process whether to continue the message flow or break the handler
     * execution and return response/fault.
     *
     * @param extensionResponseDTO ExtensionResponseDTO
     * @param messageContext       MessageContext
     * @return true or false indicating continue or return response
     */
    private static boolean evaluateExtensionResponseStatus(ExtensionResponseDTO extensionResponseDTO,
                                                           MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        String responseStatus = extensionResponseDTO.getResponseStatus();
        if (ExtensionResponseStatus.CONTINUE.toString().equals(responseStatus)) {
            if (log.isDebugEnabled()) {
                log.debug("Continuing the handler flow " + axis2MC.getLogIDString());
            }
            return true;
        } else if (ExtensionResponseStatus.RETURN_ERROR.toString().equals(responseStatus)) {
            if (log.isDebugEnabled()) {
                log.debug("Continuing the handler error flow " + axis2MC.getLogIDString());
            }
            messageContext.setProperty(SynapseConstants.ERROR_CODE,
                    APIConstants.ExtensionListenerConstants.API_EXTENSION_LISTENER_ERROR);
            messageContext.setProperty(SynapseConstants.ERROR_DETAIL,
                    APIConstants.ExtensionListenerConstants.API_EXTENSION_LISTENER_ERROR_MESSAGE);
        }
        if (ExtensionResponseStatus.RETURN_RESPONSE.toString().equals(responseStatus) ||
                ExtensionResponseStatus.RETURN_ERROR.toString().equals(responseStatus)) {
            // This property need to be set to avoid sending the content in pass-through pipe (request message)
            // as the response.
            axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            try {
                RelayUtils.discardRequestMessage(axis2MC);
            } catch (AxisFault axisFault) {
                // In case of an error it is logged and the process is continued because we have set a message in
                // the payload.
                log.error("Error occurred while discarding the message " + axis2MC.getLogIDString(), axisFault);
            }
            //break the handler flow and return back from the existing handler phase
            if (log.isDebugEnabled()) {
                log.debug("Exiting the handler flow and returning response back. " + axis2MC.getLogIDString());
            }
            Utils.send(messageContext, extensionResponseDTO.getStatusCode());
            return false;
        } else {
            log.error("Invalid extension response status received. Continuing the default flow."
                    + axis2MC.getLogIDString());
            return true;
        }
    }

    /**
     * Returns extension listener implementation for the given Extension type. If no listener implementations registered
     * for the given type, return null.
     *
     * @param type ExtensionType value
     * @return ExtensionListener implementation
     */
    private static ExtensionListener getExtensionListener(String type) {

        Map<String, ExtensionListener> extensionListeners = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getExtensionListenerMap();
        return extensionListeners.get(type);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getAxis2TransportHeaders(org.apache.axis2.context.MessageContext axis2MC) {

        return ((Map<String, String>) axis2MC
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getCustomPropertyMapFromMsgContext(MessageContext messageContext) {

        return (HashMap<String, Object>) messageContext.getProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY);
    }
}
