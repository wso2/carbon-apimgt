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

package org.apache.synapse.commons.jmx;

public class JmxConfigurationConstants {
    
     /** Token for jmx password*/
    public static final String JMX_PROTECTED_TOKEN = "jmx.password";
     /** Prefix for all properties in property file*/
    public static final String PROP_SYNAPSE_PREFIX_JMX = "synapse.jmx.";

    /** JNDI port property used for the JMX naming directory (RMI registry) */
    public static final String PROP_JNDI_PORT = "jndiPort";
    
    /** RMI port property used to configure the JMX RMI port (firewalled setup) */
    public static final String PROP_RMI_PORT = "rmiPort";

    /** Hostname property used to configure JMX Adapter */
    public static final String PROP_HOSTNAME = "hostname";
    
    /** Property for location of remote access file. */
    public static final String PROP_REMOTE_ACCESS_FILE = "remote.access.file";
    
    /** Property to activate remote SSL support (same as com.sun.management.jmxremote.ssl) */
    public static final String PROP_REMOTE_SSL = "remote.ssl";

    public static final String PROP_THREAD_JMX_ENABLE = "synapse.jmx.thread.view.enabled";
    
}
