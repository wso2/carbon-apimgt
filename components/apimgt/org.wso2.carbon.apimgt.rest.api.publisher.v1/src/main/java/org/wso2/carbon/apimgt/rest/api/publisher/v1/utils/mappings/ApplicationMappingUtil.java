/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationInfoDTO;

/**
 * This class is responsible for mapping APIM core application related objects into REST API application related DTOs
 */
public class ApplicationMappingUtil {

    /**
     * Create an ApplicationInfoDTO from an Application object
     * 
     * @param application Application object
     * @return ApplicationInfoDTO containing application information
     */
    public static ApplicationInfoDTO fromApplicationToInfoDTO (Application application) {
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();

        applicationInfoDTO.setApplicationId(application.getUUID());
        applicationInfoDTO.setDescription(application.getDescription());
        applicationInfoDTO.setName(application.getName());
        applicationInfoDTO.setSubscriber(application.getSubscriber().getName());
        applicationInfoDTO.setSubscriptionCount(application.getSubscriptionCount());

        return applicationInfoDTO;
    }
}
