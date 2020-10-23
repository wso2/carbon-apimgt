package org.wso2.carbon.apimgt.api.model.persistence;

/**
 * A subset of org.wso2.carbon.apimgt.api.model.persistence.PublisherAPI. Minimal API information required only for
 * listing APIs in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIInfo {
    String name;
    String version;
    String context;
    String provider;
    String status;
}
