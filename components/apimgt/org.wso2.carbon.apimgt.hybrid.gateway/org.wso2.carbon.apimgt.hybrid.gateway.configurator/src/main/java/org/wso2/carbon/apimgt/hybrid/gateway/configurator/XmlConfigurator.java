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
import org.w3c.dom.Node;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.ConfigDTO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
 * This is used to replace configurations in api-manager.xml
 */
public class XmlConfigurator {

    private static final Log log = LogFactory.getLog(XmlConfigurator.class);

    /**
     * Configure Gateway properties in the api-manager.xml
     *
     * @param carbonHome String
     * @param gatewayConfigs Configuration values
     * @param fileMap Map<String, Map<String, String>>
     */
    public void configure(String carbonHome, ConfigDTO gatewayConfigs, Map<String, Map<String, String>> fileMap) {

        for (Map.Entry<String, Map<String, String>> entry : fileMap.entrySet()) {
            String filePath = carbonHome + File.separator + entry.getKey().replace("/", File.separator);
            try {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(new InputSource(filePath));
                replaceValues(doc, entry.getValue(), gatewayConfigs);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StreamResult streamResult = new StreamResult(filePath);
                transformer.transform(new DOMSource(doc), streamResult);
            } catch (SAXException | ParserConfigurationException | IOException
                    | TransformerException | XPathExpressionException e) {
                log.error("Error occurred while replacing the configs in : " + filePath, e);
                Runtime.getRuntime().exit(1);
            }
        }
    }

    /**
     * Replace values in given Document
     *
     * @param doc Document
     * @param xpathMap Map<String, String>
     * @param gatewayConfigs Configuration values
     * @throws XPathExpressionException
     */
    private void replaceValues(Document doc, Map<String, String> xpathMap, ConfigDTO gatewayConfigs)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Map.Entry entry : xpathMap.entrySet()) {
            Method methodToFind = null;
            String xpathKey = (String) entry.getKey();
            String replaceKey = (String) entry.getValue();
            String postFix = replaceKey.substring(0,1).toUpperCase().concat(replaceKey.substring(1).toLowerCase());
            String replaceKeyGetMethod = "get".concat(postFix);
            try {
                methodToFind = ConfigDTO.class.getMethod(replaceKeyGetMethod);
            } catch (NoSuchMethodException e) {
                String replaceKeyIsMethod = "is".concat(postFix);
                try {
                    methodToFind = ConfigDTO.class.getMethod(replaceKeyIsMethod);
                } catch (NoSuchMethodException e2) {
                    if (log.isDebugEnabled()) {
                        log.debug("No such method exception for the method " + replaceKeyGetMethod + " or " +
                                replaceKeyIsMethod, e);
                    }
                }
            }
            try {
                if (methodToFind != null) {
                    Node node = (Node) xpath.evaluate("//" + xpathKey.replace(".", "/"),
                            doc, XPathConstants.NODE);
                    node.setTextContent(methodToFind.invoke(gatewayConfigs).toString());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred while invoking the method " + methodToFind, e);
                }
            }
        }
    }
}
