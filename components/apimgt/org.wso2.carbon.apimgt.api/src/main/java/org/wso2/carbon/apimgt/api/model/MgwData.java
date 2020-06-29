/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represent the MgwData
 */
public class MgwData {

    private String mgwDataId;
    private String apiVersion;
    private String mgwversion;
    private String gatewayURL;
    private String projectName;
    private int uptime;
    private String status;
    private List<String> services = new ArrayList<>();

    public MgwData() {
    }

    public String getMgwDataId() {
        return mgwDataId;
    }

    public void setMgwDataId(String mgwDataId) {
        this.mgwDataId = mgwDataId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getMgwversion() {
        return mgwversion;
    }

    public void setMgwversion(String mgwversion) {
        this.mgwversion = mgwversion;
    }

    public String getGatewayURL() {
        return gatewayURL;
    }

    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int uptime) {
        this.uptime = uptime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        MgwData other = (MgwData) obj;
        if (!Objects.equals(this.apiVersion, other.apiVersion)) return false;
        if (!Objects.equals(this.mgwDataId, other.mgwDataId)) return false;
        if (!Objects.equals(this.mgwversion, other.mgwversion)) return false;
        if (!Objects.equals(this.gatewayURL, other.gatewayURL)) return false;
        if (!Objects.equals(this.projectName, other.projectName)) return false;
        if (!Objects.equals(this.uptime, other.uptime)) return false;
        if (!Objects.equals(this.status, other.status)) return false;
        if (!Objects.equals(this.services, other.services)) return false;
        return true;
    }
}
