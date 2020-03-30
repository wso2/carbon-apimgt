/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.ConfigDTO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This class is used to replace configurations in registry.xml
 */
public class RegistryXmlConfigurator {

    private static final Log log = LogFactory.getLog(RegistryXmlConfigurator.class);

    private static final String REGISTRY_XML = "registry.xml";

    //Element names, attributes used for creating the Tasks elements
    private static final String TASKS = "tasks";
    private static final String TASK = "task";
    private static final String NAME = "name";
    private static final String CLASS = "class";
    private static final String TRIGGER = "trigger";
    private static final String CRON = "cron";
    private static final String PROPERTY = "property";
    private static final String FILE_RETENTION_DAYS = "fileRetentionDays";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    private Document doc;
    ConfigDTO gatewayConfigs;

    /**
     * Configure registry.xml with provided properties
     *
     * @param configDirPath String
     * @param gatewayConfigs Properties
     */
    public void configure(String configDirPath, ConfigDTO gatewayConfigs) {
        String regXmlFilePath = configDirPath + File.separator + REGISTRY_XML;
        this.gatewayConfigs = gatewayConfigs;
        try {
            doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder().parse(new InputSource(regXmlFilePath));
            configureTasks();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(new File(regXmlFilePath)));
        } catch (SAXException | ParserConfigurationException | IOException
                         | TransformerException | XPathExpressionException e) {
            log.error("Error occurred while replacing the configs in registry.xml", e);
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Configure Cron tasks for file upload, file cleanup, throttling and API update
     *
     * @throws XPathExpressionException
     */
    private void configureTasks() throws XPathExpressionException {
        //Check whether tasks element already existing and if so delete it
        Element tasks;
        if (doc.getElementsByTagName(TASKS).getLength() != 0) {
            Node taskNode = doc.getElementsByTagName(TASKS).item(0);
            taskNode.getParentNode().removeChild(taskNode);
        }
        tasks = doc.createElement(TASKS);
        //File Upload Task
        if (gatewayConfigs.isUsage_upload_task_enabled()) {
            Element fileUploadTask = doc.createElement(TASK);
            String className = gatewayConfigs.getUsage_upload_file_data_upload_task_class();
            className = (className != null && !className.isEmpty()) ? className
                                : ConfigConstants.DEFAULT_FILE_DATA_UPLOAD_TASK_CLASS;
            fileUploadTask.setAttribute(NAME,
                                        className.substring(className.lastIndexOf(".") + 1, className.length()));
            fileUploadTask.setAttribute(CLASS, className);
            Element fileUploadTrigger = doc.createElement(TRIGGER);
            fileUploadTrigger.setAttribute(CRON, gatewayConfigs.getUsage_upload_task_cron());
            fileUploadTask.appendChild(fileUploadTrigger);
            tasks.appendChild(fileUploadTask);
        }
        //File Cleanup Task
        if (gatewayConfigs.isUsage_upload_cleanup_task_enabled()) {
            Element fileCleanupTask = doc.createElement(TASK);
            String className = gatewayConfigs.getUsage_upload_cleanup_task_class();
            className = (className != null && !className.isEmpty()) ? className
                                : ConfigConstants.DEFAULT_FILE_DATA_CLEANUP_TASK_CLASS;
            fileCleanupTask.setAttribute(NAME,
                                         className.substring(className.lastIndexOf(".") + 1, className.length()));
            fileCleanupTask.setAttribute(CLASS, className);
            Element fileCleanupTrigger = doc.createElement(TRIGGER);
            fileCleanupTrigger.setAttribute(CRON,
                    String.valueOf(gatewayConfigs.getUsage_upload_cleanup_task_cron()));
            Element fileCleanupProperty = doc.createElement(PROPERTY);
            fileCleanupProperty.setAttribute(KEY, FILE_RETENTION_DAYS);
            fileCleanupProperty.setAttribute(VALUE,
                    String.valueOf(gatewayConfigs.getUsage_upload_retention_days()));
            fileCleanupTask.appendChild(fileCleanupTrigger);
            fileCleanupTask.appendChild(fileCleanupProperty);
            tasks.appendChild(fileCleanupTask);
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node registryRootNode = (Node) xpath.evaluate("//wso2registry",
                                                          doc, XPathConstants.NODE);
            registryRootNode.appendChild(tasks);
        }
        //Throttling Synchronization Task
        if (gatewayConfigs.isThrottling_synchronization_task_enabled()) {
            Element fileUploadTask = doc.createElement(TASK);
            String className = gatewayConfigs.getThrottling_synchronization_task_class();
            className = (className != null && !className.isEmpty()) ? className
                                : ConfigConstants.DEFAULT_THROTTLING_SYNC_TASK_CLASS;
            fileUploadTask.setAttribute(NAME,
                                        className.substring(className.lastIndexOf(".") + 1, className.length()));
            fileUploadTask.setAttribute(CLASS, className);
            Element fileUploadTrigger = doc.createElement(TRIGGER);
            fileUploadTrigger.setAttribute(CRON, gatewayConfigs.getThrottling_synchronization_task_cron());
            fileUploadTask.appendChild(fileUploadTrigger);
            tasks.appendChild(fileUploadTask);
        }
        //API Update Task
        if (gatewayConfigs.isApi_update_task_enabled()) {
            Element apiUpdateTask = doc.createElement(TASK);
            String className = gatewayConfigs.getApi_update_task_class();
            className = (className != null && !className.isEmpty()) ? className
                                : ConfigConstants.DEFAULT_API_UPDATE_TASK_CLASS;
            apiUpdateTask.setAttribute(NAME,
                                       className.substring(className.lastIndexOf(".") + 1, className.length()));
            apiUpdateTask.setAttribute(CLASS, className);
            Element fileUploadTrigger = doc.createElement(TRIGGER);
            fileUploadTrigger.setAttribute(CRON, gatewayConfigs.getApi_update_task_cron());
            apiUpdateTask.appendChild(fileUploadTrigger);
            tasks.appendChild(apiUpdateTask);
        }
    }

}
