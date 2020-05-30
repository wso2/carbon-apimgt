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
import { useIntl, FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import { Link as RouterLink } from 'react-router-dom';
import Grid from '@material-ui/core/Grid';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import ConditionalGroup from 'AppComponents/Throttling/Advanced/ConditionalGroup';
import cloneDeep from 'lodash.clonedeep';
import HelpLinks from 'AppComponents/Throttling/Advanced/HelpLinks';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    formTitle: {
        paddingBottom: theme.spacing(4),
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
    },
    slectRoot: {
        padding: '11.5px 14px',
        width: 100,
    },
    formControlSelect: {
        paddingTop: 7,
        paddingLeft: 5,
    },
}));


/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    return new Promise(((resolve) => {
        setTimeout(() => { resolve('Successfully did something'); }, 2000);
    }));
}


/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}.
 */
function reducer(state, { field, value }) {
    const nextState = cloneDeep(state);
    switch (field) {
        case 'policyName':
        case 'policyDescription':
        case 'executionFlows':
            nextState[field] = value;
            return nextState;
        case 'type':
            nextState.defaultQuotaPolicy[field] = value;
            return nextState;
        case 'dataUnit':
        case 'requestCount':
        case 'timeUnit':
        case 'dataAmount':
        case 'unitTime':
            nextState.defaultQuotaPolicy.limit[field] = value;
            return nextState;
        default:
            return state;
    }
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
function AddEdit() {
    const classes = useStyles();
    const [validating, setValidating] = useState(false);
    const intl = useIntl();

    const id = '8888lsls'; // This should be null
    let initialState = {
        policyName: '',
        policyDescription: '',
        executionFlows: [],
        defaultQuotaPolicy: {
            type: 'requestCount',
            limit: {
                requestCount: '',
                timeUnit: 'min',
                dataAmount: 0,
                dataUnit: 'KB',
                unitTime: '',
            },
        },
    };
    if (id) {
        initialState = {
            policyName: 'test1',
            policyDescription: 'testdesc',
            executionFlows: [
                {
                    id: 0,
                    enabled: true,
                    description: 'Sample description about condition group222',
                    quotaPolicy: {
                        type: 'requestCount',
                        limit: {
                            requestCount: '0',
                            timeUnit: 'min',
                            dataAmount: 0,
                            dataUnit: '',
                            unitTime: '1',
                        },
                    },
                    conditions: [
                        {
                            type: 'IP',
                            ipType: 'specific',
                            startingIP: '',
                            endingIP: '',
                            specificIP: '10.100.22.33',
                            invertCondition: true,
                            enabled: true,
                        },
                        {
                            type: 'Header',
                            keyValPairs: [
                                {
                                    name: 'header1',
                                    value: 'param1',
                                },
                                {
                                    name: 'header2',
                                    value: 'param2',
                                },
                            ],
                            invertCondition: true,
                            enabled: true,
                        },
                        {
                            type: 'QueryParam',
                            keyValPairs: [
                                {
                                    name: 'header1',
                                    value: 'param1',
                                },
                            ],
                            invertCondition: false,
                            enabled: true,
                        },
                        {
                            type: 'JWTClaim',
                            keyValPairs: [
                                {
                                    name: 'header1',
                                    value: 'param1',
                                },
                            ],
                            invertCondition: true,
                            hasValues: false,
                            enabled: true,
                        },
                    ],
                },
            ],
            defaultQuotaPolicy: {
                type: 'requestCount',
                limit: {
                    requestCount: '23',
                    timeUnit: 'min',
                    dataAmount: 0,
                    dataUnit: '',
                    unitTime: '1222',
                },
            },
        };
    }


    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        policyName,
        policyDescription,
        defaultQuotaPolicy: {
            type, limit: {
                requestCount,
                timeUnit,
                dataAmount,
                dataUnit,
                unitTime,
            },
        },
        executionFlows,
    } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    const setExecutionFlow = (updatedExecutionFlow) => {
        const newExecutionFlows = cloneDeep(executionFlows);
        let flowId = null;
        newExecutionFlows.forEach((flow, index) => {
            if (flow.id === updatedExecutionFlow.id) {
                flowId = index;
            }
        });
        if (flowId) {
            newExecutionFlows[flowId] = updatedExecutionFlow;
        } else {
            newExecutionFlows.push(updatedExecutionFlow);
        }

        dispatch({ field: 'executionFlows', value: newExecutionFlows });
    };
    const hasErrors = (fieldName, value) => {
        if (!validating) return '';
        let error = false;
        switch (fieldName) {
            case 'policyName':
                error = value === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const getAllFormErrors = () => {
        let errorText = '';
        const policyNameErrors = hasErrors('policyName', policyName);
        if (policyNameErrors) {
            errorText += policyNameErrors + '\n';
        }
        return errorText;
    };

    const formSave = () => {
        setValidating(true);
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return (false);
        }
        // Do the API call
        const promiseAPICall = apiCall();
        if (id) {
        // assign the update promise to the promiseAPICall
        }
        promiseAPICall.then((data) => {
            console.info(data);
        });
        return true;
    };

    return (

        <ContentBase
            pageStyle='half'
            title={
                intl.formatMessage({
                    id: 'Throttling.Advanced.AddEdit.title.main',
                    defaultMessage: 'Advanced Throttle Policy - Create New',
                })
            }
            help={<HelpLinks />}
        >
            <Box component='div' m={2}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={12} lg={6}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.general.details'
                                defaultMessage='General Details'
                            />
                        </Typography>
                        <Box component='div' m={1}>
                            <TextField
                                autoFocus
                                margin='dense'
                                name='policyName'
                                value={policyName}
                                onChange={onChange}
                                label={(
                                    <span>
                                        <FormattedMessage
                                            id='Throttling.Advanced.AddEdit.form.policyName'
                                            defaultMessage='Name'
                                        />

                                        <span className={classes.error}>*</span>
                                    </span>
                                )}
                                fullWidth
                                error={hasErrors('policyName', policyName)}
                                helperText={hasErrors('policyName', policyName) || intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.name.help',
                                    defaultMessage: 'Name of the throttle policy.',
                                })}
                                variant='outlined'
                            />
                            <TextField
                                margin='dense'
                                name='policyDescription'
                                value={policyDescription}
                                onChange={onChange}
                                label={intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.description',
                                    defaultMessage: 'Description',
                                })}
                                fullWidth
                                multiline
                                helperText={intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.description.help',
                                    defaultMessage: 'Description of the throttle policy.',
                                })}
                                variant='outlined'
                            />
                        </Box>
                        {/* Default limits */}
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Advanced.AddEdit.default.limits'
                                        defaultMessage='Default Limits'
                                    />
                                </Typography>
                            </Box>

                            <RadioGroup
                                aria-label='Default Limits'
                                name='type'
                                value={type}
                                onChange={onChange}
                                className={classes.radioGroup}
                            >
                                <FormControlLabel
                                    value='requestCount'
                                    control={<Radio />}
                                    label='Request Count'
                                />
                                <FormControlLabel
                                    value='bandwidthVolume'
                                    control={<Radio />}
                                    label='Request Bandwidth'
                                />
                            </RadioGroup>
                        </Box>

                        <Box component='div' m={1}>
                            {type === 'requestCount' && (
                                <TextField
                                    autoFocus
                                    margin='dense'
                                    name='requestCount'
                                    value={requestCount}
                                    onChange={onChange}
                                    label={(
                                        <FormattedMessage
                                            id='Throttling.Advanced.AddEdit.form.requestCount.name'
                                            defaultMessage='Name'
                                        />
                                    )}
                                    fullWidth
                                    error={hasErrors('name', requestCount)}
                                    helperText={hasErrors('name', requestCount) || 'Number of requests allowed'}
                                    variant='outlined'
                                />
                            )}
                            {type === 'bandwidthVolume' && (
                                <Box display='flex' flexDirection='row'>
                                    <TextField
                                        autoFocus
                                        margin='dense'
                                        name='dataAmount'
                                        value={dataAmount}
                                        onChange={onChange}
                                        label={(
                                            <FormattedMessage
                                                id='Throttling.Advanced.AddEdit.form.dataAmount.name'
                                                defaultMessage='Data Bandwidth'
                                            />
                                        )}
                                        fullWidth
                                        error={hasErrors('name', dataAmount)}
                                        helperText={hasErrors('name', dataAmount) || 'Bandwidth allowed'}
                                        variant='outlined'
                                    />
                                    <FormControl variant='outlined' className={classes.formControlSelect}>
                                        <Select
                                            name='dataUnit'
                                            value={dataUnit}
                                            onChange={onChange}
                                            classes={{ root: classes.slectRoot }}
                                        >
                                            <MenuItem value='KB'>KB</MenuItem>
                                            <MenuItem value='MB'>MB</MenuItem>
                                        </Select>
                                    </FormControl>
                                </Box>
                            )}
                            <Box display='flex' flexDirection='row'>
                                <TextField
                                    margin='dense'
                                    name='unitTime'
                                    value={unitTime}
                                    onChange={onChange}
                                    label='Unit Time'
                                    fullWidth
                                    multiline
                                    helperText={intl.formatMessage({
                                        id: 'Throttling.Advanced.AddEdit.unitTime',
                                        defaultMessage: 'Unit Time',
                                    })}
                                    variant='outlined'
                                />
                                <FormControl variant='outlined' className={classes.formControlSelect}>
                                    <Select
                                        name='timeUnit'
                                        value={timeUnit}
                                        onChange={onChange}
                                        classes={{ root: classes.slectRoot }}
                                    >
                                        <MenuItem value='sec'>Second(s)</MenuItem>
                                        <MenuItem value='min'>Minutes(s)</MenuItem>
                                        <MenuItem value='hour'>Hour(s)</MenuItem>
                                        <MenuItem value='days'>Day(s)</MenuItem>
                                        <MenuItem value='month'>Month(s)</MenuItem>
                                        <MenuItem value='year'>Year(s)</MenuItem>
                                    </Select>
                                </FormControl>
                            </Box>
                        </Box>
                        {/* Conditional groups */}
                    </Grid>
                </Grid>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Advanced.AddEdit.add.conditional.group'
                                        defaultMessage='Conditional groups'
                                    />
                                </Typography>
                            </Box>

                            <Button variant='contained'>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.add.conditional.group.add'
                                    defaultMessage='Add Conditional Group'
                                />
                            </Button>

                        </Box>

                        <Box component='div' m={1}>
                            {executionFlows.map((executionFlow) => (
                                <ConditionalGroup
                                    key={executionFlow.id}
                                    executionFlow={cloneDeep(executionFlow)}
                                    setExecutionFlow={setExecutionFlow}
                                />
                            ))}
                        </Box>
                        {/* Submit buttons */}
                        <Box m={4} />
                        <Box component='span' m={1}>
                            <Button variant='contained' color='primary' onClick={formSave}>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.add'
                                    defaultMessage='Add'
                                />
                            </Button>
                        </Box>
                        <RouterLink to='/throttling/advanced'>
                            <Button variant='contained' onClick={formSave}>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.cancel'
                                    defaultMessage='Cancel'
                                />
                            </Button>
                        </RouterLink>
                    </Grid>
                </Grid>
            </Box>
        </ContentBase>
    );
}

AddEdit.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};


export default AddEdit;
