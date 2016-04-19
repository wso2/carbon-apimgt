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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private Map<String, Map<String, String>> loginConfiguration = new ConcurrentHashMap<String, Map<String, String>>();

    private SecretResolver secretResolver;

    private boolean initialized;
    private ThrottleProperties throttleProperties = new ThrottleProperties();
    private Map<String, Environment> apiGatewayEnvironments = new HashMap<String, Environment>();
    private Set<APIStore> externalAPIStores = new HashSet<APIStore>();

    public Map<String, Map<String, String>> getLoginConfiguration() {
        return loginConfiguration;
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
        try {
            in = FileUtils.openInputStream(new File(filePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            secretResolver = SecretResolverFactory.create(builder.getDocumentElement(), true);
            readChildElements(builder.getDocumentElement(), new Stack<String>());
            initialized = true;
            addKeyManagerConfigsAsSystemProperties();
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
                                   Stack<String> nameStack) throws APIManagementException{
        for (Iterator childElements = serverConfig.getChildElements(); childElements
                .hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            String localName = element.getLocalName();
            nameStack.push(localName);
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                String value = element.getText();
                if (secretResolver.isInitialized() && secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                addToConfiguration(key, APIUtil.replaceSystemProperty(value));
            } else if ("Environments".equals(localName)) {
                Iterator environmentIterator = element.getChildrenWithLocalName("Environment");
                apiGatewayEnvironments = new HashMap<String, Environment>();

                while (environmentIterator.hasNext()) {
                    Environment environment = new Environment();
                    OMElement environmentElem = (OMElement) environmentIterator.next();
                    environment.setType(environmentElem.getAttributeValue(new QName("type")));
                    String showInConsole = environmentElem.getAttributeValue(new QName("api-console"));
                    if (showInConsole != null) {
                        environment.setShowInConsole(Boolean.parseBoolean(showInConsole));
                    } else {
                        environment.setShowInConsole(true);
                    }
                    environment.setName(APIUtil.replaceSystemProperty(
                            environmentElem.getFirstChildWithName(new QName("Name")).getText()));
                    environment.setServerURL(APIUtil.replaceSystemProperty(
                            environmentElem.getFirstChildWithName(new QName(
                                    APIConstants.API_GATEWAY_SERVER_URL)).getText()));
                    environment.setUserName(APIUtil.replaceSystemProperty(

                            environmentElem.getFirstChildWithName(new QName(
                                    APIConstants.API_GATEWAY_USERNAME)).getText()));

                    String key = APIConstants.API_GATEWAY + APIConstants.API_GATEWAY_PASSWORD;
                    String value;
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected(key)) {
                        value = secretResolver.resolve(key);
                    } else {
                        value = environmentElem.getFirstChildWithName(new QName(
                                APIConstants.API_GATEWAY_PASSWORD)).getText();
                    }
                    environment.setPassword(APIUtil.replaceSystemProperty(value));
                    environment.setApiGatewayEndpoint(APIUtil.replaceSystemProperty(
                            environmentElem.getFirstChildWithName(new QName(
                                    APIConstants.API_GATEWAY_ENDPOINT)).getText()));
                    OMElement description =
                            environmentElem.getFirstChildWithName(new QName("Description"));
                    if (description != null) {
                        environment.setDescription(description.getText());
                    } else {
                        environment.setDescription("");
                    }
                    if (!apiGatewayEnvironments.containsKey(environment.getName())) {
                        apiGatewayEnvironments.put(environment.getName(), environment);
                    } else {
                        /*
                          This will be happen only on server startup therefore we log and continue the startup
                         */
                        log.error("Duplicate environment name found in api-manager.xml " +
                                  environment.getName());
                    }
                }
            } else if (APIConstants.EXTERNAL_API_STORES.equals(localName)) {  //Initialize 'externalAPIStores' config elements
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
                        store.setPublisher((APIPublisher) APIUtil.getClassForName(className).newInstance());
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
                    OMElement configDisplayName = storeElem.getFirstChildWithName(new QName(APIConstants.EXTERNAL_API_STORE_DISPLAY_NAME));
                    String displayName = (configDisplayName != null) ? APIUtil.replaceSystemProperty(
                            configDisplayName.getText()) : name;
                    store.setDisplayName(displayName);//Set store display name
                    store.setEndpoint(APIUtil.replaceSystemProperty(
                            storeElem.getFirstChildWithName(new QName(
                                    APIConstants.EXTERNAL_API_STORE_ENDPOINT)).getText())); //Set store endpoint,which is used to publish APIs
                    store.setPublished(false);
                    if (APIConstants.WSO2_API_STORE_TYPE.equals(type)) {
                        OMElement password = storeElem.getFirstChildWithName(new QName(
                                APIConstants.EXTERNAL_API_STORE_PASSWORD));
                        if (password != null) {
                            String key = APIConstants.EXTERNAL_API_STORES + "." + APIConstants.EXTERNAL_API_STORE + "." + APIConstants.EXTERNAL_API_STORE_PASSWORD + '_' + name;//Set store login password [optional]
                            String value;
                            if (secretResolver.isInitialized() && secretResolver.isTokenProtected(key)) {
                                value = secretResolver.resolve(key);
                            } else {

                                value = password.getText();
                            }
                            store.setPassword(APIUtil.replaceSystemProperty(value));
                            store.setUsername(APIUtil.replaceSystemProperty(
                                    storeElem.getFirstChildWithName(new QName(
                                            APIConstants.EXTERNAL_API_STORE_USERNAME)).getText())); //Set store login username [optional]
                        } else {
                            log.error("The user-credentials of API Publisher is not defined in the <ExternalAPIStore> config of api-manager.xml.");
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

            }else if (APIConstants.AdvancedThrottleConstants.THROTTLING_CONFIGURATIONS.equals(localName)){
                setThrottleProperties(serverConfig);
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
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
    private void addKeyManagerConfigsAsSystemProperties() {
        URL keyManagerURL;
        try {
            keyManagerURL = new URL(configuration.get(APIConstants.KEYMANAGER_SERVERURL).get(0));
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
     * set the Advance Throttle Properties into Configuration
     *
     * @param element
     */
    private void setThrottleProperties(OMElement element) {
        OMElement throttleConfigurationElement = element.getFirstChildWithName(new QName(APIConstants
                .AdvancedThrottleConstants.THROTTLING_CONFIGURATIONS));
        if (throttleConfigurationElement != null) {

            OMElement enableAdvanceThrottlingElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_ADVANCE_THROTTLING));
            if (enableAdvanceThrottlingElement != null) {
                throttleProperties.setEnabled(JavaUtils.isTrueExplicitly(enableAdvanceThrottlingElement
                        .getText()));
            }

            OMElement enableUnlimitedTierElement = throttleConfigurationElement
                    .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants
                            .ENABLE_UNLIMITED_TIER));
            if (enableUnlimitedTierElement != null) {
                throttleProperties.setEnableUnlimitedTier(JavaUtils.isTrueExplicitly(enableUnlimitedTierElement
                        .getText()));
            }
            if (throttleProperties.isEnabled()) {

                ThrottleProperties.DataPublisher dataPublisher = new ThrottleProperties.DataPublisher();
                OMElement dataPublisherConfigurationElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURATION));
                OMElement receiverUrlGroupElement = dataPublisherConfigurationElement.getFirstChildWithName(new QName
                        (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_REVEIVER_URL_GROUP));
                if (receiverUrlGroupElement != null) {
                    dataPublisher.setReceiverUrlGroup(receiverUrlGroupElement.getText());
                }
                OMElement authUrlGroupElement = dataPublisherConfigurationElement.getFirstChildWithName(new QName
                        (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_AUTH_URL_GROUP));
                if (authUrlGroupElement != null) {
                    dataPublisher.setAuthUrlGroup(authUrlGroupElement.getText());
                }
                OMElement dataPublisherUsernameElement = dataPublisherConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.USERNAME));
                if (dataPublisherUsernameElement != null) {
                    dataPublisher.setUsername(dataPublisherUsernameElement.getText());
                }
                OMElement dataPublisherTypeElement = dataPublisherConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.DATA_PUBLISHER_CONFIGURAION_TYPE));
                if (dataPublisherTypeElement != null) {
                    dataPublisher.setType(dataPublisherTypeElement.getText());
                }
                String dataPublisherConfigurationPassword;
                String dataPublisherConfigurationPasswordKey = APIConstants.AdvancedThrottleConstants
                        .THROTTLING_CONFIGURATIONS + "." + APIConstants.AdvancedThrottleConstants
                        .DATA_PUBLISHER_CONFIGURATION + "." + APIConstants.AdvancedThrottleConstants
                        .PASSWORD;
                if (secretResolver.isInitialized() && secretResolver.isTokenProtected
                        (dataPublisherConfigurationPasswordKey)) {
                    dataPublisherConfigurationPassword = secretResolver.resolve(dataPublisherConfigurationPasswordKey);
                } else {
                    dataPublisherConfigurationPassword = dataPublisherConfigurationElement.getFirstChildWithName(new
                            QName(APIConstants
                            .AdvancedThrottleConstants.PASSWORD)).getText();
                }
                dataPublisher.setPassword(APIUtil.replaceSystemProperty(dataPublisherConfigurationPassword));
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
                    OMElement corePoolSizeElement = dataPublisherPoolConfigurationElement.getFirstChildWithName(new
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
                        dataPublisherThreadPool.setMaximumPoolSize(Integer.parseInt(maximumPoolSizeElement.getText()));
                    }
                    OMElement keepAliveTimeElement = dataPublisherThreadPoolConfigurationElement.getFirstChildWithName
                            (new
                                    QName
                                    (APIConstants.AdvancedThrottleConstants
                                            .DATA_PUBLISHER_THREAD_POOL_CONFIGURATION_KEEP_ALIVE_TIME));
                    if (keepAliveTimeElement != null) {
                        dataPublisherThreadPool.setKeepAliveTime(Long.parseLong(keepAliveTimeElement.getText()));
                    }
                }
                throttleProperties.setDataPublisherThreadPool(dataPublisherThreadPool);


                //GlobalPolicyEngineWSConnectionDetails
                OMElement globalEngineWSConnectionElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.GLOBAL_POLICY_ENGINE_WS_CONFIGURATION));

                ThrottleProperties.GlobalEngineWSConnection globalEngineWSConnection = new
                        ThrottleProperties
                                .GlobalEngineWSConnection();
                if (globalEngineWSConnectionElement != null) {
                    OMElement globalEngineWSConnectionServiceUrlElement = globalEngineWSConnectionElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (globalEngineWSConnectionServiceUrlElement != null) {
                        globalEngineWSConnection.setServiceUrl(globalEngineWSConnectionServiceUrlElement.getText());
                    }
                    OMElement globalEngineWSConnectionServiceUsernameElement = globalEngineWSConnectionElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (globalEngineWSConnectionServiceUsernameElement != null) {
                        globalEngineWSConnection.setUsername(globalEngineWSConnectionServiceUsernameElement.getText());
                    }
                    String globalEngineWSConnectionServicePassword;
                    String globalEngineWSConnectionServicePasswordKey = APIConstants.AdvancedThrottleConstants
                            .THROTTLING_CONFIGURATIONS + "." + APIConstants.AdvancedThrottleConstants
                            .GLOBAL_POLICY_ENGINE_WS_CONFIGURATION + "." + APIConstants.AdvancedThrottleConstants
                            .PASSWORD;
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected
                            (globalEngineWSConnectionServicePasswordKey)) {
                        globalEngineWSConnectionServicePassword = secretResolver.resolve
                                (globalEngineWSConnectionServicePasswordKey);
                    } else {
                        globalEngineWSConnectionServicePassword = globalEngineWSConnectionElement
                                .getFirstChildWithName(new QName(APIConstants
                                        .AdvancedThrottleConstants.PASSWORD)).getText();
                    }
                    globalEngineWSConnection.setPassword(APIUtil.replaceSystemProperty
                            (globalEngineWSConnectionServicePassword));
                    globalEngineWSConnection.setEnabled(true);
                }
                throttleProperties.setGlobalEngineWSConnection(globalEngineWSConnection);

                // Configuring JMSConnectionDetails
                ThrottleProperties.JMSConnectionProperties jmsConnectionProperties = new
                        ThrottleProperties
                                .JMSConnectionProperties();

                OMElement jmsConnectionDetailElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName
                        (APIConstants.AdvancedThrottleConstants.JMS_CONNECTION_DETAILS));

                if (jmsConnectionDetailElement != null) {
                    OMElement jmsConnectionUrlElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (jmsConnectionUrlElement != null) {
                        jmsConnectionProperties.setServiceUrl(jmsConnectionUrlElement.getText());
                    }
                    OMElement jmsConnectionUserElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (jmsConnectionUserElement != null) {
                        jmsConnectionProperties.setUsername(jmsConnectionUserElement.getText());
                    }
                    OMElement jmsConnectionDestinationElement = jmsConnectionDetailElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.JMS_CONNECTION_DESTINATION));
                    if (jmsConnectionDestinationElement != null) {
                        jmsConnectionProperties.setDestination(jmsConnectionDestinationElement.getText());
                    }
                    String jmsConnectionPassword;
                    String jmsConnectionPasswordKey = APIConstants.AdvancedThrottleConstants
                            .THROTTLING_CONFIGURATIONS + "." + APIConstants.AdvancedThrottleConstants
                            .JMS_CONNECTION_DETAILS + "." + APIConstants.AdvancedThrottleConstants
                            .PASSWORD;
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected(jmsConnectionPasswordKey)) {
                        jmsConnectionPassword = secretResolver.resolve(jmsConnectionPasswordKey);
                    } else {
                        jmsConnectionPassword = jmsConnectionDetailElement.getFirstChildWithName(new QName(APIConstants
                                .AdvancedThrottleConstants.PASSWORD)).getText();
                    }
                    jmsConnectionProperties.setPassword(APIUtil.replaceSystemProperty(jmsConnectionPassword));

                    OMElement jmsConnectionParameterElement = jmsConnectionDetailElement.getFirstChildWithName(new
                            QName(APIConstants.AdvancedThrottleConstants.JMS_CONNECTION_PARAMETERS));
                    if (jmsConnectionParameterElement != null) {
                        Iterator jmsProperties = jmsConnectionParameterElement.getChildElements();
                        Properties properties = new Properties();
                        while (jmsProperties.hasNext()) {
                            OMElement property = (OMElement) jmsProperties.next();
                            properties.put(property.getLocalName(), property.getText());
                        }
                        jmsConnectionProperties.setJmsConnectionProperties(properties);
                    }
                    // Configuring JMS Task Manager
                    ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties
                            jmsTaskManagerProperties = new
                            ThrottleProperties.JMSConnectionProperties.JMSTaskManagerProperties();
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
                        if (keepAliveTimeInMillisElement != null) {
                            jmsTaskManagerProperties.setJobQueueSize(Integer.parseInt(jobQueueSizeElement.getText()));
                        }
                        jmsConnectionProperties.setJmsTaskManagerProperties(jmsTaskManagerProperties);
                    }
                    throttleProperties.setJmsConnectionProperties(jmsConnectionProperties);
                }
                //Configuring policy deployer
                OMElement policyDeployerConnectionElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.POLICY_DEPLOYER_CONFIGURATION));

                ThrottleProperties.PolicyDeployer policyDeployerConfiguration = new
                        ThrottleProperties
                                .PolicyDeployer();
                if (policyDeployerConnectionElement != null) {
                    OMElement policyDeployerServiceUrlElement = policyDeployerConnectionElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (policyDeployerServiceUrlElement != null) {
                        policyDeployerConfiguration.setServiceUrl(policyDeployerServiceUrlElement.getText());
                    }
                    OMElement policyDeployerServiceServiceUsernameElement = policyDeployerConnectionElement
                            .getFirstChildWithName(new QName
                                    (APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (policyDeployerServiceServiceUsernameElement != null) {
                        policyDeployerConfiguration.setUsername(policyDeployerServiceServiceUsernameElement.getText());
                    }
                    String policyDeployerServicePassword;
                    String policyDeployerServicePasswordKey = APIConstants.AdvancedThrottleConstants
                            .THROTTLING_CONFIGURATIONS + "." + APIConstants.AdvancedThrottleConstants
                            .POLICY_DEPLOYER_CONFIGURATION + "." + APIConstants.AdvancedThrottleConstants
                            .PASSWORD;
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected
                            (policyDeployerServicePasswordKey)) {
                        policyDeployerServicePassword = secretResolver.resolve
                                (policyDeployerServicePasswordKey);
                    } else {
                        policyDeployerServicePassword = policyDeployerConnectionElement
                                .getFirstChildWithName(new QName(APIConstants
                                        .AdvancedThrottleConstants.PASSWORD)).getText();
                    }
                    policyDeployerConfiguration.setPassword(APIUtil.replaceSystemProperty
                            (policyDeployerServicePassword));
                }
                throttleProperties.setPolicyDeployer(policyDeployerConfiguration);

                //Configuring Block Condition retriever configuration
                OMElement blockConditionRetrieverElement = throttleConfigurationElement.getFirstChildWithName(new
                        QName(APIConstants.AdvancedThrottleConstants.BLOCK_CONDITION_RETRIEVER_CONFIGURATION));

                ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = new
                        ThrottleProperties
                                .BlockCondition();
                if (blockConditionRetrieverElement != null) {
                    OMElement blockConditionRetrieverServiceUrlElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.SERVICE_URL));
                    if (blockConditionRetrieverServiceUrlElement != null) {
                        blockConditionRetrieverConfiguration.setServiceUrl(blockConditionRetrieverServiceUrlElement
                                .getText());
                    }
                    OMElement blockConditionRetrieverServiceUsernameElement = blockConditionRetrieverElement
                            .getFirstChildWithName(new QName(APIConstants.AdvancedThrottleConstants.USERNAME));
                    if (blockConditionRetrieverServiceUsernameElement != null) {
                        blockConditionRetrieverConfiguration.setUsername
                                (blockConditionRetrieverServiceUsernameElement.getText());
                    }
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
                    String blockConditionRetrieverServicePassword;
                    String blockConditionRetrieverServicePasswordKey = APIConstants.AdvancedThrottleConstants
                            .THROTTLING_CONFIGURATIONS + "." + APIConstants.AdvancedThrottleConstants
                            .BLOCK_CONDITION_RETRIEVER_CONFIGURATION + "." + APIConstants.AdvancedThrottleConstants
                            .PASSWORD;
                    if (secretResolver.isInitialized() && secretResolver.isTokenProtected
                            (blockConditionRetrieverServicePasswordKey)) {
                        blockConditionRetrieverServicePassword = secretResolver.resolve
                                (blockConditionRetrieverServicePasswordKey);
                    } else {
                        blockConditionRetrieverServicePassword = blockConditionRetrieverElement
                                .getFirstChildWithName(new QName(APIConstants
                                        .AdvancedThrottleConstants.PASSWORD)).getText();
                    }
                    blockConditionRetrieverConfiguration.setPassword(APIUtil.replaceSystemProperty
                            (blockConditionRetrieverServicePassword));
                }
                throttleProperties.setBlockCondition(blockConditionRetrieverConfiguration);
            }
        } else {
            throttleProperties.setEnabled(false);
        }
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }
}
