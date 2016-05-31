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

import org.wso2.carbon.apimgt.gateway.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

public class DataBridgeExecutionTimePublisherDTO extends ExecutionTimePublisherDTO {
    public DataBridgeExecutionTimePublisherDTO(ExecutionTimePublisherDTO executionTimePublisherDTO) {
        setApiName(executionTimePublisherDTO.getApiName());
        setContext(executionTimePublisherDTO.getContext());
        setVersion(executionTimePublisherDTO.getVersion());
        setProvider(executionTimePublisherDTO.getProvider());
        setMediationType(executionTimePublisherDTO.getMediationType());
        setTenantDomain(executionTimePublisherDTO.getTenantDomain());
        setTenantId(executionTimePublisherDTO.getTenantId());
        setExecutionTime(executionTimePublisherDTO.getExecutionTime());
        setEventTime(executionTimePublisherDTO.getEventTime());
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
                "      \"name\": \"mediationName\",\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"executionTime\",\n" +
                "      \"type\": \"LONG\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"context\",\n" +
                "      \"type\": \"STRING\"\n" +
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
                getTenantDomain(), getProvider(), getMediationType(), getExecutionTime(), getContext(), getEventTime()};
    }
}
