/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.factory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerFactory {


    private static Log log = LogFactory.getLog(KeyManagerFactory.class);
    private static KeyManager keyManager = null;


    /**
     * Initialises KeyManager by reading key-manager.xml.
     *
     * @param configPath Path to key-manager.xml
     * @throws APIManagementException
     */
    public static void initializeKeyManager(String configPath) throws APIManagementException {

        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(configPath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            OMElement document = builder.getDocumentElement();
            if (document == null || !APIConstants.KEY_MANAGER.equals(document.getQName().getLocalPart())) {
                throw new APIManagementException("KeyManager section not found. key-manager.xml may be corrupted.");
            }

            log.debug("Reading key-manager.xml");

            // Instantiating the class implementing KeyManager interface.
            String clazz = document.getAttribute(new QName("class")).getAttributeValue();
            keyManager = (KeyManager) Class.forName(clazz).newInstance();

            log.debug("Initialised KeyManager implementation");

            OMElement configElement = document.getFirstElement();
            if (!"Configuration".equals(configElement.getQName().getLocalPart())) {
                throw new APIManagementException("Configuration section not found. key-manager.xml may be corrupted.");
            }

            log.debug("Loading KeyManager configuration,");

            // Reading contents inside <Configuration> block and pass it to specific KeyManager implementation.
            // Implementers can provide specific parameters needed for their implementation withing the Configuration
            // block.
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter;
            StringWriter stringStream = new StringWriter();
            streamWriter = xof.createXMLStreamWriter(stringStream);
            configElement.serialize(streamWriter);
            streamWriter.close();
            keyManager.loadConfiguration(stringStream.toString());

            log.debug("Successfully loaded KeyManager configuration.");

        } catch (IOException e) {
            log.error(e);
            throw new APIManagementException("I/O error while reading the API manager " +
                                             "configuration: " + configPath, e);
        } catch (XMLStreamException e) {
            log.error(e);
            throw new APIManagementException("Error while parsing the API manager " +
                                             "configuration: " + configPath, e);
        } catch (OMException e) {
            log.error(e);
            throw new APIManagementException("Error while parsing API Manager configuration: " + configPath, e);
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
        } catch (InstantiationException e) {
            log.error(e);
            throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
        } catch (IllegalAccessException e) {
            log.error(e);
            throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
        } catch (APIManagementException e) {
            log.error(e);
            throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * This method will take hardcoded class name from api-manager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     *
     * @return keyManager instance.
     */
    public static KeyManager getKeyManager() {
        return keyManager;
    }

}
