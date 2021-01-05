package org.wso2.carbon.graphql.api.devportal.modules;

public class DeploymentClusterInfoDTO {

    private String clusterName;
    private String clusterDisplayName;
    private String ingressURL;

    public DeploymentClusterInfoDTO(String clusterName,String clusterDisplayName,String ingressURL){
        this.clusterName = clusterName;
        this.clusterDisplayName = clusterDisplayName;
        this.ingressURL = ingressURL;

    }
    public DeploymentClusterInfoDTO(){

    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterDisplayName() {
        return clusterDisplayName;
    }

    public void setClusterDisplayName(String clusterDisplayName) {
        this.clusterDisplayName = clusterDisplayName;
    }

    public String getIngressURL() {
        return ingressURL;
    }

    public void setIngressURL(String ingressURL) {
        this.ingressURL = ingressURL;
    }
}
