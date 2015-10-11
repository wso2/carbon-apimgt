/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.utils;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorListItemDTO;
import org.wso2.carbon.context.CarbonContext;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.UUID;

/**
 * This class is used for maintaining
 * common util functionalities which can be reused across REST API implementation
 */
public class RestApiUtil {

    private static final Log log = LogFactory.getLog(RestApiUtil.class);

    /**
     * Get API provider instance of the logged-in user
     *
     * @return  API Provider instance of logged-in user
     * @throws APIManagementException  If an error occurs while retrieving provider
     */
    public static APIProvider getProvider () throws APIManagementException {
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        return APIManagerFactory.getInstance().getAPIProvider(loggedInUser);
    }
    
    public static <T> ErrorDTO getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage("Constraint Violation");
        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();
        for (ConstraintViolation violation : violations) {
            ErrorListItemDTO errorListItemDTO = new ErrorListItemDTO();
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItemDTOs.add(errorListItemDTO);
        }
        errorDTO.setError(errorListItemDTOs);
        return errorDTO;
    }

    /**
     * Checks whether a provided string is a UUID
     *
     * @param apiId String to be checked for the validity as the UUID
     * @return True id provided string matches the format of UUID
     */
    public static boolean isUUID (String apiId) {
        try {
            UUID.fromString(apiId);
            return true;
        } catch (IllegalArgumentException e) {
            // Error is logged and ignored
            // because this exception is thrown just to indicate that the provided String is not a UUID
            if (log.isDebugEnabled()) {
                log.debug(apiId + " is not a valid UUID");
            }
            return false;
        }

    }

    public static ErrorDTO getAuthenticationErrorDTO(String message) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(message);
        return errorDTO;
    }

    public static APIConsumer getConsumer(String subscriberName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
    }

    public static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static String getLoggedInUserTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

}
