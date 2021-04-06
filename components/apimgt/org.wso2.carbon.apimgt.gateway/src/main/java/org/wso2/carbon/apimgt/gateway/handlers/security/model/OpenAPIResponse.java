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
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.utils.SchemaValidationUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Response Model class for OpenAPI
 */
public class OpenAPIResponse implements Response {

    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private int status;
    private Optional<String> responseBody;
    private Multimap<String, String> headers = ArrayListMultimap.create();
    private Method method;
    private String path;

    /**
     * Build OAI Response from messageContext.
     *
     * @param messageContext Synapse message context.
     * @return OAI Response.
     */
    public OpenAPIResponse(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();

        Object statusCodeObject = axis2MessageContext.getProperty(APIMgtGatewayConstants.HTTP_SC);

        int statusCode = 0;

        if (statusCodeObject instanceof String) {
            statusCode = Integer.parseInt(String.valueOf(statusCodeObject));
        } else if (null != statusCodeObject) {
            statusCode = (Integer) statusCodeObject;
        }
        //Setting HTTP status, method and path
        status = statusCode;
        method = Request.Method.valueOf((String)
                messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
        path = SchemaValidationUtils.getRestSubRequestPath(
                messageContext.getProperty(REST_SUB_REQUEST_PATH).toString());
        Map<String, String> transportHeaders = (Map<String, String>)
                (axis2MessageContext.getProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS));

        //Setting response body
        responseBody = SchemaValidationUtils.getMessageContent(messageContext);

        Map<String, Collection<String>> headerMap = transportHeaders.entrySet()
                .stream().collect(Collectors
                        .toMap(Map.Entry::getKey, entry -> Collections.singleton(entry.getValue())));

        //Setting response headers
        for (Map.Entry<String, Collection<String>> header : headerMap.entrySet()) {
            headers.put(header.getKey(), header.getValue().iterator().next());
        }
    }

    @Override
    public int getStatus() {

        return status;
    }

    /**
     * @deprecated
     */
    @NotNull
    @Override
    public Optional<String> getBody() {

        return responseBody;
    }

    @NotNull
    @Override
    public Collection<String> getHeaderValues(String s) {

        return SchemaValidationUtils.getFromMapOrEmptyList(headers.asMap(), s);
    }

    public Method getMethod() {

        return method;
    }

    public String getPath() {

        return path;
    }
}
