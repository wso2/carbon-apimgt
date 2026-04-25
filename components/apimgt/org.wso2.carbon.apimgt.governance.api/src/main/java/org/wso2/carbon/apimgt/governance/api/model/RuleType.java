/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.api.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Locale;

/**
 * This class represents a governance rule type
 */
public enum RuleType {
    API_METADATA,
    API_DEFINITION,
    API_DOCUMENTATION;

    private static final Log log = LogFactory.getLog(RuleType.class);

    public static RuleType fromString(String ruleTypeString) {
        if (ruleTypeString == null || ruleTypeString.equalsIgnoreCase("null")) {
            log.debug("Received null or 'null' string for ruleTypeString, returning null");
            return null;
        }
        try {
            return RuleType.valueOf(ruleTypeString.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
