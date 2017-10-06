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
import ConfigManager from '../../data/ConfigManager'
import qs from 'qs'
import TextField from 'material-ui/TextField';
import Paper from 'material-ui/Paper';
import Button from 'material-ui/Button';
import Typography from 'material-ui/Typography';
import Snackbar from 'material-ui/Snackbar';

import './login.css'
import { Menu, Dropdown, Icon, message } from 'antd';
import User from '../../data/User'

class Login extends Component {

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.configManager = new ConfigManager();
        this.state = {
            isLogin: false,
            referrer: "/",
            loading: false,
            username: '',
            password: '',
            validate: false,
            messageOpen: false,
            message:'',
            env: [],
            anchorEl: undefined,
            open: false,
            key: 'default'
        };
    }


    handleSubmit = (e) => {
        e.preventDefault();
        this.setState({loading: true});
        this.setState({validate: true});
        let username = this.state.username;
        let password = this.state.password;
        let environment = this.state.key;
        localStorage.setItem("currentEnv",environment);
        if(!username || !password){
            this.setState({ messageOpen: true });
            this.setState({message: 'Please fill both username and password fields'});
            return;
        }

        var detailedValue ;
        for (let value of this.state.env) {

            if (environment == value.env) {

                detailedValue = value;
                console.log(detailedValue);
            }
        }

        let loginPromise = this.authManager.authenticateUser(username, password, detailedValue);
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
        let envDetails = this.configManager;
        envDetails.env_response.then((response) => {
            let enviromentDetails = response.data.environments;
            console.log(enviromentDetails);
            this.setState({env: enviromentDetails});
        });

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

    handleRequestClose = () => {
        this.setState({ messageOpen: false });
    };


    onClick =({ key }) =>{
        console.log(key);
        this.setState({key:key})
    }
    render() {
        const arrayies = this.state.env;
        const environmentLength = this.state.env.length;

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
                    message={<span id="message-id"></span>}
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


                        { environmentLength > 1 &&
                            <div>
                                <Dropdown overlay={<Menu onClick={this.onClick}>
                                {this.state.env.map(environment => <Menu.Item
                                    key={environment.env}>{environment.env}</Menu.Item>)}
                            </Menu>}>
                                <a className="ant-dropdown-link" href="#">
                                    Environments <Icon type="down" />
                                </a>
                            </Dropdown></div>

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