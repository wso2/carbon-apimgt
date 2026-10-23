/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.healthcheck.probe;

/**
 * The parts of the Traffic Manager that {@link TMHealthChecker} checks.
 */
public enum CheckType {

    /**
     * The throttle event receiver (9611 + offset, logging in on 9711 + offset).
     */
    THROTTLE_RECEIVER("tm.throttle.receiver", "Throttle receiver"),

    /**
     * The message broker (5672 + offset, and its SSL port 8672 + offset).
     */
    MESSAGE_BROKER("tm.message.broker", "Message broker");

    private final String keyPrefix;
    private final String label;

    CheckType(String keyPrefix, String label) {

        this.keyPrefix = keyPrefix;
        this.label = label;
    }

    /**
     * @return the prefix of this check's keys in the health check response, e.g. {@code tm.throttle.receiver}
     */
    public String getKeyPrefix() {

        return keyPrefix;
    }

    /**
     * @return a readable name, used at the start of error messages
     */
    public String getLabel() {

        return label;
    }
}
