package org.wso2.carbon.apimgt.api.model;

public class Pagination {
    private Integer offset = null;
    private Integer limit = null;
    private Integer total = null;
    private String next = null;
    private String previous = null;

    public Integer getOffset() {return offset; }

    public void setOffset(Integer offset) {this.offset = offset; }

    public Integer getLimit() {return limit; }

    public void setLimit(Integer limit) {this.limit = limit; }

    public Integer getTotal() {return total; }

    public void setTotal(Integer total) {this.total = total; }

    public String getNext() {return next; }

    public void setNext(String next) {this.next = next; }

    public String getPrevious() {return previous; }

    public void setPrevious(String previous) {this.previous = previous; }

}
