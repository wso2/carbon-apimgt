/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.apimgt.core.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores configurations specific for a KeyManager implementation. Certain features will be made available based on
 * the values set in this Config.
 */
public class KeyManagerConfiguration {
    private Map<String, String> configuration = new HashMap<String, String>();

    // If set to false, resource registration will not be enabled.
    private boolean resourceRegistrationEnabled;

    // If set to false, input to set validity period will not be shown.
    private boolean tokenValidityConfigurable;

    // If set to false, pasting OAuth app details will not be supported.
    private boolean manualModeSupported;

    public boolean isResourceRegistrationEnabled() {
        return resourceRegistrationEnabled;
    }

    public void setResourceRegistrationEnabled(boolean resourceRegistrationEnabled) {
        this.resourceRegistrationEnabled = resourceRegistrationEnabled;
    }

    public boolean isTokenValidityConfigurable() {
        return tokenValidityConfigurable;
    }

    public void setTokenValidityConfigurable(boolean tokenValidityConfigurable) {
        this.tokenValidityConfigurable = tokenValidityConfigurable;
    }

    public boolean isManualModeSupported() {
        return manualModeSupported;
    }

    public void setManualModeSupported(boolean manualModeSupported) {
        this.manualModeSupported = manualModeSupported;
    }

    public void addParameter(String name, String value) {
        configuration.put(name, value);
    }

    public String getParameter(String name) {
        return configuration.get(name);
    }
}
