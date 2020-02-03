/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.clients.scopemgt;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.http.HttpStatus;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class ScopeErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {

        if (response.status() == HttpStatus.SC_BAD_REQUEST || response.status() == HttpStatus.SC_CONFLICT
                || response.status() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

            return new APIManagementException("Error occurred while Scope Management Service Request. Error: " +
                    response.status() + ". Error Description: " + response.reason());
        }
        return null;
    }
}

