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
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.extension.listener.DefaultExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.ExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.payloadhandler.SynapsePayloadHandlerFactory;
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
    private static ExtensionListener defaultExtensionListener = new DefaultExtensionListener();

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

        RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.preProcessRequest(requestContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;    // if responseDTO is null, nothing to do hence continuing the normal flow
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

        RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.postProcessRequest(requestContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;     // if responseDTO is null, nothing to do hence continuing the normal flow
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

        ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.preProcessResponse(responseContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;     // if responseDTO is null, nothing to do hence continuing the normal flow
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

        ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.postProcessResponse(responseContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;     // if responseDTO is null, nothing to do hence continuing the normal flow
    }

    /**
     * Generates RequestContextDTO object using Synapse MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return RequestContextDTO
     */
    private static RequestContextDTO generateRequestContextDTO(MessageContext messageContext) {

        RequestContextDTO requestDTO = new RequestContextDTO();
        MsgInfoDTO msgInfoDTO = generateRequestMessageInfoDTO(messageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(messageContext);
        requestDTO.setApiRequestInfo(apiRequestInfoDTO);
        requestDTO.setMsgInfo(msgInfoDTO);
        requestDTO.setCustomProperty(
                (HashMap<String, Object>) messageContext.getProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY));
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object sslCertObject = axis2MC.getProperty(NhttpConstants.SSL_CLIENT_AUTH_CERT_X509);
        javax.security.cert.X509Certificate clientCert = null;
        if (sslCertObject != null) {
            javax.security.cert.X509Certificate[] certs = (X509Certificate[]) sslCertObject;
            clientCert = certs[0];
        }
        requestDTO.setClientCert(clientCert);
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
        MsgInfoDTO msgInfoDTO = generateResponseMessageInfoDTO(messageContext);
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
        apiRequestInfoDTO.setApiContext((String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT));
        apiRequestInfoDTO.setApiVersion((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        AuthenticationContext authenticationContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authenticationContext != null) {
            apiRequestInfoDTO.setUsername(authenticationContext.getUsername());
            apiRequestInfoDTO.setConsumerKey(authenticationContext.getConsumerKey());
        }
        return apiRequestInfoDTO;
    }

    /**
     * Generates MsgInfoDTO object using Request MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return MsgInfoDTO
     */
    private static MsgInfoDTO generateRequestMessageInfoDTO(MessageContext messageContext) {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        msgInfoDTO.setHttpMethod((String) (axis2MC.getProperty(Constants.Configuration.HTTP_METHOD)));
        populateCommonMessageInfo(messageContext, msgInfoDTO);
        return msgInfoDTO;
    }

    /**
     * Generates MsgInfoDTO object using Response MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @return MsgInfoDTO
     */
    private static MsgInfoDTO generateResponseMessageInfoDTO(MessageContext messageContext) {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod((String) messageContext.getProperty(RESTConstants.REST_METHOD));
        populateCommonMessageInfo(messageContext, msgInfoDTO);
        return msgInfoDTO;
    }

    /**
     * Populate common MsgInfoDTO properties for both Request and Response from MessageContext.
     *
     * @param messageContext Synapse MessageContext
     * @param msgInfoDTO     MsgInfoDTO
     */
    private static void populateCommonMessageInfo(MessageContext messageContext, MsgInfoDTO msgInfoDTO) {

        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        msgInfoDTO.setHeaders(
                (Map<String, String>) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        msgInfoDTO.setElectedResource(GatewayUtils.extractResource(messageContext));
        //Add a payload handler instance for the current message context to consume the payload later
        msgInfoDTO.setPayloadHandler(SynapsePayloadHandlerFactory.getInstance().buildPayloadHandler(messageContext));
        msgInfoDTO.setMessageId(axis2MC.getLogCorrelationID());
    }

    /**
     * Evaluate and process ExtensionResponseDTO. Set transport headers, payload, custom property map, status code etc.
     * to message context. If an error response type, handle mediation via a given custom mediation handler.
     *
     * @param messageContext       Synapse Message Context
     * @param extensionResponseDTO ExtensionResponseDTO
     */
    private static boolean processExtensionResponse(MessageContext messageContext,
                                                    ExtensionResponseDTO extensionResponseDTO,
                                                    String customErrorHandler) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        try {
            Map<String, String> headers = extensionResponseDTO.getHeaders();
            if (headers != null) {
                // if headers not sent back, the existing headers will not be changed
                axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                        extensionResponseDTO.getHeaders());
            }
            String contentType = ((Map<String, String>) axis2MC
                    .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                    .get(APIConstants.HEADER_CONTENT_TYPE);
            if (extensionResponseDTO.getPayload() != null && contentType != null) {
                // if payload null, not modifying existing payload
                String payload = IOUtils.toString(extensionResponseDTO.getPayload());
                // based on Content-Type header reset the payload
                if (StringUtils.equals(contentType, APIConstants.APPLICATION_JSON_MEDIA_TYPE)) {
                    JsonUtil.removeJsonPayload(axis2MC);
                    JsonUtil.getNewJsonPayload(axis2MC, payload, true, true);
                    axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                } else {
                    // by default treat payload in well formed xml format
                    OMElement omElement = AXIOMUtil.stringToOM(payload);
                    SOAPEnvelope env = TransportUtils.createSOAPEnvelope(omElement);
                    messageContext.setEnvelope(env);
                    axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            APIConstants.APPLICATION_XML_SOAP_MEDIA_TYPE);
                }
            }
        } catch (IOException | XMLStreamException e) {
            log.error("Error while setting payload", e);
        }
        // set customProperty map to send in throttle stream
        messageContext
                .setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY, extensionResponseDTO.getCustomProperty());
        // set Http Response status code
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE,
                extensionResponseDTO.getStatusCode());
        // evaluate extension response status
        String responseStatus = extensionResponseDTO.getResponseStatus();
        if (ExtensionResponseStatus.CONTINUE.toString().equals(responseStatus)) {
            //continue the handler flow
            return true;
        } else if (ExtensionResponseStatus.RETURN_ERROR.toString().equals(responseStatus) ||
                ExtensionResponseStatus.RETURN_RESPONSE.toString().equals(responseStatus)) {
            // This property need to be set to avoid sending the content in pass-through pipe (request message)
            // as the response.
            axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            try {
                RelayUtils.discardRequestMessage(axis2MC);
            } catch (AxisFault axisFault) {
                // In case of an error it is logged and the process is continued because we have set a message in
                // the payload.
                log.error("Error occurred while consuming and discarding the message", axisFault);
            }
            if (ExtensionResponseStatus.RETURN_ERROR.toString().equals(responseStatus)) {
                // Break the handler flow invoke the custom error handler specified by the user and return
                Mediator sequence = null;
                if (StringUtils.isNotBlank(customErrorHandler)) {
                    sequence = messageContext.getSequence(customErrorHandler);
                }
                if (sequence != null && !sequence.mediate(messageContext)) {
                    // If needed user should be able to prevent the rest of the fault handling logic from getting
                    // executed
                    return false;
                }
            }
            //break the handler flow and return back from the existing handler phase
            Utils.send(messageContext, extensionResponseDTO.getStatusCode());
            return false;
        } else {
            log.error("Invalid extension response status received. Continuing the default flow.");
            return true;
        }
    }

    /**
     * Returns extension listener implementation for the given Extension type. If no listener implementations registered
     * for the given type, return the default extension listener.
     *
     * @param type ExtensionType value
     * @return ExtensionListener implementation
     */
    private static ExtensionListener getExtensionListener(String type) {

        ExtensionListener extensionListener = ServiceReferenceHolder.getInstance().getExtensionListener(type);
        if (extensionListener == null) {
            extensionListener = defaultExtensionListener;
        }
        return extensionListener;
    }
}
