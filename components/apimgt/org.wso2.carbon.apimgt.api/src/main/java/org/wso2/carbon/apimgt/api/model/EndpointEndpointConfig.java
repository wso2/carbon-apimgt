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

import java.util.ArrayList;
import java.util.List;

public class EndpointEndpointConfig {


    public enum EndpointTypeEnum {
        SINGLE,  LOAD_BALANCED,  FAIL_OVER,
    };

    private EndpointTypeEnum endpointType = null;


    private List<EndpointConfig> list = new ArrayList<EndpointConfig>();

    public EndpointTypeEnum getEndpointType() {

        return endpointType;
    }

    public void setEndpointType(EndpointTypeEnum endpointType) {

        this.endpointType = endpointType;
    }

    public List<EndpointConfig> getList() {

        return list;
    }

    public void setList(List<EndpointConfig> list) {

        this.list = list;
    }

    @Override
    public String toString() {

        return "EndpointEndpointConfig{" +
                "endpointType=" + endpointType +
                ", list=" + list +
                '}';
    }
}
