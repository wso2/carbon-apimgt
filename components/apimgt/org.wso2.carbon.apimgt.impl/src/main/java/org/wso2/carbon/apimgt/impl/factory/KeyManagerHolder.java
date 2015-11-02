/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.factory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerHolder {


    private static Log log = LogFactory.getLog(KeyManagerHolder.class);
    private static KeyManager keyManager = null;


    /**
     * Read values from APIManagerConfiguration.
     *
     * @param apiManagerConfiguration API Manager Configuration
     * @throws APIManagementException
     */
    public static void initializeKeyManager(APIManagerConfiguration apiManagerConfiguration)
            throws APIManagementException {
        if (apiManagerConfiguration != null) {
            try {
                // If APIKeyManager section is disabled, we are reading values defined in APIKeyValidator section.
                if (apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT) == null) {
                    //keyManager = (KeyManager) Class.forName("org.wso2.carbon.apimgt.keymgt.AMDefaultKeyManagerImpl").newInstance();
                    keyManager = new AMDefaultKeyManagerImpl();
                    keyManager.loadConfiguration(null);
                } else {
                    // If APIKeyManager section is enabled, class name is picked from there.
                    String clazz = apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT);
                    keyManager = (KeyManager) APIUtil.getClassForName(clazz).newInstance();
                    Set<String> configKeySet = apiManagerConfiguration.getConfigKeySet();

                    KeyManagerConfiguration keyManagerConfiguration = new KeyManagerConfiguration();

                    // Iterating through the Configuration and seeing which elements are starting with APIKeyManager
                    // .Configuration. Values of those keys will be set in KeyManagerConfiguration object.
                    String startKey = APIConstants.API_KEY_MANAGER + "Configuration.";
                    for (String configKey : configKeySet) {
                        if (configKey.startsWith(startKey)) {
                            keyManagerConfiguration.addParameter(configKey.replace(startKey, ""),
                                                                 apiManagerConfiguration.getFirstProperty(configKey));
                        }
                    }

                    // Set the created configuration in the KeyManager instance.
                    keyManager.loadConfiguration(keyManagerConfiguration);
                }
            } catch (ClassNotFoundException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            } catch (InstantiationException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            } catch (IllegalAccessException e) {
                log.error("Error occurred while instantiating KeyManager implementation");
                throw new APIManagementException("Error occurred while instantiating KeyManager implementation", e);
            }
        }
    }

    /**
     * This method will take hardcoded class name from api-manager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     *
     * @return keyManager instance.
     */
    public static KeyManager getKeyManagerInstance() {
        return keyManager;
    }

}
