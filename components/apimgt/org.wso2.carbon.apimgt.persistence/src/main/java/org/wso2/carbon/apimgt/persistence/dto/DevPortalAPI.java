package org.wso2.carbon.apimgt.persistence.dto;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
  Represents the API information stored in persistence layer, that is used for DevPortal operations
 */
public class DevPortalAPI extends DevPortalAPIInfo {
    private String status; // needs when decide whether to allow return api or not.
    private Boolean isDefaultVersion;
    private String description;
    private String wsdlUrl;
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private List<String> transport = new ArrayList<>();
    private String redirectURL;  // (originalStoreUrl)
    private String apiOwner;
    private boolean advertiseOnly;
    private String subscriptionAvailability; // need to decide isSubscriptionAvailable
    private String subscriptionAvailableOrgs; // (subscriptionAvailableTenants): need to decide the value of "isSubscriptionAvailable"
    private String authorizationHeader;
    private List<String> securityScheme = new ArrayList<>();
    private Set<String> availableTierNames;
    private Set<String> environments;
    private Set<String> gatewayLabels;
    private Set<String> apiCategories;
    private boolean isMonetizationEnabled; //(monetizationStatus)
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments; // returned in apiGet call as ingressURLs
    private List<String> tags = new ArrayList<>();
    private JSONObject additionalProperties;
    private String endpointConfig;
    private String type;
    private Boolean advertisedOnly;


    /*
    private String accessControl; //publisher accessControl : 'restricted', 'all' // this won't be required

     */

    /* private String apiDefinition; currently this is also returned in apiGet call. But this is not required. In
    store, when we go into an api, separate swagger get call is sent after the normal api get call. */

    /* private List<APIOperationsDTO> operations = new ArrayList<>(); NOT needed; this set for graphql apis only
     and that is resolved using uritemplates fetched from db */


    /* private Boolean isSubscriptionAvailable;  "is subscription available" for the current tenant is returned.
    this is resolved by "subscription Availability" (subscriptionAvailability: current_tenant)  property which store
    the tenants avaialbe for subscriptions
     This should not return all the other tenants available for subscrition in the store. So this property should not be
      be returned in api get response*/

    /* private Set<String> accessControlRoles; // dev portal doesn't need this */
}
