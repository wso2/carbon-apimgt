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

package org.apache.synapse.commons.beanstalk.enterprise;

/**
 * Holds constants used in the enterprise beanstalk configuration.
 */
public interface EnterpriseBeanstalkConstants {

    /**
     * Prefix for beanstalk related configuration property names in synapse.properties.
     */
    public static final String SYNAPSE_BEANSTALK_PREFIX = "synapse.beanstalks";

    /**
     * Cache timeout in minutes for stateless session beans
     */
    public static final String STATELESS_BEANS_TIMEOUT = "cache.timeout.stateless";

    /**
     * Cache timeout in minutes for stateful session beans
     */
    public static final String STATEFUL_BEANS_TIMEOUT = "cache.timeout.stateful";

    /**
     * Warn limit for stateless session beans. A warning is generated when more than this many of
     * stateless bean stubs are cached in this beanstalk.
     */
    public static final String STATELESS_BEANS_WARN_LIMIT = "cache.warn.limit.stateless";

    /**
     * Warn limit for stateless session beans. A warning is generated when more than this many of
     * stateless bean stubs are cached in this beanstalk.
     */
    public static final String STATEFUL_BEANS_WARN_LIMIT = "cache.warn.limit.stateful";

    /**
     * ServerContextInformation property name of the BeanstalkManager
     */
    public static final String BEANSTALK_MANAGER_PROP_NAME = "__SYNAPSE.BEANSTALK.MANAGER";

    /**
     * Category name for JMX MBeans registered to monitor beanstalks.
     */
    public static final String BEANSTALK_MBEAN_CATEGORY_NAME = "EnterpriseBeanstalk";

}
