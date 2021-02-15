/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.uri.template.URITemplate;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class PreAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(PreAuthenticationInterceptor.class);
    public PreAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        String path = (String) message.get(Message.PATH_INFO);
        if (path.contains(APIConstants.RestApiConstants.REST_API_OLD_VERSION)) {
            path = path.replace("/" + APIConstants.RestApiConstants.REST_API_OLD_VERSION, "");
        }
        String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        Dictionary<URITemplate,List<String>> allowedResourcePathsMap;

        //If Authorization headers are present anonymous URI check will be skipped
        ArrayList authHeaders = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get(RestApiConstants.AUTH_HEADER_NAME);
        if (authHeaders != null)
            return;

        //Check if the accessing URI is allowed and then authorization is skipped
        try {
            allowedResourcePathsMap = RestApiUtil.getAllowedURIsToMethodsMap();
            Enumeration<URITemplate> uriTemplateSet = allowedResourcePathsMap.keys();

            while (uriTemplateSet.hasMoreElements()) {
                URITemplate uriTemplate = uriTemplateSet.nextElement();
                if (uriTemplate.matches(path, new HashMap<String, String>())) {
                    List<String> allowedVerbs = allowedResourcePathsMap.get(uriTemplate);
                    if (allowedVerbs.contains(httpMethod)) {
                        message.put(RestApiConstants.AUTHENTICATION_REQUIRED, false);
                        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        carbonContext.setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
                        carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                        return;
                    }
                }
            }
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Unable to retrieve/process allowed URIs for REST API", e, logger);
        }
    }
}
