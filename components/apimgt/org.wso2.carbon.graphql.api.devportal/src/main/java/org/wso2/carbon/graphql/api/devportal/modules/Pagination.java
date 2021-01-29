package org.wso2.carbon.graphql.api.devportal.modules;

public class Pagination {

    private int offset;
    private int limit;
    private int total;
    private String next;
    private String previous;

    public Pagination( int offset, int limit, int total, String next, String previous) {

        this.offset = offset;
        this.limit = limit;
        this.total = total;
        this.next = next;
        this.previous = previous;
    }



}
