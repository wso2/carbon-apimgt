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
import {Switch, Redirect} from 'react-router-dom'
import AuthManager from '../../data/AuthManager'
import qs from 'qs'

export default class LoginForm extends Component {
    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {isLogin: false, referrer: "/", username: "", password: "", remember: false};
        this.doLogin = this.doLogin.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
    }

    componentDidMount() {
        let queryString = this.props.location.search;
        queryString = queryString.replace(/^\?/, '');
        /* With QS version up we can directly use {ignoreQueryPrefix: true} option */
        let params = qs.parse(queryString);
        if (params.referrer) {
            this.setState({referrer: params.referrer});
        }
    }

    handleInputs(e) {
        let value = e.target.value;
        if (e.target.type === "checkbox") {
            value = e.target.checked
        }
        this.setState({[e.target.name]: value});
    }

    doLogin(e) {
        e.preventDefault();
        let loginPromise = this.authManager.authenticateUser(this.state.username, this.state.password);
        loginPromise.then((response) => {
            this.setState({isLogin: AuthManager.getUser()});
        }).catch((error) => {
                console.log(error);
            }
        );
    };

    render() {
        if (!this.state.isLogin) { // If not logged in, go to login page
            return (
                <div className="data-container">
                    <form className="form-horizontal" method="post" id="loginForm" onSubmit={this.doLogin}>
                        <h3>Sign in to your account</h3>
                        <div className="form-group">
                            <div className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                <div
                                    className="input-group input-wrap">
                                    <input className="form-control" name="username" placeholder="Username"
                                           onChange={this.handleInputs} value={this.state.username} type="text"/>
                                </div>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                <div className="input-group input-wrap">
                                    <input className="form-control" name="password" placeholder="Password"
                                           onChange={this.handleInputs} value={this.state.password}
                                           type="password"/>
                                </div>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                <input id="rememberMe" name="remember"
                                       onChange={this.handleInputs} checked={this.state.remember} type="checkbox"/>
                                <span className="checkbox-font">Remember Me</span>
                            </div>
                        </div>
                        <div className="form-group">
                            <div className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                <input type="submit" className="btn btn-default btn-primary add-margin-right-2x"
                                       value="Sign In"/>
                            </div>
                        </div>
                        <a className="add-margin-bottom-5x remove-margin-lg remove-margin-md">Forgot Password</a>
                        <p className="hidden-xs hidden-sm">Don't have an account? <a>Register Now</a></p>
                    </form>
                </div>
            )
        } else {
            return (
                <Switch>
                    <Redirect to={this.state.referrer}/>
                </Switch>
            );
        }
    }
}