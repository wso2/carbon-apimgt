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
package org.wso2.carbon.apimgt.persistence.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryLCManager {

    private static RegistryLCManager regLCManager;
    private Map<String, List<State>> lcMap = new HashMap<String, List<State>>();
    private Map<String, String> stateTransitionMap = new HashMap<String, String>();

    private RegistryLCManager() {
        //TODO parse this using lifecycle.xml
        List<State> createdList = new ArrayList<RegistryLCManager.State>();
        createdList.add(new State("Published", "Publish"));
        createdList.add(new State("Prototyped", "Deploy as a Prototype"));
        lcMap.put("Created", createdList);

        List<State> prototypedList = new ArrayList<RegistryLCManager.State>();
        prototypedList.add(new State("Published", "Publish"));
        prototypedList.add(new State("Prototyped", "Deploy as a Prototype"));
        prototypedList.add(new State("Created", "Demote to Created"));
        lcMap.put("Prototyped", prototypedList);

        List<State> publishedList = new ArrayList<RegistryLCManager.State>();
        publishedList.add(new State("Published", "Publish"));
        publishedList.add(new State("Prototyped", "Deploy as a Prototype"));
        publishedList.add(new State("Created", "Demote to Created"));
        publishedList.add(new State("Deprecated", "Deprecate"));
        publishedList.add(new State("Blocked", "Block"));
        lcMap.put("Published", publishedList);

        List<State> blockedList = new ArrayList<RegistryLCManager.State>();
        blockedList.add(new State("Deprecated", "Deprecate"));
        blockedList.add(new State("Published", "Re-Publish"));
        lcMap.put("Blocked", blockedList);

        List<State> deprecatedList = new ArrayList<RegistryLCManager.State>();
        deprecatedList.add(new State("Retired", "Retire"));
        lcMap.put("Deprecated", deprecatedList);
        
        // transitions action -> state
        stateTransitionMap.put("Publish", "Published");
        stateTransitionMap.put("Deploy as a Prototype", "Prototyped");
        stateTransitionMap.put("Demote to Created", "Created");
        stateTransitionMap.put("Block", "Blocked");
        stateTransitionMap.put("Deprecate", "Deprecated");
        stateTransitionMap.put("Re-Publish", "Published");
        stateTransitionMap.put("Retire", "Retired");
    }

    public static RegistryLCManager getInstance() {
        if (regLCManager == null) {
            regLCManager = new RegistryLCManager();
        }
        return regLCManager;
    }

    public String getTransitionAction(String currentState, String targetState) {
        if (lcMap.containsKey(currentState)) {
            List<State> stateList = lcMap.get(currentState);
            for (State state : stateList) {
                if (targetState.equalsIgnoreCase(state.getState())) {
                    return state.getTransition();
                }
            }
        }
        return null;
    }
    public String getStateForTransition(String action){
        return stateTransitionMap.get(action);
    }

    class State {
        private final String state;
        private final String event;

        public State(String state, String event) {
            this.state = state;
            this.event = event;
        }

        public String getState() {
            return state;
        }

        public String getTransition() {
            return event;
        }
    }
}
