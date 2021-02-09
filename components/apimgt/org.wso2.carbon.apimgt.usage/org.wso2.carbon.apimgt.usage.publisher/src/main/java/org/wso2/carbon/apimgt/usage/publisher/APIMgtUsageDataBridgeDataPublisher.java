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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.publisher.dto.*;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    public void init() {
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        // Ignore data publish
    }

    public void publishEvent(ThrottlePublisherDTO throttPublisherDTO) {
        // Ignore data publish
    }

    /**
     * This method will publish event for alert types configurations.
     * @param alertTypeDTO DTO object.
     * @throws APIManagementException
     */
    @Override
    public void publishEvent(AlertTypeDTO alertTypeDTO) throws APIManagementException {

        // Ignore data publish
    }

    @Override
    public void publishEvent(RequestResponseStreamDTO requestStream) {
        // Ignore data publish
    }

    @Override
    public void publishEvent(BotDataDTO botDataDTO) {
        // Ignore data publish
    }
}
