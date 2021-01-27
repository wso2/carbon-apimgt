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
package org.wso2.carbon.apimgt.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.extension.listener.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.dto.ExtensionResponseDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.extension.listener.DefaultExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.ExtensionListener;
import org.wso2.carbon.apimgt.gateway.extension.listener.dto.ResponseContextDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * TODO:// add comment
 */
public class ExtensionDataPublisher {

    private static final Log log = LogFactory.getLog(ExtensionDataPublisher.class);

    public ExtensionDataPublisher() {

    }

    /**
     * TODO:// add comment
     *
     * @param messageContext
     * @param handler
     * @return
     * @throws APIManagementException
     */
    public ExtensionResponseDTO preProcessRequest(MessageContext messageContext, String handler)
            throws APIManagementException {

        ExtensionResponseDTO responseDTO;
        try {
            //TODO:check the handler with config
            ExtensionListener extensionListener = new DefaultExtensionListener();
            boolean isBuildPayload = extensionListener.isBuildPayload();
            RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext, isBuildPayload);
            responseDTO = extensionListener.preProcessRequest(requestContextDTO);
        } catch (Exception e) {
            //TODO: error handle
            throw new APIManagementException("Error while pre processing the request", e);
        }
        return responseDTO;
    }

    /**
     * TODO:// add comment
     *
     * @param messageContext
     * @param handler
     * @return
     * @throws APIManagementException
     */
    public ExtensionResponseDTO postProcessRequest(MessageContext messageContext, String handler)
            throws APIManagementException {

        ExtensionResponseDTO responseDTO;
        try {
            //TODO:check the handler with config
            ExtensionListener extensionListener = new DefaultExtensionListener();
            boolean isBuildPayload = extensionListener.isBuildPayload();
            RequestContextDTO requestContextDTO = generateRequestContextDTO(messageContext, isBuildPayload);
            responseDTO = extensionListener.postProcessRequest(requestContextDTO);
        } catch (Exception e) {
            //TODO: error handle
            throw new APIManagementException("Error while pre processing the request", e);
        }
        return responseDTO;
    }

    /**
     * TODO: add comment
     *
     * @param messageContext
     * @param handler
     * @return
     * @throws APIManagementException
     */
    public ExtensionResponseDTO preProcessResponse(MessageContext messageContext, String handler)
            throws APIManagementException {

        ExtensionResponseDTO responseDTO;
        try {
            //TODO:check the handler with config
            ExtensionListener extensionListener = new DefaultExtensionListener();
            boolean isBuildPayload = extensionListener.isBuildPayload();
            ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext, isBuildPayload);
            responseDTO = extensionListener.preProcessResponse(responseContextDTO);
        } catch (Exception e) {
            //TODO: error handle
            throw new APIManagementException("Error while pre processing the request", e);
        }
        return responseDTO;
    }

    /**
     * TODO: add comment
     *
     * @param messageContext
     * @param handler
     * @return
     * @throws APIManagementException
     */
    public ExtensionResponseDTO postProcessResponse(MessageContext messageContext, String handler)
            throws APIManagementException {

        ExtensionResponseDTO responseDTO;
        try {
            //TODO:check the handler with config
            ExtensionListener extensionListener = new DefaultExtensionListener();
            boolean isBuildPayload = extensionListener.isBuildPayload();
            ResponseContextDTO responseContextDTO = generateResponseContextDTO(messageContext, isBuildPayload);
            responseDTO = extensionListener.postProcessResponse(responseContextDTO);
        } catch (Exception e) {
            //TODO: error handle
            throw new APIManagementException("Error while pre processing the request", e);
        }
        return responseDTO;
    }

    /**
     * TODO:// comment
     *
     * @param messageContext
     * @param isBuildPayload
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private RequestContextDTO generateRequestContextDTO(MessageContext messageContext, boolean isBuildPayload)
            throws IOException, XMLStreamException {

        RequestContextDTO requestDTO = new RequestContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfoDTO(messageContext, isBuildPayload);
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
    private ResponseContextDTO generateResponseContextDTO(MessageContext messageContext, boolean isBuildPayload)
            throws IOException, XMLStreamException {

        ResponseContextDTO responseContextDTO = new ResponseContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfoDTO(messageContext, isBuildPayload);
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
    private APIRequestInfoDTO generateAPIInfoDTO(MessageContext messageContext) {

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
     * @param isBuildPayload
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private MsgInfoDTO generateMessageInfoDTO(MessageContext messageContext, boolean isBuildPayload)
            throws IOException, XMLStreamException {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        msgInfoDTO.setHeaders((Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        msgInfoDTO.setElectedResource((String) messageContext.getProperty(APIMgtGatewayConstants.RESOURCE));
        msgInfoDTO.setHttpMethod((String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
        if (isBuildPayload) {
            // read the body
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            if (messageContext.getEnvelope().getBody() != null) {
                //TODO:check with other message types/multipart etc
                String payload = messageContext.getEnvelope().getBody().toString();
                InputStream payloadStream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                msgInfoDTO.setPayload(payloadStream);
            }
        }
        //TODO: set message id
        return msgInfoDTO;
    }
}
