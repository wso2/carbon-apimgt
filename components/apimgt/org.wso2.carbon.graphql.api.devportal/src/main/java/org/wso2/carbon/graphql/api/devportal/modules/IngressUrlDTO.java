package org.wso2.carbon.graphql.api.devportal.modules;

import java.util.ArrayList;
import java.util.List;

public class IngressUrlDTO {
    private String deploymentEnvironmentName;
    private List<DeploymentClusterInfoDTO> clusterDetails = new ArrayList<DeploymentClusterInfoDTO>();


    public IngressUrlDTO(String deploymentEnvironmentName,List<DeploymentClusterInfoDTO> clusterDetails){
        this.deploymentEnvironmentName = deploymentEnvironmentName;
        this.clusterDetails = clusterDetails;

    }

    public IngressUrlDTO(){

    }

    public String getDeploymentEnvironmentName() {
        return deploymentEnvironmentName;
    }

    public void setDeploymentEnvironmentName(String deploymentEnvironmentName) {
        this.deploymentEnvironmentName = deploymentEnvironmentName;
    }

    public List<DeploymentClusterInfoDTO> getClusterDetails() {
        return clusterDetails;
    }

    public void setClusterDetails(List<DeploymentClusterInfoDTO> clusterDetails) {
        this.clusterDetails = clusterDetails;
    }
}
