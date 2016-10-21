/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.utils;

import java.io.*;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.util.*;

/**
 * Util methods for Entitlement client use to validate requests
 */
public class EntitlementClientUtils {

    private static Properties configProperties;
    private static final Log logger = LogFactory.getLog(EntitlementClientUtils.class);

    /*public static String getPolicyDirectoryPath(String samplePolicyName) {
        String path = null;
        if(configProperties != null){
            String policyPath =  configProperties.getProperty(EntitlementClientConstants.POLICY_PATH);
            if(policyPath != null && policyPath.trim().length() > 0){
                path = configProperties.getProperty(EntitlementClientConstants.POLICY_PATH) + File.separator + samplePolicyName + ".xml";
            }
        }

        if(path == null){
            try{
                File file = new File((new File(".")).getCanonicalPath() + File.separator +"resources" +
                        File.separator +"policy" + File.separator + samplePolicyName + ".xml");
                if(file.exists()){
                    path = file.getCanonicalPath();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return path;
    } */

    /*public static String getTrustStore() throws Exception {
        if(configProperties != null  && configProperties.getProperty(EntitlementClientConstants.TRUST_STORE_PATH) != null){
            return  configProperties.getProperty(EntitlementClientConstants.TRUST_STORE_PATH);
        } else {
            try{
                File file = new File((new File(".")).getCanonicalPath() + File.separator +"resources" +
                        File.separator +"wso2carbon.jks");
                if(file.exists()){
                    return file.getCanonicalPath();
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new Exception("Error while calculating trust store path", e);
            }
        }
    }

    public static String getTrustStorePassword(){
        if(configProperties != null && configProperties.getProperty(EntitlementClientConstants.TRUST_STORE_PASSWORD) != null){
            return configProperties.getProperty(EntitlementClientConstants.TRUST_STORE_PASSWORD);
        } else {
            return "wso2carbon";
        }
    } */

    public static String getServerUrl() {
        return configProperties != null && configProperties.getProperty(RestApiConstants.SERVER_URL) != null ?
                configProperties.getProperty(RestApiConstants.SERVER_URL)
                : "https://localhost:9444/services/";
    }

    public static String getServerUsername() {
        return configProperties != null && configProperties.getProperty(RestApiConstants.SERVER_USER_NAME) != null ?
                configProperties.getProperty(RestApiConstants.SERVER_USER_NAME) :
                "admin";
    }

    public static String getServerPassword() {
        String ret;
        if (configProperties != null) {
            ret = configProperties.getProperty(RestApiConstants.SERVER_PASSWORD);
            if (ret != null) {
                return ret;
            }
        }
        return "admin";
    }

    /**
     * reads values from config property file
     *
     * @throws APIManagementException throws, if fails
     */
    public static void loadConfigProperties() throws APIManagementException {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            File file = new File((new File(".")).getCanonicalPath() + File.separator + "resources" +
                    File.separator + "config.properties");
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                String msg = "File does not exist : " + "config.properties";
                logger.error(msg);
            }
            try {
                if (inputStream != null) {
                    properties.load(inputStream);
                    configProperties = properties;
                }
            } catch (IOException e) {
                String msg = "Error loading properties from config.properties file";
                logger.error(msg, e);
                throw new APIManagementException(msg, e);
            }

        } catch (FileNotFoundException e) {
            String msg = "File can not be found : " + "config.properties";
            logger.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (IOException e) {
            String msg = "Can not create the canonical file path for given file : " + "config.properties";
            logger.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


}