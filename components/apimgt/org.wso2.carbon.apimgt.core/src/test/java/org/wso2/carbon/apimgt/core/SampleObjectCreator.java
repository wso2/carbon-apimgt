/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core;

import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;


public class SampleObjectCreator {

    public static API.APIBuilder getMockAPIObject() {
        return new API.APIBuilder("admin", "Sample", "1.0.0").context("/sample/v1").lifecycleInstanceId
                ("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    public static LifecycleState getMockLifecycleStateObject() {
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("API_LIFECYCLE");
        lifecycleState.setLifecycleId("7a2298c4-c905-403f-8fac-38c73301631f");
        lifecycleState.setState("PUBLISH");
        return lifecycleState;
    }
    public static API getMockApiSummaryObject(){
        return new API.APIBuilder("admin","Sample","1.0.0").build();
    }
}
