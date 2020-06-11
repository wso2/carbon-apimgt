package org.wso2.carbon.apimgt.api.model;

/**
 * This class is to handle configuration details for cluster
 *
 */
public class ServiceDiscoveryConfigurations {
    private boolean enabled = false;
    private String type;
    private String className;
    private String displayName;
    private ServiceDiscoveryConf implParameters;

    public ServiceDiscoveryConf getImplParameters() {
        return implParameters;
    }

    public void setImplParameters(ServiceDiscoveryConf implParameters) {
        this.implParameters = implParameters;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
