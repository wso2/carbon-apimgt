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

    public static class KeyManagerConfigurationRetrieverDto {
        private Properties jmsConnectionParameters = new Properties();

        public Properties getJmsConnectionParameters() {

            return jmsConnectionParameters;
        }

        public void setJmsConnectionParameters(Properties jmsConnectionParameters) {

            this.jmsConnectionParameters = jmsConnectionParameters;
        }
    }

}
