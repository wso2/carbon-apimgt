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

import React, {
    useReducer, useContext, useState, useEffect,
} from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { Link } from 'react-router-dom';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import { FormattedMessage, useIntl } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';
import ArrowForwardIcon from '@material-ui/icons/ArrowForward';
import ArrowBackIcon from '@material-ui/icons/ArrowBack';
import isEmpty from 'lodash/isEmpty';
import cloneDeep from 'lodash.clonedeep';
import Api from 'AppData/api';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import { isRestricted } from 'AppData/AuthManager';
import ResponseCaching from './components/ResponseCaching';
import CORSConfiguration from './components/CORSConfiguration';
import SchemaValidation from './components/SchemaValidation';
import MaxBackendTps from './components/MaxBackendTps';
import Flow from './components/Flow';
import Endpoints from './components/Endpoints';
import APISecurity from './components/APISecurity/APISecurity';
import QueryAnalysis from './components/QueryAnalysis';
import {
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_API_KEY,
    API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
    API_SECURITY_MUTUAL_SSL,
} from './components/APISecurity/components/apiSecurityConstants';

const useStyles = makeStyles((theme) => ({
    root: {
        padding: theme.spacing(3, 2),
    },
    titleWrapper: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: theme.spacing(3),
    },
    mainTitle: {
        paddingLeft: 0,
    },
    paper: {
        padding: theme.spacing(3),
    },
    paperCenter: {
        padding: theme.spacing(3),
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
    },
    heading: {
        fontSize: '1.1rem',
        fontWeight: 400,
        marginBottom: theme.spacing(0),
    },
    itemPadding: {
        marginBottom: theme.spacing(3),
    },
    arrowForwardIcon: {
        fontSize: 50,
        color: '#ccc',
        position: 'absolute',
        top: 90,
        right: -43,
    },
    arrowBackIcon: {
        fontSize: 50,
        color: '#ccc',
        position: 'absolute',
        top: 30,
        right: -71,
    },
    expansionPanel: {
        marginBottom: theme.spacing(1),
    },
    expansionPanelDetails: {
        flexDirection: 'column',
    },
    subHeading: {
        fontSize: '1rem',
        fontWeight: 400,
        margin: 0,
        display: 'inline-flex',
        lineHeight: '38px',
    },
    info: {
        display: 'flex',
        height: '100%',
    },
}));

/**
 *
 * Deep coping the properties in API object (what ever the object in the state),
 * making sure that no direct mutations happen when updating the state.
 * You should know the shape of the object that you are keeping in the state,
 * @param {Object} api
 * @returns {Object} Deep copy of an object
 */
function copyAPIConfig(api) {
    const apiConfigJson = {
        id: api.id,
        name: api.name,
        description: api.description,
        lifeCycleStatus: api.lifeCycleStatus,
        accessControl: api.accessControl,
        authorizationHeader: api.authorizationHeader,
        responseCachingEnabled: api.responseCachingEnabled,
        cacheTimeout: api.cacheTimeout,
        visibility: api.visibility,
        isDefaultVersion: api.isDefaultVersion,
        enableSchemaValidation: api.enableSchemaValidation,
        accessControlRoles: [...api.accessControlRoles],
        visibleRoles: [...api.visibleRoles],
        tags: [...api.tags],
        maxTps: api.maxTps,
        wsdlUrl: api.wsdlUrl,
        transport: [...api.transport],
        securityScheme: [...api.securityScheme],
        keyManagers: [...api.keyManagers || []],
        corsConfiguration: {
            corsConfigurationEnabled: api.corsConfiguration.corsConfigurationEnabled,
            accessControlAllowCredentials: api.corsConfiguration.accessControlAllowCredentials,
            accessControlAllowOrigins: [...api.corsConfiguration.accessControlAllowOrigins],
            accessControlAllowHeaders: [...api.corsConfiguration.accessControlAllowHeaders],
            accessControlAllowMethods: [...api.corsConfiguration.accessControlAllowMethods],
        },
    };
    return apiConfigJson;
}
/**
 * This component handles the basic configurations UI in the API details page
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function RuntimeConfiguration() {
    const [keyManagersConfigured, setKeyManagersConfigured] = useState([]);
    /**
     *
     * Reduce the configuration UI related actions in to updated state
     * @param {*} state current state
     * @param {*} configAction dispatched configuration action
     * @returns {Object} updated state
     */
    function configReducer(state, configAction) {
        const { action, value, event } = configAction;
        const nextState = { ...copyAPIConfig(state) };
        switch (action) {
            case 'description':
            case 'isDefaultVersion':
            case 'authorizationHeader':
            case 'responseCachingEnabled':
            case 'cacheTimeout':
            case 'enableSchemaValidation':
            case 'accessControl':
            case 'visibility':
            case 'maxTps':
            case 'tags':
                nextState[action] = value;
                return nextState;
            case 'accessControlRoles':
                return { ...copyAPIConfig(state), [action]: value };
            case 'visibleRoles':
                return { ...copyAPIConfig(state), [action]: value };
            case 'securityScheme':
                // If event came from mandatory selector of either Application level or Transport level
                if (
                    [API_SECURITY_MUTUAL_SSL_MANDATORY, API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY]
                        .includes(event.name)
                ) {
                    // If user select not mandatory (optional) , Remove the respective schema, else add it
                    if (event.value === 'optional') {
                        return {
                            ...copyAPIConfig(state),
                            [action]: state[action].filter((schema) => schema !== event.name),
                        };
                    } else if (state[action].includes(event.name)) {
                        return state; // Add for completeness, Ideally there couldn't exist this state
                    } else {
                        return { ...copyAPIConfig(state), [action]: [...state[action], event.name] };
                    }
                }
                // User checked on one of api security schemas (either OAuth, Basic, ApiKey or Mutual SSL)
                if (event.checked) {
                    if (state[action].includes(event.value)) {
                        return state; // Add for completeness, Ideally there couldn't exist this state
                    } else {
                        return { ...copyAPIConfig(state), [action]: [...state[action], event.value] };
                    }
                } else if (state[action].includes(event.value)) {
                    // User has unchecked a security schema type
                    const newState = {
                        ...copyAPIConfig(state),
                        [action]: state[action].filter((schema) => schema !== event.value),
                    };
                    if (
                        !(
                            newState[action].includes(DEFAULT_API_SECURITY_OAUTH2)
                            || newState[action].includes(API_SECURITY_BASIC_AUTH)
                            || newState[action].includes(API_SECURITY_API_KEY)
                        )
                    ) {
                        const noMandatoryOAuthBasicAuth = newState[action]
                            .filter((schema) => schema !== API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
                        return {
                            ...newState,
                            [action]: noMandatoryOAuthBasicAuth,
                        };
                    } else if (!newState[action].includes(API_SECURITY_MUTUAL_SSL)) {
                        const noMandatoryMutualSSL = newState[action]
                            .filter((schema) => schema !== API_SECURITY_MUTUAL_SSL_MANDATORY);
                        return {
                            ...newState,
                            [action]: noMandatoryMutualSSL,
                        };
                    }

                    return newState;
                } else {
                    return state; // Add for completeness, Ideally there couldn't exist this state
                }
            case 'transport':
                if (event.checked) {
                    return { ...copyAPIConfig(state), transport: [...state.transport, event.value] };
                } else {
                    return {
                        ...copyAPIConfig(state),
                        transport: state.transport.filter((transport) => transport !== event.value),
                    };
                }
            case 'accessControlAllowHeaders':
            case 'accessControlAllowMethods':
            case 'accessControlAllowCredentials':
            case 'corsConfigurationEnabled':
                nextState.corsConfiguration[action] = value;
                return nextState;
            case 'accessControlAllowOrigins':
                if (event.checked) {
                    nextState.corsConfiguration[action] = [event.value];
                } else {
                    nextState.corsConfiguration[action] = event.checked === false ? [] : event.value;
                }
                return nextState;
            case 'keymanagers':
                nextState.keyManagers = value;
                return nextState;
            case 'allKeyManagersEnabled':
                if (value) {
                    nextState.keyManagers = ['all'];
                } else {
                    nextState.keyManagers = keyManagersConfigured;
                }
                return nextState;
            default:
                return state;
        }
    }
    const { api, updateAPI } = useContext(APIContext);
    const [isUpdating, setIsUpdating] = useState(false);
    const [updateComplexityList, setUpdateComplexityList] = useState(null);
    const [apiConfig, configDispatcher] = useReducer(configReducer, copyAPIConfig(api));
    const classes = useStyles();
    const mediationPolicies = cloneDeep(api.mediationPolicies || []);
    const [inPolicy, setInPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'IN')[0]);
    const [outPolicy, setOutPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'OUT')[0]);
    const [faultPolicy, setFaultPolicy] = useState(mediationPolicies.filter((seq) => seq.type === 'FAULT')[0]);
    const intl = useIntl();
    useEffect(() => {
        if (!isRestricted(['apim:api_create'], api)) {
            Api.keyManagers().then((response) => {
                const kmNameList = [];
                if (response.body.list) {
                    response.body.list.forEach((km) => kmNameList.push(km.name));
                }
                setKeyManagersConfigured(kmNameList);
            })
                .catch((error) => {
                    const { response } = error;
                    if (response.body) {
                        const { description } = response.body;
                        Alert.error(description);
                    }
                });
        }
    }, []);

    const getMediationPoliciesToSave = () => {
        const NONE = 'none';
        const newMediationPolicies = [];
        if (!(isEmpty(inPolicy) || inPolicy.name === NONE)) {
            newMediationPolicies.push(inPolicy);
        }
        if (!(isEmpty(outPolicy) || outPolicy.name === NONE)) {
            newMediationPolicies.push(outPolicy);
        }
        if (!(isEmpty(faultPolicy) || faultPolicy.name === NONE)) {
            newMediationPolicies.push(faultPolicy);
        }
        return newMediationPolicies;
    };
    const updateInMediationPolicy = (policy) => {
        setInPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };
    const updateOutMediationPolicy = (policy) => {
        setOutPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };
    const updateFaultMediationPolicy = (policy) => {
        setFaultPolicy({ id: policy.id, name: policy.name, type: policy.type });
    };


    /**
     * Update the GraphQL Query Complexity Values
     */
    function updateComplexity() {
        const apiId = apiConfig.id;
        const apiClient = new Api();
        const promisedComplexity = apiClient.updateGraphqlPoliciesComplexity(
            apiId, {
                list: updateComplexityList,
            },
        );
        promisedComplexity
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    const { description } = response.body;
                    Alert.error(description);
                }
            });
    }

    /**
     *
     * Handle the configuration view save button action
     */
    function handleSave() {
        const newMediationPolicies = getMediationPoliciesToSave();
        if (api.isAPIProduct()) {
            delete apiConfig.keyManagers; // remove keyManagers property if API type is API Product
        } else {
            apiConfig.mediationPolicies = newMediationPolicies;
        }
        if (updateComplexityList !== null) {
            updateComplexity();
        }
        // Validate the key managers
        if (
            !api.isAPIProduct()
            && apiConfig.securityScheme.includes('oauth2')
            && !apiConfig.keyManagers.includes('all')
            && (apiConfig.keyManagers && apiConfig.keyManagers.length === 0)
        ) {
            Alert.error(
                intl.formatMessage(
                    {
                        id: 'Apis.Details.Configuration.RuntimeConfiguration.no.km.error',
                        defaultMessage: 'Select one or more Key Managers',
                    },
                ),
            );
            return;
        }
        setIsUpdating(true);
        updateAPI(apiConfig)
            .catch((error) => {
                if (error.response) {
                    Alert.error(error.response.body.description);
                }
            })
            .finally(() => setIsUpdating(false));
    }

    return (
        <>
            <Box pb={3}>
                <Typography variant='h5'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.RuntimeConfiguration.topic.header'
                        defaultMessage='Runtime Configurations'
                    />
                </Typography>
            </Box>
            <div className={classes.contentWrapper}>
                <Grid container direction='row' justify='space-around' alignItems='stretch' spacing={8}>
                    <Grid item xs={12} md={7}>
                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.section.request'
                                defaultMessage='Request'
                            />
                        </Typography>
                        <Grid
                            direction=' column'
                            justify='space-between'
                            alignItems='stretch'
                            spacing={6}
                        >
                            <Grid item xs={12} style={{ marginBottom: 30, position: 'relative' }}>
                                <Paper className={classes.paper} elevation={0}>
                                    <APISecurity api={apiConfig} configDispatcher={configDispatcher} />
                                    <CORSConfiguration api={apiConfig} configDispatcher={configDispatcher} />

                                    {api.type !== 'GRAPHQL'
                                        && <SchemaValidation api={apiConfig} configDispatcher={configDispatcher} />}
                                    {!api.isAPIProduct() && (
                                        <Flow
                                            api={apiConfig}
                                            type='IN'
                                            updateMediationPolicy={updateInMediationPolicy}
                                            selectedMediationPolicy={inPolicy}
                                            isRestricted={isRestricted(['apim:api_create'], api)}
                                        />
                                    )}
                                    {api.type === 'GRAPHQL' && (
                                        <Box mt={3}>
                                            <QueryAnalysis
                                                api={apiConfig}
                                                setUpdateComplexityList={setUpdateComplexityList}
                                                isRestricted={isRestricted(['apim:api_create'], api)}
                                            />
                                        </Box>
                                    )}
                                </Paper>
                                <ArrowForwardIcon className={classes.arrowForwardIcon} />
                            </Grid>
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.section.response'
                                    defaultMessage='Response'
                                />
                            </Typography>
                            <Grid item xs={12} style={{ position: 'relative' }}>
                                <Box mb={3}>
                                    <Paper className={classes.paper} elevation={0}>
                                        {!api.isAPIProduct() && (
                                            <Box mb={3}>
                                                <Flow
                                                    api={apiConfig}
                                                    type='OUT'
                                                    updateMediationPolicy={updateOutMediationPolicy}
                                                    selectedMediationPolicy={outPolicy}
                                                    isRestricted={isRestricted(['apim:api_create'], api)}
                                                />
                                            </Box>
                                        )}
                                        <ResponseCaching api={apiConfig} configDispatcher={configDispatcher} />
                                    </Paper>
                                    <ArrowBackIcon className={classes.arrowBackIcon} />
                                </Box>
                            </Grid>
                            {!api.isAPIProduct() && (
                                <>
                                    <Typography className={classes.heading} variant='h6'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.RuntimeConfiguration.section.fault'
                                            defaultMessage='Fault'
                                        />
                                    </Typography>
                                    <Grid item xs={12} style={{ position: 'relative' }}>
                                        <Paper className={classes.paper} elevation={0}>
                                            <Flow
                                                api={apiConfig}
                                                type='FAULT'
                                                updateMediationPolicy={updateFaultMediationPolicy}
                                                selectedMediationPolicy={faultPolicy}
                                                isRestricted={isRestricted(['apim:api_create'], api)}
                                            />
                                        </Paper>
                                    </Grid>
                                </>
                            )}
                        </Grid>
                    </Grid>
                    <Grid item xs={12} md={5}>
                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.section.backend'
                                defaultMessage='Backend'
                            />
                        </Typography>
                        <Paper className={classes.paper} style={{ height: 'calc(100% - 75px)' }} elevation={0}>
                            {!api.isAPIProduct() && (
                                <>
                                    <MaxBackendTps api={apiConfig} configDispatcher={configDispatcher} />
                                    <Endpoints api={api} />
                                </>
                            )}

                            {api.isAPIProduct() && (
                                <Box alignItems='center' justifyContent='center' className={classes.info}>
                                    <Typography variant='body1'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.RuntimeConfiguration.backend.api.product.
                                            endpoint'
                                            defaultMessage='Please refer respective APIs for endpoint information'
                                        />
                                    </Typography>
                                </Box>
                            )}
                        </Paper>
                    </Grid>
                </Grid>
                <Grid container>
                    <Grid container direction='row' alignItems='center' spacing={1} style={{ marginTop: 20 }}>
                        <Grid item>
                            <Button
                                disabled={isUpdating
                                || ((apiConfig.visibility === 'RESTRICTED' && apiConfig.visibleRoles.length === 0)
                                    || isRestricted(['apim:api_create'], api))}
                                type='submit'
                                variant='contained'
                                color='primary'
                                onClick={handleSave}
                            >
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.save'
                                    defaultMessage='Save'
                                />
                                {isUpdating && <CircularProgress size={15} />}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Link to={'/apis/' + api.id + '/overview'}>
                                <Button>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.Configuration.cancel'
                                        defaultMessage='Cancel'
                                    />
                                </Button>
                            </Link>
                        </Grid>
                        {isRestricted(['apim:api_create'], api) && (
                            <Grid item>
                                <Typography variant='body2' color='primary'>
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.Configuration.update.not.allowed'
                                        defaultMessage={
                                            '* You are not authorized to update particular fields of'
                                            + ' the API due to insufficient permissions'
                                        }
                                    />
                                </Typography>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </div>
        </>
    );
}
