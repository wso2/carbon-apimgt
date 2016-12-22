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
package org.wso2.carbon.apimgt.rest.api.store.mappings;


import org.wso2.carbon.apimgt.core.models.APIKey;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationMappingUtil {

    /** Converts an Application[] array into a corresponding ApplicationListDTO
     *
     * @param applications array of Application objects
     * @param limit limit parameter
     * @param offset starting index
     * @return ApplicationListDTO object corresponding to Application[] array
     */
    public static ApplicationListDTO fromApplicationsToDTO(List<Application> applications, int limit, int offset) {
        ApplicationListDTO applicationListDTO = new ApplicationListDTO();
        List<ApplicationInfoDTO> applicationInfoDTOs = applicationListDTO.getList();
        if (applicationInfoDTOs == null) {
            applicationInfoDTOs = new ArrayList<>();
            applicationListDTO.setList(applicationInfoDTOs);
        }

        //identifying the proper start and end indexes
        int start = offset < applications.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= applications.size() - 1 ? offset + limit - 1 : applications.size() - 1;

        for (int i = start; i <= end; i++) {
            applicationInfoDTOs.add(fromApplicationToInfoDTO(applications.get(i)));
        }
        applicationListDTO.setCount(applicationInfoDTOs.size());
        return applicationListDTO;
    }

    public static ApplicationDTO fromApplicationtoDTO (Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getId());
        applicationDTO.setThrottlingTier(application.getTier());
        applicationDTO.setDescription(application.getDescription());
        applicationDTO.setCallbackUrl(application.getCallbackUrl());
        applicationDTO.setName(application.getName());
        applicationDTO.setStatus(application.getStatus());
        applicationDTO.setGroupId(application.getGroupId());
        applicationDTO.setSubscriber(application.getCreatedUser());
        List<ApplicationKeyDTO> applicationKeyDTOs = new ArrayList<>();
        for(APIKey apiKey : application.getKeys()) {
            ApplicationKeyDTO applicationKeyDTO = ApplicationKeyMappingUtil.fromApplicationKeyToDTO(apiKey);
            applicationKeyDTOs.add(applicationKeyDTO);
        }
        applicationDTO.setKeys(applicationKeyDTOs);
        return applicationDTO;
    }

    /** Sets pagination urls for a ApplicationListDTO object given pagination parameters and url parameters
     *
     * @param applicationListDTO a SubscriptionListDTO object
     * @param groupId group id of the applications to be returned
     * @param limit max number of objects returned
     * @param offset starting index
     * @param size max offset
     */
    public static void setPaginationParams(ApplicationListDTO applicationListDTO, String groupId, int limit, int offset,
                                           int size) {

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getApplicationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), groupId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getApplicationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), groupId);
        }
        applicationListDTO.setNext(paginatedNext);
        applicationListDTO.setPrevious(paginatedPrevious);
    }

    public static ApplicationInfoDTO fromApplicationToInfoDTO (Application application) {
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        applicationInfoDTO.setApplicationId(application.getId());
        applicationInfoDTO.setThrottlingTier(application.getTier());
        applicationInfoDTO.setDescription(application.getDescription());
        applicationInfoDTO.setStatus(application.getStatus());
        applicationInfoDTO.setName(application.getName());
        applicationInfoDTO.setGroupId(application.getGroupId());
        applicationInfoDTO.setSubscriber(application.getCreatedUser());
        return applicationInfoDTO;
    }

    public static Application fromDTOtoApplication (ApplicationDTO applicationDTO, String createdUser) {
        //subscriber field of the body is not honored
        Application application = new Application(applicationDTO.getName(), createdUser);
        application.setTier(applicationDTO.getThrottlingTier());
        application.setDescription(applicationDTO.getDescription());
        application.setCallbackUrl(applicationDTO.getCallbackUrl());
        application.setId(applicationDTO.getApplicationId());
        //groupId is not honored for now. Later we can improve by checking admin privileges of the user.
        //application.setGroupId(applicationDTO.getGroupId());
        return application;
    }

}
