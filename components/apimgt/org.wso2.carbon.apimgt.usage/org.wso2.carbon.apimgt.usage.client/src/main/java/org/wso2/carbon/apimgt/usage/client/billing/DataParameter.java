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

import java.math.BigDecimal;
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
            log.error("Error in adding new parameter element to parameter vector");
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
     * @param usedAmount is the Used amount of data units
     * @return BigDecimal value of cost for used data units
     * @throws Exception at Illegal range of usage value or error in configuration
     */
    public Map<String,Object> evaluate(int usedAmount) throws Exception{
        BigDecimal total = BigDecimal.ZERO;
        int beginValue = 0;
        int lastIndex=elementVector.size();
        int currentIndex=1;
        double cost = 0;
        double costInRange;
        int dataUnitsInRange;
        int start;
        int end;

        for (ParameterElement iterator : elementVector) {
            start = (iterator.getStartingValue());
            end = (iterator.getEndValue());
            cost = iterator.getCostPerUnit();
            costInRange=0;
            if(beginValue==start){
                if ( end < usedAmount) {
                    dataUnitsInRange = (end - start);
                    if(dataUnitsInRange>0){
                        costInRange=dataUnitsInRange * cost;
                        total = total.add(BigDecimal.valueOf(costInRange));
                        beginValue =
                                end;

                    }
                    if(currentIndex == lastIndex ){
                        String errorMessage="data value is out of maximum configuration limit";
                        log.error(errorMessage);
                        throw new Exception(errorMessage);
                    }

                }
                else{
                    dataUnitsInRange = (usedAmount - start);
                    if(dataUnitsInRange>=0){
                        costInRange=dataUnitsInRange * cost;
                        total = total.add(BigDecimal.valueOf(costInRange));

                        break;
                    }
                }
            }
            else{
                //This error throws when configuration file does not
                // define charges for some region in bill calculation
                log.error("cannot find configuration for this region");
                throw new Exception("cannot find configuration for this region");
            }
            currentIndex=currentIndex+1;
        }

        Map<String,Object> evaluateValues=new HashMap<String,Object>();
        evaluateValues.put("cost",cost);
        evaluateValues.put("total",total.setScale(2,BigDecimal.ROUND_HALF_UP));
        return evaluateValues;

    }

}