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
package org.wso2.carbon.apimgt.impl.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LCManager {

    private static Log log = LogFactory.getLog(LCManager.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private static final String STATE_ID_PROTOTYPED = "Prototyped";
    private static final String STATE_ID_PUBLISHED = "Published";
    private static final String TRANSITION_TARGET_PROTOTYPED = "Prototyped";
    private static final String TRANSITION_TARGET_PUBLISHED = "Published";
    private Map<String, String> stateTransitionMap = new HashMap<String, String>();
    private Map<String, StateInfo> stateInfoMap = new HashMap<String, StateInfo>();
    private HashMap<String, LifeCycleTransition> stateHashMap = new HashMap<String, LifeCycleTransition>();
    private static String tenantDomain;

    public static void setTenantDomain(String tenantDomain) {
        LCManager.tenantDomain = tenantDomain;
    }

    public static String getTenantDomain() {
        return tenantDomain;
    }

    public LCManager(String tenantDomain) throws APIManagementException {
        setTenantDomain(tenantDomain);
    }

    public void processLifeCycle() throws APIManagementException {
        JSONObject tenantConfig = APIUtil.getTenantConfig(getTenantDomain());
        JSONArray states;

        //Checking whether the lifecycle exists in the tenantConfig
        if (tenantConfig.containsKey("LifeCycle")) {
            JSONObject LCObj = (JSONObject) tenantConfig.get("LifeCycle");
            states = (JSONArray) LCObj.get("States");
        } else {
            JSONObject jsonObject = getDefaultLCConfigJSON();
            states = (JSONArray) jsonObject.get("States");
        }

        for (Object state : states) {
            JSONObject stateObj = (JSONObject) state;
            String stateId = (String) stateObj.get("State");
            LifeCycleTransition lifeCycleTransition = new LifeCycleTransition();
            List<String> actions = new ArrayList<String>();
            List<String> checklistItems = new ArrayList<String>();
            if (stateObj.containsKey("Transitions")) {
                JSONArray transitions = (JSONArray) stateObj.get("Transitions");
                for (Object transition : transitions) {
                    JSONObject transitionObj = (JSONObject) transition;
                    String action = (String) transitionObj.get("Event");
                    String target = (String) transitionObj.get("Target");
                    if (stateId.equals(STATE_ID_PROTOTYPED)
                            && (target.equals(TRANSITION_TARGET_PROTOTYPED)
                    )) {
                        // skip adding "Publish" and "Deploy as a Prototype" transitions as having those transitions
                        // in Prototyped state is invalid
                    } else if (stateId.equals(STATE_ID_PUBLISHED)
                            && target.equals(TRANSITION_TARGET_PUBLISHED)) {
                        // skip adding "Publish" transition as having this transition in Published state is invalid
                    } else {
                        if (target != null && action != null) {
                            lifeCycleTransition.addTransition(target.toUpperCase(),
                                    action);
                            stateTransitionMap.put(action, target.toUpperCase());
                            actions.add(action);
                        }
                    }
                }
            }

            if (stateObj.containsKey("CheckItems")) {
                JSONArray checkItems = (JSONArray) stateObj.get("CheckItems");
                for (Object checkItem : checkItems) {
                    checklistItems.add(checkItem.toString());
                }
            }

            stateHashMap.put(stateId.toUpperCase(), lifeCycleTransition);
            StateInfo stateInfo = new StateInfo();
            stateInfo.setCheckListItems(checklistItems);
            stateInfo.setTransitions(actions);
            stateInfoMap.put(stateId.toUpperCase(), stateInfo);
        }
    }

    public JSONObject getDefaultLCConfigJSON() {

        JSONObject LCConfigObj = new JSONObject();
        JSONArray statesArray = new JSONArray();

        //Created State
        JSONObject createdState = new JSONObject();
        createdState.put("State", "Created");
        JSONArray transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Publish", "Published"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype", "Prototyped"));
        createdState.put("Transitions", transitionArray);
        createdState.put("CheckItems", getCheckItemsArray(
                new String[]{
                        "Deprecate old versions after publishing the API",
                        "Requires re-subscription when publishing the API"}));

        //Prototyped State
        JSONObject prototypedState = new JSONObject();
        prototypedState.put("State", "Prototyped");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Publish", "Published"));
        transitionArray.add(getTransitionObj("Demote to Created", "Created"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype", "Prototyped"));
        prototypedState.put("Transitions", transitionArray);
        prototypedState.put("CheckItems", getCheckItemsArray(
                new String[]{
                        "Deprecate old versions after publishing the API",
                        "Requires re-subscription when publishing the API"}));

        //Published State
        JSONObject publishedState = new JSONObject();
        publishedState.put("State", "Published");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Block", "Blocked"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype", "Prototyped"));
        transitionArray.add(getTransitionObj("Demote to Created", "Created"));
        transitionArray.add(getTransitionObj("Deprecate", "Deprecated"));
        transitionArray.add(getTransitionObj("Publish", "Published"));
        publishedState.put("Transitions", transitionArray);

        //Blocked State
        JSONObject blockedState = new JSONObject();
        blockedState.put("State", "Blocked");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Deprecate", "Deprecated"));
        transitionArray.add(getTransitionObj("Re-Publish", "Published"));
        blockedState.put("Transitions", transitionArray);

        //Deprecated State
        JSONObject deprecatedState = new JSONObject();
        deprecatedState.put("State", "Deprecated");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Retire", "Retired"));
        deprecatedState.put("Transitions", transitionArray);

        //Retired State
        JSONObject retiredState = new JSONObject();
        retiredState.put("State", "Retired");

        //Adding the all State info objects to statesArray
        statesArray.add(createdState);
        statesArray.add(prototypedState);
        statesArray.add(publishedState);
        statesArray.add(blockedState);
        statesArray.add(deprecatedState);
        statesArray.add(retiredState);

        LCConfigObj.put("States", statesArray);
        return LCConfigObj;
    }

    public JSONObject getTransitionObj(String event, String target) {
        JSONObject transitionObj = new JSONObject();
        transitionObj.put("Event", event);
        transitionObj.put("Target", target);
        return transitionObj;
    }

    public JSONArray getCheckItemsArray(String[] checkItems) {
        JSONArray checkItemsArray = new JSONArray();
        for (String checkItem : checkItems) {
            checkItemsArray.add(checkItem);
        }
        return checkItemsArray;
    }

    public String getTransitionAction(String currentState, String targetState) throws APIManagementException {
        processLifeCycle();
        if (stateHashMap.containsKey(currentState)) {
            LifeCycleTransition transition = stateHashMap.get(currentState);
            return transition.getAction(targetState);
        }
        return null;
    }

    public String getStateForTransition(String action) throws APIManagementException {
        processLifeCycle();
        return stateTransitionMap.get(action);
    }

    class LifeCycleTransition {
        private HashMap<String, String> transitions;

        /**
         * Initialize class
         */
        public LifeCycleTransition() {
            this.transitions = new HashMap<>();
        }

        /**
         * Returns action required to transit to state.
         *
         * @param state State to get action
         * @return lifecycle action associated or null if not found
         */
        public String getAction(String state) {
            if (!transitions.containsKey(state)) {
                return null;
            }
            return transitions.get(state);
        }

        /**
         * Adds a transition.
         *
         * @param targetStatus target status
         * @param action action associated with target
         */
        public void addTransition(String targetStatus, String action) {
            transitions.put(targetStatus, action);
        }
    }

    class StateInfo {
        private String state;
        private List<String> transitions = new ArrayList<String>();
        private List<String> checkListItems = new ArrayList<String>();

        public List<String> getCheckListItems() {
            return checkListItems;
        }

        public void setCheckListItems(List<String> checkListItems) {
            this.checkListItems = checkListItems;
        }

        public List<String> getTransitions() {
            return transitions;
        }

        public void setTransitions(List<String> transitions) {
            this.transitions = transitions;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

    public List<String> getCheckListItemsForState(String state) throws APIManagementException {
        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getCheckListItems();
        }
        return null;
    }

    public List<String> getAllowedActionsForState(String state) throws APIManagementException {
        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getTransitions();
        }
        return null;
    }

}
