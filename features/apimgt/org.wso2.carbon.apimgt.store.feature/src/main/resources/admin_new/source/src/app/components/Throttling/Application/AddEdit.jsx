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

// const initialState = {
//     name: '',
//     description: '',
// };


/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}.
 */
// function reducer(state, { field, value }) {
//     return {
//         ...state,
//         [field]: value,
//     };
// }

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AddEdit(props) {
    const classes = useStyles();
    const {
        updateList, icon, triggerButtonText, title, applicationThrottlingPolicyList, dataRow,
    } = props;
    const [quotaPolicyType, setQuotaPolicyType] = useState('RequestCountLimit');
    const [unitTime, setUnitTime] = useState('min');
    const [dataBandwithUnit, setDataBandwithUnit] = useState('KB');
    const applicationThrottlingPolicy = { defaultLimit: {} };
    const [policy, setPolicy] = useState({});
    const [validationError, setValidationError] = useState([]);
    const restApi = new API();

    // useEffect(() => {
    //     initialState = {
    //         label: '',
    //         description: '',
    //     };
    // }, [title]);

    // const [state, dispatch] = useReducer(reducer, initialState);
    // const { label, description } = state;

    // const onChange = (e) => {
    //     dispatch({ field: e.target.name, value: e.target.value });
    // };
    // const validate = (fieldName, value) => {
    //     let error = false;
    //     error = value === '' ? fieldName + ' is Empty' : false;
    //     setValidationError({ [fieldName]: error });
    // };
    const validate = (fieldName, value) => {
        let error = false;
        const isNumeric = (value !== '') && !Number.isNaN(Number(value));
        switch (fieldName) {
            case 'policyName':
                error = value === '' ? fieldName + ' is Empty' : false;
                setValidationError({ policyName: error });
                break;
            case 'requestCountValue' || 'dataBandWithValue' || 'unitTime':
                if (value !== '') {
                    error = isNumeric ? setValidationError({ isNumeric: true })
                        : (setValidationError({ isNumeric: false }) && fieldName + ' incorrect value');
                } else {
                    error = fieldName + ' is Empty';
                }
                break;
            default:
                error = value === '' ? fieldName + ' is Empty' : false;
                setValidationError({ [fieldName]: error });
                break;
        }
    };


    // const getAllFormErrors = () => {
    //     let errorText = '';
    //     const labelErrors = hasErrors('label', label);
    //     if (labelErrors) {
    //         errorText += labelErrors + '\n';
    //     }
    //     return errorText;
    // };

    const formSaveCallback = () => {
        if (validationError.length !== 0) {
            Alert.error('Error while adding Application Throttling Policy. Mandatory values have not been filled');
            return (false);
        }
        applicationThrottlingPolicy.defaultLimit.type = quotaPolicyType;
        applicationThrottlingPolicy.defaultLimit.timeUnit = unitTime;
        if (quotaPolicyType === 'BandwidthLimit') {
            applicationThrottlingPolicy.defaultLimit.dataUnit = dataBandwithUnit;
        }
        const promisedAddApplicationPolicy = restApi.addApplicationThrottlingPolicy(
            applicationThrottlingPolicy,
        );
        promisedAddApplicationPolicy
            .then(() => {
                updateList();
                return (
                    <FormattedMessage
                        id='Throttling.Application.Policy.policy.add.success'
                        defaultMessage='Application Throttling Policy added successfully.'
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
        return (promisedAddApplicationPolicy);
    };

    const handleChangeQuotyPolicyType = (event) => {
        setQuotaPolicyType(event.target.value);
    };

    const handleChangeUnitTime = (event) => {
        setUnitTime(event.target.value);
    };

    const handleDataBandwithUnit = (event) => {
        setDataBandwithUnit(event.target.value);
    };

    const handleThrottlingApplicationInput = ({ target: { id, value } }) => {
        if (id === 'dataBandWithValue') {
            applicationThrottlingPolicy.defaultLimit.dataAmount = value;
        } else if (id === 'requestCountValue') {
            applicationThrottlingPolicy.defaultLimit.requestCount = value;
        } else if (id === 'unitTime') {
            applicationThrottlingPolicy.defaultLimit.unitTime = value;
        } else {
            applicationThrottlingPolicy[id] = value;
        }
    };

    const dialogOpenCallback = () => {
        // We can do an API call when we are in the editing mode
        if (dataRow) {
            const selectedPolicy = applicationThrottlingPolicyList.filter(
                (policyy) => policyy.policyName === dataRow[0],
            );
            const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;
            restApi.applicationThrottlingPolicyGet(policyId).then((result) => {
                setPolicy(result.body);
                setQuotaPolicyType(result.body.defaultLimit.type);
                setDataBandwithUnit(result.body.defaultLimit.dataUnit);
                setUnitTime(result.body.defaultLimit.timeUnit);
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
            {console.log('hello', policy)}
            <TextField
                autoFocus
                margin='dense'
                id='policyName'
                label='Name'
                fullWidth
                required
                variant='outlined'
                value={policy.policyName || ''}
                onChange={handleThrottlingApplicationInput}
                InputProps={{
                    id: 'itest-id-policyName-input',
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
                id='description'
                label='Description'
                fullWidth
                variant='outlined'
                value={policy.description || ''}
                onChange={handleThrottlingApplicationInput}
            />
            <DialogContentText>
                <Typography variant='h6' className={classes.quotaHeading}>
                    <FormattedMessage
                        id='Admin.Throttling.Application.Throttling.Policy.add.general.details'
                        defaultMessage='Quota Limits'
                    />
                </Typography>
            </DialogContentText>
            <FormControl component='fieldset'>
                <RadioGroup
                    row
                    aria-label='position'
                    name='position'
                    defaultValue='top'
                    onChange={handleChangeQuotyPolicyType}
                    value={quotaPolicyType}
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
                {quotaPolicyType === 'RequestCountLimit' ? (
                    <TextField
                        autoFocus
                        margin='dense'
                        id='requestCountValue'
                        label='Request Count'
                        fullWidth
                        value={(policy.defaultLimit && policy.defaultLimit.requestCount) || ''}
                        onChange={handleThrottlingApplicationInput}
                        required
                        InputProps={{
                            id: 'itest-id-requestCountValue-input',
                            onBlur: ({ target: { value } }) => {
                                validate('requestCountValue', value);
                            },
                        }}
                        error={validationError.requestCountValue || !validationError.isNumeric}
                    />
                ) : (
                    <Grid className={classes.unitTime}>
                        <TextField
                            autoFocus
                            margin='dense'
                            id='dataBandWithValue'
                            label='Data Bandwith'
                            fullWidth
                            required
                            value={(policy.defaultLimit && policy.defaultLimit.dataAmount) || ''}
                            onChange={handleThrottlingApplicationInput}
                            InputProps={{
                                id: 'itest-id-dataBandWithValue-input',
                                onBlur: ({ target: { value } }) => {
                                    validate('dataBandWithValue', value);
                                },
                            }}
                            error={validationError.dataBandWithValue || !validationError.isNumeric}
                        />
                        <FormControl className={classes.unitTimeSelection}>
                            <Select
                                labelId='demo-simple-select-label'
                                id='demo-simple-select'
                                value={dataBandwithUnit}
                                onChange={handleDataBandwithUnit}
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
                        id='unitTime'
                        label='Unit Time'
                        type='number'
                        fullWidth
                        value={(policy.defaultLimit && policy.defaultLimit.unitTime) || ''}
                        onChange={handleThrottlingApplicationInput}
                        InputProps={{
                            id: 'itest-id-unitTime-input',
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
                            id='demo-simple-select'
                            value={unitTime}
                            onChange={handleChangeUnitTime}
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
