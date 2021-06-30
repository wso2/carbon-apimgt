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
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';

import ProvideGraphQL from './Steps/ProvideGraphQL';

/**
 * Handle API creation from GraphQL Definition.
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ApiCreateGraphQL(props) {
    const intl = useIntl();
    const [wizardStep, setWizardStep] = useState(0);
    const { history } = props;
    const [policies, setPolicies] = useState([]);

    useEffect(() => {
        API.policies('subscription').then((response) => {
            const allPolicies = response.body.list;
            if (allPolicies.length === 0) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Create.GraphQL.ApiCreateGraphQL.error.policies.not.available',
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
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'inputType':
                return { ...currentState, [action]: value, inputValue: value === 'url' ? '' : null };
            case 'graphQLInfo':
                return { ...currentState, [action]: value };
            case 'preSetAPI':
                return {
                    ...currentState,
                    name: value.name.replace(/[&/\\#,+()$~%.'":*?<>{}\s]/g, ''),
                    version: value.version,
                    context: value.context,
                };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: 'ApiCreateGraphQL',
        inputType: 'file',
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
            name,
            version,
            context,
            endpoint,
            implementationType,
            inputValue,
            graphQLInfo: { operations },
        } = apiInputs;
        const additionalProperties = {
            name,
            version,
            context,
            policies,
            operations,
        };
        const uploadMethod = 'file';
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
        const newApi = new API(additionalProperties);
        const apiData = {
            additionalProperties: JSON.stringify(additionalProperties),
            implementationType,
            [uploadMethod]: uploadMethod,
            file: inputValue,
        };

        newApi
            .importGraphQL(apiData)
            .then((response) => {
                const uuid = response.obj.id;
                Alert.info(`${name} API created successfully`);
                history.push(`/apis/${uuid}/overview`);
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
                            id='Apis.Create.GraphQL.ApiCreateGraphQL.heading'
                            defaultMessage='Create an API using a GraphQL SDL definition'
                        />
                    </Typography>
                    <Typography variant='caption'>
                        <FormattedMessage
                            id='Apis.Create.GraphQL.ApiCreateGraphQL.sub.heading'
                            defaultMessage='Create an API by importing an existing GraphQL SDL definition.'
                        />
                    </Typography>
                </>
            )}
        >
            <Box>
                {wizardStep === 0 && (
                    <Stepper alternativeLabel activeStep={0}>
                        <Step>
                            <StepLabel>
                                <FormattedMessage
                                    id='Apis.Create.GraphQL.ApiCreateGraphQL.wizard.one'
                                    defaultMessage='Provide GraphQL'
                                />
                            </StepLabel>
                        </Step>

                        <Step>
                            <StepLabel>
                                <FormattedMessage
                                    id='Apis.Create.GraphQL.ApiCreateGraphQL.wizard.two'
                                    defaultMessage='Create API'
                                />
                            </StepLabel>
                        </Step>
                    </Stepper>
                )}
                {wizardStep === 1 && (
                    <Stepper alternativeLabel activeStep={1}>
                        <Step>
                            <StepLabel>
                                <FormattedMessage
                                    id='Apis.Create.GraphQL.ApiCreateGraphQL.wizard.one'
                                    defaultMessage='Provide GraphQL'
                                />
                            </StepLabel>
                        </Step>

                        <Step>
                            <StepLabel>
                                <FormattedMessage
                                    id='Apis.Create.GraphQL.ApiCreateGraphQL.wizard.two'
                                    defaultMessage='Create API'
                                />
                            </StepLabel>
                        </Step>
                    </Stepper>
                )}
            </Box>

            <Grid container spacing={3} style={{ marginBottom: 20 }}>
                <Grid item md={12} />
                <Grid item md={1} />
                <Grid item md={11}>
                    {wizardStep === 0 && (
                        <ProvideGraphQL
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
                                    <FormattedMessage
                                        id='Apis.Create.GraphQL.ApiCreateGraphQL.back'
                                        defaultMessage='Back'
                                    />
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
                                    <FormattedMessage
                                        id='Apis.Create.GraphQL.ApiCreateGraphQL.next'
                                        defaultMessage='Next'
                                    />
                                </Button>
                            )}
                            {wizardStep === 1 && (
                                <Button
                                    variant='contained'
                                    color='primary'
                                    disabled={!apiInputs.isFormValid || isCreating}
                                    onClick={createAPI}
                                >
                                    <FormattedMessage
                                        id='Apis.Create.GraphQL.ApiCreateGraphQL.create'
                                        defaultMessage='Create'
                                    />
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

ApiCreateGraphQL.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
