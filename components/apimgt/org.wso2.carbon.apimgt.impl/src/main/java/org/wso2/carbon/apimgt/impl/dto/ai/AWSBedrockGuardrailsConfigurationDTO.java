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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto.ai;

/**
 * This class represent the AWS Bedrock Guardrails configuration DTO.
 */
public class AWSBedrockGuardrailsConfigurationDTO {
    private String accessKey;
    private String secretKey;
    private String sessionToken;
    private String roleArn;
    private String roleRegion;
    private String roleExternalId;

    public String getAccessKey() {

        return accessKey;
    }

    public void setAccessKey(String accessKey) {

        this.accessKey = accessKey;
    }

    public String getSecretKey() {

        return secretKey;
    }

    public void setSecretKey(String secretKey) {

        this.secretKey = secretKey;
    }

    public String getSessionToken() {

        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {

        this.sessionToken = sessionToken;
    }

    public String getRoleArn() {

        return roleArn;
    }

    public void setRoleArn(String roleArn) {

        this.roleArn = roleArn;
    }

    public String getRoleRegion() {

        return roleRegion;
    }

    public void setRoleRegion(String roleRegion) {

        this.roleRegion = roleRegion;
    }

    public String getRoleExternalId() {

        return roleExternalId;
    }

    public void setRoleExternalId(String roleExternalId) {

        this.roleExternalId = roleExternalId;
    }
}
