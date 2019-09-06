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
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.WSDLWriter;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WSDL20ProcessorImpl extends AbstractWSDLProcessor {

    private static final String WSDL20_NAMESPACE = "http://www.w3.org/ns/wsdl";
    //Fields required for processing a single wsdl
    protected Description wsdlDescription;

    //Fields required for processing a file path with multiple WSDLs (WSDL archive)
    protected Map<String, Description> pathToDescriptionMap;
    protected String wsdlArchiveExtractedPath;
    private ErrorHandler error;
    private boolean hasError = false;

    //Common fields
    private static volatile WSDLFactory wsdlFactoryInstance;

    private static final String WSDL_VERSION_20 = "2.0";

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

    public WSDL20ProcessorImpl() {

    }

    @Override
    public boolean init(URL url) throws APIMgtWSDLException {
        WSDLReader reader;
        try {
            reader = WSDLFactory.newInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
        Document document = getSecuredParsedDocumentFromURL(url);
        WSDLSource wsdlSource = getWSDLSourceFromDocument(document, reader);
        try {
            wsdlDescription = reader.readWSDL(wsdlSource);
        } catch (WSDLException e) {
            //This implementation class cannot process the WSDL.
            log.debug("Cannot process the WSDL by " + this.getClass().getName(), e);
            setError(new ErrorItem(ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorMessage(), e.getMessage(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorCode(),
                    ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getHttpStatusCode()));
        }
        return !hasError;
    }

    @Override
    public boolean init(byte[] wsdlContent) throws APIMgtWSDLException {
        WSDLReader reader;
        try {
            reader = getWsdlFactoryInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
        Document document = getSecuredParsedDocumentFromContent(wsdlContent);
        WSDLSource wsdlSource = getWSDLSourceFromDocument(document, reader);
        try {
            wsdlDescription = reader.readWSDL(wsdlSource);
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized an instance of " + this.getClass().getSimpleName()
                        + " with a single WSDL.");
            }
        } catch (WSDLException e) {
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
        pathToDescriptionMap = new HashMap<>();
        wsdlArchiveExtractedPath = path;
        WSDLReader reader;
        try {
            reader = getWsdlFactoryInstance().newWSDLReader();
        } catch (WSDLException e) {
            throw new APIMgtWSDLException("Error while initializing the WSDL reader", e);
        }

        reader.setFeature(WSDLReader.FEATURE_VALIDATION, false);
        File folderToImport = new File(path);
        Collection<File> foundWSDLFiles = APIFileUtil.searchFilesWithMatchingExtension(folderToImport, "wsdl");
        if (log.isDebugEnabled()) {
            log.debug("Found " + foundWSDLFiles.size() + " WSDL file(s) in path " + path);
        }

        try {
            for (File file : foundWSDLFiles) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing WSDL file: " + file.getAbsolutePath());
                }

                Document document = getSecuredParsedDocumentFromPath(file.getAbsolutePath());
                WSDLSource wsdlSource = getWSDLSourceFromDocument(document, reader);
                Description description = reader.readWSDL(wsdlSource);
                pathToDescriptionMap.put(file.getAbsolutePath(), description);
            }
            if (foundWSDLFiles.isEmpty()) {
                setError(ExceptionCodes.NO_WSDL_FOUND_IN_WSDL_ARCHIVE);
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully processed all WSDL files in path " + path);
            }
        } catch (WSDLException e) {
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
        Map<String, String> endpointsMap = getEndpoints();
        WSDLInfo wsdlInfo = new WSDLInfo();
        wsdlInfo.setEndpoints(endpointsMap);
        wsdlInfo.setVersion(WSDL_VERSION_20);
        return wsdlInfo;
    }

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

    @Override
    public byte[] updateEndpoints(API api, String environmentName, String environmentType) throws APIMgtWSDLException {
        return new byte[0];
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public boolean canProcess(byte[] content) {
        return Bytes.indexOf(content, WSDL20_NAMESPACE.getBytes()) > 0;
    }

    @Override
    public boolean canProcess(String path) {
        return APIFileUtil.hasFileContainsString(path, "wsdl", WSDL20_NAMESPACE);
    }

    @Override
    public boolean canProcess(URL url) {
        return APIUtil.isURLContentContainsString(url, WSDL20_NAMESPACE);
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

    private void setError(ErrorHandler error) {
        this.hasError = true;
        this.error = error;
    }


    private WSDLSource getWSDLSourceFromDocument(Document document, WSDLReader reader) {
        Element domElement = document.getDocumentElement();
        WSDLSource wsdlSource = reader.createWSDLSource();
        wsdlSource.setSource(domElement);
        return wsdlSource;
    }
}
