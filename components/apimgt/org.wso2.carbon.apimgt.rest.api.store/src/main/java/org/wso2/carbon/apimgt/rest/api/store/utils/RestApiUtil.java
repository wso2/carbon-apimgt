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

import org.apache.commons.logging.Log;


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
    public static void handleAuthorizationFailure(String resource, String id, Log log) {
            //throws ForbiddenException {
//        ForbiddenException forbiddenException = buildForbiddenException(resource, id);
//        log.error(forbiddenException.getMessage());
//        throw forbiddenException;
    }

    /**
     * Logs the error, builds a NotFoundException with specified details and throws it
     *
     * @param resource requested resource
     * @param id id of resource
     * @param log Log instance
     * @throws NotFoundException
     */
    public static void handleResourceNotFoundError(String resource, String id, Log log) {
//            throws NotFoundException {
//        NotFoundException notFoundException = buildNotFoundException(resource, id);
//        log.error(notFoundException.getMessage());
//        throw notFoundException;
    }
    /**
     * Logs the error, builds a internalServerErrorException with specified details and throws it
     *
     * @param msg error message
     * @param t Throwable instance
     * @param log Log instance
     * @throws InternalServerErrorException
     */
    public static void handleInternalServerError(String msg, Throwable t, Log log) {
//            throws InternalServerErrorException {
//        InternalServerErrorException internalServerErrorException = buildInternalServerErrorException();
//        log.error(msg, t);
//        throw internalServerErrorException;
    }



}
