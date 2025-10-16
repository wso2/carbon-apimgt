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

package org.wso2.carbon.apimgt.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.VectorDBProviderConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayCleanupSkipList;
import org.wso2.carbon.apimgt.impl.dto.LoadingTenants;
import org.wso2.carbon.apimgt.impl.dto.OrgAccessControl;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import org.wso2.carbon.apimgt.impl.dto.TenantSharingConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.TokenValidationDto;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.dto.ai.AIAPIConfigurationsDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.ApiChatConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.DesignAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.EmbeddingProviderConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.GuardrailProviderConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.ai.MarketplaceAssistantConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWKSConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationConfigurationDto;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.apimgt.impl.APIConstants.API_RUNTIME_READ_ONLY;
import static org.wso2.carbon.apimgt.impl.APIConstants.SHA_256;

/**
 * Global API Manager configuration. This is generally populated from a special XML descriptor
 * file at system startup. Once successfully populated, this class does not allow more parameters
 * to be added to the configuration. The design of this class has been greatly inspired by
 * the ServerConfiguration class in Carbon core. This class uses a similar '.' separated
 * approach to keep track of XML parameters.
 */
public class APIManagerConfiguration {

    private Map<String, List<String>> configuration = new ConcurrentHashMap<String, List<String>>();

    private static Log log = LogFactory.getLog(APIManagerConfiguration.class);

    private static final String USERID_LOGIN = "UserIdLogin";
    private static final String EMAIL_LOGIN = "EmailLogin";
    private static final String PRIMARY_LOGIN = "primary";
    private static final String CLAIM_URI = "ClaimUri";
    private static final String TOKEN_REVOCATION_NOTIFIERS = "TokenRevocationNotifiers";
    private static final String REALTIME_NOTIFIER = "RealtimeNotifier";
    private static final String PERSISTENT_NOTIFIER = "PersistentNotifier";
    private static final String TOKEN_REVOCATION_NOTIFIERS_PASSWORD = "TokenRevocationNotifiers.Notifier.Password";
    public static final String RECEIVER_URL_PORT = "receiver.url.port";
    public static final String AUTH_URL_PORT = "auth.url.port";
    public static final String JMS_PORT = "jms.port";
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String DEFAULT_PROVIDER = "wso2";
    public static final String WEBSOCKET_DEFAULT_GATEWAY_URL = "ws://localhost:9099";
    public static final String WEBSUB_DEFAULT_GATEWAY_URL = "http://localhost:9021";
    private Map<String, Map<String, String>> loginConfiguration = new ConcurrentHashMap<String, Map<String, String>>();
    private JSONArray applicationAttributes = new JSONArray();
    private CacheInvalidationConfiguration cacheInvalidationConfiguration;

    private HttpClientConfigurationDTO httpClientConfiguration;

    private RecommendationEnvironment recommendationEnvironment;

    private SecretResolver secretResolver;

    private boolean initialized;
    private ThrottleProperties throttleProperties = new ThrottleProperties();
    private ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
    private static MarketplaceAssistantConfigurationDTO marketplaceAssistantConfigurationDto = new MarketplaceAssistantConfigurationDTO();
    private static ApiChatConfigurationDTO apiChatConfigurationDto = new ApiChatConfigurationDTO();
    private static DesignAssistantConfigurationDTO designAssistantConfigurationDto = new DesignAssistantConfigurationDTO();
    private static AIAPIConfigurationsDTO aiapiConfigurationsDTO = new AIAPIConfigurationsDTO();
    private static final APIMGovernanceConfigDTO apimGovConfigurationDto = new APIMGovernanceConfigDTO();

    private WorkflowProperties workflowProperties = new WorkflowProperties();
    private Map<String, Environment> apiGatewayEnvironments = new LinkedHashMap<String, Environment>();
    private final Map<String, GuardrailProviderConfigurationDTO> guardrailProviders = new HashMap<>();
    private final Map<String, TenantSharingConfigurationDTO> tenantSharingConfigurations = new HashMap<>();
    private final EmbeddingProviderConfigurationDTO embeddingProviderConfigurationDTO =
            new EmbeddingProviderConfigurationDTO();
    private final VectorDBProviderConfigurationDTO vectorDBProviderConfigurationDTO =
            new VectorDBProviderConfigurationDTO();
    private static Properties realtimeNotifierProperties;
    private static Properties persistentNotifierProperties;
    private static Map<String, String> analyticsProperties;
    private static Map<String, String> persistenceProperties = new HashMap<String, String>();
    private static String tokenRevocationClassName;
    private static String certificateBoundAccessEnabled;
    private DistributedThrottleConfig distributedThrottleConfig = new DistributedThrottleConfig();
    private GatewayCleanupSkipList gatewayCleanupSkipList = new GatewayCleanupSkipList();
    private RedisConfig redisConfig = new RedisConfig();
    private SolaceConfig solaceConfig = new SolaceConfig();
    private OrgAccessControl orgAccessControl = new OrgAccessControl();
    public OrgAccessControl getOrgAccessControl() {
        return orgAccessControl;
    }

    public void setOrgAccessControl(OrgAccessControl orgAccessControl) {
        this.orgAccessControl = orgAccessControl;
    }

    private Map<String, List<String>> restApiJWTAuthAudiences = new HashMap<>();
    private JSONObject subscriberAttributes = new JSONObject();
    private static Map<String, String> analyticsMaskProps;
    private TokenValidationDto tokenValidationDto = new TokenValidationDto();
    private boolean enableAiConfiguration;
    private String hashingAlgorithm = SHA_256;
    private boolean isTransactionCounterEnabled;
    private static boolean isMCPSupportEnabled = true;
    private static String devportalMode = APIConstants.DEVPORTAL_MODE_HYBRID;
    private static volatile boolean isRuntimeReadOnly = false;

    public Map<String, List<String>> getRestApiJWTAuthAudiences() {
        return restApiJWTAuthAudiences;
    }

    public Map<String, ExtensionListener> getExtensionListenerMap() {

        return extensionListenerMap;
    }

    public boolean isRuntimeReadOnly() {
        return isRuntimeReadOnly;
    }

    public void setRuntimeReadOnly(boolean runtimeReadOnly) {
        this.isRuntimeReadOnly = runtimeReadOnly;
    }

    private Map<String, ExtensionListener> extensionListenerMap = new HashMap<>();

    public Map<String, Boolean> getDoMediateExtensionFaultSequenceMap() {

        return doMediateExtensionFaultSequenceMap;
    }

    private Map<String, Boolean> doMediateExtensionFaultSequenceMap = new HashMap<>();

    public static Properties getRealtimeTokenRevocationNotifierProperties() {

        return realtimeNotifierProperties;
    }

    public static Properties getPersistentTokenRevocationNotifiersProperties() {

        return persistentNotifierProperties;
    }

    public static String getTokenRevocationClassName() {

        return tokenRevocationClassName;
    }

    public static boolean isTokenRevocationEnabled() {

        return !tokenRevocationClassName.isEmpty();
    }

    public MarketplaceAssistantConfigurationDTO getMarketplaceAssistantConfigurationDto() {

        return marketplaceAssistantConfigurationDto;
    }

    public ApiChatConfigurationDTO getApiChatConfigurationDto() {

        return apiChatConfigurationDto;
    }

    public DesignAssistantConfigurationDTO getDesignAssistantConfigurationDto() {

        return designAssistantConfigurationDto;
    }

    private Set<APIStore> externalAPIStores = new HashSet<APIStore>();
    private EventHubConfigurationDto eventHubConfigurationDto;
    private MonetizationConfigurationDto monetizationConfigurationDto = new MonetizationConfigurationDto();

    public MonetizationConfigurationDto getMonetizationConfigurationDto() {

        return monetizationConfigurationDto;
    }

    public Map<String, Map<String, String>> getLoginConfiguration() {

        return loginConfiguration;
    }

    private final GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = new GatewayArtifactSynchronizerProperties();;

    private JSONArray customProperties = new JSONArray();
    private GatewayNotificationConfiguration gatewayNotificationConfiguration = new GatewayNotificationConfiguration();

    /**
     * Returns the configuration of the Identity Provider.
     *
     * @return configuration of the Identity Provider from the api-manager configuration
     */
    public IDPConfiguration getIdentityProviderConfig() {

        if (getFirstProperty(APIConstants.IDENTITY_PROVIDER_AUTHORIZE_ENDPOINT) != null) {
            return new IDPConfiguration.Builder()
                    .authorizeEndpoint(getFirstProperty(APIConstants.IDENTITY_PROVIDER_AUTHORIZE_ENDPOINT))
                    .oidcLogoutEndpoint(getFirstProperty(APIConstants.IDENTITY_PROVIDER_OIDC_LOGOUT_ENDPOINT))
                    .build();
        } else {
            return null;
        }
    }

    /**
     * Returns Product REST APIs' cache configuration by reading from api-manager.xml
     *
     * @return Product REST APIs' cache configuration.
     */
    public RESTAPICacheConfiguration getRESTAPICacheConfig() {
        boolean tokenCacheEnabled = Boolean.parseBoolean(getFirstProperty(APIConstants.REST_API_TOKEN_CACHE_ENABLED));
        int tokenCacheExpiry = Integer.parseInt(getFirstProperty(APIConstants.REST_API_TOKEN_CACHE_EXPIRY));
        boolean cacheControlHeadersEnabled = Boolean.parseBoolean(getFirstProperty(APIConstants.REST_API_CACHE_CONTROL_HEADERS_ENABLED));
        int cacheControlHeadersMaxAge = Integer.parseInt(getFirstProperty(APIConstants.REST_API_CACHE_CONTROL_HEADERS_MAX_AGE));
        return new RESTAPICacheConfiguration.Builder()
                .tokenCacheEnabled(tokenCacheEnabled)
                .tokenCacheExpiry(tokenCacheExpiry)
                .cacheControlHeadersEnabled(cacheControlHeadersEnabled)
                .cacheControlHeadersMaxAge(cacheControlHeadersMaxAge)
                .build();
    }

    /**
     * Populate this configuration by reading an XML file at the given location. This method
     * can be executed only once on a given APIManagerConfiguration instance. Once invoked and
     * successfully populated, it will ignore all subsequent invocations.
     *
     * @param filePath Path of the XML descriptor file
     * @throws APIManagementException If an error occurs while reading the XML descriptor
     */
    public void load(String filePath) throws APIManagementException {

        if (initialized) {
            return;
        }
        InputStream in = null;
        int offset = APIUtil.getPortOffset();
        int receiverPort = 9611 + offset;
        int authUrlPort = 9711 + offset;
        int jmsPort = 5672 + offset;
        System.setProperty(RECEIVER_URL_PORT, "" + receiverPort);
        System.setProperty(AUTH_URL_PORT, "" + authUrlPort);
        System.setProperty(JMS_PORT, "" + jmsPort);
        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
            String url = getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
            if (url == null) {
                log.error("API_KEY_VALIDATOR_URL is null");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new APIManagementException("I/O error while reading the API manager " +
                    "configuration: " + filePath, e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
            throw new APIManagementException("Error while parsing the API manager " +
                    "configuration: " + filePath, e);
        } catch (OMException e) {
            log.error(e.getMessage());
            throw new APIManagementException("Error while parsing API Manager configuration: " + filePath, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new APIManagementException("Unexpected error occurred while parsing configuration: " + filePath, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public Set<String> getConfigKeySet() {

        if (configuration != null) {
            return configuration.keySet();
        }
        return null;
    }

    @UsedByMigrationClient
    public String getFirstProperty(String key) {

        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

    public List<String> getProperty(String key) {

        return configuration.get(key);
    }

    public void reloadSystemProperties() {

        for (Map.Entry<String, List<String>> entry : configuration.entrySet()) {
            List<String> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                String text = list.remove(i);
                list.add(i, APIUtil.replaceSystemProperty(text));
            }
        }
    }

    private void readChildElements(OMElement serverConfig,
                                   Stack<String> nameStack) throws APIManagementException {

        for (Iterator childElements = serverConfig.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            String localName = element.getLocalName();
            nameStack.push(localName);
            if (API_RUNTIME_READ_ONLY.equals(localName)) {
                    isRuntimeReadOnly = Boolean.parseBoolean(element.getText());
            } else if ("APIKeyValidator".equals(localName)) {
                OMElement keyManagerServiceUrl = element.getFirstChildWithName(new QName(APIConstants.AUTHSERVER_URL));
                if (keyManagerServiceUrl != null) {
                    String serviceUrl = keyManagerServiceUrl.getText();
                    addKeyManagerConfigsAsSystemProperties(APIUtil.replaceSystemProperty(serviceUrl));
                }
            } else if (TOKEN_REVOCATION_NOTIFIERS.equals(localName)) {
                tokenRevocationClassName = element.getAttributeValue(new QName("class"));
            } else if (REALTIME_NOTIFIER.equals(localName)) {
                Iterator revocationPropertiesIterator = element.getChildrenWithLocalName("Property");
                Properties properties = new Properties();
                while (revocationPropertiesIterator.hasNext()) {
                    OMElement propertyElem = (OMElement) revocationPropertiesIterator.next();
                    properties.setProperty(propertyElem.getAttributeValue(new QName("name")),
                            propertyElem.getText());
                }
                realtimeNotifierProperties = properties;
            } else if (PERSISTENT_NOTIFIER.equals(localName)) {
                Iterator revocationPropertiesIterator = element.getChildrenWithLocalName("Property");
                Properties properties = new Properties();
                while (revocationPropertiesIterator.hasNext()) {
                    OMElement propertyElem = (OMElement) revocationPropertiesIterator.next();
                    if (propertyElem.getAttributeValue(new QName("name")).
                            equalsIgnoreCase("password")) {
                        if (secretResolver.isInitialized() && secretResolver
                                .isTokenProtected(TOKEN_REVOCATION_NOTIFIERS_PASSWORD)) {
                            properties.setProperty(propertyElem.getAttributeValue(new QName("name")),
                                    secretResolver.
                                            resolve(TOKEN_REVOCATION_NOTIFIERS_PASSWORD));
                        } else {
                            properties.setProperty(propertyElem.getAttributeValue(new QName("name")),
                                    propertyElem.getText());
                        }
                    } else {
                        properties
                                .setProperty(propertyElem.getAttributeValue(new QName("name")),
                                        propertyElem.getText());
                    }
                }
                persistentNotifierProperties = properties;
            } else if ("Analytics".equals(localName)) {
                OMElement properties = element.getFirstChildWithName(new QName("Properties"));
                Iterator analyticsPropertiesIterator = properties.getChildrenWithLocalName("Property");
                Map<String, String> analyticsProps = new HashMap<>();
                while (analyticsPropertiesIterator.hasNext()) {
                    OMElement propertyElem = (OMElement) analyticsPropertiesIterator.next();
                    String name = propertyElem.getAttributeValue(new QName("name"));
                    String value = propertyElem.getText();
                    value = MiscellaneousUtil.resolve(value, secretResolver);
                    if ("keystore_location".equals(name) || "truststore_location".equals(name)) {
                        analyticsProps.put(name, APIUtil.replaceSystemProperty(value));
                    } else {
                        analyticsProps.put(name, value);
                    }
                }

                // Load all the mask properties
                OMElement maskProperties = element.getFirstChildWithName(new QName("MaskProperties"));
                Iterator maskPropertiesIterator = maskProperties.getChildrenWithLocalName("Property");
                Map<String, String> maskProps = new HashMap<>();
                while (maskPropertiesIterator.hasNext()) {
                    OMElement propertyElem = (OMElement) maskPropertiesIterator.next();
                    String name = propertyElem.getAttributeValue(new QName("name"));
                    String value = propertyElem.getText();
                    maskProps.put(name, value.toUpperCase());
                }
                analyticsMaskProps = maskProps;

                OMElement authTokenElement = element.getFirstChildWithName(new QName("AuthToken"));
                String resolvedAuthToken = MiscellaneousUtil.resolve(authTokenElement, secretResolver);
                analyticsProps.put("auth.api.token", resolvedAuthToken);

                OMElement analyticsType = element.getFirstChildWithName(new QName("Type"));
                analyticsProps.put("type", analyticsType.getText());

                OMElement enablePolicy = element.getFirstChildWithName(new QName("PolicyEnabled"));
                analyticsProps.put("policyEnabled", enablePolicy.getText());

                analyticsProperties = analyticsProps;
            } else if ("PersistenceConfigs".equals(localName)) {
                OMElement properties = element.getFirstChildWithName(new QName("Properties"));
                Iterator analyticsPropertiesIterator = properties.getChildrenWithLocalName("Property");
                Map<String, String> persistenceProps = new HashMap<>();
                while (analyticsPropertiesIterator.hasNext()) {
                    OMElement propertyElem = (OMElement) analyticsPropertiesIterator.next();
                    String name = propertyElem.getAttributeValue(new QName("name"));
                    String value = propertyElem.getText();
                    persistenceProps.put(name, value);
                }

                persistenceProperties = persistenceProps;
            } else if (APIConstants.REDIS_CONFIG.equals(localName)) {
                OMElement redisHost = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_HOST));
                OMElement redisPort = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_PORT));
                OMElement redisUser = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_USER));
                OMElement redisPassword = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_PASSWORD));
                OMElement redisDatabaseId = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_DATABASE_ID));
                OMElement redisConnectionTimeout = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_CONNECTION_TIMEOUT));
                OMElement redisIsSslEnabled = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_IS_SSL_ENABLED));
                OMElement propertiesElement = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_PROPERTIES));
                OMElement gatewayId = element.getFirstChildWithName(new QName(APIConstants.CONFIG_REDIS_GATEWAY_ID));
                OMElement minGatewayCount = element.getFirstChildWithName(
                        new QName(APIConstants.CONFIG_REDIS_MIN_GATEWAY_COUNT));
                OMElement keyLockRetrievalTimeout = element.getFirstChildWithName(
                        new QName(APIConstants.CONFIG_REDIS_KEY_LOCK_RETRIEVAL_TIMEOUT));
                redisConfig.setRedisEnabled(true);
                redisConfig.setHost(redisHost.getText());
                redisConfig.setPort(Integer.parseInt(redisPort.getText()));
                if (gatewayId != null) {
                    redisConfig.setGatewayId(gatewayId.getText());
                } else {
                    log.error("gateway_id is not configured in deployment.toml. Please add the gateway ID" +
                            " configuration under [apim.redis_config] section in deployment.toml");
                }
                if (minGatewayCount != null) {
                    redisConfig.setMinGatewayCount(Long.parseLong(minGatewayCount.getText()));
                }
                if (keyLockRetrievalTimeout != null) {
                    redisConfig.setKeyLockRetrievalTimeout(Integer.parseInt(keyLockRetrievalTimeout.getText()));
                }
                if (redisUser != null) {
                    redisConfig.setUser(redisUser.getText());
                }
                if (redisPassword != null) {
                    redisConfig.setPassword(MiscellaneousUtil.resolve(redisPassword, secretResolver).toCharArray());
                }
                if (redisDatabaseId != null) {
                    redisConfig.setDatabaseId(Integer.parseInt(redisDatabaseId.getText()));
                }
                if (redisConnectionTimeout != null) {
                    redisConfig.setConnectionTimeout(Integer.parseInt(redisConnectionTimeout.getText()));
                }
                if (redisIsSslEnabled != null) {
                    redisConfig.setSslEnabled(Boolean.parseBoolean(redisIsSslEnabled.getText()));
                }
                if (propertiesElement != null) {
                    Iterator<OMElement> properties = propertiesElement.getChildElements();
                    if (properties != null) {
                        while (properties.hasNext()) {
                            OMElement propertyNode = properties.next();
                            if (APIConstants.CONFIG_REDIS_MAX_TOTAL.equals(propertyNode.getLocalName())) {
                                redisConfig.setMaxTotal(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_MAX_IDLE.equals(propertyNode.getLocalName())) {
                                redisConfig.setMaxIdle(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_MIN_IDLE.equals(propertyNode.getLocalName())) {
                                redisConfig.setMinIdle(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_TEST_ON_BORROW.equals(propertyNode.getLocalName())) {
                                redisConfig.setTestOnBorrow(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_TEST_ON_RETURN.equals(propertyNode.getLocalName())) {
                                redisConfig.setTestOnReturn(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_TEST_WHILE_IDLE.equals(propertyNode.getLocalName())) {
                                redisConfig.setTestWhileIdle(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_BLOCK_WHEN_EXHAUSTED.equals(propertyNode.getLocalName())) {
                                redisConfig.setBlockWhenExhausted(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_MIN_EVICTABLE_IDLE_TIME_IN_MILLIS.equals(propertyNode.getLocalName())) {
                                redisConfig.setMinEvictableIdleTimeMillis(Long.parseLong(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_TIME_BETWEEN_EVICTION_RUNS_IN_MILLIS.equals(propertyNode.getLocalName())) {
                                redisConfig.setTimeBetweenEvictionRunsMillis(Long.parseLong(propertyNode.getText()));
                            } else if (APIConstants.CONFIG_REDIS_NUM_TESTS_PER_EVICTION_RUNS.equals(propertyNode.getLocalName())) {
                                redisConfig.setNumTestsPerEvictionRun(Integer.parseInt(propertyNode.getText()));
                            }
                        }
                    }
                }
            } else if (APIConstants.DISTRIBUTED_THROTTLE_CONFIG.equals(localName)) {
                OMElement enabledElement = element.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_ENABLED));
                OMElement typeElement = element.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_TYPE));
                OMElement syncIntervalElement = element.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_SYNC_INTERVAL));
                OMElement corePoolSizeElement = element.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_CORE_POOL_SIZE));
                OMElement propertiesElement = element.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_PROPERTIES));

                if (enabledElement != null) {
                    distributedThrottleConfig.setEnabled(Boolean.parseBoolean(enabledElement.getText()));
                }
                if (typeElement != null) {
                    distributedThrottleConfig.setType(typeElement.getText());
                }
                if (syncIntervalElement != null) {
                    try {
                        distributedThrottleConfig.setSyncInterval(Integer.parseInt(syncIntervalElement.getText()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid sync interval specified", e);
                    }
                }
                if (corePoolSizeElement != null) {
                    try {
                        distributedThrottleConfig.setCorePoolSize(Integer.parseInt(corePoolSizeElement.getText()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid core pool size specified", e);
                    }
                }
                if (propertiesElement != null) {
                    OMElement host = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_HOST));
                    OMElement port = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_PORT));
                    OMElement user = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_USER));
                    OMElement password = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_PASSWORD));
                    OMElement databaseId = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_DATABASE_ID));
                    OMElement connectionTimeout = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_CONNECTION_TIMEOUT));
                    OMElement isSslEnabled = propertiesElement.getFirstChildWithName(new QName(APIConstants.DISTRIBUTED_THROTTLE_IS_SSL_ENABLED));

                    if (host != null && StringUtils.isNotBlank(host.getText())) {
                        distributedThrottleConfig.setHost(host.getText());
                    } else {
                        log.warn("DistributedThrottleConfig: KeyValueStoreOptions.Host is not specified");
                    }
                    if (port != null && StringUtils.isNotBlank(port.getText())) {
                        try {
                            distributedThrottleConfig.setPort(Integer.parseInt(port.getText()));
                        } catch (NumberFormatException e) {
                            log.warn("DistributedThrottleConfig: invalid Port value: " + port.getText(), e);
                        }
                    } else {
                        log.warn("DistributedThrottleConfig: KeyValueStoreOptions.Port is not specified");
                    }

                    if (user != null) {
                        distributedThrottleConfig.setUser(user.getText());
                    }
                    if (password != null) {
                        distributedThrottleConfig.setPassword(MiscellaneousUtil.resolve(password, secretResolver).toCharArray());
                    }

                    if (databaseId != null && StringUtils.isNotBlank(databaseId.getText())) {
                        try {
                            distributedThrottleConfig.setDatabaseId(Integer.parseInt(databaseId.getText().trim()));
                        } catch (NumberFormatException e) {
                            log.warn("Invalid DatabaseId value: " + databaseId.getText(), e);
                        }
                    }
                    if (connectionTimeout != null) {
                        try {
                            distributedThrottleConfig.setConnectionTimeout(Integer.parseInt(connectionTimeout.getText().trim()));
                        } catch (NumberFormatException e) {
                            log.warn("Invalid connectionTimeout value: " + connectionTimeout.getText(), e);
                        }                    }
                    if (isSslEnabled != null) {
                        distributedThrottleConfig.setSslEnabled(Boolean.parseBoolean(isSslEnabled.getText().trim()));
                    }

                    Iterator<OMElement> properties = propertiesElement.getChildElements();
                    if (properties != null) {
                        while (properties.hasNext()) {
                            OMElement propertyNode = properties.next();
                            if (APIConstants.DISTRIBUTED_THROTTLE_MAX_TOTAL.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setMaxTotal(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_MAX_IDLE.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setMaxIdle(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_MIN_IDLE.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setMinIdle(Integer.parseInt(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_TEST_ON_BORROW.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setTestOnBorrow(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_TEST_ON_RETURN.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setTestOnReturn(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_TEST_WHILE_IDLE.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setTestWhileIdle(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_BLOCK_WHEN_EXHAUSTED.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setBlockWhenExhausted(Boolean.parseBoolean(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_MIN_EVICTABLE_IDLE_TIME_IN_MILLIS.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setMinEvictableIdleTimeMillis(Long.parseLong(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_TIME_BETWEEN_EVICTION_RUNS_IN_MILLIS.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setTimeBetweenEvictionRunsMillis(Long.parseLong(propertyNode.getText()));
                            } else if (APIConstants.DISTRIBUTED_THROTTLE_NUM_TESTS_PER_EVICTION_RUNS.equals(propertyNode.getLocalName())) {
                                distributedThrottleConfig.setNumTestsPerEvictionRun(Integer.parseInt(propertyNode.getText()));
                            }
                        }
                    }
                }
            } else if (APIConstants.SOLACE_CONFIG.equals(localName)) {
                OMElement solaceApimApiEndpoint =
                        element.getFirstChildWithName(new QName(APIConstants.SOLACE_APIM_API_ENDPOINT));
                OMElement solaceToken = element.getFirstChildWithName(new QName(APIConstants.SOLACE_TOKEN));
                solaceConfig.setEnabled(true);
                solaceConfig.setSolaceApimApiEndpoint(solaceApimApiEndpoint.getText());
                solaceConfig.setSolaceToken(solaceToken.getText());
            } else if (elementHasText(element)) {
                String key = getKey(nameStack);
                String value = MiscellaneousUtil.resolve(element, secretResolver);
                addToConfiguration(key, APIUtil.replaceSystemProperty(value));
            } else if ("Environments".equals(localName)) {
                Iterator environmentIterator = element.getChildrenWithLocalName("Environment");
                apiGatewayEnvironments = new LinkedHashMap<String, Environment>();
                while (environmentIterator.hasNext()) {
                    OMElement environmentElem = (OMElement) environmentIterator.next();
                    setEnvironmentConfig(environmentElem);
                }
            } else if (APIConstants.EXTERNAL_API_STORES
                    .equals(localName)) {  //Initialize 'externalAPIStores' config elements
                Iterator apistoreIterator = element.getChildrenWithLocalName("ExternalAPIStore");
                externalAPIStores = new HashSet<APIStore>();
                while (apistoreIterator.hasNext()) {
                    APIStore store = new APIStore();
                    OMElement storeElem = (OMElement) apistoreIterator.next();
                    String type = storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_TYPE));
                    store.setType(type); //Set Store type [eg:wso2]
                    String className = storeElem.getAttributeValue(new QName(APIConstants
                            .EXTERNAL_API_STORE_CLASS_NAME));
                    try {
                        store.setPublisher((APIPublisher) APIUtil.getClassInstance(className));
                    } catch (InstantiationException e) {
                        String msg = "One or more classes defined in" + APIConstants.EXTERNAL_API_STORE_CLASS_NAME +
                                "cannot be instantiated";
                        log.error(msg, e);
                        throw new APIManagementException(msg, e);
                    } catch (IllegalAccessException e) {
                        String msg = "One or more classes defined in" + APIConstants.EXTERNAL_API_STORE_CLASS_NAME +
                                "cannot be access";
                        log.error(msg, e);
                        throw new APIManagementException(msg, e);
                    } catch (ClassNotFoundException e) {
                        String msg = "One or more classes defined in" + APIConstants.EXTERNAL_API_STORE_CLASS_NAME +
                                "cannot be found";
                        log.error(msg, e);
                        throw new APIManagementException(msg, e);
                    }
                    String name = storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_ID));
                    if (name == null) {
                        log.error("The ExternalAPIStore name attribute is not defined in api-manager.xml.");
                    }
                    store.setName(name); //Set store name
                    OMElement configDisplayName =
                            storeElem.getFirstChildWithName(new QName(APIConstants.EXTERNAL_API_STORE_DISPLAY_NAME));
                    String displayName = (configDisplayName != null) ? APIUtil.replaceSystemProperty(
                            configDisplayName.getText()) : name;
                    store.setDisplayName(displayName);//Set store display name
                    store.setEndpoint(APIUtil.replaceSystemProperty(
                            storeElem.getFirstChildWithName(new QName(
                                    APIConstants.EXTERNAL_API_STORE_ENDPOINT))
                                    .getText())); //Set store endpoint,which is used to publish APIs
                    store.setPublished(false);
                    if (APIConstants.WSO2_API_STORE_TYPE.equals(type)) {
                        OMElement password = storeElem.getFirstChildWithName(new QName(
                                APIConstants.EXTERNAL_API_STORE_PASSWORD));
                        if (password != null) {
                            String value = MiscellaneousUtil.resolve(password, secretResolver);
                            store.setPassword(APIUtil.replaceSystemProperty(value));
                            store.setUsername(APIUtil.replaceSystemProperty(
                                    storeElem.getFirstChildWithName(new QName(
                                            APIConstants.EXTERNAL_API_STORE_USERNAME))
                                            .getText())); //Set store login username [optional]
                        } else {
                            log.error(
                                    "The user-credentials of API Publisher is not defined in the <ExternalAPIStore> " +
                                            "config of api-manager.xml.");
                        }
                    }
                    externalAPIStores.add(store);
                }
            } else if (APIConstants.LOGIN_CONFIGS.equals(localName)) {
                Iterator loginConfigIterator = element.getChildrenWithLocalName(APIConstants.LOGIN_CONFIGS);
                while (loginConfigIterator.hasNext()) {
                    OMElement loginOMElement = (OMElement) loginConfigIterator.next();
                    parseLoginConfig(loginOMElement);
                }

            } else if (APIConstants.AdvancedThrottleConstants.THROTTLING_CONFIGURATIONS.equals(localName)) {
                setThrottleProperties(serverConfig);
            } else if (APIConstants.WorkflowConfigConstants.WORKFLOW.equals(localName)) {
                setWorkflowProperties(serverConfig);
            } else if (APIConstants.SUBSCRIBER_CONFIGURATION.equals(localName)) {
                    setSubscriberAttributeConfigs(serverConfig);
            } else if (APIConstants.ApplicationAttributes.APPLICATION_ATTRIBUTES.equals(localName)) {
                Iterator iterator = element.getChildrenWithLocalName(APIConstants.ApplicationAttributes.ATTRIBUTE);
                while (iterator.hasNext()) {
                    OMElement omElement = (OMElement) iterator.next();
                    Iterator attributes = omElement.getChildElements();
                    JSONObject jsonObject = new JSONObject();
                    boolean isHidden = Boolean.parseBoolean(
                            omElement.getAttributeValue(new QName(APIConstants.ApplicationAttributes.HIDDEN)));
                    boolean isRequired =
                            Boolean.parseBoolean(omElement
                                    .getAttributeValue(new QName(APIConstants.ApplicationAttributes.REQUIRED)));
                    jsonObject.put(APIConstants.ApplicationAttributes.HIDDEN, isHidden);
                    while (attributes.hasNext()) {
                        OMElement attribute = (OMElement) attributes.next();
                        if (attribute.getLocalName().equals(APIConstants.ApplicationAttributes.NAME)) {
                            jsonObject.put(APIConstants.ApplicationAttributes.ATTRIBUTE, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.ApplicationAttributes.DESCRIPTION)) {
                            jsonObject.put(APIConstants.ApplicationAttributes.DESCRIPTION, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.ApplicationAttributes.TOOLTIP)) {
                            jsonObject.put(APIConstants.ApplicationAttributes.TOOLTIP, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.ApplicationAttributes.TYPE)) {
                            jsonObject.put(APIConstants.ApplicationAttributes.TYPE, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.ApplicationAttributes.DEFAULT) &&
                                isRequired) {
                            jsonObject.put(APIConstants.ApplicationAttributes.DEFAULT, attribute.getText());
                        }
                    }
                    if (isHidden && isRequired && !jsonObject.containsKey(APIConstants.ApplicationAttributes.DEFAULT)) {
                        log.error("A default value needs to be given for required, hidden application attributes.");
                    }
                    jsonObject.put(APIConstants.ApplicationAttributes.REQUIRED, isRequired);
                    applicationAttributes.add(jsonObject);
                }
            } else if (APIConstants.Monetization.MONETIZATION_CONFIG.equals(localName)) {
                setMonetizationConfigurations(element);
            } else if (APIConstants.JWT_CONFIGS.equals(localName)) {
                setJWTConfiguration(element);
            } else if (APIConstants.TOKEN_ISSUERS.equals(localName)) {
                setJWTTokenIssuers(element);
            } else if (APIConstants.API_RECOMMENDATION.equals(localName)) {
                setRecommendationConfigurations(element);
            } else if (APIConstants.GlobalCacheInvalidation.GLOBAL_CACHE_INVALIDATION.equals(localName)) {
                setGlobalCacheInvalidationConfiguration(element);
            } else if (APIConstants.KeyManager.EVENT_HUB_CONFIGURATIONS.equals(localName)) {
                setEventHubConfiguration(element);
            } else if (APIConstants.GatewayArtifactSynchronizer.SYNC_RUNTIME_ARTIFACTS_PUBLISHER_CONFIG.equals(localName)) {
                setRuntimeArtifactsSyncPublisherConfig(element);
            } else if (APIConstants.GatewayArtifactSynchronizer.SYNC_RUNTIME_ARTIFACTS_GATEWAY_CONFIG.equals(localName)) {
                setRuntimeArtifactsSyncGatewayConfig(element);
            } else if (APIConstants.SkipListConstants.SKIP_LIST_CONFIG.equals(localName)) {
                setSkipListConfigurations(element);
            } else if (APIConstants.ExtensionListenerConstants.EXTENSION_LISTENERS.equals(localName)) {
                setExtensionListenerConfigurations(element);
            } else if (APIConstants.JWT_AUDIENCES.equals(localName)){
                setRestApiJWTAuthAudiences(element);
            } else if (APIConstants.CustomPropertyAttributes.CUSTOM_PROPERTIES.equals(localName)) {
                Iterator iterator = element.getChildrenWithLocalName(APIConstants.CustomPropertyAttributes.PROPERTY);
                while (iterator.hasNext()) {
                    OMElement omElement = (OMElement) iterator.next();
                    Iterator attributes = omElement.getChildElements();
                    JSONObject jsonObject = new JSONObject();
                    boolean isHidden = Boolean.parseBoolean(
                            omElement.getAttributeValue(new QName(APIConstants.CustomPropertyAttributes.HIDDEN)));
                    boolean isRequired =
                            Boolean.parseBoolean(omElement
                                    .getAttributeValue(new QName(APIConstants.CustomPropertyAttributes.REQUIRED)));
                    jsonObject.put(APIConstants.CustomPropertyAttributes.HIDDEN, isHidden);
                    while (attributes.hasNext()) {
                        OMElement attribute = (OMElement) attributes.next();
                        if (attribute.getLocalName().equals(APIConstants.CustomPropertyAttributes.NAME)) {
                            jsonObject.put(APIConstants.CustomPropertyAttributes.NAME, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.CustomPropertyAttributes.DESCRIPTION)) {
                            jsonObject.put(APIConstants.CustomPropertyAttributes.DESCRIPTION, attribute.getText());
                        } else if (attribute.getLocalName().equals(APIConstants.CustomPropertyAttributes.DEFAULT) &&
                                isRequired) {
                            jsonObject.put(APIConstants.CustomPropertyAttributes.DEFAULT, attribute.getText());
                        }
                    }
                    if (isHidden && isRequired && !jsonObject.containsKey(APIConstants.CustomPropertyAttributes.DEFAULT)) {
                        log.error("A default value needs to be given for required, hidden custom property attributes.");
                    }
                    jsonObject.put(APIConstants.CustomPropertyAttributes.REQUIRED, isRequired);
                    customProperties.add(jsonObject);
                }
            } else if (APIConstants.AI.MARKETPLACE_ASSISTANT.equals(localName)) {
                setMarketplaceAssistantConfiguration(element);
            } else if (APIConstants.AI.API_CHAT.equals(localName)) {
                setApiChatConfiguration(element);
            } else if (APIConstants.AI.DESIGN_ASSISTANT.equals(localName)) {
                setDesignAssistantConfiguration(element);
            } else if (APIConstants.AI.AI_CONFIGURATION.equals(localName)) {
                setAiConfiguration(element);
            } else if (APIConstants.AI.MCP.equals(localName)) {
                setMCPConfigurations(element);
            } else if (APIConstants.TokenValidationConstants.TOKEN_VALIDATION_CONFIG.equals(localName)) {
                setTokenValidation(element);
            } else if (APIConstants.ORG_BASED_ACCESS_CONTROL.equals(localName)) {
                setOrgBasedAccessControlConfigs(element);
            } else if (APIConstants.API_STORE_TAG.equals(localName)) {
                setDevportalConfigurations(element);
            } else if (APIConstants.TENANT_SHARING_CONFIGS.equals(localName)) {
                    // Iterate through each <TenantSharingConfig>
                    for (Iterator<?> tenantSharingConfigs = element.getChildElements(); tenantSharingConfigs.hasNext(); ) {
                        OMElement tenantSharingConfigElement = (OMElement) tenantSharingConfigs.next();

                        if (APIConstants.TENANT_SHARING_CONFIG.equals(tenantSharingConfigElement.getLocalName())) {
                            // Get the tenantSharingConfigs type
                            String type = tenantSharingConfigElement.getAttributeValue(
                                    new QName(APIConstants.TENANT_SHARING_CONFIG_TYPE));
                            if (type == null || type.isEmpty()) {
                                continue; // skip if no type defined
                            }

                            Map<String, String> propertiesMap = new HashMap<>();

                            // Iterate through each <Property>
                            for (Iterator<?> props = tenantSharingConfigElement.getChildElements(); props.hasNext(); ) {
                                OMElement prop = (OMElement) props.next();

                                if (APIConstants.TENANT_SHARING_CONFIG_PROPERTY.equals(prop.getLocalName())) {
                                    String key = prop.getAttributeValue(
                                            new QName(APIConstants.TENANT_SHARING_CONFIG_PROPERTY_KEY));
                                    String value = MiscellaneousUtil.resolve(prop, secretResolver);

                                    if (key != null && !key.isEmpty()) {
                                        propertiesMap.put(key, value);
                                    }
                                }
                            }

                            // Add to the main map
                            TenantSharingConfigurationDTO tenantSharingConfigurationDTO =
                                    new TenantSharingConfigurationDTO();
                            tenantSharingConfigurationDTO.setType(type);
                            tenantSharingConfigurationDTO.setProperties(propertiesMap);
                            tenantSharingConfigurations.put(type, tenantSharingConfigurationDTO);
                        }
                    }

            } else if (APIConstants.HASHING.equals(localName)) {
                setHashingAlgorithm(element);
            } else if (APIConstants.TransactionCounter.TRANSACTIONCOUNTER.equals(localName)) {
                OMElement counterEnabled = element.getFirstChildWithName(new QName(APIConstants.TransactionCounter.COUNTER_ENABLED));
                if (counterEnabled != null) {
                    isTransactionCounterEnabled = Boolean.parseBoolean(counterEnabled.getText());
                }
            } else if (APIConstants.APIMGovernance.GOVERNANCE_CONFIG.equals(localName)) {
                setAPIMGovernanceConfigurations(element);
            } else if (APIConstants.AI.AI.equals(localName)) {
                for (Iterator<?> aiChildren = element.getChildElements(); aiChildren.hasNext(); ) {
                    OMElement aiChildElement = (OMElement) aiChildren.next();

                    if (APIConstants.AI.GUARDRAIL_PROVIDERS.equals(aiChildElement.getLocalName())) {
                        // Iterate through each <EmbeddingProvider>
                        for (Iterator<?> providers = aiChildElement.getChildElements(); providers.hasNext(); ) {
                            OMElement providerElement = (OMElement) providers.next();

                            if (APIConstants.AI.GUARDRAIL_PROVIDER.equals(providerElement.getLocalName())) {
                                // Get the provider type
                                String type = providerElement.getAttributeValue(
                                        new QName(APIConstants.AI.GUARDRAIL_PROVIDER_TYPE));
                                if (type == null || type.isEmpty()) {
                                    continue; // skip if no type defined
                                }

                                Map<String, String> propertiesMap = new HashMap<>();

                                // Iterate through each <Property>
                                for (Iterator<?> props = providerElement.getChildElements(); props.hasNext(); ) {
                                    OMElement prop = (OMElement) props.next();

                                    if (APIConstants.AI.GUARDRAIL_PROVIDER_PROPERTY.equals(prop.getLocalName())) {
                                        String key = prop.getAttributeValue(
                                                new QName(APIConstants.AI.GUARDRAIL_PROVIDER_PROPERTY_KEY));
                                        String value = MiscellaneousUtil.resolve(prop, secretResolver);

                                        if (key != null && !key.isEmpty()) {
                                            propertiesMap.put(key, value);
                                        }
                                    }
                                }

                                // Add to the main map
                                GuardrailProviderConfigurationDTO guardrailProviderConfigurationDTO =
                                        new GuardrailProviderConfigurationDTO();
                                guardrailProviderConfigurationDTO.setType(type);
                                guardrailProviderConfigurationDTO.setProperties(propertiesMap);
                                guardrailProviders.put(type, guardrailProviderConfigurationDTO);
                            }
                        }
                    }

                    if (APIConstants.AI.EMBEDDING_PROVIDER.equals(aiChildElement.getLocalName())) {
                        // Get the provider type
                        String type = aiChildElement.getAttributeValue(
                                new QName(APIConstants.AI.EMBEDDING_PROVIDER_TYPE));
                        if (type == null || type.isEmpty()) {
                            continue; // skip if no type defined
                        }

                        Map<String, String> propertiesMap = new HashMap<>();

                        // Iterate through each <Property>
                        for (Iterator<?> props = aiChildElement.getChildElements(); props.hasNext(); ) {
                            OMElement prop = (OMElement) props.next();

                            if (APIConstants.AI.EMBEDDING_PROVIDER_PROPERTY.equals(prop.getLocalName())) {
                                String key = prop.getAttributeValue(
                                        new QName(APIConstants.AI.EMBEDDING_PROVIDER_PROPERTY_KEY));
                                String value = MiscellaneousUtil.resolve(prop, secretResolver);

                                if (key != null && !key.isEmpty()) {
                                    propertiesMap.put(key, value);
                                }
                            }
                        }

                        this.embeddingProviderConfigurationDTO.setType(type);
                        this.embeddingProviderConfigurationDTO.setProperties(propertiesMap);
                    }

                    if (APIConstants.AI.VECTOR_DB_PROVIDER.equals(aiChildElement.getLocalName())) {
                        // Get the vector DB type
                        String type = aiChildElement.getAttributeValue(
                                new QName(APIConstants.AI.VECTOR_DB_PROVIDER_TYPE));
                        if (type == null || type.isEmpty()) {
                            continue; // skip if no type defined
                        }

                        Map<String, String> propertiesMap = new HashMap<>();

                        // Iterate through each <Property>
                        for (Iterator<?> props = aiChildElement.getChildElements(); props.hasNext(); ) {
                            OMElement prop = (OMElement) props.next();

                            if (APIConstants.AI.VECTOR_DB_PROVIDER_PROPERTY.equals(prop.getLocalName())) {
                                String key = prop.getAttributeValue(
                                        new QName(APIConstants.AI.VECTOR_DB_PROVIDER_PROPERTY_KEY));
                                String value = MiscellaneousUtil.resolve(prop, secretResolver);

                                if (key != null && !key.isEmpty()) {
                                    propertiesMap.put(key, value);
                                }
                            }
                        }

                        this.vectorDBProviderConfigurationDTO.setType(type);
                        this.vectorDBProviderConfigurationDTO.setProperties(propertiesMap);
                    }
                }
            } else if (APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_CONFIGURATION.equals(localName)) {
                setGatewayNotificationConfiguration(element);
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private void setDevportalConfigurations(OMElement omElement) {

        if (omElement == null) {
            log.debug("Devportal configuration element is null. Skipping configuration parsing.");
            return;
        }
        OMElement devportalModeOmElement = omElement.getFirstChildWithName(new QName(APIConstants.DEVPORTAL_MODE));
        if (devportalModeOmElement != null && StringUtils.isNotEmpty(devportalModeOmElement.getText())) {
            String devportalModeStr = devportalModeOmElement.getText().trim().toUpperCase();
            if (APIConstants.DEVPORTAL_MODES.contains(devportalModeStr)) {
                devportalMode = devportalModeStr;
            } else {
                log.warn("Invalid Devportal mode '" + devportalModeStr + "'. Falling back to default: "
                        + devportalMode);
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Devportal mode is not specified. Using default: " + devportalMode);
        }
    }

    private void setOrgBasedAccessControlConfigs(OMElement element) {
        OMElement orgEnableElement =
                element.getFirstChildWithName(new QName(APIConstants.ORG_BASED_ACCESS_CONTROL_ENABLE));
        if (orgEnableElement != null) {
            orgAccessControl.setEnabled(Boolean.parseBoolean(orgEnableElement.getText()));
        }

        OMElement orgNameElement =
                element.getFirstChildWithName(new QName(APIConstants.ORG_BASED_ACCESS_CONTROL_ORG_NAME_CLAIM));
        if (orgNameElement != null) {
            orgAccessControl.setOrgNameLocalClaim(orgNameElement.getText());;
        }
        OMElement orgIdElement =
                element.getFirstChildWithName(new QName(APIConstants.ORG_BASED_ACCESS_CONTROL_ORG_ID_CLAIM));
        if (orgIdElement != null) {
            orgAccessControl.setOrgIdLocalClaim(orgIdElement.getText());
        }
    }

    public boolean getTransactionCounterProperties() {
        return isTransactionCounterEnabled;
    }

    public JSONObject getSubscriberAttributes() {
        return subscriberAttributes;
    }

    /**
     * Set the Subscriber Contact into Configuration.
     * @param element
     */
    private void setSubscriberAttributeConfigs(OMElement element) {
        OMElement subscriberContactConfigurationElement = element.getFirstChildWithName(new QName(APIConstants.
                SUBSCRIBER_CONFIGURATION));
        if (subscriberContactConfigurationElement != null) {
            OMElement emailRecipientElement = subscriberContactConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.SUBSCRIBER_CONFIGURATION_RECIPIENT));
            if (emailRecipientElement != null) {
                subscriberAttributes.put(APIConstants.SUBSCRIBER_CONFIGURATION_RECIPIENT,
                        emailRecipientElement.getText());
            } else {
                log.debug("Subscriber recipient field is set to default (cc).");
            }

            OMElement emailDelimiterElement = subscriberContactConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.SUBSCRIBER_CONFIGURATION_DELIMITER));
            if (emailRecipientElement != null) {
                subscriberAttributes.put(APIConstants.SUBSCRIBER_CONFIGURATION_DELIMITER,
                        emailDelimiterElement.getText());
            } else {
                log.debug("Subscriber email delimiter field is set to default (,).");
            }
        }
    }

    /**
     * Set token validation configurations from the api-manager.xml file
     *
     * @param omElement OMElement of the TokenValidation configuration block
     */
    private void setTokenValidation(OMElement omElement) {
        OMElement enforceTypeHeaderValidation = omElement.getFirstChildWithName(new QName(
                APIConstants.TokenValidationConstants.ENFORCE_JWT_TYPE_HEADER_VALIDATION));
        if (enforceTypeHeaderValidation != null) {
            tokenValidationDto.setEnforceTypeHeaderValidation(Boolean.parseBoolean(
                    enforceTypeHeaderValidation.getText()));
        }
    }

    /**
     * Set property values for each gateway environments defined in the api-manager.xml config file
     *
     * @param environmentElem OMElement of a single environment in the gateway environments list
     */
    void setEnvironmentConfig(OMElement environmentElem) throws APIManagementException {
        Environment environment = new Environment();
        environment.setType(environmentElem.getAttributeValue(new QName("type")));
        String showInConsole = environmentElem.getAttributeValue(new QName("api-console"));
        if (showInConsole != null) {
            environment.setShowInConsole(Boolean.parseBoolean(showInConsole));
        } else {
            environment.setShowInConsole(true);
        }
        String isDefault = environmentElem.getAttributeValue(new QName("isDefault"));
        if (isDefault != null) {
            environment.setDefault(Boolean.parseBoolean(isDefault));
        } else {
            environment.setDefault(false);
        }
        environment.setName(APIUtil.replaceSystemProperty(
                environmentElem.getFirstChildWithName(new QName(APIConstants.API_GATEWAY_NAME)).getText()));
        environment.setDisplayName(APIUtil.replaceSystemProperty(environmentElem.getFirstChildWithName(new QName(
                        APIConstants.API_GATEWAY_DISPLAY_NAME)).getText()));
        String gatewayType = environmentElem.getFirstChildWithName(new QName(APIConstants.API_GATEWAY_TYPE)).getText();
        if (gatewayType == null || gatewayType.isEmpty()) {
            gatewayType = APIConstants.API_GATEWAY_TYPE_REGULAR;
        }
        environment.setGatewayType(gatewayType);
        GatewayVisibilityPermissionConfigurationDTO permissionsDTO = new GatewayVisibilityPermissionConfigurationDTO();
        OMElement visibility = environmentElem.getFirstChildWithName(new QName(APIConstants.API_GATEWAY_VISIBILITY));
        List<String> visibilityRoles = new LinkedList<>();
        String[] visibilityRolesArray;
        if (visibility == null || StringUtils.isEmpty(visibility.getText())) {
            permissionsDTO.setPermissionType(APIConstants.PERMISSION_NOT_RESTRICTED);
            environment.setVisibility(APIConstants.PERMISSION_NOT_RESTRICTED);
            visibilityRolesArray = new String[]{APIConstants.EVERYONE_ROLE};
        } else {
            String visibilityString = visibility.getText();
            visibilityRolesArray = visibilityString.split(",");
            Collections.addAll(visibilityRoles, visibilityRolesArray);
            permissionsDTO.setPermissionType(APIConstants.PERMISSION_ALLOW);
            permissionsDTO.setRoles(visibilityRoles);
            environment.setVisibility(visibilityString);
        }
        environment.setVisibility(visibilityRolesArray);
        environment.setPermissions(permissionsDTO);
        if (StringUtils.isEmpty(environment.getDisplayName())) {environment.setDisplayName(environment.getName());}
        environment.setServerURL(APIUtil.replaceSystemProperty(environmentElem.getFirstChildWithName(new QName(
                        APIConstants.API_GATEWAY_SERVER_URL)).getText()));
        environment.setUserName(APIUtil.replaceSystemProperty(environmentElem.getFirstChildWithName(new QName(
                        APIConstants.API_GATEWAY_USERNAME)).getText()));
        OMElement passwordElement = environmentElem.getFirstChildWithName(new QName(APIConstants.API_GATEWAY_PASSWORD));
        String resolvedPassword = MiscellaneousUtil.resolve(passwordElement, secretResolver);
        environment.setPassword(APIUtil.replaceSystemProperty(resolvedPassword));
        String provider = environmentElem.getFirstChildWithName(new QName(APIConstants.API_GATEWAY_PROVIDER)).getText();
        if (StringUtils.isNotEmpty(provider)) {
            environment.setProvider(APIUtil.replaceSystemProperty(provider));
        } else {
            environment.setProvider(APIUtil.replaceSystemProperty(DEFAULT_PROVIDER));
        }
        environment.setApiGatewayEndpoint(APIUtil.replaceSystemProperty(environmentElem.getFirstChildWithName(new QName(
                        APIConstants.API_GATEWAY_ENDPOINT)).getText()));
        OMElement websocketGatewayEndpoint = environmentElem.getFirstChildWithName(new QName(
                APIConstants.API_WEBSOCKET_GATEWAY_ENDPOINT));
        if (websocketGatewayEndpoint != null) {
            environment.setWebsocketGatewayEndpoint(APIUtil.replaceSystemProperty(websocketGatewayEndpoint.getText()));
        } else {
            environment.setWebsocketGatewayEndpoint(WEBSOCKET_DEFAULT_GATEWAY_URL);
        }
        OMElement webSubGatewayEndpoint = environmentElem
                .getFirstChildWithName(new QName(APIConstants.API_WEBSUB_GATEWAY_ENDPOINT));
        if (webSubGatewayEndpoint != null) {
            environment.setWebSubGatewayEndpoint(APIUtil.replaceSystemProperty(webSubGatewayEndpoint.getText()));
        } else {
            environment.setWebSubGatewayEndpoint(WEBSUB_DEFAULT_GATEWAY_URL);
        }
        OMElement description =
                environmentElem.getFirstChildWithName(new QName("Description"));
        if (description != null) {
            environment.setDescription(description.getText());
        } else {
            environment.setDescription("");
        }
        environment.setReadOnly(true);
        List<VHost> vhosts = new LinkedList<>();
        environment.setVhosts(vhosts);
        environment.setEndpointsAsVhost();
        Iterator vhostIterator = environmentElem.getFirstChildWithName(new QName(
                APIConstants.API_GATEWAY_VIRTUAL_HOSTS)).getChildrenWithLocalName(
                APIConstants.API_GATEWAY_VIRTUAL_HOST);
        while (vhostIterator.hasNext()) {
            OMElement vhostElem = (OMElement) vhostIterator.next();
            String httpEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_HTTP_ENDPOINT)).getText());
            String httpsEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_HTTPS_ENDPOINT)).getText());
            String wsEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_WS_ENDPOINT)).getText());
            String wssEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_WSS_ENDPOINT)).getText());
            String webSubHttpEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_WEBSUB_HTTP_ENDPOINT)).getText());
            String webSubHttpsEp = APIUtil.replaceSystemProperty(vhostElem.getFirstChildWithName(new QName(
                    APIConstants.API_GATEWAY_VIRTUAL_HOST_WEBSUB_HTTPS_ENDPOINT)).getText());

            //Prefix websub endpoints with 'websub_' so that the endpoint URL
            // would begin with: 'websub_http://', since API type is identified by the URL protocol below.
            webSubHttpEp = StringUtils.isNotBlank(webSubHttpEp) ? "websub_" + webSubHttpEp : webSubHttpEp;
            webSubHttpsEp = StringUtils.isNotBlank(webSubHttpsEp) ? "websub_" + webSubHttpsEp : webSubHttpsEp;

            VHost vhost = VHost.fromEndpointUrls(new String[]{
                    httpEp, httpsEp, wsEp, wssEp, webSubHttpEp, webSubHttpsEp});
            vhosts.add(vhost);
        }
        OMElement properties = environmentElem.getFirstChildWithName(new
                QName(APIConstants.API_GATEWAY_ADDITIONAL_PROPERTIES));
        Map<String, String> additionalProperties = new HashMap<>();
        if (properties != null) {
            Iterator gatewayAdditionalProperties = properties.getChildrenWithLocalName
                    (APIConstants.API_GATEWAY_ADDITIONAL_PROPERTY);
            while (gatewayAdditionalProperties.hasNext()) {
                OMElement propertyElem = (OMElement) gatewayAdditionalProperties.next();
                String propName = propertyElem.getAttributeValue(new QName("name"));
                String resolvedValue = MiscellaneousUtil.resolve(propertyElem, secretResolver);
                additionalProperties.put(propName, resolvedValue);
            }
        }
        environment.setAdditionalProperties(additionalProperties);

        if (!apiGatewayEnvironments.containsKey(environment.getName())) {
            apiGatewayEnvironments.put(environment.getName(), environment);
        } else {

            //This will happen only on server startup therefore we log and continue the startup
            log.error("Duplicate environment name found in api-manager.xml " +
                    environment.getName());
        }

    }

    private void setSkipListConfigurations(OMElement element) {

        OMElement skippedApis =
                element.getFirstChildWithName(new QName(APIConstants.SkipListConstants.SKIPPED_APIS));
        if (skippedApis != null) {
            Iterator apiIterator =
                    skippedApis.getChildrenWithLocalName(APIConstants.SkipListConstants.SKIPPED_API);
            if (apiIterator != null) {
                while (apiIterator.hasNext()) {
                    OMElement apiNode = (OMElement) apiIterator.next();
                    gatewayCleanupSkipList.getApis().add(apiNode.getText());
                }
            }
        }
        OMElement skippedEndpoints =
                element.getFirstChildWithName(new QName(APIConstants.SkipListConstants.SKIPPED_ENDPOINTS));
        if (skippedEndpoints != null) {
            Iterator endpoints =
                    skippedEndpoints.getChildrenWithLocalName(APIConstants.SkipListConstants.SKIPPED_ENDPOINT);
            if (endpoints != null) {
                while (endpoints.hasNext()) {
                    OMElement endpointNode = (OMElement) endpoints.next();
                    gatewayCleanupSkipList.getEndpoints().add(endpointNode.getText());
                }
            }
        }
        OMElement skippedSequences =
                element.getFirstChildWithName(new QName(APIConstants.SkipListConstants.SKIPPED_SEQUENCES));
        if (skippedEndpoints != null) {
            Iterator sequences =
                    skippedSequences.getChildrenWithLocalName(APIConstants.SkipListConstants.SKIPPED_SEQUENCE);
            if (sequences != null) {
                while (sequences.hasNext()) {
                    OMElement sequenceNode = (OMElement) sequences.next();
                    gatewayCleanupSkipList.getSequences().add(sequenceNode.getText());
                }
            }
        }
        OMElement skippedLocalEntries =
                element.getFirstChildWithName(new QName(APIConstants.SkipListConstants.SKIPPED_LOCAL_ENTRIES));
        if (skippedEndpoints != null) {
            Iterator localEntries =
                    skippedLocalEntries.getChildrenWithLocalName(APIConstants.SkipListConstants.SKIPPED_LOCAL_ENTRY);
            if (localEntries != null) {
                while (localEntries.hasNext()) {
                    OMElement localEntryNode = (OMElement) localEntries.next();
                    gatewayCleanupSkipList.getLocalEntries().add(localEntryNode.getText());
                }
            }
        }
    }

    private void setGlobalCacheInvalidationConfiguration(OMElement element) {

        CacheInvalidationConfiguration cacheInvalidationConfiguration = new CacheInvalidationConfiguration();
        OMElement enabledElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.ENABLED));
        if (enabledElement != null) {
            cacheInvalidationConfiguration.setEnabled(Boolean.parseBoolean(enabledElement.getText()));
        }
        OMElement domainElement = element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.Domain));
        if (domainElement != null) {
            cacheInvalidationConfiguration.setDomain(domainElement.getText());
        }
        OMElement streamNameElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.Stream));
        if (streamNameElement != null) {
            cacheInvalidationConfiguration.setStream(streamNameElement.getText());
        }
        OMElement usernameElement = element.getFirstChildWithName
                (new QName(APIConstants.GlobalCacheInvalidation.USERNAME));
        if (usernameElement != null) {
            cacheInvalidationConfiguration.setUsername(APIUtil.replaceSystemProperty(usernameElement.getText()));
        }
        String password;
        OMElement passwordElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.PASSWORD));
        if (passwordElement != null) {
            password = MiscellaneousUtil.resolve(passwordElement, secretResolver);
            cacheInvalidationConfiguration.setPassword(APIUtil.replaceSystemProperty(password));
        }
        OMElement receiverUrlGroupElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.REVEIVER_URL_GROUP));
        if (receiverUrlGroupElement != null) {
            cacheInvalidationConfiguration
                    .setReceiverUrlGroup(APIUtil.replaceSystemProperty(receiverUrlGroupElement.getText()));
        }
        OMElement authUrlGroupElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.AUTH_URL_GROUP));
        if (authUrlGroupElement != null) {
            cacheInvalidationConfiguration
                    .setAuthUrlGroup(APIUtil.replaceSystemProperty(authUrlGroupElement.getText()));
        }
        OMElement receiverConnectionDetailsElement =
                element.getFirstChildWithName(
                        new QName(APIConstants.GlobalCacheInvalidation.ReceiverConnectionDetails));
        if (receiverConnectionDetailsElement != null) {
            Iterator receiverConnectionDetailsElements = receiverConnectionDetailsElement.getChildElements();
            Properties properties = new Properties();
            while (receiverConnectionDetailsElements.hasNext()) {
                OMElement omElement = (OMElement) receiverConnectionDetailsElements.next();
                String value = MiscellaneousUtil.resolve(omElement, secretResolver);
                properties.put(omElement.getLocalName(), APIUtil.replaceSystemProperty(value));
            }
            cacheInvalidationConfiguration.setJmsConnectionParameters(properties);
        }
        OMElement topicNameElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.TOPIC_NAME));
        if (topicNameElement != null) {
            cacheInvalidationConfiguration.setCacheInValidationTopic(topicNameElement.getText());
        }
        OMElement excludedCachesElement =
                element.getFirstChildWithName(new QName(APIConstants.GlobalCacheInvalidation.EXCLUDED_CACHES));
        if (excludedCachesElement != null) {
            Iterator excludedCaches = excludedCachesElement.getChildElements();
            while (excludedCaches.hasNext()) {
                cacheInvalidationConfiguration.addExcludedCaches(((OMElement) excludedCaches.next()).getText());
            }
        }
        this.cacheInvalidationConfiguration = cacheInvalidationConfiguration;
    }

    public CacheInvalidationConfiguration getCacheInvalidationConfiguration() {

        return cacheInvalidationConfiguration;
    }

    public JSONArray getApplicationAttributes() {

        return applicationAttributes;
    }

    public JSONArray getCustomProperties() {

        return customProperties;
    }

    /**
     * Read the primary/secondary login configuration
     * <LoginConfig>
     * <UserIdLogin  primary="true">
     * <ClaimUri></ClaimUri>
     * </UserIdLogin>
     * <EmailLogin  primary="false">
     * <ClaimUri>http://wso2.org/claims/emailaddress</ClaimUri>
     * </EmailLogin>           loginOMElement
     * </LoginConfig>
     *
     * @param loginConfigElem
     */
    private void parseLoginConfig(OMElement loginConfigElem) {

        if (loginConfigElem != null) {
            if (log.isDebugEnabled()) {
                log.debug("Login configuration is set ");
            }
            // Primary/Secondary supported login mechanisms
            OMElement emailConfigElem = loginConfigElem.getFirstChildWithName(new QName(EMAIL_LOGIN));

            OMElement userIdConfigElem = loginConfigElem.getFirstChildWithName(new QName(USERID_LOGIN));

            Map<String, String> emailConf = new HashMap<String, String>(2);
            emailConf.put(PRIMARY_LOGIN, emailConfigElem.getAttributeValue(new QName(PRIMARY_LOGIN)));
            emailConf.put(CLAIM_URI, emailConfigElem.getFirstChildWithName(new QName(CLAIM_URI)).getText());

            Map<String, String> userIdConf = new HashMap<String, String>(2);
            userIdConf.put(PRIMARY_LOGIN, userIdConfigElem.getAttributeValue(new QName(PRIMARY_LOGIN)));
            userIdConf.put(CLAIM_URI, userIdConfigElem.getFirstChildWithName(new QName(CLAIM_URI)).getText());

            loginConfiguration.put(EMAIL_LOGIN, emailConf);
            loginConfiguration.put(USERID_LOGIN, userIdConf);
        }
    }

    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append('.');
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private void addToConfiguration(String key, String value) {

        List<String> list = configuration.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            configuration.put(key, list);
        } else {
            list.add(value);
        }
    }

    public Map<String, Environment> getApiGatewayEnvironments() {

        return apiGatewayEnvironments;
    }

    public EmbeddingProviderConfigurationDTO getEmbeddingProvider() {

        return embeddingProviderConfigurationDTO;
    }

    public VectorDBProviderConfigurationDTO getVectorDBProvider() {

        return vectorDBProviderConfigurationDTO;
    }

    public GuardrailProviderConfigurationDTO getGuardrailProvider(String type) {

        return guardrailProviders.get(type);
    }

    public TenantSharingConfigurationDTO getTenantSharingConfiguration(String type) {

        return tenantSharingConfigurations.get(type);
    }

    public RecommendationEnvironment getApiRecommendationEnvironment() {

        return recommendationEnvironment;
    }

    public Set<APIStore> getExternalAPIStores() {  //Return set of APIStores
        return externalAPIStores;
    }

    public APIStore getExternalAPIStore(
            String storeName) { //Return APIStore object,based on store name/Here we assume store name is unique.
        for (APIStore apiStore : externalAPIStores) {
            if (apiStore.getName().equals(storeName)) {
                return apiStore;
            }
        }
        return null;
    }

    /**
     * set the hostname and the port as System properties.
     * return void
     */
    private void addKeyManagerConfigsAsSystemProperties(String serviceUrl) {

        URL keyManagerURL;
        try {
            keyManagerURL = new URL(serviceUrl);
            String hostname = keyManagerURL.getHost();

            int port = keyManagerURL.getPort();
            if (port == -1) {
                if (APIConstants.HTTPS_PROTOCOL.equals(keyManagerURL.getProtocol())) {
                    port = APIConstants.HTTPS_PROTOCOL_PORT;
                } else {
                    port = APIConstants.HTTP_PROTOCOL_PORT;
                }
            }
            System.setProperty(APIConstants.KEYMANAGER_PORT, String.valueOf(port));

            if (hostname.equals(System.getProperty(APIConstants.CARBON_LOCALIP))) {
                System.setProperty(APIConstants.KEYMANAGER_HOSTNAME, "localhost");
            } else {
                System.setProperty(APIConstants.KEYMANAGER_HOSTNAME, hostname);
            }
            //Since this is the server startup.Ignore the exceptions,invoked at the server startup
        } catch (MalformedURLException e) {
            log.error("Exception While resolving KeyManager Server URL or Port " + e.getMessage(), e);
        }
    }

    /**
     * set workflow related configurations
     *
     * @param element
     */
    private void setWorkflowProperties(OMElement element) {

        OMElement workflowConfigurationElement = element
                .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW));
        if (workflowConfigurationElement != null) {
            OMElement enableWorkflowElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_ENABLED));
            if (enableWorkflowElement != null) {
                workflowProperties.setEnabled(JavaUtils.isTrueExplicitly(enableWorkflowElement.getText()));
            }
            OMElement workflowServerUrlElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_SERVER_URL));
            if (workflowServerUrlElement != null) {
                workflowProperties.setServerUrl(APIUtil.replaceSystemProperty(workflowServerUrlElement.getText()));
            }
            OMElement workflowServerUserElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_SERVER_USER));
            if (workflowServerUserElement != null) {
                workflowProperties.setServerUser(APIUtil.replaceSystemProperty(workflowServerUserElement.getText()));
            }
            OMElement workflowDCRUserElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_DCR_EP_USER));
            if (workflowDCRUserElement != null) {
                workflowProperties.setdCREndpointUser(APIUtil.replaceSystemProperty(workflowDCRUserElement.getText()));
            }
            OMElement workflowCallbackElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_CALLBACK));
            if (workflowCallbackElement != null) {
                workflowProperties
                        .setWorkflowCallbackAPI(APIUtil.replaceSystemProperty(workflowCallbackElement.getText()));
            }

            OMElement workflowDCREPElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_DCR_EP));
            if (workflowDCREPElement != null) {
                workflowProperties.setdCREndPoint(APIUtil.replaceSystemProperty(workflowDCREPElement.getText()));
            }

            OMElement workflowTokenEpElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_TOKEN_EP));
            if (workflowTokenEpElement != null) {
                workflowProperties.setTokenEndPoint(APIUtil.replaceSystemProperty(workflowTokenEpElement.getText()));
            }
            OMElement workflowServerPasswordOmElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_SERVER_PASSWORD));
            String workflowServerPassword = MiscellaneousUtil.resolve(workflowServerPasswordOmElement, secretResolver);
            workflowServerPassword = APIUtil.replaceSystemProperty(workflowServerPassword);
            workflowProperties.setServerPassword(workflowServerPassword);

            OMElement dcrEPPasswordOmElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.WORKFLOW_DCR_EP_PASSWORD));
            String dcrEPPassword = MiscellaneousUtil.resolve(dcrEPPasswordOmElement, secretResolver);
            dcrEPPassword = APIUtil.replaceSystemProperty(dcrEPPassword);
            workflowProperties.setdCREndpointPassword(dcrEPPassword);

            OMElement listTasksElement = workflowConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.WorkflowConfigConstants.LIST_PENDING_TASKS));
            if (listTasksElement != null) {
                workflowProperties.setListTasks(JavaUtils.isTrueExplicitly(listTasksElement.getText()));
            }
            OMElement propertiesElement = workflowConfigurationElement.getFirstChildWithName(new QName("Properties"));
            if (propertiesElement != null) {
                Iterator<OMElement> properties = propertiesElement.getChildElements();
                if (properties != null) {
                    Properties workflowConfigProperties = new Properties();
                    while (properties.hasNext()) {
                        OMElement property = (OMElement) properties.next();
                        String name = property.getAttributeValue(new QName("name"));
                        String value = property.getText();
                        workflowConfigProperties.put(name, value);
                    }
                    workflowProperties.setProperties(workflowConfigProperties);
                }
            }

        }
    }

    /**
     * set the Advance Throttle Properties into Configuration
     *
     * @param element
     */
    private void setThrottleProperties(OMElement element) {

        OMElement throttleConfigurationElement = element.getFirstChildWithName(new QName(APIConstants
                .AdvancedThrottleConstants.THROTTLING_CONFIGURATIONS));
        if (throttleConfigurationElement != null) {
            // Check unlimited tier enabled
            OMElement enableUnlimitedTierElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_UNLIMITED_TIER));
            if (enableUnlimitedTierElement != null) {
                throttleProperties.setEnableUnlimitedTier(JavaUtils.isTrueExplicitly(enableUnlimitedTierElement
                        .getText()));
            }
            // Check header condition enable
            OMElement enableHeaderConditionsElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_HEADER_CONDITIONS));
            if (enableHeaderConditionsElement != null) {
                throttleProperties.setEnableHeaderConditions(JavaUtils.isTrueExplicitly(enableHeaderConditionsElement
                        .getText()));
            }
            // Check JWT condition enable
            OMElement enableJwtElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_JWT_CLAIM_CONDITIONS));
            if (enableJwtElement != null) {
                throttleProperties.setEnableJwtConditions(JavaUtils.isTrueExplicitly(enableJwtElement
                        .getText()));
            }
            // Check query param condition enable
            OMElement enableQueryParamElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_QUERY_PARAM_CONDITIONS));
            if (enableQueryParamElement != null) {
                throttleProperties.setEnableQueryParamConditions(JavaUtils.isTrueExplicitly(enableQueryParamElement
                        .getText()));
            }
            // Check skip redeploy throttle policies
            OMElement skipRedeployingPoliciesElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .SKIP_REDEPLOYING_POLICIES));
            if (skipRedeployingPoliciesElement != null) {
                throttleProperties.setSkipRedeployingPolicies(skipRedeployingPoliciesElement
                        .getText().split(APIConstants.DELEM_COMMA));
            }
            // Check skip deploy throttle policies
            OMElement skipDeployingPoliciesElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .SKIP_DEPLOYING_POLICIES));
            if (skipDeployingPoliciesElement != null) {
                throttleProperties.setSkipDeployingPolicies(
                        Arrays.asList(skipDeployingPoliciesElement.getText().split(APIConstants.DELEM_COMMA)));
                if (log.isDebugEnabled()) {
                    if (throttleProperties.getSkipDeployingPolicies() != null) {
                        log.debug("Skip deploying throttle policies: " + throttleProperties.getSkipDeployingPolicies());
                    }
                }
            }
            OMElement enablePolicyDeployElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLE_POLICY_DEPLOYMENT));
            if (enablePolicyDeployElement != null) {
                throttleProperties.setEnablePolicyDeployment(Boolean.parseBoolean(enablePolicyDeployElement.getText()));
            }
            OMElement enablePolicyRecreateElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLE_POLICY_RECREATE));
            if (enablePolicyRecreateElement != null) {
                throttleProperties.setEnablePolicyRecreate(Boolean.parseBoolean(enablePolicyRecreateElement.getText()));
            }
            // Check subscription spike arrest enable
            OMElement enabledSubscriptionLevelSpikeArrestElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_SUBSCRIPTION_SPIKE_ARREST));
            if (enabledSubscriptionLevelSpikeArrestElement != null) {
                throttleProperties.setEnabledSubscriptionLevelSpikeArrest(JavaUtils.isTrueExplicitly
                        (enabledSubscriptionLevelSpikeArrestElement
                                .getText()));
            }
                // Reading TrafficManager configuration
                OMElement trafficManagerConfigurationElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.TRAFFIC_MANAGER));
                ThrottleProperties.TrafficManager trafficManager = new ThrottleProperties.TrafficManager();
                if (trafficManagerConfigurationElement != null) {
                    OMElement receiverUrlGroupElement = trafficManagerConfigurationElement.getFirstChildWithName(new
                            QName
                            (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP));
                    if (receiverUrlGroupElement != null) {
                        trafficManager.setReceiverUrlGroup(APIUtil.replaceSystemProperty(receiverUrlGroupElement
                                .getText()));
                    }
                    OMElement authUrlGroupElement = trafficManagerConfigurationElement.getFirstChildWithName(new QName
                            (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP));
                    if (authUrlGroupElement != null) {
                        trafficManager.setAuthUrlGroup(APIUtil.replaceSystemProperty(authUrlGroupElement.getText()));
                    }
                    OMElement dataPublisherUsernameElement = trafficManagerConfigurationElement.getFirstChildWithName
                            (new QName(APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (dataPublisherUsernameElement != null) {
                        trafficManager.setUsername(APIUtil.replaceSystemProperty(dataPublisherUsernameElement.getText
                                ()));
                    }
                    OMElement dataPublisherTypeElement = trafficManagerConfigurationElement.getFirstChildWithName(new
                            QName
                            (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_TYPE));
                    if (dataPublisherTypeElement != null) {
                        trafficManager.setType(dataPublisherTypeElement.getText());
                    }
                    String dataPublisherConfigurationPassword;
                    OMElement dataPublisherConfigurationPasswordOmElement = trafficManagerConfigurationElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.PASSWORD));
                    dataPublisherConfigurationPassword = MiscellaneousUtil.
                            resolve(dataPublisherConfigurationPasswordOmElement, secretResolver);
                    trafficManager.setPassword(APIUtil.replaceSystemProperty(dataPublisherConfigurationPassword));
                    throttleProperties.setTrafficManager(trafficManager);
                }
                // Configuring throttle data publisher
                ThrottleProperties.DataPublisher dataPublisher = new ThrottleProperties.DataPublisher();
                OMElement dataPublisherConfigurationElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURATION));
                if (dataPublisherConfigurationElement != null) {
                    OMElement dataPublisherEnabledElement = dataPublisherConfigurationElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLED));
                    dataPublisher.setEnabled(JavaUtils.isTrueExplicitly(dataPublisherEnabledElement.getText()));
                    dataPublisher.setAuthUrlGroup(trafficManager.getAuthUrlGroup());
                    dataPublisher.setReceiverUrlGroup(trafficManager.getReceiverUrlGroup());
                    dataPublisher.setUsername(trafficManager.getUsername());
                    dataPublisher.setPassword(trafficManager.getPassword());
                    dataPublisher.setType(trafficManager.getType());
                }
                if (dataPublisher.isEnabled()) {

                    throttleProperties.setDataPublisher(dataPublisher);

                    // Data publisher pool configuration

                    OMElement dataPublisherPoolConfigurationElement = dataPublisherConfigurationElement
                            .getFirstChildWithName(new
                                    QName
                                    (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_POOL_CONFIGURATION));

                    ThrottleProperties.DataPublisherPool dataPublisherPool = new ThrottleProperties
                            .DataPublisherPool();
                    OMElement maxIdleElement = dataPublisherPoolConfigurationElement.getFirstChildWithName(new QName
                            (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_POOL_CONFIGURAION_MAX_IDLE));
                    if (maxIdleElement != null) {
                        dataPublisherPool.setMaxIdle(Integer.parseInt(maxIdleElement.getText()));
                    }
                    OMElement initIdleElement = dataPublisherPoolConfigurationElement.getFirstChildWithName(new QName
                            (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_POOL_CONFIGURAION_INIT_IDLE));
                    if (initIdleElement != null) {
                        dataPublisherPool.setInitIdleCapacity(Integer.parseInt(initIdleElement.getText()));
                    }
                    throttleProperties.setDataPublisherPool(dataPublisherPool);

                    // Data publisher thread pool configuration

                    OMElement dataPublisherThreadPoolConfigurationElement = dataPublisherConfigurationElement
                            .getFirstChildWithName(new
                                    QName
                                    (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_THREAD_POOL_CONFIGURATION));

                    ThrottleProperties.DataPublisherThreadPool dataPublisherThreadPool = new ThrottleProperties
                            .DataPublisherThreadPool();
                    if (dataPublisherThreadPoolConfigurationElement != null) {
                        OMElement corePoolSizeElement = dataPublisherThreadPoolConfigurationElement
                                .getFirstChildWithName
                                        (new
                                                QName
                                                (APIConstants.AdvancedThrottleConstants
                                                        .DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_CORE_POOL_SIZE));
                        if (corePoolSizeElement != null) {
                            dataPublisherThreadPool.setCorePoolSize(Integer.parseInt(corePoolSizeElement.getText()));
                        }
                        OMElement maximumPoolSizeElement = dataPublisherThreadPoolConfigurationElement
                                .getFirstChildWithName(new
                                        QName
                                        (APIConstants.AdvancedThrottleConstants
                                                .DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_MAXMIMUM_POOL_SIZE));
                        if (maximumPoolSizeElement != null) {
                            dataPublisherThreadPool.setMaximumPoolSize(Integer.parseInt(maximumPoolSizeElement
                                    .getText()));
                        }
                        OMElement keepAliveTimeElement = dataPublisherThreadPoolConfigurationElement
                                .getFirstChildWithName
                                        (new
                                                QName
                                                (APIConstants.AdvancedThrottleConstants
                                                        .DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_KEEP_ALIVE_TIME));
                        if (keepAliveTimeElement != null) {
                            dataPublisherThreadPool.setKeepAliveTime(Long.parseLong(keepAliveTimeElement.getText()));
                        }
                    }
                    throttleProperties.setDataPublisherThreadPool(dataPublisherThreadPool);
                }

                // Configuring JMSConnectionDetails
                ThrottleProperties.JMSConnectionProperties jmsConnectionProperties = new
                        ThrottleProperties
                                .JMSConnectionProperties();

                OMElement jmsConnectionDetailElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.JMS_CONNECTION_DETAILS));

                if (jmsConnectionDetailElement != null) {
                    OMElement jmsConnectionEnabledElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLED));
                    jmsConnectionProperties.setEnabled(JavaUtils.isTrueExplicitly(jmsConnectionEnabledElement
                            .getText()));
                    OMElement jmsConnectionUrlElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (jmsConnectionUrlElement != null) {
                        jmsConnectionProperties.setServiceUrl(APIUtil.replaceSystemProperty(jmsConnectionUrlElement
                                .getText()));
                        System.setProperty("jms.url", jmsConnectionProperties.getServiceUrl());
                    }
                    OMElement jmsConnectionUserElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (jmsConnectionUserElement != null) {
                        jmsConnectionProperties.setUsername(APIUtil.replaceSystemProperty(jmsConnectionUserElement
                                .getText()));
                        System.setProperty("jms.username", jmsConnectionProperties.getUsername());
                    }
                    OMElement jmsConnectionPasswordElement = jmsConnectionDetailElement.getFirstChildWithName(new
                            QName(APIConstants.AdvancedThrottleConstants.PASSWORD));
                    if (jmsConnectionPasswordElement != null) {
                        OMElement jmsConnectionPasswordOmElement = jmsConnectionDetailElement
                                .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.PASSWORD));
                        String jmsConnectionPassword = MiscellaneousUtil.
                                resolve(jmsConnectionPasswordOmElement, secretResolver);
                        jmsConnectionProperties.setPassword(APIUtil.replaceSystemProperty(jmsConnectionPassword));
                        System.setProperty("jms.password", jmsConnectionProperties.getPassword());
                    }

                    OMElement jmsConnectionParameterElement = jmsConnectionDetailElement.getFirstChildWithName(new
                            QName(APIConstants.AdvancedThrottleConstants.JMS_CONNECTION_PARAMETERS));
                    if (jmsConnectionParameterElement != null) {
                        Iterator jmsProperties = jmsConnectionParameterElement.getChildElements();
                        Properties properties = new Properties();
                        while (jmsProperties.hasNext()) {
                            OMElement property = (OMElement) jmsProperties.next();
                            String value = MiscellaneousUtil.resolve(property, secretResolver);
                            properties.put(property.getLocalName(), APIUtil.replaceSystemProperty(value));
                        }
                        jmsConnectionProperties.setJmsConnectionProperties(properties);
                    }
                    // Configuring JMS Task Manager
                    ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties jmsTaskManagerProperties =
                            new ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties();
                    OMElement jmsTaskManagerElement = jmsConnectionDetailElement.getFirstChildWithName
                            (new QName(APIConstants.AdvancedThrottleConstants.JMS_TASK_MANAGER));
                    if (jmsTaskManagerElement != null) {
                        OMElement minThreadPoolSizeElement = jmsTaskManagerElement
                                .getFirstChildWithName(new QName
                                        (APIConstants.AdvancedThrottleConstants.MIN_THREAD_POOL_SIZE));
                        if (minThreadPoolSizeElement != null) {
                            jmsTaskManagerProperties.setMinThreadPoolSize(Integer.parseInt(minThreadPoolSizeElement
                                    .getText()));
                        }
                        OMElement maxThreadPoolSizeElement = jmsTaskManagerElement
                                .getFirstChildWithName(new QName
                                        (APIConstants.AdvancedThrottleConstants.MAX_THREAD_POOL_SIZE));
                        if (maxThreadPoolSizeElement != null) {
                            jmsTaskManagerProperties.setMaxThreadPoolSize(Integer.parseInt(maxThreadPoolSizeElement
                                    .getText()));
                        }
                        OMElement keepAliveTimeInMillisElement = jmsTaskManagerElement
                                .getFirstChildWithName(new QName
                                        (APIConstants.AdvancedThrottleConstants.KEEP_ALIVE_TIME_IN_MILLIS));
                        if (keepAliveTimeInMillisElement != null) {
                            jmsTaskManagerProperties.setKeepAliveTimeInMillis(Integer.parseInt
                                    (keepAliveTimeInMillisElement.getText()));
                        }
                        OMElement jobQueueSizeElement = jmsTaskManagerElement
                                .getFirstChildWithName(new QName
                                        (APIConstants.AdvancedThrottleConstants.JOB_QUEUE_SIZE));
                        if (jobQueueSizeElement != null) {
                            jmsTaskManagerProperties.setJobQueueSize(Integer.parseInt(jobQueueSizeElement.getText()));
                        }
                    }
                    jmsConnectionProperties.setJmsTaskManagerProperties(jmsTaskManagerProperties);
                    OMElement jmsConnectionInitialDelayElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .BLOCK_CONDITION_RETRIEVER_INIT_DELAY));
                    if (jmsConnectionInitialDelayElement != null) {
                        jmsConnectionProperties.setInitialDelay(Long.parseLong
                                (jmsConnectionInitialDelayElement
                                        .getText()));
                    }
                }
                throttleProperties.setJmsConnectionProperties(jmsConnectionProperties);

                //Configuring default tier limits
                Map<String, Long> defaultThrottleTierLimits = new HashMap<String, Long>();
                OMElement defaultTierLimits = throttleConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.DEFAULT_THROTTLE_LIMITS));

                if (defaultTierLimits != null) {
                    OMElement subscriptionPolicyLimits = defaultTierLimits
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .SUBSCRIPTION_THROTTLE_LIMITS));

                    if (subscriptionPolicyLimits != null) {
                        OMElement goldTierElement = subscriptionPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_SUB_POLICY_GOLD));
                        if (goldTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_SUB_POLICY_GOLD,
                                    Long.parseLong(goldTierElement.getText()));
                        }

                        OMElement silverTierElement = subscriptionPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_SUB_POLICY_SILVER));
                        if (silverTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_SUB_POLICY_SILVER,
                                    Long.parseLong(silverTierElement.getText()));
                        }

                        OMElement bronzeTierElement = subscriptionPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_SUB_POLICY_BRONZE));
                        if (bronzeTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_SUB_POLICY_BRONZE,
                                    Long.parseLong(bronzeTierElement.getText()));
                        }

                        OMElement unauthenticatedTierElement = subscriptionPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED));
                        if (unauthenticatedTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED,
                                    Long.parseLong(unauthenticatedTierElement.getText()));
                        }

                        OMElement subscriptionlessTierElement = subscriptionPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS));
                        if (subscriptionlessTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS,
                                    Long.parseLong(subscriptionlessTierElement.getText()));
                        }
                    }

                    OMElement applicationPolicyLimits = defaultTierLimits
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .APPLICATION_THROTTLE_LIMITS));
                    if (subscriptionPolicyLimits != null) {
                        OMElement largeTierElement = applicationPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN));
                        if (largeTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                                    Long.parseLong(largeTierElement.getText()));
                        }

                        OMElement mediumTierElement = applicationPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN));
                        if (mediumTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                                    Long.parseLong(mediumTierElement.getText()));
                        }

                        OMElement smallTierElement = applicationPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN));
                        if (smallTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN,
                                    Long.parseLong(smallTierElement.getText()));
                        }
                    }

                    OMElement resourceLevelPolicyLimits = defaultTierLimits
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .RESOURCE_THROTTLE_LIMITS));
                    if (resourceLevelPolicyLimits != null) {
                        OMElement ultimateTierElement = resourceLevelPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN));
                        if (ultimateTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                                    Long.parseLong(ultimateTierElement.getText()));
                        }

                        OMElement plusTierElement = resourceLevelPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN));
                        if (plusTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                                    Long.parseLong(plusTierElement.getText()));
                        }

                        OMElement basicTierElement = resourceLevelPolicyLimits.getFirstChildWithName(new
                                QName(APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN));
                        if (basicTierElement != null) {
                            defaultThrottleTierLimits.put(APIConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN,
                                    Long.parseLong(basicTierElement.getText()));
                        }
                    }

                }

                throttleProperties.setDefaultThrottleTierLimits(defaultThrottleTierLimits);

                //Configuring policy deployer
                OMElement policyDeployerConnectionElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.POLICY_DEPLOYER_CONFIGURATION));

                ThrottleProperties.PolicyDeployer policyDeployerConfiguration = new
                        ThrottleProperties
                                .PolicyDeployer();
            if (policyDeployerConnectionElement != null) {
                OMElement policyDeployerConnectionEnabledElement = policyDeployerConnectionElement.getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLED));
                policyDeployerConfiguration.setEnabled(JavaUtils.isTrueExplicitly(policyDeployerConnectionEnabledElement.getText()));
                OMElement policyDeployerServiceUrlElement = policyDeployerConnectionElement.getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                if (policyDeployerServiceUrlElement != null) {
                    policyDeployerConfiguration.setServiceUrl(APIUtil.replaceSystemProperty(policyDeployerServiceUrlElement.getText()));
                }
                OMElement policyDeployerServiceServiceUsernameElement = policyDeployerConnectionElement.getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.USERNAME));
                if (policyDeployerServiceServiceUsernameElement != null) {
                    policyDeployerConfiguration.setUsername(APIUtil.replaceSystemProperty(policyDeployerServiceServiceUsernameElement.getText()));
                }
                OMElement policyDeployerServicePasswordElement = policyDeployerConnectionElement.getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.PASSWORD));
                String policyDeployerServicePassword = MiscellaneousUtil.resolve(policyDeployerServicePasswordElement, secretResolver);
                policyDeployerConfiguration.setPassword(APIUtil.replaceSystemProperty(policyDeployerServicePassword));
                OMElement tenantLoadingOmelement = policyDeployerConnectionElement.getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.TENANT_LOADING));
                if (tenantLoadingOmelement != null) {
                    OMElement enableTenantLoading = tenantLoadingOmelement.getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.ENABLE_TENANT_LOADING));
                    if (enableTenantLoading != null) {
                        policyDeployerConfiguration.setTenantLoading(Boolean.parseBoolean(enableTenantLoading.getText()));
                    }
                    OMElement tenantSToLoadElement = tenantLoadingOmelement.getFirstChildWithName(
                            new QName(APIConstants.GatewayArtifactSynchronizer.TENANT_LOADING_TENANTS));
                    if (tenantSToLoadElement != null && StringUtils.isNotEmpty(tenantSToLoadElement.getText())) {
                        String[] tenantsToLoad = tenantSToLoadElement.getText().split(",");
                        LoadingTenants loadingTenants = new LoadingTenants();
                        for (String tenant : tenantsToLoad) {
                            tenant = tenant.trim();
                            if (tenant.equals("*")) {
                                loadingTenants.setIncludeAllTenants(true);
                            } else if (tenant.contains("!")) {
                                if (tenant.contains("*")) {
                                    throw new IllegalArgumentException(tenant + " is not a valid tenant domain");
                                }
                                loadingTenants.getExcludingTenants().add(tenant.replace("!", ""));
                            } else {
                                loadingTenants.getIncludingTenants().add(tenant);
                            }
                        }
                        if (loadingTenants.isIncludeAllTenants()) {
                            loadingTenants.getIncludingTenants().clear();
                        }
                        loadingTenants.getIncludingTenants().removeAll(loadingTenants.getExcludingTenants());
                        policyDeployerConfiguration.setLoadingTenants(loadingTenants);
                    }
                }
            }
                throttleProperties.setPolicyDeployer(policyDeployerConfiguration);

                //Configuring Block Condition retriever configuration
                OMElement blockConditionRetrieverElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.BLOCK_CONDITION_RETRIEVER_CONFIGURATION));

                ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = new ThrottleProperties
                        .BlockCondition();
                if (blockConditionRetrieverElement != null) {
                    OMElement blockingConditionEnabledElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.ENABLED));
                    blockConditionRetrieverConfiguration.setEnabled(JavaUtils.isTrueExplicitly
                            (blockingConditionEnabledElement.getText()));
                    OMElement blockConditionRetrieverServiceUrlElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (blockConditionRetrieverServiceUrlElement != null) {
                        blockConditionRetrieverConfiguration.setServiceUrl(APIUtil
                                .replaceSystemProperty(blockConditionRetrieverServiceUrlElement
                                        .getText()));
                    } else {
                        String serviceUrl = "https://" + System.getProperty(APIConstants.KEYMANAGER_HOSTNAME) + ":" +
                                System.getProperty(APIConstants.KEYMANAGER_PORT) + APIConstants.INTERNAL_WEB_APP_EP;
                        blockConditionRetrieverConfiguration.setServiceUrl(serviceUrl);
                    }

                    blockConditionRetrieverConfiguration.setUsername(getFirstProperty(APIConstants
                            .API_KEY_VALIDATOR_USERNAME));
                    OMElement blockConditionRetrieverThreadPoolSizeElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .MAX_THREAD_POOL_SIZE));
                    if (blockConditionRetrieverThreadPoolSizeElement != null) {

                        blockConditionRetrieverConfiguration.setCorePoolSize
                                (Integer.parseInt(blockConditionRetrieverThreadPoolSizeElement.getText()));
                    }
                    OMElement blockConditionRetrieverInitIdleElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .BLOCK_CONDITION_RETRIEVER_INIT_DELAY));
                    if (blockConditionRetrieverInitIdleElement != null) {
                        blockConditionRetrieverConfiguration.setInitDelay(Long.parseLong
                                (blockConditionRetrieverInitIdleElement
                                        .getText()));
                    }
                    OMElement blockConditionRetrieverTimeIntervalElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                                    .BLOCK_CONDITION_RETRIEVER_PERIOD));
                    if (blockConditionRetrieverTimeIntervalElement != null) {
                        blockConditionRetrieverConfiguration.setPeriod(Long.parseLong
                                (blockConditionRetrieverTimeIntervalElement
                                        .getText()));
                    }
                    blockConditionRetrieverConfiguration.setPassword(getFirstProperty(APIConstants
                            .API_KEY_VALIDATOR_PASSWORD));
                }
                throttleProperties.setBlockCondition(blockConditionRetrieverConfiguration);

        }
    }

    private void setJWTConfiguration(OMElement omElement) {

        OMElement jwtEnableElement =
                omElement.getFirstChildWithName(new QName(APIConstants.ENABLE_JWT_GENERATION));
        if (jwtEnableElement != null) {
            jwtConfigurationDto.setEnabled(Boolean.parseBoolean(jwtEnableElement.getText()));
        }
        if (jwtConfigurationDto.isEnabled()) {
            OMElement jwtGeneratorImplElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.TOKEN_GENERATOR_IMPL));
            if (jwtGeneratorImplElement != null) {
                jwtConfigurationDto.setJwtGeneratorImplClass(jwtGeneratorImplElement.getText());
            }
            OMElement dialectUriElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.CONSUMER_DIALECT_URI));
            if (dialectUriElement != null) {
                jwtConfigurationDto.setConsumerDialectUri(dialectUriElement.getText());
            }
            OMElement signatureElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.JWT_SIGNATURE_ALGORITHM));
            if (signatureElement != null) {
                jwtConfigurationDto.setSignatureAlgorithm(signatureElement.getText());
            }
            OMElement useSHA256HashElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.USE_SHA256_HASH));
            if (useSHA256HashElement != null) {
                jwtConfigurationDto.setUseSHA256Hash(Boolean.parseBoolean(useSHA256HashElement.getText()));
            }
            OMElement claimRetrieverImplElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.CLAIMS_RETRIEVER_CLASS));
            if (claimRetrieverImplElement != null) {
                jwtConfigurationDto.setClaimRetrieverImplClass(claimRetrieverImplElement.getText());
            }
            OMElement useKidElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.USE_KID));
            if (useKidElement != null) {
                jwtConfigurationDto.setUseKid(Boolean.parseBoolean(useKidElement.getText()));
            }
            OMElement jwtHeaderElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.JWT_HEADER));
            if (jwtHeaderElement != null) {
                jwtConfigurationDto.setJwtHeader(jwtHeaderElement.getText());
            }
            OMElement jwtDecoding =
                    omElement.getFirstChildWithName(new QName(APIConstants.JWT_DECODING));
            if (jwtDecoding != null) {
                jwtConfigurationDto.setJwtDecoding(jwtDecoding.getText());
            }
            OMElement jwtUserClaimsElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.ENABLE_USER_CLAIMS));
            if (jwtUserClaimsElement != null) {
                jwtConfigurationDto.setEnableUserClaims(Boolean.parseBoolean(jwtUserClaimsElement.getText()));
                OMElement isBindFederatedUserClaimsForOpaque =
                        omElement.getFirstChildWithName(new QName(APIConstants.BINDING_FEDERATED_USER_CLAIMS_FOR_OPAQUE));
                if (isBindFederatedUserClaimsForOpaque != null) {
                    boolean bindValue = Boolean.parseBoolean(isBindFederatedUserClaimsForOpaque.getText());
                    jwtConfigurationDto.setBindFederatedUserClaimsForOpaque(bindValue);
                    if (log.isDebugEnabled()) {
                        log.debug("BindFederatedUserClaimsForOpaque configuration element found. Value = " +
                                bindValue);
                    }
                } else {
                    jwtConfigurationDto.setBindFederatedUserClaimsForOpaque(false);
                    log.debug("BindFederatedUserClaimsForOpaque configuration element not found. " +
                            "Defaulting to false.");
                }
            }
            OMElement enableTenantBaseSigningElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.ENABLE_TENANT_BASE_SIGNING));
            if (enableTenantBaseSigningElement != null) {
                jwtConfigurationDto.
                        setTenantBasedSigningEnabled(Boolean.parseBoolean(enableTenantBaseSigningElement.getText()));
            }
            OMElement gatewayJWTConfigurationElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.GATEWAY_JWT_GENERATOR));
            if (gatewayJWTConfigurationElement != null) {
                OMElement gatewayJWTGeneratorImplElement = gatewayJWTConfigurationElement
                        .getFirstChildWithName(new QName(APIConstants.GATEWAY_JWT_GENERATOR_IMPL));
                jwtConfigurationDto.setGatewayJWTGeneratorImpl(gatewayJWTGeneratorImplElement.getText());
                OMElement configurationElement =
                        gatewayJWTConfigurationElement
                                .getFirstChildWithName(new QName(APIConstants.GATEWAY_JWT_CONFIGURATION));
                OMElement encodeX5tWithoutPaddingElement = gatewayJWTConfigurationElement
                        .getFirstChildWithName(new QName(APIConstants.ENCODE_X5T_WITHOUT_PADDING));
                if (encodeX5tWithoutPaddingElement != null) {
                    jwtConfigurationDto.setEncodeX5tWithoutPadding(Boolean.parseBoolean(
                            encodeX5tWithoutPaddingElement.getText()));
                }
                if (configurationElement != null) {
                    OMElement claimsElement =
                            configurationElement
                                    .getFirstChildWithName(new QName(APIConstants.GATEWAY_JWT_GENERATOR_CLAIMS));
                    if (claimsElement != null) {
                        Iterator claims =
                                claimsElement.getChildrenWithName(new QName(APIConstants.GATEWAY_JWT_GENERATOR_CLAIM));
                        if (claims != null) {
                            while (claims.hasNext()) {
                                OMElement claim = (OMElement) claims.next();
                                jwtConfigurationDto.getJWTExcludedClaims().add(claim.getText());
                            }
                        }
                    }
                    OMElement claimRetrievalElement =
                            configurationElement.getFirstChildWithName(new QName(APIConstants.ENABLE_USER_CLAIMS_RETRIEVAL_FROM_KEY_MANAGER));
                    if (claimRetrievalElement != null) {
                        jwtConfigurationDto.setEnableUserClaimRetrievalFromUserStore(Boolean.parseBoolean(claimRetrievalElement.getText()));
                        OMElement isBindFederatedUserClaims =
                                omElement.getFirstChildWithName(new QName(APIConstants.BINDING_FEDERATED_USER_CLAIMS));
                        if (isBindFederatedUserClaims != null) {
                            jwtConfigurationDto.setBindFederatedUserClaims(
                                    Boolean.parseBoolean(isBindFederatedUserClaims.getText()));
                        } else {
                            jwtConfigurationDto.setBindFederatedUserClaims(true);
                        }
                    }
                }
                OMElement enableBase64PaddingElement = gatewayJWTConfigurationElement.getFirstChildWithName(
                        new QName(APIConstants.ENABLE_BASE64_PADDING));
                if (enableBase64PaddingElement != null) {
                    jwtConfigurationDto.setEnableBase64Padding(
                            JavaUtils.isTrueExplicitly(enableBase64PaddingElement.getText()));
                }
            }
        }

        OMElement jwksApiEnableElement =
                omElement.getFirstChildWithName(new QName(APIConstants.Enable_JWKS_API));
        jwtConfigurationDto.setJWKSApiEnabled(Boolean.parseBoolean(jwksApiEnableElement.getText()));
    }

    public ThrottleProperties getThrottleProperties() {

        return throttleProperties;
    }

    public WorkflowProperties getWorkflowProperties() {

        return workflowProperties;
    }

    public RedisConfig getRedisConfig() {

        return redisConfig;
    }

    public DistributedThrottleConfig getDistributedThrottleConfig() {

        return distributedThrottleConfig;
    }

    /**
     * To populate Monetization configurations
     *
     * @param element
     */
    private void setMonetizationConfigurations(OMElement element) {

        OMElement monetizationImplElement = element.getFirstChildWithName(
                new QName(APIConstants.Monetization.MONETIZATION_IMPL_CONFIG));
        if (monetizationImplElement != null) {
            monetizationConfigurationDto.setMonetizationImpl(monetizationImplElement.getText());
        }

        OMElement usagePublisherElement =
                element.getFirstChildWithName(new QName(APIConstants.Monetization.USAGE_PUBLISHER_CONFIG));
        if (usagePublisherElement != null) {
            if (analyticsProperties.get("type") != null && !analyticsProperties.get("type").trim().equals("")
                    && !analyticsProperties.get("type").equals("choreo")) {
                OMElement analyticsHost = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_HOST));

                if (analyticsHost != null) {
                    monetizationConfigurationDto.setAnalyticsHost(analyticsHost.getText());
                }

                OMElement analyticsPort = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_PORT));

                if (analyticsPort != null) {
                    monetizationConfigurationDto.setAnalyticsPort(Integer.parseInt(analyticsPort.getText()));
                }

                OMElement analyticsUsername = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_USERNAME));

                if (analyticsUsername != null) {
                    monetizationConfigurationDto.setAnalyticsUserName(analyticsUsername.getText());
                }

                OMElement analyticsPassword = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_PASSWORD));

                if (analyticsPassword != null) {
                    monetizationConfigurationDto.setAnalyticsPassword(
                            analyticsPassword.getText().getBytes(StandardCharsets.UTF_8));
                }

                OMElement analyticsIndexName = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_INDEX_NAME));

                if (analyticsIndexName != null) {
                    monetizationConfigurationDto.setAnalyticsIndexName(analyticsIndexName.getText());
                }

                OMElement analyticsProtocol = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_PROTOCOL));

                if (analyticsProtocol != null) {
                    monetizationConfigurationDto.setAnalyticsProtocol(analyticsProtocol.getText());
                }
            } else {
                OMElement choreoInsightAPIEndpointElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.INSIGHT_API_ENDPOINT_CONFIG));
                if (choreoInsightAPIEndpointElement != null) {
                    monetizationConfigurationDto.setInsightAPIEndpoint(choreoInsightAPIEndpointElement.getText());
                }

                OMElement analyticsAccessTokenElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.ANALYTICS_ACCESS_TOKEN_CONFIG));
                if (analyticsAccessTokenElement != null) {
                    String analyticsAccessToken = MiscellaneousUtil.resolve(analyticsAccessTokenElement,
                            secretResolver);
                    monetizationConfigurationDto.setAnalyticsAccessToken(analyticsAccessToken);
                }

                OMElement choreoTokenEndpointElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.CHOREO_TOKEN_URL_CONFIG));
                if (choreoTokenEndpointElement != null) {
                    monetizationConfigurationDto.setChoreoTokenEndpoint(choreoTokenEndpointElement.getText());
                }

                OMElement consumerKeyElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.CHOREO_INSIGHT_APP_CONSUMER_KEY_CONFIG));
                if (consumerKeyElement != null) {
                    String consumerKeyToken = MiscellaneousUtil.resolve(consumerKeyElement, secretResolver);
                    monetizationConfigurationDto.setInsightAppConsumerKey(consumerKeyToken);
                }

                OMElement consumerSecretElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.CHOREO_INSIGHT_APP_CONSUMER_SECRET_CONFIG));
                if (consumerSecretElement != null) {
                    String consumerSecretToken = MiscellaneousUtil.resolve(consumerSecretElement, secretResolver);
                    monetizationConfigurationDto.setInsightAppConsumerSecret(consumerSecretToken);
                }

                OMElement granularityElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.USAGE_PUBLISHER_GRANULARITY_CONFIG));
                if (granularityElement != null) {
                    monetizationConfigurationDto.setGranularity(granularityElement.getText());
                }

                OMElement publishTimeDurationElement = usagePublisherElement.getFirstChildWithName(
                        new QName(APIConstants.Monetization.FROM_TIME_CONFIGURATION_PROPERTY));
                if (publishTimeDurationElement != null) {
                    monetizationConfigurationDto.setPublishTimeDurationInDays(publishTimeDurationElement.getText());
                }
            }
        }

        OMElement additionalAttributes =
                element.getFirstChildWithName(new QName(APIConstants.Monetization.ADDITIONAL_ATTRIBUTES));
        if (additionalAttributes != null) {
            setMonetizationAdditionalAttributes(additionalAttributes);
        }
    }

    /**
     * To populate Monetization Additional Attributes
     *
     * @param element
     */
    private void setMonetizationAdditionalAttributes(OMElement element) {

        Iterator iterator = element.getChildrenWithLocalName(APIConstants.Monetization.ATTRIBUTE);
        JSONArray monetizationAttributes = new JSONArray();
        while (iterator.hasNext()) {
            OMElement omElement = (OMElement) iterator.next();
            Iterator attributes = omElement.getChildElements();
            JSONObject monetizationAttribute = new JSONObject();
            boolean isHidden = Boolean.parseBoolean(
                    omElement.getAttributeValue(new QName(APIConstants.Monetization.IS_ATTRIBUTE_HIDDEN)));
            boolean isRequired = Boolean.parseBoolean(
                    omElement.getAttributeValue(new QName(APIConstants.Monetization.IS_ATTRIBITE_REQUIRED)));
            monetizationAttribute.put(APIConstants.Monetization.IS_ATTRIBUTE_HIDDEN, isHidden);
            while (attributes.hasNext()) {
                OMElement attribute = (OMElement) attributes.next();
                if (attribute.getLocalName().equals(APIConstants.Monetization.ATTRIBUTE_NAME)) {
                    monetizationAttribute.put(APIConstants.Monetization.ATTRIBUTE, attribute.getText());
                } else if (attribute.getLocalName().equals(APIConstants.Monetization.ATTRIBUTE_DISPLAY_NAME)) {
                    monetizationAttribute.put(APIConstants.Monetization.ATTRIBUTE_DISPLAY_NAME, attribute.getText());
                } else if (attribute.getLocalName().equals(APIConstants.Monetization.ATTRIBUTE_DESCRIPTION)) {
                    monetizationAttribute.put(APIConstants.Monetization.ATTRIBUTE_DESCRIPTION, attribute.getText());
                } else if (attribute.getLocalName().equals(APIConstants.Monetization.ATTRIBUTE_DEFAULT) && isRequired) {
                    monetizationAttribute.put(APIConstants.Monetization.ATTRIBUTE_DEFAULT, attribute.getText());
                }
            }
            if (isHidden && isRequired && !monetizationAttribute
                    .containsKey(APIConstants.Monetization.ATTRIBUTE_DEFAULT)) {
                log.error("A default value needs to be given for required, hidden monetization attributes.");
            }
            monetizationAttribute.put(APIConstants.Monetization.IS_ATTRIBITE_REQUIRED, isRequired);
            monetizationAttributes.add(monetizationAttribute);
        }
        monetizationConfigurationDto.setMonetizationAttributes(monetizationAttributes);
    }

    /**
     * To populate recommendation related configurations
     *
     * @param element
     */
    private void setRecommendationConfigurations(OMElement element) {

        OMElement recommendationSeverEndpointElement = element.getFirstChildWithName(
                new QName(APIConstants.RECOMMENDATION_ENDPOINT));
        if (recommendationSeverEndpointElement != null) {
            recommendationEnvironment = new RecommendationEnvironment();
            String recommendationSeverEndpoint = recommendationSeverEndpointElement.getText();
            recommendationEnvironment.setRecommendationServerURL(recommendationSeverEndpoint);

            OMElement consumerKeyElement = element
                    .getFirstChildWithName(new QName(APIConstants.RECOMMENDATION_API_CONSUMER_KEY));
            if (consumerKeyElement != null) {
                if (secretResolver.isInitialized()
                        && secretResolver.isTokenProtected("APIManager.Recommendations.ConsumerKey")) {
                    recommendationEnvironment.setConsumerKey(secretResolver
                            .resolve("APIManager.Recommendations.ConsumerKey"));
                } else {
                    recommendationEnvironment.setConsumerKey(consumerKeyElement.getText());
                }

                OMElement consumerSecretElement = element
                        .getFirstChildWithName(new QName(APIConstants.RECOMMENDATION_API_CONSUMER_SECRET));
                if (consumerSecretElement != null) {
                    if (secretResolver.isInitialized()
                            && secretResolver.isTokenProtected("APIManager.Recommendations.ConsumerSecret")) {
                        recommendationEnvironment.setConsumerSecret(secretResolver
                                .resolve("APIManager.Recommendations.ConsumerSecret"));
                    } else {
                        recommendationEnvironment.setConsumerSecret(consumerSecretElement.getText());
                    }

                    OMElement oauthEndpointElement = element
                            .getFirstChildWithName(new QName(APIConstants.AUTHENTICATION_ENDPOINT));
                    String oauthEndpoint = null;
                    if (oauthEndpointElement != null) {
                        oauthEndpoint = oauthEndpointElement.getText();
                    } else {
                        try {
                            URL endpointURL = new URL(recommendationSeverEndpoint);
                            oauthEndpoint = endpointURL.getProtocol() + "://" + endpointURL.getHost() + ":"
                                    + endpointURL.getPort();
                        } catch (MalformedURLException e) {
                            log.error("Error when reading the recommendationServer Endpoint", e);
                        }
                    }
                    recommendationEnvironment.setOauthURL(oauthEndpoint); //Oauth URL is set only if both consumer key
                    // and consumer secrets are correctly defined
                }
            }

            OMElement applyForAllTenantsElement = element
                    .getFirstChildWithName(new QName(APIConstants.APPLY_RECOMMENDATIONS_FOR_ALL_APIS));
            if (applyForAllTenantsElement != null) {
                recommendationEnvironment.setApplyForAllTenants(JavaUtils.isTrueExplicitly(applyForAllTenantsElement
                        .getText()));
            } else {
                log.debug("Apply For All Tenants Element is not set. Set to default true");
            }
            OMElement maxRecommendationsElement = element
                    .getFirstChildWithName(new QName(APIConstants.MAX_RECOMMENDATIONS));
            if (maxRecommendationsElement != null) {
                recommendationEnvironment.setMaxRecommendations(Integer.parseInt(maxRecommendationsElement.getText()));
            } else {
                log.debug("Max recommendations is not set. Set to default 5");
            }
            OMElement userNameElement = element
                    .getFirstChildWithName(new QName(APIConstants.RECOMMENDATION_USERNAME));
            if (userNameElement != null) {
                recommendationEnvironment.setUserName(userNameElement.getText());
                log.debug("Basic OAuth used for recommendation server");
            }
            OMElement passwordElement = element
                    .getFirstChildWithName(new QName(APIConstants.RECOMMENDATION_PASSWORD));
            if (passwordElement != null) {
                if (secretResolver.isInitialized()
                        && secretResolver.isTokenProtected("APIManager.Recommendations.password")) {
                    recommendationEnvironment.setPassword(secretResolver
                            .resolve("APIManager.Recommendations.password"));
                } else {
                    recommendationEnvironment.setPassword(passwordElement.getText());
                }
            }
            OMElement waitDurationElement = element
                    .getFirstChildWithName(new QName(APIConstants.WAIT_DURATION));
            if (waitDurationElement != null) {
                recommendationEnvironment.setWaitDuration(Long.parseLong(waitDurationElement.getText()));
            } else {
                log.debug("Max recommendations is not set. Set to default 5");
            }
        }
    }

    private void setJWTTokenIssuers(OMElement omElement) {

        Iterator tokenIssuersElement =
                omElement.getChildrenWithLocalName(APIConstants.TokenIssuer.TOKEN_ISSUER);
        while (tokenIssuersElement.hasNext()) {
            OMElement issuerElement = (OMElement) tokenIssuersElement.next();
            String issuer = issuerElement.getAttributeValue(new QName("issuer"));
            OMElement consumerKeyClaimElement =
                    issuerElement.getFirstChildWithName(new QName(APIConstants.TokenIssuer.CONSUMER_KEY_CLAIM));
            OMElement scopesElement =
                    issuerElement.getFirstChildWithName(new QName(APIConstants.TokenIssuer.SCOPES_CLAIM));
            TokenIssuerDto tokenIssuerDto = new TokenIssuerDto(issuer);
            if (consumerKeyClaimElement != null){
                tokenIssuerDto.setConsumerKeyClaim(consumerKeyClaimElement.getText());
            }
            if (scopesElement != null){
                tokenIssuerDto.setScopesClaim(scopesElement.getText());
            }
            OMElement jwksConfiguration =
                    issuerElement.getFirstChildWithName(new QName(APIConstants.TokenIssuer.JWKS_CONFIGURATION));
            if (jwksConfiguration != null) {
                JWKSConfigurationDTO jwksConfigurationDTO = tokenIssuerDto.getJwksConfigurationDTO();
                jwksConfigurationDTO.setEnabled(true);
                jwksConfigurationDTO.setUrl(jwksConfiguration
                        .getFirstChildWithName(new QName(APIConstants.TokenIssuer.JWKSConfiguration.URL)).getText());
            }
            OMElement claimMappingsElement =
                    issuerElement.getFirstChildWithName(new QName(APIConstants.TokenIssuer.CLAIM_MAPPINGS));
            if (claimMappingsElement != null) {
                OMAttribute disableDefaultClaimMappingAttribute =
                        claimMappingsElement.getAttribute(new QName("disable-default-claim-mapping"));
                if (disableDefaultClaimMappingAttribute != null) {
                    String disableDefaultClaimMapping = disableDefaultClaimMappingAttribute.getAttributeValue();
                    tokenIssuerDto.setDisableDefaultClaimMapping(Boolean.parseBoolean(disableDefaultClaimMapping));
                }
                Iterator claimMapping =
                        claimMappingsElement.getChildrenWithName(new QName(APIConstants.TokenIssuer.CLAIM_MAPPING));
                while (claimMapping.hasNext()) {
                    OMElement claim = (OMElement) claimMapping.next();
                    OMElement remoteClaimElement = claim.getFirstChildWithName(
                            new QName(APIConstants.TokenIssuer.ClaimMapping.REMOTE_CLAIM));
                    OMElement localClaimElement = claim.getFirstChildWithName(
                            new QName(APIConstants.TokenIssuer.ClaimMapping.LOCAL_CLAIM));
                    if (remoteClaimElement != null && localClaimElement != null) {
                        String remoteClaim = remoteClaimElement.getText();
                        String localClaim = localClaimElement.getText();
                        if (StringUtils.isNotEmpty(remoteClaim) &&
                                StringUtils.isNotEmpty(localClaim)) {
                            tokenIssuerDto.getClaimConfigurations().put(remoteClaim, new ClaimMappingDto(remoteClaim,
                                    localClaim));
                        }
                    }
                }
            }
            jwtConfigurationDto.getTokenIssuerDtoMap().put(tokenIssuerDto.getIssuer(), tokenIssuerDto);
        }
    }

    private void setEventHubConfiguration(OMElement omElement) {

        EventHubConfigurationDto eventHubConfigurationDto = new EventHubConfigurationDto();
        OMElement enableElement = omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.ENABLE));
        if (enableElement != null && Boolean.parseBoolean(enableElement.getText())) {
            eventHubConfigurationDto.setEnabled(true);
            OMElement serviceUrlElement = omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.SERVICE_URL));
            if (serviceUrlElement != null) {
                String serviceUrl = APIUtil.replaceSystemProperty(serviceUrlElement.getText());
                if (StringUtils.isNotEmpty(serviceUrl)) {
                    serviceUrl = serviceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
                    eventHubConfigurationDto.setServiceUrl(serviceUrl);
                }
            }
            OMElement initDelay = omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.INIT_DELAY));
            if (initDelay != null) {
                eventHubConfigurationDto.setInitDelay(Integer.parseInt(initDelay.getText()));
            }
            OMElement usernameElement = omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.USERNAME));
            if (usernameElement != null) {
                eventHubConfigurationDto.setUsername(usernameElement.getText());
            }
            OMElement passwordElement = omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.PASSWORD));
            if (passwordElement != null) {
                String password = MiscellaneousUtil.resolve(passwordElement, secretResolver);
                eventHubConfigurationDto.setPassword(APIUtil.replaceSystemProperty(password).toCharArray());
            }
            OMElement eventWaitingTimeElement = omElement
                    .getFirstChildWithName(new QName(APIConstants.EVENT_WAITING_TIME_CONFIG));
            if (eventWaitingTimeElement != null) {
                long eventWaitingTime = Long.valueOf(eventWaitingTimeElement.getText());
                eventHubConfigurationDto.setEventWaitingTime(eventWaitingTime);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Event hub event waiting time not set.");
                }
            }

            OMElement configurationRetrieverElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.EVENT_RECEIVER_CONFIGURATION));
            if (configurationRetrieverElement != null) {
                EventHubConfigurationDto.EventHubReceiverConfiguration eventHubReceiverConfiguration =
                        new EventHubConfigurationDto.EventHubReceiverConfiguration();
                Iterator receiverConnectionDetailsElements = configurationRetrieverElement.getChildElements();
                Properties properties = new Properties();
                while (receiverConnectionDetailsElements.hasNext()) {
                    OMElement element = (OMElement) receiverConnectionDetailsElements.next();
                    String value = MiscellaneousUtil.resolve(element, secretResolver);
                    properties.put(element.getLocalName(), APIUtil.replaceSystemProperty(value));
                }
                eventHubReceiverConfiguration.setJmsConnectionParameters(properties);
                eventHubConfigurationDto.setEventHubReceiverConfiguration(eventHubReceiverConfiguration);
            }

            OMElement eventPublisherElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.KeyManager.EVENT_PUBLISHER_CONFIGURATIONS));
            EventHubConfigurationDto.EventHubPublisherConfiguration eventHubPublisherConfiguration =
                    new EventHubConfigurationDto.EventHubPublisherConfiguration();
            if (eventPublisherElement != null) {
                OMElement receiverUrlGroupElement = eventPublisherElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP));
                if (receiverUrlGroupElement != null) {
                    eventHubPublisherConfiguration
                            .setReceiverUrlGroup(APIUtil.replaceSystemProperty(receiverUrlGroupElement
                                    .getText()));
                }
                OMElement authUrlGroupElement = eventPublisherElement.getFirstChildWithName(new QName
                        (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP));
                if (authUrlGroupElement != null) {
                    eventHubPublisherConfiguration
                            .setAuthUrlGroup(APIUtil.replaceSystemProperty(authUrlGroupElement.getText()));
                }
                OMElement eventTypeElement = eventPublisherElement.getFirstChildWithName(
                        new QName(APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_TYPE));
                if (eventTypeElement != null) {
                    eventHubPublisherConfiguration.setType(eventTypeElement.getText().trim());
                }
                Map<String, String> publisherProps = extractPublisherProperties(eventPublisherElement);
                if (publisherProps != null) {
                    eventHubPublisherConfiguration.setProperties(publisherProps);
                }
                eventHubConfigurationDto.setEventHubPublisherConfiguration(eventHubPublisherConfiguration);
            }
        }
        this.eventHubConfigurationDto = eventHubConfigurationDto;
    }

    /**
     * Extracts out the additional parameters of the publisher configuration.
     *
     * @param eventPublisherElement publisher element defined in the format of
     *                              <EventPublisherConfiguration>"
     *                              "<Properties>"
     *                              "<Property name="testProp">testVal</Property>"
     *                              "</Properties>"
     *                              "</EventPublisherConfiguration>
     * @return the extracted properties as a map
     */
    public Map<String, String> extractPublisherProperties(OMElement eventPublisherElement) {
        OMElement propertiesElement = eventPublisherElement.getFirstChildWithName(
                new QName(APIConstants.AdvancedThrottleConstants.PROPERTIES_CONFIGURATION));
        Iterator eventPublisherPropertiesIterator = propertiesElement.getChildrenWithLocalName("Property");
        Map<String, String> publisherProps = new HashMap<>();
        while (eventPublisherPropertiesIterator.hasNext()) {
            OMElement propertyElem = (OMElement) eventPublisherPropertiesIterator.next();
            String name = propertyElem.getAttributeValue(new QName("name"));
            String value = propertyElem.getText();
            publisherProps.put(name, value);
        }
        return publisherProps;
    }

    public ExtendedJWTConfigurationDto getJwtConfigurationDto() {

        return jwtConfigurationDto;
    }

    public EventHubConfigurationDto getEventHubConfigurationDto() {

        return eventHubConfigurationDto;
    }

    private void setRuntimeArtifactsSyncPublisherConfig (OMElement omElement) {

        OMElement enableElement = omElement
                .getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.ENABLE_CONFIG));
        if (enableElement != null) {
            gatewayArtifactSynchronizerProperties.setSaveArtifactsEnabled(
                    JavaUtils.isTrueExplicitly(enableElement.getText()));
        } else {
            log.debug("Save to storage is not set. Set to default false");
        }

        OMElement saverElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.SAVER_CONFIG));
        if (saverElement != null) {
            String artifactSaver = saverElement.getText();
            gatewayArtifactSynchronizerProperties.setSaverName(artifactSaver);
        } else {
            log.debug("Artifact saver Element is not set. Set to default DB Saver");
        }

        OMElement dataSourceElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.DATA_SOURCE_NAME));
        if (dataSourceElement != null) {
            String dataSource = dataSourceElement.getText();
            gatewayArtifactSynchronizerProperties.setArtifactSynchronizerDataSource(dataSource);
        } else {
            log.debug("Data Source Element is not set. Set to default Data Source");
        }

    }

    private void setRuntimeArtifactsSyncGatewayConfig (OMElement omElement){

        OMElement enableElement = omElement
                .getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.ENABLE_CONFIG));
        if (enableElement != null) {
            gatewayArtifactSynchronizerProperties.setRetrieveFromStorageEnabled(
                    JavaUtils.isTrueExplicitly(enableElement.getText()));
        } else {
            log.debug("Retrieve from storage is not set. Set to default false");
        }

        OMElement retrieverElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.RETRIEVER_CONFIG));
        if (retrieverElement != null) {
            String artifactRetriever = retrieverElement.getText();
            gatewayArtifactSynchronizerProperties.setRetrieverName(artifactRetriever);
        } else {
            log.debug("Artifact retriever Element is not set. Set to default DB Retriever");
        }

        OMElement retryDurationElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.RETRY_DUARTION));
        if (retrieverElement != null) {
            long retryDuration = Long.valueOf(retryDurationElement.getText());
            gatewayArtifactSynchronizerProperties.setRetryDuartion(retryDuration);
        } else {
            log.debug("Retry Duration Element is not set. Set to default duration");
        }

        OMElement maxRetryCountElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.MAX_RETRY_COUNT));
        if (maxRetryCountElement != null) {
            int retryCount = Integer.parseInt(maxRetryCountElement.getText());
            gatewayArtifactSynchronizerProperties.setMaxRetryCount(retryCount);
        } else {
            log.debug("Max Retry Count Element is not set. Set to default count");
        }

        OMElement retryProgressionFactorElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.RETRY_PROGRESSION_FACTOR));
        if (retryProgressionFactorElement != null) {
            double retryProgressionFactor = Double.parseDouble(retryProgressionFactorElement.getText());
            gatewayArtifactSynchronizerProperties.setRetryProgressionFactor(retryProgressionFactor);
        } else {
            log.debug("Retry Progression Factor Element is not set. Set to default value");
        }

        OMElement dataRetrievalModeElement = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayArtifactSynchronizer.DATA_RETRIEVAL_MODE));
        if (dataRetrievalModeElement!= null) {
            String dataRetrievalMode= dataRetrievalModeElement.getText();
            gatewayArtifactSynchronizerProperties.setGatewayStartup(dataRetrievalMode);
        } else {
            log.debug("Gateway Startup mode is not set. Set to Sync Mode");
        }

        OMElement gatewayLabelElement = omElement
                .getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.GATEWAY_LABELS_CONFIG));
        if (gatewayLabelElement != null) {
            Iterator labelsIterator = gatewayLabelElement
                    .getChildrenWithLocalName(APIConstants.GatewayArtifactSynchronizer.LABEL_CONFIG);
            while (labelsIterator.hasNext()) {
                OMElement labelElement = (OMElement) labelsIterator.next();
                if (labelElement != null) {
                    gatewayArtifactSynchronizerProperties.getGatewayLabels().add(labelElement.getText());
                }
            }
        }

        OMElement gatewayFileBasedContextsElement = omElement
                .getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.FILE_BASED_API_CONTEXTS));
        if (gatewayFileBasedContextsElement != null) {
            Iterator contextsIterator = gatewayFileBasedContextsElement
                    .getChildrenWithLocalName(APIConstants.GatewayArtifactSynchronizer.FILE_BASED_API_CONTEXT);
            while (contextsIterator.hasNext()) {
                OMElement contextElement = (OMElement) contextsIterator.next();
                if (contextElement != null) {
                    gatewayArtifactSynchronizerProperties.getFileBasedApiContexts().add(contextElement.getText());
                }
            }
        }

        OMElement properties = omElement.getFirstChildWithName(new
                QName(APIConstants.API_GATEWAY_ADDITIONAL_PROPERTIES));
        Map<String, String> additionalProperties = new HashMap<>();
        if (properties != null) {
            Iterator gatewayAdditionalProperties = properties.getChildrenWithLocalName
                    (APIConstants.API_GATEWAY_ADDITIONAL_PROPERTY);
            while (gatewayAdditionalProperties.hasNext()) {
                OMElement propertyElem = (OMElement) gatewayAdditionalProperties.next();
                String propName = propertyElem.getAttributeValue(new QName("name"));
                String resolvedValue = MiscellaneousUtil.resolve(propertyElem, secretResolver);
                additionalProperties.put(propName, resolvedValue);
            }
        }

        OMElement eventWaitingTimeElement = omElement
                .getFirstChildWithName(new QName(APIConstants.EVENT_WAITING_TIME_CONFIG));
        if (eventWaitingTimeElement != null) {
            long eventWaitingTime = Long.valueOf(eventWaitingTimeElement.getText());
            gatewayArtifactSynchronizerProperties.setEventWaitingTime(eventWaitingTime);
        } else {
            log.debug("Gateway artifact synchronizer Event waiting time not set.");
        }
        OMElement enableEagerLoading =
                omElement.getFirstChildWithName(
                        new QName(APIConstants.GatewayArtifactSynchronizer.EnableOnDemandLoadingAPIS));
        if (enableEagerLoading != null) {
            gatewayArtifactSynchronizerProperties.setOnDemandLoading(
                    Boolean.parseBoolean(enableEagerLoading.getText()));
        }
        OMElement tenantLoadingOMElement =
                omElement.getFirstChildWithName(new QName(APIConstants.GatewayArtifactSynchronizer.TENANT_LOADING));
        if (tenantLoadingOMElement != null) {
            OMElement enableTenantLoading = tenantLoadingOMElement.getFirstChildWithName(
                    new QName(APIConstants.GatewayArtifactSynchronizer.ENABLE_TENANT_LOADING));
            if (enableTenantLoading != null) {
                gatewayArtifactSynchronizerProperties.setTenantLoading(
                        Boolean.parseBoolean(enableTenantLoading.getText()));
            }
            OMElement tenantSToLoadElement = tenantLoadingOMElement.getFirstChildWithName(
                    new QName(APIConstants.GatewayArtifactSynchronizer.TENANT_LOADING_TENANTS));
            if (tenantSToLoadElement != null && StringUtils.isNotEmpty(tenantSToLoadElement.getText())) {
                String[] tenantsToLoad = tenantSToLoadElement.getText().split(",");
                LoadingTenants loadingTenants = new LoadingTenants();
                for (String tenant : tenantsToLoad) {
                    tenant = tenant.trim();
                    if (tenant.equals("*")) {
                        loadingTenants.setIncludeAllTenants(true);
                    } else if (tenant.contains("!")) {
                        if (tenant.contains("*")) {
                            throw new IllegalArgumentException(tenant + " is not a valid tenant domain");
                        }
                        loadingTenants.getExcludingTenants().add(tenant.replace("!", ""));
                    } else {
                        loadingTenants.getIncludingTenants().add(tenant);
                    }
                }
                if (loadingTenants.isIncludeAllTenants()) {
                    loadingTenants.getIncludingTenants().clear();
                }
                loadingTenants.getIncludingTenants().removeAll(loadingTenants.getExcludingTenants());
                gatewayArtifactSynchronizerProperties.setLoadingTenants(loadingTenants);
            }
        }
    }

    public GatewayArtifactSynchronizerProperties getGatewayArtifactSynchronizerProperties() {

        return gatewayArtifactSynchronizerProperties; }

    public GatewayCleanupSkipList getGatewayCleanupSkipList() {

        return gatewayCleanupSkipList;
    }

    public static Map<String, String> getAnalyticsProperties() {
        return analyticsProperties;
    }

    public static Map<String, String> getAnalyticsMaskProperties() {
        return analyticsMaskProps;
    }

    public static Map<String, String> getPersistenceProperties() {
        return persistenceProperties;
    }

    /**
     * Set Extension Listener Configurations.
     *
     * @param omElement XML Config
     */
    public void setExtensionListenerConfigurations(OMElement omElement) {

        Iterator extensionListenersElement =
                omElement.getChildrenWithLocalName(APIConstants.ExtensionListenerConstants.EXTENSION_LISTENER);
        while (extensionListenersElement.hasNext()) {
            OMElement listenerElement = (OMElement) extensionListenersElement.next();
            OMElement listenerTypeElement =
                    listenerElement
                            .getFirstChildWithName(new QName(APIConstants.ExtensionListenerConstants.EXTENSION_TYPE));
            OMElement listenerClassElement =
                    listenerElement
                            .getFirstChildWithName(new QName(
                                    APIConstants.ExtensionListenerConstants.EXTENSION_LISTENER_CLASS_NAME));
            OMElement enableExtensionFaultSequenceMediationElement =
                    listenerElement
                            .getFirstChildWithName(new QName(APIConstants
                                    .ExtensionListenerConstants.EXTENSION_LISTENER_DO_MEDIATE_EXTENSION_FAULT_SEQUENCE));
            if (listenerTypeElement != null && listenerClassElement != null) {
                String listenerClass = listenerClassElement.getText();
                boolean enableExtensionFaultSequenceMediation = false;
                if (enableExtensionFaultSequenceMediationElement != null) {
                    enableExtensionFaultSequenceMediation =
                            Boolean.parseBoolean(enableExtensionFaultSequenceMediationElement.getText());
                }
                try {
                    ExtensionListener extensionListener = (ExtensionListener) APIUtil.getClassInstance(listenerClass);
                    extensionListenerMap.put(listenerTypeElement.getText().toUpperCase(), extensionListener);
                    doMediateExtensionFaultSequenceMap.put(listenerTypeElement.getText().toUpperCase(),
                            enableExtensionFaultSequenceMediation);
                } catch (InstantiationException e) {
                    log.error("Error while instantiating class " + listenerClass, e);
                } catch (IllegalAccessException e) {
                    log.error(e);
                } catch (ClassNotFoundException e) {
                    log.error("Cannot find the class " + listenerClass + e);
                }
            }
        }
    }

    private void setRestApiJWTAuthAudiences(OMElement omElement){

        Iterator jwtAudiencesElement =
                omElement.getChildrenWithLocalName(APIConstants.JWT_AUDIENCE);
        while (jwtAudiencesElement.hasNext()) {
            OMElement jwtAudienceElement = (OMElement) jwtAudiencesElement.next();
            String basePath = jwtAudienceElement.getFirstChildWithName(new QName(APIConstants.BASEPATH)).getText();
            List<String> audienceForPath = restApiJWTAuthAudiences.get(basePath);
            if (audienceForPath == null) {
                audienceForPath = new ArrayList<>();
            }
            audienceForPath.add(jwtAudienceElement.getFirstChildWithName(new QName(APIConstants.AUDIENCE)).getText());
            restApiJWTAuthAudiences.put(basePath, audienceForPath);
        }
    }

    public Map<String, Environment> getGatewayEnvironments() {
        return apiGatewayEnvironments;
    }

    public HttpClientConfigurationDTO getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    public void setHttpClientConfiguration(HttpClientConfigurationDTO httpClientConfiguration) {
        this.httpClientConfiguration = httpClientConfiguration;
    }

    public void setMarketplaceAssistantConfiguration(OMElement omElement){
        OMElement marketplaceAssistantEnableElement =
                omElement.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_ENABLED));
        if (marketplaceAssistantEnableElement != null) {
            marketplaceAssistantConfigurationDto.setEnabled(Boolean.parseBoolean(marketplaceAssistantEnableElement.getText()));
        }
        if (marketplaceAssistantConfigurationDto.isEnabled()) {
            OMElement marketplaceAssistantEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_ENDPOINT));
            if (marketplaceAssistantEndpoint != null) {
                marketplaceAssistantConfigurationDto.setEndpoint(marketplaceAssistantEndpoint.getText());
            }
            OMElement marketplaceAssistantTokenEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_TOKEN_ENDPOINT));
            if (marketplaceAssistantTokenEndpoint != null) {
                marketplaceAssistantConfigurationDto.setTokenEndpoint(marketplaceAssistantTokenEndpoint.getText());
            }
            OMElement marketplaceAssistantKey =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_KEY));

            if (marketplaceAssistantKey != null) {
                String Key = MiscellaneousUtil.resolve(marketplaceAssistantKey, secretResolver);
                marketplaceAssistantConfigurationDto.setKey(Key);
                if (!Key.isEmpty()){
                    marketplaceAssistantConfigurationDto.setKeyProvided(true);
                }
            }

            OMElement marketplaceAssistantToken =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_AUTH_TOKEN));

            if (marketplaceAssistantToken != null) {
                String AccessToken = MiscellaneousUtil.resolve(marketplaceAssistantToken, secretResolver);
                marketplaceAssistantConfigurationDto.setAccessToken(AccessToken);
                if (!AccessToken.isEmpty()){
                    marketplaceAssistantConfigurationDto.setAuthTokenProvided(true);
                }
            }

            OMElement resources =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.RESOURCES));

            OMElement marketplaceAssistantApiCountResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_API_COUNT_RESOURCE));
            if (marketplaceAssistantApiCountResource != null) {
                marketplaceAssistantConfigurationDto.setApiCountResource(marketplaceAssistantApiCountResource.getText());
            }
            OMElement marketplaceAssistantApiDeleteResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_DELETE_API_RESOURCE));
            if (marketplaceAssistantApiDeleteResource != null) {
                marketplaceAssistantConfigurationDto.setApiDeleteResource(marketplaceAssistantApiDeleteResource.getText());
            }
            OMElement marketplaceAssistantApiPublishResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_PUBLISH_API_RESOURCE));
            if (marketplaceAssistantApiPublishResource != null) {
                marketplaceAssistantConfigurationDto.setApiPublishResource(marketplaceAssistantApiPublishResource.getText());
            }
            OMElement marketplaceAssistantChatResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.MARKETPLACE_ASSISTANT_CHAT_RESOURCE));
            if (marketplaceAssistantChatResource != null) {
                marketplaceAssistantConfigurationDto.setChatResource(marketplaceAssistantChatResource.getText());
            }
        }
    }

    /**
     * Parses the AI configuration XML and populates the AIAPIConfigurationsDTO.
     *
     * @param omElement The root OMElement containing AI configuration details.
     */
    private void setAiConfiguration(OMElement omElement) {

        if (omElement == null) {
            log.debug("AI configuration element is null. Skipping configuration parsing.");
            return;
        }
        OMElement aiConfigurationEnabledElement =
                omElement.getFirstChildWithName(new QName(APIConstants.AI.ENABLED));
        if (aiConfigurationEnabledElement != null
                && StringUtils.isNotEmpty(aiConfigurationEnabledElement.getText())) {

            aiapiConfigurationsDTO.setEnabled(Boolean.parseBoolean(aiConfigurationEnabledElement.getText().trim()));

            OMElement failoverConfigurationsElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI
                            .AI_CONFIGURATION_FAILOVER_CONFIGURATIONS));

            if (failoverConfigurationsElement != null) {
                AIAPIConfigurationsDTO.FailoverConfigurations failoverConfigurations =
                        new AIAPIConfigurationsDTO.FailoverConfigurations();
                OMElement failoverEndpointsLimitElement =
                        failoverConfigurationsElement.getFirstChildWithName(new QName(APIConstants.AI
                                .AI_CONFIGURATION_FAILOVER_CONFIGURATIONS_FAILOVER_ENDPOINTS_LIMIT));
                if (failoverEndpointsLimitElement != null
                        && StringUtils.isNotEmpty(failoverEndpointsLimitElement.getText())) {
                    try {
                        failoverConfigurations.setFailoverEndpointsLimit(Integer.parseInt(
                                failoverEndpointsLimitElement.getText().trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for FailoverEndpointsLimit", e);
                    }
                }
                OMElement defaultRequestTimeoutElement =
                        failoverConfigurationsElement.getFirstChildWithName(new QName(APIConstants.AI
                                .AI_CONFIGURATION_DEFAULT_REQUEST_TIMEOUT));
                if (defaultRequestTimeoutElement != null
                        && StringUtils.isNotEmpty(defaultRequestTimeoutElement.getText())) {
                    try {
                        failoverConfigurations.setDefaultRequestTimeout(Long.parseLong(
                                defaultRequestTimeoutElement.getText().trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for defaultRequestTimeout.", e);
                    }
                }
                aiapiConfigurationsDTO.setFailoverConfigurations(failoverConfigurations);
            } else {
                log.debug("Failover configurations are not defined in AI configuration.");
            }
            OMElement defaultRequestTimeoutElement =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI
                            .AI_CONFIGURATION_DEFAULT_REQUEST_TIMEOUT));
            if (defaultRequestTimeoutElement != null
                    && StringUtils.isNotEmpty(defaultRequestTimeoutElement.getText())) {
                aiapiConfigurationsDTO.setDefaultRequestTimeout(Long.parseLong(
                        defaultRequestTimeoutElement.getText().trim()));
            } else {
                log.debug("RoundRobin configurations are not defined in AI configuration.");
            }
        }
    }

    public boolean isEnableAiConfiguration() {

        return getAiApiConfigurationsDTO().isEnabled();
    }

    public static AIAPIConfigurationsDTO getAiApiConfigurationsDTO() {

        return aiapiConfigurationsDTO;
    }

    /**
     * Set MCP Portal Configuration
     *
     * @param omElement XML Config
     */
    private void setMCPConfigurations(OMElement omElement) {

        if (omElement == null) {
            log.debug("MCP Server configuration element is null. Skipping configuration parsing.");
            return;
        }
        OMElement mcpServerConfigElement =
                omElement.getFirstChildWithName(new QName(APIConstants.AI.MCP_SUPPORT_ENABLED));
        if (mcpServerConfigElement != null
                && StringUtils.isNotEmpty(mcpServerConfigElement.getText())) {
            isMCPSupportEnabled = Boolean.parseBoolean(mcpServerConfigElement.getText().trim());
            System.setProperty(APIConstants.ENABLE_MCP_SUPPORT, Boolean.toString(isMCPSupportEnabled));
        }
    }

    /**
     * Returns whether the MCP Portal is enabled or not.
     *
     * @return true if MCP Portal is enabled, false otherwise.
     */
    public boolean isMCPSupportEnabled() {

        return isMCPSupportEnabled;
    }

    /**
     * Set Devportal Mode
     *
     * @return Devportal mode.
     */
    public String getDevportalMode() {

        if (isMCPSupportEnabled()) {
            return devportalMode;
        }
        return APIConstants.DEVPORTAL_MODE_API_ONLY;
    }

    private void setHashingAlgorithm(OMElement omElement) {

        OMElement hashingAlgorithm =
                omElement.getFirstChildWithName(new QName(APIConstants.HASGING_ALGORITHM));
        if (hashingAlgorithm != null) {
            setHashingAlgorithm(hashingAlgorithm.getText());
        }
    }

    public String getHashingAlgorithm() {

        return hashingAlgorithm;
    }

    public void setHashingAlgorithm(String hashingAlgorithm) {

        this.hashingAlgorithm = hashingAlgorithm;
    }

    public void setApiChatConfiguration(OMElement omElement){
        OMElement apiChatEnableElement =
                omElement.getFirstChildWithName(new QName(APIConstants.AI.ENABLED));
        if (apiChatEnableElement != null) {
            apiChatConfigurationDto.setEnabled(Boolean.parseBoolean(apiChatEnableElement.getText()));
        }
        if (apiChatConfigurationDto.isEnabled()) {
            OMElement apiChatEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_ENDPOINT));
            if (apiChatEndpoint != null) {
                apiChatConfigurationDto.setEndpoint(apiChatEndpoint.getText());
            }
            OMElement apiChatTokenEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_TOKEN_ENDPOINT));
            if (apiChatEndpoint != null) {
                apiChatConfigurationDto.setTokenEndpoint(apiChatTokenEndpoint.getText());
            }
            OMElement apiChatKey =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_KEY));

            if (apiChatKey != null) {
                String Key = MiscellaneousUtil.resolve(apiChatKey, secretResolver);
                apiChatConfigurationDto.setKey(Key);
                if (!Key.isEmpty()){
                    apiChatConfigurationDto.setKeyProvided(true);
                }
            }
            OMElement apiChatToken =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_AUTH_TOKEN));
            if (apiChatToken != null) {
                String AccessToken = MiscellaneousUtil.resolve(apiChatToken, secretResolver);
                apiChatConfigurationDto.setAccessToken(AccessToken);
                if (!AccessToken.isEmpty()){
                    apiChatConfigurationDto.setAuthTokenProvided(true);
                }
            }
            OMElement resources =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.RESOURCES));

            OMElement apiChatPrepareResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_PREPARE_RESOURCE));
            if (apiChatPrepareResource != null) {
                apiChatConfigurationDto.setPrepareResource(apiChatPrepareResource.getText());
            }
            OMElement apiChatExecuteResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.API_CHAT_EXECUTE_RESOURCE));
            if (apiChatExecuteResource != null) {
                apiChatConfigurationDto.setExecuteResource(apiChatExecuteResource.getText());
            }
        }
    }

    public boolean isJWTClaimCacheEnabled() {

        String jwtClaimCacheExpiryEnabledString = getFirstProperty(APIConstants.JWT_CLAIM_CACHE_EXPIRY);
        if (StringUtils.isNotEmpty(jwtClaimCacheExpiryEnabledString)){
            return Boolean.parseBoolean(jwtClaimCacheExpiryEnabledString);
        }
        return false;
    }

    public void setDesignAssistantConfiguration(OMElement omElement){
        OMElement designAssistantEnableElement =
                omElement.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_ENABLED));
        if (designAssistantEnableElement != null) {
            designAssistantConfigurationDto.setEnabled(Boolean.parseBoolean(designAssistantEnableElement.getText()));
        }
        if (designAssistantConfigurationDto.isEnabled()) {
            OMElement designAssistantEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_ENDPOINT));
            if (designAssistantEndpoint != null) {
                designAssistantConfigurationDto.setEndpoint(designAssistantEndpoint.getText());
            }
            OMElement designAssistantTokenEndpoint =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_TOKEN_ENDPOINT));
            if (designAssistantTokenEndpoint != null) {
                designAssistantConfigurationDto.setTokenEndpoint(designAssistantTokenEndpoint.getText());
            }
            OMElement designAssistantKey =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_KEY));

            if (designAssistantKey != null) {
                String Key = MiscellaneousUtil.resolve(designAssistantKey, secretResolver);
                designAssistantConfigurationDto.setKey(Key);
                if (!Key.isEmpty()){
                    designAssistantConfigurationDto.setKeyProvided(true);
                }
            }
            OMElement designAssistantToken =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_AUTH_TOKEN));
            if (designAssistantToken != null) {
                String AccessToken = MiscellaneousUtil.resolve(designAssistantToken, secretResolver);
                designAssistantConfigurationDto.setAccessToken(AccessToken);
                if (!AccessToken.isEmpty()){
                    designAssistantConfigurationDto.setAuthTokenProvided(true);
                }
            }
            OMElement resources =
                    omElement.getFirstChildWithName(new QName(APIConstants.AI.RESOURCES));

            OMElement designAssistantChatResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_CHAT_RESOURCE));
            if (designAssistantChatResource != null) {
                designAssistantConfigurationDto.setChatResource(designAssistantChatResource.getText());
            }
            OMElement designAssistantGenApiPayloadResource =
                    resources.getFirstChildWithName(new QName(APIConstants.AI.DESIGN_ASSISTANT_GEN_API_PAYLOAD_RESOURCE));
            if (designAssistantGenApiPayloadResource != null) {
                designAssistantConfigurationDto.setGenApiPayloadResource(designAssistantGenApiPayloadResource.getText());
            }
        }
    }

    public TokenValidationDto getTokenValidationDto() {
        return tokenValidationDto;
    }

    /**
     * Set APIM Governance Configurations
     *
     * @param omElement XML Config
     */
    public void setAPIMGovernanceConfigurations(OMElement omElement) {
        OMElement dataSource = omElement
                .getFirstChildWithName(new QName(APIConstants.APIMGovernance.DATA_SOURCE_NAME));
        if (dataSource != null) {
            String dataSourceName = dataSource.getText();
            apimGovConfigurationDto.setDataSourceName(dataSourceName);
        }

        OMElement schedulerConfig = omElement
                .getFirstChildWithName(new QName(APIConstants.APIMGovernance.SCHEDULER_CONFIG));

        if (schedulerConfig != null) {
            OMElement threadPoolSize = schedulerConfig
                    .getFirstChildWithName(new QName(APIConstants.APIMGovernance.SCHEDULER_THREAD_POOL_SIZE));
            if (threadPoolSize != null) {
                apimGovConfigurationDto.setSchedulerThreadPoolSize(Integer.parseInt(threadPoolSize.getText()));
            }

            OMElement queueSize = schedulerConfig
                    .getFirstChildWithName(new QName(APIConstants.APIMGovernance.SCHEDULER_QUEUE_SIZE));
            if (queueSize != null) {
                apimGovConfigurationDto.setSchedulerQueueSize(Integer.parseInt(queueSize.getText()));
            }

            OMElement checkInterval = schedulerConfig
                    .getFirstChildWithName(new QName(APIConstants.APIMGovernance.SCHEDULER_TASK_CHECK_INTERVAL));
            if (checkInterval != null) {
                apimGovConfigurationDto.setSchedulerTaskCheckInterval(Integer.parseInt(checkInterval.getText()));
            }

            OMElement cleanupInterval = schedulerConfig
                    .getFirstChildWithName(new QName(APIConstants.APIMGovernance.SCHEDULER_TASK_CLEANUP_INTERVAL));
            if (cleanupInterval != null) {
                apimGovConfigurationDto.setSchedulerTaskCleanupInterval(Integer.parseInt(cleanupInterval.getText()));
            }
        }

    }

    /**
     * Get APIM Governance Configuration DTO
     *
     * @return APIMGovernanceConfigDTO
     */
    public APIMGovernanceConfigDTO getAPIMGovernanceConfigurationDto() {
        return apimGovConfigurationDto;
    }

    private void setGatewayNotificationConfiguration(org.apache.axiom.om.OMElement omElement) {
        org.apache.axiom.om.OMElement enabledElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.GATEWAY_NOTIFICATION_ENABLED));
        if (enabledElem != null) {
            gatewayNotificationConfiguration.setEnabled(Boolean.parseBoolean(enabledElem.getText()));
        }
        org.apache.axiom.om.OMElement gatewayIdElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.GATEWAY_IDENTIFIER));
        if (gatewayIdElem != null) {
            gatewayNotificationConfiguration.setGatewayID(gatewayIdElem.getText());
        }

        org.apache.axiom.om.OMElement heartbeatElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.HEARTBEAT));
        if (heartbeatElem != null) {
            org.apache.axiom.om.OMElement intervalElem = heartbeatElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.NOTIFY_INTERVAL_SECONDS));
            if (intervalElem != null) {
                gatewayNotificationConfiguration.getHeartbeat().setNotifyIntervalSeconds(
                        Integer.parseInt(intervalElem.getText()));
            }
        }

        org.apache.axiom.om.OMElement deploymentAckElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.DEPLOYMENT_ACKNOWLEDGEMENT));
        if (deploymentAckElem != null) {
            org.apache.axiom.om.OMElement batchSizeElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_SIZE));
            if (batchSizeElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchSize(
                        Integer.parseInt(batchSizeElem.getText()));
            }
            org.apache.axiom.om.OMElement batchIntervalElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_INTERVAL_MILLIS));
            if (batchIntervalElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchIntervalMillis(
                        Long.parseLong(batchIntervalElem.getText()));
            }
            org.apache.axiom.om.OMElement maxRetryCountElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.MAX_RETRY_COUNT));
            if (maxRetryCountElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setMaxRetryCount(
                        Integer.parseInt(maxRetryCountElem.getText()));
            }
            org.apache.axiom.om.OMElement retryDurationElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.RETRY_DURATION));
            if (retryDurationElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setRetryDuration(
                        Long.parseLong(retryDurationElem.getText()));
            }
            org.apache.axiom.om.OMElement retryProgressionFactorElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.RETRY_PROGRESSION_FACTOR));
            if (retryProgressionFactorElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setRetryProgressionFactor(
                        Double.parseDouble(retryProgressionFactorElem.getText()));
            }
            org.apache.axiom.om.OMElement batchProcessorMinThreadElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_PROCESSOR_MIN_THREAD));
            if (batchProcessorMinThreadElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchProcessorMinThread(
                        Integer.parseInt(batchProcessorMinThreadElem.getText()));
            }
            org.apache.axiom.om.OMElement batchProcessorMaxThreadElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_PROCESSOR_MAX_THREAD));
            if (batchProcessorMaxThreadElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchProcessorMaxThread(
                        Integer.parseInt(batchProcessorMaxThreadElem.getText()));
            }
            org.apache.axiom.om.OMElement batchProcessorKeepAliveElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_PROCESSOR_KEEP_ALIVE));
            if (batchProcessorKeepAliveElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchProcessorKeepAlive(
                        Long.parseLong(batchProcessorKeepAliveElem.getText()));
            }
            org.apache.axiom.om.OMElement batchProcessorQueueSizeElem = deploymentAckElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.BATCH_PROCESSOR_QUEUE_SIZE));
            if (batchProcessorQueueSizeElem != null) {
                gatewayNotificationConfiguration.getDeploymentAcknowledgement().setBatchProcessorQueueSize(
                        Integer.parseInt(batchProcessorQueueSizeElem.getText()));
            }
        }

        org.apache.axiom.om.OMElement registrationElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.REGISTRATION));
        if (registrationElem != null) {
            org.apache.axiom.om.OMElement maxRetryCountElem = registrationElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.MAX_RETRY_COUNT));
            if (maxRetryCountElem != null) {
                gatewayNotificationConfiguration.getRegistration().setMaxRetryCount(
                        Integer.parseInt(maxRetryCountElem.getText()));
            }
            org.apache.axiom.om.OMElement retryDurationElem = registrationElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.RETRY_DURATION));
            if (retryDurationElem != null) {
                gatewayNotificationConfiguration.getRegistration().setRetryDuration(
                        Long.parseLong(retryDurationElem.getText()));
            }
            org.apache.axiom.om.OMElement retryProgressionFactorElem = registrationElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.RETRY_PROGRESSION_FACTOR));
            if (retryProgressionFactorElem != null) {
                gatewayNotificationConfiguration.getRegistration().setRetryProgressionFactor(
                        Double.parseDouble(retryProgressionFactorElem.getText()));
            }
        }

        org.apache.axiom.om.OMElement cleanupElem = omElement.getFirstChildWithName(
                new QName(APIConstants.GatewayNotification.GATEWAY_CLEANUP));
        if (cleanupElem != null) {
            org.apache.axiom.om.OMElement expireElem = cleanupElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.EXPIRE_TIME_SECONDS));
            if (expireElem != null) {
                gatewayNotificationConfiguration.getGatewayCleanupConfiguration().setExpireTimeSeconds(
                        Integer.parseInt(expireElem.getText()));
            }
            org.apache.axiom.om.OMElement retentionElem = cleanupElem.getFirstChildWithName(
                    new QName(APIConstants.GatewayNotification.DATA_RETENTION_PERIOD_SECONDS));
            if (retentionElem != null) {
                gatewayNotificationConfiguration.getGatewayCleanupConfiguration().setDataRetentionPeriodSeconds(
                        Integer.parseInt(retentionElem.getText()));
            }
        }
    }

    public GatewayNotificationConfiguration getGatewayNotificationConfiguration() {
        return gatewayNotificationConfiguration;
    }

    /**
     * Get Solace Config
     *
     * @return SolaceConfig
     */
    public SolaceConfig getSolaceConfig() {
        return solaceConfig;
    }
}
