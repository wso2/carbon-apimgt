package org.wso2.carbon.graphql.api.devportal.modules;

public class AdvertiseDTO {
    private boolean advertised;
    private String originalStoreUrl;
    private String apiOwner;

    public AdvertiseDTO(boolean advertised,String originalStoreUrl,String apiOwner){
        this.advertised = advertised;
        this.originalStoreUrl = originalStoreUrl;
        this.apiOwner = apiOwner;

    }
}
