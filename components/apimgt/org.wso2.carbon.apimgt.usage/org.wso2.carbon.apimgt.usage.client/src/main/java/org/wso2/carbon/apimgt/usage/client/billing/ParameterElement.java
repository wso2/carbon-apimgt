/*
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


package org.wso2.carbon.apimgt.usage.client.billing;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the lowest element in billing plan. this holds data of some region and cost within that
 * region . so Data parameter such as call may have one or more regions so each region is
 * object of this class
 */
public class ParameterElement {
    private static final Log log = LogFactory.getLog(ParameterElement.class);
    private Integer startingValue;
    private Integer endValue;
    private Double costPerUnit;
    OMElement dataElementOM;

    public Integer getStartingValue() {
        return startingValue;
    }

    public Integer getEndValue() {
        return endValue;
    }

    public Double getCostPerUnit() {
        return costPerUnit;
    }

    public String getElementName() {
        return elementName;
    }

    private String elementName;

    public void setCostPerUnit(Double costPerUnit) {
        this.costPerUnit = costPerUnit;
    }

    public void setEndValue(Integer endValue) {
        this.endValue = endValue;
    }


    public void setStartingValue(Integer startingValue) {
        this.startingValue = startingValue;
    }


    public void setElementName(String elementName) {
        this.elementName = elementName;
    }


    public ParameterElement(){
        startingValue=0;
        endValue=0;
        costPerUnit =0.0;
        elementName="";
    }

    /**
     * constructor of ParameterElement that initialize object based on serialized Parameter Element
     * @param dataElement is OMElement that represent the Data Parameter Element
     */
    public ParameterElement(OMElement dataElement){
        this.dataElementOM=dataElement;
        String nameSpace="";
        try {
            this.elementName=
                    dataElementOM.getLocalName();
            this.startingValue=Integer.valueOf(
                    dataElementOM.getFirstChildWithName(new javax.xml.namespace.QName(nameSpace,"start")).getText());
            this.endValue=Integer.valueOf(
                    dataElementOM.getFirstChildWithName(new javax.xml.namespace.QName(nameSpace,"end")).getText());
            this.costPerUnit =Double.valueOf(
                    dataElementOM.getFirstChildWithName(new javax.xml.namespace.QName(nameSpace,"value")).getText());
        } catch (Exception e) {
            log.error("Error while initializing parameter element from the OMElement") ;
        }

    }


    /**Get serialized OMElement of the current object
     * @return OMElement That represents the ParameterElement
     */
    public OMElement getSerializedElement(){
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("","");
        OMElement returnOMElement = fac.createOMElement(this.getElementName(), omNs);
        OMElement start = fac.createOMElement("start", omNs);
        start.setText(this.startingValue.toString());
        returnOMElement.addChild(start);
        OMElement end = fac.createOMElement("end", omNs);
        end.setText(this.endValue.toString());
        returnOMElement.addChild(end);
        OMElement value = fac.createOMElement("value", omNs);
        value.setText(this.costPerUnit.toString());
        returnOMElement.addChild(value);
        return returnOMElement;
    }

}