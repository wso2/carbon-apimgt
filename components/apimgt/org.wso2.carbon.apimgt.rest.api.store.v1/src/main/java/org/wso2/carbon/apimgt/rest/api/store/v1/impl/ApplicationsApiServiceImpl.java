/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    @Override
    public Response applicationsGet(String groupId, String query, String sortBy, String sortOrder,
            Integer limit, Integer offset, String ifNoneMatch) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DEFAULT_SORT_ORDER;
        sortBy = sortBy != null ?
                ApplicationMappingUtil.getApplicationSortByField(sortBy) :
                RestApiConstants.SORT_BY_NAME;
        query = query == null ? "" : query;
        ApplicationListDTO applicationListDTO = new ApplicationListDTO();

        String username = RestApiUtil.getLoggedInUsername();
        
        // todo: Do a second level filtering for the incoming group ID.
        // todo: eg: use case is when there are lots of applications which is accessible to his group "g1", he wants to see
        // todo: what are the applications shared to group "g2" among them. 
        groupId = RestApiUtil.getLoggedInUserGroupId();
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            Subscriber subscriber = new Subscriber(username);
            Application[] applications;
            applications = apiConsumer
                    .getApplicationsWithPagination(new Subscriber(username), groupId, offset, limit, query, sortBy,
                            sortOrder);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            int applicationCount = apiMgtDAO.getAllApplicationCount(subscriber, groupId, query);

            applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(applications, apiConsumer);
            ApplicationMappingUtil.setPaginationParams(applicationListDTO, groupId, limit, offset,
                    applications.length);

            PaginationDTO paginationDTO = new PaginationDTO();
            paginationDTO.setOffset(offset);
            paginationDTO.setLimit(limit);
            paginationDTO.setTotal(applicationCount);
            applicationListDTO.setPagination(paginationDTO);

            return Response.ok().entity(applicationListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of applications available.
                // Thus sends an empty response
                applicationListDTO.setCount(0);
                applicationListDTO.setNext("");
                applicationListDTO.setPrevious("");
                return Response.ok().entity(applicationListDTO).build();
            } else {
                String errorMessage = "Error while retrieving Applications";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response applicationsApplicationIdDelete(String applicationId, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdGenerateKeysPost(String applicationId,
            ApplicationKeyGenerateRequestDTO body) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdGet(String applicationId, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdKeysGet(String applicationId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId,
            String keyType, ApplicationTokenGenerateRequestDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypeGet(String applicationId, String keyType,
            String groupId) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypePut(String applicationId, String keyType,
            ApplicationKeyDTO body) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId,
            String keyType) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdMapKeysPost(String applicationId,
            ApplicationKeyMappingRequestDTO body) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String ifMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsApplicationIdScopesGet(String applicationId, Boolean filterByUserRoles,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response applicationsPost(ApplicationDTO body){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
