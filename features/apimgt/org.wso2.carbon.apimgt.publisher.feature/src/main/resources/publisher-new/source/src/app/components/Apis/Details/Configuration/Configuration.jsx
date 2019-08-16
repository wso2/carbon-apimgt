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

import { APIContext } from 'AppComponents/Apis/Details/components/ApiContext';
import ThumbnailView from 'AppComponents/Apis/Listing/components/ImageGenerator/ThumbnailView';
import AuthorizationHeader from './components/AuthorizationHeader';
import DefaultVersion from './components/DefaultVersion';
import ResponseCaching from './components/ResponseCaching';
import Transports from './components/Transports';
import Description from './components/Description';
import AccessControl from './components/AccessControl';
import StoreVisibility from './components/StoreVisibility';
import Tags from './components/Tags';
import SchemaValidation from './components/SchemaValidation';

import APISecurity, {
    DEFAULT_API_SECURITY_OAUTH2,
    API_SECURITY_BASIC_AUTH,
    API_SECURITY_OAUTH_BASIC_AUTH_MANDATORY,
    API_SECURITY_MUTUAL_SSL_MANDATORY,
    API_SECURITY_MUTUAL_SSL,
} from './components/APISecurity/APISecurity';

const useStyles = makeStyles(theme => ({
    root: {
        padding: theme.spacing(3, 2),
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
        name: api.name,
        description: api.description,
        accessControl: api.accessControl,
        authorizationHeader: api.authorizationHeader,
        responseCaching: api.responseCaching,
        cacheTimeout: api.cacheTimeout,
        visibility: api.visibility,
        isDefaultVersion: api.isDefaultVersion,
        enableSchemaValidation: api.enableSchemaValidation,
        accessControlRoles: [...api.accessControlRoles],
        visibleRoles: [...api.visibleRoles],
        tags: [...api.tags],
        transport: [...api.transport],
        securityScheme: [...api.securityScheme],
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
        switch (action) {
            case 'description':
            case 'isDefaultVersion':
            case 'authorizationHeader':
            case 'responseCaching':
            case 'enableSchemaValidation':
            case 'accessControl':
            case 'visibility':
            case 'tags':
                return { ...copyAPIConfig(state), [action]: value };
            case 'accessControlRoles':
                // TODO: need to do the role validation here ~tmkb
                return { ...copyAPIConfig(state), [action]: value.split(',') };
            case 'visibleRoles':
                // TODO: need to do the role validation here ~tmkb
                return { ...copyAPIConfig(state), [action]: value.split(',') };
            case 'securityScheme':
                // If event came from mandatory selector of either Application level or Transport level
                if ([API_SECURITY_MUTUAL_SSL_MANDATORY, API_SECURITY_OAUTH_BASIC_AUTH_MANDATORY].includes(event.name)) {
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
                // User checked on one of api security schemas (either OAuth, Basic or Mutual SSL)
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
                            newState[action].includes(API_SECURITY_BASIC_AUTH)
                        )
                    ) {
                        const noMandatoryOAuthBasicAuth = newState[action]
                            .filter(schema => schema !== API_SECURITY_OAUTH_BASIC_AUTH_MANDATORY);
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
        <Paper className={classes.root}>
            <Typography variant='h4'>
                <FormattedMessage
                    id='Apis.Details.Configuration.Configuration.topic.header'
                    defaultMessage='Configuration'
                />
            </Typography>

            <Grid container>
                <Grid item xs={12} md={2}>
                    <ThumbnailView api={api} width={200} height={200} isEditable />
                </Grid>
                <Grid item xs={12} md={10}>
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={9}>
                            <Description api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <ResponseCaching api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <Transports api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <DefaultVersion api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <AuthorizationHeader api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <AccessControl api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <StoreVisibility api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <APISecurity api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <Tags api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>

                        <Grid item xs={12} md={9}>
                            <SchemaValidation api={apiConfig} configDispatcher={configDispatcher} />
                        </Grid>
                    </Grid>
                </Grid>
                <Grid container direction='row' alignItems='flex-start' spacing={4}>
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
                </Grid>
            </Grid>
        </Paper>
    );
}
