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

let initialState = {
    name: '',
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
function AddEdit(props) {
    const classes = useStyles();
    const {
        updateList, icon, triggerButtonText, title, applicationThrottlingPolicyList, selectedRow,
    } = props;
    const [quotaPolicyType, setQuotaPolicyType] = useState('RequestCountLimit');
    const [unitTime, setUnitTime] = useState('min');
    const [dataBandwithUnit, setDataBandwithUnit] = useState('KB');
    const applicationThrottlingPolicy = { defaultLimit: {} };
    const restApi = new API();

    // useEffect(() => {
    //     initialState = {
    //         label: '',
    //         description: '',
    //     };
    // }, [title]);

    if (selectedRow) {
        const selectedPolicy = applicationThrottlingPolicyList.filter(
            (policy) => policy.policyName === selectedRow[0],
        );
        const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;
        restApi.applicationThrottlingPolicyGet(policyId).then((result) => {
            console.log('result', result);
            applicationThrottlingPolicy.policyName = result.body.policyName;
            applicationThrottlingPolicy.description = result.body.description;
            applicationThrottlingPolicy.defaultLimit.requestCount = result.body.defaultLimit.requestCount;
            applicationThrottlingPolicy.defaultLimit.timeUnit = result.body.defaultLimit.timeUnit;
            applicationThrottlingPolicy.defaultLimit.type = result.body.defaultLimit.type;
            applicationThrottlingPolicy.defaultLimit.unitTime = result.body.defaultLimit.unitTime;

            return applicationThrottlingPolicy;
        });
    }

    // useEffect(() => {
    //     applicationThrottlingPolicy = {
    //         defaultLimit: {},
    //     };
    // }, [title]);

    // const [state, dispatch] = useReducer(reducer, initialState);
    // const { label, description } = state;

    // const onChange = (e) => {
    //     dispatch({ field: e.target.name, value: e.target.value });
    // };
    // const hasErrors = (fieldName, value) => {
    //     let error = false;
    //     switch (fieldName) {
    //         case 'label':
    //             error = value === '' ? fieldName + ' is Empty' : false;
    //             break;
    //         default:
    //             break;
    //     }
    //     return error;
    // };
    // const getAllFormErrors = () => {
    //     let errorText = '';
    //     const labelErrors = hasErrors('label', label);
    //     if (labelErrors) {
    //         errorText += labelErrors + '\n';
    //     }
    //     return errorText;
    // };

    const formSaveCallback = () => {
        // const formErrors = getAllFormErrors();
        // if (formErrors !== '') {
        //     Alert.error(formErrors);
        //     return (false);
        // }
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

    console.log('applicationThrottlingPolicy', JSON.stringify(applicationThrottlingPolicy));

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Save'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
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
                id='policyName'
                label='Name'
                fullWidth
                onChange={handleThrottlingApplicationInput}
                required
                value={applicationThrottlingPolicy.policyName}
            />
            <TextField
                autoFocus
                margin='dense'
                id='description'
                label='Description'
                fullWidth
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
                        onChange={handleThrottlingApplicationInput}
                        required
                    />
                ) : (
                    <Grid className={classes.unitTime}>
                        <TextField
                            autoFocus
                            margin='dense'
                            id='dataBandWithValue'
                            label='Data Bandwith'
                            fullWidth
                            onChange={handleThrottlingApplicationInput}
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
                        onChange={handleThrottlingApplicationInput}
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
