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

import React from 'react'

const LoginBase = (props) => {
    return (
        <div>
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
            {props.children}
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

    );
};

export default LoginBase