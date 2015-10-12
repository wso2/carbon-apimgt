/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;

public class ApplicationMappingUtil {

    public static ApplicationDTO fromApplicationtoDTO (Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getId());
        applicationDTO.setThrottlingTier(application.getTier());
        applicationDTO.setDescription(application.getDescription());
        applicationDTO.setCallbackUrl(application.getCallbackUrl());
        applicationDTO.setName(application.getName());
        applicationDTO.setGroupId(application.getGroupId());
        return applicationDTO;
    }

    public static Application fromDTOtoApplication (ApplicationDTO applicationDTO, Subscriber subscriber) {
        Application application = new Application(applicationDTO.getName(), subscriber);
        application.setTier(applicationDTO.getThrottlingTier());
        application.setDescription(applicationDTO.getDescription());
        application.setCallbackUrl(applicationDTO.getCallbackUrl());
        application.setId(applicationDTO.getApplicationId());
        application.setGroupId(applicationDTO.getGroupId());
        return application;
    }
}
