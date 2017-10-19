/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.security.keys;


import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;

public class APIKeyValidatorClientWrapper extends APIKeyValidatorClient {

    public APIKeyValidatorClientWrapper() throws APISecurityException {
    }

    @Override
    protected String getAxis2ClientXmlLocation() {
        return "src/test/resources/axis2_client.xml";
    }

    @Override
    protected String getClientRepoLocation() {
        return "src/test/resources/client";
    }
}
