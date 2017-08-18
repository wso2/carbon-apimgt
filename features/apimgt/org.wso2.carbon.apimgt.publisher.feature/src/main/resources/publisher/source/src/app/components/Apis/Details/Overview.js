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
import {Col, Popconfirm, Row, Card, Form, Select, Dropdown, Tag, Menu, Button, Badge, message, Modal} from 'antd';

const FormItem = Form.Item;
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import Api from '../../../data/api'
import {Redirect} from 'react-router-dom'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation'

class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.downloadWSDL = this.downloadWSDL.bind(this);
        this.handleApiDelete = this.handleApiDelete.bind(this);
        this.apiDeletePermissionWarning = this.apiDeletePermissionWarning.bind(this);
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

    apiDeletePermissionWarning() {
      Modal.warning({
        title: 'No Permissions',
        content: "You don't have permissions to delete this API",
      });
    }

    handleApiDelete(e) {
        this.setState({loading: true});
        const api = new Api();
        let promised_delete = api.deleteAPI(this.api_uuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    message.error("Something went wrong while deleting the API!");
                    this.setState({loading: false});
                    return;
                }
                let redirect_url = "/apis/";
                this.props.history.push(redirect_url);
                message.success("API " + this.state.api.name + " was deleted successfully!");
                this.setState({active: false, loading: false});
            }
        );
    }

    downloadWSDL() {
        const api = new Api();
        let promised_wsdl = api.getWSDL(this.api_uuid);
        promised_wsdl.then(
            response => {
                let windowUrl = window.URL || window.webkitURL;
                let binary = new Blob([response.data]);
                let url = windowUrl.createObjectURL(binary);
                let anchor = document.createElement('a');
                anchor.href = url;
                if (response.headers['content-disposition']) {
                    anchor.download = Overview.getWSDLFileName(response.headers['content-disposition']);
                } else {
                    //assumes a single WSDL in text format
                    anchor.download = this.state.api.provider +
                        "-" + this.state.api.name + "-" + this.state.api.version + ".wsdl"
                }
                anchor.click();
                windowUrl.revokeObjectURL(url);
            }
        )
    }

    static getWSDLFileName(content_disposition_header) {
        let filename = "default.wsdl";
        if (content_disposition_header && content_disposition_header.indexOf('attachment') !== -1) {
            let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            let matches = filenameRegex.exec(content_disposition_header);
            if (matches !== null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }
        return filename;
    }

    render() {
        let redirect_url = "/apis/" + this.props.match.params.api_uuid + "/overview";

        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.api) {
            return <Loading/>
        }

        var partial;
        if (this.state.api.userPermissionsForApi.includes("DELETE")) {
            partial = <Popconfirm title="Do you want to delete this api?" onConfirm={this.handleApiDelete}>
                          <Link to="">Delete</Link>
                      </Popconfirm>
        } else {
            partial = <Link to={"/apis/" + this.api_uuid + "/overview"} onClick={this.apiDeletePermissionWarning}>Delete</Link>
        }

        const menu = (
            <Menu>
                <Menu.Item>
                    <ScopeValidation resourceMethod={resourceMethod.PUT} resourcePath={resourcePath.SINGLE_API}>
                        <Link to="">Edit</Link>
                    </ScopeValidation>
                </Menu.Item>
                <Menu.Item>
                    <ScopeValidation resourceMethod={resourceMethod.DELETE} resourcePath={resourcePath.SINGLE_API}>
                        {partial}
                    </ScopeValidation>
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
                            {
                                api.wsdlUri && (
                                    <FormItem {...formItemLayout} label="WSDL">
                                        <span className="ant-form-text">
                                            <a onClick={this.downloadWSDL}>Download</a>
                                        </span>
                                    </FormItem>
                                )
                            }
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