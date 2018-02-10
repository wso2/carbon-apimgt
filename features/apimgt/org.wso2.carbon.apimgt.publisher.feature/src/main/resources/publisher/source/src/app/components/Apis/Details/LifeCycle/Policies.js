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

import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Select from 'material-ui/Select';
import Button from 'material-ui/Button';

const Option = Select.Option;

export default class Policies extends Component {
    constructor() {
        super();
        this.state = {loading: false, selectedPolicies: []};
        this.handleChange = this.handleChange.bind(this);
        this.changeTiers = this.changeTiers.bind(this);
        this.api = new API();
    }

    handleChange(e) {
        this.setState({selectedPolicies: e.target.value});
        if (this.props.handlePolicies) {
            this.props.handlePolicies(e.target.value);
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
        const policies = this.props.policies;
        const {classes} = this.props;
        return (
            <div>
                <FormControl className="policies-select">
                    <InputLabel htmlFor="name-multiple">Business Plans</InputLabel>
                    <Select
                        margin="none"
                        multiple
                        value={this.state.selectedPolicies}
                        onChange={this.handleChange}
                        input={<Input id="name-multiple"/>}
                        MenuProps={{
                            PaperProps: {
                                style: {
                                    width: 200,
                                },
                            },
                        }}
                    >
                        {policies.map(policy => (
                            <MenuItem
                                key={policy.policyName}
                                value={policy.policyName}
                                style={{
                                    fontWeight: policies.indexOf(policy.policyName) !== -1 ? '500' : '400',
                                }}
                            >
                                {policy.displayName}
                            </MenuItem>
                        ))}
                    </Select>
                    <FormHelperText>Select a plan for the API and enable API level throttling.</FormHelperText>
                </FormControl>
            </div>
        );
    }
}