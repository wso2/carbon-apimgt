/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.ballerina.threatprotection.analyzer;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.ballerina.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.JSONConfig;
import org.wso2.carbon.apimgt.ballerina.threatprotection.configurations.XMLConfig;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Implementation of APIMThreatAnalyzer for XML Payloads
 */
public class XMLAnalyzer implements APIMThreatAnalyzer {
    private static final String XML_THREAT_PROTECTION_MSG_PREFIX = "Threat Protection-XML: ";

    private XMLInputFactory factory;
    private XMLConfig config;
    private Logger log = LoggerFactory.getLogger(XMLAnalyzer.class);

    public XMLAnalyzer() {
        factory = WstxInputFactory.newInstance();
    }

    /**
     * Create a XMLAnalyzer using default configuration values
     */
    public void configure(XMLConfig config) {
        this.config = config;
        boolean dtdEnabled = config.isDtdEnabled();
        boolean externalEntitiesEnabled = config.isExternalEntitiesEnabled();
        int maxDepth = config.getMaxDepth();
        int maxElementCount = config.getMaxElementCount();
        int maxAttributeCount = config.getMaxAttributeCount();
        int maxAttributeLength = config.getMaxAttributeLength();
        int entityExpansionLimit = config.getEntityExpansionLimit();
        int maxChildrenPerElement = config.getMaxChildrenPerElement();

        factory.setProperty(XMLInputFactory.SUPPORT_DTD, dtdEnabled);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, externalEntitiesEnabled);
        factory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTE_SIZE, maxAttributeLength);
        factory.setProperty(WstxInputProperties.P_MAX_ATTRIBUTES_PER_ELEMENT, maxAttributeCount);
        factory.setProperty(WstxInputProperties.P_MAX_ELEMENT_DEPTH, maxDepth);
        factory.setProperty(WstxInputProperties.P_MAX_CHILDREN_PER_ELEMENT, maxChildrenPerElement);
        factory.setProperty(WstxInputProperties.P_MAX_ENTITY_COUNT, entityExpansionLimit);
        factory.setProperty(WstxInputProperties.P_MAX_ELEMENT_COUNT, maxElementCount);
    }

    @Override
    public void configure(JSONConfig config) {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    /**
     *
     * @param payload xml payload
     * @throws APIMThreatAnalyzerException
     */
    @Override
    public void analyze(String payload, String apiContext) throws APIMThreatAnalyzerException {
        Reader reader = null;
        XMLStreamReader xmlStreamReader = null;
        try {
            reader = new StringReader(payload);
            xmlStreamReader = factory.createXMLStreamReader(reader);
            while (xmlStreamReader.hasNext()) {
                int xmlStreamEvent = xmlStreamReader.next();

                //By default, stream parsing does not enforce attribute limits on the xml content.
                //see: https://stackoverflow.com/a/7447769
                //So, we are manually checking attribute length and count
                if (xmlStreamEvent == XMLStreamReader.START_ELEMENT) {
                    int currentAttributeCount = xmlStreamReader.getAttributeCount();
                    if (currentAttributeCount > config.getMaxAttributeCount()) {
                        throw new APIMThreatAnalyzerException(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext
                                + " - XML Validation Failed: Maximum attribute limit reached.");
                    }

                    for (int i = 0; i < currentAttributeCount; i++) {
                        String attributeValue = xmlStreamReader.getAttributeValue(i);
                        if (attributeValue.length() > config.getMaxAttributeLength()) {
                            throw new APIMThreatAnalyzerException(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext
                                    + " - XML Validation Failed: Maximum attribute length reached.");
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            log.error(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext + " - XML Validation Failed: "
                    + e.getMessage(), e);
            throw new APIMThreatAnalyzerException(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext
                    + " - XML Validation Failed: " + e.getMessage(), e);
        } finally {
            try {
                if (xmlStreamReader != null) {
                    xmlStreamReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                log.warn(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext
                        + " - Failed to close XMLEventReader", e);
            } catch (IOException e) {
                log.warn(XML_THREAT_PROTECTION_MSG_PREFIX + apiContext
                        + " - Failed to close payload StringReader", e);
            }
        }
    }
}
