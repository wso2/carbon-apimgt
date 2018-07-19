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

public class DataBridgeThrottlePublisherDTO extends ThrottlePublisherDTO {

    public DataBridgeThrottlePublisherDTO(ThrottlePublisherDTO throttlePublisherDTO) {

        setAccessToken(throttlePublisherDTO.getAccessToken());
        setUsername(throttlePublisherDTO.getUsername());
        setTenantDomain(throttlePublisherDTO.getTenantDomain());
        setApiname(throttlePublisherDTO.getApiname());
        setVersion(throttlePublisherDTO.getVersion());
        setContext(throttlePublisherDTO.getContext());
        setApiCreator(throttlePublisherDTO.getApiCreator());
        setApiCreatorTenantDomain(throttlePublisherDTO.getApiCreatorTenantDomain());
        setThrottledTime(throttlePublisherDTO.getThrottledTime());
        setApplicationName(throttlePublisherDTO.getApplicationName());
        setApplicationId(throttlePublisherDTO.getApplicationId());
        setThrottledOutReason(throttlePublisherDTO.getThrottledOutReason());
        setKeyType(throttlePublisherDTO.getKeyType());
        setCorrelationID(throttlePublisherDTO.getCorrelationID());
        setGatewayType(throttlePublisherDTO.getGatewayType());
        setSubscriber(throttlePublisherDTO.getSubscriber());
    }

    public Object createPayload() {
        return new Object[] { getKeyType(), getAccessToken(), getUsername(), getTenantDomain(), getApiname(),
                getVersion(), getContext(), getApiCreator(), getApiCreatorTenantDomain(), getApplicationId(),
                getApplicationName(), getSubscriber(), getThrottledTime(), getThrottledOutReason(), getGatewayType() };
    }

    public Object createMetaData() {
        return null;
    }
}
