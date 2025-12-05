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
import org.wso2.carbon.apimgt.spec.parser.definitions.AbstractAsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.AsyncApiV2Parser;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.models.AsyncApiV3Parser;

/**
 * Factory class for creating version-specific AsyncAPI parsers.
 * This factory returns the appropriate AbstractAsyncApiParser implementation based on the provided AsyncAPI version.
 *
 * Note: By default, AsyncAPI version 3.0 will be used when creating new streaming APIs via the Publisher Portal, as
 * the default AsyncApiParser is set to AsyncApiV3Parser.
 */
public class AsyncApiParserFactory {

    private static final Log log = LogFactory.getLog(AsyncApiParserFactory.class);

    /**
     * Returns the appropriate AsyncAPI parser implementation based on the given version and parsing options.
     * <p>
     * If the version belongs to AsyncAPI v2.x, the parser type is selected based on AsyncApiParseOptions
     * — either the new or the legacy v2 parser is returned. For AsyncAPI v3.x, the v3 parser is returned.
     * </p>
     *
     * @param version  the AsyncAPI specification version
     * @param options  parsing options used to determine the v2 parser selection
     * @return a version-specific AbstractAsyncApiParser implementation
     * @throws APIManagementException if the version is null or unsupported
     */
    public static AbstractAsyncApiParser getAsyncApiParser(String version, AsyncApiParseOptions options)
            throws APIManagementException {
        if (version == null) {
            throw new APIManagementException("AsyncAPI version cannot be null");
        } else if (isAsyncApiV2(version)) {
            log.debug("AsyncAPI definition version is V2.x.x");
            if (options != null && options.getPreserveLegacyAsyncApiParser()) {
                return new AsyncApiParser();
            } else {
                return new AsyncApiV2Parser();
            }
        } else if (version.startsWith(APISpecParserConstants.AsyncApi.ASYNC_API_V30)) {
            log.debug("AsyncAPI definition version is V3.x.x");
            return new AsyncApiV3Parser();
        } else {
            throw new APIManagementException("Unsupported AsyncAPI version: " + version);
        }
    }

    /**
     * Checks whether the given version belongs to AsyncAPI v2.x.
     * Note: This method strictly allows only AsyncAPI version 2.0 → 2.6
     *
     * @param version
     * @return boolean value of True/False
     */
    private static boolean isAsyncApiV2(String version) {
        return version != null && APISpecParserConstants.ASYNC_API_V2_PATTERN.matcher(version).matches();
    }
}
