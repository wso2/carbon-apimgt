/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
'use strict';

import React from 'react'
import {Switch, Route} from 'react-router-dom'


import BusinessPlan from './Details/BusinessPlan'
import APIPolicy from './Details/APIPolicy'
import ApplicationPolicy from './Details/ApplicationPolicy'
import CreateBusinessPlan from './Create/CreateBusinessPlan'
import CreateAPIPolicy from './Create/CreateAPIPolicy'
import CreateApplicationPolicy from './Create/CreateApplicationPolicy'
import BusinessPlans from './BusinessPlans'
import APIPolicies from './APIPolicies'
import ApplicationPolicies from './ApplicationPolicies'
import CustomRules from './CustomRules'
import CreateCustomRulePolicy from './Create/CreateCustomRulePolicy'
import CustomRule from './Details/CustomRule'
import BlackLists from './BlackLists'
import CreateBlackListPolicy from './Create/CreateBlackListPolicy'

import {PageNotFound} from '../Base/Errors'


const ThrottlingPolicies = () => {
    return (
        <Switch>
            <Route path={"/policies/business_plans/create"} component={CreateBusinessPlan}/>
            <Route path={"/policies/business_plans/:policy_uuid/"} component={BusinessPlan}/>
            <Route path={"/policies/business_plans"} render={props => (<BusinessPlans/>)}/>
            <Route path={"/policies/api_policies/create"} component={CreateAPIPolicy}/>
            <Route path={"/policies/api_policies/:policy_uuid/"} component={APIPolicy}/>
            <Route path={"/policies/api_policies"} render={props => (<APIPolicies/>)}/>
            <Route path={"/policies/application_policies/create"} component={CreateApplicationPolicy}/>
            <Route path={"/policies/application_policies/:policy_uuid/"} component={ApplicationPolicy}/>
            <Route path={"/policies/application_policies"} render={props => (<ApplicationPolicies/>)}/>
            <Route path={"/policies/custom_rules/create"} render={props => (<CreateCustomRulePolicy/>)}/>  
            <Route path={"/policies/custom_rules/:policy_uuid/"} component={CustomRule}/>          
            <Route path={"/policies/custom_rules"} render={props => (<CustomRules/>)}/>
            <Route path={"/policies/black_list_policies/create"} render={props => (<CreateBlackListPolicy/>)}/>
            <Route path={"/policies/black_list"} render={props => (<BlackLists/>)}/>
            <Route path={"/policies"} render={props => (<APIPolicies/>)}/>
            <Route component={PageNotFound}/>
        </Switch>
    );
};

export default ThrottlingPolicies
