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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class PersistenceUtil {

    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    public static void handleException(String msg) throws APIManagementException {
        throw new APIManagementException(msg);
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

    public static boolean isAdminUser(UserContext userContext) {
        boolean isAdmin = false;
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_ISADMIN)) {
            isAdmin = (Boolean) properties.get(APIConstants.USER_CTX_PROPERTY_ISADMIN);
        }
        return isAdmin;
    }

    public static String getSkipRoles(UserContext userContext) {
        String skipRoles = "";
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES)) {
            skipRoles = (String) properties.get(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES);
        }
        return skipRoles;
    }
}
