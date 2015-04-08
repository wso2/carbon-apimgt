/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Represents a request to create an OAuth App.
 */
public class OAuthAppRequest {

    // A string uniquely representing an OAuth App request. Can be used in serialising/de-serialising.
    private String mappingId;

    private OAuthApplicationInfo oAuthApplicationInfo;

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public OAuthApplicationInfo getOAuthApplicationInfo() {
        return oAuthApplicationInfo;
    }

    public void setOAuthApplicationInfo(OAuthApplicationInfo oAuthApplicationInfo) {
        this.oAuthApplicationInfo = oAuthApplicationInfo;
    }
}
