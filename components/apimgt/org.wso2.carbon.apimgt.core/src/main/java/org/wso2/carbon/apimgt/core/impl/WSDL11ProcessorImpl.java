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

import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.WSDLProcessor;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 * This class handles WSDL 1.1 related operations
 */
public class WSDL11ProcessorImpl implements WSDLProcessor {

    private static final Logger log = LoggerFactory.getLogger(WSDL11ProcessorImpl.class);
    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
    private static final String WSDL_VERSION_11 = "1.1";

    private static volatile WSDLFactory wsdlFactoryInstance;
    private boolean canProcess = false;

    //Fields required for processing a single wsdl
    protected Definition wsdlDefinition;

    //Fields required for processing WSDL archive
    protected Map<String, Definition> pathToDefinitionMap;
    protected String wsdlArchiveExtractedPath;

    private static WSDLFactory getWsdlFactoryInstance() throws APIMgtWSDLException {
        if (wsdlFactoryInstance == null) {
            try {
                synchronized (WSDL11ProcessorImpl.class) {
                    if (wsdlFactoryInstance == null) {
                        wsdlFactoryInstance = WSDLFactory.newInstance();
                    }
                }
            } catch (WSDLException e) {
                throw new APIMgtWSDLException("Error while instantiating WSDL 1.1 factory", e,
                        ExceptionCodes.ERROR_WHILE_INITIALIZING_WSDL_FACTORY);
            }
        }
        return wsdlFactoryInstance;
    }

    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(null, new InputSource(new ByteArrayInputStream(wsdlContent)));
            canProcess = true;
        } catch (WSDLException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            canProcess = false;
        }
        return canProcess;
    }

    @Override
    public boolean initPath(String path) throws APIMgtWSDLException {
        pathToDefinitionMap = new HashMap<>();
        wsdlArchiveExtractedPath = path;
        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            File folderToImport = new File(path);
            Collection<File> foundWSDLFiles = APIFileUtils.searchFilesWithMatchingExtension(folderToImport, "wsdl");
            for (File file : foundWSDLFiles) {
                String absWSDLPath = file.getAbsolutePath();
                Definition definition = wsdlReader.readWSDL(null, absWSDLPath);
                pathToDefinitionMap.put(absWSDLPath, definition);
            }
            if (foundWSDLFiles.size() > 0) {
                canProcess = true;
            }
        } catch (WSDLException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            canProcess = false;
        }
        return canProcess;
    }

    public boolean canProcess() {
        return canProcess;
    }

    public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
        Map<String, String> endpointsMap = getEndpoints();
        WSDLInfo wsdlInfo = new WSDLInfo();
        wsdlInfo.setEndpoints(endpointsMap);
        wsdlInfo.setVersion(WSDL_VERSION_11);
        return wsdlInfo;
    }

    public byte[] getWSDL() throws APIMgtWSDLException {
        ByteArrayOutputStream byteArrayOutputStream = getWSDLByteArrayOutputStream(wsdlDefinition);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getUpdatedWSDL(API api, Label label) throws APIMgtWSDLException {
        if (label != null) {
            updateEndpoints(label.getAccessUrls(), api, wsdlDefinition);
            return getWSDL();
        }
        return new byte[0];
    }

    @Override
    public String getUpdatedWSDLPath(API api, Label label) throws APIMgtWSDLException {
        if (label != null) {
            for (Map.Entry<String, Definition> entry : pathToDefinitionMap.entrySet()) {
                Definition definition = entry.getValue();
                updateEndpoints(label.getAccessUrls(), api, definition);
                try (FileOutputStream wsdlFileOutputStream = new FileOutputStream(new File(entry.getKey()))) {
                    WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
                    writer.writeWSDL(definition, wsdlFileOutputStream);
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

    /**
     * Get the addressURl from the Extensibility element
     *
     * @param exElement ExtensibilityElement
     * @return {@link String}
     * @throws APIMgtWSDLException
     */
    private String getAddressUrl(Object exElement) throws APIMgtWSDLException {
        if (exElement instanceof SOAP12AddressImpl) {
            return ((SOAP12AddressImpl) exElement).getLocationURI();
        } else if (exElement instanceof SOAPAddressImpl) {
            return ((SOAPAddressImpl) exElement).getLocationURI();
        } else if (exElement instanceof HTTPAddressImpl) {
            return ((HTTPAddressImpl) exElement).getLocationURI();
        } else {
            throw new APIMgtWSDLException("Unsupported WSDL Extensibility element",
                    ExceptionCodes.UNSUPPORTED_WSDL_EXTENSIBILITY_ELEMENT);
        }
    }

    private Map<String, String> getEndpoints() throws APIMgtWSDLException {
        if (wsdlDefinition != null) {
            return getEndpoints(wsdlDefinition);
        } else {
            Map<String, String> allEndpointsOfAllWSDLs = new HashMap<>();
            for (Definition definition : pathToDefinitionMap.values()) {
                Map<String, String> wsdlEndpoints = getEndpoints(definition);
                allEndpointsOfAllWSDLs.putAll(wsdlEndpoints);
            }
            return allEndpointsOfAllWSDLs;
        }
    }

    private Map<String, String> getEndpoints(Definition definition) throws APIMgtWSDLException {
        Map serviceMap = definition.getAllServices();
        Iterator serviceItr = serviceMap.entrySet().iterator();
        Map<String, String> serviceEndpointMap = new HashMap<>();
        while (serviceItr.hasNext()) {
            Map.Entry svcEntry = (Map.Entry) serviceItr.next();
            Service svc = (Service) svcEntry.getValue();
            Map portMap = svc.getPorts();
            for (Object o : portMap.entrySet()) {
                Map.Entry portEntry = (Map.Entry) o;
                Port port = (Port) portEntry.getValue();
                List extensibilityElementList = port.getExtensibilityElements();
                for (Object extensibilityElement : extensibilityElementList) {
                    String addressURI = getAddressUrl(extensibilityElement);
                    serviceEndpointMap.put(port.getName(), addressURI);
                }
            }
        }
        return serviceEndpointMap;
    }

    /**
     * Clear the actual service Endpoint and use Gateway Endpoint instead of the
     * actual Endpoint.
     * <p/>
     * get the first api label --> get access urls
     *
     * @throws APIMgtWSDLException
     */
    private void updateEndpointUrls(String selectedEndpoint, Definition definition) throws APIMgtWSDLException {
        Map serviceMap = definition.getAllServices();
        for (Object service : serviceMap.entrySet()) {
            Map.Entry svcEntry = (Map.Entry) service;
            Service svc = (Service) svcEntry.getValue();
            Map portMap = svc.getPorts();
            for (Object o : portMap.entrySet()) {
                Map.Entry portEntry = (Map.Entry) o;
                Port port = (Port) portEntry.getValue();

                List extensibilityElementList = port.getExtensibilityElements();
                for (Object extensibilityElement : extensibilityElementList) {
                    setAddressUrl(extensibilityElement, selectedEndpoint);
                }
            }
        }
    }

    /**
     * Get the addressURl from the Extensibility element
     *
     * @param exElement - ExtensibilityElement
     * @throws APIMgtWSDLException
     */
    private void setAddressUrl(Object exElement, String endpointWithApiContext)
            throws APIMgtWSDLException {

        if (exElement instanceof SOAP12AddressImpl) {
            ((SOAP12AddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else if (exElement instanceof SOAPAddressImpl) {
            ((SOAPAddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else if (exElement instanceof HTTPAddressImpl) {
            ((HTTPAddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else {
            throw new APIMgtWSDLException("Unsupported WSDL Extensibility element",
                    ExceptionCodes.UNSUPPORTED_WSDL_EXTENSIBILITY_ELEMENT);
        }
    }

    /**
     * Read the wsdl and clean the actual service endpoint instead of that set
     * the gateway endpoint.
     *
     * @throws APIMgtWSDLException
     */
    private void updateEndpoints(List<String> endpointURLs, API api, Definition definition) throws APIMgtWSDLException {
        String context = api.getContext().startsWith("/") ? api.getContext() : "/" + api.getContext();
        String selectedUrl;
        try {
            selectedUrl = getSelectedEndpoint(endpointURLs) + context;
        } catch (MalformedURLException e) {
            throw new APIMgtWSDLException("Error while selecting endpoints for WSDL", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
        if (!StringUtils.isBlank(selectedUrl)) {
            updateEndpointUrls(selectedUrl, definition);
        }
    }

    private ByteArrayOutputStream getWSDLByteArrayOutputStream(Definition definition) throws APIMgtWSDLException {
        WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            writer.writeWSDL(definition, byteArrayOutputStream);
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while stringifying WSDL definition", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
        return byteArrayOutputStream;
    }

    private String getSelectedEndpoint(List<String> endpoints) throws MalformedURLException {
        if (endpoints.size() > 0) {
            for (String ep : endpoints) {
                URL url = new URL(ep);
                if ("https".equalsIgnoreCase(url.getProtocol())) {
                    return ep;
                }
            }
        } else {
            return endpoints.get(0);
        }
        return null;
    }
}
