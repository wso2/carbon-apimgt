/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint Config DTO class for Serializable
 *
 */

public class EndpointConfigDTO implements Serializable {

    private String url = null;
    private String timeout = null;
    private List<EndpointConfigAttributesDTO> attributes = new ArrayList<EndpointConfigAttributesDTO>();

    public List<EndpointConfigAttributesDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<EndpointConfigAttributesDTO> attributes) {
        this.attributes = attributes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
}
