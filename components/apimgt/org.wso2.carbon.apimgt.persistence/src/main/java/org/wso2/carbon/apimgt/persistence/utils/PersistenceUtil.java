/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.persistence.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.DeploymentEnvironments;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Cache;
import javax.cache.Caching;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersistenceUtil {
    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    public static void handleException(String msg) throws APIManagementException {
        throw new APIManagementException(msg);
    }

    /**
     * This method will check the validity of given url. WSDL url should be
     * contain http, https, "/t" (for tenant APIs) or file system path
     * otherwise we will mark it as invalid wsdl url. How ever here we do not
     * validate wsdl content.
     *
     * @param wsdlURL wsdl url tobe tested
     * @return true if its valid url else fale
     */
    // NO REG USAGE
    public static boolean isValidWSDLURL(String wsdlURL, boolean required) {

        if (wsdlURL != null && !"".equals(wsdlURL)) {
            if (wsdlURL.startsWith("http:") || wsdlURL.startsWith("https:") ||
                                            wsdlURL.startsWith("file:") || (wsdlURL.startsWith("/t") && !wsdlURL.endsWith(".zip"))) {
                return true;
            }
        } else if (!required) {
            // If the WSDL in not required and URL is empty, then we don't need
            // to add debug log.
            // Hence returning.
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("WSDL url validation failed. Provided wsdl url is not valid url: " + wsdlURL);
        }
        return false;
    }

    /**
     * Method used to create the file name of the wsdl to be stored in the registry
     *
     * @param provider   Name of the provider of the API
     * @param apiName    Name of the API
     * @param apiVersion API Version
     * @return WSDL file name
     */
    public static String createWsdlFileName(String provider, String apiName, String apiVersion) {

        return provider + "--" + apiName + apiVersion + ".wsdl";
    }


    /**
     * Given a wsdl resource, this method checks if the underlying document is a WSDL2
     *
     * @param wsdl byte array of wsdl definition saved in registry
     * @return true if wsdl2 definition
     * @throws APIManagementException
     */
    public static boolean isWSDL2Resource(byte[] wsdl) throws APIManagementException {

        String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
        String wsdlContent = new String(wsdl);
        return wsdlContent.indexOf(wsdl2NameSpace) > 0;
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                            APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    /**
     * load tenant axis configurations.
     *
     * @param tenantDomain
     */
    public static void loadTenantConfigBlockingMode(String tenantDomain) {

        try {
            ConfigurationContext ctx = ServiceReferenceHolder.getContextService().getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, ctx);
        } catch (Exception e) {
            log.error("Error while creating axis configuration for tenant " + tenantDomain, e);
        }
    }

    public static String getSearchQuery(String searchQuery, String userNameWithoutChange) throws APIManagementException {
        if (/*!isAccessControlRestrictionEnabled || */( userNameWithoutChange != null &&
                                        hasPermission(userNameWithoutChange, APIConstants.Permissions
                                                                        .APIM_ADMIN))) {
            return searchQuery;
        }
        String criteria = getUserRoleListQuery(userNameWithoutChange);
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            criteria = criteria + "&" + searchQuery;
        }
        return criteria;
    }

    /**
     * To get the query to retrieve user role list query based on current role list.
     *
     * @return the query with user role list.
     * @throws APIManagementException API Management Exception.
     */
    private static String getUserRoleListQuery(String userNameWithoutChange) throws APIManagementException {
        StringBuilder rolesQuery = new StringBuilder();
        rolesQuery.append('(');
        rolesQuery.append(APIConstants.NULL_USER_ROLE_LIST);
        String[] userRoles = getListOfRoles(userNameWithoutChange);
        String skipRolesByRegex = getSkipRolesByRegex();
        if (StringUtils.isNotEmpty(skipRolesByRegex)) {
            List<String> filteredUserRoles = new ArrayList<>(Arrays.asList(userRoles));
            String[] regexList = skipRolesByRegex.split(",");
            for (int i = 0; i < regexList.length; i++) {
                Pattern p = Pattern.compile(regexList[i]);
                Iterator<String> itr = filteredUserRoles.iterator();
                while(itr.hasNext()) {
                    String role = itr.next();
                    Matcher m = p.matcher(role);
                    if (m.matches()) {
                        itr.remove();
                    }
                }
            }
            userRoles = filteredUserRoles.toArray(new String[0]);
        }
        if (userRoles != null) {
            for (String userRole : userRoles) {
                rolesQuery.append(" OR ");
                rolesQuery.append(ClientUtils.escapeQueryChars(sanitizeUserRole(userRole.toLowerCase())));
            }
        }
        rolesQuery.append(")");
        if(log.isDebugEnabled()) {
            log.debug("User role list solr query " + APIConstants.PUBLISHER_ROLES + "=" + rolesQuery.toString());
        }
        return APIConstants.PUBLISHER_ROLES + "=" + rolesQuery.toString();
    }

    /**
     * Convert special characters to encoded value.
     *
     * @param role
     * @return encorded value
     */
    public static String sanitizeUserRole(String role) {

        if (role.contains("&")) {
            return role.replaceAll("&", "%26");
        } else {
            return role;
        }
    }

    /**
     * return skipRolesByRegex config
     */
    public static String getSkipRolesByRegex() {

       /* APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String skipRolesByRegex = config.getFirstProperty(APIConstants.SKIP_ROLES_BY_REGEX);*/
        return null;
        //return skipRolesByRegex;
    }

    /**
     * Retrieves the role list of a user
     *
     * @param username A username
     * @param username A username
     * @throws APIManagementException If an error occurs
     */
    public static String[] getListOfRoles(String username) throws APIManagementException {

        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                                            " the anonymous user");
        }

        String[] roles = null;

        roles = getValueFromCache(APIConstants.API_USER_ROLE_CACHE, username);
        if (roles != null) {
            return roles;
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        try {
            if (!org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                                            .equals(tenantDomain)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                .getTenantId(tenantDomain);
                UserStoreManager manager = ServiceReferenceHolder.getInstance().getRealmService()
                                                .getTenantUserRealm(tenantId).getUserStoreManager();
                roles = manager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
            } else {
                // org.wso2.carbon.apimgt.impl.utils.AuthorizationManager.getInstance()
                // roles = AuthorizationManager.getInstance()
                //                                .getRolesOfUser(MultitenantUtils.getTenantAwareUsername(username));
            }
            addToRolesCache(APIConstants.API_USER_ROLE_CACHE, username, roles);
            return roles;
        } catch (UserStoreException e) {
            throw new APIManagementException("UserStoreException while trying the role list of the user " + username,
                                            e);
        }
    }

    /**
     * To add the value to a cache.
     *
     * @param cacheName - Name of the Cache
     * @param key       - Key of the entry that need to be added.
     * @param value     - Value of the entry that need to be added.
     */
    protected static <T> void addToRolesCache(String cacheName, String key, T value) {
        //if (isPublisherRoleCacheEnabled) { need to get this from configurations
        if (true /*isPublisherRoleCacheEnabled */) {
            if (log.isDebugEnabled()) {
                log.debug("Publisher role cache is enabled, adding the roles for the " + key + " to the cache "
                                                + cacheName + "'");
            }
            Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(cacheName).put(key, value);
        }
    }

    /**
     * To get the value from the cache.
     *
     * @param cacheName Name of the cache.
     * @param key       Key of the cache entry.
     * @return Role list from the cache, if a values exists, otherwise null.
     */
    protected static <T> T getValueFromCache(String cacheName, String key) {

        if (true /*isPublisherRoleCacheEnabled*/) {
            if (log.isDebugEnabled()) {
                log.debug("Publisher role cache is enabled, retrieving the roles for  " + key + " from the cache "
                                                + cacheName + "'");
            }
            Cache<String, T> rolesCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                                            .getCache(cacheName);
            return rolesCache.get(key);
        }
        return null;
    }


    /**
     * Checks whether the specified user has the specified permission.
     *
     * @param userNameWithoutChange A username
     * @param permission            A valid Carbon permission
     * @throws APIManagementException If the user does not have the specified permission or if an error occurs
     */
    public static boolean hasPermission(String userNameWithoutChange, String permission)
                                    throws APIManagementException {

        boolean authorized = false;
        if (userNameWithoutChange == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                                            " the anonymous user");
        }

        //if (isPermissionCheckDisabled()) {
        if (false) {
            log.debug("Permission verification is disabled by APIStore configuration");
            authorized = true;
            return authorized;
        }

        if (APIConstants.Permissions.APIM_ADMIN.equals(permission)) {
            Integer value = getValueFromCache(APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE, userNameWithoutChange);
            if (value != null) {
                return value == 1;
            }
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userNameWithoutChange);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                                            getTenantId(tenantDomain);

            if (!org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                org.wso2.carbon.user.api.AuthorizationManager manager =
                                                ServiceReferenceHolder.getInstance()
                                                                                .getRealmService()
                                                                                .getTenantUserRealm(tenantId)
                                                                                .getAuthorizationManager();
                authorized =
                                                manager.isUserAuthorized(MultitenantUtils.getTenantAwareUsername(userNameWithoutChange), permission,
                                                                                CarbonConstants.UI_PERMISSION_ACTION);
            } else {
                // On the first login attempt to publisher (without browsing the
                // store), the user realm will be null.
                if (ServiceReferenceHolder.getUserRealm() == null) {
                    ServiceReferenceHolder.setUserRealm((UserRealm) ServiceReferenceHolder.getInstance()
                                                    .getRealmService()
                                                    .getTenantUserRealm(tenantId));
                }
//                authorized =
//                                                AuthorizationManager.getInstance()
//                                                                                .isUserAuthorized(MultitenantUtils.getTenantAwareUsername(userNameWithoutChange),
//                                                                                                                permission);
                authorized = true;
            }
            if (APIConstants.Permissions.APIM_ADMIN.equals(permission)) {
                addToRolesCache(APIConstants.API_PUBLISHER_ADMIN_PERMISSION_CACHE, userNameWithoutChange,
                                                authorized ? 1 : 2);
            }

        } catch (UserStoreException e) {
            throw new APIManagementException("Error while checking the user:" + userNameWithoutChange + " authorized or not", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return authorized;
    }

    public static boolean isAllowDisplayAPIsWithMultipleStatus() {

//        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
//                                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
//        String displayAllAPIs = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_ALL_APIS);
//        if (displayAllAPIs == null) {
//            log.warn("The configurations related to show deprecated APIs in APIStore " +
//                                            "are missing in api-manager.xml.");
//            return false;
//        }
//        return Boolean.parseBoolean(displayAllAPIs);
        return false;
    }
    /**
     * This method used to set selected deployment environment values to governance artifact of API .
     *
     * @param deployments DeploymentEnvironments attributes value
     */
    public static Set<DeploymentEnvironments> extractDeploymentsForAPI(String deployments) {

        HashSet<DeploymentEnvironments> deploymentEnvironmentsSet = new HashSet<>();
        if (deployments != null && !"null".equals(deployments)) {
            Type deploymentEnvironmentsSetType = new TypeToken<HashSet<DeploymentEnvironments>>() {
            }.getType();
            deploymentEnvironmentsSet = new Gson().fromJson(deployments, deploymentEnvironmentsSetType);
            return deploymentEnvironmentsSet;
        }
        return deploymentEnvironmentsSet;
    }

    /**
     * This method used to extract environment list configured with non empty URLs.
     *
     * @param endpointConfigs (Eg: {"production_endpoints":{"url":"http://www.test.com/v1/xxx","config":null,
     *                        "template_not_supported":false},"endpoint_type":"http"})
     * @return Set<String>
     */
    public static Set<String> extractEnvironmentListForAPI(String endpointConfigs)
                                    throws ParseException, ClassCastException {

        Set<String> environmentList = new HashSet<String>();
        if (StringUtils.isNotBlank(endpointConfigs) && !"null".equals(endpointConfigs)) {
            JSONParser parser = new JSONParser();
            JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfigs);
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_PRODUCTION_ENDPOINTS) &&
                                            isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_PRODUCTION_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_PRODUCTION);
            }
            if (endpointConfigJson.containsKey(APIConstants.API_DATA_SANDBOX_ENDPOINTS) &&
                                            isEndpointURLNonEmpty(endpointConfigJson.get(APIConstants.API_DATA_SANDBOX_ENDPOINTS))) {
                environmentList.add(APIConstants.API_KEY_TYPE_SANDBOX);
            }
        }
        return environmentList;
    }

    /**
     * This method used to check whether the endpoints JSON object has a non empty URL.
     *
     * @param endpoints (Eg: {"url":"http://www.test.com/v1/xxx","config":null,"template_not_supported":false})
     * @return boolean
     */
    public static boolean isEndpointURLNonEmpty(Object endpoints) {

        if (endpoints instanceof JSONObject) {
            JSONObject endpointJson = (JSONObject) endpoints;
            if (endpointJson.containsKey(APIConstants.API_DATA_URL) &&
                                            endpointJson.get(APIConstants.API_DATA_URL) != null) {
                String url = (endpointJson.get(APIConstants.API_DATA_URL)).toString();
                if (StringUtils.isNotBlank(url)) {
                    return true;
                }
            }
        } else if (endpoints instanceof JSONArray) {
            JSONArray endpointsJson = (JSONArray) endpoints;
            for (int i = 0; i < endpointsJson.size(); i++) {
                if (isEndpointURLNonEmpty(endpointsJson.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String extractPDFText(InputStream inputStream) throws IOException {
        PDFParser parser = new PDFParser(inputStream);
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(new PDDocument(cosDoc));
        cosDoc.close();
        return text;
    }

    public static String extractDocXText(InputStream inputStream) throws IOException {
        XWPFDocument doc = new XWPFDocument(inputStream);
        XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
        return msWord2007Extractor.getText();
    }

    public static String extractDocText(InputStream inputStream) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
        WordExtractor msWord2003Extractor = new WordExtractor(fs);
        return msWord2003Extractor.getText();
    }

    public static String extractPlainText(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static File writeStream(InputStream uploadedInputStream, String fileName)
            throws PersistenceException {
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator
                + APIConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);
        FileOutputStream outFileStream = null;

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            throw new PersistenceException("Failed to create temporary folder for document upload ");
        }

        try {
            outFileStream = new FileOutputStream(new File(docFile.getAbsolutePath(), fileName));
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring files.";
            log.error(errorMessage, e);
            throw new PersistenceException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
        return docFile;
    }

    public static InputStream readStream(File docFile, String fileName) throws PersistenceException {
        try {
            InputStream newInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + fileName);
            return newInputStream;
        } catch (FileNotFoundException e) {
            throw new PersistenceException("Failed to open file ");
        }
    }

}
