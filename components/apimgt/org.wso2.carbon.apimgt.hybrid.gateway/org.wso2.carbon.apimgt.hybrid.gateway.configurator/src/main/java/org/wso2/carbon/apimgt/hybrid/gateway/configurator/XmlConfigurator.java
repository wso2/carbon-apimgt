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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
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
     * @param gatewayProperties Properties
     * @param fileMap Map<String, Map<String, String>>
     */
    public void configure(String carbonHome, Properties gatewayProperties, Map<String, Map<String, String>> fileMap) {

        for (Map.Entry<String, Map<String, String>> entry : fileMap.entrySet()) {
            String filePath = carbonHome + File.separator + entry.getKey().replace("/", File.separator);
            try {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().parse(new InputSource(filePath));
                replaceValues(doc, entry.getValue(), gatewayProperties);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(new File(filePath)));
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
     * @param configProperties Properties
     * @throws XPathExpressionException
     */
    private void replaceValues(Document doc, Map<String, String> xpathMap, Properties configProperties)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Map.Entry entry : xpathMap.entrySet()) {
            String xpathKey = (String) entry.getKey();
            String replaceKey = (String) entry.getValue();
            if (configProperties.containsKey(replaceKey) && configProperties.getProperty(replaceKey) != null
                    && !configProperties.getProperty(replaceKey).isEmpty()) {
                Node node = (Node) xpath.evaluate("//" + xpathKey.replace(".", "/"),
                        doc, XPathConstants.NODE);
                node.setTextContent(configProperties.getProperty(replaceKey));
            }
        }
    }
}
