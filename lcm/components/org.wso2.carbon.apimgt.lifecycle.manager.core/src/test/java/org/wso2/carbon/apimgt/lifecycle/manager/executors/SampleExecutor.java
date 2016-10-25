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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.SampleAPI;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.InputBean;
import org.wso2.carbon.apimgt.lifecycle.manager.interfaces.Executor;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a sample executor class runs with unit tests..
 */
public class SampleExecutor implements Executor {

    private static Logger  log = LoggerFactory.getLogger(SampleExecutor.class);

    private Map parameterMap = new HashMap();

    @Override
    public void init(Map parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public void execute(Object resource, String currentState, String targetState) {
        SampleAPI api = (SampleAPI) resource;
        for (InputBean inputBean : api.getLifecycleState().getInputBeanList()) {
            log.info(inputBean.getName() + " : " + inputBean.getValues());
        }
        log.info("executed #####################################################");
    }

}
