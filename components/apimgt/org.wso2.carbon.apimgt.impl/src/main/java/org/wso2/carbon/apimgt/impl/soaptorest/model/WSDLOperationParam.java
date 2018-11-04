/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.soaptorest.model;

/**
 * Basic WSDL operation parameter.
 */
public class WSDLOperationParam {

    private String name;
    private ParamTypeEnum paramType;
    private String dataType;
    private boolean isComplexType;
    private boolean isSimpleType;
    private boolean isArray;
    private WSDLComplexType wsdlComplexType;
    private WSDLSimpleType wsdlSimpleType;

    /**
     * Parameter types
     */
    public enum ParamTypeEnum {
        QUERY, BODY, PATH, FORM_DATA
    }

    public String getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public ParamTypeEnum getParamType() {
        return paramType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamType(ParamTypeEnum paramType) {
        this.paramType = paramType;
    }

    public boolean isComplexType() {
        return isComplexType;
    }

    public void setComplexType(boolean complexType) {
        isComplexType = complexType;
    }

    public WSDLComplexType getWsdlComplexType() {
        return wsdlComplexType;
    }

    public void setWsdlComplexType(WSDLComplexType wsdlComplexType) {
        this.wsdlComplexType = wsdlComplexType;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isSimpleType() {
        return isSimpleType;
    }

    public void setSimpleType(boolean simpleType) {
        isSimpleType = simpleType;
    }

    public WSDLSimpleType getWsdlSimpleType() {
        return wsdlSimpleType;
    }

    public void setWsdlSimpleType(WSDLSimpleType wsdlSimpleType) {
        this.wsdlSimpleType = wsdlSimpleType;
    }
}
