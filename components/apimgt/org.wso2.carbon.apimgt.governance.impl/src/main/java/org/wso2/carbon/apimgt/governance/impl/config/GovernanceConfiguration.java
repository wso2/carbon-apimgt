/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents the Governance Configuration. It is responsible for reading the
 * Governance configuration from an XML file and providing access to the configuration
 * properties.
 */
public class GovernanceConfiguration {
    private static final Log log = LogFactory.getLog(GovernanceConfiguration.class);

    private final Map<String, List<String>> configuration = new ConcurrentHashMap<>();
    private SecretResolver secretResolver;
    private boolean initialized;


    /**
     * Populate this configuration by reading an XML file at the given location. This method
     * can be executed only once on a given GovernanceConfiguration instance. Once invoked and
     * successfully populated, it will ignore all subsequent invocations.
     *
     * @param filePath Path of the XML descriptor file
     * @throws GovernanceException If an error occurs while reading the XML descriptor
     */
    public void load(String filePath) throws GovernanceException {
        if (initialized) {
            return;
        }
        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
        } catch (IOException e) {
            log.error(e);
            throw new GovernanceException("I/O error while reading the Governance " +
                    "configuration: " + filePath, e);
        } catch (XMLStreamException e) {
            log.error(e);
            throw new GovernanceException("Error while parsing the Governance " +
                    "configuration: " + filePath, e);
        } catch (OMException e) {
            log.error(e);
            throw new GovernanceException("Error while parsing Governance configuration: " + filePath, e);
        } catch (Exception e) {
            log.error(e);
            throw new GovernanceException("Unexpected error occurred while parsing configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Read the child elements of the given OMElement and populate the configuration.
     *
     * @param serverConfig OMElement
     * @param nameStack    Stack<String>
     * @throws GovernanceException If an error occurs while reading the child elements
     */
    private void readChildElements(OMElement serverConfig,
                                   Stack<String> nameStack) throws GovernanceException {

        for (Iterator childElements = serverConfig.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            String localName = element.getLocalName();
            nameStack.push(localName);
            // Implement as required similar to load method in APIManagerConfiguration
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                String value = MiscellaneousUtil.resolve(element, secretResolver);
                addToConfiguration(key, GovernanceUtil.replaceSystemProperty(value));
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append('.');
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private void addToConfiguration(String key, String value) {

        List<String> list = configuration.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configuration.put(key, list);
        } else {
            list.add(value);
        }
    }

    public String getFirstProperty(String key) {

        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

}
