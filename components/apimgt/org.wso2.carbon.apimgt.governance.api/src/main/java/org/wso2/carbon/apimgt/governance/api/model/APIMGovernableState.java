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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a governable state
 */
public enum APIMGovernableState {
    API_CREATE,
    API_UPDATE,
    API_DEPLOY,
    API_PUBLISH;

    public static APIMGovernableState fromString(String stateString) {
        try {
            return APIMGovernableState.valueOf(stateString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns all governable states that should be evaluated when a request to a single governable state is made.
     * That is, we need to run all policies for the requested state and all states before it.
     *
     * Example:
     * - API_CREATE  -> [API_CREATE]
     * - API_UPDATE  -> [API_CREATE, API_UPDATE]
     * - API_DEPLOY  -> [API_CREATE, API_UPDATE, API_DEPLOY]
     * - API_PUBLISH -> [API_CREATE, API_UPDATE, API_DEPLOY, API_PUBLISH]
     *
     * @param state The requested governable state.
     * @return List of states that should be evaluated.
     */
    public static List<APIMGovernableState> getDependentGovernableStates(APIMGovernableState state) {
        return Stream.of(values())
                .filter(s -> s.ordinal() <= state.ordinal())
                .collect(Collectors.toList());
    }
}
