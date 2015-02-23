/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.store.impl.jms;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This class serves as a container for the Axis2 Message Context parameters/properties
 * , and it will be saved as a JMS message in the JMS Store.
 */
public class Axis2Message implements Serializable {
    private String messageID;

    private String operationAction;

    private QName operationName;

    private String action;

    private String service;

    private String relatesToMessageId;

    private String replyToAddress;

    private String faultToAddress;

    private String fromAddress;

    private String toAddress;

    private String transportInName;

    private String transportOutName;

    private boolean isDoingMTOM;

    private boolean isDoingSWA;

    private boolean isDoingPOX;

    private boolean isDoingGET;

    private String soapEnvelope;

    private byte[] jsonStream;

    private int FLOW;

    private HashMap<String, Object> properties = new HashMap<String, Object>();


    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getOperationAction() {
        return operationAction;
    }

    public void setOperationAction(String operationAction) {
        this.operationAction = operationAction;
    }

    public QName getOperationName() {
        return operationName;
    }

    public void setOperationName(QName operationName) {
        this.operationName = operationName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getRelatesToMessageId() {
        return relatesToMessageId;
    }

    public void setRelatesToMessageId(String relatesToMessageId) {
        this.relatesToMessageId = relatesToMessageId;
    }

    public String getReplyToAddress() {
        return replyToAddress;
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public String getSoapEnvelope() {
        return soapEnvelope;
    }

    public void setJsonStream(byte[] jsonStream) { this.jsonStream = jsonStream; }

    public byte[] getJsonStream() { return this.jsonStream; }

    public void setSoapEnvelope(String soapEnvelope) {
        this.soapEnvelope = soapEnvelope;
    }

    public int getFLOW() {
        return FLOW;
    }

    public void setFLOW(int FLOW) {
        this.FLOW = FLOW;
    }

    public String getFaultToAddress() {
        return faultToAddress;
    }

    public void setFaultToAddress(String faultToAddress) {
        this.faultToAddress = faultToAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public boolean isDoingMTOM() {
        return isDoingMTOM;
    }

    public void setDoingMTOM(boolean doingMTOM) {
        isDoingMTOM = doingMTOM;
    }

    public boolean isDoingSWA() {
        return isDoingSWA;
    }

    public void setDoingSWA(boolean doingSWA) {
        isDoingSWA = doingSWA;
    }

    public boolean isDoingPOX() {
        return isDoingPOX;
    }

    public void setDoingPOX(boolean doingPOX) {
        isDoingPOX = doingPOX;
    }

    public boolean isDoingGET() {
        return isDoingGET;
    }

    public void setDoingGET(boolean doingGET) {
        isDoingGET = doingGET;
    }

    public void addProperty(String name, Object obj) {
        properties.put(name, obj);
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public String getTransportInName() {
        return transportInName;
    }

    public void setTransportInName(String transportInName) {
        this.transportInName = transportInName;
    }

    public String getTransportOutName() {
        return transportOutName;
    }

    public void setTransportOutName(String transportOutName) {
        this.transportOutName = transportOutName;
    }
}
