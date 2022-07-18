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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.SecurityManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.xml.sax.SAXException;

public class RegistryLCManager {

    private static Log log = LogFactory.getLog(RegistryLCManager.class);
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private static final String STATE_ID_PROTOTYPED = "Prototyped";
    private static final String STATE_ID_PUBLISHED = "Published";
    private static final String TRANSITION_TARGET_PROTOTYPED = "Prototyped";
    private static final String TRANSITION_TARGET_PUBLISHED = "Published";
    private Map<String, String> stateTransitionMap = new HashMap<String, String>();
    private Map<String, StateInfo> stateInfoMap = new HashMap<String, StateInfo>();
    private HashMap<String, LifeCycleTransition> stateHashMap = new HashMap<String, LifeCycleTransition>();

    public RegistryLCManager(int tenantId)
            throws RegistryException, XMLStreamException, ParserConfigurationException, SAXException, IOException {
        UserRegistry registry;

        JSONObject jsonObject = getDefaultLCConfigJSON();
        JSONArray states = (JSONArray) jsonObject.get("States");

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
        createdState.put("State","Created");
        JSONArray transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Publish","Published"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype","Prototyped"));
        createdState.put("Transitions",transitionArray);
        createdState.put("CheckItems",getCheckItemsArray(
                new String[]{
                        "Deprecate old versions after publishing the API",
                        "Requires re-subscription when publishing the API"}));

        //Prototyped State
        JSONObject prototypedState = new JSONObject();
        prototypedState.put("State","Prototyped");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Publish","Published"));
        transitionArray.add(getTransitionObj("Demote to Created","Created"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype","Prototyped"));
        prototypedState.put("Transitions",transitionArray);
        prototypedState.put("CheckItems",getCheckItemsArray(
                new String[]{
                        "Deprecate old versions after publishing the API",
                        "Requires re-subscription when publishing the API"}));

        //Published State
        JSONObject publishedState = new JSONObject();
        publishedState.put("State","Published");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Block","Blocked"));
        transitionArray.add(getTransitionObj("Deploy as a Prototype","Prototyped"));
        transitionArray.add(getTransitionObj("Demote to Created","Created"));
        transitionArray.add(getTransitionObj("Deprecate","Deprecated"));
        transitionArray.add(getTransitionObj("Publish","Published"));
        publishedState.put("Transitions",transitionArray);

        //Blocked State
        JSONObject blockedState = new JSONObject();
        blockedState.put("State","Blocked");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Deprecate","Deprecated"));
        transitionArray.add(getTransitionObj("Re-Publish","Published"));
        blockedState.put("Transitions",transitionArray);

        //Deprecated State
        JSONObject deprecatedState = new JSONObject();
        deprecatedState.put("State","Deprecated");
        transitionArray = new JSONArray();
        transitionArray.add(getTransitionObj("Retire","Retired"));
        deprecatedState.put("Transitions",transitionArray);

        //Retired State
        JSONObject retiredState = new JSONObject();
        retiredState.put("State","Retired");

        //Adding the all State info objects to statesArray
        statesArray.add(createdState);
        statesArray.add(prototypedState);
        statesArray.add(publishedState);
        statesArray.add(blockedState);
        statesArray.add(deprecatedState);
        statesArray.add(retiredState);

        LCConfigObj.put("States",statesArray);

        return LCConfigObj;
    }

    public JSONObject getTransitionObj(String event,String target){
        JSONObject transitionObj = new JSONObject();
        transitionObj.put("Event",event);
        transitionObj.put("Target",target);
        return transitionObj;
    }

    public JSONArray getCheckItemsArray(String[] checkItems){
        JSONArray checkItemsArray = new JSONArray();
        for(String checkItem:checkItems){
            checkItemsArray.add(checkItem);
        }
        return checkItemsArray;
    }

    public String getTransitionAction(String currentState, String targetState) {
        if (stateHashMap.containsKey(currentState)) {
            LifeCycleTransition transition = stateHashMap.get(currentState);
            return transition.getAction(targetState);
        }
        return null;
    }

    public String getStateForTransition(String action) {
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
    
    public List<String> getCheckListItemsForState(String state) {
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getCheckListItems();
        }
        return null;
    }

    public List<String> getAllowedActionsForState(String state) {
        if (stateInfoMap.containsKey(state)) {
            return stateInfoMap.get(state).getTransitions();
        }
        return null;
    }
    /**
     * Returns a secured DocumentBuilderFactory instance
     *
     * @return DocumentBuilderFactory
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        org.apache.xerces.impl.Constants Constants = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }
}
