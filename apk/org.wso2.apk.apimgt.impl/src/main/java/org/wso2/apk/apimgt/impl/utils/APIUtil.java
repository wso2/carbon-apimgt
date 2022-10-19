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

package org.wso2.apk.apimgt.impl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.xerces.util.SecurityManager;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.LoginPostExecutor;
import org.wso2.apk.apimgt.api.NewPostLoginExecutor;
import org.wso2.apk.apimgt.api.PasswordResolver;
import org.wso2.apk.apimgt.api.doc.model.APIDefinition;
import org.wso2.apk.apimgt.api.doc.model.APIResource;
import org.wso2.apk.apimgt.api.doc.model.Operation;
import org.wso2.apk.apimgt.api.doc.model.Parameter;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APICategory;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.APIPublisher;
import org.wso2.apk.apimgt.api.model.APIRevision;
import org.wso2.apk.apimgt.api.model.APIStatus;
import org.wso2.apk.apimgt.api.model.APIStore;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.AsyncProtocolEndpoint;
import org.wso2.apk.apimgt.api.model.CORSConfiguration;
import org.wso2.apk.apimgt.api.model.EndpointSecurity;
import org.wso2.apk.apimgt.api.model.Environment;
import org.wso2.apk.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.apk.apimgt.api.model.Mediation;
import org.wso2.apk.apimgt.api.model.OperationPolicyData;
import org.wso2.apk.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecification;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.Tier;
import org.wso2.apk.apimgt.api.model.URITemplate;
import org.wso2.apk.apimgt.api.model.VHost;
import org.wso2.apk.apimgt.api.model.WebsubSubscriptionConfiguration;
import org.wso2.apk.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.apk.apimgt.api.model.policy.BandwidthLimit;
import org.wso2.apk.apimgt.api.model.policy.EventCountLimit;
import org.wso2.apk.apimgt.api.model.policy.Limit;
import org.wso2.apk.apimgt.api.model.policy.Policy;
import org.wso2.apk.apimgt.api.model.policy.PolicyConstants;
import org.wso2.apk.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.apk.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.apk.apimgt.impl.*;
import org.wso2.apk.apimgt.impl.config.APIMConfigService;
import org.wso2.apk.apimgt.impl.config.APIMConfigServiceImpl;
import org.wso2.apk.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.apk.apimgt.impl.dao.ScopesDAO;
import org.wso2.apk.apimgt.impl.dao.WorkflowDAO;
import org.wso2.apk.apimgt.impl.dao.impl.WorkflowDAOImpl;
import org.wso2.apk.apimgt.impl.dto.WorkflowDTO;
import org.wso2.apk.apimgt.impl.proxy.ExtendedProxyRoutePlanner;
import org.wso2.apk.apimgt.user.exceptions.UserException;
import org.wso2.apk.apimgt.user.mgt.internal.UserManagerHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.net.ssl.SSLContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

/**
 * This class contains the utility methods used by the implementations of APIManager, APIProvider
 * and APIConsumer interfaces.
 */
public final class APIUtil {

    private static final Log log = LogFactory.getLog(APIUtil.class);

    private static final Log audit = LogFactory.getLog("AUDIT_LOG");
    public static final String ERROR_RETRIEVING_GATEWAY_DOMAIN_MAPPINGS_FROM_REGISTRY = "Error while retrieving gateway domain mappings from registry";
    public static final String INVALID_JSON_IN_GATEWAY_TENANT_DOMAIN_MAPPINGS = "Invalid JSON found in the gateway tenant domain mappings";
    public static final String MALFORMED_JSON_IN_GATEWAY_TENANT_DOMAIN_MAPPINGS = "Malformed JSON found in the gateway tenant domain mappings";
    public static final String ATTEMPT_TO_EXECUTE_PRIVILEGED_OPERATION_AS_THE_ANONYMOUS_USER = "Attempt to execute privileged operation as the anonymous user";
    public static final String ERROR_WHILE_RETRIEVING_TENANT_DOMAIN = "Error while retrieving tenant domain values from user store";

    private static boolean isContextCacheInitialized = false;

    public static final String DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION = "disableRoleValidationAtScopeCreation";

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

    public static final String TENANT_IDLE_TIME = "tenant.idle.time";
    public static final String UI_PERMISSION_ACTION = "ui.execute";

    private static Schema tenantConfigJsonSchema;
    private static Schema operationPolicySpecSchema;

    private static APIMConfigService apimConfigService;

    private APIUtil() {

    }

    //Need tenantIdleTime to check whether the tenant is in idle state in loadTenantConfig method
    static {
        tenantIdleTimeMillis =
                Long.parseLong(System.getProperty(
                        TENANT_IDLE_TIME,
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

    //constants for getting masked token
    private static final int MAX_LEN = 36;
    private static final int MAX_VISIBLE_LEN = 8;
    private static final int MIN_VISIBLE_LEN_RATIO = 5;
    private static final String MASK_CHAR = "X";

    /**
     * To initialize the publisherRoleCache configurations, based on configurations.
     */
    public static void init() throws APIManagementException {

//        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        String isPublisherRoleCacheEnabledConfiguration = apiManagerConfiguration
//                .getFirstProperty(APIConstants.PUBLISHER_ROLE_CACHE_ENABLED);
//        isPublisherRoleCacheEnabled = isPublisherRoleCacheEnabledConfiguration == null || Boolean
//                .parseBoolean(isPublisherRoleCacheEnabledConfiguration);
//        try {
//            eventPublisherFactory = ServiceReferenceHolder.getInstance().getEventPublisherFactory();
//            eventPublishers.putIfAbsent(EventPublisherType.ASYNC_WEBHOOKS,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.ASYNC_WEBHOOKS));
//            eventPublishers.putIfAbsent(EventPublisherType.CACHE_INVALIDATION,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.CACHE_INVALIDATION));
//            eventPublishers.putIfAbsent(EventPublisherType.GLOBAL_CACHE_INVALIDATION,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.GLOBAL_CACHE_INVALIDATION));
//            eventPublishers.putIfAbsent(EventPublisherType.NOTIFICATION,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.NOTIFICATION));
//            eventPublishers.putIfAbsent(EventPublisherType.TOKEN_REVOCATION,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.TOKEN_REVOCATION));
//            eventPublishers.putIfAbsent(EventPublisherType.BLOCKING_EVENT,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.BLOCKING_EVENT));
//            eventPublishers.putIfAbsent(EventPublisherType.KEY_TEMPLATE,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.KEY_TEMPLATE));
//            eventPublishers.putIfAbsent(EventPublisherType.KEYMGT_EVENT,
//                    eventPublisherFactory.getEventPublisher(EventPublisherType.KEYMGT_EVENT));
//        } catch (EventPublisherException e) {
//            log.error("Could not initialize the event publishers. Events might not be published properly.");
//            throw new APIManagementException(e);
//        }
    }

//    /**
//     * This method used to send Notifications
//     *
//     * @param event        Event object
//     * @param notifierType eventType
//     */
//    public static void sendNotification(org.wso2.carbon.apimgt.impl.notifier.events.Event event, String notifierType) {
//
//        if (ServiceReferenceHolder.getInstance().getNotifiersMap().containsKey(notifierType)) {
//            List<Notifier> notifierList = ServiceReferenceHolder.getInstance().getNotifiersMap().get(notifierType);
//            notifierList.forEach((notifier) -> {
//                try {
//                    notifier.publishEvent(event);
//                } catch (NotifierException e) {
//                    log.error("Error when publish " + event + " through notifier:" + notifierType + ". Error:" + e);
//                }
//            });
//        }
//
//    }

    public static APIStatus getApiStatus(String status) throws APIManagementException {

        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }
        }
        return apiStatus;
    }

    public static void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static void handleExceptionWithCode(String msg, ErrorHandler code) throws APIManagementException {

        log.error(msg);
        throw new APIManagementException(msg, code);
    }

    public static void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t, code);
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
            log.error(msg, e);
            throw new APIManagementException(ExceptionCodes.EXTERNAL_STORE_CLASS_NOT_FOUND);
        } catch (InstantiationException e) {
            String msg = "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be load";
            log.error(msg, e);
            throw new APIManagementException(ExceptionCodes.EXTERNAL_STORE_CLASS_NOT_LOADED);
        } catch (IllegalAccessException e) {
            String msg = "One or more classes defined in APIConstants.EXTERNAL_API_STORE_CLASS_NAME cannot be access";
            log.error(msg, e);
            throw new APIManagementException(ExceptionCodes.EXTERNAL_STORE_CLASS_NOT_ACCESSIBLE);
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
            //TODO handle configs
            String content = getAPIMConfigService().getExternalStoreConfig(getTenantDomainFromTenantId(tenantId));
            OMElement element = AXIOMUtil.stringToOM(content);
            apiStoreIterator = element.getChildrenWithLocalName("ExternalAPIStore");

        } catch (XMLStreamException e) {
            throw new APIManagementException("Malformed XML found in the External Stores Configuration resource", e,
                    ExceptionCodes.MALFORMED_XML_IN_EXTERNAL_STORE_CONFIG);
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
        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
//                .getExternalAPIStores();
        //This is a dummy return value
        return new APIManagerConfiguration().getExternalAPIStores();
    }

    /**
     * Check if document visibility levels are enabled
     *
     * @return True if document visibility levels are enabled
     */
    public static boolean isDocVisibilityLevelsEnabled() {
        // checking if Doc visibility levels enabled in api-manager.xml
        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
//                getAPIManagerConfiguration().getFirstProperty(
//                        APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS).equals("true");
        //This is a dummy return value
        return true;
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
     * @return Map<String, Tier> an unfiltered Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAllTiers() throws APIManagementException {

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, APIConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns an unfiltered map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @return Map<String, Tier> an unfiltered Map of tier names and Tier objects - possibly empty
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

        return getTiersFromPolicies(PolicyConstants.POLICY_LEVEL_SUB, APIConstants.SUPER_TENANT_ID);
    }

    /**
     * Returns a map of API availability tiers as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getAdvancedSubsriptionTiers() throws APIManagementException {

        return getAdvancedSubsriptionTiers(APIConstants.SUPER_TENANT_ID);
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
     * @param tierType     type of the tiers
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
            throw new APIManagementException("No such a tier type : " + tierType, ExceptionCodes.UNSUPPORTED_TIER_TYPE);
        }
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
            throw new APIManagementException(ExceptionCodes.ANON_USER_ACTION);
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

        try {
            String tenantDomain = getTenantDomain(userNameWithoutChange);
            //TODO fix tenant flow
//            PrivilegedCarbonContext.startTenantFlow();
//            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            authorized = UserManagerHolder.getUserManager().isUserAuthorized(tenantId,
                    getTenantAwareUsername(userNameWithoutChange), permission,
                    UI_PERMISSION_ACTION);
            if (APIConstants.Permissions.APIM_ADMIN.equals(permission)) {
                addToRolesCache(APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE, userNameWithoutChange,
                        authorized ? 1 : 2);
            }

        } catch (UserException e) {
            throw new APIManagementException("Error while checking the user:" + userNameWithoutChange
                    + " authorized or not", e, ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
        } finally {
//            PrivilegedCarbonContext.endTenantFlow();
        }

        return authorized;
    }

    /**
     * Checks whether the disablePermissionCheck parameter enabled
     *
     * @return boolean
     */
    public static boolean isPermissionCheckDisabled() {

        APIManagerConfiguration config =
                //TODO handle configs
//                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration();
        String disablePermissionCheck = config.getFirstProperty(APIConstants.API_STORE_DISABLE_PERMISSION_CHECK);
        if (disablePermissionCheck == null) {
            return false;
        }

        return Boolean.parseBoolean(disablePermissionCheck);
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
            throw new APIManagementException(ExceptionCodes.ANON_USER_ACTION);
        }

        String[] roles = getValueFromCache(APIConstants.API_USER_ROLE_CACHE, username);
        if (roles != null) {
            return roles;
        }
        try {
            String tenantDomain = getTenantDomain(username);
            int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            roles = UserManagerHolder.getUserManager().getRoleListOfUser(tenantId,
                    getTenantAwareUsername(username));
            addToRolesCache(APIConstants.API_USER_ROLE_CACHE, username, roles);
            return roles;
        } catch (UserException e) {
            throw new APIManagementException("UserStoreException while trying the role list of the user " + username,
                    e, ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
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
            throw new APIManagementException("Attempt to execute privileged operation as the anonymous user",
                    ExceptionCodes.ANON_USER_ACTION);
        }
        try {
            String tenantDomain = getTenantDomain(username);
            String tenantAwareUserName = getTenantAwareUsername(username);
            int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            return UserManagerHolder.getUserManager().isExistingUser(tenantId, tenantAwareUserName);
        } catch (UserException e) {
            throw new APIManagementException("UserStoreException while trying the user existence " + username, e,
                    ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
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

    private static JsonElement getFileBaseTenantConfig() throws APIManagementException {

        try {
            byte[] localTenantConfFileData = getLocalTenantConfFileData();
            String tenantConfDataStr = new String(localTenantConfFileData, Charset.defaultCharset());
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(tenantConfDataStr);
        } catch (IOException e) {
            throw new APIManagementException("Error while retrieving file base tenant-config", e);
        }
    }

    /**
     * Gets the byte content of the local tenant-conf.json
     *
     * @return byte content of the local tenant-conf.json
     * @throws IOException error while reading local tenant-conf.json
     */
    private static byte[] getLocalTenantConfFileData() throws IOException {

        //TODO handle config files properly
//        String tenantConfLocation = CarbonUtils.getCarbonHome() + File.separator +
//                APIConstants.RESOURCE_FOLDER_LOCATION + File.separator +
//                APIConstants.API_TENANT_CONF;
//        File tenantConfFile = new File(tenantConfLocation);
//        byte[] data;
//        if (tenantConfFile.exists()) { // Load conf from resources directory in pack if it exists
//            FileInputStream fileInputStream = new FileInputStream(tenantConfFile);
//            data = IOUtils.toByteArray(fileInputStream);
//        } else { // Fallback to loading the conf that is stored at jar level if file does not exist in pack
//            InputStream inputStream =
//                    APIManagerComponent.class.getResourceAsStream("/tenant/" + APIConstants.API_TENANT_CONF);
//            data = IOUtils.toByteArray(inputStream);
//        }
        return new byte[1];
    }

    public static boolean isAnalyticsEnabled() {

        return APIManagerAnalyticsConfiguration.getInstance().isAnalyticsEnabled();
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

        //TODO configuration flow
//        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("APIManagement.Enabled"));
        return true;
    }

    public static Set<APIStore> getExternalAPIStores(int tenantId) throws APIManagementException {

        SortedSet<APIStore> apistoreSet = new TreeSet<APIStore>(new APIStoreNameComparator());
        apistoreSet.addAll(getExternalStores(tenantId));
        return apistoreSet;

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
     * Get tenants by state
     *
     * @param state state of the tenant
     * @return set of tenants
     * @throws UserException
     */
    public static Set<String> getTenantDomainsByState(String state) throws UserException {

        return UserManagerHolder.getUserManager().getTenantDomainsByState(state);
    }

    /**
     * Check if tenant is available
     *
     * @param tenantDomain tenant Domain
     * @return isTenantAvailable
     * @throws UserException
     */
    public static boolean isTenantAvailable(String tenantDomain) throws UserException {

        return UserManagerHolder.getUserManager().isTenantAvailable(tenantDomain);
    }

    public static boolean isOnPremResolver() throws APIManagementException {

        //TODO handle configs
//        OrganizationResolver resolver = APIUtil.getOrganizationResolver();
//        return resolver instanceof OnPremResolver;
        return true; //This is a dummy return value
    }

    public static int getInternalOrganizationId(String organization) throws APIManagementException {

        //TODO handle configs
//        return getOrganizationResolver().getInternalId(organization);
        return -1234; //This is a dummy return value
    }

    public static String getInternalOrganizationDomain(String organization) throws APIManagementException {

        return APIUtil.getTenantDomainFromTenantId(APIUtil.getInternalOrganizationId(organization));
    }

    /**
     * check whether given role is exist
     *
     * @param userName logged user
     * @param roleName role name need to check
     * @return true if exist and false if not
     * @throws APIManagementException If an error occurs
     */
    public static boolean isRoleNameExist(String userName, String roleName) {

        if (roleName == null || StringUtils.isEmpty(roleName.trim())) {
            return true;
        }

        //disable role validation if "disableRoleValidationAtScopeCreation" system property is set
        String disableRoleValidation = System.getProperty(DISABLE_ROLE_VALIDATION_AT_SCOPE_CREATION);
        if (Boolean.parseBoolean(disableRoleValidation)) {
            return true;
        }

        try {
            int tenantId = UserManagerHolder.getUserManager().getTenantId(getTenantDomain(userName));

            String[] roles = roleName.split(",");
            for (String role : roles) {
                if (!UserManagerHolder.getUserManager().isExistingRole(tenantId, role.trim())) {
                    return false;
                }
            }
        } catch (UserException | APIManagementException e) {
            log.error("Error when getting the list of roles", e);
            return false;
        }
        return true;
    }

    /**
     * Create API Definition in JSON
     *
     * @param api API
     * @throws APIManagementException if failed to generate the content and save
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
    public static int getTenantId(String userName) throws APIManagementException {

        String tenantDomain = null;
        //get tenant domain from user name
        tenantDomain = getTenantDomain(userName);
        return getTenantIdFromTenantDomain(tenantDomain);
    }

    /**
     * Helper method to get tenantId from tenantDomain
     *
     * @param tenantDomain tenant Domain
     * @return tenantId
     */
    public static int getTenantIdFromTenantDomain(String tenantDomain) {

        if (tenantDomain == null) {
            return APIConstants.SUPER_TENANT_ID;
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

        if (organization == null) {
            return APIConstants.SUPER_TENANT_ID;
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

        try {
            return UserManagerHolder.getUserManager().getTenantDomainByTenantId(tenantId);
        } catch (UserException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns true if sequence is set
     *
     * @param sequence
     * @return
     */
    public static boolean isSequenceDefined(String sequence) {

        return sequence != null && !"none".equals(sequence) && !StringUtils.isEmpty(sequence);
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
                    propValue = "DUMMY_CONFIG_VALUE";
                    //TODO handle configs

//                            ServiceReferenceHolder.getContextService().getServerConfigContext().getContextRoot();
                } else if ("admin.username".equals(sysProp) || "admin.password".equals(sysProp)) {
                    try {
                        if ("admin.username".equals(sysProp)) {
                            propValue = UserManagerHolder.getUserManager().getPropertyFromFile("admin.username");
                        } else {
                            propValue = UserManagerHolder.getUserManager().getPropertyFromFile("admin.password");
                        }
                    } catch (UserException e) {
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

    /**
     * Returns a map of gateway / store domains for the tenant
     *
     * @return a Map of domain names for tenant
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, String> getDomainMappings(String tenantDomain, String appType)
            throws APIManagementException {

        Map<String, String> domains = new HashMap<String, String>();
        String resourcePath;
        //TODO Registry implementation
        /*try {
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
            throw new APIManagementException(ERROR_RETRIEVING_GATEWAY_DOMAIN_MAPPINGS_FROM_REGISTRY, e,
                    ExceptionCodes.GATEWAY_DOMAIN_MAPPING_RETRIEVE_ERROR);
        } catch (ClassCastException e) {
            throw new APIManagementException(INVALID_JSON_IN_GATEWAY_TENANT_DOMAIN_MAPPINGS, e,
                    ExceptionCodes.INVALID_GATEWAY_DOMAIN_MAPPING_JSON);
        } catch (ParseException e) {
            throw new APIManagementException(MALFORMED_JSON_IN_GATEWAY_TENANT_DOMAIN_MAPPINGS, e,
                    ExceptionCodes.MALFORMED_GATEWAY_DOMAIN_MAPPING_JSON);
        }*/
        return domains;
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
     * Read the REST API group id extractor class reference from api-manager.xml.
     *
     * @return REST API group id extractor class reference.
     */
    public static String getRESTApiGroupingExtractorImplementation() {

        //TODO config setup flow
        ConfigurationHolder config = new APIManagerConfigurationServiceImpl(new ConfigurationHolder())
                .getAPIManagerConfiguration();
        String restApiGroupingExtractor = config
                .getFirstProperty(APIConstants.API_STORE_REST_API_GROUP_EXTRACTOR_IMPLEMENTATION);
        if (StringUtils.isEmpty(restApiGroupingExtractor)) {
            restApiGroupingExtractor = getGroupingExtractorImplementation();
        }
        return restApiGroupingExtractor;
    }

    /**
     * Read the group id extractor class reference from api-manager.xml.
     *
     * @return group id extractor class reference.
     */
    public static String getGroupingExtractorImplementation() {

        //TODO config setup flow
        ConfigurationHolder config = new APIManagerConfigurationServiceImpl(new ConfigurationHolder())
                .getAPIManagerConfiguration();
        return config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION);
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

    private static boolean isUnlimitedTierPaid(String tenantDomain) throws APIManagementException {

        JSONObject apiTenantConfig = getTenantConfig(tenantDomain);
        if (apiTenantConfig != null) {
            Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_IS_UNLIMITED_TIER_PAID);

            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            } else {
                throw new APIManagementException(
                        APIConstants.API_TENANT_CONF_IS_UNLIMITED_TIER_PAID + " config does not exist for tenant "
                                + tenantDomain, ExceptionCodes.CONFIG_NOT_FOUND);
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

    /**
     * Return a http client instance
     *
     * @param url - server url
     * @return
     */

    public static HttpClient getHttpClient(String url) throws APIManagementException {

        URL configUrl = null;
        try {
            configUrl = new URL(url);
        } catch (MalformedURLException e) {
            handleExceptionWithCode("URL is malformed",
                    e, ExceptionCodes.from(ExceptionCodes.URI_PARSE_ERROR, "Malformed url"));
        }
        int port = configUrl.getPort();
        String protocol = configUrl.getProtocol();
        return getHttpClient(port, protocol);
    }

    /**
     * Return a PoolingHttpClientConnectionManager instance
     *
     * @param protocol- service endpoint protocol. It can be http/https
     * @return PoolManager
     */
    private static PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(String protocol)
            throws APIManagementException {

        PoolingHttpClientConnectionManager poolManager;
        if (APIConstants.HTTPS_PROTOCOL.equals(protocol)) {
            SSLConnectionSocketFactory socketFactory = createSocketFactory();
            org.apache.http.config.Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register(APIConstants.HTTPS_PROTOCOL, socketFactory).build();
            poolManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } else {
            poolManager = new PoolingHttpClientConnectionManager();
        }
        return poolManager;
    }

    private static SSLConnectionSocketFactory createSocketFactory() throws APIManagementException {

        SSLContext sslContext;

        String keyStorePath = "";
        //TODO handle configuration
//                CarbonUtils.getServerConfiguration().getFirstProperty(APIConstants.TRUST_STORE_LOCATION);
        try {
            KeyStore trustStore =
                    //TODO handle configs
//                    ServiceReferenceHolder.getInstance().getTrustStore();
                    KeyStore.getInstance("JKS"); //Dummy instantiation
            sslContext = SSLContexts.custom().loadTrustMaterial(trustStore).build();

            X509HostnameVerifier hostnameVerifier;
            String hostnameVerifierOption = System.getProperty(HOST_NAME_VERIFIER);

            if (ALLOW_ALL.equalsIgnoreCase(hostnameVerifierOption)) {
                hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            } else if (STRICT.equalsIgnoreCase(hostnameVerifierOption)) {
                hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
            } else {
                hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
            }

            return new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        } catch (KeyStoreException e) {
            handleException("Failed to read from Key Store", e);
        } catch (NoSuchAlgorithmException e) {
            handleException("Failed to load Key Store from " + keyStorePath, e);
        } catch (KeyManagementException e) {
            handleException("Failed to load key from" + keyStorePath, e);
        }

        return null;
    }

    /**
     * Return a http client instance
     *
     * @param port      - server port
     * @param protocol- service endpoint protocol http/https
     * @return
     */
    public static HttpClient getHttpClient(int port, String protocol) {

        APIManagerConfiguration configuration =
                //TODO handle configs
//                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration(); //Dummy instantiation

        String maxTotal = configuration
                .getFirstProperty(APIConstants.HTTP_CLIENT_MAX_TOTAL);
        String defaultMaxPerRoute = configuration
                .getFirstProperty(APIConstants.HTTP_CLIENT_DEFAULT_MAX_PER_ROUTE);

        String proxyEnabled = configuration.getFirstProperty(APIConstants.PROXY_ENABLE);
        String proxyHost = configuration.getFirstProperty(APIConstants.PROXY_HOST);
        String proxyPort = configuration.getFirstProperty(APIConstants.PROXY_PORT);
        String proxyUsername = configuration.getFirstProperty(APIConstants.PROXY_USERNAME);
        String proxyPassword = configuration.getFirstProperty(APIConstants.PROXY_PASSWORD);
        String nonProxyHosts = configuration.getFirstProperty(APIConstants.NON_PROXY_HOSTS);
        String proxyProtocol = configuration.getFirstProperty(APIConstants.PROXY_PROTOCOL);

        if (proxyProtocol != null) {
            protocol = proxyProtocol;
        }

        PoolingHttpClientConnectionManager pool = null;
        try {
            pool = getPoolingHttpClientConnectionManager(protocol);
        } catch (APIManagementException e) {
            log.error("Error while getting http client connection manager", e);
        }
        pool.setMaxTotal(Integer.parseInt(maxTotal));
        pool.setDefaultMaxPerRoute(Integer.parseInt(defaultMaxPerRoute));

        RequestConfig params = RequestConfig.custom().build();
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(pool)
                .setDefaultRequestConfig(params);

        if (Boolean.parseBoolean(proxyEnabled)) {
            HttpHost host = new HttpHost(proxyHost, Integer.parseInt(proxyPort), protocol);
            DefaultProxyRoutePlanner routePlanner;
            if (!StringUtils.isBlank(nonProxyHosts)) {
                routePlanner = new ExtendedProxyRoutePlanner(host, configuration);
            } else {
                routePlanner = new DefaultProxyRoutePlanner(host);
            }
            clientBuilder = clientBuilder.setRoutePlanner(routePlanner);
            if (!StringUtils.isBlank(proxyUsername) && !StringUtils.isBlank(proxyPassword)) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxyHost, Integer.parseInt(proxyPort)),
                        new UsernamePasswordCredentials(proxyUsername, proxyPassword));
                clientBuilder = clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return clientBuilder.build();
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

    public static Class<?> getClassForName(String className) throws ClassNotFoundException {

        return Class.forName(className);
    }

    /**
     * Get external environments registered with the given name
     *
     * @return the external environments
     */
    public static ExternalEnvironment getExternalEnvironment(String providerName) {

//        return ServiceReferenceHolder.getInstance().getExternalEnvironment(providerName);
        return new ExternalEnvironment() { //Dummy return
            @Override
            public List<AsyncProtocolEndpoint> getExternalEndpointURLs(Environment environment) {

                return null;
            }

            @Override
            public String getType() {

                return null;
            }
        };
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
                        + tenantDomain, ExceptionCodes.CONFIG_NOT_FOUND);
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

    /**
     * Returns the tenant-conf.json in JSONObject format for the given tenant(id) from the registry.
     *
     * @param organization organization
     * @return tenant-conf.json in JSONObject format for the given tenant(id)
     * @throws APIManagementException when tenant-conf.json is not available in registry
     */
    public static JSONObject getTenantConfig(String organization) throws APIManagementException {

        //TODO implement with suitable cache
//        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
//        String cacheName = organization + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
//        if (tenantConfigCache.containsKey(cacheName)) {
//            return (JSONObject) tenantConfigCache.get(cacheName);
//        } else {

        String tenantConfig = getAPIMConfigService().getTenantConfig(organization);
        if (StringUtils.isNotEmpty(tenantConfig)) {
            try {
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(tenantConfig);
                //TODO implement with suitable cache
//                    tenantConfigCache.put(cacheName, jsonObject);
                return jsonObject;
            } catch (ParseException e) {
                throw new APIManagementException("Error occurred while converting tenant-conf to json", e,
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
        }
        return new JSONObject();
//        }
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
            throws APIManagementException {

        return APIUtil.getRESTAPIScopesFromConfig(APIUtil.getTenantRESTAPIScopesConfig(tenantDomain), null);
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
            APIManagerConfiguration configuration =
                    //TODO handle configs
//ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                    new APIManagerConfiguration(); //Dummy instantiation

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

    /**
     * Used to get access control allowed headers according to the api-manager.xml
     *
     * @return access control allowed headers string
     */
    public static String getAllowedHeaders() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
//                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_HEADERS);
        return ""; //Dummy return value
    }

    /**
     * Used to get access control allowed methods define in api-manager.xml
     *
     * @return access control allowed methods string
     */
    public static String getAllowedMethods() {

        //TODO handle configs
//       return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
//                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_METHODS);
        return ""; //Dummy return value
    }

    /**
     * Used to get access control allowed origins define in api-manager.xml
     *
     * @return allow origins list defined in api-manager.xml
     */
    public static String getAllowedOrigins() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().
//                getFirstProperty(APIConstants.CORS_CONFIGURATION_ACCESS_CTL_ALLOW_ORIGIN);
        return ""; //Dummy return value

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
        return new CORSConfiguration(false, allowOriginsStringSet, false,
                allowHeadersStringSet, allowMethodsStringSet);
    }

    public static WebsubSubscriptionConfiguration getDefaultWebsubSubscriptionConfiguration() {

        return new WebsubSubscriptionConfiguration(false, "",
                APIConstants.DEFAULT_WEBSUB_SIGNING_ALGO, APIConstants.DEFAULT_WEBSUB_SIGNATURE_HEADER);
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

    /**
     * Used to get unlimited throttling tier is enable
     *
     * @return condition of enable unlimited tier
     */
    public static boolean isEnabledUnlimitedTier() {

        //TODO handle configs
//        ThrottleProperties throttleProperties = ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration()
//                .getThrottleProperties();
//        return throttleProperties.isEnableUnlimitedTier();
        return false; //Dummy return value
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

        Map<String, Tier> tierMap = new TreeMap<>();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Policy[] policies;
        if (PolicyConstants.POLICY_LEVEL_SUB.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getSubscriptionPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_API.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getAPIPolicies(tenantId);
        } else if (PolicyConstants.POLICY_LEVEL_APP.equalsIgnoreCase(policyLevel)) {
            policies = apiMgtDAO.getApplicationPolicies(tenantId);
        } else {
            throw new APIManagementException("No such a policy type : " + policyLevel, ExceptionCodes.UNSUPPORTED_POLICY_TYPE);
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
            throw new APIManagementException("No such a policy type : " + policyLevel,
                    ExceptionCodes.UNSUPPORTED_POLICY_TYPE);
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
        jsonObject.put("info", entityInfo);
        audit.info(StringEscapeUtils.unescapeJava(jsonObject.toString()));
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
     * Used in application sharing to check if this featuer is enabled
     *
     * @return returns true if ENABLE_MULTIPLE_GROUPID is set to True
     */
    public static boolean isMultiGroupAppSharingEnabled() {

        if (multiGrpAppSharing == null) {

            APIManagerConfiguration config =
                    //TODO handle configs
//                    ServiceReferenceHolder.getInstance().
//                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
                    new APIManagerConfiguration(); //Dummy instantiation

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
     * This method is used to get the authorization configurations from the tenant registry or from api-manager.xml if
     * config is not available in tenant registry
     *
     * @param organization The organization
     * @param property     The configuration to get from tenant registry or api-manager.xml
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
     * @param organization organization.
     * @param property     The configuration to get from tenant registry
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
        APIManagerConfiguration apimConfig =
                //TODO handle configs
//                ServiceReferenceHolder.getInstance()
//                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration();
        String oAuthConfiguration = apimConfig.getFirstProperty(APIConstants.OAUTH_CONFIGS + property);

        if (!StringUtils.isBlank(oAuthConfiguration)) {
            return oAuthConfiguration;
        }

        return null;
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
     * Convert special characters to encoded value.
     *
     * @param role
     * @return encorded value
     */
    public static String sanitizeUserRole(String role) {

        if (role.contains("&")) {
            return role.replaceAll("&", "%26");
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
                    throw new APIManagementException(error,
                            ExceptionCodes.from(ExceptionCodes.ERROR_INVOKING_SP_REST_API, error));
                }
                String responseStr = EntityUtils.toString(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Response from SP: " + responseStr);
                }
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(responseStr);

            } catch (ClientProtocolException e) {
                String error = "Error while connecting to the server";
                handleExceptionWithCode(error, e, ExceptionCodes
                        .from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, error));
            } catch (IOException e) {
                String error = "Error while parsing the response";
                handleExceptionWithCode(error, e, ExceptionCodes
                        .from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_DESC, error));
            } catch (ParseException e) {
                handleExceptionWithCode("Error while parsing the response ", e, ExceptionCodes.JSON_PARSE_ERROR);
            } finally {
                httpPost.reset();
            }

        } catch (MalformedURLException e) {
            handleExceptionWithCode("Error while parsing the stream processor url", e,
                    ExceptionCodes.MALFORMED_SP_URL);
        }

        return null;

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
        return new String[0];
    }

    public static String getStoreUrl() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
//                getAPIManagerConfiguration().getFirstProperty(APIConstants.API_STORE_URL);
        return "";
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
     *
     * @return map of configured environments against environment name
     */
    public static Map<String, Environment> getReadOnlyEnvironments() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
//                .getAPIManagerConfiguration().getApiGatewayEnvironments();
        return new HashMap<>(); //Dummy return
    }

    /**
     * Get default (first) vhost of the given read only environment
     *
     * @param environmentName name of the read only environment
     * @return default vhost of environment
     */
    public static VHost getDefaultVhostOfReadOnlyEnvironment(String environmentName) throws APIManagementException {

        Map<String, Environment> readOnlyEnvironments = getReadOnlyEnvironments();
        if (readOnlyEnvironments.get(environmentName) == null) {
            throw new APIManagementException("Configured read only environment not found: "
                    + environmentName, ExceptionCodes.from(ExceptionCodes.READ_ONLY_ENVIRONMENT_NOT_FOUND, environmentName));
        }
        if (readOnlyEnvironments.get(environmentName).getVhosts().isEmpty()) {
            throw new APIManagementException("VHosts not found for the environment: "
                    + environmentName, ExceptionCodes.from(ExceptionCodes.VHOST_FOR_ENVIRONMENT_NOT_FOUND, environmentName));
        }
        return readOnlyEnvironments.get(environmentName).getVhosts().get(0);
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

    public static JSONArray getMonetizationAttributes() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
//                .getMonetizationConfigurationDto().getMonetizationAttributes();
        return new JSONArray(); //Dummy return
    }

    /**
     * Get the workflow status information for the given api for the given workflow type
     *
     * @param uuid         Api uuid
     * @param workflowType workflow type
     * @return WorkflowDTO
     * @throws APIManagementException
     */
    public static WorkflowDTO getAPIWorkflowStatus(String uuid, String workflowType)
            throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        int apiId = apiMgtDAO.getAPIID(uuid);
        WorkflowDAO workflowDAO = WorkflowDAOImpl.getInstance();
        return workflowDAO.retrieveWorkflowFromInternalReference(Integer.toString(apiId), workflowType);
    }

    /**
     * Returns a masked token for a given token.
     *
     * @param token token to be masked
     * @return masked token.
     */
    public static String getMaskedToken(String token) {

        StringBuilder maskedTokenBuilder = new StringBuilder();
        if (token != null) {
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

    /**
     * return skipRolesByRegex config
     */
    public static String getSkipRolesByRegex() {

        //TODO handle configs
//        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
//                getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        String skipRolesByRegex = config.getFirstProperty(APIConstants.SKIP_ROLES_BY_REGEX);
//        return skipRolesByRegex;
        return ""; //Dummy return value
    }

    public static Map<String, Object> getUserProperties(String userNameWithoutChange) throws APIManagementException {

        Map<String, Object> properties = new HashMap<String, Object>();
        if (APIUtil.hasPermission(userNameWithoutChange, APIConstants.Permissions.APIM_ADMIN)) {
            properties.put(APIConstants.USER_CTX_PROPERTY_ISADMIN, true);
        }
        properties.put(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES, APIUtil.getSkipRolesByRegex());

        return properties;
    }

    public static boolean isDevPortalAnonymous() {

        //TODO config setup flow
        ConfigurationHolder config = new APIManagerConfigurationServiceImpl(new ConfigurationHolder())
                .getAPIManagerConfiguration();
        String anonymousMode = config.getFirstProperty(APIConstants.API_DEVPORTAL_ANONYMOUS_MODE);
        if (anonymousMode == null) {
            return true;
        }
        return Boolean.parseBoolean(anonymousMode);
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

    public static Map getTenantBasedStoreDomainMapping(String tenantDomain) throws APIManagementException {

        //TODO registry usage
//        try {
//            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
//                    getGovernanceSystemRegistry();
//            String resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace(APIConstants.API_DOMAIN_MAPPING_TENANT_ID_IDENTIFIER, tenantDomain);
//            if (registry.resourceExists(resourcePath)) {
//                Resource resource = registry.get(resourcePath);
//                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
//                JSONParser parser = new JSONParser();
//                JSONObject mappings = (JSONObject) parser.parse(content);
//                if (mappings.containsKey(APIConstants.API_DOMAIN_MAPPINGS_STORE)) {
//                    return (Map) mappings.get(APIConstants.API_DOMAIN_MAPPINGS_STORE);
//                }
//            }
//        } catch (RegistryException e) {
//            String msg = "Error while retrieving gateway domain mappings from registry";
//            throw new APIManagementException(msg, e);
//        } catch (ClassCastException e) {
//            String msg = "Invalid JSON found in the gateway tenant domain mappings";
//            throw new APIManagementException(msg, e);
//        } catch (ParseException e) {
//            String msg = "Malformed JSON found in the gateway tenant domain mappings";
//            throw new APIManagementException(msg, e);
//        }
        return new HashMap();
    }

    public static boolean isPerTenantServiceProviderEnabled(String tenantDomain) throws APIManagementException {

        JSONObject tenantConfig = getTenantConfig(tenantDomain);
        if (tenantConfig.containsKey(APIConstants.ENABLE_PER_TENANT_SERVICE_PROVIDER_CREATION)) {
            return (boolean) tenantConfig.get(APIConstants.ENABLE_PER_TENANT_SERVICE_PROVIDER_CREATION);
        }
        return false;
    }

    public static String getTenantAdminUserName(String tenantDomain) throws APIManagementException {

        try {
            int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            String adminUserName = UserManagerHolder.getUserManager().getAdminUsername(tenantId);
            if (!tenantDomain.contentEquals(APIConstants.SUPER_TENANT_DOMAIN)) {
                return adminUserName.concat("@").concat(tenantDomain);
            }
            return adminUserName;
        } catch (UserException e) {
            throw new APIManagementException("Error in getting tenant admin username",
                    e, ExceptionCodes.from(ExceptionCodes.USERSTORE_INITIALIZATION_FAILED));
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
            throw new APIManagementException("Error while parsing Endpoint Config json", e,
                    ExceptionCodes.ENDPOINT_CONFIG_PARSE_FAILED);
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

        try {
            return UserManagerHolder.getUserManager().getClaims(endUserName, tenantId, dialectURI);
        } catch (UserException e) {
            throw new APIManagementException("Error while retrieving user claim values from user store", e,
                    ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
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

        try {
            String tenantDomain = getTenantDomain(subscriber);
            int tenantId = getTenantId(tenantDomain);
            return UserManagerHolder.getUserManager().getClaimDisplayName(claimURI, subscriber, tenantId);
        } catch (UserException e) {
            throw new APIManagementException("Error while retrieving claim values from user store", e,
                    ExceptionCodes.ERROR_RETRIEVING_CLAIM_VALUES);
        }
    }

    public static Map<String, KeyManagerConnectorConfiguration> getKeyManagerConfigurations() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getKeyManagerConnectorConfigurations();
        return new HashMap<>(); //Dummy return
    }

    /**
     * Get scopes attached to the API.
     *
     * @param id           API uuid
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

    /**
     * Replace new RESTAPI Role mappings to tenant-conf.
     *
     * @param newScopeRoleJson New object of role-scope mapping
     * @throws APIManagementException If failed to replace the new tenant-conf.
     */
    public static void updateTenantConfOfRoleScopeMapping(JSONObject newScopeRoleJson, String username)
            throws APIManagementException {

        String tenantDomain;
        tenantDomain = getTenantDomain(username);
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
        //TODO handle configs
//        ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(tenantDomain,
//                formattedTenantConf);

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

        String tenantDomain = getTenantDomain(username);

        //read from tenant-conf.json
        JsonObject existingTenantConfObject = new JsonObject();
        String existingTenantConf = "conf"; //Dummy config value
        //TODO handle configs
//                ServiceReferenceHolder.getInstance().getApimConfigService().getTenantConfig(tenantDomain);

        existingTenantConfObject = new JsonParser().parse(existingTenantConf).getAsJsonObject();

        //append original role to the role mapping list
        Set<Entry<String, JsonElement>> roleMappingEntries = newRoleMappingJson.entrySet();
        for (Entry<String, JsonElement> entry : roleMappingEntries) {
            List<String> currentRoles = Arrays.asList(String.valueOf(entry.getValue()).split(","));
            boolean isOriginalRoleAlreadyInRoles = false;
            for (String role : currentRoles) {
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
        //TODO handle configs
//        ServiceReferenceHolder.getInstance().getApimConfigService().updateTenantConfig(tenantDomain,
//                formattedTenantConf);
        //TODO cache implementation
//        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
//        String cacheName = tenantDomain + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
//        tenantConfigCache.remove(cacheName);
        if (log.isDebugEnabled()) {
            log.debug("Finalized tenant-conf.json: " + formattedTenantConf);
        }
    }

    public static boolean isCrossTenantSubscriptionsEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                //TODO handle configs
//               ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration();
        String crossTenantSubscriptionProperty =
                apiManagerConfiguration.getFirstProperty(APIConstants.API_DEVPORTAL_ENABLE_CROSS_TENANT_SUBSCRIPTION);
        if (StringUtils.isNotEmpty(crossTenantSubscriptionProperty)) {
            return Boolean.parseBoolean(crossTenantSubscriptionProperty);
        }
        return false;
    }

    public static boolean isDefaultApplicationCreationEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                //TODO handle configs
//               ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration();
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
     * @throws APIManagementException
     */
    public static String getUUIDFromIdentifier(APIIdentifier identifier, String organization) throws APIManagementException {

        return ApiMgtDAO.getInstance().getUUIDFromIdentifier(identifier, organization);
    }

    /**
     * Get UUID by the API Identifier.
     *
     * @param identifier
     * @param organization
     * @return String uuid string
     * @throws APIManagementException
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
     * @throws APIManagementException
     */
    public static APIProductIdentifier getAPIProductIdentifierFromUUID(String uuid) throws APIManagementException {

        return ApiMgtDAO.getInstance().getAPIProductIdentifierFromUUID(uuid);
    }

    /**
     * Get the API Identifier from UUID.
     *
     * @param uuid UUID of the API
     * @return API Identifier
     * @throws APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String uuid) throws APIManagementException {

        return ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(uuid);
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
            // If url is a JMS connection url or a Consul service discovery related url, validation is skipped.
            // If not, validity is checked.
            if (!endpoint.startsWith("jms:") && !endpoint.startsWith("consul(") && !urlValidator.isValid(endpoint)) {
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
     *
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
        APIManagerConfiguration apiManagerConfiguration =
                //TODO handle configs
//                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
                new APIManagerConfiguration();
        String supportedTypes = apiManagerConfiguration
                .getFirstProperty(APIConstants.API_PUBLISHER_SUPPORTED_DOC_TYPES);
        if (!StringUtils.isEmpty(supportedTypes)) {
            String[] definedTypesArr = supportedTypes.trim().split("\\s*,\\s*");
            list = Arrays.asList(definedTypesArr);
        } else {
            String[] defaultType = {"pdf", "txt", "doc", "docx", "xls", "xlsx", "odt", "ods", "json", "yaml", "md"};
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

    public static Schema retrieveTenantConfigJsonSchema() {

        return tenantConfigJsonSchema;
    }

    public static Schema retrieveOperationPolicySpecificationJsonSchema() {

        return operationPolicySpecSchema;
    }

    /**
     * Get gateway environments defined in the configuration: api-manager.xml
     *
     * @return map of configured gateway environments against the environment name
     */
    public static Map<String, Environment> getReadOnlyGatewayEnvironments() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
//                .getAPIManagerConfiguration().getApiGatewayEnvironments();
        return new HashMap<>(); //This is a dummy return value
    }

    /**
     * Get registered API Definition Parsers as a Map
     *
     * @return Map of Registered API Definition Parsers
     */
    public static Map<String, org.wso2.apk.apimgt.api.APIDefinition> getApiDefinitionParsersMap() {

        //TODO handle configs
//        return ServiceReferenceHolder.getInstance().getApiDefinitionMap();

        return new HashMap<>(); //This is a dummy return value
    }

    /**
     * Get the validated policy specification object from a provided policy string. Validation is done against the
     * policy schema
     *
     * @param policySpecAsString Policy specification as a string
     * @return OperationPolicySpecification object
     * @throws APIManagementException If the policy schema validation fails
     */
    public static OperationPolicySpecification getValidatedOperationPolicySpecification(String policySpecAsString)
            throws APIManagementException {

        Schema schema = APIUtil.retrieveOperationPolicySpecificationJsonSchema();
        if (schema != null) {
            try {
                org.json.JSONObject uploadedConfig = new org.json.JSONObject(policySpecAsString);
                schema.validate(uploadedConfig);
            } catch (ValidationException e) {
                List<String> errors = e.getAllMessages();
                String errorMessage = errors.size() + " validation error(s) found. Error(s) :" + errors.toString();
                throw new APIManagementException("Policy specification validation failure. " + errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INVALID_OPERATION_POLICY_SPECIFICATION,
                                errorMessage));
            }
            return new Gson().fromJson(policySpecAsString, OperationPolicySpecification.class);
        }
        return null;
    }

    /**
     * Export the policy attribute object of the specification as a string
     *
     * @param policySpecification Policy specification
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
     * @param policyData Operation policy data
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
     * @param policyDefinition Operation policy definition
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

    /**
     * Handles gateway vendor for Choreo Connect before insert DB operations.
     *
     * @param gatewayVendorType Gateway vendor
     * @param gatewayType       Gateway type
     * @return gateway vendor for the API
     */
    public static String setGatewayVendorBeforeInsertion(String gatewayVendorType, String gatewayType) {

        if (gatewayType != null && APIConstants.WSO2_CHOREO_CONNECT_GATEWAY.equals(gatewayType)) {
            gatewayVendorType = APIConstants.WSO2_CHOREO_CONNECT_GATEWAY;
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
        } else if (APIConstants.WSO2_CHOREO_CONNECT_GATEWAY.equals(gatewayVendor)) {
            gatewayType = APIConstants.WSO2_CHOREO_CONNECT_GATEWAY;
        }
        return gatewayType;
    }

    /**
     * Replaces wso2/choreo-connect gateway vendor type as wso2 after retrieving from db.
     *
     * @param gatewayVendor Gateway vendor type
     * @return wso2 gateway vendor type
     */
    public static String handleGatewayVendorRetrieval(String gatewayVendor) {

        if (APIConstants.WSO2_CHOREO_CONNECT_GATEWAY.equals(gatewayVendor)) {
            gatewayVendor = APIConstants.WSO2_GATEWAY_ENVIRONMENT;
        }
        return gatewayVendor;
    }

    public static String getTenantDomainFromRequestURL(String requestURI) {

        String domain = null;
        if (requestURI.contains("/t/")) {
            int index = requestURI.indexOf("/t/");
            int endIndex = requestURI.indexOf("/", index + 3);
            domain = endIndex != -1 ? requestURI.substring(index + 3, endIndex) : requestURI.substring(index + 3);
        }

        return domain;
    }

    public static String getTenantDomainFromUrl(String url) {

        int tenantDelimiterIndex = url.indexOf("/t/");
        if (tenantDelimiterIndex != -1) {
            String temp = url.substring(tenantDelimiterIndex + 3);
            int indexOfSlash = temp.indexOf(47);
            String tenant = indexOfSlash != -1 ? temp.substring(0, indexOfSlash) : temp;
            return tenant;
        } else {
            return url;
        }
    }

    public static String getTenantDomain(String userName) throws APIManagementException {
        String tenantDomain;
        try {
            tenantDomain = UserManagerHolder.getUserManager().getTenantDomain(userName);
            if (tenantDomain.isEmpty()) {
                tenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
            }
        } catch (UserException e) {
            throw new APIManagementException(ERROR_WHILE_RETRIEVING_TENANT_DOMAIN, e,
                    ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
        }
        return tenantDomain;
    }

    public static String getTenantAwareUsername(String userName) throws APIManagementException {
        try {
            userName = UserManagerHolder.getUserManager().getTenantAwareUsername(userName);
        } catch (UserException e) {
            throw new APIManagementException("Error while getting tenant Aware Username of the user:" + userName,
                    e, ExceptionCodes.USERSTORE_INITIALIZATION_FAILED);
        }
        return userName;
    }

    private static APIMConfigService getAPIMConfigService() {

        if (apimConfigService == null) {
            apimConfigService = new APIMConfigServiceImpl();
        }
        return apimConfigService;
    }
}
