/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.threatprotection.analyzer;

import com.ctc.wstx.stax.WstxInputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.JSONConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * Implementation of APIMThreatAnalyzer for XML Payloads
 */
public class XMLAnalyzer implements APIMThreatAnalyzer {

    private Logger log = LoggerFactory.getLogger(XMLAnalyzer.class);
    private static final String XML_THREAT_PROTECTION_MSG_PREFIX = "Threat Protection-XML: ";
    private XMLInputFactory factory;
    private boolean enabled = true;

    public XMLAnalyzer() {
        factory = WstxInputFactory.newInstance();
    }

    /**
     * Create a XMLAnalyzer using default configuration values
     */
    public void configure(XMLConfig config) {
        boolean dtdEnabled = config.isDtdEnabled();
        boolean externalEntitiesEnabled = config.isExternalEntitiesEnabled();
        Integer maxDepth = config.getMaxDepth();
        Integer maxElementCount = config.getMaxElementCount();
        Integer maxAttributeCount = config.getMaxAttributeCount();
        Integer maxAttributeLength = config.getMaxAttributeLength();
        Integer maxChildrenPerElement = config.getMaxChildrenPerElement();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, dtdEnabled);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, externalEntitiesEnabled);
        factory.setProperty(ThreatProtectorConstants.P_MAX_ATTRIBUTE_SIZE, maxAttributeLength);
        factory.setProperty(ThreatProtectorConstants.P_MAX_ATTRIBUTES_PER_ELEMENT, maxAttributeCount);
        factory.setProperty(ThreatProtectorConstants.P_MAX_ELEMENT_DEPTH, maxDepth);
        factory.setProperty(ThreatProtectorConstants.P_MAX_CHILDREN_PER_ELEMENT, maxChildrenPerElement);
        factory.setProperty(ThreatProtectorConstants.P_MAX_ELEMENT_COUNT, maxElementCount);
    }

    @Override
    public void configure(JSONConfig config) {
        throw new UnsupportedOperationException("This method is not supported on this instance");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void clearConfiguration() {
        factory = WstxInputFactory.newInstance();
    }

    /**
     * @param in xml payload
     * @throws APIMThreatAnalyzerException
     */
    @Override
    public void  analyze(InputStream in, String apiContext) throws APIMThreatAnalyzerException {
        Reader reader = null;
        XMLEventReader xmlEventReaderReader = null;
        try {
            reader = new InputStreamReader(in);
            xmlEventReaderReader = factory.createXMLEventReader(reader);
            while (xmlEventReaderReader.hasNext()) {
                xmlEventReaderReader.nextEvent();
            }
        } catch (XMLStreamException e) {
            throw new APIMThreatAnalyzerException("XML Validation Failed: due to "+ e.getMessage());
        }  finally {
            try {
                if (xmlEventReaderReader != null) {
                    xmlEventReaderReader.close();
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
