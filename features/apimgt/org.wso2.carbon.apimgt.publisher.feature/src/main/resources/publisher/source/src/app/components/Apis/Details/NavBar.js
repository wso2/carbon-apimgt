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
import {Tabs} from 'antd'
const TabPane = Tabs.TabPane;

export default class NavBar extends Component {
    constructor(props) {
        super(props);
        let location = this.props.leftMenu.location;
        let path_sections, active_tab = "overview", details_action;
        if (this.props.leftMenu.location) {
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
            DOCUMENTS: "documents",
            MEDIATION: "mediation",
            SCRIPTING: "scripting",
            SUBSCRIPTIONS: "subscriptions"
        }
    }

    setActive(event) {
        this.setState({activeTab: event.target.name});
    }

    render() {
        let api_uuid = '';
        if (this.props.leftMenu.match) {
            api_uuid = this.props.leftMenu.match.params.api_uuid;
        }
        return (
            <Tabs defaultActiveKey={NavBar.CONST.OVERVIEW} tabPosition={'left'} style={{height: '100vh'}}>
                {Object.entries(NavBar.CONST).map(
                    ([key, val]) => {
                        return (
                                <TabPane tab={
                                    <Link name={val} onClick={this.setActive} to={"/apis/" + api_uuid + "/" + val}>
                                        {val}
                                    </Link>
                                } key={val}/>
                            );
                    }
                )}
            </Tabs>
        )
    }
}
