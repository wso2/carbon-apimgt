/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.impl.utils;

import com.ibm.wsdl.extensions.http.HTTPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12AddressImpl;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLSource;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.xml.EndpointElement;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.wsdl.WSDL11SOAPOperationExtractor;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.wsdl.WSDL11ProcessorImpl;
import org.wso2.carbon.apimgt.impl.wsdl.WSDL20ProcessorImpl;
import org.wso2.carbon.apimgt.impl.wsdl.WSDLProcessor;
import org.wso2.carbon.apimgt.impl.wsdl.exceptions.APIMgtWSDLException;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

/**
 * This class is used to read the WSDL file using WSDL4J library.
 *
 */

public class APIMWSDLReader {

	private static WSDLFactory wsdlFactoryInstance;

	private String baseURI; //WSDL Original URL

	private static final String JAVAX_WSDL_VERBOSE_MODE = "javax.wsdl.verbose";
    private static final String JAVAX_WSDL_IMPORT_DOCUMENTS = "javax.wsdl.importDocuments";

    private static final int ENTITY_EXPANSION_LIMIT = 0;

	private static final Log log = LogFactory.getLog(APIMWSDLReader.class);

	private static final String WSDL20_NAMESPACE = "http://www.w3.org/ns/wsdl";
	private static final String WSDL11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";

    public APIMWSDLReader(){
    }

	public APIMWSDLReader(String baseURI) {
		this.baseURI = baseURI;
	}

	public static WSDLFactory getWsdlFactoryInstance() throws WSDLException {
		if (null == wsdlFactoryInstance) {
			wsdlFactoryInstance = WSDLFactory.newInstance();
		}
		return wsdlFactoryInstance;
	}

    /**
     * Extract the WSDL archive zip and validates it
     *
     * @param inputStream zip input stream
     * @return Validation information
     * @throws APIManagementException Error occurred during validation
     */
    public static WSDLValidationResponse extractAndValidateWSDLArchive(InputStream inputStream)
            throws APIManagementException {
        String path = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator
                + APIConstants.WSDL_ARCHIVES_TEMP_FOLDER + File.separator + UUID.randomUUID().toString();
        String archivePath = path + File.separator + APIConstants.WSDL_ARCHIVE_ZIP_FILE;
        String extractedLocation = APIFileUtil
                .extractUploadedArchive(inputStream, APIConstants.API_WSDL_EXTRACTED_DIRECTORY, archivePath, path);
        if (log.isDebugEnabled()) {
            log.debug("Successfully extracted WSDL archive. Location: " + extractedLocation);
        }
        WSDLProcessor processor;
        try {
            processor = getWSDLProcessor(extractedLocation);
        } catch (APIManagementException e) {
            return handleExceptionDuringValidation(e);
        }

        WSDLValidationResponse wsdlValidationResponse = new WSDLValidationResponse();
        if (processor.hasError()) {
            wsdlValidationResponse.setValid(false);
            wsdlValidationResponse.setError(processor.getError());
        } else {
            wsdlValidationResponse.setValid(true);
            WSDLArchiveInfo wsdlArchiveInfo = new WSDLArchiveInfo(path, APIConstants.WSDL_ARCHIVE_ZIP_FILE,
                    processor.getWsdlInfo());
            wsdlValidationResponse.setWsdlArchiveInfo(wsdlArchiveInfo);
            wsdlValidationResponse.setWsdlInfo(processor.getWsdlInfo());
            wsdlValidationResponse.setWsdlProcessor(processor);
        }
        return wsdlValidationResponse;
    }

    /**
     * Extract the WSDL file and validates it
     *
     * @param inputStream file input stream
     * @return Validation information
     * @throws APIManagementException Error occurred during validation
     */
    public static WSDLValidationResponse validateWSDLFile(InputStream inputStream) throws APIManagementException {
        try {
            byte[] wsdlContent = APIUtil.toByteArray(inputStream);
            WSDLProcessor processor = getWSDLProcessor(wsdlContent);
            return getWsdlValidationResponse(processor);
        } catch (APIManagementException e) {
            return handleExceptionDuringValidation(e);
        } catch (IOException e) {
            throw new APIMgtWSDLException("Error while validating WSDL", e);
        }
    }

    /**
     * Extract the WSDL url and validates it
     *
     * @param wsdlUrl WSDL url
     * @return Validation information
     * @throws APIManagementException Error occurred during validation
     */
    public static WSDLValidationResponse validateWSDLUrl(URL wsdlUrl) throws APIManagementException {
        try {
            WSDLProcessor processor = getWSDLProcessorForUrl(wsdlUrl);
            return getWsdlValidationResponse(processor);
        } catch (APIManagementException e) {
            return handleExceptionDuringValidation(e);
        }
    }

    /**
     * Gets WSDL processor WSDL 1.1/WSDL 2.0 based on the content {@code content}.
     *
     * @param content WSDL content
     * @return {@link WSDLProcessor}
     * @throws APIManagementException
     */
    public static WSDLProcessor getWSDLProcessor(byte[] content)
            throws APIManagementException {
        WSDLProcessor wsdl11Processor = new WSDL11ProcessorImpl();
        WSDLProcessor wsdl20Processor = new WSDL20ProcessorImpl();
        try {
            if (wsdl11Processor.canProcess(content)) {
                wsdl11Processor.init(content);
                return wsdl11Processor;
            } else if (wsdl20Processor.canProcess(content)) {
                wsdl20Processor.init(content);
                return wsdl20Processor;
            } else {
                //no processors found if this line reaches
                throw new APIManagementException("No WSDL processor found to process WSDL content.",
                        ExceptionCodes.CONTENT_NOT_RECOGNIZED_AS_WSDL);
            }
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error while instantiating wsdl processor class", e);
        }
    }

    /**
     * Returns the appropriate WSDL 1.1/WSDL 2.0 based on the file path {@code wsdlPath}.
     *
     * @param wsdlPath File path containing WSDL files and dependant files
     * @return WSDL 1.1/2.0 processor for the provided content
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDLProcessor getWSDLProcessor(String wsdlPath) throws APIManagementException {
        WSDLProcessor wsdl11Processor = new WSDL11ProcessorImpl();
        WSDLProcessor wsdl20Processor = new WSDL20ProcessorImpl();
        try {
            if (wsdl11Processor.canProcess(wsdlPath)) {
                wsdl11Processor.initPath(wsdlPath);
                return wsdl11Processor;
            } else if (wsdl20Processor.canProcess(wsdlPath)) {
                wsdl20Processor.initPath(wsdlPath);
                return wsdl20Processor;
            } else {
                //no processors found if this line reaches
                throw new APIManagementException("No WSDL processor found to process WSDL content.",
                        ExceptionCodes.CONTENT_NOT_RECOGNIZED_AS_WSDL);
            }
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error while instantiating wsdl processor class", e);
        }
    }

    /**
     * Returns the appropriate WSDL 1.1/WSDL 2.0 based on the url {@code url}.
     *
     * @param url WSDL url
     * @return WSDL 1.1/2.0 processor for the provided content
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDLProcessor getWSDLProcessorForUrl(URL url) throws APIManagementException {
        WSDLProcessor wsdl11Processor = new WSDL11ProcessorImpl();
        WSDLProcessor wsdl20Processor = new WSDL20ProcessorImpl();

        try {
            if (wsdl11Processor.canProcess(url)) {
                wsdl11Processor.init(url);
                return wsdl11Processor;
            } else if (wsdl20Processor.canProcess(url)) {
                wsdl20Processor.init(url);
                return wsdl20Processor;
            } else {
                //no processors found if this line reaches
                throw new APIManagementException("No WSDL processor found to process WSDL url: " + url,
                        ExceptionCodes.URL_NOT_RECOGNIZED_AS_WSDL);
            }
        } catch (APIMgtWSDLException e) {
            throw new APIManagementException("Error while instantiating wsdl processor class", e);
        }
    }

    /**
     * Validates the input URL string and creates URL object
     *
     * @param url url as a String
     * @return URL object
     * @throws APIManagementException when error occurred while converting String url to URL object
     */
    public static URL getURL(String url) throws APIManagementException {
        URL wsdlUrl;
        try {
            wsdlUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Invalid/Malformed WSDL URL : " + url, e,
                    ExceptionCodes.INVALID_WSDL_URL_EXCEPTION);
        }
        return wsdlUrl;
    }

    /**
     * Returns a WSDL11SOAPOperationExtractor for the url {@code url}. Only WSDL 1.1 is supported.
     *
     * @param url WSDL url
     * @return WSDL11SOAPOperationExtractor for the provided URL
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDL11SOAPOperationExtractor getWSDLSOAPOperationExtractorForUrl(URL url)
            throws APIManagementException {
        WSDL11SOAPOperationExtractor processor = new WSDL11SOAPOperationExtractor();
        processor.init(url);
        return processor;
    }

    /**
     * Returns a WSDL11SOAPOperationExtractor for the WSDL byte content {@code content}. Only WSDL 1.1 is supported.
     *
     * @param content WSDL byte[] content
     * @return WSDL11SOAPOperationExtractor for the provided URL
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDL11SOAPOperationExtractor getWSDLSOAPOperationExtractor(byte[] content)
            throws APIManagementException {
        WSDL11SOAPOperationExtractor processor = new WSDL11SOAPOperationExtractor();
        processor.init(content);
        return processor;
    }

    /**
     * Returns a WSDL11SOAPOperationExtractor for the url {@code url}. Only WSDL 1.1 is supported.
     *
     * @param wsdlPath File path containing WSDL files and dependant files
     * @return WSDL11SOAPOperationExtractor for the provided URL
     * @throws APIManagementException If an error occurs while determining the processor
     */
    public static WSDL11SOAPOperationExtractor getWSDLSOAPOperationExtractor(String wsdlPath)
            throws APIManagementException {
        WSDL11SOAPOperationExtractor processor = new WSDL11SOAPOperationExtractor();
        processor.initPath(wsdlPath);
        return processor;
    }

    /**
	 * Read the wsdl and clean the actual service endpoint instead of that set
	 * the gateway endpoint.
	 *
	 * @return {@link OMElement} - the OMElemnt of the new WSDL content
	 * @throws APIManagementException
	 *
	 */
    @Deprecated
	public OMElement readAndCleanWsdl(API api) throws APIManagementException {

		try {
			Definition wsdlDefinition = readWSDLFile();

			setServiceDefinition(wsdlDefinition, api);

			WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( byteArrayOutputStream.toByteArray());

			return APIUtil.buildOMElement(byteArrayInputStream);


		} catch (Exception e) {
			String msg = " Error occurs when change the addres URL of the WSDL";
			log.error(msg);
			throw new APIManagementException(msg, e);
		}
	}

	@Deprecated
    public OMElement readAndCleanWsdl2(API api) throws APIManagementException {

        try {
            org.apache.woden.wsdl20.Description wsdlDefinition = readWSDL2File();
            setServiceDefinitionForWSDL2(wsdlDefinition, api);
            org.apache.woden.WSDLWriter writer = org.apache.woden.WSDLFactory.newInstance().newWSDLWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            writer.writeWSDL(wsdlDefinition.toElement(), byteArrayOutputStream);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( byteArrayOutputStream.toByteArray());
            return APIUtil.buildOMElement(byteArrayInputStream);
        } catch (Exception e) {
            String msg = " Error occurs when change the addres URL of the WSDL";
            log.error(msg);
            throw new APIManagementException(msg, e);
        }

    }

    /**
     * Gets WSDL definition as a byte array
     *
     * @return converted WSDL definition as byte array
     * @throws APIManagementException
     */
    @Deprecated
    public byte[] getWSDL() throws APIManagementException {
        try {
            Definition wsdlDefinition = readWSDLFile();
            WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            String msg = "Error occurs when change the address URL of the WSDL";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Gets WSDL definition as a byte array given the WSDL definition
     * @param wsdlDefinition generated WSDL definition
     * @return converted WSDL definition as byte array
     * @throws APIManagementException
     */
    public byte[] getWSDL(Definition wsdlDefinition) throws APIManagementException {
        try {
            WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new APIManagementException("Error occurs when change the address URL of the WSDL", e);
        }
    }

    /**
     * Returns WSDL definition from a byte content of the WSDL
     *
     * @param wsdl byte content of the WSDL document
     * @return {@link Definition} - WSDL4j definition constructed form the wsdl
     * @throws APIManagementException
     */
    public Definition getWSDLDefinitionFromByteContent(byte[] wsdl, boolean readDependencies) throws APIManagementException {
        try {
            WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();
            // switch off the verbose mode
            wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
            wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);

            if (!readDependencies) {
                if (wsdlReader instanceof WSDLReaderImpl) {
                    ((WSDLReaderImpl) wsdlReader).setIgnoreSchemaContent(true);
                }
            }

            return wsdlReader.readWSDL(null, getSecuredParsedDocumentFromContent(wsdl));
        } catch (Exception e) {
            String msg = " Error occurs when updating WSDL ";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Validate the base URI of the WSDL reader
     *
     * @throws APIManagementException When error occurred while parsing the content from the URL
     */

    @Deprecated
    public void validateBaseURI() throws APIManagementException {
        if (baseURI.startsWith(APIConstants.WSDL_REGISTRY_LOCATION_PREFIX)) {
            baseURI = APIUtil.getServerURL() + baseURI;
        }

        boolean isWsdl20 = false;
        boolean isWsdl11 = false;

        BufferedReader in = null;
        try {
            String inputLine;
            StringBuilder urlContent = new StringBuilder();
            URL wsdl = new URL(baseURI);
            in = new BufferedReader(new InputStreamReader(wsdl.openStream(), Charset.defaultCharset()));
            while ((inputLine = in.readLine()) != null) {
                urlContent.append(inputLine);
                isWsdl20 = urlContent.indexOf(WSDL20_NAMESPACE) > 0;
                isWsdl11 = urlContent.indexOf(WSDL11_NAMESPACE) > 0;
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while reading WSDL from base URI " + baseURI, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        try {
            if (isWsdl11) {
                readAndValidateWSDL11();
            } else if (isWsdl20) {
                readAndValidateWSDL20();
            } else {
                throw new APIManagementException("URL is not in format of wsdl1.1 or wsdl2.0");
            }
        } catch (WSDLException e) {
            throw new APIManagementException("Error while parsing WSDL content", e);
        } catch (org.apache.woden.WSDLException e) {
            throw new APIManagementException("Error while parsing WSDL content", e);
        }
    }

    /**
     * Given a URL, this method checks if the underlying document is a WSDL2
     *
     * @return true if the underlying document is a WSDL2
     * @throws APIManagementException if error occurred while checking whether baseURI is WSDL2.0
     */
    @Deprecated
    public boolean isWSDL2BaseURI() throws APIManagementException {
        URL wsdl;
        boolean isWsdl2 = false;
        BufferedReader in = null;
        try {
            wsdl = new URL(baseURI);
            in = new BufferedReader(new InputStreamReader(wsdl.openStream(), Charset.defaultCharset()));

            String inputLine;
            StringBuilder urlContent = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                urlContent.append(inputLine);
                isWsdl2 = urlContent.indexOf(WSDL20_NAMESPACE) > 0;
            }
        } catch (MalformedURLException e) {
            throw new APIManagementException("Malformed URL encountered", e);
        } catch (IOException e) {
            throw new APIManagementException("Error Reading Input from Stream from " + baseURI, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        try {
            if (isWsdl2) {
                readAndValidateWSDL20();
            }
        } catch (org.apache.woden.WSDLException e) {
            throw new APIManagementException("Error while reading WSDL Document from " + baseURI, e);
        }
        return isWsdl2;
    }

    /**
     * Update WSDL 1.0 service definitions saved in registry
     *
     * @param wsdl 	byte array of registry content
	 * @param api 	API object
	 * @return 		the OMElemnt of the new WSDL content
	 * @throws APIManagementException
     */
	public OMElement updateWSDL(byte[] wsdl, API api) throws APIManagementException {

		try {
			// Generate wsdl document from registry data
			WSDLReader wsdlReader = getWsdlFactoryInstance().newWSDLReader();
			// switch off the verbose mode
			wsdlReader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
			wsdlReader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);

			if (wsdlReader instanceof WSDLReaderImpl) {
			    ((WSDLReaderImpl) wsdlReader).setIgnoreSchemaContent(true);
			}

			Definition wsdlDefinition = wsdlReader.readWSDL(null, getSecuredParsedDocumentFromContent(wsdl));

			// Update transports
			setServiceDefinition(wsdlDefinition, api);

			WSDLWriter writer = getWsdlFactoryInstance().newWSDLWriter();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			writer.writeWSDL(wsdlDefinition, byteArrayOutputStream);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( byteArrayOutputStream.toByteArray());
			return APIUtil.buildOMElement(byteArrayInputStream);

		} catch (Exception e) {
			String msg = " Error occurs when updating WSDL ";
			log.error(msg);
			throw new APIManagementException(msg, e);
		}
	}

	/**
	 * Update WSDL 2.0 service definitions saved in registry
	 *
	 * @param wsdl 	byte array of wsdl definition saved in registry
	 * @param api 	API object
	 * @return 		the OMElemnt of the new WSDL content
	 * @throws APIManagementException
	 */
	public OMElement updateWSDL2(byte[] wsdl, API api) throws APIManagementException {

		try {
			// Generate wsdl document from registry data
			DocumentBuilderFactory factory = getSecuredDocumentBuilder();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.apache.woden.WSDLFactory wsdlFactory = org.apache.woden.WSDLFactory.newInstance();
			org.apache.woden.WSDLReader reader = wsdlFactory.newWSDLReader();
			reader.setFeature(org.apache.woden.WSDLReader.FEATURE_VALIDATION, false);
			Document dom = builder.parse(new ByteArrayInputStream(wsdl));
			Element domElement = dom.getDocumentElement();
			WSDLSource wsdlSource = reader.createWSDLSource();
			wsdlSource.setSource(domElement);
			org.apache.woden.wsdl20.Description wsdlDefinition = reader.readWSDL(wsdlSource);

			// Update transports
			setServiceDefinitionForWSDL2(wsdlDefinition, api);

			org.apache.woden.WSDLWriter writer = org.apache.woden.WSDLFactory.newInstance().newWSDLWriter();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			writer.writeWSDL(wsdlDefinition.toElement(), byteArrayOutputStream);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			return APIUtil.buildOMElement(byteArrayInputStream);

		} catch (Exception e) {
			String msg = " Error occurs when updating WSDL ";
			log.error(msg, e);
			throw new APIManagementException(msg, e);
		}
	}

    /**
     * Handles the provided exception occurred during validation and return a validation response or the exception.
     *
     * @param e exception object
     * @return a validation response if the exception contains an ErrorHandler
     * @throws APIManagementException if the exception doesn't contains an ErrorHandler. Throws the same error as 'e'
     */
    private static WSDLValidationResponse handleExceptionDuringValidation(APIManagementException e) throws APIManagementException {
        if (e.getErrorHandler() != null && e.getErrorHandler().getHttpStatusCode() < 500) {
            log.debug("Validation error occurred due to invalid WSDL", e);
            WSDLValidationResponse validationResponse = new WSDLValidationResponse();
            validationResponse.setError(e.getErrorHandler());
            return validationResponse;
        } else {
            throw e;
        }
    }

    @Deprecated
    private static DocumentBuilderFactory getSecuredDocumentBuilder() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            // Skip throwing the error as this exception doesn't break actual DocumentBuilderFactory creation
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                    + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE, e);
        }
        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return dbf;
    }

    @Deprecated
    private org.apache.woden.wsdl20.Description readWSDL2File() throws APIManagementException, WSDLException {
        WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
        reader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);
        try {
            org.apache.woden.WSDLFactory wFactory = org.apache.woden.WSDLFactory.newInstance();
            org.apache.woden.WSDLReader wReader = wFactory.newWSDLReader();
            wReader.setFeature(org.apache.woden.WSDLReader.FEATURE_VALIDATION, true);
            Document document = getSecuredParsedDocumentFromURL(baseURI);
            Element domElement = document.getDocumentElement();
            WSDLSource wsdlSource = wReader.createWSDLSource();
            wsdlSource.setSource(domElement);
            return wReader.readWSDL(wsdlSource);
        } catch (org.apache.woden.WSDLException e) {
            String error = "Error occurred reading wsdl document.";
            log.error(error, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Reading  the WSDL. Base uri is " + baseURI);
        }
        return null;
    }

    private void setServiceDefinitionForWSDL2(org.apache.woden.wsdl20.Description definition, API api)
            throws APIManagementException {
        org.apache.woden.wsdl20.Service[] serviceMap = definition.getServices();
        // URL addressURI;
        try {
            for (org.apache.woden.wsdl20.Service svc : serviceMap) {
                Endpoint[] portMap = svc.getEndpoints();
                for (Endpoint endpoint : portMap) {
                    EndpointElement element = endpoint.toElement();
                    // addressURI = endpoint.getAddress().toURL();
                    // if (addressURI == null) {
                    //    break;
                    // } else {
                    String endpointTransport = determineURLTransport(endpoint.getAddress().getScheme(),
                                                                     api.getTransports());
                    setAddressUrl(element, new URI(APIUtil.getGatewayendpoint(endpointTransport) +
                                                   api.getContext() + '/' + api.getId().getVersion()));
                    //}
                }
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the wsdl address location";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
	 * Create the WSDL definition <javax.wsdl.Definition> from the baseURI of
	 * the WSDL
	 *
	 * @return {@link Definition} - WSDL4j definition constructed form the wsdl
	 *         original baseuri
	 * @throws APIManagementException
	 * @throws WSDLException
	 */
    @Deprecated
	private Definition readWSDLFile() throws APIManagementException, WSDLException {
		WSDLReader reader = getWsdlFactoryInstance().newWSDLReader();
		// switch off the verbose mode
		reader.setFeature(JAVAX_WSDL_VERBOSE_MODE, false);
		reader.setFeature(JAVAX_WSDL_IMPORT_DOCUMENTS, false);

		if (reader instanceof WSDLReaderImpl) {
			((WSDLReaderImpl) reader).setIgnoreSchemaContent(true);
		}

		if (log.isDebugEnabled()) {
			log.debug("Reading  the WSDL. Base uri is " + baseURI);
		}
		return reader.readWSDL(null, getSecuredParsedDocumentFromURL(baseURI));
    }

    /**
     * Returns an "XXE safe" built DOM XML object by reading the content from the provided URL.
     *
     * @param url URL to fetch the content
     * @return an "XXE safe" built DOM XML object by reading the content from the provided URL
     * @throws APIManagementException When error occurred while reading from URL
     */
    @Deprecated
    public Document getSecuredParsedDocumentFromURL(String url) throws APIManagementException {
        URL wsdl;
        String errorMsg = "Error while reading WSDL document";
        InputStream inputStream = null;
        try {
            wsdl = new URL(url);
            DocumentBuilderFactory factory = getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = wsdl.openStream();
            return builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            throw new APIManagementException(errorMsg, e);
        } catch (IOException e) {
            throw new APIManagementException(errorMsg, e);
        } catch (SAXException e) {
            throw new APIManagementException(errorMsg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Returns an "XXE safe" built DOM XML object by reading the content from the byte array.
     *
     * @param content xml content
     * @return an "XXE safe" built DOM XML object by reading the content from the byte array
     * @throws APIManagementException When error occurred while reading from the byte array
     */
    @Deprecated
    private Document getSecuredParsedDocumentFromContent(byte[] content) throws APIManagementException {
        String errorMsg = "Error while reading WSDL document";
        InputStream inputStream = null;
        try {
            DocumentBuilderFactory factory = getSecuredDocumentBuilder();
            DocumentBuilder builder = factory.newDocumentBuilder();
            inputStream = new ByteArrayInputStream(content);
            return builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            throw new APIManagementException(errorMsg, e);
        } catch (IOException e) {
            throw new APIManagementException(errorMsg, e);
        } catch (SAXException e) {
            throw new APIManagementException(errorMsg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Reads baseURI and validate if it is WSDL 2.0 resource.
     *
     * @throws org.apache.woden.WSDLException When error occurred while parsing/validating base URI
     * @throws APIManagementException When error occurred while parsing/validating base URI
     */
    private void readAndValidateWSDL20() throws org.apache.woden.WSDLException, APIManagementException {
        org.apache.woden.WSDLReader wsdlReader20 = org.apache.woden.WSDLFactory.newInstance().newWSDLReader();
        Document document = getSecuredParsedDocumentFromURL(baseURI);
        Element domElement = document.getDocumentElement();
        WSDLSource wsdlSource = wsdlReader20.createWSDLSource();
        wsdlSource.setSource(domElement);
        wsdlReader20.readWSDL(wsdlSource);
    }

    /**
     * Reads baseURI and validate if it is WSDL 1.1 resource.
     *
     * @throws WSDLException When error occurred while parsing/validating base URI
     * @throws APIManagementException When error occurred while parsing/validating base URI
     */
    private void readAndValidateWSDL11() throws WSDLException, APIManagementException {
        javax.wsdl.xml.WSDLReader wsdlReader11 = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
        wsdlReader11.readWSDL(null, getSecuredParsedDocumentFromURL(baseURI));
    }

    /**
	 * Clear the actual service Endpoint and use Gateway Endpoint instead of the
	 * actual Endpoint.
	 *
	 * @param definition
	 *            - {@link Definition} - WSDL4j wsdl definition
	 * @throws APIManagementException
	 */

	private void setServiceDefinition(Definition definition, API api) throws APIManagementException {

		Map serviceMap = definition.getAllServices();
		Iterator serviceItr = serviceMap.entrySet().iterator();
		URL addressURI;
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
						addressURI = new URL(getAddressUrl(extensibilityElement));
						String endpointTransport = determineURLTransport(addressURI.getProtocol(), api.getTransports());
						setAddressUrl(extensibilityElement, endpointTransport, api);
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
     * Clear the actual service Endpoint and use Gateway Endpoint instead of the
     * actual Endpoint for the given environment type.
     *
     * @param definition      {@link Definition} - WSDL4j wsdl definition
     * @param api             API object
     * @param environmentName gateway environment name
     * @param environmentType gateway environment type
     * @throws APIManagementException when error occurred getting WSDL address location
     */
    public void setServiceDefinition(Definition definition, API api, String environmentName, String environmentType)
            throws APIManagementException {
        Map serviceMap = definition.getAllServices();
        URL addressURI;
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
                    endpointTransport = null;
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
                    setAddressUrl(extensibilityElement, endpointTransport, api.getContext(), environmentName,
                            environmentType);
                }
            }
        }
    }
	/**
	 * Get the addressURl from the Extensibility element
	 * @param exElement - {@link ExtensibilityElement}
	 * @return {@link String}
	 * @throws APIManagementException
	 */
	private String getAddressUrl(ExtensibilityElement exElement) throws APIManagementException {
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
                throw new APIManagementException(msg);
            }
        } else {
            String msg = "Unsupported WSDL errors!";
            log.error(msg);
            throw new APIManagementException(msg);
        }
    }

	/**
	 * Get the addressURl from the Extensibility element
	 * @param exElement - {@link ExtensibilityElement}
	 * @throws APIManagementException
	 */
	private void setAddressUrl(ExtensibilityElement exElement, String transports, API api) throws APIManagementException {

        if (exElement instanceof SOAP12AddressImpl) {
            ((SOAP12AddressImpl) exElement).setLocationURI(APIUtil.getGatewayendpoint(transports) + api.getContext());
        } else if (exElement instanceof SOAPAddressImpl) {
            ((SOAPAddressImpl) exElement).setLocationURI(APIUtil.getGatewayendpoint(transports) + api.getContext());
        } else if (exElement instanceof HTTPAddressImpl) {
            ((HTTPAddressImpl) exElement).setLocationURI(APIUtil.getGatewayendpoint(transports) + api.getContext());
        } else if (exElement instanceof UnknownExtensibilityElement) {
            Element unknownExtensibilityElement = ((UnknownExtensibilityElement) exElement).getElement();
            if (unknownExtensibilityElement != null) {
                NodeList nodeList = unknownExtensibilityElement.getElementsByTagNameNS(APIConstants.WSDL_NAMESPACE_URI,
                        APIConstants.WSDL_ELEMENT_LOCAL_NAME);
                if (nodeList != null && nodeList.getLength() > 0) {
                    nodeList.item(0).setTextContent(APIUtil.getGatewayendpoint(transports) + api.getContext());
                }
            }
        } else {
			String msg = "Unsupported WSDL errors!";
			log.error(msg);
			throw new APIManagementException(msg);
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
            String environmentName, String environmentType) throws APIManagementException {
        if (exElement instanceof SOAP12AddressImpl) {
            ((SOAP12AddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((SOAP12AddressImpl) exElement).getLocationURI());
            }
        } else if (exElement instanceof SOAPAddressImpl) {
            ((SOAPAddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((SOAPAddressImpl) exElement).getLocationURI());
            }
        } else if (exElement instanceof HTTPAddressImpl) {
            ((HTTPAddressImpl) exElement)
                    .setLocationURI(APIUtil.getGatewayEndpoint(transports, environmentName, environmentType) + context);
            if (log.isDebugEnabled()) {
                log.debug("Gateway endpoint for environment:" + environmentName + " is: "
                        + ((HTTPAddressImpl) exElement).getLocationURI());
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

	private void setAddressUrl(EndpointElement endpoint,URI uri) throws APIManagementException {
        endpoint.setAddress(uri);
    }

    public static String toString(ByteArrayInputStream is) {
        int size = is.available();
        char[] theChars = new char[size];
        byte[] bytes    = new byte[size];

        is.read(bytes, 0, size);
        for (int i = 0; i < size;)
            theChars[i] = (char)(bytes[i++]&0xff);

        return new String(theChars);
    }

    /**
     * Gets WSDL validation response from the WSDL processor
     *
     * @param processor WSDL processor
     * @return WSDL validation response
     * @throws APIMgtWSDLException if error occurred while retrieving WSDL info
     */
    public static WSDLValidationResponse getWsdlValidationResponse(WSDLProcessor processor)
            throws APIMgtWSDLException {
        WSDLValidationResponse wsdlValidationResponse = new WSDLValidationResponse();
        if (processor.hasError()) {
            wsdlValidationResponse.setValid(false);
            wsdlValidationResponse.setError(processor.getError());
        } else {
            wsdlValidationResponse.setValid(true);
            wsdlValidationResponse.setWsdlInfo(processor.getWsdlInfo());
            wsdlValidationResponse.setWsdlProcessor(processor);
        }
        return wsdlValidationResponse;
    }

    private String determineURLTransport(String scheme, String transports) {
        // If transports is defined as "http,https" consider the actual transport
        // protocol of the url, else give priority to the transport defined at API level
        if ("http,https".equals(transports) || "https,http".equals(transports)) {
            if ("http".equals(scheme)) {
                return "http";
            }
            else if (scheme.startsWith("https")) {
                return "https";
            }
        }

        return transports;
    }

}
