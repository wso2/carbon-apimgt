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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ExtensionErrorResponseDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.ExtensionResponseStatus;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.DefaultExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.ExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.model.dto.ResponseContextDTO;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.payloadhandler.SynapsePayloadHandlerFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * TODO:// add comment
 */
public class ExtensionListenerUtil {

    private static final Log log = LogFactory.getLog(ExtensionListenerUtil.class);

    /**
     * TODO:// add comment
     *
     * @param messageContext
     * @param type
     * @return
     * @throws APIManagementException
     */
    public static boolean preProcessRequest(MessageContext messageContext, String type) {

        RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.preProcessRequest(requestContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;
    }

    /**
     * TODO:// add comment
     *
     * @param messageContext
     * @param type
     * @return
     * @throws APIManagementException
     */
    public static boolean postProcessRequest(MessageContext messageContext, String type) {

        RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.postProcessRequest(requestContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;
    }

    /**
     * TODO: add comment
     *
     * @param messageContext
     * @param type
     * @return
     * @throws APIManagementException
     */
    public static boolean preProcessResponse(MessageContext messageContext, String type) {

        ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.preProcessResponse(responseContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;
    }

    /**
     * TODO: add comment
     *
     * @param messageContext
     * @param type
     * @return
     * @throws APIManagementException
     */
    public static boolean postProcessResponse(MessageContext messageContext, String type) {

        ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext);
        ExtensionListener extensionListener = getExtensionListener(type);
        ExtensionResponseDTO responseDTO = extensionListener.postProcessResponse(responseContextDTO);
        if (responseDTO != null) {
            return processExtensionResponse(messageContext, responseDTO, extensionListener.getErrorHandler());
        }
        return true;
    }

    /**
     * TODO:// comment
     *
     * @param messageContext
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private static RequestContextDTO generateRequestContextDTO(MessageContext messageContext) {

        RequestContextDTO requestDTO = new RequestContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfoDTO(messageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(messageContext);
        requestDTO.setApiRequestInfo(apiRequestInfoDTO);
        requestDTO.setMsgInfo(msgInfoDTO);
        //TODO: add api client cert
        //TODO: add api properties
        return requestDTO;
    }

    /**
     * TODO:// comment
     *
     * @param messageContext
     * @return
     */
    private static ResponseContextDTO generateResponseContextDTO(MessageContext messageContext) {

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfoDTO(messageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(messageContext);
        responseContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        responseContextDTO.setMsgInfo(msgInfoDTO);
        //TODO: set status code
        return responseContextDTO;
    }

    /**
     * TODO: comment
     *
     * @param messageContext
     * @return
     */
    private static APIRequestInfoDTO generateAPIInfoDTO(MessageContext messageContext) {

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setApiContext((String) messageContext.getProperty(APIMgtGatewayConstants.CONTEXT));
        apiRequestInfoDTO.setApiVersion((String) messageContext.getProperty(APIMgtGatewayConstants.VERSION));
        //TODO: set client id
        apiRequestInfoDTO.setUsername((String) messageContext.getProperty(APIMgtGatewayConstants.USER_ID));
        return apiRequestInfoDTO;
    }

    /**
     * TODO: comment
     *
     * @param messageContext
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private static MsgInfoDTO generateMessageInfoDTO(MessageContext messageContext) {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        msgInfoDTO.setHeaders((Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        msgInfoDTO.setElectedResource((String) messageContext.getProperty(APIMgtGatewayConstants.RESOURCE));
        msgInfoDTO.setHttpMethod((String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
        msgInfoDTO.setPayloadHandler(SynapsePayloadHandlerFactory.getInstance().buildPayloadHandler(messageContext));
        //TODO: set message id
        return msgInfoDTO;
    }

    /**
     * TODO://
     *
     * @param messageContext
     * @param extensionResponseDTO
     */
    private static boolean processExtensionResponse(MessageContext messageContext,
                                                    ExtensionResponseDTO extensionResponseDTO,
                                                    String customErrorHandler) {

        try {
            Map<String, String> headers = extensionResponseDTO.getHeaders();
            if (headers != null) {
                messageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS,
                        extensionResponseDTO.getHeaders());
                if (extensionResponseDTO.getPayload() != null) {
                    String payload = IOUtils.toString(extensionResponseDTO.getPayload());
                    if (payload != null) {
                        String contentType = headers.get(APIConstants.HEADER_CONTENT_TYPE);
                        if (StringUtils.equals(contentType, APIConstants.APPLICATION_JSON_MEDIA_TYPE)) {
                            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                                    getAxis2MessageContext();
                            JsonUtil.removeJsonPayload(axis2MC);
                            JsonUtil.getNewJsonPayload(axis2MC, payload, true, true);
                        } else {
                            //by default treat payload in well formed xml format
                            SOAPEnvelope env = messageContext.getEnvelope();
                            if (env != null && env.getBody() != null) {
                                env.getBody().addChild(AXIOMUtil.stringToOM(payload));
                            }
                        }
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            log.error("Error while setting payload", e);
        }
        messageContext
                .setProperty(APIMgtGatewayConstants.CUSTOM_PROPERTY_MAP, extensionResponseDTO.getCustomProperties());
        String responseStatus = extensionResponseDTO.getResponseStatus();

        if (ExtensionResponseStatus.CONTINUE.toString().equals(responseStatus)) {
            //continue the handler flow
            return true;
        } else if (ExtensionResponseStatus.RETURN_ERROR.toString().equals(responseStatus)) {
            //break the handler flow and return back executing error flow
            ExtensionErrorResponseDTO errorResponseDTO = extensionResponseDTO.getErrorResponse();
            if (errorResponseDTO != null) {
                messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE,
                        extensionResponseDTO.getStatusCode());
                messageContext.setProperty(SynapseConstants.ERROR_CODE, errorResponseDTO.getErrorCode());
                messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorResponseDTO.getErrorMessage());
                messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorResponseDTO.getErrorDescription());
            } else {
                log.error("No error response details set from extension");
                return true;
            }
            handleExtensionError(messageContext, customErrorHandler);
            return false;
        } else if (ExtensionResponseStatus.RETURN_RESPONSE.toString().equals(responseStatus)) {
            //break the handler flow and return back from the existing handler phase
            messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE,
                    extensionResponseDTO.getStatusCode());
            sendBackExtensionResponse(messageContext);
            return false;
        } else {
            log.error("Invalid response status set from extension");
            return true;
        }
    }

    private static void setInternalError(ExtensionResponseDTO responseDTO) {

        ExtensionErrorResponseDTO errorResponseDTO = new ExtensionErrorResponseDTO();
        errorResponseDTO.setErrorCode(APIMgtGatewayConstants.EXTENSION_SERVER_ERROR);
        errorResponseDTO.setErrorDescription(APIMgtGatewayConstants.EXTENSION_SERVER_ERROR_DECRIPTION);
        errorResponseDTO.setErrorMessage(APIMgtGatewayConstants.EXTENSION_SERVER_ERROR_MESSAGE);
        responseDTO.setErrorResponse(errorResponseDTO);
        responseDTO.setStatusCode(500);
        responseDTO.setResponseStatus(ExtensionResponseStatus.RETURN_ERROR.toString());
    }

    private static OMElement getFaultPayload(int errorCode, String message, String description) {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIMgtGatewayConstants.EXTENSION_NS,
                APIMgtGatewayConstants.EXTENSION_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement code = fac.createOMElement("code", ns);
        code.setText(String.valueOf(errorCode));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(code);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private static void handleExtensionError(MessageContext messageContext, String customErrorHandler) {
        // Invoke the custom error handler specified by the user
        Mediator sequence = null;
        if (StringUtils.isNotBlank(customErrorHandler)) {
            sequence = messageContext.getSequence(customErrorHandler);
        }
        int status = (int) messageContext.getProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE);
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        String errorMessage = (String) messageContext.getProperty(SynapseConstants.ERROR_MESSAGE);
        String errorDescription = (String) messageContext.getProperty(SynapseConstants.ERROR_DETAIL);

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.discardRequestMessage(axis2MC);
        } catch (AxisFault axisFault) {
            // In case of an error it is logged and the process is continued because we're setting a fault message in
            // the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling logic from getting
            // executed
            return;
        }
        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription));
        } else {
            Utils.setSOAPFault(messageContext, "Server", errorMessage, errorDescription);
        }
        Utils.sendFault(messageContext, status);
    }

    private static void sendBackExtensionResponse(MessageContext messageContext) {

        int status = (int) messageContext.getProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE);
        Utils.send(messageContext, status);
    }

    private static ExtensionListener getExtensionListener(String type) {

        ExtensionListener extensionListener = ServiceReferenceHolder.getInstance().getExtensionListener(type);
        if (extensionListener == null) {
            extensionListener = new DefaultExtensionListener();
        }
        return extensionListener;
    }
}
