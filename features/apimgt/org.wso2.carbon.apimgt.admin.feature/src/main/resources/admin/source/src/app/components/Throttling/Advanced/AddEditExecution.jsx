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
import React from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import { useIntl, FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import FormLabel from '@material-ui/core/FormLabel';

const useStyles = makeStyles((theme) => ({
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
    defaultLimitLabel: {
        marginLeft: theme.spacing(1),
    },
}));
/**
 * Render the execution polcy section and default limits.
 * @returns {JSX} Returns form component.
 * @param {JSON} props Props passed from other components.
 */
function AddEditExecution(props) {
    const intl = useIntl();
    const classes = useStyles();

    const {
        onChange, updateGroup, hasErrors, limit, validating,
    } = props;
    const { requestCount, bandwidth } = limit;
    let timeUnit = '';
    let unitTime = '';
    let limitOption = '';
    if (requestCount) {
        timeUnit = requestCount.timeUnit;
        unitTime = requestCount.unitTime;
        limitOption = 'REQUESTCOUNTLIMIT';
    } else {
        timeUnit = bandwidth.timeUnit;
        unitTime = bandwidth.unitTime;
        limitOption = 'BANDWIDTHLIMIT';
    }
    const update = (e) => {
        if (onChange) {
            onChange(e);
            return;
        }
        const field = e.target.name;
        const { value } = e.target;
        /*
        We are preping the payload for following two cases.
        ======== 1 =========
        {
            "type": "BANDWIDTHLIMIT",
            "requestCount": null,
            "bandwidth": {
            "timeUnit": "min",
            "unitTime": 1,
            "dataAmount": 1,
            "dataUnit": "KB"
            }
        }
        ======== 2 =========
        {
            "type": "REQUESTCOUNTLIMIT",
            "requestCount": {
            "timeUnit": "min",
            "unitTime": 1,
            "requestCount": 5
            },
            "bandwidth": null
        }
        */
        if (field === 'defaultLimit') {
        // Handling the radio buttons.
            if (value === 'REQUESTCOUNTLIMIT') {
                const { timeUnit: bandwidthTimeUnit, unitTime: bandwidthUnitTime } = bandwidth;
                limit.requestCount = {
                    timeUnit: bandwidthTimeUnit, unitTime: bandwidthUnitTime, requestCount: 0,
                };
                limit.bandwidth = null;
                limit.type = 'REQUESTCOUNTLIMIT';
            } else {
                const { timeUnit: requestCountTimeUnit, unitTime: requestCountUnitTime } = requestCount;
                limit.bandwidth = {
                    timeUnit: requestCountTimeUnit, unitTime: requestCountUnitTime, dataAmount: 0, dataUnit: 'KB',
                };
                limit.requestCount = null;
                limit.type = 'BANDWIDTHLIMIT';
            }
        } else if (requestCount) {
            requestCount[field] = value;
        } else {
            bandwidth[field] = value;
        }
        updateGroup();
    };
    return (
        <>
            <Box display='flex' flexDirection='row' alignItems='center'>
                <Box flex='1' className={classes.defaultLimitLabel}>
                    <FormLabel component='legend'>
                        <FormattedMessage
                            id='Throttling.Advanced.AddEditExecution.default.limit.option'
                            defaultMessage='Default Limit Option'
                        />
                    </FormLabel>
                </Box>
                <RadioGroup
                    aria-label='Default Limits'
                    name='defaultLimit'
                    value={limitOption}
                    onChange={update}
                    className={classes.radioGroup}
                >
                    <FormControlLabel
                        value='REQUESTCOUNTLIMIT'
                        control={<Radio />}
                        label='Request Count'
                    />
                    <FormControlLabel
                        value='BANDWIDTHLIMIT'
                        control={<Radio />}
                        label='Request Bandwidth'
                    />
                </RadioGroup>
            </Box>

            <Box component='div' m={1}>
                {requestCount && (
                    <TextField
                        margin='dense'
                        name='requestCount'
                        value={requestCount.requestCount}
                        onChange={update}
                        label={(
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.form.requestCount.label'
                                defaultMessage='Request Count'
                            />
                        )}
                        fullWidth
                        type='number'
                        error={hasErrors('requestCount', requestCount.requestCount, validating)}
                        helperText={hasErrors('requestCount', requestCount.requestCount, validating)
                || 'Number of requests allowed'}
                        variant='outlined'
                    />
                )}
                {bandwidth && (
                    <Box display='flex' flexDirection='row'>
                        <TextField
                            margin='dense'
                            name='dataAmount'
                            value={bandwidth.dataAmount}
                            onChange={update}
                            type='number'
                            label={(
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.dataAmount.label'
                                    defaultMessage='Data Bandwidth'
                                />
                            )}
                            fullWidth
                            error={hasErrors('dataAmount', bandwidth.dataAmount, validating)}
                            helperText={hasErrors('dataAmount', bandwidth.dataAmount, validating)
                            || intl.formatMessage({
                                id: 'Throttling.Advanced.AddEdit.form.bandwidth.allowed.help',
                                defaultMessage: 'Bandwidth allowed',
                            })}
                            variant='outlined'
                        />
                        <FormControl variant='outlined' className={classes.formControlSelect}>
                            <Select
                                name='dataUnit'
                                value={bandwidth.dataUnit}
                                onChange={update}
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
                        onChange={update}
                        type='number'
                        label={(
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.form.unit.time.label'
                                defaultMessage='Unit Time'
                            />
                        )}
                        fullWidth
                        error={hasErrors('unitTime', unitTime, validating)}
                        helperText={hasErrors('unitTime', unitTime, validating) || intl.formatMessage({
                            id: 'Throttling.Advanced.AddEdit.form.unit.time.help',
                            defaultMessage: 'Time configuration',
                        })}
                        variant='outlined'
                    />
                    <FormControl variant='outlined' className={classes.formControlSelect}>
                        <Select
                            name='timeUnit'
                            value={timeUnit}
                            onChange={update}
                            classes={{ root: classes.slectRoot }}
                        >
                            <MenuItem value='min'>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.timeUnit.minute'
                                    defaultMessage='Minute(s)'
                                />
                            </MenuItem>
                            <MenuItem value='hour'>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.timeUnit.hour'
                                    defaultMessage='Hour(s)'
                                />
                            </MenuItem>
                            <MenuItem value='days'>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.timeUnit.day'
                                    defaultMessage='Day(s)'
                                />
                            </MenuItem>
                            <MenuItem value='month'>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.form.timeUnit.month'
                                    defaultMessage='Month(s)'
                                />
                            </MenuItem>
                        </Select>
                    </FormControl>
                </Box>
            </Box>
        </>
    );
}
AddEditExecution.defaultProps = {
    limit: null,
    validating: false,
};
AddEditExecution.propTypes = {
    onChange: PropTypes.func.isRequired,
    updateGroup: PropTypes.func.isRequired,
    hasErrors: PropTypes.func.isRequired,
    limit: PropTypes.shape({}),
    validating: PropTypes.bool,
    classes: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({}).isRequired,
};


export default AddEditExecution;
