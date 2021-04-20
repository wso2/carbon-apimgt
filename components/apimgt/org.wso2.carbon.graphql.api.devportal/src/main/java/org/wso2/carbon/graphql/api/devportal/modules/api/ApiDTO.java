package org.wso2.carbon.graphql.api.devportal.modules.api;


import java.util.List;

public class ApiDTO {
   private String uuid;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private String type;
    private String transport;
    private boolean hasthumbnail;
    private String environments;
    private String wsdUrl;
    private String status;
    private boolean isSubscriptionAvailable;
    private boolean isDefault;
    private String authorizationHeader;
    private String apiSecurity;
    private boolean isMonetizationEnabled;
    private String throttlingPolicies;
    private  String thumbnailUrl;
    private String categories;
    private String keyManagers;

    private BusinessInformationDTO businessInformation;

    private AdvertiseDTO advertiseInfo;

    private List<APIEndpointURLsDTO> apiEndPointInformation;

    private List<TierNameDTO> tierInformation;




    public ApiDTO(String uuid, String name, String description, String context, String version, String provider, String type, String transport, boolean hasthumbnail, String environments, String wsdUrl, String status, boolean isSubscriptionAvailable, boolean isDefault, String authorizationHeader, String apiSecurity, boolean isMonetizationEnabled , String throttlingPolicies, String thumbnailUrl, String categories, String keyManagers, BusinessInformationDTO businessInformation, AdvertiseDTO advertiseInfo, List<APIEndpointURLsDTO> apiEndPointInformation, List<TierNameDTO> tierInformation) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.context = context;
        this.version = version;
        this.provider = provider;
        this.type = type;
        this.transport=transport;
        this.hasthumbnail = hasthumbnail;
        this.environments = environments;
        this.wsdUrl = wsdUrl;
        this.status = status;
        this.isSubscriptionAvailable = isSubscriptionAvailable;
        this.isDefault = isDefault;
        this.authorizationHeader = authorizationHeader;
        this.apiSecurity = apiSecurity;
        this.isMonetizationEnabled = isMonetizationEnabled;
        this.throttlingPolicies = throttlingPolicies;
        this.thumbnailUrl = thumbnailUrl;
        this.categories = categories;
        this.keyManagers = keyManagers;

        this.businessInformation = businessInformation;

        this.advertiseInfo = advertiseInfo;

        this.apiEndPointInformation = apiEndPointInformation;

        this.tierInformation = tierInformation;


    }

    public String getUuid() {
        return uuid;
    }

    public String getThrottlingPolicies() {
        return throttlingPolicies;
    }

    public void setThrottlingPolicies(String throttlingPolicies) {
        this.throttlingPolicies = throttlingPolicies;
    }
}
