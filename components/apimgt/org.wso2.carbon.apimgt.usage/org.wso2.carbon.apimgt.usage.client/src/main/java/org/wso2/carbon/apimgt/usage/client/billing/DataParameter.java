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

import java.util.*;

/**
 * This class is used for keep details about the some data parameter, as example we can take calls
 * so this will hold data of call (Various regions that bill calculate and costs within those
 * regions) also this can calculate the cost for given usage of particular data parameter
 * and this contains multiple regions and costs within those regions
 */
public class DataParameter extends BillingBase {

    //This vector holds sub parameter elements of Data Parameter
    private Vector<ParameterElement> elementVector = new Vector<ParameterElement>();
    private static final Log log = LogFactory.getLog(DataParameter.class);


    public Vector<ParameterElement> getElementVector() {
        return elementVector;
    }

    public void setElementVector(Vector<ParameterElement> elementVector) {
        this.elementVector = elementVector;
    }

    public DataParameter() {
        super();
    }


    public DataParameter(Vector<ParameterElement> parameterElementVector, String parameterName) {
        this.objectName = parameterName;
        this.elementVector = parameterElementVector;
    }



    /**
     * constructor element that initialize object based on serialized Data parameter
     * @param dataParameterOME is used to get parameters of current object
     */
    public DataParameter(OMElement dataParameterOME) {
        this.objectOMElement = dataParameterOME;
        objectName =
                this.objectOMElement.getAttributeValue(new javax.xml.namespace.QName(null, "name"));
        Iterator dataParametersChildItr = objectOMElement.getChildElements();
        int paramCount = 0;
        while (dataParametersChildItr.hasNext()) {
            Object dataParameterChild = dataParametersChildItr.next();
            if (!(dataParameterChild instanceof OMElement)) {
                continue;
            }
            OMElement dataParameterChildEle = (OMElement) dataParameterChild;
            elementVector.add(new ParameterElement(dataParameterChildEle));
            paramCount = paramCount + 1;
        }
    }



    /**
     *
     * @param newElement is ParameterElement object going to add element vector
     * @return true if adding success else false
     */
    public boolean addNewParameterElement(ParameterElement newElement) {
        try {
            elementVector.add(newElement);
            return true;
        }
        catch (Exception e) {
            log.error("Error in adding new parameter element to parameter vector", e);
            return false;
        }

    }



    /**Get serialized OMElement of the current object
     * @return OMElelement of the object
     */
    public OMElement serialize() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("", "");
        Iterator itr = elementVector.iterator();
        OMElement returnElement = fac.createOMElement("parameter", omNs);
        returnElement.addAttribute("name", this.objectName, null);
        while (itr.hasNext()) {
            ParameterElement element = (ParameterElement) itr.next();
            returnElement.addChild(element.getSerializedElement());
        }
        return returnElement;
    }

    /**
     * totalInvocation - total amount of data units used
     *
     * This method calculate the total amount of money used for each data unit range
     */

    public List<APIUsageRangeCost> evaluateInvocationCost(int totalInvocation)  {

        int start, end, rangeInvocation = totalInvocation;
        double unitCost, rangeCost;
        List<APIUsageRangeCost> rangeCosts = new ArrayList<APIUsageRangeCost>();
        Vector<ParameterElement> reverseElements = new Vector<ParameterElement>(elementVector);
        Collections.reverse(reverseElements);
        for (ParameterElement element : reverseElements)  {
            start = element.getStartingValue();
            end = element.getEndValue();
            unitCost = element.getCostPerUnit();

            if (start < rangeInvocation && rangeInvocation <= end) {
                int rangeUnits = rangeInvocation - start;
                rangeCost = unitCost * rangeUnits;
                APIUsageRangeCost usageRangeCost = new APIUsageRangeCost();
                usageRangeCost.setRangeInvocationCount(rangeUnits);
                usageRangeCost.setCostPerUnit(unitCost);
                usageRangeCost.setCost(rangeCost);
                rangeCosts.add(usageRangeCost);
                rangeInvocation = start;
            } else if (start < rangeInvocation && end <= rangeInvocation) {
                int rangeUnits = end - start;
                rangeCost = rangeUnits * unitCost;
                APIUsageRangeCost usageRangeCost = new APIUsageRangeCost();
                usageRangeCost.setCost(rangeCost);
                usageRangeCost.setCostPerUnit(unitCost);
                usageRangeCost.setRangeInvocationCount(rangeInvocation);
                rangeInvocation = start;
                rangeCosts.add(usageRangeCost);
            }
        }
        return rangeCosts;
    }
}