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

import React, {
    useReducer, useState, Suspense, lazy, useEffect,
} from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { FormattedMessage, injectIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import { Typography } from '@material-ui/core';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import sqlFormatter from 'sql-formatter';
import { Progress } from 'AppComponents/Shared';

const MonacoEditor = lazy(() => import('react-monaco-editor' /* webpackChunkName: "CustomPolicyAddMonacoEditor" */));

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    dialog: {
        minWidth: theme.spacing(150),

    },
    siddhiQueryHeading: {
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(1),
    },
    showSampleButton: {
        marginTop: theme.spacing(2),
    },
}));

const sampleSiddhiQuery = "FROM RequestStream SELECT userId, ( userId == 'admin@carbon.super' ) "
+ "AS isEligible , str:concat('admin@carbon.super','') as throttleKey "
+ 'INSERT INTO EligibilityStream; FROM EligibilityStream[isEligible==true]#throttler:timeBatch(1 min) '
+ 'SELECT throttleKey, (count(userId) >= 5) as isThrottled, expiryTimeStamp group by throttleKey '
+ 'INSERT ALL EVENTS into ResultStream;';
const formattedSampleSiddhiQuery = sqlFormatter.format(sampleSiddhiQuery);


/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}
 */
function reducer(state, newValue) {
    const { field, value } = newValue;
    switch (field) {
        case 'policyName':
            return { ...state, [field]: value };
        case 'description':
            return { ...state, [field]: value };
        case 'keyTemplate':
            return { ...state, [field]: value };
        case 'siddhiQuery':
            return { ...state, [field]: value };
        default:
            return newValue;
    }
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AddEdit(props) {
    const classes = useStyles();
    const {
        updateList, icon, triggerButtonText, title, dataRow,
    } = props;
    const [initialState, setInitialState] = useState({
        policyName: '',
        description: '',
        keyTemplate: '',
        siddhiQuery: formattedSampleSiddhiQuery,
    });
    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        policyName, description, keyTemplate, siddhiQuery,
    } = state;
    const [validationError, setValidationError] = useState([]);
    const [editMode, setIsEditMode] = useState(false);
    const restApi = new API();

    useEffect(() => {
        setInitialState({
            policyName: '',
            description: '',
            defaultLimit: {
                requestCount: '',
                timeUnit: 'min',
                unitTime: '',
                type: 'RequestCountLimit',
                dataAmount: '',
                dataUnit: 'KB',
            },
        });
    }, []);

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const siddhiQueryOnChange = (newValue) => {
        dispatch({ field: 'siddhiQuery', value: newValue });
    };

    const validate = (fieldName, value) => {
        let error = '';
        switch (fieldName) {
            case 'policyName':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ policyName: error });
                break;
            case 'keyTemplate':
                if (value === '') {
                    error = (fieldName + ' is Empty');
                } else if (value.indexOf(' ') !== -1) {
                    error = 'Invalid Key Template';
                } else {
                    error = false;
                }
                setValidationError({ keyTemplate: error });
                break;
            case 'siddhiQuery':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ siddhiQuery: error });
                break;
            default:
                break;
        }
        return error;
    };

    const getAllFormErrors = () => {
        let errorText = '';
        const policyNameErrors = validate('policyName', policyName);
        const keyTemplateErrors = validate('keyTemplate', keyTemplate);
        const siddhiQueryErrors = validate('siddhiQuery', siddhiQuery);

        errorText += policyNameErrors + keyTemplateErrors + siddhiQueryErrors;

        return errorText;
    };

    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '' && formErrors !== 'false') {
            Alert.error(formErrors);
            return (false);
        }

        let promisedAddCustomPolicy;
        if (state.siddhiQuery.indexOf('\n') !== -1) {
            const siddhiQueryValue = state.siddhiQuery.split('\n').join(' ');
            delete (state.siddhiQuery);
            state.siddhiQuery = siddhiQueryValue;
        }
        const customPolicy = state;

        if (dataRow) {
            const { policyId } = dataRow;
            promisedAddCustomPolicy = restApi.updateCustomPolicy(policyId,
                customPolicy);
            return promisedAddCustomPolicy
                .then(() => {
                    return (
                        <FormattedMessage
                            id='Throttling.Application.Policy.policy.edit.success'
                            defaultMessage='Application Rate Limiting Policy edited successfully.'
                        />
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        throw (response.body.description);
                    }
                    return null;
                })
                .finally(() => {
                    updateList();
                });
        } else {
            promisedAddCustomPolicy = restApi.addCustomPolicy(
                customPolicy,
            );
            return promisedAddCustomPolicy
                .then(() => {
                    return (
                        <FormattedMessage
                            id='Throttling.Application.Policy.policy.add.success'
                            defaultMessage='Custom Policy added successfully.'
                        />
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        throw (response.body.description);
                    }
                    return null;
                })
                .finally(() => {
                    updateList();
                });
        }
    };

    const dialogOpenCallback = () => {
        if (dataRow) {
            setIsEditMode(true);
            const { policyId } = dataRow;
            restApi.customPolicyGet(policyId).then((result) => {
                const formattedSiddhiQuery = sqlFormatter.format(result.body.siddhiQuery);
                const editState = {
                    policyName: result.body.policyName,
                    description: result.body.description,
                    keyTemplate: result.body.keyTemplate,
                    siddhiQuery: formattedSiddhiQuery,
                };
                dispatch(editState);
            });
        }
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Apply Rule'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
            dialogOpenCallback={dialogOpenCallback}
        >
            <TextField
                autoFocus
                margin='dense'
                name='policyName'
                label='Name'
                fullWidth
                required
                variant='outlined'
                value={policyName}
                disabled={editMode}
                onChange={onChange}
                InputProps={{
                    id: 'policyName',
                    onBlur: ({ target: { value } }) => {
                        validate('policyName', value);
                    },
                }}
                error={validationError.policyName}
                helperText={validationError.policyName && validationError.policyName}
            />
            <TextField
                margin='dense'
                name='description'
                label='Description'
                fullWidth
                variant='outlined'
                value={description}
                onChange={onChange}
            />
            <TextField
                margin='dense'
                name='keyTemplate'
                label='Key Template'
                fullWidth
                required
                variant='outlined'
                value={keyTemplate}
                onChange={onChange}
                InputProps={{
                    id: 'keyTemplate',
                    onBlur: ({ target: { value } }) => {
                        validate('keyTemplate', value);
                    },
                }}
                error={validationError.keyTemplate}
                helperText={validationError.keyTemplate && validationError.keyTemplate}
            />
            <Typography className={classes.siddhiQueryHeading}>
                <FormattedMessage
                    id='Admin.Throttling.Custom.policy.add.siddhi.query'
                    defaultMessage='Siddhi Query: '
                />
            </Typography>
            {!editMode && (
                <>
                    <Typography>
                        <FormattedMessage
                            id='Admin.Throttling.Custom.policy.add.siddhi.query.description'
                            defaultMessage='The following query will allow 5 requests per minute for an Admin user.'
                        />
                    </Typography>
                    <Typography>
                        <FormattedMessage
                            id='Admin.Throttling.Custom.policy.add.siddhi.query.key.template'
                            defaultMessage='Key Template : $userId'
                        />
                    </Typography>
                </>
            )}
            <Suspense fallback={<Progress />}>
                <MonacoEditor
                    language='sql'
                    height='250px'
                    theme='vs-dark'
                    value={siddhiQuery}
                    onChange={siddhiQueryOnChange}
                />
            </Suspense>
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
        policyId: PropTypes.string.isRequired,
    }),
    icon: PropTypes.element,
    triggerButtonText: PropTypes.shape({}).isRequired,
    title: PropTypes.shape({}).isRequired,
};

export default injectIntl(AddEdit);
