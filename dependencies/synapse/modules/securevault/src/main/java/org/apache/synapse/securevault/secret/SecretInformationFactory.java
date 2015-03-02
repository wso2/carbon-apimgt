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
import org.apache.synapse.securevault.SecretResolver;
import org.apache.synapse.securevault.SecretResolverFactory;
import org.apache.synapse.securevault.SecurityConstants;
import org.apache.synapse.securevault.commons.MiscellaneousUtil;

import java.util.Properties;

/**
 * Factory to create a DataSourceInformation based on given properties
 */

public class SecretInformationFactory {

    private static final Log log = LogFactory.getLog(SecretInformationFactory.class);

    private SecretInformationFactory() {
    }

    /**
     * Factory method to create a SecretInformation instance based on the given properties
     *
     * @param properties          Properties to create a SecretInformation
     * @param configurationPrefix The configuration prefix to use
     * @param passwordPrompt      A specific password prompt to use
     *                            (only for interactive authentication providers)
     * @return SecretInformation instance
     */
    public static SecretInformation createSecretInformation(
            Properties properties, String configurationPrefix, String passwordPrompt) {

        SecretInformation secretInformation = new SecretInformation();

        String user = MiscellaneousUtil.getProperty(
                properties, configurationPrefix + SecurityConstants.PROP_USER_NAME, null,
                String.class);
        if (user != null && !"".equals(user)) {
            secretInformation.setUser(user);
        }

        String password = MiscellaneousUtil.getProperty(
                properties, configurationPrefix + SecurityConstants.PROP_PASSWORD, null,
                String.class);

        if (password != null && !"".equals(password)) {
            secretInformation.setAliasSecret(password);
        }

        SecretResolver secretResolver = SecretResolverFactory.create(properties,
                configurationPrefix);
        secretInformation.setLocalSecretResolver(secretResolver);
        secretInformation.setSecretPrompt(passwordPrompt);

        return secretInformation;
    }

    /**
     * Factory method to create a SecretInformation instance based on the given information
     *
     * @param secretProvider A SecretCallbackHandler implementation to use to get secrets
     * @param aliasPassword  The  alias password
     * @param passwordPrompt A specific password prompt to use
     *                       (only for interactive authentication providers)
     * @return SecretInformation instance
     */
    public static SecretInformation createSecretInformation(String secretProvider,
                                                            String aliasPassword,
                                                            String passwordPrompt) {

        SecretInformation secretInformation = new SecretInformation();
        secretInformation.setAliasSecret(aliasPassword);
        SecretResolver secretResolver = new SecretResolver();
        secretResolver.init(
                SecretCallbackHandlerFactory.createSecretCallbackHandler(secretProvider));
        secretInformation.setLocalSecretResolver(secretResolver);
        secretInformation.setSecretPrompt(passwordPrompt);
        return secretInformation;
    }
}
