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

import React, { useState, useContext, useEffect } from 'react';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Grid from '@material-ui/core/Grid';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import AuthManager from 'AppData/AuthManager';
import Icon from '@material-ui/core/Icon';
import fileDownload from 'js-file-download';
import converter from 'graphql-to-postman';
import GraphQLUI from './GraphQLUI';
import TryOutController from '../ApiConsole/TryOutController';
import { ApiContext } from '../ApiContext';
import Api from '../../../../data/api';
import Progress from '../../../Shared/Progress';


const useStyles = makeStyles((theme) => ({
    buttonIcon: {
        marginRight: 10,
    },
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
    },
    grid: {
        marginTop: theme.spacing(4),
        marginBottom: theme.spacing(4),
        paddingRight: theme.spacing(2),
        justifyContent: 'center',
    },
    userNotificationPaper: {
        padding: theme.spacing(2),
    },
    titleSub: {
        marginLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
}));

export default function GraphQLConsole() {
    const classes = useStyles();
    const { api } = useContext(ApiContext);
    const environmentObject = api.endpointURLs;
    const [URLs, setURLs] = useState(environmentObject.length > 0 ? environmentObject[0].URLs : null);
    const [securitySchemeType, setSecurityScheme] = useState('OAUTH');
    const [notFound, setNotFound] = useState(false);
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [selectedEnvironment, setSelectedEnvironment] = useState();
    const [environments, setEnvironments] = useState();
    const [scopes, setScopes] = useState([]);
    const [productionAccessToken, setProductionAccessToken] = useState();
    const [sandboxAccessToken, setSandboxAccessToken] = useState();
    const [selectedKeyType, setSelectedKey] = useState('PRODUCTION');
    const [sandboxApiKey, setSandboxApiKey] = useState('');
    const [productionApiKey, setProductionApiKey] = useState('');
    const [keys, setKeys] = useState([]);
    const user = AuthManager.getUser();

    useEffect(() => {
        const apiID = api.id;
        const apiClient = new Api();
        const promiseAPI = apiClient.getAPIById(apiID);

        promiseAPI
            .then((apiResponse) => {
                const apiData = apiResponse.obj;
                if (apiData.endpointURLs) {
                    const environment = apiData.endpointURLs.map((endpoint) => {
                        return { name: endpoint.environmentName, displayName: endpoint.environmentDisplayName };
                    });
                    setEnvironments(environment);
                }
                if (apiData.scopes) {
                    const scopeList = apiData.scopes.map((scope) => { return scope.name; });
                    setScopes(scopeList);
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                const { status } = error;
                if (status === 404) {
                    setNotFound(true);
                }
            });
    }, []);


    /**
     * Load the access token for given key type
     */
    function updateAccessToken() {
        let accessToken;
        if (keys.get(selectedKeyType)) {
            ({ accessToken } = keys.get(selectedKeyType).token);
        }
        if (selectedKeyType === 'PRODUCTION') {
            setProductionAccessToken(accessToken);
        } else {
            setSandboxAccessToken(accessToken);
        }
    }

    /**
     * set Password
     * @param {*} selectedKey
     * @param {*} isUpdateToken
     */
    function setSelectedKeyType(selectedKey, isUpdateToken) {
        if (isUpdateToken) {
            setSelectedKey(selectedKey, updateAccessToken);
        } else {
            setSelectedKey(selectedKey);
        }
    }

    function accessTokenProvider() {
        if (securitySchemeType === 'BASIC') {
            const credentials = username + ':' + password;
            return btoa(credentials);
        }
        if (securitySchemeType === 'API-KEY') {
            if (selectedKeyType === 'PRODUCTION') {
                return productionApiKey;
            } else {
                return sandboxApiKey;
            }
        } else if (selectedKeyType === 'PRODUCTION') {
            return productionAccessToken;
        } else {
            return sandboxAccessToken;
        }
    }

    function grapgQLToPostman(graphQL, URL) {
        converter.convert({
            type: 'string',
            data: graphQL,
        }, {}, (error, result) => {
            if (error) {
                console.log(error);
            } else {
                const urlValue = URL.https;
                const results = result;
                results.output[0].data.variable[0].value = urlValue;
                const outputData = results.output[0].data;
                fileDownload(
                    JSON.stringify(outputData),
                    'postman collection',
                );
                console.log('Conversion success');
            }
        });
    }

    if (api == null) {
        return <Progress />;
    }
    if (notFound) {
        return 'API Not found !';
    }
    let isApiKeyEnabled = false;

    let authorizationHeader = api.authorizationHeader ? api.authorizationHeader : 'Authorization';

    if (api && api.securityScheme) {
        isApiKeyEnabled = api.securityScheme.includes('api_key');
        if (isApiKeyEnabled && securitySchemeType === 'API-KEY') {
            authorizationHeader = 'apikey';
        }
    }

    const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';

    return (
        <>
            <Typography variant='h4' className={classes.titleSub}>
                <FormattedMessage id='Apis.Details.GraphQLConsole.GraphQLConsole.title' defaultMessage='Try Out' />
            </Typography>
            <Paper className={classes.paper}>
                <Grid container className={classes.grid}>
                    {!isPrototypedAPI && !user && (
                        <Grid item md={6}>
                            <Paper className={classes.userNotificationPaper}>
                                <Typography variant='h5' component='h3'>
                                    <Icon>warning</Icon>
                                    {' '}
                                    <FormattedMessage id='notice' defaultMessage='Notice' />
                                </Typography>
                                <Typography component='p'>
                                    <FormattedMessage
                                        id='api.console.require.access.token'
                                        defaultMessage={'You need an access token to try the API. Please log '
                                            + 'in and subscribe to the API to generate an access token. If you already '
                                            + 'have an access token, please provide it below.'}
                                    />
                                </Typography>
                            </Paper>
                        </Grid>
                    )}
                </Grid>
                <TryOutController
                    setSecurityScheme={setSecurityScheme}
                    securitySchemeType={securitySchemeType}
                    setSelectedEnvironment={setSelectedEnvironment}
                    selectedEnvironment={selectedEnvironment}
                    productionAccessToken={productionAccessToken}
                    setProductionAccessToken={setProductionAccessToken}
                    sandboxAccessToken={sandboxAccessToken}
                    setSandboxAccessToken={setSandboxAccessToken}
                    environments={environments}
                    scopes={scopes}
                    setUsername={setUsername}
                    setPassword={setPassword}
                    username={username}
                    password={password}
                    setSelectedKeyType={setSelectedKeyType}
                    convertToPostman={grapgQLToPostman}
                    selectedKeyType={selectedKeyType}
                    setKeys={setKeys}
                    setURLs={setURLs}
                    setProductionApiKey={setProductionApiKey}
                    setSandboxApiKey={setSandboxApiKey}
                    productionApiKey={productionApiKey}
                    sandboxApiKey={sandboxApiKey}
                    environmentObject={environmentObject}
                    api={api}
                />
            </Paper>
            <Paper className={classes.paper}>
                <GraphQLUI
                    authorizationHeader={authorizationHeader}
                    URLs={URLs}
                    securitySchemeType={securitySchemeType}
                    accessTokenProvider={accessTokenProvider}
                />
            </Paper>
        </>
    );
}

GraphQLConsole.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        titleSub: PropTypes.string.isRequired,
        root: PropTypes.string.isRequired,
    }).isRequired,
};
