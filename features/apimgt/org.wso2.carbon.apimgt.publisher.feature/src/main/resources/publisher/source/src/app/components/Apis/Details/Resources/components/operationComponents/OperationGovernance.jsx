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

import React from 'react';
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
import { getOperationScopes } from '../../operationUtils';

/**
 *
 * Renders the security , throttling policies and scopes selection section in the operation collapsed page
 * @export
 * @param {*} props
 * @returns
 */
export default function OperationGovernance(props) {
    const {
        operation, operationsDispatcher, operationRateLimits, api, disableUpdate, spec, target, verb,
    } = props;
    const isOperationRateLimiting = api.apiThrottlingPolicy === null;

    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography gutterBottom variant='subtitle1'>
                    Operation Governance
                    <Typography style={{ marginLeft: '10px' }} gutterBottom variant='caption'>
                        (Security, Rate Limiting & Scopes)
                    </Typography>
                    <Divider variant='middle' />
                </Typography>
            </Grid>
            <Grid item xs={1} />
            <Grid item xs={11}>
                <FormControl disabled={disableUpdate} component='fieldset'>
                    <FormControlLabel
                        control={(
                            <Switch
                                checked={operation['x-auth-type'] && operation['x-auth-type'].toLowerCase() !== 'none'}
                                onChange={({ target: { checked } }) => operationsDispatcher({
                                    action: 'authType',
                                    data: { target, verb, value: checked },
                                })}
                                size='small'
                                color='primary'
                            />
                        )}
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
            <Grid item md={5}>
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
                                Rate limiting is governed by
                                {' '}
                                <Box fontWeight='fontWeightBold' display='inline' color='primary.main'>
                                    API Level
                                </Box>
                            </div>
                        )
                    }
                    value={
                        isOperationRateLimiting && operation['x-throttling-tier'] ? operation['x-throttling-tier'] : ''
                    }
                    onChange={({ target: { value } }) => operationsDispatcher({
                        action: 'throttlingPolicy',
                        data: { target, verb, value },
                    })}
                    helperText={
                        isOperationRateLimiting ? (
                            'Select a rate limit policy for this operation'
                        ) : (
                            <span>
                                Use
                                {' '}
                                <Box fontWeight='fontWeightBold' display='inline' color='primary.main'>
                                    Operation Level
                                </Box>
                                {' '}
                                rate limiting to
                                {' '}
                                <b>enable</b>
                                {' '}
rate limiting per operation
                            </span>
                        )
                    }
                    margin='dense'
                    variant='outlined'
                >
                    {operationRateLimits.map((rateLimit) => (
                        <MenuItem key={rateLimit.name} value={rateLimit.name}>
                            {rateLimit.displayName}
                        </MenuItem>
                    ))}
                </TextField>
            </Grid>
            <Grid item md={6} />
            <Grid item md={1} />
            <Grid item md={5}>
                <TextField
                    id='operation_scope'
                    select
                    disabled={disableUpdate}
                    fullWidth
                    label='Operation scope'
                    value={getOperationScopes(operation, spec)[0]}
                    onChange={({ target: { value } }) => operationsDispatcher({
                        action: 'scopes',
                        data: { target, verb, value: [value] },
                    })}
                    helperText='Select a scope to control permissions to this operation'
                    margin='dense'
                    variant='outlined'
                >
                    <MenuItem
                        value=''
                        dense
                    >
                        None
                    </MenuItem>
                    {api.scopes.length !== 0
                        ? api.scopes.map((scope) => (
                            <MenuItem
                                key={scope.name}
                                value={scope.name}
                                dense
                            >
                                {scope.name}
                            </MenuItem>
                        )) : (
                            <Link to={`/apis/${api.id}/scopes/create`} target='_blank'>
                                <MenuItem
                                    key='Create New Scope'
                                    value='Create New Scope'
                                    dense
                                >
                                Create New Scope
                                </MenuItem>
                            </Link>
                        )}
                </TextField>
            </Grid>
            <Grid item md={5} style={{ marginTop: '14px' }}>
                {!disableUpdate && (
                    <Link to={`/apis/${api.id}/scopes/create`} target='_blank'>
                        <Typography style={{ marginLeft: '10px' }} color='primary' display='inline' variant='caption'>
                            Create new scope
                            <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                        </Typography>
                    </Link>
                )}
            </Grid>
            <Grid item md={1} />
        </>
    );
}

OperationGovernance.propTypes = {
    disableUpdate: PropTypes.bool,
    operation: PropTypes.shape({
        target: PropTypes.string.isRequired,
        verb: PropTypes.string.isRequired,
    }).isRequired,
    spec: PropTypes.shape({}).isRequired,
    operationsDispatcher: PropTypes.func.isRequired,
    operationRateLimits: PropTypes.arrayOf(PropTypes.shape({})),
    api: PropTypes.shape({ scopes: PropTypes.arrayOf(PropTypes.shape({})) }),
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
};

OperationGovernance.defaultProps = {
    operationRateLimits: [],
    api: { scopes: [] },
    disableUpdate: false,
};
