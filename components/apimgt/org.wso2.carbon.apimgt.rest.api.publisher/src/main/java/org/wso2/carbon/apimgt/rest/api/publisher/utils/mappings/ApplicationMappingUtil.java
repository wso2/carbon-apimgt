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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;

public class ApplicationMappingUtil {

    public static ApplicationDTO fromApplicationtoDTO (Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getUUID());
        applicationDTO.setThrottlingTier(application.getTier());
        applicationDTO.setDescription(application.getDescription());
        applicationDTO.setName(application.getName());
        applicationDTO.setGroupId(application.getGroupId());
        applicationDTO.setSubscriber(application.getSubscriber().getName());
        return applicationDTO;
    }

}
