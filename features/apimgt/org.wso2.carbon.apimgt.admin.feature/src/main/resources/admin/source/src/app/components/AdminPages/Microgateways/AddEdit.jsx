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
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import DialogContentText from '@material-ui/core/DialogContentText';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
}));


/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    return new Promise(((resolve) => {
        setTimeout(() => { resolve('Successfully did something'); }, 2000);
    }));
}

let initialState = {
    label: '',
    description: '',
};


/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    return {
        ...state,
        [field]: value,
    };
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AddEdit({
    updateList, dataRow, icon, triggerButtonText, title,
}) {
    const classes = useStyles();
    let id = null;
    // If the dataRow is there ( form is in edit mode ) else it's a new creation
    useEffect(() => {
        initialState = {
            label: '',
            description: '',
        };
    }, [title]);

    if (dataRow) {
        // eslint-disable-next-line react/prop-types
        const { label: originalLabel, description: originalDescription } = dataRow;
        id = dataRow.id;

        initialState = {
            label: originalLabel,
            description: originalDescription,
        };
    }
    const [state, dispatch] = useReducer(reducer, initialState);
    const { label, description } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    const hasErrors = (fieldName, value) => {
        let error = false;
        switch (fieldName) {
            case 'label':
                error = value === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const getAllFormErrors = () => {
        let errorText = '';
        const labelErrors = hasErrors('label', label);
        if (labelErrors) {
            errorText += labelErrors + '\n';
        }
        return errorText;
    };
    const dialogOpenCallback = () => {
        // We can do an API call when we are in the editing mode
        if (id) {
            // eslint-disable-next-line no-alert
            alert('use id=' + id + ' or what ever you got to make a backend call if you want.');
        }
    };
    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return (false);
        }
        // Do the API call
        const promiseAPICall = apiCall();
        if (id) {
            // assign the update promise to the promiseAPICall
        }
        promiseAPICall.then((data) => {
            updateList();
            return (data);
        })
            .catch((e) => {
                return (e);
            });
        return (promiseAPICall);
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Save'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
            dialogOpenCallback={dialogOpenCallback}
        >
            <DialogContentText>
                <FormattedMessage
                    id='AdminPages.Microgateways.AddEdit.form.info'
                    defaultMessage={'To subscribe to this website, please enter your'
                        + 'email address here. We will send updates'
                        + 'occasionally.'}
                />

            </DialogContentText>
            <TextField
                autoFocus
                margin='dense'
                name='label'
                value={label}
                onChange={onChange}
                label={(
                    <span>
                        <FormattedMessage
                            id='AdminPages.Microgateways.AddEdit.form.label'
                            defaultMessage='Label'
                        />

                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                error={hasErrors('label', label)}
                helperText={hasErrors('label', label) || 'Enter gateway label'}
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
                helperText='Enter description'
                variant='outlined'
            />
        </FormDialogBase>
    );
}

AddEdit.defaultProps = {
    icon: null,
    dataRow: null,
};

AddEdit.propTypes = {
    updateList: PropTypes.func.isRequired,
    dataRow: PropTypes.shape({
        id: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};


export default AddEdit;
