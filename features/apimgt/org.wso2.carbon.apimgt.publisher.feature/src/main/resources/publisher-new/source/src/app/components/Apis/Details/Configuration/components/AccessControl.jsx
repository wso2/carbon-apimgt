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
import FormControl from '@material-ui/core/FormControl';
import Tooltip from '@material-ui/core/Tooltip';
import HelpOutline from '@material-ui/icons/HelpOutline';
import { FormattedMessage } from 'react-intl';
import Input from '@material-ui/core/Input';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
import AuthManager from 'AppData/AuthManager';

/**
 *
 * api.accessControl possible values are `NONE` and `RESTRICTED`
 * @export
 * @param {*} props
 * @returns
 */
export default function AccessControl(props) {
    const { api, configDispatcher } = props;
    const isRestricted = api.accessControl === 'RESTRICTED';
    const isNotCreator = AuthManager.isNotCreator();

    return (
        <Grid container spacing={0} alignItems='flex-start'>
            <Grid item xs={11}>
                <FormControl style={{ display: 'flex' }}>
                    <InputLabel htmlFor='accessControl-selector'>
                        <FormattedMessage
                            id='Apis.Details.Configuration.components.AccessControl.head.topic'
                            defaultMessage='Access control'
                        />
                    </InputLabel>
                    <Select
                        disabled={isNotCreator}
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
                    title={(
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
                                    id='Apis.Details.Configuration.components.AccessControl.tooltip.restrict.desc'
                                    defaultMessage={'The API can be viewable and modifiable by only specific' +
                                    ' publishers and creators with the roles that you specify'}
                                />
                            </p>
                        </React.Fragment>
                    )}
                    aria-label='Access control'
                    placement='right-end'
                    interactive
                >
                    <HelpOutline />
                </Tooltip>
            </Grid>
            {isRestricted && (
                <Grid item>
                    <TextField
                        label='Role(s)'
                        margin='dense'
                        variant='outlined'
                        value={api.accessControlRoles.join(',')}
                        onChange={({ target: { value } }) => configDispatcher({ action: 'accessControlRoles', value })}
                        helperText={'Enter role name(s). If there are multiple roles, '
                        + 'separate them using comma (i:e role1,role2,...)'}
                        style={{ marginTop: 20 }}
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
