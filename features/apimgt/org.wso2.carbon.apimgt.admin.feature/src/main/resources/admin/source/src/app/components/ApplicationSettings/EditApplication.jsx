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

import React, { useReducer, useEffect, useState } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';

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
    switch (field) {
        case 'name':
        case 'owner':
            return { ...state, [field]: value };
        case 'editDetails':
            return value;
        default:
            return state;
    }
}
/**
 * Render a pop-up dialog to change ownership of an Application
 * @param {JSON} props props passed from parent
 * @returns {JSX}.
 */
function Edit(props) {
    const classes = useStyles();
    const restApi = new API();
    const {
        updateList, dataRow, icon, triggerButtonText, title, applicationList,
    } = props;
    const [initialState, setInitialState] = useState({
        name: '',
        owner: '',
    });

    const [state, dispatch] = useReducer(reducer, initialState);
    const { name, owner } = state;

    useEffect(() => {
        setInitialState({
            name: '',
            owner: '',
        });
    }, []);
    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const validateOwner = () => {
        let validationError = 'Something went wrong when validating user';

        const applicationsWithSameName = applicationList.filter(
            (app) => app.name === name && app.owner === owner,
        );

        const promiseValidation = new Promise((resolve, reject) => {
            if (applicationsWithSameName.length > 0) {
                validationError = `${owner} already has an application with name: ${name}`;
                reject(validationError);
            }
            const basicScope = 'apim:subscribe';
            restApi.getUserScope(owner, basicScope)
                .then(() => {
                    // This api returns 200 when only the $owner has the $basicScope.
                    resolve();
                }).catch((error) => {
                    const { response } = error;
                    // This api returns 404 when the $owner is not found.
                    // error codes: 901502, 901500 for user not found and scope not found
                    if (response?.body?.code === 901502 || response?.body?.code === 901500) {
                        validationError = `${owner} is not a valid Subscriber`;
                    }
                }).finally(() => {
                    if (validationError) {
                        reject(validationError);
                    }
                });
        });

        return promiseValidation;
    };

    const formSaveCallback = () => {
        return validateOwner().then(() => {
            return restApi.updateApplicationOwner(dataRow.applicationId, owner)
                .then(() => {
                    return (
                        <FormattedMessage
                            id='AdminPages.ApplicationSettings.Edit.form.edit.successful'
                            defaultMessage='Application owner changed successfully'
                        />
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    if (response?.body?.code === 500) {
                        const notValidSubscriber = 'Error while updating ownership to ' + owner;
                        throw notValidSubscriber;
                    } else {
                        const updateError = 'Something went wrong when updating owner';
                        throw updateError;
                    }
                })
                .finally(() => {
                    updateList();
                });
        });
    };
    const dialogOpenCallback = () => {
        if (dataRow) {
            const { name: originalName, owner: originalOwner } = dataRow;
            dispatch({ field: 'editDetails', value: { name: originalName, owner: originalOwner } });
        }
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
                helperText={(
                    <FormattedMessage
                        id='AdminPages.ApplicationSettings.Edit.form.helperText'
                        defaultMessage={'Enter a new Owner. '
                        + 'Make sure the new owner has logged into the Developer Portal at least once'}
                    />
                )}
                variant='outlined'
            />
        </FormDialogBase>
    );
}

Edit.defaultProps = {
    icon: null,
};

Edit.propTypes = {
    updateList: PropTypes.func.isRequired,
    dataRow: PropTypes.shape({
        applicationId: PropTypes.string.isRequired,
        owner: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
    }).isRequired,
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
    applicationList: PropTypes.shape([]).isRequired,
};

export default Edit;
