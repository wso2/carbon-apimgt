/* eslint-disable no-useless-escape */
/* eslint-disable react/prop-types */
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

import React, { useState, useReducer } from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import { useIntl, FormattedMessage } from 'react-intl';
import DialogContentText from '@material-ui/core/DialogContentText';
import FormDialogBase from 'AppComponents/AdminPages/Addons/FormDialogBase';
import TextField from '@material-ui/core/TextField';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Box from '@material-ui/core/Box';
import FormControl from '@material-ui/core/FormControl';
import cloneDeep from 'lodash.clonedeep';
import InputLabel from '@material-ui/core/InputLabel';
import CreateIcon from '@material-ui/icons/Create';

/**
 * validate ip address
 * @param {string} ip address
 * @returns {boolean}.
 */
function validateIPAddress(ip) {
    const expression = new RegExp('((^\s*((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.)'
    + '{3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))\s*$)|(^\s*((([0-9A-Fa-f]{1,4}:){7}'
    + '([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|'
    + '[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]'
    + '{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))'
    + '|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|'
    + '1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:'
    + '[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]'
    + '|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:'
    + '[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d))'
    + '{3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]'
    + '|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4})'
    + '{1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|'
    + '[1-9]?\d)){3}))|:)))(%.+)?\s*$))');

    if (expression.test(ip)) {
        return true;
    } else {
        return false;
    }
}
/**
 * check if valid ip range
 * @param {string} startIP start address
 * @param {string} endIP end address
 * @returns {boolean}.
 */
function validateIPRange(startIP, endIP) {
    if (startIP === null || endIP === null || startIP === '' || endIP === '') {
        return false;
    }
    if (startIP.includes('.') && endIP.includes('.')) {
        const startIPBlocks = startIP.split('.');
        const endIPBlocks = endIP.split('.');
        let startIp = 0;
        let endIp = 0;
        for (let i = 0; i < 4; i++) {
            startIp += startIPBlocks[i] * 256 ** (3 - i);
        }
        for (let i = 0; i < 4; i++) {
            endIp += endIPBlocks[i] * 256 ** (3 - i);
        }
        if (startIp < endIp) {
            return true;
        }
        return false;
    } else if (startIP.includes(':') && endIP.includes(':')) {
        // convert the short hand ip to full address. IPv6 has 7 semicolons (:)
        const startIPBlocks = startIP.split(':');
        const endIPBlocks = endIP.split(':');

        // Check from the begining which is greater. Go through 8 blocks
        let valid = true;
        for (let i = 0; i < 7; i++) {
            if (startIPBlocks[i] > endIPBlocks[i]) {
                valid = false;
                break;
            }
        }
        return valid;
    } else {
        return false;
    }
}

/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    const nextState = cloneDeep(state);
    switch (field) {
        case 'all': // We set initial state with this.
            return value;
        case 'ipConditionType':
        case 'specificIP':
        case 'startingIP':
        case 'endingIP':
            nextState.ipCondition[field] = value;
            return nextState;
        default:
            return nextState;
    }
}

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    formControlSelect: {
        width: '100%',
        marginBottom: theme.spacing(1),
    },
    outlined: {
        padding: '11.5px 14px',
    },
    labelRoot: {
        position: 'relative',
        marginTop: theme.spacing(1.5),
    },
    textFieldLeft: {
        paddingRight: 10,
    },
}));

/**
 * Render the ip condition add edit form.
 * @param {JSON} props component props.
 * @returns {JSX} Loading animation.
 */
function AddEditConditionPolicyIP(props) {
    const intl = useIntl();
    const classes = useStyles();
    const { row, callBack, item } = props;

    const {
        row: {
            description,
        },
    } = props;

    const disabled = row.items.length === 1;
    const [validating, setValidating] = useState(false);
    let initialState = {
        type: 'IPCONDITION',
        invertCondition: false,
        headerCondition: null,
        ipCondition: {
            ipConditionType: 'IPSPECIFIC',
            specificIP: '',
            startingIP: null,
            endingIP: null,
        },
        jwtClaimsCondition: null,
        queryParameterCondition: null,
    };


    if (item) {
        initialState = { ...item };
    }
    const [state, dispatch] = useReducer(reducer, initialState);
    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    const {
        ipCondition: {
            ipConditionType, specificIP, startingIP, endingIP,
        },
    } = state;
    const hasErrors = (fieldName, value, validatingActive = false) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'specificIP':
                if (value === '') {
                    error = `${fieldName}${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEditConditionPolicyIP.is.empty.error',
                        defaultMessage: ' is empty',
                    })}`;
                } else if (value !== '' && !validateIPAddress(value)) {
                    error = `${fieldName}${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEditConditionPolicyIP.valid.ip.address.error',
                        defaultMessage: 'Invalid IP Addresss',
                    })}`;
                }
                break;
            case 'startingIP':
            case 'endingIP':
                if (value === '') {
                    error = `${fieldName}${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEditConditionPolicyIP.is.empty.error',
                        defaultMessage: ' is empty',
                    })}`;
                } else if (value !== '' && !validateIPRange(startingIP, endingIP)) {
                    error = `${fieldName}${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEditConditionPolicyIP.valid.ip.range.error',
                        defaultMessage: 'Invalid IP Range',
                    })}`;
                }
                break;
            default:
                break;
        }
        return error;
    };

    const formHasErrors = (validatingActive = false) => {
        if ((hasErrors('specificIP', specificIP, validatingActive) && ipConditionType === 'IPSPECIFIC')
        || (hasErrors('endingIP', endingIP, validatingActive) && ipConditionType === 'IPRANGE')
        || (hasErrors('startingIP', startingIP, validatingActive) && ipConditionType === 'IPRANGE')
        ) {
            return true;
        } else {
            return false;
        }
    };
    const formSaveCallback = () => {
        setValidating(true);
        if (!formHasErrors(true)) {
            return ((setOpen) => {
                callBack({
                    ipConditionType, specificIP, startingIP, endingIP,
                }, item);
                setOpen(false);
            });
        }
        return false;
    };
    return (
        <FormDialogBase
            title={item
                ? intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicyIP.dialog.tilte.add.new',
                    defaultMessage: 'Add New IP Condition Policy',
                })
                : intl.formatMessage({
                    id: 'Throttling.Advanced.AddEditConditionPolicy.dialog.tilte.edit',
                    defaultMessage: 'Edit IP Condition Policy',
                })}
            saveButtonText={intl.formatMessage({
                id: 'Throttling.Advanced.AddEditConditionPolicyIP.dialog.btn.save',
                defaultMessage: 'Save',
            })}
            triggerButtonText={item ? null : intl.formatMessage({
                id: 'Throttling.Advanced.AddEditConditionPolicyIP.dialog.trigger.add',
                defaultMessage: 'Add',
            })}
            icon={item ? <CreateIcon /> : null}
            triggerButtonProps={{
                color: 'default',
                variant: 'contained',
                size: 'small',
                disabled,
            }}
            formSaveCallback={formSaveCallback}
        >
            <DialogContentText>
                {description}
            </DialogContentText>
            <FormControl variant='outlined' className={classes.formControlSelect}>
                <Select
                    name='ipConditionType'
                    value={ipConditionType}
                    onChange={onChange}
                    classes={{ outlined: classes.outlined }}
                >
                    <MenuItem value='IPSPECIFIC'>
                        <FormattedMessage
                            id='Throttling.Advanced.AddEditConditionPolicyIP.specific.ip'
                            defaultMessage='Specific IP'
                        />
                    </MenuItem>
                    <MenuItem value='IPRANGE'>
                        <FormattedMessage
                            id='Throttling.Advanced.AddEditConditionPolicyIP.ip.range'
                            defaultMessage='IP Range'
                        />
                    </MenuItem>
                </Select>
                <InputLabel classes={{ root: classes.labelRoot }}>
                    <FormattedMessage
                        id='Throttling.Advanced.AddEditConditionPolicyIP.ip.condition.type'
                        defaultMessage='IP Condition Type'
                    />
                </InputLabel>
            </FormControl>
            {ipConditionType === 'IPSPECIFIC' ? (
                <Box display='flex' flexDirection='column'>
                    <TextField
                        margin='dense'
                        name='specificIP'
                        value={specificIP}
                        onChange={onChange}
                        label={(
                            <span>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEditConditionPolicyIP.form.specific.ip'
                                    defaultMessage='Specific IP'
                                />
                                <span className={classes.error}>*</span>
                            </span>
                        )}
                        fullWidth
                        helperText={hasErrors('specificIP', specificIP, validating) || intl.formatMessage({
                            id: 'Throttling.Advanced.AddEditConditionPolicyIP.form.specific.ip.help',
                            defaultMessage: 'Provide Valid IP',
                        })}
                        variant='outlined'
                        error={hasErrors('specificIP', specificIP, validating)}
                    />
                </Box>
            ) : (
                <Box display='flex' flexDirection='row'>
                    <TextField
                        className={classes.textFieldLeft}
                        margin='dense'
                        name='startingIP'
                        value={startingIP || ''}
                        onChange={onChange}
                        label={(
                            <span>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEditConditionPolicyIP.form.start.ip'
                                    defaultMessage='Start IP'
                                />
                                <span className={classes.error}>*</span>
                            </span>
                        )}
                        fullWidth
                        helperText={hasErrors('specificIP', specificIP, validating) || intl.formatMessage({
                            id: 'Throttling.Advanced.AddEditConditionPolicyIP.form.start.ip.help',
                            defaultMessage: 'Provide Valid IP',
                        })}
                        variant='outlined'
                        error={hasErrors('startingIP', startingIP, validating)}
                    />
                    <TextField
                        margin='dense'
                        name='endingIP'
                        value={endingIP || ''}
                        onChange={onChange}
                        label={(
                            <span>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEditConditionPolicyIP.form.end.ip'
                                    defaultMessage='End IP'
                                />
                                <span className={classes.error}>*</span>
                            </span>
                        )}
                        fullWidth
                        helperText={hasErrors('endingIP', specificIP, validating) || intl.formatMessage({
                            id: 'Throttling.Advanced.AddEditConditionPolicyIP.form.end.ip.help',
                            defaultMessage: 'Provide Valid IP',
                        })}
                        variant='outlined'
                        error={hasErrors('endingIP', endingIP, validating)}
                    />
                </Box>
            )}
        </FormDialogBase>
    );
}
AddEditConditionPolicyIP.propTypes = {
    dataRow: PropTypes.shape({
        id: PropTypes.number.isRequired,
    }).isRequired,
};
export default AddEditConditionPolicyIP;
