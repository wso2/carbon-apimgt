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
import Footer from './Footer/Footer'
// import Header as HeaderMy from './Header/Header'

import LeftNav from './Navigation/LeftNav'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.min.css';
import {Link} from 'react-router-dom'

import { Layout, Menu, Breadcrumb, Icon } from 'antd';
const { SubMenu } = Menu;
const { Header, Content, Sider } = Layout;


class Base extends Component {
    constructor(props){
        super(props);
        this.state = {
            collapsed: false,
            mode: 'inline'
        }
    }
    onCollapse = (collapsed) => {
        console.log(collapsed);
        this.setState({
            collapsed,
            mode: collapsed ? 'vertical' : 'inline',
        });
    }
    render() {
        return (

            <Layout style={{height:"100vh"}} >
                <Sider
                    collapsible
                    collapsed={this.state.collapsed}
                    onCollapse={this.onCollapse}
                >
                    <div className="logo" />
                    <Menu theme="dark" mode={this.state.mode} defaultSelectedKeys={['6']}>
                        <div className="brand-wrapper" style={{float:"left", width:"200px"}}> <img
                            className="brand"
                            src="/publisher/public/images/logo.svg"
                            alt="wso2-logo"/> <span>API Publisher</span>
                        </div>
                        <SubMenu
                            key="sub1"
                            title={<span><Icon type="user" /><span className="nav-text">User</span></span>}
                        >
                            <Menu.Item key="1">Tom</Menu.Item>
                            <Menu.Item key="2">Bill</Menu.Item>
                            <Menu.Item key="3">Alex</Menu.Item>
                        </SubMenu>
                        <SubMenu
                            key="sub2"
                            title={<span><Icon type="team" /><span className="nav-text">Team</span></span>}
                        >
                            <Menu.Item key="4">Team 1</Menu.Item>
                            <Menu.Item key="5">Team 2</Menu.Item>
                        </SubMenu>
                        <Menu.Item key="6">
              <span>
                <Icon type="file" />
                <span className="nav-text">File</span>
              </span>
                        </Menu.Item>
                    </Menu>
                </Sider>
                <Layout>
                    <Header style={{ background: '#fff', padding: 0 }} >
                        <Menu
                            theme="light"
                            mode="horizontal"
                            defaultSelectedKeys={['2']}
                            style={{ lineHeight: '64px' }}
                        >

                            <Menu.Item key="1"><Link to="/apis">Apis</Link></Menu.Item>
                            <Menu.Item key="2">Statistics</Menu.Item>
                            <Menu.Item key="3">Subscriptions</Menu.Item>
                        </Menu>
                    </Header>
                    <Content style={{ margin: '0 16px' }}>
                        <Breadcrumb>
                            <Breadcrumb.Item href="">
                                <Icon type="home" />
                            </Breadcrumb.Item>
                            <Breadcrumb.Item href="">
                                <Icon type="user" />
                                <span>Application List</span>
                            </Breadcrumb.Item>
                            <Breadcrumb.Item>
                                Application
                            </Breadcrumb.Item>
                        </Breadcrumb>
                        <div style={{ padding: 24, minHeight: 360 , background: "#fff"}}>
                            {this.props.children}
                        </div>
                    </Content>
                    <Footer style={{ textAlign: 'center' }}>
                        <p>WSO2 APIM Publisher v3.0.0 | © 2017 <a href="http://wso2.com/" target="_blank"><i
                            className="icon fw fw-wso2"/> Inc</a>.</p>
                    </Footer>
                </Layout>
            </Layout>

        );
    }
}

export default Base;