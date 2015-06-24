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
package org.apache.synapse.securevault.secret.handler;

import org.apache.synapse.securevault.commons.MBeanRegistrar;
import org.apache.synapse.securevault.secret.AbstractSecretCallbackHandler;
import org.apache.synapse.securevault.secret.SingleSecretCallback;
import org.apache.synapse.securevault.secret.mbean.JMXSecretsProvider;

/**
 * Get and propagates secrets that have been collected through JMX Mean
 */
public class JMXSecretCallbackHandler extends AbstractSecretCallbackHandler {

    private static JMXSecretsProvider JMXSecretsMBean;

    static {
        JMXSecretsMBean = new JMXSecretsProvider();
        MBeanRegistrar.getInstance().registerMBean(JMXSecretsMBean, "SecretsProvider",
                "SecretsProvider");
    }

    protected void handleSingleSecretCallback(SingleSecretCallback singleSecretCallback) {
        String id = singleSecretCallback.getId();
        if (id != null && !"".equals(id)) {
            singleSecretCallback.setSecret(JMXSecretsMBean.getSecret(id));
        }
    }
}
