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

import  NavBar  from './Navigation/NavBar';
import { Layout, Breadcrumb, Icon, Menu } from 'antd';
import ComposeHeader from './Header/ComposeHeader'
const { Content, Sider } = Layout;



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
                {this.props.leftMenu ?
                    <Sider
                        collapsible
                        collapsed={this.state.collapsed}
                        onCollapse={this.onCollapse}
                    >
                        <NavBar leftMenu={this.props.leftMenu} />
                    </Sider>

                    : <div />}
                <Layout>
                    <ComposeHeader />
                    <Content>
                        <Breadcrumb style={{ margin: '10px 16px' }}>
                            <Breadcrumb.Item href="">
                                <Icon type="home" />
                            </Breadcrumb.Item>
                            <Breadcrumb.Item href="">
                                <Icon type="user" />
                                <span>Apis</span>
                            </Breadcrumb.Item>
                        </Breadcrumb>
                        <div style={{ padding: 24, minHeight: 360 , background: "#fff"}}>
                            {this.props.children}
                        </div>
                    </Content>

                </Layout>
            </Layout>

        );
    }
}

export default Base;