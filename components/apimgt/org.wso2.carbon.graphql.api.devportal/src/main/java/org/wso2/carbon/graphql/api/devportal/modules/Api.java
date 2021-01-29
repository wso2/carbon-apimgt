package org.wso2.carbon.graphql.api.devportal.modules;


public class Api {
   private String id;
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

    public Api(String id,String name,String description,String context,String version,String provider,String type,String transport,boolean hasthumbnail,String environments,String wsdUrl,String status,boolean isSubscriptionAvailable,boolean isDefault,String authorizationHeader,String apiSecurity,boolean isMonetizationEnabled ,String throttlingPolicies,String thumbnailUrl,String categories,String keyManagers) {
        this.id = id;
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


    }

    public String getId() {
        return id;
    }


}
