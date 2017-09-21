package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;
import org.wso2.carbon.kernel.annotations.Ignore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold Service Discovery configurations and generate yaml file
 */
@Configuration(namespace = "wso2.carbon.serviceDiscovery", description = "Service Discovery configurations")
public class ServiceDiscoveryConfigurations {

    //kubernetes
    @Ignore
    private String serviceAccountToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlc" +
            "m5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2Ui" +
            "OiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6InNlcnZpY2U" +
            "tZGlzY292ZXJ5LXNhLXRva2VuLTJnNGJnIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aW" +
            "NlLWFjY291bnQubmFtZSI6InNlcnZpY2UtZGlzY292ZXJ5LXNhIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlY" +
            "WNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiMjRjMjRjOTgtOTgyYi0xMWU3LWI2MjYtMDgwMDI3NTVm" +
            "MDgxIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmRlZmF1bHQ6c2VydmljZS1kaXNjb3Zlcnktc2E" +
            "ifQ.OkAfoETwdbeH-ARM3mRMR24oqUahfvP3gLw-QqzqY446jHMkmnfh71yYPI8X_pdwA7de1WCPSw3Plci" +
            "NlEhzC-Zv5w7itpmEWoB-80SsEAa07g1g713TpSqlv3oL0-98zTF8RyyyFez_5hwBP6XsbRKLv0B3cSPbI3" +
            "ByZCIcfHlThkenUarb1RGDtcS8RmcpPD-hpzsD44Jy9wMs9y_bhkCltv911EXxbqD2vlg6je4LUp0s2Zze-" +
            "IsDXS9PwzYZo4J33I3OxrenONJjBWV2LdOwi_HXJNOT8iVCV_jtXxzZ8123A8CEjmdalpceulGNfS5S7OF-A" +
            "g7GHwiHH33jGA";
    @Ignore
    private String caCertLocation = System.getProperty("user.dir") + "/resources/security/ca.crt";
    @Ignore
    private Boolean insidePod   = false;

    @Element(description = "enable service discovery")
    public Boolean enabled = true;
    @Element(description = "container management system master URL")
    private String masterUrl = "https://192.168.99.100:8443/";  //kubernetes
    @Element(description = "container management system specific properties")
    private Map<String, String> cmsSpecificParameters = new HashMap<>();
    @Element(description = "security properties")
    private Map<String, String> security = new HashMap<>();

    public ServiceDiscoveryConfigurations(){
        cmsSpecificParameters.put("insidePod", insidePod.toString());
        security.put("serviceAccountToken", serviceAccountToken);
        security.put("caCertLocation", caCertLocation);
        security.put("tokenKeyStoreFilePath", System.getProperty("user.dir") + "/resources/security/wso2carbon.jks");
        security.put("tokenKeyStorePassword", "admin");
        security.put("tokenAlias", "serviceAccountToken");
        security.put("tokenAliasPassword", "admin");
    }

    public Map<String, String> getCmsSpecificParameters() {
        return cmsSpecificParameters;
    }

    public Map<String, String> getSecurity() {
        return security;
    }

    public Boolean isServiceDiscoveryEnabled() {
        return enabled;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

}
