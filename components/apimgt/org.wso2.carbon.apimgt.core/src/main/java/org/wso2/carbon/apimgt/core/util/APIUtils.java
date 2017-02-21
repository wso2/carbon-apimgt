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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class for all utility methods
 */
public class APIUtils {


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
     * Validate the API object
     * @throws APIManagementException
     */
    public static void validate(API api) throws APIManagementException {
        if (StringUtils.isEmpty(api.getId())) {
            throw new APIManagementException("Couldn't find UUID of API");
        }
        if (StringUtils.isEmpty(api.getName())) {
            throw new APIManagementException("Couldn't find Name of API ");
        }
        if (StringUtils.isEmpty(api.getVersion())) {
            throw new APIManagementException("Couldn't find Version of API ");
        }
    }

    /**
     * Checks String lists for equality independent of the order of elements in the lists.
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

    /**
     * Checks generic lists for equality independent of the order of elements in the lists.
     *
     * Note that order of the elements in the lists will be changed as a result of sorting,
     * but this is not a concern usually since the order does not matter.
     */
    public static <T> boolean isListsEqualIgnoreOrder(List<T> list1, List<T> list2, Comparator<T> comparator) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }

        // Sort lists so that the order of elements don't affect the equal check.
        // Note that order of the elements in the lists will be changed as a result but this is not a concern since
        // the order does not matter
        Collections.sort(list1, comparator);
        Collections.sort(list2, comparator);
        return list1.equals(list2);
    }

    public static boolean isTimeStampsEquals(Temporal date1, Temporal date2) {
        if (date1 == null && date2 == null) {
            return true;
        } else {
            return Duration.between(date1, date2).toMillis() < 1000L;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_CONVERT_CASE", justification = "Didn't need to do " +
            "as String already did internally")


/**
 * used to generate operationId according to the uri template and http verb
 */
    public static String generateOperationIdFromPath(String path, String httpVerb) {
        //TODO need to write proper way of creating operationId
        StringTokenizer stringTokenizer = new StringTokenizer(path, "/");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(httpVerb.toLowerCase());
        while (stringTokenizer.hasMoreElements()) {
            String part1 = stringTokenizer.nextToken();
            if (part1.contains("{")) {
                String pathParam = part1.replace("{", "").replace("}", "");
/*
                stringBuilder.append("By" + pathParam);
*/
            } else if (part1.contains("*")) {
                stringBuilder.append(part1.replaceAll("\\*", "_star_"));
            } else {
                stringBuilder.append(part1);
            }
        }
        return stringBuilder.toString();
    }

    public static Map<String, UriTemplate> getMergedUriTemplates(Map<String, UriTemplate> oldUriTemplateMap,
                                                                 Map<String, UriTemplate> updatedUriTemplateMap) {
        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();
        for (UriTemplate uriTemplate : updatedUriTemplateMap.values()) {
            if (oldUriTemplateMap.containsKey(uriTemplate.getTemplateId())) {
                uriTemplateMap.put(uriTemplate.getTemplateId(), oldUriTemplateMap.get(uriTemplate.getTemplateId()));
            } else {
                uriTemplateMap.put(uriTemplate.getTemplateId(), uriTemplate);
            }
        }
        return uriTemplateMap;
    }
}
