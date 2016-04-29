/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.impl.template;

import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

public class ThrottlingPolicyTemplateBuilderTest extends TestCase {
    
    private final String POLICY_LOCATION = "src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "repository" + File.separator + "resources" + File.separator + "policy_templates"
            + File.separator + "";
    private ThrottlePolicyTemplateBuilder templateBuilder;
    
    @Override
    protected void setUp() throws Exception {
        templateBuilder = new ThrottlePolicyTemplateBuilder();
        //set the policy file location manually for testting
        templateBuilder.setPolicyTemplateLocation(POLICY_LOCATION);
    }

    public void testGetThrottlePolicyForAPILevelPerUser() throws Exception {
        APIPolicy policy = getPolicyAPILevelPerUser();
        System.out.println(templateBuilder.getThrottlePolicyForAPILevel(policy));
    }
    
    public void  testGetThrottlePolicyForAPILevelPerAPI() throws Exception {
        APIPolicy policy = getPolicyAPILevelPerAPI();
        System.out.println(templateBuilder.getThrottlePolicyForAPILevel(policy));
    }
    
    public void  testGetThrottlePolicyForGlobalLevel() throws Exception {        
        GlobalPolicy policy = getPolicyGlobalLevel();
        System.out.println(templateBuilder.getThrottlePolicyForGlobalLevel(policy));   
        
    }
    public void  testGetThrottlePolicyForAppLevel() throws Exception {        
        ApplicationPolicy policy = getPolicyAppLevel();
        System.out.println(templateBuilder.getThrottlePolicyForAppLevel(policy));   
        
    }
    public void  testGetThrottlePolicyForSubscriptionLevelperUser() throws Exception {        
        SubscriptionPolicy policy = getPolicySubscriptionLevelperUser();
        System.out.println(templateBuilder.getThrottlePolicyForSubscriptionLevel(policy));   
        
    } 
    
    private APIPolicy getPolicyAPILevelPerAPI(){
        APIPolicy policy = new APIPolicy("custom1");
        
        policy.setUserLevel(PolicyConstants.ACROSS_ALL);
        policy.setDescription("Description");    
        policy.setTenantDomain("carbon.super");
       // policy.setPolicyLevel("api");
       
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(1400);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        
        
        List<Pipeline> pipelines;
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        RequestCountLimit countlimit;     
        Condition cond;
        pipelines = new ArrayList<Pipeline>();
        
       
        ///////////pipeline item start//////
        p = new Pipeline();
        
        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("RequestCount");     
        countlimit = new RequestCountLimit();
        countlimit.setTimeUnit("min");
        countlimit.setUnitTime(5);
        countlimit.setRequestCount(1000);
        quotaPolicy.setLimit(countlimit);   

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");   
        condition.add(verbCond);
            
        
        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item end//////
       
        
        ///////////pipeline item start//////
        p = new Pipeline();
        
        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("RequestCount");     
        countlimit = new RequestCountLimit();
        countlimit.setTimeUnit("min");
        countlimit.setUnitTime(5);
        countlimit.setRequestCount(4000);
        quotaPolicy.setLimit(countlimit);   

        condition =  new ArrayList<Condition>();
        verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("GET");   
        condition.add(verbCond);
            
        
        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item end//////
        

        policy.setPipelines(pipelines);
        
        return policy;
    }
    
    
    private APIPolicy getPolicyAPILevelPerUser(){
        APIPolicy policy = new APIPolicy("custom1");
        
        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");    
        //policy.setPolicyLevel("api");
        policy.setTenantDomain("carbon.super");

        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(400);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        
        
        List<Pipeline> pipelines;
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        RequestCountLimit countlimit;     
        Condition cond;
        pipelines = new ArrayList<Pipeline>();
        
       
        ///////////pipeline item start//////
        p = new Pipeline();
        
        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("RequestCount");     
        countlimit = new RequestCountLimit();
        countlimit.setTimeUnit("min");
        countlimit.setUnitTime(5);
        countlimit.setRequestCount(100);
        quotaPolicy.setLimit(countlimit);   

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");   
        condition.add(verbCond);
            
        
        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item end//////    
      
        
        
        policy.setPipelines(pipelines);
        
        return policy;
    }
    
    private ApplicationPolicy getPolicyAppLevel(){
        ApplicationPolicy policy = new ApplicationPolicy("gold");
        
       // policy.setUserLevel(PolicyConstants.ACROSS_ALL); 
        policy.setDescription("Description");    
       // policy.setPolicyLevel("app");
        policy.setTenantDomain("carbon.super");
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(1000);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);    
        
        return policy;
    }
    
    private GlobalPolicy getPolicyGlobalLevel(){
        GlobalPolicy policy = new GlobalPolicy("1");        

        policy.setDescription("Description");    
        String siddhiQuery = 
                "FROM RequestStream\n"
                + "SELECT 'global_1' AS rule, messageID, true AS isEligible, (cast(map:get(propertiesMap,’ip’),’string’) == 3232235778) as isLocallyThrottled,"
                + " 'global_1_key' AS throttle_key\n"
                + "INSERT INTO EligibilityStream;";
        policy.setSiddhiQuery(siddhiQuery); 
      
        return policy;
    }
    
    private SubscriptionPolicy getPolicySubscriptionLevelperUser(){
        SubscriptionPolicy policy = new SubscriptionPolicy("gold");
        
      
        policy.setDescription("Description");    
        //policy.setPolicyLevel("sub");
       
        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setRequestCount(200);
      
        
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType("RequestCount");
        
        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);    
      
        return policy;
    }
    

}
