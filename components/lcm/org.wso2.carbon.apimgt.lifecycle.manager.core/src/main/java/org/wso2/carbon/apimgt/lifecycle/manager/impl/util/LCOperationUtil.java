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

package org.wso2.carbon.apimgt.lifecycle.manager.impl.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.lifecycle.manager.LifeCycleExecutionException;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.LCEventManager;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.AvailableTransitionBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.CustomCodeBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.beans.InputBean;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.constants.LifecycleConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.impl.interfaces.Execution;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCStateBean;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
public class LCOperationUtil {

    public static String associateLifecycle (String lcName, String initialState) throws LifeCycleExecutionException{
        LCEventManager lcEventManager = new LCEventManager();
        return lcEventManager.associateLifecycle(lcName, initialState);
    }

    public static void changeLifecycleState (String requiredState, String id) throws LifeCycleExecutionException{
        LCEventManager lcEventManager = new LCEventManager();
        lcEventManager.changeLifecycleState(requiredState, id);
    }

    public static LCStateBean getLCStateDataFromID(String uuid) throws LifeCycleExecutionException {
        LCEventManager lcEventManager = new LCEventManager();
        return lcEventManager.getLifecycleStateData(uuid);

    }

    public static List<InputBean> populateTransitionInputs(Document lcConfig, String lcState)
            throws LifeCycleExecutionException {
        List<InputBean> inputBeans = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_TRANSITION_INPUT_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if(nodeList.getLength() >0) {
                NodeList inputsNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < inputsNodeList.getLength(); i++) {
                    Node inputsNode = inputsNodeList.item(i);
                    if (inputsNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList inputNodeList = inputsNode.getChildNodes();
                        for (int j = 0; j < inputNodeList.getLength(); j++) {
                            if (inputNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element inputNode = (Element) inputNodeList.item(j);
                                InputBean inputBean = new InputBean(inputNode.getAttribute("name"), Boolean.parseBoolean(("required")), inputNode.getAttribute("label"),
                                        inputNode.getAttribute("placeHolder"), inputNode.getAttribute("tooltip"),
                                        inputNode.getAttribute("regex"), inputNode.getAttribute("values"), ((Element) inputsNode).getAttribute(LifecycleConstants.FOR_EVENT_ATTRIBUTE));
                                inputBeans.add(inputBean);

                            }
                        }
                    }
                }
            }
        } catch (XPathExpressionException e) {
            throw new LifeCycleExecutionException(
                    "Error while populating transistion inputs for lifecycle state : " + lcState, e);
        }
        return inputBeans;
    }

    public static List<CustomCodeBean> populateTransitionExecutors(Document lcConfig, String lcState)
            throws LifeCycleExecutionException {
        List<CustomCodeBean> customCodeBeansList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = buildXPathQuery(lcState, LifecycleConstants.LIFECYCLE_TRANSITION_EXECUTION_ATTRIBUTE);
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            if(nodeList.getLength()>0) {
                NodeList executionNodeList = nodeList.item(0).getChildNodes();
                for (int i = 0; i < executionNodeList.getLength(); i++) {
                    CustomCodeBean customCodeBean = new CustomCodeBean();
                    if (executionNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element executionNode = (Element) executionNodeList.item(i);

                        NodeList parameterNodeList = executionNode.getChildNodes();
                        Map<String, String> paramNameValues = new HashMap<>();
                        for (int j = 0; j < parameterNodeList.getLength(); j++) {

                            if (parameterNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                Element parameterNode = (Element) parameterNodeList.item(j);
                                paramNameValues.put(parameterNode.getAttribute("name"), parameterNode.getAttribute("value"));

                            }
                        }
                        customCodeBean.setClassObject(
                                loadCustomExecutors(executionNode.getAttribute("class"), paramNameValues));
                        customCodeBean.setEventName(executionNode.getAttribute(LifecycleConstants.FOR_EVENT_ATTRIBUTE));
                        customCodeBeansList.add(customCodeBean);
                    }
                }
            }

        } catch (XPathExpressionException e) {
            throw new LifeCycleExecutionException(
                    "Error while reading transition executors for lifecycle state: " + lcState, e);
        }
        return customCodeBeansList;
    }

    public static String getInitialState(Document lcConfig, String lcName) throws LifeCycleExecutionException {
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            XPathExpression exp = xPathInstance.compile(LifecycleConstants.LIFECYCLE_SCXML_ELEMENT_PATH);

            NodeList nodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            return ((Element) nodeList.item(0)).getAttribute(LifecycleConstants.LIFECYCLE_INITIAL_STATE_ATTRIBUTE);
        } catch (XPathExpressionException e) {
            throw new LifeCycleExecutionException("Error while getting first state for lifecycle state:" + " " + lcName,
                    e);
        }
    }

    public static List<AvailableTransitionBean> populateAvailableStates(Document lcConfig, String lcState) throws
            LifeCycleExecutionException{
        List<AvailableTransitionBean> availableTransitionBeanList = new ArrayList<>();
        try {
            XPath xPathInstance = XPathFactory.newInstance().newXPath();
            String xpathQuery = LifecycleConstants.LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH + lcState + "']"+ LifecycleConstants
                    .LIFECYCLE_TRANSITION_ELEMENT;
            XPathExpression exp = xPathInstance.compile(xpathQuery);

            NodeList transitionNodeList = (NodeList) exp.evaluate(lcConfig, XPathConstants.NODESET);
            for (int i = 0; i < transitionNodeList.getLength(); i++) {

                if (transitionNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element transitionNode = (Element) transitionNodeList.item(i);
                    AvailableTransitionBean availableTransitionBean = new AvailableTransitionBean(transitionNode
                            .getAttribute(LifecycleConstants.LIFECYCLE_EVENT_ATTRIBUTE), transitionNode.getAttribute
                            (LifecycleConstants.LIFECYCLE_TARGET_ATTRIBUTE));
                    availableTransitionBeanList.add(availableTransitionBean);

                }
            }

        } catch (XPathExpressionException e) {
            throw new LifeCycleExecutionException(
                    "Error while reading transition executors for lifecycle state: " + lcState, e);
        }
        return availableTransitionBeanList;
    }


    public static String buildXPathQuery(String lcState, String dataElementName) {
        return LifecycleConstants.LIFECYCLE_STATE_ELEMENT_WITH_NAME_PATH + lcState + "']"
                + LifecycleConstants.LIFECYCLE_DATA_ELEMENT_PATH + dataElementName + "']";
    }

    public static Execution loadCustomExecutors(String className, Map parameterMap) throws LifeCycleExecutionException {

        Execution customExecutors;
        try {
            Class<?> customCodeClass = LCOperationUtil.class.getClassLoader().loadClass(className);
            customExecutors = (Execution) customCodeClass.newInstance();
            customExecutors.init(parameterMap);

        } catch (Exception e) {
            throw new LifeCycleExecutionException("Unable to load executions class", e);
        }
        return customExecutors;
    }

    public static Document getLifecycleElement(String lcConfig) throws
            LifeCycleExecutionException {

        try {
            InputStream inputStream = new ByteArrayInputStream(lcConfig.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(false);
            Document document = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
            return document;
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new LifeCycleExecutionException("Error while building lifecycle config document element", e);
        }
    }
}
