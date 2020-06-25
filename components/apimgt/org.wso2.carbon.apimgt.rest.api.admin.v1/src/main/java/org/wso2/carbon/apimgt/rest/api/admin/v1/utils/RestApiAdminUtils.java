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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

        if (StringUtils.isBlank(policyName)) {
            String propertyName = "policyName";
            throw new APIManagementException(propertyName + " property value of payload cannot be blank",
                    ExceptionCodes.from(ExceptionCodes.BLANK_PROPERTY_VALUE, propertyName));
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
     * Extract the content of the provided tenant theme archive
     *
     * @param themeFile    content relevant to the tenant theme
     * @param tenantDomain tenant to which the theme is imported
     * @throws APIManagementException if an error occurs while importing tenant theme
     */
    public static void deployTenantTheme(InputStream themeFile, String tenantDomain) throws APIManagementException {

        ZipInputStream zipInputStream = null;
        byte[] buffer = new byte[1024];

        String outputFolder = "repository" + File.separator + "deployment" + File.separator + "server"
                + File.separator + "jaggeryapps" + File.separator + "devportal" + File.separator + "site"
                + File.separator + "public" + File.separator + "tenant_themes" + File.separator + tenantDomain;

        try {
            //create output directory if it does not exist
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    APIUtil.handleException("Unable to create tenant theme directory at " + outputFolder);
                }
            } else {
                //remove existing files inside the directory
                FileUtils.cleanDirectory(folder);
            }

            //get the zip file content
            zipInputStream = new ZipInputStream(themeFile);
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
        } catch (IOException ex) {
            APIUtil.handleException("Failed to deploy tenant theme for tenant " + tenantDomain, ex);
        } finally {
            IOUtils.closeQuietly(zipInputStream);
            IOUtils.closeQuietly(themeFile);
        }
    }
}
