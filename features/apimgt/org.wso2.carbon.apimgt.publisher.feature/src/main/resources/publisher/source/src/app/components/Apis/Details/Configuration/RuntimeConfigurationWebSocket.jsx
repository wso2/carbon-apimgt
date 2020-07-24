/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import { FormattedMessage } from 'react-intl';
import CircularProgress from '@material-ui/core/CircularProgress';
import Alert from 'AppComponents/Shared/Alert';
import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import { isRestricted } from 'AppData/AuthManager';
import MaxBackendTps from './components/MaxBackendTps';
import Endpoints from './components/Endpoints';
import KeyManager from './components/KeyManager';
import APILevelRateLimitingPolicies from './components/APILevelRateLimitingPolicies';

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
        minHeight: '250px',
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
    const keyManagers = api.apiType === 'APIProduct' ? ['all'] : [...api.keyManagers];
    return {
        id: api.id,
        name: api.name,
        description: api.description,
        lifeCycleStatus: api.lifeCycleStatus,
        accessControl: api.accessControl,
        authorizationHeader: api.authorizationHeader,
        responseCachingEnabled: api.responseCachingEnabled,
        cacheTimeout: api.cacheTimeout,
        visibility: api.visibility,
        apiThrottlingPolicy: api.apiThrottlingPolicy,
        isDefaultVersion: api.isDefaultVersion,
        enableSchemaValidation: api.enableSchemaValidation,
        accessControlRoles: [...api.accessControlRoles],
        visibleRoles: [...api.visibleRoles],
        tags: [...api.tags],
        maxTps: api.maxTps,
        wsdlUrl: api.wsdlUrl,
        transport: [...api.transport],
        securityScheme: [...api.securityScheme],
        corsConfiguration: {
            corsConfigurationEnabled: api.corsConfiguration.corsConfigurationEnabled,
            accessControlAllowCredentials: api.corsConfiguration.accessControlAllowCredentials,
            accessControlAllowOrigins: [...api.corsConfiguration.accessControlAllowOrigins],
            accessControlAllowHeaders: [...api.corsConfiguration.accessControlAllowHeaders],
            accessControlAllowMethods: [...api.corsConfiguration.accessControlAllowMethods],
        },
        keyManagers,
    };
}
/**
 * This component handles the basic configurations UI in the API details page
 *
 * @export
 * @param {*} props
 * @returns
 */
export default function RuntimeConfiguration() {
    /**
     *
     * Reduce the configuration UI related actions in to updated state
     * @param {*} state current state
     * @param {*} configAction dispatched configuration action
     * @returns {Object} updated state
     */
    function configReducer(state, configAction) {
        const { action, value } = configAction;
        const nextState = { ...copyAPIConfig(state) };
        switch (action) {
            case 'apiThrottlingPolicy':
            case 'maxTps':
                nextState[action] = value;
                return nextState;
            case 'keymanagers':
                nextState.keyManagers = value;
                return nextState;
            case 'throttlingPoliciesEnabled':
                if (value) {
                    nextState.apiThrottlingPolicy = '';
                } else {
                    nextState.apiThrottlingPolicy = null;
                }
                return nextState;
            case 'allKeyManagersEnabled':
                if (value) {
                    nextState.keyManagers = [];
                } else {
                    nextState.keyManagers = ['all'];
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
        <>
            <Box pb={3}>
                <Typography variant='h5'>
                    <FormattedMessage
                        id='Apis.Details.Configuration.RuntimeConfigurationWebSocket.topic.header'
                        defaultMessage='Runtime Configurations'
                    />
                </Typography>
            </Box>
            <div className={classes.contentWrapper}>
                <Grid container direction='row' justify='space-around' alignItems='stretch' spacing={8}>
                    <Grid item xs={12} md={7}>
                        <Grid
                            direction=' column'
                            justify='space-between'
                            alignItems='stretch'
                            spacing={6}
                        >
                            <Grid item xs={12} style={{ marginBottom: 30, position: 'relative' }}>
                                <Paper className={classes.paper} elevation={0}>
                                    <KeyManager api={apiConfig} configDispatcher={configDispatcher} />
                                    <APILevelRateLimitingPolicies api={apiConfig} configDispatcher={configDispatcher} />
                                </Paper>
                            </Grid>
                        </Grid>
                    </Grid>
                    <Grid item xs={12} md={5}>
                        <Paper className={classes.paper} style={{ height: 'calc(100% - 75px)' }} elevation={0}>
                            {!api.isAPIProduct() && (
                                <>
                                    <MaxBackendTps api={apiConfig} configDispatcher={configDispatcher} />
                                    <Endpoints api={api} />
                                </>
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
