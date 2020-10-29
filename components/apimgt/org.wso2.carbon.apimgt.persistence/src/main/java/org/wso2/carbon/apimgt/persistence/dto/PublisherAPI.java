package org.wso2.carbon.apimgt.persistence.dto;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
  Represents the API information stored in persistence layer, that is used for publisher operations
 */
public class PublisherAPI extends PublisherAPIInfo {
    // below all the attributes are added (via createAPIArtifactContent() method) and taken back from registry when
    // getting an API by ID.
    private boolean isDefaultVersion = false;
    private String description;
    private String wsdlUrl;
    private String wadlUrl; // is this required?
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String visibility;
    private String visibleRoles;
    private String visibleOrganizations; //visibleTenants
    private boolean endpointSecured;
    private boolean endpointAuthDigest;
    private String endpointUTUsername;
    private String endpointUTPassword;
    private String transports;
    private String inSequence;
    private String outSequence;
    private String faultSequence;
    private String responseCache;
    private int cacheTimeout;
    private String redirectURL;  // check ??
    private String apiOwner;
    private boolean advertiseOnly;
    private String endpointConfig;
    private String subscriptionAvailability; // e.g. "CURRENT_TENANT";who is allowed for subscriptions
    private String subscriptionAvailableOrgs; // subscriptionAvailableTenants;
    private String implementation;
    private String productionMaxTps;
    private String sandboxMaxTps;
    private String authorizationHeader;
    private String apiSecurity; // ?check whether same to private List<String> securityScheme = new ArrayList<>();
    private boolean enableSchemaValidation;
    private boolean enableStore;
    private String testKey;
    private String contextTemplate;
    private Set<String> availableTierNames;
    private Set<String> environments;
    private CORSConfiguration corsConfiguration;
    private Set<String> gatewayLabels;
    private Set<String> apiCategories;
    private boolean isMonetizationEnabled;
    private JSONObject monetizationProperties = new JSONObject();
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments;
    private Set<String> tags = new LinkedHashSet<>();
    private String accessControl; // publisher accessControl : 'restricted', 'all'
    private Set<String> accessControlRoles; // reg has a just String
    private JSONObject additionalProperties;

}

/*
    is needed? >
       artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context"); no usage found.
        API_OVERVIEW_VERSION_TYPE = "overview_versionType";

       artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true"); API_OVERVIEW_IS_LATEST =      "overview_isLatest" // this is  set only
       if api state is 'published'. no usage found
 */
