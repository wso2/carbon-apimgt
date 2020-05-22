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

let initialState = {
    name: '',
    owner: '',
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
 * Render a pop-up dialog to change ownership of an Application
 * @param {JSON} props props passed from parent
 * @returns {JSX}.
 */
function Edit(props) {
    const classes = useStyles();
    const {
        updateList, dataRow, icon, triggerButtonText, title, applicationList,
    } = props;
    let id = null;

    if (dataRow) {
        const { name: originalName, owner: originalOwner } = dataRow;
        id = dataRow.applicationId;

        initialState = {
            name: originalName,
            owner: originalOwner,
        };
    }
    const [state, dispatch] = useReducer(reducer, initialState);
    const { name, owner } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const validateOwner = () => {
        const valid = { invalid: false, error: '' };

        const applicationsWithSameName = applicationList.filter(
            (app) => app.name === name && app.owner === owner,
        );
        if (applicationsWithSameName.length > 0) {
            valid.error = `${owner} already has an application with name: ${name}`;
            valid.invalid = true;
        }
        // todo: Validate whether the owner is an existing subscriber or not.

        return valid;
    };


    const getAllFormErrors = () => {
        let errorText = '';
        const valid = validateOwner(applicationList);
        if (valid.invalid) {
            errorText += valid.error;
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

        const promiseAPICall = new Promise((resolve, reject) => {
            restApi.updateApplicationOwner(id, owner)
                .then(() => {
                    resolve(
                        <FormattedMessage
                            id='AdminPages.ApplicationSettings.Edit.form.edit.successful'
                            defaultMessage='Application owner changed successfully'
                        />,
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        const { errorBody } = response.body;
                        reject(errorBody);
                    }
                })
                .finally(() => {
                    updateList();
                });
        });
        return promiseAPICall;
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
                <FormattedMessage
                    id='AdminPages.ApplicationSettings.Edit.form.info'
                    defaultMessage='Change the owner of the selected Application'
                />
            </DialogContentText>
            <TextField
                margin='dense'
                name='name'
                value={name}
                label={(
                    <span>
                        <FormattedMessage
                            id='AdminPages.ApplicationSettings.Edit.form.name'
                            defaultMessage='Application Name'
                        />
                        <span className={classes.error}>*</span>
                    </span>
                )}
                fullWidth
                variant='outlined'
                disabled
            />
            <TextField
                autoFocus
                margin='dense'
                name='owner'
                value={owner}
                onChange={onChange}
                label='Owner'
                fullWidth
                helperText='Enter a new Owner'
                variant='outlined'
            />
        </FormDialogBase>
    );
}

Edit.defaultProps = {
    icon: null,
    dataRow: null,
};

Edit.propTypes = {
    updateList: PropTypes.func.isRequired,
    dataRow: PropTypes.shape({
        applicationId: PropTypes.string.isRequired,
        owner: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
    applicationList: PropTypes.shape([]).isRequired,
};

export default Edit;
