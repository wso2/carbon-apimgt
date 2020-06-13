package org.wso2.carbon.apimgt.keymgt.model.entity;

import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.model.subscription.CacheableEntity;

import java.util.ArrayList;
import java.util.List;

public class ApiPolicy extends Policy {

    private List<APIPolicyConditionGroup> conditionGroups = new ArrayList<>();


    @Override
    public String getCacheKey() {

        return POLICY_TYPE.API + DELEM_PERIOD + super.getCacheKey();
    }


    public List<APIPolicyConditionGroup> getConditionGroups() {
        return conditionGroups;
    }


    public void setConditionGroups(List<APIPolicyConditionGroup> conditionGroups) {
        this.conditionGroups = conditionGroups;
    }
    
    
}
