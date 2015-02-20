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
package org.apache.synapse.commons.datasource;

import java.util.Map;

/**
 * MBean for retrieving some statistics about the connection pool
 */
public interface DBPoolViewMBean {
    /**
     * Number of active connections
     *
     * @return <code>int</code> Number of active connections
     */
    public int getNumActive();

    /**
     * Number of idle connections
     *
     * @return <code>int</code> Number of idle connections
     */
    public int getNumIdle();

    /**
     * Data source name
     *
     * @return <code>String</code> data source name
     */
    public String getName();

    /**
     * Connection information as a string
     *
     * @return <code>String</code> representing connection information
     */
    public Map getConnectionUsage();

    /**
     * reset statistics
     */
    public void reset();
}
