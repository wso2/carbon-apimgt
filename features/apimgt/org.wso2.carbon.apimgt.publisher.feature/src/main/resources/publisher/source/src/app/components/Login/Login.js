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
            environmentId: 0
        };

        //Get Environments
        let promised_environments = ConfigManager.getConfigs().environments.then(response => {
            this.setState({
                environments: response.data.environments
            });
        });
    }


    handleSubmit = (e) => {
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
    }

    componentDidMount() {
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
            const user = new User(params.user_name, params.id_token);
            user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, "/publisher");
            user.scopes = params.scopes.split(" ");
            AuthManager.setUser(user);
        }
    }


    handleUsernameChange = (event) => {
        this.setState({
            username : event.target.value
        });
    };
    handlePasswordChange = (event) => {
        this.setState({
            password : event.target.value
        });
    };

    handleEnvironmentChange = (event) => {
        this.setState({
            environmentId : event.target.value
        });
    };

    handleClickEnvironmentMenu = (event) => {
        this.setState({ openEnvironmentMenu: true, anchorElEnvironmentMenu: event.currentTarget });
    };

    handleRequestCloseEnvironmentMenu = (event) => {
        this.setState({ openEnvironmentMenu: false});
    };

    handleRequestClose = () => {
        this.setState({ messageOpen: false });
    };
    render() {
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
                                    <img className="brand" src="/publisher/public/images/logo.svg" alt="wso2-logo"/>
                                    <Typography type="subheading" gutterBottom>
                                        API Publisher
                                    </Typography>
                                    <Typography type="caption" gutterBottom>
                                        Login to continue
                                    </Typography>
                                </div>

                                <TextField
                                    error={!this.state.username && this.state.validate}
                                    id="username"
                                    label="Username"
                                    type="text"
                                    autoComplete="username"
                                    margin="normal"
                                    style={{width:"100%"}}
                                    onChange={this.handleUsernameChange}
                                />
                                <TextField
                                    error={!this.state.password && this.state.validate}
                                    id="password"
                                    label="Password"
                                    type="password"
                                    autoComplete="current-password"
                                    margin="normal"
                                    style={{width:"100%"}}
                                    onChange={this.handlePasswordChange}
                                />

                                {/*Environments*/}
                                {this.state.environments && this.state.environments.length > 1 &&
                                <div>
                                    <label>Environment </label>
                                    <select id="environment" onChange={this.handleEnvironmentChange}>
                                        {this.state.environments.map((environment, index) =>
                                            <option value={index} key={index}>{environment.label}</option>
                                        )}
                                    </select>
                                </div>
                                }

                                <Button type="submit" raised color="primary"  className="login-form-submit">
                                    Login
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