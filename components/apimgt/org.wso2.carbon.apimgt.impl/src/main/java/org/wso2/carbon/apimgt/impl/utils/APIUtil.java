/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.DeprecatedRuntimeConstants;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.xerces.util.SecurityManager;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtAuthorizationFailedException;
import org.wso2.carbon.apimgt.api.APIMgtInternalException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.LoginPostExecutor;
import org.wso2.carbon.apimgt.api.NewPostLoginExecutor;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.api.PasswordResolver;
import org.wso2.carbon.apimgt.api.doc.model.APIDefinition;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.doc.model.Operation;
import org.wso2.carbon.apimgt.api.doc.model.Parameter;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.model.WebsubSubscriptionConfiguration;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.api.model.policy.EventCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.Limit;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.api.quotalimiter.OnPremQuotaLimiter;
import org.wso2.carbon.apimgt.api.quotalimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.JWTSignatureAlg;
import org.wso2.carbon.apimgt.common.gateway.util.CommonAPIUtil;
import org.wso2.carbon.apimgt.eventing.EventPublisher;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherException;
import org.wso2.carbon.apimgt.eventing.EventPublisherFactory;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIType;
import org.wso2.carbon.apimgt.impl.ExternalEnvironment;
import org.wso2.carbon.apimgt.impl.IDPConfiguration;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.CorrelationConfigDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.DataLoadingException;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIDConnectDiscoveryClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIdConnectConfiguration;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.resolver.OnPremResolver;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.ExceptionException;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfo;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.secure.AuthorizationFailedException;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.security.cert.X509Certificate;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This class contains the utility methods used by the implementations of APIManager, APIProvider
 * and APIConsumer interfaces.
 */
public final class APIUtil {

    private static final Log log = LogFactory.getLog(APIUtil.class);

    private static final Log audit = CarbonConstants.AUDIT_LOG;

    private static boolean isContextCacheInitialized = false;

    public static final String DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION = "disableRoleValidationAtScopeCreation";

    public static final String DISABLE_API_CONTEXT_VALIDATION = "disableApiContextValidation";

    private static final int ENTITY_EXPANSION_LIMIT = 0;

    private static final String DESCRIPTION = "Allows [1] request(s) per minute.";

    private static final int DEFAULT_TENANT_IDLE_MINS = 30;
    private static long tenantIdleTimeMillis;
    private static Set<String> currentLoadingTenants = new HashSet<String>();

    private static volatile Set<String> allowedScopes;
    private static boolean isPublisherRoleCacheEnabled = true;

    public static final String STRICT = "Strict";
    public static final String ALLOW_ALL = "AllowAll";
    public static final String DEFAULT_AND_LOCALHOST = "DefaultAndLocalhost";
    public static final String HOST_NAME_VERIFIER = "httpclient.hostnameVerifier";
    public static String multiGrpAppSharing = null;

    private static final String CONFIG_ELEM_OAUTH = "OAuth";
    private static final String REVOKE = "revoke";
    private static final String TOKEN = "token";

    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    private static final String NONE = "NONE";
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + APIConstants.SUPER_TENANT_DOMAIN;

    private static final int IPV4_ADDRESS_BIT_LENGTH = 32;
    private static final int IPV6_ADDRESS_BIT_LENGTH = 128;

    private static Schema tenantConfigJsonSchema;
    private static Schema operationPolicySpecSchema;
    private static final String contextRegex = "^[a-zA-Z0-9_${}/.;()-]+$";

    private APIUtil() {

    }

    //Need tenantIdleTime to check whether the tenant is in idle state in loadTenantConfig method
    static {
        tenantIdleTimeMillis =
                Long.parseLong(System.getProperty(
                        org.wso2.carbon.utils.multitenancy.MultitenantConstants.TENANT_IDLE_TIME,
                        String.valueOf(DEFAULT_TENANT_IDLE_MINS)))
                        * 60 * 1000;
        try (InputStream inputStream = APIAdminImpl.class.getResourceAsStream("/tenant/tenant-config-schema.json")) {
            org.json.JSONObject tenantConfigSchema = new org.json.JSONObject(IOUtils.toString(inputStream));
            tenantConfigJsonSchema = SchemaLoader.load(tenantConfigSchema);
        } catch (IOException e) {
            log.error("Error occurred while reading tenant-config-schema.json", e);
        }

        try (InputStream inputStream = APIUtil.class.getResourceAsStream("/operationPolicy/operation-policy-specification-schema.json")) {
            org.json.JSONObject operationPolicySpecificationSchema = new org.json.JSONObject(IOUtils.toString(inputStream));
            operationPolicySpecSchema = SchemaLoader.load(operationPolicySpecificationSchema);
        } catch (IOException e) {
            log.error("Error occurred while reading operation-policy-specification-schema.json", e);
        }
    }

    private static String hostAddress = null;
    private static final int timeoutInSeconds = 15;
    private static final int retries = 2;
    private static long retrievalTimeout;
    private static final long maxRetrievalTimeout = 1000 * 60 * 60;
    private static double retryProgressionFactor;
    private static String gatewayTypes;
    private static int maxRetryCount;

    //constants for getting masked token
    private static final int MAX_LEN = 36;
    private static final int MAX_VISIBLE_LEN = 8;
    private static final int MIN_VISIBLE_LEN_RATIO = 5;
    private static final String MASK_CHAR = "X";

    private static final Map<EventPublisherType, EventPublisher> eventPublishers =
            new EnumMap<>(EventPublisherType.class);
    private static EventPublisherFactory eventPublisherFactory = null;

    /**
     * To initialize the publisherRoleCache configurations, based on configurations.
     */
    public static void init() throws APIManagementException {

        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isPublisherRoleCacheEnabledConfiguration = apiManagerConfiguration
                .getFirstProperty(APIConstants.PUBLISHER_ROLE_CACHE_ENABLED);
        isPublisherRoleCacheEnabled = isPublisherRoleCacheEnabledConfiguration == null || Boolean
                .parseBoolean(isPublisherRoleCacheEnabledConfiguration);
        retrievalTimeout = apiManagerConfiguration.getGatewayArtifactSynchronizerProperties().getRetryDuartion();
        maxRetryCount = apiManagerConfiguration.getGatewayArtifactSynchronizerProperties().getMaxRetryCount();
        retryProgressionFactor = apiManagerConfiguration.getGatewayArtifactSynchronizerProperties()
                .getRetryProgressionFactor();
        gatewayTypes = apiManagerConfiguration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
        try {
            eventPublisherFactory = ServiceReferenceHolder.getInstance().getEventPublisherFactory();
            eventPublishers.putIfAbsent(EventPublisherType.ASYNC_WEBHOOKS,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.ASYNC_WEBHOOKS));
            eventPublishers.putIfAbsent(EventPublisherType.CACHE_INVALIDATION,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.CACHE_INVALIDATION));
            eventPublishers.putIfAbsent(EventPublisherType.GLOBAL_CACHE_INVALIDATION,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.GLOBAL_CACHE_INVALIDATION));
            eventPublishers.putIfAbsent(EventPublisherType.NOTIFICATION,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.NOTIFICATION));
            eventPublishers.putIfAbsent(EventPublisherType.TOKEN_REVOCATION,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.TOKEN_REVOCATION));
            eventPublishers.putIfAbsent(EventPublisherType.BLOCKING_EVENT,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.BLOCKING_EVENT));
            eventPublishers.putIfAbsent(EventPublisherType.KEY_TEMPLATE,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.KEY_TEMPLATE));
            eventPublishers.putIfAbsent(EventPublisherType.KEYMGT_EVENT,
                    eventPublisherFactory.getEventPublisher(EventPublisherType.KEYMGT_EVENT));
        } catch (EventPublisherException e) {
            log.error("Could not initialize the event publishers. Events might not be published properly.");
            throw new APIManagementException(e);
        }
    }

    /**
     * This method used to retrieve the api resource dependencies
     *
     * @param api      api object
     * @throws APIManagementException
     */
    public static void updateAPIProductDependencies(API api) throws APIManagementException {

        for (URITemplate uriTemplate : api.getUriTemplates()) {
            Set<APIProductIdentifier> usedByProducts = uriTemplate.retrieveUsedByProducts();
            for (APIProductIdentifier usedByProduct : usedByProducts) {
                //TODO : removed registry call until find a proper fix
                String apiProductPath = APIUtil.getAPIProductPath(usedByProduct);
                usedByProduct.setUuid(apiProductPath);
            }
        }
    }

    /**
     * This method is used to execute an HTTP request
     *
     * @param method       HttpRequest Type
     * @param httpClient   HttpClient
     * @return HTTPResponse
     * @throws IOException
     */
    public static CloseableHttpResponse executeHTTPRequest(HttpRequestBase method, HttpClient httpClient)
            throws IOException, APIManagementException {
        CloseableHttpResponse httpResponse = null;
        int retryCount = 0;
        boolean retry;
        do {
            try {
                httpResponse = (CloseableHttpResponse) httpClient.execute(method);
                retry = false;
            } catch (IOException ex) {
                retryCount++;
                if (retryCount < retries) {
                    retry = true;
                    log.warn("Failed retrieving from remote endpoint: " + ex.getMessage()
                            + ". Retrying after " + timeoutInSeconds +
                            " seconds.");
                    try {
                        Thread.sleep(timeoutInSeconds * 1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                } else {
                    throw ex;
                }
            }
        } while (retry);
        return httpResponse;
    }

    /**
     * This method is used to execute an HTTP request with retry parameters obtained from configuration parameters.
     *
     * @param method       HttpRequest Type
     * @param httpClient   HttpClient
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse executeHTTPRequestWithRetries(HttpRequestBase method, HttpClient httpClient)
            throws IOException, APIManagementException {

        CloseableHttpResponse httpResponse = null;
        String path = method.getURI().getPath();
        long retryDuration = retrievalTimeout;
        int retryCount = 0;
        boolean retry;
        do {
            try {
                httpResponse = (CloseableHttpResponse) httpClient.execute(method);
                if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode()) {
                    throw new DataLoadingException("Error while retrieving "
                            + path + ". Received response with status code "
                            + httpResponse.getStatusLine().getStatusCode());
                } else if (retryCount > 0) {
                    log.info("Successfully retrieved " + path);
                }
                retry = false;
            } catch (IOException | DataLoadingException ex) {
                retryCount++;
                if (retryCount <= maxRetryCount) {
                    retry = true;
                    log.error("Failed to retrieve " + path + " from remote endpoint: " + ex.getMessage()
                            + ". Retry attempt " + retryCount + " in " + (retryDuration / 1000) +
                            " seconds.");
                    try {
                        Thread.sleep(retryDuration);
                        retryDuration = (long) (retryDuration * retryProgressionFactor);
                        if (retryDuration > maxRetrievalTimeout) {
                            retryDuration = maxRetrievalTimeout;
                        }
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                } else {
                    log.error("Failed to retrieve " + path + " from remote endpoint. Maximum retry count exceeded."
                            + ex.getMessage());
                    throw ex;
                }
            }
        } while (retry);
        return httpResponse;
    }

    /**
     * This method used to send Notifications
     *
     * @param event        Event object
     * @param notifierType eventType
     */
    public static void sendNotification(org.wso2.carbon.apimgt.impl.notifier.events.Event event, String notifierType) {

        if (ServiceReferenceHolder.getInstance().getNotifiersMap().containsKey(notifierType)) {
            List<Notifier> notifierList = ServiceReferenceHolder.getInstance().getNotifiersMap().get(notifierType);
            notifierList.forEach((notifier) -> {
                try {
                    notifier.publishEvent(event);
                } catch (NotifierException e) {
                    log.error("Error when publish " + event + " through notifier:" + notifierType + ". Error:" + e);
                }
            });
        }

    }

    /**
     * This method used to extract environment list configured with non empty URLs.
     *
     * @param endpointConfigs (Eg: {"production_endpoints":{"url":"http://www.test.com/v1/xxx","config":null,
     *                        "template_not_supported":false},"endpoint_type":"http"})
     * @return Set<String>
     */
    public static Set<String> extractEnvironmentListForAPI(String endpointConfigs)
            throws ParseException, ClassCastException {

        Set<String> environmentList = new HashSet<String>();
        if (StringUtils.isNotBlank(endpointConfigs) && !"null".equals(endpointConfigs)) {
            JSONParser parser = new JSONParser();
            JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfigs);
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_PRODUCTION_ENDPOINTS) &&
                    isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_PRODUCTION_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_PRODUCTION);
            }
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_SANDBOX_ENDPOINTS) &&
                    isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_SANDBOX_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_SANDBOX);
            }
        }
        return environmentList;
    }

    /**
     * This method used to check whether the endpoints JSON object has a non empty URL.
     *
     * @param endpoints (Eg: {"url":"http://www.test.com/v1/xxx","config":null,"template_not_supported":false})
     * @return boolean
     */
    public static boolean isEndpointURLNonEmpty(Object endpoints) {

        if (endpoints instanceof JSONObject) {
            JSONObject endpointJson = (JSONObject) endpoints;
            if (endpointJson.containsKey(APIConstants.API_DATA_URL) &&
                    endpointJson.get(APIConstants.API_DATA_URL) != null) {
                String url = (endpointJson.get(APIConstants.API_DATA_URL)).toString();
                if (StringUtils.isNotBlank(url)) {
                    return true;
                }
            }
        } else if (endpoints instanceof JSONArray) {
            JSONArray endpointsJson = (JSONArray) endpoints;
            for (int i = 0; i < endpointsJson.size(); i++) {
                if (isEndpointURLNonEmpty(endpointsJson.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static API getAPI(GovernanceArtifact artifact)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
            int apiId = ApiMgtDAO.getInstance().getAPIID(artifact.getId());
            if (apiId == -1) {
                return null;
            }
            //set uuid
            api.setUUID(artifact.getId());
            api.setRating(getAverageRating(apiId));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setEnableStore(Boolean.parseBoolean(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
            api.setTestKey(artifact.getAttribute(APIConstants.API_OVERVIEW_TESTKEY));
            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }
            api.setCacheTimeout(cacheTimeout);

            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
            api.setApiLevelPolicy(apiLevelTier);

            Set<Tier> availablePolicy = new HashSet<Tier>();
            String[] subscriptionPolicy = ApiMgtDAO.getInstance().getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB, replaceEmailDomainBack(providerName));
            List<String> definedPolicyNames = Arrays.asList(subscriptionPolicy);
            String policies = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            if (policies != null && !"".equals(policies)) {
                String[] policyNames = policies.split("\\|\\|");
                for (String policyName : policyNames) {
                    if (definedPolicyNames.contains(policyName) || APIConstants.UNLIMITED_TIER.equals(policyName)) {
                        Tier p = new Tier(policyName);
                        availablePolicy.add(p);
                    } else {
                        log.warn("Unknown policy: " + policyName + " found on API: " + apiName);
                    }
                }
            }

            api.addAvailableTiers(availablePolicy);
            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            api.setMonetizationCategory(getAPIMonetizationCategory(availablePolicy, tenantDomainName));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));

            ArrayList<URITemplate> urlPatternsList;
            urlPatternsList = ApiMgtDAO.getInstance().getAllURITemplates(api.getContext(), api.getId().getVersion());
            Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);

            for (URITemplate uriTemplate : uriTemplates) {
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());

            }
            api.setUriTemplates(uriTemplates);
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));
            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiKeyHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_API_KEY_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));

            //get endpoint config string from artifact, parse it as a json and set the environment list configured with
            //non empty URLs to API object
            try {
                api.setEnvironmentList(extractEnvironmentListForAPI(
                        artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)));
            } catch (ParseException e) {
                String msg = "Failed to parse endpoint config JSON of API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (ClassCastException e) {
                String msg = "Invalid endpoint config JSON found in API: " + apiName + " " + apiVersion;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        } catch (GovernanceException e) {
            String msg = "Failed to get API from artifact ";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * This method is used to get an API in the Light Weight manner.
     *
     * @param artifact generic artfact
     * @return this will return an API for the selected artifact.
     * @throws APIManagementException , if invalid json config for the API or Api cannot be retrieved from the artifact
     */
    public static API getLightWeightAPI(GovernanceArtifact artifact)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
            int apiId = ApiMgtDAO.getInstance().getAPIID(artifact.getId());
            if (apiId == -1) {
                return null;
            }
            //set uuid
            api.setUUID(artifact.getId());
            api.setRating(getAverageRating(apiId));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }
            api.setCacheTimeout(cacheTimeout);
            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
            api.setApiLevelPolicy(apiLevelTier);

            Set<Tier> availablePolicy = new HashSet<Tier>();
            String[] subscriptionPolicy = ApiMgtDAO.getInstance().getPolicyNames(PolicyConstants.POLICY_LEVEL_SUB,
                    replaceEmailDomainBack(providerName));
            List<String> definedPolicyNames = Arrays.asList(subscriptionPolicy);
            String policies = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            if (!StringUtils.isEmpty(policies)) {
                String[] policyNames = policies.split("\\|\\|");
                for (String policyName : policyNames) {
                    if (definedPolicyNames.contains(policyName) || APIConstants.UNLIMITED_TIER.equals(policyName)) {
                        Tier p = new Tier(policyName);
                        availablePolicy.add(p);
                    } else {
                        log.warn("Unknown policy: " + policyName + " found on API: " + apiName);
                    }
                }
            }
            api.addAvailableTiers(availablePolicy);
            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            api.setMonetizationCategory(getAPIMonetizationCategory(availablePolicy, tenantDomainName));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));
            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));
            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));
            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            try {
                api.setEnvironmentList(extractEnvironmentListForAPI(
                        artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG)));
            } catch (ParseException e) {
                String msg = "Failed to parse endpoint config JSON of API: " + apiName + " " + apiVersion;
                throw new APIManagementException(msg, e);
            } catch (ClassCastException e) {
                String msg = "Invalid endpoint config JSON found in API: " + apiName + " " + apiVersion;
                throw new APIManagementException(msg, e);
            }
        } catch (GovernanceException e) {
            String msg = "Failed to get API from artifact";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * This method used to get Provider from provider artifact
     *
     * @param artifact provider artifact
     * @return Provider
     * @throws APIManagementException if failed to get Provider from provider artifact.
     */
    public static Provider getProvider(GenericArtifact artifact) throws APIManagementException {

        Provider provider;
        try {
            provider = new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
            provider.setDescription(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_DESCRIPTION));
            provider.setEmail(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_EMAIL));

        } catch (GovernanceException e) {
            String msg = "Failed to get provider ";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return provider;
    }

    /**
     * Create Governance artifact from given attributes
     *
     * @param artifact initial governance artifact
     * @param api      API object with the attributes value
     * @return GenericArtifact generic artifact
     * @throws APIManagementException the api management exception
     */
    public static GenericArtifact createAPIArtifactContent(GenericArtifact artifact, API api)
            throws APIManagementException {

        try {
            String apiStatus = api.getStatus();
            artifact.setAttribute(APIConstants.API_OVERVIEW_NAME, api.getId().getApiName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, api.getId().getVersion());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_COMPARABLE, api.getVersionTimestamp());

            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT, api.getContext());
            artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, api.getId().getProviderName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WADL, api.getWadlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, api.getThumbnailUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, apiStatus);
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER, api.getTechnicalOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL, api.getTechnicalOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER, api.getBusinessOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, api.getBusinessOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBILITY, api.getVisibility());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES, api.getVisibleRoles());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS, api.getVisibleTenants());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED, Boolean.toString(api.isEndpointSecured()));
            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST, Boolean.toString(api.isEndpointAuthDigest()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME, api.getEndpointUTUsername());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD, api.getEndpointUTPassword());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TRANSPORTS, api.getTransports());
            artifact.setAttribute(APIConstants.API_OVERVIEW_INSEQUENCE, api.getInSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE, api.getOutSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE, api.getFaultSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING, api.getResponseCache());
            artifact.setAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT, Integer.toString(api.getCacheTimeout()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL, api.getRedirectURL());
            artifact.setAttribute(APIConstants.API_OVERVIEW_EXTERNAL_PRODUCTION_ENDPOINT,
                    api.getApiExternalProductionEndpoint());
            artifact.setAttribute(APIConstants.API_OVERVIEW_EXTERNAL_SANDBOX_ENDPOINT,
                    api.getApiExternalSandboxEndpoint());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OWNER, api.getApiOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY, Boolean.toString(api.isAdvertiseOnly()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG, api.getEndpointConfig());

            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY, api.getSubscriptionAvailability());
            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS, api.getSubscriptionAvailableTenants());

            artifact.setAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION, api.getImplementation());

            artifact.setAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS, api.getProductionMaxTps());
            artifact.setAttribute(APIConstants.API_SANDBOX_THROTTLE_MAXTPS, api.getSandboxMaxTps());
            artifact.setAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER, api.getAuthorizationHeader());
            artifact.setAttribute(APIConstants.API_OVERVIEW_API_KEY_HEADER, api.getApiKeyHeader());
            artifact.setAttribute(APIConstants.API_OVERVIEW_API_SECURITY, api.getApiSecurity());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA,
                    Boolean.toString(api.isEnabledSchemaValidation()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE, Boolean.toString(api.isEnableStore()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_TESTKEY, api.getTestKey());

            //Validate if the API has an unsupported context before setting it in the artifact
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                String invalidContext = File.separator + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(api.getContextTemplate())) {
                    throw new APIManagementException(
                            "API : " + api.getId() + " has an unsupported context : " + api.getContextTemplate());
                }
            } else {
                String invalidContext =
                        APIConstants.TENANT_PREFIX + tenantDomain + File.separator + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(api.getContextTemplate())) {
                    throw new APIManagementException(
                            "API : " + api.getId() + " has an unsupported context : " + api.getContextTemplate());
                }
            }
            // This is to support the pluggable version strategy.
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE, api.getContextTemplate());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context");
            artifact.setAttribute(APIConstants.API_OVERVIEW_TYPE, api.getType());

            StringBuilder policyBuilder = new StringBuilder();
            for (Tier tier : api.getAvailableTiers()) {
                policyBuilder.append(tier.getName());
                policyBuilder.append("||");
            }

            String policies = policyBuilder.toString();

            if (!"".equals(policies)) {
                policies = policies.substring(0, policies.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, policies);
            }

            StringBuilder tiersBuilder = new StringBuilder();
            for (Tier tier : api.getAvailableTiers()) {
                tiersBuilder.append(tier.getName());
                tiersBuilder.append("||");
            }

            String tiers = tiersBuilder.toString();

            if (!"".equals(tiers)) {
                tiers = tiers.substring(0, tiers.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, tiers);
            } else {
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, tiers);
            }

            if (APIConstants.PUBLISHED.equals(apiStatus)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true");
            }
            String[] keys = artifact.getAttributeKeys();
            for (String key : keys) {
                if (key.contains("URITemplate")) {
                    artifact.removeAttribute(key);
                }
            }

            Set<URITemplate> uriTemplateSet = api.getUriTemplates();
            int i = 0;
            for (URITemplate uriTemplate : uriTemplateSet) {
                artifact.addAttribute(APIConstants.API_URI_PATTERN + i, uriTemplate.getUriTemplate());
                artifact.addAttribute(APIConstants.API_URI_HTTP_METHOD + i, uriTemplate.getHTTPVerb());
                artifact.addAttribute(APIConstants.API_URI_AUTH_TYPE + i, uriTemplate.getAuthType());

                i++;

            }
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS, writeEnvironmentsToArtifact(api));

            artifact.setAttribute(APIConstants.API_OVERVIEW_CORS_CONFIGURATION,
                    APIUtil.getCorsConfigurationJsonFromDto(api.getCorsConfiguration()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_WEBSUB_SUBSCRIPTION_CONFIGURATION,
                    APIUtil.getWebsubSubscriptionConfigurationJsonFromDto(api.getWebsubSubscriptionConfiguration()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_WS_URI_MAPPING, APIUtil.getWsUriMappingJsonFromDto(api.getWsUriMapping()));

            //attaching api categories to the API
            List<APICategory> attachedApiCategories = api.getApiCategories();
            artifact.removeAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME);
            if (attachedApiCategories != null) {
                for (APICategory category : attachedApiCategories) {
                    artifact.addAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME, category.getName());
                }
            }

            //set monetization status (i.e - enabled or disabled)
            artifact.setAttribute(
                    APIConstants.Monetization.API_MONETIZATION_STATUS, Boolean.toString(api.getMonetizationStatus()));
            //set additional monetization data
            if (api.getMonetizationProperties() != null) {
                artifact.setAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES,
                        api.getMonetizationProperties().toJSONString());
            }
            if (api.getKeyManagers() != null) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_KEY_MANAGERS, new Gson().toJson(api.getKeyManagers()));
            }

            //check in github code to see this method was removed
            String apiSecurity = artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY);
            if (apiSecurity != null && !apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) &&
                    !apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, "");
            }
        } catch (GovernanceException e) {
            String msg = "Failed to create API for : " + api.getId().getApiName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * Create Governance artifact from given attributes
     *
     * @param artifact   initial governance artifact
     * @param apiProduct APIProduct object with the attributes value
     * @return GenericArtifact
     * @throws APIManagementException if failed to create API Product
     */
    public static GenericArtifact createAPIProductArtifactContent(GenericArtifact artifact, APIProduct apiProduct)
            throws APIManagementException {

        try {
            //todo : review and add missing fields
            artifact.setAttribute(APIConstants.API_OVERVIEW_NAME, apiProduct.getId().getName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, apiProduct.getId().getVersion());
            artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, apiProduct.getId().getProviderName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT, apiProduct.getContext());
            artifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, apiProduct.getDescription());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TYPE, APIConstants.AuditLogConstants.API_PRODUCT);
            artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, apiProduct.getState());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBILITY, apiProduct.getVisibility());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES, apiProduct.getVisibleRoles());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS, apiProduct.getVisibleTenants());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER, apiProduct.getBusinessOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, apiProduct.getBusinessOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER, apiProduct.getTechnicalOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL, apiProduct.getTechnicalOwnerEmail());
            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY, apiProduct.getSubscriptionAvailability());
            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS, apiProduct.getSubscriptionAvailableTenants());
            artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, apiProduct.getThumbnailUrl());
            artifact.setAttribute(
                    APIConstants.API_OVERVIEW_CACHE_TIMEOUT, Integer.toString(apiProduct.getCacheTimeout()));

            StringBuilder policyBuilder = new StringBuilder();
            for (Tier tier : apiProduct.getAvailableTiers()) {
                policyBuilder.append(tier.getName());
                policyBuilder.append("||");
            }

            String policies = policyBuilder.toString();

            if (!"".equals(policies)) {
                policies = policies.substring(0, policies.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, policies);
            } else {
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, policies);
            }

            artifact.setAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS, writeEnvironmentsToArtifact(apiProduct));
            artifact.setAttribute(APIConstants.API_OVERVIEW_TRANSPORTS, apiProduct.getTransports());
            artifact.setAttribute(APIConstants.API_OVERVIEW_CORS_CONFIGURATION,
                    APIUtil.getCorsConfigurationJsonFromDto(apiProduct.getCorsConfiguration()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER, apiProduct.getAuthorizationHeader());
            artifact.setAttribute(APIConstants.API_OVERVIEW_API_KEY_HEADER, apiProduct.getApiKeyHeader());
            artifact.setAttribute(APIConstants.API_OVERVIEW_API_SECURITY, apiProduct.getApiSecurity());

            //Validate if the API has an unsupported context before setting it in the artifact
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                String invalidContext = File.separator + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(apiProduct.getContextTemplate())) {
                    throw new APIManagementException("API : " + apiProduct.getId() + " has an unsupported context : " +
                            apiProduct.getContextTemplate());
                }
            } else {
                String invalidContext =
                        APIConstants.TENANT_PREFIX + tenantDomain + File.separator + APIConstants.VERSION_PLACEHOLDER;
                if (invalidContext.equals(apiProduct.getContextTemplate())) {
                    throw new APIManagementException("API : " + apiProduct.getId() + " has an unsupported context : " +
                            apiProduct.getContextTemplate());
                }
            }

            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA, Boolean.toString(apiProduct.
                    isEnabledSchemaValidation()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE, Boolean.toString(apiProduct.isEnableStore()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING, apiProduct.getResponseCache());
            // This is to support the pluggable version strategy.
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE, apiProduct.getContextTemplate());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context");

            //set monetization status (i.e - enabled or disabled)
            artifact.setAttribute(
                    APIConstants.Monetization.API_MONETIZATION_STATUS, Boolean.toString(apiProduct.getMonetizationStatus()));
            //set additional monetization data
            if (apiProduct.getMonetizationProperties() != null) {
                artifact.setAttribute(APIConstants.Monetization.API_MONETIZATION_PROPERTIES,
                        apiProduct.getMonetizationProperties().toJSONString());
            }

            //attaching api categories to the API
            List<APICategory> attachedApiCategories = apiProduct.getApiCategories();
            artifact.removeAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME);
            if (attachedApiCategories != null) {
                for (APICategory category : attachedApiCategories) {
                    artifact.addAttribute(APIConstants.API_CATEGORIES_CATEGORY_NAME, category.getName());
                }
            }
        } catch (GovernanceException e) {
            String msg = "Failed to create API for : " + apiProduct.getId().getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact) throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setId(artifact.getId());
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));
            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

            if (visibilityAttr != null) {
                if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                    documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                    documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                    documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.URL;
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.FILE;
                documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(APIConstants.DOC_FILE_PATH)));
            } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.MARKDOWN;
            }
            documentation.setSourceType(docSourceType);
            if (documentation.getType() == DocumentationType.OTHER) {
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact, String docCreatorName)
            throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setId(artifact.getId());
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));

            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
            if (visibilityAttr != null) {
                if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                    documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                    documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                    documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (artifactAttribute.equals(Documentation.DocumentSourceType.MARKDOWN.name())) {
                docSourceType = Documentation.DocumentSourceType.MARKDOWN;
            } else if (artifactAttribute.equals(Documentation.DocumentSourceType.URL.name())) {
                docSourceType = Documentation.DocumentSourceType.URL;
            } else if (artifactAttribute.equals(Documentation.DocumentSourceType.FILE.name())) {
                docSourceType = Documentation.DocumentSourceType.FILE;
            }

            documentation.setSourceType(docSourceType);
            if ("URL".equals(artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE))) {
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            }

            if (docSourceType == Documentation.DocumentSourceType.FILE) {
                String filePath = prependTenantPrefix(artifact.getAttribute(APIConstants.DOC_FILE_PATH), docCreatorName);
                documentation.setFilePath(prependWebContextRoot(filePath));
            }

            if (documentation.getType() == DocumentationType.OTHER) {
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact: " + e);
        }
        return documentation;
    }

    public static APIStatus getApiStatus(String status) throws APIManagementException {

        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }
        }
        return apiStatus;
    }

    public static String getLcStateFromArtifact(GovernanceArtifact artifact) throws GovernanceException {
        String lcState = artifact.getLifecycleState();
        String state = (lcState != null) ? lcState : artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
        return (state != null) ? state.toUpperCase() : null;
    }

    /**
     * Prepends the Tenant Prefix to a registry path. ex: /t/test1.com
     *
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he Tenant domain prefix.
     */
    public static String prependTenantPrefix(String postfixUrl, String username) {

        String tenantDomain = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(username));
        if (!(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain))) {
            String tenantPrefix = "/t/";
            postfixUrl = tenantPrefix + tenantDomain + postfixUrl;
        }

        return postfixUrl;
    }

    /**
     * Prepends the webcontextroot to a registry path.
     *
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he WebContext root.
     */
    public static String prependWebContextRoot(String postfixUrl) {

        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (webContext != null && !"/".equals(webContext)) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }

    /**
     * Utility method for creating storage path for an icon.
     *
     * @param identifier Identifier
     * @return Icon storage path.
     */
    public static String getIconPath(Identifier identifier) {

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        return artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
    }

    /**
     * Utility method for get registry path for wsdl archive.
     *
     * @param identifier APIIdentifier
     * @return wsdl archive path
     */
    public static String getWsdlArchivePath(APIIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                replaceEmailDomain(identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + RegistryConstants.PATH_SEPARATOR + APIConstants.API_WSDL_ARCHIVE_LOCATION +
                identifier.getProviderName() + APIConstants.WSDL_PROVIDER_SEPERATOR + identifier.getApiName() +
                identifier.getVersion() + APIConstants.ZIP_FILE_EXTENSION;
    }

    /**
     * Utility method to generate the path for a file.
     *
     * @param identifier APIIdentifier
     * @return Generated path.
     * @fileName File name.
     */
    public static String getDocumentationFilePath(Identifier identifier, String fileName) {

        if (identifier instanceof APIIdentifier) {
            return APIUtil.getAPIDocPath((APIIdentifier) identifier) + APIConstants.DOCUMENT_FILE_DIR +
                    RegistryConstants.PATH_SEPARATOR + fileName;
        } else {
            return APIUtil.getProductDocPath((APIProductIdentifier) identifier) + APIConstants.DOCUMENT_FILE_DIR +
                    RegistryConstants.PATH_SEPARATOR + fileName;
        }
    }

    public static String getOpenAPIDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR;
    }

    public static String getRevisionPath(String apiUUID, int revisionId) {
        return APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiUUID +
                RegistryConstants.PATH_SEPARATOR + revisionId +
                RegistryConstants.PATH_SEPARATOR;
    }

    public static String getAsyncAPIDefinitionFilePath(String apiName, String apipVersion, String apiProvider) {
        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apipVersion + RegistryConstants.PATH_SEPARATOR;
    }

    public static String getGraphqlDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR;
    }

    public static String getAPIProductOpenAPIDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR;
    }

    public static String getWSDLDefinitionFilePath(String apiName, String apiVersion, String apiProvider) {
        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                replaceEmailDomain(apiProvider) + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR +
                apiVersion + RegistryConstants.PATH_SEPARATOR + apiProvider + "--" + apiName + apiVersion + ".wsdl";
    }

    /**
     * Utility method to get OpenAPI registry path for API product
     *
     * @param identifier product identifier
     * @return path path to the
     */
    public static String getAPIProductOpenAPIDefinitionFilePath(APIProductIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName() +
                RegistryConstants.PATH_SEPARATOR + identifier.getName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Utility method to get api path from APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return API path
     */
    public static String getAPIPath(APIIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                replaceEmailDomain(identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
    }

    /**
     * Utility method to get api product path from APIProductIdentifier
     *
     * @param identifier APIProductIdentifier
     * @return APIProduct path
     */
    public static String getAPIProductPath(APIProductIdentifier identifier) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                replaceEmailDomain(identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR +
                identifier.getName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
    }

    /**
     * Utility method for creating storage path for an api product icon.
     *
     * @param identifier APIProductIdentifier
     * @return Icon storage path.
     */
    public static String getProductIconPath(APIProductIdentifier identifier) {

        String artifactPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion() +
                RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
        return artifactPath;
    }

    /**
     * Utility method to get api identifier from api path.
     *
     * @param apiPath Path of the API in registry
     * @return relevant API Identifier
     */
    public static APIIdentifier getAPIIdentifier(String apiPath) {

        int length = (APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR).length();
        if (!apiPath.contains(APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR)) {
            length = (APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR).length();
        }
        if (length <= 0) {
            length = (APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR).length();
        }
        String relativePath = apiPath.substring(length);
        String[] values = relativePath.split(RegistryConstants.PATH_SEPARATOR);
        if (values.length > 3) {
            return new APIIdentifier(values[0], values[1], values[2]);
        }
        return null;
    }

    /**
     * Utility method to get API provider path
     *
     * @param identifier APIIdentifier
     * @return API provider path
     */
    public static String getAPIProviderPath(APIIdentifier identifier) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName();
    }

    /**
     * Utility method to get API Product provider path
     *
     * @param identifier APIProductIdentifier
     * @return API Product provider path
     */
    public static String getAPIProductProviderPath(APIProductIdentifier identifier) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR + identifier.getProviderName();
    }

    /**
     * Utility method to get documentation path
     *
     * @param id Identifier (API or API Product)
     * @return Doc path
     */
    public static String getAPIOrAPIProductDocPath(Identifier id) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                id.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                id.getName() + RegistryConstants.PATH_SEPARATOR +
                id.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Utility method to get documentation path of the revision
     *
     * @param apiUUID  API UUID
     * @param revisionId revision id
     * @return Doc path
     */
    public static String getAPIOrAPIProductRevisionDocPath(String apiUUID, int revisionId) {
        return APIConstants.API_REVISION_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiUUID +
                RegistryConstants.PATH_SEPARATOR + revisionId +
                RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Utility method to get documentation path
     *
     * @param apiId APIIdentifier
     * @return Doc path
     */
    public static String getAPIDocPath(APIIdentifier apiId) {

        return getAPIOrAPIProductDocPath(apiId);
    }

    /**
     * Utility method to get documentation content file path
     *
     * @param apiId             APIIdentifier
     * @param documentationName String
     * @return Doc content path
     */
    public static String getAPIDocContentPath(APIIdentifier apiId, String documentationName) {

        return getAPIDocPath(apiId) + RegistryConstants.PATH_SEPARATOR + documentationName;
    }

    /**
     * This utility method used to create documentation artifact content
     *
     * @param artifact      GovernanceArtifact
     * @param id            Identifier
     * @param documentation Documentation
     * @return GenericArtifact
     * @throws APIManagementException if failed to get GovernanceArtifact from Documentation
     */
    public static GenericArtifact createDocArtifactContent(GenericArtifact artifact, Identifier id,
                                                           Documentation documentation) throws APIManagementException {

        try {
            artifact.setAttribute(APIConstants.DOC_NAME, documentation.getName());
            artifact.setAttribute(APIConstants.DOC_SUMMARY, documentation.getSummary());
            artifact.setAttribute(APIConstants.DOC_TYPE, documentation.getType().getType());
            artifact.setAttribute(APIConstants.DOC_VISIBILITY, documentation.getVisibility().name());

            Documentation.DocumentSourceType sourceType = documentation.getSourceType();

            switch (sourceType) {
                case INLINE:
                    sourceType = Documentation.DocumentSourceType.INLINE;
                    break;
                case MARKDOWN:
                    sourceType = Documentation.DocumentSourceType.MARKDOWN;
                    break;
                case URL:
                    sourceType = Documentation.DocumentSourceType.URL;
                    break;
                case FILE: {
                    sourceType = Documentation.DocumentSourceType.FILE;
                }
                break;
                default:
                    throw new APIManagementException("Unknown sourceType " + sourceType + " provided for documentation");
            }
            //Documentation Source URL is a required field in the documentation.rxt for migrated setups
            //Therefore setting a default value if it is not set.
            if (documentation.getSourceUrl() == null) {
                documentation.setSourceUrl(" ");
            }
            artifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, sourceType.name());
            artifact.setAttribute(APIConstants.DOC_SOURCE_URL, documentation.getSourceUrl());
            artifact.setAttribute(APIConstants.DOC_FILE_PATH, documentation.getFilePath());
            artifact.setAttribute(APIConstants.DOC_OTHER_TYPE_NAME, documentation.getOtherTypeName());
            String basePath = id.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                    id.getName() + RegistryConstants.PATH_SEPARATOR + id.getVersion();
            artifact.setAttribute(APIConstants.DOC_API_BASE_PATH, basePath);
        } catch (GovernanceException e) {
            String msg = "Failed to create doc artifact content from :" + documentation.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key) throws APIManagementException {

        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, key);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + key +
                        ". Tenant id set in registry : " + ((UserRegistry) registry).getTenantId() +
                        ", Tenant domain set in PrivilegedCarbonContext: " +
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
    }

    public static void handleException(String msg) throws APIManagementException {

        log.error(msg);
        throw new APIManagementException(msg);
    }

    public static void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static void handleInternalException(String msg, Throwable t) throws APIMgtInternalException {

        log.error(msg, t);
        throw new APIMgtInternalException(msg, t);
    }

    public static void handleResourceAlreadyExistsException(String msg) throws APIMgtResourceAlreadyExistsException {

        log.error(msg);
        throw new APIMgtResourceAlreadyExistsException(msg);
    }

    public static void handleResourceNotFoundException(String msg) throws APIMgtResourceNotFoundException {

        log.error(msg);
        throw new APIMgtResourceNotFoundException(msg);
    }

    public static void handleAuthFailureException(String msg) throws APIMgtAuthorizationFailedException {

        log.error(msg);
        throw new APIMgtAuthorizationFailedException(msg);
    }

    /**
     * Method used to create the file name of the wsdl to be stored in the registry
     *
     * @param provider   Name of the provider of the API
     * @param apiName    Name of the API
     * @param apiVersion API Version
     * @return WSDL file name
     */
    public static String createWsdlFileName(String provider, String apiName, String apiVersion) {

        return provider + "--" + apiName + apiVersion + ".wsdl";
    }

    /**
     * Get the External IDP host name when UIs use an external IDP for SSO or other purpose
     * By default this is equal to $ref{server.base_path} (i:e https://localhost:9443)
     *
     * @return Origin string of the external IDP
     */
    public static String getExternalIDPOrigin() throws APIManagementException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String idpEndpoint = config.getFirstProperty(APIConstants.IDENTITY_PROVIDER_SERVER_URL);
        if (idpEndpoint == null) {
            return getServerURL();
        } else {
            return idpEndpoint;
        }
    }

    /**
     * Get the check session URL to load in the session management iframe
     *
     * @return URL to be used in iframe source for the check session with IDP
     */
    public static String getExternalIDPCheckSessionEndpoint() throws APIManagementException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String oidcCheckSessionEndpoint = config.getFirstProperty(
                APIConstants.IDENTITY_PROVIDER_OIDC_CHECK_SESSION_ENDPOINT);
        if (oidcCheckSessionEndpoint == null) {
            return getServerURL() + APIConstants.IDENTITY_PROVIDER_OIDC_CHECK_SESSION_URL;
        } else {
            return oidcCheckSessionEndpoint;
        }
    }

    /**
     * Read the GateWay Endpoint from the APIConfiguration. If multiple Gateway
     * environments defined,
     * take only the production node's Endpoint.
     * Else, pick what is available as the gateway node.
     *
     * @return {@link String} - Gateway URL
     */

    public static String getGatewayendpoint(String transports, String organization) throws APIManagementException {

        String gatewayURLs;

        Map<String, Environment> gatewayEnvironments = getEnvironments(organization);
        if (gatewayEnvironments.size() > 1) {
            for (Environment environment : gatewayEnvironments.values()) {
                if (APIConstants.GATEWAY_ENV_TYPE_HYBRID.equals(environment.getType())) {
                    gatewayURLs = environment.getApiGatewayEndpoint(); // This might have http,https
                    // pick correct endpoint
                    return APIUtil.extractHTTPSEndpoint(gatewayURLs, transports);
                }
            }
            for (Environment environment : gatewayEnvironments.values()) {
                if (APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType())) {
                    gatewayURLs = environment.getApiGatewayEndpoint(); // This might have http,https
                    // pick correct endpoint
                    return APIUtil.extractHTTPSEndpoint(gatewayURLs, transports);
                }
            }
            for (Environment environment : gatewayEnvironments.values()) {
                if (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environment.getType())) {
                    gatewayURLs = environment.getApiGatewayEndpoint(); // This might have http,https
                    // pick correct endpoint
                    return APIUtil.extractHTTPSEndpoint(gatewayURLs, transports);
                }
            }
        } else {
            gatewayURLs = ((Environment) gatewayEnvironments.values().toArray()[0]).getApiGatewayEndpoint();
            return extractHTTPSEndpoint(gatewayURLs, transports);
        }

        return null;
    }

    /**
     * Read the GateWay Endpoint from the APIConfiguration. If multiple Gateway
     * environments defined, get the gateway endpoint according to the environment type
     *
     * @param transports      transports allowed for gateway endpoint
     * @param environmentName gateway environment name
     * @param environmentType gateway environment type
     * @return Gateway URL
     */
    public static String getGatewayEndpoint(String transports, String environmentName, String environmentType,
                                            String organization)
            throws APIManagementException {

        String gatewayURLs;
        String gatewayEndpoint = "";

        Map<String, Environment> gatewayEnvironments = getEnvironments(organization);
        Environment environment = gatewayEnvironments.get(environmentName);
        if (environment.getType().equals(environmentType)) {
            gatewayURLs = environment.getApiGatewayEndpoint();
            gatewayEndpoint = extractHTTPSEndpoint(gatewayURLs, transports);
            if (log.isDebugEnabled()) {
                log.debug("Gateway urls are: " + gatewayURLs + " and the url with the correct transport is: "
                        + gatewayEndpoint);
            }
        } else {
            handleException("Environment type mismatch for environment: " + environmentName +
                    " for the environment types: " + environment.getType() + " and " + environmentType);
        }
        return gatewayEndpoint;
    }

    /**
     * Gateway endpoint  has HTTP and HTTPS endpoints.
     * If both are defined pick HTTPS only. Else, pick whatever available.
     * eg: <GatewayEndpoint>http://${carbon.local.ip}:${http.nio.port},
     * https://${carbon.local.ip}:${https.nio.port}</GatewayEndpoint>
     *
     * @param gatewayURLs - String contains comma separated gateway urls.
     * @return {@link String} - Returns HTTPS gateway endpoint
     */

    private static String extractHTTPSEndpoint(String gatewayURLs, String transports) {

        String gatewayURL;
        String gatewayHTTPURL = null;
        String gatewayHTTPSURL = null;
        boolean httpsEnabled = false;
        String[] gatewayURLsArray = gatewayURLs.split(",");
        String[] transportsArray = transports.split(",");

        for (String transport : transportsArray) {
            if (transport.startsWith(APIConstants.HTTPS_PROTOCOL)) {
                httpsEnabled = true;
            }
        }
        if (gatewayURLsArray.length > 1) {
            for (String url : gatewayURLsArray) {
                if (url.startsWith("https:")) {
                    gatewayHTTPSURL = url;
                } else {
                    if (!url.startsWith("ws:")) {
                        gatewayHTTPURL = url;
                    }
                }
            }

            if (httpsEnabled) {
                gatewayURL = gatewayHTTPSURL;
            } else {
                gatewayURL = gatewayHTTPURL;
            }
        } else {
            gatewayURL = gatewayURLs;
        }
        return gatewayURL;
    }

    /**
     * Create an Endpoint
     *
     * @param endpointUrl Endpoint url
     * @param registry    Registry space to save the endpoint
     * @return Path of the created resource
     * @throws APIManagementException If an error occurs while adding the endpoint
     */
    public static String createEndpoint(String endpointUrl, Registry registry) throws APIManagementException {

        try {
            EndpointManager endpointManager = new EndpointManager(registry);
            Endpoint endpoint = endpointManager.newEndpoint(endpointUrl);
            endpointManager.addEndpoint(endpoint);
            return GovernanceUtils.getArtifactPath(registry, endpoint.getId());
        } catch (RegistryException e) {
            String msg = "Failed to import endpoint " + endpointUrl + " to registry ";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Sorts the list of tiers according to the number of requests allowed per minute in each tier in descending order.
     *
     * @param tiers - The list of tiers to be sorted
     * @return - The sorted list.
     */
    public static List<Tier> sortTiers(Set<Tier> tiers) {

        List<Tier> tierList = new ArrayList<Tier>();
        tierList.addAll(tiers);
        Collections.sort(tierList);
        return tierList;
    }

    /**
     * Returns a set of External API Stores as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Set<APIStore> getExternalStores(int tenantId) throws APIManagementException {
        // First checking if ExternalStores are defined in api-manager.xml
        Set<APIStore> externalAPIStores = getGlobalExternalStores();
        // If defined, return Store Config provided there.
        if (externalAPIStores != null && !externalAPIStores.isEmpty()) {
            return externalAPIStores;
        }
        // Else Read the config from Tenant's Registry.
        externalAPIStores = new HashSet<>();
        try {
            Iterator apiStoreIterator = getExternalStoresIteratorFromConfig(tenantId);
            if (apiStoreIterator != null) {
                while (apiStoreIterator.hasNext()) {
                    APIStore store = new APIStore();
                    OMElement storeElem = (OMElement) apiStoreIterator.next();
                    String type = storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_TYPE));
                    String className =
                            storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_CLASS_NAME));
                    store.setPublisher((APIPublisher) getClassInstance(className));
                    store.setType(type); //Set Store type [eg:wso2]
                    String name = storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_ID));
                    if (name == null) {
                        log.error("The ExternalAPIStore name attribute is not defined in external-api-stores.xml.");
                    }
                    store.setName(name); //Set store name
                    OMElement configDisplayName = storeElem.getFirstChildWithName
                            (new QName(APIConstants.EXTERNAL_API_STORE_DISPLAY_NAME));
                    String displayName = (configDisplayName != null) ? replaceSystemProperty(
                            configDisplayName.getText()) : name;
                    store.setDisplayName(displayName);//Set store display name
                    store.setEndpoint(replaceSystemProperty(storeElem.getFirstChildWithName(
                            new QName(APIConstants.EXTERNAL_API_STORE_ENDPOINT)).getText()));
                    //Set store endpoint, which is used to publish APIs
                    store.setPublished(false);
                    if (APIConstants.WSO2_API_STORE_TYPE.equals(type)) {
                        OMElement password = storeElem.getFirstChildWithName(new QName(
                                APIConstants.EXTERNAL_API_STORE_PASSWORD));
                        if (password != null) {

                            String value = password.getText();
                            PasswordResolver passwordResolver = PasswordResolverFactory.getInstance();
                            store.setPassword(replaceSystemProperty(passwordResolver.getPassword(value)));
                            store.setUsername(replaceSystemProperty(storeElem.getFirstChildWithName(
                                    new QName(APIConstants.EXTERNAL_API_STORE_USERNAME)).getText()));
                            //Set store login username
                        } else {
                            log.error("The user-credentials of API Publisher is not defined in the <ExternalAPIStore> " +
                                    "config of external-api-stores.xml.");
                        }
                    }
                    externalAPIStores.add(store);
                }
            }
        } catch (ClassNotFoundException e) {
            String msg = "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be found";
            throw new APIManagementException(msg, e);
        } catch (InstantiationException e) {
            String msg = "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be load";
            throw new APIManagementException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be access";
            throw new APIManagementException(msg, e);
        }
        return externalAPIStores;
    }

    /**
     * Get OMElement iterator for external stores configured in external-store.xml in tenant registry.
     *
     * @param tenantId Tenant ID
     * @return ExternalStores OMElement Iterator
     * @throws APIManagementException If an error occurs while reading external-store.xml
     */
    private static Iterator getExternalStoresIteratorFromConfig(int tenantId) throws APIManagementException {

        Iterator apiStoreIterator = null;
        try {
            String content =
                    ServiceReferenceHolder.getInstance().getApimConfigService().getExternalStoreConfig(getTenantDomainFromTenantId(tenantId));
            OMElement element = AXIOMUtil.stringToOM(content);
            apiStoreIterator = element.getChildrenWithLocalName("ExternalAPIStore");

        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return apiStoreIterator;
    }

    /**
     * Check if external stores are configured and exists for given tenant.
     *
     * @param tenantDomain Tenant Domain of logged in user
     * @return Whether external stores are configured and non empty
     */
    public static boolean isExternalStoresEnabled(String tenantDomain) throws APIManagementException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        //First check external stores are present globally
        Set<APIStore> globalExternalStores = getGlobalExternalStores();
        if (globalExternalStores != null && !globalExternalStores.isEmpty()) {
            return true;
        }
        //If not present check in registry
        Iterator apiStoreIterator = getExternalStoresIteratorFromConfig(tenantId);
        return apiStoreIterator != null && apiStoreIterator.hasNext();
    }

    /**
     * Get external stores configured globally in api-manager.xml.
     *
     * @return Globally configured external store set
     */
    public static Set<APIStore> getGlobalExternalStores() {
        // First checking if ExternalStores are defined in api-manager.xml
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getExternalAPIStores();
    }

    /**
     * Check if document visibility levels are enabled
     *
     * @return True if document visibility levels are enabled
     */
    public static boolean isDocVisibilityLevelsEnabled() {
        // checking if Doc visibility levels enabled in api-manager.xml
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(
                APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS).equals("true");
    }

    /**
     * Returns the External API Store Configuration with the given Store Name
     *
     * @param apiStoreName
     * @return
     * @throws APIManagementException
     */
    public static APIStore getExternalAPIStore(String apiStoreName, int tenantId) throws APIManagementException {

        Set<APIStore> externalAPIStoresConfig = APIUtil.getExternalStores(tenantId);
        for (APIStore apiStoreConfig : externalAPIStoresConfig) {
            if (apiStoreConfig.getName().equals(apiStoreName)) {
                return apiStoreConfig;
            }
        }
        return null;
    }

    /**
     * Returns an unfiltered map of API availability tiers as defined in the underlying governance
     * registry.
     *
     * @return Map<String ,   Tier> an unfiltered Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAllTiers() throws APIManagementException {

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns an unfiltered map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @return Map<String ,   Tier> an unfiltered Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAllTiers(int tenantId) throws APIManagementException {

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);

    }

    /**
     * Returns a map of API availability tiers as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getTiers() throws APIManagementException {

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns a map of API availability tiers as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAdvancedSubsriptionTiers() throws APIManagementException {

        return getAdvancedSubsriptionTiers(MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns a map of API subscription tiers of the tenant as defined in database
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAdvancedSubsriptionTiers(int tenantId) throws APIManagementException {

        return APIUtil.getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
    }

    /**
     * Returns a map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getTiers(int tenantId) throws APIManagementException {

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
    }

    /**
     * Returns a map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @param tierType type of the tiers
     * @param organization identifier of the organization
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getTiers(int tierType, String organization) throws APIManagementException {
        int tenantId = APIUtil.getInternalOrganizationId(organization);
        if (tierType == APIConstants.TIER_API_TYPE) {
            return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
        } else if (tierType == APIConstants.TIER_RESOURCE_TYPE) {
            return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_API, tenantId);
        } else if (tierType == APIConstants.TIER_APPLICATION_TYPE) {
            return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_APP, tenantId);
        } else {
            throw new APIManagementException("No such a tier type : " + tierType);
        }
    }

    /**
     * Retrieves unfiltered list of all available tiers from registry.
     * Result will contains all the tiers including unauthenticated tier which is
     * filtered out in   getTiers}
     *
     * @param registry registry
     * @param tierLocation registry location of tiers config
     * @return Map<String, Tier> containing all available tiers
     * @throws RegistryException      when registry action fails
     * @throws XMLStreamException     when xml parsing fails
     * @throws APIManagementException when fails to retrieve tier attributes
     */
    private static Map<String, Tier> getAllTiers(Registry registry, String tierLocation, int tenantId)
            throws RegistryException, XMLStreamException, APIManagementException {
        // We use a treeMap here to keep the order
        Map<String, Tier> tiers = new TreeMap<String, Tier>();

        if (registry.resourceExists(tierLocation)) {
            Resource resource = registry.get(tierLocation);
            String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());

            OMElement element = AXIOMUtil.stringToOM(content);
            OMElement assertion = element.getFirstChildWithName(APIConstants.ASSERTION_ELEMENT);
            Iterator policies = assertion.getChildrenWithName(APIConstants.POLICY_ELEMENT);

            while (policies.hasNext()) {
                OMElement policy = (OMElement) policies.next();
                OMElement id = policy.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT);

                String tierName = id.getText();

                // Constructing the tier object
                Tier tier = new Tier(tierName);
                tier.setPolicyContent(policy.toString().getBytes(Charset.defaultCharset()));

                if (id.getAttribute(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT) != null) {
                    tier.setDisplayName(id.getAttributeValue(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT));
                } else {
                    tier.setDisplayName(tierName);
                }
                String desc;
                try {
                    long requestPerMin = APIDescriptionGenUtil.getAllowedCountPerMinute(policy);
                    tier.setRequestsPerMin(requestPerMin);

                    long requestCount = APIDescriptionGenUtil.getAllowedRequestCount(policy);
                    tier.setRequestCount(requestCount);

                    long unitTime = APIDescriptionGenUtil.getTimeDuration(policy);
                    tier.setUnitTime(unitTime);

                    if (requestPerMin >= 1) {
                        desc = DESCRIPTION.replaceAll("\\[1\\]", Long.toString(requestPerMin));
                    } else {
                        desc = DESCRIPTION;
                    }
                    tier.setDescription(desc);

                } catch (APIManagementException ex) {
                    // If there is any issue in getting the request counts or the time duration, that means this tier
                    // information can not be used for throttling. Hence we log this exception and continue the flow
                    // to the next tier.
                    log.warn("Unable to get the request count/time duration information for : " + tier.getName() + ". "
                            + ex.getMessage());
                    continue;
                }

                // Get all the attributes of the tier.
                Map<String, Object> tierAttributes = APIDescriptionGenUtil.getTierAttributes(policy);
                if (!tierAttributes.isEmpty()) {
                    // The description, billing plan and the stop on quota reach properties are also stored as attributes
                    // of the tier attributes. Hence we extract them from the above attributes map.
                    Iterator<Entry<String, Object>> attributeIterator = tierAttributes.entrySet().iterator();
                    while (attributeIterator.hasNext()) {
                        Entry<String, Object> entry = attributeIterator.next();

                        if (APIConstants.THROTTLE_TIER_DESCRIPTION_ATTRIBUTE.equals(entry.getKey())
                                && entry.getValue() instanceof String) {

                            tier.setDescription((String) entry.getValue());

                            // We remove the attribute from the map
                            attributeIterator.remove();
                            continue;

                        }
                        if (APIConstants.THROTTLE_TIER_PLAN_ATTRIBUTE.equals(entry.getKey())
                                && entry.getValue() instanceof String) {

                            tier.setTierPlan((String) entry.getValue());

                            // We remove the attribute from the map
                            attributeIterator.remove();
                            continue;

                        }
                        if (APIConstants.THROTTLE_TIER_QUOTA_ACTION_ATTRIBUTE.equals(entry.getKey())
                                && entry.getValue() instanceof String) {

                            tier.setStopOnQuotaReached(Boolean.parseBoolean((String) entry.getValue()));

                            // We remove the attribute from the map
                            attributeIterator.remove();
                            // We do not need a continue since this is the last statement.

                        }
                    }
                    tier.setTierAttributes(tierAttributes);
                }
                tiers.put(tierName, tier);
            }
        }

        if (isEnabledUnlimitedTier()) {
            Tier tier = new Tier(APIConstants.UNLIMITED_TIER);
            tier.setDescription(APIConstants.UNLIMITED_TIER_DESC);
            tier.setDisplayName(APIConstants.UNLIMITED_TIER);
            tier.setRequestsPerMin(Long.MAX_VALUE);

            if (isUnlimitedTierPaid(getTenantDomainFromTenantId(tenantId))) {
                tier.setTierPlan(APIConstants.COMMERCIAL_TIER_PLAN);
            } else {
                tier.setTierPlan(APIConstants.BILLING_PLAN_FREE);
            }

            tiers.put(tier.getName(), tier);
        }

        return tiers;
    }

    /**
     * Retrieves filtered list of available tiers from registry. This method will not return Unauthenticated
     * tier in the list. Use  to retrieve all tiers without
     * any filtering.
     *
     * @param registry     registry to access tiers config
     * @param tierLocation registry location of tiers config
     * @return map containing available tiers
     * @throws APIManagementException when fails to retrieve tier attributes
     */
    private static Map<String, Tier> getTiers(Registry registry, String tierLocation, int tenantId) throws APIManagementException {

        Map<String, Tier> tiers = null;
        try {
            tiers = getAllTiers(registry, tierLocation, tenantId);
            tiers.remove(APIConstants.UNAUTHENTICATED_TIER);
        } catch (RegistryException e) {
            handleException(APIConstants.MSG_TIER_RET_ERROR, e);
        } catch (XMLStreamException e) {
            handleException(APIConstants.MSG_MALFORMED_XML_ERROR, e);
        } catch (APIManagementException e) {
            handleException("Unable to get tier attributes", e);
        } catch (Exception e) {

            // generic exception is caught to catch exceptions thrown from map remove method
            handleException("Unable to remove Unauthenticated tier from tiers list", e);
        }
        return tiers;
    }

    /**
     * Checks whether the specified user has the specified permission.
     *
     * @param userNameWithoutChange A username
     * @param permission            A valid Carbon permission
     * @throws APIManagementException If the user does not have the specified permission or if an error occurs
     */
    public static boolean hasPermission(String userNameWithoutChange, String permission)
            throws APIManagementException {

        boolean authorized = false;
        if (userNameWithoutChange == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                    " the anonymous user");
        }

        if (isPermissionCheckDisabled()) {
            log.debug("Permission verification is disabled by APIStore configuration");
            authorized = true;
            return authorized;
        }

        if (APIConstants.Permissions.APIM_ADMIN.equals(permission)) {
            Integer value = getValueFromCache(APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE, userNameWithoutChange);
            if (value != null) {
                return value == 1;
            }
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userNameWithoutChange);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);

                org.wso2.carbon.user.api.AuthorizationManager manager =
                        ServiceReferenceHolder.getInstance()
                                .getRealmService()
                                .getTenantUserRealm(tenantId)
                                .getAuthorizationManager();
                authorized =
                        manager.isUserAuthorized(MultitenantUtils.getTenantAwareUsername(userNameWithoutChange), permission,
                                CarbonConstants.UI_PERMISSION_ACTION);
            if (APIConstants.Permissions.APIM_ADMIN.equals(permission)) {
                addToRolesCache(APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE, userNameWithoutChange,
                        authorized ? 1 : 2);
            }

        } catch (UserStoreException e) {
            throw new APIManagementException("Error while checking the user:" + userNameWithoutChange + " authorized or not", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return authorized;
    }

    /**
     * Checks whether the disablePermissionCheck parameter enabled
     *
     * @return boolean
     */
    public static boolean isPermissionCheckDisabled() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String disablePermissionCheck = config.getFirstProperty(APIConstants.API_STORE_DISABLE_PERMISSION_CHECK);
        if (disablePermissionCheck == null) {
            return false;
        }

        return Boolean.parseBoolean(disablePermissionCheck);
    }

    /**
     * Gets the information of the logged in User.
     *
     * @param cookie     Cookie of the previously logged in session.
     * @param serviceUrl Url of the authentication service.
     * @return LoggedUserInfo object containing details of the logged in user.
     * @throws ExceptionException
     * @throws RemoteException
     */
    public static LoggedUserInfo getLoggedInUserInfo(String cookie, String serviceUrl) throws RemoteException, ExceptionException {

        LoggedUserInfoAdminStub stub = new LoggedUserInfoAdminStub(null,
                serviceUrl + "LoggedUserInfoAdmin");
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        return stub.getUserInfo();
    }

    /**
     * Retrieves the role list of a user
     *
     * @param username A username
     * @param username A username
     * @throws APIManagementException If an error occurs
     */
    public static String[] getListOfRoles(String username) throws APIManagementException {

        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                    " the anonymous user");
        }

        String[] roles = getValueFromCache(APIConstants.API_USER_ROLE_CACHE, username);
        if (roles != null) {
            return roles;
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            UserStoreManager manager = ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager();
            roles = manager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
            addToRolesCache(APIConstants.API_USER_ROLE_CACHE, username, roles);
            return roles;
        } catch (UserStoreException e) {
            throw new APIManagementException("UserStoreException while trying the role list of the user " + username,
                    e);
        }
    }

    /**
     * Check whether user is exist
     *
     * @param username A username
     * @throws APIManagementException If an error occurs
     */
    public static boolean isUserExist(String username) throws APIManagementException {

        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as the anonymous user");
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        try {
            int tenantId =
                    ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            UserStoreManager manager =
                    ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                            .getUserStoreManager();
            return manager.isExistingUser(tenantAwareUserName);
        } catch (UserStoreException e) {
            throw new APIManagementException("UserStoreException while trying the user existence " + username, e);
        }
    }

    /**
     * To add the value to a cache.
     *
     * @param cacheName - Name of the Cache
     * @param key       - Key of the entry that need to be added.
     * @param value     - Value of the entry that need to be added.
     */
    protected static <T> void addToRolesCache(String cacheName, String key, T value) {

        if (isPublisherRoleCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Publisher role cache is enabled, adding the roles for the " + key + " to the cache "
                        + cacheName + "'");
            }
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName).put(key, value);
        }
    }

    /**
     * To get the value from the cache.
     *
     * @param cacheName Name of the cache.
     * @param key       Key of the cache entry.
     * @return Role list from the cache, if a values exists, otherwise null.
     */
    protected static <T> T getValueFromCache(String cacheName, String key) {

        if (isPublisherRoleCacheEnabled) {
            if (log.isDebugEnabled()) {
                log.debug("Publisher role cache is enabled, retrieving the roles for  " + key + " from the cache "
                        + cacheName + "'");
            }
            Cache<String, T> rolesCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                    .getCache(cacheName);
            return rolesCache.get(key);
        }
        return null;
    }

    /**
     * Retrieves the list of user roles without throwing any exceptions.
     *
     * @param username A username
     * @return the list of roles to which the user belongs to.
     */
    public static String[] getListOfRolesQuietly(String username) {

        try {
            return getListOfRoles(username);
        } catch (APIManagementException e) {
            return new String[0];
        }
    }

    /**
     * Sets permission for uploaded file resource.
     *
     * @param filePath Registry path for the uploaded file
     * @throws APIManagementException
     */

    public static void setFilePermission(String filePath) throws APIManagementException {

        try {
            String filePathString = filePath.replaceFirst("/registry/resource/", "");
            org.wso2.carbon.user.api.AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
                    getAuthorizationManager();
            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    filePathString, ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        filePathString, ActionConstants.GET);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while setting up permissions for file location", e);
        }
    }

    /**
     * This method used to get API from governance artifact specific to copyAPI
     *
     * @param artifact API artifact
     * @param registry Registry
     * @return API
     * @throws APIManagementException if failed to get API from artifact
     */
    public static API getAPI(GovernanceArtifact artifact, Registry registry, APIIdentifier oldId, String oldContext)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            api = new API(new APIIdentifier(providerName, apiName, apiVersion));
            int apiId = ApiMgtDAO.getInstance().getAPIID(artifact.getId());
            if (apiId == -1) {
                return null;
            }
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            BigDecimal bigDecimal = BigDecimal.valueOf(registry.getAverageRating(artifactPath));
            BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);
            api.setRating(res.floatValue());
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            //set uuid
            api.setUUID(artifact.getId());
            // set url
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                    .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
            }
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));

            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

            String apiLevelTier = ApiMgtDAO.getInstance().getAPILevelTier(apiId);
            api.setApiLevelPolicy(apiLevelTier);

            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            Map<String, Tier> definedTiers = getTiers(tenantId);
            Set<Tier> availableTier = getAvailableTiers(definedTiers, tiers, apiName);
            api.addAvailableTiers(availableTier);

            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
            ArrayList<URITemplate> urlPatternsList;

            Map<String, Scope> scopeToKeyMapping = getAPIScopes(artifact.getId(), tenantDomainName);
            api.setScopes(new LinkedHashSet<>(scopeToKeyMapping.values()));

            HashMap<Integer, Set<String>> resourceScopes;
            resourceScopes = ApiMgtDAO.getInstance().getResourceToScopeMapping(artifact.getId());

            urlPatternsList = ApiMgtDAO.getInstance().getAllURITemplates(oldContext, oldId.getVersion());
            Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);

            for (URITemplate uriTemplate : uriTemplates) {
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
                List<String> templateScopeKeys = new ArrayList<>(resourceScopes.get(uriTemplate.getId()));
                List<Scope> newTemplateScopes = new ArrayList<>();
                if (!templateScopeKeys.isEmpty()) {
                    for (String templateScope : templateScopeKeys) {
                        Scope scope = scopeToKeyMapping.get(templateScope);
                        newTemplateScopes.add(scope);
                    }
                }
                uriTemplate.addAllScopes(newTemplateScopes);
            }
            api.setUriTemplates(uriTemplates);

            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));

            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));
            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));

        } catch (GovernanceException e) {
            String msg = "Failed to get API fro artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Provider";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    public static boolean checkAccessTokenPartitioningEnabled() {

        return ServiceReferenceHolder.getInstance().getOauthServerConfiguration().isAccessTokenPartitioningEnabled();
    }

    public static boolean checkUserNameAssertionEnabled() {

        return ServiceReferenceHolder.getInstance().getOauthServerConfiguration().isUserNameAssertionEnabled();
    }

    /**
     * validates if an accessToken has expired or not
     *
     * @param accessTokenDO
     * @return true if token has expired else false
     */
    public static boolean isAccessTokenExpired(APIKeyValidationInfoDTO accessTokenDO) {

        long validityPeriod = accessTokenDO.getValidityPeriod();
        long issuedTime = accessTokenDO.getIssuedTime();
        long timestampSkew =
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getTimeStampSkewInSeconds() * 1000;
        long currentTime = System.currentTimeMillis();

        //If the validity period is not an never expiring value
        if (validityPeriod != Long.MAX_VALUE &&
                // For cases where validityPeriod is closer to Long.MAX_VALUE (then issuedTime + validityPeriod would spill
                // over and would produce a negative value)
                (currentTime - timestampSkew) > validityPeriod) {
            //check the validity of cached OAuth2AccessToken Response

            if ((currentTime - timestampSkew) > (issuedTime + validityPeriod)) {
                accessTokenDO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return true;
            }
        }

        return false;
    }

    /**
     * When an input is having '@',replace it with '-AT-' [This is required to persist API data in registry,as registry
     * paths don't allow '@' sign.]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomain(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR, APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return input;
    }

    /**
     * This method is used to validate character length of crucial api params.
     *
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param context    API Context of the API
     * @param provider   Provider of the API
     * @throws APIManagementException If the params length exceeds the allowed length
     */
    public static void validateCharacterLengthOfAPIParams(String apiName, String apiVersion, String context,
                                                          String provider) throws APIManagementException {

        validateCharacterLengthOfAPIParams(apiName, context, provider);
        if (!hasValidLength(apiVersion, APIConstants.MAX_LENGTH_VERSION)) {
            throw new APIManagementException("API version exceeds allowed character length",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
    }

    /**
     * This method is used to validate character length of crucial api product params.
     *
     * @param apiName  Name of the API
     * @param context  API Context of the API
     * @param provider Provider of the API
     * @throws APIManagementException If the params length exceeds the allowed length
     */
    public static void validateCharacterLengthOfAPIParams(String apiName, String context, String provider)
            throws APIManagementException {

        if (!hasValidLength(apiName, APIConstants.MAX_LENGTH_API_NAME)) {
            throw new APIManagementException("API name exceeds allowed character length",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
        if (!hasValidLength(context, APIConstants.MAX_LENGTH_CONTEXT)) {
            throw new APIManagementException("API context exceeds allowed character length",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
        if (!hasValidLength(provider, APIConstants.MAX_LENGTH_PROVIDER)) {
            throw new APIManagementException("API provider name exceeds allowed character length",
                    ExceptionCodes.LENGTH_EXCEEDS);
        }
    }

    /**
     * This method is used to validate character length.
     */
    public static boolean hasValidLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                    APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    /**
     * This method is used to get the APIProvider instance for a given username
     *
     * @param context API Context of the API
     * @throws APIManagementException If the context is not valid
     */
    public static void validateAPIContext(String context, String apiName) throws APIManagementException {
        String disableAPIContextValidation = System.getProperty(DISABLE_API_CONTEXT_VALIDATION);
        if (Boolean.parseBoolean(disableAPIContextValidation)) {
            return;
        }
        Pattern pattern = Pattern.compile(contextRegex);
        String errorMsg = ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION.getErrorMessage();

        if (context == null || context.isEmpty()) {
            errorMsg = errorMsg + " For API " + apiName + ", context cannot be empty or null";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }

        if (context.endsWith("/")) {
            errorMsg = errorMsg + " For API " + apiName + ", context " + context + " cannot end with /";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }

        Matcher matcher = pattern.matcher(context);

        // if the context has allowed characters
        if (matcher.matches()) {
            context = context.startsWith("/") ? context : "/".concat(context);
            String split[] = context.split("/");

            for (String param : split) {
                if (param != null && !APIConstants.VERSION_PLACEHOLDER.equals(param)) {
                    if (param.contains(APIConstants.VERSION_PLACEHOLDER)) {
                        errorMsg = errorMsg + " For API " + apiName +
                                ", {version} cannot exist as a substring of a sub-context";
                        log.error(errorMsg);
                        throw new APIManagementException(errorMsg);
                    } else if (param.contains("{") || param.contains("}")) {
                        errorMsg = errorMsg + " For API " + apiName +
                                ", { or } cannot exist as a substring of a sub-context";
                        log.error(errorMsg);
                        throw new APIManagementException(errorMsg);
                    }
                }
            }

            //check whether the parentheses are balanced
            boolean isBalanced = checkBalancedParentheses(context);
            if (!isBalanced) {
                errorMsg = errorMsg + " Unbalanced parenthesis cannot be used in context " + context + " for API "
                        + apiName;
                throw new APIManagementException(errorMsg);
            }
        } else {
            errorMsg = errorMsg + " Special characters cannot be used in context " + context + " for API "+ apiName;
            log.error(errorMsg);
            throw new APIManagementException(errorMsg);
        }
    }

    /**
     * Check whether the parentheses are balanced
     *
     * @param input API Context
     * @throws APIManagementException If parentheses are not balanced
     */
    public static boolean checkBalancedParentheses(@NotNull String input) throws APIManagementException {
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '(') {
                count++;
            } else if (input.charAt(i) == ')') {
                count--;
            }
            if (count < 0) {
                log.error("Unbalanced parentheses in : " + input);
                return false;
            }
        }
        if (count > 0) {
            log.error("Unbalanced parentheses in : " + input);
            return false;
        }
        return true;
    }


    public static void copyResourcePermissions(String username, String sourceArtifactPath, String targetArtifactPath)
            throws APIManagementException {

        String sourceResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                        + sourceArtifactPath);

        String targetResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                        + targetArtifactPath);

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
            org.wso2.carbon.user.api.AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getAuthorizationManager();
            String[] allowedRoles = authManager.getAllowedRolesForResource(sourceResourcePath, ActionConstants.GET);

            if (allowedRoles != null) {

                for (String allowedRole : allowedRoles) {
                    authManager.authorizeRole(allowedRole, targetResourcePath, ActionConstants.GET);
                }
            }

        } catch (UserStoreException e) {
            throw new APIManagementException("Error while adding role permissions to API", e);
        }
    }

    /**
     * Load the External API Store Configuration  to the registry
     *
     * @param organization
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */

    public static void loadTenantExternalStoreConfig(String organization) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Adding External Stores configuration to the tenant's registry");
        }
        try (InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/externalstores/default" +
                "-external-api-stores.xml")) {
            ServiceReferenceHolder.getInstance().getApimConfigService().addExternalStoreConfig(organization, IOUtils.toString(inputStream));
        } catch (IOException e) {
            throw new APIManagementException("Error while reading External Stores configuration file content", e);
        }
    }

    /**
     * Load the Google Analytics Configuration  to the registry
     *
     * @param organization
     * @throws APIManagementException
     */

    public static void loadTenantGAConfig(String organization) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Adding Google Analytics configuration to the tenant's registry");
        }
        try (InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/statistics/default-ga-config.xml")) {
            ServiceReferenceHolder.getInstance().getApimConfigService().addGAConfig(organization, IOUtils.toString(inputStream));
            //Resource already in the registry, set media type as ga-config
            log.debug("Google Analytics configuration already uploaded to the registry");

        } catch (IOException e) {
            throw new APIManagementException("Error while reading Google Analytics configuration file content", e);
        }
    }

    public static void loadTenantWorkFlowExtensions(String organization)
            throws APIManagementException {

        try {
            String workflowExtensionLocation =
                    CarbonUtils.getCarbonHome() + File.separator + APIConstants.WORKFLOW_EXTENSION_LOCATION;

            File wfExtension = new File(workflowExtensionLocation);
            if (log.isDebugEnabled()) {
                log.debug("Adding External Stores configuration to the tenant's registry");
            }

            InputStream inputStream;
            if (wfExtension.exists()) {
                inputStream = new FileInputStream(workflowExtensionLocation);
            } else {
                inputStream = APIManagerComponent.class
                        .getResourceAsStream("/workflowextensions/default-workflow-extensions.xml");
            }
            ServiceReferenceHolder.getInstance().getApimConfigService().addWorkflowConfig(organization, IOUtils.toString(inputStream));
        } catch (IOException e) {
            throw new APIManagementException("Error while reading Workflow configuration file content", e);
        }
    }

    /**
     * Load tenant self sign up configurations.
     *
     * @param organization the organization
     * @throws APIManagementException the api management exception
     */
    public static void loadTenantSelfSignUpConfigurations(String organization)
            throws APIManagementException {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding Self signup configuration to the tenant's registry");
            }
            InputStream inputStream;
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                inputStream =
                        APIManagerComponent.class.getResourceAsStream("/signupconfigurations/default-sign-up-config" +
                                ".xml");
            } else {
                inputStream =
                        APIManagerComponent.class.getResourceAsStream("/signupconfigurations/tenant-sign-up-config" +
                                ".xml");
            }

            ServiceReferenceHolder.getInstance().getApimConfigService().addSelfSighupConfig(organization, IOUtils.toString(inputStream));
        } catch (IOException  e) {
            throw new APIManagementException("Error while reading Self signup configuration file content", e);
        }
    }

    /**
     * Loads tenant-conf.json (tenant config) to registry from the tenant-conf.json available in the file system.
     * If any REST API scopes are added to the local tenant-conf.json, they will be updated in the registry.
     *
     * @param organization organization.
     * @throws APIManagementException when error occurred while loading the tenant-conf
     */
    public static void loadAndSyncTenantConf(String organization) throws APIManagementException {

        try {
            String currentConfig = ServiceReferenceHolder.getInstance().getApimConfigService()
                    .getTenantConfig(organization);
            if (currentConfig == null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject fileBaseTenantConfig = (JsonObject) getFileBaseTenantConfig();
                ServiceReferenceHolder.getInstance().getApimConfigService()
                        .addTenantConfig(organization, gson.toJson(fileBaseTenantConfig));
            }
        } catch (APIManagementException e) {
            throw new APIManagementException(
                    "Error while saving tenant conf to the Advanced configuration of the " + "tenant " + organization, e);
        }
    }

    private static JsonElement getFileBaseTenantConfig() throws APIManagementException{
        try {
            byte[] localTenantConfFileData = getLocalTenantConfFileData();
            String tenantConfDataStr = new String(localTenantConfFileData, Charset.defaultCharset());
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(tenantConfDataStr);
        } catch (IOException e) {
            throw new APIManagementException("Error while retrieving file base tenant-config" , e);
        }
    }

    /**
     * Gets the byte content of the local tenant-conf.json
     *
     * @return byte content of the local tenant-conf.json
     * @throws IOException error while reading local tenant-conf.json
     */
    private static byte[] getLocalTenantConfFileData() throws IOException {

        String tenantConfLocation = CarbonUtils.getCarbonHome() + File.separator +
                APIConstants.RESOURCE_FOLDER_LOCATION + File.separator +
                APIConstants.API_TENANT_CONF;
        File tenantConfFile = new File(tenantConfLocation);
        byte[] data;
        if (tenantConfFile.exists()) { // Load conf from resources directory in pack if it exists
            FileInputStream fileInputStream = new FileInputStream(tenantConfFile);
            data = IOUtils.toByteArray(fileInputStream);
        } else { // Fallback to loading the conf that is stored at jar level if file does not exist in pack
            InputStream inputStream =
                    APIManagerComponent.class.getResourceAsStream("/tenant/" + APIConstants.API_TENANT_CONF);
            data = IOUtils.toByteArray(inputStream);
        }
        return data;
    }



    /**
     * Returns whether subscriber role creation enabled for the given tenant in tenant-conf.json
     *
     * @param tenantId id of the tenant
     * @return true if subscriber role creation enabled in tenant-conf.json
     */
    public static boolean isSubscriberRoleCreationEnabled(int tenantId) throws APIManagementException {

        String tenantDomain = getTenantDomainFromTenantId(tenantId);
        JSONObject defaultRoles = getTenantDefaultRoles(tenantDomain);
        boolean isSubscriberRoleCreationEnabled = false;
        if (defaultRoles != null) {
            JSONObject subscriberRoleConfig = (JSONObject) defaultRoles
                    .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_SUBSCRIBER_ROLE);
            isSubscriberRoleCreationEnabled = isRoleCreationEnabled(subscriberRoleConfig);
        }
        return isSubscriberRoleCreationEnabled;
    }

    /**
     * Create default roles specified in APIM per-tenant configuration file
     *
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createDefaultRoles(int tenantId) throws APIManagementException {

        String tenantDomain = getTenantDomainFromTenantId(tenantId);
        JSONObject defaultRoles = getTenantDefaultRoles(tenantDomain);

        if (defaultRoles != null) {
            // create publisher role if it's creation is enabled in tenant-conf.json
            JSONObject publisherRoleConfig = (JSONObject) defaultRoles
                    .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_PUBLISHER_ROLE);
            if (isRoleCreationEnabled(publisherRoleConfig)) {
                String publisherRoleName = String.valueOf(publisherRoleConfig
                        .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                if (!StringUtils.isBlank(publisherRoleName)) {
                    createPublisherRole(publisherRoleName, tenantId);
                }
            }

            // create creator role if it's creation is enabled in tenant-conf.json
            JSONObject creatorRoleConfig = (JSONObject) defaultRoles.get(
                    APIConstants.API_TENANT_CONF_DEFAULT_ROLES_CREATOR_ROLE);
            if (isRoleCreationEnabled(creatorRoleConfig)) {
                String creatorRoleName = String.valueOf(
                        creatorRoleConfig.get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                if (!StringUtils.isBlank(creatorRoleName)) {
                    createCreatorRole(creatorRoleName, tenantId);
                }
            }

            // create subscriber role if it's creation is enabled in tenant-conf.json
            JSONObject subscriberRoleConfig = (JSONObject) defaultRoles.get(
                    APIConstants.API_TENANT_CONF_DEFAULT_ROLES_SUBSCRIBER_ROLE);
            if (isRoleCreationEnabled(subscriberRoleConfig)) {
                String subscriberRoleName;
                if (subscriberRoleConfig.containsKey(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME)) {
                    subscriberRoleName = String.valueOf(
                            subscriberRoleConfig.get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                } else {
                    subscriberRoleName = APIConstants.SUBSCRIBER_ROLE;
                }
                createSubscriberRole(subscriberRoleName, tenantId);
            }

            // create devOps role if it's creation is enabled in tenant-conf.json
            JSONObject devOpsRoleConfig = (JSONObject) defaultRoles
                    .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_DEVOPS_ROLE);
            if (isRoleCreationEnabled(devOpsRoleConfig)) {
                String devOpsRoleName = String.valueOf(devOpsRoleConfig
                        .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                if (!StringUtils.isBlank(devOpsRoleName)) {
                    createDevOpsRole(devOpsRoleName, tenantId);
                }
            }

            // create observer role if it's creation is enabled in tenant-conf.json
            JSONObject observerRoleConfig = (JSONObject) defaultRoles
                    .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_OBSERVER_ROLE);
            if (isRoleCreationEnabled(observerRoleConfig)) {
                String observerRoleName = String.valueOf(observerRoleConfig
                        .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                if (!StringUtils.isBlank(observerRoleName)) {
                    createObserverRole(observerRoleName, tenantId);
                }
            }

            // create IntegrationDeveloperRole role if it's creation is enabled in tenant-conf.json
            JSONObject integrationDeveloperRoleConfig = (JSONObject) defaultRoles
                    .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_INTEGRATIONDEVELOPER_ROLE);
            if (isRoleCreationEnabled(integrationDeveloperRoleConfig)) {
                String integrationDeveloperRoleName = String.valueOf(integrationDeveloperRoleConfig
                        .get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES_ROLENAME));
                if (!StringUtils.isBlank(integrationDeveloperRoleName)) {
                    createIntegrationDeveloperRole(integrationDeveloperRoleName, tenantId);
                }
            }

            createAnalyticsRole(APIConstants.ANALYTICS_ROLE, tenantId);
        }
    }

    /**
     * Returns whether role creation enabled for the provided role config
     *
     * @param roleConfig role config in tenat-conf.json
     * @return true if role creation enabled for the provided role config
     */
    private static boolean isRoleCreationEnabled(JSONObject roleConfig) {

        boolean roleCreationEnabled = false;
        if (roleConfig != null && roleConfig.get(
                APIConstants.API_TENANT_CONF_DEFAULT_ROLES_CREATE_ON_TENANT_LOAD) != null && (Boolean) (roleConfig.get(
                APIConstants.API_TENANT_CONF_DEFAULT_ROLES_CREATE_ON_TENANT_LOAD))) {
            roleCreationEnabled = true;
        }
        return roleCreationEnabled;
    }

    public static boolean isAnalyticsEnabled() {

        return APIManagerAnalyticsConfiguration.getInstance().isAnalyticsEnabled();
    }

    public static List<String> getGatewayTypes () {
        // Get the gateway types from the deployment.toml
        List<String> gatewayTypesList = new ArrayList<>();
        if (gatewayTypes != null && !gatewayTypes.isEmpty()) {
            gatewayTypesList = Arrays.asList(gatewayTypes.split(","));
        }
        return gatewayTypesList;
    }

    /**
     * Returns whether API Publisher Access Control is enabled or not
     *
     * @return true if publisher access control enabled
     */
    public static boolean isAccessControlEnabled() {

        boolean accessControlEnabled = false;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config.getFirstProperty(
                APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS) != null && config.getFirstProperty(
                APIConstants.API_PUBLISHER_ENABLE_ACCESS_CONTROL_LEVELS).equals("true")) {
            accessControlEnabled = true;
        }
        return accessControlEnabled;
    }

    /**
     * Converting the user store domain name to uppercase.
     *
     * @param username Username to be modified
     * @return Username with domain name set to uppercase.
     */
    public static String setDomainNameToUppercase(String username) {

        String modifiedName = username;
        if (username != null) {
            String[] nameParts = username.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (nameParts.length > 1) {
                modifiedName = nameParts[0].toUpperCase() + CarbonConstants.DOMAIN_SEPARATOR + nameParts[1];
            }
        }

        return modifiedName;
    }

    /**
     * Create APIM Subscriber role with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createSubscriberRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] subscriberPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
        createRole(roleName, subscriberPermissions, tenantId);
    }

    /**
     * Create APIM Publisher roles with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createPublisherRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] publisherPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_PUBLISH, UserMgtConstants.EXECUTE_ACTION)};
        createRole(roleName, publisherPermissions, tenantId);
    }

    /**
     * Create APIM Creator roles with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createCreatorRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] creatorPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.CONFIGURE_GOVERNANCE, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.RESOURCE_GOVERN, UserMgtConstants.EXECUTE_ACTION)};
        createRole(roleName, creatorPermissions, tenantId);
    }

    /**
     * Create APIM DevOps roles with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createDevOpsRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] devOpsPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_PUBLISH, UserMgtConstants.EXECUTE_ACTION),
                new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION),
        };
        createRole(roleName, devOpsPermissions, tenantId);
    }

    /**
     * Create APIM Observer roles with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createObserverRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] observerPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION)
        };
        createRole(roleName, observerPermissions, tenantId);
    }

    /**
     * Create Integration developer roles with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createIntegrationDeveloperRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] integrationDeveloperPermissions = new Permission[]{};
        createRole(roleName, integrationDeveloperPermissions, tenantId);
    }

    /**
     * Create Analytics role with the given name in specified tenant
     *
     * @param roleName role name
     * @param tenantId id of the tenant
     * @throws APIManagementException
     */
    public static void createAnalyticsRole(String roleName, int tenantId) throws APIManagementException {

        Permission[] analyticsPermissions = new Permission[]{
                new Permission(APIConstants.Permissions.LOGIN, UserMgtConstants.EXECUTE_ACTION)};
        createRole(roleName, analyticsPermissions, tenantId);
    }

    /**
     * Creates a role with a given set of permissions for the specified tenant
     *
     * @param roleName    role name
     * @param permissions a set of permissions to be associated with the role
     * @param tenantId    id of the tenant
     * @throws APIManagementException
     */
    public static void createRole(String roleName, Permission[] permissions, int tenantId)
            throws APIManagementException {

        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm;
            org.wso2.carbon.user.api.UserRealm tenantRealm;
            UserStoreManager manager;

            if (tenantId < 0) {
                realm = realmService.getBootstrapRealm();
                manager = realm.getUserStoreManager();
            } else {
                tenantRealm = realmService.getTenantUserRealm(tenantId);
                manager = tenantRealm.getUserStoreManager();
            }
            if (!manager.isExistingRole(roleName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating role: " + roleName);
                }
                String tenantAdminName = ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId).getRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{tenantAdminName};
                manager.addRole(roleName, userList, permissions);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while creating role: " + roleName, e);
        }
    }

    public void setupSelfRegistration(APIManagerConfiguration config, int tenantId) throws APIManagementException {

        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            return;
        }
        // Create the subscriber role as an internal role
        String role = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR
                + config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if ((UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR).equals(role)) {
            // Required parameter missing - Throw an exception and interrupt startup
            throw new APIManagementException("Required subscriber role parameter missing "
                    + "in the self sign up configuration");
        }

        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm;
            org.wso2.carbon.user.api.UserRealm tenantRealm;
            UserStoreManager manager;

            if (tenantId < 0) {
                realm = realmService.getBootstrapRealm();
                manager = realm.getUserStoreManager();
            } else {
                tenantRealm = realmService.getTenantUserRealm(tenantId);
                manager = tenantRealm.getUserStoreManager();
            }
            if (!manager.isExistingRole(role)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating subscriber role: " + role);
                }
                Permission[] subscriberPermissions = new Permission[]{
                        new Permission("/permission/admin/login", UserMgtConstants.EXECUTE_ACTION),
                        new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
                String tenantAdminName = ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId).getRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{tenantAdminName};
                manager.addRole(role, userList, subscriberPermissions);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while creating subscriber role: " + role + " - "
                    + "Self registration might not function properly.", e);
        }
    }

    public static String removeAnySymbolFromUriTempate(String uriTemplate) {

        if (uriTemplate != null) {
            int anySymbolIndex = uriTemplate.indexOf("/*");
            if (anySymbolIndex != -1) {
                return uriTemplate.substring(0, anySymbolIndex);
            }
        }
        return uriTemplate;
    }

    public static float getAverageRating(String id) throws APIManagementException {

        return ApiMgtDAO.getInstance().getAverageRating(id);
    }

    public static float getAverageRating(int apiId) throws APIManagementException {

        return ApiMgtDAO.getInstance().getAverageRating(apiId);
    }

    public static List<Tenant> getAllTenantsWithSuperTenant() throws UserStoreException {

        Tenant[] tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        ArrayList<Tenant> tenantArrayList = new ArrayList<Tenant>();
        Collections.addAll(tenantArrayList, tenants);
        Tenant superAdminTenant = new Tenant();
        superAdminTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superAdminTenant.setId(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID);
        superAdminTenant.setAdminName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
        tenantArrayList.add(superAdminTenant);
        return tenantArrayList;
    }

    /**
     * In multi tenant environment, publishers should allow only to revoke the tokens generated within his domain.
     * Super tenant should not see the tenant created tokens and vise versa. This method is used to check the logged in
     * user have permissions to revoke a given users tokens.
     *
     * @param loggedInUser   current logged in user to publisher
     * @param authorizedUser access token owner
     * @return
     */
    public static boolean isLoggedInUserAuthorizedToRevokeToken(String loggedInUser, String authorizedUser) {

        String loggedUserTenantDomain = MultitenantUtils.getTenantDomain(loggedInUser);
        String authorizedUserTenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(loggedUserTenantDomain) && MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME.equals(authorizedUserTenantDomain)) {
            return true;
        } else if (authorizedUserTenantDomain.equals(loggedUserTenantDomain)) {
            return true;
        }

        return false;
    }

    public static String getApplicationUUID(String appName, String userId) throws APIManagementException {
        return ApiMgtDAO.getInstance().getApplicationUUID(appName, userId);
    }

    public static int getApplicationId(String appName, String userId) throws APIManagementException {

        return ApiMgtDAO.getInstance().getApplicationId(appName, userId);
    }

    public static int getApplicationId(String appName, String userId, String groupId) throws APIManagementException {

        Application application = ApiMgtDAO.getInstance().getApplicationByName(appName, userId, groupId);
        if (application != null) {
            return application.getId();
        } else {
            return 0;
        }
    }

    /**
     * This method is used to retrieve complexity details
     *
     * @param api API
     * @return GraphqlComplexityInfo object that contains the complexity details
     * @throws APIManagementException
     */

    public static GraphqlComplexityInfo getComplexityDetails(API api) throws APIManagementException {

        return ApiMgtDAO.getInstance().getComplexityDetails(api.getUuid());
    }

    public static boolean isAPIManagementEnabled() {

        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("APIManagement.Enabled"));
    }

    public static boolean isLoadAPIContextsAtStartup() {

        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty(
                "APIManagement.LoadAPIContextsInServerStartup"));
    }

    public static Set<APIStore> getExternalAPIStores(int tenantId) throws APIManagementException {

        SortedSet<APIStore> apistoreSet = new TreeSet<APIStore>(new APIStoreNameComparator());
        apistoreSet.addAll(getExternalStores(tenantId));
        return apistoreSet;

    }

    public static boolean isAllowDisplayAPIsWithMultipleStatus() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String displayAllAPIs = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_ALL_APIS);
        if (displayAllAPIs == null) {
            log.warn("The configurations related to show deprecated APIs in APIStore " +
                    "are missing in api-manager.xml.");
            return false;
        }
        return Boolean.parseBoolean(displayAllAPIs);
    }

    public static boolean isAllowDisplayMultipleVersions() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String displayMultiVersions = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_MULTIPLE_VERSIONS);
        if (displayMultiVersions == null) {
            log.warn("The configurations related to show multiple versions of API in APIStore " +
                    "are missing in api-manager.xml.");
            return false;
        }
        return Boolean.parseBoolean(displayMultiVersions);
    }

    public static boolean updateNullThrottlingTierAtStartup() {

        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isNullThrottlingTierUpdateEnabled = configuration
                .getFirstProperty("StartupConfiguration.UpdateNullThrottlingTier");
        return isNullThrottlingTierUpdateEnabled == null || Boolean.parseBoolean(isNullThrottlingTierUpdateEnabled);
    }

    public static Set<APIStore> getExternalAPIStores(Set<APIStore> inputStores, int tenantId)
            throws APIManagementException {

        SortedSet<APIStore> apiStores = new TreeSet<APIStore>(new APIStoreNameComparator());
        apiStores.addAll(getExternalStores(tenantId));
        //Retains only the stores that contained in configuration
        inputStores.retainAll(apiStores);
        boolean exists = false;
        if (!apiStores.isEmpty()) {
            for (APIStore store : apiStores) {
                for (APIStore inputStore : inputStores) {
                    if (inputStore.getName().equals(store.getName())) { // If the configured apistore already stored in
                        // db,ignore adding it again
                        exists = true;
                    }
                }
                if (!exists) {
                    inputStores.add(store);
                }
                exists = false;
            }
        }
        return inputStores;
    }

    public static boolean isAPIsPublishToExternalAPIStores(int tenantId)
            throws APIManagementException {

        return !getExternalStores(tenantId).isEmpty();
    }

    public static boolean isAPIGatewayKeyCacheEnabled() {

        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            return Boolean.parseBoolean(serviceURL);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. Use default configuration"
                    + e);
        }
        return true;
    }

    public static Cache getAPIContextCache() {

        CacheManager contextCacheManager = Caching.getCacheManager(APIConstants.API_CONTEXT_CACHE_MANAGER).
                getCache(APIConstants.API_CONTEXT_CACHE).getCacheManager();
        if (!isContextCacheInitialized) {
            isContextCacheInitialized = true;
            return contextCacheManager.<String, Boolean>createCacheBuilder(APIConstants.API_CONTEXT_CACHE_MANAGER).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.DAYS,
                            APIConstants.API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS)).setStoreByValue(false).build();
        } else {
            return Caching.getCacheManager(APIConstants.API_CONTEXT_CACHE_MANAGER).getCache(
                    APIConstants.API_CONTEXT_CACHE);
        }
    }

    /**
     * Get active tenant domains
     *
     * @return
     * @throws UserStoreException
     */
    public static Set<String> getActiveTenantDomains() throws UserStoreException {

        Set<String> tenantDomains;
        Tenant[] tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        if (tenants == null || tenants.length == 0) {
            tenantDomains = Collections.<String>emptySet();
        } else {
            tenantDomains = new HashSet<String>();
            for (Tenant tenant : tenants) {
                if (tenant.isActive()) {
                    tenantDomains.add(tenant.getDomain());
                }
            }
            if (!tenantDomains.isEmpty()) {
                tenantDomains.add(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }
        }
        return tenantDomains;
    }

    /**
     * Get tenants by state
     *
     * @param state state of the tenant
     * @return set of tenants
     * @throws UserStoreException
     */
    public static Set<String> getTenantDomainsByState(String state) throws UserStoreException {

        boolean isActive = state.equalsIgnoreCase(APIConstants.TENANT_STATE_ACTIVE);
        if (isActive) {
            return getActiveTenantDomains();
        }
        Tenant[] tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        Set<String> tenantDomains = new HashSet<>();
        for (Tenant tenant : tenants) {
            if (!tenant.isActive()) {
                tenantDomains.add(tenant.getDomain());
            }
        }
        return tenantDomains;
    }

    /**
     * Check if tenant is available
     *
     * @param tenantDomain tenant Domain
     * @return isTenantAvailable
     * @throws UserStoreException
     */
    public static boolean isTenantAvailable(String tenantDomain) throws UserStoreException {

        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);

        if (tenantId == -1) {
            return false;
        }

        return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .isTenantActive(tenantId);
    }

    public static OrganizationResolver getOrganizationResolver() throws APIManagementException {

        OrganizationResolver resolver = ServiceReferenceHolder.getInstance().getOrganizationResolver();
        if (resolver == null) {
            resolver = new OnPremResolver();
        }
        return resolver;
    }

    public static boolean isOnPremResolver() throws APIManagementException {
        OrganizationResolver resolver = APIUtil.getOrganizationResolver();
        return resolver instanceof OnPremResolver;
    }

    public static ResourceQuotaLimiter getResourceQuotaLimiter() throws APIManagementException {
        ResourceQuotaLimiter resourceQuotaLimiter = ServiceReferenceHolder.getInstance().getResourceQuotaLimiter();
        if (resourceQuotaLimiter == null) {
            resourceQuotaLimiter = new OnPremQuotaLimiter();
        }
        return resourceQuotaLimiter;
    }
    
    public static int getInternalOrganizationId(String organization) throws APIManagementException {
        return getOrganizationResolver().getInternalId(organization);
    }

    public static String getInternalOrganizationDomain(String organization) throws APIManagementException {
        return APIUtil.getTenantDomainFromTenantId(APIUtil.getInternalOrganizationId(organization));
    }

    public static boolean isInternalOrganization(String organization) throws UserStoreException {
        return isTenantAvailable(organization);
    }

    /**
     * check whether given role is exist
     *
     * @param userName logged user
     * @param roleName role name need to check
     * @return true if exist and false if not
     * @throws APIManagementException If an error occurs
     */
    public static boolean isRoleNameExist(String userName, String roleName) throws APIManagementException {

        if (roleName == null || StringUtils.isEmpty(roleName.trim())) {
            return true;
        }

        //disable role validation if "disableRoleValidationAtScopeCreation" system property is set
        String disableRoleValidation = System.getProperty(DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION);
        if (Boolean.parseBoolean(disableRoleValidation)) {
            return true;
        }

        org.wso2.carbon.user.api.UserStoreManager userStoreManager;
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(MultitenantUtils.getTenantDomain(userName));
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();

            String[] roles = roleName.split(",");
            for (String role : roles) {
                if (!userStoreManager.isExistingRole(role.trim())) {
                    return false;
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error when getting the list of roles", e);
            return false;
        }
        return true;
    }

    /**
     * Check whether roles exist for the user.
     *
     * @param userName
     * @param roleName
     * @return
     * @throws APIManagementException
     */
    public static boolean isRoleExistForUser(String userName, String roleName) throws APIManagementException {

        boolean foundUserRole = false;
        String[] userRoleList = APIUtil.getListOfRoles(userName);
        String[] inputRoles = roleName.split(",");
        if (log.isDebugEnabled()) {
            log.debug("isRoleExistForUser(): User Roles " + Arrays.toString(userRoleList));
            log.debug("isRoleExistForUser(): InputRoles Roles " + Arrays.toString(inputRoles));
        }
        if (inputRoles != null) {
            for (String inputRole : inputRoles) {
                if (APIUtil.compareRoleList(userRoleList, inputRole)) {
                    foundUserRole = true;
                    break;
                }
            }
        }
        return foundUserRole;
    }

    /**
     * Create API Definition in JSON
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to generate the content and save
     * @deprecated
     */

    @Deprecated
    public static String createSwaggerJSONContent(API api) throws APIManagementException {

        APIIdentifier identifier = api.getId();
        String organization = api.getOrganization();
        Environment environment = (Environment) getEnvironments(organization).values().toArray()[0];
        String endpoints = environment.getApiGatewayEndpoint();
        String[] endpointsSet = endpoints.split(",");
        String apiContext = api.getContext();
        String version = identifier.getVersion();
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        String description = api.getDescription();

        // With the new context version strategy, the URL prefix is the apiContext. the verison will be embedded in
        // the apiContext.
        String urlPrefix = apiContext;

        if (endpointsSet.length < 1) {
            throw new APIManagementException("Error in creating JSON representation of the API"
                    + identifier.getApiName());
        }
        if (description == null || "".equals(description)) {
            description = "";
        } else {
            description = description.trim();
        }

        Map<String, List<Operation>> uriTemplateDefinitions = new HashMap<String, List<Operation>>();
        List<APIResource> apis = new ArrayList<APIResource>();
        for (URITemplate template : uriTemplates) {
            List<Operation> ops;
            List<Parameter> parameters;
            String path = urlPrefix + APIUtil.removeAnySymbolFromUriTempate(template.getUriTemplate());
            /* path exists in uriTemplateDefinitions */
            if (uriTemplateDefinitions.get(path) != null) {
                ops = uriTemplateDefinitions.get(path);
                parameters = new ArrayList<Parameter>();

                String httpVerb = template.getHTTPVerb();
                /* For GET and DELETE Parameter name - Query Parameters */
                if (Constants.Configuration.HTTP_METHOD_GET.equals(httpVerb)
                        || Constants.Configuration.HTTP_METHOD_DELETE.equals(httpVerb)) {
                    Parameter queryParam = new Parameter(APIConstants.OperationParameter.QUERY_PARAM_NAME,
                            APIConstants.OperationParameter.QUERY_PARAM_DESCRIPTION,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
                    parameters.add(queryParam);
                } else {/* For POST, PUT and PATCH Parameter name - Payload */
                    Parameter payLoadParam = new Parameter(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
                    parameters.add(payLoadParam);
                }

                Parameter authParam = new Parameter(APIConstants.OperationParameter.AUTH_PARAM_NAME,
                        APIConstants.OperationParameter.AUTH_PARAM_DESCRIPTION,
                        APIConstants.OperationParameter.AUTH_PARAM_TYPE, false, false, "String");
                parameters.add(authParam);
                if (!"OPTIONS".equals(httpVerb)) {
                    Operation op = new Operation(httpVerb, description, description, parameters);
                    ops.add(op);
                }
            } else {/* path not exists in uriTemplateDefinitions */
                ops = new ArrayList<Operation>();
                parameters = new ArrayList<Parameter>();

                String httpVerb = template.getHTTPVerb();
                /* For GET and DELETE Parameter name - Query Parameters */
                if (Constants.Configuration.HTTP_METHOD_GET.equals(httpVerb)
                        || Constants.Configuration.HTTP_METHOD_DELETE.equals(httpVerb)) {
                    Parameter queryParam = new Parameter(APIConstants.OperationParameter.QUERY_PARAM_NAME,
                            APIConstants.OperationParameter.QUERY_PARAM_DESCRIPTION,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
                    parameters.add(queryParam);
                } else {/* For POST,PUT and PATCH Parameter name - Payload */
                    Parameter payLoadParam = new Parameter(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION,
                            APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
                    parameters.add(payLoadParam);
                }
                Parameter authParam = new Parameter(APIConstants.OperationParameter.AUTH_PARAM_NAME,
                        APIConstants.OperationParameter.AUTH_PARAM_DESCRIPTION,
                        APIConstants.OperationParameter.AUTH_PARAM_TYPE, false, false, "String");
                parameters.add(authParam);
                if (!"OPTIONS".equals(httpVerb)) {
                    Operation op = new Operation(httpVerb, description, description, parameters);
                    ops.add(op);
                }
                uriTemplateDefinitions.put(path, ops);
            }
        }

        final Set<Entry<String, List<Operation>>> entries = uriTemplateDefinitions.entrySet();

        for (Entry entry : entries) {
            APIResource apiResource = new APIResource((String) entry.getKey(), description,
                    (List<Operation>) entry.getValue());
            apis.add(apiResource);
        }
        APIDefinition apidefinition = new APIDefinition(version, APIConstants.SWAGGER_VERSION, endpointsSet[0],
                apiContext, apis);

        Gson gson = new Gson();
        return gson.toJson(apidefinition);
    }

    /**
     * Helper method to get tenantId from userName
     *
     * @param userName user name
     * @return tenantId
     */
    public static int getTenantId(String userName) {
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        return getTenantIdFromTenantDomain(tenantDomain);
    }

    /**
     * Helper method to get tenantId from tenantDomain
     *
     * @param tenantDomain tenant Domain
     * @return tenantId
     */
    public static int getTenantIdFromTenantDomain(String tenantDomain) {

        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if (realmService == null || tenantDomain == null) {
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            return getInternalOrganizationId(tenantDomain);
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }

        return -1;
    }

    /**
     * Helper method to get tenantId from organization
     *
     * @param organization Organization
     * @return tenantId
     */
    public static int getInternalIdFromTenantDomainOrOrganization(String organization) {
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        if (realmService == null || organization == null) {
            return MultitenantConstants.SUPER_TENANT_ID;
        }
        try {
            return getInternalOrganizationId(organization);
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Helper method to get tenantDomain from tenantId
     *
     * @param tenantId tenant Id
     * @return tenantId
     */
    public static String getTenantDomainFromTenantId(int tenantId) {

        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if (realmService == null) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            return realmService.getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static int getSuperTenantId() {

        return MultitenantConstants.SUPER_TENANT_ID;
    }

    /**
     * Helper method to get username with tenant domain.
     *
     * @param userName
     * @return userName with tenant domain
     */
    public static String getUserNameWithTenantSuffix(String userName) {

        String userNameWithTenantPrefix = userName;
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        if (userName != null && !userName.endsWith("@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)
                && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            userNameWithTenantPrefix = userName + "@" + tenantDomain;
        }

        return userNameWithTenantPrefix;
    }

    /**
     * Build OMElement from inputstream
     *
     * @param inputStream
     * @return
     * @throws APIManagementException
     */
    public static OMElement buildOMElement(InputStream inputStream) throws APIManagementException {

        XMLStreamReader parser;
        StAXOMBuilder builder;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            parser = factory.createXMLStreamReader(inputStream);
            builder = new StAXOMBuilder(parser);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser.";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }

        return builder.getDocumentElement();
    }

    /**
     * Build OMElement from input stream with securely configured parser.
     * @param inputStream Input Stream
     * @return  OMElement
     * @throws APIManagementException XMLStreamException while parsing the inputStream
     */
    public static OMElement buildSecuredOMElement(InputStream inputStream) throws APIManagementException {

        XMLStreamReader parser;
        StAXOMBuilder builder;

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            parser = factory.createXMLStreamReader(inputStream);
            builder = new StAXOMBuilder(parser);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser.";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return builder.getDocumentElement();
    }

    /**
     * Get stored in sequences, out sequences and fault sequences from the governanceSystem registry
     *
     * @param sequenceName -The sequence to be retrieved
     * @param tenantId
     * @param direction    - Direction indicates which sequences to fetch. Values would be
     *                     "in", "out" or "fault"
     * @return
     * @throws APIManagementException
     */
    public static OMElement getCustomSequence(String sequenceName, int tenantId, String direction,
                                              APIIdentifier identifier) throws APIManagementException {

        org.wso2.carbon.registry.api.Collection seqCollection = null;

        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);

            if ("in".equals(direction)) {
                seqCollection = (org.wso2.carbon.registry.api.Collection) registry
                        .get(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION);
            } else if ("out".equals(direction)) {
                seqCollection = (org.wso2.carbon.registry.api.Collection) registry
                        .get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
            } else if ("fault".equals(direction)) {
                seqCollection = (org.wso2.carbon.registry.api.Collection) registry
                        .get(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION);
            }

            if (seqCollection == null) {
                seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(getSequencePath(identifier,
                        direction));

            }

            if (seqCollection != null) {
                String[] childPaths = seqCollection.getChildren();

                for (String childPath : childPaths) {
                    Resource sequence = registry.get(childPath);
                    OMElement seqElment = APIUtil.buildSecuredOMElement(sequence.getContentStream());
                    if (sequenceName.equals(seqElment.getAttributeValue(new QName("name")))) {
                        return seqElment;
                    }
                }
            }

            // If the sequence not found the default sequences, check in custom sequences

            seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(getSequencePath(identifier,
                    direction));
            if (seqCollection != null) {
                String[] childPaths = seqCollection.getChildren();

                for (String childPath : childPaths) {
                    Resource sequence = registry.get(childPath);
                    OMElement seqElment = APIUtil.buildSecuredOMElement(sequence.getContentStream());
                    if (sequenceName.equals(seqElment.getAttributeValue(new QName("name")))) {
                        return seqElment;
                    }
                }
            }

        } catch (Exception e) {
            String msg = "Issue is in accessing the Registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    /**
     * Returns true if the sequence is a per API one
     *
     * @param sequenceName
     * @param tenantId
     * @param identifier   API identifier
     * @param sequenceType in/out/fault
     * @return true/false
     * @throws APIManagementException
     */
    public static boolean isPerAPISequence(String sequenceName, int tenantId, APIIdentifier identifier,
                                           String sequenceType) throws APIManagementException {

        org.wso2.carbon.registry.api.Collection seqCollection = null;
        try {
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);

            // If the sequence not found the default sequences, check in custom sequences

            if (registry.resourceExists(getSequencePath(identifier, sequenceType))) {

                seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(getSequencePath(identifier,
                        sequenceType));
                if (seqCollection != null) {
                    String[] childPaths = seqCollection.getChildren();

                    for (String childPath : childPaths) {
                        Resource sequence = registry.get(childPath);
                        OMElement seqElment = APIUtil.buildSecuredOMElement(sequence.getContentStream());
                        if (sequenceName.equals(seqElment.getAttributeValue(new QName("name")))) {
                            return true;
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving registry for tenant " + tenantId;
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            String msg = "Error while processing the " + sequenceType + " sequences of " + identifier
                    + " in the registry";
            log.error(msg);
            throw new APIManagementException(msg, e);
        } catch (Exception e) {
            throw new APIManagementException(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Returns true if sequence is set
     *
     * @param sequence
     * @return
     */
    public static boolean isSequenceDefined(String sequence) {

        return sequence != null && !"none".equals(sequence) && !StringUtils.isEmpty(sequence) ;
    }

    /**
     * Return the sequence extension name.
     * eg: admin--testAPi--v1.00
     *
     * @param api
     * @return
     */
    public static String getSequenceExtensionName(API api) {

        return api.getId().getApiName() + ":v" + api.getId().getVersion();
    }

    /**
     * Return the sequence extension name.
     * eg: admin--testAPi--v1.00
     *
     * @return
     */
    public static String getSequenceExtensionName(String name, String version) {

        return name + ":v" + version;
    }

    /**
     * @param token
     * @return
     */
    public static String decryptToken(String token) throws CryptoException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENCRYPT_TOKENS_ON_PERSISTENCE))) {
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(token), Charset.defaultCharset());
        }

        String enableTokenHashMode = config.getFirstProperty(APIConstants.HASH_TOKENS_ON_PERSISTENCE);
        if (enableTokenHashMode != null && Boolean.parseBoolean(enableTokenHashMode)) {
            return null;
        }
        return token;
    }

    /**
     * @param token
     * @return
     */
    public static String encryptToken(String token) throws CryptoException, APIManagementException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENCRYPT_TOKENS_ON_PERSISTENCE))) {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(token.getBytes(Charset.defaultCharset()));
        }

        String enableTokenHashMode = config.getFirstProperty(APIConstants.HASH_TOKENS_ON_PERSISTENCE);
        if (enableTokenHashMode != null && Boolean.parseBoolean(enableTokenHashMode)) {
            return hash(token);
        }
        return token;
    }

    /**
     * Method to generate hash value.
     *
     * @param plainText Plain text value.
     * @return hashed value.
     */
    private static String hash(String plainText) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Hashing the token for " + plainText);
        }

        if (StringUtils.isEmpty(plainText)) {
            throw new APIManagementException("plainText value is null or empty to be hash.");
        }

        MessageDigest messageDigest = null;
        byte[] hash = null;
        String hashAlgorithm = ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getHashAlgorithm();
        if (log.isDebugEnabled()) {
            log.debug("Getting the hash algorithm from the configuration: " + hashAlgorithm);
        }
        try {
            messageDigest = MessageDigest.getInstance(hashAlgorithm);
            messageDigest.update(plainText.getBytes());
            hash = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new APIManagementException(
                    "Error while retrieving MessageDigest for the provided hash algorithm: " + hashAlgorithm, e);
        }
        JSONObject object = new JSONObject();
        object.put("algorithm", hashAlgorithm);
        object.put("hash", bytesToHex(hash));

        return object.toString();
    }

    private static String bytesToHex(byte[] bytes) {

        StringBuilder result = new StringBuilder();
        for (byte byt : bytes) {
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public static void loadTenantRegistry(int tenantId) throws RegistryException {

        TenantRegistryLoader tenantRegistryLoader = APIManagerComponent.getTenantRegistryLoader();
        ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
        tenantRegistryLoader.loadTenantRegistry(tenantId);
    }

    public static boolean isSandboxEndpointsExists(String endpointConfig) {

        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(endpointConfig);

            if (config.containsKey("sandbox_endpoints")) {
                return true;
            }
            if (StringUtils.equals(config.get("endpoint_type").toString(),"graphql")) {
                JSONObject httpConfig =(JSONObject) parser.parse(config.get("http").toString());
                if (httpConfig.containsKey("sandbox_endpoints")) {
                    return true;
                }
            }
        } catch (ParseException e) {
            log.error(APIConstants.MSG_JSON_PARSE_ERROR, e);
        } catch (ClassCastException e) {
            log.error(APIConstants.MSG_JSON_PARSE_ERROR, e);
        }
        return false;
    }

    public static boolean isProductionEndpointsExists(String endpointConfig) {

        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(endpointConfig);

            if (config.containsKey("production_endpoints")) {
                return true;
            }
            if (StringUtils.equals(config.get("endpoint_type").toString(),"graphql")) {
                JSONObject httpConfig =(JSONObject) parser.parse(config.get("http").toString());
                if (httpConfig.containsKey("production_endpoints")) {
                    return true;
                }
            }
        } catch (ParseException e) {
            log.error(APIConstants.MSG_JSON_PARSE_ERROR, e);
        } catch (ClassCastException e) {
            log.error(APIConstants.MSG_JSON_PARSE_ERROR, e);
        }
        return false;
    }

    /**
     * This method used to get API minimum information from governance artifact
     *
     * @param artifact API artifact
     * @param registry Registry
     * @return API
     * @throws APIManagementException if failed to get API from artifact
     */
    public static API getAPIInformation(GovernanceArtifact artifact, Registry registry) throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            api = new API(new APIIdentifier(providerName, apiName, apiVersion, artifact.getId()));
            //set uuid
            api.setUUID(artifact.getId());
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));
            api.setCorsConfiguration(getCorsConfigurationFromArtifact(artifact));
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setCreatedTime(String.valueOf(registry.get(artifactPath).getCreatedTime().getTime()));
        } catch (GovernanceException e) {
            String msg = "Failed to get API from artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * Get the cache key of the ResourceInfoDTO
     *
     * @param apiContext  - Context of the API
     * @param apiVersion  - API Version
     * @param resourceUri - The resource uri Ex: /name/version
     * @param httpMethod  - The http method. Ex: GET, POST
     * @return - The cache key
     */
    public static String getResourceInfoDTOCacheKey(String apiContext, String apiVersion,
                                                    String resourceUri, String httpMethod) {

        return apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;
    }

    /**
     * Get the key of the Resource ( used in scopes)
     *
     * @param api      - API
     * @param template - URI Template
     * @return - The resource key
     */
    public static String getResourceKey(API api, URITemplate template) {

        return APIUtil.getResourceKey(api.getContext(), api.getId().getVersion(), template.getUriTemplate(),
                template.getHTTPVerb());
    }

    /**
     * Get the key of the Resource using context, version, uri template and http verb.( used in scopes)
     *
     * @param apiIdentifier - API Identifier
     * @param template      - URI Template
     * @return - The resource key
     */
    public static String getResourceKey(APIIdentifier apiIdentifier, String context, URITemplate template) {

        return APIUtil.getResourceKey(context, apiIdentifier.getVersion(), template.getUriTemplate(),
                template.getHTTPVerb());
    }

    /**
     * Get the key of the Resource ( used in scopes)
     *
     * @param apiContext  - Context of the API
     * @param apiVersion  - API Version
     * @param resourceUri - The resource uri Ex: /name/version
     * @param httpMethod  - The http method. Ex: GET, POST
     * @return - The resource key
     */
    public static String getResourceKey(String apiContext, String apiVersion, String resourceUri, String httpMethod) {

        return apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;
    }

    /**
     * Find scope object in a set based on the key
     *
     * @param scopes - Set of scopes
     * @param key    - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes, String key) {

        for (Scope scope : scopes) {
            if (scope.getKey().equals(key)) {
                return scope;
            }
        }
        return null;
    }

    /**
     * Get the cache key of the APIInfoDTO
     *
     * @param apiContext - Context of the API
     * @param apiVersion - API Version
     * @return - The cache key of the APIInfoDTO
     */
    public static String getAPIInfoDTOCacheKey(String apiContext, String apiVersion) {

        return apiContext + "/" + apiVersion;
    }

    /**
     * Get the cache key of the Access Token
     *
     * @param accessToken - The access token which is cached
     * @param apiContext  - The context of the API
     * @param apiVersion  - The version of the API
     * @param resourceUri - The value of the resource url
     * @param httpVerb    - The http method. Ex: GET, POST
     * @param authLevel   - Required Authentication level. Ex: Application/Application User
     * @return - The Key which will be used to cache the access token
     */
    public static String getAccessTokenCacheKey(String accessToken, String apiContext, String apiVersion,
                                                String resourceUri, String httpVerb, String authLevel) {

        return accessToken + ':' + apiContext + '/' + apiVersion + resourceUri + ':' + httpVerb + ':' + authLevel;
    }

    /**
     * Resolves system properties and replaces in given in text
     *
     * @param text
     * @return System properties resolved text
     */
    public static String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
            // property
            // used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);

            if (propValue == null) {
                if ("carbon.context".equals(sysProp)) {
                    propValue = ServiceReferenceHolder.getContextService().getServerConfigContext().getContextRoot();
                } else if ("admin.username".equals(sysProp) || "admin.password".equals(sysProp)) {
                    try {
                        RealmConfiguration realmConfig =
                                new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
                        if ("admin.username".equals(sysProp)) {
                            propValue = realmConfig.getAdminUserName();
                        } else {
                            propValue = realmConfig.getAdminPassword();
                        }
                    } catch (UserStoreException e) {
                        // Can't throw an exception because the server is
                        // starting and can't be halted.
                        log.error("Unable to build the Realm Configuration", e);
                        return null;
                    }
                }
            }
            //Derive original text value with resolved system property value
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if ("carbon.home".equals(sysProp) && propValue != null
                    && ".".equals(propValue)) {
                text = new File(".").getAbsolutePath() + File.separator + text;
            }
        }
        return text;
    }

    public static String encryptPassword(String plainTextPassword) throws APIManagementException {

        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(plainTextPassword.getBytes(Charset.defaultCharset()));
        } catch (CryptoException e) {
            String errorMsg = "Error while encrypting the password. " + e.getMessage();
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * This method will check the validity of given url. WSDL url should be
     * contain http, https, "/t" (for tenant APIs) or file system path
     * otherwise we will mark it as invalid wsdl url. How ever here we do not
     * validate wsdl content.
     *
     * @param wsdlURL wsdl url tobe tested
     * @return true if its valid url else fale
     */
    public static boolean isValidWSDLURL(String wsdlURL, boolean required) {

        if (wsdlURL != null && !"".equals(wsdlURL)) {
            if (wsdlURL.startsWith("http:") || wsdlURL.startsWith("https:") ||
                    wsdlURL.startsWith("file:") || (wsdlURL.startsWith("/t") && !wsdlURL.endsWith(".zip"))) {
                return true;
            }
        } else if (!required) {
            // If the WSDL in not required and URL is empty, then we don't need
            // to add debug log.
            // Hence returning.
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("WSDL url validation failed. Provided wsdl url is not valid url: " + wsdlURL);
        }
        return false;
    }

    /**
     * Returns whether the provided URL content contains the string to match
     *
     * @param url   URL
     * @param match string to match
     * @return whether the provided URL content contains the string to match
     */
    public static boolean isURLContentContainsString(URL url, String match, int maxLines) {

        try (BufferedReader in =
                     new BufferedReader(new InputStreamReader(url.openStream(), Charset.defaultCharset()))) {
            String inputLine;
            StringBuilder urlContent = new StringBuilder();
            while ((inputLine = in.readLine()) != null && maxLines > 0) {
                maxLines--;
                urlContent.append(inputLine);
                if (urlContent.indexOf(match) > 0) {
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("Error Reading Input from Stream from " + url, e);

        }
        return false;
    }

    /**
     * load tenant axis configurations.
     *
     * @param tenantDomain
     */
    public static void loadTenantConfig(String tenantDomain) {

        final String finalTenantDomain = tenantDomain;
        ConfigurationContext ctx =
                ServiceReferenceHolder.getContextService().getServerConfigContext();

        //Cannot use the tenantDomain directly because it's getting locked in createTenantConfigurationContext()
        // method in TenantAxisUtils
        String accessFlag = tenantDomain + "@WSO2";

        long lastAccessed = TenantAxisUtils.getLastAccessed(tenantDomain, ctx);
        //Only if the tenant is in unloaded state, we do the loading
        if (System.currentTimeMillis() - lastAccessed >= tenantIdleTimeMillis) {
            synchronized (accessFlag.intern()) {
                // Currently loading tenants are added to a set.
                // If a tenant domain is in the set it implies that particular tenant is being loaded.
                // Therefore if and only if the set does not contain the tenant.
                if (!currentLoadingTenants.contains(tenantDomain)) {
                    //Only one concurrent request is allowed to add to the currentLoadingTenants
                    currentLoadingTenants.add(tenantDomain);
                    ctx.getThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {

                            Thread.currentThread().setName("APIMHostObjectUtils-loadTenantConfig-thread");
                            try {
                                PrivilegedCarbonContext.startTenantFlow();
                                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                        .setTenantDomain(finalTenantDomain, true);
                                ConfigurationContext ctx = ServiceReferenceHolder.getContextService()
                                        .getServerConfigContext();
                                TenantAxisUtils.getTenantAxisConfiguration(finalTenantDomain, ctx);
                            } catch (Exception e) {
                                log.error("Error while creating axis configuration for tenant " + finalTenantDomain, e);
                            } finally {
                                //only after the tenant is loaded completely, the tenant domain is removed from the set
                                currentLoadingTenants.remove(finalTenantDomain);
                                PrivilegedCarbonContext.endTenantFlow();
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * load tenant axis configurations.
     *
     * @param tenantDomain
     */
    public static void loadTenantConfigBlockingMode(String tenantDomain) {

        try {
            ConfigurationContext ctx = ServiceReferenceHolder.getContextService().getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, ctx);
        } catch (Exception e) {
            log.error("Error while creating axis configuration for tenant " + tenantDomain, e);
        }
    }

    public static String extractCustomerKeyFromAuthHeader(Map headersMap) {

        //From 1.0.7 version of this component onwards remove the OAuth authorization header from
        // the message is configurable. So we dont need to remove headers at this point.
        String authHeader = (String) headersMap.get(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return null;
        }

        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(APIConstants.OAUTH_HEADER_SPLITTER);
        for (String header : headers) {
            String[] elements = header.split(APIConstants.CONSUMER_KEY_SEGMENT_DELIMITER);
            if (elements.length > 1) {
                int j = 0;
                boolean isConsumerKeyHeaderAvailable = false;
                for (String element : elements) {
                    if (!"".equals(element.trim())) {
                        if (APIConstants.CONSUMER_KEY_SEGMENT.equals(elements[j].trim())) {
                            isConsumerKeyHeaderAvailable = true;
                        } else if (isConsumerKeyHeaderAvailable) {
                            return removeLeadingAndTrailing(elements[j].trim());
                        }
                    }
                    j++;
                }
            }
        }
        return null;
    }

    private static String removeLeadingAndTrailing(String base) {

        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     *
     * @param registryContext Registry Context instance which holds path mappings
     * @param path            default path of the registry
     * @return mounted path or path
     */
    public static String getMountedPath(RegistryContext registryContext, String path) {

        if (registryContext != null && path != null) {
            List<Mount> mounts = registryContext.getMounts();
            if (mounts != null) {
                for (Mount mount : mounts) {
                    if (path.equals(mount.getPath())) {
                        return mount.getTargetPath();
                    }
                }
            }
        }
        return path;
    }

    /**
     * Returns a map of gateway / store domains for the tenant
     *
     * @return a Map of domain names for tenant
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, String> getDomainMappings(String tenantDomain, String appType)
            throws APIManagementException {

        Map<String, String> domains = new HashMap<String, String>();
        String resourcePath;
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if (mappings.get(appType) != null) {
                    mappings = (JSONObject) mappings.get(appType);
                    for (Object o : mappings.entrySet()) {
                        Entry thisEntry = (Entry) o;
                        String key = (String) thisEntry.getKey();
                        //Instead strictly comparing customUrl, checking whether name is starting with customUrl
                        //to allow users to add multiple URLs if needed
                        if (!StringUtils.isEmpty(key) && key.startsWith(APIConstants.CUSTOM_URL)) {
                            String value = (String) thisEntry.getValue();
                            domains.put(key, value);
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving gateway domain mappings from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the gateway tenant domain mappings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the gateway tenant domain mappings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return domains;
    }

    /**
     * This method used to Downloaded Uploaded Documents from publisher
     *
     * @param userName     logged in username
     * @param resourceUrl  resource want to download
     * @param tenantDomain loggedUserTenantDomain
     * @return map that contains Data of the resource
     * @throws APIManagementException
     */
    public static Map<String, Object> getDocument(String userName, String resourceUrl, String tenantDomain)
            throws APIManagementException {

        Map<String, Object> documentMap = new HashMap<String, Object>();

        InputStream inStream = null;
        String[] resourceSplitPath =
                resourceUrl.split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        if (resourceSplitPath.length == 2) {
            resourceUrl = resourceSplitPath[1];
        } else {
            handleException("Invalid resource Path " + resourceUrl);
        }
        Resource apiDocResource;
        Registry registryType = null;
        boolean isTenantFlowStarted = false;
        try {
            int tenantId;
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;

                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            } else {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }

            userName = MultitenantUtils.getTenantAwareUsername(userName);
            registryType = ServiceReferenceHolder
                    .getInstance().
                            getRegistryService().getGovernanceUserRegistry(userName, tenantId);
            if (registryType.resourceExists(resourceUrl)) {
                apiDocResource = registryType.get(resourceUrl);
                inStream = apiDocResource.getContentStream();
                documentMap.put("Data", inStream);
                documentMap.put("contentType", apiDocResource.getMediaType());
                String[] content = apiDocResource.getPath().split("/");
                documentMap.put("name", content[content.length - 1]);
            }
        } catch (RegistryException e) {
            String msg = "Couldn't retrieve registry for User " + userName + " Tenant " + tenantDomain;
            log.error(msg, e);
            handleException(msg, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return documentMap;
    }

    /**
     * this method used to set environments values to api object.
     *
     * @param environments environments values in json format
     * @return set of environments that Published
     */
    public static Set<String> extractEnvironmentsForAPI(String environments) throws APIManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Set<String> environmentStringSet = extractEnvironmentsForAPI(environments, tenantDomain);

        return environmentStringSet;
    }

    public static Set<String> extractEnvironmentsForAPI(String environments, String organization) throws APIManagementException {

        Set<String> environmentStringSet = null;
        if (environments == null) {
            environmentStringSet = new HashSet<>(getEnvironments(organization).keySet());
        } else {
            //handle not to publish to any of the gateways
            if (APIConstants.API_GATEWAY_NONE.equals(environments)) {
                environmentStringSet = new HashSet<String>();
            }
            //handle to set published gateways nto api object
            else if (!"".equals(environments)) {
                String[] publishEnvironmentArray = environments.split(",");
                environmentStringSet = new HashSet<String>(Arrays.asList(publishEnvironmentArray));
                environmentStringSet.remove(APIConstants.API_GATEWAY_NONE);
            }
            //handle to publish to any of the gateways when api creating stage
            else if ("".equals(environments)) {
                environmentStringSet = new HashSet<>(getEnvironments(organization).keySet());
            }
        }

        return environmentStringSet;
    }

    /**
     * This method used to set environment values to governance artifact of API .
     *
     * @param api API object with the attributes value
     */
    public static String writeEnvironmentsToArtifact(API api) {

        StringBuilder publishedEnvironments = new StringBuilder();
        Set<String> apiEnvironments = api.getEnvironments();
        if (apiEnvironments != null) {
            for (String environmentName : apiEnvironments) {
                publishedEnvironments.append(environmentName).append(',');
            }

            if (apiEnvironments.isEmpty()) {
                publishedEnvironments.append("none,");
            }

            if (!publishedEnvironments.toString().isEmpty()) {
                publishedEnvironments.deleteCharAt(publishedEnvironments.length() - 1);
            }
        }
        return publishedEnvironments.toString();
    }

    /**
     * This method used to set environment values to governance artifact of APIProduct .
     *
     * @param apiProduct API object with the attributes value
     */
    public static String writeEnvironmentsToArtifact(APIProduct apiProduct) {

        StringBuilder publishedEnvironments = new StringBuilder();
        Set<String> apiEnvironments = apiProduct.getEnvironments();
        if (apiEnvironments != null) {
            for (String environmentName : apiEnvironments) {
                publishedEnvironments.append(environmentName).append(',');
            }

            if (apiEnvironments.isEmpty()) {
                publishedEnvironments.append("none,");
            }

            if (!publishedEnvironments.toString().isEmpty()) {
                publishedEnvironments.deleteCharAt(publishedEnvironments.length() - 1);
            }
        }
        return publishedEnvironments.toString();
    }

    /**
     * This method used to get the currently published gateway environments of an API .
     *
     * @param api API object with the attributes value
     */
    public static List<Environment> getEnvironmentsOfAPI(API api) throws APIManagementException {

        String organization = api.getOrganization();
        Map<String, Environment> gatewayEnvironments = getEnvironments(organization);
        Set<String> apiEnvironments = api.getEnvironments();
        List<Environment> returnEnvironments = new ArrayList<Environment>();

        for (Environment environment : gatewayEnvironments.values()) {
            for (String apiEnvironment : apiEnvironments) {
                if (environment.getName().equals(apiEnvironment)) {
                    returnEnvironments.add(environment);
                    break;
                }
            }
        }
        return returnEnvironments;
    }

    /**
     * Given the apps and the application name to check for, it will check if the application already exists.
     *
     * @param apps The collection of applications
     * @param name The application to be checked if exists
     * @return true - if an application of the name <name> already exists in the collection <apps>
     * false-  if an application of the name <name>  does not already exists in the collection <apps>
     */
    public static boolean doesApplicationExist(Application[] apps, String name) {

        boolean doesApplicationExist = false;
        if (apps != null) {
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                    doesApplicationExist = true;
                }
            }
        }
        return doesApplicationExist;
    }

    /**
     * Read the group id extractor class reference from api-manager.xml.
     *
     * @return group id extractor class reference.
     */
    public static String getGroupingExtractorImplementation() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        return config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION);
    }

    /**
     * Read the REST API group id extractor class reference from api-manager.xml.
     *
     * @return REST API group id extractor class reference.
     */
    public static String getRESTApiGroupingExtractorImplementation() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String restApiGroupingExtractor = config
                .getFirstProperty(APIConstants.API_STORE_REST_API_GROUP_EXTRACTOR_IMPLEMENTATION);
        if (StringUtils.isEmpty(restApiGroupingExtractor)) {
            restApiGroupingExtractor = getGroupingExtractorImplementation();
        }
        return restApiGroupingExtractor;
    }

    /**
     * This method will update the permission cache of the tenant which is related to the given usename
     *
     * @param username User name to find the relevant tenant
     * @throws UserStoreException if the permission update failed
     */
    public static void updatePermissionCache(String username) throws UserStoreException {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
        PermissionUpdateUtil.updatePermissionTree(tenantId);
    }

    /**
     * Check whether given application name is available under current subscriber or group
     *
     * @param subscriber      subscriber name
     * @param applicationName application name
     * @param groupId         group of the subscriber
     * @param organization    identifier of the organization
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public static boolean isApplicationExist(String subscriber, String applicationName, String groupId,
                                             String organization) throws APIManagementException {

        return ApiMgtDAO.getInstance().isApplicationExist(applicationName, subscriber, groupId, organization);
    }

    /**
     * Check whether given application , group combination exists
     *
     * @param subscriber      subscriber name
     * @param applicationName application name
     * @param groupId         group of the subscriber
     * @return true if application group combination exist
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public static boolean isApplicationGroupCombinationExist(String subscriber, String applicationName, String groupId)
            throws APIManagementException {
        return ApiMgtDAO.getInstance().isApplicationGroupCombinationExists(applicationName, subscriber, groupId);
    }

    /**
     * Check whether the new user has an application
     *
     * @param subscriber      subscriber name
     * @param applicationName application name
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public static boolean isApplicationOwnedBySubscriber(String subscriber, String applicationName, String organization)
            throws APIManagementException {

        return ApiMgtDAO.getInstance().isApplicationOwnedBySubscriber(applicationName, subscriber, organization);
    }

    public static String getHostAddress() {

        if (hostAddress != null) {
            return hostAddress;
        }
        hostAddress = ServerConfiguration.getInstance().getFirstProperty(APIConstants.API_MANAGER_HOSTNAME);
        if (null == hostAddress) {
            if (getLocalAddress() != null) {
                hostAddress = getLocalAddress().getHostName();
            }
            if (hostAddress == null) {
                hostAddress = APIConstants.API_MANAGER_HOSTNAME_UNKNOWN;
            }
            return hostAddress;
        } else {
            return hostAddress;
        }
    }

    private static InetAddress getLocalAddress() {

        Enumeration<NetworkInterface> ifaces = null;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Failed to get host address", e);
        }
        if (ifaces != null) {
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isStringArray(Object[] args) {

        for (Object arg : args) {
            if (!(arg instanceof String)) {
                return false;
            }
        }
        return true;
    }

    public static String appendDomainWithUser(String username, String domain) {

        if (username.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR) || username.contains(
                APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT) || MultitenantConstants.SUPER_TENANT_NAME.equalsIgnoreCase(username)) {
            return username;
        }
        return username + APIConstants.EMAIL_DOMAIN_SEPARATOR + domain;
    }

    /*
     *  Util method to convert a java object to a json object
     *
     */
    public static String convertToString(Object obj) {

        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String getSequencePath(APIIdentifier identifier, String pathFlow) {

        String artifactPath = APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                replaceEmailDomain(identifier.getProviderName()) + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        return artifactPath + RegistryConstants.PATH_SEPARATOR + pathFlow + RegistryConstants.PATH_SEPARATOR;
    }

    private static String getAPIMonetizationCategory(Set<Tier> tiers, String tenantDomain)
            throws APIManagementException {

        boolean isPaidFound = false;
        boolean isFreeFound = false;
        for (Tier tier : tiers) {
            if (isTierPaid(tier.getName(), tenantDomain)) {
                isPaidFound = true;
            } else {
                isFreeFound = true;

                if (isPaidFound) {
                    break;
                }
            }
        }

        if (!isPaidFound) {
            return APIConstants.API_CATEGORY_FREE;
        } else if (!isFreeFound) {
            return APIConstants.API_CATEGORY_PAID;
        } else {
            return APIConstants.API_CATEGORY_FREEMIUM;
        }
    }

    private static boolean isTierPaid(String tierName, String tenantDomainName) throws APIManagementException {

        String tenantDomain = tenantDomainName;
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        if (APIConstants.UNLIMITED_TIER.equalsIgnoreCase(tierName)) {
            return isUnlimitedTierPaid(tenantDomain);
        }

        boolean isPaid = false;
        Tier tier = getPolicyByName(PolicyConstants.POLICY_LEVEL_SUB, tierName, tenantDomain);

        if (tier != null) {
            final Map<String, Object> tierAttributes = tier.getTierAttributes();

            if (tierAttributes != null) {
                String isPaidValue = tier.getTierPlan();

                if (isPaidValue != null && APIConstants.COMMERCIAL_TIER_PLAN.equals(isPaidValue)) {
                    isPaid = true;
                }
            }
        } else {
            throw new APIManagementException("Tier " + tierName + "cannot be found");
        }
        return isPaid;
    }

    private static boolean isUnlimitedTierPaid(String tenantDomain) throws APIManagementException {
        JSONObject apiTenantConfig = getTenantConfig(tenantDomain);
        if (apiTenantConfig != null) {
            Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_IS_UNLIMITED_TIER_PAID);

            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            } else {
                throw new APIManagementException(APIConstants.API_TENANT_CONF_IS_UNLIMITED_TIER_PAID
                        + " config does not exist for tenant " + tenantDomain);
            }
        }

        return false;
    }

    public static Map<String, Tier> getTiers(String organization) throws APIManagementException {

        int requestedTenantId = getInternalOrganizationId(organization);

        if (requestedTenantId == 0) {
            return APIUtil.getAdvancedSubsriptionTiers();
        } else {
            return APIUtil.getAdvancedSubsriptionTiers(requestedTenantId);
        }
    }

    public static void clearTiersCache(String tenantDomain) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            getTiersCache().removeAll();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static Cache getTiersCache() {

        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                getCache(APIConstants.TIERS_CACHE);
    }

    /**
     * Return a http client instance
     *
     * @param url      - server url
     * @return
     */

    public static HttpClient getHttpClient(String url) throws APIManagementException {
        URL configUrl = null;
        try {
            configUrl = new URL(url);
        } catch (MalformedURLException e) {
            handleException("URL is malformed", e);
        }
        int port = configUrl.getPort();
        String protocol = configUrl.getProtocol();
        return APIUtil.getHttpClient(port, protocol);
    }

    /**
     * Return a http client instance
     *
     * @param protocol- service endpoint protocol http/https
     * @return
     */
    public static HttpClient getHttpClient(int port, String protocol) {

        HttpClientConfigurationDTO configuration = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration().getHttpClientConfiguration();
        return CommonAPIUtil.getHttpClient(protocol, configuration);
    }


    /**
     * This method will return a relative URL for given registry resource which we can used to retrieve the resource
     * from the web UI. For example, URI for a thumbnail icon of a tag can be generated from this method.
     *
     * @param resourceType Type of the registry resource. Based on this value the way URL is generate can be changed.
     * @param tenantDomain tenant domain of the resource
     * @param resourcePath path of the resource
     * @return relative path of the registry resource from the web context level
     */
    public static String getRegistryResourcePathForUI(APIConstants.RegistryResourceTypesForUI resourceType, String
            tenantDomain, String resourcePath) {

        StringBuilder resourcePathBuilder = new StringBuilder();
        if (APIConstants.RegistryResourceTypesForUI.TAG_THUMBNAIL.equals(resourceType)) {
            if (tenantDomain != null && !"".equals(tenantDomain)
                    && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                // The compiler will concatenate the 2 constants. If we use the builder to append the 2 constants, then
                // it will happen during the runtime.
                resourcePathBuilder.append(RegistryConstants.PATH_SEPARATOR + MultitenantConstants
                        .TENANT_AWARE_URL_PREFIX + RegistryConstants.PATH_SEPARATOR).append(tenantDomain);
            }
            // The compiler will concatenate the 2 constants. If we use the builder to append the 2 constants, then
            // it will happen during the runtime.
            resourcePathBuilder.append(
                    APIConstants.REGISTRY_RESOURCE_PREFIX + RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            resourcePathBuilder.append(resourcePath);
        }
        return resourcePathBuilder.toString();
    }

    /**
     * Gets the  class given the class name.
     *
     * @param className the fully qualified name of the class.
     * @return an instance of the class with the given name
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */

    public static Object getClassInstance(String className) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {

        return getClassForName(className).newInstance();
    }

    /**
     * Gets the  class given the class name.
     *
     * @param className the fully qualified name of the class.
     * @return an instance of the class with the given name
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */

    public static Class getClassForName(String className) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {

        return Class.forName(className);
    }

    /**
     * This method will check the validity of given url.
     * otherwise we will mark it as invalid url.
     *
     * @param url url tobe tested
     * @return true if its valid url else fale
     */
    public static boolean isValidURL(String url) {

        if (url == null) {
            return false;
        }
        try {
            URL urlVal = new URL(url);
            // If there are no issues, then this is a valid URL. Hence returning true.
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * @param tenantDomain Tenant domain to be used to get configurations for REST API scopes
     * @return JSON object which contains configuration for REST API scopes
     * @throws APIManagementException
     */
    public static JSONObject getTenantRESTAPIScopesConfig(String tenantDomain) throws APIManagementException {

        JSONObject restAPIConfigJSON = null;
        JSONObject tenantConfJson = getTenantConfig(tenantDomain);
        if (tenantConfJson != null) {
            restAPIConfigJSON = getRESTAPIScopesFromTenantConfig(tenantConfJson);
            if (restAPIConfigJSON == null) {
                throw new APIManagementException("RESTAPIScopes config does not exist for tenant "
                        + tenantDomain);
            }
        }
        return restAPIConfigJSON;
    }

    /**
     * @param tenantDomain Tenant domain to be used to get configurations for REST API scopes
     * @return JSON object which contains configuration for REST API scopes
     * @throws APIManagementException
     */
    public static JSONObject getTenantRESTAPIScopeRoleMappingsConfig(String tenantDomain) throws APIManagementException {

        JSONObject restAPIConfigJSON = null;
        JSONObject tenantConfJson = getTenantConfig(tenantDomain);
        if (tenantConfJson != null) {
            restAPIConfigJSON = getRESTAPIScopeRoleMappingsFromTenantConfig(tenantConfJson);
            if (restAPIConfigJSON == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No REST API role mappings are defined for the tenant " + tenantDomain);
                }
            }
        }
        return restAPIConfigJSON;
    }

    public static String getGAConfigFromRegistry(String tenantDomain) throws APIManagementException {

        return ServiceReferenceHolder.getInstance().getApimConfigService().getGAConfig(tenantDomain);
    }



    /**
     * Returns the tenant-conf.json in JSONObject format for the given tenant(id) from the registry.
     *
     * @param organization organization
     * @return tenant-conf.json in JSONObject format for the given tenant(id)
     * @throws APIManagementException when tenant-conf.json is not available in registry
     */
    public static JSONObject getTenantConfig(String organization) throws APIManagementException {

        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
        String cacheName = organization + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
        if (tenantConfigCache.containsKey(cacheName)) {
            return (JSONObject) tenantConfigCache.get(cacheName);
        } else {
            String tenantConfig =
             ServiceReferenceHolder.getInstance().getApimConfigService().getTenantConfig(organization);
            if (StringUtils.isNotEmpty(tenantConfig)){
                try {
                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(tenantConfig);
                    tenantConfigCache.put(cacheName, jsonObject);
                    return jsonObject;
                } catch (ParseException e) {
                    throw new APIManagementException("Error occurred while converting to json",e);
                }
            }
            return new JSONObject();
        }
    }

    private static JSONObject getRESTAPIScopesFromTenantConfig(JSONObject tenantConf) {

        return (JSONObject) tenantConf.get(APIConstants.REST_API_SCOPES_CONFIG);
    }

    private static JSONObject getRESTAPIScopeRoleMappingsFromTenantConfig(JSONObject tenantConf) {

        return (JSONObject) tenantConf.get(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
    }

    /**
     * This method gets the RESTAPIScopes configuration from REST_API_SCOPE_CACHE if available, if not from
     * tenant-conf.json in registry.
     *
     * @param tenantDomain tenant domain name
     * @return Map of scopes which contains scope names and associated role list
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getRESTAPIScopesForTenant(String tenantDomain) {

        Map<String, String> restAPIScopes;
        restAPIScopes = (Map) Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                .getCache(APIConstants.REST_API_SCOPE_CACHE)
                .get(tenantDomain);
        if (restAPIScopes == null) {
            try {
                restAPIScopes = APIUtil.getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain),
                        APIUtil.getTenantRESTAPIScopeRoleMappingsConfig(tenantDomain));
                //call load tenant config for rest API.
                //then put cache
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                        .getCache(APIConstants.REST_API_SCOPE_CACHE)
                        .put(tenantDomain, restAPIScopes);
            } catch (APIManagementException e) {
                log.error("Error while getting REST API scopes for tenant: " + tenantDomain, e);
            }
        }
        return restAPIScopes;
    }

    /**
     * This method gets the RESTAPIScopes configuration from tenant-conf.json in registry. Role Mappings (Role aliases
     * will not be substituted to the scope/role mappings)
     *
     * @param tenantDomain Tenant domain
     * @return RESTAPIScopes configuration without substituting role mappings
     * @throws APIManagementException error while getting RESTAPIScopes configuration
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getRESTAPIScopesForTenantWithoutRoleMappings(String tenantDomain)
            throws APIManagementException{
        return APIUtil.getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain), null);
    }

    /**
     * @param tenantDomain Tenant domain to be used to get default role configurations
     * @return JSON object which contains configuration for default roles
     * @throws APIManagementException
     */
    public static JSONObject getTenantDefaultRoles(String tenantDomain) throws APIManagementException {

        JSONObject apiTenantConfig;
        JSONObject defaultRolesConfigJSON = null;
        apiTenantConfig = getTenantConfig(tenantDomain);
        if (apiTenantConfig != null) {
            Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_DEFAULT_ROLES);
            if (value != null) {
                defaultRolesConfigJSON = (JSONObject) value;
            } else {
                //Config might not exist for migrated environments from previous release
                if (log.isDebugEnabled()) {
                    log.debug(APIConstants.API_TENANT_CONF_DEFAULT_ROLES + " config does not exist for tenant "
                            + tenantDomain);
                }
            }
        }
        return defaultRolesConfigJSON;
    }

    /**
     * @param scopesConfig JSON configuration object with scopes and associated roles
     * @param roleMappings JSON Configuration object with role mappings
     * @return Map of scopes which contains scope names and associated role list
     */
    public static Map<String, String> getRESTAPIScopesFromConfig(JSONObject scopesConfig, JSONObject roleMappings) {

        Map<String, String> scopes = new HashMap<String, String>();
        JSONArray scopesArray = (JSONArray) scopesConfig.get("Scope");
        for (Object scopeObj : scopesArray) {
            JSONObject scope = (JSONObject) scopeObj;
            String scopeName = scope.get(APIConstants.REST_API_SCOPE_NAME).toString();
            String scopeRoles = scope.get(APIConstants.REST_API_SCOPE_ROLE).toString();
            if (roleMappings != null) {
                if (log.isDebugEnabled()) {
                    log.debug("REST API scope role mappings exist. Hence proceeding to swap original scope roles "
                            + "for mapped scope roles.");
                }
                //split role list string read using comma separator
                List<String> originalRoles = Arrays.asList(scopeRoles.split("\\s*,\\s*"));
                List<String> mappedRoles = new ArrayList<String>();
                for (String role : originalRoles) {
                    String mappedRole = (String) roleMappings.get(role);
                    if (mappedRole != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(role + " was mapped to " + mappedRole);
                        }
                        mappedRoles.add(mappedRole);
                    } else {
                        mappedRoles.add(role);
                    }
                }
                scopeRoles = String.join(",", mappedRoles);
            }
            scopes.put(scopeName, scopeRoles);
        }
        return scopes;
    }

    /**
     * Determines if the scope is specified in the whitelist.
     *
     * @param scope - The scope key to check
     * @return - 'true' if the scope is white listed. 'false' if not.
     */
    public static boolean isAllowedScope(String scope) {

        if (allowedScopes == null) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            // Read scope whitelist from Configuration.
            List<String> whitelist = configuration.getProperty(APIConstants.ALLOWED_SCOPES);

            // If whitelist is null, default scopes will be put.
            if (whitelist == null) {
                whitelist = new ArrayList<String>();
                whitelist.add(APIConstants.OPEN_ID_SCOPE_NAME);
                whitelist.add(APIConstants.DEVICE_SCOPE_PATTERN);
            }

            allowedScopes = new HashSet<String>(whitelist);
        }

        for (String scopeTobeSkipped : allowedScopes) {
            if (scope.matches(scopeTobeSkipped)) {
                return true;
            }
        }
        return false;
    }

    public static int getManagementTransportPort(String mgtTransport) {

        if (StringUtils.isEmpty(mgtTransport)) {
            mgtTransport = APIConstants.HTTPS_PROTOCOL;
        }
        AxisConfiguration axisConfiguration = ServiceReferenceHolder
                .getContextService().getServerConfigContext().getAxisConfiguration();
        int mgtTransportPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        if (mgtTransportPort <= 0) {
            mgtTransportPort = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        }
        return mgtTransportPort;
    }

    public static int getCarbonTransportPort(String mgtTransport) {

        if (StringUtils.isEmpty(mgtTransport)) {
            mgtTransport = APIConstants.HTTPS_PROTOCOL;
        }
        AxisConfiguration axisConfiguration = ServiceReferenceHolder
                .getContextService().getServerConfigContext().getAxisConfiguration();
        return CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);

    }

    /*
     * Checks whether the proxy port is configured.
     * @param transport  The transport
     * @return boolean proxyport is enabled
     * */
    public static boolean isProxyPortEnabled(String mgtTransport) {

        AxisConfiguration axisConfiguration = ServiceReferenceHolder
                .getContextService().getServerConfigContext().getAxisConfiguration();
        int mgtTransportProxyPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        return mgtTransportProxyPort > 0;
    }

    public static String getServerURL() throws APIManagementException {

        String hostName = ServerConfiguration.getInstance().getFirstProperty(APIConstants.HOST_NAME);

        try {
            if (hostName == null) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw new APIManagementException("Error while trying to read hostname.", e);
        }

        String mgtTransport = CarbonUtils.getManagementTransport();
        int mgtTransportPort = getManagementTransportPort(mgtTransport);
        String serverUrl = mgtTransport + "://" + hostName.toLowerCase();
        // If it's well known HTTPS port, skip adding port
        if (mgtTransportPort != APIConstants.DEFAULT_HTTPS_PORT) {
            serverUrl += ":" + mgtTransportPort;
        }
        // If ProxyContextPath is defined then append it
        String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(APIConstants.PROXY_CONTEXT_PATH);
        if (proxyContextPath != null && !proxyContextPath.trim().isEmpty()) {
            if (proxyContextPath.charAt(0) == '/') {
                serverUrl += proxyContextPath;
            } else {
                serverUrl += "/" + proxyContextPath;
            }
        }

        return serverUrl;
    }

    /**
     * Returns the configuration of the Identity Provider. This is used for login/logout operation of API Publisher and
     * API Developer Portal. By default, this is not defined in the configuration hence this returns null. In that
     * case, local server will be used as the IDP.
     *
     * @return configuration of the Identity Provider from the api-manager configuration
     */
    public static IDPConfiguration getIdentityProviderConfig() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getIdentityProviderConfig();
    }

    /**
     * Returns Product REST APIs' cache configuration by reading from api-manager.xml
     *
     * @return Product REST APIs' cache configuration.
     */
    public static RESTAPICacheConfiguration getRESTAPICacheConfig() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getRESTAPICacheConfig();
    }

    /**
     * Get the API Provider name by giving the api name version and the tenant which it belongs to
     *
     * @param apiName    Name of the API
     * @param apiVersion Version of the API
     * @param tenant     Tenant name
     * @return Provider name who created the API
     * @throws APIManagementException
     */
    public static String getAPIProviderFromAPINameVersionTenant(String apiName, String apiVersion, String tenant)
            throws APIManagementException {

        return ApiMgtDAO.getInstance().getAPIProviderByNameAndVersion(apiName, apiVersion, tenant);
    }

    /**
     * Used to generate CORS Configuration object from CORS Configuration Json
     *
     * @param jsonString json representation of CORS configuration
     * @return CORSConfiguration Object
     */
    public static CORSConfiguration getCorsConfigurationDtoFromJson(String jsonString) {

        return new Gson().fromJson(jsonString, CORSConfiguration.class);

    }

    public static WebsubSubscriptionConfiguration getWebsubSubscriptionConfigurationDtoFromJson(String jsonString) {
        return new Gson().fromJson(jsonString, WebsubSubscriptionConfiguration.class);
    }

    /**
     * Used to generate Json string from CORS Configuration object
     *
     * @param corsConfiguration CORSConfiguration Object
     * @return Json string according to CORSConfiguration Object
     */
    public static String getCorsConfigurationJsonFromDto(CORSConfiguration corsConfiguration) {

        return new Gson().toJson(corsConfiguration);
    }

    public static String getWebsubSubscriptionConfigurationJsonFromDto(
            WebsubSubscriptionConfiguration websubSubscriptionConfiguration) {
            return new Gson().toJson(websubSubscriptionConfiguration);
    }

    public static String getWsUriMappingJsonFromDto(Map<String, String> mappings) {
        return new Gson().toJson(mappings);
    }

    /**
     * Used to get access control allowed headers according to the api-manager.xml
     *
     * @return access control allowed headers string
     */
    public static String getAllowedHeaders() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS);
    }

    /**
     * Used to get access control allowed methods define in api-manager.xml
     *
     * @return access control allowed methods string
     */
    public static String getAllowedMethods() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS);
    }

    /**
     * Used to get access control expose headers define in api-manager.xml
     *
     * @return access control expose headers string
     */
    public static String getAccessControlExposedHeaders() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_EXPOSE_HEADERS);
    }

    /**
     * Used to get access control allowed credential define in api-manager.xml
     *
     * @return true if access control allow credential enabled
     */
    public static boolean isAllowCredentials() {

        String allowCredentials =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                        getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_CREDENTIALS);
        return Boolean.parseBoolean(allowCredentials);
    }

    /**
     * Used to get CORS Configuration enabled from api-manager.xml
     *
     * @return true if CORS-Configuration is enabled in api-manager.xml
     */
    public static boolean isCORSEnabled() {

        String corsEnabled =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                        getFirstProperty(APIConstants.CORS_CONFIGURATION_ENABLED);

        return Boolean.parseBoolean(corsEnabled);
    }

    /**
     * Used to get the configuration which is set to decide whether CORS validation is enabled for Websocket APIs,
     * from api-manager.xml
     *
     * @return true if CORS validation for WS APIs is enabled in api-manager.xml
     */
    public static boolean isCORSValidationEnabledForWS() {

        String corsValidationEnabledForWS =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                        getFirstProperty(APIConstants.CORS_CONFIGURATION_ENABLE_VALIDATION_FOR_WS);

        return Boolean.parseBoolean(corsValidationEnabledForWS);
    }

    /**
     * Used to get access control allowed origins define in api-manager.xml
     *
     * @return allow origins list defined in api-manager.xml
     */
    public static String getAllowedOrigins() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN);

    }

    /**
     * Used to get CORSConfiguration according to the API artifact
     *
     * @param artifact registry artifact for the API
     * @return CORS Configuration object extract from the artifact
     * @throws GovernanceException if attribute couldn't fetch from the artifact.
     */
    public static CORSConfiguration getCorsConfigurationFromArtifact(GovernanceArtifact artifact)
            throws GovernanceException {

        CORSConfiguration corsConfiguration = APIUtil.getCorsConfigurationDtoFromJson(
                artifact.getAttribute(APIConstants.API_OVERVIEW_CORS_CONFIGURATION));
        if (corsConfiguration == null) {
            corsConfiguration = getDefaultCorsConfiguration();
        }
        return corsConfiguration;
    }

    public static WebsubSubscriptionConfiguration getWebsubSubscriptionConfigurationFromArtifact(
            GovernanceArtifact artifact) throws GovernanceException {
        WebsubSubscriptionConfiguration configuration = APIUtil.getWebsubSubscriptionConfigurationDtoFromJson(
                artifact.getAttribute(APIConstants.API_OVERVIEW_WEBSUB_SUBSCRIPTION_CONFIGURATION));
        if (configuration == null) {
            configuration = getDefaultWebsubSubscriptionConfiguration();
        }
        return configuration;
    }

    /**
     * Used to get Default CORS Configuration object according to configuration define in api-manager.xml
     *
     * @return CORSConfiguration object accordine to the defined values in api-manager.xml
     */
    public static CORSConfiguration getDefaultCorsConfiguration() {

        List<String> allowHeadersStringSet = Arrays.asList(getAllowedHeaders().split(","));
        List<String> allowMethodsStringSet = Arrays.asList(getAllowedMethods().split(","));
        List<String> allowOriginsStringSet = Arrays.asList(getAllowedOrigins().split(","));
        return new CORSConfiguration(false, allowOriginsStringSet, false, allowHeadersStringSet, allowMethodsStringSet);
    }

    public static WebsubSubscriptionConfiguration getDefaultWebsubSubscriptionConfiguration() {
        return new WebsubSubscriptionConfiguration(false, "",
                APIConstants.DEFAULT_WEBSUB_SIGNING_ALGO, APIConstants.DEFAULT_WEBSUB_SIGNATURE_HEADER);
    }

    /**
     * Used to get API name from synapse API Name
     *
     * @param api_version API name from synapse configuration
     * @return api name according to the tenant
     */
    public static String getAPINamefromRESTAPI(String api_version) {

        int index = api_version.indexOf("--");
        String api;
        if (index != -1) {
            api_version = api_version.substring(index + 2);
        }
        api = api_version.split(":")[0];
        index = api.indexOf("--");
        if (index != -1) {
            api = api.substring(index + 2);
        }
        return api;
    }

    /**
     * @param stakeHolder value "publisher" for publisher value "subscriber" for subscriber value "admin-dashboard" for admin
     *                    Return all alert types.
     * @return Hashmap of alert types.
     * @throws APIManagementException
     */
    public static HashMap<Integer, String> getAllAlertTypeByStakeHolder(String stakeHolder) throws APIManagementException {

        HashMap<Integer, String> map;
        map = ApiMgtDAO.getInstance().getAllAlertTypesByStakeHolder(stakeHolder);
        return map;
    }

    /**
     * @param userName    user name with tenant domain ex: admin@carbon.super
     * @param stakeHolder value "p" for publisher value "s" for subscriber value "a" for admin
     * @return map of saved values of alert types.
     * @throws APIManagementException
     */
    public static List<Integer> getSavedAlertTypesIdsByUserNameAndStakeHolder(String userName, String stakeHolder) throws APIManagementException {

        List<Integer> list;
        list = ApiMgtDAO.getInstance().getSavedAlertTypesIdsByUserNameAndStakeHolder(userName, stakeHolder);
        return list;

    }

    /**
     * This util method retrieves saved email list by user and stakeHolder name
     *
     * @param userName    user name with tenant ID.
     * @param stakeHolder if its publisher values should "p", if it is store value is "s" if admin dashboard value is "a"
     * @return List of eamil list.
     * @throws APIManagementException
     */
    public static List<String> retrieveSavedEmailList(String userName, String stakeHolder) throws APIManagementException {

        List<String> list;
        list = ApiMgtDAO.getInstance().retrieveSavedEmailList(userName, stakeHolder);

        return list;
    }

    private static boolean isDefaultQuotaPolicyContentAware(Policy policy) {

        if (PolicyConstants.BANDWIDTH_TYPE.equalsIgnoreCase(policy.getDefaultQuotaPolicy().getType())) {
            return true;
        }
        return false;
    }

    public static void addDefaultTenantAdvancedThrottlePolicies(String tenantDomain, int tenantId) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        /* Check if 'Unlimited' policy is available in AM_POLICY_APPLICATION table, to determine whether the default policies are loaded
         into the database at least once. If yes, default policies won't be added to database again. */

        if (apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, tenantId, APIConstants.DEFAULT_APP_POLICY_UNLIMITED)) {
            log.debug(
                    "Default Throttling Policies are not written into the database again, as they were added once at initial server startup");
            return;
        }

        Map<String, Long> defualtLimits = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getThrottleProperties().getDefaultThrottleTierLimits();
        long tenPerMinTier = defualtLimits.containsKey(APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN) : 10;
        long twentyPerMinTier = defualtLimits.containsKey(APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN) : 20;
        long fiftyPerMinTier = defualtLimits.containsKey(APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN) : 50;
        long[] requestCount = new long[]{fiftyPerMinTier, twentyPerMinTier, tenPerMinTier, Integer.MAX_VALUE};
        //Adding application level throttle policies
        String[] appPolicies = new String[]{
                APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, APIConstants.DEFAULT_APP_POLICY_UNLIMITED};
        String[] appPolicyDecs = new String[]{
                APIConstants.DEFAULT_APP_POLICY_LARGE_DESC, APIConstants.DEFAULT_APP_POLICY_MEDIUM_DESC,
                APIConstants.DEFAULT_APP_POLICY_SMALL_DESC, APIConstants.DEFAULT_APP_POLICY_UNLIMITED_DESC};
        String policyName;
        //Add application level throttle policies
        for (int i = 0; i < appPolicies.length; i++) {
            policyName = appPolicies[i];
            boolean needDeployment = false;
            ApplicationPolicy applicationPolicy = new ApplicationPolicy(policyName);
            applicationPolicy.setDisplayName(policyName);
            applicationPolicy.setDescription(appPolicyDecs[i]);
            applicationPolicy.setTenantId(tenantId);
            applicationPolicy.setDeployed(false);
            applicationPolicy.setTenantDomain(tenantDomain);
            QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
            RequestCountLimit requestCountLimit = new RequestCountLimit();
            requestCountLimit.setRequestCount(requestCount[i]);
            requestCountLimit.setUnitTime(1);
            requestCountLimit.setTimeUnit(APIConstants.TIME_UNIT_MINUTE);
            defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
            defaultQuotaPolicy.setLimit(requestCountLimit);
            applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);

            if (!apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, tenantId, policyName)) {
                apiMgtDAO.addApplicationPolicy(applicationPolicy);
                needDeployment = true;
            }
            if (!needDeployment) {
                continue;
            }
            String superTenantDomain = null;
            try {
                superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getSuperTenantDomain();
            } catch (UserStoreException e) {
                handleInternalException("Error in getting the super tenant domain", e);
            }
            boolean isSuperTenant = tenantDomain.equals(superTenantDomain);
            if (isSuperTenant) {
                continue;
            }

            if (!APIConstants.DEFAULT_APP_POLICY_UNLIMITED.equalsIgnoreCase(policyName)) {
                ApplicationPolicy retrievedPolicy = apiMgtDAO.getApplicationPolicy(policyName, tenantId);
                ApplicationPolicyEvent applicationPolicyEvent = new ApplicationPolicyEvent(
                        UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                        retrievedPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                        retrievedPolicy.getPolicyName(),
                        retrievedPolicy.getDefaultQuotaPolicy().getType(), retrievedPolicy.getRateLimitCount(),
                        retrievedPolicy.getRateLimitTimeUnit());
                APIUtil.sendNotification(applicationPolicyEvent, APIConstants.NotifierType.POLICY.name());
            }
        }

        long bronzeTierLimit = defualtLimits.containsKey(APIConstants.DEFAULT_SUB_POLICY_BRONZE) ?
                defualtLimits.get(APIConstants.DEFAULT_SUB_POLICY_BRONZE) : 1000;
        long silverTierLimit = defualtLimits.containsKey(APIConstants.DEFAULT_SUB_POLICY_SILVER) ?
                defualtLimits.get(APIConstants.DEFAULT_SUB_POLICY_SILVER) : 2000;
        long goldTierLimit = defualtLimits.containsKey(APIConstants.DEFAULT_SUB_POLICY_GOLD) ?
                defualtLimits.get(APIConstants.DEFAULT_SUB_POLICY_GOLD) : 5000;
        long unauthenticatedTierLimit = defualtLimits.containsKey(APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED) : 500;
        //Adding Subscription level policies
        long[] requestCountSubPolicies = new long[]{goldTierLimit, silverTierLimit, bronzeTierLimit, unauthenticatedTierLimit, Integer.MAX_VALUE};
        String[] subPolicies = new String[]{APIConstants.DEFAULT_SUB_POLICY_GOLD, APIConstants.DEFAULT_SUB_POLICY_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED, APIConstants.DEFAULT_SUB_POLICY_UNLIMITED};
        String[] subPolicyDecs = new String[]{
                APIConstants.DEFAULT_SUB_POLICY_GOLD_DESC, APIConstants.DEFAULT_SUB_POLICY_SILVER_DESC,
                APIConstants.DEFAULT_SUB_POLICY_BRONZE_DESC, APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED_DESC, APIConstants.DEFAULT_SUB_POLICY_UNLIMITED_DESC};
        for (int i = 0; i < subPolicies.length; i++) {
            policyName = subPolicies[i];
            boolean needDeployment = false;
            SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(policyName);
            subscriptionPolicy.setDisplayName(policyName);
            subscriptionPolicy.setDescription(subPolicyDecs[i]);
            subscriptionPolicy.setTenantId(tenantId);
            subscriptionPolicy.setDeployed(false);
            subscriptionPolicy.setTenantDomain(tenantDomain);
            QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
            RequestCountLimit requestCountLimit = new RequestCountLimit();
            requestCountLimit.setRequestCount(requestCountSubPolicies[i]);
            requestCountLimit.setUnitTime(1);
            requestCountLimit.setTimeUnit(APIConstants.TIME_UNIT_MINUTE);
            defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
            defaultQuotaPolicy.setLimit(requestCountLimit);
            subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
            subscriptionPolicy.setStopOnQuotaReach(true);
            subscriptionPolicy.setBillingPlan(APIConstants.BILLING_PLAN_FREE);

            if (!apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, tenantId, policyName)) {
                apiMgtDAO.addSubscriptionPolicy(subscriptionPolicy);
                needDeployment = true;
            }

            if (!needDeployment) {
                continue;
            }
            String superTenantDomain = null;
            try {
                superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getSuperTenantDomain();
            } catch (UserStoreException e) {
                handleInternalException("Error in getting the super tenant domain", e);
            }

            boolean isSuperTenant = tenantDomain.equals(superTenantDomain);
            if (isSuperTenant) {
                continue;
            }

            if (!APIConstants.DEFAULT_SUB_POLICY_UNLIMITED.equalsIgnoreCase(policyName)) {
                SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantId);
                deployRetrievedSubscriptionPolicy(tenantId, retrievedPolicy);
            }
        }

        //Adding Event based subscription level policies for async policies (WS & SSE)
        long[] eventCountSubPolicyValues = new long[]{50000, 25000, 5000, Integer.MAX_VALUE};
        String[] eventCountSubPolicyNames = new String[]{APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD, APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE, APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED};
        String[] eventCountSubPolicyDescriptions = new String[]{
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_GOLD_DESC, APIConstants.DEFAULT_SUB_POLICY_ASYNC_SILVER_DESC,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_BRONZE_DESC, APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED_DESC};

        for (int i = 0; i < eventCountSubPolicyNames.length; i++) {
            policyName = eventCountSubPolicyNames[i];
            boolean needDeployment = false;
            SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(policyName);
            subscriptionPolicy.setDisplayName(policyName);
            subscriptionPolicy.setDescription(eventCountSubPolicyDescriptions[i]);
            subscriptionPolicy.setTenantId(tenantId);
            subscriptionPolicy.setDeployed(true);
            QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
            EventCountLimit eventCountLimit = new EventCountLimit();
            eventCountLimit.setEventCount(eventCountSubPolicyValues[i]);
            eventCountLimit.setUnitTime(1);
            eventCountLimit.setTimeUnit(APIConstants.TIME_UNIT_DAY);
            defaultQuotaPolicy.setType(PolicyConstants.EVENT_COUNT_TYPE);
            defaultQuotaPolicy.setLimit(eventCountLimit);
            subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
            subscriptionPolicy.setStopOnQuotaReach(true);
            subscriptionPolicy.setBillingPlan(APIConstants.BILLING_PLAN_FREE);

            if (!apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, tenantId, policyName)) {
                apiMgtDAO.addSubscriptionPolicy(subscriptionPolicy);
                needDeployment = true;
            }

            if (!needDeployment) {
                continue;
            }
            String superTenantDomain = null;
            try {
                superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getSuperTenantDomain();
            } catch (UserStoreException e) {
                handleInternalException("Error in getting the super tenant domain", e);
            }

            boolean isSuperTenant = tenantDomain.equals(superTenantDomain);
            if (isSuperTenant) {
                continue;
            }
            SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantId);
            deployRetrievedSubscriptionPolicy(tenantId, retrievedPolicy);
        }
        
        long tenThousandPerMinTier = defualtLimits.containsKey(APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN) : 10000;
        long twentyThousandPerMinTier = defualtLimits.containsKey(
                APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN) : 20000;
        long fiftyThousandPerMinTier = defualtLimits.containsKey(
                APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN) ?
                defualtLimits.get(APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN) : 50000;
        long[] requestCountAPIPolicies = new long[]{fiftyThousandPerMinTier, twentyThousandPerMinTier, tenThousandPerMinTier, Integer.MAX_VALUE};

        //Adding Resource level policies
        String[] apiPolicies = new String[]{
                APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN, APIConstants.DEFAULT_API_POLICY_UNLIMITED};
        String[] apiPolicyDecs = new String[]{
                APIConstants.DEFAULT_API_POLICY_ULTIMATE_DESC, APIConstants.DEFAULT_API_POLICY_PLUS_DESC,
                APIConstants.DEFAULT_API_POLICY_BASIC_DESC, APIConstants.DEFAULT_API_POLICY_UNLIMITED_DESC};
        for (int i = 0; i < apiPolicies.length; i++) {
            boolean needDeployment = false;
            policyName = apiPolicies[i];
            APIPolicy apiPolicy = new APIPolicy(policyName);
            apiPolicy.setDisplayName(policyName);
            apiPolicy.setDescription(apiPolicyDecs[i]);
            apiPolicy.setTenantId(tenantId);
            apiPolicy.setUserLevel(APIConstants.API_POLICY_API_LEVEL);
            apiPolicy.setDeployed(false);
            apiPolicy.setTenantDomain(tenantDomain);
            QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
            RequestCountLimit requestCountLimit = new RequestCountLimit();
            requestCountLimit.setRequestCount(requestCountAPIPolicies[i]);
            requestCountLimit.setUnitTime(1);
            requestCountLimit.setTimeUnit(APIConstants.TIME_UNIT_MINUTE);
            defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
            defaultQuotaPolicy.setLimit(requestCountLimit);
            apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);

            if (!apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_API, tenantId, policyName)) {
                apiMgtDAO.addAPIPolicy(apiPolicy);
                needDeployment = true;
            }

            if (!needDeployment) {
                continue;
            }
            String superTenantDomain = null;
            try {
                superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getSuperTenantDomain();
            } catch (UserStoreException e) {
                handleInternalException("Error in getting the super tenant domain", e);
            }

            boolean isSuperTenant = tenantDomain.equals(superTenantDomain);
            if (isSuperTenant) {
                continue;
            }

            if (!APIConstants.DEFAULT_API_POLICY_UNLIMITED.equalsIgnoreCase(policyName)) {
                APIPolicy retrievedPolicy = apiMgtDAO.getAPIPolicy(policyName, tenantId);
                List<Integer> addedConditionGroupIds = new ArrayList<>();
                for (Pipeline pipeline : retrievedPolicy.getPipelines()) {
                    addedConditionGroupIds.add(pipeline.getId());
                }
                APIPolicyEvent apiPolicyEvent = new APIPolicyEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                        retrievedPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                        retrievedPolicy.getPolicyName(),
                        retrievedPolicy.getDefaultQuotaPolicy().getType(), addedConditionGroupIds, null);
                APIUtil.sendNotification(apiPolicyEvent, APIConstants.NotifierType.POLICY.name());
            }
        }
    }

    public static void addDefaultTenantAsyncThrottlePolicies(String tenantDomain, int tenantId) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        String policyName;

        if (apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, tenantId, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED)) {
            log.debug(
                    "Default Throttling Policies are not written into the database again, as they were added once at initial server startup");
            return;
        }

        //Adding Event based Webhooks API specific policies (WEBSUB)
        long[] eventCountWHSubPolicyValues = new long[]{10000, 5000, 1000, Integer.MAX_VALUE};
        int[] subscriptionCountValues = new int[]{1000, 500, 100, Integer.MAX_VALUE};
        String[] eventCountWHSubPolicyNames = new String[]{APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED};
        String[] eventCountWHSubPolicyDescriptions = new String[]{
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_GOLD_DESC, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_SILVER_DESC,
                APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_BRONZE_DESC, APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED_DESC};

        for (int i = 0; i < eventCountWHSubPolicyNames.length; i++) {
            policyName = eventCountWHSubPolicyNames[i];
            boolean needDeployment = false;
            SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(policyName);
            subscriptionPolicy.setDisplayName(policyName);
            subscriptionPolicy.setDescription(eventCountWHSubPolicyDescriptions[i]);
            subscriptionPolicy.setTenantId(tenantId);
            subscriptionPolicy.setDeployed(true);
            QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
            EventCountLimit eventCountLimit = new EventCountLimit();
            eventCountLimit.setEventCount(eventCountWHSubPolicyValues[i]);
            eventCountLimit.setUnitTime(1);
            eventCountLimit.setTimeUnit(APIConstants.TIME_UNIT_MONTH);
            defaultQuotaPolicy.setType(PolicyConstants.EVENT_COUNT_TYPE);
            defaultQuotaPolicy.setLimit(eventCountLimit);
            subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
            subscriptionPolicy.setStopOnQuotaReach(true);
            subscriptionPolicy.setBillingPlan(APIConstants.BILLING_PLAN_FREE);
            subscriptionPolicy.setSubscriberCount(subscriptionCountValues[i]);

            if (!apiMgtDAO.isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, tenantId, policyName)) {
                apiMgtDAO.addSubscriptionPolicy(subscriptionPolicy);
                needDeployment = true;
            }

            if (!needDeployment) {
                continue;
            }
            String superTenantDomain = null;
            try {
                superTenantDomain = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getSuperTenantDomain();
            } catch (UserStoreException e) {
                handleInternalException("Error in getting the super tenant domain", e);
            }

            boolean isSuperTenant = tenantDomain.equals(superTenantDomain);
            if (isSuperTenant) {
                continue;
            }

            SubscriptionPolicy retrievedPolicy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantId);
            deployRetrievedSubscriptionPolicy(tenantId, retrievedPolicy);
        }
    }

    private static void deployRetrievedSubscriptionPolicy(int tenantId, SubscriptionPolicy retrievedPolicy) {

        SubscriptionPolicyEvent subscriptionPolicyEvent = new SubscriptionPolicyEvent(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), tenantId,
                retrievedPolicy.getTenantDomain(), retrievedPolicy.getPolicyId(),
                retrievedPolicy.getPolicyName(), retrievedPolicy.getDefaultQuotaPolicy().getType(),
                retrievedPolicy.getRateLimitCount(), retrievedPolicy.getRateLimitTimeUnit(),
                retrievedPolicy.isStopOnQuotaReach(),
                retrievedPolicy.getGraphQLMaxDepth(), retrievedPolicy.getGraphQLMaxComplexity(),
                retrievedPolicy.getSubscriberCount());
        APIUtil.sendNotification(subscriptionPolicyEvent, APIConstants.NotifierType.POLICY.name());
    }

    /**
     * Used to get unlimited throttling tier is enable
     *
     * @return condition of enable unlimited tier
     */
    public static boolean isEnabledUnlimitedTier() {

        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getThrottleProperties();
        return throttleProperties.isEnableUnlimitedTier();

    }

    /**
     * Used to get subscription Spike arrest Enable
     *
     * @return condition of Subscription Spike arrest configuration
     */
    public static boolean isEnabledSubscriptionSpikeArrest() {

        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getThrottleProperties();
        return throttleProperties.isEnabledSubscriptionLevelSpikeArrest();
    }

    /**
     * This method is used to get the default policy in a given tenant space
     *
     * @param tenantDomain tenant domain name
     * @return default throttling policy for a given tenant
     */
    public static String getDefaultThrottlingPolicy(String tenantDomain) {
        String defaultTier = APIConstants.UNLIMITED_TIER;
        if (!isEnabledUnlimitedTier()) {
            // Set an available value if the Unlimited policy is disabled
            try {
                Map<String, Tier> tierMap = getTiers(APIConstants.TIER_RESOURCE_TYPE, tenantDomain);
                if (tierMap.size() > 0) {
                    defaultTier = tierMap.keySet().toArray()[0].toString();
                } else {
                    log.error("No throttle policies available in the tenant " + tenantDomain);
                }
            } catch (APIManagementException e) {
                log.error("Error while getting throttle policies for tenant " + tenantDomain);
            }
        }
        return defaultTier;
    }

    public static Map<String, Tier> getTiersFromPolicies(String policyLevel, int tenantId) throws APIManagementException {
        Map<String, Tier> tierMap = new TreeMap<String, Tier>();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Policy[] policies;
        if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getSubscriptionPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getAPIPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getApplicationPolicies(tenantId);
        } else {
            throw new APIManagementException("No such a policy type : " + policyLevel);
        }

        for (Policy policy : policies) {
            if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(policy.getPolicyName())) {
                Tier tier = new Tier(policy.getPolicyName());
                tier.setDescription(policy.getDescription());
                tier.setDisplayName(policy.getDisplayName());
                Limit limit = policy.getDefaultQuotaPolicy().getLimit();
                tier.setTimeUnit(limit.getTimeUnit());
                tier.setUnitTime(limit.getUnitTime());
                tier.setQuotaPolicyType(policy.getDefaultQuotaPolicy().getType());

                //If the policy is a subscription policy
                if (policy instanceof SubscriptionPolicy) {
                    SubscriptionPolicy subscriptionPolicy = (SubscriptionPolicy) policy;
                    tier.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
                    tier.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
                    setBillingPlanAndCustomAttributesToTier(subscriptionPolicy, tier);
                    if (StringUtils.equals(subscriptionPolicy.getBillingPlan(), APIConstants.COMMERCIAL_TIER_PLAN)) {
                        tier.setMonetizationAttributes(subscriptionPolicy.getMonetizationPlanProperties());
                    }
                }

                if (limit instanceof RequestCountLimit) {

                    RequestCountLimit countLimit = (RequestCountLimit) limit;
                    tier.setRequestsPerMin(countLimit.getRequestCount());
                    tier.setRequestCount(countLimit.getRequestCount());
                } else if (limit instanceof BandwidthLimit){
                    BandwidthLimit bandwidthLimit = (BandwidthLimit) limit;
                    tier.setRequestsPerMin(bandwidthLimit.getDataAmount());
                    tier.setRequestCount(bandwidthLimit.getDataAmount());
                    tier.setBandwidthDataUnit(bandwidthLimit.getDataUnit());
                } else {
                    EventCountLimit eventCountLimit = (EventCountLimit) limit;
                    tier.setRequestCount(eventCountLimit.getEventCount());
                    tier.setRequestsPerMin(eventCountLimit.getEventCount());
                }
                if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
                    tier.setTierPlan(((SubscriptionPolicy) policy).getBillingPlan());
                }
                tierMap.put(policy.getPolicyName(), tier);
            } else {
                if (APIUtil.isEnabledUnlimitedTier()) {
                    Tier tier = new Tier(policy.getPolicyName());
                    tier.setDescription(policy.getDescription());
                    tier.setDisplayName(policy.getDisplayName());
                    tier.setRequestsPerMin(Integer.MAX_VALUE);
                    tier.setRequestCount(Integer.MAX_VALUE);
                    if (isUnlimitedTierPaid(getTenantDomainFromTenantId(tenantId))) {
                        tier.setTierPlan(APIConstants.COMMERCIAL_TIER_PLAN);
                    } else {
                        tier.setTierPlan(APIConstants.BILLING_PLAN_FREE);
                    }

                    tierMap.put(policy.getPolicyName(), tier);
                }
            }
        }

        if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
            tierMap.remove(APIConstants.UNAUTHENTICATED_TIER);
        }
        return tierMap;
    }

    public static Tier getPolicyByName(String policyLevel, String policyName, String organization)
            throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Policy policy;
        if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
            policy = apiMgtDAO.getSubscriptionPolicy(policyName, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyLevel)) {
            policy = apiMgtDAO.getAPIPolicy(policyName, tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyLevel)) {
            policy = apiMgtDAO.getApplicationPolicy(policyName, tenantId);
        } else {
            throw new APIManagementException("No such a policy type : " + policyLevel);
        }
        if (policy != null) {
            if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(policy.getPolicyName())) {
                Tier tier = new Tier(policy.getPolicyName());
                tier.setDescription(policy.getDescription());
                tier.setDisplayName(policy.getDisplayName());
                Limit limit = policy.getDefaultQuotaPolicy().getLimit();
                tier.setTimeUnit(limit.getTimeUnit());
                tier.setUnitTime(limit.getUnitTime());
                tier.setQuotaPolicyType(policy.getDefaultQuotaPolicy().getType());

                //If the policy is a subscription policy
                if (policy instanceof SubscriptionPolicy) {
                    SubscriptionPolicy subscriptionPolicy = (SubscriptionPolicy) policy;
                    tier.setRateLimitCount(subscriptionPolicy.getRateLimitCount());
                    tier.setRateLimitTimeUnit(subscriptionPolicy.getRateLimitTimeUnit());
                    setBillingPlanAndCustomAttributesToTier(subscriptionPolicy, tier);
                    if (StringUtils.equals(subscriptionPolicy.getBillingPlan(), APIConstants.COMMERCIAL_TIER_PLAN)) {
                        tier.setMonetizationAttributes(subscriptionPolicy.getMonetizationPlanProperties());
                    }
                }

                if (limit instanceof RequestCountLimit) {
                    RequestCountLimit countLimit = (RequestCountLimit) limit;
                    tier.setRequestsPerMin(countLimit.getRequestCount());
                    tier.setRequestCount(countLimit.getRequestCount());
                } else if (limit instanceof BandwidthLimit) {
                    BandwidthLimit bandwidthLimit = (BandwidthLimit) limit;
                    tier.setRequestsPerMin(bandwidthLimit.getDataAmount());
                    tier.setRequestCount(bandwidthLimit.getDataAmount());
                    tier.setBandwidthDataUnit(bandwidthLimit.getDataUnit());
                } else {
                    EventCountLimit eventCountLimit = (EventCountLimit) limit;
                    tier.setRequestCount(eventCountLimit.getEventCount());
                    tier.setRequestsPerMin(eventCountLimit.getEventCount());
                }
                if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
                    tier.setTierPlan(((SubscriptionPolicy) policy).getBillingPlan());
                }
                return tier;
            } else {
                if (APIUtil.isEnabledUnlimitedTier()) {
                    Tier tier = new Tier(policy.getPolicyName());
                    tier.setDescription(policy.getDescription());
                    tier.setDisplayName(policy.getDisplayName());
                    tier.setRequestsPerMin(Integer.MAX_VALUE);
                    tier.setRequestCount(Integer.MAX_VALUE);
                    if (isUnlimitedTierPaid(getTenantDomainFromTenantId(tenantId))) {
                        tier.setTierPlan(APIConstants.COMMERCIAL_TIER_PLAN);
                    } else {
                        tier.setTierPlan(APIConstants.BILLING_PLAN_FREE);
                    }
                    return tier;
                }
            }
        }
        return null;
    }


    /**
     * Extract custom attributes and billing plan from subscription policy and set to tier.
     *
     * @param subscriptionPolicy - The SubscriptionPolicy object to extract details from
     * @param tier               - The Tier to set information into
     */
    public static void setBillingPlanAndCustomAttributesToTier(SubscriptionPolicy subscriptionPolicy, Tier tier) {

        //set the billing plan.
        tier.setTierPlan(subscriptionPolicy.getBillingPlan());

        //If the tier has custom attributes
        if (subscriptionPolicy.getCustomAttributes() != null &&
                subscriptionPolicy.getCustomAttributes().length > 0) {

            Map<String, Object> tierAttributes = new HashMap<String, Object>();
            try {
                String customAttr = new String(subscriptionPolicy.getCustomAttributes(), "UTF-8");
                JSONParser parser = new JSONParser();
                JSONArray jsonArr = (JSONArray) parser.parse(customAttr);
                Iterator jsonArrIterator = jsonArr.iterator();
                while (jsonArrIterator.hasNext()) {
                    JSONObject json = (JSONObject) jsonArrIterator.next();
                    tierAttributes.put(String.valueOf(json.get("name")), json.get("value"));
                }
                tier.setTierAttributes(tierAttributes);
            } catch (ParseException e) {
                log.error("Unable to convert String to Json", e);
                tier.setTierAttributes(null);
            } catch (UnsupportedEncodingException e) {
                log.error("Custom attribute byte array does not use UTF-8 character set", e);
                tier.setTierAttributes(null);
            }
        }
    }

    public static Set<Tier> getAvailableTiers(Map<String, Tier> definedTiers, String tiers, String apiName) {

        Set<Tier> availableTier = new HashSet<Tier>();
        if (tiers != null && !"".equals(tiers)) {
            String[] tierNames = tiers.split("\\|\\|");
            for (String tierName : tierNames) {
                Tier definedTier = definedTiers.get(tierName);
                if (definedTier != null) {
                    availableTier.add(definedTier);
                } else {
                    log.warn("Unknown tier: " + tierName + " found on API: " + apiName);
                }
            }
        }
        return availableTier;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {

        return IOUtils.toByteArray(is);
    }

    public static long ipToLong(String ipAddress) {

        long result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            //left shifting 24,16,8,0 and bitwise OR
            //1. 192 << 24
            //1. 168 << 16
            //1. 1   << 8
            //1. 2   << 0
            result |= ip << (i * 8);

        }
        return result;
    }

    /**
     * This method provides the BigInteger value for the given IP address. This supports both IPv4 and IPv6 address
     *
     * @param ipAddress ip address
     * @return BigInteger value for the given ip address. returns 0 for unknown host
     */
    public static BigInteger ipToBigInteger(String ipAddress) {

        InetAddress address;
        try {
            address = getAddress(ipAddress);
            byte[] bytes = address.getAddress();
            return new BigInteger(1, bytes);
        } catch (UnknownHostException e) {
            //ignore the error and log it
            log.error("Error while parsing host IP " + ipAddress, e);
        }
        return BigInteger.ZERO;
    }

    public static InetAddress getAddress(String ipAddress) throws UnknownHostException {

        return InetAddress.getByName(ipAddress);
    }

    public static boolean isIpInNetwork(String ip, String cidr) {

        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(cidr)) {
            return false;
        }
        ip = ip.trim();
        cidr = cidr.trim();

        if (cidr.contains("/")) {
            String[] cidrArr = cidr.split("/");
            if (cidrArr.length < 2 || (ip.contains(".") && !cidr.contains(".")) ||
                    (ip.contains(":") && !cidr.contains(":"))) {
                return false;
            }

            BigInteger netAddress = ipToBigInteger(cidrArr[0]);
            int netBits = Integer.parseInt(cidrArr[1]);
            BigInteger givenIP = ipToBigInteger(ip);

            if (ip.contains(".")) {
                // IPv4
                if (netAddress.shiftRight(IPV4_ADDRESS_BIT_LENGTH - netBits)
                        .shiftLeft(IPV4_ADDRESS_BIT_LENGTH - netBits).compareTo(
                                givenIP.shiftRight(IPV4_ADDRESS_BIT_LENGTH - netBits)
                                        .shiftLeft(IPV4_ADDRESS_BIT_LENGTH - netBits)) == 0) {
                    return true;
                }
            } else if (ip.contains(":")) {
                // IPv6
                if (netAddress.shiftRight(IPV6_ADDRESS_BIT_LENGTH - netBits)
                        .shiftLeft(IPV6_ADDRESS_BIT_LENGTH - netBits).compareTo(
                                givenIP.shiftRight(IPV6_ADDRESS_BIT_LENGTH - netBits)
                                        .shiftLeft(IPV6_ADDRESS_BIT_LENGTH - netBits)) == 0) {
                    return true;
                }
            }
        } else if (ip.equals(cidr)) {
            return true;
        }
        return false;
    }

    /**
     * Composes OR based search criteria from provided array of values
     *
     * @param values
     * @return
     */
    public static String getORBasedSearchCriteria(String[] values) {

        String criteria = "(";
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                criteria = criteria + values[i];
                if (i != values.length - 1) {
                    criteria = criteria + " OR ";
                } else {
                    criteria = criteria + ")";
                }
            }
            return criteria;
        }
        return null;
    }

    /**
     * Generates solr compatible search criteria synatax from user entered query criteria.
     * Ex: From version:1.0.0, this returns version=*1.0.0*
     *
     * @param criteria
     * @return solar compatible criteria
     * @throws APIManagementException
     */
    public static String getSingleSearchCriteria(String criteria) throws APIManagementException {

        criteria = criteria.trim();
        String searchValue = criteria;
        String searchKey = APIConstants.NAME_TYPE_PREFIX;

        if (criteria.contains(":")) {
            if (criteria.split(":").length > 1) {
                String[] splitValues = criteria.split(":");
                searchKey = splitValues[0].trim();
                searchValue = splitValues[1];
                //if search key is 'tag' instead of 'tags', allow it as well since rest api document says query
                // param to use for tag search is 'tag'

                if (APIConstants.TAG_SEARCH_TYPE_PREFIX.equals(searchKey)) {
                    searchKey = APIConstants.TAGS_SEARCH_TYPE_PREFIX;
                    searchValue = searchValue.replace(" ", "\\ ");
                }

                if (!APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey) &&
                        !APIConstants.TAGS_SEARCH_TYPE_PREFIX.equalsIgnoreCase(searchKey)) {
                    if (APIConstants.API_STATUS.equalsIgnoreCase(searchKey)) {
                        searchValue = searchValue.toLowerCase();
                    }
                    if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                        if (!searchValue.endsWith("*")) {
                            searchValue = searchValue + "*";
                        }
                        if (!searchValue.startsWith("*")) {
                            searchValue = "*" + searchValue;
                        }
                    }
                }

            } else {
                throw new APIManagementException("Search term is missing. Try again with valid search query.");
            }
        } else {
            if (!(searchValue.endsWith("\"") && searchValue.startsWith("\""))) {
                if (!searchValue.endsWith("*")) {
                    searchValue = searchValue + "*";
                }
                if (!searchValue.startsWith("*")) {
                    searchValue = "*" + searchValue;
                }
            }
        }
        if (APIConstants.API_PROVIDER.equalsIgnoreCase(searchKey)) {
            searchValue = searchValue.replaceAll("@", "-AT-");
        }
        return searchKey + "=" + searchValue;
    }

    /**
     * return whether store forum feature is enabled
     *
     * @return true or false indicating enable or not
     */
    public static boolean isStoreForumEnabled() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String forumEnabled = config.getFirstProperty(APIConstants.API_STORE_FORUM_ENABLED);
        if (forumEnabled == null) {
            return true;
        }
        return Boolean.parseBoolean(forumEnabled);
    }

    /**
     * Returns a secured DocumentBuilderFactory instance
     *
     * @return DocumentBuilderFactory
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        org.apache.xerces.impl.Constants Constants = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * Logs an audit message on actions performed on entities (APIs, Applications, etc). The log is printed in the
     * following JSON format
     * {
     * "typ": "API",
     * "action": "update",
     * "performedBy": "admin@carbon.super",
     * "info": {
     * "name": "Twitter",
     * "context": "/twitter",
     * "version": "1.0.0",
     * "provider": "nuwan"
     * }
     * }
     *
     * @param entityType  - The entity type. Ex: API, Application
     * @param entityInfo  - The details of the entity. Ex: API Name, Context
     * @param action      - The type of action performed. Ex: Create, Update
     * @param performedBy - The user who performs the action.
     */
    public static void logAuditMessage(String entityType, String entityInfo, String action, String performedBy) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("typ", entityType);
        jsonObject.put("action", action);
        jsonObject.put("performedBy", performedBy);
        if (entityInfo != null && !StringUtils.isBlank(entityInfo)) {
            try {
                JSONObject entityInfoJson = (JSONObject) new JSONParser().parse(entityInfo);
                jsonObject.put("info", entityInfoJson);
            } catch (ParseException ignored) { // if entityInfo cannot be parsed as json, log as a simple string
                jsonObject.put("info", entityInfo);
            }
        }
        audit.info(StringEscapeUtils.unescapeJava(jsonObject.toString()));
    }

    public static int getPortOffset() {

        ServerConfiguration carbonConfig = CarbonUtils.getServerConfiguration();
        String portOffset = System.getProperty(APIConstants.PORT_OFFSET_SYSTEM_VAR,
                carbonConfig.getFirstProperty(APIConstants.PORT_OFFSET_CONFIG));
        try {
            if ((portOffset != null)) {
                return Integer.parseInt(portOffset.trim());
            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid Port Offset: " + portOffset + ". Default value 0 will be used.", e);
            return 0;
        }
    }

    public static boolean isQueryParamDataPublishingEnabled() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getThrottleProperties().isEnableQueryParamConditions();
    }

    public static boolean isHeaderDataPublishingEnabled() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getThrottleProperties().isEnableHeaderConditions();
    }

    public static boolean isJwtTokenPublishingEnabled() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getThrottleProperties().isEnableJwtConditions();
    }

    public static String getAnalyticsServerURL() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIAnalyticsConfiguration().
                getDasServerUrl();
    }

    public static String getAnalyticsServerUserName() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIAnalyticsConfiguration().
                getDasReceiverServerUser();
    }

    public static String getAnalyticsServerPassword() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIAnalyticsConfiguration().
                getDasReceiverServerPassword();
    }

    /**
     * Create the Cache object from the given parameters
     *
     * @param cacheManagerName - Name of the Cache Manager
     * @param cacheName        - Name of the Cache
     * @param modifiedExp      - Value of the MODIFIED Expiry Type
     * @param accessExp        - Value of the ACCESSED Expiry Type
     * @return - The cache object
     */
    public synchronized static Cache getCache(final String cacheManagerName, final String cacheName, final long modifiedExp,
                                              final long accessExp) {

        Iterable<Cache<?, ?>> availableCaches = Caching.getCacheManager(cacheManagerName).getCaches();
        for (Cache cache : availableCaches) {
            if (cache.getName().equalsIgnoreCase(getCacheName(cacheName))) {
                return Caching.getCacheManager(cacheManagerName).getCache(cacheName);
            }
        }

        return Caching.getCacheManager(
                cacheManagerName).createCacheBuilder(cacheName).
                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                        modifiedExp)).
                setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                        accessExp)).setStoreByValue(false).build();
    }

    /**
     * Get the Cache object using cacheManagerName & cacheName
     *
     * @param cacheManagerName - Name of the Cache Manager
     * @param cacheName        - Name of the Cache
     * @return existing cache
     */
    public static Cache getCache(final String cacheManagerName, final String cacheName) {

        return Caching.getCacheManager(cacheManagerName).getCache(cacheName);
    }

    private static String getCacheName(String cacheName) {

        return (Boolean.parseBoolean(ServerConfiguration.getInstance().getFirstProperty("Cache.ForceLocalCache"))
                && !cacheName.startsWith("$__local__$.")) ? "$__local__$." + cacheName : cacheName;
    }

    /**
     * This method is used to get the actual endpoint password of an API from the hidden property
     * in the case where the handler APIEndpointPasswordRegistryHandler is enabled in registry.xml
     *
     * @param api      The API
     * @param registry The registry object
     * @return The actual password of the endpoint if exists
     * @throws RegistryException Throws if the api resource doesn't exist
     */
    private static String getActualEpPswdFromHiddenProperty(API api, Registry registry) throws RegistryException {

        String apiPath = APIUtil.getAPIPath(api.getId());
        Resource apiResource = registry.get(apiPath);
        return apiResource.getProperty(APIConstants.REGISTRY_HIDDEN_ENDPOINT_PROPERTY);
    }

    /**
     * To check whether given role exist in the array of roles.
     *
     * @param userRoleList      Role list to check against.
     * @param accessControlRole Access Control Role.
     * @return true if the Array contains the role specified.
     */
    public static boolean compareRoleList(String[] userRoleList, String accessControlRole) {

        if (userRoleList != null) {
            for (String userRole : userRoleList) {
                if (userRole.equalsIgnoreCase(accessControlRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * To clear the publisherRoleCache for certain users.
     *
     * @param userName Names of the user.
     */
    public static void clearRoleCache(String userName) {

        if (isPublisherRoleCacheEnabled) {
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(
                    APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE).remove(userName);
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(
                    APIConstants.API_USER_ROLE_CACHE).remove(userName);
        }
    }

    /**
     * Used in application sharing to check if this featuer is enabled
     *
     * @return returns true if ENABLE_MULTIPLE_GROUPID is set to True
     */
    public static boolean isMultiGroupAppSharingEnabled() {

        if (multiGrpAppSharing == null) {

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            String groupIdExtractorClass = config.getFirstProperty(
                    APIConstants.API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION);

            if (groupIdExtractorClass != null && !groupIdExtractorClass.isEmpty()) {
                try {

                    LoginPostExecutor groupingExtractor =
                            (LoginPostExecutor) APIUtil.getClassInstance(groupIdExtractorClass);

                    if (groupingExtractor instanceof NewPostLoginExecutor) {
                        multiGrpAppSharing = "true";
                    } else {
                        multiGrpAppSharing = "false";
                    }
                    // if there is a exception the default flow will work hence ingnoring the applications
                } catch (InstantiationException e) {
                    multiGrpAppSharing = "false";
                } catch (IllegalAccessException e) {
                    multiGrpAppSharing = "false";
                } catch (ClassNotFoundException e) {
                    multiGrpAppSharing = "false";
                }
            } else {
                multiGrpAppSharing = "false";
            }
        }
        return Boolean.valueOf(multiGrpAppSharing);
    }

    /**
     * Enable jwt for portal logins
     *
     * @return boolean value of the config
     */
    public static boolean isJWTEnabledForPortals() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String isEnabledJwtForPortals = config.getFirstProperty(APIConstants.IS_ENABLE_JWT_FOR_PORTALS);
        if (isEnabledJwtForPortals != null) {
            return Boolean.valueOf(isEnabledJwtForPortals);
        }

        return false;

    }
    /**
     * Used to check whether Provisioning Out-of-Band OAuth Clients feature is enabled
     *
     * @return true if feature is enabled
     */
    public static boolean isMapExistingAuthAppsEnabled() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String mappingEnabled = config.getFirstProperty(APIConstants.API_STORE_MAP_EXISTING_AUTH_APPS);
        if (mappingEnabled == null) {
            return false;
        }
        return Boolean.parseBoolean(mappingEnabled);
    }

    /**
     * Used to reconstruct the input search query as sub context and doc content doesn't support AND search
     *
     * @param query Input search query
     * @return Reconstructed new search query
     * @throws APIManagementException If there is an error in the search query
     */
    public static String constructNewSearchQuery(String query) throws APIManagementException {

        return constructQueryWithProvidedCriterias(query.trim());
    }

    /**
     * Used to reconstruct the input get APIs query as sub context and doc content doesn't support AND search
     *
     * @param query Input apis get query
     * @return Reconstructed new apis get query
     * @throws APIManagementException If there is an error in the search query
     */
    public static String constructApisGetQuery(String query) throws APIManagementException {

        String newSearchQuery = constructQueryWithProvidedCriterias(query.trim());
        if (!query.contains(APIConstants.TYPE)) {
            String typeCriteria = APIConstants.TYPE_SEARCH_TYPE_KEY + APIUtil.getORBasedSearchCriteria
                    (APIConstants.API_SUPPORTED_TYPE_LIST);
            newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + typeCriteria;
        }
        return newSearchQuery;
    }

    /**
     * @param inputSearchQuery search Query
     * @return Reconstructed new search query
     * @throws APIManagementException If there is an error in the search query
     */
    private static String constructQueryWithProvidedCriterias(String inputSearchQuery) throws APIManagementException {

        String newSearchQuery = "";
        // sub context and doc content doesn't support AND search
        if (inputSearchQuery != null && inputSearchQuery.contains(" ") && !inputSearchQuery
                .contains(APIConstants.TAG_COLON_SEARCH_TYPE_PREFIX) && (!inputSearchQuery
                .contains(APIConstants.CONTENT_SEARCH_TYPE_PREFIX) || inputSearchQuery.split(":").length > 2)) {
            if (inputSearchQuery.split(" ").length > 1) {
                String[] searchCriterias = inputSearchQuery.split(" ");
                for (int i = 0; i < searchCriterias.length; i++) {
                    if (searchCriterias[i].contains(":") && searchCriterias[i].split(":").length > 1) {
                        if (APIConstants.DOCUMENTATION_SEARCH_TYPE_PREFIX
                                .equalsIgnoreCase(searchCriterias[i].split(":")[0])) {
                            throw new APIManagementException("Invalid query. AND based search is not supported for "
                                    + "doc prefix");
                        }
                    }
                    if (i == 0) {
                        newSearchQuery = APIUtil.getSingleSearchCriteria(searchCriterias[i]);
                    } else {
                        newSearchQuery = newSearchQuery + APIConstants.SEARCH_AND_TAG + APIUtil
                                .getSingleSearchCriteria(searchCriterias[i]);
                    }
                }
            }
        } else {
            newSearchQuery = APIUtil.getSingleSearchCriteria(inputSearchQuery);
        }
        return newSearchQuery;
    }

    /**
     * Removes x-mediation-scripts from swagger as they should not be provided to store consumers
     *
     * @param apiSwagger swagger definition of API
     * @return swagger which exclude x-mediation-script elements
     */
    public static String removeXMediationScriptsFromSwagger(String apiSwagger) {
        //removes x-mediation-script key:values
        String mediationScriptRegex = "\"x-mediation-script\":\".*?(?<!\\\\)\"";
        Pattern pattern = Pattern.compile("," + mediationScriptRegex);
        Matcher matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        pattern = Pattern.compile(mediationScriptRegex + ",");
        matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        return apiSwagger;
    }

    /**
     * Removes x-wso2-request-interceptor and x-wso2-response-interceptor from swagger as they should not be provided
     * to store consumers
     *
     * @param apiSwagger swagger definition of API
     * @return swagger which exclude x-wso2-request-interceptor and x-wso2-response-interceptor elements
     */
    public static String removeInterceptorsFromSwagger(String apiSwagger) {
        // Removes x-wso2-request-interceptor key:values
        String requestInterceptorScriptRegex = "\"x-wso2-request-interceptor\" ?: ?\\{([^}]*)}";
        Pattern pattern = Pattern.compile("[,\\n ]*" + requestInterceptorScriptRegex);
        Matcher matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        pattern = Pattern.compile(requestInterceptorScriptRegex + ",?");
        matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }

        // Removes x-wso2-response-interceptor key:values
        String responseInterceptorScriptRegex = "[,\\n ]*\"x-wso2-response-interceptor\" ?: ?\\{([^}]*)}";
        pattern = Pattern.compile("[,\\n ]*" + responseInterceptorScriptRegex);
        matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }
        pattern = Pattern.compile(responseInterceptorScriptRegex + ",?");
        matcher = pattern.matcher(apiSwagger);
        while (matcher.find()) {
            apiSwagger = apiSwagger.replace(matcher.group(), "");
        }

        return apiSwagger;
    }

    /**
     * This method is used to get the authorization configurations from the tenant registry or from api-manager.xml if
     * config is not available in tenant registry
     *
     * @param organization The organization
     * @param property The configuration to get from tenant registry or api-manager.xml
     * @return The configuration read from tenant registry or api-manager.xml
     * @throws APIManagementException Throws if the registry resource doesn't exist
     *                                or the content cannot be parsed to JSON
     */
    public static String getOAuthConfiguration(String organization, String property)
            throws APIManagementException {

        String authConfigValue = APIUtil
                .getOAuthConfigurationFromTenantRegistry(organization, property);
        if (StringUtils.isBlank(authConfigValue)) {
            authConfigValue = APIUtil.getOAuthConfigurationFromAPIMConfig(property);
        }
        return authConfigValue;
    }

    /**
     * This method is used to get the authorization configurations from the tenant registry
     *
     * @param organization  organization.
     * @param property The configuration to get from tenant registry
     * @return The configuration read from tenant registry or else null
     * @throws APIManagementException Throws if the registry resource doesn't exist
     *                                or the content cannot be parsed to JSON
     */
    public static String getOAuthConfigurationFromTenantRegistry(String organization, String property)
            throws APIManagementException {

        JSONObject tenantConfig = getTenantConfig(organization);
        //Read the configuration from the tenant registry
        String oAuthConfiguration = "";
        if (null != tenantConfig.get(property)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tenantConfig.get(property));
            oAuthConfiguration = stringBuilder.toString();
        }

        if (!StringUtils.isBlank(oAuthConfiguration)) {
            return oAuthConfiguration;
        }

        return null;
    }

    /**
     * This method is used to get the authorization configurations from the api manager configurations
     *
     * @param property The configuration to get from api-manager.xml
     * @return The configuration read from api-manager.xml or else null
     * @throws APIManagementException Throws if the registry resource doesn't exist
     *                                or the content cannot be parsed to JSON
     */
    public static String getOAuthConfigurationFromAPIMConfig(String property)
            throws APIManagementException {

        //If tenant registry doesn't have the configuration, then read it from api-manager.xml
        APIManagerConfiguration apimConfig = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String oAuthConfiguration = apimConfig.getFirstProperty(APIConstants.OAUTH_CONFIGS + property);

        if (!StringUtils.isBlank(oAuthConfiguration)) {
            return oAuthConfiguration;
        }

        return null;
    }

    public static boolean isForgetPasswordConfigured() {

        AxisConfiguration axis2Config = ServiceReferenceHolder.getContextService().getServerConfigContext()
                .getAxisConfiguration();
        TransportOutDescription emailTransportSender = axis2Config.getTransportOut(APIConstants.EMAIL_TRANSPORT);
        if (emailTransportSender != null) {
            return true;
        }
        return false;
    }

    /**
     * Used to get the custom pagination limit for store
     *
     * @return returns the store pagination value from api-manager.xml
     */
    public static int getApisPerPageInStore() {

        String paginationLimit = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_APIS_PER_PAGE);
        if (paginationLimit != null) {
            return Integer.parseInt(paginationLimit);
        }
        return 0;
    }

    /**
     * Used to get the custom pagination limit for publisher
     *
     * @return returns the publisher pagination value from api-manager.xml
     */
    public static int getApisPerPageInPublisher() {

        String paginationLimit = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getFirstProperty(APIConstants.API_PUBLISHER_APIS_PER_PAGE);
        if (paginationLimit != null) {
            return Integer.parseInt(paginationLimit);
        }
        return 0;
    }

    /**
     * This method is used to get application from client id.
     *
     * @param clientId client id
     * @return application object.
     * @throws APIManagementException
     */
    public static Application getApplicationByClientId(String clientId) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getApplicationByClientId(clientId);
    }

    public static String getQuotaTypeForApplicationPolicy(String policyName, int tenantId)
            throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        ApplicationPolicy policy = apiMgtDAO.getApplicationPolicy(policyName, tenantId);
        if (policy != null) {
            return policy.getDefaultQuotaPolicy().getType();
        }
        return null;
    }

    public static String getMediationConfigurationFromAPIMConfig(String property) {
        APIManagerConfiguration apimConfig = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String mediatorConfiguration = apimConfig.getFirstProperty(APIConstants.MEDIATOR_CONFIG + property);
        if (!StringUtils.isBlank(mediatorConfiguration)) {
            return mediatorConfiguration;
        }
        return null;
    }

    public static List<ConditionDto> extractConditionDto(String base64EncodedString) throws ParseException {

        List<ConditionDto> conditionDtoList = new ArrayList<>();
        String base64Decoded = new String(Base64.decodeBase64(base64EncodedString));
        JSONArray conditionJsonArray = (JSONArray) new JSONParser().parse(base64Decoded);
        for (Object conditionJson : conditionJsonArray) {
            ConditionDto conditionDto = new ConditionDto();
            JSONObject conditionJsonObject = (JSONObject) conditionJson;
            if (conditionJsonObject.containsKey(PolicyConstants.IP_SPECIFIC_TYPE.toLowerCase())) {
                JSONObject ipSpecificCondition = (JSONObject) conditionJsonObject.get(PolicyConstants.IP_SPECIFIC_TYPE
                        .toLowerCase());
                ConditionDto.IPCondition ipCondition = new Gson().fromJson(ipSpecificCondition.toJSONString(),
                        ConditionDto.IPCondition.class);
                conditionDto.setIpCondition(ipCondition);
            } else if (conditionJsonObject.containsKey(PolicyConstants.IP_RANGE_TYPE.toLowerCase())) {
                JSONObject ipRangeCondition = (JSONObject) conditionJsonObject.get(PolicyConstants.IP_RANGE_TYPE
                        .toLowerCase());
                ConditionDto.IPCondition ipCondition = new Gson().fromJson(ipRangeCondition.toJSONString(),
                        ConditionDto.IPCondition.class);
                conditionDto.setIpRangeCondition(ipCondition);
            }
            if (conditionJsonObject.containsKey(PolicyConstants.JWT_CLAIMS_TYPE.toLowerCase())) {
                JSONObject jwtClaimConditions = (JSONObject) conditionJsonObject.get(PolicyConstants.JWT_CLAIMS_TYPE
                        .toLowerCase());
                ConditionDto.JWTClaimConditions jwtClaimCondition = new Gson().fromJson(jwtClaimConditions
                        .toJSONString(), ConditionDto.JWTClaimConditions.class);
                conditionDto.setJwtClaimConditions(jwtClaimCondition);
            }
            if (conditionJsonObject.containsKey(PolicyConstants.HEADER_TYPE.toLowerCase())) {
                JSONObject headerConditionJson = (JSONObject) conditionJsonObject.get(PolicyConstants.HEADER_TYPE
                        .toLowerCase());
                ConditionDto.HeaderConditions headerConditions = new Gson().fromJson(headerConditionJson
                        .toJSONString(), ConditionDto.HeaderConditions.class);
                conditionDto.setHeaderConditions(headerConditions);
            }

            if (conditionJsonObject.containsKey(PolicyConstants.QUERY_PARAMETER_TYPE.toLowerCase())) {
                JSONObject queryParamConditionJson = (JSONObject) conditionJsonObject.get(PolicyConstants
                        .QUERY_PARAMETER_TYPE.toLowerCase());
                ConditionDto.QueryParamConditions queryParamCondition = new Gson().fromJson(queryParamConditionJson
                        .toJSONString(), ConditionDto.QueryParamConditions.class);
                conditionDto.setQueryParameterConditions(queryParamCondition);
            }
            conditionDtoList.add(conditionDto);
        }
        conditionDtoList.sort(new Comparator<ConditionDto>() {
            @Override
            public int compare(ConditionDto o1, ConditionDto o2) {

                if (o1.getIpCondition() != null && o2.getIpCondition() == null) {
                    return -1;
                } else if (o1.getIpCondition() == null && o2.getIpCondition() != null) {
                    return 1;
                } else {
                    if (o1.getIpRangeCondition() != null && o2.getIpRangeCondition() == null) {
                        return -1;
                    } else if (o1.getIpRangeCondition() == null && o2.getIpRangeCondition() != null) {
                        return 1;
                    } else {
                        if (o1.getHeaderConditions() != null && o2.getHeaderConditions() == null) {
                            return -1;
                        } else if (o1.getHeaderConditions() == null && o2.getHeaderConditions() != null) {
                            return 1;
                        } else {
                            if (o1.getQueryParameterConditions() != null && o2.getQueryParameterConditions() == null) {
                                return -1;
                            } else if (o1.getQueryParameterConditions() == null && o2.getQueryParameterConditions()
                                    != null) {
                                return 1;
                            } else {
                                if (o1.getJwtClaimConditions() != null && o2.getJwtClaimConditions() == null) {
                                    return -1;
                                } else if (o1.getJwtClaimConditions() == null && o2.getJwtClaimConditions() != null) {
                                    return 1;
                                }
                            }
                        }
                    }
                }
                return 0;
            }
        });

        return conditionDtoList;
    }

    public static KeyManagerConfiguration toKeyManagerConfiguration(String base64EncodedString)
            throws APIManagementException {

        String decodedString = new String(Base64.decodeBase64(base64EncodedString));
        KeyManagerConfigurationDTO keyManagerConfigurationDTO = new Gson().fromJson(decodedString,
                KeyManagerConfigurationDTO.class);
        return toKeyManagerConfiguration(keyManagerConfigurationDTO);
    }

    public static KeyManagerConfiguration toKeyManagerConfiguration(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
    {

        KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();
        keyManagerConfiguration.setName(keyManagerConfigurationDTO.getName());
        keyManagerConfiguration.setConfiguration(keyManagerConfigurationDTO.getAdditionalProperties());
        keyManagerConfiguration.setTenantDomain(keyManagerConfigurationDTO.getOrganization());
        keyManagerConfiguration.setTokenType(KeyManagerConfiguration.TokenType.valueOf(keyManagerConfigurationDTO
                .getTokenType().toUpperCase()));
        keyManagerConfiguration.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerConfiguration.setType(keyManagerConfigurationDTO.getType());
        return keyManagerConfiguration;
    }

    /**
     * Get if there any tenant-specific application configurations from the tenant
     * registry
     *
     * @param organization The organization name.
     * @return JSONObject The Application Attributes read from tenant registry or else null
     * @throws APIManagementException Throws if the registry resource doesn't exist
     *                                or the content cannot be parsed to JSON
     */
    public static JSONObject getAppAttributeKeysFromRegistry(String organization) throws APIManagementException {

        JSONObject tenantConfigs = getTenantConfig(organization);
        String property = APIConstants.ApplicationAttributes.APPLICATION_CONFIGURATIONS;
        if (tenantConfigs.keySet().contains(property)) {
            return (JSONObject) tenantConfigs.get(
                    APIConstants.ApplicationAttributes.APPLICATION_CONFIGURATIONS);
        }
        return null;
    }

    /**
     * Get the Security Audit Attributes for tenant from the Registry
     *
     * @param organization organization name.
     * @return JSONObject JSONObject containing the properties
     * @throws APIManagementException Throw if a registry or parse exception arises
     */
    public static JSONObject getSecurityAuditAttributesFromRegistry(String organization) throws APIManagementException {

        JSONObject tenantConfigs = getTenantConfig(organization);
        String property = APIConstants.SECURITY_AUDIT_CONFIGURATION;
        if (tenantConfigs.containsKey(property)) {
            return (JSONObject) tenantConfigs.get(property);
        }
        return null;
    }

    /**
     * Validate the input file name for invalid path elements
     *
     * @param fileName
     */
    public static void validateFileName(String fileName) throws APIManagementException {

        if (!fileName.isEmpty() && (fileName.contains("../") || fileName.contains("..\\"))) {
            handleException("File name contains invalid path elements. " + fileName);
        }
    }

    /**
     * Convert special characters to encoded value.
     *
     * @param role
     * @return encorded value
     */
    public static String sanitizeUserRole(String role) {

        if (role.contains("&")) {
            return role.replaceAll("&", "%26");
        } else if (role.contains(" ")) {
            return role.replaceAll(" ", "%20");
        } else {
            return role;
        }
    }

    /**
     * Util method to call SP rest api to invoke queries.
     *
     * @param appName SP app name that the query should run against
     * @param query   query
     * @return jsonObj JSONObject of the response
     * @throws APIManagementException
     */
    public static JSONObject executeQueryOnStreamProcessor(String appName, String query) throws APIManagementException {

        String spEndpoint = APIManagerAnalyticsConfiguration.getInstance().getDasServerUrl() + "/stores/query";
        String spUserName = APIManagerAnalyticsConfiguration.getInstance().getDasServerUser();
        String spPassword = APIManagerAnalyticsConfiguration.getInstance().getDasServerPassword();
        byte[] encodedAuth = Base64
                .encodeBase64((spUserName + ":" + spPassword).getBytes(Charset.forName("ISO-8859-1")));
        String authHeader = "Basic " + new String(encodedAuth);
        URL spURL;
        try {
            spURL = new URL(spEndpoint);
            HttpClient httpClient = APIUtil.getHttpClient(spURL.getPort(), spURL.getProtocol());
            HttpPost httpPost = new HttpPost(spEndpoint);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            JSONObject obj = new JSONObject();
            obj.put("appName", appName);
            obj.put("query", query);

            if (log.isDebugEnabled()) {
                log.debug("Request from SP: " + obj.toJSONString());
            }

            StringEntity requestEntity = new StringEntity(obj.toJSONString(), ContentType.APPLICATION_JSON);

            httpPost.setEntity(requestEntity);

            HttpResponse response;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    String error = "Error while invoking SP rest api :  " + response.getStatusLine().getStatusCode()
                            + " " + response.getStatusLine().getReasonPhrase();
                    log.error(error);
                    throw new APIManagementException(error);
                }
                String responseStr = EntityUtils.toString(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Response from SP: " + responseStr);
                }
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(responseStr);

            } catch (ClientProtocolException e) {
                handleException("Error while connecting to the server ", e);
            } catch (IOException e) {
                handleException("Error while connecting to the server ", e);
            } catch (ParseException e) {
                handleException("Error while parsing the response ", e);
            } finally {
                httpPost.reset();
            }

        } catch (MalformedURLException e) {
            handleException("Error while parsing the stream processor url", e);
        }

        return null;

    }

    /**
     * Implemented to get the API usage count for monetization.
     *
     * @param from : the start timestamp of the query.
     * @param to   : the end timestamp of the query.
     * @return JSON Object.
     */
    public static JSONObject getUsageCountForMonetization(long from, long to)
            throws APIManagementException {

        JSONObject jsonObject = null;
        String granularity = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getMonetizationConfigurationDto().getGranularity();
        if (StringUtils.isEmpty(granularity)) {
            //set the default granularity to days, if it is not set in configuration
            granularity = APIConstants.Monetization.USAGE_PUBLISH_DEFAULT_GRANULARITY;
        }
        StringBuilder query = new StringBuilder(
                "from " + APIConstants.Monetization.MONETIZATION_USAGE_RECORD_AGG
                        + " within " + from
                        + "L, " + to + "L per '" + granularity
                        + "' select "
                        + APIConstants.Analytics.API_NAME + ", "
                        + APIConstants.Analytics.API_VERSION + ", "
                        + APIConstants.Analytics.API_CREATOR + ", "
                        + APIConstants.Analytics.API_CREATOR_TENANT_DOMAIN + ", "
                        + APIConstants.Analytics.APPLICATION_ID + ", "
                        + "sum (requestCount) as requestCount "
                        + "group by "
                        + APIConstants.Analytics.API_NAME + ", "
                        + APIConstants.Analytics.API_VERSION + ", "
                        + APIConstants.Analytics.API_CREATOR + ", "
                        + APIConstants.Analytics.API_CREATOR_TENANT_DOMAIN + ", "
                        + APIConstants.Analytics.APPLICATION_ID
        );
        try {
            jsonObject = APIUtil.executeQueryOnStreamProcessor(
                    APIConstants.Monetization.MONETIZATION_USAGE_RECORD_APP,
                    query.toString());
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
        } catch (APIManagementException ex) {
            String msg = "Unable to Retrieve monetization usage records";
            handleException(msg, ex);
        }
        return jsonObject;
    }

    public static boolean isDueToAuthorizationFailure(Throwable e) {

        Throwable rootCause = getPossibleErrorCause(e);
        return rootCause instanceof AuthorizationFailedException
                || rootCause instanceof APIMgtAuthorizationFailedException;
    }

    /**
     * Attempts to find the actual cause of the throwable 'e'
     *
     * @param e throwable
     * @return the root cause of 'e' if the root cause exists, otherwise returns 'e' itself
     */
    private static Throwable getPossibleErrorCause(Throwable e) {

        Throwable rootCause = ExceptionUtils.getRootCause(e);
        rootCause = rootCause == null ? e : rootCause;
        return rootCause;
    }

    /**
     * Notify document artifacts if an api state change occured. This change is required to re-trigger the document
     * indexer so that the documnet indexes will be updated with the new associated api status.
     *
     * @param apiArtifact
     * @param registry
     * @throws RegistryException
     * @throws APIManagementException
     */
    public static void notifyAPIStateChangeToAssociatedDocuments(GenericArtifact apiArtifact, Registry registry)
            throws RegistryException, APIManagementException {

        Association[] docAssociations = registry
                .getAssociations(apiArtifact.getPath(), APIConstants.DOCUMENTATION_ASSOCIATION);
        for (Association association : docAssociations) {
            String documentResourcePath = association.getDestinationPath();
            Resource docResource = registry.get(documentResourcePath);
            String oldStateChangeIndicatorStatus = docResource.getProperty(APIConstants.API_STATE_CHANGE_INDICATOR);
            String newStateChangeIndicatorStatus = "false";
            if (oldStateChangeIndicatorStatus != null) {
                newStateChangeIndicatorStatus = String.valueOf(!Boolean.parseBoolean(oldStateChangeIndicatorStatus));
            }
            docResource.setProperty(APIConstants.API_STATE_CHANGE_INDICATOR, "false");
            registry.put(documentResourcePath, docResource);
        }
    }

    /**
     * This method is used to extact group ids from Extractor.
     *
     * @param response               login response String.
     * @param groupingExtractorClass extractor class.
     * @return group ids
     * @throws APIManagementException Throws is an error occured when stractoing group Ids
     */
    public static String[] getGroupIdsFromExtractor(String response, String groupingExtractorClass)
            throws APIManagementException {

        if (groupingExtractorClass != null) {
            try {
                LoginPostExecutor groupingExtractor = (LoginPostExecutor) APIUtil.getClassInstance
                        (groupingExtractorClass);
                //switching 2.1.0 and 2.2.0
                if (APIUtil.isMultiGroupAppSharingEnabled()) {
                    NewPostLoginExecutor newGroupIdListExtractor = (NewPostLoginExecutor) groupingExtractor;
                    return newGroupIdListExtractor.getGroupingIdentifierList(response);
                } else {
                    String groupId = groupingExtractor.getGroupingIdentifiers(response);
                    return new String[]{groupId};
                }

            } catch (ClassNotFoundException e) {
                String msg = groupingExtractorClass + " is not found in runtime";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (ClassCastException e) {
                String msg = "Cannot cast " + groupingExtractorClass + " NewPostLoginExecutor";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (IllegalAccessException e) {
                String msg = "Error occurred while invocation of getGroupingIdentifier method";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            } catch (InstantiationException e) {
                String msg = "Error occurred while instantiating " + groupingExtractorClass + " class";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return null;
    }

    /**
     * This method is used to set environments values to api object.
     *
     * @param environments environments values in json format
     * @return set of environments that need to Publish
     */
    public static Set<String> extractEnvironmentsForAPI(List<String> environments, String organization) throws APIManagementException {

        Set<String> environmentStringSet = null;
        if (environments == null) {
            environmentStringSet = new HashSet<>(getEnvironments(organization).keySet());
        } else {
            //handle not to publish to any of the gateways
            if (environments.size() == 1 && APIConstants.API_GATEWAY_NONE.equals(environments.get(0))) {
                environmentStringSet = new HashSet<String>();
            }
            //handle to set published gateways into api object
            else if (environments.size() > 0) {
                environmentStringSet = new HashSet<String>(environments);
                environmentStringSet.remove(APIConstants.API_GATEWAY_NONE);
            }
            //handle to publish to any of the gateways when api creating stage
            else if (environments.size() == 0) {
                environmentStringSet = new HashSet<>(getEnvironments(organization).keySet());
            }
        }
        return environmentStringSet;
    }

    public static List<String> getGrantTypes() throws APIManagementException {

        OAuthAdminService oAuthAdminService = new OAuthAdminService();
        String[] allowedGrantTypes = oAuthAdminService.getAllowedGrantTypes();
        return Arrays.asList(allowedGrantTypes);
    }

    public static String getTokenUrl() throws APIManagementException {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.REVOKE_API_URL).
                replace(REVOKE, TOKEN);
    }

    public static String getStoreUrl() throws APIManagementException {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_URL);
    }

    public static Map<String, Environment> getEnvironments() throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        // get dynamic gateway environments read from database
        Map<String, Environment> envFromDB = ApiMgtDAO.getInstance().getAllEnvironments(tenantDomain).stream()
                .collect(Collectors.toMap(Environment::getName, env -> env));

        // clone and overwrite api-manager.xml environments with environments from DB if exists with same name
        Map<String, Environment> allEnvironments = new LinkedHashMap<>(getReadOnlyEnvironments());
        allEnvironments.putAll(envFromDB);
        return allEnvironments;
    }

    // Take organization as a parameter
    public static Map<String, Environment> getEnvironments(String organization) throws APIManagementException {
        // get dynamic gateway environments read from database
        Map<String, Environment> envFromDB = ApiMgtDAO.getInstance().getAllEnvironments(organization).stream()
                .collect(Collectors.toMap(Environment::getName, env -> env));

        // clone and overwrite api-manager.xml environments with environments from DB if exists with same name
        Map<String, Environment> allEnvironments = new LinkedHashMap<>(getReadOnlyEnvironments());
        allEnvironments.putAll(envFromDB);
        return allEnvironments;
    }

    /**
     * Get gateway environments defined in the configuration: api-manager.xml
     * @return map of configured environments against environment name
     */
    public static Map<String, Environment> getReadOnlyEnvironments() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getApiGatewayEnvironments();
    }

    /**
     * Get default (first) vhost of the given read only environment
     * @param environmentName name of the read only environment
     * @return default vhost of environment
     */
    public static VHost getDefaultVhostOfReadOnlyEnvironment(String environmentName) throws APIManagementException {
        Map<String, Environment> readOnlyEnvironments = getReadOnlyEnvironments();
        if (readOnlyEnvironments.get(environmentName) == null) {
            throw new APIManagementException("Configured read only environment not found: "
                    + environmentName);
        }
        if (readOnlyEnvironments.get(environmentName).getVhosts().isEmpty()) {
            throw new APIManagementException("VHosts not found for the environment: "
                    + environmentName);
        }
        return readOnlyEnvironments.get(environmentName).getVhosts().get(0);
    }

    private static QName getQNameWithIdentityNS(String localPart) {

        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }

    /**
     * Return autogenerated product scope when product ID is given
     *
     * @param productIdentifier product identifier
     * @return product scope
     */
    public static String getProductScope(APIProductIdentifier productIdentifier) {

        return APIConstants.PRODUCTSCOPE_PREFIX + "-" + productIdentifier.getName() + ":" + productIdentifier.getProviderName();
    }

    /**
     * Return the generated endpoint config JSON string for advertise only APIs
     *
     * @param api API object
     * @return generated JSON string
     */
    public static String generateEndpointConfigForAdvertiseOnlyApi(API api) {
        JSONObject endpointConfig = new JSONObject();
        endpointConfig.put("endpoint_type", "http");
        if (StringUtils.isNotEmpty(api.getApiExternalProductionEndpoint())) {
            JSONObject productionEndpoints = new JSONObject();
            productionEndpoints.put("url", api.getApiExternalProductionEndpoint());
            endpointConfig.put("production_endpoints", productionEndpoints);
        }
        if (StringUtils.isNotEmpty(api.getApiExternalSandboxEndpoint())) {
            JSONObject sandboxEndpoints = new JSONObject();
            sandboxEndpoints.put("url", api.getApiExternalSandboxEndpoint());
            endpointConfig.put("sandbox_endpoints", sandboxEndpoints);
        }
        return endpointConfig.toJSONString();
    }

    /**
     * Return the admin username read from the user-mgt.xml
     *
     * @return
     * @throws APIMgtInternalException
     */
    public static String getAdminUsername() throws APIMgtInternalException {

        String adminName = "admin";
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            adminName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();

        } catch (UserStoreException e) {
            handleInternalException("Error in getting admin username from user-mgt.xml", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return adminName;
    }

    /**
     * Return the admin password read from the user-mgt.xml
     *
     * @return
     * @throws APIMgtInternalException
     */
    public static String getAdminPassword() throws APIMgtInternalException {

        String adminPassword = "admin";
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            adminPassword = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminPassword();

        } catch (UserStoreException e) {
            handleInternalException("Error in getting admin password from user-mgt.xml", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return adminPassword;
    }

    /**
     * This method returns the base64 encoded for the given username and password
     *
     * @return base64 encoded username and password
     */
    public static String getBase64EncodedAdminCredentials() throws APIMgtInternalException {

        String credentials = getAdminUsername() + ":" + getAdminPassword();
        byte[] encodedCredentials = Base64.encodeBase64(
                credentials.getBytes(Charset.forName("UTF-8")));
        return new String(encodedCredentials, Charset.forName("UTF-8"));
    }

    /* Utility method to get api identifier from api path.
     *
     * @param productPath Path of the API Product in registry
     * @return relevant API Product Identifier
     */
    public static APIProductIdentifier getProductIdentifier(String productPath) {

        int length = (APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR).length();
        String relativePath = productPath.substring(length);
        String[] values = relativePath.split(RegistryConstants.PATH_SEPARATOR);
        if (values.length > 3) {
            return new APIProductIdentifier(values[0], values[1], values[2]);
        }
        return null;
    }

    /**
     * Utility method to get product documentation content file path
     *
     * @param productId         APIProductIdentifier
     * @param documentationName String
     * @return Doc content path
     */
    public static String getProductDocContentPath(APIProductIdentifier productId, String documentationName) {

        return getProductDocPath(productId) + documentationName;
    }

    /**
     * Utility method to get product documentation path
     *
     * @param productId APIProductIdentifier
     * @return Doc path
     */
    public static String getProductDocPath(APIProductIdentifier productId) {

        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                productId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                productId.getName() + RegistryConstants.PATH_SEPARATOR +
                productId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Check whether the user has the given role
     *
     * @param username Logged-in username
     * @param roleName role that needs to be checked
     * @throws UserStoreException
     */
    public static boolean checkIfUserInRole(String username, String roleName) throws UserStoreException {

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);

        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
        org.wso2.carbon.user.core.UserStoreManager manager = realm.getUserStoreManager();
        AbstractUserStoreManager abstractManager = (AbstractUserStoreManager) manager;
        return abstractManager.isUserInRole(tenantAwareUserName, roleName);
    }

    public static JSONArray getMonetizationAttributes() {

        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getMonetizationConfigurationDto().getMonetizationAttributes();
    }

    /**
     * Utility method to sign a JWT assertion with a particular signature algorithm
     *
     * @param assertion          valid JWT assertion
     * @param privateKey         private key which use to sign the JWT assertion
     * @param signatureAlgorithm signature algorithm which use to sign the JWT assertion
     * @return byte array of the JWT signature
     * @throws APIManagementException
     */
    public static byte[] signJwt(String assertion, PrivateKey privateKey, String signatureAlgorithm) throws APIManagementException {

        try {
            //initialize signature with private key and algorithm
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey);

            //update signature with data to be signed
            byte[] dataInBytes = assertion.getBytes(Charset.defaultCharset());
            signature.update(dataInBytes);

            //sign the assertion and return the signature
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            //do not log
            throw new APIManagementException("Signature algorithm not found", e);
        } catch (InvalidKeyException e) {
            //do not log
            throw new APIManagementException("Invalid private key provided for signing", e);
        } catch (SignatureException e) {
            //do not log
            throw new APIManagementException("Error while signing JWT", e);
        }
    }

    /**
     * Get the JWS compliant signature algorithm code of the algorithm used to sign the JWT.
     *
     * @param signatureAlgorithm - The algorithm used to sign the JWT. If signing is disabled, the value will be NONE.
     * @return - The JWS Compliant algorithm code of the signature algorithm.
     */
    public static String getJWSCompliantAlgorithmCode(String signatureAlgorithm) {

        if (signatureAlgorithm == null || NONE.equals(signatureAlgorithm)) {
            return JWTSignatureAlg.NONE.getJwsCompliantCode();
        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            return JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode();
        } else {
            return signatureAlgorithm;
        }
    }

    /**
     * Helper method to add kid claim into to JWT_HEADER.
     *
     * @param certThumbprint     thumbPrint generated for certificate
     * @param signatureAlgorithm relevant signature algorithm
     * @return KID
     */
    private static String getKID(String certThumbprint, String signatureAlgorithm) {

        return certThumbprint + "_" + signatureAlgorithm;
    }

    /**
     * Helper method to hexify a byte array.
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    public static String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }
        return buf.toString();
    }

    public static JwtTokenInfoDTO getJwtTokenInfoDTO(Application application, String userName, String tenantDomain)
            throws APIManagementException {

        int applicationId = application.getId();

        String appOwner = application.getOwner();
        APISubscriptionInfoDTO[] apis = ApiMgtDAO.getInstance()
                .getSubscribedAPIsForAnApp(appOwner, applicationId);

        JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
        jwtTokenInfoDTO.setSubscriber("sub");
        jwtTokenInfoDTO.setEndUserName(userName);
        jwtTokenInfoDTO.setContentAware(true);

        Set<String> subscriptionTiers = new HashSet<>();
        List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
        for (APISubscriptionInfoDTO api : apis) {
            subscriptionTiers.add(api.getSubscriptionTier());

            SubscribedApiDTO subscribedApiDTO = new SubscribedApiDTO();
            subscribedApiDTO.setName(api.getApiName());
            subscribedApiDTO.setContext(api.getContext());
            subscribedApiDTO.setVersion(api.getVersion());
            subscribedApiDTO.setPublisher(APIUtil.replaceEmailDomainBack(api.getProviderId()));
            subscribedApiDTO.setSubscriptionTier(api.getSubscriptionTier());
            subscribedApiDTO.setSubscriberTenantDomain(tenantDomain);
            subscribedApiDTOList.add(subscribedApiDTO);
        }
        jwtTokenInfoDTO.setSubscribedApiDTOList(subscribedApiDTOList);

        if (subscriptionTiers.size() > 0) {
            SubscriptionPolicy[] subscriptionPolicies = ApiMgtDAO.getInstance()
                    .getSubscriptionPolicies(subscriptionTiers.toArray(new String[0]), APIUtil.getTenantId(appOwner));

            Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<>();
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies) {
                SubscriptionPolicyDTO subscriptionPolicyDTO = new SubscriptionPolicyDTO();
                subscriptionPolicyDTO.setSpikeArrestLimit(subscriptionPolicy.getRateLimitCount());
                subscriptionPolicyDTO.setSpikeArrestUnit(subscriptionPolicy.getRateLimitTimeUnit());
                subscriptionPolicyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
                subscriptionPolicyDTO.setTierQuotaType(subscriptionPolicy.getTierQuotaType());
                subscriptionPolicyDTO.setGraphQLMaxDepth(subscriptionPolicy.getGraphQLMaxDepth());
                subscriptionPolicyDTO.setGraphQLMaxComplexity(subscriptionPolicy.getGraphQLMaxComplexity());
                subscriptionPolicyDTOList.put(subscriptionPolicy.getPolicyName(), subscriptionPolicyDTO);
            }
            jwtTokenInfoDTO.setSubscriptionPolicyDTOList(subscriptionPolicyDTOList);
        }
        return jwtTokenInfoDTO;
    }

    public static String getApiKeyAlias() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String alias = config.getFirstProperty(APIConstants.API_STORE_API_KEY_ALIAS);
        if (alias == null) {
            log.warn("The configurations related to Api Key alias in APIStore " +
                    "are missing in api-manager.xml. Hence returning the default value.");
            return APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS;
        }
        return alias;
    }

    public static String getInternalApiKeyAlias() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String alias = config.getFirstProperty(APIConstants.API_PUBLISHER_INTERNAL_API_KEY_ALIAS);
        if (alias == null) {
            log.warn("The configurations related to Api Key alias in API Publisher " +
                    "are missing in api-manager.xml. Hence returning the default value.");
            return APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS;
        }
        return alias;
    }

    public static String getApiKeyGeneratorImpl() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String keyGeneratorClassName = config.getFirstProperty(APIConstants.API_STORE_API_KEY_GENERATOR_IMPL);
        if (keyGeneratorClassName == null) {
            log.warn("The configurations related to Api Key Generator Impl class in APIStore " +
                    "is missing in api-manager.xml. Hence returning the default value.");
            return APIConstants.DEFAULT_API_KEY_GENERATOR_IMPL;
        }
        return keyGeneratorClassName;
    }

    public static String getApiKeySignKeyStoreName() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String apiKeySignKeyStoreName = config.getFirstProperty(APIConstants.API_STORE_API_KEY_SIGN_KEY_STORE);
        if (apiKeySignKeyStoreName == null) {
            log.warn("The configurations related to APIKey sign keystore in APIStore " +
                    "is missing in api-manager.xml. Hence returning the default value.");
            return APIConstants.DEFAULT_API_KEY_SIGN_KEY_STORE;
        }
        return apiKeySignKeyStoreName;
    }

    /**
     * Get the workflow status information for the given api for the given workflow type
     *
     * @param uuid Api uuid
     * @param workflowType  workflow type
     * @return WorkflowDTO
     * @throws APIManagementException
     */
    public static WorkflowDTO getAPIWorkflowStatus(String uuid, String workflowType)
            throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        int apiId = apiMgtDAO.getAPIID(uuid);
        return apiMgtDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId), workflowType);
    }

    /**
     * Get expiry time of a given jwt token. This method should be called only after validating whether the token is
     * JWT via isValidJWT method.
     *
     * @param token jwt token.
     * @return the expiry time.
     */
    public static Long getExpiryifJWT(String token) {

        String[] jwtParts = token.split("\\.");
        org.json.JSONObject jwtPayload = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder().
                decode(jwtParts[1])));
        return jwtPayload.getLong("exp"); // extract expiry time and return
    }

    /**
     * Checks whether the given token is a valid JWT by parsing header and validating the
     * header,payload,signature format
     *
     * @param token the token to be validated
     * @return true if valid JWT
     */
    public static boolean isValidJWT(String token) {

        boolean isJwtToken = false;
        try {
            // Check if the decoded header contains type as 'JWT'.
            if (StringUtils.countMatches(token, APIConstants.DOT) == 2) {
                isJwtToken = true;
            } else {
                log.debug("Not a valid JWT token. " + getMaskedToken(token));
            }
        } catch (JSONException | IllegalArgumentException e) {
            isJwtToken = false;
            log.debug("Not a valid JWT token. " + getMaskedToken(token), e);
        }
        return isJwtToken;
    }

    /**
     * Get signature of  given JWT token. This method should be called only after validating whether the token is
     * JWT via isValidJWT method.
     *
     * @param token jwt token.
     * @return signature of the jwt token.
     */
    public static String getSignatureIfJWT(String token) {

        String[] jwtParts = token.split("\\.");
        return jwtParts[2];
    }

    /**
     * Extracts the tenant domain of the subject in a given JWT
     *
     * @param token jwt token
     * @return tenant domain of the the sub claim
     */
    public static String getTenantDomainIfJWT(String token) {

        String[] jwtParts = token.split("\\.");
        org.json.JSONObject jwtPayload = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder().
                decode(jwtParts[1])));
        String jwtSubClaim = jwtPayload.getString("sub"); // extract sub claim from payload
        return MultitenantUtils.getTenantDomain(jwtSubClaim);
    }

    /**
     * Returns a masked token for a given token.
     *
     * @param token token to be masked
     * @return masked token.
     */
    public static String getMaskedToken(String token) {
        StringBuilder maskedTokenBuilder = new StringBuilder();
        if (token != null){
            int allowedVisibleLen = Math.min(token.length() / MIN_VISIBLE_LEN_RATIO, MAX_VISIBLE_LEN);
            if (token.length() > MAX_LEN) {
                maskedTokenBuilder.append("...");
                maskedTokenBuilder.append(String.join("", Collections.nCopies(MAX_LEN, MASK_CHAR)));
            } else {
                maskedTokenBuilder.append(String.join("", Collections.nCopies(token.length()
                        - allowedVisibleLen, MASK_CHAR)));
            }
            maskedTokenBuilder.append(token.substring(token.length() - allowedVisibleLen));
        }
        return maskedTokenBuilder.toString();
    }

    public static Certificate getCertificateFromParentTrustStore(String certAlias) throws APIManagementException {

        Certificate publicCert = null;
        //Read the client-truststore.jks into a KeyStore
        try {
            KeyStore trustStore = ServiceReferenceHolder.getInstance().getTrustStore();
            if (trustStore != null) {
                // Read public certificate from trust store
                publicCert = trustStore.getCertificate(certAlias);
            }
        } catch (KeyStoreException e) {
            String msg = "Error in retrieving public certificate from the trust store with alias : "
                    + certAlias;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return publicCert;
    }

    /**
     * Verify the JWT token signature.
     * <p>
     * This method only used for API Key revocation which contains some duplicate logic in GatewayUtils class.
     *
     * @param splitToken The JWT token which is split into [header, payload, signature]
     * @return whether the signature is verified or or not
     */
    public static boolean verifyTokenSignature(String[] splitToken, Certificate certificate,
                                               String signatureAlgorithm) throws APIManagementException {
        // Retrieve public key from the certificate
        PublicKey publicKey = certificate.getPublicKey();
        try {
            // Verify token signature
            Signature signatureInstance = Signature.getInstance(signatureAlgorithm);
            signatureInstance.initVerify(publicKey);
            String assertion = splitToken[0] + "." + splitToken[1];
            signatureInstance.update(assertion.getBytes());
            byte[] decodedSignature = java.util.Base64.getUrlDecoder().decode(splitToken[2]);
            return signatureInstance.verify(decodedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IllegalArgumentException e) {
            String msg = "Error while verifying JWT signature with signature algorithm " + signatureAlgorithm;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Retrieve the signature algorithm specified in the token header.
     *
     * @param splitToken The JWT token which is split into [header, payload, signature]
     * @return whether the signature algorithm
     * @throws APIManagementException in case of signature algorithm extraction failure
     */
    public static String getSignatureAlgorithm(String[] splitToken) throws APIManagementException {

        String signatureAlgorithm;
        org.json.JSONObject decodedHeader;
        try {
            decodedHeader = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder().decode(splitToken[0])));
            signatureAlgorithm = decodedHeader.getString(APIConstants.JwtTokenConstants.SIGNATURE_ALGORITHM);
            if (APIConstants.SIGNATURE_ALGORITHM_RS256.equals(signatureAlgorithm)) {
                signatureAlgorithm = APIConstants.SIGNATURE_ALGORITHM_SHA256_WITH_RSA;
            }
            return signatureAlgorithm;
        } catch (JSONException | IllegalArgumentException e) {
            String msg = "Unable to find signature algorithm in the token";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Retrieve the signature algorithm specified in the token header.
     *
     * @param splitToken The JWT token which is split into [header, payload, signature]
     * @return whether the signature algorithm
     * @throws APIManagementException in case of signature algorithm extraction failure
     */
    public static String getSigningAlias(String[] splitToken) throws APIManagementException {

        String signCertAlias;
        org.json.JSONObject decodedHeader;
        try {
            decodedHeader = new org.json.JSONObject(new String(java.util.Base64.getUrlDecoder().decode(splitToken[0])));
            signCertAlias = decodedHeader.getString(APIConstants.JwtTokenConstants.JWT_KID);
            return signCertAlias;
        } catch (JSONException | IllegalArgumentException e) {
            String msg = "Unable to signing certificate alias in the token";
            throw new APIManagementException(msg, e);
        }
    }

    public static String convertOMtoString(OMElement faultSequence) throws XMLStreamException {

        StringWriter stringWriter = new StringWriter();
        faultSequence.serializeAndConsume(stringWriter);
        return stringWriter.toString();
    }

    public static String getFaultSequenceName(API api) throws APIManagementException {

        if (APIUtil.isSequenceDefined(api.getFaultSequence())) {
            String tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            if (api.getId().getProviderName().contains("-AT-")) {
                String provider = api.getId().getProviderName().replace("-AT-", "@");
                tenantDomain = MultitenantUtils.getTenantDomain(provider);
            }
            int tenantId;
            try {
                tenantId = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantManager().getTenantId(tenantDomain);
                if (APIUtil.isPerAPISequence(api.getFaultSequence(), tenantId, api.getId(),
                        APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT)) {
                    return APIUtil.getSequenceExtensionName(api) + APIConstants.API_CUSTOM_SEQ_FAULT_EXT;
                } else {
                    return api.getFaultSequence();
                }
            } catch (UserStoreException e) {
                throw new APIManagementException("Error while retrieving tenant Id from " +
                        api.getId().getProviderName(), e);
            } catch (APIManagementException e) {
                throw new APIManagementException("Error while checking whether sequence " + api.getFaultSequence() +
                        " is a per API sequence.", e);
            }
        }
        return null;

    }

    /**
     * return skipRolesByRegex config
     */
    public static String getSkipRolesByRegex() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String skipRolesByRegex = config.getFirstProperty(APIConstants.SKIP_ROLES_BY_REGEX);
        return skipRolesByRegex;
    }
    
    public static Map<String, Object> getUserProperties(String userNameWithoutChange) throws APIManagementException {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (APIUtil.hasPermission(userNameWithoutChange, APIConstants.Permissions.APIM_ADMIN)) {
            properties.put(APIConstants.USER_CTX_PROPERTY_ISADMIN, true);
        }
        properties.put(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES, APIUtil.getSkipRolesByRegex());

        return properties;
    }

    public static JSONObject getSubscriberAttributes() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                .getSubscriberAttributes();
    }

    public static String getSubscriberRecipient() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.SUBSCRIBER_CONFIGURATION_RECIPIENT);
    }

    public static String getSubscriberDelimeter() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
                getFirstProperty(APIConstants.SUBSCRIBER_CONFIGURATION_DELIMITER);
    }

    /**
     * append the tenant domain to the username when an email is used as the username and EmailUserName is not enabled
     * in the super tenant
     *
     * @param username
     * @param tenantDomain
     * @return username is an email
     */
    public static String appendTenantDomainForEmailUsernames(String username, String tenantDomain) {

        if (APIConstants.SUPER_TENANT_DOMAIN.equalsIgnoreCase(tenantDomain) &&
                !username.endsWith(SUPER_TENANT_SUFFIX) &&
                !MultitenantUtils.isEmailUserName() &&
                username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR) > 0) {
            return username += SUPER_TENANT_SUFFIX;
        }
        return username;
    }

    /**
     * This method returns the categories attached to the API
     *
     * @param artifact API artifact
     * @param tenantID tenant ID of API Provider
     * @return List<APICategory> list of categories
     */
    private static List<APICategory> getAPICategoriesFromAPIGovernanceArtifact(GovernanceArtifact artifact, int tenantID)
            throws GovernanceException, APIManagementException {

        String[] categoriesOfAPI = artifact.getAttributes(APIConstants.API_CATEGORIES_CATEGORY_NAME);

        List<APICategory> categoryList = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(categoriesOfAPI)) {
            //category array retrieved from artifact has only the category name, therefore we need to fetch categories
            //and fill out missing attributes before attaching the list to the api
            String tenantDomain = getTenantDomainFromTenantId(tenantID);
            List<APICategory> allCategories = getAllAPICategoriesOfOrganization(tenantDomain);

            //todo-category: optimize this loop with breaks
            for (String categoryName : categoriesOfAPI) {
                for (APICategory category : allCategories) {
                    if (categoryName.equals(category.getName())) {
                        categoryList.add(category);
                        break;
                    }
                }
            }
        }
        return categoryList;
    }

    /**
     * This method is used to get the categories in a given tenant space
     *
     * @param organization organization name
     * @return categories in a given tenant space
     * @throws APIManagementException if failed to fetch categories
     */
    public static List<APICategory> getAllAPICategoriesOfOrganization(String organization)
            throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getAllCategories(organization);
    }

    /**
     * Validates the API category names to be attached to an API
     *
     * @param categories
     * @param organization
     * @return
     */
    public static boolean validateAPICategories(List<APICategory> categories, String organization)
            throws APIManagementException {

        List<APICategory> availableCategories = getAllAPICategoriesOfOrganization(organization);
        for (APICategory category : categories) {
            if (!availableCategories.contains(category)) {
                return false;
            }
        }
        return true;
    }

    public static String getTenantBasedDevPortalContext(String tenantDomain) throws APIManagementException {

        String context = null;
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            String resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if (mappings.containsKey(APIConstants.API_DOMAIN_MAPPINGS_STORE)) {
                    JSONObject storeMapping = (JSONObject) mappings.get(APIConstants.API_DOMAIN_MAPPINGS_STORE);
                    if (storeMapping.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT)) {
                        context = (String) storeMapping.get(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
                    } else {
                        context = "";
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving gateway domain mappings from registry";
            throw new APIManagementException(msg, e);
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the gateway tenant domain mappings";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the gateway tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return context;
    }

    public static Map getTenantBasedStoreDomainMapping(String tenantDomain) throws APIManagementException {

        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            String resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if (mappings.containsKey(APIConstants.API_DOMAIN_MAPPINGS_STORE)) {
                    return (Map) mappings.get(APIConstants.API_DOMAIN_MAPPINGS_STORE);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving gateway domain mappings from registry";
            throw new APIManagementException(msg, e);
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the gateway tenant domain mappings";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the gateway tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    public static Map getTenantBasedPublisherDomainMapping(String tenantDomain) throws APIManagementException {

        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            String resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if (mappings.containsKey(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER)) {
                    return (Map) mappings.get(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER);
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving publisher domain mappings from registry";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the publisher tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    public static String getTenantBasedPublisherContext(String tenantDomain) throws APIManagementException {

        String context = null;
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            String resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if (mappings.containsKey(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER)) {
                    JSONObject publisherMapping = (JSONObject) mappings.get(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER);
                    if (publisherMapping.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT)) {
                        context = (String) publisherMapping.get(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
                    } else {
                        context = "";
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving publisher domain mappings from registry";
            throw new APIManagementException(msg, e);
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the publisher tenant domain mappings";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the publisher tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return context;
    }

    public static boolean isPerTenantServiceProviderEnabled(String tenantDomain) throws APIManagementException,
            RegistryException {

        JSONObject tenantConfig = getTenantConfig(tenantDomain);
        if (tenantConfig.containsKey(APIConstants.ENABLE_PER_TENANT_SERVICE_PROVIDER_CREATION)) {
            return (boolean) tenantConfig.get(APIConstants.ENABLE_PER_TENANT_SERVICE_PROVIDER_CREATION);
        }
        return false;
    }

    public static String getTenantAdminUserName(String tenantDomain) throws APIManagementException {

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminUserName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
            if (!tenantDomain.contentEquals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                return adminUserName.concat("@").concat(tenantDomain);
            }
            return adminUserName;
        } catch (UserStoreException e) {
            throw new APIManagementException("Error in getting tenant admin username", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static boolean isDefaultApplicationCreationDisabledForTenant(String organization) {

        boolean state = false;
        try {
            JSONObject tenantConfig = getTenantConfig(organization);
            if (tenantConfig.containsKey(APIConstants.DISABLE_DEFAULT_APPLICATION_CREATION)) {
                state = (boolean) tenantConfig.get(APIConstants.DISABLE_DEFAULT_APPLICATION_CREATION);
            }
        } catch (APIManagementException e) {
            log.error("Error while reading tenant-config.json for tenant " + organization, e);
            state = false;
        }
        return state;
    }

    /**
     * Validate Certificate exist in TrustStore
     *
     * @param certificate
     * @return true if certificate exist in truststore
     * @throws APIManagementException
     */
    public static boolean isCertificateExistsInListenerTrustStore(Certificate certificate) throws APIManagementException {

        if (certificate != null) {
            try {
                KeyStore trustStore = ServiceReferenceHolder.getInstance().getListenerTrustStore();
                if (trustStore != null) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    byte[] certificateEncoded = certificate.getEncoded();
                    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certificateEncoded)) {
                        java.security.cert.X509Certificate x509Certificate =
                                (java.security.cert.X509Certificate) cf.generateCertificate(byteArrayInputStream);
                        String certificateAlias = trustStore.getCertificateAlias(x509Certificate);
                        if (certificateAlias != null) {
                            return true;
                        }
                    }
                }
            } catch (KeyStoreException | CertificateException | IOException e) {
                String msg = "Error in validating certificate existence";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return false;
    }

    public static boolean isDevPortalAnonymous() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String anonymousMode = config.getFirstProperty(APIConstants.API_DEVPORTAL_ANONYMOUS_MODE);
        if (anonymousMode == null) {
            return true;
        }
        return Boolean.parseBoolean(anonymousMode);
    }

    public static boolean isTenantDevportalAnonymous(String tenantDomain) throws APIManagementException {

        JSONObject tenantConfig = getTenantConfig(tenantDomain);
        if (tenantConfig.containsKey(APIConstants.ENABLE_ANONYMOUS_MODE)) {
            Boolean anonymousEnabled = (boolean) tenantConfig.get(APIConstants.ENABLE_ANONYMOUS_MODE);
            return anonymousEnabled;
        } else {
            return true;
        }
    }

    public static Map<String, EndpointSecurity> setEndpointSecurityForAPIProduct(API api) throws APIManagementException {

        Map<String, EndpointSecurity> endpointSecurityMap = new HashMap<>();
        try {
            endpointSecurityMap.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, new EndpointSecurity());
            endpointSecurityMap.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, new EndpointSecurity());
            if (api.isEndpointSecured() && !api.isAdvertiseOnly()) {
                EndpointSecurity productionEndpointSecurity = new EndpointSecurity();
                productionEndpointSecurity.setEnabled(true);
                productionEndpointSecurity.setUsername(api.getEndpointUTUsername());
                productionEndpointSecurity.setPassword(api.getEndpointUTPassword());
                if (api.isEndpointAuthDigest()) {
                    productionEndpointSecurity.setType(APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST.toUpperCase());
                } else {
                    productionEndpointSecurity.setType(APIConstants.ENDPOINT_SECURITY_TYPE_BASIC.toUpperCase());
                }
                endpointSecurityMap.replace(APIConstants.ENDPOINT_SECURITY_PRODUCTION, productionEndpointSecurity);
                endpointSecurityMap.replace(APIConstants.ENDPOINT_SECURITY_SANDBOX, productionEndpointSecurity);
            } else if (!api.isAdvertiseOnly()) {
                String endpointConfig = api.getEndpointConfig();
                if (endpointConfig != null) {
                    JSONObject endpointConfigJson = (JSONObject) new JSONParser().parse(endpointConfig);
                    if (endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null) {
                        JSONObject endpointSecurity =
                                (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                        if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                            JSONObject productionEndpointSecurity =
                                    (JSONObject) endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION);
                            endpointSecurityMap.replace(APIConstants.ENDPOINT_SECURITY_PRODUCTION, new ObjectMapper()
                                    .convertValue(productionEndpointSecurity, EndpointSecurity.class));
                        }
                        if (endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                            JSONObject sandboxEndpointSecurity =
                                    (JSONObject) endpointSecurity.get(APIConstants.ENDPOINT_SECURITY_SANDBOX);
                            endpointSecurityMap.replace(APIConstants.ENDPOINT_SECURITY_SANDBOX, new ObjectMapper()
                                    .convertValue(sandboxEndpointSecurity, EndpointSecurity.class));
                        }
                    }
                }
            }
            return endpointSecurityMap;
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing Endpoint Config json", e);
        }
    }

    /**
     * To check whether the API recommendation is enabled. It can be either enabled globally or tenant vice.
     *
     * @param tenantDomain Tenant domain
     * @return whether recommendation is enabled or not
     */
    public static boolean isRecommendationEnabled(String tenantDomain) {

        RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiRecommendationEnvironment();
        if (recommendationEnvironment != null) {
            if (recommendationEnvironment.isApplyForAllTenants()) {
                return true;
            } else {
                try {
                    org.json.simple.JSONObject tenantConfig = getTenantConfig(tenantDomain);
                    Object value = tenantConfig.get(APIConstants.API_TENANT_CONF_ENABLE_RECOMMENDATION_KEY);
                    return Boolean.parseBoolean(value.toString());
                } catch (APIManagementException e) {
                    log.debug("Error while retrieving Recommendation config from registry", e);
                }
            }
        }
        return false;
    }

    public static void addNewEventPublisher(EventPublisherType type) {
        try {
            eventPublishers.putIfAbsent(type, eventPublisherFactory.getEventPublisher(type));
        } catch (EventPublisherException e) {
            log.error("Could not add the event publisher." + type + " Events might not be published properly.");
        }
    }

    public static void publishEvent(EventPublisherType type, EventPublisherEvent event, String eventString) {
        try {
            if (eventPublishers.get(type) != null) {
                eventPublishers.get(type).publish(event);
            } else {
                log.error("Error occurred while trying to retrieve the event publisher for type: " + type);
            }
        } catch (EventPublisherException e) {
            log.error("Error occurred while trying to publish event.\n" + eventString, e);
        }
    }

    public static void publishEvent(String eventName, Map dynamicProperties, Event event) {

        boolean tenantFlowStarted = false;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            tenantFlowStarted = true;
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                    .publish(eventName, dynamicProperties, event);
        } finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

    }

    /**
     * Returns the user claims for the given user.
     *
     * @param endUserName name of the user whose claims needs to be returned
     * @param tenantId    tenant id of the user
     * @param dialectURI  claim dialect URI
     * @return claims map
     * @throws APIManagementException
     */
    public static SortedMap<String, String> getClaims(String endUserName, int tenantId, String dialectURI)
            throws APIManagementException {

        SortedMap<String, String> claimValues;
        try {
            ClaimManager claimManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getClaimManager();
            ClaimMapping[] claims = claimManager.getAllClaimMappings(dialectURI);
            String[] claimURIs = claimMappingtoClaimURIString(claims);
            UserStoreManager userStoreManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager();
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(endUserName);
            claimValues = new TreeMap(userStoreManager.getUserClaimValues(tenantAwareUserName, claimURIs, null));
            return claimValues;
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while retrieving user claim values from user store", e);
        }
    }

    /**
     * Returns the display name of the given claim URI.
     *
     * @param claimURI
     * @param subscriber
     * @return display name of the claim
     * @throws APIManagementException
     */
    public static String getClaimDisplayName(String claimURI, String subscriber) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(subscriber);
        int tenantId;
        String displayName;
        try {
            tenantId = getTenantId(tenantDomain);
            ClaimManager claimManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getClaimManager();
            displayName = claimManager.getClaim(claimURI).getDisplayTag();
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while retrieving claim values from user store", e);
        }
        return displayName;
    }

    /**
     * Helper method to convert array of <code>Claim</code> object to
     * array of <code>String</code> objects corresponding to the ClaimURI values.
     *
     * @param claims claims object
     * @return String array of claims
     */
    private static String[] claimMappingtoClaimURIString(ClaimMapping[] claims) {

        String[] temp = new String[claims.length];
        for (int i = 0; i < claims.length; i++) {
            temp[i] = claims[i].getClaim().getClaimUri();

        }
        return temp;
    }

    public static KeyManagerConfigurationDTO getAndSetDefaultKeyManagerConfiguration(
            KeyManagerConfigurationDTO keyManagerConfigurationDTO) throws APIManagementException {

        boolean clientSecretHashEnabled =
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().isClientSecretHashEnabled();
        Set<String> availableGrantTypes =
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getSupportedGrantTypes().keySet();
        long validityPeriod = ServiceReferenceHolder.getInstance().getOauthServerConfiguration()
                .getApplicationAccessTokenValidityPeriodInSeconds();
        APIManagerConfigurationService config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
        String issuerIdentifier = OAuthServerConfiguration.getInstance().getOpenIDConnectIDTokenIssuerIdentifier();
        if (config != null) {
            OpenIdConnectConfiguration openIdConnectConfigurations = null;
            APIManagerConfiguration apiManagerConfiguration = config.getAPIManagerConfiguration();
            String keyManagerUrl;
            String enableTokenEncryption =
                    apiManagerConfiguration.getFirstProperty(APIConstants.ENCRYPT_TOKENS_ON_PERSISTENCE);
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.AUTHSERVER_URL)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.AUTHSERVER_URL,
                        apiManagerConfiguration.getFirstProperty(APIConstants.KEYMANAGER_SERVERURL));
            }
            keyManagerUrl =
                    (String) keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.AUTHSERVER_URL);
            if (StringUtils.isNotEmpty(keyManagerUrl)){
                openIdConnectConfigurations = APIUtil.getOpenIdConnectConfigurations(
                        keyManagerUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0]
                                .concat(getTenantAwareContext(keyManagerConfigurationDTO.getOrganization()))
                                .concat(APIConstants.KeyManager.DEFAULT_KEY_MANAGER_OPENID_CONNECT_DISCOVERY_ENDPOINT));

            }
            if (keyManagerConfigurationDTO.getProperty(APIConstants.KeyManager.ENABLE_TOKEN_ENCRYPTION) == null) {
                keyManagerConfigurationDTO.addProperty(APIConstants.ENCRYPT_TOKENS_ON_PERSISTENCE,
                        Boolean.parseBoolean(enableTokenEncryption));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.REVOKE_URL)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.REVOKE_URL,
                        keyManagerUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0]
                                .concat(APIConstants.IDENTITY_REVOKE_ENDPOINT));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(APIConstants.TOKEN_URL)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.TOKEN_URL,
                        keyManagerUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0]
                                .concat(APIConstants.IDENTITY_TOKEN_ENDPOINT_CONTEXT));
            }

            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.AVAILABLE_GRANT_TYPE,
                        new ArrayList<>(availableGrantTypes));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.ENABLE_TOKEN_HASH)) {
                keyManagerConfigurationDTO
                        .addProperty(APIConstants.KeyManager.ENABLE_TOKEN_HASH, clientSecretHashEnabled);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.ENABLE_OAUTH_APP_CREATION, true);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.ENABLE_MAP_OAUTH_CONSUMER_APPS)) {
                keyManagerConfigurationDTO
                        .addProperty(APIConstants.KeyManager.ENABLE_MAP_OAUTH_CONSUMER_APPS,
                                isMapExistingAuthAppsEnabled());
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.ENABLE_TOKEN_GENERATION, true);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.TOKEN_ENDPOINT)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.TOKEN_ENDPOINT,
                        keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.TOKEN_URL));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.REVOKE_ENDPOINT)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.REVOKE_ENDPOINT,
                        keyManagerConfigurationDTO.getAdditionalProperties().get(APIConstants.REVOKE_URL));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(
                    APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD,
                        String.valueOf(validityPeriod));
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(
                    APIConstants.KeyManager.ENABLE_TOKEN_VALIDATION)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.ENABLE_TOKEN_VALIDATION, true);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(
                    APIConstants.KeyManager.SELF_VALIDATE_JWT)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.SELF_VALIDATE_JWT, true);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(
                    APIConstants.KeyManager.ISSUER)) {
                if (openIdConnectConfigurations == null) {
                    throw new APIMgtInternalException("Error in fetching Open ID configuration.");
                }
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.ISSUER,
                        openIdConnectConfigurations.getIssuer());
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties().containsKey(
                    APIConstants.KeyManager.CLAIM_MAPPING)) {
                keyManagerConfigurationDTO
                        .addProperty(APIConstants.KeyManager.CLAIM_MAPPING, getDefaultClaimMappings());
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.CERTIFICATE_TYPE)) {
                keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_TYPE,
                        APIConstants.KeyManager.CERTIFICATE_TYPE_JWKS_ENDPOINT);
            }
            if (!keyManagerConfigurationDTO.getAdditionalProperties()
                    .containsKey(APIConstants.KeyManager.CERTIFICATE_VALUE)) {
                if (openIdConnectConfigurations != null) {
                    keyManagerConfigurationDTO
                            .addProperty(APIConstants.KeyManager.CERTIFICATE_VALUE,
                                    openIdConnectConfigurations.getJwksEndpoint());
                } else {
                    keyManagerConfigurationDTO.addProperty(APIConstants.KeyManager.CERTIFICATE_VALUE,
                            keyManagerUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0]
                                    .concat(getTenantAwareContext(keyManagerConfigurationDTO.getOrganization()))
                                    .concat(APIConstants.KeyManager.DEFAULT_JWKS_ENDPOINT));
                }
            }
            String defaultKeyManagerType =
                    apiManagerConfiguration.getFirstProperty(APIConstants.DEFAULT_KEY_MANAGER_TYPE);
            if (StringUtils.isNotEmpty(defaultKeyManagerType)) {
                keyManagerConfigurationDTO.setType(defaultKeyManagerType);
            }
        }
        return keyManagerConfigurationDTO;
    }

    public static String getTokenEndpointsByType(String type, String organization) throws APIManagementException {

        Map<String, Environment> environments = getEnvironments(organization);
        Map<String, String> map = new HashMap<>();

        String productionUrl = "";
        String sandboxUrl = "";
        String hybridUrl = "";

        // Set URL for a given default env
        for (Environment environment : environments.values()) {
            String environmentUrl = getHttpsEnvironmentUrl(environment);
            String environmentType = environment.getType();
            boolean isDefault = environment.isDefault();

            if (APIConstants.GATEWAY_ENV_TYPE_HYBRID.equals(environmentType)) {
                if (isDefault) {
                    map.put(APIConstants.GATEWAY_ENV_TYPE_HYBRID, environmentUrl);
                } else {
                    hybridUrl = environmentUrl;
                }
            } else if (APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environmentType)) {
                if (isDefault) {
                    map.put(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, environmentUrl);
                } else {
                    productionUrl = environmentUrl;
                }
            } else if (APIConstants.GATEWAY_ENV_TYPE_SANDBOX.equals(environmentType)) {
                if (isDefault) {
                    map.put(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, environmentUrl);
                } else {
                    sandboxUrl = environmentUrl;
                }
            } else {
                log.warn("Invalid gateway environment type : " + environmentType +
                        " has been configured in api-manager.xml");
            }
        }
        // If no default envs are specified, set URL from each of the configured env types at random
        if (map.isEmpty()) {
            if (StringUtils.isNotEmpty(productionUrl)) {
                map.put(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION, productionUrl);
            }
            if (StringUtils.isNotEmpty(sandboxUrl)) {
                map.put(APIConstants.GATEWAY_ENV_TYPE_SANDBOX, sandboxUrl);
            }
            if (StringUtils.isNotEmpty(hybridUrl)) {
                map.put(APIConstants.GATEWAY_ENV_TYPE_HYBRID, hybridUrl);
            }
        }
        if (map.containsKey(APIConstants.GATEWAY_ENV_TYPE_HYBRID)) {
            return map.get(APIConstants.GATEWAY_ENV_TYPE_HYBRID);
        }
        if (map.containsKey(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION)) {
            return map.get(APIConstants.GATEWAY_ENV_TYPE_PRODUCTION);
        }
        if (map.containsKey(APIConstants.GATEWAY_ENV_TYPE_SANDBOX)) {
            return map.get(APIConstants.GATEWAY_ENV_TYPE_SANDBOX);
        }
        return map.get(type);
    }

    private static String getHttpsEnvironmentUrl(Environment environment) {

        for (String url : environment.getApiGatewayEndpoint().split(",")) {
            if (url.startsWith(APIConstants.HTTPS_PROTOCOL)) {
                return url;
            }
        }
        return "";
    }

    public static Map<String, KeyManagerConnectorConfiguration> getKeyManagerConfigurations() {

        return ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfigurations();
    }

    /**
     * Get scopes attached to the API.
     *
     * @param id   API uuid
     * @param organization Organization
     * @return Scope key to Scope object mapping
     * @throws APIManagementException if an error occurs while getting scope attached to API
     */
    public static Map<String, Scope> getAPIScopes(String id, String organization)
            throws APIManagementException {
        String currentApiUuid;
        APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(id);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = id;
        }
        Set<String> scopeKeys = ApiMgtDAO.getInstance().getAPIScopeKeys(currentApiUuid);
        return getScopes(scopeKeys, organization);
    }

    /**
     * Get scopes for the given scope keys from authorization server.
     *
     * @param scopeKeys    Scope Keys
     * @param organization organization
     * @return Scope key to Scope object mapping
     * @throws APIManagementException if an error occurs while getting scopes using scope keys
     */
    public static Map<String, Scope> getScopes(Set<String> scopeKeys, String organization)
            throws APIManagementException {

        Map<String, Scope> scopeToKeyMap = new HashMap<>();
        for (String scopeKey : scopeKeys) {
            Scope scope = getScopeByName(scopeKey, organization);
            scopeToKeyMap.put(scopeKey, scope);
        }
        return scopeToKeyMap;
    }

    public static Scope getScopeByName(String scopeKey, String organization) throws APIManagementException {

        int tenantId = APIUtil.getInternalIdFromTenantDomainOrOrganization(organization);
        return ScopesDAO.getInstance().getScope(scopeKey, tenantId);
    }

    public static KeyManagerConnectorConfiguration getKeyManagerConnectorConfigurationsByConnectorType(String type) {

        return ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfiguration(type);
    }

    public static List<ClaimMappingDto> getDefaultClaimMappings() {

        List<ClaimMappingDto> claimMappingDtoList = new ArrayList<>();
        try (InputStream resourceAsStream = APIUtil.class.getClassLoader()
                .getResourceAsStream("claimMappings/default-claim-mapping.json")) {
            String content = IOUtils.toString(resourceAsStream);
            Map<String, String> claimMapping = new Gson().fromJson(content, Map.class);
            claimMapping.forEach((remoteClaim, localClaim) -> {
                claimMappingDtoList.add(new ClaimMappingDto(remoteClaim, localClaim));
            });
        } catch (IOException e) {
            log.error("Error while reading default-claim-mapping.json", e);
        }
        return claimMappingDtoList;
    }


    public static String getX509certificateContent(String certificate) {
        String content = certificate.replaceAll(APIConstants.BEGIN_CERTIFICATE_STRING, "")
                .replaceAll(APIConstants.END_CERTIFICATE_STRING, "");

        return content.trim();
    }

    /**
     * Util method to convert Base64 URL encoded certificate content to X509Certificate instance.
     *
     * @param base64EncodedCertificate Base64 URL encoded cert string
     * @return javax.security.cert.X509Certificate
     * @throws APIManagementException if an error occurs while retrieving from IDP
     */
    public static X509Certificate retrieveCertificateFromURLEncodedContent(String base64EncodedCertificate)
            throws APIManagementException {

        if (base64EncodedCertificate != null) {
            try {
                base64EncodedCertificate = URLDecoder.decode(base64EncodedCertificate, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                String msg = "Error while URL decoding certificate";
                throw new APIManagementException(msg, e);
            }

            base64EncodedCertificate = APIUtil.getX509certificateContent(base64EncodedCertificate);
            byte[] bytes = Base64.decodeBase64(base64EncodedCertificate);
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                return X509Certificate.getInstance(inputStream);
            } catch (IOException | javax.security.cert.CertificateException e) {
                String msg = "Error while converting into X509Certificate";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return null;
    }

    /**
     * Util method to convert non URL encoded but base64 encoded certificate content to X509Certificate instance.
     *
     * @param base64EncodedCertificate Base64 encoded cert string (not URL encoded)
     * @return javax.security.cert.X509Certificate
     * @throws APIManagementException if an error occurs while retrieving from IDP
     */
    public static X509Certificate retrieveCertificateFromContent(String base64EncodedCertificate)
            throws APIManagementException {

        if (base64EncodedCertificate != null) {
            base64EncodedCertificate = APIUtil.getX509certificateContent(base64EncodedCertificate);
            byte[] bytes = Base64.decodeBase64(base64EncodedCertificate.getBytes());
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                return X509Certificate.getInstance(inputStream);
            } catch (IOException | javax.security.cert.CertificateException e) {
                String msg = "Error while converting into X509Certificate";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return null;
    }

    /**
     * Replace new RESTAPI Role mappings to tenant-conf.
     *
     * @param newScopeRoleJson New object of role-scope mapping
     * @throws APIManagementException If failed to replace the new tenant-conf.
     */
    public static void updateTenantConfOfRoleScopeMapping(JSONObject newScopeRoleJson, String username)
            throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        //read from tenant-conf.json
        JSONObject tenantConfig = getTenantConfig(tenantDomain);
        JsonObject existingTenantConfObject = (JsonObject) new JsonParser().parse(tenantConfig.toJSONString());
        JsonElement existingTenantConfScopes = existingTenantConfObject.get(APIConstants.REST_API_SCOPES_CONFIG);
        JsonElement newTenantConfScopes = new JsonParser().parse(newScopeRoleJson.toJSONString());
        JsonObject mergedTenantConfScopes = mergeTenantConfScopes(existingTenantConfScopes, newTenantConfScopes);

        // Removing the old RESTAPIScopes config from the existing tenant-conf
        existingTenantConfObject.remove(APIConstants.REST_API_SCOPES_CONFIG);
        // Adding the merged RESTAPIScopes config to the tenant-conf
        existingTenantConfObject.add(APIConstants.REST_API_SCOPES_CONFIG, mergedTenantConfScopes);

        // Prettify the tenant-conf
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedTenantConf = gson.toJson(existingTenantConfObject);
        ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(tenantDomain,
                formattedTenantConf);

        if (log.isDebugEnabled()) {
            log.debug("Finalized tenant-conf.json: " + formattedTenantConf);
        }
    }

    /**
     * Merge the existing and new scope-role mappings (RESTAPIScopes config) in the tenant-conf
     *
     * @param existingTenantConfScopes Existing (old) scope-role mappings
     * @param newTenantConfScopes      Modified (new) scope-role mappings
     * @return JsonObject with merged tenant-sconf scope mappings
     */
    public static JsonObject mergeTenantConfScopes(JsonElement existingTenantConfScopes, JsonElement newTenantConfScopes) {
        JsonArray existingTenantConfScopesArray = (JsonArray) existingTenantConfScopes.getAsJsonObject().
                get(APIConstants.REST_API_SCOPE);
        JsonArray newTenantConfScopesArray = (JsonArray) newTenantConfScopes.getAsJsonObject().
                get(APIConstants.REST_API_SCOPE);
        JsonArray mergedTenantConfScopesArray = new JsonParser().parse(newTenantConfScopesArray.toString()).
                getAsJsonArray();

        // Iterating the existing (old) scope-role mappings
        for (JsonElement existingScopeRoleMapping : existingTenantConfScopesArray) {
            String existingScopeName = existingScopeRoleMapping.getAsJsonObject().get(APIConstants.REST_API_SCOPE_NAME).
                    getAsString();
            Boolean scopeRoleMappingExists = false;
            // Iterating the modified (new) scope-role mappings and add the old scope mappings
            // if those are not present in the list (merging)
            for (JsonElement newScopeRoleMapping : newTenantConfScopesArray) {
                String newScopeName = newScopeRoleMapping.getAsJsonObject().get(APIConstants.REST_API_SCOPE_NAME).
                        getAsString();
                if (StringUtils.equals(existingScopeName, newScopeName)) {
                    // If a particular mapping is already there, skip it
                    scopeRoleMappingExists = true;
                    break;
                }
            }
            // If the particular old mapping does not exist in the new list, add it to the new list
            if (!scopeRoleMappingExists) {
                mergedTenantConfScopesArray.add(existingScopeRoleMapping);
            }
        }
        JsonObject mergedTenantConfScopes = new JsonObject();
        mergedTenantConfScopes.add(APIConstants.REST_API_SCOPE, mergedTenantConfScopesArray);
        return mergedTenantConfScopes;
    }

    /**
     * Replace new RoleMappings  to tenant-conf.
     *
     * @param newRoleMappingJson New object of role-alias mapping
     * @throws APIManagementException If failed to replace the new tenant-conf.
     */
    public static void updateTenantConfRoleAliasMapping(JSONObject newRoleMappingJson, String username)
            throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        //read from tenant-conf.json
        JsonObject existingTenantConfObject = new JsonObject();
        String existingTenantConf =
                ServiceReferenceHolder.getInstance().getApimConfigService().getTenantConfig(tenantDomain);
        existingTenantConfObject = new JsonParser().parse(existingTenantConf).getAsJsonObject();

        //append original role to the role mapping list
        Set<Map.Entry<String, JsonElement>> roleMappingEntries = newRoleMappingJson.entrySet();
        for (Map.Entry<String, JsonElement> entry: roleMappingEntries) {
            List<String> currentRoles = Arrays.asList(String.valueOf(entry.getValue()).split(","));
            boolean isOriginalRoleAlreadyInRoles = false;
            for (String role: currentRoles) {
                if (role.equals(entry.getKey())) {
                    isOriginalRoleAlreadyInRoles = true;
                    break;
                }
            }
            if (!isOriginalRoleAlreadyInRoles) {
                String newRoles = entry.getKey() + "," + entry.getValue();
                newRoleMappingJson.replace(entry.getKey(), entry.getValue(), newRoles);
            }
        }
        existingTenantConfObject.remove(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
        JsonElement jsonElement = new JsonParser().parse(String.valueOf(newRoleMappingJson));
        existingTenantConfObject.add(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG, jsonElement);

        // Prettify the tenant-conf
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedTenantConf = gson.toJson(existingTenantConfObject);
        ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(tenantDomain,
                formattedTenantConf);
        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
        String cacheName = tenantDomain + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
        tenantConfigCache.remove(cacheName);
        if (log.isDebugEnabled()) {
            log.debug("Finalized tenant-conf.json: " + formattedTenantConf);
        }
    }

    public static OpenIdConnectConfiguration getOpenIdConnectConfigurations(String url) throws APIManagementException {

        OpenIDConnectDiscoveryClient openIDConnectDiscoveryClient = Feign.builder()
                .client(new ApacheFeignHttpClient(APIUtil.getHttpClient(url))).encoder(new GsonEncoder())
                .decoder(new GsonDecoder()).target(OpenIDConnectDiscoveryClient.class, url);
        return openIDConnectDiscoveryClient.getOpenIdConnectConfiguration();
    }

    private static String getTenantAwareContext(String tenantDomain) throws APIManagementException {
        int tenantId = APIUtil.getInternalOrganizationId(tenantDomain);
        if (MultitenantConstants.SUPER_TENANT_ID != tenantId) {
            return "/t/".concat(tenantDomain);
        }
        return "";
    }

    /**
     * Copy of the getAPI(GovernanceArtifact artifact, Registry registry) method with reduced DB calls for api
     * publisher list view listing.
     * @param artifact
     * @param registry
     * @return
     * @throws APIManagementException
     */
    public static API getReducedPublisherAPIForListing(GovernanceArtifact artifact, Registry registry)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set uuid
            api.setUUID(artifact.getId());

            // set url
            api.setStatus(getLcStateFromArtifact(artifact));
            api.setType(artifact.getAttribute(APIConstants.API_OVERVIEW_TYPE));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointAuthDigest(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_ENDPOINT_AUTH_DIGEST)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            if (!((APIConstants.DEFAULT_MODIFIED_ENDPOINT_PASSWORD)
                    .equals(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD)))) {
                api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            } else { //If APIEndpointPasswordRegistryHandler is enabled take password from the registry hidden property
                api.setEndpointUTPassword(getActualEpPswdFromHiddenProperty(api, registry));
            }
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            api.setProductionMaxTps(artifact.getAttribute(APIConstants.API_PRODUCTION_THROTTLE_MAXTPS));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch (NumberFormatException e) {
                //ignore
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
            api.setEnableSchemaValidation(Boolean.parseBoolean(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_JSON_SCHEMA)));
            api.setEnableStore(Boolean.parseBoolean(
                    artifact.getAttribute(APIConstants.API_OVERVIEW_ENABLE_STORE)));
            api.setAsDefaultVersion(Boolean.parseBoolean(artifact.getAttribute(
                    APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));

            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));

            api.setAuthorizationHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_AUTHORIZATION_HEADER));
            api.setApiKeyHeader(artifact.getAttribute(APIConstants.API_OVERVIEW_API_KEY_HEADER));
            api.setApiSecurity(artifact.getAttribute(APIConstants.API_OVERVIEW_API_SECURITY));

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    public static boolean isCrossTenantSubscriptionsEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String crossTenantSubscriptionProperty =
                apiManagerConfiguration.getFirstProperty(APIConstants.API_DEVPORTAL_ENABLE_CROSS_TENANT_SUBSCRIPTION);
        if (StringUtils.isNotEmpty(crossTenantSubscriptionProperty)) {
            return Boolean.parseBoolean(crossTenantSubscriptionProperty);
        }
        return false;
    }

    public static String retrieveDefaultReservedUsername() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String defaultReservedUsername =
                apiManagerConfiguration.getFirstProperty(APIConstants.API_DEVPORTAL_DEFAULT_RESERVED_USERNAME);
        return defaultReservedUsername;
    }

    public static JSONArray getCustomProperties(String userId) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);

        JSONArray customPropertyAttributes = null;
        JSONObject propertyConfig = getMandatoryPropertyKeysFromRegistry(tenantDomain);
        if (propertyConfig != null) {
            customPropertyAttributes = (JSONArray) propertyConfig.get(APIConstants.CustomPropertyAttributes.PROPERTIES);
        } else {
            APIManagerConfiguration apiManagerConfiguration =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
            customPropertyAttributes = apiManagerConfiguration.getCustomProperties();
        }
        return customPropertyAttributes;
    }

    public static JSONObject getMandatoryPropertyKeysFromRegistry(String organization) throws APIManagementException {

        JSONObject tenantConfigs = getTenantConfig(organization);
        String property = APIConstants.CustomPropertyAttributes.PROPERTY_CONFIGURATIONS;
        if (tenantConfigs.keySet().contains(property)) {
            return (JSONObject) tenantConfigs.get(
                    APIConstants.CustomPropertyAttributes.PROPERTY_CONFIGURATIONS);
        }
        return null;
    }

    public static boolean isDefaultApplicationCreationEnabled() {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String createDefaultApp = apiManagerConfiguration
                .getFirstProperty(APIConstants.API_STORE_CREATE_DEFAULT_APPLICATION);
        if (StringUtils.isNotEmpty(createDefaultApp)) {
            return Boolean.parseBoolean(createDefaultApp);
        }
        return true;
    }

    /**
     * Get UUID by the API Identifier.
     *
     * @param identifier
     * @param organization identifier of the organization
     * @return String uuid string
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static String getUUIDFromIdentifier(APIIdentifier identifier, String organization)
            throws APIManagementException{
        if (organization != null) {
            return ApiMgtDAO.getInstance().getUUIDFromIdentifier(identifier, organization);
        } else {
            return ApiMgtDAO.getInstance().getUUIDFromIdentifier(identifier);
        }
    }

    /**
     * Get UUID by the API Identifier.
     *
     * @param identifier
     * @param organization
     * @return String uuid string
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static String getUUIDFromIdentifier(APIProductIdentifier identifier, String organization)
            throws APIManagementException {
        return ApiMgtDAO.getInstance().getUUIDFromIdentifier(identifier, organization);
    }

    /**
     * Get the API Product Identifier from UUID.
     *
     * @param uuid UUID of the API
     * @return API Product Identifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static APIProductIdentifier getAPIProductIdentifierFromUUID(String uuid) throws APIManagementException{
        return ApiMgtDAO.getInstance().getAPIProductIdentifierFromUUID(uuid);
    }

    /**
     * Get the API Identifier from UUID.
     *
     * @param uuid UUID of the API
     * @return API Identifier
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String uuid) throws APIManagementException{
        return ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(uuid);
    }
    
    public static String getconvertedId(Identifier apiId) {
        String id = null;
        if (apiId instanceof APIIdentifier) {
            id = APIType.API + ":" + apiId.getProviderName() + ":" + apiId.getName() + ":" + apiId.getVersion();
        } else if (apiId instanceof APIProductIdentifier) {
            id = APIType.API_PRODUCT + ":" + apiId.getProviderName() + ":" + apiId.getName() + ":" + apiId.getVersion();
        }
        return id;
    }

    public static String[] getFilteredUserRoles(String username) throws APIManagementException {
        String[] userRoles = APIUtil.getListOfRoles(username);
        String skipRolesByRegex = APIUtil.getSkipRolesByRegex();
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while (itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        return userRoles;
    }

    public static boolean isStreamingApi(API api) {
        return APIConstants.APITransportType.WS.toString().equalsIgnoreCase(api.getType()) ||
                APIConstants.APITransportType.SSE.toString().equalsIgnoreCase(api.getType()) ||
                APIConstants.APITransportType.WEBSUB.toString().equalsIgnoreCase(api.getType()) ||
                APIConstants.APITransportType.ASYNC.toString().equalsIgnoreCase(api.getType());
    }

    public static boolean isStreamingApi(APIProduct apiProduct) {
        return APIConstants.APITransportType.WS.toString().equalsIgnoreCase(apiProduct.getType()) ||
                APIConstants.APITransportType.SSE.toString().equalsIgnoreCase(apiProduct.getType()) ||
                APIConstants.APITransportType.WEBSUB.toString().equalsIgnoreCase(apiProduct.getType()) ||
                APIConstants.APITransportType.ASYNC.toString().equalsIgnoreCase(apiProduct.getType());
    }

    /**
     * Validate sandbox and production endpoint URLs.
     *
     * @param endpoints sandbox and production endpoint URLs inclusive list
     * @return validity of given URLs
     */
    public static boolean validateEndpointURLs(ArrayList<String> endpoints) {
        long validatorOptions =
                UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS;
        RegexValidator authorityValidator = new RegexValidator(".*");
        UrlValidator urlValidator = new UrlValidator(authorityValidator, validatorOptions);

        for (String endpoint : endpoints) {
            // If url is a JMS connection url or a Consul service discovery related url, or a parameterized URL
            // validation is skipped. If not, validity is checked.
            if (!endpoint.startsWith("jms:") && !endpoint.startsWith("consul(") && !endpoint.contains("{") &&
                    !endpoint.contains("}") && !urlValidator.isValid(endpoint)) {
                try {
                    // If the url is not identified as valid from the above check,
                    // next step is determine the validity of the encoded url (done through the URI constructor).
                    URL endpointUrl = new URL(endpoint);
                    URI endpointUri = new URI(endpointUrl.getProtocol(), endpointUrl.getAuthority(),
                            endpointUrl.getPath(), endpointUrl.getQuery(), null);

                    if (!urlValidator.isValid(endpointUri.toString())) {
                        log.error("Invalid endpoint url " + endpointUrl);
                        return false;
                    }
                } catch (URISyntaxException | MalformedURLException e) {
                    log.error("Error while parsing the endpoint url " + endpoint);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check whether the file type is supported.
     * @param filename name
     * @return true if supported
     */
    public static boolean isSupportedFileType(String filename) {
        if (log.isDebugEnabled()) {
            log.debug("File name " + filename);
        }
        if (StringUtils.isEmpty(filename)) {
            return false;
        }
        String fileType = FilenameUtils.getExtension(filename);
        List<String> list = null;
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String supportedTypes = apiManagerConfiguration
                .getFirstProperty(APIConstants.API_PUBLISHER_SUPPORTED_DOC_TYPES);
        if (!StringUtils.isEmpty(supportedTypes)) {
            String[] definedTypesArr = supportedTypes.trim().split("\\s*,\\s*");
            list = Arrays.asList(definedTypesArr);
        } else {
            String[] defaultType = { "pdf", "txt", "doc", "docx", "xls", "xlsx", "odt", "ods", "json", "yaml", "md" };
            list = Arrays.asList(defaultType);
        }
        return list.contains(fileType.toLowerCase());
    }
    public static void validateRestAPIScopes(String tenantConfig) throws APIManagementException {
        JsonObject fileBaseTenantConfig = (JsonObject) getFileBaseTenantConfig();
        Set<String> fileBaseScopes = getRestAPIScopes(fileBaseTenantConfig);
        Set<String> uploadedTenantConfigScopes = getRestAPIScopes((JsonObject) new JsonParser().parse(tenantConfig));
        fileBaseScopes.removeAll(uploadedTenantConfigScopes);
        if (fileBaseScopes.size() > 0) {
            throw new APIManagementException("Insufficient scopes available in tenant-config", ExceptionCodes.INVALID_TENANT_CONFIG);
        }
    }

    private static Set<String> getRestAPIScopes(JsonObject tenantConfig) {

        Set<String> scopes = new HashSet<>();
        if (tenantConfig.has(APIConstants.REST_API_SCOPES_CONFIG)) {
            JsonObject restApiScopes = (JsonObject) tenantConfig.get(APIConstants.REST_API_SCOPES_CONFIG);
            if (restApiScopes.has(APIConstants.REST_API_SCOPE)
                    && restApiScopes.get(APIConstants.REST_API_SCOPE) instanceof JsonArray) {
                JsonArray restAPIScopes = (JsonArray) restApiScopes.get(APIConstants.REST_API_SCOPE);
                if (restAPIScopes != null) {
                    for (JsonElement scopeElement : restAPIScopes) {
                        if (scopeElement instanceof JsonObject) {
                            if (((JsonObject) scopeElement).has(APIConstants.REST_API_SCOPE_NAME)
                                    && ((JsonObject) scopeElement).get(APIConstants.REST_API_SCOPE_NAME)
                                    instanceof JsonPrimitive) {
                                JsonElement name = ((JsonObject) scopeElement).get(APIConstants.REST_API_SCOPE_NAME);
                                scopes.add(name.toString());
                            }
                        }
                    }
                }

            }
        }
        return scopes;
    }
    public static Schema retrieveTenantConfigJsonSchema(){
        return tenantConfigJsonSchema;
    }

    public static Schema retrieveOperationPolicySpecificationJsonSchema(){
        return operationPolicySpecSchema;
    }

    /**
     * Get gateway environments defined in the configuration: api-manager.xml
     *
     * @return map of configured gateway environments against the environment name
     */
    public static Map<String, Environment> getReadOnlyGatewayEnvironments() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getApiGatewayEnvironments();
    }

    /**
     * Get external environments registered with the given name
     *
     * @return the external environments
     */
    public static ExternalEnvironment getExternalEnvironment(String providerName) {
        return ServiceReferenceHolder.getInstance().getExternalEnvironment(providerName);
    }

    /**
     * Get registered API Definition Parsers as a Map
     *
     * @return Map of Registered API Definition Parsers
     */
    public static Map<String, org.wso2.carbon.apimgt.api.APIDefinition> getApiDefinitionParsersMap() {
        return ServiceReferenceHolder.getInstance().getApiDefinitionMap();
    }

    /**
     * Check whether there are external environments registered
     */
    public static boolean isAnyExternalGateWayProviderExists() {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        for (Environment environment : gatewayEnvironments.values()) {
            if (!APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals(environment.getProvider())) {
                return true;
            }
        }
        return false;
    }
    public static Tier findTier(Collection<Tier> tiers, String tierName) {
        for (Tier tier : tiers) {
            if (tier.getName() != null && tierName != null && tier.getName().equals(tierName)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * Load the common policies for the organization at the first startup. This will only copy the policies to the
     * database if the total policies for this organization is zero.
     *
     * @param organization organization name
     */
    public static void loadCommonOperationPolicies(String organization) {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        try {
            Set<String> existingPolicies = apiMgtDAO.getCommonOperationPolicyNames(organization);
            String policySpecLocation = CarbonUtils.getCarbonHome() + File.separator
                    + APIConstants.COMMON_OPERATION_POLICY_SPECIFICATIONS_LOCATION;
            String policyDefinitionLocation = CarbonUtils.getCarbonHome() + File.separator
                    + APIConstants.COMMON_OPERATION_POLICY_DEFINITIONS_LOCATION;
            File policiesDir = new File(policySpecLocation);
            File[] files = policiesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String jsonContent;
                    String fileName = file.getName();
                    if (!existingPolicies.contains(fileName.substring(0, fileName.lastIndexOf('.')))) {
                        try {
                            jsonContent = FileUtils.readFileToString(file);
                            OperationPolicySpecification policySpec = getValidatedOperationPolicySpecification(jsonContent);
                            if (policySpec != null) {
                                OperationPolicyData policyData = new OperationPolicyData();
                                policyData.setSpecification(policySpec);
                                policyData.setOrganization(organization);
                                String policyFileName = getOperationPolicyFileName(policySpec.getName(),
                                        policySpec.getVersion());
                                OperationPolicyDefinition synapsePolicyDefinition =
                                        getOperationPolicyDefinitionFromFile(policyDefinitionLocation,
                                                policyFileName, APIConstants.SYNAPSE_POLICY_DEFINITION_EXTENSION);
                                if (synapsePolicyDefinition != null) {
                                    synapsePolicyDefinition.setGatewayType(OperationPolicyDefinition.GatewayType.Synapse);
                                    policyData.setSynapsePolicyDefinition(synapsePolicyDefinition);
                                }
                                OperationPolicyDefinition ccPolicyDefinition =
                                        getOperationPolicyDefinitionFromFile(policyDefinitionLocation,
                                                policyFileName, APIConstants.CC_POLICY_DEFINITION_EXTENSION);
                                if (ccPolicyDefinition != null) {
                                    ccPolicyDefinition.setGatewayType(OperationPolicyDefinition.GatewayType.ChoreoConnect);
                                    policyData.setCcPolicyDefinition(ccPolicyDefinition);
                                }

                                policyData.setMd5Hash(getMd5OfOperationPolicy(policyData));
                                apiMgtDAO.addCommonOperationPolicy(policyData);
                                log.info("Common operation policy " + policySpec.getName() + "_" + policySpec.getVersion()
                                        + " was added to the organization " + organization + " successfully");
                            }
                        } catch (IOException | APIManagementException e) {
                            log.error("Invalid policy specification for file " + file.getName()
                                    + ".Hence skipped from importing as a common operation policy.", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception when loading " + APIConstants.OPERATION_POLICIES + " for organization "
                    + organization, e);
        }
    }

    /**
     * Read the operation policy definition from the provided path and return the definition object
     *
     * @param extractedFolderPath   Location of the policy definition
     * @param definitionFileName    Name of the policy file
     * @param fileExtension         Since there can be both synapse and choreo connect definitons, fileExtension is used
     *
     * @return OperationPolicyDefinition
     */
    public static OperationPolicyDefinition getOperationPolicyDefinitionFromFile(String extractedFolderPath,
                                                                                 String definitionFileName,
                                                                                 String fileExtension)
            throws APIManagementException {

        OperationPolicyDefinition policyDefinition = null;
        try {
            String fileName = extractedFolderPath + File.separator + definitionFileName + fileExtension;
            if (checkFileExistence(fileName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found policy definition file " + fileName);
                }
                String yamlContent = FileUtils.readFileToString(new File(fileName));
                policyDefinition = new OperationPolicyDefinition();
                policyDefinition.setContent(yamlContent);
                policyDefinition.setMd5Hash(getMd5OfOperationPolicyDefinition(policyDefinition));
                if (StringUtils.equals(APIConstants.CC_POLICY_DEFINITION_EXTENSION, fileExtension)) {
                    policyDefinition.setGatewayType(OperationPolicyDefinition.GatewayType.ChoreoConnect);
                }
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while reading policy specification from path: "
                    + extractedFolderPath, e, ExceptionCodes.ERROR_READING_META_DATA);
        }
        return policyDefinition;
    }

    /**
     * Check whether there exists a file for the provided location
     *
     * @param fileLocation   Location of the file
     * @return True if file exists
     */
    public static boolean checkFileExistence(String fileLocation) {

        File testFile = new File(fileLocation);
        return testFile.exists();
    }

    /**
     * Get the validated policy specification object from a provided policy string. Validation is done against the
     * policy schema
     *
     * @param policySpecAsString  Policy specification as a string
     * @return OperationPolicySpecification object
     * @throws APIManagementException If the policy schema validation fails
     */
    public static OperationPolicySpecification getValidatedOperationPolicySpecification(String policySpecAsString)
            throws APIManagementException {

        Schema schema = APIUtil.retrieveOperationPolicySpecificationJsonSchema();
        org.json.JSONObject policySpecJson = null;
        if (schema != null) {
            try {
                policySpecJson = new org.json.JSONObject(policySpecAsString);
                if (policySpecJson.has(APIConstants.DATA)) {
                    policySpecJson = policySpecJson.getJSONObject(APIConstants.DATA);
                }
                schema.validate(policySpecJson);
            } catch (ValidationException e) {
                List<String> errors = e.getAllMessages();
                String errorMessage = errors.size() + " validation error(s) found. Error(s) :" + errors.toString();
                throw new APIManagementException("Policy specification validation failure. " + errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_POLICY_SPECIFICATION,
                                errorMessage));
            }
            return new Gson().fromJson(policySpecJson.toString(), OperationPolicySpecification.class);
        }
        throw new APIManagementException("API policy schema not found");
    }

    /**
     * Export the policy attribute object of the specification as a string
     *
     * @param policySpecification  Policy specification
     * @return policy attributes string
     * @throws APIManagementException If the policy schema validation fails
     */
    public static String getPolicyAttributesAsString(OperationPolicySpecification policySpecification) {

        String policyParamsString = "";
        if (policySpecification != null) {
            if (policySpecification.getPolicyAttributes() != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                policyParamsString = gson.toJson(policySpecification.getPolicyAttributes());
            }
        }
        return policyParamsString;
    }

    /**
     * Return the md5 hash of the provided policy. To generate the md5 hash, policy Specification and the
     * two definitions are used
     *
     * @param policyData  Operation policy data
     * @return md5 hash
     */
    public static String getMd5OfOperationPolicy(OperationPolicyData policyData) {

        String policySpecificationAsString = "";
        String synapsePolicyDefinitionAsString = "";
        String ccPolicyDefinitionAsString = "";

        if (policyData.getSpecification() != null) {
            policySpecificationAsString = new Gson().toJson(policyData.getSpecification());
        }
        if (policyData.getSynapsePolicyDefinition() != null) {
            synapsePolicyDefinitionAsString = new Gson().toJson(policyData.getSynapsePolicyDefinition());
        }
        if (policyData.getCcPolicyDefinition() != null) {
            ccPolicyDefinitionAsString = new Gson().toJson(policyData.getCcPolicyDefinition());
        }

        return DigestUtils.md5Hex(policySpecificationAsString + synapsePolicyDefinitionAsString
                + ccPolicyDefinitionAsString);
    }

    /**
     * Return the md5 hash of the policy definition string
     *
     * @param policyDefinition  Operation policy definition
     * @return md5 hash of the definition content
     */
    public static String getMd5OfOperationPolicyDefinition(OperationPolicyDefinition policyDefinition) {

        String md5Hash = "";

        if (policyDefinition != null) {
            if (policyDefinition.getContent() != null) {
                md5Hash = DigestUtils.md5Hex(policyDefinition.getContent());
            }
        }
        return md5Hash;
    }

    /**
     * This method will generate a operation policy data object if a mediation policy is found for the selected flow.
     *
     * @param api             API
     * @param policyDirection Request, response of fault flow
     * @param organization    organization
     * @throws APIManagementException
     */
    public static OperationPolicyData getPolicyDataForMediationFlow(API api, String policyDirection,
                                                                    String organization) {

        OperationPolicyData policyData = null;
        switch (policyDirection) {
            case APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST:
                if (isSequenceDefined(api.getInSequence()) && api.getInSequenceMediation() != null) {
                    Mediation inSequenceMediation = api.getInSequenceMediation();
                    policyData = generateOperationPolicyDataObject(api.getUuid(), organization,
                            inSequenceMediation.getName(), inSequenceMediation.getConfig());
                }
                break;
            case APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE:
                if (isSequenceDefined(api.getOutSequence()) && api.getOutSequenceMediation() != null) {
                    Mediation outSequenceMediation = api.getOutSequenceMediation();
                    policyData = generateOperationPolicyDataObject(api.getUuid(), organization,
                            outSequenceMediation.getName(), outSequenceMediation.getConfig());
                }
                break;
            case APIConstants.OPERATION_SEQUENCE_TYPE_FAULT:
                if (isSequenceDefined(api.getFaultSequence()) && api.getFaultSequenceMediation() != null) {
                    Mediation faultSequenceMediation = api.getFaultSequenceMediation();
                    policyData = generateOperationPolicyDataObject(api.getUuid(), organization,
                            faultSequenceMediation.getName(), faultSequenceMediation.getConfig());
                }
                break;
        }
        return policyData;
    }

    public static OperationPolicyData generateOperationPolicyDataObject(String apiUuid, String organization,
                                                                        String policyName,
                                                                        String policyDefinitionString) {

        OperationPolicySpecification policySpecification = new OperationPolicySpecification();
        policySpecification.setCategory(OperationPolicySpecification.PolicyCategory.Mediation);
        policySpecification.setName(policyName);
        policySpecification.setDisplayName(policyName);
        policySpecification.setDescription("This is a mediation policy migrated to an operation policy.");

        ArrayList<String> gatewayList = new ArrayList<>();
        gatewayList.add(APIConstants.OPERATION_POLICY_SUPPORTED_GATEWAY_SYNAPSE);
        policySpecification.setSupportedGateways(gatewayList);

        ArrayList<String> supportedAPIList = new ArrayList<>();
        supportedAPIList.add(APIConstants.OPERATION_POLICY_SUPPORTED_API_TYPE_HTTP);
        supportedAPIList.add(APIConstants.OPERATION_POLICY_SUPPORTED_API_TYPE_SOAP);
        supportedAPIList.add(APIConstants.OPERATION_POLICY_SUPPORTED_API_TYPE_SOAPTOREST);
        supportedAPIList.add(APIConstants.OPERATION_POLICY_SUPPORTED_API_TYPE_GRAPHQL);
        policySpecification.setSupportedApiTypes(supportedAPIList);

        ArrayList<String> applicableFlows = new ArrayList<>();
        applicableFlows.add(APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);
        applicableFlows.add(APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE);
        applicableFlows.add(APIConstants.OPERATION_SEQUENCE_TYPE_FAULT);
        policySpecification.setApplicableFlows(applicableFlows);

        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setOrganization(organization);
        policyData.setSpecification(policySpecification);
        policyData.setApiUUID(apiUuid);

        if (policyDefinitionString != null) {
            OperationPolicyDefinition policyDefinition = new OperationPolicyDefinition();
            policyDefinition.setContent(policyDefinitionString);
            policyDefinition.setGatewayType(OperationPolicyDefinition.GatewayType.Synapse);
            policyDefinition.setMd5Hash(APIUtil.getMd5OfOperationPolicyDefinition(policyDefinition));
            policyData.setSynapsePolicyDefinition(policyDefinition);
        }

        policyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(policyData));

        return policyData;
    }


    public static String getOperationPolicyFileName(String policyName, String policyVersion) {
        if (StringUtils.isEmpty(policyVersion)) {
            policyVersion = "v1";
        }
        return policyName + "_" + policyVersion;
    }

    public static void initializeVelocityContext(VelocityEngine velocityEngine){
        velocityEngine.setProperty(RuntimeConstants.OLD_CHECK_EMPTY_OBJECTS, false);
        velocityEngine.setProperty(DeprecatedRuntimeConstants.OLD_SPACE_GOBBLING,"bc");
        velocityEngine.setProperty("runtime.conversion.handler", "none");
        velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    }

    /**
     * Handles gateway vendor for APK before insert DB operations.
     *
     * @param gatewayVendorType Gateway vendor
     * @param gatewayType       Gateway type
     * @return gateway vendor for the API
     */
    public static String setGatewayVendorBeforeInsertion(String gatewayVendorType, String gatewayType) {
        if (gatewayType != null && APIConstants.WSO2_APK_GATEWAY.equals(gatewayType)) {
            gatewayVendorType =  APIConstants.WSO2_APK_GATEWAY;
        }
        return gatewayVendorType;
    }

    /**
     * Provides gateway type considering gateway vendor.
     *
     * @param gatewayVendor Gateway vendor type.
     * @return gateway type
     */
    public static String getGatewayType(String gatewayVendor) {
        String gatewayType = null;
        if (APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals(gatewayVendor)) {
            gatewayType = APIConstants.WSO2_SYNAPSE_GATEWAY;
        } else if (APIConstants.WSO2_APK_GATEWAY.equals(gatewayVendor)) {
            gatewayType = APIConstants.WSO2_APK_GATEWAY;
        }
        return gatewayType;
    }

    /**
     * Replaces wso2/apk gateway vendor type as wso2 after retrieving from db.
     *
     * @param gatewayVendor Gateway vendor type
     * @return wso2 gateway vendor type
     */
    public static String handleGatewayVendorRetrieval(String gatewayVendor) {
        if (APIConstants.WSO2_APK_GATEWAY.equals(gatewayVendor)) {
            gatewayVendor = APIConstants.WSO2_GATEWAY_ENVIRONMENT;
        }
        return  gatewayVendor;
    }

    /**
     * Add the default correlation configs to the database at the initial server start up.
     */
    public static void addDefaultCorrelationConfigs() throws APIManagementException {
        CorrelationConfigDAO correlationConfigDAO = CorrelationConfigDAO.getInstance();

        if (correlationConfigDAO.isConfigExist()) {
            log.debug("Default correlation configs are not written to the database again.");
            return;
        }

        correlationConfigDAO.addDefaultCorrelationConfigs();
    }

    /**
     * Generate code verifier for PKCE
     * @return code verifier
     */
    public static String generateCodeVerifier () {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    /**
     * Generate code challenge for PKCE
     * @param codeVerifier verifier
     * @return code challenge
     */
    public static String generateCodeChallenge(String codeVerifier) throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(APIConstants.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance(APIConstants.SHA_256);
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
    
    /**
     * This method is used to get the default API level policy in a given tenant space
     *
     * @param tenantId tenantId
     * @return default API level policy for a given tenant
     */
    public static String getDefaultAPILevelPolicy(int tenantId) throws APIManagementException {

        Map<String, Tier> apiPolicies = getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_API, tenantId);
        if (apiPolicies.size() > 0) {
            String defaultTier =
                    getTenantConfigPropertyValue(APIConstants.API_TENANT_CONF_DEFAULT_API_TIER, tenantId);
            if (StringUtils.isNotEmpty(defaultTier) && apiPolicies.containsKey(defaultTier)) {
                return defaultTier;
            }
            if (isEnabledUnlimitedTier()) {
                return APIConstants.UNLIMITED_TIER;
            }
            return apiPolicies.keySet().toArray()[0].toString();
        }
        return null;
    }

    /**
     * This method is used to get the default Application level policy in a given tenant space
     *
     * @param tenantId tenantId
     * @return default Application level policy for a given tenant
     */
    public static String getDefaultApplicationLevelPolicy(int tenantId) throws APIManagementException {

        Map<String, Tier> applicationLevelPolicies = getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_APP,
                tenantId);
        if (applicationLevelPolicies.size() > 0) {
            String defaultTier =
                    getTenantConfigPropertyValue(APIConstants.API_TENANT_CONF_DEFAULT_APPLICATION_TIER, tenantId);
            if (StringUtils.isNotEmpty(defaultTier) && applicationLevelPolicies.containsKey(defaultTier)) {
                return defaultTier;
            }
            if (isEnabledUnlimitedTier()) {
                return APIConstants.UNLIMITED_TIER;
            }
            return applicationLevelPolicies.keySet().toArray()[0].toString();
        }
        return null;
    }

    /**
     * This method is used to get the default Subscription level policy in a given tenant space
     *
     * @param tenantId tenantId
     * @return default Subscription level policy for a given tenant
     */
    public static String getDefaultSubscriptionPolicy(int tenantId) throws APIManagementException {

        Map<String, Tier> subscriptionPolicies = getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, tenantId);
        if (subscriptionPolicies.size() > 0) {
            String defaultTier =
                    getTenantConfigPropertyValue(APIConstants.API_TENANT_CONF_DEFAULT_SUBSCRIPTION_TIER, tenantId);
            if (StringUtils.isNotEmpty(defaultTier) && subscriptionPolicies.containsKey(defaultTier)) {
                return defaultTier;
            }
            if (isEnabledUnlimitedTier()) {
                return APIConstants.UNLIMITED_TIER;
            }
            return subscriptionPolicies.keySet().toArray()[0].toString();
        }
        return null;
    }

    /**
     * This method is used to check whether a given policy is configured as default or not in a given tenant domain
     *
     * @param policyName policy name
     * @param policyLevel policy level
     * @param tenantDomain tenant domain name
     * @return default Subscription level policy for a given tenant
     */
    public static boolean checkPolicyConfiguredAsDefault(String policyName, String policyLevel, String tenantDomain)
            throws APIManagementException {

        String configKey = null;
        if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyLevel)) {
            configKey = APIConstants.API_TENANT_CONF_DEFAULT_API_TIER;
        } else if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
            configKey = APIConstants.API_TENANT_CONF_DEFAULT_SUBSCRIPTION_TIER;
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyLevel)) {
            configKey = APIConstants.API_TENANT_CONF_DEFAULT_APPLICATION_TIER;
        }
        if (StringUtils.isNotEmpty(configKey)) {
            String defaultPolicyValue = getTenantConfigPropertyValue(configKey,
                    getTenantIdFromTenantDomain(tenantDomain));
            return StringUtils.equalsIgnoreCase(defaultPolicyValue, policyName);
        }
        return false;
    }

    /**
     * Get configured value of a property in a given tenant domain
     *
     * @param propertyName property name
     * @param tenantId tenant ID
     * @return default Subscription level policy for a given tenant
     */
    private static String getTenantConfigPropertyValue(String propertyName, int tenantId)
            throws APIManagementException {
        String organization = getTenantDomainFromTenantId(tenantId);
        JSONObject tenantConfig = getTenantConfig(organization);
        if (tenantConfig.containsKey(propertyName)) {
            return tenantConfig.get(propertyName).toString();
        }
        return null;
    }

    /**
     * Get aggregated string from set of scopes
     *
     * @param scopes set of scopes
     * @return scopes string
     */
    public static String getScopesAsString(Set<Scope> scopes) {
        StringBuilder scopesStringBuilder = new StringBuilder();
        for (Scope scope : scopes) {
            scopesStringBuilder.append(scope.getKey()).append(" ");
        }
        return scopesStringBuilder.toString().trim();
    }

    /**
     * Check whether API Chat feature is enabled
     *
     * @return returns true if API Chat feature is enabled, false if disabled.
     */
    public static boolean isApiChatEnabled() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isApiChatEnabled = config.getFirstProperty(APIConstants.API_CHAT_ENABLED);
        if (isApiChatEnabled == null) {
            return false;
        }

        return Boolean.parseBoolean(isApiChatEnabled);
    }

    /**
     * This method is used for AI Service calls related to API-Chat feature and Marketplace-Assistant feature
     *
     * @param endpointConfigName Config name to retrieve the AI Service URL
     * @param authTokenConfigName Config name to retrieve the token for authentication purposes
     * @return CloseableHttpResponse
     * @throws APIManagementException
     */
    public static CloseableHttpResponse getAIServiceHealth(String endpointConfigName, String authTokenConfigName)
            throws APIManagementException, MalformedURLException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String endpoint = config.getFirstProperty(endpointConfigName);
        String authToken = config.getFirstProperty(authTokenConfigName);

        HttpGet healthGet = new HttpGet(endpoint + "/health");
        healthGet.setHeader("auth-key", authToken);

        URL url = new URL(endpoint);
        int port = url.getPort();
        String protocol = url.getProtocol();
        HttpClient httpClient = APIUtil.getHttpClient(port, protocol);

        try {
            return executeHTTPRequest(healthGet, httpClient);
        } catch (APIManagementException | IOException e) {
            throw new APIManagementException("Error encountered while connecting to service", e);
        }
    }

//    public static CloseableHttpResponse invokeAIService(String endpointConfigName, String authTokenConfigName) {
//
//    }
}
