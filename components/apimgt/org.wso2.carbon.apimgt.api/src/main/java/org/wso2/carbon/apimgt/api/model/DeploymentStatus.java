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

import java.util.List;
import java.util.Map;

public class DeploymentStatus {
    //use this class to build the model
    private String clusterName;
    private Integer podsRunning;
    private  List<Map<String,String>> podStatus;

    public DeploymentStatus() {
    }

    public DeploymentStatus(String clusterName, Integer podsRunning, boolean deployed, List<Map<String, String>> podStatus) {
        this.clusterName = clusterName;
        this.podsRunning = podsRunning;
        this.podStatus = podStatus;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getPodsRunning() {
        return podsRunning;
    }

    public void setPodsRunning(Integer podsRunning) {
        this.podsRunning = podsRunning;
    }

    public List<Map<String, String>> getPodStatus() {
        return podStatus;
    }

    public void setPodStatus(List<Map<String, String>> podStatus) {
        this.podStatus = podStatus;
    }

}
