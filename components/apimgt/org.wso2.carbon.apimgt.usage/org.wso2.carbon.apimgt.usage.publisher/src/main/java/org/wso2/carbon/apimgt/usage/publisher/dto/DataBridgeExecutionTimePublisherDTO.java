/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class DataBridgeExecutionTimePublisherDTO extends ExecutionTimePublisherDTO {
    public DataBridgeExecutionTimePublisherDTO(ExecutionTimePublisherDTO executionTimePublisherDTO) {
        setApiName(executionTimePublisherDTO.getApiName());
        setContext(executionTimePublisherDTO.getContext());
        setVersion(executionTimePublisherDTO.getVersion());
        setProvider(executionTimePublisherDTO.getProvider());
        setTenantDomain(executionTimePublisherDTO.getTenantDomain());
        setTenantId(executionTimePublisherDTO.getTenantId());
        setApiResponseTime(executionTimePublisherDTO.getApiResponseTime());
        setEventTime(executionTimePublisherDTO.getEventTime());
        setSecurityLatency(executionTimePublisherDTO.getSecurityLatency());
        setThrottlingLatency(executionTimePublisherDTO.getThrottlingLatency());
        setRequestMediationLatency(executionTimePublisherDTO.getRequestMediationLatency());
        setResponseMediationLatency(executionTimePublisherDTO.getResponseMediationLatency());
        setBackEndLatency(executionTimePublisherDTO.getBackEndLatency());
        setOtherLatency(executionTimePublisherDTO.getOtherLatency());
    }

    public static String getStreamDefinition() {

        return "{\n" +
                "  \"name\": \"" + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getExecutionTimeStreamName
                () + "\",\n" +
                "  \"version\": \"" + DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                .getExecutionTimeStreamVersion() + "\",\n" +
                "  \"nickName\": \"Execution Time Data\",\n" +
                "  \"description\": \"This stream will persist the data which send by the mediation executions\",\n" +
                "  \"metaData\": [\n" +
                "    {\n" +
                "      \"name\": \"clientType\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"payloadData\": [\n" +
                "    {\n" +
                "      \"name\": \"api\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"api_version\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"tenantDomain\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"apiPublisher\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"apiResponseTime\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"context\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"securityLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"throttlingLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"requestMediationLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"responseMediationLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"backendLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"otherLatency\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"eventTime\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    }\n" +

                "  ]\n" +
                "}";
    }

    public Object createPayload() {
        return new Object[]{getApiName(), getVersion(),
                getTenantDomain(), getProvider(), getApiResponseTime(), getContext(), getSecurityLatency(),
                getThrottlingLatency(), getRequestMediationLatency(), getResponseMediationLatency(),
                getBackEndLatency(), getOtherLatency(), getEventTime()};
    }
}
