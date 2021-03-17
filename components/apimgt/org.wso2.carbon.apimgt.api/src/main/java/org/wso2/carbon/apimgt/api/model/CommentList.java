package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;

public class CommentList {

    private Integer count = null;
    private List<Comment> list = new ArrayList<Comment>();
    private Pagination pagination = null;

    public Integer getCount() {return count; }

    public void setCount(Integer count) {this.count = count; }

    public List<Comment> getList() {return list; }

    public void setList(List<Comment> list) {this.list = list; }

    public Pagination getPagination() {return pagination; }

    public void setPagination(Pagination pagination) {this.pagination = pagination; }
}
