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

/**
 * This class represents a governance rule Severity
 */
public enum Severity {
    ERROR,
    WARN,
    INFO;

    public static Severity fromString(String severityString) {
        if ("error".equalsIgnoreCase(severityString)) {
            return Severity.ERROR;
        } else if ("warn".equalsIgnoreCase(severityString)) {
            return Severity.WARN;
        } else if ("info".equalsIgnoreCase(severityString)) {
            return Severity.INFO;
        }
        return null;
    }
}
