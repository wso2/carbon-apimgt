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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimeDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
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

    /**
     * Extracts the IP from Message Context.
     *
     * @param messageContext Axis2 Message Context.
     * @return IP as a String.
     */
    public static String getIp(MessageContext messageContext) {

        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) messageContext
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
     * @throws APIManagementException
     */
    public static void deleteRegistryProperty(String propertyName, String path, String tenantDomain)
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
            if (resource != null && resource.getProperty(propertyName) != null) {
                resource.removeProperty(propertyName);
                registry.put(resource.getPath(), resource);
                resource.discard();
            }
        } catch (RegistryException e) {
            throw new APIManagementException("Error while reading registry resource " + path + " for tenant " +
                    tenantDomain);
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
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_MSG, APIMgtGatewayConstants.BAD_REQUEST);
        messageContext.setProperty(APIMgtGatewayConstants.THREAT_DESC, desc);
        Mediator sequence = messageContext.getSequence(APIMgtGatewayConstants.THREAT_FAULT);
        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
        }
        return true;
    }

    /**
     * This method use to clone the InputStream from the the message context. Basically
     * clone the request body.
     * @param messageContext contains the message properties of the relevant API request which was
     *                       enabled the regexValidator message mediation in flow.
     * @return cloned InputStreams.
     * @throws IOException this exception might occurred while cloning the inputStream.
     */
    public static Map<String,InputStream> cloneRequestMessage(org.apache.synapse.MessageContext messageContext)
            throws IOException {
        BufferedInputStream bufferedInputStream = null;
        Map<String, InputStream> inputStreamMap = null;
        InputStream inputStreamSchema;
        InputStream inputStreamXml;
        InputStream inputStreamJSON;
        InputStream inputStreamOriginal;
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
        if (bufferedInputStream != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[requestBufferSize];
            int length;
            while ((length = bufferedInputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byteArrayOutputStream.flush();
            inputStreamMap = new HashMap<>();
            inputStreamSchema = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            inputStreamXml = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            inputStreamOriginal = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            inputStreamJSON = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            inputStreamMap.put(ThreatProtectorConstants.SCHEMA, inputStreamSchema);
            inputStreamMap.put(ThreatProtectorConstants.XML, inputStreamXml);
            inputStreamMap.put(ThreatProtectorConstants.ORIGINAL, inputStreamOriginal);
            inputStreamMap.put(ThreatProtectorConstants.JSON, inputStreamJSON);
        }
        return  inputStreamMap;
    }

    /**
     *  This method use to set the originInput stream to the message Context
     * @param inputStreams cloned InputStreams
     * @param axis2MC axis2 message context
     */
    public static void setOriginalInputStream(Map <String, InputStream> inputStreams,
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

    /**
     * This method extracts the endpoint address base path if query parameters are contained in endpoint
     * @param mc The message context
     * @return The endpoint address base path
     */
    public static String extractAddressBasePath(org.apache.synapse.MessageContext mc) {
        String endpointAddress = (String) mc.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpointAddress.contains("?")) {
            endpointAddress = endpointAddress.substring(0, endpointAddress.indexOf("?"));
        }
        return endpointAddress;
    }
}
