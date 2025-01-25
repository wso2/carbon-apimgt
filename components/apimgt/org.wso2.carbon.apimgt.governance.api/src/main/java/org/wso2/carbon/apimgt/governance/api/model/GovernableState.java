/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a governable state
 */
public enum GovernableState {
    API_CREATE,
    API_UPDATE,
    API_DEPLOY,
    API_PUBLISH;

    public static GovernableState fromString(String stateString) {
        if ("api_create".equalsIgnoreCase(stateString)) {
            return GovernableState.API_CREATE;
        } else if ("api_update".equalsIgnoreCase(stateString)) {
            return GovernableState.API_UPDATE;
        } else if ("api_deploy".equalsIgnoreCase(stateString)) {
            return GovernableState.API_DEPLOY;
        } else if ("api_publish".equalsIgnoreCase(stateString)) {
            return GovernableState.API_PUBLISH;
        }
        return null;
    }


    /**
     * This method returns all the governable states that should be evaluated when a request to a single governable
     * state is made
     * That is we need to run all the policies for the requested state and all the states before that
     * - API_CREATE -> [API_CREATE]
     * - API_UPDATE -> [API_CREATE, API_UPDATE]
     * - API_DEPLOY -> [API_CREATE, API_UPDATE, API_DEPLOY]
     * - API_PUBLISH -> [API_CREATE, API_UPDATE, API_DEPLOY, API_PUBLISH]
     *
     * @param state GovernableState
     * @return List of GovernableStates that should be evaluated
     */
    public static List<GovernableState> getDependentGovernableStates(GovernableState state) {
        List<GovernableState> governableStates = new ArrayList<>();

        switch (state) {
            case API_CREATE:
                governableStates.add(GovernableState.API_CREATE);
                break;
            case API_UPDATE:
                governableStates.add(GovernableState.API_CREATE);
                governableStates.add(GovernableState.API_UPDATE);
                break;
            case API_DEPLOY:
                governableStates.add(GovernableState.API_CREATE);
                governableStates.add(GovernableState.API_UPDATE);
                governableStates.add(GovernableState.API_DEPLOY);
                break;
            case API_PUBLISH:
                governableStates.add(GovernableState.API_CREATE);
                governableStates.add(GovernableState.API_UPDATE);
                governableStates.add(GovernableState.API_DEPLOY);
                governableStates.add(GovernableState.API_PUBLISH);
                break;
        }
        return governableStates;
    }
}