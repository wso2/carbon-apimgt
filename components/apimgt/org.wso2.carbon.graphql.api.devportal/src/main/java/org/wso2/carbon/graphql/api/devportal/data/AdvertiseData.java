package org.wso2.carbon.graphql.api.devportal.data;


import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.AdvertiseDTO;


public class AdvertiseData {

    public AdvertiseDTO getAdvertiseInformation(String Id) throws  APIPersistenceException {

        ArtifactData artifactData = new ArtifactData();

        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        boolean advertised = devPortalAPI.isAdvertiseOnly();
        String originalStoreUrl = devPortalAPI.getRedirectURL();
        String apiOwner = devPortalAPI.getApiOwner();

        return new AdvertiseDTO(advertised,originalStoreUrl,apiOwner);
    }
}
