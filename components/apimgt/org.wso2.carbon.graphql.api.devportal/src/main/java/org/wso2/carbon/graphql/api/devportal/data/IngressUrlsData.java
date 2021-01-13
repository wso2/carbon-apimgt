package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.DeployEnvironment;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.graphql.api.devportal.modules.DeploymentClusterInfoDTO;
import org.wso2.carbon.graphql.api.devportal.modules.IngressUrlDTO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.DeploymentEnvironments;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.apimgt.persistence.utils.RegistryPersistenceUtil.extractDeploymentsForAPI;

public class IngressUrlsData {

    public List<IngressUrlDTO>  getIngressUrlData(String Id) throws APIManagementException, RegistryException, APIPersistenceException, UserStoreException {
//        RegistryData registryData = new RegistryData();
//        ApiTypeWrapper apiTypeWrapper  = registryData.getApiData(Id);

        ArtifactData artifactData = new ArtifactData();
        List<IngressUrlDTO> apiDeployedIngressURLs = new ArrayList<>();

        String deployments = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_DEPLOYMENTS);
        Set<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> deploymentEnvironments = extractDeploymentsForAPI(deployments);

        if (deploymentEnvironments != null && !deploymentEnvironments.isEmpty()) {
            Set<DeploymentEnvironments> selectedDeploymentEnvironments = deploymentEnvironments;
            if (selectedDeploymentEnvironments != null && !selectedDeploymentEnvironments.isEmpty()) {
                for (org.wso2.carbon.apimgt.api.model.DeploymentEnvironments deploymentEnvironment : selectedDeploymentEnvironments) {
                    IngressUrlDTO ingressUrlDTO = new IngressUrlDTO();
                    JSONArray clusterConfigs = APIUtil.getAllClustersFromConfig();
                    for (Object clusterConfig : clusterConfigs) {
                        JSONObject clusterConf = (JSONObject) clusterConfig;
                        List<DeploymentClusterInfoDTO> clusterInfoArray = new ArrayList<>();
                        if (clusterConf.get(ContainerBasedConstants.TYPE).toString().equalsIgnoreCase(deploymentEnvironment.getType())) {
                            JSONArray containerMgtInfoArray = (JSONArray) (clusterConf.get(ContainerBasedConstants.CONTAINER_MANAGEMENT_INFO));
                            for (Object containerMgtInfoObj : containerMgtInfoArray) {
                                JSONObject containerMgtInfo = (JSONObject) containerMgtInfoObj;
                                DeploymentClusterInfoDTO deploymentClusterInfoDTO = new DeploymentClusterInfoDTO();
                                if (deploymentEnvironment.getClusterNames().contains(containerMgtInfo.get(ContainerBasedConstants.CLUSTER_NAME).toString())) {

                                    String ClusterName =  containerMgtInfo.get(ContainerBasedConstants.CLUSTER_NAME).toString();
                                    deploymentClusterInfoDTO.setClusterName(ClusterName);
                                    String ClusterDisplayName = containerMgtInfo.get(ContainerBasedConstants.DISPLAY_NAME).toString();
                                    deploymentClusterInfoDTO.setClusterDisplayName(ClusterDisplayName);
                                    String IngressURL=null;
                                    if(((JSONObject) containerMgtInfo.get(ContainerBasedConstants.PROPERTIES)).get(ContainerBasedConstants.ACCESS_URL) != null){
                                        IngressURL = ((JSONObject) containerMgtInfo.get(ContainerBasedConstants.PROPERTIES))
                                                .get(ContainerBasedConstants.ACCESS_URL).toString();
                                        deploymentClusterInfoDTO.setIngressURL(IngressURL);
                                    }
                                    clusterInfoArray.add(deploymentClusterInfoDTO);
                                }
                            }
                            if (!clusterInfoArray.isEmpty()) {
                                ingressUrlDTO.setClusterDetails(clusterInfoArray);
                                ingressUrlDTO.setDeploymentEnvironmentName(deploymentEnvironment.getType());
                            }
                        }

                    }
                    if (ingressUrlDTO.getDeploymentEnvironmentName() != null && !ingressUrlDTO.getClusterDetails()
                            .isEmpty()) {
                        apiDeployedIngressURLs.add(ingressUrlDTO);
                    }

                }
            }



        }
        return apiDeployedIngressURLs;
    }

    public List<DeploymentClusterInfoDTO> getDeploymentClusterData(String Id) throws APIManagementException, RegistryException, APIPersistenceException, UserStoreException {
        List<DeploymentClusterInfoDTO> deploymentClusterInfoDTOList = new ArrayList<>();
        List<IngressUrlDTO> ingressUrlDTOList = getIngressUrlData(Id);

        for (int i = 0; i< ingressUrlDTOList.size();i++){
            deploymentClusterInfoDTOList.add((DeploymentClusterInfoDTO) ingressUrlDTOList.get(i).getClusterDetails());
        }
        return deploymentClusterInfoDTOList;
    }

    public List<DeployEnvironment> getDeplymentEnvironmentName(String Id) throws APIManagementException, RegistryException, APIPersistenceException, UserStoreException {
        List<IngressUrlDTO> ingressUrlDTOList = getIngressUrlData(Id);
        List<DeployEnvironment> deployEnvironmentList = new ArrayList<>();
        for(int i = 0; i< ingressUrlDTOList.size();i++){
            deployEnvironmentList.add(new DeployEnvironment(ingressUrlDTOList.get(i).getDeploymentEnvironmentName()));
        }
        return deployEnvironmentList;
    }



}
