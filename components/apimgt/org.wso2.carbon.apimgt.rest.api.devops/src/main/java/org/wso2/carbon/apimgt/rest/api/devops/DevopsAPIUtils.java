/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.devops;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Devops util functions.
 */
public class DevopsAPIUtils {
    public static boolean validateLogLevel(String logLevel) {
        return (APIConstants.APILogHandler.OFF.equalsIgnoreCase(logLevel) || APIConstants.APILogHandler.BASIC
                .equalsIgnoreCase(logLevel) || APIConstants.APILogHandler.STANDARD.equalsIgnoreCase(logLevel) ||
                APIConstants.APILogHandler.FULL.equalsIgnoreCase(logLevel));
    }

    public static LoggingApiOutputListDTO getLoggingAPIList(List<APILogInfoDTO> apiLogInfoDTOList) {
        LoggingApiOutputListDTO loggingApiOutputListDTO = new LoggingApiOutputListDTO();
        List<LoggingApiOutputDTO> loggingApiOutputDTOList = new ArrayList<>();
        for (APILogInfoDTO apiLogInfoDTO: apiLogInfoDTOList) {
            LoggingApiOutputDTO loggingApiOutputDTO = new LoggingApiOutputDTO();
            loggingApiOutputDTO.setContext(apiLogInfoDTO.getContext());
            loggingApiOutputDTO.setLogLevel(apiLogInfoDTO.getLogLevel());
            loggingApiOutputDTO.setApiId(apiLogInfoDTO.getApiId());
            loggingApiOutputDTOList.add(loggingApiOutputDTO);
        }
        loggingApiOutputListDTO.apis(loggingApiOutputDTOList);
        return loggingApiOutputListDTO;
    }
}
