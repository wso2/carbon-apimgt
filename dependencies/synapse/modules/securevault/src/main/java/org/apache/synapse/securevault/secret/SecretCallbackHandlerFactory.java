/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.securevault.secret;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.SecureVaultException;
import org.apache.synapse.securevault.commons.MiscellaneousUtil;

import java.util.Properties;

/**
 * Contains factory methods to create SecretCallbackHandler
 */
public class SecretCallbackHandlerFactory {

    private static final Log log = LogFactory.getLog(SecretCallbackHandlerFactory.class);

    /**
     * Creates a   SecretCallbackHandler instance based on given properties
     * At future , can extend this , if SecretCallbackHandler instance need
     * some data in the initialization
     *
     * @param properties properties contains some information about how to create a SecretCallbackHandler
     * @param key        use for getting related properties out of all properties
     * @return SecretCallbackHandler instance
     */
    public static SecretCallbackHandler createSecretCallbackHandler(Properties properties,
                                                                    String key) {
        String provider = MiscellaneousUtil.getProperty(properties, key, null);
        return createSecretCallbackHandler(provider);

    }

    /**
     * Creates a SecretCallbackHandler instance based on provided class
     *
     * @param provider provider class name
     * @return SecretCallbackHandler instance
     */
    public static SecretCallbackHandler createSecretCallbackHandler(String provider) {

        if (provider != null && !"".equals(provider)) {

            try {
                Class aClass = Thread.currentThread().getContextClassLoader().loadClass(provider);
                Object instance = aClass.newInstance();

                if (instance != null && instance instanceof SecretCallbackHandler) {
                    return (SecretCallbackHandler) instance;
                } else {
                    handleException("Invalid class as SecretCallbackHandler : Class Name : " +
                            provider);
                }

            } catch (ClassNotFoundException e) {
                handleException("A SecretCallbackHandler cannot be found for class name : " +
                        provider, e);
            } catch (IllegalAccessException e) {
                handleException("Error creating a instance from class : " + provider, e);
            } catch (InstantiationException e) {
                handleException("Error creating a instance from class : " + provider, e);
            }
        }
        return null;
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     * @param e   Thorwen Exception
     */
    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SecureVaultException(msg, e);
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     */
    private static void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }
}
