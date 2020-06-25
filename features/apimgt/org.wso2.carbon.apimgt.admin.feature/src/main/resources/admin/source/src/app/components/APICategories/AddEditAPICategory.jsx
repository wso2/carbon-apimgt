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

import React, { useReducer } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
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
 * Render a pop-up dialog to add/edit an API category
 * @param {JSON} props .
 * @returns {JSX}.
 */
function AddEdit(props) {
    const {
        updateList, dataRow, icon, triggerButtonText, title,
    } = props;
    const classes = useStyles();

    let id = null;
    let initialState = {
        description: '',
    };

    // If the dataRow is there, assign data to initialState
    if (dataRow) {
        const { name: originalName, description: originalDescription } = dataRow;
        id = dataRow.id;

        initialState = {
            name: originalName,
            description: originalDescription,
        };
    }

    const [state, dispatch] = useReducer(reducer, initialState);
    const { name, description } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
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
                    error = 'Name is Empty';
                } else if (/\s/.test(value)) {
                    error = 'Name contains spaces';
                } else if (/[!@#$%^&*(),?"{}[\]|<>\t\n]/i.test(value)) {
                    error = 'Name field contains special characters';
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
        let NameErrors;
        if (name === undefined) {
            dispatch({ field: 'name', value: '' });
            NameErrors = hasErrors('name', '');
        } else {
            NameErrors = hasErrors('name', name);
        }
        if (NameErrors) {
            errorText += NameErrors + '\n';
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
            promiseAPICall = restApi.updateAPICategory(id, name, description);
        } else {
            // assign the create promise to the promiseAPICall
            promiseAPICall = restApi.createAPICategory(name, description);
        }

        return promiseAPICall
            .then(() => {
                if (id) {
                    return (
                        <FormattedMessage
                            id='AdminPages.ApiCategories.AddEdit.form.edit.successful'
                            defaultMessage='API Category edited successfully'
                        />
                    );
                } else {
                    return (
                        <FormattedMessage
                            id='AdminPages.ApiCategories.AddEdit.form.add.successful'
                            defaultMessage='API Category added successfully'
                        />
                    );
                }
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    throw response.body.description;
                }
            })
            .finally(() => {
                updateList();
            });
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Save'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
        >
            <TextField
                autoFocus
                margin='dense'
                name='name'
                value={name}
                onChange={onChange}
                label={(
                    <span>
                        <FormattedMessage id='AdminPages.ApiCategories.AddEdit.form.name' defaultMessage='Name' />
                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                error={hasErrors('name', name)}
                helperText={hasErrors('name', name) || 'Enter API category name'}
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
        name: PropTypes.string.isRequired,
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default AddEdit;
