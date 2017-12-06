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

import JSONThreatProtectionPolicies from './JSONThreatProtectionPolicies'
import EditJSONThreatProtectionPolicy from './Details/EditJSONThreatProtectionPolicy'
import CreateJSONThreatProtectionPolicy from './Details/CreateJSONThreatProtectionPolicy'

import XMLThreatProtectionPolicies from './XMLThreatProtectionPolicies'
import EditXMLThreatProtectionPolicy from './Details/EditXMLThreatProtectionPolicy'
import CreateXMLThreatProtectionPolicy from './Details/CreateXMLThreatProtectionPolicy'

import {PageNotFound} from '../Base/Errors'

const Security = () => {
    return (
        <Switch>
            <Route path={"/security/json_threat_protection/create"} component={CreateJSONThreatProtectionPolicy}/>
            <Route path={"/security/json_threat_protection/:policy_uuid/"} component={EditJSONThreatProtectionPolicy}/>
            <Route path={"/security/json_threat_protection"} component={JSONThreatProtectionPolicies} />
            <Route path={"/security/xml_threat_protection/create"} component={CreateXMLThreatProtectionPolicy} />
            <Route path={"/security/xml_threat_protection/:policy_uuid/"} component={EditXMLThreatProtectionPolicy} />
            <Route path={"/security/xml_threat_protection"} component={XMLThreatProtectionPolicies} />
            <Route component={PageNotFound}/>
        </Switch>
    );
};

export default Security