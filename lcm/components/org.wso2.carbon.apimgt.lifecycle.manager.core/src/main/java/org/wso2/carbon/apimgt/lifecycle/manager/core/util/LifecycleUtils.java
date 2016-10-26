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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.lifecycle.manager.constants.LifecycleConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleConfigBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.dao.LifecycleMgtDAO;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LifecycleManagerDatabaseException;
import org.wso2.carbon.kernel.utils.Utils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final Logger log = LoggerFactory.getLogger(LifecycleUtils.class);
    private static Map<String, String> lifecycleMap;
    private static Validator lifecycleSchemaValidator;

    /**
     * Method used to add new lifecycle configuration.
     *
     * @param lcConfig                  Lifecycle configuration
     * @throws LifecycleException
     */
    public static void addLifecycle(String lcConfig) throws LifecycleException {
        Element element = (Element) getLifecycleElement(lcConfig).getElementsByTagName(LifecycleConstants.ASPECT)
                .item(0);
        String lcName = element.getAttribute("name");
        if ("".equals(lcName)) {
            throw new LifecycleException("Lifecycle name can not be empty in the configuration");
        }

        validateLifecycleContent(lcConfig);
        try {
            if (!checkLifecycleExist(lcName)) {
                LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
                lifecycleConfigBean.setLcName(lcName);
                lifecycleConfigBean.setLcContent(lcConfig);
                getLCMgtDAOInstance().addLifecycle(lifecycleConfigBean);
                getLifecycleMapInstance().put(lcName, lcConfig);

            } else {
                throw new LifecycleException("Lifecycle already exist with name " + lcName);
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error in adding lifecycle with name " + lcName, e);
        }
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
        if ("".equals(newName)) {
            throw new LifecycleException("Lifecycle name can not be empty in the configuration");
        }
        // adding new lifecycle. Not update operation
        if (!newName.equals(oldName)) {
            addLifecycle(newContent);
        } else {
            validateLifecycleContent(newContent);
            try {
                LifecycleConfigBean lifecycleConfigBean = new LifecycleConfigBean();
                lifecycleConfigBean.setLcName(newName);
                lifecycleConfigBean.setLcContent(newContent);
                getLCMgtDAOInstance().updateLifecycle(lifecycleConfigBean);
                getLifecycleMapInstance().put(newName, newContent);

            } catch (LifecycleManagerDatabaseException e) {
                throw new LifecycleException("Error in adding lifecycle with name " + newName, e);
            }
        }
    }

    /**
     * Method used to delete exiting lifecycle.
     *
     * @param lcName                  Lifecycle to be deleted.
     * @throws LifecycleException
     */
    public static void deleteLifecycle(String lcName) throws LifecycleException {
        try {
            if (!checkLifecycleInUse(lcName)) {
                getLCMgtDAOInstance().deleteLifecycle(lcName);
                if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
                    lifecycleMap.remove(lcName);
                }
            } else {
                throw new LifecycleException(
                        lcName + " is associated with assets. Delete operation can not be " + "allowed");
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error in deleting lifecycle with name " + lcName, e);
        }
    }

    /**
     * Get the list of life cycles for a particular tenant.
     *
     * @return List of available life cycles.
     * @throws LifecycleException
     */
    public static String[] getLifecycleList() throws LifecycleException {
        try {
            if (lifecycleMap != null) {
                return lifecycleMap.keySet().toArray(new String[0]);
            }
            return getLCMgtDAOInstance().getLifecycleList();
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list. ", e);
        }
    }

    /**
     * Get the lifecycle configuration with a particular name.
     *
     * @param lcName                Name of the lifecycle.
     * @return                      Lifecycle configuration.
     * @throws LifecycleException
     */
    public static String getLifecycleConfiguration(String lcName) throws LifecycleException {
        try {
            if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
                return lifecycleMap.get(lcName);
            }
            return getLCMgtDAOInstance().getLifecycleConfig(lcName).getLcContent();
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list.", e);
        }
    }

    /**
     * Initiates the static tenant specific lifecycle map during startup.
     *
     * @throws LifecycleException
     */
    public static void initiateLCMap() throws LifecycleException {
        lifecycleMap = new ConcurrentHashMap<>();
        try {
            LifecycleConfigBean[] lifecycleConfigBeen = getLCMgtDAOInstance().getAllLifecycleConfigs();
            for (LifecycleConfigBean lifecycleConfigBean : lifecycleConfigBeen) {
                lifecycleMap.put(lifecycleConfigBean.getLcName(), lifecycleConfigBean.getLcContent());
            }
        } catch (LifecycleManagerDatabaseException e) {
            throw new LifecycleException("Error while getting Lifecycle list for all tenants", e);
        }
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
            } else {
                log.error(
                        "Lifecycle schema validator not found. Check the existence  of resources/lifecycle-config.xsd");
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
    public static synchronized Validator getLifecycleSchemaValidator(String schemaPath) {
        if (lifecycleSchemaValidator != null) {
            return lifecycleSchemaValidator;
        }
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(schemaPath));
            lifecycleSchemaValidator = schema.newValidator();
        } catch (SAXException e) {
            log.error("Unable to get a schema validator from the given file path : " + schemaPath);
        }
        return lifecycleSchemaValidator;
    }

    private static synchronized  Map<String, String> getLifecycleMapInstance() {
        if (lifecycleMap == null) {
            lifecycleMap = new ConcurrentHashMap<>();
        }

        return lifecycleMap;
    }

    /**
     * This method will return the lifecycle schema location in the server directory.
     * @return schema location.
     * @throws LifecycleException
     */
    private static String getLifecycleSchemaLocation() {
        return Utils.getCarbonHome() + File.separator + "resources" + File.separator + "lifecycle-config.xsd";
    }

    private static boolean checkLifecycleExist(String lcName) throws LifecycleManagerDatabaseException {
        if (lifecycleMap != null && lifecycleMap.containsKey(lcName)) {
            return true;
        }
        return getLCMgtDAOInstance().checkLifecycleExist(lcName);
    }

    private static boolean checkLifecycleInUse(String lcName) throws LifecycleManagerDatabaseException {
        return getLCMgtDAOInstance().isLifecycleIsInUse(lcName);
    }

    private static LifecycleMgtDAO getLCMgtDAOInstance() {
        return LifecycleMgtDAO.getInstance();
    }
}
