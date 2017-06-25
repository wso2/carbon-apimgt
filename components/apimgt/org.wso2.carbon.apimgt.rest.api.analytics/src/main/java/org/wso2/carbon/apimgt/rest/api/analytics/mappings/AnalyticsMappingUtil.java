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
package org.wso2.carbon.apimgt.rest.api.analytics.mappings;

import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsMappingUtil {

    /**
     * Converts and ApplicationCountList to if failed to get admin api resource
     *
     * @param applicationCountList list of ApplicationCount objects
     * @return corresponding ApplicationCountListDTO object
     */
    public static ApplicationCountListDTO fromApplicationCountToListDTO(List<ApplicationCount> applicationCountList) {
        ApplicationCountListDTO applicationCountListDTO = new ApplicationCountListDTO();
        List<ApplicationCountDTO> applicationCountDTOList = new ArrayList<>();
        applicationCountListDTO.setCount(applicationCountList.size());

        for (int i = 0; i < applicationCountList.size(); i++) {
            applicationCountDTOList.add(fromApplicationCountToDTO(applicationCountList.get(i)));
        }

        applicationCountListDTO.setList(applicationCountDTOList);
        return applicationCountListDTO;
    }

    private static ApplicationCountDTO fromApplicationCountToDTO(ApplicationCount applicationCount) {
        ApplicationCountDTO applicationCountDTO = new ApplicationCountDTO();

        applicationCountDTO.setTime(applicationCount.getTimestamp());
        applicationCountDTO.setCount(applicationCount.getCount());

        return applicationCountDTO;
    }
}