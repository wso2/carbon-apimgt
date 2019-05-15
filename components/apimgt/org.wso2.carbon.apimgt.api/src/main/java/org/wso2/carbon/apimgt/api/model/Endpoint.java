/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class Endpoint {

    private String id = null;

    private String name = null;

    private EndpointEndpointConfig endpointConfig = null;

    private EndpointSecurity endpointSecurity = null;

    private Long maxTps = null;

    private String type = null;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public EndpointEndpointConfig getEndpointConfig() {

        return endpointConfig;
    }

    public void setEndpointConfig(EndpointEndpointConfig endpointConfig) {

        this.endpointConfig = endpointConfig;
    }

    public EndpointSecurity getEndpointSecurity() {

        return endpointSecurity;
    }

    public void setEndpointSecurity(EndpointSecurity endpointSecurity) {

        this.endpointSecurity = endpointSecurity;
    }

    public Long getMaxTps() {

        return maxTps;
    }

    public void setMaxTps(Long maxTps) {

        this.maxTps = maxTps;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    @Override
    public String toString() {

        return "Endpoint{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", endpointConfig=" + endpointConfig +
                ", endpointSecurity=" + endpointSecurity +
                ", maxTps=" + maxTps +
                ", type='" + type + '\'' +
                '}';
    }
}
