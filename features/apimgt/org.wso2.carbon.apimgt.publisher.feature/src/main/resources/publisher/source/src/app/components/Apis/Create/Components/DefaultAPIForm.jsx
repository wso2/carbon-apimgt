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
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import APIValidation from 'AppData/APIValidation';

import SelectPolicies from './SelectPolicies';

const useStyles = makeStyles(theme => ({
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
 * @returns {String} Derived actual context string
 */
function actualContext({ context, version }) {
    let initialContext = '{context}/{version}';
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
 * Improved API create default form
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DefaultAPIForm(props) {
    const {
        onChange, onValidate, api, isAPIProduct,
    } = props;
    const classes = useStyles();
    const [validity, setValidity] = useState({});

    // Check the provided API validity on mount, TODO: Better to use Joi schema here ~tmkb
    useEffect(() => {
        onValidate(Boolean(api.name) && Boolean(api.version) && Boolean(api.context));
    }, []);

    const updateValidity = (newState) => {
        let isFormValid =
            Object.entries(newState).length > 0 &&
            Object.entries(newState)
                .map(([key, value]) =>
                    value === null ||
                        value === undefined ||
                        (isAPIProduct && ['version', 'endpoints'].includes(key)))
                .reduce((acc, cVal) => acc && cVal); // Aggregate the individual validation states
        // API Name , Version & Context is a must that's why `&&` chain
        // if isAPIProduct gets true version validation has been skipped
        isFormValid =
            isFormValid && Boolean(api.name) && (isAPIProduct || Boolean(api.version)) && Boolean(api.context);
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
                const nameValidity = APIValidation.apiName.required().validate(value).error;
                if (nameValidity === null) {
                    APIValidation.apiParameter.validate(field + ':' + value).then((result) => {
                        if (result.body.list.length > 0) {
                            updateValidity({
                                ...validity,
                                name: {
                                    message: value + ' name already exists',
                                },
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
                const contextValidity = APIValidation.apiContext.required().validate(value).error;
                if (contextValidity === null) {
                    const apiContext = value.includes('/') ? value + '/' + (isAPIProduct ? '1.0.0' : api.version)
                        : '/' + value + '/' + (isAPIProduct ? '1.0.0' : api.version);
                    APIValidation.apiParameter.validate(field + ':' + apiContext).then((result) => {
                        if (result.body.list.length > 0) {
                            updateValidity({
                                ...validity,
                                context: { message: apiContext + ' context with version already exists' },
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
                    const apiVersion = api.context.includes('/')
                        ? api.context + '/' + value
                        : '/' + api.context + '/' + value;
                    APIValidation.apiParameter.validate('context:' + apiVersion).then((result) => {
                        if (result.body.list.length > 0) {
                            updateValidity({
                                ...validity,
                                version: { message: apiVersion + ' context with version already exists' },
                            });
                        } else {
                            updateValidity({ ...validity, version: versionValidity, context: null });
                        }
                    });
                } else {
                    updateValidity({ ...validity, version: versionValidity });
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    return (
        <Grid item md={11}>
            <form noValidate autoComplete='off'>
                <TextField
                    autoFocus
                    fullWidth
                    id='outlined-name'
                    error={validity.name}
                    label={
                        <React.Fragment>
                            <FormattedMessage id='Apis.Create.WSDL.Steps.DefaultAPIForm.name' defaultMessage='Name' />
                            <sup className={classes.mandatoryStar}>*</sup>
                        </React.Fragment>
                    }
                    helperText={
                        (validity.name && validity.name.message)
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
                        <React.Fragment>
                            <Grid item md={8} xs={6}>
                                <TextField
                                    fullWidth
                                    id='outlined-name'
                                    error={validity.context}
                                    label={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Create.WSDL.Steps.DefaultAPIForm.context'
                                                defaultMessage='Context'
                                            />
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </React.Fragment>
                                    }
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
                                        (validity.context && validity.context.message) ||
                                        `API will be exposed in ${actualContext(api)} context at the gateway`
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
                                    label={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Create.WSDL.Steps.DefaultAPIForm.version'
                                                defaultMessage='Version'
                                            />
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </React.Fragment>
                                    }
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
                        </React.Fragment>
                    ) : (
                        <React.Fragment>
                            <Grid item md={12}>
                                <TextField
                                    fullWidth
                                    id='outlined-name'
                                    error={validity.context}
                                    label={
                                        <React.Fragment>
                                            <FormattedMessage
                                                id='Apis.Create.WSDL.Steps.DefaultAPIForm.context'
                                                defaultMessage='Context'
                                            />
                                            <sup className={classes.mandatoryStar}>*</sup>
                                        </React.Fragment>
                                    }
                                    name='context'
                                    value={api.context}
                                    onChange={onChange}
                                    InputProps={{
                                        onBlur: ({ target: { value } }) => {
                                            validate('context', value);
                                        },
                                    }}
                                    helperText={
                                        (validity.context && validity.context.message) ||
                                        `API Product will be exposed in ${actualContext(api)} context at the gateway`
                                    }
                                    margin='normal'
                                    variant='outlined'
                                />
                            </Grid>
                        </React.Fragment>
                    )}
                </Grid>
                {!isAPIProduct && (
                    <TextField
                        fullWidth
                        id='itest-id-apiendpoint-input'
                        label='Endpoint'
                        name='endpoint'
                        value={api.endpoint}
                        onChange={onChange}
                        helperText={
                            validity.endpointURL && (
                                <span>
                                    Enter a valid {''}
                                    <a
                                        rel='noopener noreferrer'
                                        target='_blank'
                                        href='http://tools.ietf.org/html/rfc3986'
                                    >
                                        RFC 3986
                                    </a>{' '}
                                    URI
                                </span>
                            )
                        }
                        error={validity.endpointURL}
                        margin='normal'
                        variant='outlined'
                    />
                )}

                <SelectPolicies policies={api.policies} isAPIProduct={isAPIProduct} onChange={onChange} />
            </form>
            <Grid container direction='row' justify='flex-end' alignItems='center'>
                <Grid item>
                    <Typography variant='caption' display='block' gutterBottom>
                        <sup style={{ color: 'red' }}>*</sup> Mandatory fields
                    </Typography>
                </Grid>
            </Grid>
        </Grid>
    );
}

DefaultAPIForm.defaultProps = {
    onValidate: () => {},
    api: {}, // Uncontrolled component
};
DefaultAPIForm.propTypes = {
    api: PropTypes.shape({}),
    isAPIProduct: PropTypes.shape({}).isRequired,
    onChange: PropTypes.func.isRequired,
    onValidate: PropTypes.func,
};
