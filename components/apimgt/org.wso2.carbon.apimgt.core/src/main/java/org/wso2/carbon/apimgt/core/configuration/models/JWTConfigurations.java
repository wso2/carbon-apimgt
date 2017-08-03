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

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold JWT configurations
 */
@Configuration(description = "JWT Configurations")
public class JWTConfigurations {

    @Element(description = "Enable JWT generation")
    private boolean enableJWTGeneration = false;
    @Element(description = "JWT Header")
    private String jwtHeader = "X-JWT-Assertion";
    @Element(description = "Signature Algorithm")
    private String signatureAlgorithm = "SHA256withRSA";

    public boolean isEnableJWTGeneration() {
        return enableJWTGeneration;
    }

    public void setEnableJWTGeneration(boolean enableJWTGeneration) {
        this.enableJWTGeneration = enableJWTGeneration;
    }

    public String getJwtHeader() {
        return jwtHeader;
    }

    public void setJwtHeader(String jwtHeader) {
        this.jwtHeader = jwtHeader;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }
}
