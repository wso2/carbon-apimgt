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
package org.wso2.carbon.apimgt.core.util;

import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

/**
 * This class is used to read the WSDL file using WSDL4J library.
 */

public class APIMWSDLUtils {

    private static WSDLFactory wsdlFactoryInstance;

    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";

    private static final Log log = LogFactory.getLog(APIMWSDLUtils.class);

    private static WSDLFactory getWsdlFactoryInstance() throws WSDLException {
        if (null == wsdlFactoryInstance) {
            wsdlFactoryInstance = WSDLFactory.newInstance();
        }
        return wsdlFactoryInstance;
    }

    /**
     * Create the WSDL definition <javax.wsdl.Definition> from the baseURI of
     * the WSDL
     *
     * @return {@link Definition} - WSDL4j definition constructed form the wsdl
     * original baseuri
     * @throws APIManagementException
     * @throws WSDLException
     */

    private static Definition readWSDLFile(String baseURI) throws APIManagementException, WSDLException {
        WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
        // switch off the verbose mode
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        reader.setFeature("javax.wsdl.importDocuments", false);

        if (log.isDebugEnabled()) {
            log.debug("Reading  the WSDL. Base uri is " + baseURI);
        }
        return reader.readWSDL(baseURI);
    }

    public static WSDLInfo getWSDLInfo(String wsdlUrl) throws APIManagementException {
        WSDLInfo wsdlInfo = new WSDLInfo();
        if (isValidWSDLURL(wsdlUrl, true)) {
            if (!isWSDL2Document(wsdlUrl)) {
                Definition wsdlDefinition = null;
                try {
                    wsdlDefinition = readWSDLFile(wsdlUrl);
                } catch (WSDLException e) {
                    throw new APIManagementException("Error while reading WSDL definition", e);
                }
                Map<String, String> endpointsMap = getEndpoints(wsdlDefinition);
                wsdlInfo.setEndpoints(endpointsMap);
                wsdlInfo.setVersion("1.1");
                return wsdlInfo;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Clear the actual service Endpoint and use Gateway Endpoint instead of the
     * actual Endpoint.
     *
     * @param definition - {@link Definition} - WSDL4j wsdl definition
     * @throws APIManagementException
     */

    private static Map<String, String> getEndpoints(Definition definition) throws APIManagementException {

        Map serviceMap = definition.getAllServices();
        Iterator serviceItr = serviceMap.entrySet().iterator();
        Map<String, String> serviceEndpointMap = new HashMap<>();
        try {
            while (serviceItr.hasNext()) {
                Map.Entry svcEntry = (Map.Entry) serviceItr.next();
                Service svc = (Service) svcEntry.getValue();
                Map portMap = svc.getPorts();
                for (Object o : portMap.entrySet()) {
                    Map.Entry portEntry = (Map.Entry) o;
                    Port port = (Port) portEntry.getValue();
                    List<ExtensibilityElement> extensibilityElementList = port.getExtensibilityElements();
                    for (ExtensibilityElement extensibilityElement : extensibilityElementList) {
                        String addressURI = getAddressUrl(extensibilityElement);
                        serviceEndpointMap.put(port.getName(), addressURI);
                    }
                }
            }
            return serviceEndpointMap;
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the wsdl address location";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Get the addressURl from the Extensibility element
     *
     * @param exElement - {@link ExtensibilityElement}
     * @return {@link String}
     * @throws APIManagementException
     */
    private static String getAddressUrl(ExtensibilityElement exElement) throws APIManagementException {
        if (exElement instanceof SOAP12AddressImpl) {
            return ((SOAP12AddressImpl) exElement).getLocationURI();
        } else if (exElement instanceof SOAPAddressImpl) {
            return ((SOAPAddressImpl) exElement).getLocationURI();
        } else if (exElement instanceof HTTPAddressImpl) {
            return ((HTTPAddressImpl) exElement).getLocationURI();
        } else {
            String msg = "Unsupported WSDL errors!";
            log.error(msg);
            throw new APIManagementException(msg);
        }
    }

    /**
     * This method will check the validity of given url. WSDL url should be
     * contain http, https or file system patch
     * otherwise we will mark it as invalid wsdl url. How ever here we do not
     * validate wsdl content.
     *
     * @param wsdlURL wsdl url tobe tested
     * @return true if its valid url else fale
     */
    public static boolean isValidWSDLURL(String wsdlURL, boolean required) {
        if (wsdlURL != null && !"".equals(wsdlURL)) {
            if (wsdlURL.startsWith("http:") || wsdlURL.startsWith("https:") || wsdlURL.startsWith("file:")) {
                return true;
            }
        } else if (!required) {
            // If the WSDL in not required and URL is empty, then we don't need
            // to add debug log.
            // Hence returning.
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("WSDL url validation failed. Provided wsdl url is not valid url: " + wsdlURL);
        }
        return false;
    }

    /**
     * Given a URL, this method checks if the underlying document is a WSDL2
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static boolean isWSDL2Document(String url) throws APIManagementException {
        URL wsdl = null;
        boolean isWsdl2 = false;
        try {
            wsdl = new URL(url);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Malformed URL encountered", e);
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(wsdl.openStream(), Charset.defaultCharset()));

            String inputLine;
            StringBuilder urlContent = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
                urlContent.append(inputLine);
                isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
            }
            in.close();

            if (isWsdl2) {
                org.apache.woden.WSDLReader wsdlReader20 = null;
                wsdlReader20 = org.apache.woden.WSDLFactory.newInstance().newWSDLReader();
                wsdlReader20.readWSDL(url);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error Reading Input from Stream from " + url, e);
        } catch (org.apache.woden.WSDLException e) {
            throw new APIManagementException("Error while reading WSDL2 Document from " + url, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error when closing input stream", e);
                }
            }
        }
        return isWsdl2;
    }

}
