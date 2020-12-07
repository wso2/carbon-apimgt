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
package org.wso2.carbon.apimgt.rest.api.util.interceptors.request;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class LogRequestInInterceptor extends AbstractPhaseInterceptor {

    public LogRequestInInterceptor() {

        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        //message.get("org.apache.cxf.request.method") = PUT
        //         InputStream in = message.getContent(InputStream.class);
        //        byte payload[] = IOUtils.readBytesFromStream(in);
        //        ByteArrayInputStream bin = new ByteArrayInputStream(payload);
        //        message.setContent(InputStream.class, bin);
        //message.get("path_to_match_slash") = /apis/13db822a-b52e-401f-99a6-da45dc05c7b6/swagger

        //Read the resource path and verbs for history events on API Manager configuration
        //Check whether invoking request matches with that list, if yes add event and payload to the Exchange object
    }
}

