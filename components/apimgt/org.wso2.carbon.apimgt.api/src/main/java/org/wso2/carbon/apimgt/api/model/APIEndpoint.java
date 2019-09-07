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

public class APIEndpoint {

    private Endpoint inline = null;

    private String type = null;

    private String key = null;

    public Endpoint getInline() {

        return inline;
    }

    public void setInline(Endpoint inline) {

        this.inline = inline;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    @Override
    public String toString() {

        return "APIEndpoint{" +
                "inline=" + inline +
                ", type='" + type + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
