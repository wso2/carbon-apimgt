package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;

public class AdvertiseData {

    public AdvertiseDTO getAdvertiseInformation(String Id) throws GovernanceException {

        ArtifactData artifactData = new ArtifactData();

        boolean advertised = Boolean.parseBoolean(artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY));
        String originalStoreUrl = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL);
        String apiOwner = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_OWNER);

        return new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);
    }
}
