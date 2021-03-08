/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;

public class APIRevisionDeployment implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String revisionUUID;
    private String deployment;
    private String vhost;
    private boolean isDisplayOnDevportal;
    private String deployedTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRevisionUUID() {
        return revisionUUID;
    }

    public void setRevisionUUID(String revisionUUID) {
        this.revisionUUID = revisionUUID;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public boolean isDisplayOnDevportal() {
        return isDisplayOnDevportal;
    }

    public void setDisplayOnDevportal(boolean displayOnDevportal) {
        isDisplayOnDevportal = displayOnDevportal;
    }

    public String getDeployedTime() {
        return deployedTime;
    }

    public void setDeployedTime(String deployedTime) {
        this.deployedTime = deployedTime;
    }
}
