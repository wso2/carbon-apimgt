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
package org.apache.synapse.securevault.secret.mbean;

/**
 * Managing MBean for secret
 */
public interface JMXSecretsProviderMBean {

    /**
     * Add a secret through JMX
     *
     * @param id     identify for what this secret is
     * @param secret Secret
     */
    public void addSecret(String id, String secret);

    /**
     * Remove a Secret for given ID
     *
     * @param id identify for what this secret is
     */
    public void removeSecret(String id);

    /**
     * Clear all secrets
     */
    public void clear();
}
