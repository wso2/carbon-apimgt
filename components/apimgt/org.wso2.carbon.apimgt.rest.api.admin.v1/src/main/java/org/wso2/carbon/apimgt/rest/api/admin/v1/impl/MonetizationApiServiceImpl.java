/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.MonetizationApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.MonetizationCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationUsagePublishInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PublishStatusDTO;

import javax.ws.rs.core.Response;

public class MonetizationApiServiceImpl implements MonetizationApiService {

    /**
     * Run the monetization usage publish job
     *
     * @return Response of the server
     */
    @Override
    public Response publishMonetizationRecords(MessageContext messageContext) throws APIManagementException {

        PublishStatusDTO publishStatusDTO = MonetizationCommonImpl.publishMonetizationRecords();
        if (publishStatusDTO != null) {
            if (publishStatusDTO.getStatus().contains("Request Accepted")) {
                return Response.accepted().entity(publishStatusDTO).build();
            } else {
                return Response.serverError().entity(publishStatusDTO).build();
            }
        }
        return null;
    }

    /**
     * Retrieves the status of the last monetization usage publishing job
     *
     * @return Retruns the status of the last monetization usage publishing jon
     */
    @Override
    public Response getMonetizationUsagePublisherStatus(MessageContext messageContext) throws APIManagementException {

        MonetizationUsagePublishInfoDTO monetizationUsagePublishInfoDTO
                = MonetizationCommonImpl.getMonetizationUsagePublisherStatus();
        return Response.ok().entity(monetizationUsagePublishInfoDTO).build();
    }
}
