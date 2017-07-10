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
import {Col, Row, Card, Form, Select, Dropdown, Tag, Menu, Button, Badge} from 'antd';

const FormItem = Form.Item;
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        this.api_uuid = this.props.match.params.api_uuid;
    }

    componentDidMount() {
        const api = new Api();
        let promised_api = api.get(this.api_uuid);
        promised_api.then(
            response => {
                this.setState({api: response.obj});
            }
        ).catch(
            error => {
                if (process.env.NODE_ENV !== "production") {
                    console.log(error);
                }
                let status = error.status;
                if (status === 404) {
                    this.setState({notFound: true});
                }
            }
        );
    }

    render() {
        const menu = (
            <Menu>
                <Menu.Item>
                    <Link to="">Edit</Link>
                </Menu.Item>
                <Menu.Item>
                    <Link to="">Create New Version</Link>
                </Menu.Item>
                <Menu.Item>
                    <Link to="">View Swagger</Link>
                </Menu.Item>
            </Menu>
        );
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.api) {
            return <Loading/>
        }
        return (
            <div>
                <Row type="flex" justify="center">
                    <Col span={4}>

                        <Card bodyStyle={{padding: 10}}>
                            <div className="custom-image">
                                <img alt="API thumb" width="100%" src="/publisher/public/images/api/api-default.png"/>
                            </div>
                            <div className="custom-card">
                                <Badge status="processing" text={api.lifeCycleStatus}/>
                                <p>11 Apps</p>
                                <a href={"/store/apis/" + this.api_uuid} target="_blank" title="Store">View in store</a>
                            </div>
                        </Card>
                    </Col>
                    <Col span={15} offset={1}>
                        <Form layout="vertical">
                            <FormItem {...formItemLayout} label="API Name">
                                <span className="ant-form-text">{api.name}</span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Version">
                                <span className="ant-form-text">{api.version}</span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Context">
                                <span className="ant-form-text">{api.context}</span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Last Updated">
                                <span className="ant-form-text">{api.createdTime}</span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Business Plans">
                                <span className="ant-form-text">{api.policies.map(policy => policy + ", ")}</span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Tags">
                                <span className="ant-form-text">
                                    <Tag><a href="#somelink">Social</a></Tag>
                                    <Tag><a href="#somelink">Facebook</a></Tag>
                                </span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Labels">
                                <span className="ant-form-text">
                                      <Tag color="pink">pink</Tag>
                                      <Tag color="red">red</Tag>
                                      <Tag color="orange">orange</Tag>
                                      <Tag color="green">green</Tag>
                                </span>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Business Owner">
                                <span className="ant-form-text">{"WSO2"}</span>
                                <a className="ant-form-text" href="#email">{"(bizdev@wso2.com)"}</a>
                            </FormItem>
                            <FormItem {...formItemLayout} label="Tech Owner">
                                <span className="ant-form-text">{"WSO2 Support"}</span>
                                <a className="ant-form-text" href="#email">{"(support@wso2.com)"}</a>
                            </FormItem>
                        </Form>
                    </Col>
                </Row>
                <div className="api-add-links">
                    <Dropdown overlay={menu} placement="topCenter">
                        <Button shape="circle" icon="edit"/>
                    </Dropdown>
                </div>
            </div>
        );
    }
}

export default Overview