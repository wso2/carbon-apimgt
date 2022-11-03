/*
 *  Copyright 2022 WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LCC licenses this file to you under the Apache License,
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

package org.wso2.apk.apimgt.api.model;

/**
 * This class represents Deployments
 */
public class APIDeploymentInfo {

    private String name;
    private String deployedTime;

    public APIDeploymentInfo(String name, String deployedTime) {

        this.name = name;
        this.deployedTime = deployedTime;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDeployedTime() {

        return deployedTime;
    }

    public void setDeployedTime(String deployedTime) {

        this.deployedTime = deployedTime;
    }
}
