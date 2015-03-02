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

package org.apache.synapse.transport.nhttp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Store and manage properties that tune the nhttp transport
 */
public final class NHttpConfiguration {

    // defaults
    private static final int WORKERS_CORE_THREADS  = 20;
    private static final int WORKERS_MAX_THREADS   = 100;
    private static final int WORKER_KEEP_ALIVE     = 5;
    private static final int BLOCKING_QUEUE_LENGTH = -1;
    private static final int IO_WORKER_COUNT = 2;
    private static final int BUFFER_SIZE           = 8192;
    public static final int MAX_ACTIVE_CON = -1;

    // server listener
    private static final String S_T_CORE     = "snd_t_core";
    private static final String S_T_MAX      = "snd_t_max";
    private static final String S_T_ALIVE    = "snd_alive_sec";
    private static final String S_T_QLEN     = "snd_qlen";
    private static final String S_IO_WORKERS = "snd_io_threads";
    private static final String C_MAX_ACTIVE = "max_open_connections";

    // client sender
    private static final String C_T_CORE     = "lst_t_core";
    private static final String C_T_MAX      = "lst_t_max";
    private static final String C_T_ALIVE    = "lst_alive_sec";
    private static final String C_T_QLEN     = "lst_qlen";
    private static final String C_IO_WORKERS = "lst_io_threads";

    // general
    private static final String G_BUFFER_SIZE  = "nhttp_buffer_size";
    private static final String G_DISABLED_HTTP_METHODS = "nhttp_disabled_methods";
  
    //additional rest dispatch handlers
    private static final String NHTTP_REST_DISPATCHER_SERVICE="nhttp.rest.dispatcher.service";
    // URI configurations that determine if it requires custom rest dispatcher
    private static final String REST_URI_API_REGEX = "rest_uri_api_regex";
    private static final String REST_URI_PROXY_REGEX = "rest_uri_proxy_regex";

    public static final String TRANSPORT_LISTENER_SHUTDOWN_WAIT_TIME = "transport.listener.shutdown.wait.sec";
    public static final int DEFAULT_LISTENER_SHUTDOWN_WAIT_TIME = 0;

    private static final Log log = LogFactory.getLog(NHttpConfiguration.class);
    private static NHttpConfiguration _instance = new NHttpConfiguration();
    private Properties props;
    List<String> methods;

    /** Comma separated list of blocked uris*/
    public static final String BLOCK_SERVICE_LIST = "http.block_service_list";
    /** Default value for BLOCK_SERVICE_LIST*/
    public static final String BLOCK_SERVICE_LIST_DEFAULT = "false";
    
    private NHttpConfiguration() {
        try {
            props = MiscellaneousUtil.loadProperties("nhttp.properties");
        } catch (Exception ignore) {}
    }

    public static NHttpConfiguration getInstance() {
        return _instance;
    }

    public int getServerCoreThreads() {
        return getProperty(S_T_CORE, WORKERS_CORE_THREADS);
    }

    public int getServerMaxThreads() {
        return getProperty(S_T_MAX, WORKERS_MAX_THREADS);
    }

    public int getServerKeepalive() {
        return getProperty(S_T_ALIVE, WORKER_KEEP_ALIVE);
    }

    public int getServerQueueLen() {
        return getProperty(S_T_QLEN, BLOCKING_QUEUE_LENGTH);
    }

    public int getServerIOWorkers() {
        return getProperty(S_IO_WORKERS, IO_WORKER_COUNT);
    }


    public int getClientCoreThreads() {
        return getProperty(C_T_CORE, WORKERS_CORE_THREADS);
    }

    public int getClientMaxThreads() {
        return getProperty(C_T_MAX, WORKERS_MAX_THREADS);
    }

    public int getClientKeepalive() {
        return getProperty(C_T_ALIVE, WORKER_KEEP_ALIVE);
    }

    public int getClientQueueLen() {
        return getProperty(C_T_QLEN, BLOCKING_QUEUE_LENGTH);
    }

    public int getClientIOWorkers() {
        return getProperty(C_IO_WORKERS, IO_WORKER_COUNT);
    }
    
    public int getMaxActiveConnections() {
    	return getProperty(C_MAX_ACTIVE, MAX_ACTIVE_CON);
    }    

    public int getBufferSize() {
        return getProperty(G_BUFFER_SIZE, BUFFER_SIZE);
    }

    public boolean isKeepAliveDisabled() {
        return getProperty(NhttpConstants.DISABLE_KEEPALIVE, 0) == 1;
    }

    public boolean isPreserveUserAgentHeader() {
        return getBooleanValue(NhttpConstants.USER_AGENT_HEADER_PRESERVE, false);
    }

    public boolean isPreserveServerHeader() {
        return getBooleanValue(NhttpConstants.SERVER_HEADER_PRESERVE, true);
    }

    public boolean isCountConnections() {
        return getBooleanValue(NhttpConstants.COUNT_CONNECTIONS, false);
    }

    public String isServiceListBlocked() {
        return getStringValue(BLOCK_SERVICE_LIST, BLOCK_SERVICE_LIST_DEFAULT);
    }
    
    public String getRESTDispatchService() {
        return getStringValue(NHTTP_REST_DISPATCHER_SERVICE,"");
    }

    public String getRestUriApiRegex() {
        return getStringValue(REST_URI_API_REGEX, "");
    }

    public String getRestUriProxyRegex() {
        return getStringValue(REST_URI_PROXY_REGEX, "");
    }

    public int getListenerShutdownWaitTime() {
        return getProperty(TRANSPORT_LISTENER_SHUTDOWN_WAIT_TIME, DEFAULT_LISTENER_SHUTDOWN_WAIT_TIME)*1000;
    }

    /**
     * Get properties that tune nhttp transport. Preference to system properties
     * @param name name of the system/config property
     * @param def default value to return if the property is not set
     * @return the value of the property to be used
     */
    public int getProperty(String name, int def) {
        String val = System.getProperty(name);
        if (val == null) {
            val = props.getProperty(name);
        }

        if (val!=null) {
            val = val.trim();
        }

        if (val != null && Integer.valueOf(val) > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Using nhttp tuning parameter : " + name + " = " + val);
            }
            return Integer.valueOf(val);
        }        
        return def;
    }

    /**
     * Get properties that tune nhttp transport. Preference to system properties
     * @param name name of the system/config property
     * @param def default value to return if the property is not set
     * @return the value of the property to be used
     */
    public boolean getBooleanValue(String name, boolean def) {
        String val = System.getProperty(name);
        if (val == null) {
            val = props.getProperty(name);
        }

        if (val != null && Boolean.parseBoolean(val)) {
            if (log.isDebugEnabled()) {
                log.debug("Using nhttp tuning parameter : " + name);
            }
            return true;
        } else if (val != null && !Boolean.parseBoolean(val)) {
            if (log.isDebugEnabled()) {
                log.debug("Using nhttp tuning parameter : " + name);
            }
            return false;
        }
        return def;
    }

    /**
     * Get properties that tune nhttp transport. Preference to system properties
     * @param name name of the system/config property
     * @param def default value to return if the property is not set
     * @return the value of the property to be used
     */
    public String getStringValue(String name, String def) {
        String val = System.getProperty(name);
        if (val == null) {
            val = props.getProperty(name);
        }

        return val == null ? def : val;
    }

    public boolean isHttpMethodDisabled(String method) {
        if (methods == null) {
            methods = new ArrayList<String>();
            String methodsString = getStringValue(G_DISABLED_HTTP_METHODS, "");
            for (String methodStr : methodsString.split(",")) {
                methods.add(methodStr.trim().toUpperCase());
            }
        }
        return methods.contains(method);
    }

}
