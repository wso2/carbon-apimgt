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
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import DialogContentText from '@material-ui/core/DialogContentText';
import { FormattedMessage, injectIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import {
    Typography, RadioGroup, Radio, FormControlLabel, FormControl, Grid, Select, MenuItem,
} from '@material-ui/core';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    dialog: {
        minWidth: theme.spacing(150),

    },
    quotaHeading: {
        marginTop: theme.spacing(3),
        marginBottom: theme.spacing(2),
    },
    unitTime: {
        display: 'flex',
        minWidth: theme.spacing(60),
    },
    unitTimeSelection: {
        marginTop: theme.spacing(2.6),
        marginLeft: theme.spacing(2),
        minWidth: theme.spacing(15),
    },
}));

let initialState = {
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
};


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
        case 'requestCount':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'timeUnit':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'unitTime':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'type':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'dataAmount':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'dataUnit':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
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
        updateList, icon, triggerButtonText, title, applicationThrottlingPolicyList, dataRow,
    } = props;
    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        policyName, description, defaultLimit: {
            requestCount, timeUnit, unitTime, type, dataAmount, dataUnit,
        },
    } = state;
    const [validationError, setValidationError] = useState([]);
    const [editMode, setIsEditMode] = useState(false);
    const restApi = new API();

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const validate = (fieldName, value) => {
        let error = '';
        switch (fieldName) {
            case 'policyName':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ policyName: error });
                break;
            case 'requestCount':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ requestCount: error });
                break;
            case 'dataAmount':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ dataAmount: error });
                break;
            case 'unitTime':
                error = value === '' ? (fieldName + ' is Empty') : '';
                setValidationError({ unitTime: error });
                break;
            default:
                break;
        }
        return error;
    };

    const getAllFormErrors = () => {
        let errorText = '';
        const policyNameErrors = validate('policyName', policyName);
        const requestCountErrors = validate('requestCount', requestCount);
        const dataAmounttErrors = validate('dataAmount', dataAmount);
        const unitTimeErrors = validate('unitTime', unitTime);

        if (type === 'BandwidthLimit') {
            errorText += policyNameErrors + dataAmounttErrors + unitTimeErrors;
        } else {
            errorText += policyNameErrors + requestCountErrors + unitTimeErrors;
        }
        return errorText;
    };

    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return (false);
        }
        let applicationThrottlingPolicy;
        let promisedAddApplicationPolicy;

        if (type === 'BandwidthLimit') {
            delete (state.defaultLimit.requestCount);
            applicationThrottlingPolicy = state;
        } else {
            applicationThrottlingPolicy = delete (state.defaultLimit.dataUnit);
            applicationThrottlingPolicy = delete (state.defaultLimit.dataAmount);
            applicationThrottlingPolicy = state;
        }

        if (dataRow) {
            const selectedPolicy = applicationThrottlingPolicyList.filter(
                (policyy) => policyy.policyName === dataRow[0],
            );
            const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;
            promisedAddApplicationPolicy = restApi.updateApplicationThrottlingPolicy(policyId,
                applicationThrottlingPolicy);
            promisedAddApplicationPolicy
                .then(() => {
                    updateList();
                    return (
                        <FormattedMessage
                            id='Throttling.Application.Policy.policy.add.success'
                            defaultMessage='Application Rate Limiting Policy added successfully.'
                        />
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    let errorDescription;
                    if (response.body) {
                        errorDescription = response.body;
                    }
                    return (errorDescription);
                });
        } else {
            promisedAddApplicationPolicy = restApi.addApplicationThrottlingPolicy(
                applicationThrottlingPolicy,
            );
            promisedAddApplicationPolicy
                .then(() => {
                    updateList();
                    return (
                        <FormattedMessage
                            id='Throttling.Application.Policy.policy.add.success'
                            defaultMessage='Application Rate Limiting Policy added successfully.'
                        />
                    );
                })
                .catch((error) => {
                    const { response } = error;
                    let errorDescription;
                    if (response.body) {
                        errorDescription = response.body;
                    }
                    return (errorDescription);
                });
        }
        return (promisedAddApplicationPolicy);
    };

    const dialogOpenCallback = () => {
        if (dataRow) {
            setIsEditMode(true);
            const selectedPolicy = applicationThrottlingPolicyList.filter(
                (policy) => policy.policyName === dataRow[0],
            );
            const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;
            restApi.applicationThrottlingPolicyGet(policyId).then((result) => {
                initialState = {
                    policyName: result.body.policyName,
                    description: result.body.description,
                    defaultLimit: {
                        requestCount: result.body.defaultLimit.requestCount,
                        timeUnit: result.body.defaultLimit.timeUnit,
                        unitTime: result.body.defaultLimit.unitTime,
                        type: result.body.defaultLimit.type,
                        dataAmount: result.body.defaultLimit.dataAmount,
                        dataUnit: result.body.defaultLimit.dataUnit,
                    },
                };
                dispatch(initialState);
            });
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
            <DialogContentText>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Admin.Throttling.Application.Throttling.Policy.add.general.details'
                        defaultMessage='General Details'
                    />
                </Typography>
            </DialogContentText>
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
                helperText={validationError.policyName && 'Application Policy Name is empty'}
            />
            <TextField
                autoFocus
                margin='dense'
                name='description'
                label='Description'
                fullWidth
                variant='outlined'
                value={description}
                onChange={onChange}
            />
            <DialogContentText>
                <Typography variant='h6' className={classes.quotaHeading}>
                    <FormattedMessage
                        id='Admin.Throttling.Application.Throttling.Policy.add.quota.limits.details'
                        defaultMessage='Quota Limits'
                    />
                </Typography>
            </DialogContentText>
            <FormControl component='fieldset'>
                <RadioGroup
                    row
                    aria-label='position'
                    defaultValue='top'
                    name='type'
                    onChange={onChange}
                    value={type}
                >
                    <FormControlLabel
                        value='RequestCountLimit'
                        control={<Radio color='primary' />}
                        label='Request Count '
                        labelPlacement='end'
                    />
                    <FormControlLabel
                        value='BandwidthLimit'
                        control={<Radio color='primary' />}
                        label='Request Bandwidth'
                        labelPlacement='end'
                    />
                </RadioGroup>
                {type === 'RequestCountLimit' ? (
                    <TextField
                        autoFocus
                        margin='dense'
                        name='requestCount'
                        label='Request Count'
                        fullWidth
                        value={requestCount}
                        type='number'
                        onChange={onChange}
                        variant='outlined'
                        required
                        InputProps={{
                            id: 'requestCount',
                            onBlur: ({ target: { value } }) => {
                                validate('requestCount', value);
                            },
                        }}
                        error={validationError.requestCountValue}
                    />
                ) : (
                    <Grid className={classes.unitTime}>
                        <TextField
                            autoFocus
                            margin='dense'
                            name='dataAmount'
                            label='Data Bandwith'
                            fullWidth
                            required
                            type='number'
                            variant='outlined'
                            value={dataAmount}
                            onChange={onChange}
                            InputProps={{
                                id: 'dataAmount',
                                onBlur: ({ target: { value } }) => {
                                    validate('dataAmount', value);
                                },
                            }}
                            error={validationError.dataAmount}
                        />
                        <FormControl className={classes.unitTimeSelection}>
                            <Select
                                labelId='demo-simple-select-label'
                                name='dataUnit'
                                value={dataUnit}
                                onChange={onChange}
                                fullWidth
                            >
                                <MenuItem value='KB'>KB</MenuItem>
                                <MenuItem value='MB'>MB</MenuItem>
                            </Select>
                        </FormControl>

                    </Grid>

                )}
                <Grid className={classes.unitTime}>
                    <TextField
                        autoFocus
                        margin='dense'
                        name='unitTime'
                        label='Unit Time'
                        type='number'
                        fullWidth
                        variant='outlined'
                        value={unitTime}
                        onChange={onChange}
                        InputProps={{
                            id: 'unitTime',
                            onBlur: ({ target: { value } }) => {
                                validate('unitTime', value);
                            },
                        }}
                        error={validationError.unitTime}
                        helperText={validationError.unitTime && 'Unit Time is empty'}
                    />
                    <FormControl className={classes.unitTimeSelection}>
                        <Select
                            labelId='demo-simple-select-label'
                            name='timeUnit'
                            value={timeUnit}
                            onChange={onChange}
                            fullWidth
                        >
                            <MenuItem value='min'>Minute(s)</MenuItem>
                            <MenuItem value='hour'>Hour(s)</MenuItem>
                            <MenuItem value='day'>Day(s)</MenuItem>
                            <MenuItem value='week'>Week(s)</MenuItem>
                            <MenuItem value='month'>Month(s)</MenuItem>
                            <MenuItem value='year'>Year(s)</MenuItem>
                        </Select>
                    </FormControl>
                </Grid>
            </FormControl>
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

export default injectIntl(AddEdit);
