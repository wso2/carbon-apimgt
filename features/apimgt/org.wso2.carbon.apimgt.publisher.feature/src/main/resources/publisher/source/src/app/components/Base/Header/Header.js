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
import {Link} from "react-router-dom";
import AuthManager from '../../../data/Auth.js';

const Header = (props) => {
    return (
        <header className="header header-default">
            <div className="container-fluid">
                <div id="nav-icon1" className="menu-trigger navbar-left " data-toggle="sidebar"
                     data-target="#left-sidebar" data-container=".page-content-wrapper" data-container-divide="true"
                     aria-expanded="false" rel="sub-nav">
                    <span />
                    <span />
                    <span />
                </div>
                <div className="pull-left brand">
                    <Link to="/">
                        <span>APIM Publisher</span>
                    </Link>
                </div>
                <ul className="nav navbar-right">
                    <li className="visible-inline-block">
                        <a className="dropdown" data-toggle="dropdown" aria-expanded="false">
                <span className="icon fw-stack">
                  <i className="fw fw-circle fw-stack-2x"/>
                  <i className="fw fw-user fw-stack-1x fw-inverse"/>
                </span>
                            <span className="hidden-xs add-margin-left-1x" id="logged-in-username">
                                { AuthManager.getUserName()}
                            <span className="caret"/></span>
                        </a>
                        <ul className="dropdown-menu dropdown-menu-right slideInDown" role="menu">
                            <li><a href="#">Profile Settings</a></li>
                            <li>
                                <Link to="/logout">Logout</Link>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </header >
    );
};

export default Header