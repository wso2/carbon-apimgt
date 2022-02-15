/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;

import java.io.IOException;

public class V2URLValidationInterceptor extends URLValidationInterceptor {

    private static final Log log = LogFactory.getLog(V2URLValidationInterceptor.class);
    private static String majorVersion = "v2";
    private static String latestVersion = "v2.1";

    public V2URLValidationInterceptor() throws IOException, APIManagementException {
        super();
    }

    @Override
    @MethodStats
    public void handleMessage(Message message) throws Fault {
        validateMessage(message, majorVersion, latestVersion);
    }
}

