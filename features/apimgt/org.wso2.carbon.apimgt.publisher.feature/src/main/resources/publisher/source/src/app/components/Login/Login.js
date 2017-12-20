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

import React, {Component} from 'react'
import './login.css'
import {Redirect, Switch} from 'react-router-dom'
import AuthManager from '../../data/AuthManager'
import qs from 'qs'
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';
import User from '../../data/User'
import ConfigManager from "../../data/ConfigManager";
import Utils from "../../data/Utils";
import Input, {InputLabel} from 'material-ui/Input';
import Select from 'material-ui/Select';
import {FormControl} from 'material-ui/Form';
import {MenuItem} from 'material-ui/Menu';
import {CircularProgress} from "material-ui/Progress";

class Login extends Component {

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false,
            referrer: "/",
            loading: false,
            username: '',
            password: '',
            validate: false,
            messageOpen: false,
            message:'',
            environments: {},
            environmentId: 0,
            authConfigs: {_updated: false},
        };
        this.fetch_ssoData = this.fetch_ssoData.bind(this);
    }

    componentDidMount() {
        //Get Environments and SSO data
        ConfigManager.getConfigs().environments.then(response => {
            const environments = response.data.environments;
            const environmentId = Utils.getEnvironmentID(environments);

            // Do not need to render before fetch sso data
            this.state.environments = environments;
            this.state.environmentId = environmentId;

            // Update environment to discard default environment configuration
            const environment = environments[environmentId];
            Utils.setEnvironment(environment);

            //Fetch SSO data and render
            this.fetch_ssoData(environment);
        });

        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        let params = qs.parse(queryString);
        if (params.referrer) {
            this.setState({referrer: params.referrer});
        }
        if (params.user_name) {
            this.setState({isLogin: true});
            const validityPeriod = params.validity_period; // In seconds
            const WSO2_AM_TOKEN_1 = params.partial_token;
            const user = new User(Utils.getEnvironment().label, params.user_name, params.id_token);
            user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, "/publisher");
            user.scopes = params.scopes.split(" ");
            AuthManager.setUser(user);
        }
    }

    fetch_ssoData(environment){
        this.state.authConfigs._updated = false;
        let promised_ssoData = Utils.getPromised_ssoData(environment);
        promised_ssoData.then(response => {
            response.data.members._updated = true;

            this.setState({
                authConfigs: response.data.members
            });
        });
    }

    handleSubmit = (e) => {
        const isSsoEnabled = this.state.authConfigs.is_sso_enabled.value;
        if(isSsoEnabled){
            this.handleSsoLogin(e);
        }else{
            this.handleDefaultLogin(e);
        }
    };

    handleSsoLogin = (e) => {
        e.preventDefault();
        const authorizationEndpoint = this.state.authConfigs.authorizationEndpoint.value;
        const client_id = this.state.authConfigs.client_id.value;
        const callback_URL = `${this.state.authConfigs.callback_url.value}`;
        const scopes = this.state.authConfigs.scopes.value;

        window.location = `${authorizationEndpoint}?response_type=code&client_id=${client_id}` +
            `&redirect_uri=${callback_URL}&scope=${scopes}`;
    };

    handleDefaultLogin = (e) => {
        e.preventDefault();
        this.setState({loading: true});
        this.setState({validate: true});
        let username = this.state.username;
        let password = this.state.password;
        let environment = this.state.environments[this.state.environmentId];

        if(!username || !password){
            this.setState({ messageOpen: true });
            this.setState({message: 'Please fill both username and password fields'});
            return;
        }

        let loginPromise = this.authManager.authenticateUser(username, password, environment);
        loginPromise.then((response) => {
            this.setState({isLogin: AuthManager.getUser(), loading: false});
        }).catch((error) => {
                this.setState({ messageOpen: true });
                this.setState({message: error});
                console.log(error);
                this.setState({loading: false});
            }
        );
    };

    handleInputChange = (event) => {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.id;

        this.setState({
            [name]: value
        });
    };

    handleEnvironmentChange = (event) => {
        const environmentId = event.target.value;
        let environment = this.state.environments[environmentId];

        //Get sso data of selected environment
        this.fetch_ssoData(environment);

        this.setState({
            environmentId
        });
    };

    handleRequestClose = () => {
        this.setState({ messageOpen: false });
    };

    render() {
        const isMoreThanTwoEnvironments = this.state.environments && this.state.environments.length > 1;
        const isSsoUpdated = this.state.authConfigs._updated;
        const isSsoEnabled = isSsoUpdated ? this.state.authConfigs.is_sso_enabled.value : undefined;

        if(isSsoEnabled && !isMoreThanTwoEnvironments){ // If sso enabled and no more than two environments
            this.handleSsoLogin();
        }

        // Show login page if sso disabled or more than two environments
        if (!this.state.isLogin) { // If not logged in, go to login page
            return (
                <div className="login-flex-container">
                    <Snackbar
                        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
                        open={this.state.messageOpen}
                        onRequestClose={this.handleRequestClose}
                        SnackbarContentProps={{
                            'aria-describedby': 'message-id',
                        }}
                        message={<span id="message-id">{this.state.message}</span>}
                    />
                    <div className="login-main-content">
                        <Paper className="login-paper">
                            <form onSubmit={this.handleSubmit} className="login-form">
                                <div>
                                    <img className="brand" src="/publisher/public/app/images/logo.svg" alt="wso2-logo"/>
                                    <Typography type="subheading" gutterBottom>
                                        API Publisher
                                    </Typography>
                                    <Typography type="caption" gutterBottom>
                                        Login to continue
                                    </Typography>
                                </div>

                                {/*Environments*/}
                                {isMoreThanTwoEnvironments &&
                                <div>
                                    <br/><br/>
                                    <FormControl>
                                        <InputLabel htmlFor="environment">Environment</InputLabel>
                                        <Select onChange={this.handleEnvironmentChange} value={this.state.environmentId}
                                                input={<Input id="environment"/>}>
                                            {this.state.environments.map((environment, index) =>
                                                <MenuItem value={index} key={index}>{environment.label}</MenuItem>
                                            )}
                                        </Select>
                                    </FormControl>
                                </div>
                                }

                                {isSsoUpdated ?
                                    <div>
                                        {isSsoEnabled ?
                                            <div style={{width: '100%', marginTop: '29%', fontSize: 'medium'}}>
                                                Single Sign On is enabled.
                                            </div>
                                            :
                                            <div>
                                                <TextField
                                                    error={!this.state.username && this.state.validate}
                                                    id="username"
                                                    label="Username"
                                                    type="text"
                                                    autoComplete="username"
                                                    margin="normal"
                                                    style={{width: "100%"}}
                                                    onChange={this.handleInputChange}
                                                />
                                                <TextField
                                                    error={!this.state.password && this.state.validate}
                                                    id="password"
                                                    label="Password"
                                                    type="password"
                                                    autoComplete="current-password"
                                                    margin="normal"
                                                    style={{width: "100%"}}
                                                    onChange={this.handleInputChange}
                                                />
                                            </div>
                                        }
                                    </div>
                                    :
                                    <div style={{width: '100%', marginTop: '15%', marginBottom: '10%'}}>
                                        <CircularProgress style={{margin: 'auto', display: 'block'}}/>
                                    </div>
                                }

                                <Button
                                    type="submit"
                                    raised color="primary"
                                    className="login-form-submit"
                                    disabled={!isSsoUpdated}
                                >
                                    {isSsoEnabled ? "Visit Login Page" : "Login"}
                                </Button>

                            </form>
                        </Paper>
                    </div>
                    <div className="login-footer">
                        WSO2 | © 2017
                        <a href="http://wso2.com/" target="_blank"><i
                            className="icon fw fw-wso2"/> Inc</a>.
                    </div>
                </div>
            );
        } else {// If logged in, redirect to /apis page
            return (
                <Switch>
                    <Redirect to={this.state.referrer}/>
                </Switch>
            );
        }
    }
}

export default Login;