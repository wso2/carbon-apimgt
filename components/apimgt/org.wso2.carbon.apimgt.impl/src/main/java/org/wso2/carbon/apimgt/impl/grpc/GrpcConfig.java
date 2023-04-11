/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.grpc;

public class GrpcConfig {
    private static GrpcConfig instance;
    private boolean isEnabled;
    private String appServiceUrl;
    private String authzServiceUrl;
    private boolean authzServiceEnabled;

    private GrpcConfig() {

    }

    public static GrpcConfig getInstance() {
        if (instance == null) {
            instance = new GrpcConfig();
        }
        return instance;
    }

    public static void setInstance(GrpcConfig instance) {
        GrpcConfig.instance = instance;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getAppServiceUrl() {
        return appServiceUrl;
    }

    public void setAppServiceUrl(String appServiceUrl) {
        this.appServiceUrl = appServiceUrl;
    }

    public String getAuthzServiceUrl() {
        return authzServiceUrl;
    }

    public void setAuthzServiceUrl(String authzServiceUrl) {
        this.authzServiceUrl = authzServiceUrl;
    }

    public boolean isAuthzServiceEnabled() {
        return authzServiceEnabled;
    }

    public void setAuthzServiceEnabled(boolean authzServiceEnabled) {
        this.authzServiceEnabled = authzServiceEnabled;
    }
}
