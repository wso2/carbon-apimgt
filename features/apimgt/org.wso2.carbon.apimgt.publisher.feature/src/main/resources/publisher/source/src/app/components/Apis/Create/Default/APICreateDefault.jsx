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
import Grid from '@material-ui/core/Grid';
import { FormattedMessage } from 'react-intl';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { withRouter } from 'react-router';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import API from 'AppData/api';
import { useAppContext } from 'AppComponents/Shared/AppContext';

import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import APIProduct from 'AppData/APIProduct';

/**
 * Handle API creation from WSDL.
 *
 * @export
 * @param {*} props
 * @returns
 */
function APICreateDefault(props) {
    const { settings } = useAppContext();
    /**
     *
     * Reduce the events triggered from API input fields to current state
     */
    function apiInputsReducer(currentState, inputAction) {
        const { action, value } = inputAction;
        switch (action) {
            case 'name':
            case 'version':
            case 'endpoint':
            case 'context':
            case 'policies':
            case 'isFormValid':
                return { ...currentState, [action]: value };
            default:
                return currentState;
        }
    }

    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
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
            name, version, context, endpoint, policies,
        } = apiInputs;
        const apiData = {
            name,
            version,
            context,
            policies,
        };
        if (endpoint) {
            apiData.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: endpoint,
                },
                production_endpoints: {
                    url: endpoint,
                },
            };
        }
        apiData.gatewayEnvironments = settings.environment.map(env => env.name);
        if (props.isAPIProduct) {
            const newAPIProduct = new APIProduct(apiData);
            newAPIProduct
                .saveProduct(apiData)
                .then((apiProduct) => {
                    Alert.info('API Product created successfully');
                    props.history.push(`/api-products/${apiProduct.id}/overview`);
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                    } else {
                        Alert.error('Something went wrong while adding the API Product');
                    }
                })
                .finally(() => setCreating(false));
        } else {
            const newAPI = new API(apiData);
            newAPI
                .save()
                .then((api) => {
                    Alert.info('API created successfully');
                    props.history.push(`/apis/${api.id}/overview`);
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
    }

    return (
        <APICreateBase
            title={
                props.isAPIProduct ? (
                    <React.Fragment>
                        <Typography variant='h5'>
                            <FormattedMessage
                                id='Apis.Create.Default.APICreateDefault.apiProduct.heading'
                                defaultMessage='Create an API Product'
                            />
                        </Typography>
                        <Typography variant='caption'>
                            <FormattedMessage
                                id='Apis.Create.Default.APICreateDefault.apiProduct.sub.heading'
                                defaultMessage={
                                    'Create an API Product providing Name, Context parameters' +
                                    ' and optionally bushiness plans'
                                }
                            />
                        </Typography>
                    </React.Fragment>
                ) : (
                    <React.Fragment>
                        <Typography variant='h5'>
                            <FormattedMessage
                                id='Apis.Create.Default.APICreateDefault.api.heading'
                                defaultMessage='Create an API'
                            />
                        </Typography>
                        <Typography variant='caption'>
                            <FormattedMessage
                                id='Apis.Create.Default.APICreateDefault.api.sub.heading'
                                defaultMessage={
                                    'Create an API providing Name, Version and Context parameters' +
                                    ' and optionally backend endpoint and bushiness plans'
                                }
                            />
                        </Typography>
                    </React.Fragment>
                )
            }
        >
            <Grid container spacing={3}>
                <Grid item md={12} />
                <Grid item md={1} />
                <Grid item md={11}>
                    <DefaultAPIForm
                        onValidate={handleOnValidate}
                        onChange={handleOnChange}
                        api={apiInputs}
                        isAPIProduct={props.isAPIProduct}
                    />
                </Grid>
                <Grid item md={1} />
                <Grid item md={9}>
                    <Grid container direction='row' justify='flex-start' alignItems='center' spacing={2}>
                        <Grid item>
                            <Button
                                variant='contained'
                                color='primary'
                                disabled={!apiInputs.isFormValid || isCreating}
                                onClick={createAPI}
                            >
                                Create {isCreating && <CircularProgress size={24} />}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Link to='/apis/'>
                                <Button variant='outlined'>
                                    <FormattedMessage
                                        id='Apis.Create.Default.APICreateDefault.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </APICreateBase>
    );
}
APICreateDefault.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    isAPIProduct: PropTypes.shape({}).isRequired,
};
export default withRouter(APICreateDefault);
