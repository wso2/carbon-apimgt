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

import React, { useState, useContext } from 'react';
import Paper from '@material-ui/core/Paper';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import GraphQLAuthentication from './GraphQLAuthentication';
import GraphQLUI from './GraphQLUI';
import { ApiContext } from '../ApiContext';
import Progress from '../../../Shared/Progress';


const useStyles = makeStyles((theme) => ({
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
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
    const [URLs, setURLs] = useState(environmentObject[0].URLs);
    const [accessToken, setAccessTocken] = useState('');
    const [securitySchemeType, setSecuritySchemeType] = useState('OAUTH');
    const [notFound, setFound] = useState(false);
    const [username, setUserName] = useState('');
    const [password, setPassword] = useState('');

    if (api == null) {
        return <Progress />;
    }
    if (notFound) {
        return 'API Not found !';
    }
    let isApiKeyEnabled = false;
    let isBasicAuthEnabled = false;
    let isOAuthEnabled = false;
    let authorizationHeader = api.authorizationHeader ? api.authorizationHeader : 'Authorization';
    let prefix = 'Bearer';

    if (api && api.securityScheme) {
        isApiKeyEnabled = api.securityScheme.includes('api_key');
        isBasicAuthEnabled = api.securityScheme.includes('basic_auth');
        isOAuthEnabled = api.securityScheme.includes('oauth2');
        if (isApiKeyEnabled && securitySchemeType === 'API-KEY') {
            authorizationHeader = 'apikey';
            prefix = '';
        }
    }

    return (
        <>
            <Typography variant='h4' className={classes.titleSub}>
                <FormattedMessage id='Apis.Details.GraphQLConsole.GraphQLConsole.title' defaultMessage='Try Out' />
            </Typography>

            <GraphQLAuthentication
                api={api}
                securitySchemeType={securitySchemeType}
                setSecuritySchemeType={setSecuritySchemeType}
                username={username}
                setUserName={setUserName}
                password={password}
                setPassword={setPassword}
                prefix={prefix}
                isApiKeyEnabled={isApiKeyEnabled}
                isOAuthEnabled={isOAuthEnabled}
                isBasicAuthEnabled={isBasicAuthEnabled}
                accessToken={accessToken}
                setAccessTocken={setAccessTocken}
                authorizationHeader={authorizationHeader}
                setURLs={setURLs}
                environmentObject={environmentObject}
                setFound={setFound}
            />

            <Paper className={classes.paper}>
                <GraphQLUI
                    accessToken={accessToken}
                    authorizationHeader={authorizationHeader}
                    URLs={URLs}
                    username={username}
                    password={password}
                    securitySchemeType={securitySchemeType}
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
