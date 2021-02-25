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
import { makeStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ChipInput from 'material-ui-chip-input';
import APIValidation from 'AppData/APIValidation';
import base64url from 'base64url';
import Error from '@material-ui/icons/Error';
import InputAdornment from '@material-ui/core/InputAdornment';
import Chip from '@material-ui/core/Chip';
import { red } from '@material-ui/core/colors/';
import Alert from 'AppComponents/Shared/Alert';
import { isRestricted } from 'AppData/AuthManager';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import CONSTS from 'AppData/Constants';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    tooltip: {
        position: 'absolute',
        right: theme.spacing(-4),
        top: theme.spacing(1),
    },
}));

/**
 *
 * api.accessControl possible values are `NONE` and `RESTRICTED`
 * @export
 * @param {*} props
 * @returns
 */
export default function StoreVisibility(props) {
    const [roleValidity, setRoleValidity] = useState(true);
    const [roleExists, setRoleExists] = useState(true);
    const { api, configDispatcher } = props;
    const [invalidRoles, setInvalidRoles] = useState([]);
    const isRestrictedByRoles = api.visibility === 'RESTRICTED';
    const [apiFromContext] = useAPI();
    const classes = useStyles();
    const restApi = new API();
    const [tenants, setTenants] = useState([]);
    useEffect(() => {
        restApi.getTenantsByState(CONSTS.TENANT_STATE_ACTIVE)
            .then((result) => {
                setTenants(result.body.count);
            });
    }, []);

    useEffect(() => {
        if (invalidRoles.length === 0) {
            setRoleValidity(true);
        }
        if (api.visibility === 'RESTRICTED' && api.visibleRoles.length !== 0) {
            setRoleExists(true);
        }
    }, [invalidRoles]);
    const handleRoleAddition = (role) => {
        const promise = APIValidation.role.validate(base64url.encode(role));
        promise.then(() => {
            setRoleExists(true);
            setRoleValidity(true);
            configDispatcher({
                action: 'visibleRoles',
                value: [...api.visibleRoles, role],
            });
        }).catch((error) => {
            if (error.status === 404) {
                setRoleValidity(false);
                setInvalidRoles([...invalidRoles, role]);
            } else {
                Alert.error('Error when validating role: ' + role);
                console.error('Error when validating roles ' + error);
            }
        });
    };

    const handleRoleDeletion = (role) => {
        if (invalidRoles.includes(role)) {
            setInvalidRoles(invalidRoles.filter((existingRole) => existingRole !== role));
        }
        if (api.visibility === 'RESTRICTED' && api.visibleRoles.length > 1) {
            setRoleExists(true);
        } else {
            setRoleExists(false);
        }
        configDispatcher({
            action: 'visibleRoles',
            value: api.visibleRoles.filter((existingRole) => existingRole !== role),
        });
    };

    return (
        <>
            <Box style={{ position: 'relative' }}>
                <TextField
                    fullWidth
                    id='storeVisibility-selector'
                    select
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.head.topic'
                            defaultMessage='Developer Portal Visibility'
                        />
                    )}
                    value={api.visibility}
                    name='storeVisibility'
                    onChange={({ target: { value } }) => configDispatcher({ action: 'visibility', value })}
                    SelectProps={{
                        MenuProps: {
                            className: classes.menu,
                        },
                    }}
                    helperText={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.form.helper.text'
                            defaultMessage='By default API is visible to all developer portal users'
                        />
                    )}
                    margin='normal'
                    variant='outlined'
                    disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                >
                    <MenuItem value='PUBLIC'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.StoreVisibility.dropdown.public'
                            defaultMessage='Public'
                        />
                    </MenuItem>
                    <MenuItem value='RESTRICTED'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.dropdown.restrict'
                            defaultMessage='Restrict by role(s)'
                        />
                    </MenuItem>
                    {tenants !== 0
                        && (
                            <MenuItem value='PRIVATE'>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.storeVisibility.dropdown.private'
                                    defaultMessage='Visible to my domain'
                                />
                            </MenuItem>
                        )}
                </TextField>
                <Tooltip
                    title={(
                        <>
                            <p>
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.storeVisibility.tooltip.public'
                                        defaultMessage='Public :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.storeVisibility.tooltip.public.desc'
                                    defaultMessage={
                                        'The API is accessible to everyone and can be advertised '
                                        + 'in multiple developer portals - a central developer portal '
                                        + 'and/or non-WSO2 developer portals.'
                                    }
                                />
                                <br />
                                <br />
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.storeVisibility.tooltip.restrict'
                                        defaultMessage='Restricted by roles(s) :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.storeVisibility.tooltip.restrict.desc'
                                    defaultMessage={
                                        'The API is visible only to specific user'
                                        + ' roles in the tenant developer portal that you specify.'
                                    }
                                />
                            </p>
                        </>
                    )}
                    aria-label='Store Visibility'
                    placement='right-end'
                    className={classes.tooltip}
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Box>
            {isRestrictedByRoles && (
                <Box py={2} style={{ marginTop: -10, marginBottom: 10 }}>
                    <ChipInput
                        fullWidth
                        variant='outlined'
                        label={(
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.storeVisibility.roles'
                                defaultMessage='Roles'
                            />
                        )}
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                        value={api.visibleRoles.concat(invalidRoles)}
                        alwaysShowPlaceholder={false}
                        placeholder='Enter roles and press Enter'
                        blurBehavior='clear'
                        InputProps={{
                            endAdornment: !roleValidity && (
                                <InputAdornment position='end'>
                                    <Error color='error' style={{ paddingBottom: 8 }} />
                                </InputAdornment>
                            ),
                        }}
                        onAdd={handleRoleAddition}
                        error={!roleValidity || !roleExists}
                        helperText={
                            roleValidity ? (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.visibility.CreateScope.roles.help'
                                    defaultMessage='Enter valid role and press enter'
                                />
                            ) : (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Roles.Invalid'
                                    defaultMessage='Role is invalid'
                                />
                            )
                        }
                        chipRenderer={({ value }, key) => (
                            <Chip
                                key={key}
                                size='small'
                                label={value}
                                onDelete={() => {
                                    handleRoleDeletion(value);
                                }}
                                style={{
                                    backgroundColor: invalidRoles.includes(value) ? red[300] : null,
                                    margin: '0 8px 12px 0',
                                    float: 'left',
                                }}
                            />
                        )}
                    />
                </Box>
            )}
        </>
    );
}

StoreVisibility.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
