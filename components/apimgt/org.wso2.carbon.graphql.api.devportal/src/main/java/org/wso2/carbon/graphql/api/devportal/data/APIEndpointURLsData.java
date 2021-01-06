package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.APIEndpointURLsDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import java.util.*;

public class APIEndpointURLsData {

    public List<APIEndpointURLsDTO> apiEndpointURLsDTO(String Id) throws GovernanceException {

        String env = "";

        ArtifactData artifactData = new ArtifactData();

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        Set<String> environmentsPublishedByAPI = ApiDetails.getEnvironments(artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));//new HashSet<>(apiTypeWrapper.getApi().getEnvironments());
        environmentsPublishedByAPI.remove("none");


        List<APIEndpointURLsDTO> apiEndpointURLsDTOS = new ArrayList<APIEndpointURLsDTO>();
        for (String environmentName : environmentsPublishedByAPI) {
            env += environmentName;
            Environment environment = environments.get(environmentName);
            if(environment!=null){
                String name = environment.getName();
                String type = environment.getType();
                apiEndpointURLsDTOS.add(new APIEndpointURLsDTO(Id,name, type));
            }


        }
        return  apiEndpointURLsDTOS;
    }
}
