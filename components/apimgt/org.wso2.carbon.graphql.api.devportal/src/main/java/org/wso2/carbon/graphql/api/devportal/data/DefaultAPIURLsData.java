package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.DefaultAPIURLsDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultAPIURLsData {

    public DefaultAPIURLsDTO getDefaultAPIURLsData(String Id) throws RegistryException, APIPersistenceException, UserStoreException {


        ArtifactData artifactData = new ArtifactData();


        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        Set<String> environmentsPublishedByAPI = devPortalAPI.getEnvironments();//ApiDetails.getEnvironments(artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS));;
        environmentsPublishedByAPI.remove("none");

        String http = null;
        String https = null;
        String ws = null;
        String wss = null;
        Set<String> apiTransports = new HashSet<>(Arrays.asList(devPortalAPI.getTransports().split(",")));
        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if(environment !=null){
                String[] gwEndpoints = null; //environment.getApiGatewayEndpoint().split(",");
                if ("WS".equalsIgnoreCase(devPortalAPI.getType())) {
                    gwEndpoints = environment.getWebsocketGatewayEndpoint().split(",");
                } else {
                    gwEndpoints = environment.getApiGatewayEndpoint().split(",");
                }
                for (String gwEndpoint : gwEndpoints) {
                    StringBuilder endpointBuilder = new StringBuilder(gwEndpoint);
                    endpointBuilder.append(devPortalAPI.getContext());
                    if(devPortalAPI.getIsDefaultVersion()){
                        //artifactData.getDevportalApis(Id).getAttribute(
                        //                            APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)
                        int index = endpointBuilder.indexOf(devPortalAPI.getVersion());
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        if (gwEndpoint.contains("http:") && apiTransports.contains("http")){
                            http = endpointBuilder.toString();
                        }else if(gwEndpoint.contains("https:") && apiTransports.contains("https")){
                            https = endpointBuilder.toString();
                        }else if (gwEndpoint.contains("ws:")) {
                            ws = endpointBuilder.toString();
                        } else if (gwEndpoint.contains("wss:")) {
                            wss = endpointBuilder.toString();
                        }
                    }

                }
            }
        }
        return new DefaultAPIURLsDTO(http,https,ws,wss);
    }
}
