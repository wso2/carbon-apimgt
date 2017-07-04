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
import {  Menu, Icon } from 'antd'


export default class NavBar extends Component {
    constructor(props) {
        super(props);
        let location = this.props.leftMenu.location;
        let path_sections,active_tab = "overview",details_action;
        if(this.props.leftMenu.location){
            path_sections = location.pathname.split('/');
            details_action = path_sections[path_sections.indexOf("apis") + 2];
            active_tab = (Object.values(NavBar.CONST).includes(details_action)) ? details_action : NavBar.CONST.OVERVIEW;
        }
        this.state = {
            activeTab: active_tab
        };
        this.setActive = this.setActive.bind(this);

    }



    static get CONST() {
        return {
            OVERVIEW: "overview",
            LIFECYCLE: "lifecycle",
            ENDPOINTS: "endpoints",
            RESOURCES: "resources",
            PERMISSION: "permission",
            DOCUMENTS:  "documents",
            MEDIATION:  "mediation",
            SCRIPTING: "scripting",
            SUBSCRIPTIONS: "subscriptions"
        }
    }

    setActive(event) {
        this.setState({activeTab: event.target.name});
    }

    render() {
        let apiPath = '';
        if(this.props.leftMenu.match){
            apiPath = this.props.leftMenu.match.params.api_uuid;
        }
        console.info(this.props);
        return (
                <Menu theme="light" mode={this.state.mode} defaultSelectedKeys={['1']}>

                    <Menu.Item key="1">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.OVERVIEW} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/overview"}>Overview</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="2">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.LIFECYCLE} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/lifecycle"}>Life-Cycle</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="3">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.ENDPOINTS} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/endpoints"}>Endpoints</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="4">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.RESOURCES} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/resources"}>Resources</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="5">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.DOCUMENTS} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/documents"}>Documents</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="6">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.MEDIATION} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/mediation"}>Mediation</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="7">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.SCRIPTING} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/scripting"}>Scripting</Link>
                                </span>
                              </span>
                    </Menu.Item>
                    <Menu.Item key="8">
                              <span>
                                <Icon type="file" />
                                <span className="nav-text">
                                    <Link name={NavBar.CONST.SUBSCRIPTIONS} onClick={this.setActive}
                                          to={"/apis/" + apiPath + "/subscriptions"}>Subscriptions</Link>
                                </span>
                              </span>
                    </Menu.Item>


                </Menu>



        );
    }
}
