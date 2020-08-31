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
import { useIntl, FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import {
    Typography, RadioGroup, Radio, FormControlLabel, FormControl, Grid, FormHelperText, Switch, Checkbox,
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
    unitTime: {
        display: 'flex',
        marginTop: theme.spacing(2),
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
    addForm: {
        minHeight: theme.spacing(24),
    },
}));

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
            return {
                ...state,
                conditionValue: { ...state.conditionValue, [field]: value },
            };
        case 'fixedIp':
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
        updateList, icon, triggerButtonText, title,
    } = props;
    const intl = useIntl();
    const [initialState, setInitialState] = useState({
        conditionType: 'API',
        conditionValue: {
            endingIp: '',
            startingIp: '',
            invert: false,
            fixedIp: '',
        },
        conditionStatus: true,
    });
    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        conditionType, conditionValue, conditionValue: {
            endingIp, startingIp, invert, fixedIp,
        }, conditionStatus,
    } = state;
    const restApi = new API();

    const onChange = (e) => {
        const { name } = e.target;
        if (name === 'invert' || name === 'conditionStatus') {
            dispatch({ field: name, value: e.target.checked });
        } else {
            dispatch({ field: name, value: e.target.value });
        }
    };

    useEffect(() => {
        setInitialState({
            conditionType: 'API',
            conditionValue: {
                endingIp: '',
                startingIp: '',
                invert: false,
                fixedIp: '',
            },
            conditionStatus: true,
        });
    }, [conditionType]);

    const hasErrors = (fieldName, value) => {
        let error = false;
        // matches for both ipv4 and ipv6
        const ip4Pattern = '^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}'
        + '([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$';
        const ip6Pattern = '(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:)'
        + '{1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}'
        + '(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}'
        + '(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)'
        + '|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,'
        + '1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:('
        + '(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))';
        const regexIpv4 = new RegExp(ip4Pattern);
        const regexIpv6 = new RegExp(ip6Pattern);
        switch (fieldName) {
            case 'startingIp':
                if (value === '') {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.startingIp.blank',
                        defaultMessage: 'Starting Ip address field is empty.',
                    });
                } else if (/\s/g.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.startingIp.white.spaces',
                        defaultMessage: 'Starting Ip address field contains white spaces. ',
                    });
                } else if (!regexIpv4.test(value) && !regexIpv6.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.startingIp.invalid',
                        defaultMessage: 'Starting Ip address entered is not valid.  ',
                    });
                }
                break;
            case 'endingIp':
                if (value === '') {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.endingIp.blank',
                        defaultMessage: 'Ending Ip address field is empty.  ',
                    });
                } else if (/\s/g.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.endingIp.white.spaces',
                        defaultMessage: 'Starting Ip address field contains white spaces. ',
                    });
                } else if (!regexIpv4.test(value) && !regexIpv6.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.endingIp.invalid',
                        defaultMessage: 'Starting Ip address entered is not valid.  ',
                    });
                }
                break;
            case 'fixedIp':
                if (value === '') {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.fixedIp.blank',
                        defaultMessage: 'Ending Ip address field is empty.  ',
                    });
                } else if (/\s/g.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.fixedIp.white.spaces',
                        defaultMessage: 'Starting Ip address field contains white spaces. ',
                    });
                } else if (!regexIpv4.test(value) && !regexIpv6.test(value)) {
                    error = intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.policy.fixedIp.invalid',
                        defaultMessage: 'Starting Ip address entered is not valid.  ',
                    });
                }
                break;
            case 'conditionValue':
                if (value.startingIp === '' && value.endingIp === ''
                && value.fixedIp === '' && value.invert === false) {
                    error = fieldName + ' is Empty';
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
        if (conditionType === 'IPRANGE') {
            const startingIPErrors = hasErrors('startingIp', startingIp);
            const endingIpErrors = hasErrors('endingIp', endingIp);
            errorText += startingIPErrors + endingIpErrors;
        } else if (conditionType === 'IP') {
            const fixedIpErrors = hasErrors('fixedIp', fixedIp);
            errorText += fixedIpErrors;
        } else {
            const conditionValueErrors = hasErrors('conditionValue', conditionValue);
            errorText += conditionValueErrors;
        }
        return errorText;
    };

    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '' && formErrors !== 'false' && formErrors !== '0') {
            Alert.error(formErrors);
            return (false);
        }
        let blacklistThrottlingPolicy;
        if (conditionType === 'IPRANGE') {
            blacklistThrottlingPolicy = delete (state.conditionValue.fixedIp);
            blacklistThrottlingPolicy = state;
        } else if (conditionType === 'IP') {
            blacklistThrottlingPolicy = delete (state.conditionValue.startingIp);
            blacklistThrottlingPolicy = delete (state.conditionValue.endingIp);
            blacklistThrottlingPolicy = state;
        } else {
            blacklistThrottlingPolicy = state;
        }

        const promisedAddBlacklistPolicy = restApi.addBlacklistPolicy(
            blacklistThrottlingPolicy,
        );
        return promisedAddBlacklistPolicy
            .then(() => {
                return (
                    <FormattedMessage
                        id='Throttling.Blacklist.Policy.policy.add.success'
                        defaultMessage='Deny Policy added successfully.'
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
    };

    return (
        <FormDialogBase
            title={title}
            saveButtonText='Deny'
            icon={icon}
            triggerButtonText={triggerButtonText}
            formSaveCallback={formSaveCallback}
            className={classes.addForm}
        >
            <DialogContentText>
                <Typography variant='h6'>
                    <FormattedMessage
                        id='Admin.Throttling.Blacklist.Throttling.Policy.add.condition.type'
                        defaultMessage='Condition Type'
                    />
                </Typography>
            </DialogContentText>
            <FormControl component='fieldset' className={classes.addForm}>
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
                                {/* eslint-disable-next-line no-template-curly-in-string */ }
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
                                    {/* eslint-disable-next-line no-template-curly-in-string */}
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
                                    {/* eslint-disable-next-line no-template-curly-in-string */}
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
                                    {/* eslint-disable-next-line no-template-curly-in-string */}
                                    {'Format : ${userName}'}
                                </FormHelperText>
                                <FormHelperText className={classes.helperText}>
                                    Eg : admin
                                </FormHelperText>
                            </>
                        )}
                    />
                )}
                <FormControlLabel
                    control={(
                        <Checkbox
                            checked={conditionStatus}
                            onChange={onChange}
                            name='conditionStatus'
                            label='Enable Condition'
                            color='primary'
                        />
                    )}
                    label={(
                        <FormattedMessage
                            id='Admin.Throttling.Blacklist.policy.enable.condition'
                            defaultMessage='Enable Condition'
                        />
                    )}
                />
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

export default AddEdit;
