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


package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Subscriber;

import java.util.UUID;

public class TestUtils {

    protected static Application getUniqueApplication() {
        return new Application("TestApplication", getUniqueSubscriber());
    }

    protected static Subscriber getUniqueSubscriber() {
        return new Subscriber(UUID.randomUUID().toString());
    }

    protected static APIIdentifier getUniqueAPIIdentifier() {
        return new APIIdentifier(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID()
                 .toString());
    }
}
