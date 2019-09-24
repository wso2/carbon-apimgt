/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.wsdl.model;

import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;

import java.util.ArrayList;

public class WSDLValidationResponse {

    private boolean isValid;
    private WSDLInfo wsdlInfo;
    private WSDLArchiveInfo wsdlArchiveInfo;
    private WSDLProcessor wsdlProcessor;
    private ErrorHandler error;

    public WSDLInfo getWsdlInfo() {
        return wsdlInfo;
    }

    public WSDLArchiveInfo getWsdlArchiveInfo() {
        return wsdlArchiveInfo;
    }

    public void setWsdlArchiveInfo(WSDLArchiveInfo wsdlArchiveInfo) {
        this.wsdlArchiveInfo = wsdlArchiveInfo;
    }

    public ErrorHandler getError() {
        return error;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void setWsdlInfo(WSDLInfo wsdlInfo) {
        this.wsdlInfo = wsdlInfo;
    }

    public void setError(ErrorHandler error) {
        this.error = error;
    }

    public void setWsdlProcessor(WSDLProcessor wsdlProcessor) {
        this.wsdlProcessor = wsdlProcessor;
    }

    public WSDLProcessor getWsdlProcessor() {
        return wsdlProcessor;
    }
}
