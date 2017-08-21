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
import './login.css'

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
            message:''
        };
    }


    handleSubmit = (e) => {
        e.preventDefault();
        this.setState({loading: true});
<<<<<<< HEAD
        this.setState({validate: true});
        let username = this.state.username;
        let password = this.state.password;
        if(!username || !password){
            this.setState({ messageOpen: true });
            this.setState({message: 'Please fill both username and password fields'});
            return;
        }
        let loginPromise = this.authManager.authenticateUser(username, password);
        loginPromise.then((response) => {
            this.setState({isLogin: AuthManager.getUser(), loading: false});
        }).catch((error) => {
                this.setState({ messageOpen: true });
                this.setState({message: error});
                console.log(error);
                this.setState({loading: false});
=======
        this.props.form.validateFields((err, values) => {
            if (!err) {
                let username = values.userName;
                let password = values.password;
                let currentEnvironment = values.currentEnv;
                if(typeof currentEnvironment == "undefined"){
                    localStorage.setItem("currentEnv","default");
                }else{
                    localStorage.setItem("currentEnv",currentEnvironment);
                }

                var detailedValue ;
                for (let value of this.state.env) {

                    if (currentEnvironment == value.env) {

                        detailedValue = value;
                        console.log(detailedValue);
                    }
                }
                let loginPromise = this.authManager.authenticateUser(username, password,detailedValue);
                loginPromise.then((response) => {
                    this.setState({isLogin: AuthManager.getUser(), loading: false});
                }).catch((error) => {
                        message.error("error");
                        console.log(error);
                        this.setState({loading: false});
                    }
                );
            } else {
>>>>>>> fixed issues and tested the UI part
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
<<<<<<< HEAD
=======
        let envDetails = this.configManager;
        envDetails.env_response.then((response) => {
            let enviromentDetails = response.data.environments;
            this.setState({env: enviromentDetails});
        });

>>>>>>> fixed issues and tested the UI part
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
    render() {
<<<<<<< HEAD
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

=======
        const {getFieldDecorator} = this.props.form;
        const makeEmptySuffix = this.state.userNameEmpty ? <Icon type="close-circle" onClick={this.emitEmpty}/> : '';
        const environmentLength = this.state.env.length;



        if (!this.state.isLogin) { // If not logged in, go to login page
            return (
                <Form onSubmit={this.handleSubmit} className="login-form">
                    <FormItem>
                        {getFieldDecorator('userName', {
                            rules: [{required: true, message: 'Please input your username!'}],
                        })(
                            <Input prefix={<Icon type="user" style={{fontSize: 13}}/>} placeholder="Username"
                                   suffix={makeEmptySuffix}/>
                        )}
                    </FormItem>
                    <FormItem>
                        {getFieldDecorator('password', {
                            rules: [{required: true, message: 'Please input your Password!'}],
                        })(
                            <Input prefix={<Icon type="lock" style={{fontSize: 13}}/>} type="password"
                                   placeholder="Password"/>
                        )}
                    </FormItem>
                    { environmentLength > 1 &&
                        <FormItem
                            hasFeedback>
                            {getFieldDecorator('currentEnv', {
                                initialValue: this.state.env[0].env,
                                rules: [
                                    {required: true, message: 'Please select Environment ! '},
                                ],
                            })(
                                (<Select placeholder="Select Environment ">
                                    {this.state.env.map(environment => <Option
                                        key={environment.env}>{environment.env}</Option>)}
                                </Select>)
                            )}
                        </FormItem>
                    }
                    <FormItem>
                        {getFieldDecorator('remember', {
                            valuePropName: 'checked',
                            initialValue: true,
                        })(
                            <Checkbox>Remember me</Checkbox>
                        )}
                        <Button loading={this.state.loading} type="primary" htmlType="submit"
                                className="login-form-button">
                            Log in
                        </Button>
                    </FormItem>
                </Form>
>>>>>>> fixed issues and tested the UI part
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