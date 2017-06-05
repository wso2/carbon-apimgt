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
import {Link} from 'react-router-dom'

const LeftNav = (props) => {
    return (
        <div>
            {/* .left-sidebar */}
            <div className="sidebar-wrapper sidebar-nav affix-top" data-side="left" data-width={260}
                 data-container=".page-content-wrapper" data-container-divide="true" data-fixed-offset-top={0}
                 data-spy="affix" data-offset-top={80} id="left-sidebar">
                <div className="nano ">
                    <div className="nano-content ">
                        <ul className="nav nav-pills nav-stacked pages">
                            <li><Link to="/apis"><i className="fw fw-api"/> APIs</Link></li>
                            <li><a className="icon" href="#"><i className="fw fw-statistics"/> Statistics</a></li>
                            <li><a className="icon" href="#"><i className="fw fw-subscribe"/> Subscriptions</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LeftNav