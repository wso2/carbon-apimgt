/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { Component } from 'react';
import qs from 'qs';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import Alert from 'AppComponents/Shared/Alert';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import AuthManager from 'AppData/AuthManager';
import Footer from 'AppComponents/Base/Footer/Footer';
import User from 'AppData/User';
import ConfigManager from 'AppData/ConfigManager';
import Utils from 'AppData/Utils';
import { Redirecting, Progress } from 'AppComponents/Shared';
import { FormattedMessage } from 'react-intl';

import './login.css';

/**
 * Login page React Component
 * @class Login
 * @extends {Component}
 */
class Login extends Component {
    // TODO: [rnk] This Login component should be shared. Store/Login is coded with props
    /**
     * Creates an instance of Login.
     * @param {any} props @inheritDoc
     * @memberof Login
     */
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false,
            username: '',
            password: '',
            validate: false,
            environments: [],
            environmentId: 0,
            loginStatusEnvironments: [],
            authConfigs: [],
            redirectToIS: false,
        };
        this.fetch_DCRAppInfo = this.fetchDCRAppInfo.bind(this);
        this.handleRedirectionFromIDP = this.handleRedirectionFromIDP.bind(this);
    }

    /**
     * @inheritDoc
     * @memberof Login
     */
    componentDidMount() {
        let idToken = this.handleRedirectionFromIDP();
        // Get Environments and SSO data
        ConfigManager.getConfigs()
            .environments.then((response) => {
                const { environments } = response.data;
                let environmentId = Utils.getEnvironmentID(environments);
                if (environmentId === -1) {
                    environmentId = 0;
                }
                this.setState({ environments, environmentId });

                // Update environment to discard default environment configuration
                const environment = environments[environmentId];
                Utils.setEnvironment(environment);

                // Set authentication status of environments
                this.setLoginStatusOfEnvironments(environments);

                // Fetch DCR App data and handle SSO login if redirected from IDP
                this.fetchDCRAppInfo(environments, idToken);
                idToken = null; // Discard ID Token
            })
            .catch((error) => {
                console.error('Error while receiving environment configurations : ', error);
            });
    }

    setLoginStatusOfEnvironments(environments) {
        const loginStatusEnvironments = environments.map((environment) => {
            return AuthManager.getUser(environment.label) !== null;
        });
        this.setState({ loginStatusEnvironments });
    }

    fetchDCRAppInfo(environments, idToken) {
        // Array of promises
        const promisedSSoData = environments.map(environment => Utils.getPromisedDCRAppInfo(environment));

        Promise.all(promisedSSoData)
            .then((responses) => {
                const authConfigs = responses.map(response => response.data);
                this.setState({ authConfigs });
                // If idToken is not null or redirected from IDP
                if (idToken) this.authManager.handleAutoLoginEnvironments(idToken, environments, authConfigs);
                Utils.setMultiEnvironmentOverviewEnabledInfo(environments, authConfigs);
            })
            .catch((error) => {
                console.error('Error while creating/receiving DCR Application info : ', error);
            });
    }

    /**
     *
     *
     * @returns
     * @memberof Login
     */
    handleRedirectionFromIDP() {
        const { search } = this.props.location;
        const queryString = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const params = qs.parse(queryString);
        if (params.user_name) {
            const environmentName = Utils.getCurrentEnvironment().label;
            const validityPeriod = params.validity_period; // In seconds
            const WSO2_AM_TOKEN_1 = params.partial_token;

            if (WSO2_AM_TOKEN_1) {
                const user = new User(environmentName, params.user_name);
                user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, '/publisher-new');
                user.scopes = params.scopes.split(' ');
                AuthManager.setUser(user);
                this.setState({ isLogin: true });
                this.props.updateUser(user);
                this.props.history.push(params.referrer);
                console.log(`Successfully login to : ${environmentName}`);
            } else {
                this.setState({ isLogin: false });
                console.error(`Login failed in : ${environmentName}`);
            }
        }
        // return id token if exists
        return params.id_token;
    }

    handleSubmit = (e) => {
        const isSsoEnabled = this.state.authConfigs[this.state.environmentId].is_sso_enabled.value;
        if (isSsoEnabled) {
            this.setState({ redirectToIS: true });
            const environment = this.state.environments[this.state.environmentId];
            Utils.setEnvironment(environment);
            this.handleSsoLogin(e);
        } else {
            this.handleDefaultLogin(e);
        }
    };

    handleSsoLogin = (e) => {
        if (e) {
            e.preventDefault();
        }
        const authConfigs = this.state.authConfigs[this.state.environmentId];
        const authorizationEndpoint = authConfigs.authorizationEndpoint.value;
        const clientId = authConfigs.client_id.value;
        const callbackURL = authConfigs.callback_url.value;
        const scopes = authConfigs.scopes.value;

        window.location =
            `${authorizationEndpoint}?response_type=code&client_id=${clientId}` +
            `&redirect_uri=${callbackURL}&scope=${scopes}`;
    };

    handleDefaultLogin = (e) => {
        e.preventDefault();
        this.setState({ validate: true });
        const { username, password } = this.state;
        const environment = this.state.environments[this.state.environmentId];
        const { search } = this.props.location;
        const queryString = search.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        const params = qs.parse(queryString);

        if (!username || !password) {
            Alert.error('Please fill both username and password fields');
            return;
        }

        const loginPromise = this.authManager.authenticateUser(username, password, environment);
        loginPromise
            .then((response) => {
                this.setState({ isLogin: AuthManager.getUser() });
                this.props.updateUser(AuthManager.getUser());
                this.authManager.handleAutoLoginEnvironments(
                    response.data.idToken,
                    this.state.environments,
                    this.state.authConfigs,
                );
                this.props.history.push(params.referrer || '/');
            })
            .catch((error) => {
                const { response } = error;
                Alert.error('Error occurred while login to the Publisher!');
                if (response) {
                    const { data } = response;
                    Alert.error(`Error [${data.code}]: ${data.description}`);
                }
                console.log(error);
            });
    };

    handleInputChange = (event) => {
        const { target } = event;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.id;

        this.setState({
            [name]: value,
        });
    };

    handleEnvironmentChange = (event) => {
        const environmentId = event.target.value;
        const environment = this.state.environments[environmentId];
        const isLogin = this.state.loginStatusEnvironments[environmentId];
        if (isLogin) {
            Utils.setEnvironment(environment);
        }
        this.setState({ environmentId, isLogin });
    };

    /**
     * @inheritDoc
     * @returns {React.Component} Login component
     * @memberof Login
     */
    render() {
        const isMoreThanOneEnvironments = this.state.environments && this.state.environments.length > 1;
        const isSsoUpdated = this.state.authConfigs.length !== 0;
        const isSsoEnabled = isSsoUpdated && this.state.authConfigs[this.state.environmentId].is_sso_enabled.value;

        // Redirect to Identity Provider
        if (this.state.redirectToIS) {
            return <Redirecting message='You are now being redirected to Identity Provider.' />;
        }

        if (isSsoEnabled && !isMoreThanOneEnvironments) {
            // If sso enabled and no more than one environments
            this.handleSsoLogin();
        }

        // Show login page if sso disabled or more than two environments
        if (!this.state.isLogin) {
            // If not logged in, go to login page
            return (
                <div className='login-flex-container'>
                    <Grid container justify='center' alignItems='center' spacing={0} style={{ height: '100vh' }}>
                        <Grid item lg={6} md={8} xs={10}>
                            <Grid container>
                                {/* Brand */}
                                <Grid item sm={3} xs={12}>
                                    <Grid container direction='column'>
                                        <Grid item>
                                            <img
                                                className='brand'
                                                src='/publisher-new/site/public/images/logo.svg'
                                                alt='wso2-logo'
                                            />
                                        </Grid>
                                        <Grid item>
                                            <Typography type='subheading' align='right' gutterBottom>
                                                <FormattedMessage id='api.publisher' defaultMessage='API PUBLISHER' />
                                            </Typography>
                                        </Grid>
                                    </Grid>
                                </Grid>

                                {/* Login Form */}
                                <Grid item sm={9} xs={12}>
                                    <div className='login-main-content'>
                                        <Paper elevation={1} square className='login-paper'>
                                            <form onSubmit={this.handleSubmit} className='login-form'>
                                                <Typography type='body1' gutterBottom>
                                                    <FormattedMessage
                                                        id='sign.in.account'
                                                        defaultMessage='Sign in to your account'
                                                    />
                                                </Typography>

                                                {/* Environments */}
                                                {isMoreThanOneEnvironments && (
                                                    <FormControl style={{ width: '100%', marginTop: '2%' }}>
                                                        <InputLabel htmlFor='environment'>
                                                            <FormattedMessage
                                                                id='environment'
                                                                defaultMessage='Environment'
                                                            />
                                                        </InputLabel>
                                                        <Select
                                                            onChange={this.handleEnvironmentChange}
                                                            value={this.state.environmentId}
                                                            input={<Input id='environment' />}
                                                        >
                                                            {this.state.environments.map((environment, index) => (
                                                                <MenuItem value={index} key={environment.host}>
                                                                    {environment.label}
                                                                </MenuItem>
                                                            ))}
                                                        </Select>
                                                    </FormControl>
                                                )}

                                                {isSsoUpdated ? (
                                                    <span>
                                                        {isSsoEnabled ? (
                                                            <FormControl
                                                                style={{
                                                                    width: '100%',
                                                                    fontSize: 'medium',
                                                                    marginTop: '5%',
                                                                }}
                                                            >
                                                                <FormattedMessage
                                                                    id='single.sign.on.enabled'
                                                                    defaultMessage='Single Sign On is enabled.'
                                                                />
                                                            </FormControl>
                                                        ) : (
                                                            <FormControl style={{ width: '100%' }}>
                                                                <TextField
                                                                    error={!this.state.username && this.state.validate}
                                                                    id='username'
                                                                    label={
                                                                        <FormattedMessage
                                                                            id='username'
                                                                            defaultMessage='Username'
                                                                        />
                                                                    }
                                                                    type='text'
                                                                    autoComplete='username'
                                                                    margin='normal'
                                                                    style={{ width: '100%' }}
                                                                    onChange={this.handleInputChange}
                                                                />
                                                                <TextField
                                                                    error={!this.state.password && this.state.validate}
                                                                    id='password'
                                                                    label={
                                                                        <FormattedMessage
                                                                            id='password'
                                                                            defaultMessage='Password'
                                                                        />
                                                                    }
                                                                    type='password'
                                                                    autoComplete='current-password'
                                                                    margin='normal'
                                                                    style={{ width: '100%' }}
                                                                    onChange={this.handleInputChange}
                                                                />
                                                            </FormControl>
                                                        )}
                                                    </span>
                                                ) : (
                                                    <FormControl style={{ width: '100%', margin: '10% 0 6% 0' }}>
                                                        <Progress />
                                                    </FormControl>
                                                )}

                                                {/* Buttons */}
                                                <Button
                                                    type='submit'
                                                    variant='raised'
                                                    color='primary'
                                                    className='login-form-submit'
                                                    disabled={!isSsoUpdated}
                                                >
                                                    {isSsoEnabled ? (
                                                        <FormattedMessage
                                                            id='visit.login.page'
                                                            defaultMessage='Visit Login Page'
                                                        />
                                                    ) : (
                                                        <FormattedMessage id='login' defaultMessage='Login' />
                                                    )}
                                                </Button>
                                            </form>
                                        </Paper>
                                    </div>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                    <Footer />
                </div>
            );
        } else {
            return 'Redirecting ...';
        }
    }
}

Login.propTypes = {
    location: PropTypes.shape({
        search: PropTypes.string,
    }).isRequired,
    history: PropTypes.shape({
        push: PropTypes.func,
    }).isRequired,
    updateUser: PropTypes.func.isRequired,
};

export default Login;
