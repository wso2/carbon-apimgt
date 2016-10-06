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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.core.LCCrudManager;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class LCUtils {

    private static final Log log = LogFactory.getLog(LCUtils.class);
    private static Validator lifecycleSchemaValidator = null;

    public static boolean addLifecycle(String lcConfig) throws LifeCycleException{
        String name;
        OMElement element = null;
        element = buildOMElement(lcConfig);

        //        We have added an validation here too since someone can directly call this method
        validateOMContent(element);
        name = element.getAttributeValue(new QName("name"));
        LCCrudManager lcCrudManager = new LCCrudManager();
        lcCrudManager.addLifecycle(name, lcConfig);

        /*if (isLifecycleNameInUse(name, registry, rootRegistry)){
            String msg = String.format("The lifecycle name %s is already in use", name);
            throw new RegistryException(msg);
        }*/


        return true;
    }

    public static String[] getLifeCycleList() throws LifeCycleException{
        LCCrudManager lcCrudManager = new LCCrudManager();
        return lcCrudManager.getLifecycleList();
    }

    public static String getLifecycleConfiguration(String lcName) throws LifeCycleException{
        LCCrudManager lcCrudManager = new LCCrudManager();
        return lcCrudManager.getLifecycleConfiguration(lcName).getLCContent();
    }

    public static OMElement buildOMElement(String payload) throws LifeCycleException {
        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(payload);
            element.build();
        } catch (Exception e) {
            String message = "Unable to parse the XML configuration. Please validate the XML configuration";
            log.error(message,e);
            throw new LifeCycleException(message,e);
        }
        return element;
    }

    public static void validateOMContent(OMElement element) throws LifeCycleException {
        if(!validateOMContent(element, getLifecycleSchemaValidator(getLifecycleSchemaLocation()))){
            String message = "Unable to validate the lifecycle configuration";
            log.error(message);
            throw new LifeCycleException(message);
        }
    }

    public static boolean validateOMContent(OMElement omContent, Validator validator) {
        try {
            InputStream is = new ByteArrayInputStream(omContent.toString().getBytes("utf-8"));
            Source xmlFile = new StreamSource(is);
            if (validator != null) {
                validator.validate(xmlFile);
            }
        } catch (SAXException e) {
            log.error("Unable to parse the XML configuration. Please validate the XML configuration",e);
            return false;
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported content",e);
            return false;
        } catch (IOException e) {
            log.error("Unable to parse the XML configuration. Please validate the XML configuration",e);
            return false;
        }
        return true;
    }

    public static Validator getLifecycleSchemaValidator(String schemaPath){

        if (lifecycleSchemaValidator == null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new File(schemaPath));
                lifecycleSchemaValidator = schema.newValidator();
            } catch (SAXException e) {
                log.error("Unable to get a schema validator from the given file path : " + schemaPath);
            }
        }
        return lifecycleSchemaValidator;
    }

    public static String getLifecycleSchemaLocation(){
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
                File.separator+ "lifecycle-config.xsd";
    }
}
