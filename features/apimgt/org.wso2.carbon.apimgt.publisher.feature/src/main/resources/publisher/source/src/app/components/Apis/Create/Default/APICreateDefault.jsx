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
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { withRouter } from 'react-router';
import Alert from 'AppComponents/Shared/Alert';
import CircularProgress from '@material-ui/core/CircularProgress';
import API from 'AppData/api';
import { useAppContext } from 'AppComponents/Shared/AppContext';
import Banner from 'AppComponents/Shared/Banner';

import APICreateBase from 'AppComponents/Apis/Create/Components/APICreateBase';
import DefaultAPIForm from 'AppComponents/Apis/Create/Components/DefaultAPIForm';
import APIProduct from 'AppData/APIProduct';
import AuthManager from 'AppData/AuthManager';

/**
 *
 * @export
 * @param {*} props
 * @returns
 */
/**
 * Handle API creation.
 * @param {JSON} props properties passed in.
 * @returns {JSX} API creation form.
 */
function APICreateDefault(props) {
    const {
        isWebSocket, isAPIProduct, history, intl,
    } = props;
    const { settings } = useAppContext();
    const [pageError, setPageError] = useState(null);
    const [isCreating, setIsCreating] = useState();
    const [isPublishing, setIsPublishing] = useState(false);
    const [policies, setPolicies] = useState([]);

    useEffect(() => {
        API.policies('subscription').then((response) => {
            const allPolicies = response.body.list;
            if (allPolicies.length === 0) {
                Alert.info(intl.formatMessage({
                    id: 'Apis.Create.Default.APICreateDefault.error.policies.not.available',
                    defaultMessage: 'Throttling policies not available. Contact your administrator',
                }));
            } else if (allPolicies.filter((p) => p.name === 'Unlimited').length > 0) {
                setPolicies(['Unlimited']);
            } else {
                setPolicies([allPolicies[0].name]);
            }
        });
    }, []);
    const [isRevisioning, setIsRevisioning] = useState(false);
    const [isDeploying, setIsDeploying] = useState(false);
    const [isPublishButtonClicked, setIsPublishButtonClicked] = useState(false);
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
            case 'isFormValid':
                return { ...currentState, [action]: value };
            default:
                return currentState;
        }
    }
    const [apiInputs, inputsDispatcher] = useReducer(apiInputsReducer, {
        formValidity: false,
    });
    const isPublishable = apiInputs.endpoint;
    const isAPICreateDisabled = !(apiInputs.name && apiInputs.version && apiInputs.context) || isCreating
                                 || isPublishing;

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

    /**
     *
     *
     * @param {*} params
     */
    function createAPI() {
        setIsCreating(true);
        const {
            name, version, context, endpoint,
        } = apiInputs;
        let promisedCreatedAPI;
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
        if (isWebSocket) {
            apiData.type = 'WS';
        }
        if (isAPIProduct) {
            const newAPIProduct = new APIProduct(apiData);
            promisedCreatedAPI = newAPIProduct
                .saveProduct(apiData)
                .then((apiProduct) => {
                    Alert.info('API Product created successfully');
                    history.push(`/api-products/${apiProduct.id}/overview`);
                    return apiProduct;
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                        setPageError(error.response.body);
                    } else {
                        // TODO add i18n ~tmkb
                        const message = 'Something went wrong while adding the API Product';
                        Alert.error(message);
                        setPageError(message);
                    }
                    console.error(error);
                });
        } else {
            const newAPI = new API(apiData);
            promisedCreatedAPI = newAPI
                .save()
                .then((api) => {
                    Alert.info('API created successfully');
                    return api;
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                        setPageError(error.response.body);
                    } else {
                        const message = 'Something went wrong while adding the API';
                        Alert.error(message);
                        setPageError(message);
                    }
                    console.error(error);
                    setIsPublishing(false); // We don't publish if something when wrong
                })
                .finally(() => {
                    setIsCreating(false);
                });
        }
        return promisedCreatedAPI.finally(() => setIsCreating(false));
    }

    /**
     *
     */
    function createAndPublish() {
        const restApi = new API();
        setIsPublishButtonClicked(true);
        createAPI().then((api) => {
            setIsRevisioning(true);
            const body = {
                description: 'Initial Revision',
            };
            restApi.createRevision(api.id, body)
                .then((api1) => {
                    const revisionId = api1.body.id;
                    Alert.info('API Revision created successfully');
                    setIsRevisioning(false);
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
                    setIsDeploying(true);
                    restApi.deployRevision(api.id, revisionId, body1)
                        .then(() => {
                            Alert.info('API Revision Deployed Successfully');
                            setIsDeploying(false);
                            // Publishing API after deploying
                            setIsPublishing(true);
                            api.publish()
                                .then((response) => {
                                    const { workflowStatus } = response.body;
                                    if (workflowStatus === APICreateDefault.WORKFLOW_STATUS.CREATED) {
                                        Alert.info(intl.formatMessage({
                                            id: 'Apis.Create.Default.APICreateDefault.success.publishStatus',
                                            defaultMessage: 'Lifecycle state change request has been sent',
                                        }));
                                    } else {
                                        Alert.info(intl.formatMessage({
                                            id: 'Apis.Create.Default.APICreateDefault.success.otherStatus',
                                            defaultMessage: 'API updated successfully',
                                        }));
                                    }
                                    history.push(`/apis/${api.id}/overview`);
                                })
                                .catch((error) => {
                                    if (error.response) {
                                        Alert.error(error.response.body.description);
                                        setPageError(error.response.body);
                                    } else {
                                        Alert.error(intl.formatMessage({
                                            id: 'Apis.Create.Default.APICreateDefault.error.errorMessage.publish',
                                            defaultMessage: 'Something went wrong while publishing the API',
                                        }));
                                        setPageError('Something went wrong while publishing the API');
                                    }
                                    console.error(error);
                                })
                                .finally(() => {
                                    setIsPublishing(false);
                                    setIsPublishButtonClicked(false);
                                });
                        })
                        .catch((error) => {
                            if (error.response) {
                                Alert.error(error.response.body.description);
                                setPageError(error.response.body);
                            } else {
                                Alert.error(intl.formatMessage({
                                    id: 'Apis.Create.Default.APICreateDefault.error.errorMessage.deploy.revision',
                                    defaultMessage: 'Something went wrong while deploying the API Revision',
                                }));
                                setPageError('Something went wrong while deploying the API Revision');
                            }
                            console.error(error);
                        })
                        .finally(() => {
                            setIsDeploying(false);
                        });
                })
                .catch((error) => {
                    if (error.response) {
                        Alert.error(error.response.body.description);
                        setPageError(error.response.body);
                    } else {
                        Alert.error(intl.formatMessage({
                            id: 'Apis.Create.Default.APICreateDefault.error.errorMessage.create.revision',
                            defaultMessage: 'Something went wrong while creating the API Revision',
                        }));
                        setPageError('Something went wrong while creating the API Revision');
                    }
                    console.error(error);
                })
                .finally(() => {
                    setIsRevisioning(false);
                });
        });
    }

    /**
     *
     *
     */
    function createAPIOnly() {
        createAPI().then((api) => {
            history.push(`/apis/${api.id}/overview`);
        });
    }
    let pageTitle = (
        <>
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
                        'Create an API by providing a Name, a Version, a Context and'
                        + ' Backend Endpoint (optional)'
                    }
                />
            </Typography>
        </>
    );
    if (isAPIProduct) {
        pageTitle = (
            <>
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
                            'Create an API Product by providing a Name, a Context,'
                            + ' and Business Plans (optional).'
                        }
                    />
                </Typography>
            </>
        );
    } else if (isWebSocket) {
        pageTitle = (
            <>
                <Typography variant='h5'>
                    <FormattedMessage
                        id='Apis.Create.Default.APICreateDefault.webSocket.heading'
                        defaultMessage='Create a WebSocket API'
                    />
                </Typography>
                <Typography variant='caption'>
                    <FormattedMessage
                        id='Apis.Create.Default.APICreateDefault.webSocket.sub.heading'
                        defaultMessage='Create a WebSocket API by providing a Name, and a Context.'
                    />
                </Typography>
            </>
        );
    }

    return (
        <APICreateBase title={pageTitle}>
            <Grid container direction='row' justify='center' alignItems='center' spacing={3}>
                {/* Page error banner */}
                {pageError && (
                    <Grid item xs={11}>
                        <Banner
                            onClose={() => setPageError(null)}
                            disableActions
                            dense
                            paperProps={{ elevation: 1 }}
                            type='error'
                            message={pageError}
                        />
                    </Grid>
                )}
                {/* end of Page error banner */}
                <Grid item xs={12} />
                <Grid item md={1} xs={0} />
                <Grid item md={11} xs={12}>
                    <DefaultAPIForm
                        onValidate={handleOnValidate}
                        onChange={handleOnChange}
                        api={apiInputs}
                        isAPIProduct={isAPIProduct}
                        isWebSocket={isWebSocket}
                    />
                </Grid>
                <Grid item md={1} xs={0} />
                <Grid item md={11} xs={12}>
                    <Grid container direction='row' justify='flex-start' alignItems='center' spacing={2}>
                        <Grid item>
                            <Button
                                id='itest-create-default-api-button'
                                variant='contained'
                                color='primary'
                                disabled={isAPICreateDisabled || !apiInputs.isFormValid}
                                onClick={createAPIOnly}
                            >
                                Create
                                {' '}
                                {isCreating && !isPublishButtonClicked && <CircularProgress size={24} />}
                            </Button>
                        </Grid>
                        {!AuthManager.isNotPublisher() && (
                            <Grid item>
                                <Button
                                    id='itest-id-apicreatedefault-createnpublish'
                                    variant='contained'
                                    color='primary'
                                    disabled={isDeploying || isRevisioning || !isPublishable
                                        || isAPICreateDisabled || !apiInputs.isFormValid}
                                    onClick={createAndPublish}
                                >
                                    {(!isPublishing && !isRevisioning && !isDeploying) && 'Create & Publish'}
                                    {(isPublishing || isRevisioning || isDeploying) && <CircularProgress size={24} />}
                                    {isCreating && isPublishing && 'Creating API . . .'}
                                    {!isCreating && isRevisioning && !isDeploying && 'Creating Revision . . .'}
                                    {!isCreating && isPublishing
                                        && !isRevisioning && !isDeploying && 'Publishing API . . .'}
                                    {!isCreating && isPublishing
                                        && !isRevisioning && isDeploying && 'Deploying Revision . . .'}
                                </Button>
                            </Grid>
                        )}
                        <Grid item>
                            <Link to='/apis/'>
                                <Button variant='text'>
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
APICreateDefault.defaultProps = {
    isWebSocket: false,
    isAPIProduct: false,
};
APICreateDefault.WORKFLOW_STATUS = {
    CREATED: 'CREATED',
};
APICreateDefault.propTypes = {
    history: PropTypes.shape({ push: PropTypes.func }).isRequired,
    isAPIProduct: PropTypes.shape({}),
    isWebSocket: PropTypes.shape({}),
    intl: PropTypes.shape({
        formatMessage: PropTypes.func,
    }).isRequired,
};
export default withRouter(injectIntl(APICreateDefault));
