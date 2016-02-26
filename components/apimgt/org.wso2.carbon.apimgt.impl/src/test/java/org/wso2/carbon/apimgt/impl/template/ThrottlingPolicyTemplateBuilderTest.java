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

import org.wso2.carbon.apimgt.api.model.policy.Condition;
import org.wso2.carbon.apimgt.api.model.policy.HTTPVerbCondition;
import org.wso2.carbon.apimgt.api.model.policy.IPCondition;
import org.wso2.carbon.apimgt.api.model.policy.Pipeline;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.api.model.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.api.model.policy.RequestCountLimit;

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
        Policy policy = getPolicyAPILevelPerUser();
        String apiContext = "/weather";
        String apiName = "weather";
        String apiVersion = "1.0.0";
        System.out.println(templateBuilder.getThrottlePolicyForAPILevel(policy, apiName, apiVersion, apiContext));
    }
    
    public void  testGetThrottlePolicyForAPILevelPerAPI() throws Exception {
        Policy policy = getPolicyAPILevelPerAPI();
        String apiContext = "/weather";
        String apiName = "weather";
        String apiVersion = "1.0.0";
        System.out.println(templateBuilder.getThrottlePolicyForAPILevel(policy, apiName, apiVersion, apiContext));
    }
    
    public void  testGetThrottlePolicyForGlobalLevel() throws Exception {        
        Policy policy = getPolicyGlobalLevel();       
        System.out.println(templateBuilder.getThrottlePolicyForGlobalLevel(policy));   
        
    }
    public void  testGetThrottlePolicyForAppLevel() throws Exception {        
        Policy policy = getPolicyAppLevel();   
        System.out.println(templateBuilder.getThrottlePolicyForAppLevel(policy));   
        
    }
    public void  testGetThrottlePolicyForSubscriptionLevelperUser() throws Exception {        
        Policy policy = getPolicySubscriptionLevelperUser();
        System.out.println(templateBuilder.getThrottlePolicyForSubscriptionLevel(policy));   
        
    } 
    
    private Policy getPolicyAPILevelPerAPI(){
        Policy policy = new Policy("Gold");
        
        policy.setUserLevel(PolicyConstants.ACROSS_ALL);
        policy.setDescription("Description");    
        policy.setPolicyLevel("api");
       
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
    
    
    private Policy getPolicyAPILevelPerUser(){
        Policy policy = new Policy("Gold");
        
        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");    
        policy.setPolicyLevel("api");
       
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
    
    private Policy getPolicyAppLevel(){
        Policy policy = new Policy("gold");
        
        policy.setUserLevel(PolicyConstants.ACROSS_ALL); 
        policy.setDescription("Description");    
        policy.setPolicyLevel("app");
       
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
    
    private Policy getPolicyGlobalLevel(){
        Policy policy = new Policy("1");
        
        policy.setUserLevel(PolicyConstants.ACROSS_ALL);
        policy.setDescription("Description");    
        policy.setPolicyLevel("global");
       
        List<Pipeline> pipelines = new ArrayList<Pipeline>();
        Pipeline p = new Pipeline();

        List<Condition> condition;
       
       
        

        condition =  new ArrayList<Condition>();   
        IPCondition ipcond = new IPCondition();
        ipcond.setSpecificIP("192.168.1.2");
        condition.add(ipcond);
            
      
        p.setConditions(condition);
        pipelines.add(p);
        
        policy.setPipelines(pipelines);
      
        return policy;
    }
    
    private Policy getPolicySubscriptionLevelperUser(){
        Policy policy = new Policy("gold");
        
      
        policy.setDescription("Description");    
        policy.setPolicyLevel("sub");
       
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
