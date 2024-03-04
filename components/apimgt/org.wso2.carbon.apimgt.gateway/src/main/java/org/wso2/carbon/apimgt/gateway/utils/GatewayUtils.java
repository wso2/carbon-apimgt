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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.IPRange;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayUtils {

    private static final Log log = LogFactory.getLog(GatewayUtils.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static final String HTTP_SC = "HTTP_SC";
    private static final String HTTP_SC_DESC = "HTTP_SC_DESC";
    private static final Gson gson = new Gson();

    public static boolean isClusteringEnabled() {

        ClusteringAgent agent = ServiceReferenceHolder.getInstance().getServerConfigurationContext().
                getAxisConfiguration().getClusteringAgent();
        if (agent != null) {
            return true;
        }
        return false;
    }

    public static String getClientIp(org.apache.synapse.MessageContext synCtx) {
        String clientIp;
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String xForwardForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardForHeader)) {
            clientIp = xForwardForHeader;
            int idx = xForwardForHeader.indexOf(',');
            if (idx > -1) {
                clientIp = clientIp.substring(0, idx);
            }
        } else {
            clientIp = (String) axis2MsgContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }
        return clientIp;
    }

    public static <T> Map<String, T> generateMap(Collection<T> list) {

        Map<String, T> map = new HashMap<String, T>();
        for (T el : list) {
            map.put(el.toString(), el);
        }
        return map;
    }

    public static Map<String, Set<IPRange>> generateIpRangeMap(List<IPRange> ipRangeList) {

        Map<String, Set<IPRange>> ipRangeMap = new HashMap<>();
        for (IPRange ipRange : ipRangeList) {
            Set<IPRange> tenantWiseIpRangeList;
            if (!ipRangeMap.containsKey(ipRange.getTenantDomain())) {
                tenantWiseIpRangeList = new HashSet<>();
            } else {
                tenantWiseIpRangeList = ipRangeMap.get(ipRange.getTenantDomain());
            }
            if (APIConstants.BLOCK_CONDITION_IP_RANGE.equals(ipRange.getType())) {
                convertIpRangeBigIntValue(ipRange);
            }
            tenantWiseIpRangeList.add(ipRange);
            ipRangeMap.put(ipRange.getTenantDomain(), tenantWiseIpRangeList);
        }
        return ipRangeMap;
    }

    private static void convertIpRangeBigIntValue(IPRange ipRange) {

        ipRange.setStartingIpBigIntValue(APIUtil.ipToBigInteger(ipRange.getStartingIP()));
        ipRange.setEndingIpBigIntValue(APIUtil.ipToBigInteger(ipRange.getEndingIp()));
    }

    /**
     * Extracts the IP from Message Context.
     *
     * @param messageContext Axis2 Message Context.
     * @return IP as a String.
     */
    public static String getIp(MessageContext messageContext) {

        //Set transport headers of the message
        Map<String, String> transportHeaderMap = (Map<String, String>) messageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        // Assigning an Empty String so that when doing comparisons, .equals method can be used without explicitly
        // checking for nullity.
        String remoteIP = "";
        //Check whether headers map is null and x forwarded for header is present
        if (transportHeaderMap != null) {
            remoteIP = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client by looking at x forded for header and  if it's empty get remote address
        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) messageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        return remoteIP;
    }

    /**
     * Can be used to extract Query Params from {@code org.apache.axis2.context.MessageContext}.
     *
     * @param messageContext The Axis2 MessageContext
     * @return A Map with Name Value pairs.
     */
    public static Map<String, String> getQueryParams(MessageContext messageContext) {

        String queryString = (String) messageContext.getProperty(NhttpConstants.REST_URL_POSTFIX);
        if (!StringUtils.isEmpty(queryString)) {
            if (queryString.indexOf("?") > -1) {
                queryString = queryString.substring(queryString.indexOf("?") + 1);
            }
            String[] queryParams = queryString.split("&");
            Map<String, String> queryParamsMap = new HashMap<String, String>();
            String[] queryParamArray;
            String queryParamName, queryParamValue = "";
            for (String queryParam : queryParams) {
                queryParamArray = queryParam.split("=");
                if (queryParamArray.length == 2) {
                    queryParamName = queryParamArray[0];
                    queryParamValue = queryParamArray[1];
                } else {
                    queryParamName = queryParamArray[0];
                }
                queryParamsMap.put(queryParamName, queryParamValue);
            }

            return queryParamsMap;
        }
        return null;
    }

    /**
     * Get the config system registry for tenants
     *
     * @param tenantDomain - The tenant domain
     * @return - A UserRegistry instance for the tenant
     * @throws APIManagementException
     */
    public static UserRegistry getRegistry(String tenantDomain) throws APIManagementException {

        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry registry;
        try {
            registry = RegistryServiceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Failed to get registry instance for the tenant : " + tenantDomain + e.getMessage();
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return registry;
    }

    /**
     * Delete the given registry property from the given tenant registry path
     *
     * @param propertyName property name
     * @param path         resource path
     * @param tenantDomain
     * @throws AxisFault
     */
    public static void deleteRegistryProperty(String propertyName, String path, String tenantDomain)
            throws AxisFault {

        try {
            UserRegistry registry = getRegistry(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            } else {
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            Resource resource = registry.get(path);
            if (resource != null && resource.getProperty(propertyName) != null) {
                resource.removeProperty(propertyName);
                registry.put(resource.getPath(), resource);
                resource.discard();
            }
        } catch (RegistryException | APIManagementException e) {
            String msg = "Failed to delete secure endpoint password alias " + e.getMessage();
            throw new AxisFault(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add/Update the given registry property from the given tenant registry
     * path
     *
     * @param propertyName  property name
     * @param propertyValue property value
     * @param path          resource path
     * @param tenantDomain
     * @throws APIManagementException
     */
    public static void setRegistryProperty(String propertyName, String propertyValue, String path, String tenantDomain)
            throws APIManagementException {

        UserRegistry registry = getRegistry(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }
        try {
            Resource resource = registry.get(path);
            // add or update property
            if (resource.getProperty(propertyName) != null) {
                resource.setProperty(propertyName, propertyValue);
            } else {
                resource.addProperty(propertyName, propertyValue);
            }
            registry.put(resource.getPath(), resource);
            resource.discard();
        } catch (RegistryException e) {
            throw new APIManagementException("Error while reading registry resource " + path + " for tenant " +
                    tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Returns the alias string of API endpoint security password
     *
     * @param apiProviderName
     * @param apiName
     * @param version
     * @return
     */
    public static String getAPIEndpointSecretAlias(String apiProviderName, String apiName, String version) {

        String secureVaultAlias = apiProviderName + "--" + apiName + version;
        return secureVaultAlias;
    }

    /**
     * return existing correlation ID in the message context or set new correlation ID to the message context.
     *
     * @param messageContext synapse message context
     * @return correlation ID
     */
    public static String getAndSetCorrelationID(org.apache.synapse.MessageContext messageContext) {

        Object correlationObj = messageContext.getProperty(APIMgtGatewayConstants.AM_CORRELATION_ID);
        String correlationID;
        if (correlationObj != null) {
            correlationID = (String) correlationObj;
        } else {
            correlationID = UUID.randomUUID().toString();
            messageContext.setProperty(APIMgtGatewayConstants.AM_CORRELATION_ID, correlationID);
            if (log.isDebugEnabled()) {
                log.debug("Setting correlation ID to message context.");
            }
        }
        return correlationID;
    }

    /**
     * This method handles threat violations. If the request propagates a threat, this method generates
     * an custom exception.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @param errorCode      It depends on status of the error message.
     * @param desc           Description of the error message.It describes the vulnerable type and where it happens.
     * @return here return true to continue the sequence. No need to return any value from this method.
     */
    public static boolean handleThreat(org.apache.synapse.MessageContext messageContext,
                                       String errorCode, String desc) {

        messageContext.setProperty(APIMgtGatewayConstants.THREAT_FOUND, true);
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_CODE, Integer.parseInt(errorCode));
        if (messageContext.isResponse()) {
            messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, APIMgtGatewayConstants.BAD_RESPONSE);
            messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, APIMgtGatewayConstants.BAD_RESPONSE);
        } else {
            messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, APIMgtGatewayConstants.BAD_REQUEST);
            messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, APIMgtGatewayConstants.BAD_REQUEST);
        }
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_DESC, desc);
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, desc);
        Mediator sequence = messageContext.getSequence(APIMgtGatewayConstants.THREAT_FAULT);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return false;
        }
        return true;
    }

    /**
     * This method use to clone the InputStream from the the message context. Basically
     * clone the request body.
     *
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return cloned InputStreams.
     * @throws IOException this exception might occurred while cloning the inputStream.
     */
    public static Map<String, InputStream> cloneRequestMessage(org.apache.synapse.MessageContext messageContext)
            throws IOException {

        BufferedInputStream bufferedInputStream = null;
        Map<String, InputStream> inputStreamMap;
        InputStream inputStreamSchema = null;
        InputStream inputStreamXml = null;
        InputStream inputStreamJSON = null;
        InputStream inputStreamOriginal = null;
        int requestBufferSize = 1024;
        org.apache.axis2.context.MessageContext axis2MC;
        Pipe pipe;

        axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Object bufferSize = messageContext.getProperty(ThreatProtectorConstants.REQUEST_BUFFER_SIZE);
        if (bufferSize != null) {
            requestBufferSize = Integer.parseInt(bufferSize.toString());
        }
        pipe = (Pipe) axis2MC.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
        if (pipe != null) {
            bufferedInputStream = new BufferedInputStream(pipe.getInputStream());
        }
        inputStreamMap = new HashMap<>();
        String contentType = axis2MC.getProperty(ThreatProtectorConstants.CONTENT_TYPE).toString();

        if (bufferedInputStream != null) {
            bufferedInputStream.mark(0);
            if (bufferedInputStream.read() != -1) {
                bufferedInputStream.reset();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[requestBufferSize];
                int length;
                while ((length = bufferedInputStream.read(buffer)) > -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                byteArrayOutputStream.flush();
                inputStreamSchema = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                inputStreamXml = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                inputStreamOriginal = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                inputStreamJSON = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            } else {
                String payload;
                if (ThreatProtectorConstants.APPLICATION_JSON.equals(contentType)) {
                    inputStreamJSON = JsonUtil.getJsonPayload(axis2MC);
                } else {
                    payload = axis2MC.getEnvelope().getBody().getFirstElement().toString();
                    inputStreamXml = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                    inputStreamSchema = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        inputStreamMap.put(ThreatProtectorConstants.SCHEMA, inputStreamSchema);
        inputStreamMap.put(ThreatProtectorConstants.XML, inputStreamXml);
        inputStreamMap.put(ThreatProtectorConstants.ORIGINAL, inputStreamOriginal);
        inputStreamMap.put(ThreatProtectorConstants.JSON, inputStreamJSON);
        return inputStreamMap;
    }

    /**
     * This method use to set the originInput stream to the message Context
     *
     * @param inputStreams cloned InputStreams
     * @param axis2MC      axis2 message context
     */
    public static void setOriginalInputStream(Map<String, InputStream> inputStreams,
                                              org.apache.axis2.context.MessageContext axis2MC) {

        InputStream inputStreamOriginal;
        if (inputStreams != null) {
            inputStreamOriginal = inputStreams.get(ThreatProtectorConstants.ORIGINAL);
            if (inputStreamOriginal != null) {
                BufferedInputStream bufferedInputStreamOriginal = new BufferedInputStream(inputStreamOriginal);
                axis2MC.setProperty(PassThroughConstants.BUFFERED_INPUT_STREAM, bufferedInputStreamOriginal);
            }
        }
    }

    public static String extractResource(org.apache.synapse.MessageContext mc) {

        Pattern resourcePattern = Pattern.compile("^/.+?/.+?([/?].+)$");
        String resource = "/";
        Matcher matcher = resourcePattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

    public static String getHostName(org.apache.synapse.MessageContext messageContext) {

        String hostname = System.getProperty("datacenterId");
        if (hostname == null) {
            hostname = (String) messageContext.getProperty(APIMgtGatewayConstants.HOST_NAME);
        }
        return hostname;
    }

    public static String getQualifiedApiName(String apiName, String version) {

        return apiName + ":v" + version;
    }

    public static String getQualifiedDefaultApiName(String apiName) {

        return apiName;
    }

    /**
     * This method extracts the endpoint address base path if query parameters are contained in endpoint
     *
     * @param mc The message context
     * @return The endpoint address base path
     */
    public static String extractAddressBasePath(org.apache.synapse.MessageContext mc) {

        String endpointAddress = (String) mc.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpointAddress == null) {
            endpointAddress = APIMgtGatewayConstants.DUMMY_ENDPOINT_ADDRESS;
        }
        if (endpointAddress.contains("?")) {
            endpointAddress = endpointAddress.substring(0, endpointAddress.indexOf("?"));
        }
        return endpointAddress;
    }

    public static AuthenticationContext generateAuthenticationContext(String jti,
                                                                      JWTValidationInfo jwtValidationInfo,
                                                                      APIKeyValidationInfoDTO apiKeyValidationInfoDTO,
                                                                      String endUserToken,
                                                                      boolean isOauth) {

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setApiKey(jti);
        authContext.setUsername(getEndUserFromJWTValidationInfo(jwtValidationInfo, apiKeyValidationInfoDTO));
        authContext.setRequestTokenScopes(jwtValidationInfo.getScopes());
        authContext.setAccessToken(jwtValidationInfo.getRawPayload());

        if (apiKeyValidationInfoDTO != null) {
            authContext.setApiTier(apiKeyValidationInfoDTO.getApiTier());
            authContext.setKeyType(apiKeyValidationInfoDTO.getType());
            authContext.setApplicationId(apiKeyValidationInfoDTO.getApplicationId());
            authContext.setApplicationUUID(apiKeyValidationInfoDTO.getApplicationUUID());
            authContext.setApplicationGroupIds(apiKeyValidationInfoDTO.getApplicationGroupIds());
            authContext.setApplicationName(apiKeyValidationInfoDTO.getApplicationName());
            authContext.setApplicationTier(apiKeyValidationInfoDTO.getApplicationTier());
            authContext.setSubscriber(apiKeyValidationInfoDTO.getSubscriber());
            authContext.setTier(apiKeyValidationInfoDTO.getTier());
            authContext.setSubscriberTenantDomain(apiKeyValidationInfoDTO.getSubscriberTenantDomain());
            authContext.setApiName(apiKeyValidationInfoDTO.getApiName());
            authContext.setApiPublisher(apiKeyValidationInfoDTO.getApiPublisher());
            authContext.setStopOnQuotaReach(apiKeyValidationInfoDTO.isStopOnQuotaReach());
            authContext.setSpikeArrestLimit(apiKeyValidationInfoDTO.getSpikeArrestLimit());
            authContext.setSpikeArrestUnit(apiKeyValidationInfoDTO.getSpikeArrestUnit());
            authContext.setApplicationSpikesArrestLimit(apiKeyValidationInfoDTO.getApplicationSpikeArrestLimit());
            authContext.setApplicationSpikesArrestUnit(apiKeyValidationInfoDTO.getApplicationSpikeArrestUnit());
            authContext.setConsumerKey(apiKeyValidationInfoDTO.getConsumerKey());
            authContext.setIsContentAware(apiKeyValidationInfoDTO.isContentAware());
            authContext.setGraphQLMaxDepth(apiKeyValidationInfoDTO.getGraphQLMaxDepth());
            authContext.setGraphQLMaxComplexity(apiKeyValidationInfoDTO.getGraphQLMaxComplexity());
        }
        if (isOauth) {
            authContext.setConsumerKey(jwtValidationInfo.getConsumerKey());
            if (jwtValidationInfo.getIssuer() != null) {
                authContext.setIssuer(jwtValidationInfo.getIssuer());
            }
        }
        // Set JWT token sent to the backend
        if (StringUtils.isNotEmpty(endUserToken)) {
            authContext.setCallerToken(endUserToken);
        }

        return authContext;
    }

    /**
     * This method returns the end username from the JWTValidationInfo.
     * If isAppToken true subscriber username from APIKeyValidationInfoDTO with tenant domain is returned as end
     * username.
     * If false, tenant domain of the subscriber is appended to the username from JWTValidationInfo.
     * If null, same username from JWTValidation info is returned as it is.
     *
     * @param jwtValidationInfo       JWTValidationInfo
     * @param apiKeyValidationInfoDTO APIKeyValidationInfoDTO
     * @return String end username
     */
    private static String getEndUserFromJWTValidationInfo(JWTValidationInfo jwtValidationInfo,
                                                          APIKeyValidationInfoDTO apiKeyValidationInfoDTO) {

        Boolean isAppToken = jwtValidationInfo.getAppToken();
        String endUsername = jwtValidationInfo.getUser();
        if (isAppToken != null) {
            if (isAppToken) {
                endUsername = apiKeyValidationInfoDTO.getSubscriber();
                if (!APIConstants.SUPER_TENANT_DOMAIN.equals(apiKeyValidationInfoDTO.getSubscriberTenantDomain())) {
                    return endUsername;
                }
            }
            return endUsername + UserCoreConstants.TENANT_DOMAIN_COMBINER
                    + apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        }
        return endUsername;
    }

    public static AuthenticationContext generateAuthenticationContext(String tokenSignature, JWTClaimsSet payload,
                                                                      JSONObject api, String apiLevelPolicy)
            throws java.text.ParseException {

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setApiKey(tokenSignature);
        authContext.setUsername(payload.getSubject());
        if (payload.getClaim(APIConstants.JwtTokenConstants.KEY_TYPE) != null) {
            authContext.setKeyType(payload.getStringClaim(APIConstants.JwtTokenConstants.KEY_TYPE));
        } else {
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        }

        authContext.setApiTier(apiLevelPolicy);
        if (api != null) {
            authContext.setTier(APIConstants.UNLIMITED_TIER);
            authContext.setApiName(api.getAsString(APIConstants.JwtTokenConstants.API_NAME));
            authContext.setApiPublisher(api.getAsString(APIConstants.JwtTokenConstants.API_PUBLISHER));

        }
        authContext.setApplicationId("-1");
        authContext.setApplicationName(APIConstants.INTERNAL_KEY_APP_NAME);
        authContext.setApplicationUUID(UUID.nameUUIDFromBytes(APIConstants.INTERNAL_KEY_APP_NAME.
                getBytes(StandardCharsets.UTF_8)).toString());
        authContext.setApplicationTier(APIConstants.UNLIMITED_TIER);
        authContext.setSubscriber(APIConstants.INTERNAL_KEY_APP_NAME);
        return authContext;
    }


    public static AuthenticationContext generateAuthenticationContext(String tokenSignature, JWTClaimsSet payload,
                                                                      JSONObject api,
                                                                      String apiLevelPolicy, String endUserToken,
                                                                      org.apache.synapse.MessageContext synCtx)
            throws java.text.ParseException {

        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setApiKey(tokenSignature);
        authContext.setUsername(payload.getSubject());
        if (payload.getClaim(APIConstants.JwtTokenConstants.KEY_TYPE) != null) {
            authContext.setKeyType(payload.getStringClaim(APIConstants.JwtTokenConstants.KEY_TYPE));
        } else {
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        }

        authContext.setApiTier(apiLevelPolicy);

        if (payload.getClaim(APIConstants.JwtTokenConstants.APPLICATION) != null) {
            Map<String, Object> applicationObjMap =
                    payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.APPLICATION);
            JSONObject applicationObj = new JSONObject(applicationObjMap);
            authContext
                    .setApplicationId(
                            String.valueOf(applicationObj.getAsNumber(APIConstants.JwtTokenConstants.APPLICATION_ID)));
            authContext.setApplicationUUID(
                    String.valueOf(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_UUID)));
            authContext.setApplicationName(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_NAME));
            authContext.setApplicationTier(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_TIER));
            authContext.setSubscriber(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_OWNER));
            if (applicationObj.containsKey(APIConstants.JwtTokenConstants.QUOTA_TYPE)
                    && APIConstants.JwtTokenConstants.QUOTA_TYPE_BANDWIDTH
                    .equals(applicationObj.getAsString(APIConstants.JwtTokenConstants.QUOTA_TYPE))) {
                authContext.setIsContentAware(true);
                ;
            }
        }
        if (api != null) {

            // If the user is subscribed to the API
            String subscriptionTier = api.getAsString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER);
            authContext.setTier(subscriptionTier);
            authContext.setSubscriberTenantDomain(
                    api.getAsString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN));
            Map<String, Object> tierInfoObj = payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.TIER_INFO);
            JSONObject tierInfo = new JSONObject(tierInfoObj);
            authContext.setApiName(api.getAsString(APIConstants.JwtTokenConstants.API_NAME));
            authContext.setApiPublisher(api.getAsString(APIConstants.JwtTokenConstants.API_PUBLISHER));
            if (tierInfo.get(subscriptionTier) != null) {
                String jsonString = gson.toJson(tierInfo.get(subscriptionTier));
                JSONObject subscriptionTierObj = JSONValue.parse(jsonString, JSONObject.class);
                authContext.setStopOnQuotaReach(
                        Boolean.parseBoolean(
                                subscriptionTierObj.getAsString(APIConstants.JwtTokenConstants.STOP_ON_QUOTA_REACH)));
                authContext.setSpikeArrestLimit
                        (subscriptionTierObj.getAsNumber(APIConstants.JwtTokenConstants.SPIKE_ARREST_LIMIT).intValue());
                if (!"null".equals(
                        subscriptionTierObj.getAsString(APIConstants.JwtTokenConstants.SPIKE_ARREST_UNIT))) {
                    authContext.setSpikeArrestUnit(
                            subscriptionTierObj.getAsString(APIConstants.JwtTokenConstants.SPIKE_ARREST_UNIT));
                }
                //check whether the quota type is there and it is equal to bandwithVolume type.
                if (subscriptionTierObj.containsKey(APIConstants.JwtTokenConstants.QUOTA_TYPE)
                        && APIConstants.JwtTokenConstants.QUOTA_TYPE_BANDWIDTH
                        .equals(subscriptionTierObj.getAsString(APIConstants.JwtTokenConstants.QUOTA_TYPE))) {
                    authContext.setIsContentAware(true);
                    ;
                }
                if (synCtx != null && APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                    Integer graphQLMaxDepth = (int) (long) subscriptionTierObj.get(GraphQLConstants.GRAPHQL_MAX_DEPTH);
                    Integer graphQLMaxComplexity =
                            (int) (long) subscriptionTierObj.get(GraphQLConstants.GRAPHQL_MAX_COMPLEXITY);
                    synCtx.setProperty(GraphQLConstants.MAXIMUM_QUERY_DEPTH, graphQLMaxDepth);
                    synCtx.setProperty(GraphQLConstants.MAXIMUM_QUERY_COMPLEXITY, graphQLMaxComplexity);
                }
            }
        }
        // Set JWT token sent to the backend
        if (StringUtils.isNotEmpty(endUserToken)) {
            authContext.setCallerToken(endUserToken);
        }

        return authContext;
    }

    /**
     * Validate whether the user is subscribed to the invoked API. If subscribed, return a JSON object containing
     * the API information.
     *
     * @param apiContext        API context
     * @param apiVersion        API version
     * @param jwtValidationInfo The payload of the JWT token
     * @return an JSON object containing subscribed API information retrieved from token payload.
     * If the subscription information is not found, return a null object.
     * @throws APISecurityException if the user is not subscribed to the API
     */
    public static JSONObject validateAPISubscription(String apiContext, String apiVersion,
                                                     JWTValidationInfo jwtValidationInfo,
                                                     String jwtHeader, boolean isOauth)
            throws APISecurityException {

        JSONObject api = null;

        if (jwtValidationInfo.getClaims().get(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS) != null) {
            // Subscription validation
            JSONArray subscribedAPIs =
                    (JSONArray) jwtValidationInfo.getClaims().get(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS);
            for (int i = 0; i < subscribedAPIs.size(); i++) {
                JSONObject subscribedAPIsJSONObject =
                        (JSONObject) subscribedAPIs.get(i);
                if (apiContext
                        .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_CONTEXT)) &&
                        apiVersion
                                .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_VERSION)
                                )) {
                    api = subscribedAPIsJSONObject;
                    if (log.isDebugEnabled()) {
                        log.debug("User is subscribed to the API: " + apiContext + ", " +
                                "version: " + apiVersion + ". Token: " + getMaskedToken(jwtHeader));
                    }
                    break;
                }
            }
            if (api == null) {
                if (log.isDebugEnabled()) {
                    log.debug("User is not subscribed to access the API: " + apiContext +
                            ", version: " + apiVersion + ". Token: " + getMaskedToken(jwtHeader));
                }
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No subscription information found in the token.");
            }
            // we perform mandatory authentication for Api Keys
            if (!isOauth) {
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        }
        return api;
    }

    /**
     * Validate whether the user is subscribed to the invoked API. If subscribed, return a JSON object containing
     * the API information.
     *
     * @param apiContext API context
     * @param apiVersion API version
     * @param payload    The payload of the JWT token
     * @return an JSON object containing subscribed API information retrieved from token payload.
     * If the subscription information is not found, return a null object.
     * @throws APISecurityException if the user is not subscribed to the API
     */
    public static JSONObject validateAPISubscription(String apiContext, String apiVersion, JWTClaimsSet payload,
                                                     String[] splitToken, boolean isOauth)
            throws APISecurityException {

        JSONObject api = null;
        APIKeyValidator apiKeyValidator = new APIKeyValidator();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
        boolean apiKeySubValidationEnabled = isAPIKeySubscriptionValidationEnabled();
        JSONObject application;
        int appId = 0;
        if (payload.getClaim(APIConstants.JwtTokenConstants.APPLICATION) != null) {
            try {
                Map<String, Object> applicationObjMap =
                        payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.APPLICATION);
                application = new JSONObject(applicationObjMap);
                appId = Integer.parseInt(application.getAsString(APIConstants.JwtTokenConstants.APPLICATION_ID));
            } catch (ParseException e) {
                log.error("Error while parsing the application object from the JWT token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        // validate subscription
        // if the appId is equal to 0 then it's a internal key
        if (apiKeySubValidationEnabled && appId != 0) {
            apiKeyValidationInfoDTO =
                    apiKeyValidator.validateSubscription(apiContext, apiVersion, appId, getTenantDomain());
        }

        if (payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS) != null) {
            // Subscription validation
            ArrayList subscribedAPIs = (ArrayList) payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS);
            for (Object subscribedAPI : subscribedAPIs) {
                String subscribedAPIsJSONString = gson.toJson(subscribedAPI);
                JSONObject subscribedAPIsJSONObject = JSONValue.parse(subscribedAPIsJSONString, JSONObject.class);
                if (apiContext
                        .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_CONTEXT)) &&
                        apiVersion
                                .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_VERSION)
                                )) {
                    // check whether the subscription is authorized
                    if (apiKeySubValidationEnabled && appId != 0) {
                        if (apiKeyValidationInfoDTO.isAuthorized()) {
                            api = subscribedAPIsJSONObject;
                            if (log.isDebugEnabled()) {
                                log.debug("User is subscribed to the API: " + apiContext + ", " +
                                        "version: " + apiVersion + ". Token: " + getMaskedToken(splitToken[0]));
                            }
                        }
                    } else {
                        api = subscribedAPIsJSONObject;
                        if (log.isDebugEnabled()) {
                            log.debug("User is subscribed to the API: " + apiContext + ", " +
                                    "version: " + apiVersion + ". Token: " + getMaskedToken(splitToken[0]));
                        }
                    }
                    break;
                }
            }
            if (api == null) {
                if (log.isDebugEnabled()) {
                    log.debug("User is not subscribed to access the API: " + apiContext +
                            ", version: " + apiVersion + ". Token: " + getMaskedToken(splitToken[0]));
                }
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No subscription information found in the token.");
            }
            // we perform mandatory authentication for Api Keys
            if (!isOauth) {
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        }
        return api;
    }

    /**
     * Validate whether the user is subscribed to the invoked API. If subscribed, return a JSON object containing
     * the API information.
     *
     * @param apiContext API context
     * @param apiVersion API version
     * @param payload    The payload of the JWT token
     * @param token      The token which was used to invoke the API
     * @return an JSON object containing subscribed API information retrieved from token payload.
     * If the subscription information is not found, return a null object.
     * @throws APISecurityException if the user is not subscribed to the API
     */
    public static JSONObject validateAPISubscription(String apiContext, String apiVersion, JWTClaimsSet payload,
                                                     String token)
            throws APISecurityException {

        JSONObject api = null;
        APIKeyValidator apiKeyValidator = new APIKeyValidator();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = null;
        boolean apiKeySubValidationEnabled = isAPIKeySubscriptionValidationEnabled();
        JSONObject application;
        int appId = 0;
        if (payload.getClaim(APIConstants.JwtTokenConstants.APPLICATION) != null) {
            try {
                Map<String, Object> applicationObjMap =
                        payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.APPLICATION);
                application = new JSONObject(applicationObjMap);
                appId = Integer.parseInt(application.getAsString(APIConstants.JwtTokenConstants.APPLICATION_ID));
            } catch (ParseException e) {
                log.error("Error while parsing the application object from the JWT token.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        // validate subscription
        // if the appId is equal to 0 then it's a internal key
        if (apiKeySubValidationEnabled && appId != 0) {
            apiKeyValidationInfoDTO =
                    apiKeyValidator.validateSubscription(apiContext, apiVersion, appId, getTenantDomain());
        }

        if (payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS) != null) {
            // Subscription validation
            ArrayList subscribedAPIs =
                    (ArrayList) payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS);
            for (Object subscribedAPI : subscribedAPIs) {
                String subscribedAPIsJSONString = gson.toJson(subscribedAPI);
                JSONObject subscribedAPIsJSONObject = JSONValue.parse(subscribedAPIsJSONString, JSONObject.class);
                if (apiContext
                        .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_CONTEXT)) &&
                        apiVersion
                                .equals(subscribedAPIsJSONObject.getAsString(APIConstants.JwtTokenConstants.API_VERSION)
                                )) {
                    // check whether the subscription is authorized
                    if (apiKeySubValidationEnabled && appId != 0) {
                        if (apiKeyValidationInfoDTO.isAuthorized()) {
                            api = subscribedAPIsJSONObject;
                            if (log.isDebugEnabled()) {
                                log.debug("User is subscribed to the API: " + apiContext + ", " +
                                        "version: " + apiVersion + ". Token: " + getMaskedToken(token));
                            }
                        }
                    } else {
                        api = subscribedAPIsJSONObject;
                        if (log.isDebugEnabled()) {
                            log.debug("User is subscribed to the API: " + apiContext + ", " +
                                    "version: " + apiVersion + ". Token: " + getMaskedToken(token));
                        }
                    }
                    break;
                }
            }
            if (api == null) {
                if (log.isDebugEnabled()) {
                    log.debug("User is not subscribed to access the API: " + apiContext +
                            ", version: " + apiVersion + ". Token: " + getMaskedToken(token));
                }
                log.error("User is not subscribed to access the API.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        APISecurityConstants.API_AUTH_FORBIDDEN_MESSAGE);
            }
        } else {
            log.debug("No subscription information found in the token.");
        }
        return api;
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwt   SignedJwt Token
     * @param alias public certificate keystore alias
     * @return whether the signature is verified or or not
     * @throws APISecurityException in case of signature verification failure
     */
    public static boolean verifyTokenSignature(SignedJWT jwt, String alias) throws APISecurityException {

        Certificate publicCert = null;
        //Read the client-truststore.jks into a KeyStore
        try {
            publicCert = APIUtil.getCertificateFromParentTrustStore(alias);
        } catch (APIManagementException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
        }

        if (publicCert != null) {
            JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
            if (algorithm != null && (JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
                    JWSAlgorithm.RS384.equals(algorithm))) {
                return verifyTokenSignature(jwt, (RSAPublicKey) publicCert.getPublicKey());
            } else {
                log.error("Public key is not a RSA");
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        } else {
            log.error("Couldn't find a public certificate to verify signature with alias " + alias);
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.SIGNATURE_VERIFICATION_FAILURE_MESSAGE);
        }
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwt       SignedJwt Token
     * @param publicKey public certificate
     * @return whether the signature is verified or or not
     * @throws APISecurityException in case of signature verification failure
     */
    public static boolean verifyTokenSignature(SignedJWT jwt, RSAPublicKey publicKey) throws APISecurityException {

        JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
        if (algorithm != null && (JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
                JWSAlgorithm.RS384.equals(algorithm))) {
            try {
                JWSVerifier jwsVerifier = new RSASSAVerifier(publicKey);
                return jwt.verify(jwsVerifier);
            } catch (JOSEException e) {
                log.error("Error while verifying JWT signature");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
            }
        } else {
            log.error("Public key is not a RSA");
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
    }

    public static String getMaskedToken(String token) {

        if (token.length() >= 10) {
            return "XXXXX" + token.substring(token.length() - 10);
        } else {
            return "XXXXX" + token.substring(token.length() / 2);
        }
    }

    public static boolean isGatewayTokenCacheEnabled() {

        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            String cacheEnabled = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(cacheEnabled);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. " +
                    "Use default configuration.", e);
        }
        return true;
    }

    public static boolean isAPIKeySubscriptionValidationEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            String subscriptionValidationEnabled = config.getFirstProperty(APIConstants.API_KEY_SUBSCRIPTION_VALIDATION_ENABLED);
            return Boolean.parseBoolean(subscriptionValidationEnabled);
        } catch (Exception e) {
            log.error("Did not find valid API Key Subscription Validation Enabled configuration. " +
                    "Use default configuration.", e);
        }
        return false;
    }

    /**
     * Return tenant domain of the API being invoked.
     *
     * @return tenant domain
     */
    public static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    public static String getAccessTokenCacheKey(String accessToken, String apiContext, String apiVersion,
                                                String resourceUri, String httpVerb) {

        return accessToken + ":" + apiContext + ":" + apiVersion + ":" + resourceUri + ":" + httpVerb;
    }

    public static JWTInfoDto generateJWTInfoDto(JWTValidationInfo jwtValidationInfo,
                                                APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String apiContext,
                                                String apiVersion) {

        JWTInfoDto jwtInfoDto = new JWTInfoDto();
        jwtInfoDto.setJwtValidationInfo(jwtValidationInfo);
        //jwtInfoDto.setMessageContext(null);
        jwtInfoDto.setApiContext(apiContext);
        jwtInfoDto.setVersion(apiVersion);
        constructJWTContent(null, apiKeyValidationInfoDTO, jwtInfoDto);
        return jwtInfoDto;
    }

    private static void constructJWTContent(JSONObject subscribedAPI,
                                            APIKeyValidationInfoDTO apiKeyValidationInfoDTO, JWTInfoDto jwtInfoDto) {

        if (jwtInfoDto.getJwtValidationInfo() != null) {
            jwtInfoDto.setEndUser(getEndUserFromJWTValidationInfo(jwtInfoDto.getJwtValidationInfo(),
                    apiKeyValidationInfoDTO));
            if (jwtInfoDto.getJwtValidationInfo().getClaims() != null) {
                Map<String, Object> claims = jwtInfoDto.getJwtValidationInfo().getClaims();
                if (claims.get(JWTConstants.SUB) != null) {
                    String sub = (String) jwtInfoDto.getJwtValidationInfo().getClaims().get(JWTConstants.SUB);

                    // A system property is used to enable/disable getting the tenant aware username as sub claim.
                    String tenantAwareSubClaim = System.getProperty(APIConstants.ENABLE_TENANT_AWARE_SUB_CLAIM);
                    if (StringUtils.isNotEmpty(tenantAwareSubClaim) && Boolean.parseBoolean(tenantAwareSubClaim)) {
                        sub = MultitenantUtils.getTenantAwareUsername(sub);
                    }
                    jwtInfoDto.setSub(sub);
                }
                if (claims.get(JWTConstants.ORGANIZATIONS) != null) {
                    String[] organizations = (String[]) jwtInfoDto.getJwtValidationInfo().getClaims().
                            get(JWTConstants.ORGANIZATIONS);
                    jwtInfoDto.setOrganizations(organizations);
                }
            }
        }

        if (apiKeyValidationInfoDTO != null) {
            jwtInfoDto.setApplicationId(apiKeyValidationInfoDTO.getApplicationId());
            jwtInfoDto.setApplicationName(apiKeyValidationInfoDTO.getApplicationName());
            jwtInfoDto.setApplicationTier(apiKeyValidationInfoDTO.getApplicationTier());
            jwtInfoDto.setKeyType(apiKeyValidationInfoDTO.getType());
            jwtInfoDto.setSubscriber(apiKeyValidationInfoDTO.getSubscriber());
            jwtInfoDto.setSubscriptionTier(apiKeyValidationInfoDTO.getTier());
            jwtInfoDto.setApiName(apiKeyValidationInfoDTO.getApiName());
            jwtInfoDto.setEndUserTenantId(
                    APIUtil.getTenantIdFromTenantDomain(apiKeyValidationInfoDTO.getSubscriberTenantDomain()));
            jwtInfoDto.setApplicationUUId(apiKeyValidationInfoDTO.getApplicationUUID());
            jwtInfoDto.setAppAttributes(apiKeyValidationInfoDTO.getAppAttributes());
        } else if (subscribedAPI != null) {
            // If the user is subscribed to the API
            String apiName = subscribedAPI.getAsString(APIConstants.JwtTokenConstants.API_NAME);
            jwtInfoDto.setApiName(apiName);
            String subscriptionTier = subscribedAPI.getAsString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER);
            String subscriptionTenantDomain =
                    subscribedAPI.getAsString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN);
            jwtInfoDto.setSubscriptionTier(subscriptionTier);
            jwtInfoDto.setEndUserTenantId(APIUtil.getTenantIdFromTenantDomain(subscriptionTenantDomain));

            Map<String, Object> claims = jwtInfoDto.getJwtValidationInfo().getClaims();
            if (claims.get(APIConstants.JwtTokenConstants.APPLICATION) != null) {
                String applicationString = gson.toJson(claims.get(APIConstants.JwtTokenConstants.APPLICATION));
                JSONObject
                        applicationObj = JSONValue.parse(applicationString, JSONObject.class);
                jwtInfoDto.setApplicationId(
                        String.valueOf(applicationObj.getAsNumber(APIConstants.JwtTokenConstants.APPLICATION_ID)));
                jwtInfoDto
                        .setApplicationName(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_NAME));
                jwtInfoDto
                        .setApplicationTier(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_TIER));
                jwtInfoDto.setSubscriber(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_OWNER));
            }
        }
    }

    /**
     * This method is used to generate JWTInfoDto
     *
     * @param subscribedAPI     The subscribed API
     * @param jwtValidationInfo The JWT validation info
     * @param apiContext        API context
     * @param apiVersion        API version
     * @return JWTInfoDto object with JWT validation info and API related info
     */
    public static JWTInfoDto generateJWTInfoDto(JSONObject subscribedAPI, JWTValidationInfo jwtValidationInfo,
                                                String apiContext, String apiVersion) {

        JWTInfoDto jwtInfoDto = new JWTInfoDto();
        jwtInfoDto.setJwtValidationInfo(jwtValidationInfo);
        jwtInfoDto.setApiContext(apiContext);
        jwtInfoDto.setVersion(apiVersion);
        constructJWTContent(subscribedAPI, null, jwtInfoDto);
        return jwtInfoDto;
    }

    public static JWTInfoDto generateJWTInfoDto(JSONObject subscribedAPI, JWTValidationInfo jwtValidationInfo,
                                                APIKeyValidationInfoDTO apiKeyValidationInfoDTO,
                                                org.apache.synapse.MessageContext synCtx) {

        JWTInfoDto jwtInfoDto = new JWTInfoDto();
        jwtInfoDto.setJwtValidationInfo(jwtValidationInfo);
        //jwtInfoDto.setMessageContext(synCtx);
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        jwtInfoDto.setApiContext(apiContext);
        jwtInfoDto.setVersion(apiVersion);
        constructJWTContent(subscribedAPI, apiKeyValidationInfoDTO, jwtInfoDto);
        return jwtInfoDto;
    }

    //for OpenTracing
    public static void setAPIRelatedTags(TracingSpan tracingSpan, org.apache.synapse.MessageContext messageContext) {

        API api = GatewayUtils.getAPI(messageContext);
        Object electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        if (electedResource != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_RESOURCE, (String) electedResource);
        }
        if (api != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_NAME, api.getApiName());
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_VERSION, api.getApiVersion());
        }

        Object httpStatusCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(HTTP_SC);
        if (httpStatusCode != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_HTTP_RESPONSE_STATUS_CODE, httpStatusCode.toString());
        }
        Object httpStatusCodeDescription =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(HTTP_SC_DESC);
        if (httpStatusCodeDescription != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_HTTP_RESPONSE_STATUS_CODE_DESCRIPTION,
                    httpStatusCodeDescription.toString());
        }

        Object consumerKey = messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY);
        if (consumerKey != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_APPLICATION_CONSUMER_KEY,
                    (String) consumerKey);
        }
    }

    //for OpenTelemetry
    public static void setAPIRelatedTags(TelemetrySpan tracingSpan, org.apache.synapse.MessageContext messageContext) {

        API api = GatewayUtils.getAPI(messageContext);
        Object electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        if (electedResource != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_RESOURCE, (String) electedResource);
        }
        if (api != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_NAME, api.getApiName());
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_VERSION, api.getApiVersion());
        }

        Object httpStatusCode = ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(HTTP_SC);
        if (httpStatusCode != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_HTTP_RESPONSE_STATUS_CODE,
                    httpStatusCode.toString());
        }
        Object httpStatusCodeDescription =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(HTTP_SC_DESC);
        if (httpStatusCodeDescription != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_HTTP_RESPONSE_STATUS_CODE_DESCRIPTION,
                    httpStatusCodeDescription.toString());
        }

        Object consumerKey = messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY);
        if (consumerKey != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_APPLICATION_CONSUMER_KEY,
                    (String) consumerKey);
        }
    }

    //for OpenTracing
    public static void setAPIResource(TracingSpan tracingSpan, org.apache.synapse.MessageContext messageContext) {

        Object electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String httpMethod = (String) axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if (StringUtils.isEmpty(httpMethod)) {
            httpMethod = (String) messageContext.getProperty(RESTConstants.REST_METHOD);
        }
        if (electedResource instanceof String && StringUtils.isNotEmpty((String) electedResource)) {
            Util.updateOperation(tracingSpan, (httpMethod.toUpperCase().concat("--").concat((String) electedResource)));
        }

    }

    //for OpenTelemetry
    public static void setAPIResource(TelemetrySpan tracingSpan, org.apache.synapse.MessageContext messageContext) {

        Object electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String httpMethod = (String) axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if (StringUtils.isEmpty(httpMethod)) {
            httpMethod = (String) messageContext.getProperty(RESTConstants.REST_METHOD);
        }
        if (electedResource instanceof String && StringUtils.isNotEmpty((String) electedResource)) {
            TelemetryUtil.updateOperation(tracingSpan,
                    (httpMethod.toUpperCase().concat("--").concat((String) electedResource)));
        }

    }

    //for OpenTracing
    private static void setTracingId(TracingSpan tracingSpan, MessageContext axis2MessageContext) {

        Map headersMap =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (headersMap != null && headersMap.containsKey(APIConstants.ACTIVITY_ID)) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID,
                    (String) headersMap.get(APIConstants.ACTIVITY_ID));
        } else {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID, axis2MessageContext.getMessageID());
        }
    }

    //for OpenTelemetry
    private static void setTracingId(TelemetrySpan tracingSpan, MessageContext axis2MessageContext) {

        Map headersMap =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        if (headersMap != null && headersMap.containsKey(APIConstants.ACTIVITY_ID)) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID,
                    (String) headersMap.get(APIConstants.ACTIVITY_ID));
        } else {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID,
                    axis2MessageContext.getMessageID());
        }
    }

    //for OpenTracing
    public static void setRequestRelatedTags(TracingSpan tracingSpan,
                                             org.apache.synapse.MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object restUrlPostfix = axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_URL_POSTFIX);
        String httpMethod = (String) (axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD));
        if (restUrlPostfix != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_PATH,
                    (String) restUrlPostfix);
        }
        if (httpMethod != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_METHOD, httpMethod);
        }
        setTracingId(tracingSpan, axis2MessageContext);

    }

    //for OpenTelemetry
    public static void setRequestRelatedTags(TelemetrySpan tracingSpan,
                                             org.apache.synapse.MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object restUrlPostfix = axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_URL_POSTFIX);
        String httpMethod = (String) (axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD));
        if (restUrlPostfix != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_PATH,
                    (String) restUrlPostfix);
        }
        if (httpMethod != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_METHOD, httpMethod);
        }
        setTracingId(tracingSpan, axis2MessageContext);

    }

    //for OpenTracing
    public static void setEndpointRelatedInformation(TracingSpan tracingSpan,
                                                     org.apache.synapse.MessageContext messageContext) {

        Object endpoint = messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpoint != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ENDPOINT, (String) endpoint);
        }
    }

    //for OpenTelemetry
    public static void setEndpointRelatedInformation(TelemetrySpan tracingSpan,
                                                     org.apache.synapse.MessageContext messageContext) {

        Object endpoint = messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpoint != null) {
            TelemetryUtil.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ENDPOINT, (String) endpoint);
        }
    }

    public static List<String> retrieveDeployedSequences(String apiName, String version, String tenantDomain)
            throws APIManagementException {

        try {
            List<String> deployedSequences = new ArrayList<>();
            String inSequenceExtensionName =
                    APIUtil.getSequenceExtensionName(apiName, version) + APIConstants.API_CUSTOM_SEQ_IN_EXT;
            String outSequenceExtensionName =
                    APIUtil.getSequenceExtensionName(apiName, version) + APIConstants.API_CUSTOM_SEQ_OUT_EXT;
            String faultSequenceExtensionName =
                    APIUtil.getSequenceExtensionName(apiName, version) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
            SequenceAdminServiceProxy sequenceAdminServiceProxy = new SequenceAdminServiceProxy(tenantDomain);
            MessageContext.setCurrentMessageContext(createAxis2MessageContext());
            if (sequenceAdminServiceProxy.isExistingSequence(inSequenceExtensionName)) {
                OMElement sequence = sequenceAdminServiceProxy.getSequence(inSequenceExtensionName);
                deployedSequences.add(sequence.toString());
            }
            if (sequenceAdminServiceProxy.isExistingSequence(outSequenceExtensionName)) {
                OMElement sequence = sequenceAdminServiceProxy.getSequence(outSequenceExtensionName);
                deployedSequences.add(sequence.toString());
            }
            if (sequenceAdminServiceProxy.isExistingSequence(faultSequenceExtensionName)) {
                OMElement sequence = sequenceAdminServiceProxy.getSequence(faultSequenceExtensionName);
                deployedSequences.add(sequence.toString());
            }
            return deployedSequences;
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while retrieving Deployed Sequences", axisFault,
                    ExceptionCodes.INTERNAL_ERROR);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
    }

    public static List<String> retrieveDeployedLocalEntries(String apiName, String version, String tenantDomain)
            throws APIManagementException {

        try {
            SubscriptionDataStore tenantSubscriptionStore =
                    SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
            List<String> deployedLocalEntries = new ArrayList<>();
            if (tenantSubscriptionStore != null) {
                API retrievedAPI = tenantSubscriptionStore.getApiByNameAndVersion(apiName, version);
                if (retrievedAPI != null) {
                    MessageContext.setCurrentMessageContext(createAxis2MessageContext());
                    LocalEntryServiceProxy localEntryServiceProxy = new LocalEntryServiceProxy(tenantDomain);
                    String localEntryKey = retrievedAPI.getUuid();
                    if (APIConstants.GRAPHQL_API.equals(retrievedAPI.getApiType())) {
                        localEntryKey = retrievedAPI.getUuid().concat(APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION);
                    }
                    if (localEntryServiceProxy.isEntryExists(localEntryKey)) {
                        OMElement entry = localEntryServiceProxy.getEntry(localEntryKey);
                        deployedLocalEntries.add(entry.toString());
                    }
                }
            }
            return deployedLocalEntries;
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while retrieving LocalEntries", axisFault,
                    ExceptionCodes.INTERNAL_ERROR);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
    }

    public static List<String> retrieveDeployedEndpoints(String apiName, String version, String tenantDomain)
            throws APIManagementException {

        List<String> deployedEndpoints = new ArrayList<>();
        try {
            MessageContext.setCurrentMessageContext(createAxis2MessageContext());
            EndpointAdminServiceProxy endpointAdminServiceProxy = new EndpointAdminServiceProxy(tenantDomain);
            String productionEndpointKey = apiName.concat("--v").concat(version).concat("_APIproductionEndpoint");
            String sandboxEndpointKey = apiName.concat("--v").concat(version).concat("_APIsandboxEndpoint");
            if (endpointAdminServiceProxy.isEndpointExist(productionEndpointKey)) {
                String entry = endpointAdminServiceProxy.getEndpoint(productionEndpointKey);
                deployedEndpoints.add(entry);
            }
            if (endpointAdminServiceProxy.isEndpointExist(sandboxEndpointKey)) {
                String entry = endpointAdminServiceProxy.getEndpoint(sandboxEndpointKey);
                deployedEndpoints.add(entry);
            }
        } catch (AxisFault e) {
            throw new APIManagementException("Error in fetching deployed endpoints from Synapse Configuration", e,
                    ExceptionCodes.INTERNAL_ERROR);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }

        return deployedEndpoints;
    }

    public static String retrieveDeployedAPI(String apiName, String version, String tenantDomain)
            throws APIManagementException {

        try {
            MessageContext.setCurrentMessageContext(createAxis2MessageContext());
            RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy(tenantDomain);
            String qualifiedName;
            if (version != null) {
                qualifiedName = GatewayUtils.getQualifiedApiName(apiName, version);
            } else {
                qualifiedName = apiName;
            }
            OMElement api = restapiAdminServiceProxy.getApiContent(qualifiedName);
            if (api != null) {
                return api.toString();
            }
            return null;
        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while retrieving API Artifacts", axisFault,
                    ExceptionCodes.INTERNAL_ERROR);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
    }

    public static org.apache.axis2.context.MessageContext createAxis2MessageContext() throws AxisFault {

        AxisService axisService = new AxisService();
        axisService.addParameter("adminService", true);
        org.apache.axis2.context.MessageContext axis2MsgCtx = new org.apache.axis2.context.MessageContext();
        axis2MsgCtx.setMessageID(UIDGenerator.generateURNString());
        axis2MsgCtx.setConfigurationContext(ServiceReferenceHolder.getInstance()
                .getConfigurationContextService().getServerConfigContext());
        axis2MsgCtx.setProperty(org.apache.axis2.context.MessageContext.CLIENT_API_NON_BLOCKING, Boolean.TRUE);
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setAxisService(axisService);
        return axis2MsgCtx;
    }

    public static API getAPI(org.apache.synapse.MessageContext messageContext) {

        Object api = messageContext.getProperty(APIMgtGatewayConstants.API_OBJECT);
        if (api != null) {
            return (API) api;
        } else {
            api = messageContext.getProperty(APIMgtGatewayConstants.API_OBJECT);
            if (api != null) {
                return (API) api;
            }
            String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            SubscriptionDataStore tenantSubscriptionStore =
                    SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(getTenantDomain());
            if (tenantSubscriptionStore != null) {
                API api1 = tenantSubscriptionStore.getApiByContextAndVersion(context, version);
                if (api1 != null) {
                    messageContext.setProperty(APIMgtGatewayConstants.API_OBJECT, api1);
                    return api1;
                }
            }
            return null;
        }
    }

    public static String getStatus(org.apache.synapse.MessageContext messageContext) {

        Object status = messageContext.getProperty(APIMgtGatewayConstants.API_STATUS);
        if (status != null) {
            return (String) status;
        }
        API api = getAPI(messageContext);
        if (api != null) {
            String apiStatus = api.getStatus();
            messageContext.setProperty(APIMgtGatewayConstants.API_STATUS, apiStatus);
            return apiStatus;
        }
        return null;
    }

    public static boolean isAPIStatusPrototype(org.apache.synapse.MessageContext messageContext) {

        return APIConstants.PROTOTYPED.equals(getStatus(messageContext));
    }

    public static String getAPINameFromContextAndVersion(org.apache.synapse.MessageContext messageContext) {

        API api = getAPI(messageContext);
        if (api != null) {
            return api.getApiName();
        }
        return null;
    }

    public static String getApiProviderFromContextAndVersion(org.apache.synapse.MessageContext messageContext) {

        API api = getAPI(messageContext);
        if (api != null) {
            return api.getApiProvider();
        }
        return null;
    }

    public static boolean isAPIKey(JWTClaimsSet jwtClaimsSet) {
        Object tokenTypeClaim = jwtClaimsSet.getClaim(APIConstants.JwtTokenConstants.TOKEN_TYPE);
        if (tokenTypeClaim != null) {
            return APIConstants.JwtTokenConstants.API_KEY_TOKEN_TYPE.equals(tokenTypeClaim);
        }
        return jwtClaimsSet.getClaim(APIConstants.JwtTokenConstants.APPLICATION) != null
                && jwtClaimsSet.getClaim(APIConstants.JwtTokenConstants.CONSUMER_KEY) == null;
    }

    public static boolean isInternalKey(JWTClaimsSet jwtClaimsSet) {
        Object tokenTypeClaim = jwtClaimsSet.getClaim(APIConstants.JwtTokenConstants.TOKEN_TYPE);
        if (tokenTypeClaim != null) {
            return APIConstants.JwtTokenConstants.INTERNAL_KEY_TOKEN_TYPE.equals(tokenTypeClaim);
        }
        return false;
    }
    /**
     * Check whether the jwt token is expired or not.
     *
     * @param payload The payload of the JWT token
     * @return returns true if the JWT token is expired
     */
    public static boolean isJwtTokenExpired(JWTClaimsSet payload) {

        int timestampSkew = (int) OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();

        DefaultJWTClaimsVerifier jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier();
        jwtClaimsSetVerifier.setMaxClockSkew(timestampSkew);
        try {
            jwtClaimsSetVerifier.verify(payload, null);
            if (log.isDebugEnabled()) {
                log.debug("Token is not expired. User: " + payload.getSubject());
            }
        } catch (BadJWTException e) {
            if ("Expired JWT".equals(e.getMessage())) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getSubject());
        }
        return false;
    }

    public static void setRequestDestination(org.apache.synapse.MessageContext messageContext) {

        String requestDestination = null;
        EndpointReference objectTo =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext().getOptions().getTo();
        if (objectTo != null) {
            requestDestination = objectTo.getAddress();
        }
        if (requestDestination != null) {
            messageContext.setProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS, requestDestination);
        }
    }

    public static void setWebsocketEndpointsToBeRemoved(GatewayAPIDTO gatewayAPIDTO, String tenantDomain)
            throws AxisFault {
        String apiName = gatewayAPIDTO.getName();
        String apiVersion = gatewayAPIDTO.getVersion();
        if (apiName != null && apiVersion != null) {
            String prefix = apiName.concat("--v").concat(apiVersion).concat("_API");
            EndpointAdminServiceProxy endpointAdminServiceProxy = new EndpointAdminServiceProxy(tenantDomain);
            String[] endpoints = endpointAdminServiceProxy.getEndpoints();
            for (String endpoint : endpoints) {
                if (endpoint.startsWith(prefix)) {
                    gatewayAPIDTO.setEndpointEntriesToBeRemove(
                            org.wso2.carbon.apimgt.impl.utils.GatewayUtils.addStringToList(endpoint,
                                    gatewayAPIDTO.getEndpointEntriesToBeRemove()));
                }
            }
        }
    }

    public static TracingTracer getTracingTracer() {
        return ServiceReferenceHolder.getInstance().getTracer();
    }

    public static TelemetryTracer getTelemetryTracer() {
        return ServiceReferenceHolder.getInstance().getTelemetryTracer();
    }

    public static boolean isAllApisDeployed () {
        return DataHolder.getInstance().isAllApisDeployed();
    }

    public static boolean isAllGatewayPoliciesDeployed () {
        return DataHolder.getInstance().isAllGatewayPoliciesDeployed();
    }

    public static List<String> getKeyManagers(org.apache.synapse.MessageContext messageContext) {

        API api = getAPI(messageContext);
        if (api != null) {
            return DataHolder.getInstance().getKeyManagersFromUUID(api.getUuid());
        }
        return Arrays.asList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS);
    }

    public static boolean isOnDemandLoading() {
        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration()
                        .getGatewayArtifactSynchronizerProperties();
        return gatewayArtifactSynchronizerProperties.isOnDemandLoading();
    }
}
