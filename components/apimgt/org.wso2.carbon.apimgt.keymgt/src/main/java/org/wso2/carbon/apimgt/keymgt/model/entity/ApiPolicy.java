package org.wso2.carbon.apimgt.keymgt.model.entity;

import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;

import java.util.ArrayList;
import java.util.List;

public class ApiPolicy extends Policy {

    private Integer id = null;
    private Integer tenantId = null;
    private String name = null;
    private String quotaType = null;
    private List<Object> conditionGroups = new ArrayList<>();

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getQuotaType() {

        return quotaType;
    }

    public void setQuotaType(String quotaType) {

        this.quotaType = quotaType;
    }

    @Override
    public String getCacheKey() {

        return POLICY_TYPE.API + DELEM_PERIOD + super.getCacheKey();
    }
}
