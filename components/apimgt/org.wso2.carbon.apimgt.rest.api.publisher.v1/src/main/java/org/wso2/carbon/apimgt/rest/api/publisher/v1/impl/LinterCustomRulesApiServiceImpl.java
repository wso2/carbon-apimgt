/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LinterCustomRulesApiServiceImpl implements LinterCustomRulesApiService {

    private static final Log log = LogFactory.getLog(LinterCustomRulesApiServiceImpl.class);
    private static final String linterCustomRulesKey = "LinterCustomRules";

    /**
     * Get linter custom rules (LinterCustomRules) from the tenant config
     *
     * @return content of LinterCustomRules in tenant config
     * @throws APIManagementException
     */
    public Response getLinterCustomRules(MessageContext messageContext) throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantConfig = apiAdmin.getTenantConfig(RestApiCommonUtil.getLoggedInUserTenantDomain());

        try {
            JSONObject jsonObject = new JSONObject(tenantConfig);
            if (jsonObject.has(linterCustomRulesKey)) {
                String customRulesJson = jsonObject.getJSONObject(linterCustomRulesKey).toString();
                return Response.ok().entity(customRulesJson)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
            }
        } catch (JSONException e) {
            RestApiUtil.handleInternalServerError("Error while getting linter custom rules from the tenant " +
                    "config", e, log);
        }
        return null;
    }
}
