package org.wso2.carbon.apimgt.gateway.common.dto;

/**
 * Holds configurations related to JWKS
 */
public class JWKSConfigurationDTO {
    private String url;
    private boolean enabled;

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public JWKSConfigurationDTO(String url, boolean enabled) {

        this.url = url;
        this.enabled = enabled;
    }

    public JWKSConfigurationDTO() {

    }
}
