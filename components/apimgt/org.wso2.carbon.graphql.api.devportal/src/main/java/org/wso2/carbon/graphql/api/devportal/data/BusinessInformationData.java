package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

public class BusinessInformationData {


    public BusinessInformationDTO getBusinessInformations(String Id) throws RegistryException, APIPersistenceException, UserStoreException {


        ArtifactData artifactData = new ArtifactData();

        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);

        String businessOwner = devPortalAPI.getBusinessOwner();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER);
        String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL);
        String technicalOwner = devPortalAPI.getTechnicalOwner();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER);
        String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();//apiArtifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL);
        
        return new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);


    }
}
