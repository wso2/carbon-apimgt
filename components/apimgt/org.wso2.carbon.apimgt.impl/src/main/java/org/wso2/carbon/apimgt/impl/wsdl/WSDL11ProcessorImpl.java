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
package org.wso2.carbon.apimgt.impl.wsdl;

import com.google.common.primitives.Bytes;
import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class that reads wsdl soap operations and maps with the types.
 */
public class WSDL11ProcessorImpl extends AbstractWSDLProcessor {
    private static final Logger log = LoggerFactory.getLogger(WSDL11ProcessorImpl.class);
    private boolean hasError = false;

    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
    private static final String WSDL11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private ErrorHandler error;
    private Definition wsdlDefinition;

    private Map<String, Definition> pathToDefinitionMap;

    public WSDL11ProcessorImpl() {
    }

    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        WSDLReader wsdlReader;
        try {
            wsdlReader = APIMWSDLReader.getWsdlFactoryInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(null, getSecuredParsedDocumentFromContent(wsdlContent));
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                        + " with a single WSDL.");
            }
        } catch (WSDLException | APIManagementException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            setError(new ErrorItem(ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorMessage(), e.getMessage(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorCode(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getHttpStatusCode()));
        }
        return !hasError;
    }

    @Override
    public boolean init(URL url) throws APIMgtWSDLException {
        WSDLReader wsdlReader;
        try {
            wsdlReader = APIMWSDLReader.getWsdlFactoryInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(null, getSecuredParsedDocumentFromURL(url));
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                        + " with a single WSDL.");
            }
        } catch (WSDLException | APIManagementException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            setError(new ErrorItem(ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorMessage(), e.getMessage(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorCode(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getHttpStatusCode()));
        }
        return !hasError;
    }

    @Override
    public boolean initPath(String path) throws APIMgtWSDLException {
        pathToDefinitionMap = new HashMap<>();
        WSDLReader wsdlReader;
        try {
            wsdlReader = APIMWSDLReader.getWsdlFactoryInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        try {
            // switch off the verbose mode
            wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
            wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
            File folderToImport = new File(path);
            Collection<File> foundWSDLFiles = APIFileUtil.searchFilesWithMatchingExtension(folderToImport, "wsdl");
            if (log.isDebugEnabled()) {
                log.debug("Found " + foundWSDLFiles.size() + " WSDL file(s) in path " + path);
            }
            for (File file : foundWSDLFiles) {
                String absWSDLPath = file.getAbsolutePath();
                if (log.isDebugEnabled()) {
                    log.debug("Processing WSDL file: " + absWSDLPath);
                }
                Definition definition = wsdlReader.readWSDL(path, getSecuredParsedDocumentFromPath(absWSDLPath));
                pathToDefinitionMap.put(absWSDLPath, definition);
            }
            if (foundWSDLFiles.isEmpty()) {
                setError(ExceptionCodes.NO_WSDL_FOUND_IN_WSDL_ARCHIVE);
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully processed all WSDL files in path " + path);
            }
        } catch (WSDLException | APIManagementException e) {
            //This implementation class cannot process the WSDL. Continuing after setting canProcess = false
            log.debug(this.getClass().getName() + " was unable to process the WSDL Files for the path: " + path, e);
            setError(new ErrorItem(ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorMessage(), e.getMessage(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorCode(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getHttpStatusCode()));
        }
        return !hasError;
    }

    @Override
    public WSDLInfo getWsdlInfo() throws APIMgtWSDLException {
        WSDLInfo wsdlInfo = new WSDLInfo();
        Map<String, String> endpointsMap = getEndpoints();
        wsdlInfo.setEndpoints(endpointsMap);
        wsdlInfo.setVersion(APIConstants.WSDL_VERSION_11);
        return wsdlInfo;
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    public boolean canProcess(byte[] content) {
        return Bytes.indexOf(content, WSDL11_NAMESPACE.getBytes()) > 0;
    }

    public boolean canProcess(String path) {
        return APIFileUtil.hasFileContainsString(path, "wsdl", WSDL11_NAMESPACE);
    }

    @Override
    public boolean canProcess(URL url) {
        return APIUtil.isURLContentContainsString(url, WSDL11_NAMESPACE);
    }

    @Override
    public ErrorHandler getError() {
        return error;
    }

    /**
     * Get endpoints defined in WSDL file(s).
     *
     * @return a Map of endpoint names and their URIs.
     * @throws APIMgtWSDLException if error occurs while reading endpoints
     */
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

    /**
     * Get endpoints defined in the provided WSDL definition.
     *
     * @param definition WSDL Definition
     * @return a Map of endpoint names and their URIs.
     * @throws APIMgtWSDLException if error occurs while reading endpoints
     */
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
     * Get the addressURl from the Extensibility element.
     *
     * @param exElement ExtensibilityElement
     * @return {@link String}
     * @throws APIMgtWSDLException when the extensibility element is not supported
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

    private void setError(ErrorHandler error) {
        this.hasError = true;
        this.error = error;
    }
}
