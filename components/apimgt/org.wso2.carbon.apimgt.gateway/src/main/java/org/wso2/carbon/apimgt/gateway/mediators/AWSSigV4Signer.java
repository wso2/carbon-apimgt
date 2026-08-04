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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
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
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.AWSUtil;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

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
    private String roleArn;
    private String roleRegion;
    private String roleExternalId;
    private String authType;

    // Built once in init() whenever temporary credentials are involved (environment mode and/or STS
    // AssumeRole). The base identity is the stored static keys or the runtime chain (EKS IRSA / EC2
    // IMDS / env variables); when a role is configured it is wrapped with an STS AssumeRole provider.
    // The SDK providers cache and auto-refresh the temporary credentials internally, so this must never
    // be rebuilt per request. Null for the stored-credentials-without-role case (signs directly).
    private volatile AwsCredentialsProvider credentialsProvider;
    // Base provider used to sign the STS AssumeRole call. When a role is assumed it is a distinct object
    // from credentialsProvider (which is the STS provider) and must be closed separately in destroy();
    // when no role is assumed it IS credentialsProvider, so it is closed only once.
    private AwsCredentialsProvider baseCredentialsProvider;
    private StsClient stsClient;
    private static final String AWS_STS_SESSION_NAME = "apim-bedrock-session";
    // Request-scoped carriers for the credential-failure values, set on the message context.
    private static final String AWS_CREDENTIAL_ERROR_CODE = "AWS_CREDENTIAL_ERROR_CODE";
    private static final String AWS_CREDENTIAL_ERROR_MESSAGE = "AWS_CREDENTIAL_ERROR_MESSAGE";
    private static final String AWS_CREDENTIAL_ERROR_DETAIL = "AWS_CREDENTIAL_ERROR_DETAIL";

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
            Map<String, String> headers;
            if (credentialsProvider != null) {
                // A cached SDK provider supplies the credentials for every case involving temporary
                // credentials: environment mode (EC2 instance profile / EKS IRSA) and any mode that
                // assumes a role (STS AssumeRole). The provider caches and auto-refreshes internally, so
                // no STS call is made per request. Temporary credentials carry a session token, which
                // must be included in the signature.
                AwsCredentials credentials = resolveProviderCredentials(messageContext);
                if (credentials == null) {
                    // Credentials could not be resolved (the cause is already logged). Stop here so the
                    // request is never forwarded to the backend unsigned.
                    return GatewayUtils.handleAWSAuthFailure(messageContext,
                            failureProperty(messageContext, AWS_CREDENTIAL_ERROR_CODE),
                            failureProperty(messageContext, AWS_CREDENTIAL_ERROR_MESSAGE),
                            failureProperty(messageContext, AWS_CREDENTIAL_ERROR_DETAIL));
                }
                String sessionToken = credentials instanceof AwsSessionCredentials
                        ? ((AwsSessionCredentials) credentials).sessionToken() : null;
                headers = AWSUtil.generateAWSSignature(uri.getHost(), httpMethod.toUpperCase(), service,
                        encodePathTrimSlashes(backendRequestResource), getQueryString(path), payload,
                        credentials.accessKeyId(), credentials.secretAccessKey(), region, sessionToken,
                        new HashMap<>());
            } else {
                // Stored credentials without role assumption: sign directly with the static keys.
                headers = AWSUtil.generateAWSSignature(uri.getHost(), httpMethod.toUpperCase(), service,
                        encodePathTrimSlashes(backendRequestResource), getQueryString(path), payload, accessKey,
                        secretKey, region, null, new HashMap<>());
            }
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

    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public String getRoleRegion() {
        return roleRegion;
    }

    public void setRoleRegion(String roleRegion) {
        this.roleRegion = roleRegion;
    }

    public String getRoleExternalId() {
        return roleExternalId;
    }

    public void setRoleExternalId(String roleExternalId) {
        this.roleExternalId = roleExternalId;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * Whether this signer resolves credentials from the runtime environment (EC2 instance profile /
     * EKS IRSA) instead of using the stored access/secret keys.
     *
     * @return {@code true} if the configured auth type is "environment".
     */
    private boolean isEnvironmentMode() {
        return APIConstants.ENDPOINT_SECURITY_AWS_AUTH_TYPE_ENVIRONMENT.equalsIgnoreCase(authType);
    }

    /**
     * Resolves credentials from the cached provider (environment mode and/or STS AssumeRole). The
     * provider caches and auto-refreshes, so this is cheap per request.
     *
     * @return the resolved AWS credentials (may be session credentials with a token), or {@code null}
     * when they could not be resolved. The cause is logged here; the caller turns a {@code null} into
     * an error response.
     */
    private AwsCredentials resolveProviderCredentials(MessageContext messageContext) {
        try {
            return credentialsProvider.resolveCredentials();
        } catch (SdkException e) {
            // SdkException is the common root of both SdkClientException (no credentials found) and
            // service exceptions such as StsException/AwsServiceException (AssumeRole denied, wrong
            // trust policy, invalid role ARN), so both are reported with the same actionable message.
            //
            // Neither the exception nor its message is logged: the AWS text quotes the account id, the
            // IAM user, the role ARN and the request id, and the stack trace header repeats it. Only the
            // structured, non-identifying summary is recorded.
            // Stored on the message context, not fields: this mediator instance is shared by every
            // concurrent request, so fields would be raced between them.
            setAwsFailureProperties(messageContext, e);
            log.error("Unable to resolve AWS credentials for signing. In environment mode, verify the gateway "
                    + "has an attached IAM role (EC2 instance profile / EKS IRSA); when assuming a role, verify "
                    + "the role ARN, region, external ID and trust policy. Cause: "
                    + failureProperty(messageContext, AWS_CREDENTIAL_ERROR_MESSAGE) + " ("
                    + failureProperty(messageContext, AWS_CREDENTIAL_ERROR_DETAIL) + ")");
            return null;
        }
    }

    /**
     * Records the AWS failure on the message context so the response reports the backend's own error
     * rather than a gateway-specific one.
     *
     * <p>Only AWS's status, error code and service name are used. Its message is never recorded: that
     * text quotes the account id, the IAM user, the role ARN and the request id.</p>
     *
     * @param messageContext the message context of the current request.
     * @param e              the AWS failure.
     */
    private void setAwsFailureProperties(MessageContext messageContext, SdkException e) {
        if (e instanceof AwsServiceException) {
            AwsServiceException serviceException = (AwsServiceException) e;
            AwsErrorDetails errorDetails = serviceException.awsErrorDetails();
            if (errorDetails != null && StringUtils.isNotBlank(errorDetails.errorCode())) {
                // AWS answered: report its own status and error code. The raw AWS message is excluded
                // because it quotes the account id, the IAM user, the role ARN and the request id.
                messageContext.setProperty(AWS_CREDENTIAL_ERROR_CODE, String.valueOf(serviceException.statusCode()));
                messageContext.setProperty(AWS_CREDENTIAL_ERROR_MESSAGE, errorDetails.errorCode());
                messageContext.setProperty(AWS_CREDENTIAL_ERROR_DETAIL,
                        errorDetails.serviceName() + ", HTTP " + serviceException.statusCode());
                return;
            }
        }
        // No AWS response at all - the credential chain found nothing, so there is no AWS status or
        // error code to report. Fall back to the SDK exception type.
        String failureType = e.getClass().getSimpleName();
        messageContext.setProperty(AWS_CREDENTIAL_ERROR_CODE, failureType);
        messageContext.setProperty(AWS_CREDENTIAL_ERROR_MESSAGE, failureType);
        messageContext.setProperty(AWS_CREDENTIAL_ERROR_DETAIL, "No AWS credentials could be resolved");
    }

    /**
     * Reads a failure property set by {@link #setAwsFailureProperties}.
     *
     * @param messageContext the message context of the current request.
     * @param property       the property name.
     * @return the value, or an empty string when absent.
     */
    private static String failureProperty(MessageContext messageContext, String property) {
        Object value = messageContext.getProperty(property);
        return value != null ? value.toString() : "";
    }


    /**
     * Builds the cached credentials provider used whenever temporary credentials are involved. The base
     * provider is either the stored static keys (stored mode) or the AWS SDK default provider chain
     * (environment mode: environment variables, the EKS web-identity token (IRSA), ECS container
     * credentials, or EC2 instance metadata). When a role ARN is configured, the base provider is
     * wrapped with an STS AssumeRole provider that caches and auto-refreshes the assumed credentials.
     *
     * @param environmentMode {@code true} to resolve base credentials from the runtime; {@code false}
     *                        to use the stored access/secret keys.
     * @return the credentials provider to use for signing.
     */
    private AwsCredentialsProvider buildCredentialsProvider(boolean environmentMode) {
        // Use builder().build() (not the shared create() singleton) so this mediator owns an independent
        // provider instance that is safe to close in destroy() without affecting the rest of the JVM.
        AwsCredentialsProvider base = environmentMode
                ? DefaultCredentialsProvider.builder().build()
                : StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        this.baseCredentialsProvider = base;
        if (StringUtils.isBlank(roleArn)) {
            return base;
        }
        String stsRegion = StringUtils.isNotBlank(roleRegion) ? roleRegion : region;
        stsClient = StsClient.builder()
                // Set the HTTP client explicitly (ApacheHttpClient, shipped in the AWS SDK orbit bundle)
                // so the SDK does not discover one via ServiceLoader, which fails across OSGi bundle
                // boundaries with "Unable to load an HTTP implementation from any provider in the chain".
                .httpClient(ApacheHttpClient.builder().build())
                .region(Region.of(stsRegion))
                .credentialsProvider(base)
                .build();
        AssumeRoleRequest.Builder assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName(AWS_STS_SESSION_NAME);
        if (StringUtils.isNotBlank(roleExternalId)) {
            assumeRoleRequest.externalId(roleExternalId);
        }
        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(assumeRoleRequest.build())
                .build();
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
        boolean environmentMode = isEnvironmentMode();
        if (StringUtils.isEmpty(region) || StringUtils.isEmpty(service) || StringUtils.isEmpty(endpoint)) {
            throw new SynapseException("AWSSigV4Signer mediator is not properly configured. " +
                    "Region, Service and Endpoint are required.");
        }
        // Access/Secret keys are only required for stored-credentials mode. In environment mode the
        // credentials are resolved from the runtime (EC2 instance profile / EKS IRSA).
        if (!environmentMode && (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey))) {
            throw new SynapseException("AWSSigV4Signer mediator is not properly configured. " +
                    "Access Key and Secret Key are required for stored-credentials mode.");
        }
        if (StringUtils.isNotBlank(roleArn) != StringUtils.isNotBlank(roleRegion)) {
            throw new SynapseException("AWSSigV4Signer mediator is not properly configured. " +
                    "Role ARN and Role Region must be provided together to assume a role.");
        }
        // Build a cached SDK provider whenever temporary credentials are involved: environment mode, or
        // any mode that assumes a role. Stored credentials without a role sign directly and need no
        // provider.
        if (environmentMode || StringUtils.isNotBlank(roleArn)) {
            this.credentialsProvider = buildCredentialsProvider(environmentMode);
        }
    }

    @Override
    public void destroy() {
        // Close in dependency order: the STS AssumeRole provider first (it uses stsClient to refresh),
        // then the STS client, then the base provider. The base is closed only when it is a distinct
        // object from credentialsProvider (i.e. a role is assumed); without a role it IS
        // credentialsProvider and was already closed above, so we avoid a double close.
        closeQuietly(credentialsProvider);
        if (stsClient != null) {
            stsClient.close();
        }
        if (baseCredentialsProvider != credentialsProvider) {
            closeQuietly(baseCredentialsProvider);
        }
    }

    private void closeQuietly(AwsCredentialsProvider provider) {
        if (provider instanceof AutoCloseable) {
            try {
                ((AutoCloseable) provider).close();
            } catch (Exception e) {
                log.warn("Error while closing the AWS credentials provider", e);
            }
        }
    }

    @Override
    public boolean isContentAware() {
        return true;
    }
}
