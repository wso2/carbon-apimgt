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
package org.wso2.carbon.throttle.core.impl.domainbase;

import org.wso2.carbon.throttle.core.CallerConfiguration;
import org.wso2.carbon.throttle.core.ThrottleConstants;

/**
 * Caller Configuration implementation for domain name based throttle
 */

public class DomainBaseCallerConfiguration extends CallerConfiguration {
    /* The id - domain name */
    private String id;

    public String getID() {
        return this.id;
    }

    public void setID(String ID) {
        if (ID == null || "".equals(ID)) {
            throw new IllegalArgumentException("Invalid argument : ID cannot be null");
        }
        this.id = ID.trim();

    }

    public int getType() {
        return ThrottleConstants.DOMAIN_BASE;
    }
}
