/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.WSDLWriter;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.xml.EndpointElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMWSDLUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class handles WSDL 2.0 related operations. This is implemented using apache woden.
 */
public class WSDL20ProcessorImpl implements WSDLProcessor {

    //Fields required for processing a single wsdl
    protected Description wsdlDescription;

    //Fields required for processing a file path with multiple WSDLs (WSDL archive)
    protected Map<String, Description> pathToDescriptionMap;
    protected String wsdlArchiveExtractedPath;

    //Common fields
    private boolean canProcess;
    private static volatile WSDLFactory wsdlFactoryInstance;
    private static final Logger log = LoggerFactory.getLogger(WSDL20ProcessorImpl.class);

    private static WSDLFactory getWsdlFactoryInstance() throws WSDLException {
        if (wsdlFactoryInstance == null) {
            synchronized (WSDL20ProcessorImpl.class) {
                if (wsdlFactoryInstance == null) {
                    wsdlFactoryInstance = WSDLFactory.newInstance();
                }
            }
        }
        return wsdlFactoryInstance;
    }

    /**
     * {@inheritDoc}
     * Will return true if the provided WSDL is of 2.0 and can be successfully parsed by woden library.
     */
    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
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
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            canProcess = false;
        }
        return canProcess;
    }

    /**
     * {@inheritDoc}
     * @return WSDL 2.0 content bytes
     */
    @Override
    public byte[] getWSDL() throws APIMgtWSDLException {
        WSDLWriter writer;
        try {
            writer = getWsdlFactoryInstance().newWSDLWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            writer.writeWSDL(wsdlDescription.toElement(), byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while stringifying WSDL definition", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
    }

    /**
     * Updates the WSDL's endpoints based on the provided API (context) and Label (host).
     *
     * @param api Provided API object
     * @param label Provided label object
     * @return Updated WSDL 2.0 content bytes
     * @throws APIMgtWSDLException Error while updating the WSDL
     */
    @Override
    public byte[] getUpdatedWSDL(API api, Label label) throws APIMgtWSDLException {
        if (label != null) {
            updateEndpoints(label.getAccessUrls(), api, wsdlDescription);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated the endpoints of WSDL with API:" + api.getId() + ", label:" + label
                        .getName());
            }
            return getWSDL();
        }
        return new byte[0];
    }

    /**
     * {@inheritDoc}
     * Will return true if all the provided WSDL files in the initialized path is of 2.0 and can be successfully 
     * parsed by woden.
     */
    @Override
    public boolean initPath(String path) throws APIMgtWSDLException {
        pathToDescriptionMap = new HashMap<>();
        wsdlArchiveExtractedPath = path;
        try {
            WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
            reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
            File folderToImport = new File(path);
            Collection<File> foundWSDLFiles = APIFileUtils.searchFilesWithMatchingExtension(folderToImport, "wsdl");
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
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            canProcess = false;
        }
        return canProcess;
    }

    /**
     * Updates the endpoints of all the WSDL files in the path based on the provided API (context) and Label (host).
     *
     * @param api Provided API object
     * @param label Provided label object
     * @return Updated WSDL file path
     * @throws APIMgtWSDLException Error while updating WSDL files
     */
    @Override
    public String getUpdatedWSDLPath(API api, Label label) throws APIMgtWSDLException {
        if (label != null) {
            for (Map.Entry<String, Description> entry : pathToDescriptionMap.entrySet()) {
                Description description = entry.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("Updating endpoints of WSDL: " + entry.getKey());
                }
                updateEndpoints(label.getAccessUrls(), api, description);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated endpoints of WSDL: " + entry.getKey());
                }
                try (FileOutputStream wsdlFileOutputStream = new FileOutputStream(new File(entry.getKey()))) {
                    WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
                    writer.writeWSDL(description.toElement(), wsdlFileOutputStream);
                } catch (IOException | WSDLException e) {
                    throw new APIMgtWSDLException(
                            "Failed to create WSDL archive for API:" + api.getName() + ":" + api.getVersion()
                                    + " for label " + label.getName(),
                            ExceptionCodes.ERROR_WHILE_CREATING_WSDL_ARCHIVE);
                }
            }
        }
        return wsdlArchiveExtractedPath;
    }

    @Override
    public boolean canProcess() {
        return canProcess;
    }

    @Override
    public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
        Map<String, String> endpointsMap = getEndpoints();
        WSDLInfo wsdlInfo = new WSDLInfo();
        wsdlInfo.setEndpoints(endpointsMap);
        wsdlInfo.setVersion(APIMgtConstants.WSDLConstants.WSDL_VERSION_20);
        return wsdlInfo;
    }

    /**
     * Updates the endpoints of the {@code description} based on the provided {@code endpointURLs} and {@code api}.
     *
     * @param endpointURLs Endpoint URIs
     * @param api Provided API object
     * @param description WSDL 2.0 description
     * @throws APIMgtWSDLException If an error occurred while updating endpoints
     */
    private void updateEndpoints(List<String> endpointURLs, API api, Description description)
            throws APIMgtWSDLException {
        String context = api.getContext().startsWith("/") ? api.getContext() : "/" + api.getContext();
        String selectedUrl;
        try {
            selectedUrl = APIMWSDLUtils.getSelectedEndpoint(endpointURLs) + context;
            if (log.isDebugEnabled()) {
                log.debug("Selected URL for updating endpoints of WSDL: " + selectedUrl);
            }
        } catch (MalformedURLException e) {
            throw new APIMgtWSDLException("Error while selecting endpoints for WSDL", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
        if (!StringUtils.isBlank(selectedUrl)) {
            updateEndpoints(selectedUrl, description);
        }
    }

    /**
     * Clear the actual service endpoints in {@code description} and use {@code selectedEndpoint} instead of
     * actual endpoints.
     *
     * @param selectedEndpoint Endpoint which will replace the WSDL endpoints
     * @param description WSDL 2.0 description
     * @throws APIMgtWSDLException If an error occurred while updating endpoints
     */
    private void updateEndpoints(String selectedEndpoint, Description description) throws APIMgtWSDLException {
        Service[] serviceMap = description.getServices();
        for (Service svc : serviceMap) {
            Endpoint[] portMap = svc.getEndpoints();
            for (Endpoint endpoint : portMap) {
                EndpointElement element = endpoint.toElement();
                try {
                    element.setAddress(new URI(selectedEndpoint));
                } catch (URISyntaxException e) {
                    throw new APIMgtWSDLException(
                            "Error occurred while setting the wsdl address location as " + selectedEndpoint, e,
                            ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
                }
            }
        }
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
