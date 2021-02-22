package org.wso2.carbon.graphql.api.devportal.data;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import org.apache.openjpa.conf.Specification;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.Api;
import org.wso2.carbon.graphql.api.devportal.modules.BusinessInformationDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

public class BusinessInformationData {


    public BusinessInformationDTO getBusinessInformations(DataFetchingEnvironment env) throws  APIPersistenceException {


        ArtifactData artifactData = new ArtifactData();

        Api api = env.getSource();
        String Id = api.getId();

        DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet = env.getSelectionSet();

//        if(dataFetchingFieldSelectionSet.contains("businessInformation")){
//            //Specification<BusinessInformationData> spec = byId(Id);
//            return new BusinessInformationDTO("","","","");
//        }else{
            DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
            String businessOwner = devPortalAPI.getBusinessOwner();
            String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();
            String technicalOwner = devPortalAPI.getTechnicalOwner();
            String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();

            return new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);
       // }
//
//        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
//
//        String businessOwner = devPortalAPI.getBusinessOwner();
//        String businessOwnerEmail = devPortalAPI.getBusinessOwnerEmail();
//        String technicalOwner = devPortalAPI.getTechnicalOwner();
//        String technicalOwnerEmail = devPortalAPI.getTechnicalOwnerEmail();
//
//        return new BusinessInformationDTO(businessOwner,businessOwnerEmail,technicalOwner,technicalOwnerEmail);


    }
//
//    private Specification byId(String id) {
//    }
}
