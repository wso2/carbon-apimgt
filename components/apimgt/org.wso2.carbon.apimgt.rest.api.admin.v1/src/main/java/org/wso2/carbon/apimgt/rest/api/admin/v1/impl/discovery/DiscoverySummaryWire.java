package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire format for the API Discovery Server's GET /summary response. Field
 * names mirror the server's snake_case JSON via {@link JsonProperty}.
 *
 * <p>Kept separate from the generated DTO so wire-format changes don't
 * leak into the public REST contract; {@code DiscoveryMappingUtil}
 * translates between the two.</p>
 *
 * @since 4.7.0
 */
public class DiscoverySummaryWire {

    @JsonProperty("total")
    private long total;

    @JsonProperty("managed")
    private long managed;

    @JsonProperty("unmanaged")
    private long unmanaged;

    @JsonProperty("skip_internal")
    private boolean skipInternal;

    @JsonProperty("by_type")
    private ByType byType;

    @JsonProperty("by_reachability")
    private ByReachability byReachability;

    @JsonProperty("by_service")
    private List<ByServiceEntry> byService;

    public long getTotal() { return total; }
    public void setTotal(final long total) { this.total = total; }

    public long getManaged() { return managed; }
    public void setManaged(final long managed) { this.managed = managed; }

    public long getUnmanaged() { return unmanaged; }
    public void setUnmanaged(final long unmanaged) { this.unmanaged = unmanaged; }

    public boolean isSkipInternal() { return skipInternal; }
    public void setSkipInternal(final boolean skipInternal) { this.skipInternal = skipInternal; }

    public ByType getByType() { return byType; }
    public void setByType(final ByType byType) { this.byType = byType; }

    public ByReachability getByReachability() { return byReachability; }
    public void setByReachability(final ByReachability byReachability) {
        this.byReachability = byReachability;
    }

    public List<ByServiceEntry> getByService() { return byService; }
    public void setByService(final List<ByServiceEntry> byService) {
        this.byService = byService;
    }

    /** Inner: shadow + drift counts. */
    public static class ByType {
        @JsonProperty("shadow") private long shadow;
        @JsonProperty("drift")  private long drift;

        public long getShadow() { return shadow; }
        public void setShadow(final long shadow) { this.shadow = shadow; }

        public long getDrift() { return drift; }
        public void setDrift(final long drift) { this.drift = drift; }
    }

    /** Inner: external + internal counts. */
    public static class ByReachability {
        @JsonProperty("external") private long external;
        @JsonProperty("internal") private long internal;

        public long getExternal() { return external; }
        public void setExternal(final long external) { this.external = external; }

        public long getInternal() { return internal; }
        public void setInternal(final long internal) { this.internal = internal; }
    }

    /** Inner: per-service breakdown row. */
    public static class ByServiceEntry {
        @JsonProperty("service_identity") private String serviceIdentity;
        @JsonProperty("fully_governed")   private boolean fullyGoverned;
        @JsonProperty("shadow")           private long shadow;
        @JsonProperty("drift")            private long drift;

        public String getServiceIdentity() { return serviceIdentity; }
        public void setServiceIdentity(final String serviceIdentity) {
            this.serviceIdentity = serviceIdentity;
        }

        public boolean isFullyGoverned() { return fullyGoverned; }
        public void setFullyGoverned(final boolean fullyGoverned) {
            this.fullyGoverned = fullyGoverned;
        }

        public long getShadow() { return shadow; }
        public void setShadow(final long shadow) { this.shadow = shadow; }

        public long getDrift() { return drift; }
        public void setDrift(final long drift) { this.drift = drift; }
    }
}
