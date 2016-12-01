/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.util;

import org.slf4j.Logger;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for all utility methods
 */
public class APIUtils {

    /**
     * Log and throw exceptions. This should be used only at service level.
     *
     * @param msg Error message
     * @param log Logger to be used to log the error message
     * @throws APIManagementException
     */
    public static void logAndThrowException(String msg, Logger log) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    /**
     *
     * @param msg Error message
     * @param codes Exception code that need to pass in error DTO.
     * @param log Logger to be used to log the error message
     * @throws APIManagementException
     */
    public static void logAndThrowException(String msg, ExceptionCodes codes, Logger log) throws
            APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg, codes);
    }

    /**
     * Log and throw exceptions. This should be used only at service level.
     *
     * @param msg Error message
     * @param t Exception to be thrown
     * @param log Logger to be used to log the error message
     * @throws APIManagementException
     */
    public static void logAndThrowException(String msg, Throwable t, Logger log) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Checks if debug log is enabled and logs the message
     *
     * @param msg Message to be logged
     * @param log Logger to be used to log
     */
    public static void logDebug(String msg, Logger log) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    /**
     * Find scope object in a set based on the key
     *
     * @param scopes - Set of scopes
     * @param key    - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes, String key) {
        for (Scope scope : scopes) {
            if (scope.getKey().equals(key)) {
                return scope;
            }
        }
        return null;
    }

    /**
     * Returns a map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Policy> getPolicies(int policyType) throws APIManagementException {
        return null;
    }

    public static String getDefaultAPIPolicy() {
        // TODO: 11/25/16 need to implement logic
        return "Unlimited";
    }

    /**
     * Checks to String lists for equality independent of the order of elements in the lists.
     *
     * Note that order of the elements in the lists will be changed as a result of sorting,
     * but this is not a concern usually since the order does not matter.
     */
    public static boolean isListsEqualIgnoreOrder(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        // Sort lists so that the order of elements don't affect the equal check.
        // Note that order of the elements in the lists will be changed as a result but this is not a concern since
        // the order does not matter
        Collections.sort(list1);
        Collections.sort(list2);
        return list1.equals(list2);
    }
}
