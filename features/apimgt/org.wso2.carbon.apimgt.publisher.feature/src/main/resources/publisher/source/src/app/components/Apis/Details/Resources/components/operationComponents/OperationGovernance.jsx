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

import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Box from '@material-ui/core/Box';
import Divider from '@material-ui/core/Divider';
import Typography from '@material-ui/core/Typography';
import Switch from '@material-ui/core/Switch';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import LaunchIcon from '@material-ui/icons/Launch';
import { Link } from 'react-router-dom';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
/**
 *
 * Renders the security , throttling policies and scopes selection section in the operation collapsed page
 * @export
 * @param {*} props
 * @returns
 */
export default function OperationGovernance(props) {
    const {
        operation, operationActionsDispatcher, operationRateLimits, api, disableUpdate,
    } = props;
    const isOperationRateLimiting = api.apiThrottlingPolicy === null;
    return (
        <Fragment>
            <Grid item md={12}>
                <Typography gutterBottom variant='subtitle1'>
                    Operation Governance
                    <Typography style={{ marginLeft: '10px' }} gutterBottom variant='caption'>
                        {'(Security, Rate Limiting & Scopes)'}
                    </Typography>
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item md={1} />
            <Grid item md={11}>
                <FormControl disabled={disableUpdate} component='fieldset'>
                    <FormControlLabel
                        control={
                            <Switch
                                checked={operation.authType && operation.authType.toLowerCase() !== 'none'}
                                onChange={({ target: { checked } }) =>
                                    operationActionsDispatcher({
                                        action: 'authType',
                                        event: { value: checked },
                                    })
                                }
                                size='small'
                                color='primary'
                            />
                        }
                        label='Security'
                        labelPlacement='start'
                    />
                </FormControl>
                <sup style={{ marginLeft: '10px' }}>
                    <Tooltip
                        fontSize='small'
                        title='If enabled, Users will need an access token with valid scopes to use the operation'
                        aria-label='Operation security'
                        placement='right-end'
                        interactive
                    >
                        <HelpOutline />
                    </Tooltip>
                </sup>
            </Grid>
            <Grid item md={1} />
            <Grid item md={3}>
                <TextField
                    id='operation_rate_limiting_policy'
                    select
                    fullWidth={!isOperationRateLimiting}
                    SelectProps={{
                        autoWidth: true,
                        IconComponent: isOperationRateLimiting ? ArrowDropDownIcon : 'span',
                    }}
                    disabled={disableUpdate || !isOperationRateLimiting}
                    label={
                        isOperationRateLimiting ? (
                            'Rate limiting policy'
                        ) : (
                            <div>
                                Rate limiting is governed by{' '}
                                <Box fontWeight='fontWeightBold' display='inline' color='primary.main'>
                                    API Level
                                </Box>
                            </div>
                        )
                    }
                    value={isOperationRateLimiting ? operation.throttlingPolicy : ''}
                    onChange={({ target: { value } }) =>
                        operationActionsDispatcher({
                            action: 'throttlingPolicy',
                            event: { value },
                        })
                    }
                    helperText={
                        isOperationRateLimiting ? (
                            'Select a rate limit policy for this operation'
                        ) : (
                            <span>
                                Use{' '}
                                <Box fontWeight='fontWeightBold' display='inline' color='primary.main'>
                                    Operation Level
                                </Box>{' '}
                                rate limiting to <b>enable</b> rate limiting per operation
                            </span>
                        )
                    }
                    margin='dense'
                    variant='outlined'
                >
                    {operationRateLimits.map(rateLimit => (
                        <MenuItem key={rateLimit.name} value={rateLimit.name}>
                            {rateLimit.displayName}
                        </MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid item md={8} />
            <Grid item md={1} />
            <Grid item md={11}>
                <TextField
                    id='operation_scope'
                    select
                    disabled={disableUpdate}
                    label='Operation scope'
                    value={operation.scopes[0]}
                    onChange={({ target: { value } }) =>
                        operationActionsDispatcher({
                            action: 'scopes',
                            event: { value: [value] },
                        })
                    }
                    helperText='Select a scope to control permissions to this operation'
                    margin='dense'
                    variant='outlined'
                >
                    {api.scopes.map(scope => (
                        <MenuItem key={scope.name} value={scope.name}>
                            {scope.name}
                        </MenuItem>
                    ))}
                </TextField>
                {!disableUpdate && (
                    <Link to={`/apis/${api.id}/scopes/create`} target='_blank'>
                        <Typography style={{ marginLeft: '10px' }} color='primary' display='inline' variant='caption'>
                            Create new scope
                            <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                        </Typography>
                    </Link>
                )}
            </Grid>
        </Fragment>
    );
}

OperationGovernance.propTypes = {
    disableUpdate: PropTypes.bool,
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
        spec: PropTypes.shape({}).isRequired,
    }).isRequired,
    operationActionsDispatcher: PropTypes.func.isRequired,
    operationRateLimits: PropTypes.arrayOf(PropTypes.shape({})),
    api: PropTypes.shape({ scopes: PropTypes.arrayOf(PropTypes.shape({})) }),
};

OperationGovernance.defaultProps = {
    operationRateLimits: [],
    api: { scopes: [] },
    disableUpdate: false,
};
