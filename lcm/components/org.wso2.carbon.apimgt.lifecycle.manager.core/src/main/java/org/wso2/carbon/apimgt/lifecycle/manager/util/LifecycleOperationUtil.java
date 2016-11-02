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

package org.wso2.carbon.apimgt.lifecycle.manager.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.lifecycle.manager.Executor;
import org.wso2.carbon.apimgt.lifecycle.manager.beans.AvailableTransitionBean;
import org.wso2.carbon.apimgt.lifecycle.manager.beans.CheckItemBean;
import org.wso2.carbon.apimgt.lifecycle.manager.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.beans.InputBean;
import org.wso2.carbon.apimgt.lifecycle.manager.beans.PermissionBean;
import org.wso2.carbon.apimgt.lifecycle.manager.constants.LifecycleConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleEventManager;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LifecycleState;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleStateBean;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This is a utility class which provide methods to support lifecycle operations.
 */
public class LifecycleOperationUtil {

    public static final String CLOSE_ATTRIBUTE_BRACKET = "']";

    /**
     * This method is used to associate a lifecycle with an asset.
     *
     * @param lcName                        LC name which associates with the resource.
     * @param initialState                  Initial lifecycle state to be set.
     * @param user                          The user who invoked the action. This will be used for auditing purposes.
     * @return Object of added life cycle state.
     * @throws LifecycleException  If failed to associate life cycle with asset.
     */
    public static String associateLifecycle(String lcName, String initialState, String user) throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        return lifecycleEventManager.associateLifecycle(lcName, initialState, user);
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param currentState              Current state .
     * @param requiredState             Target lifecycle state.
     * @param id                        State uuid which maps with the asset.
     * @param user                      The user who invoked the action. This will be used for
     *                                  auditing purposes.
     * @throws LifecycleException       If exception occurred while execute life cycle state change.
     */
    public static void changeLifecycleState(String currentState, String requiredState, String id, String user)
            throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        lifecycleEventManager.changeLifecycleState(currentState, requiredState, id, user);
    }

    /**
     * This method will remove state data for a particular lifecycle id.
     *
     * @param uuid                      State uuid which maps with the asset.
     * @throws LifecycleException       If exception occurred while deleting life cycle state data.
     */
    public static void removeLifecycleStateData(String uuid) throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        lifecycleEventManager.removeLifecycleStateData(uuid);
    }

    /**
     * Get current life cycle state object.
     *
     * @return {@code LifecycleState} object represent current life cycle.
     */
    public static LifecycleState getCurrentLifecycleState(String uuid) throws LifecycleException {
        LifecycleState currentLifecycleState = new LifecycleState();
        LifecycleStateBean lifecycleStateBean = getLCStateDataFromID(uuid);
        String lcName = lifecycleStateBean.getLcName();
        Document lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        currentLifecycleState.setLcName(lcName);
        currentLifecycleState.setLifecycleId(uuid);
        currentLifecycleState.setState(lifecycleStateBean.getPostStatus());
        populateItems(currentLifecycleState, lcContent);
        setCheckListItemData(currentLifecycleState, lifecycleStateBean.getCheckListData());
        return currentLifecycleState;
    }

    /**
     * This method need to call for each and event life cycle state changes.
     *
     * @param uuid                              State uuid which maps with the asset.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException      If exception occurred while life cycle state change.
     */
    public static LifecycleStateBean getLCStateDataFromID(String uuid) throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        return lifecycleEventManager.getLifecycleStateData(uuid);
    }

    public static void changeCheckListItem(String uuid, String currentState, String checkListItemName, boolean value)
            throws LifecycleException {
        new LifecycleEventManager().changeCheckListItemData(uuid, currentState, checkListItemName, value);
    }

    /**
     * This method provides set of operations performed to a particular lifecycle id.
     *
     * @param uuid  Lifecycle Id which requires history.
     * @return  List of lifecycle history objects.
     * @throws LifecycleException
     */
    public static List<LifecycleHistoryBean> getLifecycleHistoryFromId(String uuid) throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        return lifecycleEventManager.getLifecycleHistoryFromId(uuid);
    }

    /**
     * This method provides set of lifecycle ids in a particular state.
     *
     * @param state                 Filtering state.
     * @param lcName                Name of the relevant lifecycle.
     * @return                      {@code List<LifecycleHistoryBean>} List of lifecycle ids in the given state.
     * @throws LifecycleException
     */
    public static List<String> getLifecycleIds(String state, String lcName) throws LifecycleException {
        LifecycleEventManager lifecycleEventManager = new LifecycleEventManager();
        return lifecycleEventManager.getLifecycleIds(state, lcName);
    }

    /**
     * This method add state data like, transition inputs, custom executors etc to the lifecycle state object.
     *
     * @param lifecycleState                lc state object which is being populated.
     * @param lcConfig                      lc configuration.
     * @throws LifecycleException           If failed to get lifecycle list.
     */
    public static void populateItems(LifecycleState lifecycleState, Document lcConfig) throws LifecycleException {

        String lcState = lifecycleState.getState();
        lifecycleState.setInputBeanList(populateTransitionInputs(lcConfig, lcState));
        lifecycleState.setCustomCodeBeanList(populateTransitionExecutors(lcConfig, lcState));
        lifecycleState.setAvailableTransitionBeanList(populateAvailableStates(lcConfig, lcState));
        lifecycleState.setPermissionBeanList(populateTransitionPermission(lcConfig, lcState));
        lifecycleState.setCheckItemBeanList(populateCheckItems(lcConfig, lcState));

    }

    public static List<CheckItemBean> populateCheckItems(Document lcConfig, String lcState) throws LifecycleException {
        List<CheckItemBean> checkItemBeanList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_CHECKLIST_ITEM_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                NodeList checkItemsNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < checkItemsNodeList.getLength(); i++) {
                    Node checkItemNode = checkItemsNodeList.item(i);
                    if (checkItemNode.getNodeType() == Node.ELEMENT_NODE) {
                        CheckItemBean checkItemBean = new CheckItemBean();
                        Element checkItemElement = (Element) checkItemNode;
                        checkItemBean.setName(checkItemElement.getAttribute(LifecycleConstants.NAME));
                        checkItemBean.setTargets(Arrays.asList(
                                (checkItemElement.getAttribute(LifecycleConstants.FOR_TARGET_ATTRIBUTE)).split(",")));
                        checkItemBeanList.add(checkItemBean);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while populating checl list items for lifecycle state : " + lcState, e);
        }
        return checkItemBeanList;
    }

    /**
     * This method is used to read lifecycle config and provide input element details for a particular state..
     *
     * @param lcConfig                          Lifecycle config document element.
     * @param lcState                           State which requires information.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static List<InputBean> populateTransitionInputs(Document lcConfig, String lcState)
            throws LifecycleException {
        List<InputBean> inputBeans = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_TRANSITION_INPUT_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                NodeList inputsNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < inputsNodeList.getLength(); i++) {
                    Node inputsNode = inputsNodeList.item(i);
                    if (inputsNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList inputNodeList = inputsNode.getChildNodes();
                        for (int j = 0; j < inputNodeList.getLength(); j++) {
                            if (inputNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element inputNode = (Element) inputNodeList.item(j);
                                InputBean inputBean = new InputBean(inputNode.getAttribute(LifecycleConstants.NAME),
                                        Boolean.parseBoolean(LifecycleConstants.REQUIRED),
                                        inputNode.getAttribute(LifecycleConstants.LABEL),
                                        inputNode.getAttribute(LifecycleConstants.PLACE_HOLDER),
                                        inputNode.getAttribute(LifecycleConstants.TOOLTIP),
                                        inputNode.getAttribute(LifecycleConstants.REGEX),
                                        inputNode.getAttribute(LifecycleConstants.VALUES),
                                        ((Element) inputsNode).getAttribute(LifecycleConstants.FOR_TARGET_ATTRIBUTE));
                                inputBeans.add(inputBean);

                            }
                        }
                    }
                }
            }
        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while populating transition inputs for lifecycle state : " + lcState,
                    e);
        }
        return inputBeans;
    }

    /**
     * This method is used to read lifecycle config and provide transition executor details for a particular state.
     *
     * @param lcConfig                          Lifecycle config document element.
     * @param lcState                           State which requires information.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static List<CustomCodeBean> populateTransitionExecutors(Document lcConfig, String lcState)
            throws LifecycleException {
        List<CustomCodeBean> customCodeBeansList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_TRANSITION_EXECUTION_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                NodeList executionNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < executionNodeList.getLength(); i++) {
                    if (executionNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        CustomCodeBean customCodeBean = new CustomCodeBean();
                        Element executionNode = (Element) executionNodeList.item(i);

                        NodeList parameterNodeList = executionNode.getChildNodes();
                        Map<String, String> paramNameValues = new HashMap<>();
                        for (int j = 0; j < parameterNodeList.getLength(); j++) {

                            if (parameterNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element parameterNode = (Element) parameterNodeList.item(j);
                                paramNameValues.put(parameterNode.getAttribute(LifecycleConstants.NAME),
                                        parameterNode.getAttribute(LifecycleConstants.VALUE_ATTRIBUTE));

                            }
                        }
                        customCodeBean.setClassObject(
                                loadCustomExecutors(executionNode.getAttribute(LifecycleConstants.CLASS_ATTRIBUTE),
                                        paramNameValues));
                        customCodeBean
                                .setTargetName(executionNode.getAttribute(LifecycleConstants.FOR_TARGET_ATTRIBUTE));
                        customCodeBeansList.add(customCodeBean);
                    }
                }
            }

        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while reading transition executors for lifecycle state: " + lcState, e);
        }
        return customCodeBeansList;
    }

    /**
     * This method is used to get the initial state defined in the scxml.
     *
     * @param lcConfig                          Lifecycle config document element.
     * @param lcName                            Name of lifecycle.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static String getInitialState(Document lcConfig, String lcName) throws LifecycleException {
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            XPathExpression exp = xPathInstance.compile(LifecycleConstants.LIFECYCLE_SCXML_ELEMENT_PATH);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            return ((Element) nodeList.item(0)).getAttribute(LifecycleConstants.LIFECYCLE_INITIAL_STATE_ATTRIBUTE);
        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while getting first state for lifecycle state:" + " " + lcName, e);
        }
    }

    /**
     * This method is used to read lifecycle config and provide next available states for the current state..
     *
     * @param lcConfig                          Lifecycle config document element.
     * @param lcState                           State which requires information.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static List<AvailableTransitionBean> populateAvailableStates(Document lcConfig, String lcState)
            throws LifecycleException {
        List<AvailableTransitionBean> availableTransitionBeanList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery =
                    LifecycleConstants.LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH + lcState + CLOSE_ATTRIBUTE_BRACKET
                            + LifecycleConstants.LIFECYCLE_TRANSITION_ELEMENT;
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList transitionNodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            for (int i = 0; i < transitionNodeList.getLength(); i++) {

                if (transitionNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element transitionNode = (Element) transitionNodeList.item(i);
                    AvailableTransitionBean availableTransitionBean = new AvailableTransitionBean(
                            transitionNode.getAttribute(LifecycleConstants.LIFECYCLE_EVENT_ATTRIBUTE),
                            transitionNode.getAttribute(LifecycleConstants.LIFECYCLE_TARGET_ATTRIBUTE));
                    availableTransitionBeanList.add(availableTransitionBean);

                }
            }

        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while reading transition executors for lifecycle state: " + lcState, e);
        }
        return availableTransitionBeanList;
    }

    /**
     * This method is used to read lifecycle config and provide permission details associated with each state change.
     *
     * @param lcConfig                          Lifecycle config document element.
     * @param lcState                           State which requires information.
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static List<PermissionBean> populateTransitionPermission(Document lcConfig, String lcState)
            throws LifecycleException {
        List<PermissionBean> permissionBeanList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_TRANSITION_PERMISSION_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if (nodeList.getLength() > 0) {
                NodeList permissionNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < permissionNodeList.getLength(); i++) {
                    if (permissionNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        PermissionBean permissionBean = new PermissionBean();
                        Element permissionElement = (Element) permissionNodeList.item(i);
                        permissionBean
                                .setForTarget(permissionElement.getAttribute(LifecycleConstants.FOR_TARGET_ATTRIBUTE));
                        if (!"".equals(permissionElement.getAttribute(LifecycleConstants.LIFECYCLE_ROLES_ATTRIBUTE))) {
                            permissionBean.setRoles(Arrays.asList(
                                    permissionElement.getAttribute(LifecycleConstants.LIFECYCLE_ROLES_ATTRIBUTE)
                                            .split(",")));
                        }
                        permissionBeanList.add(permissionBean);

                    }
                }
            }
        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error while reading transition permissions for lifecycle state: " + lcState,
                    e);
        }
        return permissionBeanList;
    }

    /**
     * This method is used to build xpath query to get particular data element from lifecycle config.
     *
     * @param lcState                           State which requires information.
     * @param dataElementName                   Data element (for ex : data name="transitionExecution")
     * @return Lifecycle state bean with state data.
     * @throws LifecycleException
     */
    public static String buildXPathQuery(String lcState, String dataElementName) {
        return LifecycleConstants.LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH + lcState + CLOSE_ATTRIBUTE_BRACKET
                + LifecycleConstants.LIFECYCLE_DATA_ELEMENT_PATH + dataElementName + CLOSE_ATTRIBUTE_BRACKET;
    }

    /**
     * This method is used to read lifecycle config and provide permission details associated with each state change.
     *
     * @param className                         Custom executor class provided in lifecycle config for state change.
     * @param parameterMap                      Parameters provided to executor class.
     * @return Execution class object.
     * @throws LifecycleException
     */
    public static Executor loadCustomExecutors(String className, Map parameterMap) throws LifecycleException {

        Executor customExecutors;
        try {
            Class<?> customCodeClass = LifecycleOperationUtil.class.getClassLoader().loadClass(className);
            customExecutors = (Executor) customCodeClass.newInstance();
            customExecutors.init(parameterMap);

        } catch (Exception e) {
            throw new LifecycleException("Unable to load executions class", e);
        }
        return customExecutors;
    }

    /**
     * This method is used to read lifecycle config and provide permission details associated with each state change.
     *
     * @param lcConfig                          Lifecycle configuration element.
     * @return Document element for the lifecycle confi
     * @throws LifecycleException
     */
    public static Document getLifecycleElement(String lcConfig) throws LifecycleException {

        try {
            InputStream inputStream = new ByteArrayInputStream(lcConfig.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
            return document;
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new LifecycleException("Error while building lifecycle config document element", e);
        }
    }

    public static void setCheckListItemData(LifecycleState lifecycleState, Map<String, Boolean> checkListItemData) {
        for (CheckItemBean checkItemBean : lifecycleState.getCheckItemBeanList()) {
            if (checkListItemData.containsKey(checkItemBean.getName())) {
                checkItemBean.setValue(checkListItemData.get(checkItemBean.getName()));
            }
        }
    }
}
