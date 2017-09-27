/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.throttling.publisher;


import org.apache.axis2.context.MessageContext;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

public class DataProcessAndPublishingAgentWrapper extends DataProcessAndPublishingAgent {
    private ThrottleProperties throttleProperties;

    public DataProcessAndPublishingAgentWrapper(ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }



    @Override
    protected ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    @Override
    protected DataPublisher getDataPublisher() {
        return Mockito.mock(DataPublisher.class);
    }

    @Override
    protected void buildMessage(MessageContext axis2MessageContext) throws IOException, XMLStreamException {
    }
}
