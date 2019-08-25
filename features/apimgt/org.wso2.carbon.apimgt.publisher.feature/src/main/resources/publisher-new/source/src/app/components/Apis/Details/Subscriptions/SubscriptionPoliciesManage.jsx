/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';
import Checkbox from '@material-ui/core/Checkbox';
import FormLabel from '@material-ui/core/FormLabel';
import FormControl from '@material-ui/core/FormControl';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormHelperText from '@material-ui/core/FormHelperText';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';

const styles = theme => ({
    subscriptionPoliciesPaper: {
        marginTop: theme.spacing.unit * 2,
        paddingLeft: theme.spacing.unit * 2,
        paddingBottom: theme.spacing.unit * 2,
    },
    grid: {
        margin: theme.spacing.unit * 1.25,
    },
    gridLabel: {
        marginTop: theme.spacing.unit * 1.5,
    }
});

/**
 * Manage subscription policies of the API
 * */
class SubscriptionPoliciesManage extends Component {
    constructor(props) {
        super(props);
        this.api = props.api;
        this.state = {
            subscriptionPolicies: {},
            selectedSubscriptionPolicies: props.api.policies,
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        API.policies('subscription')
            .then((res) => {
                this.setState({ subscriptionPolicies: res.obj.list });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
            });
    }

    /**
     * Handle onChange of selected subsription policies
     *
     * @param event onChange event
     */
    handleChange(event) {
        const apiClient = new API();
        const { name, checked } = event.target;
        const { selectedSubscriptionPolicies } = this.state;
        const { intl } = this.props;
        let updatedSelectedPolicies = [...selectedSubscriptionPolicies];
        const updatedAPI = { ...this.api };

        if (checked) {
            updatedSelectedPolicies[updatedSelectedPolicies.length] = name;
        } else {
            updatedSelectedPolicies = updatedSelectedPolicies.filter(policy => policy !== name);
        }
        updatedAPI.policies = updatedSelectedPolicies;
        apiClient.update(updatedAPI.id, updatedAPI)
            .then((res) => {
                this.api = res._data;
                this.setState({ selectedSubscriptionPolicies: updatedSelectedPolicies });
                Alert.info(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionPoliciesManage.policy.update.success',
                    defaultMessage: 'API subscription policies updated successfully',
                }));
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionPoliciesManage.policy.update.error',
                    defaultMessage: 'Error occurred while updating subscription policies',
                }));
            });
    }

    render() {
        const { classes } = this.props;
        const { subscriptionPolicies, selectedSubscriptionPolicies } = this.state;

        return (
            <Paper className={classes.subscriptionPoliciesPaper}>
                <FormControl className={classes.formControl}>
                    <Grid container spacing={2} className={classes.grid}>
                        <Grid item xs={4} className={classes.gridLabel}>
                            <FormLabel>
                                <FormattedMessage
                                    id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.subscription.policies'
                                    defaultMessage='Subscription Policies'
                                /> { ' : '}
                            </FormLabel>
                        </Grid>
                        <Grid item xs={8}>
                            <FormGroup>
                                { subscriptionPolicies && Object.entries(subscriptionPolicies).map(([key, value]) => (
                                    <FormControlLabel
                                        control={<Checkbox
                                            checked={selectedSubscriptionPolicies.includes(value.name)}
                                            onChange={e => this.handleChange(e)}
                                            name={value.name}
                                        />}
                                        label={value.name + ' : ' + value.description}
                                    />
                                ))}
                            </FormGroup>
                            <FormHelperText>
                                <FormattedMessage
                                    id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.policies.update'
                                    defaultMessage='Add/remove API subscription policies'
                                />
                            </FormHelperText>
                        </Grid>
                    </Grid>

                </FormControl>
            </Paper>
        );
    }
}

SubscriptionPoliciesManage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    api: PropTypes.shape({ policies: PropTypes.array }).isRequired,
};

export default injectIntl(withStyles(styles)(SubscriptionPoliciesManage));
