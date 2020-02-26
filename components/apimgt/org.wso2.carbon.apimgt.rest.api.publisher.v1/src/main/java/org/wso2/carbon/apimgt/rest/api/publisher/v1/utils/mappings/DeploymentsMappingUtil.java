package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryService;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentClusterInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class DeploymentsMappingUtil {

    private static final Log log = LogFactory.getLog(DeploymentsMappingUtil.class);

    /**
     * This method feeds data into DeploymentsDTO list from tenant-conf.json
     *
     * @return DeploymentsDTO list. List of Deployments
     * @throws APIManagementException
     */
    public DeploymentListDTO fromTenantConftoDTO() throws APIManagementException {

        DeploymentListDTO deploymentListDTO = new DeploymentListDTO();
        List<DeploymentsDTO> deploymentsList = new ArrayList<DeploymentsDTO>();

        //Get cloud environments from tenant-conf.json file
        //Get tenant domain to access tenant conf
        APIMRegistryService apimRegistryService = new APIMRegistryServiceImpl();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //read tenant-conf.json and get details
        try {
            String getTenantDomainConfContent = apimRegistryService
                    .getConfigRegistryResourceContent(tenantDomain, APIConstants.API_TENANT_CONF_LOCATION);
            JSONParser jsonParser = new JSONParser();
            Object tenantObject = jsonParser.parse(getTenantDomainConfContent);
            JSONObject tenant_conf = (JSONObject) tenantObject;
            //get kubernetes cluster info
            JSONObject ContainerMgtInfo = (JSONObject) tenant_conf.get("ContainerMgtInfo");
            DeploymentsDTO k8sClustersInfoDTO = new DeploymentsDTO();
            k8sClustersInfoDTO.setName((String) ContainerMgtInfo.get("Type"));
            //get clusters' properties
            List<DeploymentClusterInfoDTO> deploymentClusterInfoDTOList = new ArrayList<>();
            JSONObject clustersInfo = APIUtil.getClusterInfoFromConfig(ContainerMgtInfo.toString());
            clustersInfo.keySet().forEach(keyStr ->
            {
                Object clusterProperties = clustersInfo.get(keyStr);
                DeploymentClusterInfoDTO deploymentClusterInfoDTO = new DeploymentClusterInfoDTO();
                deploymentClusterInfoDTO.setClusterId((String) keyStr);
                deploymentClusterInfoDTO.setClusterName(((JSONObject) clusterProperties).get("Name").toString());
                deploymentClusterInfoDTO.setMasterURL(((JSONObject) clusterProperties).get("MasterURL").toString());
                deploymentClusterInfoDTO.setNamespace(((JSONObject) clusterProperties).get("Namespace").toString());
                deploymentClusterInfoDTO.setIngressURL(((JSONObject)clusterProperties).get("IngressURL").toString());

                if (!keyStr.toString().equals("")) {
                    deploymentClusterInfoDTOList.add(deploymentClusterInfoDTO);
                }
                //else part should be handle: null pointer exception
            });

            k8sClustersInfoDTO.setClusters(deploymentClusterInfoDTOList);
            deploymentsList.add(k8sClustersInfoDTO);

            deploymentListDTO.setCount(deploymentsList.size());
            deploymentListDTO.setList(deploymentsList);

        } catch (RegistryException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (UserStoreException e) {
            handleException("Couldn't read tenant configuration from tenant registry", e);
        } catch (ParseException e) {
            handleException("Couldn't parse tenant configuration for reading extension handler position", e);
        }
        return deploymentListDTO;
    }

}
