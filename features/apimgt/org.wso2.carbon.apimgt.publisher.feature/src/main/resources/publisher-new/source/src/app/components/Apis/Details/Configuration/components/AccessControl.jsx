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

import React, { useState } from 'react';
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
    const isRestricted = api.accessControl === 'RESTRICTED';
    const [invalidRoles, setInvalidRoles] = useState([]);
    const handleRoleAddition = (systemValidation, userValidation, role) => {
        systemValidation.then((resp) => {
            if (resp) {
                setRoleValidity(true);
                userValidation.then((response) => {
                    if (response) {
                        setUserRoleValidity(true);
                        configDispatcher({ action: 'accessControlRoles',
                            value: api.accessControlRoles.length === 0 ? role : (api.accessControlRoles + ',' + role)});
                    } else {
                        setUserRoleValidity(false);
                        setInvalidRoles([...invalidRoles, role]);
                    }
                }).catch((error) => {
                    console.error('Error when validating user roles ' + error);
                });
            } else {
                setRoleValidity(false);
                setInvalidRoles([...invalidRoles, role]);
            }
        }).catch((error) => {
            console.error('Error when validating roles ' + error);
        });
    };

    const handleRoleDeletion = (role) => {
        const validRoles = api.accessControlRoles;
        const index = validRoles.indexOf(role);
        if (index > -1) {
            validRoles.splice(index, 1);
        } else {
            invalidRoles.splice(invalidRoles.indexOf(role), 1);
            setInvalidRoles(invalidRoles);
            if (invalidRoles.length === 0) {
                setRoleValidity(true);
                setUserRoleValidity(true);
            }
        }
        configDispatcher({action: 'accessControlRoles', value: validRoles.length ===0 ? '' : validRoles.join(',')});
    };
    return (
        <Grid container spacing={0} alignItems='flex-start'>
            <Grid item xs={11}>
                <FormControl style={{ display: 'flex' }} >
                    <InputLabel htmlFor='accessControl-selector'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.head.topic'
                            defaultMessage='Access control'
                        />
                    </InputLabel>
                    <Select
                        value={api.accessControl}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'accessControl', value })}
                        input={<Input name='accessControl' id='accessControl-selector' />}
                    >
                        <MenuItem value='NONE'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.AccessControl.dropdown.all'
                                defaultMessage='All'
                            />
                        </MenuItem>
                        <MenuItem value='RESTRICTED'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.components.AccessControl.dropdown.restrict'
                                defaultMessage='Restrict by role'
                            />
                        </MenuItem>
                    </Select>
                    <FormHelperText>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.form.helper.text'
                            defaultMessage='By default there is no access restrictions'
                        />
                    </FormHelperText>
                </FormControl>
            </Grid>
            <Grid item xs={1}>
                <Tooltip
                    title={
                        <React.Fragment>
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
                                    defaultMessage='The API is viewable, modifiable by all the publishers and
                                creators.'
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
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict.desc'
                                    defaultMessage='The API can be viewable and modifiable by only specific
                                    publishers and creators with the roles that you specify'
                                />
                            </p>
                        </React.Fragment>
                    }
                    aria-label='Access control'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            {isRestricted && (
                <Grid item>
                    <ChipInput
                        value={api.accessControlRoles.length ===0 ? invalidRoles :
                            api.accessControlRoles.concat(invalidRoles)}
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
                        onAdd={(role) => {
                            handleRoleAddition(
                                APIValidation.role.validate(base64url.encode(role)),
                                APIValidation.userRole.validate(base64url.encode(role)),
                                role,
                            );
                        }}
                        error={!roleValidity || !userRoleValidity}
                        helperText={
                            !roleValidity ? (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Roles.Invalid'
                                    defaultMessage='Role is invalid'
                                />
                            ) : !userRoleValidity ? (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.Roles.User.Invalid'
                                    defaultMessage='user does not have role'
                                />
                            ) : (
                                <FormattedMessage
                                    id='Apis.Details.Scopes.CreateScope.roles.help'
                                    defaultMessage='Enter valid role and press enter'
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
                                    float: 'left'
                                }}
                            />
                        )}
                    />
                </Grid>
            )}
        </Grid>
    );
}

AccessControl.propTypes = {
    api: PropTypes.shape({}).isRequired,
    configDispatcher: PropTypes.func.isRequired,
};
