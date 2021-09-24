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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
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

        registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
        String data = CommonUtil.getLifecycleConfiguration("APILifeCycle", registry);
        DocumentBuilderFactory factory = getSecuredDocumentBuilder();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(inputStream);
        Element root = doc.getDocumentElement();

        // Get all nodes with state
        NodeList states = root.getElementsByTagName("state");
        int nStates = states.getLength();
        for (int i = 0; i < nStates; i++) {
            Node node = states.item(i);
            Node id = node.getAttributes().getNamedItem("id");
            if (id != null && !id.getNodeValue().isEmpty()) {
                LifeCycleTransition lifeCycleTransition = new LifeCycleTransition();
                List<String> actions = new ArrayList<String>();
                List<String> checklistItems = new ArrayList<String>();
                NodeList stateChiledNodes = node.getChildNodes();
                int nTransitions = stateChiledNodes.getLength();
                for (int j = 0; j < nTransitions; j++) {
                    Node transition = stateChiledNodes.item(j);
                    // Add transitions
                    if ("transition".equals(transition.getNodeName())) {
                        Node target = transition.getAttributes().getNamedItem("target");
                        Node action = transition.getAttributes().getNamedItem("event");
                        if (id.getNodeValue().equals(STATE_ID_PROTOTYPED)
                                && (target.getNodeValue().equals(TRANSITION_TARGET_PROTOTYPED)
                                )) {
                            // skip adding "Publish" and "Deploy as a Prototype" transitions as having those transitions
                            // in Prototyped state is invalid
                        } else if (id.getNodeValue().equals(STATE_ID_PUBLISHED)
                                && target.getNodeValue().equals(TRANSITION_TARGET_PUBLISHED)) {
                            // skip adding "Publish" transition as having this transition in Published state is invalid
                        } else {
                            if (target != null && action != null) {
                                lifeCycleTransition.addTransition(target.getNodeValue().toUpperCase(),
                                        action.getNodeValue());
                                stateTransitionMap.put(action.getNodeValue(), target.getNodeValue().toUpperCase());
                                actions.add(action.getNodeValue());
                            }
                        }
                    }
                    if ("datamodel".equals(transition.getNodeName())) {
                        NodeList datamodels = transition.getChildNodes();
                        int nDatamodel = datamodels.getLength();
                        for (int k = 0; k < nDatamodel; k++) {
                            Node dataNode = datamodels.item(k);
                            if (dataNode != null && dataNode.getAttributes() != null && "checkItems"
                                    .equals(dataNode.getAttributes().getNamedItem("name").getNodeValue())) {
                                NodeList items = dataNode.getChildNodes();
                                for (int x = 0; x < items.getLength(); x++) {
                                    Node item = items.item(x);
                                    if (item != null && item.getAttributes() != null
                                            && item.getAttributes().getNamedItem("name") != null) {
                                        checklistItems.add(item.getAttributes().getNamedItem("name").getNodeValue());

                                    }
                                }
                            }
                        }
                    }
                }
                stateHashMap.put(id.getNodeValue().toUpperCase(), lifeCycleTransition);
                StateInfo stateInfo = new StateInfo();
                stateInfo.setCheckListItems(checklistItems);
                stateInfo.setTransitions(actions);
                stateInfoMap.put(id.getNodeValue().toUpperCase(), stateInfo);
            }
        }

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
