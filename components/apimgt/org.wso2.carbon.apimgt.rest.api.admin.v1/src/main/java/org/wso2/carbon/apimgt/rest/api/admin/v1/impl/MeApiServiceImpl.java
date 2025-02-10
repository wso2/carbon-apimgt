/*
 * Copyright (c) 2025, WSO2 LLc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.rest.api.admin.v1.MeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;


public class MeApiServiceImpl implements MeApiService {

    public Response organizationInformation(MessageContext messageContext) throws APIManagementException {
        OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
        OrganizationInfoDTO dto = new OrganizationInfoDTO();
        dto.setName(orgInfo.getName());
        dto.setOrganizationId(orgInfo.getOrganizationId());
        return Response.ok().entity(dto).build();
    }
}
