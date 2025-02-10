/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.gateway;

import java.util.List;

public class RBEndpointsPolicyDTO {

    List<RBEndpointDTO> production;
    List<RBEndpointDTO> sandbox;
    Long suspendDuration;

    public List<RBEndpointDTO> getProduction() {

        return production;
    }

    public void setProduction(List<RBEndpointDTO> production) {

        this.production = production;
    }

    public List<RBEndpointDTO> getSandbox() {

        return sandbox;
    }

    public void setSandbox(List<RBEndpointDTO> sandbox) {

        this.sandbox = sandbox;
    }

    public Long getSuspendDuration() {

        return suspendDuration;
    }

    public void setSuspendDuration(Long suspendDuration) {

        this.suspendDuration = suspendDuration;
    }
}
