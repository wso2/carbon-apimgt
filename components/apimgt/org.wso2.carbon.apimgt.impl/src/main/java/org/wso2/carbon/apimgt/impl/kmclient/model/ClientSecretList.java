package org.wso2.carbon.apimgt.impl.kmclient.model;

import java.util.List;

public class ClientSecretList {
    private int count;
    private List<ClientSecret> list;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ClientSecret> getList() {
        return list;
    }

    public void setList(List<ClientSecret> list) {
        this.list = list;
    }
}
