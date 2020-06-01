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
 * Product REST API Cache Configuration
 */
public class RESTAPICacheConfiguration {

    private boolean tokenCacheEnabled = true;
    private int tokenCacheExpiry = 300;
    private boolean cacheControlHeadersEnabled = true;
    private int cacheControlHeadersMaxAge = 86400;

    private RESTAPICacheConfiguration() {
    }

    public boolean isCacheControlHeadersEnabled() {
        return cacheControlHeadersEnabled;
    }

    public boolean isTokenCacheEnabled() {
        return tokenCacheEnabled;
    }

    public int getCacheControlHeadersMaxAge() {
        return cacheControlHeadersMaxAge;
    }

    public int getTokenCacheExpiry() {
        return tokenCacheExpiry;
    }

    public static class Builder {
        private boolean tokenCacheEnabled = true;
        private int tokenCacheExpiry = 300;
        private boolean cacheControlHeadersEnabled = true;
        private int cacheControlHeadersMaxAge = 86400;

        public Builder tokenCacheEnabled(boolean tokenCacheEnabled) {
            this.tokenCacheEnabled = tokenCacheEnabled;
            return this;
        }

        public Builder tokenCacheExpiry(int tokenCacheExpiry) {
            this.tokenCacheExpiry = tokenCacheExpiry;
            return this;
        }

        public Builder cacheControlHeadersEnabled(boolean cacheControlHeadersEnabled) {
            this.cacheControlHeadersEnabled = cacheControlHeadersEnabled;
            return this;
        }

        public Builder cacheControlHeadersMaxAge(int cacheControlHeadersMaxAge) {
            this.cacheControlHeadersMaxAge = cacheControlHeadersMaxAge;
            return this;
        }

        public RESTAPICacheConfiguration build() {
            RESTAPICacheConfiguration restApiCacheConfiguration = new RESTAPICacheConfiguration();
            restApiCacheConfiguration.tokenCacheEnabled = tokenCacheEnabled;
            restApiCacheConfiguration.tokenCacheExpiry = tokenCacheExpiry;
            restApiCacheConfiguration.cacheControlHeadersEnabled = cacheControlHeadersEnabled;
            restApiCacheConfiguration.cacheControlHeadersMaxAge = cacheControlHeadersMaxAge;
            return restApiCacheConfiguration;
        }
    }
}
