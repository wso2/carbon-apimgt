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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 * The class that processes WSDL 1.1 documents
 */
public class WSDL11ProcessorImpl extends AbstractWSDLProcessor {
    private static final Logger log = LoggerFactory.getLogger(WSDL11ProcessorImpl.class);
    private boolean hasError = false;

    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";
    private static final String WSDL11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    private ErrorHandler error;

    // root WSDL definition
    private Definition wsdlDefinition;

    private static volatile WSDLFactory wsdlFactoryInstance;

    // path of the extracted zip when processing a WSDL archive (.zip)
    private String wsdlArchiveExtractedPath;

    // path of each WSDL is stored here when while processing a WSDL archive (.zip)
    private Map<String, Definition> pathToDefinitionMap;

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

    public WSDL11ProcessorImpl() {
    }

    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        setMode(Mode.SINGLE);
        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

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
        setMode(Mode.SINGLE);
        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            wsdlDefinition = wsdlReader.readWSDL(url.toString(), getSecuredParsedDocumentFromURL(url));
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
        setMode(Mode.ARCHIVE);
        pathToDefinitionMap = new HashMap<>();
        wsdlArchiveExtractedPath = path;
        WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();

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
                Definition definition = wsdlReader.readWSDL(absWSDLPath, getSecuredParsedDocumentFromPath(absWSDLPath));
                pathToDefinitionMap.put(absWSDLPath, definition);

                // set the first found WSDL as wsdlDefinition variable assuming that it is the root WSDL
                if (wsdlDefinition == null) {
                    wsdlDefinition = definition;
                }
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
    public void updateEndpoints(API api, String environmentName, String environmentType) throws APIMgtWSDLException {
        if (Mode.SINGLE.equals(getMode())) {
            updateEndpointsOfSingleWSDL(api, environmentName, environmentType);
        } else {
            updateEndpointsOfWSDLArchive(api, environmentName, environmentType);
        }
    }

    /**
     * Update the endpoint information of the WSDL (single WSDL scenario) when an API and the environment details are
     * provided
     *
     * @param api API
     * @param environmentName name of the gateway environment
     * @param environmentType type of the gateway environment
     * @throws APIMgtWSDLException when error occurred while updating the endpoints
     */
    private void updateEndpointsOfSingleWSDL(API api, String environmentName, String environmentType)
            throws APIMgtWSDLException {
        updateEndpointsOfSingleWSDL(api, environmentName, environmentType, wsdlDefinition);
    }

    /**
     * Update the endpoint information of the provided WSDL definition when an API and the environment details are
     * provided
     *
     * @param api API
     * @param environmentName name of the gateway environment
     * @param environmentType type of the gateway environment
     * @param wsdlDefinition WSDL 1.1 definition
     * @throws APIMgtWSDLException when error occurred while updating the endpoints
     */
    private void updateEndpointsOfSingleWSDL(API api, String environmentName, String environmentType,
              Definition wsdlDefinition) throws APIMgtWSDLException {
        Map serviceMap = wsdlDefinition.getAllServices();
        URL addressURI;
        String organization = api.getOrganization();
        for (Object entry : serviceMap.entrySet()) {
            Map.Entry svcEntry = (Map.Entry) entry;
            Service svc = (Service) svcEntry.getValue();
            Map portMap = svc.getPorts();
            for (Object o : portMap.entrySet()) {
                Map.Entry portEntry = (Map.Entry) o;
                Port port = (Port) portEntry.getValue();

                List<ExtensibilityElement> extensibilityElementList = port.getExtensibilityElements();
                String endpointTransport;
                for (ExtensibilityElement extensibilityElement : extensibilityElementList) {
                    try {
                        addressURI = new URL(getAddressUrl(extensibilityElement));
                        endpointTransport = determineURLTransport(addressURI.getProtocol(), api.getTransports());
                        if (log.isDebugEnabled()) {
                            log.debug("Address URI for the port:" + port.getName() + " is " + addressURI.toString());
                        }
                    } catch (MalformedURLException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error occurred while getting the wsdl address location [" +
                                    getAddressUrl(extensibilityElement) + "]");
                        }
                        endpointTransport = determineURLTransport("https", api.getTransports());
                        // This string to URL conversion done in order to identify URL transport eg - http or https.
                        // Here if there is a conversion failure , consider "https" as default protocol
                    }
                    try {
                        setAddressUrl(extensibilityElement, endpointTransport, api.getContext(), environmentName,
                                environmentType, organization);
                    } catch (APIManagementException e) {
                        throw new APIMgtWSDLException("Error while setting gateway access URLs in the WSDL", e);
                    }
                }
            }
        }
    }

    /**
     * Update the endpoint information of the WSDL (WSDL archive scenario) when an API and the environment details are
     * provided
     *
     * @param api API
     * @param environmentName name of the gateway environment
     * @param environmentType type of the gateway environment
     * @throws APIMgtWSDLException when error occurred while updating the endpoints
     */
    private void updateEndpointsOfWSDLArchive(API api, String environmentName, String environmentType)
            throws APIMgtWSDLException {
        for (Map.Entry<String, Definition> entry : pathToDefinitionMap.entrySet()) {
            Definition definition = entry.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Updating endpoints of WSDL: " + entry.getKey());
            }
            updateEndpointsOfSingleWSDL(api, environmentName, environmentType, definition);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated endpoints of WSDL: " + entry.getKey());
            }
            try (FileOutputStream wsdlFileOutputStream = new FileOutputStream(new File(entry.getKey()))) {
                WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
                writer.writeWSDL(definition, wsdlFileOutputStream);
            } catch (IOException | WSDLException e) {
                throw new APIMgtWSDLException("Failed to create WSDL archive for API:" + api.getId().getName() + ":"
                        + api.getId().getVersion() + " for environment " + environmentName, e,
                        ExceptionCodes.ERROR_WHILE_CREATING_WSDL_ARCHIVE);
            }
        }
    }

    @Override
    public ByteArrayInputStream getWSDL() throws APIMgtWSDLException {
        if (Mode.SINGLE.equals(getMode())) {
            return getSingleWSDL();
        } else {
            return getWSDLArchive(wsdlArchiveExtractedPath);
        }
    }

    /**
     * Retrieves an InputStream representing the WSDL (single WSDL scenario) attached to the processor
     *
     * @return Retrieves an InputStream representing the WSDL
     * @throws APIMgtWSDLException when error occurred while getting the InputStream
     */
    private ByteArrayInputStream getSingleWSDL() throws APIMgtWSDLException {
        WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while converting WSDL definition object to text format", e,
                    ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(bytes);
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
        return APIUtil.isURLContentContainsString(url, WSDL11_NAMESPACE, MAX_URL_READ_LINES);
    }

    @Override
    public ErrorHandler getError() {
        return error;
    }

    /**
     * Returns the processed WSDL Definition object
     *
     * @return processed WSDL Definition object
     */
    protected Definition getWSDLDefinition() {
        return wsdlDefinition;
    }

    @Override
    public void loadXSDs(APIMWSDLReader wsdlReader, String url) throws APIManagementException {
        throw new UnsupportedOperationException("This method is not implemented");
    }

    /**
     * Get endpoints defined in WSDL file(s).
     *
     * @return a Map of endpoint names and their URIs.
     * @throws APIMgtWSDLException if error occurs while reading endpoints
     */
    private Map<String, String> getEndpoints() throws APIMgtWSDLException {
        if (Mode.SINGLE.equals(getMode())) {
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
        } else if (exElement instanceof UnknownExtensibilityElement) {
            Element unknownExtensibilityElement = ((UnknownExtensibilityElement) exElement).getElement();
            if (unknownExtensibilityElement != null) {
                NodeList nodeList = unknownExtensibilityElement.getElementsByTagNameNS(APIConstants.WSDL_NAMESPACE_URI,
                        APIConstants.WSDL_ELEMENT_LOCAL_NAME);
                String url = "";
                if (nodeList != null && nodeList.getLength() > 0) {
                    url = nodeList.item(0).getTextContent();
                }
                return url;
            } else {
                String msg = "WSDL errors! Extensibility Element is null";
                log.error(msg);
                throw new APIMgtWSDLException(msg);
            }
        } else {
            throw new APIMgtWSDLException("Unsupported WSDL Extensibility element",
                    ExceptionCodes.UNSUPPORTED_WSDL_EXTENSIBILITY_ELEMENT);
        }
    }

    /**
     * Set the addressURl from the Extensibility element for the given environment type
     *
     * @param exElement       {@link ExtensibilityElement}
     * @param transports      transports allowed for the address url
     * @param context         API context
     * @param environmentName gateway environment name
     * @param environmentType gateway environment type
     * @throws APIManagementException when unsupported WSDL as a input
     */
    private void setAddressUrl(ExtensibilityElement exElement, String transports, String context,
                               String environmentName, String environmentType, String organization)
            throws APIManagementException {
        if (exElement instanceof SOAP12AddressImpl) {
            ((SOAP12AddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType,
                            organization) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((SOAP12AddressImpl) exElement).getLocationURI());
            }
        } else if (exElement instanceof SOAPAddressImpl) {
            ((SOAPAddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType,
                            organization) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((SOAPAddressImpl) exElement).getLocationURI());
            }
        } else if (exElement instanceof HTTPAddressImpl) {
            ((HTTPAddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType,
                            organization) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((HTTPAddressImpl) exElement).getLocationURI());
            }
        } else if (exElement instanceof UnknownExtensibilityElement) {
            Element unknownExtensibilityElement = ((UnknownExtensibilityElement) exElement).getElement();
            if (unknownExtensibilityElement != null) {
                NodeList nodeList = unknownExtensibilityElement.getElementsByTagNameNS(APIConstants.WSDL_NAMESPACE_URI,
                        APIConstants.WSDL_ELEMENT_LOCAL_NAME);
                if (nodeList != null && nodeList.getLength() > 0) {
                    nodeList.item(0).setTextContent(
                            APIUtil.getGatewayEndpoint(transports, environmentName, environmentType, organization)
                                    + context);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("WSDL address element type is not supported for WSDL element type: " + exElement
                        .getElementType().toString());
            }
            throw new APIManagementException("WSDL address element type is not supported for WSDL element type:" +
                    exElement.getElementType().toString());
        }
    }

    private void setError(ErrorHandler error) {
        this.hasError = true;
        this.error = error;
    }
}
