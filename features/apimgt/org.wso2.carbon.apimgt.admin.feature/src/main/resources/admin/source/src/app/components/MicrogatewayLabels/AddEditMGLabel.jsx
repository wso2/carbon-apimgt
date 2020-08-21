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

import React, { useReducer, useState } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import Joi from '@hapi/joi';
import TextField from '@material-ui/core/TextField';
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
 * Render a pop-up dialog to add/edit an Gateway label
 * @param {JSON} props .
 * @returns {JSX}.
 */
function AddEditMGLabel(props) {
    const {
        updateList, dataRow, icon, triggerButtonText, title,
    } = props;
    const classes = useStyles();

    const [id, SetId] = useState();
    const initialState = {
        description: '',
        hosts: [],
    };

    const [state, dispatch] = useReducer(reducer, initialState);
    const { name, description, hosts } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const handleHostValidation = (hostName) => {
        if (hostName === undefined) {
            return false;
        }
        const schema = Joi.string().uri().empty();
        const validationError = schema.validate(hostName).error;

        if (validationError) {
            const errorType = validationError.details[0].type;
            if (errorType === 'any.empty') {
                return 'Host is empty';
            } else if (errorType === 'string.uri') {
                return 'Invalid Host';
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
                if (hosts === undefined) {
                    error = false;
                    break;
                }
                if (value.length === 0) {
                    error = 'Host is empty';
                    break;
                }
                for (const h in value) {
                    if (handleHostValidation(value[h])) {
                        error = handleHostValidation(value[h]);
                        break;
                    }
                }
                break;
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

        return promiseAPICall.then(() => {
            if (id) {
                return (
                    <FormattedMessage
                        id='AdminPages.Gateways.AddEdit.form.info.edit.successful'
                        defaultMessage='Gateway Label edited successfully'
                    />
                );
            } else {
                return (
                    <FormattedMessage
                        id='AdminPages.Gateways.AddEdit.form.info.add.successful'
                        defaultMessage='Gateway Label added successfully'
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

    const handleHostChange = (userHosts) => {
        dispatch({ field: 'hosts', value: userHosts });
    };

    const dialogOpenCallback = () => {
        if (dataRow) {
            SetId(dataRow.id);
            dispatch({ field: 'name', value: dataRow.name });
            dispatch({ field: 'description', value: dataRow.description });
            dispatch({ field: 'hosts', value: dataRow.accessUrls });
        }
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText={(
                <FormattedMessage
                    id='AdminPages.Gateways.AddEdit.form.save.button.label'
                    defaultMessage='Save'
                />
            )}
            icon={icon}
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
                            <FormattedMessage id='AdminPages.Gateways.AddEdit.form.name' defaultMessage='Name' />
                            <span className={classes.error}>*</span>
                        </span>
                    )}
                    fullWidth
                    error={hasErrors('name', name)}
                    helperText={hasErrors('name', name) || 'Name of the Gateway label'}
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
                    helperText='Description of the Gateway label'
                    variant='outlined'
                />
                {(id)
                    ? (
                        <ListInput
                            onInputListChange={handleHostChange}
                            initialList={hosts}
                            inputLabelPrefix='Host'
                            helperText='Enter Host'
                            addButtonLabel='Add Host'
                            onValidation={handleHostValidation}
                        />
                    )
                    : (
                        <ListInput
                            onInputListChange={handleHostChange}
                            inputLabelPrefix='Host'
                            helperText='Name of the Host'
                            addButtonLabel='Add Host'
                            onValidation={handleHostValidation}
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
        accessUrls: PropTypes.shape([]),
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default AddEditMGLabel;
