package org.wso2.carbon.apimgt.api.model;

import java.util.List;

public interface KeyManagerConnectorConfiguration {
    public String getImplementation();
    public String getJWTValidator();
    public List<ConfigurationDto> getConnectionConfigurations();
    public List<ConfigurationDto> getApplicationConfigurations();
}
