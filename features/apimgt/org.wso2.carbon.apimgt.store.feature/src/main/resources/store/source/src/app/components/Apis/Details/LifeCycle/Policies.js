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
import API from '../../../../data/api'

import {Select, Button, message} from 'antd';
const Option = Select.Option;

export default class Policies extends Component {
    constructor() {
        super();
        this.state = {loading: false, selectedPolicies: []};
        this.handleChange = this.handleChange.bind(this);
        this.changeTiers = this.changeTiers.bind(this);
        this.api = new API();
    }

    handleChange(data) {
        this.setState({selectedPolicies: data});
        if(this.props.handlePolicies){
            this.props.handlePolicies(data);
        }
    }
    changeTiers(event) {
        this.setState({loading: true});
        const api_uuid = this.props.api.id;
        let promisedApi = this.api.get(api_uuid);
        promisedApi.then(response => {
            let api_data = JSON.parse(response.data);
            api_data.policies = this.state.selectedPolicies;
            let promised_update = this.api.update(api_data);
            promised_update.then(response => {
                this.setState({loading: false});
                message.info("Lifecycle state updated successfully");
            })
        });
    }

    render() {
        let currentPolicies = this.props.api ? this.props.api.policies : this.props.selectedPolicies;
        const policies = this.props.policies.map(
            policy => <Option key={policy.policyName}>{policy.displayName}</Option>);
        const props = {
            mode: "multiple",
            style: {width: '100%'},
            placeholder: "Please select",
            onChange: this.handleChange,
            defaultValue: currentPolicies
        };
        return (

            <div>
                <Select {...props}>
                    {policies}
                </Select>
                {this.props.api && <Button style={{margin: "5px"}} type="primary" loading={this.state.loading}
                        onClick={this.changeTiers}>Update</Button> }
            </div>

        );
    }
}