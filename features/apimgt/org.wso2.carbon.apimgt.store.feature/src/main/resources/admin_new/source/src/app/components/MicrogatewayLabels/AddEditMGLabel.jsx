/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useReducer, useEffect } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import DialogContentText from '@material-ui/core/DialogContentText';
import { FormattedMessage } from 'react-intl';
import FormControl from '@material-ui/core/FormControl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import Alert from 'AppComponents/Shared/Alert';
import ListInput from 'AppComponents/AdminPages/Addons/InputListBase';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    addEditFormControl: {
        minHeight: theme.spacing(40),
        maxHeight: theme.spacing(40),
        minWidth: theme.spacing(55),
    },
}));

let initialState = {
    name: '',
    description: '',
    hosts: [],
};

/**
 * Reducer
 * @param {JSON} state State
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    return {
        ...state,
        [field]: value,
    };
}

/**
 * Render a pop-up dialog to add/edit an Microgateway label
 * @param {JSON} props .
 * @returns {JSX}.
 */
function AddEditMGLabel(props) {
    const {
        updateList, dataRow, icon, triggerButtonText, title,
    } = props;
    const classes = useStyles();
    let id = null;
    // If the dataRow is there ( form is in edit mode ) else it's a new creation
    useEffect(() => {
        initialState = {
            name: '',
            description: '',
            hosts: [],
        };
    }, [title]);

    if (dataRow) {
        // eslint-disable-next-line react/prop-types
        const { name: originalName, description: originalDescription, accessUrls: originalHosts } = dataRow;
        id = dataRow.id;

        initialState = {
            name: originalName,
            description: originalDescription,
            hosts: originalHosts,
        };
    }

    const [state, dispatch] = useReducer(reducer, initialState);
    const { name, description, hosts } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    const hasErrors = (fieldName, value) => {
        let error;
        switch (fieldName) {
            case 'name':
                if (value === '') {
                    error = 'Name is Empty';
                } else if (/\s/.test(value)) {
                    error = 'Name contains spaces';
                } else if (/[!@#$%^&*(),?"{}[\]|<>\t\n]/i.test(value)) {
                    error = 'Name field contains special characters';
                } else {
                    error = false;
                }
                break;
            case 'hosts':
                if (value && value.length === 0) {
                    error = 'Please add at least one host';
                } else {
                    error = false;
                }
                break;
            default:
                break;
        }
        return error;
    };
    const getAllFormErrors = () => {
        let errorText = '';
        const NameErrors = hasErrors('name', name);
        const hostErrors = hasErrors('hosts', hosts);
        if (NameErrors) {
            errorText += NameErrors + '\n';
        }
        if (hostErrors) {
            errorText += hostErrors + '\n';
        }
        return errorText;
    };
    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return false;
        }
        const restApi = new API();
        let promiseAPICall;
        if (id) {
            // assign the update promise to the promiseAPICall
            promiseAPICall = restApi.updateMicrogatewayLabel(id, name.trim(), description, hosts);
        } else {
            // assign the create promise to the promiseAPICall
            promiseAPICall = restApi.addMicrogatewayLabel(name.trim(), description, hosts);
        }
        promiseAPICall = new Promise((resolve, reject) => {
            promiseAPICall
                .then(() => {
                    resolve(
                        (id)
                            ? (
                                <FormattedMessage
                                    id='AdminPages.Microgateway.AddEdit.form.info.edit.successful'
                                    defaultMessage='Microgateway Label edited successfully'
                                />
                            )
                            : (
                                <FormattedMessage
                                    id='AdminPages.Microgateway.AddEdit.form.info.add.successful'
                                    defaultMessage='microgateway Label added successfully'
                                />
                            )
                        ,
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        reject(response.body.description);
                    }
                })
                .finally(() => {
                    updateList();
                });
        });
        return promiseAPICall;
    };

    const onHostChange = (userHosts) => {
        dispatch({ field: 'hosts', value: userHosts });
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Save'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                {(id) ? (
                    <FormattedMessage
                        id='AdminPages.Microgateway.AddEdit.form.edit.info'
                        defaultMessage='Edit selected Microgateway Label'
                    />
                )
                    : (
                        <FormattedMessage
                            id='AdminPages.Microgateway.AddEdit.form.add.info'
                            defaultMessage='Add a new Microgateway Label'
                        />
                    )}
            </DialogContentText>
            <FormControl component='fieldset' className={classes.addEditFormControl}>
                <TextField
                    autoFocus
                    margin='dense'
                    name='name'
                    value={name}
                    onChange={onChange}
                    label={(
                        <span>
                            <FormattedMessage id='AdminPages.Microgateway.AddEdit.form.name' defaultMessage='Name' />
                            <span className={classes.error}>*</span>
                        </span>
                    )}
                    fullWidth
                    error={hasErrors('name', name)}
                    helperText={hasErrors('name', name) || 'Enter Microgateway Label'}
                    variant='outlined'
                    disabled={id}
                />
                <TextField
                    margin='dense'
                    name='description'
                    value={description}
                    onChange={onChange}
                    label='Description'
                    fullWidth
                    multiline
                    helperText='Enter description'
                    variant='outlined'
                />
                {(id)
                    ? (
                        <ListInput
                            onInputListChange={onHostChange}
                            initialList={hosts}
                            inputLabelPrefix='Host'
                            helperText='Enter Host'
                            addButtonLabel='Add Host'
                        />
                    )
                    : (
                        <ListInput
                            onInputListChange={onHostChange}
                            inputLabelPrefix='Host'
                            helperText='Enter Host'
                            addButtonLabel='Add Host'
                        />
                    )}
            </FormControl>
        </FormDialogBase>
    );
}

AddEditMGLabel.defaultProps = {
    icon: null,
    dataRow: null,
};

AddEditMGLabel.propTypes = {
    updateList: PropTypes.func.isRequired,
    dataRow: PropTypes.shape({
        id: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default AddEditMGLabel;
