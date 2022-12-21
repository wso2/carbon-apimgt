/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.common.gateway.jwt;

import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;

import java.security.KeyStore;

/**
 * JWTValidatorConfiguration holds the necessary configurations for the JWTValidator.
 */
public class JWTValidatorConfiguration {

    private boolean enableCertificateBoundAccessToken;
    private JWTTransformer jwtTransformer;
    private long timeStampSkewInSeconds;
    private TokenIssuerDto jwtIssuer;
    private KeyStore trustStore;


    public boolean isEnableCertificateBoundAccessToken() {
        return enableCertificateBoundAccessToken;
    }

    public JWTTransformer getJwtTransformer() {
        return jwtTransformer;
    }

    public long getTimeStampSkewInSeconds() {
        return timeStampSkewInSeconds;
    }

    public TokenIssuerDto getJwtIssuer() {
        return jwtIssuer;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * Builder class for the JWTValidatorConfiguration.
     */
    public static class Builder {
        private boolean enableCertificateBoundAccessToken = false;
        private JWTTransformer jwtTransformer;
        private long timeStampSkewInSeconds;
        private TokenIssuerDto jwtIssuer;
        private KeyStore trustStore;

        public Builder enableCertificateBoundAccessToken(boolean enableCertificateBoundAccessToken) {
            this.enableCertificateBoundAccessToken = enableCertificateBoundAccessToken;
            return this;
        }

        public Builder jwtTransformer(JWTTransformer jwtTransformer) {
            this.jwtTransformer = jwtTransformer;
            return this;
        }

        public Builder jwtIssuer(TokenIssuerDto tokenIssuerDto) {
            this.jwtIssuer = tokenIssuerDto;
            return this;
        }

        public Builder timeStampSkewInSeconds(long timeStampSkewInSeconds) {
            this.timeStampSkewInSeconds = timeStampSkewInSeconds;
            return this;
        }

        public Builder trustStore(KeyStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        public JWTValidatorConfiguration build() {
            JWTValidatorConfiguration jwtValidatorConfiguration = new JWTValidatorConfiguration();
            jwtValidatorConfiguration.jwtIssuer = this.jwtIssuer;
            jwtValidatorConfiguration.jwtTransformer = this.jwtTransformer;
            jwtValidatorConfiguration.timeStampSkewInSeconds = this.timeStampSkewInSeconds;
            jwtValidatorConfiguration.enableCertificateBoundAccessToken = this.enableCertificateBoundAccessToken;
            jwtValidatorConfiguration.trustStore = this.trustStore;
            return jwtValidatorConfiguration;
        }
    }
}
