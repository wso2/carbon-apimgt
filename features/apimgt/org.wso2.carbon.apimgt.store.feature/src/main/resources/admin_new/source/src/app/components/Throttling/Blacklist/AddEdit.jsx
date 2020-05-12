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
    Typography, RadioGroup, Radio, FormControlLabel, FormControl, Grid, FormHelperText, Switch,
} from '@material-ui/core';
import { green } from '@material-ui/core/colors';
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
        marginTop: theme.spacing(2),
    },
    unitTimeSelection: {
        marginTop: theme.spacing(2.6),
        marginLeft: theme.spacing(2),
        minWidth: theme.spacing(15),
    },
    helperText: {
        color: green[600],
        fontSize: theme.spacing(1.6),
        marginLeft: theme.spacing(-1),
    },
    invertCondition: {
        fontSize: theme.spacing(1.8),
        marginRight: theme.spacing(3),
        marginTop: theme.spacing(1.2),
    },
}));

const initialState = {
    conditionType: 'API',
    conditionValue: {
        endingIp: '',
        startingIp: '',
        invert: false,
        fixedIp: '',
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
        case 'conditionType':
            return { ...state, [field]: value };
        case 'endingIp':
            return {
                ...state,
                conditionValue: { ...state.conditionValue, [field]: value },
            };
        case 'startingIp':
            return {
                ...state,
                conditionValue: { ...state.conditionValue, [field]: value },
            };
        case 'invert':
            console.log('invert', value);
            return {
                ...state,
                conditionValue: { ...state.conditionValue, [field]: value },
            };
        case 'fixedIp':
            console.log('fixedIp', value);
            return {
                ...state,
                conditionValue: { ...state.conditionValue, [field]: value },
            };
        default:
            return { ...state, [field]: value };
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
        conditionType, conditionValue: {
            endingIp, startingIp, invert, fixedIp,
        },
    } = state;
    const [validationError, setValidationError] = useState([]);
    const [editMode, setIsEditMode] = useState(false);
    const restApi = new API();

    const onChange = (e) => {
        if (e.target.name === 'invert') {
            dispatch({ field: e.target.name, value: e.target.checked });
        } else {
            dispatch({ field: e.target.name, value: e.target.value });
        }
    };

    // const validate = (fieldName, value) => {
    //     let error = '';
    //     switch (fieldName) {
    //         case 'policyName':
    //             error = value === '' ? (fieldName + ' is Empty') : '';
    //             setValidationError({ policyName: error });
    //             break;
    //         case 'requestCount':
    //             error = value === '' ? (fieldName + ' is Empty') : '';
    //             setValidationError({ requestCount: error });
    //             break;
    //         case 'dataAmount':
    //             error = value === '' ? (fieldName + ' is Empty') : '';
    //             setValidationError({ dataAmount: error });
    //             break;
    //         case 'unitTime':
    //             error = value === '' ? (fieldName + ' is Empty') : '';
    //             setValidationError({ unitTime: error });
    //             break;
    //         default:
    //             break;
    //     }
    //     return error;
    // };

    // const getAllFormErrors = () => {
    //     let errorText = '';
    //     const policyNameErrors = validate('policyName', policyName);
    //     const requestCountErrors = validate('requestCount', requestCount);
    //     const dataAmounttErrors = validate('dataAmount', dataAmount);
    //     const unitTimeErrors = validate('unitTime', unitTime);

    //     if (type === 'BandwidthLimit') {
    //         errorText += policyNameErrors + dataAmounttErrors + unitTimeErrors;
    //     } else {
    //         errorText += policyNameErrors + requestCountErrors + unitTimeErrors;
    //     }
    //     return errorText;
    // };

    const formSaveCallback = () => {
        // const formErrors = getAllFormErrors();
        // if (formErrors !== '') {
        //     Alert.error(formErrors);
        //     return (false);
        // }
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
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Blacklist'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
            dialogOpenCallback={dialogOpenCallback}
        >
            <DialogContentText>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Admin.Throttling.Blacklist.Throttling.Policy.add.condition.type'
                        defaultMessage='Condition Type'
                    />
                </Typography>
            </DialogContentText>
            <FormControl component='fieldset'>
                <RadioGroup
                    row
                    aria-label='position'
                    defaultValue='top'
                    name='conditionType'
                    onChange={onChange}
                    value={conditionType}
                >
                    <FormControlLabel
                        value='API'
                        control={<Radio color='primary' />}
                        label='API Context '
                        labelPlacement='end'
                    />
                    <FormControlLabel
                        value='APPLICATION'
                        control={<Radio color='primary' />}
                        label='Application'
                        labelPlacement='end'
                    />
                    <FormControlLabel
                        value='IP'
                        control={<Radio color='primary' />}
                        label='IP Address'
                        labelPlacement='end'
                    />
                    <FormControlLabel
                        value='IPRANGE'
                        control={<Radio color='primary' />}
                        label='IP Range'
                        labelPlacement='end'
                    />
                    <FormControlLabel
                        value='USER'
                        control={<Radio color='primary' />}
                        label='User'
                        labelPlacement='end'
                    />
                </RadioGroup>
                {conditionType === 'API' && (
                    <TextField
                        autoFocus
                        margin='dense'
                        name='conditionValue'
                        label='Value'
                        fullWidth
                        onChange={onChange}
                        variant='outlined'
                        required
                        helperText={(
                            <>
                                <FormHelperText className={classes.helperText}>{'Format : ${context}'}</FormHelperText>
                                <FormHelperText className={classes.helperText}>Eg : /test/1.0.0</FormHelperText>
                            </>
                        )}
                    />
                )}
                {conditionType === 'APPLICATION' && (
                    <TextField
                        autoFocus
                        margin='dense'
                        name='conditionValue'
                        label='Value'
                        fullWidth
                        onChange={onChange}
                        variant='outlined'
                        required
                        helperText={(
                            <>
                                <FormHelperText className={classes.helperText}>
                                    {'Format : ${userName}:${applicationName}'}
                                </FormHelperText>
                                <FormHelperText className={classes.helperText}>
                                    Eg : admin:DefaultApplication
                                </FormHelperText>
                            </>
                        )}
                    />
                )}
                {conditionType === 'IP' && (
                    <TextField
                        autoFocus
                        margin='dense'
                        name='fixedIp'
                        label='Value'
                        fullWidth
                        onChange={onChange}
                        variant='outlined'
                        required
                        helperText={(
                            <>
                                <FormHelperText className={classes.helperText}>
                                    {'Format : ${ip}'}
                                </FormHelperText>
                                <FormHelperText className={classes.helperText}>
                                    Eg : 127.0.0.1
                                </FormHelperText>
                            </>
                        )}
                    />
                )}
                {conditionType === 'IPRANGE' && (
                    <>
                        <TextField
                            autoFocus
                            margin='dense'
                            name='startingIp'
                            label='Start IP Address'
                            fullWidth
                            onChange={onChange}
                            variant='outlined'
                            required
                        />
                        <TextField
                            autoFocus
                            margin='dense'
                            name='endingIp'
                            label='End IP Address'
                            fullWidth
                            onChange={onChange}
                            variant='outlined'
                            required
                        />
                    </>
                )}
                {(conditionType === 'IP' || conditionType === 'IPRANGE') && (
                    <Grid className={classes.unitTime}>
                        <Typography className={classes.invertCondition}>
                            <FormattedMessage
                                id='Admin.Throttling.Blacklist.policy.add.invert.condition'
                                defaultMessage='Invert Condition: '
                            />
                        </Typography>
                        <Switch
                            checked={invert}
                            onChange={onChange}
                            name='invert'
                            color='primary'
                        />
                    </Grid>
                ) }
                {conditionType === 'USER' && (
                    <TextField
                        autoFocus
                        margin='dense'
                        name='conditionValue'
                        label='Value'
                        fullWidth
                        onChange={onChange}
                        variant='outlined'
                        required
                        helperText={(
                            <>
                                <FormHelperText className={classes.helperText}>
                                    {'Format : ${userName}'}
                                </FormHelperText>
                                <FormHelperText className={classes.helperText}>
                                    Eg : admin
                                </FormHelperText>
                            </>
                        )}
                    />
                )}
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
