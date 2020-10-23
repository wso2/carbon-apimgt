package org.wso2.carbon.apimgt.api.model.persistence;

/**
 * A subset of org.wso2.carbon.apimgt.api.model.persistence.DevPortalAPI. Minimal API information required only for
 * listing
 * APIs in DevPortal which are stored in the persistence layer are included in this.
 */
public class DevPortalAPIInfo {
    String name;
    String version;
    String context;
    String provider;
}
