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
package org.wso2.carbon.apimgt.usage.publisher;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;


public class Test {

    public static void main(String[] args) throws Exception {

        APIManagerConfiguration config = new APIManagerConfiguration();

        //TODO: Populate the config with required BAM parameters - config.load(testFilePath)

        APIMgtUsageDataBridgeDataPublisher testPublisher = new APIMgtUsageDataBridgeDataPublisher();

        RequestPublisherDTO testRequestPublisherDTO = new RequestPublisherDTO();
        ResponsePublisherDTO testResponsePublisherDTO = new ResponsePublisherDTO();
        DataBridgeRequestPublisherDTO testDataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(testRequestPublisherDTO);
        DataBridgeResponsePublisherDTO testDataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(testResponsePublisherDTO);

        //Only the properties needed for the test are set
        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("FacebookAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("TwitterAPI");
        testRequestPublisherDTO.setVersion("v1.0.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("TwitterAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.1.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("DeliciousAPI");
        testRequestPublisherDTO.setVersion("v1.2.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testRequestPublisherDTO.setApi("FacebookAPI");
        testRequestPublisherDTO.setVersion("v1.2.0");
        testRequestPublisherDTO.setRequestTime(System.currentTimeMillis());
        testPublisher.publishEvent(testDataBridgeRequestPublisherDTO);

        testResponsePublisherDTO.setApi("DeliciousAPI");
        testResponsePublisherDTO.setVersion("v1.0.0");
        testResponsePublisherDTO.setResponseTime(System.currentTimeMillis());
        testResponsePublisherDTO.setServiceTime(5L);
        testPublisher.publishEvent(testDataBridgeResponsePublisherDTO);

    }
}

