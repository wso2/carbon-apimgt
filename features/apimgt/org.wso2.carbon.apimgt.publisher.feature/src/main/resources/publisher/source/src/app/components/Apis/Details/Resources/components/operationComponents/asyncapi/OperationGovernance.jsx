/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { useIntl, FormattedMessage } from 'react-intl';
import { getAsyncAPIOperationScopes } from '../../../operationUtils';

/**
 *
 * Renders the security and scopes selection section in the operation collapsed page
 * @export
 * @param {*} props
 * @returns
 */
export default function OperationGovernance(props) {
    const {
        operation, operationsDispatcher, api, disableUpdate, target, verb, sharedScopes,
    } = props;
    const operationScopes = getAsyncAPIOperationScopes(operation[verb]);
    const filteredApiScopes = api.scopes.filter((sharedScope) => !sharedScope.shared);
    const intl = useIntl();

    return (
        <>
            <Grid item xs={12} md={12}>
                <Typography gutterBottom variant='subtitle1'>
                    <FormattedMessage
                        id='Apis.Details.Topics.components.operationComponents.OperationGovernance.title'
                        defaultMessage='Operation Governance'
                    />
                    <Typography style={{ marginLeft: '10px' }} gutterBottom variant='caption'>
                        <FormattedMessage
                            id='Apis.Details.Topics.components.operationComponents.OperationGovernance.subTitle'
                            defaultMessage='(Security & Scopes)'
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
                                id={'Apis.Details.Topics.components.operationComponents.OperationGovernance.Security'
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
                {
                    operation['x-auth-type'] && operation['x-auth-type'].toLowerCase() !== 'none' ? (
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
                                id: 'Apis.Details.Topics.components.operationComponents.'
                                + 'OperationGovernance.operation.scope.label.default',
                                defaultMessage: 'Operation scope',
                            }) : intl.formatMessage({
                                id: 'Apis.Details.Topics.components.operationComponents.'
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
                                    id={'Apis.Details.Topics.components.operationComponents.'
                                    + 'OperationGovernance.operation.scope.helperText'}
                                    defaultMessage='Select a scope to control permissions to this operation'
                                />
                            )}
                            margin='dense'
                            variant='outlined'
                        >
                            <ListSubheader>
                                <FormattedMessage
                                    id={'Apis.Details.Topics.components.operationComponents.'
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
                                            id={'Apis.Details.Topics.components.operationComponents.'
                                        + 'OperationGovernance.operation.no.api.scope.available'}
                                            defaultMessage='No API scopes available'
                                        />
                                    </em>
                                </MenuItem>
                            )}
                            <ListSubheader>
                                <FormattedMessage
                                    id={'Apis.Details.Topics.components.operationComponents.'
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
                                    <Checkbox
                                        checked={operationScopes.includes(sharedScope.scope.name)}
                                        color='primary'
                                    />
                                    {sharedScope.scope.name}
                                </MenuItem>
                            )) : (
                                <MenuItem
                                    value=''
                                    disabled
                                >
                                    <em>
                                        <FormattedMessage
                                            id={'Apis.Details.Topics.components.operationComponents.'
                                        + 'OperationGovernance.operation.no.sharedpi.scope.available'}
                                            defaultMessage='No shared scopes available'
                                        />
                                    </em>
                                </MenuItem>
                            )}
                        </TextField>
                    ) : null
                }
            </Grid>
            <Grid item md={5} style={{ marginTop: '14px' }}>
                {
                    operation['x-auth-type'] && operation['x-auth-type'].toLowerCase() !== 'none' ? !disableUpdate && (
                        <Link
                            to={`/apis/${api.id}/scopes/create`}
                            target='_blank'
                        >
                            <Typography
                                style={{ marginLeft: '10px' }}
                                color='primary'
                                display='inline'
                                variant='caption'
                            >
                                <FormattedMessage
                                    id={'Apis.Details.Topics.components.operationComponents.'
                                    + 'OperationGovernance.operation.scope.create.new.scope'}
                                    defaultMessage='Create New Scope'
                                />
                                <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                            </Typography>
                        </Link>
                    ) : null
                }
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
    operationsDispatcher: PropTypes.func.isRequired,
    api: PropTypes.shape({ scopes: PropTypes.arrayOf(PropTypes.shape({})) }),
    target: PropTypes.string.isRequired,
    verb: PropTypes.string.isRequired,
    sharedScopes: PropTypes.arrayOf(PropTypes.shape({})),
};

OperationGovernance.defaultProps = {
    api: { scopes: [] },
    sharedScopes: [],
    disableUpdate: false,
};
