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

import java.util.HashMap;
import java.util.Map;

/**
 * The representation for Throttle - holds the both of runtime and static data.
 * There is a one to one relationship between each throttle configuration and context.
 * Currently support two type of throttle configuration. One is remote caller IP based and
 * other one is remote caller domain name based.There may be a concurrent access controller,
 * if the throttle policy has defined the maximum concurrent access .
 */

public class Throttle {

    /* Holder for ThrottleContext - keeps all runtime data */
    private Map throttleContexts;

    /* Holder for ThrottleConfigurations - keeps all static data (data that
     have extracted from the throttle policy) */
    private Map throttleConfigurations;

    /* ConcurrentAccessController instance- This is common to all remote callers
    - controls the concurrent access through this throttle*/
    private ConcurrentAccessController controller;
    /* unique identifier for each throttle */
    private String id;

    /**
     * Default Constructor - initiates the context and configuration maps
     */
    public Throttle() {
        this.throttleContexts = new HashMap();
        this.throttleConfigurations = new HashMap();
    }

    /**
     * Adds a ThrottleConfiguration with the given key - configuration
     * holds all static data for registered callers
     *
     * @param key                   - corresponding key for throttle type.This key
     *                              has one-one relationship with key of contexts
     * @param throttleConfiguration - holds all static data for a throttle -
     *                              ex: all callers configurations
     */
    public void addThrottleConfiguration(String key, ThrottleConfiguration throttleConfiguration) {
        this.throttleConfigurations.put(key, throttleConfiguration);
    }

    /**
     * Adds a ThrotleContext with the given key - context holds all runtime data
     * for registered callers
     *
     * @param key             - corresponding key for throttle type.This key has one-one
     *                        relationship with key of configurations
     * @param throttleContext - holds runtime data - ex: all callers states
     */
    public void addThrottleContext(String key, ThrottleContext throttleContext) {
        this.throttleContexts.put(key, throttleContext);
    }

    /**
     * Returns the ThrotleContext for a given key - context holds all
     * runtime data for registered callers
     *
     * @param contextID - corresponding key for throttle type
     * @return ThrottleContext  returns the context that holds runtime data
     *         - ex: all callers state
     */

    public ThrottleContext getThrottleContext(String contextID) {
        return (ThrottleContext) this.throttleContexts.get(contextID);
    }

    /**
     * Returns the ThrottleConfiguration for a given key -
     * configuration  holds all static data for registered callers
     *
     * @param key -corresponding key for throttle type
     * @return ThrottleConfiguration Returns configuration that holds
     *         all static data for a throttle - ex: all callers configurations
     */
    public ThrottleConfiguration getThrottleConfiguration(String key) {
        return (ThrottleConfiguration) this.throttleConfigurations.get(key);
    }

    /**
     * Sets the ConcurrentAccessController - this will control all the concurrent access
     *
     * @param controller -  the ConcurrentAccessController instance
     */
    public void setConcurrentAccessController(ConcurrentAccessController controller) {
        this.controller = controller;
    }

    /**
     * Returns the ConcurrentAccessController - this will control all the concurrent access
     *
     * @return the ConcurrentAccessController instance
     */
    public ConcurrentAccessController getConcurrentAccessController() {
        return this.controller;
    }

    /**
     * Returns the unique identifier for this throttle
     *
     * @return String representation of the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the unique identifier for this throttle
     *
     * @param id String representation of the id
     */
    public void setId(String id) {
        if (id == null || "".equals(id)) {
            throw new IllegalArgumentException("Invalid argument : ID cannot be null");
        }
        this.id = id.trim();
    }
}
