/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers.common;

import com.atlassian.oai.validator.model.Headers;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class APIMgtLatencyStatsHandler extends AbstractHandler {
    private static final Log log = LogFactory.getLog(APIMgtLatencyStatsHandler.class);
    private OpenAPI openAPI;
    private String apiUUID;
    private String swagger;

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    public boolean handleRequest(MessageContext messageContext) {

        if (TelemetryUtil.telemetryEnabled()) {
            TelemetrySpan responseLatencySpan =
                    (TelemetrySpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TelemetryTracer tracer = ServiceReferenceHolder.getInstance().getTelemetryTracer();
            TelemetrySpan span = TelemetryUtil.startSpan(APIMgtGatewayConstants.RESOURCE_SPAN, responseLatencySpan,
                    tracer);
            messageContext.setProperty(APIMgtGatewayConstants.RESOURCE_SPAN, span);
        } else if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingTracer tracer = Util.getGlobalTracer();
            TracingSpan span = Util.startSpan(APIMgtGatewayConstants.RESOURCE_SPAN, responseLatencySpan, tracer);
            messageContext.setProperty(APIMgtGatewayConstants.RESOURCE_SPAN, span);
        }
        messageContext.setProperty(APIMgtGatewayConstants.API_UUID_PROPERTY, apiUUID);
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        if (messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, Long.toString(System
                    .currentTimeMillis()));
            String method = (String) (axis2MsgContext.getProperty(
                    Constants.Configuration.HTTP_METHOD));
            messageContext.setProperty(APIMgtGatewayConstants.HTTP_METHOD, method);
        }
        /*
        * The axis2 message context is set here so that the method level logging can access the transport headers
        */
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MsgContext);
        long currentTime = System.currentTimeMillis();
        messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
        setSwaggerToMessageContext(messageContext);
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        /*
         * The axis2 message context is set here so that the method level logging can access the
         * transport headers
         */
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MC);
        if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME, System.currentTimeMillis());
            if (APIUtil.isAnalyticsEnabled()
                    && messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME) != null) {
                long executionStartTime = Long.parseLong(
                        (String) messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME));
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_LATENCY,
                        System.currentTimeMillis() - executionStartTime);
            }
        }
        return true;
    }

    private void setSwaggerToMessageContext(MessageContext messageContext) {
        // Read OpenAPI from local entry
        if (openAPI == null && apiUUID != null) {
            synchronized (this) {
                if (openAPI == null) {
                    long startTime = System.currentTimeMillis();
                    Entry localEntryObj = (Entry) messageContext.getConfiguration().getLocalRegistry().get(apiUUID);
                    if (localEntryObj != null) {
                        swagger = localEntryObj.getValue().toString();
                        OpenAPIParser parser = new OpenAPIParser();
                        ParseOptions parseOptions = new ParseOptions();
                        parseOptions.setResolve(true);
                        parseOptions.setResolveFully(true);
                        parseOptions.setResolveCombinators(false);
                        openAPI = parser.readContents(swagger, null, parseOptions).getOpenAPI();
                        // HTTP headers should be case insensitive as for HTTP 1.1 RFC
                        // Thus converting headers to lowercase for schema validation.
                        convertHeadersToLowercase(openAPI);
                    }
                    long endTime = System.currentTimeMillis();
                    if (log.isDebugEnabled()) {
                        log.debug("Time to parse the swagger(ms) : " + (endTime - startTime));
                    }
                }
            }
        }
        // Add OpenAPI to message context
        messageContext.setProperty(APIMgtGatewayConstants.OPEN_API_OBJECT, openAPI);
        // Add swagger String to message context
        messageContext.setProperty(APIMgtGatewayConstants.OPEN_API_STRING, swagger);
    }

    /**
     * This method iterate through openAPI paths and convert header parameter names to lowercase for each operation
     *
     * @param openAPI openAPI object
     */
    private void convertHeadersToLowercase(OpenAPI openAPI) {

        // Iterate each path
        for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
            // Iterate each operation
            PathItem pathItem = entry.getValue();
            if (pathItem != null) {
                List<Operation> operations = pathItem.readOperations();
                for (Operation operation : operations) {
                    if (operation.getParameters() != null) {
                        operation.setParameters(getLowercaseHeaderParameters(operation.getParameters()));
                    }
                }
            }
        }
    }

    /**
     * This method read the parameter list and convert header parameter's name to lowercase
     * @param parameters list of params
     * @return
     */
    @NotNull
    private List<Parameter> getLowercaseHeaderParameters(List<Parameter> parameters) {

        List<Parameter> headerParameters = parameters.stream()
                .filter(param -> param.getIn().equalsIgnoreCase("header"))
                .filter(param -> !param.getName().equalsIgnoreCase(Headers.CONTENT_TYPE)) // Ignore content-type header
                .collect(Collectors.toList());
        List<Parameter> modifiedHeaderParameters = headerParameters.stream()
                .map(APIMgtLatencyStatsHandler::replaceLowerCaseHeaderName).collect(Collectors.toList());
        List<Parameter> nonHeaderParameters = parameters.stream()
                .filter(param -> !(param instanceof HeaderParameter)).collect(Collectors.toList());
        nonHeaderParameters.addAll(modifiedHeaderParameters);
        return nonHeaderParameters;
    }

    /**
     * This method convert parameter name to lowercase.
     * @param parameter param
     * @return
     */
    private static Parameter replaceLowerCaseHeaderName(Parameter parameter) {

        parameter.setName(parameter.getName().toLowerCase(Locale.ROOT));
        return parameter;
    }

}
