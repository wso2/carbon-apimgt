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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Manages beanstalks configured in the Synapse Environment. Only one instance of this class is
 * created per Synapse environment. It is attached to the ServerContextInformation with
 * BeanstalkConstants.BEANSTALK_MANAGER_PROP_NAME property name.
 */
public class EnterpriseBeanstalkManager {

    private final static Log log = LogFactory.getLog(EnterpriseBeanstalkManager.class);

    /**
     * Stores all the beanstalks configured.
     */
    private Map<String, EnterpriseBeanstalk> beanstalkMap = new ConcurrentHashMap<String, EnterpriseBeanstalk>();

    /**
     * ScheduledExecutorService for cleaning up timed out client stubs in all beanstalks.
     */
    private ScheduledExecutorService scheduler;

    /**
     * Initializes the beanstalk manager, which creates and initializes beanstalk defined in the
     * given Properties instance.
     * @param props Properties to read beanstalk configurations from. Usually, source of this is
     * synapse.properties file.
     */
    public void init(Properties props) {

        if (props == null) {
            if (log.isDebugEnabled()) {
                log.debug("Beanstalk properties cannot be found.");
            }
            return;
        }

        String beanstalkNameList = MiscellaneousUtil.getProperty(props,
                EnterpriseBeanstalkConstants.SYNAPSE_BEANSTALK_PREFIX, null);

        if (beanstalkNameList == null || "".equals(beanstalkNameList)) {
            if (log.isDebugEnabled()) {
                log.debug("No beanstalks defined for initialization.");
            }
            return;
        }

        String[] beanstalkNames = beanstalkNameList.split(",");
        if (beanstalkNames == null || beanstalkNames.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No beanstalk definitions found for initialization.");
            }
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "enterprise-beanstalk-cleaner");
                    }
                }
        );

        for (String beanstalkName : beanstalkNames) {

            if (beanstalkName == null || beanstalkName.trim().length() == 0) {
                continue;
            }

            String propertyPrefix = EnterpriseBeanstalkConstants.SYNAPSE_BEANSTALK_PREFIX + "." +
                    beanstalkName + ".";
            Properties currentBeanstalkProps = new Properties();

            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                    String key = (String) entry.getKey();
                    if (key.startsWith(propertyPrefix)) {
                        currentBeanstalkProps.setProperty(
                                key.replace(propertyPrefix, ""), (String) entry.getValue());
                    }
                }
            }

            EnterpriseBeanstalk beanstalk =
                    new EnterpriseBeanstalk(beanstalkName, currentBeanstalkProps, scheduler);
            beanstalk.init();
            beanstalkMap.put(beanstalkName, beanstalk);
        }
    }

    /**
     * Returns the beanstalk with the given name, null if it's not found.
     * @param name Name of the beanstalk.
     * @return Beanstalk specified by the name, null if it's not found.
     */
    public EnterpriseBeanstalk getBeanstalk(String name) {
        return beanstalkMap.get(name);
    }

    /**
     * Cleans up resources allocated by this BeanstalkManager.
     */
    public void destroy() {

        Iterator<EnterpriseBeanstalk> it = beanstalkMap.values().iterator();
        while (it.hasNext()) {
            it.next().destroy();
            it.remove();
        }

        if (!scheduler.isShutdown()) {
            if (log.isDebugEnabled()) {
                log.debug("Shutting down beanstalk cleaner executor...");
            }
            scheduler.shutdownNow();
        }
    }
}
