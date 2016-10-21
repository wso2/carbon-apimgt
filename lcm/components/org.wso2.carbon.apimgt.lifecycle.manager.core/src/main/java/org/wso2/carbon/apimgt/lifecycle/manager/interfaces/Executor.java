/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.interfaces;


import java.util.Map;

/**
 * This is the interface that is used to write custom executors to lifecycles
 * Executors are code segments that will run once a transition happens
 * */
public interface Executor {

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user. These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     *
     *                     Eg:- <execution forEvent="Promote"
     *                     class="org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor">
                                    <parameter name="currentEnvironment" value="/_system/governance/trunk/"/>
                                    <parameter name="targetEnvironment" value="/_system/governance/branches/testing/"/>
                                    <parameter name="service.mediatype" value="application/vnd.wso2-service+xml"/>
                                </execution>

                           The parameters defined here are passed to the executor using this method.
     * */
    void init(Map parameterMap);

    /**
     * This method will be called when the invoke() method of the default lifecycle implementation is called.
     * Execution logic should reside in this method since the default lifecycle implementation will determine
     * the execution output by looking at the output of this method.
     *

     * @param targetState The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     * */
    boolean execute(Object resource, String currentState, String targetState);
}
