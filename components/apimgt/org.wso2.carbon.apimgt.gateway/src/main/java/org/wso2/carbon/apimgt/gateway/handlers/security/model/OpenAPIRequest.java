/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.model;

import com.atlassian.oai.validator.model.Request;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.utils.SchemaValidationUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Request Model class for OpenAPI
 */
public class OpenAPIRequest implements Request {

    private static final Log logger = LogFactory.getLog(OpenAPIRequest.class);
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private Request.Method method;
    private String path;
    private Multimap<String, String> headers = ArrayListMultimap.create();
    private Map<String, Collection<String>> queryParams;
    private Optional<String> requestBody;
    /**
     * Build OAI Request from Message Context.
     *
     * @param messageContext Synapse message context.
     * @return OAI Request.
     */
    public OpenAPIRequest(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        //set HTTP Method
        method = Request.Method.valueOf((String)
                messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
        //Set Request path
        path = SchemaValidationUtils.getRestSubRequestPath(
                messageContext.getProperty(REST_SUB_REQUEST_PATH).toString());
        String swagger = messageContext.getProperty(APIMgtGatewayConstants.OPEN_API_STRING).toString();
        if (swagger != null) {
            OpenAPIParser openAPIParser = new OpenAPIParser();
            SwaggerParseResult swaggerParseResult =
                    openAPIParser.readContents(swagger, new ArrayList<>(), new ParseOptions());
            OpenAPI openAPI = swaggerParseResult.getOpenAPI();
            validatePath(openAPI);;
        }
        //extract transport headers
        Map<String, String> transportHeaders = (Map<String, String>)
                (axis2MessageContext.getProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS));
        //Set Request body
        requestBody = SchemaValidationUtils.getMessageContent(messageContext);
        Map<String, Collection<String>> headerMap = transportHeaders.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Collections.singleton(entry.getValue())));
        //Set transport headers
        String contentTypeHeader = "content-type";
        for (Map.Entry<String, Collection<String>> header : headerMap.entrySet()) {
            String headerKey = header.getKey();
            String value =  header.getValue().iterator().next();
            headerKey = headerKey.equalsIgnoreCase(contentTypeHeader) ? "Content-Type" : headerKey;
            headers.put(headerKey, value);
        }
        String apiResource = messageContext.getProperty(APIMgtGatewayConstants.RESOURCE).toString();
        //Extracting query params
        try {
            queryParams = SchemaValidationUtils.getQueryParams(apiResource, (String)
                    messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE));
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to decode query string");
        }
    }

    @Nonnull
    @Override
    public String getPath() {

        return this.path;
    }

    @Nonnull
    @Override
    public Method getMethod() {

        return this.method;
    }

    @Nonnull
    @Override
    public Optional<String> getBody() {

        return this.requestBody;
    }

    @Nonnull
    @Override
    public Collection<String> getQueryParameters() {

        if (this.queryParams == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(this.queryParams.keySet());
    }

    @Nonnull
    @Override
    public Collection<String> getQueryParameterValues(String s) {

        if (this.queryParams == null) {
            return Collections.emptyList();
        }
        return SchemaValidationUtils.getFromMapOrEmptyList(this.queryParams, s);
    }

    @Nonnull
    @Override
    public Map<String, Collection<String>> getHeaders() {

        if (this.headers == null) {
            return Collections.emptyMap();
        }
        return headers.asMap();
    }

    @Nonnull
    @Override
    public Collection<String> getHeaderValues(String s) {

        if (this.headers == null) {
            return Collections.emptyList();
        }
        return SchemaValidationUtils.getFromMapOrEmptyList(this.headers.asMap(), s);
    }

    protected void validatePath(OpenAPI openAPI) {

        Paths paths = openAPI.getPaths();
        if (path.equals("/") && !paths.containsKey(path)) {
            if (paths.containsKey("/*")) {
                path = "/*";
            }
        }
    }
}
