/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import { InputAdornment, IconButton, Icon } from '@material-ui/core';
import CircularProgress from '@material-ui/core/CircularProgress';
import Chip from '@material-ui/core/Chip';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import green from '@material-ui/core/colors/green';
import APIValidation from 'AppData/APIValidation';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    mandatoryStar: {
        color: theme.palette.error.main,
        marginLeft: theme.spacing(0.1),
    },
    helperTextContext: {
        '& p': {
            textOverflow: 'ellipsis',
            width: 400,
            display: 'block',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
        },
    },
    endpointValidChip: {
        color: 'green',
        border: '1px solid green',
    },
    endpointInvalidChip: {
        color: '#ffd53a',
        border: '1px solid #ffd53a',
    },
    endpointErrorChip: {
        color: 'red',
        border: '1px solid red',
    },
    iconButton: {
        padding: theme.spacing(1),
    },
    iconButtonValid: {
        padding: theme.spacing(1),
        color: green[500],
    },
}));

/**
 *
 * Return the actual API context that will be exposed in the gateway.
 * If the context value contains `{version}` placeholder text it will be replaced with the actual version value.
 * If there is no such placeholder text in the context, The version will be appended to the context
 * i:e /context/version
 * Parameter expect an object containing `context` and `version` properties.
 * @param {String} context API Context
 * @param {String} version API Version string
 * @param isWebSocket check whether it is a webSocketAPI
 * @returns {String} Derived actual context string
 */
function actualContext({ context, version }, isWebSocket) {
    let initialContext;
    // eslint-disable-next-line no-unused-expressions
    isWebSocket ? (initialContext = '{channel}/{version}') : (initialContext = '{context}/{version}');
    if (context) {
        initialContext = context;
        if (context.indexOf('{version}') < 0) {
            initialContext = context + '/{version}';
        }
    }
    if (version) {
        initialContext = initialContext.replace('{version}', version);
    }
    return initialContext;
}

/**
 * This method used to  compare the context values
 * @param {*} value  input value
 * @param {*} result resulted value
 * @returns {Boolean} true or false
 */
function checkContext(value, result) {
    const contextVal = value.includes('/') ? value.toLowerCase() : '/' + value.toLowerCase();
    if (contextVal === '/' + result.toLowerCase().slice(result.toLowerCase().lastIndexOf('/') + 1)
     || contextVal === result.toLowerCase()) {
        return true;
    }
    return false;
}

/**
 * Improved API create default form
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DefaultAPIForm(props) {
    const {
        onChange, onValidate, api, isAPIProduct, isWebSocket, children, appendChildrenBeforeEndpoint, hideEndpoint,
    } = props;
    const classes = useStyles();
    const [validity, setValidity] = useState({});
    const [isEndpointValid, setIsEndpointValid] = useState();
    const [statusCode, setStatusCode] = useState('');
    const [isUpdating, setUpdating] = useState(false);
    const [isErrorCode, setIsErrorCode] = useState(false);
    const iff = (condition, then, otherwise) => (condition ? then : otherwise);

    // Check the provided API validity on mount, TODO: Better to use Joi schema here ~tmkb
    useEffect(() => {
        onValidate(Boolean(api.name)
                && (isAPIProduct || Boolean(api.version))
                && Boolean(api.context));
    }, []);

    const updateValidity = (newState) => {
        let isFormValid = Object.entries(newState).length > 0
            && Object.entries(newState)
                .map(([, value]) => value === null || value === undefined)
                .reduce((acc, cVal) => acc && cVal); // Aggregate the individual validation states
        // TODO: refactor following redundant validation.
        // The valid state should available in the above reduced state ~tmkb
        // if isAPIProduct gets true version validation has been skipped
        isFormValid = isFormValid
            && Boolean(api.name)
            && (isAPIProduct || Boolean(api.version))
            && Boolean(api.context);
        onValidate(isFormValid, validity);
        setValidity(newState);
    };
    /**
     * Trigger the provided onValidate call back on each input validation run
     * Do the validation state aggregation and call the onValidate method with aggregated value
     * @param {string} field The input field.
     * @param {string} value Validation state object
     */
    function validate(field, value) {
        switch (field) {
            case 'name': {
                const nameValidity = APIValidation.apiName.required().validate(value, { abortEarly: false }).error;
                if (nameValidity === null) {
                    APIValidation.apiParameter.validate(field + ':' + value).then((result) => {
                        if (result.body.list.length > 0 && value.toLowerCase() === result.body.list[0]
                            .name.toLowerCase()) {
                            updateValidity({
                                ...validity,
                                name: { details: [{ message: 'Name ' + value + ' already exists' }] },
                            });
                        } else {
                            updateValidity({ ...validity, name: nameValidity });
                        }
                    });
                } else {
                    updateValidity({ ...validity, name: nameValidity });
                }
                break;
            }
            case 'context': {
                const contextValidity = APIValidation.apiContext.required().validate(value, { abortEarly: false })
                    .error;
                const apiContext = value.includes('/') ? value : '/' + value;
                if (contextValidity === null) {
                    APIValidation.apiParameter.validate(field + ':' + apiContext).then((result) => {
                        if (result.body.list.length > 0 && checkContext(value, result.body.list[0].context)) {
                            updateValidity({
                                ...validity,
                                // eslint-disable-next-line max-len
                                context: { details: [{ message: apiContext + isWebSocket ? ' channel already exists' : ' context already exists' }] },
                            });
                        } else {
                            updateValidity({ ...validity, context: contextValidity, version: null });
                        }
                    });
                } else {
                    updateValidity({ ...validity, context: contextValidity });
                }
                break;
            }
            case 'version': {
                const versionValidity = APIValidation.apiVersion.required().validate(value).error;
                if (versionValidity === null) {
                    const apiVersion = api.context.includes('/') ? api.context + '/' + value : '/'
                    + api.context + '/' + value;
                    APIValidation.apiParameter.validate('context:' + api.context
                    + '/' + value).then((result) => {
                        // version of APIProduct equals to 1.0.0
                        if (result.body.list.length > 0 && (
                            (result.body.list[0].version !== undefined
                            && (result.body.list[0].version.toLowerCase()
                                === value.toLowerCase())) || value === '1.0.0')) {
                            updateValidity({
                                ...validity,
                                version: { message: apiVersion + ' context with version already exists' },
                            });
                        } else {
                            updateValidity({ ...validity, version: versionValidity });
                        }
                    });
                } else {
                    updateValidity({ ...validity, version: versionValidity });
                }
                break;
            }
            case 'endpoint': {
                if (isWebSocket && value && value.length > 0) {
                    const wsUrlValidity = APIValidation.wsUrl.validate(value).error;
                    updateValidity({ ...validity, endpointURL: wsUrlValidity });
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    function testEndpoint(endpoint) {
        setUpdating(true);
        const restApi = new API();
        restApi.testEndpoint(endpoint)
            .then((result) => {
                if (result.body.error !== null) {
                    setStatusCode(result.body.error);
                    setIsErrorCode(true);
                } else {
                    setStatusCode(result.body.statusCode + ' ' + result.body.statusMessage);
                    setIsErrorCode(false);
                }
                if (result.body.statusCode >= 200 && result.body.statusCode < 300) {
                    setIsEndpointValid(true);
                    setIsErrorCode(false);
                } else {
                    setIsEndpointValid(false);
                }
            }).finally(() => {
                setUpdating(false);
            });
    }

    return (
        <Grid item md={11}>
            <form noValidate autoComplete='off'>
                <TextField
                    autoFocus
                    fullWidth
                    id='outlined-name'
                    error={validity.name}
                    label={(
                        <>
                            <FormattedMessage id='Apis.Create.Components.DefaultAPIForm.name' defaultMessage='Name' />
                            <sup className={classes.mandatoryStar}>*</sup>
                        </>
                    )}
                    helperText={
                        validity.name
                        && validity.name.details.map((detail, index) => {
                            return <div style={{ marginTop: index !== 0 && '10px' }}>{detail.message}</div>;
                        })
                    }
                    value={api.name}
                    name='name'
                    onChange={onChange}
                    InputProps={{
                        id: 'itest-id-apiname-input',
                        onBlur: ({ target: { value } }) => {
                            validate('name', value);
                        },
                    }}
                    margin='normal'
                    variant='outlined'
                />
                <Grid container spacing={2}>
                    {!isAPIProduct ? (
                        <>
                            <Grid item md={8} xs={6}>
                                <TextField
                                    fullWidth
                                    id='outlined-name'
                                    error={validity.context}
                                    label={(
                                        <>
                                            {isWebSocket ? (
                                                <FormattedMessage
                                                    id='Apis.Create.Components.DefaultAPIForm.api.channel'
                                                    defaultMessage='Channel'
                                                />
                                            ) : (
                                                <FormattedMessage
                                                    id='Apis.Create.Components.DefaultAPIForm.api.context'
                                                    defaultMessage='Context'
                                                />
                                            )}
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </>
                                    )}
                                    name='context'
                                    value={api.context}
                                    onChange={onChange}
                                    InputProps={{
                                        id: 'itest-id-apicontext-input',
                                        onBlur: ({ target: { value } }) => {
                                            validate('context', value);
                                        },
                                    }}
                                    helperText={
                                        (validity.context
                                            && validity.context.details.map((detail, index) => {
                                                return (
                                                    <div style={{ marginTop: index !== 0 && '10px' }}>
                                                        {detail.message}
                                                    </div>
                                                );
                                            }))
                                        // eslint-disable-next-line max-len
                                        || `API will be exposed in ${actualContext(api, isWebSocket)} context at the gateway`
                                    }
                                    classes={{ root: classes.helperTextContext }}
                                    margin='normal'
                                    variant='outlined'
                                />
                            </Grid>
                            <Grid item md={4} xs={6}>
                                <TextField
                                    fullWidth
                                    error={validity.version}
                                    label={(
                                        <>
                                            <FormattedMessage
                                                id='Apis.Create.Components.DefaultAPIForm.version'
                                                defaultMessage='Version'
                                            />
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </>
                                    )}
                                    name='version'
                                    value={api.version}
                                    onChange={onChange}
                                    InputProps={{
                                        id: 'itest-id-apiversion-input',
                                        onBlur: ({ target: { value } }) => {
                                            validate('version', value);
                                        },
                                    }}
                                    helperText={validity.version && validity.version.message}
                                    margin='normal'
                                    variant='outlined'
                                />
                            </Grid>
                        </>
                    ) : (
                        <>
                            <Grid item md={12}>
                                <TextField
                                    fullWidth
                                    id='outlined-name'
                                    error={validity.context}
                                    label={(
                                        <>
                                            <FormattedMessage
                                                id='Apis.Create.Components.DefaultAPIForm.api.product.context'
                                                defaultMessage='Context'
                                            />
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </>
                                    )}
                                    name='context'
                                    value={api.context}
                                    onChange={onChange}
                                    InputProps={{
                                        onBlur: ({ target: { value } }) => {
                                            validate('context', value);
                                        },
                                    }}
                                    helperText={
                                        (validity.context
                                            && validity.context.details.map((detail, index) => {
                                                return (
                                                    <div style={{ marginTop: index !== 0 && '10px' }}>
                                                        {detail.message}
                                                    </div>
                                                );
                                            }))
                                        || `API Product will be exposed in ${actualContext(api)} context at the gateway`
                                    }
                                    margin='normal'
                                    variant='outlined'
                                />
                            </Grid>
                        </>
                    )}
                </Grid>
                {appendChildrenBeforeEndpoint && !!children && children}
                {!isAPIProduct && !hideEndpoint && (
                    <TextField
                        fullWidth
                        id='itest-id-apiendpoint-input'
                        label='Endpoint'
                        name='endpoint'
                        value={api.endpoint}
                        onChange={onChange}
                        helperText={
                            (validity.endpointURL
                                && validity.endpointURL.details.map((detail, index) => {
                                    return (
                                        <div style={{ marginTop: index !== 0 && '10px' }}>
                                            {detail.message}
                                        </div>
                                    );
                                }))
                        }
                        error={validity.endpointURL}
                        margin='normal'
                        variant='outlined'
                        InputProps={{
                            onBlur: ({ target: { value } }) => {
                                validate('endpoint', value);
                            },
                            endAdornment: (
                                <InputAdornment position='end'>
                                    {statusCode && (
                                        <Chip
                                            label={statusCode}
                                            className={isEndpointValid ? classes.endpointValidChip : iff(
                                                isErrorCode,
                                                classes.endpointErrorChip, classes.endpointInvalidChip,
                                            )}
                                            variant='outlined'
                                        />
                                    )}
                                    {!isWebSocket && (
                                        <IconButton
                                            className={isEndpointValid ? classes.iconButtonValid : classes.iconButton}
                                            aria-label='TestEndpoint'
                                            onClick={() => testEndpoint(api.endpoint)}
                                            disabled={isUpdating}
                                        >
                                            {isUpdating
                                                ? <CircularProgress size={20} />
                                                : (
                                                    <Icon>
                                                        check_circle
                                                    </Icon>
                                                )}
                                        </IconButton>
                                    )}
                                </InputAdornment>
                            ),
                        }}
                    />
                )}

                {!appendChildrenBeforeEndpoint && !!children && children}
            </form>
            <Grid container direction='row' justify='flex-end' alignItems='center'>
                <Grid item>
                    <Typography variant='caption' display='block' gutterBottom>
                        <sup style={{ color: 'red' }}>*</sup>
                        {' '}
                        Mandatory fields
                    </Typography>
                </Grid>
            </Grid>
        </Grid>
    );
}

DefaultAPIForm.defaultProps = {
    onValidate: () => {},
    api: {}, // Uncontrolled component
    isWebSocket: false,
};
DefaultAPIForm.propTypes = {
    api: PropTypes.shape({}),
    isAPIProduct: PropTypes.shape({}).isRequired,
    isWebSocket: PropTypes.shape({}),
    onChange: PropTypes.func.isRequired,
    onValidate: PropTypes.func,
};
