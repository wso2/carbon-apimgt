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
'use strict';

import React, {Component} from 'react'
import {Link} from 'react-router-dom'
import {Table, Popconfirm, Button, Dropdown, Menu, message} from 'antd';

import API from '../../../data/api'

export default class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
            selectedRowKeys: [],
            endpointBeingAdded: false,
        };
        this.onSelectChange = this.onSelectChange.bind(this);
        this.handleEndpointsAddtoDB = this.handleEndpointsAddtoDB.bind(this);
    }

    handleEndpointAddtoDB(endpointUuid, name, endpointType, endpointConfig, endpointSecurity, maxTps) {
        //Todo check if exists in DB
        const hideMessage = message.loading("Adding the Endpoint to the Database ...", 0);
        let endpointDefinition = {
            endpointConfig: endpointConfig,
            endpointSecurity: endpointSecurity,
            type: endpointType,
            name: name,
            maxTps: maxTPS
        };
        const api = new API();
        const promised_addEndpoint = api.addEndpoint(endpointDefinition);
        return promised_addEndpoint.then(
            response => {
                const {name, id} = response.obj;
                message.success("New endpoint " + name + " created successfully");
                let redirect_url = "/endpoints/" + id + "/";
                this.props.history.push(redirect_url);
            }
        ).catch(
            error => {
                console.error(error);
                message.error("Error occurred while creating the endpoint!");
                this.setState({loading: false});
            }
        )
    }

    componentDidMount() {
        const api = new API();
        const promised_discoveredEndpoints = api.discoverEndpoints();
        /* TODO: Handle catch case , auth errors and ect ~tmkb ------copied from EndpointListing class*/
        promised_discoveredEndpoints.then(
            response => {
                this.setState({endpoints: response.obj.list});
            }
        );
    }

    onSelectChange(selectedRowKeys) {
        this.setState({selectedRowKeys: selectedRowKeys});
    }

    render() {
        const {selectedRowKeys, endpoints} = this.state;
        const columns = [{
            title: 'Name',
            dataIndex: 'name',
            key: 'age',
            sorter: (a, b) => a.name.length - b.name.length,
            render: (text, record) => <Link to={"/endpoints/" + record.id}>{text}</Link>
        }, {
            title: 'Type',
            dataIndex: 'type'
        }, {
            title: 'Service URL',
            dataIndex: 'endpointConfig',
            render: (text, record, index) => JSON.parse(text).url
        }, {
            title: 'Service Type',
            dataIndex: 'endpointConfig',
            render: (text, record, index) => JSON.parse(text).urlType
        }, {
            title: 'Max TPS',
            dataIndex: 'maxTps',
            sorter: (a, b) => a.maxTps - b.maxTps,
        }, {
            title: 'Action',
            key: 'action',
            render: (text, record) => {
                console.log("record");
                console.log(record);
                console.log("text");
                console.log(text);
                return (
                    <Button type="primary" loading={this.state.endpointBeingAdded}
                        onClick={this.handleEndpointAddtoDB(text.id, text.name, text.type,
                        text.endpointConfig, text.endpointSecurity, text.maxTps)}>
                              Add Endpoint to DB
                    </Button>)
            }
        }];
        const rowSelection = {
            selectedRowKeys,
            onChange: this.onSelectChange,
        };

        const endpointCreatMenu = (
            <Menu>
                <Menu.Item key="0">
                    <a target="_blank" rel="noopener noreferrer" href="/endpoints">List</a>
                </Menu.Item>
                <Menu.Item key="1">
                    <a target="_blank" rel="noopener noreferrer" href="/endpoints/create">Create Custom</a>
                </Menu.Item>
            </Menu>
        );
        return (
            <div>
                <div className="api-add-links">
                    <a className="ant-dropdown-link" href="#">
                          Global Endpoints <Icon type="down" />
                    </a>
                </div>
                <h3>Global Endpoints</h3>
                <Table rowSelection={rowSelection} loading={endpoints === null} columns={columns}
                       dataSource={endpoints}
                       rowKey="id"
                       size="middle"/>
            </div>
        );
    }
}