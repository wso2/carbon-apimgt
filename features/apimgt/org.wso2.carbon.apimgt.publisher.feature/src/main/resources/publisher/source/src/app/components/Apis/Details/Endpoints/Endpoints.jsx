/**
 * Copyright (c)  WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, {
    useContext, useEffect, useState, useReducer,
} from 'react';
import { Grid, CircularProgress } from '@material-ui/core';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl } from 'react-intl';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import { Link, withRouter } from 'react-router-dom';
import NewEndpointCreate from 'AppComponents/Apis/Details/Endpoints/NewEndpointCreate';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import cloneDeep from 'lodash.clonedeep';
import { isRestricted } from 'AppData/AuthManager';
import EndpointOverview from './EndpointOverview';
import { createEndpointConfig, getEndpointTemplateByType } from './endpointUtils';

const styles = (theme) => ({
    endpointTypesWrapper: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
        margin: '2px',
    },
    root: {
        flexGrow: 1,
        paddingRight: '10px',
    },
    buttonSection: {
        marginTop: theme.spacing(2),
    },
    radioGroup: {
        display: 'flex',
        flexDirection: 'row',
        marginLeft: theme.spacing(2),
    },
    endpointValidityMessage: {
        color: theme.palette.error.main,
    },
    errorMessageContainer: {
        marginTop: theme.spacing(1),
    },
    implSelectRadio: {
        padding: theme.spacing(1) / 2,
    },
});

const defaultSwagger = { paths: {} };

/**
 * The base component of the endpoints view.
 * @param {any} props The props passed to the layout
 * @returns {any} HTML representation.
 */
function Endpoints(props) {
    const { classes, intl, history } = props;
    const { api, updateAPI } = useContext(APIContext);
    const [swagger, setSwagger] = useState(defaultSwagger);
    const [endpointValidity, setAPIEndpointsValid] = useState({ isValid: true, message: '' });
    const [isUpdating, setUpdating] = useState(false);

    const apiReducer = (initState, configAction) => {
        const tmpEndpointConfig = cloneDeep(initState.endpointConfig);
        const { action, value } = configAction;
        switch (action) {
            case 'production_endpoints':
            case 'sandbox_endpoints': {
                if (value) {
                    return { ...initState, endpointConfig: { ...tmpEndpointConfig, [action]: value } };
                }
                return { ...initState, endpointConfig: { ...tmpEndpointConfig } };
            }
            case 'select_endpoint_category': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'set_lb_config': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'add_endpoint': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'set_advance_config': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'remove_endpoint': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'endpointImplementationType': { // set implementation status
                const { endpointType, implementationType } = value;
                const config = createEndpointConfig(endpointType);
                if (endpointType === 'prototyped') {
                    if (implementationType === 'mock') {
                        api.generateMockScripts(api.id).then((res) => { // generates mock/sample payloads
                            setSwagger(res.obj);
                        });
                        return { ...initState, endpointConfig: config, endpointImplementationType: 'INLINE' };
                    }
                    return { ...initState, endpointConfig: config, endpointImplementationType: 'ENDPOINT' };
                }
                return { ...initState, endpointConfig: config };
            }
            case 'endpointSecurity': { // set endpoint security
                const config = cloneDeep(initState.endpointConfig);
                const tmpSecurityInfo = cloneDeep(value);
                return { ...initState, endpointConfig: { ...config, endpoint_security: tmpSecurityInfo } };
            }
            case 'endpoint_type': { // set endpoint type
                const config = getEndpointTemplateByType(
                    value.category,
                    value.endpointType === 'address',
                    tmpEndpointConfig,
                );
                return { ...initState, endpointConfig: { ...config } };
            }
            case 'set_inline': {
                const { endpointImplementationType, endpointConfig } = value;
                api.generateMockScripts(api.id).then((res) => { // generates mock/sample payloads
                    setSwagger(res.obj);
                });
                return { ...initState, endpointConfig, endpointImplementationType };
            }
            case 'set_prototyped': {
                const { endpointImplementationType, endpointConfig } = value;
                return {
                    ...initState,
                    endpointImplementationType,
                    endpointConfig,
                };
            }
            case 'set_awsCredentials': {
                return { ...initState, endpointConfig: { ...value } };
            }
            case 'select_endpoint_type': {
                const { endpointImplementationType, endpointConfig } = value;
                let { endpointSecurity } = initState;
                if (endpointSecurity && (endpointSecurity.username === '')) {
                    endpointSecurity = null;
                }
                return {
                    ...initState,
                    endpointConfig,
                    endpointImplementationType,
                    endpointSecurity: null,
                };
            }
            default: {
                return initState;
            }
        }
    };
    const [apiObject, apiDispatcher] = useReducer(apiReducer, api.toJSON());


    /**
     * Method to update the api.
     *
     * @param {boolean} isRedirect Used for dynamic endpoints to redirect to the runtime config page.
     */
    const saveAPI = (isRedirect) => {
        const { endpointConfig, endpointImplementationType, endpointSecurity } = apiObject;
        setUpdating(true);

        if (endpointConfig.endpoint_security
            && (endpointConfig.endpoint_security.production || endpointConfig.endpoint_security.sandbox)
            && endpointConfig.endpoint_security.username && endpointConfig.endpoint_security.type) {
            delete endpointConfig.endpoint_security.type;
            delete endpointConfig.endpoint_security.username;
            delete endpointConfig.endpoint_security.password;
            apiObject.endpointConfig = endpointConfig;
        }

        if (endpointImplementationType === 'INLINE') {
            api.updateSwagger(swagger).then((resp) => {
                setSwagger(resp.obj);
            }).then(() => {
                updateAPI({ endpointConfig, endpointImplementationType, endpointSecurity });
            }).finally(() => {
                setUpdating(false);
                if (isRedirect) {
                    history.push('/apis/' + api.id + '/runtime-configuration');
                }
            });
        } else {
            updateAPI(apiObject).finally(() => {
                setUpdating(false);
                if (isRedirect) {
                    history.push('/apis/' + api.id + '/runtime-configuration');
                }
            });
        }
    };

    /**
     * Validate the provided endpoint config object.
     *
     * @param {any} endpointConfig The provided endpoint config for validation.
     * @param {string} implementationType The api implementation type (INLINE/ ENDPOINT)
     * @return {{isValid: boolean, message: string}} The endpoint validity information.
     * */
    const validate = (implementationType) => {
        const { endpointConfig, endpointSecurity } = apiObject;
        if (endpointSecurity) {
            if (endpointSecurity.type === 'OAUTH') {
                if (endpointSecurity.grantType === 'PASSWORD') {
                    if (endpointSecurity.tokenUrl === null
                        || endpointSecurity.apiKey === null
                        || endpointSecurity.apiSecret === null
                        || endpointSecurity.username === null
                        || endpointSecurity.password === null) {
                        return {
                            isValid: false,
                            message: intl.formatMessage({
                                id: 'Apis.Details.Endpoints.Endpoints.missing.security.oauth.password.error',
                                defaultMessage: 'Endpoint Security Token URL'
                                        + '/API Key/API Secret/Username/Password should not be empty',
                            }),
                        };
                    }
                } else if (endpointSecurity.grantType === 'CLIENT_CREDENTIALS') {
                    if (endpointSecurity.tokenUrl === null
                        || endpointSecurity.apiKey === null
                        || endpointSecurity.apiSecret === null) {
                        return {
                            isValid: false,
                            message: intl.formatMessage({
                                id: 'Apis.Details.Endpoints.Endpoints.missing.security.oauth.client.error',
                                defaultMessage: 'Endpoint Security Token URL'
                                        + '/API Key/API Secret should not be empty',
                            }),
                        };
                    }
                }
            } else if (endpointSecurity.username === '' || endpointSecurity.password === null) {
                return {
                    isValid: false,
                    message: intl.formatMessage({
                        id: 'Apis.Details.Endpoints.Endpoints.missing.security.username.error',
                        defaultMessage: 'Endpoint Security User Name/ Password should not be empty',
                    }),
                };
            }
        }
        if (endpointConfig === null) {
            return { isValid: true, message: '' };
        }
        const endpointType = endpointConfig.endpoint_type;
        if (endpointType === 'awslambda') {
            if (endpointConfig.access_method === 'stored') {
                if (endpointConfig.amznAccessKey === '' || endpointConfig.amznSecretKey === ''
                || endpointConfig.amznRegion === '') {
                    return {
                        isValid: false,
                        message: intl.formatMessage({
                            id: 'Apis.Details.Endpoints.Endpoints.missing.accessKey.secretKey.error',
                            defaultMessage: 'Access Key, Secret Key and Region should not be empty',
                        }),
                    };
                }
            }
            if (endpointConfig.amznAccessKey !== '' && endpointConfig.amznSecretKey === 'AWS_SECRET_KEY') {
                return {
                    isValid: false,
                    message: '',
                };
            }
        } else if (endpointType === 'load_balance') {
            /**
             * Checklist:
             *  production/ sandbox endpoints should be an array.
             *  production/ sandbox endpoint [0] must be present.
             * */
            if (endpointConfig.production_endpoints && endpointConfig.production_endpoints.length > 0) {
                if (!endpointConfig.production_endpoints[0].url
                    || (endpointConfig.production_endpoints[0].url
                        && endpointConfig.production_endpoints[0].url === '')) {
                    return {
                        isValid: false,
                        message: intl.formatMessage({
                            id: 'Apis.Details.Endpoints.Endpoints.missing.prod.endpoint.loadbalance',
                            defaultMessage: 'Default Production Endpoint should not be empty',
                        }),
                    };
                }
            }
            if (endpointConfig.sandbox_endpoints && endpointConfig.sandbox_endpoints.length > 0) {
                if (!endpointConfig.sandbox_endpoints[0].url
                    || (endpointConfig.sandbox_endpoints[0].url && endpointConfig.sandbox_endpoints[0].url === '')) {
                    return {
                        isValid: false,
                        message: intl.formatMessage({
                            id: 'Apis.Details.Endpoints.Endpoints.missing.sandbox.endpoint.loadbalance',
                            defaultMessage: 'Default Sandbox Endpoint should not be empty',
                        }),
                    };
                }
            }
        } else {
            let isValidEndpoint = false;
            if (endpointConfig.implementation_status === 'prototyped') {
                if (implementationType === 'ENDPOINT') {
                    if (endpointConfig.production_endpoints && endpointConfig.production_endpoints.url === '') {
                        return {
                            isValid: false,
                            message: intl.formatMessage({
                                id: 'Apis.Details.Endpoints.Endpoints.missing.prototype.url',
                                defaultMessage: 'Prototype Endpoint URL should not be empty',
                            }),
                        };
                    }
                }
                isValidEndpoint = true;
            } else if (endpointConfig.production_endpoints && !endpointConfig.sandbox_endpoints) {
                isValidEndpoint = endpointConfig.production_endpoints.url !== '';
            } else if (endpointConfig.sandbox_endpoints && !endpointConfig.production_endpoints) {
                isValidEndpoint = endpointConfig.sandbox_endpoints.url !== '';
            } else if (!endpointConfig.sandbox_endpoints && !endpointConfig.production_endpoints) {
                isValidEndpoint = false;
            } else {
                isValidEndpoint = endpointConfig.sandbox_endpoints.url !== ''
                        || endpointConfig.production_endpoints.url !== '';
            }
            return !isValidEndpoint ? {
                isValid: false,
                message: intl.formatMessage({
                    id: 'Apis.Details.Endpoints.Endpoints.missing.endpoint.error',
                    defaultMessage: 'Either one of Production or Sandbox Endpoints should be added.',
                }),
            } : { isValid: true, message: '' };
        }
        return {
            isValid: true,
            message: '',
        };
    };

    useEffect(() => {
        if (api.type !== 'WS') {
            api.getSwagger(apiObject.id).then((resp) => {
                setSwagger(resp.obj);
            }).catch((err) => {
                console.err(err);
            });
        }
    }, []);

    useEffect(() => {
        setAPIEndpointsValid(validate(apiObject.endpointImplementationType));
    }, [apiObject]);

    const saveAndRedirect = () => {
        saveAPI(true);
    };
    /**
     * Method to update the swagger object.
     *
     * @param {any} swaggerObj The updated swagger object.
     * */
    const changeSwagger = (swaggerObj) => {
        setSwagger(swaggerObj);
    };

    /**
     * Generate endpoint configuration based on the selected endpoint type and set to the api object.
     *
     * @param {string} endpointType The endpoint type.
     * @param {string} implementationType The endpoint implementationType. (Required only for prototype endpoints)
     * */
    const generateEndpointConfig = (endpointType, implementationType) => {
        apiDispatcher({ action: 'endpointImplementationType', value: { endpointType, implementationType } });
    };

    return (
        <>
            {/* Since the api is set to the state in component did mount, check both the api and the apiObject. */}
            {(api.endpointConfig === null && apiObject.endpointConfig === null)
                ? <NewEndpointCreate generateEndpointConfig={generateEndpointConfig} apiType={apiObject.type} />
                : (
                    <div className={classes.root}>
                        <Typography variant='h4' align='left' gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Endpoints.Endpoints.endpoints.header'
                                defaultMessage='Endpoints'
                            />
                        </Typography>
                        <div>
                            <Grid container>
                                <Grid item xs={12} className={classes.endpointsContainer}>
                                    <EndpointOverview
                                        swaggerDef={swagger}
                                        updateSwagger={changeSwagger}
                                        api={apiObject}
                                        onChangeAPI={apiDispatcher}
                                        endpointsDispatcher={apiDispatcher}
                                        saveAndRedirect={saveAndRedirect}
                                    />
                                </Grid>
                            </Grid>
                            {
                                endpointValidity.isValid
                                    ? <div />
                                    : (
                                        <Grid item className={classes.errorMessageContainer}>
                                            <Typography className={classes.endpointValidityMessage}>
                                                {endpointValidity.message}
                                            </Typography>
                                        </Grid>
                                    )
                            }
                            <Grid
                                container
                                direction='row'
                                alignItems='flex-start'
                                spacing={1}
                                className={classes.buttonSection}
                            >
                                <Grid item>
                                    <Button
                                        disabled={isUpdating || !endpointValidity.isValid
                                    || isRestricted(['apim:api_create'], api)}
                                        type='submit'
                                        variant='contained'
                                        color='primary'
                                        onClick={() => saveAPI()}
                                    >
                                        <FormattedMessage
                                            id='Apis.Details.Endpoints.Endpoints.save'
                                            defaultMessage='Save'
                                        />
                                        {isUpdating && <CircularProgress size={24} />}
                                    </Button>
                                </Grid>
                                <Grid item>
                                    <Link to={'/apis/' + api.id + '/overview'}>
                                        <Button>
                                            <FormattedMessage
                                                id='Apis.Details.Endpoints.Endpoints.cancel'
                                                defaultMessage='Cancel'
                                            />
                                        </Button>
                                    </Link>
                                </Grid>
                                {isRestricted(['apim:api_create'], api)
                                && (
                                    <Grid item>
                                        <Typography variant='body2' color='primary'>
                                            <FormattedMessage
                                                id='Apis.Details.Endpoints.Endpoints.update.not.allowed'
                                                defaultMessage={'*You are not authorized to update endpoints of'
                                                + ' the API due to insufficient permissions'}
                                            />
                                        </Typography>
                                    </Grid>
                                )}
                            </Grid>
                        </div>
                    </div>
                )}
        </>

    );
}

Endpoints.propTypes = {
    classes: PropTypes.shape({
        root: PropTypes.shape({}),
        buttonSection: PropTypes.shape({}),
        endpointTypesWrapper: PropTypes.shape({}),
        mainTitle: PropTypes.shape({}),
    }).isRequired,
    api: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
    history: PropTypes.shape({}).isRequired,
};

export default withRouter(injectIntl(withStyles(styles)(Endpoints)));
