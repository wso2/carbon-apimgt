/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com)
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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles loading and accessing dependency configuration properties. The properties file is first read from the Carbon
 * configuration directory, and if not found, it is loaded from the classpath. Loaded properties are kept in memory and
 * can be retrieved through this class.
 */
public class APIMDependencyConfiguration {

    private static final Log log = LogFactory.getLog(APIMDependencyConfiguration.class);
    private final OASParserOptions oasParserOptions = new OASParserOptions();
    private Properties dependencyProperties = new Properties();

    public APIMDependencyConfiguration() {
    }

    public Properties getDependencyProperties() {
        return dependencyProperties;
    }

    public void setDependencyProperties(Properties dependencyProperties) {
        this.dependencyProperties = dependencyProperties;
    }

    /**
     * Returns the OAS parser options.
     *
     * @return OASParserOptions instance
     */
    public OASParserOptions getOasParserOptions() {
        return oasParserOptions;
    }

    /**
     * Returns the value of the specified property key.
     *
     * @param key property name
     * @return value of the property or null if not found
     */
    public Object getProperty(String key) {
        return dependencyProperties.get(key);
    }

    /**
     * Loads the properties file from the Carbon config directory.
     *
     * @param propertiesFileName name of the properties file to load
     * @return loaded Properties object
     * @throws APIManagementException if an error occurs while reading the file
     */
    public static Properties loadProperties(String propertiesFileName) throws APIManagementException {

        Properties properties = new Properties();

        if (log.isDebugEnabled()) {
            log.debug("Loading a file '" + propertiesFileName + "' from classpath");
        }
        try (InputStream in = new FileInputStream(
                CarbonUtils.getCarbonConfigDirPath() + File.separator + propertiesFileName)) {
            properties.load(in);
        } catch (FileNotFoundException e) {
            String msg = "Error loading properties from a file at from the System defined location: " + propertiesFileName;
            log.warn(msg);
        } catch (IOException e) {
            throw new APIManagementException(
                    "I/O error while reading the API manager dependency configuration " + "properties file: " + propertiesFileName,
                    e);
        } catch (Exception e) {
            throw new APIManagementException(
                    "Unexpected error occurred while loading dependency properties: " + propertiesFileName, e);
        }
        return properties;
    }

    /**
     * Loads dependency properties from the given file.
     *
     * @param dependencyConfigFilePath properties file path
     * @throws APIManagementException if loading fails
     */
    public void load(String dependencyConfigFilePath) throws APIManagementException {
        this.dependencyProperties = loadProperties(dependencyConfigFilePath);
        oasParserOptions.setExplicitStyleAndExplode(
                this.dependencyProperties.getProperty(DependencyConstants.EXPLICIT_STYLE_AND_EXPLODE));
    }
}
