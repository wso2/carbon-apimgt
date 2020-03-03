/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.cava.toml.TomlArray;
import net.consensys.cava.toml.TomlTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dto.ConfigDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class used to read the on-premise-gateway.toml configuration file
 */
public class ConfigManager {

    private static final Log log = LogFactory.getLog(ConfigManager.class);

    private static ConfigDTO configObject;
    private static volatile ConfigManager configManager = null;

    private ConfigManager() throws OnPremiseGatewayException {
        init();
    }

    /**
     * Method to get micro API gateway configuration manager
     *
     * @return micro API gateway configuration manager
     */
    public static ConfigDTO getConfigurationDTO() throws OnPremiseGatewayException {
        if (configManager == null) {
            synchronized (ConfigManager.class) {
                if (configManager == null) {
                    configManager = new ConfigManager();
                }
            }
        }
        return configObject;
    }

    /**
     * Method to get initialize micro API gateway configuration
     */
    private void init() throws OnPremiseGatewayException {
        if (log.isDebugEnabled()) {
            log.debug("Started reading micro gateway configurations");
        }
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                OnPremiseGatewayConstants.CONFIG_FILE_TOML_NAME;
        try {
            Map<String, Object> tomlResult = TomlParser.parse(configPath);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            configObject = mapper.convertValue(tomlResult, ConfigDTO.class);
        } catch (ConfigParserException ex) {
            String errorMessage = "Error occurred while reading the config file : "
                    + OnPremiseGatewayConstants.CONFIG_FILE_TOML_NAME;
            throw new OnPremiseGatewayException(errorMessage, ex);
        }
    }

    /**
     * Processing TomlArray for values
     * @param value TomlArray
     * @return processed TomlArray in to List
     */
    private static List<Object> processTomlArray(TomlArray value) {
        List<Object> finalList = new ArrayList<>();
        List<Object> tomlList = value.toList();
        for (Object obj : tomlList) {
            if (obj instanceof TomlArray) {
                finalList.add(processTomlArray((TomlArray) obj));
            } else if (obj instanceof TomlTable) {
                finalList.add(processTomlMap((TomlTable) obj));
            } else {
                finalList.add(obj);
            }
        }
        return finalList;
    }

    /**
     * Processing TomlTable in to a Map for reading
     * @param tomlTable TomlTable values from parsed toml
     * @return  Map from Key <String> to Value <Object>
     */
    private static Map<String, Object> processTomlMap(TomlTable tomlTable) {

        Map<String, Object> finalMap = new LinkedHashMap<>();
        Set<String> dottedKeySet = tomlTable.dottedKeySet();
        for (String key : dottedKeySet) {
            // To support single quoted keys in the toml inside an array.
            // Eg: [[a.b]]
            //     'c.d' = "value"
            key = key.replaceAll("\"", "'");
            Object value = tomlTable.get(key);
            if (value instanceof TomlArray) {
                finalMap.put(key, processTomlArray((TomlArray) value));
            } else {
                finalMap.put(key, tomlTable.get(key));
            }
        }

        return finalMap;
    }
}