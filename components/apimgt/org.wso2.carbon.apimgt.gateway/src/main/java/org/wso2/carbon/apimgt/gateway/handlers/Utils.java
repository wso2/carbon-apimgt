/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2Sender;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Caching;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import javax.xml.namespace.QName;

public class Utils {
    
    private static final Log log = LogFactory.getLog(Utils.class);

    public static void sendFault(MessageContext messageContext, int status) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();

        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty("RESPONSE", "true");
        messageContext.setTo(null);        
        axis2MC.removeProperty("NO_ENTITY_BODY");

        // Always remove the ContentType - Let the formatter do its thing
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            headers.remove(HttpHeaders.AUTHORIZATION);

            headers.remove(HttpHeaders.HOST);
        }
        Axis2Sender.sendBack(messageContext);
    }
    
    public static void setFaultPayload(MessageContext messageContext, OMElement payload) {
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        JsonUtil.removeJsonPayload(axis2MC);
        messageContext.getEnvelope().getBody().addChild(payload);
        Map headers = (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String acceptType = (String) headers.get(HttpHeaders.ACCEPT);
        Set<String> supportedMimes = new HashSet<String>(Arrays.asList("application/x-www-form-urlencoded",
                                                                       "multipart/form-data",
                                                                       "text/html",
                                                                       "application/xml",
                                                                       "text/xml",
                                                                       "application/soap+xml",
                                                                       "text/plain",
                                                                       "application/json",
                                                                       "application/json/badgerfish",
                                                                       "text/javascript"));

        // If an Accept header has been provided and is supported by the Gateway
        if(!StringUtils.isEmpty(acceptType) && supportedMimes.contains(acceptType)){
            axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, acceptType);
        } else {
            // If there isn't Accept Header in the request, will use error_message_type property
            // from _auth_failure_handler_.xml file
            if (messageContext.getProperty("error_message_type") != null) {
                axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE,
                                    messageContext.getProperty("error_message_type"));
            }
        }
    }
    
    public static void setSOAPFault(MessageContext messageContext, String code, 
                                    String reason, String detail) {
        SOAPFactory factory = (messageContext.isSOAP11() ?
                OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory());

        OMDocument soapFaultDocument = factory.createOMDocument();
        SOAPEnvelope faultEnvelope = factory.getDefaultFaultEnvelope();
        soapFaultDocument.addChild(faultEnvelope);

        SOAPFault fault = faultEnvelope.getBody().getFault();
        if (fault == null) {
            fault = factory.createSOAPFault();
        }

        SOAPFaultCode faultCode = factory.createSOAPFaultCode();
        if (messageContext.isSOAP11()) {
            faultCode.setText(new QName(fault.getNamespace().getNamespaceURI(), code));
        } else {
            SOAPFaultValue value = factory.createSOAPFaultValue(faultCode);
            value.setText(new QName(fault.getNamespace().getNamespaceURI(), code));            
        }
        fault.setCode(faultCode);

        SOAPFaultReason faultReason = factory.createSOAPFaultReason();
        if (messageContext.isSOAP11()) {
            faultReason.setText(reason);
        } else {
            SOAPFaultText text = factory.createSOAPFaultText();
            text.setText(reason);
            text.setLang("en");
            faultReason.addSOAPText(text);            
        }
        fault.setReason(faultReason);

        SOAPFaultDetail soapFaultDetail = factory.createSOAPFaultDetail();
        soapFaultDetail.setText(detail);
        fault.setDetail(soapFaultDetail);
        
        // set the all headers of original SOAP Envelope to the Fault Envelope
        if (messageContext.getEnvelope() != null) {
            SOAPHeader soapHeader = messageContext.getEnvelope().getHeader();
            if (soapHeader != null) {
                for (Iterator iterator = soapHeader.examineAllHeaderBlocks(); iterator.hasNext();) {
                    Object o = iterator.next();
                    if (o instanceof SOAPHeaderBlock) {
                        SOAPHeaderBlock header = (SOAPHeaderBlock) o;
                        faultEnvelope.getHeader().addChild(header);
                    } else if (o instanceof OMElement) {
                        faultEnvelope.getHeader().addChild((OMElement) o);
                    }
                }
            }
        }

        try {
            messageContext.setEnvelope(faultEnvelope);
        } catch (AxisFault af) {
            log.error("Error while setting SOAP fault as payload", af);
            return;
        }

        if (messageContext.getFaultTo() != null) {
            messageContext.setTo(messageContext.getFaultTo());
        } else if (messageContext.getReplyTo() != null) {
            messageContext.setTo(messageContext.getReplyTo());
        } else {
            messageContext.setTo(null);
        }

        // set original messageID as relatesTo
        if (messageContext.getMessageID() != null) {
            RelatesTo relatesTo = new RelatesTo(messageContext.getMessageID());
            messageContext.setRelatesTo(new RelatesTo[] { relatesTo });
        }
    }
//// moving methods to Util
    /**
     * validates if an accessToken has expired or not
     *
     * @param accessTokenDO
     * @return
     */
    public static boolean hasAccessTokenExpired(APIKeyValidationInfoDTO accessTokenDO) {
        long currentTime;
        long validityPeriod;
        if (accessTokenDO.getValidityPeriod() != Long.MAX_VALUE) {
            validityPeriod = accessTokenDO.getValidityPeriod() * 1000;
        } else {
            validityPeriod = accessTokenDO.getValidityPeriod();
        }
        long issuedTime = accessTokenDO.getIssuedTime();
        //long issuedTime = accessTokenDO.getIssuedTime().getTime();
        currentTime = System.currentTimeMillis();

        //If the validity period is not an never expiring value
        if (validityPeriod != Long.MAX_VALUE) {
            //check the validity of cached OAuth2AccessToken Response
            if ((currentTime) > (issuedTime + validityPeriod)) {
                accessTokenDO.setValidationStatus(
                        APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                if (accessTokenDO.getEndUserToken() != null) {
                    log.info("Token " + accessTokenDO.getEndUserToken() + " expired.");
                }
                return true;
            }
        }


        return false;
    }

    public static String getRequestPath(MessageContext synCtx, String fullRequestPath, String apiContext, String
            apiVersion) {
        String requestPath;
        String versionStrategy = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION_STRATEGY);

        if(VersionStrategyFactory.TYPE_URL.equals(versionStrategy)){
            // most used strategy. server:port/context/version/resource
            requestPath = fullRequestPath.substring((apiContext + apiVersion).length() + 1, fullRequestPath.length());
         }else{
            // default version. assume there is no version is used
            requestPath = fullRequestPath.substring(apiContext.length(), fullRequestPath.length());
        }
        return requestPath;
    }

    /**
     * This method used to send the response back from the request.
     *
     * @param messageContext messageContext of the request
     * @param status         HTTP Status to return from the response
     */
    public static void send(MessageContext messageContext, int status) {
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        axis2MC.setProperty(NhttpConstants.HTTP_SC, status);
        messageContext.setResponse(true);
        messageContext.setProperty(SynapseConstants.RESPONSE, "true");
        messageContext.setTo(null);
        axis2MC.removeProperty(Constants.Configuration.CONTENT_TYPE);
        Axis2Sender.sendBack(messageContext);
    }

    /**
     * Removes the access token that was cached in the tenant's cache space.
     *
     * @param accessToken        - Token to be removed from the cache.
     * @param cachedTenantDomain - Tenant domain from which the token should be removed.
     */
    public static void removeTokenFromTenantTokenCache(String accessToken, String cachedTenantDomain) {
        //If the token is cached in the tenant cache
        if (cachedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(cachedTenantDomain)) {

            if (log.isDebugEnabled()) {
                log.debug("Removing cache entry " + accessToken + " from " + cachedTenantDomain + " domain");
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(cachedTenantDomain, true);
                //Remove the tenant cache entry.
                removeCacheEntryFromGatewayCache(accessToken);
                if (log.isDebugEnabled()) {
                    log.debug("Removed cache entry " + accessToken + " from " + cachedTenantDomain + " domain");
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Removes the apikey that was cached in the tenant's cache space and adds it to the invalid apiKey token cache.
     *
     * @param tokenIdentifier        - Token Identifier to be removed from the cache.
     * @param cachedTenantDomain - Tenant domain from which the apikey should be removed.
     */
    public static void invalidateApiKeyInTenantCache(String tokenIdentifier, String cachedTenantDomain) {
        //If the apiKey is cached in the tenant cache
        if (cachedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(cachedTenantDomain)) {

            if (log.isDebugEnabled()) {
                log.debug("Removing cache entry " + tokenIdentifier + " from " + cachedTenantDomain + " domain");
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(cachedTenantDomain, true);
                removeCacheEntryFromGatewayAPiKeyCache(tokenIdentifier);
                putInvalidApiKeyEntryIntoInvalidApiKeyCache(tokenIdentifier, cachedTenantDomain);
                if (log.isDebugEnabled()) {
                    log.debug("Removed cache entry " + tokenIdentifier + " from " + cachedTenantDomain + " domain");
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Put the access token that was cached in the tenant's cache space into invalid token cache
     *
     * @param accessToken        - Invalid token that should be added to the invalid token cache
     * @param cachedTenantDomain - Tenant domain of the cached token
     */
    public static void putInvalidTokenIntoTenantInvalidTokenCache(String accessToken, String cachedTenantDomain) {
        //If the token was cached in the tenant cache
        if (cachedTenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(cachedTenantDomain)) {

            if (log.isDebugEnabled()) {
                log.debug("Putting the cache entry " + accessToken + " of " + cachedTenantDomain + " domain " +
                        "to the invalid token cache...");
            }
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(cachedTenantDomain, true);
                putInvalidTokenEntryIntoInvalidTokenCache(accessToken, cachedTenantDomain);
                if (log.isDebugEnabled()) {
                    log.debug(" Token " + accessToken + " of " + cachedTenantDomain + " domain was put to the " +
                            "invalid token cache.");
                }
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Remove a token from gateway token cache
     *
     * @param key Access token which should be removed from the cache
     */
    public static void removeCacheEntryFromGatewayCache(String key) {
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME)
                .remove(key);
    }

    /**
     * Remove a token from gateway API Key token cache
     *
     * @param key signature of JWT token which should be removed from the cache
     */
    public static void removeCacheEntryFromGatewayAPiKeyCache(String key) {
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.GATEWAY_API_KEY_CACHE_NAME)
                .remove(key);
    }

    /**
     * Add a token to the invalid token cache of the given tenant domain
     *
     * @param cachedToken   Access token to be added to the invalid token cache
     * @param tenantDomain  Tenant domain of the token
     */
    public static void putInvalidTokenEntryIntoInvalidTokenCache(String cachedToken, String tenantDomain) {
        Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants
                .GATEWAY_INVALID_TOKEN_CACHE_NAME).put(cachedToken, tenantDomain);
    }

    /**
     * Add a token identifier to the invalid apikey cache of the given tenant domain
     *
     * @param tokenIdentifier   Token identifier to be added to the invalid token cache
     * @param tenantDomain  Tenant domain of the apikey
     */
    public static void putInvalidApiKeyEntryIntoInvalidApiKeyCache(String tokenIdentifier, String tenantDomain) {
        CacheProvider.getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
    }

    /**
     * Get the tenant domain of a cached token
     *
     * @param token Cached access token
     * @return Tenant domain
     */
    public static String getCachedTenantDomain(String token) {
        return (String) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                .getCache(APIConstants.GATEWAY_TOKEN_CACHE_NAME).get(token);
    }

    /**
     * Get the tenant domain of a cached api key
     *
     * @param token Cached access token
     * @return Tenant domain
     */
    public static String getApiKeyCachedTenantDomain(String token) {
        return (String) CacheProvider.getGatewayApiKeyCache().get(token);
    }

    public static String getClientCertificateHeader() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String clientCertificateHeader =
                    apiManagerConfiguration.getFirstProperty(APIConstants.MutualSSL.CLIENT_CERTIFICATE_HEADER);
            if (StringUtils.isNotEmpty(clientCertificateHeader)) {
                return clientCertificateHeader;
            }
        }
        return APIMgtGatewayConstants.BASE64_ENCODED_CLIENT_CERTIFICATE_HEADER;
    }

    public static X509Certificate getClientCertificate(org.apache.axis2.context.MessageContext axis2MessageContext)
            throws APIManagementException {

        Map headers =
                (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Object sslCertObject = axis2MessageContext.getProperty(NhttpConstants.SSL_CLIENT_AUTH_CERT_X509);
        X509Certificate certificateFromMessageContext = null;
        if (sslCertObject != null) {
            X509Certificate[] certs = (X509Certificate[]) sslCertObject;
            certificateFromMessageContext = certs[0];
        }
        if (headers.containsKey(Utils.getClientCertificateHeader())) {
            try {
                if (!isClientCertificateValidationEnabled() || APIUtil
                        .isCertificateExistsInTrustStore(certificateFromMessageContext)){
                        String certificate = (String) headers.get(Utils.getClientCertificateHeader());
                        byte[] bytes;
                        if (certificate != null) {
                            if (!isClientCertificateEncoded()) {
                                certificate = certificate
                                        .replaceAll(APIConstants.BEGIN_CERTIFICATE_STRING, "")
                                        .replaceAll(APIConstants.BEGIN_CERTIFICATE_STRING_SPACE, "")
                                        .replaceAll(APIConstants.END_CERTIFICATE_STRING, "");
                                certificate = certificate.replaceAll(" ", "\n");
                                certificate = APIConstants.BEGIN_CERTIFICATE_STRING + certificate
                                        + APIConstants.END_CERTIFICATE_STRING;
                                bytes = certificate.getBytes();
                            } else {
                                certificate = URLDecoder.decode(certificate)
                                        .replaceAll(APIConstants.BEGIN_CERTIFICATE_STRING, "")
                                        .replaceAll(APIConstants.END_CERTIFICATE_STRING, "");
                                bytes = Base64.decodeBase64(certificate);
                            }
                        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                            X509Certificate x509Certificate = X509Certificate.getInstance(inputStream);
                            if (APIUtil.isCertificateExistsInTrustStore(x509Certificate)) {
                                return x509Certificate;
                            }else{
                                log.debug("Certificate in Header didn't exist in truststore");
                                return null;
                            }
                        } catch (IOException | CertificateException | APIManagementException e) {
                            String msg = "Error while converting into X509Certificate";
                            log.error(msg, e);
                            throw new APIManagementException(msg, e);
                        }
                    }
                }
            } catch (APIManagementException e) {
                String msg = "Error while validating into Certificate Existence";
                log.error(msg, e);
                throw new APIManagementException(msg, e);

            }
        }
        return certificateFromMessageContext;
    }
    private static boolean isClientCertificateValidationEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String firstProperty = apiManagerConfiguration
                    .getFirstProperty(APIConstants.MutualSSL.ENABLE_CLIENT_CERTIFICATE_VALIDATION);
            return Boolean.parseBoolean(firstProperty);
        }
        return false;
    }

    private static boolean isClientCertificateEncoded() {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String firstProperty = apiManagerConfiguration
                    .getFirstProperty(APIConstants.MutualSSL.CLIENT_CERTIFICATE_ENCODE);
            if (firstProperty != null) {
                return Boolean.parseBoolean(firstProperty);
            } else {
                return true;
            }
        }
        return true;
    }

    /**
     * Populate custom properties define in a mediation sequence
     *
     * @param messageContext MessageContext
     * @return Map<String, String> with custom properties
     */
    public static Map<String, String> getCustomAnalyticsProperties(MessageContext messageContext) {
        Map<String, String> requestProperties = getCustomAnalyticsProperties(messageContext,
                APIMgtGatewayConstants.CUSTOM_ANALYTICS_REQUEST_PROPERTIES);
        Map<String, String> responseProperties = getCustomAnalyticsProperties(messageContext,
                APIMgtGatewayConstants.CUSTOM_ANALYTICS_RESPONSE_PROPERTIES);
        Map<String, String> properties = new HashMap<>(requestProperties);
        properties.putAll(responseProperties);
        return properties;
    }

    private static Map<String, String> getCustomAnalyticsProperties(MessageContext messageContext,
            String propertyPathKey) {
        Set<String> keys = messageContext.getPropertyKeySet();
        String properties = (String) messageContext.getProperty(propertyPathKey);
        if (StringUtils.isBlank(properties)) {
            return Collections.emptyMap();
        }
        Map<String, String> propertyMap = new HashMap<>();
        String[] propertyKeys = properties.split(APIMgtGatewayConstants.CUSTOM_ANALYTICS_PROPERTY_SEPARATOR);
        for (String propertyKey : propertyKeys) {
            if (keys.contains(propertyKey.trim())) {
                propertyMap.put(propertyKey, (String) messageContext.getProperty(propertyKey.trim()));
            }
        }
        return propertyMap;
    }

    public static API getSelectedAPI(MessageContext messageContext) {

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        Object apiObject = messageContext.getProperty(RESTConstants.PROCESSED_API);
        if (apiObject != null) {
            return (API) apiObject;
        } else {
            return messageContext.getConfiguration().getAPI(apiName);
        }
    }

    public static String getEncodedClientCertificate(X509Certificate certificate) throws CertificateEncodingException {
        byte[] encoded = Base64.encodeBase64(certificate.getEncoded());
        if (isClientCertificateEncoded()) {
            String base64EncodedString = APIConstants.BEGIN_CERTIFICATE_STRING.concat(new String(encoded)).concat("\n"
            ).concat(APIConstants.END_CERTIFICATE_STRING);
            return Base64.encodeBase64URLSafeString(base64EncodedString.getBytes());
        } else {
            return APIConstants.BEGIN_CERTIFICATE_STRING_SPACE.concat(new String(encoded)).concat(" ").concat(APIConstants.END_CERTIFICATE_STRING);
        }
    }
}
