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
import {
    Col,
    Row,
    Card,
    Radio,
    InputNumber,
    Switch,
    Button,
    message,
    Form,
    Select,
    Dropdown,
    Tag,
    Menu,
    Badge
} from 'antd';

const FormItem = Form.Item;
import Api from '../../../data/api'
import Loading from '../../Base/Loading/Loading'
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";
import {Input} from 'antd';
import {Checkbox} from 'antd';
import {Table, Icon} from 'antd';

const RadioButton = Radio.Button;
const RadioGroup = Radio.Group;
const Option = Select.Option;


class Permission extends Component {
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        this.api_uuid = this.props.match.params.api_uuid;
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleInputs = this.handleInputs.bind(this);
        this.handleSwitch = this.handleSwitch.bind(this);
        this.handleMaxTPS = this.handleMaxTPS.bind(this);
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

    handleSwitch(secured) {
        this.setState({secured: secured})
    }

    handleInputs(e) {
        this.setState({[e.target.name]: e.target.value});
    }

    handleMaxTPS(maxTPS) {
        this.setState({maxTPS: maxTPS});
    }


    handleSubmit = (e) => {
        debugger;
    }

    render() {
        const {getFieldDecorator} = this.props.form;
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };

        const columns = [{
            title: 'Role',
            dataIndex: 'name',
            key: 'name',
            render: text => <a href="#">{text}</a>,
        }, {
            title: 'Read',
            dataIndex: 'age',
            key: 'age',
        }, {
            title: 'Write',
            dataIndex: 'address',
            key: 'address',
        }, {
            title: 'Update',
            dataIndex: 'update',
            key: 'update',
        }
            , {
                title: 'Action',
                key: 'action',
                render: (text, record) => (
                    <span>
                    <a href="#">Delete</a>
                </span>
                ),
            }];

        const columnsOfScopeTable = [{
            title: 'Scopes',
            dataIndex: 'name',
            key: 'name',
            render: text => <a href="#">{text}</a>,
        }, {
            title: '',
            key: 'delete',
            render: (text, record) => (
                <span>
                    <a href="#">Delete</a>
                </span>
            ),
        }];

        const dataOfScopes = [{
            key: '1',
            name: 'John Brown',

        }, {
            key: '2',
            name: 'Jim Green',

        }];

        const data = [{
            key: '1',
            name: 'Engineering',
            age: 'Yes',
            address: 'New York No. 1 Lake Park',
            update: 'update'
        }, {
            key: '2',
            name: 'Sales',
            age: 42,
            address: 'New York No. 1 Lake Park',
            update: 'update'
        }, {
            key: '3',
            name: 'QA',
            age: 32,
            address: 'New York No. 1 Lake Park',
            update: 'update'
        }];


        const api = this.state.api;
        if (this.state.notFound) {
            return <ResourceNotFound/>
        }
        if (!this.state.api) {
            return <Loading/>
        }


        return (

            <div>
                <Row type="flex" justify="left">
                    <Col span={4}>
                        <Card bodyStyle={{padding: 5}}>
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
                    <Col span={19} offset={1}>
                        <form onSubmit={this.handleSubmit}>
                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="left">
                                    <Col span={8}>Visibility</Col>
                                    <Col span={16}>
                                        <Select>
                                            <Option value="RestrictedByRoles">RestrictedByRoles</Option>
                                            <Option value="Public">Public</Option>
                                        </Select>
                                    </Col>
                                </Row>

                                <Row style={{marginBottom: "10px"}} type="flex" justify="left">
                                    <Col span={8}>Roels</Col>
                                    <Col span={16}>
                                        <Input name="roles" placeholder="Sales-group,Engineering"/>
                                    </Col>
                                </Row>
                            </Card>

                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}>API Permission</Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={7} style={{margin: "10px"}}>
                                                <Input name="role" placeholder="role"/>
                                            </Col>
                                            <Col span={3} style={{margin: "10px"}}>
                                                <Checkbox name="read"> Read </Checkbox>
                                            </Col>
                                            <Col span={3} style={{margin: "10px"}}>
                                                <Checkbox name="update"> Update </Checkbox>
                                            </Col>
                                            <Col span={3} style={{margin: "10px"}}>
                                                <Checkbox name="delete"> Delete </Checkbox>
                                            </Col>
                                        </Row>
                                    </Col>

                                </Row>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}></Col>
                                    <Col span={16}>
                                        <Table columns={columns} dataSource={data}/>
                                    </Col>
                                </Row>
                            </Card>
                            <Card bodyStyle={{padding: 5}}>
                                <Row style={{marginBottom: "10px"}} type="flex" justify="center">
                                    <Col span={8}>API Scopes</Col>
                                    <Col span={16}>
                                        <Row>
                                            <Col span={8} style={{margin: "10px"}}>
                                                <Input name="scopeKey" placeholder="scopeKey"/>
                                            </Col>
                                            <Col span={8} style={{margin: "10px"}}>
                                                <Input name="scopeName" placeholder="scopeName"/>
                                            </Col>
                                        </Row>
                                        <Row>
                                            <Col span={16} style={{margin: "10px"}}>
                                                <Input name="roles" placeholder="roles"/>
                                            </Col>
                                        </Row>
                                        <Row>
                                            <Col span={16} style={{margin: "10px"}}>
                                                <Input name="descriptions" placeholder="descriptions"/>
                                            </Col>
                                        </Row>
                                        <Row>
                                            <Col span={16} style={{margin: "10px"}}>
                                                <Table columns={columnsOfScopeTable} dataSource={dataOfScopes}/>
                                            </Col>
                                        </Row>
                                    </Col>
                                </Row>
                            </Card>

                            <Button loading={this.state.creating} type="primary"
                                    onClick={this.handleSubmit}>Create</Button>
                        </form>
                    </Col>
                </Row>
            </div>


        );
    }
}

const PermissionFormGenerated = Form.create()(Permission);

class PermissionFormWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            api: null,
            notFound: false
        };
        // this.api_uuid = this.props.match.params.api_uuid;
    }


    render = () => {
        const {match} = this.props;
        return <PermissionFormGenerated match={match} history={this.props.history}/>
    }
}

export default PermissionFormWrapper