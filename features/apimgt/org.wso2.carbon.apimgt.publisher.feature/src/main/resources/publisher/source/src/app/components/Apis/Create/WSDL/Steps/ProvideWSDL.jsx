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
import Radio from '@material-ui/core/Radio';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import { makeStyles } from '@material-ui/core/styles';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import InputAdornment from '@material-ui/core/InputAdornment';

import APIValidation from 'AppData/APIValidation';
import Wsdl from 'AppData/Wsdl';
import DropZoneLocal from 'AppComponents/Shared/DropZoneLocal';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import CheckIcon from '@material-ui/icons/Check';

const useStyles = makeStyles(theme => ({
    mandatoryStar: {
        color: theme.palette.error.main,
    },
}));

/**
 * Sub component of API Create using WSDL UI, This is handling the taking input of WSDL file or URL from the user
 * In the create API using WSDL wizard first step out of 2 steps
 * @export
 * @param {*} props
 * @returns {React.Component} @inheritdoc
 */
export default function ProvideWSDL(props) {
    const { apiInputs, inputsDispatcher, onValidate } = props;
    const isFileInput = apiInputs.inputType === 'file';
    const isArchiveInput = apiInputs.inputType === 'archive';
    const isGenerateRESTAPI = apiInputs.type === 'SOAPTOREST';
    const classes = useStyles();
    const [isError, setValidity] = useState(); // If valid value is `null` else an error object will be there
    const [isValidating, setIsValidating] = useState(false);

    /**
     * Handles WSDL validation response and returns the state.
     *
     * @param {*} response WSDL validation response
     * @param {string} type of the input; file or url
     * @returns {boolean} validation status
     */
    function handleWSDLValidationResponse(response, type) {
        const isWSDLValid = response.body.isValid;
        let success = false;
        if (isWSDLValid) {
            if (type === 'file') {
                setValidity({ ...isError, file: null });
            } else {
                setValidity({ ...isError, url: null });
            }
            success = true;
        } else if (type === 'file') {
            setValidity({ ...isError, file: { message: 'WSDL content validation failed!' } });
        } else {
            setValidity({ ...isError, url: { message: 'Invalid WSDL URL!' } });
        }
        onValidate(isWSDLValid);
        setIsValidating(false);
        return success;
    }

    /**
     * Handles WSDL validation error response.
     *
     * @param error {*} error object
     * @param type {string} file/url type
     */
    function handleWSDLValidationErrorResponse(error, type) {
        let message = 'Error occurred during validation';
        if (error.response && error.response.body.description) {
            message = error.response.body.description;
        }
        if (type === 'file') {
            setValidity({ ...isError, file: { message } });
        } else {
            setValidity({ ...isError, url: { message } });
        }
        setIsValidating(false);
    }

    /**
     * Trigger the onValidate call back after validating WSDL url from the state.
     * Do the validation state aggregation and call the onValidate method with aggregated value
     * @param {Object} state Validation state object
     */
    function validateUrl(state) {
        if (state === null) {
            setIsValidating(true);
            Wsdl.validateUrl(apiInputs.inputValue).then((response) => {
                handleWSDLValidationResponse(response, 'url');
            }).catch((error) => {
                handleWSDLValidationErrorResponse(error, 'url');
            });
        } else {
            setValidity({ ...isError, url: state });
            onValidate(false);
        }
    }

    /**
     * Trigger the provided onValidate callback after validating the provided WSDL file.
     * Do the validation state aggregation and call the onValidate method with aggregated value
     * @param {*} file WSDL file or archive
     * @param {Object} state Validation state object
     */
    function validateFileOrArchive(file, state = null) {
        if (state === null) {
            setIsValidating(true);
            Wsdl.validateFileOrArchive(file).then((response) => {
                if (handleWSDLValidationResponse(response, 'file')) {
                    inputsDispatcher({ action: 'inputValue', value: file });
                }
            }).catch((error) => {
                handleWSDLValidationErrorResponse(error, 'file');
            });
        } else {
            setValidity({ ...isError, file: state });
            onValidate(false);
        }
    }

    /**
     *
     *
     * @param {*} files
     */
    function onDrop(files) {
        // Why `files[0]` below is , We only handle one wsdl file at a time, So if use provide multiple, We would only
        // accept the first file. This information is shown in the dropdown helper text
        validateFileOrArchive(files[0]);
    }

    let urlStateEndAdornment = null;
    if (isValidating) {
        urlStateEndAdornment = (
            <InputAdornment position='end'>
                <CircularProgress />
            </InputAdornment>
        );
    } else if (isError && isError.url) {
        urlStateEndAdornment = (
            <InputAdornment position='end'>
                <ErrorOutlineIcon fontSize='large' color='error' />
            </InputAdornment>
        );
    } else if (isError && !isError.url) {
        urlStateEndAdornment = (
            <InputAdornment position='end'>
                <CheckIcon fontSize='large' color='primary' />
            </InputAdornment>
        );
    }

    const fileControlLabel = isGenerateRESTAPI ? (<FormattedMessage
        id='Apis.Create.WSDL.Steps.ProvideWSDL.file.label.wsdl.file'
        defaultMessage='WSDL File'
    />) : (<FormattedMessage
        id='Apis.Create.WSDL.Steps.ProvideWSDL.file.label.wsdl.file.archive'
        defaultMessage='WSDL File/Archive'
    />);

    const dropBoxControlLabel = isGenerateRESTAPI ? (<FormattedMessage
        id='Apis.Create.WSDL.Steps.ProvideWSDL.Input.file.dropzone'
        defaultMessage='Select a WSDL file'
    />) : (<FormattedMessage
        id='Apis.Create.WSDL.Steps.ProvideWSDL.Input.file.archive.dropzone'
        defaultMessage='Select a WSDL file/archive'
    />);

    return (
        <React.Fragment>
            <Grid container spacing={5}>
                <Grid item md={12}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>
                            <React.Fragment>
                                <sup className={classes.mandatoryStar}>*</sup>{' '}
                                <FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.implementation.type'
                                    defaultMessage='Implementation type'
                                />
                            </React.Fragment>
                        </FormLabel>
                        <RadioGroup
                            aria-label='Implementation type'
                            value={apiInputs.type}
                            onChange={event => inputsDispatcher({ action: 'type', value: event.target.value })}
                        >
                            <FormControlLabel
                                value='SOAP'
                                control={<Radio color='primary' />}
                                label={<FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.passthrough.label'
                                    defaultMessage='Pass Through'
                                />}
                            />
                            <FormControlLabel
                                value='SOAPTOREST'
                                control={<Radio color='primary' />}
                                label={<FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.SOAPtoREST.label'
                                    defaultMessage='Generate REST APIs'
                                />}
                            />
                        </RadioGroup>
                        <FormHelperText>
                            <sup>*</sup>
                            <b>Generate REST APIs</b> option is only available for WSDL URL input type
                        </FormHelperText>
                    </FormControl>
                </Grid>
                <Grid item md={12}>
                    <FormControl component='fieldset'>
                        <FormLabel component='legend'>
                            <React.Fragment>
                                <sup className={classes.mandatoryStar}>*</sup>{' '}
                                <FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.Input.type'
                                    defaultMessage='Input type'
                                />
                            </React.Fragment>
                        </FormLabel>
                        <RadioGroup
                            aria-label='Input type'
                            value={apiInputs.inputType}
                            onChange={event => inputsDispatcher({ action: 'inputType', value: event.target.value })}
                        >
                            <FormControlLabel
                                value='url'
                                control={<Radio color='primary' />}
                                label={<FormattedMessage
                                    id='Apis.Create.WSDL.Steps.ProvideWSDL.url.label'
                                    defaultMessage='WSDL URL'
                                />}
                            />
                            <FormControlLabel
                                value='file'
                                control={<Radio color='primary' />}
                                label={fileControlLabel}
                            />
                        </RadioGroup>
                    </FormControl>
                </Grid>
                <Grid item md={7}>
                    {(isFileInput || isArchiveInput) ? (
                        // TODO: Pass message saying accepting only one file ~tmkb
                        <DropZoneLocal onDrop={onDrop} files={apiInputs.inputValue} error={isError && isError.file}>
                            {isValidating && <CircularProgress />}
                            {isError && isError.file ? (
                                isError.file.message
                            ) : dropBoxControlLabel}
                        </DropZoneLocal>
                    ) : (
                        <TextField
                            autoFocus
                            id='outlined-full-width'
                            label='WSDL URL'
                            placeholder='Enter WSDL URL'
                            fullWidth
                            margin='normal'
                            variant='outlined'
                            onChange={({ target: { value } }) => inputsDispatcher({ action: 'inputValue', value })}
                            value={apiInputs.inputValue}
                            InputLabelProps={{
                                shrink: true,
                            }}
                            InputProps={{
                                onBlur: ({ target: { value } }) => {
                                    validateUrl(APIValidation.url.required().validate(value).error);
                                },
                                endAdornment: urlStateEndAdornment,
                            }}
                            helperText={
                                (isError && isError.url && isError.url.message) || 'Click away to validate the URL'}
                            error={isError && Boolean(isError.url)}
                            disabled={isValidating}
                        />
                    )}
                </Grid>
            </Grid>
        </React.Fragment>
    );
}

ProvideWSDL.defaultProps = {
    onValidate: () => { },
};
ProvideWSDL.propTypes = {
    apiInputs: PropTypes.shape({
        type: PropTypes.string,
        inputType: PropTypes.string,
    }).isRequired,
    inputsDispatcher: PropTypes.func.isRequired,
    onValidate: PropTypes.func,
};
