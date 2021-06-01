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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.IPRange;
import org.wso2.carbon.apimgt.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimeDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayUtils {

    private static final Log log = LogFactory.getLog(GatewayUtils.class);

    public static boolean isClusteringEnabled() {

        ClusteringAgent agent = ServiceReferenceHolder.getInstance().getServerConfigurationContext().
                getAxisConfiguration().getClusteringAgent();
        if (agent != null) {
            return true;
        }
        return false;
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

    public static Map getJWTClaims(AuthenticationContext authContext) {

        String[] jwtTokenArray = authContext.getCallerToken().split(Pattern.quote("."));
        // decoding JWT
        try {
            byte[] jwtByteArray = Base64.decodeBase64(jwtTokenArray[1].getBytes("UTF-8"));
            String jwtAssertion = new String(jwtByteArray, "UTF-8");
            JSONParser parser = new JSONParser();
            return (Map) parser.parse(jwtAssertion);
        } catch (UnsupportedEncodingException e) {
            log.error("Error while decoding jwt header", e);
        } catch (ParseException e) {
            log.error("Error while parsing jwt header", e);
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
        if (messageContext.isResponse()) {
            messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, APIMgtGatewayConstants.BAD_RESPONSE);
        } else {
            messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, APIMgtGatewayConstants.BAD_REQUEST);
        }
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_DESC, desc);
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

    /**
     * Build execution time related information using message context
     *
     * @param messageContext
     * @return
     */
    public static ExecutionTimeDTO getExecutionTime(org.apache.synapse.MessageContext messageContext) {

        Object securityLatency = messageContext.getProperty(APIMgtGatewayConstants.SECURITY_LATENCY);
        Object throttleLatency = messageContext.getProperty(APIMgtGatewayConstants.THROTTLING_LATENCY);
        Object reqMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY);
        Object resMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY);
        Object otherLatency = messageContext.getProperty(APIMgtGatewayConstants.OTHER_LATENCY);
        Object backendLatency = messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY);
        ExecutionTimeDTO executionTime = new ExecutionTimeDTO();
        executionTime.setBackEndLatency(backendLatency == null ? 0 : ((Number) backendLatency).longValue());
        executionTime.setOtherLatency(otherLatency == null ? 0 : ((Number) otherLatency).longValue());
        executionTime.setRequestMediationLatency(
                reqMediationLatency == null ? 0 : ((Number) reqMediationLatency).longValue());
        executionTime.setResponseMediationLatency(
                resMediationLatency == null ? 0 : ((Number) resMediationLatency).longValue());
        executionTime.setSecurityLatency(securityLatency == null ? 0 : ((Number) securityLatency).longValue());
        executionTime.setThrottlingLatency(throttleLatency == null ? 0 : ((Number) throttleLatency).longValue());
        return executionTime;
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

        String hostname = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDatacenterId();
        if (hostname == null) {
            hostname = (String) messageContext.getProperty(APIMgtGatewayConstants.HOST_NAME);
        }
        return hostname;
    }

    public static String getQualifiedApiName(String apiProviderName, String apiName, String version) {

        return apiProviderName + "--" + apiName + ":v" + version;
    }

    public static String getQualifiedDefaultApiName(String apiProviderName, String apiName) {

        return apiProviderName + "--" + apiName;
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

        if (apiKeyValidationInfoDTO != null) {
            authContext.setApiTier(apiKeyValidationInfoDTO.getApiTier());
            authContext.setKeyType(apiKeyValidationInfoDTO.getType());
            authContext.setApplicationId(apiKeyValidationInfoDTO.getApplicationId());
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
            authContext.setConsumerKey(apiKeyValidationInfoDTO.getConsumerKey());
            authContext.setIsContentAware(apiKeyValidationInfoDTO.isContentAware());
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
            JSONObject
                    applicationObj = payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.APPLICATION);

            authContext
                    .setApplicationId(
                            String.valueOf(applicationObj.getAsNumber(APIConstants.JwtTokenConstants.APPLICATION_ID)));
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
            JSONObject tierInfo = payload.getJSONObjectClaim(APIConstants.JwtTokenConstants.TIER_INFO);
            authContext.setApiName(api.getAsString(APIConstants.JwtTokenConstants.API_NAME));
            authContext.setApiPublisher(api.getAsString(APIConstants.JwtTokenConstants.API_PUBLISHER));
            if (tierInfo.get(subscriptionTier) != null) {
                JSONObject subscriptionTierObj = (JSONObject) tierInfo.get(subscriptionTier);
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
                if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                    Integer graphQLMaxDepth = (int) (long) subscriptionTierObj.get(APIConstants.GRAPHQL_MAX_DEPTH);
                    Integer graphQLMaxComplexity =
                            (int) (long) subscriptionTierObj.get(APIConstants.GRAPHQL_MAX_COMPLEXITY);
                    synCtx.setProperty(APIConstants.MAXIMUM_QUERY_DEPTH, graphQLMaxDepth);
                    synCtx.setProperty(APIConstants.MAXIMUM_QUERY_COMPLEXITY, graphQLMaxComplexity);
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
     * @param apiContext API context
     * @param apiVersion API version
     * @param jwtValidationInfo    The payload of the JWT token
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

        if (payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS) != null) {
            // Subscription validation
            JSONArray subscribedAPIs =
                    (JSONArray) payload.getClaim(APIConstants.JwtTokenConstants.SUBSCRIBED_APIS);
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
                                "version: " + apiVersion + ". Token: " + getMaskedToken(splitToken[0]));
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
     * Verify the JWT token signature.
     *
     * @param jwt SignedJwt Token
     * @param alias      public certificate keystore alias
     * @return whether the signature is verified or or not
     * @throws APISecurityException in case of signature verification failure
     */
    public static boolean verifyTokenSignature(SignedJWT jwt, String alias) throws APISecurityException {

        Certificate publicCert = null;
        //Read the client-truststore.jks into a KeyStore
        try {
            publicCert = APIUtil.getCertificateFromTrustStore(alias);
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
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwt SignedJwt Token
     * @param publicKey      public certificate
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
        jwtInfoDto.setMessageContext(null);
        jwtInfoDto.setApicontext(apiContext);
        jwtInfoDto.setVersion(apiVersion);
        constructJWTContent(null, apiKeyValidationInfoDTO, jwtInfoDto);
        return jwtInfoDto;
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

        Boolean isAppToken = jwtValidationInfo.isAppToken();
        String endUsername = jwtValidationInfo.getUser();
        if (isAppToken != null) {
            if (isAppToken) {
                endUsername = apiKeyValidationInfoDTO.getSubscriber();
                if (!APIConstants.SUPER_TENANT_DOMAIN.equals(apiKeyValidationInfoDTO.getSubscriberTenantDomain())) {
                    return endUsername;
                }
            }
            return endUsername + UserCoreConstants.TENANT_DOMAIN_COMBINER +
                    apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        }
        return endUsername;
    }

    private static void constructJWTContent(JSONObject subscribedAPI,
                                            APIKeyValidationInfoDTO apiKeyValidationInfoDTO, JWTInfoDto jwtInfoDto) {

        if (jwtInfoDto.getJwtValidationInfo() != null) {
            jwtInfoDto.setEnduser(getEndUserFromJWTValidationInfo(jwtInfoDto.getJwtValidationInfo(),
                    apiKeyValidationInfoDTO));
        }

        if (apiKeyValidationInfoDTO != null) {
            jwtInfoDto.setApplicationid(apiKeyValidationInfoDTO.getApplicationId());
            jwtInfoDto.setApplicationname(apiKeyValidationInfoDTO.getApplicationName());
            jwtInfoDto.setApplicationtier(apiKeyValidationInfoDTO.getApplicationTier());
            jwtInfoDto.setKeytype(apiKeyValidationInfoDTO.getType());
            jwtInfoDto.setSubscriber(apiKeyValidationInfoDTO.getSubscriber());
            jwtInfoDto.setSubscriptionTier(apiKeyValidationInfoDTO.getTier());
            jwtInfoDto.setApiName(apiKeyValidationInfoDTO.getApiName());
            jwtInfoDto.setEndusertenantid(
                    APIUtil.getTenantIdFromTenantDomain(apiKeyValidationInfoDTO.getSubscriberTenantDomain()));
            jwtInfoDto.setApplicationuuid(apiKeyValidationInfoDTO.getApplicationUUID());
            jwtInfoDto.setAppAttributes(apiKeyValidationInfoDTO.getAppAttributes());
        } else if (subscribedAPI != null) {
            // If the user is subscribed to the API
            String apiName = subscribedAPI.getAsString(APIConstants.JwtTokenConstants.API_NAME);
            jwtInfoDto.setApiName(apiName);
            String subscriptionTier = subscribedAPI.getAsString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER);
            String subscriptionTenantDomain =
                    subscribedAPI.getAsString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN);
            jwtInfoDto.setSubscriptionTier(subscriptionTier);
            jwtInfoDto.setEndusertenantid(APIUtil.getTenantIdFromTenantDomain(subscriptionTenantDomain));

            Map<String, Object> claims = jwtInfoDto.getJwtValidationInfo().getClaims();
            if (claims.get(APIConstants.JwtTokenConstants.APPLICATION) != null) {
                JSONObject
                        applicationObj = (JSONObject) claims.get(APIConstants.JwtTokenConstants.APPLICATION);
                jwtInfoDto.setApplicationid(
                        String.valueOf(applicationObj.getAsNumber(APIConstants.JwtTokenConstants.APPLICATION_ID)));
                jwtInfoDto
                        .setApplicationname(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_NAME));
                jwtInfoDto
                        .setApplicationtier(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_TIER));
                jwtInfoDto.setSubscriber(applicationObj.getAsString(APIConstants.JwtTokenConstants.APPLICATION_OWNER));
            }
        }
    }

    public static JWTInfoDto generateJWTInfoDto(JSONObject subscribedAPI, JWTValidationInfo jwtValidationInfo,
                                                APIKeyValidationInfoDTO apiKeyValidationInfoDTO,
                                                org.apache.synapse.MessageContext synCtx) {

        JWTInfoDto jwtInfoDto = new JWTInfoDto();
        jwtInfoDto.setJwtValidationInfo(jwtValidationInfo);
        jwtInfoDto.setMessageContext(synCtx);
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        jwtInfoDto.setApicontext(apiContext);
        jwtInfoDto.setVersion(apiVersion);
        constructJWTContent(subscribedAPI, apiKeyValidationInfoDTO, jwtInfoDto);
        return jwtInfoDto;
    }

    public static void setAPIRelatedTags(TracingSpan tracingSpan, org.apache.synapse.MessageContext messageContext) {

        Object electedResource = messageContext.getProperty(APIMgtGatewayConstants.API_ELECTED_RESOURCE);
        if (electedResource != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_RESOURCE, (String) electedResource);
        }
        Object api = messageContext.getProperty(APIMgtGatewayConstants.API);
        if (api != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_NAME, (String) api);
        }
        Object version = messageContext.getProperty(APIMgtGatewayConstants.VERSION);
        if (version != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_API_VERSION, (String) version);
        }
        Object consumerKey = messageContext.getProperty(APIMgtGatewayConstants.CONSUMER_KEY);
        if (consumerKey != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_APPLICATION_CONSUMER_KEY, (String) consumerKey);
        }
    }

    private static void setTracingId(TracingSpan tracingSpan, MessageContext axis2MessageContext) {

        Map headersMap =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headersMap.containsKey(APIConstants.ACTIVITY_ID)) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID,
                    (String) headersMap.get(APIConstants.ACTIVITY_ID));
        } else {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ACTIVITY_ID, axis2MessageContext.getMessageID());
        }
    }

    public static void setRequestRelatedTags(TracingSpan tracingSpan,
                                             org.apache.synapse.MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object restUrlPostfix = axis2MessageContext.getProperty(APIMgtGatewayConstants.REST_URL_POSTFIX);
        String httpMethod = (String) (axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD));
        if (restUrlPostfix != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_PATH, (String) restUrlPostfix);
        }
        if (httpMethod != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_REQUEST_METHOD, httpMethod);
        }
        setTracingId(tracingSpan, axis2MessageContext);
    }

    public static void setEndpointRelatedInformation(TracingSpan tracingSpan,
                                                     org.apache.synapse.MessageContext messageContext) {

        Object endpoint = messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpoint != null) {
            Util.setTag(tracingSpan, APIMgtGatewayConstants.SPAN_ENDPOINT, (String) endpoint);
        }
    }
}
