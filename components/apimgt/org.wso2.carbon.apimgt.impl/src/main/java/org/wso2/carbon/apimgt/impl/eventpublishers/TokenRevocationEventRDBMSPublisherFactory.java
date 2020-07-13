/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.eventpublishers;

import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TokenRevocationEventRDBMSPublisherFactory extends OutputEventAdapterFactory {
    private static final String TOKEN_REVOCATION_RDBMS_PUBLISHER = "token_revocation-rdbms";
    @Override
    public String getType() {

        return TOKEN_REVOCATION_RDBMS_PUBLISHER;
    }

    @Override
    public List<String> getSupportedMessageFormats() {

        List<String> supportedMessageFormats = new ArrayList<>();
        supportedMessageFormats.add(MessageType.MAP);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {

        return new ArrayList<>();
    }

    @Override
    public List<Property> getDynamicPropertyList() {

        return new ArrayList<>();
    }

    @Override
    public String getUsageTips() {

        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration outputEventAdapterConfiguration,
                                                 Map<String, String> map) {

        return new TokenRevocationEventRDBMSPublisher();
    }
}
