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

import React, { useReducer, useContext, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';
import ArrowForwardIcon from '@material-ui/icons/ArrowForward';
import ArrowBackIcon from '@material-ui/icons/ArrowBack';
import LaunchIcon from '@material-ui/icons/Launch';

import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import { isRestricted } from 'AppData/AuthManager';
import DefaultVersion from './components/DefaultVersion';
import ResponseCaching from './components/ResponseCaching';
import Description from './components/Description';
import AccessControl from './components/AccessControl';
import StoreVisibility from './components/StoreVisibility';
import CORSConfiguration from './components/CORSConfiguration';
import Tags from './components/Tags';
import SchemaValidation from './components/SchemaValidation';

import APISecurity, {
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_API_KEY,
    API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
    API_SECURITY_MUTUAL_SSL,
} from './components/APISecurity/APISecurity';

const useStyles = makeStyles(theme => ({
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
    return {
        id: api.id,
        name: api.name,
        description: api.description,
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
        transport: [...api.transport],
        securityScheme: [...api.securityScheme],
        corsConfiguration: {
            corsConfigurationEnabled: api.corsConfiguration.corsConfigurationEnabled,
            accessControlAllowCredentials: api.corsConfiguration.accessControlAllowCredentials,
            accessControlAllowOrigins: [...api.corsConfiguration.accessControlAllowOrigins],
            accessControlAllowHeaders: [...api.corsConfiguration.accessControlAllowHeaders],
            accessControlAllowMethods: [...api.corsConfiguration.accessControlAllowMethods],
        },
    };
}
/**
 * This component handles the basic configurations UI in the API details page
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function Configuration() {
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
            case 'tags':
                nextState[action] = value;
                return nextState;
            case 'accessControlRoles':
                return { ...copyAPIConfig(state), [action]: value };
            case 'visibleRoles':
                return { ...copyAPIConfig(state), [action]: value };
            case 'securityScheme':
                // If event came from mandatory selector of either Application level or Transport level
                if ([API_SECURITY_MUTUAL_SSL_MANDATORY,
                    API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY].includes(event.name)) {
                    // If user select not mandatory (optional) , Remove the respective schema, else add it
                    if (event.value === 'optional') {
                        return {
                            ...copyAPIConfig(state),
                            [action]: state[action].filter(schema => schema !== event.name),
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
                        [action]: state[action].filter(schema => schema !== event.value),
                    };
                    if (
                        !(
                            newState[action].includes(DEFAULT_API_SECURITY_OAUTH2) ||
                            newState[action].includes(API_SECURITY_BASIC_AUTH) ||
                            newState[action].includes(API_SECURITY_API_KEY)
                        )
                    ) {
                        const noMandatoryOAuthBasicAuth = newState[action]
                            .filter(schema => schema !== API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY);
                        return {
                            ...newState,
                            [action]: noMandatoryOAuthBasicAuth,
                        };
                    } else if (!newState[action].includes(API_SECURITY_MUTUAL_SSL)) {
                        const noMandatoryMutualSSL = newState[action]
                            .filter(schema => schema !== API_SECURITY_MUTUAL_SSL_MANDATORY);
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
                        transport: state.transport.filter(transport => transport !== event.value),
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
            default:
                return state;
        }
    }
    const { api, updateAPI } = useContext(APIContext);
    const [isUpdating, setIsUpdating] = useState(false);
    const [apiConfig, configDispatcher] = useReducer(configReducer, copyAPIConfig(api));
    const classes = useStyles();
    const paperHeight = window.innerHeight - 400;
    /**
     *
     * Handle the configuration view save button action
     */
    function handleSave() {
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
        <React.Fragment>
            <div className={classes.titleWrapper}>
                <Typography variant='h4' align='left' className={classes.mainTitle}>
                    <FormattedMessage
                        id='Apis.Details.Configuration.Configuration.topic.header'
                        defaultMessage='Configuration'
                    />
                </Typography>
            </div>
            {
                // TODO:
                // Move design configurations to a sub page
            }
            <div className={classes.contentWrapper}>
                <Grid
                    container
                    direction='row'
                    justify='space-around'
                    alignItems='flex-start'
                    spacing={8}
                >
                    <Grid item xs={4}>
                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.section.design'
                                defaultMessage='Design'
                            />
                        </Typography>
                        <Paper className={classes.paper} style={{ minHeight: paperHeight }}>
                            <Grid
                                container
                                direction='row'
                                justify='space-around'
                                alignItems='flex-start'
                                spacing={1}
                                style={{ marginBottom: 20 }}
                            >
                                <Grid item xs={12} md={4}>
                                    <ThumbnailView api={api} width={100} height={100} isEditable />
                                </Grid>
                                <Grid item xs={12} md={8}>
                                    <Description api={apiConfig} configDispatcher={configDispatcher} />
                                </Grid>
                            </Grid>
                            <div className={classes.itemPadding}>
                                <AccessControl
                                    api={apiConfig}
                                    configDispatcher={configDispatcher}
                                />
                            </div>
                            <div className={classes.itemPadding}>
                                <StoreVisibility
                                    api={apiConfig}
                                    configDispatcher={configDispatcher}
                                />
                            </div>
                            <div className={classes.itemPadding}>
                                <Tags api={apiConfig} configDispatcher={configDispatcher} />
                            </div>
                            <div className={classes.itemPadding}>
                                <Grid
                                    container
                                    direction='row'
                                >
                                    <Grid item xs={12} md={6}>
                                        <DefaultVersion api={apiConfig} configDispatcher={configDispatcher} />
                                    </Grid>
                                </Grid>
                            </div>
                        </Paper>
                    </Grid>
                    <Grid item xs={5}>
                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.section.request'
                                defaultMessage='Request'
                            />
                        </Typography>
                        <Grid
                            direction=' column'
                            justify='space-between'
                            alignItems='flex-start'
                            spacing={6}
                            style={{ minHeight: paperHeight }}
                        >
                            <Grid item xs={12} style={{ marginBottom: 30, position: 'relative' }}>
                                <Paper className={classes.paper}>
                                    <APISecurity api={apiConfig} configDispatcher={configDispatcher} />
                                    <CORSConfiguration api={apiConfig} configDispatcher={configDispatcher} />
                                    <SchemaValidation api={apiConfig} configDispatcher={configDispatcher} />
                                </Paper>
                                <ArrowForwardIcon className={classes.arrowForwardIcon} />
                            </Grid>
                            {
                                // TODO:
                                // Add Mediation Policies
                            }
                            <Typography className={classes.heading} variant='h6'>
                                <FormattedMessage
                                    id='Apis.Details.Configuration.Configuration.section.response'
                                    defaultMessage='Response'
                                />
                            </Typography>
                            <Grid item xs={12} style={{ position: 'relative' }}>
                                <Paper className={classes.paper}>
                                    <ResponseCaching api={apiConfig} configDispatcher={configDispatcher} />
                                </Paper>
                                <ArrowBackIcon className={classes.arrowBackIcon} />
                            </Grid>
                        </Grid>
                    </Grid>
                    <Grid item xs={3}>
                        <Typography className={classes.heading} variant='h6'>
                            <FormattedMessage
                                id='Apis.Details.Configuration.Configuration.section.backend'
                                defaultMessage='Backend'
                            />
                        </Typography>
                        <Paper className={classes.paperCenter} style={{ minHeight: paperHeight }}>
                            <Link to='/apis/:api_uuid/endpoints'>
                                <Typography
                                    className={classes.subHeading}
                                    color='primary'
                                    display='inline'
                                    variant='caption'
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Configuration.Configuration.endpoints'
                                        defaultMessage='Endpoints'
                                    />
                                    <LaunchIcon style={{ marginLeft: '2px' }} fontSize='small' />
                                </Typography>
                            </Link>
                        </Paper>
                    </Grid>
                </Grid>
                <Grid container>
                    <Grid container direction='row' alignItems='center' spacing={4} style={{ marginTop: 20 }}>
                        <Grid item>
                            <Button
                                disabled={isUpdating}
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
                        {(isRestricted(['apim:api_create'], api))
                            && (
                                <Grid item>
                                    <Typography variant='body2' color='primary'>
                                        <FormattedMessage
                                            id='Apis.Details.Configuration.Configuration.update.not.allowed'
                                            defaultMessage={'* You are not authorized to update particular fields of' +
                                            ' the API due to insufficient permissions'}
                                        />
                                    </Typography>
                                </Grid>
                            )
                        }
                    </Grid>
                </Grid>
            </div>
        </React.Fragment>
    );
}
