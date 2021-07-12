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
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RestApiAdminUtils {

    //using a set for file extensions white list since it will be faster to search
    private static final Set<String> EXTENSION_WHITELIST = new HashSet<String>(Arrays.asList(
            "css", "jpg", "png", "gif", "svg", "ttf", "html", "js", "json", "ico"));

    /**
     * Checks whether given policy is allowed to access to user
     *
     * @param user   username with tenant domain
     * @param policy policy to check
     * @return true if user is allowed to access the policy
     */
    public static boolean isPolicyAccessibleToUser(String user, Policy policy) {
        //This block checks whether policy's tenant domain and user's tenant domain are same
        String userTenantDomain = MultitenantUtils.getTenantDomain(user);
        if (!StringUtils.isBlank(policy.getTenantDomain())) {
            return policy.getTenantDomain().equals(userTenantDomain);
        } else {
            String tenantDomainFromId = APIUtil.getTenantDomainFromTenantId(policy.getTenantId());
            return !StringUtils.isBlank(tenantDomainFromId) && tenantDomainFromId.equals(userTenantDomain);
        }
    }

    /**
     * Checks whether given block condition is allowed to access to user
     *
     * @param user           username with tenant domain
     * @param blockCondition Block condition to check
     * @return true if user is allowed to access the block condition
     */
    public static boolean isBlockConditionAccessibleToUser(String user, BlockConditionsDTO blockCondition) {

        String userTenantDomain = MultitenantUtils.getTenantDomain(user);
        return !StringUtils.isBlank(blockCondition.getTenantDomain()) && blockCondition.getTenantDomain()
                .equals(userTenantDomain);
    }

    /**
     * Validate the required properties of Custom Rule Policy
     *
     * @param customRuleDTO custom rule object to check
     * @param httpMethod    HTTP method of the request
     * @throws APIManagementException if a required property validation fails
     */
    public static void validateCustomRuleRequiredProperties(CustomRuleDTO customRuleDTO, String httpMethod)
            throws APIManagementException {

        String propertyName;
        //policyName property is validated only for POST request
        if (httpMethod.equalsIgnoreCase(APIConstants.HTTP_POST)) {
            if (StringUtils.isBlank(customRuleDTO.getPolicyName())) {
                propertyName = "policyName";
                throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                        ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
            }
        }
        if (StringUtils.isBlank(customRuleDTO.getSiddhiQuery())) {
            propertyName = "siddhiQuery";
            throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                    ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
        }
        if (StringUtils.isBlank(customRuleDTO.getKeyTemplate())) {
            propertyName = "keyTemplate";
            throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                    ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
        }
    }

    /**
     * Validate the policy name property of Throttle Policy
     *
     * @param policyName policy name value of throttle policy
     */
    public static void validateThrottlePolicyNameProperty(String policyName)
            throws APIManagementException {
        String propertyName = "policyName";
        Pattern pattern = Pattern.compile("[^A-Za-z0-9_]");//. represents single character
        Matcher matcher = pattern.matcher(policyName);
        if (StringUtils.isBlank(policyName)) {
            throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                    ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
        }

        if (matcher.find()) {
            throw new APIManagementException(propertyName +
                    " property value of payload cannot contain invalid characters",
                    ExceptionCodes.from(ExceptionCodes.CONTAIN_SPECIAL_CHARACTERS, propertyName));
        }
    }

    public static void validateIPAddress(String ipAddress) throws APIManagementException {
        String ip4 = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        String ip6 = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:)" +
                "{1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}" +
                "(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}" +
                "(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)" +
                "|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0," +
                "1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:(" +
                "(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";

        Pattern ip4Pattern = Pattern.compile(ip4);
        Pattern ip6Pattern = Pattern.compile(ip6);
        Matcher ip4Matcher = ip4Pattern.matcher(ipAddress);
        Matcher ip6Matcher = ip6Pattern.matcher(ipAddress);
        boolean result = !ip4Matcher.find() && !ip6Matcher.find();
        if (result) {
            throw new APIManagementException(ipAddress + " is an invalid ip address format");
        }
    }


    /**
     * Constructs an error message to indicate that the object corresponding to the specified type has not been provided
     *
     * @param typeEnum enum representing the particular type
     * @return constructed error message
     */
    public static String constructMissingThrottleObjectErrorMessage(Enum<?> typeEnum) {

        String propertyName = null;
        if (typeEnum.equals(ThrottleConditionDTO.TypeEnum.HEADERCONDITION)) {
            propertyName = "headerCondition";
        }
        if (typeEnum.equals(ThrottleConditionDTO.TypeEnum.IPCONDITION)) {
            propertyName = "ipCondition";
        }
        if (typeEnum.equals(ThrottleConditionDTO.TypeEnum.QUERYPARAMETERCONDITION)) {
            propertyName = "queryParameter";
        }
        if (typeEnum.equals(ThrottleConditionDTO.TypeEnum.JWTCLAIMSCONDITION)) {
            propertyName = "jwtClaimsCondition";
        }
        if (typeEnum.equals(ThrottleLimitDTO.TypeEnum.REQUESTCOUNTLIMIT)) {
            propertyName = "requestCount";
        }
        if (typeEnum.equals(ThrottleLimitDTO.TypeEnum.BANDWIDTHLIMIT)) {
            propertyName = "bandwidth";
        }
        return propertyName + " object corresponding to type " + typeEnum + " not provided\n";
    }

    /**
     * Import the content of the provided tenant theme archive to the file system and the database
     *
     * @param themeContentInputStream content relevant to the tenant theme
     * @param tenantDomain            tenant to which the theme is imported
     * @throws APIManagementException if an error occurs while importing the tenant theme
     * @throws IOException            if an error occurs while performing file or directory related operations
     */
    public static void importTenantTheme(InputStream themeContentInputStream, String tenantDomain)
            throws APIManagementException, IOException {

        ZipInputStream zipInputStream = null;
        byte[] buffer = new byte[1024];
        InputStream existingTenantTheme = null;
        InputStream themeContent = null;
        File tenantThemeDirectory;
        File backupDirectory = null;

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);

        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            //add or update the tenant theme in the database
            if (apiAdmin.isTenantThemeExist(tenantId)) {
                existingTenantTheme = apiAdmin.getTenantTheme(tenantId);
                apiAdmin.updateTenantTheme(tenantId, themeContentInputStream);
            } else {
                apiAdmin.addTenantTheme(tenantId, themeContentInputStream);
            }
            //retrieve the tenant theme from the database to import it to the file system
            themeContent = apiAdmin.getTenantTheme(tenantId);

            //import the tenant theme to the file system
            String outputFolder = getTenantThemeDirectoryPath(tenantDomain);
            tenantThemeDirectory = new File(outputFolder);
            if (!tenantThemeDirectory.exists()) {
                if (!tenantThemeDirectory.mkdirs()) {
                    APIUtil.handleException("Unable to create tenant theme directory at " + outputFolder);
                }
            } else {
                //copy the existing tenant theme as a backup in case a restoration is needed to take place
                String tempPath = getTenantThemeBackupDirectoryPath(tenantDomain);
                backupDirectory = new File(tempPath);
                FileUtils.copyDirectory(tenantThemeDirectory, backupDirectory);

                //remove existing files inside the directory
                FileUtils.cleanDirectory(tenantThemeDirectory);
            }

            //get the zip file content
            zipInputStream = new ZipInputStream(themeContent);
            //get the zipped file list entry
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                String fileName = zipEntry.getName();
                APIUtil.validateFileName(fileName);
                File newFile = new File(outputFolder + File.separator + fileName);
                String canonicalizedNewFilePath = newFile.getCanonicalPath();
                String canonicalizedDestinationPath = new File(outputFolder).getCanonicalPath();
                if (!canonicalizedNewFilePath.startsWith(canonicalizedDestinationPath)) {
                    APIUtil.handleException(
                            "Attempt to upload invalid zip archive with file at " + fileName + ". File path is " +
                                    "outside target directory");
                }

                if (zipEntry.isDirectory()) {
                    if (!newFile.exists()) {
                        boolean status = newFile.mkdir();
                        if (!status) {
                            APIUtil.handleException("Error while creating " + newFile.getName() + " directory");
                        }
                    }
                } else {
                    String ext = FilenameUtils.getExtension(zipEntry.getName());
                    if (EXTENSION_WHITELIST.contains(ext)) {
                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        new File(newFile.getParent()).mkdirs();
                        FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }

                        fileOutputStream.close();
                    } else {
                        APIUtil.handleException(
                                "Unsupported file is uploaded with tenant theme by " + tenantDomain + " : file name : "
                                        + zipEntry.getName());
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
            if (backupDirectory != null) {
                FileUtils.deleteDirectory(backupDirectory);
            }
        } catch (APIManagementException | IOException e) {
            //if an error occurs, revert the changes that were done when importing a tenant theme
            revertTenantThemeImportChanges(tenantDomain, existingTenantTheme);
            throw new APIManagementException(e.getMessage(), e,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_IMPORT_FAILED, tenantDomain, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(zipInputStream);
            IOUtils.closeQuietly(themeContent);
            IOUtils.closeQuietly(themeContentInputStream);
        }
    }

    /**
     * Retrieves the directory location in the file system where the tenant theme is imported
     *
     * @param tenantDomain tenant to which the theme is imported
     * @return directory location in the file system where the tenant theme is imported
     */
    public static String getTenantThemeDirectoryPath(String tenantDomain) {

        return "repository" + File.separator + "deployment" + File.separator + "server" + File.separator + "jaggeryapps"
                + File.separator + "devportal" + File.separator + "site" + File.separator + "public"
                + File.separator + "tenant_themes" + File.separator + tenantDomain;
    }

    /**
     * Retrieves the directory location in the file system where the tenant theme is temporarily backed-up
     *
     * @param tenantDomain tenant to which the theme is imported
     * @return directory location in the file system where the tenant theme is temporarily backed-up
     */
    public static String getTenantThemeBackupDirectoryPath(String tenantDomain) {

        return System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + tenantDomain;
    }

    /**
     * Reverts the changes that occurred when importing a tenant theme
     *
     * @param tenantDomain        tenant to which the theme is imported
     * @param existingTenantTheme tenant theme which existed before the current import operation
     * @throws APIManagementException if an error occurs when reverting the changes
     * @throws IOException            if an error occurs when reverting the changes
     */
    public static void revertTenantThemeImportChanges(String tenantDomain, InputStream existingTenantTheme)
            throws APIManagementException, IOException {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        String tenantThemeDirectoryPath = getTenantThemeDirectoryPath(tenantDomain);
        File tenantThemeDirectory = new File(tenantThemeDirectoryPath);
        if (existingTenantTheme == null) {
            removeTenantTheme(tenantId, tenantThemeDirectory);
        } else {
            String tenantThemeBackupDirectoryPath = getTenantThemeBackupDirectoryPath(tenantDomain);
            File backupDirectory = new File(tenantThemeBackupDirectoryPath);
            restoreTenantTheme(tenantId, tenantThemeDirectory, backupDirectory, existingTenantTheme);
        }
    }

    /**
     * Deletes a tenant theme from the file system and deletes the tenant theme from the database
     *
     * @param tenantId             tenant ID of the tenant to which the theme is imported
     * @param tenantThemeDirectory directory in the file system to where the tenant theme is imported
     * @throws APIManagementException if an error occurs when deleting the tenant theme from the database
     * @throws IOException            if an error occurs when deleting the tenant theme directory
     */
    public static void removeTenantTheme(int tenantId, File tenantThemeDirectory)
            throws APIManagementException, IOException {

        APIAdmin apiAdmin = new APIAdminImpl();
        if (tenantThemeDirectory.exists()) {
            FileUtils.deleteDirectory(tenantThemeDirectory);
        }
        apiAdmin.deleteTenantTheme(tenantId);
    }

    /**
     * Restores the tenant theme which existed before the current import operation was performed
     *
     * @param tenantId             tenant ID of the tenant to which the theme is imported
     * @param tenantThemeDirectory directory in the file system to where the tenant theme is imported
     * @param backupDirectory      directory in the file system where the tenant theme is temporarily backed-up
     * @param existingTenantTheme  tenant theme which existed before the current import operation
     * @throws APIManagementException if an error occurs when updating the tenant theme in the database
     * @throws IOException            if an error occurs when restoring the tenant theme directory
     */
    public static void restoreTenantTheme(int tenantId, File tenantThemeDirectory, File backupDirectory,
                                          InputStream existingTenantTheme) throws APIManagementException, IOException {

        APIAdmin apiAdmin = new APIAdminImpl();
        FileUtils.copyDirectory(backupDirectory, tenantThemeDirectory);
        FileUtils.deleteDirectory(backupDirectory);
        apiAdmin.updateTenantTheme(tenantId, existingTenantTheme);
    }
}
