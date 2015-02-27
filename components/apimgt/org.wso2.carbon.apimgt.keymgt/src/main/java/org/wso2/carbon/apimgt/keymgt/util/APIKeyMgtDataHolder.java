/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.token.JWTGenerator;
import org.wso2.carbon.apimgt.impl.token.TokenGenerator;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class APIKeyMgtDataHolder {

    private static RegistryService registryService;
    private static RealmService realmService;
    private static APIManagerConfigurationService amConfigService;
    private static Boolean isJWTCacheEnabledKeyMgt = true;
    private static Boolean isKeyCacheEnabledKeyMgt = true;
    private static Boolean isThriftServerEnabled = true;
    private static TokenGenerator tokenGenerator;
    private static boolean jwtGenerationEnabled = false;
    private static final Log log = LogFactory.getLog(APIKeyMgtDataHolder.class);
    private static KeyManager keyManager = null;

    // Scope used for marking Application Tokens
    private static String applicationTokenScope;

    public static Boolean isJWTCacheEnabledKeyMgt() {
        return isJWTCacheEnabledKeyMgt;
    }

    public static void setJWTCacheEnabledKeyMgt(Boolean JWTCacheEnabledKeyMgt) {
        isJWTCacheEnabledKeyMgt = JWTCacheEnabledKeyMgt;
    }

    public static Boolean getKeyCacheEnabledKeyMgt() {
        return isKeyCacheEnabledKeyMgt;
    }

    public static void setKeyCacheEnabledKeyMgt(Boolean keyCacheEnabledKeyMgt) {
        isKeyCacheEnabledKeyMgt = keyCacheEnabledKeyMgt;
    }

    public static void initializeKeyManager(String configPath) throws APIManagementException {

        InputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(configPath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            OMElement document = builder.getDocumentElement();
            if (document == null || !APIConstants.KEY_MANAGER.equals(document.getQName().getLocalPart())) {
                throw new APIManagementException("KeyManager section not found. key-manager.xml may be corrupted.");
            }

            log.debug("Reading key-manager.xml");

            String clazz = document.getAttribute(new QName("class")).getAttributeValue();
            keyManager = (KeyManager) Class.forName(clazz).newInstance();

            log.debug("Initialised KeyManager implementation");

            OMElement configElement = document.getFirstElement();
            if (!"Configuration".equals(configElement.getQName().getLocalPart())) {
                throw new APIManagementException("Configuration section not found. key-manager.xml may be corrupted.");
            }

            log.debug("Loading KeyManager configuration,");

            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter streamWriter;
            StringWriter stringStream = new StringWriter();
            streamWriter = xof.createXMLStreamWriter(stringStream);
            configElement.serialize(streamWriter);
            streamWriter.close();
            keyManager.loadConfiguration(stringStream.toString());

            log.debug("Successfully loaded KeyManager configuration.");

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new APIManagementException("I/O error while reading the API manager " +
                                             "configuration: " + configPath, e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage());
            throw new APIManagementException("Error while parsing the API manager " +
                                             "configuration: " + configPath, e);
        } catch (OMException e) {
            log.error(e.getMessage());
            throw new APIManagementException("Error while parsing API Manager configuration: " + configPath, e);
        } catch (ClassNotFoundException e) {
            log.error("Error occurred while instantiating KeyManager implementation");
            throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (APIManagementException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


    public static APIManagerConfigurationService getAmConfigService() {
        return amConfigService;
    }

    public static void setAmConfigService(APIManagerConfigurationService amConfigService) {
        APIKeyMgtDataHolder.amConfigService = amConfigService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        APIKeyMgtDataHolder.registryService = registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        APIKeyMgtDataHolder.realmService = realmService;
    }

    public static Boolean getThriftServerEnabled() {
        return isThriftServerEnabled;
    }

    public static void setThriftServerEnabled(Boolean thriftServerEnabled) {
        isThriftServerEnabled = thriftServerEnabled;
    }

    public static void initData() {
        try {
            APIKeyMgtDataHolder.isJWTCacheEnabledKeyMgt = getInitValues(APIConstants.API_KEY_MANAGER_ENABLE_JWT_CACHE);
            APIKeyMgtDataHolder.isKeyCacheEnabledKeyMgt = getInitValues(APIConstants.API_KEY_MANAGER_ENABLE_VALIDATION_INFO_CACHE);
            APIKeyMgtDataHolder.isThriftServerEnabled = getInitValues(APIConstants.API_KEY_MANAGER_ENABLE_THRIFT_SERVER);

            APIManagerConfiguration configuration = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();

            if (configuration == null) {
                log.error("API Manager configuration is not initialized");
            } else {
                applicationTokenScope = configuration.getFirstProperty(APIConstants
                                                                               .API_KEY_MANAGER_APPLICATION_TOKEN_SCOPE);
                jwtGenerationEnabled = Boolean.parseBoolean(configuration.getFirstProperty(APIConstants
                                                                                                   .ENABLE_JWT_GENERATION));
                if (log.isDebugEnabled()) {
                    log.debug("JWTGeneration enabled : " + jwtGenerationEnabled);
                }

                if (jwtGenerationEnabled) {
                    String clazz = configuration.getFirstProperty(APIConstants.TOKEN_GENERATOR_IMPL);
                    if (clazz == null) {
                        tokenGenerator = new JWTGenerator();
                    } else {
                        try {
                            tokenGenerator = (TokenGenerator) Class.forName(clazz).newInstance();
                        } catch (InstantiationException e) {
                            log.error("Error while instantiating class " + clazz, e);
                        } catch (IllegalAccessException e) {
                            log.error(e);
                        } catch (ClassNotFoundException e) {
                            log.error("Cannot find the class " + clazz + e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occur while initializing API KeyMgt Data Holder.Default configuration will be used." + e.toString());
        }
    }

    private static boolean getInitValues(String constVal) {
        String val = getAmConfigService().getAPIManagerConfiguration().getFirstProperty(constVal);
        if (val != null) {
            return Boolean.parseBoolean(val);
        }
        return false;
    }

    public static boolean isJwtGenerationEnabled(){
        return jwtGenerationEnabled;
    }

    // Returns the implementation for JWTTokenGenerator.
    public static TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public static String getApplicationTokenScope() {
        return applicationTokenScope;
    }
}
