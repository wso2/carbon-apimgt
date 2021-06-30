/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes from 'prop-types';

import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

const styles = (theme) => ({
    addNewWrapper: {
        backgroundColor: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
        border: 'solid 1px ' + theme.palette.grey['300'],
        borderRadius: theme.shape.borderRadius,
        marginTop: theme.spacing(2),
    },
    addNewHeader: {
        padding: theme.spacing(2),
        backgroundColor: theme.palette.grey['300'],
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
    addNewOther: {
        padding: theme.spacing(2),
    },
    button: {
        marginLeft: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.primary.main),
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    addJsonContent: {
        whiteSpace: 'pre',
    },
});

class AddPolicy extends Component {
    /**
     * AddPolicy
     */
    constructor() {
        super();
        this.state = {
            selectedPolicy: {
                uuid: '',
                name: 'Select',
                policy: '',
                type: '',
            },
            policies: [],
        };
    }

    /**
     * AddPolicy
     */
    componentDidMount() {
        const api = new API();
        const promisedPolicies = api.getThreatProtectionPolicies();
        promisedPolicies.then((response) => {
            this.setState({ policies: response.obj.list });
        });
        const promisedApi = api.get(this.props.id);
        promisedApi.then((response) => {
            this.setState({ currentApi: response.obj });
        });
    }

    /**
     * AddPolicy
     */
    formatPolicy = (policy) => {
        let formattedPolicy = policy;
        formattedPolicy = formattedPolicy.replace(':', ' : ');
        formattedPolicy = formattedPolicy.split(',').join(',\n');
        return formattedPolicy;
    }

    handleChange = () => (event) => {
        const policyId = event.target.value;
        const api = new API();
        const promisedPolicy = api.getThreatProtectionPolicy(policyId);
        promisedPolicy.then((response) => {
            this.setState({ selectedPolicy: response.obj });
        });
    }


    handlePolicyAdd() {
        const { intl } = this.props;
        const policy = this.state.selectedPolicy;
        if (policy.uuid === '' || policy.name === '') {
            Alert.error(intl.formatMessage({
                id: 'Apis.Details.Security.AddPolicy.select.policy',
                defaultMessage: 'Please select a policy',
            }));
            return;
        }

        if (this.state.currentApi) {
            const { currentApi } = this.state;
            const api = new API();
            const promisedPolicyAdd = api.addThreatProtectionPolicyToApi(currentApi.id, this.state.selectedPolicy.uuid);
            promisedPolicyAdd.then((response) => {
                if (response.status === 200) {
                    Alert.info(intl.formatMessage({
                        id: 'Apis.Details.Security.AddPolicy.threat.protection.policy.add.success',
                        defaultMessage: 'Threat protection policy added successfully.',
                    }));
                    this.props.updateData();
                } else {
                    Alert.error(intl.formatMessage({
                        id: 'Apis.Details.Security.AddPolicy.threat.protection.policy.add.failure',
                        defaultMessage: 'Failed to add threat protection policy.',
                    }));
                }
            });
        }
    }

    render() {
        const { classes } = this.props;
        return (
            <div className={classes.contentWrapper}>
                <div className={classes.addNewWrapper}>
                    <Typography className={classes.addNewHeader}>
                        <FormattedMessage
                            id='Apis.Details.Security.AddPolicy.add.threat.protection.policy'
                            defaultMessage='Add New Threat Protection Policy'
                        />
                    </Typography>
                    <Divider className={classes.divider} />
                    <div className={classes.addNewOther}>
                        <InputLabel htmlFor='selectedPolicy'>
                            <FormattedMessage
                                id='Apis.Details.Security.AddPolicy.policy'
                                defaultMessage='Policy'
                            />
                        </InputLabel>
                        &nbsp;&nbsp;
                        <Select
                            value={this.state.selectedPolicy.uuid}
                            onChange={this.handleChange('selectedPolicy')}
                            input={<Input name='selectedPolicy' id='selectedPolicy' />}
                        >
                            {this.state.policies.map((n) => {
                                return (
                                    <MenuItem key={n.uuid} value={n.uuid}>{n.name}</MenuItem>
                                );
                            })}
                            ;
                        </Select>
                        <br />
                        <br />
                        <p>
                            <FormattedMessage
                                id='Apis.Details.Security.AddPolicy.policy.type.label'
                                defaultMessage='Policy Type: '
                            />
                            {this.state.selectedPolicy.type}
                        </p>
                        <div>
                            <p>
                                <FormattedMessage
                                    id='Apis.Details.Security.AddPolicy.policy.label'
                                    defaultMessage='Policy: '
                                />
                            </p>
                            <div className={classes.addJsonContent}>
                                {this.formatPolicy(this.state.selectedPolicy.policy)}
                            </div>
                        </div>
                    </div>
                    <Divider className={classes.divider} />
                    <div className={classes.addNewOther}>
                        <Button variant='contained' color='primary' onClick={() => this.handlePolicyAdd()}>
                            <FormattedMessage
                                id='Apis.Details.Security.AddPolicy.add.policy.to.api'
                                defaultMessage='Add Policy to API'
                            />
                        </Button>
                        <Button className={classes.button} onClick={this.props.toggleShowAddPolicy}>
                            <FormattedMessage
                                id='Apis.Details.Security.AddPolicy.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </div>
                </div>
            </div>
        );
    }
}

AddPolicy.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    toggleShowAddPolicy: PropTypes.func.isRequired,
    id: PropTypes.string.isRequired,
    updateData: PropTypes.func.isRequired,
    intl: PropTypes.shape({}).isRequired,
};


export default injectIntl(withStyles(styles)(AddPolicy));
