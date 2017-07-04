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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WSDLInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 * This class is used to read the WSDL file using WSDL4J library.
 */

public class APIMWSDLUtils {

    private static WSDLFactory wsdlFactoryInstance;
    private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";

    private static final String WSDL_VERSION_11 = "1.1";
    private static final String WSDL_VERSION_20 = "2.0";

    private static final Log log = LogFactory.getLog(APIMWSDLUtils.class);
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private static WSDLFactory getWsdlFactoryInstance() throws WSDLException {
        if (null == wsdlFactoryInstance) {
            wsdlFactoryInstance = WSDLFactory.newInstance();
        }
        return wsdlFactoryInstance;
    }

    public static byte[] getWSDL(String wsdlUrl) throws APIMgtWSDLException {
        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        URLConnection conn;
        try {
            URL url = new URL(wsdlUrl);
            conn = url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.connect();

            outputStream = new ByteArrayOutputStream();
            inputStream = conn.getInputStream();
            IOUtils.copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new APIMgtWSDLException("Error while reading content from " + wsdlUrl, e,
                    ExceptionCodes.INVALID_WSDL_URL_EXCEPTION);
        } finally {
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    
    /**
     * Returns a {@link String} from the WSDL retrieved from the baseURI
     * 
     * @param wsdlUrl URL of the WSDL
     * @return {@link String} by stringifying the WSDL
     * @throws APIMgtWSDLException
     */
    public static String getWSDLAsString(String wsdlUrl) throws APIMgtWSDLException {
        byte[] wsdlBytes = getWSDL(wsdlUrl);
        return new String(wsdlBytes);
//        WSDLInfo wsdlInfo = getWSDLInfoFromUrl(baseURI);
//        if (WSDL_VERSION_11.equals(wsdlInfo.getVersion())) {
//            Definition wsdl1Definition = null;//= wsdlInfo.getWsdl1Definition();
//            WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            try {
//                writer.writeWSDL(wsdl1Definition, byteArrayOutputStream);
//            } catch (WSDLException e) {
//                throw new APIMgtWSDLException("Error while stringifying WSDL 1.1 content",
//                        ExceptionCodes.INTERNAL_WSDL_EXCEPTION);
//            }
//            return byteArrayOutputStream.toString();
//        } else if(WSDL_VERSION_20.equals(wsdlInfo.getVersion())) {
//            return null; //todo implement this
//        }else{
//            throw new APIMgtWSDLException("Provided WSDL was not recognised as WSDL 1.x or 2.0",
//                    ExceptionCodes.INVALID_WSDL_CONTENT_EXCEPTION);
//        }
    }

    /**
     * Create the WSDL definition {@link Definition} from the baseURI of
     * the WSDL
     *
     * @return {@link Definition} - WSDL4j definition constructed form the wsdl
     * original baseuri
     * @throws WSDLException
     */
    private static Definition readWSDL1(String baseURI) throws WSDLException {
        WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
        // switch off the verbose mode
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        reader.setFeature("javax.wsdl.importDocuments", false);

        if (log.isDebugEnabled()) {
            log.debug("Reading  the WSDL. Base uri is " + baseURI);
        }
        return reader.readWSDL(baseURI);
    }

    public static WSDLInfo getWSDLInfoFromUrl(String wsdlUrl) throws APIMgtWSDLException {
//        WSDLInfo wsdlInfo = new WSDLInfo();
//        if (isValidWSDLURL(wsdlUrl, true)) {
//            if (!isWSDL2Document(wsdlUrl)) {
//                Definition wsdlDefinition = null;
//                try {
//                    wsdlDefinition = readWSDL1(wsdlUrl);
//                } catch (WSDLException e) {
//                    throw new APIManagementException("Error while reading WSDL definition", e);
//                }
//                Map<String, String> endpointsMap = getEndpoints(wsdlDefinition);
//                wsdlInfo.setEndpoints(endpointsMap);
//                wsdlInfo.setVersion(WSDL_VERSION_11);
//                wsdlInfo.setWsdl1Definition(wsdlDefinition);
//                return wsdlInfo;
//            } else {
//                return null; //todo implement 2.0
//            }
//        } else {
//            throw new APIMgtWSDLException("Provided WSDL URL '" + wsdlUrl + "' is invalid.",
//                    ExceptionCodes.INVALID_WSDL_URL_EXCEPTION);
//        }
        return new WSDLInfo();
    }

    /*
    public static WSDLInfo getWSDLInfoFromContent(String wsdlContent) throws APIMgtWSDLException {
        WSDLInfo wsdlInfo = new WSDLInfo();

        
        WSDLReader wsdlReader = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
        // switch off the verbose mode
        wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        wsdlReader.setFeature("javax.wsdl.importDocuments", false);
        Definition wsdlDefinition = wsdlReader.getWSDL(null, new InputSource(new ByteArrayInputStream(wsdl)));
        
        
        if (!isWSDL2Document(wsdlUrl)) {
            Definition wsdlDefinition = null;
            try {
                wsdlDefinition = readWSDL1(wsdlUrl);
            } catch (WSDLException e) {
                throw new APIManagementException("Error while reading WSDL definition", e);
            }
            Map<String, String> endpointsMap = getEndpoints(wsdlDefinition);
            wsdlInfo.setEndpoints(endpointsMap);
            wsdlInfo.setVersion(WSDL_VERSION_11);
            wsdlInfo.setWsdl1Definition(wsdlDefinition);
            return wsdlInfo;
        } else {
            return null; //todo implement 2.0
        }
    }*/
    
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
    private static boolean isWSDL2Document(String url) throws APIManagementException {
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
        } catch (IOException e) {
            throw new APIManagementException("Error Reading Input from Stream from " + url, e);
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

//    /**
//     * Read the wsdl and clean the actual service endpoint instead of that set
//     * the gateway endpoint.
//     *
//     * @return {@link String} - The new WSDL content
//     * @throws APIManagementException
//     */
//    public static String readAndUpdateEndpointsWSDL1(String wsdlUrl, String endpointWithApiContext)
//            throws APIManagementException {
//
//        try {
//            Definition wsdlDefinition = readWSDL1(wsdlUrl);
//
//            if (!StringUtils.isBlank(endpointWithApiContext)) {
//                setEndpointUrlsWSDL1(wsdlDefinition, endpointWithApiContext);
//            }
//
//            WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);
//            return byteArrayOutputStream.toString();
//            
//        } catch (Exception e) {
//            String msg = " Error occurs when change the addres URL of the WSDL";
//            log.error(msg);
//            throw new APIManagementException(msg, e);
//        }
//    }
    
    /**
     * Clear the actual service Endpoint and use Gateway Endpoint instead of the
     * actual Endpoint.
     *
     * get the first api label --> get access urls
     *
     * @param definition - {@link Definition} - WSDL4j wsdl definition
     * @throws APIManagementException
     */
    private static void setEndpointUrlsWSDL1(Definition definition, String endpointWithApiContext)
            throws APIManagementException {
        Map serviceMap = definition.getAllServices();
        Iterator serviceItr = serviceMap.entrySet().iterator();
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
                        setAddressUrl(extensibilityElement, endpointWithApiContext);
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the wsdl address location";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }


    /**
     * Get the addressURl from the Extensibility element
     * @param exElement - {@link ExtensibilityElement}
     * @throws APIManagementException
     */
    private static void setAddressUrl(ExtensibilityElement exElement, String endpointWithApiContext) throws APIManagementException {

        if (exElement instanceof SOAP12AddressImpl) {
            ((SOAP12AddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else if (exElement instanceof SOAPAddressImpl) {
            ((SOAPAddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else if (exElement instanceof HTTPAddressImpl) {
            ((HTTPAddressImpl) exElement).setLocationURI(endpointWithApiContext);
        } else {
            String msg = "Unsupported WSDL errors!";
            log.error(msg);
            throw new APIManagementException(msg);
        }
    }

}
