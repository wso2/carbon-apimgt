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
import org.wso2.carbon.apimgt.core.exception.APIMgtWSDLException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WSDLOperation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
     * This will always include "POST /" resource
     *
     * @param operations a Set of {@link WSDLOperation} objects
     * @return Map of URI Templates
     */
    public static Map<String, UriTemplate> getUriTemplatesForWSDLOperations(Set<WSDLOperation> operations) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();

        //add default "POST /" operation
        UriTemplate.UriTemplateBuilder builderForPOSTRootCtx = new UriTemplate.UriTemplateBuilder();
        builderForPOSTRootCtx.uriTemplate("/");
        builderForPOSTRootCtx.httpVerb("POST");
        builderForPOSTRootCtx.policy(APIUtils.getDefaultAPIPolicy());
        builderForPOSTRootCtx.templateId(APIUtils.generateOperationIdFromPath("/", "POST"));
        uriTemplateMap.put(builderForPOSTRootCtx.getTemplateId(), builderForPOSTRootCtx.build());

        //add URI templates for operations
        if (operations != null && operations.size() > 0) {
            for (WSDLOperation operation : operations) {
                UriTemplate.UriTemplateBuilder builder = new UriTemplate.UriTemplateBuilder();
                builder.uriTemplate(operation.getURI().startsWith("/") ? operation.getURI() : "/" + operation.getURI());
                builder.httpVerb(operation.getVerb());
                builder.policy(APIUtils.getDefaultAPIPolicy());
                builder.templateId(APIUtils.generateOperationIdFromPath(builder.getUriTemplate(), operation.getVerb()));
                uriTemplateMap.put(builder.getTemplateId(), builder.build());
            }
        }
        return uriTemplateMap;
    }
}
