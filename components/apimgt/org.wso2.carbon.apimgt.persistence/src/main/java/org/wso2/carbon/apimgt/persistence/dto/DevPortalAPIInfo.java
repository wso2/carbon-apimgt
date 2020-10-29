package org.wso2.carbon.apimgt.persistence.dto;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.DevPortalAPI. Minimal API information required only for
 * listing
 * APIs in DevPortal which are stored in the persistence layer are included in this.
 */
public class DevPortalAPIInfo {
    private String id;
    private String apiName;
    private String version;
    private String providerName;
    private String context;
    private String type;
    private String thumbnail;
    private String businessOwner;

    //monetizationCategory which is returned to UI as 'monetizationLabel' is not required. it is derived from the
    // attached tiers.
}
