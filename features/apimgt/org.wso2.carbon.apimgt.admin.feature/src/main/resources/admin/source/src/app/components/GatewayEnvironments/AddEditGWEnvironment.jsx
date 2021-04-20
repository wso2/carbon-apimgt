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

import React, { useEffect, useReducer, useState } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage, useIntl } from 'react-intl';
import FormControl from '@material-ui/core/FormControl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import Alert from 'AppComponents/Shared/Alert';
import AddEditVhost from 'AppComponents/GatewayEnvironments/AddEditVhost';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    addEditFormControl: {
        minHeight: theme.spacing(40),
        maxHeight: theme.spacing(100),
        minWidth: theme.spacing(55),
    },
    vhostPaper: {
        padding: theme.spacing(1),
        marginBottom: theme.spacing(1),
    },
}));

/**
 * Reducer
 * @param {JSON} state State
 * @param field form field
 * @param value value of field
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    switch (field) {
        case 'name':
        case 'displayName':
        case 'description':
        case 'vhosts':
            return { ...state, [field]: value };
        case 'editDetails':
            return value;
        default:
            return state;
    }
}

/**
 * Render a pop-up dialog to add/edit a Gateway Environment
 * @param {JSON} props .
 * @returns {JSX}.
 */
function AddEditGWEnvironment(props) {
    const intl = useIntl();
    const {
        updateList, dataRow, icon, triggerButtonText, title,
    } = props;
    const classes = useStyles();

    const defaultVhost = {
        host: '', httpContext: '', httpsPort: 8243, httpPort: 8280, wssPort: 8099, wsPort: 9099, isNew: true,
    };
    const [initialState, setInitialState] = useState({
        displayName: '',
        description: '',
        vhosts: [defaultVhost],
    });
    const [editMode, setIsEditMode] = useState(false);

    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        name, displayName, description, vhosts,
    } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    useEffect(() => {
        setInitialState({
            displayName: '',
            description: '',
            vhosts: [defaultVhost],
        });
    }, []);

    const handleHostValidation = (vhost) => {
        if (!vhost) {
            return false;
        }
        if (!vhost.host) {
            return (
                intl.formatMessage({
                    id: 'GatewayEnvironments.AddEditGWEnvironment.form.vhost.host.empty',
                    defaultMessage: 'Host of Vhost is empty',
                })
            );
        }
        // same pattern used in admin Rest API
        const hostPattern = '^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9]'
            + '[A-Za-z0-9\\-]*[A-Za-z0-9])$';
        const hostRegex = new RegExp(hostPattern, 'g');
        const validHost = vhost.host && vhost.host.match(hostRegex);
        if (!validHost) {
            return (
                intl.formatMessage({
                    id: 'GatewayEnvironments.AddEditGWEnvironment.form.vhost.host.invalid',
                    defaultMessage: 'Invalid Host',
                })
            );
        }

        // same pattern used in admin Rest API
        const httpContextRegex = /^\/?([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])*$/g;
        // empty http context are valid
        const validHttpContext = !vhost.httpContext || vhost.httpContext.match(httpContextRegex);
        if (!validHttpContext) {
            return (
                intl.formatMessage({
                    id: 'GatewayEnvironments.AddEditGWEnvironment.form.vhost.context.invalid',
                    defaultMessage: 'Invalid Http context',
                })
            );
        }

        let portError;
        const ports = ['httpPort', 'httpsPort', 'wsPort', 'wssPort'];
        for (const port of ports) {
            portError = Number.isInteger(vhost[port]) && vhost[port] >= 1 && vhost[port] <= 65535 ? '' : 'Invalid Port';
            if (portError) {
                return portError;
            }
        }
        return false;
    };

    const hasErrors = (fieldName, value) => {
        let error;
        switch (fieldName) {
            case 'name':
                if (value === undefined) {
                    error = false;
                    break;
                }
                if (value === '') {
                    error = (
                        intl.formatMessage({
                            id: 'GatewayEnvironments.AddEditGWEnvironment.form.environment.name.empty',
                            defaultMessage: 'Name is Empty',
                        })
                    );
                } else if (!(/^[A-Za-z0-9_-]+$/).test(value)) {
                    error = (
                        intl.formatMessage({
                            id: 'GatewayEnvironments.AddEditGWEnvironment.form.environment.name.invalid',
                            defaultMessage: 'Name must not contain special characters or spaces',
                        })
                    );
                } else {
                    error = false;
                }
                break;
            case 'displayName':
                if (!value) {
                    error = (
                        intl.formatMessage({
                            id: 'AdminPagesGatewayEnvironments.AddEditGWEnvironment.form.environment.displayName.empty',
                            defaultMessage: 'Display Name is Empty',
                        })
                    );
                } else {
                    error = false;
                }
                break;
            case 'vhosts': {
                if (value === undefined) {
                    error = false;
                    break;
                }
                if (value.length === 0) {
                    error = (
                        intl.formatMessage({
                            id: 'AdminPagesGatewayEnvironments.AddEditGWEnvironment.form.environment.vhost.empty',
                            defaultMessage: 'VHost is empty',
                        })
                    );
                    break;
                }
                const hosts = value.map((vhost) => vhost.host);
                if (hosts.length !== new Set(hosts).size) {
                    error = (
                        intl.formatMessage({
                            id: 'AdminPagesGatewayEnvironments.AddEditGWEnvironment.form.environment.vhost.duplicate',
                            defaultMessage: 'VHosts are duplicated',
                        })
                    );
                    break;
                }
                for (const host of value) {
                    error = handleHostValidation(host);
                    if (error) {
                        break;
                    }
                }
                break;
            }
            default:
                break;
        }
        return error;
    };
    const getAllFormErrors = () => {
        let errorText = '';
        if (name === undefined) {
            dispatch({ field: 'name', value: '' });
        }
        const nameErrors = hasErrors('name', name);
        const displayNameErrors = hasErrors('displayName', displayName);
        const vhostErrors = hasErrors('vhosts', vhosts);
        if (nameErrors) {
            errorText += nameErrors + '\n';
        }
        if (displayNameErrors) {
            errorText += displayNameErrors + '\n';
        }
        if (vhostErrors) {
            errorText += vhostErrors + '\n';
        }
        return errorText;
    };
    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return false;
        }
        const vhostDto = [];
        vhosts.forEach((vhost) => {
            vhostDto.push({
                host: vhost.host,
                httpContext: vhost.httpContext,
                httpPort: vhost.httpPort,
                httpsPort: vhost.httpsPort,
                wsPort: vhost.wsPort,
                wssPort: vhost.wssPort,
            });
        });

        const restApi = new API();
        let promiseAPICall;
        if (dataRow) {
            // assign the update promise to the promiseAPICall
            promiseAPICall = restApi.updateGatewayEnvironment(
                dataRow.id, name.trim(), displayName, description, vhostDto,
            );
        } else {
            // assign the create promise to the promiseAPICall
            promiseAPICall = restApi.addGatewayEnvironment(name.trim(), displayName, description, vhostDto);
        }

        return promiseAPICall.then(() => {
            if (dataRow) {
                return (
                    <FormattedMessage
                        id='GatewayEnvironments.AddEditGWEnvironment.form.info.edit.successful'
                        defaultMessage='Gateway Environment edited successfully'
                    />
                );
            } else {
                return (
                    <FormattedMessage
                        id='GatewayEnvironments.AddEditGWEnvironment.form.info.add.successful'
                        defaultMessage='Gateway Environment added successfully'
                    />
                );
            }
        }).catch((error) => {
            const { response } = error;
            if (response.body) {
                throw (response.body.description);
            }
            return null;
        }).finally(() => {
            updateList();
        });
    };

    const dialogOpenCallback = () => {
        if (dataRow) {
            const {
                name: originalName,
                displayName: originalDisplayName,
                description: originalDescription,
                vhosts: originalVhosts,
            } = dataRow;
            setIsEditMode(true);
            dispatch({
                field: 'editDetails',
                value: {
                    name: originalName,
                    displayName: originalDisplayName,
                    description: originalDescription,
                    vhosts: originalVhosts,
                },
            });
        }
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText={(
                <FormattedMessage
                    id='GatewayEnvironments.AddEditGWEnvironment.form.save.button.label'
                    defaultMessage='Save'
                />
            )}
            icon={icon}
            triggerIconProps={{ disabled: dataRow && dataRow.isReadOnly }}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
            dialogOpenCallback={dialogOpenCallback}
        >
            <FormControl component='fieldset' className={classes.addEditFormControl}>
                <TextField
                    autoFocus
                    margin='dense'
                    name='name'
                    value={name}
                    onChange={onChange}
                    label={(
                        <span>
                            <FormattedMessage
                                id='GatewayEnvironments.AddEditGWEnvironment.form.name'
                                defaultMessage='Name'
                            />
                            <span className={classes.error}>*</span>
                        </span>
                    )}
                    fullWidth
                    error={hasErrors('name', name)}
                    helperText={hasErrors('name', name) || 'Name of the Gateway Environment'}
                    variant='outlined'
                    disabled={editMode}
                />
                <TextField
                    margin='dense'
                    name='displayName'
                    value={displayName}
                    onChange={onChange}
                    label={(
                        <span>
                            <FormattedMessage
                                id='GatewayEnvironments.AddEditGWEnvironment.form.displayName'
                                defaultMessage='Display Name'
                            />
                            <span className={classes.error}>*</span>
                        </span>
                    )}
                    fullWidth
                    helperText={(
                        <FormattedMessage
                            id='GatewayEnvironments.AddEditGWEnvironment.form.displayName.help'
                            defaultMessage='Display name of the Gateway Environment'
                        />
                    )}
                    variant='outlined'
                />
                <TextField
                    margin='dense'
                    name='description'
                    value={description}
                    onChange={onChange}
                    label='Description'
                    fullWidth
                    multiline
                    helperText={(
                        <FormattedMessage
                            id='GatewayEnvironments.AddEditGWEnvironment.form.description.help'
                            defaultMessage='Description of the Gateway Environment'
                        />
                    )}
                    variant='outlined'
                />
                <AddEditVhost
                    initialVhosts={vhosts}
                    onVhostChange={onChange}
                />
            </FormControl>
        </FormDialogBase>
    );
}

AddEditGWEnvironment.defaultProps = {
    icon: null,
    dataRow: null,
};

AddEditGWEnvironment.propTypes = {
    updateList: PropTypes.func.isRequired,
    dataRow: PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
        displayName: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        isReadOnly: PropTypes.bool.isRequired,
        vhosts: PropTypes.shape([]),
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default AddEditGWEnvironment;
