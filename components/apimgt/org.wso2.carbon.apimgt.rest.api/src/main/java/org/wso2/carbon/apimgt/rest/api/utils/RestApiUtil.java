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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorListItemDTO;
import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RestApiUtil {

    public static APIProvider getProvider (String providerName) throws APIManagementException {
        return APIManagerFactory.getInstance().getAPIProvider(providerName);
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

    public static ErrorDTO getAuthenticationErrorDTO(String message) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(message);
        return errorDTO;
    }
}
