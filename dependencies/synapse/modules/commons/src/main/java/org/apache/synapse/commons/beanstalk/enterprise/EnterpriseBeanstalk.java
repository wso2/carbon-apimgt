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
import org.apache.synapse.commons.jmx.MBeanRegistrar;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An enterprise beanstalk is used to retrieve Enterprise JavaBean (EJB) client stubs. This class
 * supports stateless and stateful session beans.
 * Enterprise beanstalks can be configured in synapse.properties file. Parameters of the JNDI
 * service to be looked up for EJBs should be provided in that configuration.
 * Enterprise beanstalks cache EJB client stubs to improve efficiency by omitting excessive JNDI
 * look ups. Cache timeout for both stateless and stateful session beans could be configured
 * separately.
 */
public class EnterpriseBeanstalk {

    private static final Log log = LogFactory.getLog(EnterpriseBeanstalk.class);

    /**
     * Name of the beanstalk
     */
    private String name;

    /**
     * Properties used while initializing this beanstalk.
     */
    private Properties props;

    /**
     * Executor that runs the Cleaner.
     */
    private ScheduledExecutorService scheduler;

    /**
     * Cache timeout for stateless session bean stubs.
     */
    private int statelessBeanTimeoutMinutes = 30;

    /**
     * Cache timeout for stateful session bean stubs.
     */
    private int statefulBeanTimeoutMinutes = 30;

    /**
     * Warn limit for stateless session beans. A warning is generated when more than this many of
     * stateless bean stubs are cached by this beanstalk.
     */
    private int statelessBeanWarnLimit = Short.MAX_VALUE;

    /**
     * Warn limit for stateful session beans. A warning is generated when more than this many of
     * stateful bean stubs are cached by this beanstalk.
     */
    private int statefulBeanWarnLimit = Short.MAX_VALUE;

    /**
     * JNDI context constructed with the properties provided while initializing this beanstalk.
     */
    private InitialContext initialCtx;

    /**
     * Stateless session bean stub cache.
     */
    private Map<String, CacheEntry> statelessBeans = new ConcurrentHashMap<String, CacheEntry>();

    /**
     * Stateful session bean stub cache.
     */
    private Map<String, CacheEntry> statefulBeans = new ConcurrentHashMap<String, CacheEntry>();

    /**
     * Constructs a new enterprise beanstalk with the given name and properties.
     * @param name Name of the enterprise beanstalk.
     * @param props Configuration properties. This should include properties of the JNDI service
     * to be looked up for EJBs.
     * @param scheduler ScheduledExecutorService for cleaning up timed-out stubs.
     */
    public EnterpriseBeanstalk(String name, Properties props, ScheduledExecutorService scheduler) {
        this.name = name;
        this.scheduler = scheduler;
        this.props = props;
    }

    /**
     * Initialize the current beanstalk by creating the JNDI context, registering the MBean etc.
     */
    public void init() {

        if (log.isDebugEnabled()) {
            log.debug("Initializing Beanstalk: " + name);
        }

        // Initialize the JNDI context.
        try {
            initialCtx = new EnterpriseIntitalContext(props);
        } catch (NamingException e) {
            log.error("Could not initialize the JNDI context for the Enterprise Beanstalk " +
                    "named '" + name + "'.", e);
            return;
        }

        // Read settings from the provided properties.
        if (props != null) {
            String value;

            value = props.getProperty(EnterpriseBeanstalkConstants.STATELESS_BEANS_TIMEOUT);
            if (value != null) {
                statelessBeanTimeoutMinutes = Integer.parseInt(value);
            }

            value = props.getProperty(EnterpriseBeanstalkConstants.STATEFUL_BEANS_TIMEOUT);
            if (value != null) {
                statefulBeanTimeoutMinutes = Integer.parseInt(value);
            }

            value = props.getProperty(EnterpriseBeanstalkConstants.STATELESS_BEANS_WARN_LIMIT);
            if (value != null) {
                statelessBeanWarnLimit = Integer.parseInt(value);
            }

            value = props.getProperty(EnterpriseBeanstalkConstants.STATEFUL_BEANS_WARN_LIMIT);
            if (value != null) {
                statefulBeanWarnLimit = Integer.parseInt(value);
            }
        }

        // Schedule the cleaner that removes expired beans periodically.
        int minDelay = Math.min(statelessBeanTimeoutMinutes, statefulBeanTimeoutMinutes);
        scheduler.scheduleWithFixedDelay(
                        new Runnable() {
                            public void run() {
                                removeExpiredBeans();
                            }
                        }, minDelay, minDelay, TimeUnit.MINUTES);

        // Register the MBean for this beanstalk.
        MBeanRegistrar.getInstance().registerMBean(new EnterpriseBeanstalkView(this),
                EnterpriseBeanstalkConstants.BEANSTALK_MBEAN_CATEGORY_NAME, name);

        if (log.isDebugEnabled()) {
            log.debug("Successfully initialized Beanstalk: " + name);
        }
    }

    /**
     * Destroys the beanstalk by un-registering MBeans and cleaning up other resources.
     */
    public void destroy() {
        MBeanRegistrar.getInstance().unRegisterMBean(
                EnterpriseBeanstalkConstants.BEANSTALK_MBEAN_CATEGORY_NAME, name);
    }

    /**
     * Returns the name of the beanstalk.
     *
     * @return Name of the beanstalk.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a client stub for the EJB with the given class name, bean id and jndi name. If the
     * stub is already available in the cache, it is retrieved from there. Otherwise, a JNDI lookup
     * is performed with the given JNDI name.
     *
     * @param className Fully qualified name of the remote interface of the session bean.
     * @param sessionId Session id for stateful beans. null for stateless ones.
     * @param jndiName JNDI name of the EJB. null could be used if the bean is already available in
     * the cache.
     * @return Retrieved EJB client stub. null if the EJB is not found.
     */
    public Object getEnterpriseBean(String className, String sessionId, String jndiName) {
        return sessionId == null ?
            findEjb(statelessBeans, className, sessionId, jndiName, statelessBeanWarnLimit) :
            findEjb(statefulBeans, className, sessionId, jndiName, statefulBeanWarnLimit);
    }

    /**
     * Remove the specified client stub from the cache. If the stub is not removed using this
     * method it will be automatically removed from the beanstalk after it times out.
     * @param className Fully qualified name of the remote interface of the session bean
     * @param sessionId Session id for stateful session beans, null for stateless ones.
     * @return EJB client stub that was removed from the beanstalk.
     */
    public Object removeEnterpriseBean(String className, String sessionId) {
        return (sessionId == null) ?
                statelessBeans.remove(getMapKey(className, sessionId)) :
                statefulBeans.remove(getMapKey(className, sessionId));
    }

    /**
     * Removes expired stateless and stateful bean stubs from the cache. Timeouts are configurable.
     */
    public void removeExpiredBeans() {
        removeExpiredBeansFromMap(statelessBeans, statelessBeanTimeoutMinutes);
        removeExpiredBeansFromMap(statefulBeans, statefulBeanTimeoutMinutes);
    }

    /**
     * Retrieves an EJB client stub from the cache, looks up in the JNDI service if it is not
     * available in the cache. If the bean is found from the JNDI lookup, it is added to the cache.
     *
     * @param map Cache to search the stub in.
     * @param className Fully qualified name of the remote interface of the session bean.
     * @param sessionId Session id for stateful beans. null for stateless ones.
     * @param jndiName JNDI name of the EJB. null could be used if the bean is already available in
     * the cache.
     * @param warnLimit If this many of stubs are already available in the cache, a warning is
     * generated before adding a new stub to the cache.
     * @return Retrieved EJB client stub. null if the EJB is not found.
     */
    private Object findEjb(Map<String, CacheEntry> map, String className, String sessionId,
                           String jndiName, int warnLimit) {

        CacheEntry entry = map.get(getMapKey(className, sessionId));

        if (entry == null && jndiName != null) {

            synchronized (this) {
                entry = map.get(getMapKey(className, sessionId));
                if (entry == null) {
                    Object ejb = lookupInJndi(jndiName);
                    if (ejb != null) {
                        map.put(getMapKey(className, sessionId), entry = new CacheEntry(ejb));
                        int size = map.size();
                        if (size > warnLimit) {
                            String type = sessionId == null ? "stateless" : "stateful";
                            log.warn("Warn limit reached for " + type + " beans. Currently there " +
                                    "are " + size + " " + type + " EJB stubs cached in '" + name +
                                    "' " + "beanstalk.");
                        }
                    }
                }
            }

        }

        if (entry == null) {
            return null;
        } else {
            entry.setLastAccessTime(System.currentTimeMillis());
            return entry.getBean();
        }

    }

    /**
     * Looks up the given resource in the JNDI service.
     * @param jndiName JNDI name of the resource (EJB).
     * @return Resource retrieved from the JNDI lookup. null if no resource is found.
     */
    private Object lookupInJndi(String jndiName) {
        try {
            return initialCtx.lookup(jndiName);
        } catch (NamingException ex) {
            log.error("Lookup failed for JNDI name: " + jndiName, ex);
            return null;
        }
    }

    /**
     * Remove stubs from the given map that have not been used for a time period longer than the
     * provided timeout.
     * @param map Bean stub cache.
     * @param timeoutInMinutes Expiry timeout.
     */
    private void removeExpiredBeansFromMap(Map<String, CacheEntry> map, int timeoutInMinutes) {

        Iterator<Map.Entry<String, CacheEntry>> itr = map.entrySet().iterator();

        while (itr.hasNext()) {

            Map.Entry<String, CacheEntry> mapEntry = itr.next();

            if (System.currentTimeMillis() - mapEntry.getValue().getLastAccessTime() >
                                                        timeoutInMinutes * 60L * 1000L) {

                if (log.isDebugEnabled()) {
                    log.debug("Removing the timed-out EJB stub with key '" + mapEntry.getKey() +
                            "', from '" + name + "' beanstalk cache.");
                }
                itr.remove();
            }
        }
    }

    /**
     * Derives the map key for this session bean.
     * @param className Fully qualified name of the remote interface of the EJB.
     * @param sessionId Session Id for stateful beans, null for stateless ones.
     * @return Map key derived from the given parameters.
     */
    private String getMapKey(String className, String sessionId) {
        return sessionId == null ? className : className + "-" + sessionId;
    }

    Map<String, CacheEntry> getStatelessBeans() {
        return statelessBeans;
    }

    Map<String, CacheEntry> getStatefulBeans() {
        return statefulBeans;
    }
}
