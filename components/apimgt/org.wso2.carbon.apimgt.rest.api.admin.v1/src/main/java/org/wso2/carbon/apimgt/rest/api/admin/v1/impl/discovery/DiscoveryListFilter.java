package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

/**
 * Filter parameters for {@link DiscoveryApiServerClient#listApis}. Mirrors
 * the daemon's GET /apis query-string parameters.
 *
 * @since 4.7.0
 */
public final class DiscoveryListFilter {

    private String classification;
    private String service;
    private String internal;
    private int limit;
    private int offset;

    public DiscoveryListFilter classification(final String classification) {
        this.classification = classification;
        return this;
    }

    public DiscoveryListFilter service(final String service) {
        this.service = service;
        return this;
    }

    public DiscoveryListFilter internal(final String internal) {
        this.internal = internal;
        return this;
    }

    public DiscoveryListFilter pagination(final int limit, final int offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    public String getClassification() { return classification; }
    public String getService() { return service; }
    public String getInternal() { return internal; }
    public int getLimit() { return limit; }
    public int getOffset() { return offset; }
}
