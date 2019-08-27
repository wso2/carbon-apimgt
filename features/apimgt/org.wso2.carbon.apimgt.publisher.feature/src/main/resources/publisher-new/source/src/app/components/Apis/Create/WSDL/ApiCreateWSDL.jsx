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
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import Wsdl from 'AppData/Wsdl';

import ProvideWSDL from './Steps/ProvideWSDL';
import DefaultAPIForm from './Steps/DefaultAPIForm';

/**
 * Base component for all API create forms
 *
 * @param {Object} props title and children components are expected
 * @returns {React.Component} Base element
 */
function APICreateBase(props) {
    const { title, children } = props;
    return (
        <Grid container spacing={3}>
            <Grid item sm={12} md={12} />
            {/*
            Following two grids control the placement of whole create page
            For centering the content better use `container` props, but instead used an empty grid item for flexibility
             */}
            <Grid item sm={0} md={3} />
            <Grid item sm={12} md={6}>
                <Grid container spacing={5}>
                    <Grid item md={12}>
                        {title}
                    </Grid>
                    <Grid item md={12}>
                        <Paper elevation={0}>{children}</Paper>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    );
}
APICreateBase.propTypes = {
    title: PropTypes.element.isRequired,
    children: PropTypes.element.isRequired,
};
/**
 * Handle API creation from WSDL.
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function SOAPToREST() {
    const [wizardStep, setWizardStep] = useState(0);

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
            case 'policies':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'inputType':
                return { ...currentState, [action]: value, inputValue: value === 'url' ? '' : [] };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: 'SOAPtoREST',
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
        // API Name , Version & Context is a must that's why `&&` chain
        inputsDispatcher({
            action: 'isFormValid',
            value: isFormValid,
        });
    }

    /**
     *
     *
     * @param {*} params
     */
    function createAPI() {
        const {
            name, version, context, endpoint, policies,
        } = apiInputs;
        const endpointConfig = {
            endpoint_type: 'http',
            sandbox_endpoints: {
                url: endpoint,
            },
            production_endpoints: {
                url: endpoint,
            },
        };
        const promisedWSDLImport = Wsdl.import(
            apiInputs.inputValue,
            {
                name,
                version,
                context,
                endpointConfig,
                policies,
            },
            'SOAPTOREST',
        );
        promisedWSDLImport.then((response) => {
            console.log(response);
        });
        // TODO: catch error ~tmkb
    }

    return (
        <APICreateBase
            title={
                <React.Fragment>
                    <Typography variant='h5'>
                        <FormattedMessage
                            id='Apis.Create.WSDL.ApiCreateWSDL.heading'
                            defaultMessage='Create an API using WSDL'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Create.WSDL.ApiCreateWSDL.sub.heading'
                            defaultMessage={
                                'Use an existing SOAP endpoint to create a managed API.' +
                                ' Import the WSDL of the SOAP service.'
                            }
                        />
                    </Typography>
                </React.Fragment>
            }
        >
            <Paper elevation={1}>
                <Stepper activeStep={0}>
                    <Step>
                        <StepLabel>Provide WSDL</StepLabel>
                    </Step>

                    <Step>
                        <StepLabel>Create API</StepLabel>
                    </Step>
                </Stepper>
            </Paper>

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
                        <DefaultAPIForm onValidate={handleOnValidate} onChange={handleOnChange} api={apiInputs} />
                    )}
                </Grid>
                <Grid item md={1} />
                <Grid item md={9}>
                    <Grid container direction='row' justify='space-between'>
                        <Grid item>
                            {wizardStep === 0 && (
                                <Link to='/apis/'>
                                    <Button>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.Configuration.cancel'
                                            defaultMessage='Cancel'
                                        />
                                    </Button>
                                </Link>
                            )}
                            {wizardStep === 1 && <Button onClick={() => setWizardStep(step => step - 1)}>Back</Button>}
                        </Grid>
                        <Grid item>
                            {wizardStep === 0 && (
                                <Button
                                    onClick={() => setWizardStep(step => step + 1)}
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
                                    disabled={!apiInputs.isFormValid}
                                    onClick={createAPI}
                                >
                                    Create
                                </Button>
                            )}
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </APICreateBase>
    );
}
