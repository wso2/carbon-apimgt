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

import io.swagger.models.ModelImpl;

import java.util.List;

/**
 * Extracted SOAP operation details representation.
 */
public class WSDLSOAPOperation {
    private String name;
    private String msgName;
    private String soapBindingOpName;
    private String soapAction;
    private String targetNamespace;
    private String style;
    private String httpVerb;
    private List<WSDLOperationParam> parameters;
    private List<WSDLOperationParam> outputParams;
    private List<ModelImpl> inputParameterModel;
    private List<ModelImpl> outputParameterModel;

    public WSDLSOAPOperation() {
    }

    public String getName() {
        return name;
    }

    public String getMsgName() {
        return msgName;
    }

    public void setMsgName(String msgName) {
        this.msgName = msgName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public List<WSDLOperationParam> getParameters() {
        return parameters;
    }

    public void setParameters(List<WSDLOperationParam> parameters) {
        this.parameters = parameters;
    }

    public List<WSDLOperationParam> getOutputParams() {
        return outputParams;
    }

    public void setOutputParams(List<WSDLOperationParam> outputParams) {
        this.outputParams = outputParams;
    }

    public String getSoapBindingOpName() {
        return soapBindingOpName;
    }

    public void setSoapBindingOpName(String soapBindingOpName) {
        this.soapBindingOpName = soapBindingOpName;
    }

    public List<ModelImpl> getInputParameterModel() {
        return inputParameterModel;
    }

    public void setInputParameterModel(List<ModelImpl> inputParameterModel) {
        this.inputParameterModel = inputParameterModel;
    }

    public List<ModelImpl> getOutputParameterModel() {
        return outputParameterModel;
    }

    public void setOutputParameterModel(List<ModelImpl> outputParameterModel) {
        this.outputParameterModel = outputParameterModel;
    }
}
