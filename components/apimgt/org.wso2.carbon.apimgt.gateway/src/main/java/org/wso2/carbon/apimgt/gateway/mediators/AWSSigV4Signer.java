/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.Constants;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.AWSUtil;

/**
 * This mediator is used to sign requests with AWS Signature Version 4.
 * It generates the required headers and adds them to the request.
 */
public class AWSSigV4Signer extends AbstractMediator implements ManagedLifecycle {
    private String accessKey;
    private String secretKey;
    private String region;
    private String service;
    private String endpoint;

    @Override
    public boolean mediate(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2Ctx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String payload = "";
        try {
            RelayUtils.buildMessage(axis2Ctx);
            String contentType = (String) axis2Ctx.getProperty(Constants.Configuration.CONTENT_TYPE);
            String httpMethod = (String) axis2Ctx.getProperty(Constants.Configuration.HTTP_METHOD);
            if (!APIConstants.HTTP_GET.equals(httpMethod.toUpperCase(Locale.getDefault()))) {
                if (APIConstants.APPLICATION_JSON_MEDIA_TYPE.equals(contentType)) {
                    try (InputStream payloadInputStream = JsonUtil.getJsonPayload(axis2Ctx)) {
                        if (payloadInputStream != null) {
                            payload = IOUtils.toString(payloadInputStream);
                        } else {
                            throw new SynapseException(
                                    "Payload is null or empty. Cannot sign the request with AWS SigV4.");
                        }
                    }
                } else {
                    messageContext.getEnvelope().buildWithAttachments();
                    SOAPBody body = messageContext.getEnvelope().getBody();
                    OMElement payLoadOmelement = body.getFirstElement();
                    if (payLoadOmelement != null) {
                        payload = payLoadOmelement.toString();
                    }
                }
            }
            String path = (String) axis2Ctx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            URI uri = new URI(endpoint);
            String backendRequestResource = (String) axis2Ctx.getProperty(NhttpConstants.REST_URL_POSTFIX);
            Map<String, String> incomingHeaders = new HashMap<>();
            if (axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS) instanceof Map) {
                incomingHeaders = (Map<String, String>) axis2Ctx.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            }
            Map<String, String> headers = AWSUtil.generateAWSSignature(uri.getHost(), httpMethod.toUpperCase(), service,
                    encodePathTrimSlashes(backendRequestResource), getQueryString(path), payload, accessKey, secretKey,
                    region, null, new HashMap<>());
            incomingHeaders.putAll(headers);
            axis2Ctx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, incomingHeaders);
            return true;
        } catch (IOException | XMLStreamException | APIManagementException | URISyntaxException e) {
            throw new SynapseException("Error while signing the request with AWS SigV4", e);
        }
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private static String getQueryString(String request) {
        String queryString = null;
        if (request != null && request.contains("?")) {
            int index = request.indexOf("?");
            queryString = request.substring(index + 1);
        }
        return queryString;
    }

    private static String encodePathTrimSlashes(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        return Arrays.stream(path.split("/")).map(fragment -> URLEncoder.encode(fragment, StandardCharsets.UTF_8))
                .collect(Collectors.joining("/"));
    }

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey) || StringUtils.isEmpty(region) ||
                StringUtils.isEmpty(service) || StringUtils.isEmpty(endpoint)) {
            throw new SynapseException("AWSSigV4Signer mediator is not properly configured. " +
                    "Access Key, Secret Key, Region, Service and Endpoint are required.");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isContentAware() {
        return true;
    }
}
