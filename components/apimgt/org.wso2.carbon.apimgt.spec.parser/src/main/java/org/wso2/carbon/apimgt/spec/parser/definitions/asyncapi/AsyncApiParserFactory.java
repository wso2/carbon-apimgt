/*
 *   Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.spec.parser.definitions.APISpecParserConstants;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.AsyncApiV2Parser;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.AsyncApiV3Parser;

public class AsyncApiParserFactory {

    private static final Log log = LogFactory.getLog(AsyncApiParserUtil.class);

    public static AsyncApiParser getAsyncApiParser(String version) throws APIManagementException {
        if (version == null) {
            throw new APIManagementException("AsyncAPI version cannot be null");
        } else if (isAsyncApiV2(version)) {
            log.debug("AsyncAPI definition version is V2.x.x");
            return new AsyncApiV2Parser();
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V30)) {
            log.debug("AsyncAPI definition version is V3.x.x");
            return new AsyncApiV3Parser();
        } else {
            throw new APIManagementException("Unsupported AsyncAPI version: " + version);
        }
    }

    private static boolean isAsyncApiV2(String version) {
        return version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V20)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V21)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V22)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V23)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V24)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V25)
                || version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V26);
    }
}
