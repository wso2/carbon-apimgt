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
import AuthManager from '../../data/AuthManager';

class Login extends Component {

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false
        }
    }

    doLogin() {
        var username = document.getElementById('username').value;
        var password = document.getElementById('password').value;
        var loginPromise = this.authManager.authenticateUser(username, password);
        loginPromise.then((data) => {
            AuthManager.setAuthStatus(true);
            AuthManager.setUserName(data.data.authUser);
            this.authManager.setUserScope(data.data.scopes);
            var expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
            window.localStorage.setItem("expiresIn", expiresIn);
            window.localStorage.setItem("user", data.authUser);
            window.localStorage.setItem("rememberMe", document.getElementById("rememberMe").checked);
            window.localStorage.setItem("userScopes", data.scopes);
            this.setState({isLogin: AuthManager.getAuthStatus()});
        });
        loginPromise.catch(function (error) {
                var error_data = JSON.parse(error.responseText);
                var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message;
                noty({
                    text: message,
                    type: 'error',
                    dismissQueue: true,
                    modal: true,
                    progressBar: true,
                    timeout: 5000,
                    layout: 'top',
                    theme: 'relax',
                    maxVisible: 10,
                });
            }
        );
    };

    render() {
        if (!this.state.isLogin) { // If not logged in, go to login page
            return (
                <div>
                    <meta charSet="utf-8"/>
                    <meta httpEquiv="X-UA-Compatible" content="IE=edge"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1"/>
                    <link rel="shortcut icon"
                          href="/publisher/public/components/root/base/images/favicon.png"
                          type="image/png"/>
                    <title>Login | API Publisher</title>
                    <header className="header header-default">
                        <div className="container-fluid">
                            <div className="pull-left brand float-remove-xs text-center-xs">
                                <a href="/publisher/">
                                    <img
                                        src="/publisher/public/images/logo.svg"

                                        className="logo"/>
                                    <h1>API Publisher</h1>
                                </a>
                            </div>
                        </div>
                    </header>
                    <div className="page-content-wrapper" style={{background: 'white'}}>
                        <div className="container-fluid body-wrapper">
                            <div id="general-alerts">
                                <div className="alert alert-danger" role="alert"
                                     style={{display: 'none'}}>
                                    <div className="alert-message">
                                    </div>
                                    <button type="button" className="close" aria-label="close"
                                            data-dismiss="alert">
                  <span aria-hidden="true">
                    <i className="fw fw-cancel"/>
                  </span>
                                    </button>
                                </div>
                            </div>
                            <div className="page-content-wrapper">
                                <div className="container" style={{background: 'white'}}>
                                    <div className="login-form-wrapper">
                                        <div className="row">
                                            <div
                                                className="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                                                <div
                                                    className="brand-container add-margin-bottom-5x">
                                                    <div className="row">
                                                        <div className="col-xs-6 col-sm-3 col-md-9 col-lg-9 center-block float-remove-sm
                                float-remove-xs pull-right-md pull-right-lg">
                                                            <img
                                                                className="img-responsive brand-spacer"
                                                                src="/publisher/public/images/logo.svg"
                                                                alt="wso2-logo"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div
                                                className="col-xs-12 col-sm-12 col-md-9 col-lg-9 login">
                                                <div className="data-container">
                                                    <form className="form-horizontal"
                                                          method="post"
                                                          id="loginForm"
                                                          onKeyDown={ (e) => {
                                                              if (e.keyCode == '13') {
                                                                  this.doLogin();
                                                              }
                                                          }
                                                          }
                                                    >
                                                        <h3>Sign in to your account</h3>
                                                        <div className="form-group">
                                                            <div
                                                                className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                                                <div
                                                                    className="input-group input-wrap">
                                                                    <input
                                                                        className="form-control"
                                                                        id="username"
                                                                        name="username"
                                                                        placeholder="Username"
                                                                        type="text"/>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <div className="form-group">
                                                            <div
                                                                className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                                                <div
                                                                    className="input-group input-wrap">
                                                                    <input
                                                                        className="form-control"
                                                                        id="password"
                                                                        name="password"
                                                                        placeholder="Password"
                                                                        type="password"/>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <div className="form-group">
                                                            <div
                                                                className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                                                <input id="rememberMe"
                                                                       name="rememberMe"
                                                                       type="checkbox"/>
                                                                <span className="checkbox-font">Remember Me</span>
                                                            </div>
                                                        </div>
                                                        <div className="form-group">
                                                            <div
                                                                className="col-xs-12 col-sm-12 col-md-5 col-lg-5">
                                                                <button type="button"
                                                                        id="loginButton"
                                                                        className="btn btn-default btn-primary add-margin-right-2x"
                                                                        onClick={this.doLogin.bind(this)}>
                                                                    Sign In
                                                                </button>
                                                            </div>
                                                        </div>
                                                        <a href
                                                           className="add-margin-bottom-5x remove-margin-lg remove-margin-md">Forgot
                                                                                                                              Password</a>
                                                        <p className="hidden-xs hidden-sm">Don't
                                                                                           have
                                                                                           an
                                                                                           account?
                                                            <a
                                                                href>Register Now</a></p>
                                                    </form>
                                                </div>
                                                <p className="visible-xs visible-sm add-margin-2x text-center">
                                                    Don't have an account?
                                                    <a href="#">Register Now</a>
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <footer className="footer">
                        <div className="container-fluid">
                            <p>
                                WSO2 | © 2016
                                <a href="http://wso2.com/" target="_blank"><i
                                    className="icon fw fw-wso2"/> Inc</a>.
                            </p>
                        </div>
                    </footer>
                </div>
            )
        } else // If logged in, redirect to /apis page
            return (
                <Switch>
                    <Redirect from={'/login'} to={"/apis"}/>
                </Switch>
            );
    }
}

export default Login;