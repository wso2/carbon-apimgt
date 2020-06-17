/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import AuthManager from 'AppData/AuthManager';
import Progress from 'AppComponents/Shared/Progress';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage } from 'react-intl';
import Paper from '@material-ui/core/Paper';
import 'swagger-ui-react/swagger-ui.css';
import API from 'AppData/api';
import { TryOutController, SwaggerUI } from 'developer_portal';
import { withAPI } from 'AppComponents/Apis/Details/components/ApiContext';

/**
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = (theme) => ({
    centerItems: {
        margin: 'auto',
    },
    categoryHeading: {
        marginBottom: theme.spacing(2),
        marginLeft: theme.spacing(-5),
    },
    buttonIcon: {
        marginRight: 10,
    },
    paper: {
        margin: theme.spacing(1),
        padding: theme.spacing(1),
        '& span, & h5, & label, & td, & li, & div, & input': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
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
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    tryoutHeading: {
        fontWeight: 400,
    },
    noDataMessage: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#888888',
        width: '100%',
    },
    swaggerUIPaper: {
        showTryout: true,
        swaggerUIBackground: '#efefef',
        documentBackground: '#efefef',
        tokenTextBoxBackground: '#efefef',
    },
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    content: {
        display: 'flex',
        flex: 1,
        flexDirection: 'column',
        marginLeft: theme.custom.leftMenuWidth,
        paddingBottom: theme.spacing(3),
    },
});

/**
 * @class TestConsole
 * @extends {React.Component}
 */
class TestConsole extends React.Component {
    /**
     *
     * @param {*} props the props parameters
     */
    constructor(props) {
        super(props);
        this.state = {
            securitySchemeType: 'BASIC',
            username: '',
            password: '',
            scopes: [],
            selectedKeyType: 'PRODUCTION',
            keys: [],
        };
        this.accessTokenProvider = this.accessTokenProvider.bind(this);
        this.updateSwagger = this.updateSwagger.bind(this);
        this.setSecurityScheme = this.setSecurityScheme.bind(this);
        this.setSelectedEnvironment = this.setSelectedEnvironment.bind(this);
        this.setProductionAccessToken = this.setProductionAccessToken.bind(this);
        this.setSandboxAccessToken = this.setSandboxAccessToken.bind(this);
        this.setUsername = this.setUsername.bind(this);
        this.setPassword = this.setPassword.bind(this);
        this.setSelectedKeyType = this.setSelectedKeyType.bind(this);
        this.setKeys = this.setKeys.bind(this);
        this.updateAccessToken = this.updateAccessToken.bind(this);
    }

    /**
     * @memberof ApiConsole
     */
    componentDidMount() {
        const { apiObj } = this.props;
        let apiData;
        let environments;
        let labels;
        let selectedEnvironment;
        let swagger;
        let productionAccessToken;
        let sandboxAccessToken;
        let apiID;
        let urls;
        let httpVal;
        let httpsVal;
        const user = AuthManager.getUser();
        const promisedAPI = API.getAPIById(apiObj.id);
        promisedAPI
            .then((apiResponse) => {
                apiID = apiResponse.obj.id;
                apiData = apiResponse.obj;
                if (apiData.gatewayEnvironments) {
                    environments = apiData.gatewayEnvironments.map((endpoint) => { return endpoint; });
                }
                const securtySchemas = apiData.securityScheme;
                securtySchemas.push('basic_auth');
                securtySchemas.shift();
                if (apiData.endpointURLs) {
                    environments = apiData.endpointURLs.map((endpoint) => { return endpoint.environmentName; });
                }
                if (apiData.labels) {
                    labels = apiData.labels.map((label) => { return label.name; });
                }
                if (apiData.scopes) {
                    const scopeList = apiData.scopes.map((scope) => { return scope.name; });
                    this.setState({ scopes: scopeList });
                }
                if (environments && environments.length > 0) {
                    [selectedEnvironment] = environments;
                    return API.getSwaggerByAPIIdAndEnvironment(apiResponse.obj.id, selectedEnvironment);
                } else if (labels && labels.length > 0) {
                    [selectedEnvironment] = labels;
                    return API.getSwaggerByAPIIdAndLabel(apiResponse.obj.id, selectedEnvironment);
                } else {
                    return API.getSwaggerByAPIId(apiResponse.obj.id);
                }
            })
            .then((swaggerResponse) => {
                swagger = swaggerResponse.obj;
                if (user != null) {
                    this.setState({
                        api: apiData,
                        swagger,
                        environments,
                        labels,
                        productionAccessToken,
                        sandboxAccessToken,
                    });
                    return API.getSubscriptions(apiID);
                } else {
                    return null;
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.error(error);
                }
                this.setState({ serverError: `${error.statusCode} - ${error.response.body.description}` });
            });
        const settingPromise = API.getSettings();
        const newServer = [];
        settingPromise
            .then((settingsNew) => {
                if (settingsNew.environment) {
                    urls = settingsNew.environment.map((endpoints) => { return endpoints.endpoints; });
                    httpVal = urls.map((val) => { return val.http; });
                    httpsVal = urls.map((value) => { return value.https; });
                    newServer.push({ url: httpsVal + apiData.context + '/' + apiData.version });
                    newServer.push({ url: httpVal + apiData.context + '/' + apiData.version });
                }
                this.setState({
                    settings: newServer,
                });
            });
    }

    /**
     * Set SecurityScheme value
     * @memberof ApiConsole
     */
    setSecurityScheme(securityScheme) {
        this.setState({ securitySchemeType: securityScheme });
    }

    /**
     * Set Selected Environment
     * @memberof ApiConsole
     */
    setSelectedEnvironment(selectedEnvironment) {
        this.setState({ selectedEnvironment });
    }

    /**
     * Set Production Access Token
     * @memberof ApiConsole
     */
    setProductionAccessToken(productionAccessToken) {
        this.setState({ productionAccessToken });
    }

    /**
     * Set Sandbox Access Token
     * @memberof ApiConsole
     */
    setSandboxAccessToken(sandboxAccessToken) {
        this.setState({ sandboxAccessToken });
    }

    /**
     * Set Username
     * @memberof ApiConsole
     */
    setUsername(username) {
        this.setState({ username });
    }

    /**
     * Set Password
     * @memberof ApiConsole
     */
    setPassword(password) {
        this.setState({ password });
    }

    /**
     * Set Password
     * @memberof ApiConsole
     */
    setSelectedKeyType(selectedKeyType, isUpdateToken) {
        if (isUpdateToken) {
            this.setState({ selectedKeyType }, this.updateAccessToken);
        } else {
            this.setState({ selectedKeyType });
        }
    }

    /**
     * Set Password
     * @memberof ApiConsole
     */
    setKeys(keys) {
        this.setState({ keys });
    }

    /**
     * Load the access token for given key type
     * @memberof TryOutController
     */
    updateAccessToken() {
        const {
            keys, selectedKeyType,
        } = this.state;
        let accessToken;
        if (keys.get(selectedKeyType)) {
            ({ accessToken } = keys.get(selectedKeyType).token);
        }
        if (selectedKeyType === 'PRODUCTION') {
            this.setProductionAccessToken(accessToken);
        } else {
            this.setSandboxAccessToken(accessToken);
        }
    }

    /**
     *
     * Provids the access token to the Swagger UI
     * @returns {*} access token
     * @memberof ApiConsole
     */
    accessTokenProvider() {
        const {
            securitySchemeType, username, password, productionAccessToken, sandboxAccessToken, selectedKeyType,
        } = this.state;
        if (securitySchemeType === 'BASIC') {
            const credentials = username + ':' + password;
            return btoa(credentials);
        }
        if (selectedKeyType === 'PRODUCTION') {
            return productionAccessToken;
        } else {
            return sandboxAccessToken;
        }
    }

    /**
     * Load the swagger file of the selected environemnt
     * @memberof ApiConsole
     */
    updateSwagger() {
        const {
            selectedEnvironment, api, environments,
        } = this.state;
        let promiseSwagger;

        if (selectedEnvironment) {
            if (environments.includes(selectedEnvironment)) {
                promiseSwagger = API.getSwaggerByAPIIdAndEnvironment(api.id, selectedEnvironment);
            } else {
                promiseSwagger = API.getSwaggerByAPIIdAndLabel(api.id, selectedEnvironment);
            }
        } else {
            promiseSwagger = API.getSwaggerByAPIId(api.id);
        }
        promiseSwagger.then((swaggerResponse) => {
            this.setState({ swagger: swaggerResponse.obj });
        });
    }

    /**
     * @inheritdoc
     * @memberof ApiConsole
     */
    render() {
        const { classes } = this.props;
        const {
            swagger, api, securitySchemeType, selectedEnvironment, productionAccessToken, sandboxAccessToken,
            labels, environments, scopes, username, password, selectedKeyType, serverError, settings,
        } = this.state;
        if (serverError) {
            return (
                <Typography variant='h4' className={classes.titleSub}>
                    {serverError}
                </Typography>
            );
        }

        if (api == null || swagger == null) {
            return <Progress />;
        }

        let isApiKeyEnabled = false;
        let authorizationHeader = api.authorizationHeader ? api.authorizationHeader : 'Authorization';
        if (api && api.securityScheme) {
            isApiKeyEnabled = api.securityScheme.includes('api_key');
            if (isApiKeyEnabled && securitySchemeType === 'API-KEY') {
                authorizationHeader = 'apikey';
            }
        }
        swagger.servers = settings;
        return (
            <>
                <Typography variant='h4' className={classes.titleSub}>
                    <FormattedMessage id='Apis.Details.index.Tryout' defaultMessage='Test Console' />
                </Typography>
                <Paper className={classes.paper}>
                    <TryOutController
                        setSecurityScheme={this.setSecurityScheme}
                        securitySchemeType={securitySchemeType}
                        setSelectedEnvironment={this.setSelectedEnvironment}
                        selectedEnvironment={selectedEnvironment}
                        productionAccessToken={productionAccessToken}
                        setProductionAccessToken={this.setProductionAccessToken}
                        sandboxAccessToken={sandboxAccessToken}
                        setSandboxAccessToken={this.setSandboxAccessToken}
                        swagger={swagger}
                        labels={labels}
                        environments={environments}
                        scopes={scopes}
                        setUsername={this.setUsername}
                        setPassword={this.setPassword}
                        username={username}
                        password={password}
                        setSelectedKeyType={this.setSelectedKeyType}
                        selectedKeyType={selectedKeyType}
                        updateSwagger={this.updateSwagger}
                        setKeys={this.setKeys}
                        api={this.state.api}
                    />
                    <SwaggerUI
                        api={this.state.api}
                        accessTokenProvider={this.accessTokenProvider}
                        spec={swagger}
                        authorizationHeader={authorizationHeader}
                        securitySchemeType={securitySchemeType}
                    />
                </Paper>
            </>
        );
    }
}
TestConsole.propTypes = {
    classes: PropTypes.shape({
        paper: PropTypes.string.isRequired,
        titleSub: PropTypes.string.isRequired,
        grid: PropTypes.string.isRequired,
        userNotificationPaper: PropTypes.string.isRequired,
        buttonIcon: PropTypes.string.isRequired,
    }).isRequired,
};
export default withAPI(withStyles(styles)(TestConsole));
