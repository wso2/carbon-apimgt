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
 * This class defined for store data and methods for particular payment plan
 * Each plan has one to many parameters and this can calculate cost for
 * usage (by providing hashTable contains parameter name and values ) according to the
 * plan
 */
public class PaymentPlan extends BillingBase {

    private static final Log log = LogFactory.getLog(PaymentPlan.class);
    public Vector<DataParameter> elementVector =new Vector<DataParameter>();


    public PaymentPlan(){
        super();
    }


    public Vector<DataParameter> getElementVector() {
        return elementVector;
    }


    public void setElementVector(Vector<DataParameter> elementVector) {
        this.elementVector = elementVector;
    }


    public PaymentPlan(Vector<DataParameter> parameterVector,String paymentPlanName){
        this.objectName =paymentPlanName;
        this.elementVector =parameterVector;
    }


    /**
     * constructor element that initialize object based on serialized Payment Plan
     * @param paymentPlanOME is used to get parameters of current object
     */
    public PaymentPlan(OMElement paymentPlanOME){
        this.setObjectOMElement(paymentPlanOME);
        this.setObjectName(
                this.getObjectOMElement().
                        getAttributeValue(new javax.xml.namespace.QName(null, "name")));
        Iterator dataParametersChildItr = objectOMElement.getChildElements();
        int paramCount=0;
        while(dataParametersChildItr.hasNext()){
            Object dataParameterChild = dataParametersChildItr.next();
            if (!(dataParameterChild instanceof OMElement)) {
                continue;
            }
            OMElement dataParameterChildEle = (OMElement) dataParameterChild;
            elementVector.add(new DataParameter(dataParameterChildEle));
            paramCount=paramCount+1;
        }
    }



    /**
     * add nev parameters to payment plan
     * @param newParameter is DataParameter object going to add element vector
     * @return true if adding success else false
     */
    public boolean addNewParameter(DataParameter newParameter){
        try{
            elementVector.add(newParameter);
            return true;
        }
        catch (Exception e){
            log.error("Error in adding new parameter element to parameter vector");
            return false;
        }

    }


    @Override
    public OMElement serialize(){
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("", "");
        Iterator itr = elementVector.iterator ();
        OMElement returnElement = fac.createOMElement("PaymentPlan", omNs);
        returnElement.addAttribute("name",this.objectName,null);
        while (itr.hasNext ()) {
            DataParameter element = (DataParameter)itr.next ();
            returnElement.addChild(element.serialize());
        }
        return returnElement;
    }



    public DataParameter getElementValueByName(String name){
        for(DataParameter iterator:elementVector){
            if(name.equals(iterator.objectName)){
                return iterator;
            }
        }
        return null;
    }


    /**
     * This just for testing or evaluate individual element
     * @param usedAmount is the Used amount of data units
     * @return BigDecimal value of cost for used data units
     * @throws Exception at Illegal range of usage value or error in configuration
     */
    public Map<String,Object>  evaluate (String parameterName,int usedAmount) throws Exception {
        BigDecimal total=new BigDecimal(0);
        Double costPerAPI=new Double(0);
        Map<String,Object> values=new HashMap<String,Object>();
        for(DataParameter iterator: elementVector){
            if(iterator.objectName.equals(parameterName)){
                total=total.add((BigDecimal) iterator.evaluate(usedAmount).get("total"));
                costPerAPI= (Double) iterator.evaluate(usedAmount).get("cost");

            }
        }
        values.put("total",total);
        values.put("cost",costPerAPI);
        return values;
    }



    /**
     * @param billOrder is hash table that contains data parameter name and value pairs
     * @return BigDecimal value of cost in billing order
     * @throws Exception @ error in evaluating
     */
    public BigDecimal evaluate(Hashtable billOrder) throws Exception{
        BigDecimal total=new BigDecimal(0);
        for(DataParameter iterator: elementVector){
            if(billOrder.containsKey(iterator.objectName)){
                Integer amount=(Integer)billOrder.get(iterator.objectName);
                total=total.add((BigDecimal) iterator.evaluate(amount).get("total"));
            }
        }
        return total;
    }

}