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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.eventing.SynapseSubscription;
import org.wso2.eventing.EventingConstants;

import javax.xml.namespace.QName;
import java.util.Calendar;

/**
 *
 */
public class SubscriptionMessageBuilder {

    private static final Log log = LogFactory.getLog(SubscriptionMessageBuilder.class);

    private static final QName SUBSCRIBE_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_SUBSCRIBE);
    private static final QName DELIVERY_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_DELIVERY);
    private static final QName FILTER_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_FILTER);
    private static final QName NOTIFY_TO_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_NOTIFY_TO);
    private static final QName ATT_DIALECT =
            new QName(XMLConfigConstants.NULL_NAMESPACE, EventingConstants.WSE_EN_DIALECT);
    private static final QName IDENTIFIER =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_IDENTIFIER);
    private static final QName EXPIRES =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_EXPIRES);
    private static final QName RENEW =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_RENEW);

    private static String errorSubCode = null;
    private static String errorReason = null;
    private static String errorCode = null;

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ew="http://www.example.com/warnings" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)      <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>
     * (15)      <wsa:ReferenceProperties>
     * (16)        <ew:MySubscription>2597</ew:MySubscription>
     * (17)      </wsa:ReferenceProperties>
     * (18)     </wsa:ReplyTo>
     * (19)     <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>
     * (20)   </s12:Header>
     * (21)   <s12:Body>
     * (22)     <wse:Subscribe>
     * (23)       <wse:EndTo>
     * (24)         <wsa:Address>
     * (25)           http://www.example.com/MyEventSink
     * (26)         </wsa:Address>
     * (27)         <wsa:ReferenceProperties>
     * (28)           <ew:MySubscription>2597</ew:MySubscription>
     * (29)         </wsa:ReferenceProperties>
     * (30)       </wse:EndTo>
     * (31)       <wse:Delivery>
     * (32)         <wse:NotifyTo>
     * (33)           <wsa:Address>
     * (34)             http://www.other.example.com/OnStormWarning
     * (35)           </wsa:Address>
     * (36)           <wsa:ReferenceProperties>
     * (37)             <ew:MySubscription>2597</ew:MySubscription>
     * (38)           </wsa:ReferenceProperties>
     * (39)         </wse:NotifyTo>
     * (40)       </wse:Delivery>
     * (41)       <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>
     * (42)       <wse:Filter xmlns:ow="http://www.example.org/oceanwatch"
     * (43)           Dialect="http://www.example.org/topicFilter" >
     * (44)         weather.storms
     * (45)       </wse:Filter>
     * (46)     </wse:Subscribe>
     * (47)   </s12:Body>
     * (48) </s12:Envelope>
     *
     * @param mc The MessageContext from which to create the SynapseSubscription
     * @return The SynapseSubscription
     */
    public static SynapseSubscription createSubscription(MessageContext mc) {
        SynapseSubscription subscription = null;
        OMElement notifyToElem;
        OMElement elem = mc.getEnvelope().getBody().getFirstChildWithName(SUBSCRIBE_QNAME);
        if (elem != null) {
            OMElement deliveryElem = elem.getFirstChildWithName(DELIVERY_QNAME);
            if (deliveryElem != null) {
                notifyToElem = deliveryElem.getFirstChildWithName(NOTIFY_TO_QNAME);
                if (notifyToElem != null) {
                    subscription = new SynapseSubscription(
                            EventingConstants.WSE_DEFAULT_DELIVERY_MODE);
                    subscription.setAddressUrl(notifyToElem.getFirstElement().getText());
                    subscription.setEndpointUrl(notifyToElem.getFirstElement().getText());
                    subscription.setSubManUrl(mc.getTo().getAddress());

                } else {
                    handleException("NotifyTo element not found in the subscription message");
                }
            } else {
                handleException("Delivery element is not found in the subscription message");
            }

            OMElement filterElem = elem.getFirstChildWithName(FILTER_QNAME);
            if (subscription != null && filterElem != null) {
                OMAttribute dialectAttr = filterElem.getAttribute(ATT_DIALECT);
                if (dialectAttr != null && dialectAttr.getAttributeValue() != null) {
                    subscription.setFilterDialect(dialectAttr.getAttributeValue());
                    subscription.setFilterValue(filterElem.getText());
                } else {
                    handleException("Error in creating subscription. Filter dialect not defined");
                }
            }
            OMElement expiryElem = elem.getFirstChildWithName(EXPIRES);
            if (expiryElem != null) {
                Calendar calendarExpires = null;
                try {
                    if (expiryElem.getText().startsWith("P")) {
                        calendarExpires = ConverterUtil.convertToDuration(expiryElem.getText())
                                .getAsCalendar();
                    } else {
                        calendarExpires = ConverterUtil.convertToDateTime(expiryElem.getText());
                    }
                } catch (Exception e) {
                    log.error("Error converting the expiration date ," + e.toString());
                    setExpirationFault(subscription);
                }
                Calendar calendarNow = Calendar.getInstance();
                if ((isValidDate(expiryElem.getText(), calendarExpires)) &&
                        (calendarNow.before(calendarExpires))) {
                    subscription.setExpires(calendarExpires);
                } else {
                    setExpirationFault(subscription);
                }
            }
        } else {
            handleException(
                    "Subscribe element is required as the payload of the subscription message");
        }
        return subscription;
    }


    /**
     * create request for unsubscribr request
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:2653f89f-25bc-4c2a-a7c4-620504f6b216
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)      <wsa:Address>http://www.example.com/MyEventSink</wsa:Address>
     * (15)     </wsa:ReplyTo>
     * (16)     <wsa:To>
     * (17)       http://www.example.org/oceanwatch/SubscriptionManager
     * (18)     </wsa:To>
     * (19)     <wse:Identifier>
     * (20)       uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (21)     </wse:Identifier>
     * (22)   </s12:Header>
     * (23)   <s12:Body>
     * (24)     <wse:Unsubscribe />
     * (25)   </s12:Body>
     * (26) </s12:Envelope>
     *
     * @param mc The MessageContext from which to create the SynapseSubscription
     * @return The SynapseSubscription
     */
    public static SynapseSubscription createUnSubscribeMessage(MessageContext mc) {
        SynapseSubscription subscription = new SynapseSubscription();
        OMElement elem = mc.getEnvelope().getHeader().getFirstChildWithName(IDENTIFIER);
        String id = elem.getText();
        subscription.setId(id);
        subscription.setAddressUrl(mc.getTo().getAddress());
        return subscription;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)      <wsa:Address>http://www.example.com/MyEventSink</wsa:Address>
     * (15)     </wsa:ReplyTo>
     * (16)     <wsa:To>
     * (17)       http://www.example.org/oceanwatch/SubscriptionManager
     * (18)     </wsa:To>
     * (19)     <wse:Identifier>
     * (20)       uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (21)     </wse:Identifier>
     * (22)   </s12:Header>
     * (23)   <s12:Body>
     * (24)     <wse:Renew>
     * (25)       <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>
     * (26)     </wse:Renew>
     * (27)   </s12:Body>
     * (28) </s12:Envelope>
     *
     * @param mc MessageContext from which to create the SynapseSubscription
     * @return The SynapseSubscription
     */
    public static SynapseSubscription createRenewSubscribeMessage(MessageContext mc) {
        SynapseSubscription subscription = new SynapseSubscription();
        OMElement elem = mc.getEnvelope().getHeader().getFirstChildWithName(IDENTIFIER);
        String id = elem.getText();
        subscription.setId(id);
        subscription.setAddressUrl(mc.getTo().getAddress());
        OMElement renewElem = mc.getEnvelope().getBody().getFirstChildWithName(RENEW);
        if (renewElem != null) {
            OMElement expiryElem = renewElem.getFirstChildWithName(EXPIRES);
            if (expiryElem != null) {
                if (!(expiryElem.getText().startsWith("*"))) {
                    Calendar calendarExpires = null;
                    try {
                        if (expiryElem.getText().startsWith("P")) {
                            calendarExpires = ConverterUtil.convertToDuration(expiryElem.getText())
                                    .getAsCalendar();
                        } else {
                            calendarExpires = ConverterUtil.convertToDateTime(expiryElem.getText());
                        }
                    } catch (Exception e) {
                        setExpirationFault(subscription);
                    }
                    Calendar calendarNow = Calendar.getInstance();
                    if ((isValidDate(expiryElem.getText(), calendarExpires)) &&
                            (calendarNow.before(calendarExpires))) {
                        subscription.setExpires(calendarExpires);
                    } else {
                        setExpirationFault(subscription);
                    }

                    subscription.setExpires(calendarExpires);
                } else {
                    setExpirationFault(subscription);
                }
            } else {
                setExpirationFault(subscription);
            }
        }
        return subscription;
    }

    /**
     * (01) <s12:Envelope
     * (02)     xmlns:s12="http://www.w3.org/2003/05/soap-envelope"
     * (03)     xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing"
     * (04)     xmlns:wse="http://schemas.xmlsoap.org/ws/2004/08/eventing"
     * (05)     xmlns:ow="http://www.example.org/oceanwatch" >
     * (06)   <s12:Header>
     * (07)     <wsa:Action>
     * (08)       http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus
     * (09)     </wsa:Action>
     * (10)     <wsa:MessageID>
     * (11)       uuid:bd88b3df-5db4-4392-9621-aee9160721f6
     * (12)     </wsa:MessageID>
     * (13)     <wsa:ReplyTo>
     * (14)       <wsa:Address>http://www.example.com/MyEventSink</wsa:Address>
     * (15)     </wsa:ReplyTo>
     * (16)     <wsa:To>
     * (17)       http://www.example.org/oceanwatch/SubscriptionManager
     * (18)     </wsa:To>
     * (19)     <wse:Identifier>
     * (20)       uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa
     * (21)     </wse:Identifier>
     * (22)   </s12:Header>
     * (23)   <s12:Body>
     * (24)     <wse:GetStatus />
     * (25)   </s12:Body>
     * (26) </s12:Envelope>
     *
     * @param mc The MessageContext from which to extract the SynapseSubscription
     * @return The SynapseSubscription
     */
    public static SynapseSubscription createGetStatusMessage(MessageContext mc) {
        SynapseSubscription subscription = new SynapseSubscription();
        subscription.setAddressUrl(mc.getTo().getAddress());
        OMElement elem = mc.getEnvelope().getHeader().getFirstChildWithName(IDENTIFIER);
        String id = elem.getText();
        subscription.setId(id);
        return subscription;
    }

    private static void handleException(String message) {
        log.error(message);
        throw new SynapseException(message);
    }

    public static String getErrorSubCode() {
        return errorSubCode;
    }

    public static void setErrorSubCode(String errorCode) {
        errorSubCode = errorCode;
    }

    public static String getErrorReason() {
        return errorReason;
    }

    public static void setErrorReason(String errorReasons) {
        errorReason = errorReasons;
    }

    public static String getErrorCode() {
        return errorCode;
    }

    public static void setErrorCode(String errorCodes) {
        errorCode = errorCodes;
    }

    private static void setExpirationFault(SynapseSubscription subscription) {
        setErrorCode(EventingConstants.WSE_FAULT_CODE_SENDER);
        setErrorSubCode("InvalidExpirationTime");
        setErrorReason("The expiration time requested is invalid");
        subscription.setId(null);
    }

    /**
     * Check is a valid date, this check required due to Java calendar use the Julion
     * date to create dates, so feb-31 is taken as a valid date and converts to march-03,
     * ConverterUtil wont validate.
     *
     * @param original The original date as a string
     * @param converted The Calendar instance to be validated
     * @return true || false
     */
    private static boolean isValidDate(String original, Calendar converted) {
        try {
            String check = ConverterUtil.convertToString(converted);
            if (original.equals(check)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Converting the date to string, " + e.toString());
            return false;
        }
    }
}
