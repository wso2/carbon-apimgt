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
import React, { useState } from 'react';
import PropTypes from 'prop-types';
import TextField from '@material-ui/core/TextField';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import APIValidation from 'AppData/APIValidation';

import SelectPolicies from './components/SelectPolicies';

const useStyles = makeStyles(theme => ({
    mandatoryStar: {
        color: theme.palette.error.main,
    },
}));

/**
 * Improved API create default form
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function DefaultAPIForm(props) {
    const { onChange, onValidate, api } = props;
    const classes = useStyles();
    const [validation, setValidation] = useState({});

    /**
     * Trigger the provided onValidate call back on each input validation run
     * Do the validation state aggregation and call the onValidate method with aggregated value
     * @param {Object} state Validation state object
     */
    function validate(state) {
        setValidation(state);
        const isFormValid = Object.values(state)
            .map(value => value === null || value === undefined) // Map the validation entries to booleans
            .reduce((acc, cVal) => acc && cVal); // Aggregate the individual validation states
        onValidate(isFormValid, state);
    }
    return (
        <Grid item md={9}>
            <form noValidate autoComplete='off'>
                <TextField
                    autoFocus
                    fullWidth
                    id='outlined-name'
                    error={validation.name}
                    label={
                        <React.Fragment>
                            <sup className={classes.mandatoryStar}>*</sup>{' '}
                            <FormattedMessage id='Apis.Create.WSDL.Steps.DefaultAPIForm.name' defaultMessage='Name' />
                        </React.Fragment>
                    }
                    helperText={
                        (validation.name && validation.name.message) ||
                        'API name can not contain spaces or any special characters'
                    }
                    value={api.name}
                    name='name'
                    onChange={onChange}
                    InputProps={{
                        onBlur: ({ target: { value } }) => {
                            validate({
                                ...validation,
                                name: APIValidation.apiName.required().validate(value).error,
                            });
                        },
                    }}
                    margin='normal'
                    variant='outlined'
                />
                <Grid container spacing={2}>
                    <Grid item md={4}>
                        <TextField
                            fullWidth
                            error={validation.version}
                            id='outlined-name'
                            label={
                                <React.Fragment>
                                    <sup className={classes.mandatoryStar}>*</sup>{' '}
                                    <FormattedMessage
                                        id='Apis.Create.WSDL.Steps.DefaultAPIForm.version'
                                        defaultMessage='Version'
                                    />
                                </React.Fragment>
                            }
                            name='version'
                            value={api.version}
                            onChange={onChange}
                            InputProps={{
                                onBlur: ({ target: { value } }) => {
                                    validate({
                                        ...validation,
                                        version: APIValidation.apiVersion.required().validate(value).error,
                                    });
                                },
                            }}
                            helperText={validation.version && validation.version.message}
                            margin='normal'
                            variant='outlined'
                        />
                    </Grid>
                    <Grid item md={8}>
                        <TextField
                            fullWidth
                            id='outlined-name'
                            error={validation.context}
                            label={
                                <React.Fragment>
                                    <sup className={classes.mandatoryStar}>*</sup>{' '}
                                    <FormattedMessage
                                        id='Apis.Create.WSDL.Steps.DefaultAPIForm.context'
                                        defaultMessage='Context'
                                    />
                                </React.Fragment>
                            }
                            name='context'
                            value={api.context}
                            onChange={onChange}
                            InputProps={{
                                onBlur: ({ target: { value } }) => {
                                    validate({
                                        ...validation,
                                        context: APIValidation.apiContext.required().validate(value).error,
                                    });
                                },
                            }}
                            helperText={
                                (validation.context && validation.context.message) ||
                                'API will be exposed in this context at the gateway'
                            }
                            margin='normal'
                            variant='outlined'
                        />
                    </Grid>
                </Grid>
                <TextField
                    fullWidth
                    id='outlined-name'
                    label='Endpoint'
                    name='endpoint'
                    value={api.endpoint}
                    onChange={onChange}
                    InputProps={{
                        onBlur: ({ target: { value } }) => {
                            validate({
                                ...validation,
                                endpointURL: value ? APIValidation.url.validate(value).error : null,
                            });
                        },
                    }}
                    helperText={
                        validation.endpointURL && (
                            <span>
                                Enter a valid {''}
                                <a rel='noopener noreferrer' target='_blank' href='http://tools.ietf.org/html/rfc3986'>
                                    RFC 3986
                                </a>{' '}
                                URI
                            </span>
                        )
                    }
                    error={validation.endpointURL}
                    margin='normal'
                    variant='outlined'
                />

                <SelectPolicies policies={api.policies} onChange={onChange} />
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
    onChange: PropTypes.func.isRequired,
    onValidate: PropTypes.func,
};
