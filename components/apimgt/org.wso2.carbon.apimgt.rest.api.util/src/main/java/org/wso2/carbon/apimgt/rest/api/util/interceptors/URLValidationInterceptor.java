/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.IOException;


public class URLValidationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Log log = LogFactory.getLog(URLValidationInterceptor.class);
    private static String majorVersion = "v1";
    //TODO: Get latest version from swagger
    private static String latestVersion = "v1.1";
    private String pathSeparator = "/";
    private final String BASE_PATH = "org.apache.cxf.message.Message.BASE_PATH";
    private final String PATH_INFO = "org.apache.cxf.message.Message.PATH_INFO";
    private final String REQUEST_URI = "org.apache.cxf.request.uri";
    private final String REQUEST_URL = "org.apache.cxf.request.url";

    public URLValidationInterceptor() throws IOException, APIManagementException {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (message.get(PATH_INFO).toString()
                .contains(message.get(BASE_PATH).toString().concat(latestVersion + pathSeparator))) {
            message.put(PATH_INFO, message.get(PATH_INFO).toString()
                    .replace(latestVersion + pathSeparator, ""));
            message.put(REQUEST_URI, message.get(REQUEST_URI).toString().
                    replace(latestVersion + pathSeparator, ""));
            message.put(REQUEST_URL, message.get(REQUEST_URL).toString().
                    replace(latestVersion + pathSeparator, ""));
            message.put(RestApiConstants.API_VERSION, latestVersion);
        }
        if (message.get(PATH_INFO).toString()
                .contains(message.get(BASE_PATH).toString().concat(majorVersion + pathSeparator))) {
            message.put(PATH_INFO, message.get(PATH_INFO).toString()
                    .replace(majorVersion + pathSeparator, ""));
            message.put(REQUEST_URI, message.get(REQUEST_URI).toString()
                    .replace(majorVersion + pathSeparator, ""));
            message.put(REQUEST_URL, message.get(REQUEST_URL).toString()
                    .replace(majorVersion + pathSeparator, ""));
            message.put(RestApiConstants.API_VERSION, majorVersion);
        }

    }

}

