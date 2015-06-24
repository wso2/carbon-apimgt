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
        setConsumerKey(faultPublisherDTO.getConsumerKey());
        setContext(faultPublisherDTO.getContext());
        setApi_version(faultPublisherDTO.getApi_version());
        setApi(faultPublisherDTO.getApi());
        setResourcePath(faultPublisherDTO.getResourcePath());
        setMethod(faultPublisherDTO.getMethod());
        setVersion(faultPublisherDTO.getVersion());
        setErrorCode(faultPublisherDTO.getErrorCode());
        setErrorMessage(faultPublisherDTO.getErrorMessage());
        setRequestTime((faultPublisherDTO.getRequestTime()));
        setUsername(faultPublisherDTO.getUsername());
        setTenantDomain(faultPublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(faultPublisherDTO.getApiPublisher());
        setApplicationName(faultPublisherDTO.getApplicationName());
        setApplicationId(faultPublisherDTO.getApplicationId());
        setProtocol(faultPublisherDTO.getProtocol());
    }

    public static String getStreamDefinition() {

        return  "{" +
                "  'name':'" +
                DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamName() + "'," +
                "  'version':'" +
                DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamVersion() + "'," +
                "  'nickName': 'API Manager Fault Data'," +
                "  'description': 'Fault Data'," +
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
                "          {'name':'errorCode','type':'STRING'}," +
                "          {'name':'errorMessage','type':'STRING'}," +
                "          {'name':'requestTime','type':'LONG'}," +
                "          {'name':'userId','type':'STRING'}," +
                "          {'name':'tenantDomain','type':'STRING'}," +
                "          {'name':'hostName','type':'STRING'}," +
                "          {'name':'apiPublisher','type':'STRING'}," +
                "          {'name':'applicationName','type':'STRING'}," +
                "          {'name':'applicationId','type':'STRING'}," +
                "          {'name':'protocol','type':'STRING'}" +
                "  ]" +

                "}";

    }

    public Object createPayload(){
        return new Object[]{getConsumerKey(),getContext(),getApi_version(),getApi(), getResourcePath(),getMethod(),
                getVersion(),getErrorCode(),getErrorMessage(), getRequestTime(),getUsername(),
                getTenantDomain(),getHostName(),getApiPublisher(), getApplicationName(), getApplicationId(),getProtocol()};
    }
}
