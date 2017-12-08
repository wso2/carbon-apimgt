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
import {Switch, Redirect} from 'react-router-dom'
import AuthManager from '../../data/AuthManager'
import qs from 'qs'
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';
import User from '../../data/User'
import Footer from '../Base/Footer/Footer'
import Input, { InputLabel } from 'material-ui/Input';
import Select from 'material-ui/Select';
import {FormControl} from 'material-ui/Form';
import {MenuItem} from 'material-ui/Menu';
import Utils from "../../data/Utils";
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
        //Get Environments
        let promised_environments = ConfigManager.getConfigs().environments.then(response => {
            this.setState({
                environments: response.data.environments,
                environmentId: Utils.getEnvironmentID(response.data.environments)
            });
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
            user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, "/store");
            user.scopes = params.scopes.split(" ");
            AuthManager.setUser(user);
        }
    }

    handleInputChange = (event) => {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.id;

        this.setState({
            [name]: value
        });
    }

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
                    <Paper elevation={0} square={true} className="branding">
                      <div>
                          <img className="brand" src="/store/public/app/images/logo.svg" alt="wso2-logo"/>
                          <Typography type="headline" align="right" gutterBottom>
                              API STORE
                          </Typography>
                      </div>
                    </Paper>
                    <Paper elevation={1} square={true} className="login-paper">
                        <form onSubmit={this.handleSubmit} className="login-form">
                          <Typography type="body1" gutterBottom>
                              Sign in to your account
                          </Typography>
                            <TextField
                                error={!this.state.username && this.state.validate}
                                id="username"
                                label="Username"
                                type="text"
                                autoComplete="username"
                                margin="normal"
                                style={{width:"100%"}}
                                onChange={this.handleInputChange}
                            />
                            <TextField
                                error={!this.state.password && this.state.validate}
                                id="password"
                                label="Password"
                                type="password"
                                autoComplete="current-password"
                                margin="normal"
                                style={{width:"100%"}}
                                onChange={this.handleInputChange}
                            />

                            {/*Environments*/}
                            {this.state.environments && this.state.environments.length > 1 &&
                            <div>
                                <br/>
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

                            <Button type="submit" raised color="primary" className="login-form-submit">
                                Login
                            </Button>
                            <Button type="button" className="login-form-back">
                                Go Back
                            </Button>
                        </form>
                    </Paper>
                </div>
                <Footer/>
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
