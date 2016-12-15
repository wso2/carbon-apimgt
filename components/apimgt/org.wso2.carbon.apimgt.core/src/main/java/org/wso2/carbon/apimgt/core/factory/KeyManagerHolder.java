/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.AMDefaultKeyManagerImpl;



/**
 * This is a factory class.you have to use this when you need to initiate classes by reading config file.
 * for example key manager class will be initiate from here.
 */
public class KeyManagerHolder {
    private static final Logger LOG = LoggerFactory.getLogger(KeyManagerHolder.class);
    private static KeyManager keyManager = null;


    /**
     * Set the keymanager implementation class
     *
     * @throws org.wso2.carbon.apimgt.core.exception.KeyManagementException
     */
    public static void initializeKeyManager()
            throws KeyManagementException {
        keyManager = new AMDefaultKeyManagerImpl();
        //TO-DO- Dynamically set the keymanager based on a config


    }

    /**
     * This method will take hardcoded class name from api-manager.xml file and will return that class's instance.
     * This class should be implementation class of keyManager.
     *
     * @return keyManager instance.
     */
    public static KeyManager getKeyManagerInstance() {
        if (keyManager != null) {
            return keyManager;
        } else {
            try {
                initializeKeyManager();

            } catch (KeyManagementException e) {
                LOG.error("Error while initialzing the keymanager implementation.", e);
            }

        }
        return keyManager;
    }

}
