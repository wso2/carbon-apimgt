/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.TokenBasedThrottlingCountHolder;

public class BackendThrottlingConfiguration {

    private String productionMaxTps;
    private String productionTimeUnit = "1000";
    private String sandboxMaxTps;
    private String sandboxTimeUnit = "1000";
    private TokenBasedThrottlingCountHolder tokenBasedThrottlingConfiguration;

    public String getProductionMaxTps() {

        return productionMaxTps;
    }

    public void setProductionMaxTps(String productionMaxTps) {

        this.productionMaxTps = productionMaxTps;
    }

    public String getProductionTimeUnit() {
        return productionTimeUnit;
    }

    public void setProductionTimeUnit(String productionTimeUnit) {

        this.productionTimeUnit = productionTimeUnit;
    }

    public String getSandboxMaxTps() {

        return sandboxMaxTps;
    }

    public void setSandboxMaxTps(String sandboxMaxTps) {

        this.sandboxMaxTps = sandboxMaxTps;
    }

    public String getSandboxTimeUnit() {

        return sandboxTimeUnit;
    }

    public void setSandboxTimeUnit(String sandboxTimeUnit) {

        this.sandboxTimeUnit = sandboxTimeUnit;
    }

    public TokenBasedThrottlingCountHolder getTokenBasedThrottlingConfiguration() {

        return tokenBasedThrottlingConfiguration;
    }

    public void setTokenBasedThrottlingConfiguration(TokenBasedThrottlingCountHolder tokenBasedThrottlingConfiguration) {

        this.tokenBasedThrottlingConfiguration = tokenBasedThrottlingConfiguration;
    }
}
