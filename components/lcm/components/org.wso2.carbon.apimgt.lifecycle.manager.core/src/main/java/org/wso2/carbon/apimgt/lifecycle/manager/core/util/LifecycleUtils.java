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
package org.wso2.carbon.apimgt.lifecycle.manager.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.lifecycle.manager.constants.LifecycleConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.core.LifecycleCrudManager;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * This utility class provides methods to perform CRUD operations for lifecycle configurations.
 */
public class LifecycleUtils {

    private static final Log log = LogFactory.getLog(LifecycleUtils.class);
    private static Validator lifecycleSchemaValidator = null;

    /**
     * Method used to add new lifecycle configuration.
     *
     * @param lcConfig                  Lifecycle configuration
     * @throws LifecycleException
     */
    public static void addLifecycle(String lcConfig) throws LifecycleException {
        Element element = (Element) getLifecycleElement(lcConfig).getElementsByTagName(LifecycleConstants.ASPECT)
                .item(0);
        String name = element.getAttribute("name");
        if (name == null || name.equals("")) {
            throw new LifecycleException("Lifecycle name can not be empty in the configuration");
        }

        validateLifecycleContent(lcConfig);

        LifecycleCrudManager lifecycleCrudManager = new LifecycleCrudManager();
        lifecycleCrudManager.addLifecycle(name, lcConfig);
    }

    /**
     * Method used to update existing lifecycle configuration.
     *
     * @param oldName                       Name of the existing lifecycle.
     * @param newContent                    Lifecycle configuration
     * @throws LifecycleException
     */
    public static void updateLifecycle(String oldName, String newContent) throws LifecycleException {
        Element element = (Element) getLifecycleElement(newContent).getElementsByTagName(LifecycleConstants.ASPECT)
                .item(0);
        String newName = element.getAttribute("name");
        if (newName == null || newName.equals("")) {
            throw new LifecycleException("Lifecycle name can not be empty in the configuration");
        }
        // adding new lifecycle. Not update operation
        if (!newName.equals(oldName)) {
            addLifecycle(newContent);
        } else {
            validateLifecycleContent(newContent);
            LifecycleCrudManager lifecycleCrudManager = new LifecycleCrudManager();
            lifecycleCrudManager.updateLifecycle(oldName, newContent);
        }
    }

    /**
     * Method used to delete exiting lifecycle.
     *
     * @param lcName                  Lifecycle to be deleted.
     * @throws LifecycleException
     */
    public static void deleteLifecycle(String lcName) throws LifecycleException {
        LifecycleCrudManager lifecycleCrudManager = new LifecycleCrudManager();
        lifecycleCrudManager.deleteLifecycle(lcName);
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifecycleException
     */
    public static String[] getLifecycleList() throws LifecycleException {
        LifecycleCrudManager lifecycleCrudManager = new LifecycleCrudManager();
        return lifecycleCrudManager.getLifecycleList();
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Lifecycle configuration.
     * @throws LifecycleException
     */
    public static String getLifecycleConfiguration(String lcName) throws LifecycleException {
        LifecycleCrudManager lifecycleCrudManager = new LifecycleCrudManager();
        return lifecycleCrudManager.getLifecycleConfiguration(lcName).getLcContent();
    }

    /**
     * Initiates the static tenant specific lifecycle map during startup.
     *
     * @throws LifecycleException
     */
    public static void initiateLCMap() throws LifecycleException {
        new LifecycleCrudManager().initLifecycleMap();
    }

    /**
     * This method is used to read lifecycle config and provide permission details associated with each state change.
     *
     * @param lcConfig                          Lifecycle configuration element.
     * @return                                  Document element for the lifecycle confi
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

    /**
     * Method used to validate lifecycle config adheres to the schema.
     * @param lcConfig              Lifecycle configuration element.
     * @throws LifecycleException   If validation fails.
     */
    public static void validateLifecycleContent(String lcConfig) throws LifecycleException {
        if (!validateLifecycleContent(lcConfig, getLifecycleSchemaValidator(getLifecycleSchemaLocation()))) {
            String message = "Unable to validate the lifecycle configuration";
            log.error(message);
            throw new LifecycleException(message);
        }
    }

    private static boolean validateLifecycleContent(String lcConfig, Validator validator) {
        try {
            InputStream is = new ByteArrayInputStream(lcConfig.getBytes("utf-8"));
            Source xmlFile = new StreamSource(is);
            if (validator != null) {
                validator.validate(xmlFile);
            }
        } catch (SAXException e) {
            log.error("Unable to parse the XML configuration. Please validate the XML configuration", e);
            return false;
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported content", e);
            return false;
        } catch (IOException e) {
            log.error("Unable to parse the XML configuration. Please validate the XML configuration", e);
            return false;
        }
        return true;
    }

    /**
     * Method used to get schema validaor object for lifecycle configurations.
     * @param schemaPath               Schema path in the server extracted directory.
     * @throws LifecycleException
     */
    public static Validator getLifecycleSchemaValidator(String schemaPath) {

        if (lifecycleSchemaValidator == null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new File(schemaPath));
                lifecycleSchemaValidator = schema.newValidator();
            } catch (SAXException e) {
                log.error("Unable to get a schema validator from the given file path : " + schemaPath);
            }
        }
        return lifecycleSchemaValidator;
    }

    /**
     * This method will return the lifecycle schema location in the server directory.
     * @return schema location.
     * @throws LifecycleException
     */
    private static String getLifecycleSchemaLocation() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources"
                + File.separator + "lifecycle-config.xsd";
    }
}
