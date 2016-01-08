package org.wso2.carbon.apimgt.rest.api.utils;
/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Util methods for Entitlement client use to validate requests
 */
public class EntitlementClientUtils {

    private static Properties configProperties;


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
        return configProperties != null && configProperties.getProperty(EntitlementClientConstants.SERVER_URL) != null ?
                configProperties.getProperty(EntitlementClientConstants.SERVER_URL)
                : "https://localhost:9444/services/";
    }

    public static String getServerUsername() {
        return configProperties != null && configProperties.getProperty(EntitlementClientConstants.SERVER_USER_NAME) != null ?
                configProperties.getProperty(EntitlementClientConstants.SERVER_USER_NAME) :
                "admin";
    }

    public static String getServerPassword() {
        String ret;
        if (configProperties != null){
            ret = configProperties.getProperty(EntitlementClientConstants.SERVER_PASSWORD);
            if(ret !=null){
                return ret;
            }
        }
        return "admin";
    }

    /**
     * reads values from config property file
     *
     * @throws Exception throws, if fails
     */
    public static void loadConfigProperties() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            File file = new File((new File(".")).getCanonicalPath() + File.separator + "resources" +
                    File.separator + "config.properties");
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                String msg = "File does not exist : " + "config.properties";
                System.out.println(msg);
            }
        } catch (FileNotFoundException e) {
            String msg = "File can not be found : " + "config.properties";
            System.out.println(msg);
            throw new Exception(msg, e);
        } catch (IOException e) {
            String msg = "Can not create the canonical file path for given file : " + "config.properties";
            System.out.println(msg);
            throw new Exception(msg, e);
        }

        try {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            String msg = "Error loading properties from config.properties file";
            System.out.println(msg);
            throw new Exception(msg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {
                System.out.println("Error while closing input stream");
            }
        }
        configProperties = properties;
    }


}