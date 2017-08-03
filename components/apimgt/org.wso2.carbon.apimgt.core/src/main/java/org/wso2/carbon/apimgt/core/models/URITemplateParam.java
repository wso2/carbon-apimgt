/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.models;

/**
 * URI Template parameter used for swagger definition generation
 */
public class URITemplateParam {

    private String name;
    private ParamTypeEnum paramType;
    private String dataType;

    /**
     * Parameter types
     */
    public enum ParamTypeEnum {
        QUERY, BODY, PATH, FORM_DATA
    }

    public URITemplateParam() {
    }

    public URITemplateParam(WSDLOperationParam wsdlOperationParam) {
        this.name = wsdlOperationParam.getName();
        this.paramType = ParamTypeEnum.valueOf(wsdlOperationParam.getParamType().toString());
        this.dataType = wsdlOperationParam.getDataType();
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
}
