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

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

import java.util.ArrayList;
import java.util.List;

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
        setHostName(throttlePublisherDTO.getHostName());
        setProperties(throttlePublisherDTO.getProperties());
    }

    public Object createPayload() {
        return new Object[] { getUsername(), getTenantDomain(), getApiname(),
                getVersion(), getContext(), getApiCreator(), getApiCreatorTenantDomain(), getApplicationId(),
                getApplicationName(), getSubscriber(), getThrottledOutReason(), getGatewayType(), getThrottledTime(),
                getHostName(), DataPublisherUtil.toJsonString(getProperties()) };
    }

    public Object createMetaData() {
        JSONObject obj = new JSONObject();
        obj.put("keyType", getKeyType());
        obj.put("correlationID", getCorrelationID());
        String metaClientType = obj.toJSONString();
        return new Object[]{metaClientType};
    }

    /*
     *  This method validates null for any mandatory field
     *
     *  @return Alist of mandatory values which are null
     *
     * */
    public List<String> getMissingMandatoryValues() {

        List<String> missingMandatoryValues = new ArrayList<String>();
        if (getApiname() == null) {
            missingMandatoryValues.add("API name");
        }
        if (getContext() == null) {
            missingMandatoryValues.add("API context");
        }
        if (getVersion() == null) {
            missingMandatoryValues.add("API version");
        }
        if (getApiCreator() == null) {
            missingMandatoryValues.add("API creator");
        }
        if (getApiCreatorTenantDomain() == null) {
            missingMandatoryValues.add("API creator tenant domain");
        }
        if (getApplicationName() == null) {
            missingMandatoryValues.add("Application names");
        }
        if (getApplicationId() == null) {
            missingMandatoryValues.add("Application ID");
        }
        if (getThrottledOutReason() == null) {
            missingMandatoryValues.add("Throttle out reason");
        }
        if (getHostName() == null) {
            missingMandatoryValues.add("API host name");
        }
        return missingMandatoryValues;
    }

    @Override
    public String toString() {

        return "Application name: " + getApplicationName() + ", Application ID: " + getApplicationId() +
                ", API name: " + getApiname() + ", API version: " + getVersion() +
                ", API context: " + getContext() + ", API creator: " + getApiCreator() +
                ", API creator tenant domain: " + getApiCreatorTenantDomain() + ", Username: " + getUsername() +
                ", Tenant domain: " + getTenantDomain() + ", Host name: " + getHostName() +
                ", Subscriber: " + getSubscriber() + ", Gateway type: " + getGatewayType() +
                ", Throttled out reason: " + getThrottledOutReason() + ", Throttled time: " + getThrottledTime();
    }
}
