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

import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionDataListDTO;

import java.util.ArrayList;
import java.util.List;

public class BotDetectionMappingUtil {

    /**
     * Converts a list of Bot Detection Data model objects into a List DTO
     *
     * @param botDetectionDataList list of Bot Detection data
     * @return A List DTO of converted Bot Detection data
     */
    public static BotDetectionDataListDTO fromBotDetectionModelToDTO(List<BotDetectionData> botDetectionDataList) {

        BotDetectionDataListDTO listDTO = new BotDetectionDataListDTO();
        List<BotDetectionDataDTO> botDetectionDataDTOs = new ArrayList<>();
        for (BotDetectionData botData : botDetectionDataList) {
            botDetectionDataDTOs.add(fromBotDetectionModelToDTO(botData));
        }
        listDTO.setList(botDetectionDataDTOs);
        listDTO.setCount(botDetectionDataDTOs.size());
        return listDTO;
    }

    /**
     * Converts a single Bot Detection Data model into Bot Detection DTO
     *
     * @param botDetectionData Bot Detection Data model object
     * @return Converted Bot Detection Data DTO object
     */
    public static BotDetectionDataDTO fromBotDetectionModelToDTO(BotDetectionData botDetectionData) {

        BotDetectionDataDTO botDetectionDataDTO = new BotDetectionDataDTO();
        botDetectionDataDTO.setRecordedTime(botDetectionData.getCurrentTime());
        botDetectionDataDTO.setMessageID(botDetectionData.getMessageID());
        botDetectionDataDTO.setApiMethod(botDetectionData.getApiMethod());
        botDetectionDataDTO.setHeaderSet(botDetectionData.getHeaderSet());
        botDetectionDataDTO.setMessageBody(botDetectionData.getMessageBody());
        botDetectionDataDTO.setClientIp(botDetectionData.getClientIp());
        return botDetectionDataDTO;
    }

    /**
     * Converts a list of Bot Detection Alert Subscription model objects into a list DTO
     *
     * @param alertSubscriptionList list of Bot Detection Alert Subscriptions
     * @return A List DTO of converted Bot Detection Alert Subscriptions
     */
    public static BotDetectionAlertSubscriptionListDTO fromAlertSubscriptionListToListDTO(
            List<BotDetectionData> alertSubscriptionList) {

        BotDetectionAlertSubscriptionListDTO listDTO = new BotDetectionAlertSubscriptionListDTO();
        List<BotDetectionAlertSubscriptionDTO> alertSubscriptionDTOs = new ArrayList<>();
        for (BotDetectionData alertSubscription : alertSubscriptionList) {
            alertSubscriptionDTOs.add(fromAlertSubscriptionToDTO(alertSubscription));
        }
        listDTO.setList(alertSubscriptionDTOs);
        listDTO.setCount(alertSubscriptionDTOs.size());
        return listDTO;
    }

    /**
     * Converts a single Bot Detection Alert Subscription model into a Bot Detection Alert Subscription DTO
     *
     * @param alertSubscription Bot Detection Alert Subscription model object
     * @return Converted Bot Detection Alert Subscription DTO object
     */
    public static BotDetectionAlertSubscriptionDTO fromAlertSubscriptionToDTO(BotDetectionData alertSubscription) {

        BotDetectionAlertSubscriptionDTO alertSubscriptionDTO = new BotDetectionAlertSubscriptionDTO();
        alertSubscriptionDTO.setUuid(alertSubscription.getUuid());
        alertSubscriptionDTO.setEmail(alertSubscription.getEmail());
        return alertSubscriptionDTO;
    }
}
