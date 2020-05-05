/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

/**
 * Endpoint Registry Info Object.
 */
public class EndpointRegistryInfo {

    private String uuid = null;

    private String name = null;

    private String type = null;

    private String mode = null;

    private String owner = null;

    public String getUuid() {

        return uuid;
    }

    public String getName() {

        return name;
    }

    public String getType() {

        return type;
    }

    public String getMode() {

        return mode;
    }

    public String getOwner() {

        return owner;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setType(String type) {

        this.type = type;
    }

    public void setMode(String mode) {

        this.mode = mode;
    }

    public void setOwner(String owner) {

        this.owner = owner;
    }
}
