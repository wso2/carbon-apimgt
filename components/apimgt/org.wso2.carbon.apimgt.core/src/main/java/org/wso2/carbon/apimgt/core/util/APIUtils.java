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

import io.swagger.models.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class for all utility methods
 */
public class APIUtils {

    private static final Logger log = LoggerFactory.getLogger(APIUtils.class);

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
     * @param policyType Policy Type
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Policy> getPolicies(int policyType) throws APIManagementException {
        return null;
    }

    public static Policy getDefaultAPIPolicy() {
        return new APIPolicy(APIMgtConstants.DEFAULT_API_POLICY);
    }

    /**
     * Validate the API object
     * @param api API object
     * @throws APIManagementException if mandatory field is not set
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
     * Validate the CompositeAPI object
     * @param api CompositeAPI object
     * @throws APIManagementException if mandatory field is not set
     */
    public static void validate(CompositeAPI api) throws APIManagementException {
        if (StringUtils.isEmpty(api.getId())) {
            throw new APIManagementException("Couldn't find UUID of CompositeAPI");
        }
        if (StringUtils.isEmpty(api.getName())) {
            throw new APIManagementException("Couldn't find Name of CompositeAPI ");
        }
        if (StringUtils.isEmpty(api.getVersion())) {
            throw new APIManagementException("Couldn't find Version of CompositeAPI ");
        }
    }

    /**
     * Checks String lists for equality independent of the order of elements in the lists.
     *
     * Note that order of the elements in the lists will be changed as a result of sorting,
     * but this is not a concern usually since the order does not matter.
     * @param list1 String list
     * @param list2 String list
     * @return true if elements in lists are equal irrespective of order
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
     * @param list1 T list
     * @param list2 T list
     * @param comparator Comparator class
     * @return true if elements in lists are equal irrespective of order
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


    /**
     * Generates default URI templates to be assigned to an API
     * @return  Map of URI Templates
     */
    public static Map<String, UriTemplate> getDefaultUriTemplates() {
        Map<String, UriTemplate> uriTemplateMap = new HashMap<>();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.uriTemplate("/");
        for (String httpVerb : APIMgtConstants.SUPPORTED_HTTP_VERBS.split(",")) {
            if (!HttpMethod.OPTIONS.toString().equals(httpVerb)) {
                uriTemplateBuilder.httpVerb(httpVerb);
                uriTemplateBuilder.policy(APIUtils.getDefaultAPIPolicy());
                uriTemplateBuilder.templateId(APIUtils.generateOperationIdFromPath(uriTemplateBuilder.getUriTemplate
                        (), httpVerb));
                uriTemplateMap.put(uriTemplateBuilder.getTemplateId(), uriTemplateBuilder.build());
            }
        }

        return uriTemplateMap;
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

    /**
     * Validate lifecycle state transition is valid from one state to other
     * @param lifecycleState Lifecycle state object
     * @param nextState target lifecycle state
     * @return true if target state is valid
     */
    public static boolean validateTargetState(LifecycleState lifecycleState, String nextState) {
        return lifecycleState.getAvailableTransitionBeanList().stream().anyMatch(availableTransitionBean ->
                availableTransitionBean.getTargetState().equals(nextState));
    }

    /**
     * This method is used to construct the aggregate permission list from the aggregate permission integer value
     *
     * @param permissionValue - The aggregate permission value for the logged in user
     * @return The array containing the aggregate permissions for the logged in user
     */
    public static List<String> constructApiPermissionsListForValue(Integer permissionValue) {
        List<String> array = new ArrayList<String>();
        if ((permissionValue & APIMgtConstants.Permission.READ_PERMISSION) != 0) {
            array.add(APIMgtConstants.Permission.READ);
        }
        if ((permissionValue & APIMgtConstants.Permission.UPDATE_PERMISSION) != 0) {
            array.add(APIMgtConstants.Permission.UPDATE);
        }
        if ((permissionValue & APIMgtConstants.Permission.DELETE_PERMISSION) != 0) {
            array.add(APIMgtConstants.Permission.DELETE);
        }
        if ((permissionValue & APIMgtConstants.Permission.MANAGE_SUBSCRIPTION_PERMISSION) != 0) {
            array.add(APIMgtConstants.Permission.MANAGE_SUBSCRIPTION);
        }
        return array;
    }

    /**
     * This method returns all available roles
     *
     * @return all available roles
     */
    public static Set<String> getAllAvailableRoles() {

        //this should be a call to IS endpoint and get all the roles, we are returning a dummy list till then
        Set<String> availableRoles = new HashSet<>();
        availableRoles.add("admin");
        availableRoles.add("subscriber");
        availableRoles.add("manager");
        availableRoles.add("developer");
        availableRoles.add("lead");
        availableRoles.add(APIMgtConstants.Permission.EVERYONE_GROUP);
        return availableRoles;
    }

    /**
     * Used to get roles of a particular user
     *
     * @param username username of the person
     * @return role list of the user
     */
    public static Set<String> getAllRolesOfUser(String username) {

        //this should be a call to IS endpoint and get roles of the user, we are returning a dummy list till then
        Set<String> userRoles = new HashSet<>();
        if ("admin".equalsIgnoreCase(username)) {
            userRoles.add("admin");
            userRoles.add("comment-moderator");
            userRoles.add(APIMgtConstants.Permission.EVERYONE_GROUP);
        } else if ("subscriber".equalsIgnoreCase(username)) {
            userRoles.add("subscriber");
        } else if ("John".equalsIgnoreCase(username)) {
            userRoles.add("manager");
            userRoles.add("developer");
        } else if ("Smith".equalsIgnoreCase(username)) {
            userRoles.add("lead");
        } else if ("Alex".equalsIgnoreCase(username)) {
            userRoles.add("admin");
            userRoles.add("manager");
        }
        return userRoles;
    }

    /**
     * Check the validity of roles to be assigned to an API
     *
     * @param availableRoleList all available roles
     * @param candidateRoleList candidate roles to be assigned to the API
     * @return true if all candidate roles are eligible
     * @throws APIManagementException if the check fails
     */
    public static boolean checkAllowedRoles(Set<String> availableRoleList, Set<String> candidateRoleList)
            throws APIManagementException {

        //check if availableRoleList and candidateRoleList is not null
        if (availableRoleList != null && candidateRoleList != null) {
            if (availableRoleList.isEmpty() || candidateRoleList.isEmpty()) {
                String errorMsg = "Role list is empty.";
                log.error(errorMsg);
                throw new APIManagementException(errorMsg, ExceptionCodes.ROLES_CANNOT_BE_EMPTY);
            } else {
                //check if all roles in candidateRoleList are in availableRoleList
                if (availableRoleList.containsAll(candidateRoleList)) {
                    return true;
                } else {
                    String errorMsg = "Invalid role(s) found.";
                    log.error(errorMsg);
                    throw new APIManagementException(errorMsg, ExceptionCodes.UNSUPPORTED_ROLE);
                }
            }
        } else {
            String errorMsg = "Role(s) list is null.";
            log.error(errorMsg);
            throw new APIManagementException(errorMsg, ExceptionCodes.ROLES_CANNOT_BE_NULL);
        }
    }

    /**
     * This method will return map with role names and its permission values. This is not for an API. This is for other
     * resources of an API.
     *
     * @param permissionJsonString Permission json object a string
     * @return Map of permission values.
     * @throws ParseException If failed to parse the json string.
     */
    public static HashMap getAPIPermissionArray(String permissionJsonString) throws ParseException {

        HashMap roleNamePermissionList = new HashMap();
        JSONParser jsonParser = new JSONParser();

        JSONArray baseJsonArray = (JSONArray) jsonParser.parse(permissionJsonString);
        for (Object aBaseJsonArray : baseJsonArray) {
            JSONObject jsonObject = (JSONObject) aBaseJsonArray;
            String groupId = jsonObject.get(APIMgtConstants.Permission.GROUP_ID).toString();
            JSONArray subJsonArray = (JSONArray) jsonObject.get(APIMgtConstants.Permission.PERMISSION);
            int totalPermissionValue = 0;
            for (Object aSubJsonArray : subJsonArray) {
                if (APIMgtConstants.Permission.READ.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.READ_PERMISSION;
                } else if (APIMgtConstants.Permission.UPDATE.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.UPDATE_PERMISSION;
                } else if (APIMgtConstants.Permission.DELETE.equals(aSubJsonArray.toString().trim())) {
                    totalPermissionValue += APIMgtConstants.Permission.DELETE_PERMISSION;
                }
            }
            roleNamePermissionList.put(groupId, totalPermissionValue);
        }

        return roleNamePermissionList;

    }



     /**
     * Verifies that fields that cannot be changed via an API update
     * do not differ from the values in the original API
     *
     * @param apiBuilder Updated APIBuilder object
     * @param originalAPI Original API being updated
     * @throws APIManagementException If non modifiable field update is detected
     */
    public static void verifyValidityOfApiUpdate(API.APIBuilder apiBuilder, API originalAPI)
                                                                                    throws APIManagementException {
        if (!originalAPI.getLifeCycleStatus().equals(apiBuilder.getLifeCycleStatus())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " Couldn't update as " +
                    "the API Status cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getName().equals(apiBuilder.getName())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Name cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getContext().equals(apiBuilder.getContext())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Context cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getVersion().equals(apiBuilder.getVersion())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Version cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getProvider().equals(apiBuilder.getProvider())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Provider cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }
    }

    /**
     * Verifies that fields that cannot be changed via an API update
     * do not differ from the values in the original API
     *
     * @param apiBuilder Updated APIBuilder object
     * @param originalAPI Original API being updated
     * @throws APIManagementException If non modifiable field update is detected
     */
    public static void verifyValidityOfApiUpdate(CompositeAPI.Builder apiBuilder, CompositeAPI originalAPI)
            throws APIManagementException {

        if (!originalAPI.getName().equals(apiBuilder.getName())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Name cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getContext().equals(apiBuilder.getContext())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Context cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getVersion().equals(apiBuilder.getVersion())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Version cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }

        if (!originalAPI.getProvider().equals(apiBuilder.getProvider())) {
            String msg = "API " + apiBuilder.getName() + "-" + apiBuilder.getVersion() + " update not allowed, " +
                    "the API Provider cannot be changed";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }

            throw new APIManagementException(msg, ExceptionCodes.COULD_NOT_UPDATE_API);
        }
    }

    /**
     * Utility for Ip to Long convrsion
     * @param ip ip value
     * @return return long value of Ip
     */
    public static long ipToLong(String ip) {
        long ipAddressinLong = 0;
        if (ip != null) {
            //convert ipaddress into a long
            String[] ipAddressArray = ip.split("\\.");    //split by "." and add to an array

            for (int i = 0; i < ipAddressArray.length; i++) {
                int power = 3 - i;
                long ipAddress = Long.parseLong(ipAddressArray[i]);   //parse to long
                ipAddressinLong += ipAddress * Math.pow(256, power);
            }
        }
        return ipAddressinLong;
    }
}
