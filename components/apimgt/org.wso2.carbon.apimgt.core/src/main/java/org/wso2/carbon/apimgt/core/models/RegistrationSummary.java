/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.core.models;

import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.AnalyticsConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.CredentialConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.DataPublisherConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.JWTConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.ThrottlingConfigurations;

/**
 * This class holds the Gateway Registration Summary required by gateway
 */
public class RegistrationSummary {
    private KeyManagerInfo keyManagerInfo;
    private AnalyticsInfo analyticsInfo;
    private JWTInfo jwtInfo;
    private ThrottlingInfo throttlingInfo;

    public AnalyticsInfo getAnalyticsInfo() {
        return analyticsInfo;
    }

    public JWTInfo getJwtInfo() {
        return jwtInfo;
    }

    public ThrottlingInfo getThrottlingInfo() {
        return throttlingInfo;
    }

    public RegistrationSummary(APIMConfigurations apimConfigurations) {
        this.keyManagerInfo = new KeyManagerInfo(apimConfigurations.getKeyManagerConfigs());
    }

    public KeyManagerInfo getKeyManagerInfo() {
        return keyManagerInfo;
    }

    /**
     * This class holds KeyManager information required by gateway
     */
    public class KeyManagerInfo {
        private String dcrEndpoint;
        private String tokenEndpoint;
        private String revokeEndpoint;
        private String introspectEndpoint;
        private Credentials credentials;

        public KeyManagerInfo(KeyMgtConfigurations keyMgtConfigurations) {
            this.dcrEndpoint = keyMgtConfigurations.getDcrEndpoint();
            this.tokenEndpoint = keyMgtConfigurations.getTokenEndpoint();
            this.revokeEndpoint = keyMgtConfigurations.getRevokeEndpoint();
            this.introspectEndpoint = keyMgtConfigurations.getIntrospectEndpoint();
            this.credentials = new Credentials(keyMgtConfigurations.getKeyManagerCredentials());
        }

        public String getDcrEndpoint() {
            return dcrEndpoint;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public String getRevokeEndpoint() {
            return revokeEndpoint;
        }

        public String getIntrospectEndpoint() {
            return introspectEndpoint;
        }

        public Credentials getCredentials() {
            return credentials;
        }
    }

    /**
     * This class holds Analytics information required by gateway
     */
    public class AnalyticsInfo {
        private String dasServerURL;
        private Credentials dasServerCredentials;

        public AnalyticsInfo(AnalyticsConfigurations analyticsConfigurations) {
            this.dasServerURL = analyticsConfigurations.getDasServerURL();
            this.dasServerCredentials = new Credentials(analyticsConfigurations.getDasServerCredentials());
        }

        public String getDasServerURL() {
            return dasServerURL;
        }

        public Credentials getDasServerCredentials() {
            return dasServerCredentials;
        }
    }

    /**
     * This class holds JWT information required by gateway
     */
    public class JWTInfo {
        private boolean isEnableJWTGeneration;
        private String jwtHeader;

        public JWTInfo(JWTConfigurations jwtConfigurations) {
            this.isEnableJWTGeneration = jwtConfigurations.isEnableJWTGeneration();
            this.jwtHeader = jwtConfigurations.getJwtHeader();
        }

        public boolean isEnableJWTGeneration() {
            return isEnableJWTGeneration;
        }

        public String getJwtHeader() {
            return jwtHeader;
        }
    }

    /**
     * This class holds Throttling information required by gateway
     */
    public class ThrottlingInfo {
        private DataPublisher dataPublisher;

        public ThrottlingInfo(ThrottlingConfigurations throttlingConfigurations) {
            this.dataPublisher = new DataPublisher(throttlingConfigurations.getDataPublisherConfigurations());
        }

        public DataPublisher getDataPublisher() {
            return dataPublisher;
        }

        public class DataPublisher {
            private String receiverURL;
            private Credentials credentials;

            public DataPublisher(DataPublisherConfigurations dataPublisherConfigurations) {
                this.receiverURL = dataPublisherConfigurations.getReceiverURL();
                this.credentials = new Credentials(dataPublisherConfigurations.getDataPublisherCredentials());
            }

            public String getReceiverURL() {
                return receiverURL;
            }

            public Credentials getCredentials() {
                return credentials;
            }
        }
    }

    /**
     * This class holds the credential infomation
     */
    public class Credentials {
        private String username;
        private String password;

        public Credentials(CredentialConfigurations credentialConfigurations) {
            this.username = credentialConfigurations.getUsername();
            this.password = credentialConfigurations.getPassword();
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}