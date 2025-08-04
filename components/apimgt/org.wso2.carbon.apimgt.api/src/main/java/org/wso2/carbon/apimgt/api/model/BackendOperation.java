/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIConstants;

import java.io.Serializable;

/**
 * Represents a Backend Operation in the API Management system.
 * This class encapsulates the details of a backend operation including its reference URI mapping ID,
 * target endpoint, and HTTP verb.
 */
public class BackendOperation implements Serializable {

    private static final long serialVersionUID = 1L;
    private int refUriMappingId;
    private String target;
    private APIConstants.SupportedHTTPVerbs verb;

    public BackendOperation() {

    }

    public String getTarget() {

        return target;
    }

    public void setTarget(String target) {

        this.target = target;
    }

    public APIConstants.SupportedHTTPVerbs getVerb() {

        return verb;
    }

    public void setVerb(APIConstants.SupportedHTTPVerbs verb) {

        this.verb = verb;
    }

    public int getRefUriMappingId() {

        return refUriMappingId;
    }

    public void setRefUriMappingId(int refUriMappingId) {

        this.refUriMappingId = refUriMappingId;
    }
}
