/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.soaptorest;

import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.impl.soaptorest.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.soaptorest.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WSDL20SOAPOperationExtractor implements WSDLSOAPOperationExtractor {

    //Fields required for processing a single wsdl
    protected Description wsdlDescription;

    //Fields required for processing a file path with multiple WSDLs (WSDL archive)
    protected Map<String, Description> pathToDescriptionMap;
    protected String wsdlArchiveExtractedPath;

    //Common fields
    private boolean canProcess;
    private static volatile WSDLFactory wsdlFactoryInstance;
    private static volatile WSDLReader reader;

    private static final String WSDL_VERSION_20 = "2.0";

    private static final Logger log = LoggerFactory.getLogger(WSDL20SOAPOperationExtractor.class);

    private static WSDLFactory getWsdlFactoryInstance() throws WSDLException {
        if (wsdlFactoryInstance == null) {
            synchronized (WSDL20SOAPOperationExtractor.class) {
                if (wsdlFactoryInstance == null) {
                    wsdlFactoryInstance = WSDLFactory.newInstance();
                }
            }
        }
        return wsdlFactoryInstance;
    }

    public WSDL20SOAPOperationExtractor() {

    }

    public WSDL20SOAPOperationExtractor(WSDLReader wsdlReader) {
        WSDL20SOAPOperationExtractor.reader = wsdlReader;
    }

    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
            Document dom = builder.parse(new ByteArrayInputStream(wsdlContent));
            Element domElement = dom.getDocumentElement();
            WSDLSource wsdlSource = reader.createWSDLSource();
            wsdlSource.setSource(domElement);
            wsdlDescription = reader.readWSDL(wsdlSource);
            canProcess = true;
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                        + " with a single WSDL.");
            }
        } catch (WSDLException | ParserConfigurationException | SAXException | IOException e) {
            //This implementation class cannot process the WSDL.
            log.debug(this.getClass().getName() + " was unable to process the WSDL.", e);
            canProcess = false;
        }
        return canProcess;
    }

    @Override
    public boolean initPath(String path) throws APIMgtWSDLException {
        pathToDescriptionMap = new HashMap<>();
        wsdlArchiveExtractedPath = path;
        try {
            WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
            reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
            File folderToImport = new File(path);
            Collection<File> foundWSDLFiles = APIFileUtil.searchFilesWithMatchingExtension(folderToImport, "wsdl");
            if (log.isDebugEnabled()) {
                log.debug("Found " + foundWSDLFiles.size() + " WSDL file(s) in path " + path);
            }
            for (File file : foundWSDLFiles) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing WSDL file: " + file.getAbsolutePath());
                }
                Description description = reader.readWSDL(file.getAbsolutePath());
                pathToDescriptionMap.put(file.getAbsolutePath(), description);
            }
            if (foundWSDLFiles.size() > 0) {
                canProcess = true;
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully processed all WSDL files in path " + path);
            }
        } catch (WSDLException e) {
            //This implementation class cannot process the WSDL.
            throw new APIMgtWSDLException(
                    this.getClass().getName() + " was unable to process the WSDL Files for the path: " + path, e);
        }
        return canProcess;
    }

    @Override
    public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
        Map<String, String> endpointsMap = getEndpoints();
        WSDLInfo wsdlInfo = new WSDLInfo();
        wsdlInfo.setEndpoints(endpointsMap);
        wsdlInfo.setVersion(WSDL_VERSION_20);
        return wsdlInfo;
    }

    @Override
    public boolean canProcess() {
        return canProcess;
    }

    /**
     * Get endpoints defined in WSDL file(s).
     *
     * @return a Map of endpoint names and their URIs.
     * @throws APIMgtWSDLException if error occurs while reading endpoints
     */
    private Map<String, String> getEndpoints() throws APIMgtWSDLException {
        if (wsdlDescription != null) {
            return getEndpoints(wsdlDescription);
        } else {
            Map<String, String> allEndpointsOfAllWSDLs = new HashMap<>();
            for (Description description : pathToDescriptionMap.values()) {
                Map<String, String> wsdlEndpoints = getEndpoints(description);
                allEndpointsOfAllWSDLs.putAll(wsdlEndpoints);
            }
            return allEndpointsOfAllWSDLs;
        }
    }

    /**
     * Get endpoints defined in the provided WSDL description.
     *
     * @param description WSDL 2.0 description
     * @return a Map of endpoint names and their URIs.
     * @throws APIMgtWSDLException if error occurs while reading endpoints
     */
    private Map<String, String> getEndpoints(Description description) throws APIMgtWSDLException {
        Service[] services = description.getServices();
        Map<String, String> serviceEndpointMap = new HashMap<>();
        for (Service service : services) {
            Endpoint[] endpoints = service.getEndpoints();
            for (Endpoint endpoint : endpoints) {
                serviceEndpointMap.put(endpoint.getName().toString(), endpoint.getAddress().toString());
            }
        }
        return serviceEndpointMap;
    }
}
