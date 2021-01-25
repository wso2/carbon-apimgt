package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.AdvertiseDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

public class AdvertiseData {

    public AdvertiseDTO getAdvertiseInformation(String Id) throws RegistryException, APIPersistenceException, UserStoreException {

        ArtifactData artifactData = new ArtifactData();

        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        boolean advertised = devPortalAPI.isAdvertiseOnly();//Boolean.parseBoolean(apiArtifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY));
        String originalStoreUrl = devPortalAPI.getRedirectURL();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL);
        String apiOwner = devPortalAPI.getApiOwner();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_OWNER);

        return new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);
    }
}
