package org.wso2.carbon.apimgt.gateway.handlers.security.model;

import com.atlassian.oai.validator.model.Request;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class OpenAPIRequest implements Request {

    public static final String ACCEPT = "Accept";
    private static final Log logger = LogFactory.getLog(OpenAPIRequest.class);
    private Request.Method method;
    private String path;
    private Multimap<String, String> headers = ArrayListMultimap.create();
    private Map<String, Collection<String>> queryParams;
    private Optional<String> requestBody;

    private OpenAPIRequest() {

    }

    /**
     * Build OAI Request from RequestValidation model.
     *
     * @param messageContext request validation model.
     * @return OAI Request.
     */
    public static OpenAPIRequest from(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)
                messageContext).getAxis2MessageContext();
        OpenAPIRequest openAPIRequest = new OpenAPIRequest();
        openAPIRequest.method = Request.Method.valueOf((String) messageContext.getProperty("api.ut.HTTP_METHOD"));

        openAPIRequest.path = (String) messageContext.getProperty("API_ELECTED_RESOURCE");
        Map<String, String> transportHeaders = (Map<String, String>)
                (axis2MessageContext.getProperty("TRANSPORT_HEADERS"));
        try {
            openAPIRequest.requestBody = Utils.buildMessagePayload(axis2MessageContext, transportHeaders);
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
            openAPIRequest.headers.put(header.getKey(), header.getValue().iterator().next());
        }
        String apiResource = messageContext.getProperty("api.ut.resource").toString();
        try {
            openAPIRequest.queryParams = getQueryParams(apiResource, openAPIRequest.path);
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to decode query string");
        }
        return openAPIRequest;

    }

    private static Map<String, Collection<String>> getQueryParams(String apiResource, String path)
            throws UnsupportedEncodingException {

        Map<String, String> queryParams = new HashMap<>();
        if (!apiResource.equals(path)) {
            String queryString = apiResource.replace(path + "?", "");
            String[] query = queryString.split("&");
            for (String keyValue : query) {
                int idx = keyValue.indexOf("=");
                queryParams.put(
                        URLDecoder.decode(keyValue.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(keyValue.substring(idx + 1), "UTF-8"));
            }
        }
        return queryParams.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> Collections.singleton(entry.getValue())));
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
        return Utils.getFromMapOrEmptyList(this.queryParams, s);
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
        return Utils.getFromMapOrEmptyList(this.headers.asMap(), s);
    }

}
