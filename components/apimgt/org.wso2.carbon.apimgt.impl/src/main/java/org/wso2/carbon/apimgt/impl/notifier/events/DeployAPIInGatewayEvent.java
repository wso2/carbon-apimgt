package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeployAPIInGatewayEvent extends Event {

    private int apiId;
    private String uuid;
    private String name;
    private String version;
    private String provider;
    private String apiType;
    private Set<String> gatewayLabels;
    /**
     * Platform (API Platform / Envoy) gateway IDs to which this deploy/undeploy event applies.
     * When non-empty, PlatformGatewayDeployNotifier will dispatch to the platform path; when null or empty,
     * only Synapse (JMS) path is used.
     */
    private Set<String> platformGatewayIds;
    /**
     * Explicit platform gateway deployment IDs keyed by platform gateway ID.
     * This lets CP send the exact persisted deployment identity for each gateway instead of deriving it from the
     * event correlation ID.
     */
    private Map<String, String> platformGatewayDeploymentIds;
    private Set<APIEvent> associatedApis;
    private String context;
    private boolean deleted;

    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, String tenantDomain, int apiId,
                                   String uuid, Set<String> gatewayLabels, String name, String version, String provider,
                                   String apiType, String context, Set<APIEvent> associatedApis) {
        this.uuid = uuid;
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.apiId = apiId;
        this.tenantDomain = tenantDomain;
        this.name = name;
        this.version = version;
        this.provider = provider;
        this.gatewayLabels = gatewayLabels;
        this.name = name;
        this.context = context;
        this.provider = provider;
        this.apiType = apiType;
        this.associatedApis = associatedApis;
    }

    /**
     *
     * @param eventId
     * @param timestamp
     * @param type
     * @param tenantDomain
     * @param apiId
     * @param gatewayLabels
     * @param name
     * @param version
     * @param provider
     * @param apiType
     * @param context
     */
    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, String tenantDomain, int apiId,
                                   String uuid, Set<String> gatewayLabels, String name, String version, String provider,
                                   String apiType, String context) {
        this.uuid = uuid;
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.apiId = apiId;
        this.tenantDomain = tenantDomain;
        this.name = name;
        this.version = version;
        this.provider = provider;
        this.gatewayLabels = gatewayLabels;
        this.name = name;
        this.version = version;
        this.provider = provider;
        this.apiType = apiType;
        this.context = context;
        this.associatedApis = new HashSet<>();
    }

    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, String tenantDomain, int apiId,
                                   String uuid, Set<String> gatewayLabels, String name, String version, String provider,
                                   String apiType, String context,boolean deleted) {
        this(eventId,timestamp,type,tenantDomain,apiId,uuid,gatewayLabels,name,version,provider,apiType,context);
        this.deleted = deleted;
    }

    public Set<String> getGatewayLabels() {

        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {

        this.gatewayLabels = gatewayLabels;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }


    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public String getApiType() {

        return apiType;
    }

    public void setApiType(String apiType) {

        this.apiType = apiType;
    }

    public Set<APIEvent> getAssociatedApis() {

        return associatedApis;
    }

    public void setAssociatedApis(Set<APIEvent> associatedApis) {

        this.associatedApis = associatedApis;
    }
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Platform gateway IDs targeted by this event. When non-empty, the platform deploy notifier will dispatch.
     *
     * @return set of platform gateway IDs, or null if none
     */
    public Set<String> getPlatformGatewayIds() {
        return platformGatewayIds;
    }

    public void setPlatformGatewayIds(Set<String> platformGatewayIds) {
        this.platformGatewayIds = platformGatewayIds;
    }

    /**
     * Retrieves the mapping of platform gateway deployment IDs.
     *
     * @return a map where the key represents the platform gateway identifier and the value represents its corresponding
     * deployment ID. Returns null if no mapping exists.
     */
    public Map<String, String> getPlatformGatewayDeploymentIds() {
        return platformGatewayDeploymentIds;
    }

    /**
     * Sets the mapping of platform gateway deployment IDs. If the input map is null or empty,
     * the internal reference is set to null. Otherwise, a new map is created from the given data.
     *
     * @param platformGatewayDeploymentIds a map where the key is the platform gateway identifier
     *                                     and the value is its corresponding deployment ID
     */
    public void setPlatformGatewayDeploymentIds(Map<String, String> platformGatewayDeploymentIds) {
        if (platformGatewayDeploymentIds == null || platformGatewayDeploymentIds.isEmpty()) {
            this.platformGatewayDeploymentIds = null;
            return;
        }
        this.platformGatewayDeploymentIds = new HashMap<>(platformGatewayDeploymentIds);
    }
}
