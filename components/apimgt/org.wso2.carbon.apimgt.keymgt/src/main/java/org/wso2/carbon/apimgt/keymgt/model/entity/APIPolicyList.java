package org.wso2.carbon.apimgt.keymgt.model.entity;

import java.util.ArrayList;
import java.util.List;

public class APIPolicyList {

    private Integer count = null;
    private List<ApiPolicy> list = new ArrayList<>();

    public Integer getCount() {

        return count;
    }

    public void setCount(Integer count) {

        this.count = count;
    }

    public List<ApiPolicy> getList() {

        return list;
    }

    public void setList(List<ApiPolicy> list) {

        this.list = list;
    }

}
