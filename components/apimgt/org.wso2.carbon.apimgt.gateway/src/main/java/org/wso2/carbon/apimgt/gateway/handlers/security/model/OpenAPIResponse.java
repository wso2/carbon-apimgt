package org.wso2.carbon.apimgt.gateway.handlers.security.model;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OpenAPIResponse implements Response {

    private static final Log logger = LogFactory.getLog(OpenAPIResponse.class);
    private int status;
    private Optional<String> body;
    private Multimap<String, String> headers = ArrayListMultimap.create();
    private Method method;
    private String path;

    private OpenAPIResponse() {

    }

    public static OpenAPIResponse from(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        OpenAPIResponse openAPIResponse = new OpenAPIResponse();

        Object statusCodeObject = messageContext.getProperty("HTTP_SC");

        int statusCode = 200;

        if (statusCodeObject instanceof String) {
            statusCode = Integer.parseInt(String.valueOf(statusCodeObject));
        } else if (null != statusCodeObject) {
            statusCode = (Integer) statusCodeObject;
        }
        openAPIResponse.status = statusCode;
        openAPIResponse.method = Request.Method.valueOf((String)
                messageContext.getProperty("api.ut.HTTP_METHOD"));
        openAPIResponse.path = (String) messageContext.getProperty("REST_SUB_REQUEST_PATH");
        Map<String, String> transportHeaders = (Map<String, String>)
                (axis2MessageContext.getProperty("TRANSPORT_HEADERS"));

        try {
            openAPIResponse.body = Utils.buildMessagePayload(axis2MessageContext, transportHeaders);
        } catch (APIManagementException e) {
            logger.error("Failed to build the message payload");
        }

        Map<String, Collection<String>> headerMap = transportHeaders.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ((String) (entry.getKey())),
                        entry -> Collections.singleton((String) (entry.getValue()))
                                         ));

        for (Map.Entry<String, Collection<String>> header : headerMap.entrySet()) {
            openAPIResponse.headers.put(header.getKey(), header.getValue().iterator().next());
        }

        return openAPIResponse;

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

        return body;
    }

    @NotNull
    @Override
    public Collection<String> getHeaderValues(String s) {

        return Utils.getFromMapOrEmptyList(headers.asMap(), s);
    }

    public Method getMethod() {

        return method;
    }

    public String getPath() {

        return path;
    }
}
