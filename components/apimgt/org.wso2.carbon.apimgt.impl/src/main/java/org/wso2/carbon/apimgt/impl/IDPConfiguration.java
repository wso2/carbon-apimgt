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
package org.wso2.carbon.apimgt.impl;

/**
 * Identity Provider Configuration used for API Manager Portal Apps
 */
public class IDPConfiguration {

    private String authorizeEndpoint;
    private String oidcLogoutEndpoint;

    private IDPConfiguration() {
    }

    public String getAuthorizeEndpoint() {
        return authorizeEndpoint;
    }

    public String getOidcLogoutEndpoint() {
        return oidcLogoutEndpoint;
    }

    public static class Builder {
        private String authorizeEndpoint;
        private String oidcLogoutEndpoint;

        public Builder authorizeEndpoint(String authorizeEndpoint) {
            this.authorizeEndpoint = authorizeEndpoint;
            return this;
        }

        public Builder oidcLogoutEndpoint(String oidcLogoutEndpoint) {
            this.oidcLogoutEndpoint = oidcLogoutEndpoint;
            return this;
        }

        public IDPConfiguration build() {
            IDPConfiguration idpConfiguration = new IDPConfiguration();
            idpConfiguration.authorizeEndpoint = authorizeEndpoint;
            idpConfiguration.oidcLogoutEndpoint = oidcLogoutEndpoint;
            return idpConfiguration;
        }
    }
}
