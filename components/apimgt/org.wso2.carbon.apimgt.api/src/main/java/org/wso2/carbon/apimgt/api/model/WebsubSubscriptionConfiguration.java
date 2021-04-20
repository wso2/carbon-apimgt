/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class WebsubSubscriptionConfiguration {
    private boolean enable;
    private String secret;
    private String signingAlgorithm;
    private String signatureHeader;

    public WebsubSubscriptionConfiguration(
            boolean enable, String secret, String signingAlgorithm, String signatureHeader) {
        this.enable = enable;
        this.secret = secret;
        this.signingAlgorithm = signingAlgorithm;
        this.signatureHeader = signatureHeader;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public String getSignatureHeader() {
        return signatureHeader;
    }

    public void setSignatureHeader(String signatureHeader) {
        this.signatureHeader = signatureHeader;
    }
}
