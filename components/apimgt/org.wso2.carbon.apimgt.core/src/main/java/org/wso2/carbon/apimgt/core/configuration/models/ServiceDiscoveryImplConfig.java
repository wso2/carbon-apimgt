package org.wso2.carbon.apimgt.core.configuration.models;


import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

import java.util.HashMap;

/**
 * Class to hold Container Management System specific Service Discovery configurations
 */
@Configuration(description = "Container Management System specific Service Discovery configurations")
public class ServiceDiscoveryImplConfig {

    @Element(description = "enable specified service discovery")
    private Boolean cmsWiseEnabled = true;

    @Element(description = "service discovery implementation class")
    private String implementationClass = APIMgtConstants.ServiceDiscoveryConstants.
            KUBERNETES_SERVICE_DISCOVERER;

    @Element(description = "container management system specific properties")
    private HashMap<String, String> cmsSpecificParameters = new HashMap<>();

    public ServiceDiscoveryImplConfig() {

        cmsSpecificParameters.put("masterUrl", "https://192.168.99.100:8443/");
        cmsSpecificParameters.put("includeClusterIPs", "false");
        cmsSpecificParameters.put("includeExternalNameServices", "false");

        String serviceAccountToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlc" +
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
        cmsSpecificParameters.put("serviceAccountToken", serviceAccountToken);

        String caCertLocation = System.getProperty("user.dir") + "/resources/security/ca.crt";
        cmsSpecificParameters.put("caCertLocation", caCertLocation);
    }


    public Boolean isEnabled() {
        return cmsWiseEnabled;
    }

    public void setEnabled(Boolean enabled) {
        this.cmsWiseEnabled = enabled;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public HashMap<String, String> getCmsSpecificParameters() {
        return cmsSpecificParameters;
    }

    public void setCmsSpecificParameters(HashMap<String, String> cmsSpecificParameters) {
        this.cmsSpecificParameters = cmsSpecificParameters;
    }
}
