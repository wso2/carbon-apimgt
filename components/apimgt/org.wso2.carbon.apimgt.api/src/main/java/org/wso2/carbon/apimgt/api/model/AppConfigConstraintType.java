/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

/**
 * Enum to define the types of application configuration constraints.
 */
public enum AppConfigConstraintType {
    RANGE("RANGE"),
    RANGE_MIN("RANGE_MIN"),
    RANGE_MAX("RANGE_MAX"),
    REGEX("REGEX"),
    ENUM("ENUM");

    private final String value;

    AppConfigConstraintType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public static AppConfigConstraintType fromString(String text) {
        try {
            return AppConfigConstraintType.valueOf(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
