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

import React, {Component} from 'react'

import {Select} from 'antd';
const Option = Select.Option;

export default class Policies extends Component {
    constructor() {
        super();
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(event) {
        debugger;
    }

    render() {
        const policies = [], currentPolicies = [];
        for (let policy of this.props.policies) {
            policies.push(<Option key={policy.policyName}>{policy.displayName}</Option>);
            policy.isDeployed && currentPolicies.push(policy.name);
        }
        /*const children = [];
         for (let i = 10; i < 36; i++) {
         children.push(<Option key={i.toString(36) + i}>{i.toString(36) + i}</Option>);
         }*/
        let props = {
            mode: "multiple",
            style: {width: '100%'},
            placeholder: "Please select",
            onChange: this.handleChange
        };
        return (
            <div className="col-xs-6 col-sm-6 col-md-6 col-lg-6">
                <Select {...props}>
                    {policies}
                </Select>
                <button id="update-tiers-button" type="button" data-resource-path="/apis/{apiId}"
                        data-resource-method="put" className="btn btn-primary">Update
                </button>
            </div>

        )
            ;
    }
}