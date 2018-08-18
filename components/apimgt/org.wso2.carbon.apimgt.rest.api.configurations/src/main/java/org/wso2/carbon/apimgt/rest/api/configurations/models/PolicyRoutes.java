/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.configurations.models;

import org.wso2.carbon.config.annotation.Configuration;

/**
 * Class to hold policy url configurations
 */
@Configuration(description = "environment and key management configurations")
public class PolicyRoutes {

    private String privacyPolicyUrl = "/policy/privacy-policy";

    private String cookiePolicyUrl = "/policy/cookie-policy";

    public String getCookiePolicyUrl() { return cookiePolicyUrl; }

    public void setCookiePolicyUrl(String cookiePolicyUrl) { this.cookiePolicyUrl = cookiePolicyUrl; }

    public String getPrivacyPolicyUrl() { return privacyPolicyUrl; }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {this.privacyPolicyUrl = privacyPolicyUrl; }

}
