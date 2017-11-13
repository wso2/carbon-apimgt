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

public class DataBridgeResponsePublisherDTO extends ResponsePublisherDTO {

    public DataBridgeResponsePublisherDTO(ResponsePublisherDTO responsePublisherDTO) {
        setConsumerKey(responsePublisherDTO.getConsumerKey());
        setContext(responsePublisherDTO.getContext());
        setApiVersion(responsePublisherDTO.getApiVersion());
        setApi(responsePublisherDTO.getApi());
        setResourcePath(responsePublisherDTO.getResourcePath());
        setResourceTemplate(responsePublisherDTO.getResourceTemplate());
        setMethod(responsePublisherDTO.getMethod());
        setVersion(responsePublisherDTO.getVersion());
        setResponseTime(responsePublisherDTO.getResponseTime());
        setServiceTime(responsePublisherDTO.getServiceTime());
        setBackendTime(responsePublisherDTO.getBackendTime());
        setUsername(responsePublisherDTO.getUsername());
        setEventTime(responsePublisherDTO.getEventTime());
        setTenantDomain(responsePublisherDTO.getTenantDomain());
        setHostName(DataPublisherUtil.getHostAddress());
        setApiPublisher(responsePublisherDTO.getApiPublisher());
        setApplicationName(responsePublisherDTO.getApplicationName());
        setApplicationId(responsePublisherDTO.getApplicationId());
        setCacheHit(responsePublisherDTO.getCacheHit());
        setResponseSize(responsePublisherDTO.getResponseSize());
        setProtocol(responsePublisherDTO.getProtocol());
        setResponseCode(responsePublisherDTO.getResponseCode());
        setDestination(responsePublisherDTO.getDestination());
        setKeyType(responsePublisherDTO.getKeyType());
        setCorrelationID(responsePublisherDTO.getCorrelationID());
    }

    public static String getStreamDefinition() {

        return "{" +
               "  'name':'" +
                DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamName() + "'," +
               "  'version':'" +
                DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamVersion() + "'," +
               "  'nickName': 'API Manager Response Data'," +
               "  'description': 'Response Data'," +
               "  'metaData':[" +
               "          {'name':'clientType','type':'STRING'}" +
               "  ]," +
               "  'payloadData':[" +
               "          {'name':'consumerKey','type':'STRING'}," +
               "          {'name':'context','type':'STRING'}," +
               "          {'name':'api_version','type':'STRING'}," +
               "          {'name':'api','type':'STRING'}," +
               "          {'name':'resourcePath','type':'STRING'}," +
               "          {'name':'resourceTemplate','type':'STRING'}," +
               "          {'name':'method','type':'STRING'}," +
               "          {'name':'version','type':'STRING'}," +
               "          {'name':'response','type':'INT'}," +
               "          {'name':'responseTime','type':'LONG'}," +
               "          {'name':'serviceTime','type':'LONG'}," +
               "          {'name':'backendTime','type':'LONG'}," +
               "          {'name':'username','type':'STRING'}," +
               "          {'name':'eventTime','type':'LONG'}," +
               "          {'name':'tenantDomain','type':'STRING'}," +
               "          {'name':'hostName','type':'STRING'}," +
               "          {'name':'apiPublisher','type':'STRING'}," +
               "          {'name':'applicationName','type':'STRING'}," +
               "          {'name':'applicationId','type':'STRING'}," +
               "          {'name':'cacheHit','type':'BOOL'}," +
               "          {'name':'responseSize','type':'LONG'}," +
               "          {'name':'protocol','type':'STRING'}," +
               "          {'name':'responseCode','type':'INT'}" +
               "          {'name':'destination','type':'STRING'}" +
               "  ]" +

               "}";
    }

    public Object createPayload() {
        return new Object[]{getConsumerKey(), getContext(), getApiVersion(), getApi(),
                            getResourcePath(), getResourceTemplate(), getMethod(),
                            getVersion(), getResponse(), getResponseTime(), getServiceTime(), getBackendTime(), getUsername(),
                            getEventTime(), getTenantDomain(), getHostName(),
                            getApiPublisher(), getApplicationName(), getApplicationId(), getCacheHit(),
                            getResponseSize(), getProtocol(), getResponseCode(), getDestination()};
    }

    public Object createMetaData() {
        String jsonString = "{\"keyType\":\"" + getKeyType() + "\",\"correlationID\", \"" + getCorrelationID() + "\"}";
        return new Object[] { jsonString };
    }
}
