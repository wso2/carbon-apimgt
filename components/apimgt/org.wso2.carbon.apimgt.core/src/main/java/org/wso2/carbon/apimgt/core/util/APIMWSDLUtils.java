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

import io.swagger.models.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.URITemplateParam;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WSDLOperation;
import org.wso2.carbon.apimgt.core.models.WSDLOperationParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains utility functions which are required for WSDL file processing.
 */
public class APIMWSDLUtils {
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final Logger log = LoggerFactory.getLogger(APIMWSDLUtils.class);
    /**
     * Retrieves the WSDL located in the provided URI ({@code wsdlUrl}
     *
     * @param wsdlUrl URL of the WSDL file
     * @return Content bytes of the WSDL file
     * @throws APIMgtWSDLException If an error occurred while retrieving the WSDL file
     */
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
     * Prioritizes the first https endpoint. If https endpoints are not available returns the first endpoint in the
     * {@code endpoints}
     *
     * @param endpoints Endpoint list
     * @return First https endpoint in {@code endpoint} or first available endpoint
     * @throws MalformedURLException If a URL in {@code endpoints} is invalid
     */
    public static String getSelectedEndpoint(List<String> endpoints) throws MalformedURLException {
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

    /**
     * Generates URI templates to be assigned to an API from a set of operations extracted from WSDL.
     *
     * @param operations a Set of {@link WSDLOperation} objects
     * @return Map of URI Templates
     */
    public static Map<String, UriTemplate> getUriTemplatesForWSDLOperations(Set<WSDLOperation> operations,
            boolean isHttpBinding) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();

        //add default "POST /" operation if no http binding methods required or operations are not provided
        if (!isHttpBinding || operations == null || operations.isEmpty()) {
            if (log.isDebugEnabled()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Adding the POST / operation. ");
                stringBuilder.append("isHttpBinding: ").append(isHttpBinding).append(". ");
                stringBuilder.append("operations are null?: ").append(operations == null).append(". ");
                if (operations != null) {
                    stringBuilder.append("operations are empty?: ").append(operations.isEmpty()).append(". ");
                }
                log.debug(stringBuilder.toString());
            }
            UriTemplate.UriTemplateBuilder builderForPOSTRootCtx = new UriTemplate.UriTemplateBuilder();
            builderForPOSTRootCtx.uriTemplate("/");
            builderForPOSTRootCtx.httpVerb("POST");
            builderForPOSTRootCtx.policy(APIUtils.getDefaultAPIPolicy());
            builderForPOSTRootCtx.templateId(APIUtils.generateOperationIdFromPath("/", "POST"));
            uriTemplateMap.put(builderForPOSTRootCtx.getTemplateId(), builderForPOSTRootCtx.build());
            return uriTemplateMap;
        }

        //add URI templates for operations
        for (WSDLOperation operation : operations) {
            if (log.isDebugEnabled()) {
                log.debug("Adding URI template for WSDL operation: " + operation.getVerb() + ", " + operation.getURI());
            }
            UriTemplate.UriTemplateBuilder builder = new UriTemplate.UriTemplateBuilder();
            builder.uriTemplate(operation.getURI().startsWith("/") ? operation.getURI() : "/" + operation.getURI());
            builder.httpVerb(operation.getVerb());
            builder.policy(APIUtils.getDefaultAPIPolicy());
            builder.templateId(APIUtils.generateOperationIdFromPath(builder.getUriTemplate(), operation.getVerb()));
            builder.contentType(operation.getContentType());

            List<URITemplateParam> uriTemplateParams = getUriTemplatesParamsForWSDLOperationParams(
                    operation.getParameters());
            builder.parameters(uriTemplateParams);

            uriTemplateMap.put(builder.getTemplateId(), builder.build());
        }

        return uriTemplateMap;
    }

    /**
     * Returns URI template params from a set of operation parameters extracted from WSDL.
     *
     * @param params a List of {@link WSDLOperationParam} objects
     * @return A List of URI Template Params
     */
    private static List<URITemplateParam> getUriTemplatesParamsForWSDLOperationParams(
            List<WSDLOperationParam> params) {
        List<URITemplateParam> uriTemplateParams = new ArrayList<>();
        if (params != null) {
            for (WSDLOperationParam wsdlParam : params) {
                uriTemplateParams.add(new URITemplateParam(wsdlParam));
            }
        }
        return uriTemplateParams;
    }

    /**
     * Replace "(" with "{" and ")" with "}" in the provided URL String.
     * 
     * @param url URL string
     * @return parentheses replaced with curly brackets in the given url.
     */
    public static String replaceParentheses(String url) {
        if (url != null) {
            return url.replace("(", "{").replace(")", "}");
        }
        return "";
    }

    /**
     * Returns whether an HTTP operation with provided verb can contain a body or not.
     *
     * @param verb HTTP verb
     * @return whether an HTTP operation with provided verb can contain a body or not.
     */
    public static boolean canContainBody(String verb) {
        return verb.equalsIgnoreCase(HttpMethod.POST.toString()) || verb.equalsIgnoreCase(HttpMethod.PUT.toString())
                || verb.equalsIgnoreCase(HttpMethod.PATCH.toString());
    }

    /**
     * Returns whether a body with provided content type can contain form data parameters
     *
     * @param contentType Content type
     * @return whether a body with provided content type can contain form data parameters
     */
    public static boolean hasFormDataParams(String contentType) {
        return APPLICATION_FORM_URLENCODED.equals(contentType) || MULTIPART_FORM_DATA.equals(contentType);
    }
}
