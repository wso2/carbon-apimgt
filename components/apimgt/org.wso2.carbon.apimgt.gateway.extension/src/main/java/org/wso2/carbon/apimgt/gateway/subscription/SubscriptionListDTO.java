package org.wso2.carbon.apimgt.gateway.subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for list of SubscriptionDTO
 */
public class SubscriptionListDTO {

    private List<SubscriptionDTO> list = new ArrayList<SubscriptionDTO>();

    public void addListItem(SubscriptionDTO listItem) {
        this.list.add(listItem);
    }

    public List<SubscriptionDTO> getSubscriptions() {
        return list;
    }

    public void setList(List<SubscriptionDTO> list) {
        this.list = list;
    }

}

