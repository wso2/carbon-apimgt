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

import React, { Component } from 'react';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
import { FormattedMessage } from 'react-intl';
import { withStyles} from '@material-ui/core/styles';
import PropTypes from 'prop-types';

import API from 'AppData/api';

const styles = theme => ({
    FormControl: {
        padding: 0,
        width: '100%',
        marginTop: 0,
    },
})

/**
 *
 *
 * @export
 * @class Policies
 * @extends {Component}
 */
class Policies extends Component {
    constructor() {
        super();
        this.state = {
            loading: false,
            selectedPolicies: [],
        };
        this.handleChange = this.handleChange.bind(this);
        this.changeTiers = this.changeTiers.bind(this);
        this.api = new API();
    }

    handleChange(e) {
        this.setState({
            selectedPolicies: e.target.value,
        });
        if (this.props.handlePolicies) {
            this.props.handlePolicies(e.target.value);
        }
    }

    changeTiers(event) {
        this.setState({
            loading: true,
        });
        const api_uuid = this.props.api.id;
        const promisedApi = this.api.get(api_uuid);
        promisedApi.then((response) => {
            const api_data = JSON.parse(response.data);
            api_data.policies = this.state.selectedPolicies;
            const promised_update = this.api.update(api_data);
            promised_update.then((response) => {
                this.setState({
                    loading: false,
                });
                message.info('Lifecycle state updated successfully');
            });
        });
    }

    /**
     *
     *
     * @returns
     * @memberof Policies
     */
    render() {
        const { handleInputChange, api, policies } = this.props;
        const { classes } = this.props;
        return (
            <React.Fragment>
                <FormControl clsssName={classes.FormControl}>
                    <InputLabel htmlFor='name-multiple'>
                        <FormattedMessage id="business.plans" defaultMessage="Business Plans" />
                    </InputLabel>
                    <Select
                        error={api.policies && api.policies.length === 0}
                        fullWidth
                        margin='none'
                        multiple
                        name='policies'
                        value={api.policies || []}
                        onChange={handleInputChange}
                        input={<Input id='name-multiple' />}
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
                                key={policy.name}
                                value={policy.name}
                                style={{
                                    fontWeight: policies.indexOf(policy.name) !== -1 ? '500' : '400',
                                }}
                            >
                                {policy.name}
                            </MenuItem>
                        ))}
                    </Select>
                    <FormHelperText>
                        <FormattedMessage id="select.a.plan.for.the.api.and.enable.api.level.throttling"
                            defaultMessage="Select a plan for the API and enable API level throttling." />
                    </FormHelperText>
                </FormControl>
            </React.Fragment>
        );
    }
}
Policies.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({}).isRequired,
    handleInputChange: PropTypes.func.isRequired,
    policies: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(Policies);
