/*
 * Copyright (c) 2020, WSO2 Inc.(http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * */

package org.wso2.carbon.apimgt.rest.api.gateway.util;

import org.wso2.carbon.apimgt.impl.APIConstants;

public class GatewayAPIUtils {

    /**
     * This method would validate and modify the input context to cater to rest of the logic
     *
     * @param context API context
     * @return validated API context
     */
    public static String contextTemplateValidation(String context) {
        // To check if starts with "/" eg: /pizzashack/1.0.0
        if (context.startsWith("/")) {
            context = context.replaceFirst("/", "");
        }
        // To check if ends with "/" eg: /pizzashack/1.0.0/
        if (context.endsWith("/")) {
            context = context.substring(0, context.length() - 1);
        }
        // Returning context would be pizzashack/1.0.0
        return context;
    }

    /**
     * Validate the user input log level
     *
     * @param logLevel user input
     * @return true or false
     */
    public static boolean validateLogLevel(String logLevel) {
        return (APIConstants.APILogHandler.ALL.equalsIgnoreCase(logLevel) || APIConstants.APILogHandler.BODY
                .equalsIgnoreCase(logLevel) || APIConstants.APILogHandler.HEADERS.equalsIgnoreCase(logLevel));
    }

}
