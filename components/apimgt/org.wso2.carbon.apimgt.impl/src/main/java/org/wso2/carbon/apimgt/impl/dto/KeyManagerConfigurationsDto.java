package org.wso2.carbon.apimgt.impl.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KeyManagerConfigurationsDto {
    private boolean enabled = false;
    private String serviceUrl;
    private int initDelay = 0;
    private String username;
    private char[] password;
    private KeyManagerConfigurationRetrieverDto keyManagerConfigurationRetrieverDto;
    private Map<String, KeyManagerConfigurationDto> keyManagerConfigurationDtoMap = new HashMap<>();

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getServiceUrl() {

        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {

        this.serviceUrl = serviceUrl;
    }

    public int getInitDelay() {

        return initDelay;
    }

    public void setInitDelay(int initDelay) {

        this.initDelay = initDelay;
    }

    public KeyManagerConfigurationRetrieverDto getKeyManagerConfigurationRetrieverDto() {

        return keyManagerConfigurationRetrieverDto;
    }

    public void setKeyManagerConfigurationRetrieverDto(
            KeyManagerConfigurationRetrieverDto keyManagerConfigurationRetrieverDto) {

        this.keyManagerConfigurationRetrieverDto = keyManagerConfigurationRetrieverDto;
    }

    public Map<String, KeyManagerConfigurationDto> getKeyManagerConfiguration() {

        return keyManagerConfigurationDtoMap;
    }

    public void putKeyManagerConfigurationDto(String type, KeyManagerConfigurationDto keyManagerConfigurationDto) {

        this.keyManagerConfigurationDtoMap.put(type, keyManagerConfigurationDto);
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public void setPassword(char[] password) {

        this.password = password;
    }

    public String getPassword() {

        return String.valueOf(password);
    }

    public static class KeyManagerConfigurationDto {
        private String implementationClass;

        private List<ConfigurationDto> connectionConfigurationDtoList = new ArrayList<>();
        private List<ConfigurationDto> applicationConfigurationDtoList = new ArrayList<>();

        public String getImplementationClass() {

            return implementationClass;
        }

        public void setImplementationClass(String implementationClass) {

            this.implementationClass = implementationClass;
        }

        public List<ConfigurationDto> getConnectionConfigurationDtoList() {

            return connectionConfigurationDtoList;
        }

        public void addConnectionConfigurationDtoList(ConfigurationDto configurationDto) {

            this.connectionConfigurationDtoList.add(configurationDto);
        }

        public List<ConfigurationDto> getApplicationConfigurationDtoList() {

            return applicationConfigurationDtoList;
        }

        public void addApplicationConfigurationDtoList(ConfigurationDto configurationDto) {

            this.applicationConfigurationDtoList.add(configurationDto);
        }
    }

    public static class KeyManagerConfigurationRetrieverDto {
        private Properties jmsConnectionParameters = new Properties();

        public Properties getJmsConnectionParameters() {

            return jmsConnectionParameters;
        }

        public void setJmsConnectionParameters(Properties jmsConnectionParameters) {

            this.jmsConnectionParameters = jmsConnectionParameters;
        }
    }

    public static class ConfigurationDto {

        private String name;
        private String label;
        private String type;
        private String tooltip;
        private String defaultValue;
        private boolean required;
        private boolean mask;
        private List<String> values = new ArrayList<>();
        private boolean multiple;
        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }

        public String getLabel() {

            return label;
        }

        public void setLabel(String label) {

            this.label = label;
        }

        public String getType() {

            return type;
        }

        public void setType(String type) {

            this.type = type;
        }

        public String getTooltip() {

            return tooltip;
        }

        public void setTooltip(String tooltip) {

            this.tooltip = tooltip;
        }

        public String getDefaultValue() {

            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {

            this.defaultValue = defaultValue;
        }

        public boolean isRequired() {

            return required;
        }

        public void setRequired(boolean required) {

            this.required = required;
        }

        public boolean isMask() {

            return mask;
        }

        public void setMask(boolean mask) {

            this.mask = mask;
        }

        public List<String> getValues() {

            return values;
        }

        public void setValues(List<String> values) {

            this.values = values;
        }

        public boolean isMultiple() {

            return multiple;
        }

        public void setMultiple(boolean multiple) {

            this.multiple = multiple;
        }
        public void addValue(String value){
            this.values.add(value);
        }
    }
}
