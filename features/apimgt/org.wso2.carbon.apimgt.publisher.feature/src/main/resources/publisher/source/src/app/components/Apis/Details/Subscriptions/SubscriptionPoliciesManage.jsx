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
import Typography from '@material-ui/core/Typography';
import FormControl from '@material-ui/core/FormControl';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Paper from '@material-ui/core/Paper';
import API from 'AppData/api';
import { isRestricted } from 'AppData/AuthManager';

const styles = (theme) => ({
    subscriptionPoliciesPaper: {
        marginTop: theme.spacing(2),
        padding: theme.spacing(2),
    },
    grid: {
        margin: theme.spacing(1.25),
    },
    gridLabel: {
        marginTop: theme.spacing(1.5),
    },
    mainTitle: {
        paddingLeft: 0,
    },
});

/**
 * Manage subscription policies of the API
 * */
class SubscriptionPoliciesManage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            subscriptionPolicies: {},
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        const { api } = this.props;
        const isAsyncAPI = (api.type === 'WS' || api.type === 'WEBSUB' || api.type === 'SSE');
        const policyPromise = isAsyncAPI ? API.asyncAPIPolicies() : API.policies('subscription');
        policyPromise
            .then((res) => {
                this.setState({ subscriptionPolicies: res.body.list });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
            });
    }

    /**
     * Handle onChange of selected subsription policies
     *
     * @param event onChange event
     */
    handleChange(event) {
        const { name, checked } = event.target;
        const { setPolices, policies } = this.props;
        let newSelectedPolicies = [...policies];
        if (checked) {
            newSelectedPolicies.push(name);
        } else {
            newSelectedPolicies = policies.filter((policy) => policy !== name);
        }
        setPolices(newSelectedPolicies);
    }

    render() {
        const { classes, api, policies } = this.props;
        const { subscriptionPolicies } = this.state;

        return (
            <>
                <Typography id='itest-api-details-bushiness-plans-head' variant='h4'>
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.business.plans'
                        defaultMessage='Business Plans'
                    />
                </Typography>
                {api.apiType === API.CONSTS.APIProduct
                    ? (
                        <Typography variant='caption' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.APIProduct.sub.heading'
                                defaultMessage='Attach business plans to API'
                            />
                        </Typography>
                    )
                    : (
                        <Typography variant='caption' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Subscriptions.SubscriptionPoliciesManage.sub.heading'
                                defaultMessage='Attach business plans to API'
                            />
                        </Typography>
                    )}
                <Paper className={classes.subscriptionPoliciesPaper}>
                    <FormControl className={classes.formControl}>
                        <FormGroup>
                            { subscriptionPolicies && Object.entries(subscriptionPolicies).map((value) => (
                                <FormControlLabel
                                    key={value[1].displayName}
                                    control={(
                                        <Checkbox
                                            disabled={isRestricted(['apim:api_publish', 'apim:api_create'], api)}
                                            color='primary'
                                            checked={policies.includes(value[1].displayName)}
                                            onChange={(e) => this.handleChange(e)}
                                            name={value[1].displayName}
                                        />
                                    )}
                                    label={value[1].displayName + ' : ' + value[1].description}
                                />
                            ))}
                        </FormGroup>
                    </FormControl>
                </Paper>
            </>
        );
    }
}

SubscriptionPoliciesManage.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({ formatMessage: PropTypes.func }).isRequired,
    api: PropTypes.shape({ policies: PropTypes.arrayOf(PropTypes.shape({})) }).isRequired,
    setPolices: PropTypes.func.isRequired,
    policies: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(SubscriptionPoliciesManage));
