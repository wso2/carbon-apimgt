/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.usage.publisher.dto;

import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;

public class DataBridgeThrottlePublisherDTO extends ThrottlePublisherDTO {

    public DataBridgeThrottlePublisherDTO(ThrottlePublisherDTO throttlePublisherDTO) {

        setAccessToken(throttlePublisherDTO.getAccessToken());
        setUsername(throttlePublisherDTO.getUsername());
        setTenantDomain(throttlePublisherDTO.getTenantDomain());
        setApiname(throttlePublisherDTO.getApiname());
        setVersion(throttlePublisherDTO.getVersion());
        setContext(throttlePublisherDTO.getContext());
        setProvider(throttlePublisherDTO.getProvider());
        setThrottledTime(throttlePublisherDTO.getThrottledTime());
        setApplicationName(throttlePublisherDTO.getApplicationName());
        setApplicationId(throttlePublisherDTO.getApplicationId());

    }

    public static String getStreamDefinition() {
        APIMGTConfigReaderService apimgtConfigReaderService =
                UsageComponent.getApiMgtConfigReaderService();
        return "{" +
               "  'name':'" +
               apimgtConfigReaderService.getThrottleStreamName() +
               "'," +
               "  'version':'" +
               apimgtConfigReaderService.getThrottleStreamVersion() +
               "'," +
               "  'nickName': 'API Manager Throttle Data'," +
               "  'description': 'Throttle Data'," +
               "  'metaData':[" +
               "          {'name':'clientType','type':'STRING'}" +
               "  ]," +
               "  'payloadData':[" +
               "          {'name':'accessToken','type':'STRING'}," +
               "          {'name':'userId','type':'STRING'}," +
               "          {'name':'tenantDomain','type':'STRING'}," +
               "          {'name':'api','type':'STRING'}," +
               "          {'name':'api_version','type':'STRING'}," +
               "          {'name':'context','type':'STRING'}," +
               "          {'name':'apiPublisher','type':'STRING'}," +
               "          {'name':'throttledTime','type':'LONG'}," +
               "          {'name':'applicationName','type':'STRING'}," +
               "          {'name':'applicationId','type':'STRING'}" +
               "  ]" +

               "}";
    }

    public Object createPayload() {
        return new Object[]{getAccessToken(), getUsername(), getTenantDomain(), getApiname(),
                            getVersion(), getContext(), getProvider(), getThrottledTime(),
                            getApplicationName(), getApplicationId()};
    }
}
