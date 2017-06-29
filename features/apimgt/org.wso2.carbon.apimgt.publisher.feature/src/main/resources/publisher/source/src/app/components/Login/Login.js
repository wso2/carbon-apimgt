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
import Utils from '../../data/utils'

class Login extends Component {

    constructor(props) {
        super(props);
        this.authManager = new AuthManager();
        this.state = {
            isLogin: false,
            referrer: "/",
        };
        this.doLogin = this.doLogin.bind(this);
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

    doLogin(e) {
        e.preventDefault();
        let username = document.getElementById('username').value;
        let password = document.getElementById('password').value;
        let loginPromise = this.authManager.authenticateUser(username, password);
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
                <div>
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
                                                          onSubmit={this.doLogin}
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
                                                                <input type="submit"
                                                                       className="btn btn-default btn-primary add-margin-right-2x"
                                                                       value="Sign In"/>
                                                            </div>
                                                        </div>
                                                        <a
                                                            className="add-margin-bottom-5x remove-margin-lg remove-margin-md">Forgot
                                                            Password</a>
                                                        <p className="hidden-xs hidden-sm">Don't
                                                            have
                                                            an
                                                            account?
                                                            <a>Register Now</a></p>
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
                    <Redirect to={this.state.referrer}/>
                </Switch>
            );
    }
}

export default Login;