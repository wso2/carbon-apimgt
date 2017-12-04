/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold SDK Language configurations
 */
@Configuration(description = "SDK Generation Language Configuration")
public class SdkLanguageConfigurations {

    public SdkLanguageConfigurations() {
        sdkGenLanguages.put("java", "io.swagger.codegen.languages.JavaClientCodegen");
        sdkGenLanguages.put("android", "io.swagger.codegen.languages.AndroidClientCodegen");
        sdkGenLanguages.put("python", "io.swagger.codegen.languages.PythonClientCodegen");
    }

    @Element(description = "SDK Generation Supported Languages")
    private Map<String, String> sdkGenLanguages = new HashMap<>();

    public Map<String, String> getSdkGenLanguages() {
        return sdkGenLanguages;
    }

    public void setSdkGenLanguages(Map<String, String> sdkGenLanguages) {
        this.sdkGenLanguages = sdkGenLanguages;
    }
}
