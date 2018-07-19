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

import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

public class DataBridgeFaultPublisherDTO extends FaultPublisherDTO{

    public DataBridgeFaultPublisherDTO(FaultPublisherDTO faultPublisherDTO){
        setApplicationConsumerKey(faultPublisherDTO.getApplicationConsumerKey());
        setApiContext(faultPublisherDTO.getApiContext());
        setApiVersion(faultPublisherDTO.getApiVersion());
        setApiName(faultPublisherDTO.getApiName());
        setApiResourcePath(faultPublisherDTO.getApiResourcePath());
        setApiMethod(faultPublisherDTO.getApiMethod());
        setApiVersion(faultPublisherDTO.getApiVersion());
        setErrorCode(faultPublisherDTO.getErrorCode());
        setErrorMessage(faultPublisherDTO.getErrorMessage());
        setRequestTimestamp((faultPublisherDTO.getRequestTimestamp()));
        setUsername(faultPublisherDTO.getUsername());
        setApiCreatorTenantDomain(faultPublisherDTO.getApiCreatorTenantDomain());
        setHostname(DataPublisherUtil.getHostAddress());
        setApiCreator(faultPublisherDTO.getApiCreator());
        setApplicationName(faultPublisherDTO.getApplicationName());
        setApplicationId(faultPublisherDTO.getApplicationId());
        setProtocol(faultPublisherDTO.getProtocol());
        setMetaClientType(faultPublisherDTO.getMetaClientType());
        setGatewaType(faultPublisherDTO.getGatewaType());
        setUserTenantDomain(faultPublisherDTO.getUserTenantDomain());
    }

    public Object createPayload() {
        return new Object[] { getMetaClientType(), getApplicationConsumerKey(), getApiName(), getApiVersion(),
                getApiContext(), getApiResourcePath(), getApiMethod(), getApiCreator(), getApiCreatorTenantDomain(),
                getUsername(), getUserTenantDomain(), getHostname(), getApplicationId(), getApplicationName(),
                getProtocol(), getErrorCode(), getErrorMessage(), getRequestTimestamp(), getGatewaType() };
    }

    public Object createMetaData() {
        return null;
    }
}
