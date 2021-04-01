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
import Button from '@material-ui/core/Button';
import Box from '@material-ui/core/Box';
import Checkbox from '@material-ui/core/Checkbox';
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
import ListSubheader from '@material-ui/core/ListSubheader';
import { Link } from 'react-router-dom';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
import ExpandLessIcon from '@material-ui/icons/ExpandLess';
import { useIntl, FormattedMessage } from 'react-intl';
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
        operation, operationsDispatcher, operationRateLimits, api, disableUpdate, spec, target, verb, sharedScopes,
        setFocusOperationLevel,
    } = props;
    const operationScopes = getOperationScopes(operation, spec);
    const isOperationRateLimiting = api.apiThrottlingPolicy === null;
    const filteredApiScopes = api.scopes.filter((sharedScope) => !sharedScope.shared);
    const intl = useIntl();
    const scrollToTop = () => {
        setFocusOperationLevel(true);
        document.querySelector('#react-root').scrollTop = 195;
    };
    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography gutterBottom variant='subtitle1'>
                    <FormattedMessage
                        id='Apis.Details.Resources.components.operationComponents.OperationGovernance.title'
                        defaultMessage='Operation Governance'
                    />
                    <Typography style={{ marginLeft: '10px' }} gutterBottom variant='caption'>
                        <FormattedMessage
                            id='Apis.Details.Resources.components.operationComponents.OperationGovernance.subTitle'
                            defaultMessage='(Security, Rate Limiting & Scopes)'
                        />
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
                        title={(
                            <FormattedMessage
                                id={'Apis.Details.Resources.components.operationComponents.OperationGovernance.Security'
                                + '.tooltip'}
                                defaultMessage='This will enable/disable Application Level securities defined in the
                                Runtime Configurations page.'
                            />
                        )}
                        fontSize='small'
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
                <Box display='flex' flexDirection='row' alignItems='flex-start'>
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
                            isOperationRateLimiting
                                ? intl.formatMessage({
                                    id: 'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.rate.limiting.policy',
                                    defaultMessage: 'Rate limiting policy',
                                })
                                : (
                                    <div>
                                        <FormattedMessage
                                            id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.governed.by'}
                                            defaultMessage='Rate limiting is governed by '
                                        />
                                        <Box
                                            fontWeight='fontWeightBold'
                                            display='inline'
                                            color='primary.main'
                                            cursor='pointer'
                                        >
                                            <FormattedMessage
                                                id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.API.level'}
                                                defaultMessage='API Level'
                                            />
                                        </Box>
                                    </div>
                                )
                        }
                        value={
                            isOperationRateLimiting
                            && operation['x-throttling-tier']
                                ? operation['x-throttling-tier']
                                : ''
                        }
                        onChange={({ target: { value } }) => operationsDispatcher({
                            action: 'throttlingPolicy',
                            data: { target, verb, value },
                        })}
                        helperText={
                            isOperationRateLimiting
                                ? intl.formatMessage({
                                    id: 'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.rate.limiting.policy.select',
                                    defaultMessage: 'Select a rate limit policy for this operation',
                                })
                                : (
                                    <span>
                                        <FormattedMessage
                                            id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.helperText.section1'}
                                            defaultMessage='Use '
                                        />
                                        <Box fontWeight='fontWeightBold' display='inline' color='primary.main'>
                                            <FormattedMessage
                                                id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.helperText.section2'}
                                                defaultMessage='Operation Level'
                                            />
                                        </Box>
                                        <FormattedMessage
                                            id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.helperText.section3'}
                                            defaultMessage=' rate limiting to '
                                        />
                                        <b>
                                            <FormattedMessage
                                                id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.helperText.section4'}
                                                defaultMessage='enable'
                                            />
                                        </b>
                                        <FormattedMessage
                                            id={'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.rate.limiting.helperText.section5'}
                                            defaultMessage=' rate limiting per operation'
                                        />
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
                    {!isOperationRateLimiting && (
                        <Button onClick={scrollToTop}>
                            <FormattedMessage
                                id={
                                    'Apis.Details.Resources.components.operationComponents.'
                                    + 'OperationGovernance.rate.limiting.button.text'
                                }
                                defaultMessage='Change rate limiting level'
                            />
                            <ExpandLessIcon />
                        </Button>
                    )}
                </Box>
            </Grid>
            <Grid item md={6} />
            <Grid item md={1} />
            <Grid item md={5}>
                { operation['x-auth-type'] && operation['x-auth-type'].toLowerCase() !== 'none' ? (
                    <TextField
                        id='operation_scope'
                        select
                        SelectProps={{
                            multiple: true,
                            renderValue: (selected) => (Array.isArray(selected) ? selected.join(', ') : selected),
                        }}
                        disabled={disableUpdate}
                        fullWidth
                        label={api.scopes.length !== 0 || sharedScopes ? intl.formatMessage({
                            id: 'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.operation.scope.label.default',
                            defaultMessage: 'Operation scope',
                        }) : intl.formatMessage({
                            id: 'Apis.Details.Resources.components.operationComponents.'
                            + 'OperationGovernance.operation.scope.label.notAvailable',
                            defaultMessage: 'No scope available',
                        })}
                        value={operationScopes}
                        onChange={({ target: { value } }) => operationsDispatcher({
                            action: 'scopes',
                            data: { target, verb, value: value ? [value] : [] },
                        })}
                        helperText={(
                            <FormattedMessage
                                id={'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.operation.scope.helperText'}
                                defaultMessage='Select a scope to control permissions to this operation'
                            />
                        )}
                        margin='dense'
                        variant='outlined'
                    >
                        <ListSubheader>
                            <FormattedMessage
                                id={'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.operation.scope.select.local'}
                                defaultMessage='API Scopes'
                            />
                        </ListSubheader>
                        {filteredApiScopes.length !== 0 ? filteredApiScopes.map((apiScope) => (
                            <MenuItem
                                key={apiScope.scope.name}
                                value={apiScope.scope.name}
                                dense
                            >
                                <Checkbox checked={operationScopes.includes(apiScope.scope.name)} color='primary' />
                                {apiScope.scope.name}
                            </MenuItem>
                        )) : (
                            <MenuItem
                                value=''
                                disabled
                            >
                                <em>
                                    <FormattedMessage
                                        id={'Apis.Details.Resources.components.operationComponents.'
                                    + 'OperationGovernance.operation.no.api.scope.available'}
                                        defaultMessage='No API scopes available'
                                    />
                                </em>
                            </MenuItem>
                        )}
                        <ListSubheader>
                            <FormattedMessage
                                id={'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.operation.scope.select.shared'}
                                defaultMessage='Shared Scopes'
                            />
                        </ListSubheader>
                        {sharedScopes && sharedScopes.length !== 0 ? sharedScopes.map((sharedScope) => (
                            <MenuItem
                                key={sharedScope.scope.name}
                                value={sharedScope.scope.name}
                                dense
                            >
                                <Checkbox checked={operationScopes.includes(sharedScope.scope.name)} color='primary' />
                                {sharedScope.scope.name}
                            </MenuItem>
                        )) : (
                            <MenuItem
                                value=''
                                disabled
                            >
                                <em>
                                    <FormattedMessage
                                        id={'Apis.Details.Resources.components.operationComponents.'
                                    + 'OperationGovernance.operation.no.sharedpi.scope.available'}
                                        defaultMessage='No shared scopes available'
                                    />
                                </em>
                            </MenuItem>
                        )}
                    </TextField>
                ) : null }
            </Grid>
            <Grid item md={5} style={{ marginTop: '14px' }}>
                { operation['x-auth-type'] && operation['x-auth-type'].toLowerCase() !== 'none' ? !disableUpdate && (
                    <Link to={`/apis/${api.id}/scopes/create`} target='_blank'>
                        <Typography style={{ marginLeft: '10px' }} color='primary' display='inline' variant='caption'>
                            <FormattedMessage
                                id={'Apis.Details.Resources.components.operationComponents.'
                                + 'OperationGovernance.operation.scope.create.new.scope'}
                                defaultMessage='Create New Scope'
                            />
                            <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                        </Typography>
                    </Link>
                ) : null}
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
    sharedScopes: PropTypes.arrayOf(PropTypes.shape({})),
};

OperationGovernance.defaultProps = {
    operationRateLimits: [],
    api: { scopes: [] },
    sharedScopes: [],
    disableUpdate: false,
};
