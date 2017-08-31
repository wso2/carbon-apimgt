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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import org.wso2.carbon.apimgt.rest.api.admin.mappings.BlockingConditionMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;


public class BlacklistApiServiceImpl extends BlacklistApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceImpl.class);

    /**
     * Delete blacklist condition using ID
     *
     * @param conditionId       condition ID of the block condition
     * @param ifMatch           IF-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response Object
     * @throws NotFoundException Iif an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response blacklistConditionIdDelete(String conditionId, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received Blacklist Condition DELETE request with id: " + conditionId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deleteBlockConditionByUuid(conditionId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting blacklist condition with UUID " + conditionId;
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get blacklist condition by ID.
     *
     * @param conditionId     ID of the blacklist condition to be retrieved
     * @param ifNoneMatch     IF-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response blacklistConditionIdGet(String conditionId, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received BlockCondition GET request with id: " + conditionId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            BlockConditions blockCondition = apiMgtAdminService.getBlockConditionByUUID(conditionId);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting blacklist condition with UUID " + conditionId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Update blacklist/block condition statues
     *
     * @param conditionId       uuid of the blacklist condition
     * @param body              body of the blacklist status to be updated
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response Object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response blacklistConditionIdPut(String conditionId, BlockingConditionDTO body, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received BlockCondition GET request with id: " + conditionId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            //This will give BlockConditionNotFoundException if there's no block condition exists with UUID
            Boolean status = apiMgtAdminService.updateBlockConditionStateByUUID(conditionId, body.getStatus());
            BlockingConditionDTO dto = null;
            if (status) {
                BlockConditions blockCondition = apiMgtAdminService.getBlockConditionByUUID(conditionId);
                dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(blockCondition);
            }
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting blacklist condition with UUID " + conditionId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Get blacklist conditions.
     *
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response blacklistGet(String ifNoneMatch, String ifModifiedSince, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received BlockCondition GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<BlockConditions> blockConditions = apiMgtAdminService.getBlockConditions();
            BlockingConditionListDTO listDTO = BlockingConditionMappingUtil
                    .fromBlockConditionListToListDTO(blockConditions);
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting blacklist ";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Add a blacklist condition.
     *
     * @param body        DTO object including the blacklist condition data
     * @param request     msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override
    public Response blacklistPost(BlockingConditionDTO body, Request request) throws NotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Received BlockCondition POST request with body: " + body);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            //Add the block condition. It will throw BlockConditionAlreadyExistsException if the condition already
            //  exists in the system
            BlockConditions blockConditions = BlockingConditionMappingUtil
                    .fromBlockingConditionDTOToBlockCondition(body);
            String uuid = apiMgtAdminService.addBlockCondition(blockConditions);
            //retrieve the new blocking condition and send back as the response
            BlockConditions newBlockingCondition = apiMgtAdminService.getBlockConditionByUUID(uuid);
            BlockingConditionDTO dto = BlockingConditionMappingUtil.fromBlockingConditionToDTO(newBlockingCondition);
            return Response.status(Response.Status.CREATED).entity(dto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding blocking condition with UUID " + body.getConditionId();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
