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
import LoginBase from './LoginBase'
import LoginForm from './LoginForm'

const Login = (props) => {
    return (
        <LoginBase>
            <div className="page-content-wrapper" style={{background: 'white'}}>
                <div className="container-fluid body-wrapper">
                    <div className="page-content-wrapper">
                        <div className="container" style={{background: 'white'}}>
                            <div className="login-form-wrapper">
                                <div className="row">
                                    <div className="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                                        <div className="brand-container add-margin-bottom-5x">
                                            <div className="row">
                                                <div className="col-xs-6 col-sm-3 col-md-9 col-lg-9 center-block float-remove-sm
                                float-remove-xs pull-right-md pull-right-lg">
                                                    <img className="img-responsive brand-spacer"
                                                         src="/publisher/public/images/logo.svg" alt="wso2-logo"/>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-xs-12 col-sm-12 col-md-9 col-lg-9 login">
                                        <LoginForm {...props}/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </LoginBase>
    );
};

export default Login;