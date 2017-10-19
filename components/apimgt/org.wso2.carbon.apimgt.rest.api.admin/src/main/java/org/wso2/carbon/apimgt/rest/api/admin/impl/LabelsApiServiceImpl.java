/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import java.util.HashMap;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

public class LabelsApiServiceImpl extends LabelsApiService {

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceImpl.class);

    /**
     * Delete label by label id
     *
     * @param labelId           Id of the label
     * @param ifMatch           if-Match header value
     * @param ifUnmodifiedSince if-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK if the operation is successful
     * @throws NotFoundException If failed to find the particular resource
     */
    @Override
    public Response labelsLabelIdDelete(String labelId, String ifMatch, String ifUnmodifiedSince, Request request
    ) throws NotFoundException {

        try {
            if (labelId != null) {
                APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
                apiMgtAdminService.deleteLabel(labelId);
            } else {
                //mandatory parameters not provided
                String errorMessage = "Label Id parameter should be provided";
                ErrorHandler errorHandler = ExceptionCodes.PARAMETER_NOT_PROVIDED;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
                log.error(errorMessage);
                return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting the label [labelId] " + labelId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

        return Response.ok().build();
    }

    @Override
    public Response labelsGet(String ifNoneMatch, String ifModifiedSince, Request request) throws NotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response labelsLabelIdGet(String labelId, String ifNoneMatch, String ifModifiedSince, Request request)
            throws NotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response labelsLabelIdPut(String labelId, LabelDTO body, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response labelsPost(LabelDTO body, Request request) throws NotFoundException {
        // TODO Auto-generated method stub
        return null;
    }
}
