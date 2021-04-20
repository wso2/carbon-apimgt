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

import React, { useReducer, useState, useEffect } from 'react';
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
import Switch from '@material-ui/core/Switch';
import HelpOutlinedIcon from '@material-ui/icons/HelpOutlined';
import CircularProgress from '@material-ui/core/CircularProgress';
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import AddCircle from '@material-ui/icons/AddCircle';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import API from 'AppData/api';
import Joi from '@hapi/joi';
import ChipInput from 'material-ui-chip-input';
import base64url from 'base64url';
import Error from '@material-ui/icons/Error';
import InputAdornment from '@material-ui/core/InputAdornment';
import Chip from '@material-ui/core/Chip';
import { red } from '@material-ui/core/colors/';

const useStyles = makeStyles((theme) => ({
    root: {
        marginBottom: theme.spacing(10),
    },
    error: {
        color: theme.palette.error.dark,
    },
    formTitle: {
        paddingBottom: theme.spacing(4),
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
    },
    radioGroupBilling: {
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'center',
        alignItems: 'center',
    },
    selectRoot: {
        padding: '11.5px 14px',
        width: 100,
    },
    formControlSelect: {
        paddingTop: 7,
        paddingLeft: 5,
    },
    formControlSelectBottom: {
        display: 'flex',
        paddingTop: 7,
        paddingBottom: 7,
    },
    toggleSwitchPadding: {
        paddingTop: 10,
        paddingBottom: 7,
    },
    buttonIcon: {
        marginRight: theme.spacing(1),
    },
    paddingTooltip: {
        paddingLeft: 5,
    },
}));

/**
 * Reducer
 * @param {JSON} state The second number.
 * @returns {Promise}.
 */
function reducer(state, newValue) {
    const { field, value } = newValue;
    switch (field) {
        case 'policyName':
        case 'description':
        case 'rateLimitCount':
        case 'rateLimitTimeUnit':
        case 'subscriberCount':
        case 'billingPlan':
        case 'stopOnQuotaReach':
            return { ...state, [field]: value };
        case 'type':
        case 'dataUnit':
        case 'requestCount':
        case 'timeUnit':
        case 'dataAmount':
        case 'unitTime':
        case 'eventCount':
            return {
                ...state,
                defaultLimit: { ...state.defaultLimit, [field]: value },
            };
        case 'monetizationPlan':
        case 'fixedPrice':
        case 'pricePerRequest':
        case 'currencyType':
        case 'billingCycle':
            return {
                ...state,
                monetization: { ...state.monetization, [field]: value },
            };
        case 'roles':
        case 'permissionStatus':
            return {
                ...state,
                permissions: { ...state.permissions, [field]: value },
            };
        case 'maxComplexity':
        case 'maxDepth':
            return {
                ...state,
                graphQL: { ...state.graphQL, [field]: value },
            };
        case 'editDetails':
            return value;
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
    const intl = useIntl();
    const [saving, setSaving] = useState(false);
    const { history, match: { params } } = props;
    const restApi = new API();
    const isEdit = (params.id !== null) && (params.id !== undefined);
    const [validRoles, setValidRoles] = useState([]);
    const [invalidRoles, setInvalidRoles] = useState([]);
    const [roleValidity, setRoleValidity] = useState(true);

    const [initialState, setInitialState] = useState({
        policyName: '',
        description: '',
        defaultLimit: {
            requestCount: '',
            timeUnit: 'min',
            unitTime: '',
            type: 'REQUESTCOUNTLIMIT',
            dataAmount: '',
            dataUnit: 'KB',
            eventCount: '',
        },
        rateLimitCount: '',
        rateLimitTimeUnit: 'sec',
        billingPlan: 'FREE',
        monetization: {
            monetizationPlan: 'FIXEDRATE',
            fixedPrice: '',
            pricePerRequest: '',
            currencyType: '',
            billingCycle: 'week',
        },
        customAttributes: [],
        stopOnQuotaReach: true,
        permissions: {
            roles: [],
            permissionStatus: 'NONE',
        },
        graphQL: {
            maxComplexity: '',
            maxDepth: '',
        },
        subscriberCount: '',
    });

    const [customAttributes, setCustomAttributes] = useState([]);
    const [state, dispatch] = useReducer(reducer, initialState);

    useEffect(() => {
        if (isEdit) {
            restApi.subscriptionThrottlingPolicyGet(params.id).then((result) => {
                let requestCountEdit = '';
                let dataAmountEdit = '';
                let timeUnitEdit = 'min';
                let unitTimeEdit = '';
                let typeEdit = 'REQUESTCOUNTLIMIT';
                let dataUnitEdit = 'KB';
                let eventCountEdit = '';
                if (result.body.defaultLimit.requestCount !== null) {
                    requestCountEdit = result.body.defaultLimit.requestCount.requestCount;
                    timeUnitEdit = result.body.defaultLimit.requestCount.timeUnit;
                    unitTimeEdit = result.body.defaultLimit.requestCount.unitTime;
                    typeEdit = result.body.defaultLimit.type;
                } else if (result.body.defaultLimit.bandwidth != null) {
                    dataAmountEdit = result.body.defaultLimit.bandwidth.dataAmount;
                    dataUnitEdit = result.body.defaultLimit.bandwidth.dataUnit;
                    timeUnitEdit = result.body.defaultLimit.bandwidth.timeUnit;
                    unitTimeEdit = result.body.defaultLimit.bandwidth.unitTime;
                    typeEdit = result.body.defaultLimit.type;
                } else {
                    eventCountEdit = result.body.defaultLimit.eventCount.eventCount;
                    unitTimeEdit = result.body.defaultLimit.eventCount.unitTime;
                    timeUnitEdit = result.body.defaultLimit.eventCount.timeUnit;
                    typeEdit = result.body.defaultLimit.type;
                }
                setValidRoles(result.body.permissions
                    && result.body.permissions.roles
                    ? result.body.permissions.roles
                    : []);
                const editState = {
                    policyName: result.body.policyName,
                    description: result.body.description,
                    defaultLimit: {
                        requestCount: requestCountEdit,
                        timeUnit: timeUnitEdit,
                        unitTime: unitTimeEdit,
                        type: typeEdit,
                        dataAmount: dataAmountEdit,
                        dataUnit: dataUnitEdit,
                        eventCount: eventCountEdit,
                    },
                    subscriberCount: (result.body.subscriberCount === 0) ? '' : result.body.subscriberCount,
                    rateLimitCount: (result.body.rateLimitCount === 0) ? '' : result.body.rateLimitCount,
                    rateLimitTimeUnit: (result.body.rateLimitCount === 0) ? 'sec' : result.body.rateLimitTimeUnit,
                    billingPlan: result.body.billingPlan,
                    monetization: {
                        monetizationPlan: (result.body.monetization !== null)
                            ? result.body.monetization.monetizationPlan : '',
                        fixedPrice: (result.body.monetization !== null)
                            ? result.body.monetization.properties.fixedPrice : '',
                        pricePerRequest: (result.body.monetization !== null)
                            ? result.body.monetization.properties.pricePerRequest : '',
                        currencyType: (result.body.monetization !== null)
                            ? result.body.monetization.properties.currencyType : '',
                        billingCycle: (result.body.monetization !== null)
                            ? result.body.monetization.properties.billingCycle : '',
                    },
                    customAttributes: setCustomAttributes(result.body.customAttributes),
                    stopOnQuotaReach: result.body.stopOnQuotaReach,
                    permissions: (result.body.permissions === null || result.body.permissions === 'NONE')
                        ? {
                            permissionStatus: 'NONE',
                        } : {
                            permissionStatus: result.body.permissions.permissionType,
                            roles: validRoles,
                        },
                    graphQL: {
                        maxComplexity: (result.body.graphQLMaxComplexity === 0) ? '' : result.body.graphQLMaxComplexity,
                        maxDepth: (result.body.graphQLMaxDepth === 0) ? '' : result.body.graphQLMaxDepth,
                    },
                };
                dispatch({ field: 'editDetails', value: editState });
            });
        }
        setInitialState({
            policyName: '',
            description: '',
            defaultLimit: {
                requestCount: '',
                timeUnit: 'min',
                unitTime: '',
                type: 'REQUESTCOUNTLIMIT',
                dataAmount: '',
                dataUnit: 'KB',
                eventCount: '',
            },
            rateLimitCount: '',
            rateLimitTimeUnit: 'sec',
            billingPlan: 'FREE',
            monetization: {
                monetizationPlan: 'FIXEDRATE',
                fixedPrice: '',
                pricePerRequest: '',
                currencyType: '',
                billingCycle: 'week',
            },
            customAttributes: [],
            stopOnQuotaReach: true,
            permissions: null,
            graphQL: {
                maxComplexity: '',
                maxDepth: '',
            },
            subscriberCount: '',
        });
    }, []);

    const [validationError, setValidationError] = useState([]);

    const validate = (fieldName, value) => {
        let error = '';
        const schema = Joi.string().regex(/^[^~!@#;:%^*()+={}|\\<>"',&$\s+]*$/);
        switch (fieldName) {
            case 'policyName':
                if (value === '') {
                    error = intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.name.empty.error.msg',
                        defaultMessage: 'Name is Empty',
                    });
                } else if (value.indexOf(' ') !== -1) {
                    error = intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.name.space.error.msg',
                        defaultMessage: 'Name contains spaces',
                    });
                } else if (value.length > 60) {
                    error = intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.name.too.long.error.msg',
                        defaultMessage: 'Subscription policy name is too long',
                    });
                } else if (schema.validate(value).error) {
                    error = intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.name.invalid.character.error.msg',
                        defaultMessage: 'Name contains one or more illegal characters',
                    });
                } else {
                    error = '';
                }
                setValidationError({ policyName: error });
                break;
            case 'requestCount':
                error = value === '' ? intl.formatMessage({
                    id: 'Throttling.Subscription.Policy.policy.request.count.empty.error.msg',
                    defaultMessage: 'Request Count is Empty',
                }) : '';
                setValidationError({ requestCount: error });
                break;
            case 'eventCount':
                error = value === '' ? intl.formatMessage({
                    id: 'Throttling.Subscription.Policy.policy.event.count.empty.error.msg',
                    defaultMessage: 'Event Count is Empty',
                }) : '';
                setValidationError({ eventCount: error });
                break;
            case 'dataAmount':
                error = value === '' ? intl.formatMessage({
                    id: 'Throttling.Subscription.Policy.policy.data.amount.empty.error.msg',
                    defaultMessage: 'Data Bandwidth amount is Empty',
                }) : '';
                setValidationError({ dataAmount: error });
                break;
            case 'unitTime':
                error = value === '' ? intl.formatMessage({
                    id: 'Throttling.Subscription.Policy.policy.unit.time.empty.error.msg',
                    defaultMessage: 'Unit Time is Empty',
                }) : '';
                setValidationError({ unitTime: error });
                break;
            default:
                break;
        }
        return error;
    };

    const {
        policyName,
        description,
        defaultLimit: {
            requestCount,
            timeUnit,
            unitTime,
            type,
            dataAmount,
            dataUnit,
            eventCount,
        },
        rateLimitCount,
        rateLimitTimeUnit,
        billingPlan,
        stopOnQuotaReach,
        monetization: {
            monetizationPlan,
            fixedPrice,
            pricePerRequest,
            currencyType,
            billingCycle,
        },
        permissions,
        graphQL: {
            maxComplexity,
            maxDepth,
        },
        subscriberCount,
    } = state;
    let permissionStatus = '';
    if (permissions) {
        permissionStatus = state.permissions.permissionStatus;
    }

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };

    const onToggle = (e) => {
        dispatch({ field: e.target.name, value: e.target.checked });
    };

    const getAllFormErrors = () => {
        let errorText = '';
        const policyNameErrors = validate('policyName', policyName);
        const requestCountErrors = validate('requestCount', requestCount);
        const eventCountErrors = validate('eventCount', eventCount);
        const dataAmountErrors = validate('dataAmount', dataAmount);
        const unitTimeErrors = validate('unitTime', unitTime);

        if (type === 'BANDWIDTHLIMIT') {
            errorText += policyNameErrors + dataAmountErrors + unitTimeErrors;
        } else if (type === 'REQUESTCOUNTLIMIT') {
            errorText += policyNameErrors + requestCountErrors + unitTimeErrors;
        } else {
            errorText += policyNameErrors + eventCountErrors + unitTimeErrors;
        }
        return errorText;
    };

    const handleRoleAddition = (role) => {
        const promise = restApi.validateSystemRole(base64url.encode(role));
        promise
            .then(() => {
                setValidRoles(validRoles.concat(role));
                if (invalidRoles.length === 0) {
                    setRoleValidity(true);
                } else {
                    setRoleValidity(false);
                }
            })
            .catch((error) => {
                if (error.status === 404) {
                    setInvalidRoles(invalidRoles.concat(role));
                    setRoleValidity(false);
                } else {
                    Alert.error('Error when validating role: ' + role);
                    console.error('Error when validating role ' + error);
                }
            });
    };

    const handleRoleDeletion = (role) => {
        if (invalidRoles.includes(role)) {
            const invalidRolesArray = invalidRoles.filter((existingRole) => existingRole !== role);
            setInvalidRoles(invalidRolesArray);
            if (invalidRolesArray.length === 0) {
                setRoleValidity(true);
            }
        } else {
            setValidRoles(validRoles.filter((existingRole) => existingRole !== role));
        }
    };

    const formSaveCallback = () => {
        const formErrors = getAllFormErrors();
        if (formErrors !== '') {
            Alert.error(formErrors);
            return (false);
        }
        let subscriptionThrottlingPolicy;
        let promisedAddSubscriptionPolicy;
        if (type === 'REQUESTCOUNTLIMIT') {
            subscriptionThrottlingPolicy = {
                policyName: state.policyName,
                description: state.description,
                defaultLimit: {
                    type: state.defaultLimit.type,
                    requestCount: {
                        requestCount: state.defaultLimit.requestCount,
                        timeUnit: state.defaultLimit.timeUnit,
                        unitTime: state.defaultLimit.unitTime,
                    },
                },
                subscriberCount: (state.subscriberCount === '') ? 0 : state.subscriberCount,
                rateLimitCount: (state.rateLimitCount === '') ? 0 : state.rateLimitCount,
                rateLimitTimeUnit: state.rateLimitTimeUnit,
                billingPlan: state.billingPlan,
                stopOnQuotaReach: state.stopOnQuotaReach,
                customAttributes,
                graphQLMaxComplexity: (state.graphQL.maxComplexity === '') ? 0 : state.graphQL.maxComplexity,
                graphQLMaxDepth: (state.graphQL.maxDepth === '') ? 0 : state.graphQL.maxDepth,
                monetization: {
                    monetizationPlan: state.monetization.monetizationPlan,
                    properties: {
                        fixedPrice: state.monetization.fixedPrice,
                        pricePerRequest: state.monetization.pricePerRequest,
                        currencyType: state.monetization.currencyType,
                        billingCycle: state.monetization.billingCycle,
                    },
                },
                permissions: (state.permissions === null || state.permissions.permissionStatus === null
                    || state.permissions.permissionStatus === 'NONE') ? null : {
                        permissionType: state.permissions.permissionStatus,
                        roles: validRoles,
                    },
            };
        } else if (type === 'BANDWIDTHLIMIT') {
            subscriptionThrottlingPolicy = {
                policyName: state.policyName,
                description: state.description,
                defaultLimit: {
                    type: state.defaultLimit.type,
                    bandwidth: {
                        dataAmount: state.defaultLimit.dataAmount,
                        dataUnit: state.defaultLimit.dataUnit,
                        timeUnit: state.defaultLimit.timeUnit,
                        unitTime: state.defaultLimit.unitTime,
                    },
                },
                subscriberCount: (state.subscriberCount === '') ? 0 : state.subscriberCount,
                rateLimitCount: (state.rateLimitCount === '') ? 0 : state.rateLimitCount,
                rateLimitTimeUnit: state.rateLimitTimeUnit,
                billingPlan: state.billingPlan,
                stopOnQuotaReach: state.stopOnQuotaReach,
                customAttributes,
                graphQLMaxComplexity: (state.graphQL.maxComplexity === '') ? 0 : state.graphQL.maxComplexity,
                graphQLMaxDepth: (state.graphQL.maxDepth === '') ? 0 : state.graphQL.maxDepth,
                monetization: {
                    monetizationPlan: state.monetization.monetizationPlan,
                    properties: {
                        fixedPrice: state.monetization.fixedPrice,
                        pricePerRequest: state.monetization.pricePerRequest,
                        currencyType: state.monetization.currencyType,
                        billingCycle: state.monetization.billingCycle,
                    },
                },
                permissions: (state.permissions == null || state.permissions.permissionStatus === null
                    || state.permissions.permissionStatus === 'NONE') ? null : {
                        permissionType: state.permissions.permissionStatus,
                        roles: validRoles,
                    },
            };
        } else {
            subscriptionThrottlingPolicy = {
                policyName: state.policyName,
                description: state.description,
                defaultLimit: {
                    type: state.defaultLimit.type,
                    eventCount: {
                        eventCount: state.defaultLimit.eventCount,
                        timeUnit: state.defaultLimit.timeUnit,
                        unitTime: state.defaultLimit.unitTime,
                    },
                },
                subscriberCount: (state.subscriberCount === '') ? 0 : state.subscriberCount,
                rateLimitCount: (state.rateLimitCount === '') ? 0 : state.rateLimitCount,
                rateLimitTimeUnit: state.rateLimitTimeUnit,
                billingPlan: state.billingPlan,
                stopOnQuotaReach: state.stopOnQuotaReach,
                customAttributes,
                graphQLMaxComplexity: (state.graphQL.maxComplexity === '') ? 0 : state.graphQL.maxComplexity,
                graphQLMaxDepth: (state.graphQL.maxDepth === '') ? 0 : state.graphQL.maxDepth,
                monetization: {
                    monetizationPlan: state.monetization.monetizationPlan,
                    properties: {
                        fixedPrice: state.monetization.fixedPrice,
                        pricePerRequest: state.monetization.pricePerRequest,
                        currencyType: state.monetization.currencyType,
                        billingCycle: state.monetization.billingCycle,
                    },
                },
                permissions: (state.permissions === null || state.permissions.permissionStatus === null
                    || state.permissions.permissionStatus === 'NONE') ? null : {
                        permissionType: state.permissions.permissionStatus,
                        roles: validRoles,
                    },
            };
        }

        if (isEdit) {
            const policyId = params.id;
            promisedAddSubscriptionPolicy = restApi.updateSubscriptionThrottlingPolicy(policyId,
                subscriptionThrottlingPolicy);
            if (promisedAddSubscriptionPolicy) {
                setSaving(true);
            }
            return promisedAddSubscriptionPolicy
                .then(() => {
                    Alert.success(intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.edit.success',
                        defaultMessage: 'Subscription Rate Limiting Policy updated successfully.',
                    }));
                    history.push('/throttling/subscription');
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        Alert.error(response.body.description);
                    }
                    return null;
                })
                .finally(() => {
                    setSaving(false);
                });
        } else {
            promisedAddSubscriptionPolicy = restApi.addSubscriptionThrottlingPolicy(
                subscriptionThrottlingPolicy,
            );
            if (promisedAddSubscriptionPolicy) {
                setSaving(true);
            }
            return promisedAddSubscriptionPolicy
                .then(() => {
                    Alert.success(intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.policy.add.success',
                        defaultMessage: 'Subscription Rate Limiting Policy added successfully.',
                    }));
                    history.push('/throttling/subscription');
                })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        Alert.error(response.body.description);
                    }
                    return null;
                })
                .finally(() => {
                    setSaving(false);
                });
        }
    };

    const handleAddNewAttributeClick = () => {
        setCustomAttributes((prevCustomAttributes) => [...prevCustomAttributes, { name: '', value: '' }]);
    };

    const handleAttributeDelete = (event) => {
        const tmpCustomAttributesForDelete = [...customAttributes];
        const { id } = event.currentTarget;
        tmpCustomAttributesForDelete.splice(id, 1);
        setCustomAttributes(tmpCustomAttributesForDelete);
    };

    const handleAttributeChange = (event) => {
        const { id, name, value } = event.target;
        const tmpCustomAttributes = [...customAttributes];
        tmpCustomAttributes[id][name] = value;
        setCustomAttributes(tmpCustomAttributes);
    };

    return (
        <ContentBase
            pageStyle='half'
            title={isEdit
                ? intl.formatMessage({
                    id: 'Throttling.Subscription.AddEdit.title.edit',
                    defaultMessage: 'Subscription Rate Limiting Policy - Edit',
                })
                : intl.formatMessage({
                    id: 'Throttling.Subscription.AddEdit.title.add',
                    defaultMessage: 'Subscription Rate Limiting Policy - Create new',
                })}
        >
            <Box component='div' m={2} className={classes.root}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.general.details'
                                        defaultMessage='General Details'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.general.details.description'
                                        defaultMessage='Provide the name and description of the subscription policy.'
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    {/* General Details */}
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <TextField
                                autoFocus
                                margin='dense'
                                name='policyName'
                                value={policyName}
                                disabled={isEdit}
                                onChange={onChange}
                                InputProps={{
                                    id: 'policyName',
                                    onBlur: ({ target: { value } }) => {
                                        validate('policyName', value);
                                    },
                                }}
                                required
                                label={(
                                    <span>
                                        <FormattedMessage
                                            id='Throttling.Subscription.AddEdit.form.policyName'
                                            defaultMessage='Name'
                                        />
                                    </span>
                                )}
                                fullWidth
                                error={validationError.policyName}
                                helperText={validationError.policyName || intl.formatMessage({
                                    id: 'Throttling.Subscription.AddEdit.form.name.help',
                                    defaultMessage: 'Name of the rate limiting policy',
                                })}
                                variant='outlined'
                            />
                            <TextField
                                margin='dense'
                                name='description'
                                value={description}
                                onChange={onChange}
                                label={intl.formatMessage({
                                    id: 'Throttling.Subscription.AddEdit.form.description',
                                    defaultMessage: 'Description',
                                })}
                                fullWidth
                                multiline
                                helperText={intl.formatMessage({
                                    id: 'Throttling.Subscription.AddEdit.form.description.help',
                                    defaultMessage: 'Description of the rate limiting policy',
                                })}
                                variant='outlined'
                            />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Quota limits */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.quota.limits'
                                        defaultMessage='Quota Limits'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.quota.policies.add.description'
                                        defaultMessage={'Request Count and Request Bandwidth are the '
                                + 'two options for Quota Limit. You can use the option according '
                                + 'to your requirement.'}
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <RadioGroup
                                aria-label='Quota Limits'
                                name='type'
                                value={type}
                                onChange={onChange}
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
                                <FormControlLabel
                                    value='EVENTCOUNTLIMIT'
                                    control={<Radio />}
                                    label='Event Based (Async API)'
                                />
                            </RadioGroup>
                        </Box>
                        <Box component='div' m={1}>
                            {type === 'REQUESTCOUNTLIMIT' && (
                                <Box display='flex' flexDirection='row'>
                                    <TextField
                                        margin='dense'
                                        name='requestCount'
                                        value={requestCount}
                                        type='number'
                                        onChange={onChange}
                                        required
                                        InputProps={{
                                            id: 'requestCount',
                                            onBlur: ({ target: { value } }) => {
                                                validate('requestCount', value);
                                            },
                                        }}
                                        label={(
                                            <FormattedMessage
                                                id='Throttling.Subscription.AddEdit.form.requestCount.count'
                                                defaultMessage='Request Count'
                                            />
                                        )}
                                        fullWidth
                                        error={validationError.requestCount}
                                        helperText={validationError.requestCount || 'Number of requests allowed'}
                                        variant='outlined'
                                    />
                                </Box>
                            )}
                            {type === 'BANDWIDTHLIMIT' && (
                                <Box display='flex' flexDirection='row'>
                                    <TextField
                                        margin='dense'
                                        name='dataAmount'
                                        value={dataAmount}
                                        type='number'
                                        onChange={onChange}
                                        required
                                        InputProps={{
                                            id: 'dataAmount',
                                            onBlur: ({ target: { value } }) => {
                                                validate('dataAmount', value);
                                            },
                                        }}
                                        label={(
                                            <FormattedMessage
                                                id='Throttling.Subscription.AddEdit.form.dataAmount.name'
                                                defaultMessage='Data Bandwidth'
                                            />
                                        )}
                                        fullWidth
                                        error={validationError.dataAmount}
                                        helperText={validationError.dataAmount || 'Bandwidth allowed'}
                                        variant='outlined'
                                    />
                                    <FormControl variant='outlined' className={classes.formControlSelect}>
                                        <Select
                                            name='dataUnit'
                                            fullWidth
                                            value={dataUnit}
                                            onChange={onChange}
                                            classes={{ root: classes.selectRoot }}
                                        >
                                            <MenuItem value='KB'>KB</MenuItem>
                                            <MenuItem value='MB'>MB</MenuItem>
                                        </Select>
                                    </FormControl>
                                </Box>
                            )}
                            {type === 'EVENTCOUNTLIMIT' && (
                                <Box display='flex' flexDirection='row'>
                                    <TextField
                                        margin='dense'
                                        name='eventCount'
                                        value={eventCount}
                                        type='number'
                                        onChange={onChange}
                                        required
                                        InputProps={{
                                            id: 'eventCount',
                                            onBlur: ({ target: { value } }) => {
                                                validate('eventCount', value);
                                            },
                                        }}
                                        label={(
                                            <FormattedMessage
                                                id='Throttling.Subscription.AddEdit.form.eventCount.count'
                                                defaultMessage='Event Count'
                                            />
                                        )}
                                        fullWidth
                                        error={validationError.eventCount}
                                        helperText={validationError.eventCount || 'Number of events allowed'}
                                        variant='outlined'
                                    />
                                </Box>
                            )}
                            <Box display='flex' flexDirection='row'>
                                <TextField
                                    margin='dense'
                                    name='unitTime'
                                    value={unitTime}
                                    onChange={onChange}
                                    type='number'
                                    label='Unit Time'
                                    required
                                    InputProps={{
                                        id: 'unitTime',
                                        onBlur: ({ target: { value } }) => {
                                            validate('unitTime', value);
                                        },
                                    }}
                                    error={validationError.unitTime}
                                    helperText={validationError.unitTime || intl.formatMessage({
                                        id: 'Throttling.Subscription.AddEdit.unitTime',
                                        defaultMessage: 'Unit Time',
                                    })}
                                    fullWidth
                                    variant='outlined'
                                />
                                <FormControl variant='outlined' className={classes.formControlSelect}>
                                    <Select
                                        name='timeUnit'
                                        value={timeUnit}
                                        fullWidth
                                        onChange={onChange}
                                        classes={{ root: classes.selectRoot }}
                                    >
                                        <MenuItem value='min'>Minute(s)</MenuItem>
                                        <MenuItem value='hours'>Hour(s)</MenuItem>
                                        <MenuItem value='days'>Day(s)</MenuItem>
                                        <MenuItem value='months'>Month(s)</MenuItem>
                                        <MenuItem value='years'>Year(s)</MenuItem>
                                    </Select>
                                </FormControl>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Burst Control (Rate Limiting) */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.burst.control.limits'
                                        defaultMessage='Burst Control (Rate Limiting)'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.burst.control.add.description'
                                        defaultMessage={'Define Burst Control Limits for the subscription policy.'
                                        + ' This is optional.'}
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Box display='flex' flexDirection='row'>
                                <Grid item xs={12} md={12} lg={9}>
                                    <TextField
                                        margin='dense'
                                        name='rateLimitCount'
                                        value={rateLimitCount}
                                        type='number'
                                        onChange={onChange}
                                        label={(
                                            <FormattedMessage
                                                id='Throttling.Subscription.AddEdit.form.request.rate'
                                                defaultMessage='Request Rate'
                                            />
                                        )}
                                        fullWidth
                                        helperText={intl.formatMessage({
                                            id: 'Throttling.Subscription.AddEdit.burst.control.limit',
                                            defaultMessage: 'Number of requests for burst control',
                                        })}
                                        variant='outlined'
                                    />
                                </Grid>
                                <FormControl variant='outlined' className={classes.formControlSelect}>
                                    <Select
                                        name='rateLimitTimeUnit'
                                        fullWidth
                                        value={rateLimitTimeUnit}
                                        onChange={onChange}
                                        classes={{ root: classes.selectRoot }}
                                    >
                                        <MenuItem value='sec'>Requests/s</MenuItem>
                                        <MenuItem value='min'>Requests/min</MenuItem>
                                    </Select>
                                </FormControl>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>

                    {/* GraphQL */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.GraphQL'
                                        defaultMessage='GraphQL'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.graphql.add.description'
                                        defaultMessage={'Provide the Maximum Complexity and Maximum depth'
                                        + ' values for GraphQL APIs using this policy.'}
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <TextField
                                        margin='dense'
                                        name='maxComplexity'
                                        value={maxComplexity}
                                        type='number'
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.AddEdit.form.max.complexity'
                                                    defaultMessage='Max Complexity'
                                                />
                                            </span>
                                        )}
                                        fullWidth
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <TextField
                                        margin='dense'
                                        name='maxDepth'
                                        type='number'
                                        value={maxDepth}
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.AddEdit.form.max.depth'
                                                    defaultMessage='Max Depth'
                                                />
                                            </span>
                                        )}
                                        fullWidth
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Web Hooks */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.Subscriber.Count'
                                        defaultMessage='Webhooks'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.subscription.count.add.description'
                                        defaultMessage={'Maximum number of webhooks'
                                        + ' allowed for a Webhooks API using this policy.'}
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Box flex='1'>
                                    <TextField
                                        margin='dense'
                                        name='subscriberCount'
                                        value={subscriberCount}
                                        type='number'
                                        onChange={onChange}
                                        label={(
                                            <span>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.AddEdit.form.max.webhooks.connections'
                                                    defaultMessage='Max Subscriptions'
                                                />
                                            </span>
                                        )}
                                        fullWidth
                                        variant='outlined'
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Policy flags */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.Policy.Flags'
                                        defaultMessage='Policy Flags'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.policy.flags.add.description'
                                        defaultMessage={'Define the billing plan for the subscription policy.'
                                        + ' Enable stop on quota reach to block invoking an API when the'
                                        + ' defined quota is reached.'}
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Box display='flex' flexDirection='row' alignItems='center'>
                                <Typography color='inherit' variant='body1' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.Billing.Plan'
                                        defaultMessage='Billing Plan'
                                    />
                                </Typography>
                                <Box flex='1'>
                                    <RadioGroup
                                        aria-label='position'
                                        name='billingPlan'
                                        value={billingPlan}
                                        onChange={onChange}
                                        className={classes.radioGroupBilling}
                                    >
                                        <FormControlLabel
                                            value='FREE'
                                            control={<Radio />}
                                            label='Free'
                                        />
                                        <FormControlLabel
                                            value='COMMERCIAL'
                                            control={<Radio />}
                                            label='Commercial'
                                        />
                                    </RadioGroup>
                                </Box>
                            </Box>
                            {billingPlan === 'COMMERCIAL' && (
                                <div>
                                    <Box display='flex' flexDirection='row' alignItems='center'>
                                        <Box flex='1'>
                                            <Box display='flex' flexDirection='row' alignItems='center'>
                                                <Typography color='inherit' variant='body1' component='div'>
                                                    <FormattedMessage
                                                        id='Throttling.Subscription.monetization.plan'
                                                        defaultMessage='Monetization Plan'
                                                    />
                                                </Typography>
                                                <Tooltip
                                                    title={(
                                                        <FormattedMessage
                                                            id='Throttling.Subscription.monetization.plan.tooltip'
                                                            defaultMessage='Monetization category type'
                                                        />
                                                    )}
                                                    placement='right-end'
                                                    className={classes.paddingTooltip}
                                                    interactive
                                                >
                                                    <HelpOutlinedIcon fontSize='medium' />
                                                </Tooltip>
                                            </Box>
                                        </Box>
                                        <Box flex='1'>
                                            <FormControl variant='outlined' className={classes.formControlSelectBottom}>
                                                <Select
                                                    name='monetizationPlan'
                                                    margin='dense'
                                                    fullWidth
                                                    variant='outlined'
                                                    align='center'
                                                    value={monetizationPlan}
                                                    onChange={onChange}
                                                >
                                                    <MenuItem value='FIXEDRATE'>Fixed Rate</MenuItem>
                                                    <MenuItem value='DYNAMICRATE'>Dynamic Usage</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Box>
                                    </Box>
                                    {monetizationPlan === 'FIXEDRATE' && (
                                        <Box display='flex' flexDirection='row' alignItems='center'>
                                            <Box flex='1'>
                                                <Box display='flex' flexDirection='row' alignItems='center'>
                                                    <Typography color='inherit' variant='body1' component='div'>
                                                        <FormattedMessage
                                                            id='Throttling.Subscription.Fixed.Rate'
                                                            defaultMessage='Fixed Rate'
                                                        />
                                                    </Typography>
                                                    <Tooltip
                                                        title={(
                                                            <FormattedMessage
                                                                id='Throttling.Subscription.fixed.rate.tooltip'
                                                                defaultMessage='Fixed rate for the given billing cycle'
                                                            />
                                                        )}
                                                        placement='right-end'
                                                        className={classes.paddingTooltip}
                                                        interactive
                                                    >
                                                        <HelpOutlinedIcon fontSize='medium' />
                                                    </Tooltip>
                                                </Box>
                                            </Box>
                                            <Box flex='1'>
                                                <TextField
                                                    margin='dense'
                                                    name='fixedPrice'
                                                    value={fixedPrice}
                                                    onChange={onChange}
                                                    label='Fixed Rate'
                                                    fullWidth
                                                    variant='outlined'
                                                />
                                            </Box>
                                        </Box>
                                    )}
                                    {monetizationPlan === 'DYNAMICRATE' && (
                                        <Box display='flex' flexDirection='row' alignItems='center'>
                                            <Box flex='1'>
                                                <Box display='flex' flexDirection='row' alignItems='center'>
                                                    <Typography color='inherit' variant='body1' component='div'>
                                                        <FormattedMessage
                                                            id='Throttling.Subscription.dynamic.usage'
                                                            defaultMessage='Price Per Request'
                                                        />
                                                    </Typography>
                                                    <Tooltip
                                                        title={(
                                                            <FormattedMessage
                                                                id='Throttling.Subscription.dynamic.usage.tooltip'
                                                                defaultMessage={'Price per request for the given'
                                                                    + ' billing cycle in the given currency'}
                                                            />
                                                        )}
                                                        placement='right-end'
                                                        className={classes.paddingTooltip}
                                                        interactive
                                                    >
                                                        <HelpOutlinedIcon fontSize='medium' />
                                                    </Tooltip>
                                                </Box>
                                            </Box>
                                            <Box flex='1'>
                                                <TextField
                                                    margin='dense'
                                                    name='pricePerRequest'
                                                    value={pricePerRequest}
                                                    onChange={onChange}
                                                    label='Price Per Request'
                                                    fullWidth
                                                    variant='outlined'
                                                />
                                            </Box>
                                        </Box>
                                    )}
                                    <Box display='flex' flexDirection='row' alignItems='center'>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.currency'
                                                    defaultMessage='Currency'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <TextField
                                                margin='dense'
                                                name='currencyType'
                                                value={currencyType}
                                                onChange={onChange}
                                                label='Currency'
                                                fullWidth
                                                variant='outlined'
                                            />
                                        </Box>
                                    </Box>
                                    <Box display='flex' flexDirection='row' alignItems='center'>
                                        <Box flex='1'>
                                            <Typography color='inherit' variant='body1' component='div'>
                                                <FormattedMessage
                                                    id='Throttling.Subscription.billing.cycle'
                                                    defaultMessage='Billing Cycle'
                                                />
                                            </Typography>
                                        </Box>
                                        <Box flex='1'>
                                            <FormControl variant='outlined' className={classes.formControlSelectBottom}>
                                                <Select
                                                    name='billingCycle'
                                                    fullWidth
                                                    margin='dense'
                                                    variant='outlined'
                                                    align='center'
                                                    value={billingCycle}
                                                    onChange={onChange}
                                                >
                                                    <MenuItem value='week'>Week</MenuItem>
                                                    <MenuItem value='month'>Month</MenuItem>
                                                    <MenuItem value='year'>Year</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Box>
                                    </Box>
                                </div>
                            )}
                            <Box
                                display='flex'
                                flexDirection='row'
                                alignItems='center'
                                className={classes.toggleSwitchPadding}
                            >
                                <Box flex='0.65'>
                                    <Typography color='inherit' variant='body1' component='div'>
                                        <FormattedMessage
                                            id='Throttling.Subscription.stop.quota.reach'
                                            defaultMessage='Stop On Quota Reach'
                                        />
                                    </Typography>
                                </Box>
                                <Box flex='1'>
                                    <Switch
                                        checked={stopOnQuotaReach}
                                        onChange={onToggle}
                                        fullWidth
                                        align='center'
                                        size='medium'
                                        color='primary'
                                        name='stopOnQuotaReach'
                                    />
                                </Box>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Custom Attributes */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.custom.attributes'
                                        defaultMessage='Custom Attributes'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.custom.attributes.add.description'
                                        defaultMessage='Define custom attributes for the subscription policy.'
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <Grid item xs={12}>
                                <Box flex='1'>
                                    <Button
                                        variant='outlined'
                                        onClick={handleAddNewAttributeClick}
                                    >
                                        <AddCircle className={classes.buttonIcon} />
                                        <FormattedMessage
                                            id='Throttling.Subscription.custom.attributes.add'
                                            defaultMessage='Add Custom Attribute'
                                        />
                                    </Button>
                                </Box>
                                <Table className={classes.table}>
                                    <TableBody>
                                        {customAttributes.map((attribute, index) => (
                                            <TableRow>
                                                <TableCell>
                                                    <TextField
                                                        fullWidth
                                                        required
                                                        id={index}
                                                        name='name'
                                                        label={intl.formatMessage({
                                                            id: 'Throttling.Subscription.Properties.Properties.'
                                                                        + 'show.add.property.property.name',
                                                            defaultMessage: 'Name',
                                                        })}
                                                        margin='dense'
                                                        variant='outlined'
                                                        value={attribute.name}
                                                        onChange={handleAttributeChange}
                                                    />
                                                </TableCell>
                                                <TableCell>
                                                    <TextField
                                                        fullWidth
                                                        required
                                                        id={index}
                                                        name='value'
                                                        label={intl.formatMessage({
                                                            id: 'Throttling.Subscription.Properties.property.value',
                                                            defaultMessage: 'Value',
                                                        })}
                                                        margin='dense'
                                                        value={attribute.value}
                                                        onChange={handleAttributeChange}
                                                        variant='outlined'
                                                    />
                                                </TableCell>
                                                <TableCell align='right'>
                                                    <Tooltip
                                                        title={(
                                                            <FormattedMessage
                                                                id='Throttling.Subscription.attribute.delete.tooltip'
                                                                defaultMessage='Delete'
                                                            />
                                                        )}
                                                        placement='right-end'
                                                        interactive
                                                    >
                                                        <IconButton
                                                            id={index}
                                                            onClick={handleAttributeDelete}
                                                        >
                                                            <DeleteForeverIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </Grid>
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Permissions */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1'>
                                <Typography color='inherit' variant='subtitle2' component='div'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.permissions'
                                        defaultMessage='Permissions'
                                    />
                                </Typography>
                                <Typography color='inherit' variant='caption' component='p'>
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.permissions.add.description'
                                        defaultMessage='Define the permissions for the subscription policy.'
                                    />
                                </Typography>
                            </Box>
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            {
                                (permissionStatus === 'ALLOW' || permissionStatus === 'DENY')
                                && (
                                    <Box display='flex' flexDirection='row' alignItems='center'>
                                        <ChipInput
                                            label='Roles'
                                            InputLabelProps={{
                                                shrink: true,
                                            }}
                                            variant='outlined'
                                            value={validRoles.concat(invalidRoles)}
                                            alwaysShowPlaceholder={false}
                                            placeholder='Enter roles and press Enter'
                                            blurBehavior='clear'
                                            InputProps={{
                                                endAdornment: !roleValidity && (
                                                    <InputAdornment position='end'>
                                                        <Error color='error' />
                                                    </InputAdornment>
                                                ),
                                            }}
                                            onAdd={handleRoleAddition}
                                            error={!roleValidity}
                                            helperText={
                                                !roleValidity ? (
                                                    <FormattedMessage
                                                        id='Apis.Details.Scopes.Roles.Invalid'
                                                        defaultMessage='A Role is invalid'
                                                    />
                                                ) : [
                                                    (permissionStatus === 'ALLOW'
                                                        ? (
                                                            <FormattedMessage
                                                                id='Throttling.Subscription.enter.permission.allowed'
                                                                defaultMessage='This policy is "Allowed" for above
                                                                roles.'
                                                            />
                                                        )
                                                        : (
                                                            <FormattedMessage
                                                                id='Throttling.Subscription.enter.permission.denied'
                                                                defaultMessage='This policy is "Denied" for above
                                                                roles.'
                                                            />
                                                        )
                                                    ),
                                                    ' ',
                                                    <FormattedMessage
                                                        id='Apis.Details.Scopes.CreateScope.roles.help'
                                                        defaultMessage='Enter a valid role and press `Enter`.'
                                                    />,
                                                ]
                                            }
                                            chipRenderer={({ value }, key) => (
                                                <Chip
                                                    key={key}
                                                    label={value}
                                                    onDelete={() => {
                                                        handleRoleDeletion(value);
                                                    }}
                                                    style={{
                                                        backgroundColor: invalidRoles.includes(value) ? red[300] : null,
                                                        margin: '8px 8px 8px 0',
                                                        float: 'left',
                                                    }}
                                                />
                                            )}
                                        />
                                    </Box>
                                )
                            }
                            <Box flex='1' mt={3}>
                                <RadioGroup
                                    name='permissionStatus'
                                    value={permissionStatus}
                                    onChange={onChange}
                                    className={classes.radioGroup}
                                    defaultValue='NONE'
                                >
                                    <FormControlLabel value='NONE' control={<Radio />} label='None' />
                                    <FormControlLabel value='ALLOW' control={<Radio />} label='Allow' />
                                    <FormControlLabel value='DENY' control={<Radio />} label='Deny' />
                                </RadioGroup>
                            </Box>
                        </Box>
                    </Grid>
                </Grid>
                {/* Submit buttons */}
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <Box component='span' m={1}>
                            <Button
                                variant='contained'
                                color='primary'
                                onClick={formSaveCallback}
                                disabled={invalidRoles.length !== 0}
                            >
                                {saving ? (<CircularProgress size={16} />) : (
                                    <FormattedMessage
                                        id='Throttling.Subscription.AddEdit.form.add'
                                        defaultMessage='Save'
                                    />
                                )}
                            </Button>
                        </Box>
                        <RouterLink to='/throttling/subscription'>
                            <Button variant='contained'>
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

AddEdit.defaultProps = {
    match: {
        params: {
            id: null,
        },
    },
};

AddEdit.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            id: PropTypes.string,
        }),
    }),
};

export default AddEdit;
