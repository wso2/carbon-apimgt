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

export default class NavBar extends Component {
    constructor(props) {
        super(props);
        const path_sections = props.location.pathname.split('/');
        let details_action = path_sections[path_sections.indexOf("apis") + 2];
        let active_tab = (Object.values(NavBar.CONS).includes(details_action)) ? details_action : NavBar.CONS.OVERVIEW;
        this.state = {
            activeTab: active_tab
        };
        this.setActive = this.setActive.bind(this);
    }

    static get CONS() {
        return {
            OVERVIEW: "overview",
            LIFECYCLE: "lifecycle",
            ENDPOINTS: "endpoints",
            RESOURCE: "resource"
        }
    }

    isActive(tab) {
        return this.state.activeTab === tab ? "active" : "";
    }

    setActive(event) {
        this.setState({activeTab: event.target.name});
    }

    render() {
        return (
            <div>
                <div className="tabs-holder" style={{background: 'floralwhite'}}>
                    <div className="button-bar">
                        <ul className="nav nav-pills tab-effect">
                            <li className={this.isActive(NavBar.CONS.OVERVIEW)}>
                                <Link name={NavBar.CONS.OVERVIEW} onClick={this.setActive}
                                      to={"/apis/" + this.props.match.params.api_uuid + "/overview"}>
                                    <i className="fw fw-view"/>&nbsp;Overview
                                </Link>
                            </li>
                            <li className={this.isActive(NavBar.CONS.LIFECYCLE)}>
                                <Link name={NavBar.CONS.LIFECYCLE} onClick={this.setActive}
                                      to={"/apis/" + this.props.match.params.api_uuid + "/lifecycle"}>
                                    <i className="fw fw-lifecycle"/>&nbsp;Life-Cycle
                                </Link>
                            </li>
                            <li className={this.isActive(NavBar.CONS.ENDPOINTS)}>
                                <Link name={NavBar.CONS.ENDPOINTS} onClick={this.setActive}
                                      to={"/apis/" + this.props.match.params.api_uuid + "/endpoints"}>
                                    <i className="fw fw-endpoint"/>&nbsp; Endpoints
                                </Link>
                            </li>
                            <li className={this.isActive(NavBar.CONS.RESOURCE)}>
                                <Link onClick={this.setActive}
                                      to={"/apis/" + this.props.match.params.api_uuid + "/resource"}>
                                    <i className="fw fw-resource"/>&nbsp;Resources
                                </Link>
                            </li>
                            <li id="tab-5" role="presentation"><a href="#documents-tab" role="tab"
                                                                  aria-controls="documents-tab" data-toggle="tab"><i
                                className="fw fw-document"/>&nbsp; Documents</a>
                            </li>
                            <li id="tab-6" role="presentation"><a href="#actrl-tab" role="tab" aria-controls="actrl-tab"
                                                                  data-toggle="tab"><i
                                className="fw fw-contract"/>&nbsp; Access Control</a>
                            </li>
                            <li id="tab-7" role="presentation"><a href="#mediation-tab" role="tab"
                                                                  aria-controls="mediation-tab" data-toggle="tab"><i
                                className="fw fw-sequence"/>&nbsp;
                                Mediation</a>
                            </li>
                            <li id="tab-8" role="presentation"><a href="#scripting-tab" role="tab"
                                                                  aria-controls="scripting-tab" data-toggle="tab"><i
                                className="fw fw-prototype"/>&nbsp; Scripting</a>
                            </li>
                            <li id="tab-9" role="presentation"><a href="#subscriptions-tab" role="tab"
                                                                  aria-controls="subscriptions-tab" data-toggle="tab"><i
                                className="fw fw-subscribe"/>&nbsp; Subscriptions</a>
                            </li>
                        </ul>
                    </div>
                </div>

            </div>
        );
    }
}
