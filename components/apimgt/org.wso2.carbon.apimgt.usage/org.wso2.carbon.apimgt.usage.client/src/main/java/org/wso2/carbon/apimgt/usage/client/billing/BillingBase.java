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

/**
 * This abstract class is used to generate base template for elements used in billing
 * plan and components
 */

package org.wso2.carbon.apimgt.usage.client.billing;

import org.apache.axiom.om.OMElement;


public abstract class BillingBase {

    public String objectName;
    public OMElement objectOMElement;

    public BillingBase() {
        super();
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public OMElement getObjectOMElement() {
        return objectOMElement;
    }

    public void setObjectOMElement(OMElement objectOMElement) {
        this.objectOMElement = objectOMElement;
    }

    /**
     * must be implement on lower level classes according to object status
     *
     * @return Serialized OMElement that represents the current object
     */
    public abstract OMElement serialize();

}