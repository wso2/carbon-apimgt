/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Switch from '@material-ui/core/Switch';
import { makeStyles } from '@material-ui/core/styles';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    expansionPanel: {
        marginBottom: theme.spacing(3),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
        display: 'inline-flex',
    },
    iconSpace: {
        marginLeft: theme.spacing(0.5),
    },
    actionSpace: {
        margin: '-7px auto',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: 1.5,
    },
    keyManagerSelect: {
        minWidth: 180,
    },
}));

/**
 *
 * API Level Rate Limiting configuration
 * @param {*} props
 * @returns
 */
export default function APILevelRateLimitingPolicies(props) {
    const [apiFromContext] = useAPI();
    const classes = useStyles();
    const {
        configDispatcher,
        api: { apiThrottlingPolicy },
    } = props;
    const [apiRateLimits, setApiRateLimits] = useState([]);
    const handleChange = (event) => {
        configDispatcher({
            action: 'apiThrottlingPolicy',
            value: event.target.value,
        });
    };

    useEffect(() => {
        API.policies('api').then((response) => setApiRateLimits(response.body.list));
    }, []);

    return (
        <ExpansionPanel className={classes.expansionPanel} defaultExpanded>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                <Typography className={classes.subHeading} variant='h6'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.components.APILevelRateLimitingPolicies.configuration'
                        defaultMessage='Rate Limiting Configuration'
                    />
                    <Tooltip
                        title={(
                            <FormattedMessage
                                id='Apis.Details.APILevelRateLimitingPolicies.components.Configuration.tooltip'
                                defaultMessage={'Selected Rate Limiting Policy will be applied to all the'
                                + ' requests of this API.'}
                            />
                        )}
                        aria-label='Rate Limiting Policies'
                        placement='right-end'
                        interactive
                    >
                        <HelpOutline className={classes.iconSpace} />
                    </Tooltip>
                </Typography>
                <FormControlLabel
                    className={classes.actionSpace}
                    control={(
                        <Switch
                            disabled={isRestricted(['apim:api_create'], apiFromContext)}
                            checked={!(apiThrottlingPolicy === null)}
                            onChange={({ target: { checked } }) => configDispatcher({
                                action: 'throttlingPoliciesEnabled',
                                value: checked,
                            })}
                            color='primary'
                        />
                    )}
                />
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className={classes.expansionPanelDetails}>
                <Grid container spacing={1} alignItems='center'>
                    <Grid item md={6} xs={12}>
                        {!(apiThrottlingPolicy === null) && (
                            <TextField
                                disabled={isRestricted(['apim:api_create'], apiFromContext)}
                                id='operation_throttling_policy'
                                select
                                value={apiThrottlingPolicy}
                                onChange={handleChange}
                                label='Rate limiting policies'
                                margin='dense'
                                variant='outlined'
                                style={{ display: 'flex', minWidth: 180 }}
                            >
                                {apiRateLimits.map((rateLimit) => (
                                    <MenuItem key={rateLimit.name} value={rateLimit.name}>
                                        {rateLimit.displayName}
                                    </MenuItem>
                                ))}
                            </TextField>
                        )}
                    </Grid>
                </Grid>
            </ExpansionPanelDetails>
        </ExpansionPanel>
    );
}

APILevelRateLimitingPolicies.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
