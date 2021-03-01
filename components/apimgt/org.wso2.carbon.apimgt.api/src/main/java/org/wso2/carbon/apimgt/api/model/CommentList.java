package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;

public class CommentList {
    private Integer count = null;
    private List<Comment> list = new ArrayList<Comment>();
    //private PaginationDTO pagination = null;
    private Integer offset = null;
    private Integer limit = null;
    private Integer total = null;
    private String next = null;
    private String previous = null;

    public Integer getCount(){return this.count; }

    public void setCount(Integer count){ this.count = count; }

    public List getList(){return this.list; }

    public void setList(List list){ this.list = list; }
}
