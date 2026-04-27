package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire format for the API Discovery Server's GET /untrafficked response.
 *
 * @since 4.7.0
 */
public class UntraffickedListWire {

    @JsonProperty("count") private int count;
    @JsonProperty("list")  private List<UntraffickedItemWire> list;

    public int getCount() { return count; }
    public void setCount(final int count) { this.count = count; }

    public List<UntraffickedItemWire> getList() { return list; }
    public void setList(final List<UntraffickedItemWire> list) { this.list = list; }

    /** One entry: a managed API with no observed traffic. */
    public static class UntraffickedItemWire {
        @JsonProperty("apim_api_id")      private String apimApiId;
        @JsonProperty("apim_api_name")    private String apimApiName;
        @JsonProperty("apim_api_version") private String apimApiVersion;
        @JsonProperty("method")           private String method;
        @JsonProperty("gateway_path")     private String gatewayPath;
        @JsonProperty("service_identity") private String serviceIdentity;
        @JsonProperty("last_synced_at")   private String lastSyncedAt;

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

        public String getMethod() { return method; }
        public void setMethod(final String method) { this.method = method; }

        public String getGatewayPath() { return gatewayPath; }
        public void setGatewayPath(final String gatewayPath) {
            this.gatewayPath = gatewayPath;
        }

        public String getServiceIdentity() { return serviceIdentity; }
        public void setServiceIdentity(final String serviceIdentity) {
            this.serviceIdentity = serviceIdentity;
        }

        public String getLastSyncedAt() { return lastSyncedAt; }
        public void setLastSyncedAt(final String lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
        }
    }
}
