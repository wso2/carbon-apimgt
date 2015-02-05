/*
* Copyright 2005,2006 WSO2, Inc. http://wso2.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

package org.wso2.carbon.throttle.core;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.*;

/**
 * Holds the all runtime data corresponding to call remote callers.
 * In addition to that this hold clean list for callers.
 */

public abstract class ThrottleContext {

    private static Log log = LogFactory.getLog(ThrottleContext.class.getName());

    /* The callersMap that contains all registered callers for a particular throttle */
    private Map callersMap;
    /* For mapping id (ip | domainame) to TimeStamp */
    private Map keyToTimeStampMap;
    /* The Time which next cleaning for this throttle will have to take place */
    private long nextCleanTime;
    /* The configuration of a throttle */
    private ThrottleConfiguration throttleConfiguration;
    /* The configuration that corresponding to this context – this holds all
     static (configuration) data */
    private String throttleId;
    /* The pre-fix of key for any caller */
    private String keyPrefix;
    /*is log level has set to debug */
    private boolean debugOn;

    /**
     * default constructor – expects a throttle configuration.
     *
     * @param throttleConfiguration - configuration data according to the policy
     */
    public ThrottleContext(ThrottleConfiguration throttleConfiguration) {
        if (throttleConfiguration == null) {
            throw new InstantiationError("Couldn't create the throttle context " +
                    "from null a throttle configuration");
        }
        this.keyToTimeStampMap = new HashMap();
        this.callersMap = new TreeMap();
        this.nextCleanTime = 0;
        this.throttleConfiguration = throttleConfiguration;
        this.debugOn = log.isDebugEnabled();
    }

    /**
     * To get the ThrottleConfiguration
     *
     * @return ThrottleConfiguration returns the ThrottleConfiguration of this context
     */
    public ThrottleConfiguration getThrottleConfiguration() {
        return throttleConfiguration;
    }

    /**
     * To get the runtime states of a remote caller
     *
     * @param id the remote caller id ex: domain , ip
     * @return Returns the CallerContext which holds runtime state of a remote caller
     */
    public CallerContext getCallerContext(String id) {

        if (id != null) {

            if (debugOn) {
                log.debug("Found a configuration with id :" + id);
            }
            // for cluster env , caller state is contained in the axis configuration context
            if (keyPrefix != null) {
                return ThrottleUtil.getThrottleCache().get(keyPrefix + id);
            }
        } else {
            if (debugOn) {
                log.debug("Couldn't find a configuration for the remote caller : " + id);
            }
        }
        return null;
    }

    /**
     * setting callerContext - put callersMap against  time and
     * put time against remote caller id (ip/domain)
     *
     * @param callerContext - The remote caller's runtime data.
     * @param id            - The id of the remote caller
     */
    public void addCallerContext(CallerContext callerContext, String id) {
        if (callerContext != null && id != null) {
            addCaller(callerContext, id);
        }
    }

    /**
     * Helper method to add a caller context
     *
     * @param callerContext The CallerContext
     * @param id            The id of the remote caller
     */
    private void addCaller(CallerContext callerContext, String id) {
        // acquiring  cache manager.
        Cache<String, CallerContext> cache = ThrottleUtil.getThrottleCache();
        //if this is a cluster env.,put the context into axis configuration context
        if (keyPrefix != null) {
            cache.put(keyPrefix + id, callerContext);
        }
        // for clean up list
        Long time = new Long(callerContext.getNextTimeWindow());
        if (!callersMap.containsKey(time)) {
            callersMap.put(time, callerContext);
        } else {
            //if there are callersMap with same timewindow ,then use linkedList to hold those
            Object callerObject = callersMap.get(time);
            if (callerObject != null) {
                if (callerObject instanceof CallerContext) {
                    LinkedList callersWithSameTimeStamp = new LinkedList();
                    callersWithSameTimeStamp.add(callerObject);
                    callersWithSameTimeStamp.add(callerContext);
                    callersMap.remove(time);
                    callersMap.put(time, callersWithSameTimeStamp);
                } else if (callerObject instanceof LinkedList) {
                    LinkedList callersWithSameTimeStamp = (LinkedList) callerObject;
                    callersWithSameTimeStamp.add(callerContext);
                }
            }
        }
        //set Time Vs key
        keyToTimeStampMap.put(id, time);
    }

    /**
     * removing a caller with a given id - caller will remove from clean list
     *
     * @param id Caller ID
     */
    public void removeCallerContext(String id) {
        if (id != null) {
            removeCaller(id);
        }
    }

    /**
     * Helper method to remove a caller
     *
     * @param id The id of the caller
     */
    private void removeCaller(String id) {
        Long time = (Long) keyToTimeStampMap.get(id);
        // acquiring  cache manager.
        Cache<String, CallerContext> cache = ThrottleUtil.getThrottleCache();
        // if (time != null) {
        if (keyPrefix != null) {
            if (debugOn) {
                log.debug("Removing the caller with the configuration id " + id);
            }
            cache.remove(keyPrefix + id);
        }
        if(time!=null){
            callersMap.remove(time);
            keyToTimeStampMap.remove(id);

        }
        // }
    }

    /**
     * /**
     * processing cleaning list- only process callerContexts which unit time already had over
     *
     * @param time - the current System Time
     * @throws ThrottleException
     */

    public void processCleanList(long time) throws ThrottleException {
        if (debugOn) {
            log.debug("Cleaning up process is executing");
        }
        if (time > nextCleanTime) {
            SortedMap map = ((TreeMap) callersMap).headMap(new Long(time));
            if (map != null && map.size() > 0) {
                for (Iterator it = map.values().iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (o != null) {
                        if (o instanceof CallerContext) { // In the case nextAccessTime is unique
                            CallerContext c = ((CallerContext) o);
                            String key = c.getID();
                            if (key != null) {
                                if (keyPrefix != null) {
                                    c = ThrottleUtil.getThrottleCache().get(
                                            keyPrefix + key);
                                }
                                if (c != null) {
                                    c.cleanUpCallers(
                                            this.throttleConfiguration.getCallerConfiguration(key)
                                            , this
                                            , time);
                                }
                            }
                        }
                        if (o instanceof LinkedList) { //In the case nextAccessTime of callers are same
                            LinkedList callers = (LinkedList) o;
                            for (Iterator ite = callers.iterator(); ite.hasNext();) {
                                CallerContext c = (CallerContext) ite.next();
                                String key = c.getID();
                                if (key != null) {
                                    if (keyPrefix != null) {
                                        c = ThrottleUtil.getThrottleCache().get(
                                                keyPrefix + key);
                                    }
                                    if (c != null) {
                                        c.cleanUpCallers(
                                                this.throttleConfiguration.getCallerConfiguration(key)
                                                , this
                                                , time);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            nextCleanTime = time + ThrottleConstants.DEFAULT_THROTTLE_CLEAN_PERIOD;
        }
    }

    public void setThrottleId(String throttleId) {
        if (throttleId == null) {
            throw new IllegalArgumentException("The throttle id cannot be null");
        }
        this.throttleId = throttleId;
        this.keyPrefix = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + throttleId;
    }

    public String getThrottleId() {
        return this.throttleId;
    }
/*
    public ConfigurationContext getConfigurationContext() {
        return this.configctx;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configctx = configurationContext;
    }*/

    /**
     * @return Returns the type of throttle ex : ip /domain
     */
    public abstract int getType();

    /**
     * To add the caller and replicates the states of the given caller
     *
     * @param callerContext The states of the caller
     * @param id            The id of the caller
     */
    public void addAndFlushCallerContext(CallerContext callerContext, String id) {
        if (callerContext != null && id != null) {
            addCaller(callerContext, id);
        }
    }

    /**
     * To replicates the states of the already exist caller
     *
     * @param callerContext The states of the caller
     * @param id            The id of the remote caller
     */
    public void flushCallerContext(CallerContext callerContext, String id) {
        if (callerContext != null && id != null) {
            String key = keyPrefix + id;
            ThrottleUtil.getThrottleCache().put(key,callerContext);
        }
    }

    /**
     * Removes the caller and repicate the states
     *
     * @param id The Id of the caller
     */
    public void removeAndFlushCaller(String id) {
        Cache<String, CallerContext> cache = ThrottleUtil.getThrottleCache();
        if (id != null && keyPrefix != null) {
            if (debugOn) {
                log.debug("Removing the caller with the configuration id " + id);
            }
            cache.remove(keyPrefix + id);
        }
    }


}
