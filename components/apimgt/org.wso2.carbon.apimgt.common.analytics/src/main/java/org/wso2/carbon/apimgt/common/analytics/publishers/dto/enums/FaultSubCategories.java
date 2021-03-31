/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums;

/**
 * Enum to denote sub categories of faulty.
 */
public class FaultSubCategories {
    /**
     * Enum for sub categories of other category.
     */
    public enum Other implements FaultSubCategory {
        MEDIATION_ERROR,
        RESOURCE_NOT_FOUND,
        METHOD_NOT_ALLOWED,
        UNCLASSIFIED
    }

    /**
     * Enum for sub categories of Throttling category.
     */
    public enum Throttling implements FaultSubCategory {
        API_LEVEL_LIMIT_EXCEEDED,
        HARD_LIMIT_EXCEEDED,
        RESOURCE_LEVEL_LIMIT_EXCEEDED,
        APPLICATION_LEVEL_LIMIT_EXCEEDED,
        SUBSCRIPTION_LIMIT_EXCEEDED,
        BLOCKED,
        CUSTOM_POLICY_LIMIT_EXCEEDED,
        BURST_CONTROL_LIMIT_EXCEEDED,
        QUERY_TOO_DEEP,
        QUERY_TOO_COMPLEX,
        OTHER
    }

    /**
     * Enum for sub categories of TargetConnectivity category.
     */
    public enum TargetConnectivity implements FaultSubCategory {
        CONNECTION_TIMEOUT,
        CONNECTION_SUSPENDED,
        OTHER
    }

    /**
     * Enum for sub categories of Authentication category.
     */
    public enum Authentication implements FaultSubCategory {
        AUTHENTICATION_FAILURE,
        AUTHORIZATION_FAILURE,
        SUBSCRIPTION_VALIDATION_FAILURE,
        OTHER
    }
}
