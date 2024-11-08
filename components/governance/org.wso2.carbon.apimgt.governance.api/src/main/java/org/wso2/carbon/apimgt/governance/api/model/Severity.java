/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;

public enum Severity {
    ERROR(0),
    WARN(1),
    INFO(2);

    private final int value;

    Severity(int value) {
        this.value = value;
    }

    public static Severity fromValue(int value) throws GovernanceException {
        for (Severity severity : Severity.values()) {
            if (severity.getValue() == value) {
                return severity;
            }
        }
        throw new GovernanceException(GovernanceExceptionCodes.INVALID_RULE_SEVERITY, value);
    }

    public static Severity fromString(String severityString) {
        if ("error".equals(severityString)) {
            return Severity.ERROR;
        } else if ("warnings".equals(severityString)) {
            return Severity.WARN;
        } else if ("info".equals(severityString)) {
            return Severity.INFO;
        }
        return Severity.WARN;
    }

    public int getValue() {
        return value;
    }
}
