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
import Box from '@material-ui/core/Box';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, useIntl } from 'react-intl';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { Link } from 'react-router-dom';
import APIProduct from 'AppData/APIProduct';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import APICreateProductBase from 'AppComponents/Apis/Create/Components/APICreateProductBase';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import ProductResourcesEditWorkspace from 'AppComponents/Apis/Details/ProductResources/ProductResourcesEditWorkspace';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    Paper: {
        height: '40px',
    },
    saveButton: {
        padding: '0px 0px 0px 10px',
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        paddingBottom: theme.spacing(2),
    },
    buttonWrapper: {
        marginTop: theme.spacing(4),
    },
    alternativeLabel: {
        marginTop: theme.spacing(1),
    },
}));

/**
 * Handle API creation from GraphQL Definition.
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function ApiProductCreateWrapper(props) {
    const { history } = props;
    const intl = useIntl();
    const [wizardStep, setWizardStep] = useState(0);
    const [apiResources, setApiResources] = useState([]);
    const { settings } = useAppContext();

    const [policies, setPolicies] = useState([]);

    useEffect(() => {
        API.policies('subscription').then((response) => {
            const allPolicies = response.body.list;
            if (allPolicies.length === 0) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Create.APIProduct.APIProductCreateWrapper.error.policies.not.available',
                    defaultMessage: 'Throttling policies not available. Contact your administrator',
                }));
            } else if (allPolicies.filter((p) => p.name === 'Unlimited').length > 0) {
                setPolicies(['Unlimited']);
            } else {
                setPolicies([allPolicies[0].name]);
            }
        });
    }, []);
    const pageTitle = (
        <>
            <Typography variant='h5'>
                <FormattedMessage
                    id='Apis.Create.APIProduct.APIProductCreateWrapper.heading'
                    defaultMessage='Create an API Product'
                />
            </Typography>
            <Typography variant='caption'>
                <FormattedMessage
                    id='Apis.Create.APIProduct.APIProductCreateWrapper.sub.heading'
                    defaultMessage={
                        'Create an API Product by providing a Name, a Context, Resources, '
                        + 'and Business Plans (optional).'
                    }
                />
            </Typography>
        </>
    );
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
            case 'name':
            case 'context':
            case 'version':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            case 'apiResources':
                return { ...currentState, [action]: value };
            case 'preSetAPI':
                return {
                    ...currentState,
                    name: value.name.replace(/[&/\\#,+()$~%.'":*?<>{}\s]/g, ''),
                    context: value.context,
                };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        type: 'ApiProductCreateWrapper',
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
    function getSteps() {
        return [
            <FormattedMessage
                variant='caption'
                id='Apis.Create.APIProduct.APIProductCreateWrapper.defineProvide'
                defaultMessage='Define API Product'
            />, <FormattedMessage
                variant='caption'
                id='Apis.Create.APIProduct.APIProductCreateWrapper.resources'
                defaultMessage='Add Resources'
            />];
    }

    const [isCreating, setCreating] = useState();
    const classes = useStyles();
    const steps = getSteps();

    const createAPIProduct = () => {
        setCreating(true);
        const {
            name, context,
        } = apiInputs;
        const apiData = {
            name,
            context,
            policies,
            apis: apiResources,
        };
        apiData.transport = ['http', 'https'];
        const newAPIProduct = new APIProduct(apiData);
        newAPIProduct
            .saveProduct(apiData)
            .then((apiProduct) => {
                Alert.info('API Product created successfully');
                const body = {
                    description: 'Initial Revision',
                };
                newAPIProduct.createProductRevision(apiProduct.id, body)
                    .then((api1) => {
                        const revisionId = api1.body.id;
                        Alert.info('API Revision created successfully');
                        const envList = settings.environment.map((env) => env.name);
                        const body1 = [];
                        const getFirstVhost = (envName) => {
                            const env = settings.environment.find(
                                (e) => e.name === envName && e.vhosts.length > 0,
                            );
                            return env && env.vhosts[0].host;
                        };
                        if (envList && envList.length > 0) {
                            if (envList.includes('Default') && getFirstVhost('Default')) {
                                body1.push({
                                    name: 'Default',
                                    displayOnDevportal: true,
                                    vhost: getFirstVhost('Default'),
                                });
                            } else if (getFirstVhost(envList[0])) {
                                body1.push({
                                    name: envList[0],
                                    displayOnDevportal: true,
                                    vhost: getFirstVhost(envList[0]),
                                });
                            }
                        }
                        newAPIProduct.deployProductRevision(apiProduct.id, revisionId, body1)
                            .then(() => {
                                Alert.info('API Revision Deployed Successfully');
                            })
                            .catch((error) => {
                                if (error.response) {
                                    Alert.error(error.response.body.description);
                                } else {
                                    Alert.error(intl.formatMessage({
                                        id: 'Apis.APIProductCreateWrapper.error.errorMessage.deploy.revision',
                                        defaultMessage: 'Something went wrong while deploying the API Revision',
                                    }));
                                }
                                console.error(error);
                            });
                    })
                    .catch((error) => {
                        if (error.response) {
                            Alert.error(error.response.body.description);
                        } else {
                            Alert.error(intl.formatMessage({
                                id: 'Apis.APIProductCreateWrapper.error.errorMessage.create.revision',
                                defaultMessage: 'Something went wrong while creating the API Revision',
                            }));
                        }
                        console.error(error);
                    });
                history.push(`/api-products/${apiProduct.id}/overview`);
            })
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                } else {
                    Alert.error('Something went wrong while adding the API Product');
                }
            })
            .finally(() => setCreating(false));
    };

    return (
        <>
            <APICreateProductBase
                title={pageTitle}
            >
                <Box>
                    {wizardStep === 0 && (
                        <Stepper alternativeLabel activeStep={0}>
                            {steps.map((label) => (
                                <Step key={label}>
                                    <StepLabel className={classes.alternativeLabel}>{label}</StepLabel>
                                </Step>
                            ))}

                        </Stepper>
                    )}
                    {wizardStep === 1 && (
                        <Stepper alternativeLabel activeStep={1}>
                            {steps.map((label) => (
                                <Step key={label}>
                                    <StepLabel>{label}</StepLabel>
                                </Step>
                            ))}
                        </Stepper>
                    )}
                </Box>
                <Grid container spacing={2}>
                    {wizardStep === 0 && <Grid item md={12} />}
                    {wizardStep === 0 && <Grid item md={1} />}
                    <Grid item md={wizardStep === 0 ? 11 : 12}>
                        {wizardStep === 0 && (
                            <DefaultAPIForm
                                onValidate={handleOnValidate}
                                onChange={handleOnChange}
                                api={apiInputs}
                                isAPIProduct
                            />
                        )}
                        {wizardStep === 1 && (
                            <ProductResourcesEditWorkspace
                                apiResources={apiResources}
                                setApiResources={setApiResources}
                                isStateCreate
                                api={apiInputs}
                            />
                        )}
                    </Grid>
                    {wizardStep === 0 && <Grid item md={1} />}
                    <Grid item md={9}>
                        <Grid
                            className={wizardStep === 1 && classes.saveButton}
                            container
                            direction='row'
                            justify='flex-start'
                            alignItems='center'
                            spacing={2}
                        >
                            <Grid item>
                                {wizardStep === 1
                                    && (
                                        <Button
                                            onClick={() => setWizardStep((step) => step - 1)}
                                        >
                                            <FormattedMessage
                                                id='Apis.Create.APIProduct.APIProductCreateWrapper.back'
                                                defaultMessage='Back'
                                            />
                                        </Button>
                                    )}
                                {wizardStep === 0 && (
                                    <Link to='/api-products/'>
                                        <Button>
                                            <FormattedMessage
                                                id='Apis.Create.APIProduct.APIProductCreateWrapper.cancel'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    </Link>
                                )}
                            </Grid>
                            <Grid item>
                                {wizardStep === 1 && (
                                    <Button
                                        variant='contained'
                                        color='primary'
                                        disabled={!apiInputs.isFormValid || isCreating || (apiResources.length === 0)}
                                        onClick={createAPIProduct}
                                    >
                                        <FormattedMessage
                                            id='Apis.Create.APIProduct.APIProductCreateWrapper.publish'
                                            defaultMessage='Publish'
                                        />
                                        {isCreating && <CircularProgress size={24} />}
                                    </Button>
                                )}
                                {wizardStep === 0 && (
                                    <Button
                                        onClick={() => setWizardStep((step) => step + 1)}
                                        variant='contained'
                                        color='primary'
                                        disabled={!apiInputs.isFormValid}
                                    >
                                        <FormattedMessage
                                            id='Apis.Create.APIProduct.APIProductCreateWrapper.next'
                                            defaultMessage='Next'
                                        />
                                    </Button>
                                )}
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </APICreateProductBase>
        </>

    );
}

ApiProductCreateWrapper.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
};
