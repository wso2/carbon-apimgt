/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import static feign.FeignException.errorStatus;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ChoreoClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        String errorDescription = getErrorDescriptionFromStream(response);
        if (StringUtils.isEmpty(errorDescription)) {
            errorDescription = response.reason();
        }
        if (response.status() >= 400 && response.status() <= 499) {
            return new ChoreoClientException(response.status(), errorDescription);
        }
        if (response.status() >= 500 && response.status() <= 599) {
            return new ChoreoClientException(response.status(), errorDescription);
        }
        return errorStatus(methodKey, response);
    }

    private String getErrorDescriptionFromStream(Response response) {

        String errorDescription = null;
        if (response.body() != null) {
            try {
                String responseStr = IOUtils.toString(response.body().asInputStream(), UTF_8);
                JSONParser jsonParser = new JSONParser();
                JSONObject responseJson = (JSONObject) jsonParser.parse(responseStr);
                Object errorObj = responseJson.get("error");
                if (errorObj != null) {
                    errorDescription = errorObj.toString();
                }
            } catch (IOException | ParseException ignore) {

            }
        }
        return errorDescription;
    }
}