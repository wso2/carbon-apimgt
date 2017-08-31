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
import {Link, withRouter} from "react-router-dom";
import AuthManager from '../../../data/AuthManager.js';
import ConfigManager from '../../../data/ConfigManager.js';
import qs from 'qs'
import {Layout, Menu, Icon, Dropdown, Button} from 'antd';
import { Select } from 'antd';
const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;
const {Header} = Layout;


const ComposeHeader = (props) => {
    let params = qs.stringify({referrer: props.location.pathname});
    let envdetails = new ConfigManager();
    let variableData = envdetails.env_response

    return (
        <Header className='custom-header'>
            <div className="logo">
                <Link to="/apis">
                    <img className="brand" src="/publisher/public/images/logo.svg" alt="wso2-logo"/>
                    <span>API Publisher</span>
                </Link>
            </div>

            <Menu
                mode="horizontal"
                defaultSelectedKeys={['2']}
                className='custom-menu'
                theme="light"
            >
                <Dropdown style={{float:"right"}} overlay={<Menu>

                    {JSON.parse(localStorage.getItem("environmentSC")).map(environment => <Menu.Item
                        key={environment.env}>{environment.env}</Menu.Item>)}
                </Menu>}>
                    <a className="ant-dropdown-link" href="#">
                        {localStorage.getItem("correctvalue")} <Icon type="down" />
                    </a>
                </Dropdown>
                <SubMenu
                    title={<span><Icon type="down"/>{ AuthManager.getUser() ? AuthManager.getUser().name : ""}</span>}>
                    <Menu.Item key="setting:2"><Icon type="user"/>Profile</Menu.Item>
                    <Menu.Item key="setting:1">
                        <Link to={{pathname: '/logout', search: params}}><Icon type="logout"/>Logout</Link>
                    </Menu.Item>
                </SubMenu>
                <SubMenu title={<Icon type="appstore-o" style={{fontSize: 20}}/>}>
                    <Menu.Item key="endpoints">
                        <Link to={{pathname: '/endpoints'}}>
                            <Icon type="rocket" style={{fontSize: 20}}/> Endpoints
                        </Link>
                    </Menu.Item>
                    <Menu.Item key="settings">
                        <Link to={{pathname: '/apis'}}>
                            <Icon type="fork" style={{fontSize: 20}}/> Apis
                        </Link>
                    </Menu.Item>
                </SubMenu>

 </SubMenu>

                    {localStorage.getItem("currentEnv") != 'default' &&
                    <SubMenu title={<span><Icon type="setting"/>{localStorage
                        .getItem("currentEnv")}</span>}>
                        {this.state.availableEnv.map(environment => <Menu.Item onSelect={this.handleClick}
                            key={environment.env}>{environment.env}</Menu.Item>)}

                    </SubMenu>

            </Menu>
        </Header>

    );
};

// Using `withRouter` helper from React-Router-Dom to get the current user location to be used with logout action,
// We pass the current path in referrer parameter to redirect back the user to where he/she was after login.
// DOC: https://github.com/ReactTraining/react-router/blob/master/packages/react-router/docs/api/withRouter.md
export default withRouter(ComposeHeader)