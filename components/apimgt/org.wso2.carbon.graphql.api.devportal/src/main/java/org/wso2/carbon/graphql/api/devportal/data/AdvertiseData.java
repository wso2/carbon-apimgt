package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;

public class AdvertiseData {

    public AdvertiseDTO getAdvertiseInformation(String Id) throws GovernanceException {

        ArtifactData artifactData = new ArtifactData();
        GenericArtifact apiArtifact = artifactData.getDevportalApis(Id);

        boolean advertised = Boolean.parseBoolean(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY));
        String originalStoreUrl =apiArtifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL);
        String apiOwner = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_OWNER);

        return new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);
    }
}
