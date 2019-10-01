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
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
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

/**
 *
 * api.accessControl possible values are `NONE` and `RESTRICTED`
 * @export
 * @param {*} props
 * @returns
 */
export default function StoreVisibility(props) {
    const [roleValidity, setRoleValidity] = useState(true);
    const [roleExists, setRoleExists] = useState(false);
    const { api, configDispatcher } = props;
    const [invalidRoles, setInvalidRoles] = useState([]);
    const isPublic = api.visibility === 'PUBLIC';
    const [apiFromContext] = useAPI();

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
            setInvalidRoles(invalidRoles.filter(existingRole => existingRole !== role));
        }
        if (api.visibility === 'RESTRICTED' && api.visibleRoles.length > 1) {
            setRoleExists(true);
        } else {
            setRoleExists(false);
        }
        configDispatcher({
            action: 'visibleRoles',
            value: api.visibleRoles.filter(existingRole => existingRole !== role),
        });
    };

    return (
        <Grid container spacing={0} alignItems='flex-start'>
            <Grid item xs={11}>
                <FormControl style={{ display: 'flex' }} >
                    <InputLabel htmlFor='storeVisibility-selector'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.head.topic'
                            defaultMessage='Store Visibility'
                        />
                    </InputLabel>
                    <Select
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                        value={api.visibility}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'visibility', value })}
                        input={<Input name='storeVisibility' id='storeVisibility-selector' />}
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
                    </Select>
                    <FormHelperText>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.storeVisibility.form.helper.text'
                            defaultMessage='By default API is visible to all store users'
                        />
                    </FormHelperText>
                </FormControl>
            </Grid>
            <Grid item xs={1}>
                <Tooltip
                    title={(
                        <React.Fragment>
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
                                        'The API is accessible to everyone and can be advertised ' +
                                        'in multiple stores - a central store and/or non-WSO2 stores.'
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
                                        'The API is visible only to specific user' +
                                        ' roles in the tenant store that you specify.'
                                    }
                                />
                            </p>
                        </React.Fragment>
                    )}
                    aria-label='Store Visibility'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            {!isPublic && (
                <Grid item>
                    <ChipInput
                        disabled={isRestricted(['apim:api_create', 'apim:api_publish'], apiFromContext)}
                        value={api.visibleRoles.concat(invalidRoles)}
                        alwaysShowPlaceholder={false}
                        placeholder='Enter roles and press Enter'
                        blurBehavior='clear'
                        InputProps={{
                            endAdornment: !roleValidity && (
                                <InputAdornment position='end'>
                                    <Error color='error' />
                                </InputAdornment>
                            ),
                        }}
                        onAdd={handleRoleAddition}
                        error={!roleValidity || !roleExists}
                        helperText={
                            roleValidity ? (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.CreateScope.roles.help'
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
                                label={value}
                                onDelete={() => {
                                    handleRoleDeletion(value);
                                }}
                                style={{
                                    backgroundColor: invalidRoles.includes(value) ? red[300] : null,
                                    margin: '8px 8px 8px 0',
                                    float: 'left',
                                }}
                            />
                        )}
                    />
                </Grid>
            )}
        </Grid>
    );
}

StoreVisibility.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
