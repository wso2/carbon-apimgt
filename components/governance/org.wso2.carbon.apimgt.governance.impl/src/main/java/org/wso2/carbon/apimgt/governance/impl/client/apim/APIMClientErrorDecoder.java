/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.client.apim;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;

import static org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil.getEncodedLog;
import static org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil.getResponseBytesIfPresent;

/**
 * This class represents the error decoder for the APIM client
 */
public class APIMClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return createAPIManagementException(response);
    }

    private static GovernanceException createAPIManagementException(Response response) {
        byte[] responseBytes = getResponseBytesIfPresent(response);
        Request request = response.request();
        String encodedLog;
        if (request != null) {
            encodedLog = getEncodedLog(request.body(), request.httpMethod().name(), request.url(),
                    response.status(), responseBytes, "");
        } else {
            encodedLog = getEncodedLog(new byte[0], null, null,
                    response.status(), responseBytes, "");
        }
        if (response.status() == 404) {
            return new GovernanceException("Endpoint not found on APIM: " + encodedLog,
                    GovernanceExceptionCodes.ENDPOINT_NOT_FOUND_IN_APIM);
        } else {
            return new GovernanceException("Operation on APIM Failed: " + encodedLog,
                    GovernanceExceptionCodes.INTERNAL_SERVER_ERROR_FROM_APIM);
        }
    }

}
