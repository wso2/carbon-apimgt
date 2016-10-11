/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.executors;

import org.wso2.carbon.apimgt.lifecycle.manager.impl.interfaces.Execution;

import java.util.HashMap;
import java.util.Map;

public class ServiceVersionExecutor implements Execution{

    private Map parameterMap = new HashMap();
    @Override public void init(Map parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override public boolean execute(Object resource, String currentState, String targetState) {
        System.out.println("executed #####################################################");
        return true;
    }

}
