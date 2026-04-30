package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire format for the API Discovery Server's GET /apis paginated list
 * response.
 *
 * @since 4.7.0
 */
public class DiscoveryListWire {

    @JsonProperty("count")      private int count;
    @JsonProperty("list")       private List<DiscoveryListItemWire> list;
    @JsonProperty("pagination") private PaginationWire pagination;

    public int getCount() { return count; }
    public void setCount(final int count) { this.count = count; }

    public List<DiscoveryListItemWire> getList() { return list; }
    public void setList(final List<DiscoveryListItemWire> list) { this.list = list; }

    public PaginationWire getPagination() { return pagination; }
    public void setPagination(final PaginationWire pagination) {
        this.pagination = pagination;
    }

    /** Pagination block matching the daemon's response shape. */
    public static class PaginationWire {
        @JsonProperty("offset") private int offset;
        @JsonProperty("limit")  private int limit;
        @JsonProperty("total")  private int total;

        public int getOffset() { return offset; }
        public void setOffset(final int offset) { this.offset = offset; }

        public int getLimit() { return limit; }
        public void setLimit(final int limit) { this.limit = limit; }

        public int getTotal() { return total; }
        public void setTotal(final int total) { this.total = total; }
    }
}
