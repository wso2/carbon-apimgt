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
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

public class DataBridgeRequestPublisherDTO extends RequestPublisherDTO {

    public DataBridgeRequestPublisherDTO (RequestPublisherDTO requestPublisherDTO){
        setConsumerKey(requestPublisherDTO.getConsumerKey());
        setContext(requestPublisherDTO.getContext());
        setApi_version(requestPublisherDTO.getApi_version());
        setApi(requestPublisherDTO.getApi());
        setResourcePath(requestPublisherDTO.getResourcePath());
        setMethod(requestPublisherDTO.getMethod());
        setVersion(requestPublisherDTO.getVersion());
        setRequestTime(requestPublisherDTO.getRequestTime());
        setUsername(requestPublisherDTO.getUsername());
        setTenantDomain(requestPublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(requestPublisherDTO.getApiPublisher());
        setApplicationName(requestPublisherDTO.getApplicationName());
        setApplicationId(requestPublisherDTO.getApplicationId());
    }

    public static String getStreamDefinition() {
        String streamDefinition = "{" +
                "  'name':'" + APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_NAME + "'," +
                "  'version':'" + APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_VERSION + "'," +
                "  'nickName': 'API Manager Request Data'," +
                "  'description': 'Request Data'," +
                "  'metaData':[" +
                "          {'name':'clientType','type':'STRING'}" +
                "  ]," +
                "  'payloadData':[" +
                "          {'name':'consumerKey','type':'STRING'}," +
                "          {'name':'context','type':'STRING'}," +
                "          {'name':'api_version','type':'STRING'}," +
                "          {'name':'api','type':'STRING'}," +
                "          {'name':'resourcePath','type':'STRING'}," +
                "          {'name':'method','type':'STRING'}," +
                "          {'name':'version','type':'STRING'}," +
                "          {'name':'request','type':'INT'}," +
                "          {'name':'requestTime','type':'LONG'}," +
                "          {'name':'userId','type':'STRING'}," +
                "          {'name':'tenantDomain','type':'STRING'}," +
                "          {'name':'hostName','type':'STRING'}," +
                "          {'name':'apiPublisher','type':'STRING'}," +
                "          {'name':'applicationName','type':'STRING'}," +
                "          {'name':'applicationId','type':'STRING'}" +
                "  ]" +
                "}";

        return streamDefinition;
    }

    public Object createPayload(){
        return new Object[]{getConsumerKey(),getContext(),getApi_version(),getApi(), getResourcePath(),getMethod(),
                            getVersion(), getRequestCount(),getRequestTime(),getUsername(),getTenantDomain(),getHostName(),
                            getApiPublisher(), getApplicationName(), getApplicationId()};
        
    }

}
