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
import { Link as RouterLink } from 'react-router-dom';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import Box from '@material-ui/core/Box';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import ConditionalGroup from 'AppComponents/Throttling/Advanced/ConditionalGroup';
import cloneDeep from 'lodash.clonedeep';
import HelpLinks from 'AppComponents/Throttling/Advanced/HelpLinks';
import API from 'AppData/api';
import AddEditExecution from 'AppComponents/Throttling/Advanced/AddEditExecution';
import CircularProgress from '@material-ui/core/CircularProgress';

const useStyles = makeStyles((theme) => ({
    error: {
        color: theme.palette.error.dark,
    },
    hr: {
        border: 'solid 1px #efefef',
    },
    root: {
        marginBottom: theme.spacing(15),
    },
}));


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
        case 'policyName':
        case 'description':
        case 'conditionalGroups':
            nextState[field] = value;
            return nextState;
        case 'defaultLimit':
            if (value === 'REQUESTCOUNTLIMIT') {
                const { defaultLimit: { bandwidth: { timeUnit, unitTime } } } = nextState;
                nextState.defaultLimit.requestCount = {
                    timeUnit, unitTime, requestCount: 0,
                };
                nextState.defaultLimit.type = 'REQUESTCOUNTLIMIT';
                nextState.defaultLimit.bandwidth = null;
            } else {
                const { defaultLimit: { requestCount: { timeUnit, unitTime } } } = nextState;
                nextState.defaultLimit.bandwidth = {
                    timeUnit, unitTime, dataAmount: 0, dataUnit: 'KB',
                };
                nextState.defaultLimit.type = 'BANDWIDTHLIMIT';
                nextState.defaultLimit.requestCount = null;
            }
            return nextState;
        case 'dataUnit':
        case 'requestCount':
        case 'timeUnit':
        case 'dataAmount':
        case 'unitTime':
            if (nextState.defaultLimit.requestCount) {
                nextState.defaultLimit.requestCount[field] = value;
            } else {
                nextState.defaultLimit.bandwidth[field] = value;
            }
            return nextState;
        default:
            return nextState;
    }
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 * @param {JSON} props Props passed from other components.
 */
function AddEdit(props) {
    const classes = useStyles();
    const [validating, setValidating] = useState(false);
    const [saving, setSaving] = useState(false);
    const intl = useIntl();
    const { match: { params: { id } }, history } = props;
    const editMode = id !== undefined;
    const initialState = {
        policyName: '',
        description: '',
        conditionalGroups: [],
        defaultLimit: {
            requestCount: {
                timeUnit: 'min',
                unitTime: '',
                requestCount: '',
            },
            type: 'REQUESTCOUNTLIMIT',
            bandwidth: null,
        },
    };
    const [state, dispatch] = useReducer(reducer, initialState);

    useEffect(() => {
        const restApi = new API();
        restApi
            .getThrottlingPoliciesAdvancedPolicyId(id)
            .then((result) => {
                const { body } = result;
                return body;
            })
            .then((data) => {
                dispatch({ field: 'all', value: data });
            })
            .catch((error) => {
                throw error;
            });
    }, []);

    const onChange = (e) => {
        dispatch({ field: e.target.name, value: e.target.value });
    };
    const hasErrors = (fieldName, fieldValue, validatingActive) => {
        let error = false;
        if (!validatingActive) {
            return (false);
        }
        switch (fieldName) {
            case 'policyName':
                if (fieldValue === '') {
                    error = `Policy name ${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.is.empty.error',
                        defaultMessage: ' is empty',
                    })}`;
                } else if (fieldValue.length > 60) {
                    error = intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.policy.name.too.long.error.msg',
                        defaultMessage: 'Throttling policy name is too long',
                    });
                } else if (fieldValue !== '' && /\s/g.test(fieldValue)) {
                    error = `Policy name ${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.empty.error',
                        defaultMessage: ' contains white spaces.',
                    })}`;
                } else if (/[^A-Za-z0-9]/.test(fieldValue)) {
                    error = `Policy name ${intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.special.characters.error',
                        defaultMessage: ' contains invalid characters.',
                    })}`;
                }
                break;
            case 'requestCount':
            case 'dataAmount':
            case 'unitTime':
                error = fieldValue === '' ? fieldName + ' is Empty' : false;
                break;
            default:
                break;
        }
        return error;
    };
    const {
        policyName,
        description,
        defaultLimit,
        conditionalGroups,
    } = state;

    const formHasErrors = (validatingActive = false) => {
        if (hasErrors('policyName', policyName, validatingActive)
        || hasErrors('description', description, validatingActive)) {
            return true;
        } else {
            return false;
        }
    };

    const newConditionalGroups = cloneDeep(conditionalGroups);
    const updateGroup = () => {
        dispatch({ field: 'conditionalGroups', value: newConditionalGroups });
    };

    const formSave = () => {
        setValidating(true);
        if (formHasErrors(true)) {
            Alert.error(intl.formatMessage({
                id: 'Throttling.Advanced.AddEdit.form.has.errors',
                defaultMessage: 'One or more fields contain errors.',
            }));
            return false;
        }
        // Do the API call
        const restApi = new API();
        let promiseAPICall = null;
        const body = {
            ...state,
        };
        body.conditionalGroups.forEach(
            (item) => {
                if (item.conditions.length) {
                    item.conditions.forEach((con) => {
                        if (con.id) {
                            // eslint-disable-next-line no-param-reassign
                            delete con.id;
                        }
                    });
                }
            },
        );
        setSaving(true);
        if (id) {
            promiseAPICall = restApi
                .putThrottlingPoliciesAdvanced(id, body).then(() => {
                    return intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.edit.success',
                        defaultMessage: 'Policy Updated Successfully',
                    });
                });
        } else {
            promiseAPICall = restApi
                .postThrottlingPoliciesAdvanced(body).then(() => {
                    return intl.formatMessage({
                        id: 'Throttling.Advanced.AddEdit.add.success',
                        defaultMessage: 'Policy Added Successfully',
                    });
                });
        }
        promiseAPICall.then((msg) => {
            Alert.success(`${policyName} ${msg}`);
            history.push('/throttling/advanced/');
        }).catch((error) => {
            const { response, message } = error;
            if (response && response.body) {
                Alert.error(response.body.description);
            } else if (message) {
                Alert.error(message);
            }
            return null;
        }).finally(() => {
            setSaving(false);
        });
        return true;
    };
    const addConditionalGroup = () => {
        const newGroup = {
            description: 'Sample description about condition group',
            conditions: [],
            limit: {
                requestCount: {
                    timeUnit: 'min', unitTime: 1, requestCount: 1000,
                },
                bandwidth: null,
                type: 'REQUESTCOUNTLIMIT',
            },
        };
        newConditionalGroups.push(newGroup);
        updateGroup();
    };
    const deleteGroup = (i) => {
        const clearedGroup = newConditionalGroups.slice(0, i).concat(
            newConditionalGroups.slice(i + 1, newConditionalGroups.length),
        );
        dispatch({ field: 'conditionalGroups', value: clearedGroup });
    };
    return (

        <ContentBase
            pageStyle='half'
            title={
                id ? `${intl.formatMessage({
                    id: 'Throttling.Advanced.AddEdit.title.edit',
                    defaultMessage: 'Advance Rate Limiting Policy - Edit ',
                })} ${policyName}` : intl.formatMessage({
                    id: 'Throttling.Advanced.AddEdit.title.new',
                    defaultMessage: 'Advanced Rate Limiting Policy - Create new',
                })
            }
            help={<HelpLinks />}
        >
            <Box component='div' m={2} className={classes.root}>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.general.details'
                                defaultMessage='General Details'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.general.details.description'
                                defaultMessage={'Provide name and description of the policy.'
                            + 'The policy can be refered from the name.'}
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box component='div' m={1}>
                            <TextField
                                autoFocus
                                margin='dense'
                                name='policyName'
                                disabled={editMode}
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
                                error={hasErrors('policyName', policyName, validating)}
                                helperText={hasErrors('policyName', policyName, validating) || intl.formatMessage({
                                    id: 'Throttling.Advanced.AddEdit.form.name.help',
                                    defaultMessage: 'Name of the throttle policy.',
                                })}
                                variant='outlined'
                            />
                            <TextField
                                margin='dense'
                                name='description'
                                value={description}
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
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    {/* Default limits */}
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.default.limits'
                                defaultMessage='Default Limits'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.default.limits.description'
                                defaultMessage={'Request Count and Request Bandwidth are the '
                                + 'two options for default limit. You can use the option according '
                                + 'to your requirement.'}
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <AddEditExecution
                            limit={cloneDeep(defaultLimit)}
                            onChange={onChange}
                            hasErrors={hasErrors}
                            validating={validating}
                        />
                        {/* Conditional groups */}
                    </Grid>
                </Grid>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12} md={12} lg={3}>
                        <Typography color='inherit' variant='subtitle2' component='div'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.add.conditional.group'
                                defaultMessage='Conditional groups'
                            />
                        </Typography>
                        <Typography color='inherit' variant='caption' component='p'>
                            <FormattedMessage
                                id='Throttling.Advanced.AddEdit.conditional.group.description'
                                defaultMessage={'To add throttling limits with different '
                                + 'parameters base on IP, Header, Query Param, and JWT '
                                + 'Claim conditions, click Add Conditional Group. '}
                            />
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={12} lg={9}>
                        <Box display='flex' flexDirection='row' alignItems='center'>
                            <Box flex='1' />

                            <Button variant='contained' onClick={addConditionalGroup}>
                                <FormattedMessage
                                    id='Throttling.Advanced.AddEdit.add.conditional.group.add'
                                    defaultMessage='Add Conditional Group'
                                />
                            </Button>

                        </Box>

                        <Box component='div' m={1}>
                            {newConditionalGroups.map((group, index) => (
                                <ConditionalGroup
                                    // The API is not providing a unique key for each item.
                                    // eslint-disable-next-line react/no-array-index-key
                                    key={index}
                                    index={index}
                                    group={group}
                                    defaultLimit={defaultLimit}
                                    updateGroup={updateGroup}
                                    hasErrors={hasErrors}
                                    deleteGroup={deleteGroup}
                                />
                            ))}
                        </Box>
                        {/* Submit buttons */}
                        <Box m={4} />
                    </Grid>
                    <Grid item xs={12}>
                        <Box marginTop={2} marginBottom={2}>
                            <hr className={classes.hr} />
                        </Box>
                    </Grid>
                    <Grid item xs={12}>
                        <Box component='span' m={1}>
                            <Button variant='contained' color='primary' onClick={formSave}>
                                {saving ? (<CircularProgress size={16} />) : (
                                    <>
                                        {id ? (
                                            <FormattedMessage
                                                id='Throttling.Advanced.AddEdit.form.update.btn'
                                                defaultMessage='Update'
                                            />
                                        ) : (
                                            <FormattedMessage
                                                id='Throttling.Advanced.AddEdit.form.add.btn'
                                                defaultMessage='Add'
                                            />
                                        )}
                                    </>
                                )}

                            </Button>
                        </Box>
                        <RouterLink to='/throttling/advanced'>
                            <Button variant='contained'>
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
    match: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({}).isRequired,
};


export default AddEdit;
