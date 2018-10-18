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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Information extracted from WSDL.
 */
public class WSDLInfo {

    private String version;
    private Map<String, String> endpoints;
    private boolean hasSoapBindingOperations;
    private boolean hasSoap12BindingOperations;
    private boolean hasHttpBindingOperations;
    private Set<WSDLOperation> httpBindingOperations;
    private Set<WSDLSOAPOperation> soapBindingOperations;
    private Map<String, ModelImpl> parameterModelMap;

    public WSDLInfo() {
        endpoints = new HashMap<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    public Set<WSDLOperation> getHttpBindingOperations() {
        return httpBindingOperations;
    }

    public void setHasHttpBindingOperations(boolean hasHttpBindingOperations) {
        this.hasHttpBindingOperations = hasHttpBindingOperations;
    }

    public boolean hasHttpBindingOperations() {
        return hasHttpBindingOperations;
    }

    public void setHasSoapBindingOperations(boolean hasSoapBindingOperations) {
        this.hasSoapBindingOperations = hasSoapBindingOperations;
    }

    public boolean hasSoapBindingOperations() {
        return hasSoapBindingOperations;
    }

    public boolean isHasSoap12BindingOperations() {
        return hasSoap12BindingOperations;
    }

    public void setHasSoap12BindingOperations(boolean hasSoap12BindingOperations) {
        this.hasSoap12BindingOperations = hasSoap12BindingOperations;
    }

    public void setHttpBindingOperations(Set<WSDLOperation> operations) {
        this.httpBindingOperations = operations;
    }

    public Set<WSDLSOAPOperation> getSoapBindingOperations() {
        return soapBindingOperations;
    }

    public void setSoapBindingOperations(Set<WSDLSOAPOperation> soapBindingOperations) {
        this.soapBindingOperations = soapBindingOperations;
    }

    public Map<String, ModelImpl> getParameterModelMap() {
        return parameterModelMap;
    }

    public void setParameterModelMap(Map<String, ModelImpl> parameterModelMap) {
        this.parameterModelMap = parameterModelMap;
    }
}
