/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.MonetizationUsagePublishInfo;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationUsagePublishInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PublishStatusDTO;

/**
 * This class manage mapping to DTO of monetization usage publisher
 */
public class MonetizationAPIMappinUtil {

    /**
     * Set value to DTO of the monetization usage publisher
     *
     * @param status status of the request
     * @param msg    description of the the status
     * @return the DTO for monetization usage publish API
     */
    public static PublishStatusDTO fromStatusToDTO(String status, String msg) {

        PublishStatusDTO publishStatusDTO = new PublishStatusDTO();
        publishStatusDTO.setStatus(status);
        publishStatusDTO.setMessage(msg);
        return publishStatusDTO;
    }

    /**
     * Set the info of monetization usage publisher to DTO of the monetization usage status
     *
     * @param info info about the monetization usage publisher job
     * @return the DTO for monetization usage publish status
     */
    public static MonetizationUsagePublishInfoDTO fromUsageStateToDTO(MonetizationUsagePublishInfo info) {

        MonetizationUsagePublishInfoDTO monetizationUsagePublishInfo = new MonetizationUsagePublishInfoDTO();
        monetizationUsagePublishInfo.setState(info.getState());
        monetizationUsagePublishInfo.setStatus(info.getStatus());
        monetizationUsagePublishInfo.setStartedTime(Long.toString(info.getStartedTime()));
        monetizationUsagePublishInfo.setLastPublsihedTime(Long.toString(info.getLastPublishTime()));
        return monetizationUsagePublishInfo;
    }
}
