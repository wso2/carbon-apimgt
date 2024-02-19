package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;

public class NotificationList {

    private Integer count = null;
    private List<Notification> list = new ArrayList<Notification>();
    private Pagination pagination = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Notification> getList() {
        return list;
    }

    public void setList(List<Notification> list) {
        this.list = list;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

}
