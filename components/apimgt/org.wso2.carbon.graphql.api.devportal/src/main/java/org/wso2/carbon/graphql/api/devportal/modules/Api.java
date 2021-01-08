package org.wso2.carbon.graphql.api.devportal.modules;


public class Api {
   private String id;
//    private String name;
    private String description;
    //private String context;
    //private String version;
   // private String provider;
    //private String apiDefinition;
    //private String type;
    private String transport;
    private boolean hasthumbnail;
    //private String additionalProperties;
    private String environments;
    private String wsdUrl;
    private String status;
    private boolean isSubscriptionAvailable;
    private String monetizationLabel;
    private boolean isDefault;
    private String authorizationHeader;
    private String apiSecurity;
    //private String tags;
    private boolean isMonetizationEnabled;
    //private Float rating;
    private String throttlingPolicies;
    private  String thumbnailUrl;
    private String categories;
    private String keyManagers;
    //private String createdTime;
    //private String lastUpdate;

    public Api(String id,String description,String transport,boolean hasthumbnail,String environments,String wsdUrl,String status,boolean isSubscriptionAvailable,String monetizationLabel,boolean isDefault,String authorizationHeader,String apiSecurity,boolean isMonetizationEnabled ,String throttlingPolicies,String thumbnailUrl,String categories,String keyManagers) {
        this.id = id;

        this.description = description;
//        this.context = context;
//        this.version = version;
//        this.provider = provider;
        //this.apiDefinition = apiDefinition;
       // this.type = type;
        this.transport=transport;
        this.hasthumbnail = hasthumbnail;
        //this.additionalProperties = additionalProperties;
        this.environments = environments;
        this.wsdUrl = wsdUrl;
        this.status = status;
        this.isSubscriptionAvailable = isSubscriptionAvailable;
        this.monetizationLabel = monetizationLabel;
        this.isDefault = isDefault;
        this.authorizationHeader = authorizationHeader;
        this.apiSecurity = apiSecurity;
        //this.tags = tags;
        this.isMonetizationEnabled = isMonetizationEnabled;
        //this.rating = rating;
        this.throttlingPolicies = throttlingPolicies;
        this.thumbnailUrl = thumbnailUrl;
        this.categories = categories;
        this.keyManagers = keyManagers;
//        this.createdTime = createdTime;
//        this.lastUpdate = lastUpdate;

    }

    public String getId() {
        return id;
    }
}
