package org.wso2.carbon.apimgt.persistence.dto;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
  Represents the API information stored in persistence layer, that is used for publisher operations
 */
public class PublisherAPI {
    private String apiName;
    private String version;
    private String id;
    private boolean isDefaultVersion = false;
    private String context;
    private final String providerName = ""; // set the final value in a constructor
    private String description;
    private String wsdlUrl;
    private String wadlUrl; // is this required?
    private String thumbnailUrl;
    private String status;
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String visibility;
    private String visibleRoles;
    private String visibleTenants; // >> change to visibleOrganizations
    private boolean endpointSecured = false;
    private boolean endpointAuthDigest = false;
    private String endpointUTUsername;
    private String endpointUTPassword;
    private String transports;
    private String inSequence;
    private String outSequence;
    private String faultSequence;
    private String responseCache;
    private int cacheTimeout;
    private String redirectURL;
    private String apiOwner;
    private boolean advertiseOnly;
    private String endpointConfig;
    private String subscriptionAvailability;
    private String subscriptionAvailableTenants; // >> change to subscriptionAvailableOrgs
    private String implementation = "ENDPOINT";
    private String productionMaxTps;
    private String sandboxMaxTps;
    private String authorizationHeader;
    private String apiSecurity = "oauth2";
    private boolean enableSchemaValidation = false;
    private boolean enableStore = true;
    private String testKey;
    private String contextTemplate;
    private String type;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
    private Set<String> environments;
    private CORSConfiguration corsConfiguration;
    private List<Label> gatewayLabels;
    private List<APICategory> apiCategories;
    private boolean isMonetizationEnabled = false;
    private JSONObject monetizationProperties = new JSONObject();
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments;
    private Set<String> tags = new LinkedHashSet<String>();
    private String accessControlRoles;
    private String accessControl;

}

/**
 *   is needed? >
 *      artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context");
 *      artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true");  // this is  set only if api state is 'published'
 */
