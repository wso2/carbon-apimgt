/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the LifeCycle
 */
public class LCManager {

    private static final String STATE_ID_PROTOTYPED = "Prototyped";
    private static final String STATE_ID_PUBLISHED = "Published";
    private static final String TRANSITION_TARGET_PROTOTYPED = "Prototyped";
    private static final String TRANSITION_TARGET_PUBLISHED = "Published";
    private static final String LIFECYCLE_KEY = "LifeCycle";
    private static final String STATES_KEY = "States";
    private static final String STATE_KEY = "State";
    private static final String TRANSITIONS_KEY = "Transitions";
    private static final String CHECK_ITEMS_KEY = "CheckItems";
    private static final String API_LIFECYCLE_PATH = "lifecycle/APILifeCycle.json";
    private static final String EVENT_KEY = "Event";
    private static final String TARGET_KEY = "Target";
    private static JSONObject defaultLCObj;
    private static final Log log = LogFactory.getLog(LCManager.class);

    static {
        if (defaultLCObj == null) {

            try {
                defaultLCObj = getDefaultLCConfigJSON();
            } catch (APIManagementException e) {
                log.error("Error while Reading/Parsing the Default LifeCycle Json");
            }
        }
    }

    private final Map<String, String> stateTransitionMap = new HashMap<>();
    private final Map<String, StateInfo> stateInfoMap = new HashMap<>();
    private final HashMap<String, LifeCycleTransition> stateHashMap = new HashMap<>();
    private final String tenantDomain;

    /**
     * Initialize LCManager Class
     *
     * @param tenantDomain
     */
    public LCManager(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Reading the default API Lifecycle
     *
     * @return
     * @throws URISyntaxException
     * @throws APIManagementException
     */
    public static JSONObject getDefaultLCConfigJSON() throws APIManagementException {

        try (InputStream lcStream = LCManager.class.getClassLoader().getResourceAsStream(API_LIFECYCLE_PATH);
             InputStreamReader reader = new InputStreamReader(lcStream, StandardCharsets.UTF_8)) {
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            throw new APIManagementException(e);
        }
    }

    /**
     * Process the lifecycle object into the states
     *
     * @throws APIManagementException
     */
    private void processLifeCycle() throws APIManagementException {

        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONArray states;

        //Checking whether the lifecycle exists in the tenantConfig
        if (tenantConfig.containsKey(LIFECYCLE_KEY)) {
            JSONObject lcObj = (JSONObject) tenantConfig.get(LIFECYCLE_KEY);
            states = (JSONArray) lcObj.get(STATES_KEY);
        } else {
            JSONObject jsonObject = defaultLCObj;
            states = (JSONArray) jsonObject.get(STATES_KEY);
        }

        for (Object state : states) {
            JSONObject stateObj = (JSONObject) state;
            String stateId = (String) stateObj.get(STATE_KEY);
            LifeCycleTransition lifeCycleTransition = new LifeCycleTransition();
            List<String> actions = new ArrayList<>();
            List<String> checklistItems = new ArrayList<>();
            if (stateObj.containsKey(TRANSITIONS_KEY)) {
                JSONArray transitions = (JSONArray) stateObj.get(TRANSITIONS_KEY);
                for (Object transition : transitions) {
                    JSONObject transitionObj = (JSONObject) transition;
                    String action = (String) transitionObj.get(EVENT_KEY);
                    String target = (String) transitionObj.get(TARGET_KEY);
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

            if (stateObj.containsKey(CHECK_ITEMS_KEY)) {
                JSONArray checkItems = (JSONArray) stateObj.get(CHECK_ITEMS_KEY);
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

    /**
     * Returning the Action for the current and the target state
     *
     * @param currentState
     * @param targetState
     * @return
     * @throws APIManagementException
     */
    public String getTransitionAction(String currentState, String targetState) throws APIManagementException {

        processLifeCycle();
        if (stateHashMap.containsKey(currentState)) {
            LifeCycleTransition transition = stateHashMap.get(currentState);
            return transition.getAction(targetState);
        }
        return null;
    }

    /**
     * Get State Transition for the action
     *
     * @param action
     * @return
     * @throws APIManagementException
     */
    public String getStateForTransition(String action) throws APIManagementException {

        processLifeCycle();
        return stateTransitionMap.get(action);
    }

    /**
     * Get check list items for the state
     *
     * @param state
     * @return
     * @throws APIManagementException
     */
    public List<String> getCheckListItemsForState(String state) throws APIManagementException {

        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getCheckListItems();
        }
        return null;
    }

    /**
     * Get allowed actions for the state
     *
     * @param state
     * @return
     * @throws APIManagementException
     */
    public List<String> getAllowedActionsForState(String state) throws APIManagementException {

        processLifeCycle();
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getTransitions();
        }
        return null;
    }

    static class LifeCycleTransition {

        private final HashMap<String, String> transitions;

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
         * @param action       action associated with target
         */
        public void addTransition(String targetStatus, String action) {

            transitions.put(targetStatus, action);
        }
    }

    static class StateInfo {

        private String state;
        private List<String> transitions = new ArrayList<>();
        private List<String> checkListItems = new ArrayList<>();

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

}
