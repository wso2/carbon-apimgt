/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

/**
 * Model class for Solace connection properties.
 */
public class SolaceConfig {

    private boolean isEnabled;

    private String solaceApimApiEndpoint;

    private String solaceToken;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getSolaceApimApiEndpoint() {
        return solaceApimApiEndpoint;
    }

    public void setSolaceApimApiEndpoint(String solaceApimApiEndpoint) {
        this.solaceApimApiEndpoint = solaceApimApiEndpoint;
    }

    public String getSolaceToken() {
        return solaceToken;
    }

    public void setSolaceToken(String solaceToken) {
        this.solaceToken = solaceToken;
    }
}
