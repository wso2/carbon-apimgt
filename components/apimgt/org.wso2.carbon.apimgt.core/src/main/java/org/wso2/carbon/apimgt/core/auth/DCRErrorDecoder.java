/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.auth;

import feign.Response;
import feign.codec.ErrorDecoder;
import feign.gson.GsonDecoder;
import org.wso2.carbon.apimgt.core.auth.dto.DCRError;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.io.IOException;

/**
 * Custom error decoder for DCR errors
 */
public class DCRErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.status() == APIMgtConstants.HTTPStatusCodes.SC_400_BAD_REQUEST) {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                return new KeyManagementException("Error occurred while DCR request. Error: " + error.getError() +
                        ". Error Description: " + error.getErrorDescription());
            }
        } catch (IOException e) {
            return new KeyManagementException("Error occurred while parsing the DCR error message: "
                    + response.body().toString(), e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
        return null;
    }
}
