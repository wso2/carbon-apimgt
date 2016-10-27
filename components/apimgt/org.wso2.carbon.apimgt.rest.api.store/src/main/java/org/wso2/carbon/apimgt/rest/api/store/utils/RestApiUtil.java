/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.apimgt.core.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.core.util.exception.*;

public class RestApiUtil {

    public static String getLoggedInUsername() {
        return "DUMMY_LOGGEDUSER";
    }

    public static String getLoggedInUserTenantDomain() {
        return "DUMMY_TENANTdOMAIN";//CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }


    /**
     * Returns the current logged in consumer's group id
     * @return group id of the current logged in user.
     */
    @SuppressWarnings("unchecked")
    public static String getLoggedInUserGroupId() {
//        String username = RestApiUtil.getLoggedInUsername();
//        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
//        JSONObject loginInfoJsonObj = new JSONObject();
//        try {
//            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
//            loginInfoJsonObj.put("user", username);
//            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//                loginInfoJsonObj.put("isSuperTenant", true);
//            } else {
//                loginInfoJsonObj.put("isSuperTenant", false);
//            }
//            String loginInfoString = loginInfoJsonObj.toJSONString();
//            return apiConsumer.getGroupIds(loginInfoString);
//        } catch (APIManagementException e) {
//            String errorMsg = "Unable to get groupIds of user " + username;
//            handleInternalServerError(errorMsg, e, log);
            return null;
//        }
    }

    /**
     * Logs the error, builds a ForbiddenException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws ForbiddenException
     */
    public static void handleAuthorizationFailure(String resource, String id, Log log) throws ForbiddenException {
        ForbiddenException forbiddenException = buildForbiddenException(resource, id);
        log.error(forbiddenException.getMessage());
        throw forbiddenException;
    }

    /**
     * Returns a new ForbiddenException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new ForbiddenException with the specified details as a response DTO
     */
    public static ForbiddenException buildForbiddenException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "You don't have permission to access the " + resource + " with Id " + id;
        } else {
            description = "You don't have permission to access the " + resource;
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_FORBIDDEN_MESSAGE_DEFAULT, 403l, description);
        return new ForbiddenException(errorDTO);
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Log log) throws NotFoundException {
        NotFoundException notFoundException = buildNotFoundException(resource, id);
        log.error(notFoundException.getMessage());
        throw notFoundException;
    }
    /**
     * Returns a new NotFoundException
     *
     * @param resource Resource type
     * @param id identifier of the resource
     * @return a new NotFoundException with the specified details as a response DTO
     */
    public static NotFoundException buildNotFoundException(String resource, String id) {
        String description;
        if (!StringUtils.isEmpty(id)) {
            description = "Requested " + resource + " with Id '" + id + "' not found";
        } else {
            description = "Requested " + resource + " not found";
        }
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404l, description);
        return new NotFoundException(errorDTO);
    }

    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param t Throwable instance
     * @param log Log instance
     * @throws InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Throwable t, Log log) throws InternalServerErrorException {
        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
        log.error(msg, t);
        throw internalServerErrorException;
    }

    /**
     * Returns a new InternalServerErrorException
     *
     * @return a new InternalServerErrorException with default details as a response DTO
     */
    public static InternalServerErrorException buildInternalServerErrorException() {
        ErrorDTO errorDTO = getErrorDTO(RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, 500l,
                RestApiConstants.STATUS_INTERNAL_SERVER_ERROR_DESCRIPTION_DEFAULT);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * Returns a generic errorDTO
     *
     * @param message specifies the error message
     * @return A generic errorDTO with the specified details
     */
    public static ErrorDTO getErrorDTO(String message, Long code, String description){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMoreInfo("");
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }


}
