/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.store.v1.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.SdkGenServiceImpl;

import java.util.List;
import javax.ws.rs.core.Response;

public class SdkGenApiServiceImpl implements SdkGenApiService {

    private static final Log log = LogFactory.getLog(SdkGenApiServiceImpl.class);

    /**
     * Rest API implementation to get the supported sdk languages
     */
    @Override
    public Response sdkGenLanguagesGet(MessageContext messageContext) {

        List<String> languageList = SdkGenServiceImpl.getSdkGenLanguageList();
        return Response.ok().entity(languageList).build();
    }
}
