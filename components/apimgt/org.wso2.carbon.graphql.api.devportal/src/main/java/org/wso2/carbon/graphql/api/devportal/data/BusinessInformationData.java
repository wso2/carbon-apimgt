package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;

public class BusinessInformationData {


    public BusinessInformationDTO getBusinessInformations(String Id) throws GovernanceException {


        ArtifactData artifactData = new ArtifactData();


        String businessOwner = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER);
        String businessOwnerEmail = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL);
        String technicalOwner = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER);
        String technicalOwnerEmail = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL);
        
        return new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);


    }
}
