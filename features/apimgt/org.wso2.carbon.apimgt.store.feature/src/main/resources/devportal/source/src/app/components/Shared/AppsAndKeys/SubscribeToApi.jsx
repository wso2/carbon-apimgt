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

import React, { useState, useEffect } from 'react';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Grid from '@material-ui/core/Grid';
import ListItemText from '@material-ui/core/ListItemText';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Typography from '@material-ui/core/Typography';
import Select from '@material-ui/core/Select';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import FormHelperText from '@material-ui/core/FormHelperText';
import { FormattedMessage } from 'react-intl';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
        marginLeft: 20,
    },
    title: {
        display: 'inline-block',
        marginLeft: 20,
    },
    buttonsWrapper: {
        marginTop: 40,
    },
    legend: {
        marginBottom: 0,
        borderBottomStyle: 'none',
        marginTop: 20,
        fontSize: 12,
    },
    inputText: {
        marginTop: 20,
    },
    buttonRightLink: {
        textDecoration: 'none',
    },
    FormControl: {
        padding: theme.spacing.unit * 2,
        width: '100%',
    },
    FormControlOdd: {
        padding: theme.spacing.unit * 2,
        backgroundColor: theme.palette.background.paper,
        width: '100%',
    },
    quotaHelp: {
        position: 'relative',
    },
    subscribeRoot: {
        paddingLeft: theme.spacing.unit * 2,
    },
});

const subscribeToApi = (props) => {
    const [appSelected, setAppSelected] = useState('');
    const [policySelected, setPolicySelected] = useState('');
    const [applicationsList, setAapplicationsList] = useState([]);
    const {
        classes, throttlingPolicyList, applicationsAvailable, subscriptionRequest, updateSubscriptionRequest,
    } = props;

    useEffect(() => {
        if (throttlingPolicyList && throttlingPolicyList[0]) {
            setPolicySelected(throttlingPolicyList[0].tierName);
        }
    }, [throttlingPolicyList]);

    useEffect(() => {
        if (applicationsAvailable && applicationsAvailable[0]) {
            setAapplicationsList(applicationsAvailable);
            setAppSelected(applicationsAvailable[0].value);
            const newRequest = { ...subscriptionRequest };
            newRequest.applicationId = applicationsAvailable[0].value;
            updateSubscriptionRequest(newRequest);
        }
    }, [applicationsAvailable]);

    /**
    * This method is used to handle the updating of subscription
    * request object and selected fields.
    * @param {*} field field that should be updated in subscription request
    * @param {*} event event fired
    */
    const handleChange = (field, event) => {
        const newRequest = { ...subscriptionRequest };
        const { target } = event;
        switch (field) {
            case 'application':
                newRequest.applicationId = target.value;
                setAppSelected(target.value);
                break;
            case 'throttlingPolicy':
                newRequest.throttlingPolicy = target.value;
                setPolicySelected(target.value);
                break;
            default:
                break;
        }
        updateSubscriptionRequest(newRequest);
    };

    return (
        <Grid container spacing={3} className={classes.subscribeRoot}>
            <Grid item xs={12} md={6}>
                {appSelected && (
                    <FormControl className={classes.FormControl}>
                        <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.SubscribeToApi.application'
                                defaultMessage='Application'
                            />
                        </InputLabel>
                        <Select
                            value={appSelected}
                            onChange={e => handleChange('application', e)}
                            input={<Input name='appSelected' id='app-label-placeholder' />}
                            displayEmpty
                            name='appSelected'
                            className={classes.selectEmpty}
                        >
                            {applicationsList.map(app => (
                                <MenuItem value={app.value} key={app.value}>
                                    {app.label}
                                </MenuItem>
                            ))}
                        </Select>
                        <FormHelperText>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.SubscribeToApi.select.an.application.to.subscribe'
                                defaultMessage='Select an Application to subscribe'
                            />
                        </FormHelperText>
                    </FormControl>
                )}
                {throttlingPolicyList && (
                    <FormControl className={classes.FormControlOdd}>
                        <InputLabel shrink htmlFor='policy-label-placeholder' className={classes.quotaHelp}>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.SubscribeToApi.throttling.policy'
                                defaultMessage='Throttling Policy'
                            />
                        </InputLabel>
                        <Select
                            value={policySelected}
                            onChange={e => handleChange('throttlingPolicy', e)}
                            input={<Input name='policySelected' id='policy-label-placeholder' />}
                            displayEmpty
                            name='policySelected'
                            className={classes.selectEmpty}
                        >
                            {throttlingPolicyList.map(policy => (
                                <MenuItem value={policy.tierName} key={policy.tierName}>
                                    {(policy.tierPlan === 'COMMERCIAL') ? (
                                        <React.Fragment>
                                            <ListItemText
                                                primary={policy.tierName}
                                                secondary={
                                                    policy.monetizationAttributes.pricePerRequest ? (
                                                        <Typography>
                                                            {policy.monetizationAttributes.pricePerRequest}
                                                            {' '}
                                                            {' '}
                                                            {policy.monetizationAttributes.currencyType}
                                                            {' '}
                                                            {' per '}
                                                            {policy.monetizationAttributes.billingCycle}
                                                        </Typography>
                                                    ) : (
                                                        <Typography>
                                                            {policy.monetizationAttributes.fixedPrice}
                                                            {' '}
                                                            {' '}
                                                            {policy.monetizationAttributes.currencyType}
                                                            {' per '}
                                                            {policy.monetizationAttributes.billingCycle}
                                                        </Typography>
                                                    )}
                                            />
                                        </React.Fragment>
                                    ) : (
                                        <ListItemText
                                            primary={policy.tierName}
                                        />
                                    )}
                                </MenuItem>
                            ))}
                        </Select>
                        <FormHelperText>
                            <FormattedMessage
                                id='Shared.AppsAndKeys.SubscribeToApi.available.policies'
                                defaultMessage='Available Policies -'
                            />
                            {' '}
                            {throttlingPolicyList.map((policy, index) => (
                                <span key={policy.tierName}>
                                    {policy.tierName}
                                    {index !== throttlingPolicyList.length - 1 && <span>,</span>}
                                </span>
                            ))}
                        </FormHelperText>
                    </FormControl>
                )}
            </Grid>
        </Grid>
    );
};
subscribeToApi.propTypes = {
    classes: PropTypes.shape({
        FormControl: PropTypes.shape({}),
        quotaHelp: PropTypes.shape({}),
        selectEmpty: PropTypes.shape({}),
        FormControlOdd: PropTypes.shape({}),
        subscribeRoot: PropTypes.shape({}),
    }).isRequired,
    applicationsAvailable: PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string,
        label: PropTypes.string,
    })).isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
    subscriptionRequest: PropTypes.shape({}).isRequired,
    updateSubscriptionRequest: PropTypes.func.isRequired,
    rootClass: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(subscribeToApi);
