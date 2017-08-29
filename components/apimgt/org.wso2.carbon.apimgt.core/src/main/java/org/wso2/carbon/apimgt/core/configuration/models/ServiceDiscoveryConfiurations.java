package org.wso2.carbon.apimgt.core.configuration.models;

/**
 * For Openshift and Kubernetes
 */
public class ServiceDiscoveryConfiurations {
    private Boolean insidePod   = true;

    private String masterUrl = "https://.....:8443/";
    private String clientCertLocation = System.getProperty("user.dir")+"/src/main/resources/ca.crt";
    private String serviceAccountToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJteS1wcm9qZWN0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6Im15LXNlcnZpY2UtZGlzY292ZXJ5LXRva2VuLTdtNjRjIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6Im15LXNlcnZpY2UtZGlzY292ZXJ5Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiNjdlNjFiMWQtOGJkMS0xMWU3LTkyOGEtMDgwMDI3NTVmMDgxIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Om15LXByb2plY3Q6bXktc2VydmljZS1kaXNjb3ZlcnkifQ.Z3QaL4OIkLtuD32XtY183b8StQNOOI6uUcJi_9VQViJGtNhK7utjV-Q9nu3GVeD4Z7GwCvIH6F0hOwZGjdhNDIbUBh5mnNbzcxcX57RxrVLunymsuCZ2zurAeKpeXYwwlfeyHJfysOBXOFCIP4USlRmDoM8YcRjLsYS47fKlf4iCbzcPxPPlMkmQOMo_k0q_c03BRXKm-3d6mutokhOMoSvx4_9B1eMnTTRnz4k7SlNhHfNfSyv_FwzBbQLX7leb1ep3gmQ0nSLaEllBh5htkZXAGpMPsZvxRcZK1c9xj_0mpLiSYN8Rn9GLM9Fd6m2OPKAStKzeNCu9O3vBp1KNdg";


    public String getMasterUrl() {
        return masterUrl;
    }

    public Boolean isInsidePod() {
        return insidePod;
    }

    public String getClientCertLocation() {
        return clientCertLocation;
    }

    public String getServiceAccountToken() {
        return serviceAccountToken;
    }


}
