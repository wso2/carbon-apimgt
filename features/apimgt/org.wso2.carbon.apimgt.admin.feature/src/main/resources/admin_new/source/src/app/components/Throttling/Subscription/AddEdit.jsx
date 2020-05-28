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
import MenuItem from '@material-ui/core/MenuItem';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Select from '@material-ui/core/Select';
import Switch from '@material-ui/core/Switch';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
// import ConditionalGroups from 'AppComponents/Throttling/Advanced/ConditionalGroups';

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

let initialState = {
    label: '',
    description: '',
    type: 'RequestCountLimit',
    plan: 'Free',
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
function AddEdit() {
    const classes = useStyles();
    const [validating, setValidating] = useState(false);
    const intl = useIntl();

    const id = null;
    useEffect(() => {
        initialState = {
            label: '',
            description: '',
        };
    }, []);

    const [state, dispatch] = useReducer(reducer, initialState);
    const {
        label,
        description,
        type,
        plan,
    } = state;

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };


    const hasErrors = (fieldName, value) => {
        if (!validating) return '';
        let error = false;
        switch (fieldName) {
            case 'label':
                error = value === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const getAllFormErrors = () => {
        let errorText = '';
        const labelErrors = hasErrors('label', label);
        if (labelErrors) {
            errorText += labelErrors + '\n';
        }
        return errorText;
    };

    // eslint-disable-next-line consistent-return
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
        })
            .catch((e) => {
                return (e);
            });
    };

    return (
        <ContentBase
            pageStyle='half'
            title={
                intl.formatMessage({
                    id: 'Throttling.Subscription.AddEdit.title.main',
                    defaultMessage: 'Subscription Throttle Policy - Create New',
                })
            }
        >
            <Box component='div' m={2}>
                <Grid container spacing={2}>
                    <Grid item md={12} lg={6}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Subscription.AddEdit.general.details'
                                defaultMessage='General Details'
                            />
                        </Typography>
                        <Box component='div' m={1}>
                            <TextField
                                autoFocus
                                name='name'
                                value={label}
                                onChange={onChange}
                                label={(
                                    <span>
                                        <FormattedMessage
                                            id='Throttling.Advanced.AddEdit.form.name'
                                            defaultMessage='Name'
                                        />

                                        <span className={classes.error}>*</span>
                                    </span>
                                )}
                                fullWidth
                                error={hasErrors('name', label)}
                                helperText={hasErrors('name', label) || 'Enter a name for policy'}
                                variant='outlined'
                            />
                            <TextField
                                name='description'
                                value={description}
                                onChange={onChange}
                                label='Description'
                                fullWidth
                                multiline
                                helperText={intl.formatMessage({
                                    id: 'Throttling.Subscription.AddEdit.enter.desccription',
                                    defaultMessage: 'Enter description',
                                })}
                                variant='outlined'
                            />

                        </Box>
                        {/* Default limits */}

                        <Box>
                            <Typography color='inherit' variant='subtitle2' component='div'>
                                <FormattedMessage
                                    id='Throttling.Subscription.AddEdit.quota.limits'
                                    defaultMessage='Quota Limits'
                                />
                            </Typography>
                        </Box>
                        <Box display='flex' flexDirection='row' alignItems='left' m={1}>
                            <RadioGroup
                                aria-label='position'
                                name='type'
                                value={type}
                                onChange={onChange}
                                className={classes.radioGroup}
                            >
                                <FormControlLabel value='RequestCountLimit' control={<Radio />} label='Request Count' />
                                <FormControlLabel
                                    value='BandwidthLimit'
                                    control={<Radio />}
                                    label='Request Bandwidth'
                                />
                            </RadioGroup>
                        </Box>
                        {type === 'RequestCountLimit' ? (
                            <TextField

                                name='requestCount'
                                label='Request Count'
                                // value={requestCount}
                                multiline
                                fullWidth
                                type='number'
                                onChange={onChange}
                                variant='outlined'
                                required
                                InputProps={{
                                    id: 'requestCount',
                                    onBlur: ({ target: { value } }) => {
                                        // eslint-disable-next-line no-undef
                                        validate('requestCount', value);
                                    },
                                }}
                            // error={validationError.requestCountValue}
                            />
                        ) : (
                            <Box display='flex' flexDirection='row' alignItems='left' mt={1}>
                                <Box flex='1'>
                                    <TextField
                                        name='dataAmount'
                                        label='Data Bandwith'
                                        multiline
                                        required
                                        type='number'
                                        variant='outlined'
                                        // value={dataAmount}
                                        onChange={onChange}
                                        InputProps={{
                                            id: 'dataAmount',
                                            onBlur: ({ target: { value } }) => {
                                                // eslint-disable-next-line no-undef
                                                validate('dataAmount', value);
                                            },
                                        }}
                                    // error={validationError.dataAmount}
                                    />
                                </Box>
                                <Box flex='1' mt={1}>
                                    <Box>
                                        <Select
                                            labelId='demo-simple-select-label'
                                            name='dataUnit'
                                            // value={dataUnit}
                                            onChange={onChange}
                                            align='center'
                                            variant='outlined'
                                            margin='dense'
                                            fullWidth
                                        >
                                            <MenuItem value='KB'>KB</MenuItem>
                                            <MenuItem value='MB'>MB</MenuItem>
                                        </Select>
                                    </Box>
                                </Box>

                            </Box>
                        )}
                        <Box display='flex' flexDirection='row' alignItems='left' mt={1}>
                            <Box flex='1'>
                                <TextField
                                    name='unitTime'
                                    label='Unit Time'
                                    type='number'
                                    multiline
                                    variant='outlined'
                                    // value={unitTime}
                                    onChange={onChange}
                                    InputProps={{
                                        id: 'unitTime',
                                        onBlur: ({ target: { value } }) => {
                                            // eslint-disable-next-line no-undef
                                            validate('unitTime', value);
                                        },
                                    }}
                                // error={validationError.unitTime}
                                // helperText={validationError.unitTime && 'Unit Time is empty'}
                                />
                            </Box>
                            <Box flex='1' mt={1}>
                                <Select
                                    labelId='demo-simple-select-label'
                                    name='timeUnit'
                                    fullWidth
                                    variant='outlined'
                                    margin='dense'
                                    align='center'
                                    multiline
                                    // value={timeUnit}
                                    onChange={onChange}
                                >
                                    <MenuItem value='min'>Minute(s)</MenuItem>
                                    <MenuItem value='hour'>Hour(s)</MenuItem>
                                    <MenuItem value='day'>Day(s)</MenuItem>
                                    <MenuItem value='week'>Week(s)</MenuItem>
                                    <MenuItem value='month'>Month(s)</MenuItem>
                                    <MenuItem value='year'>Year(s)</MenuItem>
                                </Select>

                            </Box>
                        </Box>
                        <Box component='div' m={1}>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.Policy.Flag'
                                        defaultMessage='Policy Flag'
                                    />
                                </Typography>
                            </Box>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.Billing.Plan'
                                            defaultMessage='Billing Plan'
                                        />
                                    </Typography>
                                </Box>
                                <Box flex='1'>
                                    <RadioGroup
                                        aria-label='position'
                                        name='plan'
                                        value={plan}
                                        onChange={onChange}
                                        className={classes.radioGroup}
                                    >
                                        <FormControlLabel value='Free' control={<Radio />} label='Free' />
                                        <FormControlLabel value='Commercial' control={<Radio />} label='Commercial' />
                                    </RadioGroup>
                                </Box>
                            </Box>
                            {plan === 'Commercial' && (
                                <div>
                                    <Box display='flex' flexDirection='row' alignItems='center' mt={1}>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.Monetization.Plan'
                                                    defaultMessage='Monetization Plan'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <Select
                                                labelId='demo-simple-select-label'
                                                name='timeUnit'
                                                fullWidth
                                                variant='outlined'
                                                align='center'
                                                multiline
                                                // value={timeUnit}
                                                onChange={onChange}
                                            >
                                                <MenuItem value='fixedRate'>Fixed Rate</MenuItem>
                                                <MenuItem value='usage'>Dynamic Usage</MenuItem>

                                            </Select>
                                        </Box>
                                    </Box>

                                    <Box display='flex' flexDirection='row' alignItems='center' mt={1}>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.Fixed.Rate'
                                                    defaultMessage='Fixed Rate'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <TextField
                                                name='description'
                                                value={description}
                                                onChange={onChange}
                                                label='Fixed Rate'
                                                fullWidth
                                                multiline
                                                helperText={intl.formatMessage({
                                                    id: 'Throttling.Subscription.Fixed.Rate.description',
                                                    defaultMessage: 'Fixed Rate',
                                                })}
                                                variant='outlined'
                                            />
                                        </Box>
                                    </Box>

                                    <Box display='flex' flexDirection='row' alignItems='center' mt={1}>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.Currency'
                                                    defaultMessage='Currency'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <TextField

                                                name='description'
                                                value={description}
                                                onChange={onChange}
                                                label='Currency'
                                                fullWidth
                                                multiline
                                                helperText={intl.formatMessage({
                                                    id: 'Throttling.Subscription.Currency.description',
                                                    defaultMessage: 'Currency',
                                                })}
                                                variant='outlined'
                                            />
                                        </Box>
                                    </Box>

                                    <Box display='flex' flexDirection='row' alignItems='center' mt={1}>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.Billing.Cycle'
                                                    defaultMessage='Billing Cycle'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <Select
                                                labelId='demo-simple-select-label'
                                                name='timeUnit'
                                                fullWidth
                                                variant='outlined'
                                                align='center'
                                                multiline
                                                // value={timeUnit}
                                                onChange={onChange}
                                            >
                                                <MenuItem value='week'>Week</MenuItem>
                                                <MenuItem value='month'>Month</MenuItem>
                                                <MenuItem value='year'>Year</MenuItem>
                                            </Select>
                                        </Box>
                                    </Box>
                                </div>
                            )}
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.Stop.quota.reach'
                                            defaultMessage='Stop On Quota Reach'
                                        />
                                    </Typography>
                                </Box>

                                <Box flex='1'>
                                    <Switch
                                        onChange={onchange}
                                        color='primary'
                                        name='checkedB'
                                        inputProps={{ 'aria-label': 'primary checkbox' }}
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Grid>
                    <Box component='div' m={1}>
                        <Box flex='1'>
                            <Typography color='inherit' variant='subtitle2' component='div'>
                                <FormattedMessage
                                    id='Throttling.Subscription.Custom.Control'
                                    defaultMessage='Custom Attribute'
                                />
                            </Typography>
                        </Box>

                        <Grid item xs={12}>
                            <Button variant='outlined'>
                                <FormattedMessage
                                    id='Throttling.Subscription.Properties.add'
                                    defaultMessage='Add Attribute'
                                />
                            </Button>

                            <Table className={classes.table}>

                                <TableBody>

                                    <>
                                        <TableRow>
                                            <TableCell>
                                                <TextField
                                                    fullWidth
                                                    required
                                                    id='outlined-required'
                                                    label={intl.formatMessage({
                                                        id: `Throttling.Subscription.Properties.Properties.
                                                                show.add.property.property.name`,
                                                        defaultMessage: 'Name',
                                                    })}
                                                    margin='merge'
                                                    variant='outlined'
                                                    className={classes.addProperty}

                                                />
                                            </TableCell>
                                            <TableCell>
                                                <TextField
                                                    fullWidth
                                                    required
                                                    id='outlined-required'
                                                    label={intl.formatMessage({
                                                        // eslint-disable-next-line max-len
                                                        id: 'Throttling.Subscription.Properties.Properties.property.value',
                                                        defaultMessage: 'Value',
                                                    })}
                                                    margin='merge'
                                                    variant='outlined'

                                                />
                                            </TableCell>
                                            <TableCell align='right'>
                                                <Button
                                                    variant='outlined'

                                                >
                                                    <FormattedMessage
                                                        id='Throttling.Subscription.Properties.Properties.add'
                                                        defaultMessage='Delete'
                                                    />
                                                </Button>


                                            </TableCell>
                                        </TableRow>
                                        <TableRow>

                                            <Typography variant='caption'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.Properties.Properties.help'
                                                    defaultMessage='Property name should be unique.'
                                                />
                                            </Typography>

                                        </TableRow>
                                    </>


                                </TableBody>
                            </Table>

                        </Grid>
                    </Box>

                    <Grid item md={12} lg={6}>

                        <Box component='div' m={1}>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.Permissions'
                                        defaultMessage='Permissions'
                                    />
                                </Typography>
                            </Box>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.Roles'
                                            defaultMessage='Roles'
                                        />
                                    </Typography>
                                </Box>
                                <Box flex='2'>
                                    <TextField

                                        name='description'
                                        value={description}
                                        onChange={onChange}
                                        label='Permission'
                                        fullWidth
                                        multiline
                                        helperText={intl.formatMessage({
                                            id: 'Throttling.Subscription.enter.permission',
                                            defaultMessage: 'Enter Permission',
                                        })}
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                            <RadioGroup
                                aria-label='gender'
                                name='gender1'
                                value={description}
                                onChange={onChange}
                                className={classes.radioGroup}
                            >
                                <FormControlLabel value='female' control={<Radio />} label='Allow' />
                                <FormControlLabel value='male' control={<Radio />} label='Denied' />
                            </RadioGroup>
                        </Box>
                        <Box component='div' m={1}>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.GraphQL'
                                        defaultMessage='GraphQL'
                                    />
                                </Typography>
                            </Box>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.Max.Complexity'
                                            defaultMessage='Max Complexity'
                                        />
                                    </Typography>
                                </Box>
                                <Box flex='2'>
                                    <TextField

                                        name='description'
                                        value={description}
                                        onChange={onChange}
                                        label='Max Complexity'
                                        fullWidth
                                        multiline
                                        helperText={intl.formatMessage({
                                            id: 'Throttling.Subscription.Max.Complexity.desccription',
                                            defaultMessage: 'Max Complexity',
                                        })}
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.Max.Depth'
                                            defaultMessage='Max Depth'
                                        />
                                    </Typography>
                                </Box>
                                <Box flex='2'>
                                    <TextField

                                        name='description'
                                        value={description}
                                        onChange={onChange}
                                        label='Max Depth'
                                        fullWidth
                                        multiline
                                        helperText={intl.formatMessage({
                                            id: 'Throttling.Subscription.Max.Depth.desccription',
                                            defaultMessage: 'Max Depth',
                                        })}
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Grid>

                </Grid>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        {/* Submit buttons */}
                        <Box component='span' m={1}>
                            <Button variant='contained' color='primary' onClick={formSave}>
                                <FormattedMessage
                                    id='Throttling.Subscription.AddEdit.form.add'
                                    defaultMessage='Add'
                                />
                            </Button>
                        </Box>
                        <RouterLink to='/throttling/advanced'>
                            <Button variant='contained' onClick={formSave}>
                                <FormattedMessage
                                    id='Throttling.Subscription.AddEdit.form.cancel'
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
