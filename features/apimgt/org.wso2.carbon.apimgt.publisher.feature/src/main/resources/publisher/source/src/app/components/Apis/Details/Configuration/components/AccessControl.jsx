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
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import MenuItem from '@material-ui/core/MenuItem';
import { isRestricted } from 'AppData/AuthManager';
import ChipInput from 'material-ui-chip-input';
import APIValidation from 'AppData/APIValidation';
import base64url from 'base64url';
import Error from '@material-ui/icons/Error';
import InputAdornment from '@material-ui/core/InputAdornment';
import Chip from '@material-ui/core/Chip';
import { red } from '@material-ui/core/colors/';
import Alert from 'AppComponents/Shared/Alert';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';

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
export default function AccessControl(props) {
    const [roleValidity, setRoleValidity] = useState(true);
    const [userRoleValidity, setUserRoleValidity] = useState(true);
    const { api, configDispatcher } = props;
    const isNone = api.accessControl === 'NONE';
    const [apiFromContext] = useAPI();
    const classes = useStyles();

    const [invalidRoles, setInvalidRoles] = useState([]);
    const [otherValidSystemRoles, setOtherValidSystemRoles] = useState([]);
    useEffect(() => {
        if (invalidRoles.length === 0) {
            setRoleValidity(true);
        }
    }, [invalidRoles]);
    useEffect(() => {
        if (otherValidSystemRoles.length === api.accessControlRoles.length && otherValidSystemRoles.length !== 0
            && (otherValidSystemRoles.every((val) => api.accessControlRoles.includes(val)))) {
            setUserRoleValidity(false);
        } else {
            setUserRoleValidity(true);
        }
    }, [otherValidSystemRoles]);
    const handleRoleAddition = (role) => {
        const systemRolePromise = APIValidation.role.validate(base64url.encode(role));
        const userRolePromise = APIValidation.userRole.validate(base64url.encode(role));
        systemRolePromise.then(() => {
            setRoleValidity(true);
            userRolePromise.then(() => {
                setUserRoleValidity(true);
                configDispatcher({
                    action: 'accessControlRoles',
                    value: [...api.accessControlRoles, role],
                });
            }).catch((error) => {
                if (error.status === 404) {
                    configDispatcher({
                        action: 'accessControlRoles',
                        value: [...api.accessControlRoles, role],
                    });
                    setOtherValidSystemRoles([...otherValidSystemRoles, role]);
                } else {
                    Alert.error('Error when validating role: ' + role);
                    console.error('Error when validating user roles ' + error);
                }
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
        setOtherValidSystemRoles(otherValidSystemRoles.filter((existingRole) => existingRole !== role));
        setInvalidRoles(invalidRoles.filter((existingRole) => existingRole !== role));
        configDispatcher({
            action: 'accessControlRoles',
            value: api.accessControlRoles.filter((existingRole) => existingRole !== role),
        });
    };

    const handleRoleValidationFailure = () => {
        if (!roleValidity) {
            return (
                <FormattedMessage
                    id='Apis.Details.Scopes.Roles.Invalid'
                    defaultMessage='Role is invalid'
                />
            );
        } else if (!userRoleValidity) {
            return (
                <FormattedMessage
                    id='Apis.Details.Scopes.Roles.User.Invalid'
                    defaultMessage='At least one role must be associated with the API creator'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='Apis.Details.AccessControl.roles.help'
                    defaultMessage='Enter valid role and press enter'
                />
            );
        }
    };

    return (
        <>
            <Box style={{ position: 'relative', marginBottom: -12 }}>
                <TextField
                    fullWidth
                    id='accessControl-selector'
                    select
                    label={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.head.topic'
                            defaultMessage='Publisher Access Control'
                        />
                    )}
                    value={api.accessControl}
                    name='accessControl'
                    onChange={({ target: { value } }) => configDispatcher({ action: 'accessControl', value })}
                    SelectProps={{
                        MenuProps: {
                            className: classes.menu,
                        },
                    }}
                    helperText={(
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.form.helper.text'
                            defaultMessage='There are no access restrictions by default'
                        />
                    )}
                    margin='normal'
                    variant='outlined'
                    disabled={isRestricted(['apim:api_create'], apiFromContext)}
                >
                    <MenuItem value='NONE'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.dropdown.none'
                            defaultMessage='All'
                        />
                    </MenuItem>
                    <MenuItem value='RESTRICTED'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.dropdown.restricted'
                            defaultMessage='Restrict by role(s)'
                        />
                    </MenuItem>
                </TextField>
                <Tooltip
                    title={(
                        <>
                            <p>
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.AccessControl.tooltip.all'
                                        defaultMessage='All :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.all.desc'
                                    defaultMessage='The API is viewable, modifiable by all the publishers and creators.'
                                />
                                <br />
                                <br />
                                <strong>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict'
                                        defaultMessage='Restricted by roles :'
                                    />
                                </strong>
                                {'  '}
                                <FormattedMessage
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict.
                                    desc'
                                    defaultMessage={'The API can be viewed and modified only by specific'
                                    + ' publishers and creators with the roles that you specify'}
                                />
                            </p>
                        </>
                    )}
                    aria-label='Publisher Access Control'
                    placement='right-end'
                    interactive
                    className={classes.tooltip}
                >
                    <HelpOutline />
                </Tooltip>
            </Box>
            {!isNone && (
                <Box py={1} style={{ marginTop: 10 }}>
                    <ChipInput
                        fullWidth
                        variant='outlined'
                        label={(
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.AccessControl.roles'
                                defaultMessage='Roles'
                            />
                        )}
                        disabled={isRestricted(['apim:api_create'], apiFromContext)}
                        value={api.accessControlRoles.concat(invalidRoles)}
                        alwaysShowPlaceholder={false}
                        placeholder='Enter roles and press Enter'
                        blurBehavior='clear'
                        InputProps={{
                            endAdornment: (!roleValidity || !userRoleValidity) && (
                                <InputAdornment position='end'>
                                    <Error color='error' style={{ paddingBottom: 8 }} />
                                </InputAdornment>
                            ),
                        }}
                        onAdd={handleRoleAddition}
                        error={!roleValidity || !userRoleValidity}
                        helperText={handleRoleValidationFailure()}
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

AccessControl.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
