package org.wso2.carbon.graphql.api.devportal.modules;

public class BusinessInformationDTO {

    private String id;
    private String businessOwner;
    private String businessOwnerEmail;
    private String technicalOwner;
    private String technicalOwnerEmail;

    public BusinessInformationDTO(String businessOwner, String businessOwnerEmail, String technicalOwner, String technicalOwnerEmail) {

        this.businessOwner = businessOwner;
        this.businessOwnerEmail = businessOwnerEmail;
        this.technicalOwner = technicalOwner;
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getId() {
        return id;
    }
}
