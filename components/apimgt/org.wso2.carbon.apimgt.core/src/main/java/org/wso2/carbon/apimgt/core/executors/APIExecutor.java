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

package org.wso2.carbon.apimgt.core.executors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.core.Executor;
import org.wso2.carbon.apimgt.lifecycle.manager.core.beans.CheckItemBean;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.util.Map;

public class APIExecutor implements Executor {
    private static final Logger log = LoggerFactory.getLogger(APIExecutor.class);

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user. These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     *                     <p>
     *                     Eg:- <execution forEvent="Promote"
     *                     class="org.wso2.carbon.governance.registry.extensions.executors.ServiceVersionExecutor">
     *                     <parameter name="currentEnvironment" value="/_system/governance/trunk/"/>
     *                     <parameter name="targetEnvironment" value="/_system/governance/branches/testing/"/>
     *                     <parameter name="service.mediatype" value="application/vnd.wso2-service+xml"/>
     *                     </execution>
     *                     <p>
     *                     The parameters defined here are passed to the executor using this method.
     */
    @Override
    public void init(Map parameterMap) {

    }

    /**
     * This method will be called when the invoke() method of the default lifecycle implementation is called.
     * Execution logic should reside in this method since the default lifecycle implementation will determine
     * the execution output by looking at the output of this method.
     *
     * @param resource     The resource in which the lifecycle state is changed.
     * @param currentState Current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @throws LifecycleException If exception occurs while running the executor.
     */
    @Override
    public void execute(Object resource, String currentState, String targetState) throws LifecycleException {
        API api = (API) resource;
        LifecycleState lifecycleState = api.getLifecycleState();
        boolean deprecateOlderVersions = false;
        boolean requireReSubscriptions = false;
        if (!currentState.equals(targetState)) {
            //todo:This place need to write how to handle Gateway publishing
            try {
                APIPublisher apiPublisher = APIManagerFactory.getInstance().getAPIProvider(api.getProvider());
                for (CheckItemBean checkItemBean : lifecycleState.getCheckItemBeanList()) {
                    if (APIConstants.DEPRECATE_PREVIOUS_VERSIONS.equals(checkItemBean.getName())) {
                        deprecateOlderVersions = checkItemBean.isValue();
                    }
                    if (APIConstants.REQUIRE_RE_SUBSCRIPTIONS.equals(checkItemBean.getName())) {
                        requireReSubscriptions = checkItemBean.isValue();
                    }
                }
                apiPublisher.updateAPIForStateChange(api.getId(), targetState, deprecateOlderVersions,
                        requireReSubscriptions);
            } catch (APIManagementException e) {
                throw new LifecycleException("Couldn't create APIPublisher from user", e);
            }
        }
    }
}
