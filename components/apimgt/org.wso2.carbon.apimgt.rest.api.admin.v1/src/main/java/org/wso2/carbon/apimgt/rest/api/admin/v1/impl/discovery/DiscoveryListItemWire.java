package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * One entry in the API Discovery Server's GET /apis list response. Used
 * by both {@link DiscoveryListWire} (the paginated list) and the unwrapped
 * single-row consumers in tests.
 *
 * @since 4.7.0
 */
public class DiscoveryListItemWire {

    @JsonProperty("id")                    private String id;
    @JsonProperty("service_identity")      private String serviceIdentity;
    @JsonProperty("env_kind")              private String envKind;
    @JsonProperty("method")                private String method;
    @JsonProperty("normalized_path")       private String normalizedPath;
    @JsonProperty("classification")        private String classification;
    @JsonProperty("is_internal")           private boolean isInternal;
    @JsonProperty("observation_count")     private long observationCount;
    @JsonProperty("distinct_client_count") private int distinctClientCount;
    @JsonProperty("last_seen_at")          private String lastSeenAt;
    @JsonProperty("matched_apim_api_ids")  private List<String> matchedApimApiIds;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }

    public String getServiceIdentity() { return serviceIdentity; }
    public void setServiceIdentity(final String serviceIdentity) {
        this.serviceIdentity = serviceIdentity;
    }

    public String getEnvKind() { return envKind; }
    public void setEnvKind(final String envKind) { this.envKind = envKind; }

    public String getMethod() { return method; }
    public void setMethod(final String method) { this.method = method; }

    public String getNormalizedPath() { return normalizedPath; }
    public void setNormalizedPath(final String normalizedPath) {
        this.normalizedPath = normalizedPath;
    }

    public String getClassification() { return classification; }
    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public boolean isInternal() { return isInternal; }
    public void setInternal(final boolean isInternal) {
        this.isInternal = isInternal;
    }

    public long getObservationCount() { return observationCount; }
    public void setObservationCount(final long observationCount) {
        this.observationCount = observationCount;
    }

    public int getDistinctClientCount() { return distinctClientCount; }
    public void setDistinctClientCount(final int distinctClientCount) {
        this.distinctClientCount = distinctClientCount;
    }

    public String getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(final String lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public List<String> getMatchedApimApiIds() { return matchedApimApiIds; }
    public void setMatchedApimApiIds(final List<String> matchedApimApiIds) {
        this.matchedApimApiIds = matchedApimApiIds;
    }
}
