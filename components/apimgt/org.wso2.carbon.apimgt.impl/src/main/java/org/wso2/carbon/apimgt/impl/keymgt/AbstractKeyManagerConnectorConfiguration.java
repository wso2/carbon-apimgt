package org.wso2.carbon.apimgt.impl.keymgt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.base.CarbonBaseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractKeyManagerConnectorConfiguration implements KeyManagerConnectorConfiguration {

    private Map<String, List<ConfigurationDto>> configListMap = new HashMap<>();
    private Log log = LogFactory.getLog(AbstractKeyManagerConnectorConfiguration.class);
    public static final String CONNECTOR_CONFIGURATION = "configurations";
    public static final String APPLICATION_CONFIGURATIONS = "application_configurations";

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {

        if (configListMap.get(CONNECTOR_CONFIGURATION) == null) {
            convertJsonToConnectorConfiguration();
        }
        return configListMap.get(CONNECTOR_CONFIGURATION);
    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {

        if (configListMap.get(APPLICATION_CONFIGURATIONS) == null) {
            convertJsonToConnectorConfiguration();
        }
        return configListMap.get(APPLICATION_CONFIGURATIONS);
    }

    private void convertJsonToConnectorConfiguration() {

        configListMap.put(CONNECTOR_CONFIGURATION, Collections.emptyList());
        configListMap.put(APPLICATION_CONFIGURATIONS, Collections.EMPTY_LIST);
        String connectorConfigPath = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" +
                File.separator + "keyManager-extensions" +File.separator+ getType() + ".json";
        File file = new File(connectorConfigPath);
        if (file.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                String content = IOUtils.toString(fileInputStream);
                Gson gson = new Gson();
                Type configurationDtoType = new TypeToken<Map<String, List<ConfigurationDto>>>() {
                }.getType();
                configListMap = gson.fromJson(content, configurationDtoType);
            } catch (IOException e) {
                log.error("Error while reading connector configuration", e);
            }
        }
    }
}
