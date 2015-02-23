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

package org.apache.synapse.eventing.builders;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.eventing.SynapseSubscription;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.Subscription;

import javax.xml.namespace.QName;

public class ResponseMessageBuilder {
    private SOAPFactory factory;
    private static final Log log = LogFactory.getLog(ResponseMessageBuilder.class);

    public ResponseMessageBuilder(MessageContext messageCtx) {
        factory = (SOAPFactory) messageCtx.getEnvelope().getOMFactory();
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ew="http://www.example.com/warnings"
     * (06)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (07)   <s12:Header>
     * (08)     <wsa:Action>
     * (09) http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse
     * (10)     </wsa:Action>
     * (11)     <wsa:RelatesTo>
     * (12)       uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180
     * (13)     </wsa:RelatesTo>
     * (14)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (15)     <ew:MySubscription>2597</ew:MySubscription>
     * (16)   </s12:Header>
     * (17)   <s12:Body>
     * (18)     <wse:SubscribeResponse>
     * (19)       <wse:SubscriptionManager>
     * (20)         <wsa:Address>
     * (21)           http://www.example.org/oceanwatch/SubscriptionManager
     * (22)         </wsa:Address>
     * (23)         <wsa:ReferenceParameters>
     * (24)           <wse:Identifier>
     * (25)             uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (26)           </wse:Identifier>
     * (27)         </wsa:ReferenceParameters>
     * (28)       </wse:SubscriptionManager>
     * (29)       <wse:Expires>2004-07-01T00:00:00.000-00:00</wse:Expires>
     * (30)     </wse:SubscribeResponse>
     * (31)   </s12:Body>
     * (32) </s12:Envelope>
     * Generate the subscription responce message
     *
     * @param subscription
     * @return
     */
    public SOAPEnvelope genSubscriptionResponse(SynapseSubscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        EndpointReference subscriptionManagerEPR =
                new EndpointReference(subscription.getSubManUrl());
        subscriptionManagerEPR.addReferenceParameter(new QName(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EN_IDENTIFIER, EventingConstants.WSE_EVENTING_PREFIX),
                subscription.getId());
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement subscribeResponseElement = factory.createOMElement(
                EventingConstants.WSE_EN_SUBSCRIBE_RESPONSE, eventingNamespace);
        try {
            OMElement subscriptionManagerElement = EndpointReferenceHelper.toOM(
                    subscribeResponseElement.getOMFactory(),
                    subscriptionManagerEPR,
                    new QName(EventingConstants.WSE_EVENTING_NS,
                            EventingConstants.WSE_EN_SUBSCRIPTION_MANAGER,
                            EventingConstants.WSE_EVENTING_PREFIX),
                    AddressingConstants.Submission.WSA_NAMESPACE);
            subscribeResponseElement.addChild(subscriptionManagerElement);
            message.getBody().addChild(subscribeResponseElement);
        } catch (AxisFault axisFault) {
            handleException("unable to create subscription response", axisFault);
        }
        return message;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing" >
     * (04)   <s12:Header>
     * (05)     <wsa:Action>
     * (06) http://schemas.xmlsoap.org/ws/2004/08/eventing/UnsubscribeResponse
     * (07)     </wsa:Action>
     * (08)     <wsa:RelatesTo>
     * (09)       uuid:2653f89f-25bc-4c2a-a7c4-620504f6b216
     * (10)     </wsa:RelatesTo>
     * (11)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (12)   </s12:Header>
     * (13)   <s12:Body />
     * (14) </s12:Envelope>
     *
     * @param subscription
     * @return
     */
    public SOAPEnvelope genUnSubscribeResponse(SynapseSubscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement dummyBody = factory.createOMElement("UnsubscribeResponse", eventingNamespace);
        message.getBody().addChild(dummyBody);
        return message;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)      http://schemas.xmlsoap.org/ws/2004/08/eventing/RenewResponse
     * (09)     </wsa:Action>
     * (10)     <wsa:RelatesTo>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:RelatesTo>
     * (13)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (14)   </s12:Header>
     * (15)   <s12:Body>
     * (16)     <wse:RenewResponse>
     * (17)       <wse:Expires>2004-06-26T12:00:00.000-00:00</wse:Expires>
     * (18)     </wse:RenewResponse>
     * (19)   </s12:Body>
     * (20) </s12:Envelope>
     *
     * @param subscription
     * @return
     */
    public SOAPEnvelope genRenewSubscriptionResponse(SynapseSubscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement renewResponseElement =
                factory.createOMElement(EventingConstants.WSE_EN_RENEW_RESPONSE, eventingNamespace);
        OMElement expiresElement =
                factory.createOMElement(EventingConstants.WSE_EN_EXPIRES, eventingNamespace);
        factory.createOMText(expiresElement,
                ConverterUtil.convertToString(subscription.getExpires()));
        renewResponseElement.addChild(expiresElement);
        message.getBody().addChild(renewResponseElement);
        return message;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)      http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatusResponse
     * (09)     </wsa:Action>
     * (10)     <wsa:RelatesTo>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:RelatesTo>
     * (13)     <wsa:To>http://www.example.com/MyEventSink</wsa:To>
     * (14)   </s12:Header>
     * (15)   <s12:Body>
     * (16)     <wse:GetStatusResponse>
     * (17)       <wse:Expires>2004-06-26T12:00:00.000-00:00</wse:Expires>
     * (18)     </wse:GetStatusResponse>
     * (19)   </s12:Body>
     * (20) </s12:Envelope>
     *
     * @param subscription
     * @return
     */
    public SOAPEnvelope genGetStatusResponse(Subscription subscription) {
        SOAPEnvelope message = factory.getDefaultEnvelope();
        OMNamespace eventingNamespace = factory.createOMNamespace(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement renewResponseElement = factory.createOMElement(
                EventingConstants.WSE_EN_GET_STATUS_RESPONSE, eventingNamespace);
        OMElement expiresElement =
                factory.createOMElement(EventingConstants.WSE_EN_EXPIRES, eventingNamespace);
        if (subscription.getExpires() != null) {
            factory.createOMText(expiresElement,
                    ConverterUtil.convertToString(subscription.getExpires()));
        } else {
            factory.createOMText(expiresElement, "*");
        }
        renewResponseElement.addChild(expiresElement);
        message.getBody().addChild(renewResponseElement);
        return message;
    }

    /**
     * <S:Envelope>
     * <S:Header>
     * <wsa:Action>
     * http://schemas.xmlsoap.org/ws/2004/08/addressing/fault
     * </wsa:Action>
     * <!-- Headers elided for clarity.  -->
     * </S:Header>
     * <S:Body>
     * <S:Fault>
     * <S:Code>
     * <S:Value>[Code]</S:Value>
     * <S:Subcode>
     * <S:Value>[Subcode]</S:Value>
     * </S:Subcode>
     * </S:Code>
     * <S:Reason>
     * <S:Text xml:lang="en">[Reason]</S:Text>
     * </S:Reason>
     * <S:Detail>
     * [Detail]
     * </S:Detail>
     * </S:Fault>
     * </S:Body>
     * </S:Envelope>
     *
     * @param code
     * @param subCode
     * @param reason
     * @param detail
     * @return
     */
    public SOAPEnvelope genFaultResponse(MessageContext messageCtx,
                                         String code,
                                         String subCode,
                                         String reason,
                                         String detail) {
        SOAPFactory soapFactory = null;
        if (messageCtx.isSOAP11()) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            soapFaultReason.setText(reason);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            QName qNameSubCode = new QName(EventingConstants.WSE_EVENTING_NS, subCode,
                    EventingConstants.WSE_EVENTING_PREFIX);
            soapFaultCode.setText(qNameSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message;
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail();
            soapFaultDetail.setText(detail);
            message.getBody().getFault().setDetail(soapFaultDetail);
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText();
            soapFaultText.setText(reason);
            soapFaultReason.addSOAPText(soapFaultText);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
            soapFaultValue.setText(code);
            soapFaultCode.setValue(soapFaultValue);
            SOAPFaultSubCode soapFaultSubCode = soapFactory.createSOAPFaultSubCode(soapFaultCode);
            SOAPFaultValue soapFaultValueSub = soapFactory.createSOAPFaultValue(soapFaultSubCode);
            QName qNameSubCode = new QName(EventingConstants.WSE_EVENTING_NS, subCode,
                    EventingConstants.WSE_EVENTING_PREFIX);
            soapFaultValueSub.setText(qNameSubCode);
            soapFaultSubCode.setValue(soapFaultValueSub);
            soapFaultCode.setSubCode(soapFaultSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message;
        }
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new SynapseException(message, e);
    }

}
