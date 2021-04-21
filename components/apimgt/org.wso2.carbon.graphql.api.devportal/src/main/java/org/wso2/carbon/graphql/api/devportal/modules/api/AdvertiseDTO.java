package org.wso2.carbon.graphql.api.devportal.modules.api;

public class AdvertiseDTO {
    private boolean advertised;
    private String originalStoreUrl;
    private String apiOwner;
    private String vendor;

    public AdvertiseDTO(boolean advertised,String originalStoreUrl,String apiOwner,String vendor){
        this.advertised = advertised;
        this.originalStoreUrl = originalStoreUrl;
        this.apiOwner = apiOwner;
        this.vendor = vendor;

    }
}
