package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import java.util.Set;


public class ThrottlingPoliciesData {




    public String getThrottlingPoliciesData(Set<String> tierNames)  {

        String throttlingPolicy = "";

       for (String tierName : tierNames) {
            throttlingPolicy += tierName;
        }

        return throttlingPolicy;
    }
}
