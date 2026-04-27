package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire format for the API Discovery Server's GET /apis/{id} response.
 * Carries every field the Detail page renders.
 *
 * @since 4.7.0
 */
public class DiscoveryDetailWire {

    @JsonProperty("id")                      private String id;
    @JsonProperty("service_identity")        private String serviceIdentity;
    @JsonProperty("env_kind")                private String envKind;
    @JsonProperty("namespace")               private String namespace;
    @JsonProperty("service_name")            private String serviceName;
    @JsonProperty("sample_pod")              private String samplePod;
    @JsonProperty("sample_workload")         private String sampleWorkload;
    @JsonProperty("method")                  private String method;
    @JsonProperty("normalized_path")         private String normalizedPath;
    @JsonProperty("raw_path_samples")        private List<String> rawPathSamples;
    @JsonProperty("classification")          private String classification;
    @JsonProperty("is_internal")             private boolean isInternal;
    @JsonProperty("first_seen_at")           private String firstSeenAt;
    @JsonProperty("last_seen_at")            private String lastSeenAt;
    @JsonProperty("observation_count")       private long observationCount;
    @JsonProperty("distinct_client_count")   private int distinctClientCount;
    @JsonProperty("distinct_clients_sample") private List<String> distinctClientsSample;
    @JsonProperty("status_codes")            private List<Integer> statusCodes;
    @JsonProperty("avg_duration_us")         private double avgDurationUs;
    @JsonProperty("matched_apim_api_ids")    private List<String> matchedApimApiIds;
    @JsonProperty("matched_apim_apis")       private List<APIRef> matchedApimAPIs;
    @JsonProperty("service_managed_apis")    private List<APIRef> serviceManagedAPIs;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }

    public String getServiceIdentity() { return serviceIdentity; }
    public void setServiceIdentity(final String s) { this.serviceIdentity = s; }

    public String getEnvKind() { return envKind; }
    public void setEnvKind(final String envKind) { this.envKind = envKind; }

    public String getNamespace() { return namespace; }
    public void setNamespace(final String namespace) { this.namespace = namespace; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSamplePod() { return samplePod; }
    public void setSamplePod(final String samplePod) { this.samplePod = samplePod; }

    public String getSampleWorkload() { return sampleWorkload; }
    public void setSampleWorkload(final String sampleWorkload) {
        this.sampleWorkload = sampleWorkload;
    }

    public String getMethod() { return method; }
    public void setMethod(final String method) { this.method = method; }

    public String getNormalizedPath() { return normalizedPath; }
    public void setNormalizedPath(final String normalizedPath) {
        this.normalizedPath = normalizedPath;
    }

    public List<String> getRawPathSamples() { return rawPathSamples; }
    public void setRawPathSamples(final List<String> rawPathSamples) {
        this.rawPathSamples = rawPathSamples;
    }

    public String getClassification() { return classification; }
    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public boolean isInternal() { return isInternal; }
    public void setInternal(final boolean isInternal) {
        this.isInternal = isInternal;
    }

    public String getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(final String firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public String getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(final String lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public long getObservationCount() { return observationCount; }
    public void setObservationCount(final long observationCount) {
        this.observationCount = observationCount;
    }

    public int getDistinctClientCount() { return distinctClientCount; }
    public void setDistinctClientCount(final int distinctClientCount) {
        this.distinctClientCount = distinctClientCount;
    }

    public List<String> getDistinctClientsSample() { return distinctClientsSample; }
    public void setDistinctClientsSample(final List<String> distinctClientsSample) {
        this.distinctClientsSample = distinctClientsSample;
    }

    public List<Integer> getStatusCodes() { return statusCodes; }
    public void setStatusCodes(final List<Integer> statusCodes) {
        this.statusCodes = statusCodes;
    }

    public double getAvgDurationUs() { return avgDurationUs; }
    public void setAvgDurationUs(final double avgDurationUs) {
        this.avgDurationUs = avgDurationUs;
    }

    public List<String> getMatchedApimApiIds() { return matchedApimApiIds; }
    public void setMatchedApimApiIds(final List<String> matchedApimApiIds) {
        this.matchedApimApiIds = matchedApimApiIds;
    }

    public List<APIRef> getMatchedApimAPIs() { return matchedApimAPIs; }
    public void setMatchedApimAPIs(final List<APIRef> matchedApimAPIs) {
        this.matchedApimAPIs = matchedApimAPIs;
    }

    public List<APIRef> getServiceManagedAPIs() { return serviceManagedAPIs; }
    public void setServiceManagedAPIs(final List<APIRef> serviceManagedAPIs) {
        this.serviceManagedAPIs = serviceManagedAPIs;
    }

    /** Trimmed APIM-API reference embedded in the detail. */
    public static class APIRef {
        @JsonProperty("apim_api_id")      private String apimApiId;
        @JsonProperty("apim_api_name")    private String apimApiName;
        @JsonProperty("apim_api_version") private String apimApiVersion;

        public String getApimApiId() { return apimApiId; }
        public void setApimApiId(final String apimApiId) {
            this.apimApiId = apimApiId;
        }

        public String getApimApiName() { return apimApiName; }
        public void setApimApiName(final String apimApiName) {
            this.apimApiName = apimApiName;
        }

        public String getApimApiVersion() { return apimApiVersion; }
        public void setApimApiVersion(final String apimApiVersion) {
            this.apimApiVersion = apimApiVersion;
        }
    }
}
