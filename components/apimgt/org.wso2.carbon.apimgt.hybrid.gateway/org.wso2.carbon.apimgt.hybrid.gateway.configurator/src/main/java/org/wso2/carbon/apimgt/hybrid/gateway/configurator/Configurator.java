/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.TokenUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.configurator.dto.MicroGatewayInitializationDTO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configurator class for Micro Gateway Configuration specific to WSO2 API Cloud
 */
public class Configurator {

    private static final Log log = LogFactory.getLog(Configurator.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Main method to handle Micro Gateway Configuration
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        String carbonHome = System.getProperty(ConfigConstants.CARBON_HOME);
        if (carbonHome == null || carbonHome.isEmpty()) {
            log.error("Carbon Home has not been set. Startup will be cancelled.");
            Runtime.getRuntime().exit(1);
        }
        if (args.length < 3 || carbonHome == null || carbonHome.isEmpty()) {
            log.error("Required arguments are not provided. Startup will be cancelled.\n" +
                              "Required:\n" +
                              "\t(1) Email\n" +
                              "\t(2) Password\n" +
                              "\t(3) Organization Key\n");
            Runtime.getRuntime().exit(1);
        }
        String carbonConfigDirPath = carbonHome + File.separator + ConfigConstants.REPOSITORY_DIR + File.separator
                                             + ConfigConstants.CONF_DIR;
        //Update Gateway properties file with API cloud related configs
        String cloudConfigsPath = carbonHome + File.separator + ConfigConstants.RESOURCES_DIR + File.separator
                + ConfigConstants.CLOUD_CONFIG_DIR + File.separator + ConfigConstants.CLOUD_CONFIG_FILE_NAME;
        String gatewayConfigPath = carbonConfigDirPath + File.separator + OnPremiseGatewayConstants.CONFIG_FILE_NAME;
        updateGatewayConfigDetails(cloudConfigsPath, gatewayConfigPath);
        //Read Gateway properties
        Properties gatewayProperties = getGatewayProperties(gatewayConfigPath, args);
        String configToolPropertyFilePath = carbonConfigDirPath + File.separator +
                                                    ConfigConstants.CONFIG_TOOL_CONFIG_FILE_NAME;
        //Configure api-manager.xml
        Properties configToolProperties = readPropertiesFromFile(configToolPropertyFilePath);
        setAPIMConfigurations(configToolProperties, carbonHome, gatewayProperties);
        //Configure registry.xml
        RegistryXmlConfigurator registryXmlConfigurator = new RegistryXmlConfigurator();
        registryXmlConfigurator.configure(carbonConfigDirPath, gatewayProperties);
        //Configure log4j.properties
        Log4JConfigurator log4JConfigurator = new Log4JConfigurator();
        log4JConfigurator.configure(carbonConfigDirPath);
        writeConfiguredLock(carbonHome);
        try {
            initializeOnPremGateway(gatewayProperties.getProperty(ConfigConstants.INITIALIZATION_API_URL),
                                    carbonConfigDirPath, args);
        } catch (OnPremiseGatewayException | IOException e) {
            log.error("Error while initializing gateway.", e);
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Configure api-manager.xml with given properties
     *
     * @param configToolProperties Properties
     * @param carbonHome String
     * @param gatewayProperties Properties
     */
    protected static void setAPIMConfigurations(Properties configToolProperties, String carbonHome,
                                                Properties gatewayProperties) {
        Map<String, Map<String, String>> fileMap = new HashMap<>();
        for (Map.Entry entry : configToolProperties.entrySet()) {
            String xpathKey = (String) entry.getKey();
            String[] values = ((String) entry.getValue()).split("::");
            String file = values[0];
            String gwPropertyKey = values[1];
            Map<String, String> xpathMap;
            if (fileMap.containsKey(file)) {
                xpathMap = fileMap.get(file);
            } else {
                xpathMap = new HashMap<>();
            }
            xpathMap.put(xpathKey, gwPropertyKey);
            fileMap.put(file, xpathMap);
        }
        XmlConfigurator xmlConfigurator = new XmlConfigurator();
        xmlConfigurator.configure(carbonHome, gatewayProperties, fileMap);
    }

    /**
     * Initialize the Micro Gateway in Cloud
     *
     * @param initApiUrl String
     * @param carbonConfigDirPath String
     * @param args String[]
     * @throws OnPremiseGatewayException
     * @throws IOException
     */
    private static void initializeOnPremGateway(String initApiUrl, String carbonConfigDirPath, String[] args)
            throws OnPremiseGatewayException, IOException {
        //Collect device details
        Map<String, String> deviceDetails = getDeviceDetails();
        String carbonFilePath = carbonConfigDirPath + File.separator + ConfigConstants.GATEWAY_CARBON_FILE_NAME;
        int port = getGatewayPort(carbonFilePath);
        deviceDetails.put(ConfigConstants.PORT, Integer.toString(port));
        String payload = getInitializationPayload(deviceDetails, args);
        String authHeader = createAuthHeader(args);
        String token = callInitializationAPI(initApiUrl, authHeader, payload);
        //Update token in gateway properties file
        String gatewayConfigPath = carbonConfigDirPath + File.separator + OnPremiseGatewayConstants.CONFIG_FILE_NAME;
        updateOnPremGatewayUniqueId(gatewayConfigPath, token);
    }

    /**
     * Update the default Micro Gateway configs with Cloud specific configs
     *
     * @param cloudConfigsPath String
     * @param gatewayConfigPath String
     */
    protected static void updateGatewayConfigDetails(String cloudConfigsPath, String gatewayConfigPath) {
        File gatewayConfigFile = new File(gatewayConfigPath);
        File cloudConfigFile = new File(cloudConfigsPath);
        try {
            String cloudConfigContent = FileUtils.readFileToString(cloudConfigFile,
                                                                   OnPremiseGatewayConstants.DEFAULT_CHARSET);
            FileUtils.writeStringToFile(gatewayConfigFile, cloudConfigContent,
                                        OnPremiseGatewayConstants.DEFAULT_CHARSET);
        } catch (IOException e) {
            log.error("Error occurred while updating default Gateway configs with Cloud configs", e);
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Update the Micro Gateway property file with the unique identifier
     *
     * @param gatewayConfigPath String
     * @param token String
     */
    protected static void updateOnPremGatewayUniqueId(String gatewayConfigPath, String token) {
        File gatewayConfigFile = new File(gatewayConfigPath);
        try {
            String gatewayConfigContent = FileUtils.readFileToString(gatewayConfigFile,
                                                                     OnPremiseGatewayConstants.DEFAULT_CHARSET);
            gatewayConfigContent = gatewayConfigContent.replace(OnPremiseGatewayConstants.UNIQUE_IDENTIFIER_HOLDER, token);
            FileUtils.writeStringToFile(gatewayConfigFile, gatewayConfigContent,
                                        OnPremiseGatewayConstants.DEFAULT_CHARSET);
        } catch (IOException e) {
            log.error("Error occurred while updating token in Gateway property file", e);
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Generate authentication header
     *
     * @param args
     * @return
     * @throws IOException
     */
    protected static String createAuthHeader(String[] args) throws IOException {
        //Order of args - email, tenantDomain, password
        String username = args[0] + OnPremiseGatewayConstants.USERNAME_SEPARATOR + args[1];
        char[] password = args[2].toCharArray();
        return TokenUtil.getBasicAuthHeaderValue(username, password);
    }

    /**
     * Call Micro Gateway initialization API and get a unique identifier
     *
     * @param initApiUrl String
     * @param authHeaderValue String
     * @param payload String
     * @return token String
     * @throws OnPremiseGatewayException
     * @throws IOException
     */
    private static String callInitializationAPI(String initApiUrl, String authHeaderValue, String payload) throws
            OnPremiseGatewayException, IOException {
        String token = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(initApiUrl);
        httpPost.addHeader(AUTHORIZATION_HEADER, authHeaderValue);
        httpPost.addHeader(OnPremiseGatewayConstants.CONTENT_TYPE_HEADER,
                           OnPremiseGatewayConstants.CONTENT_TYPE_APPLICATION_JSON);
        httpPost.setEntity(new StringEntity(payload));
        token = HttpRequestUtil.executeHTTPMethodWithRetry(httpClient, httpPost,
                                                           OnPremiseGatewayConstants.DEFAULT_RETRY_COUNT);
        return token;
    }

    /**
     * Get a JSON String with the details required to initialize Micro Gateway
     *
     * @param deviceDetails Map<String, String>
     * @param args String[]
     * @return details String
     * @throws IOException
     */
    protected static String getInitializationPayload(Map<String, String> deviceDetails,
                                                     String[] args) throws IOException {
        //Create object
        MicroGatewayInitializationDTO microGatewayInitializationDTO = new MicroGatewayInitializationDTO();
        //Order of args - email, tenantDomain, password
        microGatewayInitializationDTO.setTenantDomain(args[1]);
        microGatewayInitializationDTO.setMacAddress(deviceDetails.get(ConfigConstants.MAC_ADDRESS));
        microGatewayInitializationDTO.setPort(deviceDetails.get(ConfigConstants.PORT));
        microGatewayInitializationDTO.setHostName(deviceDetails.get(ConfigConstants.HOST_NAME));
        //Convert to JSON string
        ObjectMapper mapper = new ObjectMapper();
        String details = mapper.writeValueAsString(microGatewayInitializationDTO);
        return details;
    }

    /**
     * Write configuration lock after the configuration is complete
     *
     * @param carbonHome String
     */
    protected static void writeConfiguredLock(String carbonHome) {
        String filePath = carbonHome + File.separator + ConfigConstants.CONFIGURE_LOCK_FILE_NAME;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            String content = "configured";
            fileOutputStream = new FileOutputStream(filePath);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(content);
            bufferedWriter.flush();
        } catch (IOException e) {
            log.error("Error occurred while writing the lock. ", e);
        } finally {
            String msg = "Error occurred while closing the output writers for : " + filePath;
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                log.warn(msg, e);
            }
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
            } catch (IOException e) {
                log.warn(msg, e);
            }
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                log.warn(msg, e);
            }
        }
    }

    /**
     * Read the Properties from a given property file
     *
     * @param filePath String
     * @return properties
     */
    protected static Properties readPropertiesFromFile(String filePath) {
        InputStream inputStream = null;
        Properties properties = new Properties();
        try {
            inputStream = new FileInputStream(filePath);
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("Error occurred while reading the property file " + filePath, e);
            Runtime.getRuntime().exit(1);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.warn("Unable to close the Input Stream for : " + filePath, e);
                }
            }
        }
        return properties;
    }

    /**
     * Retrieve Gateway properties from file and add missing properties
     *
     * @param configFilePath String
     * @param args String[]
     * @return Properties
     */
    protected static Properties getGatewayProperties(String configFilePath, String[] args) {
        Properties gatewayProperties = readPropertiesFromFile(configFilePath);
        String email = args[0];
        gatewayProperties.put(ConfigConstants.EMAIL, email);
        String tenantDomain = args[1];
        gatewayProperties.put(ConfigConstants.TENANT_DOMAIN, tenantDomain);
        char[] password = args[2].toCharArray();
        gatewayProperties.put(ConfigConstants.PASSWORD, String.valueOf(password));
        gatewayProperties.put(ConfigConstants.USERNAME,
                              email + OnPremiseGatewayConstants.USERNAME_SEPARATOR + tenantDomain);
        //Following are default values for some of the configs, which will be set for a public cloud setup. These can be
        //overridden by adding the respective property to on-premise-gateway.properties file
        if (gatewayProperties.containsKey(ConfigConstants.PUBLIC_CLOUD_SETUP)
                    && Boolean.parseBoolean(gatewayProperties.getProperty(ConfigConstants.PUBLIC_CLOUD_SETUP))) {
            if (!gatewayProperties.containsKey(ConfigConstants.ANALYTICS_ENABLED)) {
                gatewayProperties.put(ConfigConstants.ANALYTICS_ENABLED, "true");
            }
            if (!gatewayProperties.containsKey(ConfigConstants.API_KEY_VALIDATION_CLIENT_TYPE)) {
                gatewayProperties.put(ConfigConstants.API_KEY_VALIDATION_CLIENT_TYPE, "WSClient");
            }
            if (!gatewayProperties.containsKey(ConfigConstants.FILE_DATA_PUBLISHER_CLASS)) {
                gatewayProperties.put(ConfigConstants.FILE_DATA_PUBLISHER_CLASS,
                                      ConfigConstants.DEFAULT_FILE_DATA_PUBLISHER_CLASS);
            }
            //In a public cloud setup use StratosPublicCloudSetup as false to create tenants without a domain
            gatewayProperties.put(ConfigConstants.STRATOS_PUBLIC_CLOUD_SETUP, "false");
        }
        return gatewayProperties;
    }

    /**
     * Retrieve host name, mac address of the device
     *
     * @return details Map<String, String>
     */
    protected static Map<String, String> getDeviceDetails() {
        InetAddress ip;
        String hostName = "";
        String macAddress = ConfigConstants.DEFAULT_MAC_ADDRESS;
        Map<String, String> details = new HashMap();
        try {
            ip = InetAddress.getLocalHost();
            hostName = ip.getHostName();
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                Enumeration<InetAddress> enumeration = networkInterface.getInetAddresses();
                for (; enumeration.hasMoreElements(); ) {
                    InetAddress address = enumeration.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() && address.isSiteLocalAddress()) {
                        byte[] mac = networkInterface.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < mac.length; i++) {
                                //Construct mac address
                                sb.append(String.format("%02X%s", mac[i],
                                        (i < mac.length - 1) ? ConfigConstants.DELIMITER : ""));
                            }
                            macAddress = sb.toString();
                            break;
                        }
                    }
                }
            }
        } catch (UnknownHostException | SocketException e) {
            log.error("Error while retrieving mac address", e);
            Runtime.getRuntime().exit(1);
        }
        details.put(ConfigConstants.HOST_NAME, hostName);
        details.put(ConfigConstants.MAC_ADDRESS, macAddress);
        return details;
    }

    /**
     * Retrieve Gateway port based on the configured port offset
     *
     * @param carbonFilePath String
     * @return port int
     */
    protected static int getGatewayPort(String carbonFilePath) {
        int port = 0;
        try {
            File file = new File(carbonFilePath);
            String carbonXMLContent = FileUtils.readFileToString(file);
            String offsetStr = StringUtils.substringBetween(carbonXMLContent, ConfigConstants.START_OFFSET_TAG,
                                                            ConfigConstants.END_OFFSET_TAG);
            port = ConfigConstants.GATEWAY_DEFAULT_PORT + Integer.parseInt(offsetStr);
        } catch (IOException e) {
            log.error("Error occurred while reading the carbon XML.", e);
            Runtime.getRuntime().exit(1);
        }
        return port;
    }
}
