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
import React, { useReducer, useState } from 'react';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';

import ProvideOpenAPI from './Steps/ProvideOpenAPI';

/**
 * Handle API creation from OpenAPI Definition.
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ApiCreateOpenAPI(props) {
    const [wizardStep, setWizardStep] = useState(0);
    const { history } = props;

    /**
     *
     * Reduce the events triggered from API input fields to current state
     * @param {*} currentState
     * @param {*} inputAction
     * @returns
     */
    function apiInputsReducer(currentState, inputAction) {
        const { action, value } = inputAction;
        switch (action) {
            case 'type':
            case 'inputValue':
            case 'name':
            case 'version':
            case 'endpoint':
            case 'context':
            case 'policies':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'inputType':
                return { ...currentState, [action]: value, inputValue: value === 'url' ? '' : null };
            case 'preSetAPI':
                return {
                    ...currentState,
                    name: value.name.replace(/[&/\\#,+()$~%.'":*?<>{}\s]/g, ''),
                    version: value.version,
                    context: value.context,
                    endpoint: value.endpoints && value.endpoints[0],
                };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: 'ApiCreateOpenAPI',
        inputType: 'url',
        inputValue: '',
        formValidity: false,
    });

    /**
     *
     *
     * @param {*} event
     */
    function handleOnChange(event) {
        const { name: action, value } = event.target;
        inputsDispatcher({ action, value });
    }

    /**
     *
     * Set the validity of the API Inputs form
     * @param {*} isValidForm
     * @param {*} validationState
     */
    function handleOnValidate(isFormValid) {
        inputsDispatcher({
            action: 'isFormValid',
            value: isFormValid,
        });
    }

    const [isCreating, setCreating] = useState();
    /**
     *
     *
     * @param {*} params
     */
    function createAPI() {
        setCreating(true);
        const {
            name, version, context, endpoint, policies, inputValue, inputType,
        } = apiInputs;
        const additionalProperties = {
            name,
            version,
            context,
            policies,
        };
        if (endpoint) {
            additionalProperties.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: endpoint,
                },
                production_endpoints: {
                    url: endpoint,
                },
            };
        }
        const newAPI = new API(additionalProperties);
        const promisedResponse = inputType === 'file'
            ? newAPI.importOpenAPIByFile(inputValue) : newAPI.importOpenAPIByUrl(inputValue);
        promisedResponse
            .then((api) => {
                Alert.info('API created successfully');
                history.push(`/apis/${api.id}/overview`);
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while adding the API');
                }
                console.error(error);
            })
            .finally(() => setCreating(false));
    }

    return (
        <APICreateBase
            title={(
                <>
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Create.OpenAPI.ApiCreateOpenAPI.heading'
                            defaultMessage='Create an API using an OpenAPI definition.'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Create.OpenAPI.ApiCreateOpenAPI.sub.heading'
                            defaultMessage='Create an API using an existing OpenAPI definition file or URL.'
                        />
                    </Typography>
                </>
            )}
        >
            <Box>
                <Stepper alternativeLabel activeStep={0}>
                    <Step>
                        <StepLabel>
                            <FormattedMessage
                                id='Apis.Create.OpenAPI.ApiCreateOpenAPI.wizard.one'
                                defaultMessage='Provide OpenAPI'
                            />
                        </StepLabel>
                    </Step>

                    <Step>
                        <StepLabel>
                            <FormattedMessage
                                id='Apis.Create.OpenAPI.ApiCreateOpenAPI.wizard.two'
                                defaultMessage='Create API'
                            />
                        </StepLabel>
                    </Step>
                </Stepper>
            </Box>

            <Grid container spacing={3}>
                <Grid item xs={12} />
                <Grid item xs={1} />
                <Grid item xs={11}>
                    {wizardStep === 0 && (
                        <ProvideOpenAPI
                            onValidate={handleOnValidate}
                            apiInputs={apiInputs}
                            inputsDispatcher={inputsDispatcher}
                        />
                    )}
                    {wizardStep === 1 && (
                        <DefaultAPIForm
                            onValidate={handleOnValidate}
                            onChange={handleOnChange}
                            api={apiInputs}
                            isAPIProduct={false}
                        />
                    )}
                </Grid>
                <Grid item xs={1} />
                <Grid item xs={11}>
                    <Grid container direction='row' justify='flex-start' alignItems='center' spacing={2}>
                        <Grid item>
                            {wizardStep === 0 && (
                                <Link to='/apis/'>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Create.OpenAPI.ApiCreateOpenAPI.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            )}
                            {wizardStep === 1 && (
                                <Button onClick={() => setWizardStep((step) => step - 1)}>
                                    Back
                                </Button>
                            )}
                        </Grid>
                        <Grid item>
                            {wizardStep === 0 && (
                                <Button
                                    onClick={() => setWizardStep((step) => step + 1)}
                                    variant='contained'
                                    color='primary'
                                    disabled={!apiInputs.isFormValid}
                                >
                                    Next
                                </Button>
                            )}
                            {wizardStep === 1 && (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    disabled={!apiInputs.isFormValid || isCreating}
                                    onClick={createAPI}
                                >
                                    Create
                                    {' '}
                                    {isCreating && <CircularProgress size={24} />}
                                </Button>
                            )}
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </APICreateBase>
    );
}

ApiCreateOpenAPI.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
