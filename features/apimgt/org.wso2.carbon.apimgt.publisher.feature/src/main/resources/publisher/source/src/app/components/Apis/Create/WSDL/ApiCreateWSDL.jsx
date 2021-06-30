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
import React, { useReducer, useState, useEffect } from 'react';
import API from 'AppData/api';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, useIntl } from 'react-intl';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import Wsdl from 'AppData/Wsdl';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';

import ProvideWSDL from './Steps/ProvideWSDL';

/**
 * Handle API creation from WSDL.
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ApiCreateWSDL(props) {
    const intl = useIntl();
    const [wizardStep, setWizardStep] = useState(0);
    const { history } = props;
    const [policies, setPolicies] = useState([]);

    useEffect(() => {
        API.policies('subscription').then((response) => {
            const allPolicies = response.body.list;
            if (allPolicies.length === 0) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Create.WSDL.ApiCreateWSDL.error.policies.not.available',
                    defaultMessage: 'Throttling policies not available. Contact your administrator',
                }));
            } else if (allPolicies.filter((p) => p.name === 'Unlimited').length > 0) {
                setPolicies(['Unlimited']);
            } else {
                setPolicies([allPolicies[0].name]);
            }
        });
    }, []);
    /**
     *
     * Reduce the events triggered from API input fields to current state
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
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'inputType':
                return { ...currentState, [action]: value, inputValue: value === 'url' ? '' : null };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: 'SOAP',
        inputType: 'url',
        inputValue: '',
        formValidity: false,
        mode: 'create',
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
            name, version, context, endpoint, type,
        } = apiInputs;
        const additionalProperties = {
            name,
            version,
            context,
            policies,
        };
        if (endpoint) {
            additionalProperties.endpointConfig = {
                endpoint_type: type === 'SOAPTOREST' ? 'address' : 'http',
                sandbox_endpoints: {
                    type: type === 'SOAPTOREST' ? 'address' : undefined,
                    url: endpoint,
                },
                production_endpoints: {
                    type: type === 'SOAPTOREST' ? 'address' : undefined,
                    url: endpoint,
                },
            };
        }
        let promisedWSDLImport;
        if (apiInputs.inputType === 'url') {
            promisedWSDLImport = Wsdl.importByUrl(apiInputs.inputValue, additionalProperties, apiInputs.type);
        } else {
            promisedWSDLImport = Wsdl.importByFileOrArchive(apiInputs.inputValue, additionalProperties, apiInputs.type);
        }
        promisedWSDLImport
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
                            id='Apis.Create.WSDL.ApiCreateWSDL.heading'
                            defaultMessage='Expose a SOAP Service as a REST API'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Create.WSDL.ApiCreateWSDL.sub.heading'
                            defaultMessage={
                                'Expose an existing SOAP service as a REST API by importing the WSDL of the '
                                + 'SOAP service.'
                            }
                        />
                    </Typography>
                </>
            )}
        >
            <Box>
                <Stepper alternativeLabel activeStep={wizardStep}>
                    <Step>
                        <StepLabel>Provide WSDL</StepLabel>
                    </Step>

                    <Step>
                        <StepLabel>Create API</StepLabel>
                    </Step>
                </Stepper>
            </Box>

            <Grid container spacing={3}>
                <Grid item md={12} />
                <Grid item md={1} />
                <Grid item md={11}>
                    {wizardStep === 0 && (
                        <ProvideWSDL
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
                <Grid item md={1} />
                <Grid item md={9}>
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
                                <Button onClick={
                                    () => setWizardStep((step) => step - 1)
                                }
                                >
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

ApiCreateWSDL.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
