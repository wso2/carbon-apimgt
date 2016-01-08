/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import java.util.*;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
// import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO_TokenValidationContextParam;


import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

public class Utils {
    
    private static final Log log = LogFactory.getLog(Utils.class);
    private static APIManagerConfiguration config= ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    
    public static void sendFault(MessageContext messageContext, int status) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);        
        axis2MC.removeProperty("NO_ENTITY_BODY");
        String method = (String) axis2MC.getProperty(Constants.Configuration.HTTP_METHOD);
        if (method.matches("^(?!.*(POST|PUT|PATCH)).*$")) {
            // If the request was not an entity enclosing request, send a XML response back
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/xml");
        }
        // Always remove the ContentType - Let the formatter do its thing
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.remove(HttpHeaders.AUTHORIZATION);
            // headers.remove(HttpHeaders.ACCEPT);
            headers.remove(HttpHeaders.AUTHORIZATION);
            //headers.remove(HttpHeaders.ACCEPT);
            //Default we will send xml out put if error_message_type is json then we will send json response to client
            // We can set this parameter in _auth_failure_handler_ as follows
            /*<sequence name="_auth_failure_handler_">
            <property name="error_message_type" value="application/json"/>
            <sequence key="_build_"/>
            </sequence>     */
            /*  if (messageContext.getProperty("error_message_type") != null &&
                    messageContext.getProperty("error_message_type").toString().equalsIgnoreCase("application/json")) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/json");
            }*/
            //adding this fix to support any message type as error message type
            if (messageContext.getProperty("error_message_type") != null) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, messageContext.getProperty("error_message_type"));
            }

            headers.remove(HttpHeaders.HOST);
        }
        Axis2Sender.sendBack(messageContext);
    }
    
    public static void setFaultPayload(MessageContext messageContext, OMElement payload) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        JsonUtil.removeJsonPayload(axis2MC);
        messageContext.getEnvelope().getBody().addChild(payload);
    }
    
    public static void setSOAPFault(MessageContext messageContext, String code, 
                                    String reason, String detail) {
        SOAPFactory factory = (messageContext.isSOAP11() ?
                OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory());

        OMDocument soapFaultDocument = factory.createOMDocument();
        SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();
        soapFaultDocument.addChild(faultEnvelope);

        SOAPFault fault = faultEnvelope.getBody().getFault();
        if (fault == null) {
            fault = factory.createSOAPFault();
        }

        SOAPFaultCode faultCode = factory.createSOAPFaultCode();
        if (messageContext.isSOAP11()) {
            faultCode.setText(new QName(fault.getNamespace().getNamespaceURI(), code));
        } else {
            SOAPFaultValue value = factory.createSOAPFaultValue(faultCode);
            value.setText(new QName(fault.getNamespace().getNamespaceURI(), code));            
        }
        fault.setCode(faultCode);

        SOAPFaultReason faultReason = factory.createSOAPFaultReason();
        if (messageContext.isSOAP11()) {
            faultReason.setText(reason);
        } else {
            SOAPFaultText text = factory.createSOAPFaultText();
            text.setText(reason);
            text.setLang("en");
            faultReason.addSOAPText(text);            
        }
        fault.setReason(faultReason);

        SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
        soapFaultDetail.setText(detail);
        fault.setDetail(soapFaultDetail);
        
        // set the all headers of original SOAP Envelope to the Fault Envelope
        if (messageContext.getEnvelope() != null) {
            SOAPHeader soapHeader = messageContext.getEnvelope().getHeader();
            if (soapHeader != null) {
                for (Iterator iterator = soapHeader.examineAllHeaderBlocks(); iterator.hasNext();) {
                    Object o = iterator.next();
                    if (o instanceof SOAPHeaderBlock) {
                        SOAPHeaderBlock header = (SOAPHeaderBlock) o;
                        faultEnvelope.getHeader().addChild(header);
                    } else if (o instanceof OMElement) {
                        faultEnvelope.getHeader().addChild((OMElement) o);
                    }
                }
            }
        }

        try {
            messageContext.setEnvelope(faultEnvelope);
        } catch (AxisFault af) {
            log.error("Error while setting SOAP fault as payload", af);
            return;
        }

        if (messageContext.getFaultTo() != null) {
            messageContext.setTo(messageContext.getFaultTo());
        } else if (messageContext.getReplyTo() != null) {
            messageContext.setTo(messageContext.getReplyTo());
        } else {
            messageContext.setTo(null);
        }

        // set original messageID as relatesTo
        if (messageContext.getMessageID() != null) {
            RelatesTo relatesTo = new RelatesTo(messageContext.getMessageID());
            messageContext.setRelatesTo(new RelatesTo[] { relatesTo });
        }
    }

    public static String getAllowedHeaders() {
    	return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
    	        getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS);
    }
    
    public static String getAllowedMethods() {
    	return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
    	        getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS);
    }

    public static boolean isAllowCredentials() {
        String allowCredentials = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_CREDENTIALS);
        return Boolean.parseBoolean(allowCredentials);
    }
    
    public static boolean isCORSEnabled() {
    	String corsEnabled = config.
    	        getFirstProperty(APIConstants.CORS_CONFIGURATION_ENABLED);
    	    	    	
    	return Boolean.parseBoolean(corsEnabled);
    }

    public static boolean isStatsEnabled() {
        return ServiceReferenceHolder.getInstance().getApiManagerConfigurationService().
                getAPIAnalyticsConfiguration().isAnalyticsEnabled();
    }

    /**
     * validates if an accessToken has expired or not
     *
     * @param accessTokenDO
     * @return
     */
    public static boolean hasAccessTokenExpired(APIKeyValidationInfoDTO accessTokenDO) {
        long currentTime;
        long validityPeriod;
        if (accessTokenDO.getValidityPeriod() != Long.MAX_VALUE) {
            validityPeriod = accessTokenDO.getValidityPeriod() * 1000;
        } else {
            validityPeriod = accessTokenDO.getValidityPeriod();
        }
        long issuedTime = accessTokenDO.getIssuedTime();
        //long issuedTime = accessTokenDO.getIssuedTime().getTime();
        currentTime = System.currentTimeMillis();

        //If the validity period is not an never expiring value
        if (validityPeriod != Long.MAX_VALUE) {
            //check the validity of cached OAuth2AccessToken Response
            if ((currentTime) > (issuedTime + validityPeriod)) {
                accessTokenDO.setValidationStatus(
                        APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                if (accessTokenDO.getEndUserToken() != null) {
                    log.info("Token " + accessTokenDO.getEndUserToken() + " expired.");
                }
                return true;
            }
        }


        return false;
    }

    public static String getRequestPath(MessageContext synCtx, String fullRequestPath, String apiContext, String
            apiVersion) {
        String requestPath;
        String versionStrategy = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY);

        if(VersionStrategyFactory.TYPE_URL.equals(versionStrategy)){
            // most used strategy. server:port/context/version/resource
            requestPath = fullRequestPath.substring((apiContext + apiVersion).length() + 1, fullRequestPath.length());
        }else if(VersionStrategyFactory.TYPE_CONTEXT.equals(versionStrategy)){
            // version in context. server:port/contextWithVersion/resource
            requestPath = fullRequestPath.substring(apiContext.length(), fullRequestPath.length());
        }else{
            // default version. assume there is no version is used
            requestPath = fullRequestPath.substring(apiContext.length(), fullRequestPath.length());
        }
        return requestPath;
    }

    /**
     * This method used to send the response back from the request.
     *
     * @param messageContext messageContext of the request
     * @param status         HTTP Status to return from the response
     */
    public static void send(MessageContext messageContext, int status) {
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty(SynapseConstants.RESPONSE, "true");
        messageContext.setTo(null);
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Axis2Sender.sendBack(messageContext);
    }
}
