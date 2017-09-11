package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold Service Discovery configurations
 */
@Configuration(description = "Service Discovery configurations")
public class ServiceDiscoveryConfiurations {

    private String serviceAccountToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJl" +
            "cm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2" +
            "UiOiJteS1wcm9qZWN0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6Im15" +
            "LXNlcnZpY2UtZGlzY292ZXJ5LXRva2VuLTdtNjRjIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9" +
            "zZXJ2aWNlLWFjY291bnQubmFtZSI6Im15LXNlcnZpY2UtZGlzY292ZXJ5Iiwia3ViZXJuZXRlcy5pby9zZX" +
            "J2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNjdlNjFiMWQtOGJkMS0xMWU3LTkyOGEtMDgwMD" +
            "I3NTVmMDgxIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Om15LXByb2plY3Q6bXktc2VydmljZS1kaX" +
            "Njb3ZlcnkifQ.Z3QaL4OIkLtuD32XtY183b8StQNOOI6uUcJi_9VQViJGtNhK7utjV-Q9nu3GVeD4Z7GwCvI" +
            "H6F0hOwZGjdhNDIbUBh5mnNbzcxcX57RxrVLunymsuCZ2zurAeKpeXYwwlfeyHJfysOBXOFCIP4USlRmDoM8" +
            "YcRjLsYS47fKlf4iCbzcPxPPlMkmQOMo_k0q_c03BRXKm-3d6mutokhOMoSvx4_9B1eMnTTRnz4k7SlNhHfN" +
            "fSyv_FwzBbQLX7leb1ep3gmQ0nSLaEllBh5htkZXAGpMPsZvxRcZK1c9xj_0mpLiSYN8Rn9GLM9Fd6m2OPKA" +
            "StKzeNCu9O3vBp1KNdg";
    private String caCertLocation = System.getProperty("user.dir") + "/resources/security/ca.crt";
    private Boolean insidePod   = false;

    @Element(description = "enable service discovery")
    public Boolean enabled = true;
    @Element(description = "container management system master URL")
    private String masterUrl = "https://192.168.99.100:8443/";
    @Element(description = "container management system specific properties")
    private String properties = "{\"insidePod\": " + insidePod + "}";
    @Element(description = "security properties")
    private String security = "{\"serviceAccountToken\": \"" + serviceAccountToken + "\","
                                + "\"caCertLocation\": \"" + caCertLocation + "\"}";

    public String getProperties() {
        return properties;
    }

    public String getSecurity() {
        return security;
    }

    public Boolean isServiceDiscoveryEnabled() {
        return enabled;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

}
