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

import java.util.List;
import java.util.Objects;

/**
 * Basic WSDL operation
 */
public class WSDLOperation {

    private String verb;
    private String uri;
    private String contentType;
    private List<WSDLOperationParam> parameters;

    public WSDLOperation () {
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getURI() {
        return uri;
    }

    public String getVerb() {
        return verb;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setParameters(List<WSDLOperationParam> parameters) {
        this.parameters = parameters;
    }

    public List<WSDLOperationParam> getParameters() {
        return parameters;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WSDLOperation) {
            WSDLOperation otherObj = (WSDLOperation) obj;
            return otherObj.getVerb() != null && otherObj.getVerb().equals(verb)
                    && otherObj.getURI() != null && otherObj.getURI().equals(uri);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(verb, uri);
    }
}
