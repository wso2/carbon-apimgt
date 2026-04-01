/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;

/**
 * Maps a local APIM tier to a remote gateway plan reference.
 */
public class GatewayTierMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private String localTierName;
    private String remotePlanReference;

    public String getLocalTierName() {
        return localTierName;
    }

    public void setLocalTierName(String localTierName) {
        this.localTierName = localTierName;
    }

    public String getRemotePlanReference() {
        return remotePlanReference;
    }

    public void setRemotePlanReference(String remotePlanReference) {
        this.remotePlanReference = remotePlanReference;
    }
}
