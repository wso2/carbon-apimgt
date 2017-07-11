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

export default class EndpointsListing extends Component {
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
            selectedRowKeys: []
        };
        this.onSelectChange = this.onSelectChange.bind(this);
        this.handleEndpointDelete = this.handleEndpointDelete.bind(this);
    }

    handleEndpointDelete(endpointUuid, name) {
        const hideMessage = message.loading("Deleting the Endpoint ...", 0);
        const api = new API();
        let promised_delete = api.deleteEndpoint(endpointUuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    message.error("Something went wrong while deleting the " + name + " Endpoint!");
                    hideMessage();
                    return;
                }
                message.success(name + " Endpoint deleted successfully!");
                let endpoints = this.state.endpoints;
                for (let endpointIndex in endpoints) {
                    if (endpoints.hasOwnProperty(endpointIndex) && endpoints[endpointIndex].id === endpointUuid) {
                        endpoints.splice(endpointIndex, 1);
                        break;
                    }
                }
                this.setState({active: false, endpoints: endpoints});
                hideMessage();
            }
        );
    }

    componentDidMount() {
        const api = new API();
        const promised_endpoints = api.getEndpoints();
        /* TODO: Handle catch case , auth errors and ect ~tmkb*/
        promised_endpoints.then(
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
            // sortOrder: sortedInfo.columnKey === 'age' && sortedInfo.order,
        }, {
            title: 'Type',
            dataIndex: 'type'
        }, {
            title: 'Service URL',
            dataIndex: 'endpointConfig',
            render: (text, record, index) => JSON.parse(text).serviceUrl
        }, {
            title: 'Max TPS',
            dataIndex: 'maxTps',
            sorter: (a, b) => a.maxTps - b.maxTps,
        }, {
            title: 'Action',
            key: 'action',
            render: (text, record) => {
                return (
                    <Popconfirm title="Confirm delete?" onConfirm={() => this.handleEndpointDelete(text.id, text.name)}>
                        <Button type="danger" icon="delete">Delete</Button>
                    </Popconfirm>)
            }
        }];
        const rowSelection = {
            selectedRowKeys,
            onChange: this.onSelectChange,
        };

        const endpointCreatMenu = (
            <Menu>
                <Menu.Item>
                    <Link to="/endpoints/create">Create new Endpoint</Link>
                </Menu.Item>
            </Menu>
        );
        return (
            <div>
                <div className="api-add-links">
                    <Dropdown overlay={endpointCreatMenu} placement="topRight">
                        <Button shape="circle" icon="plus"/>
                    </Dropdown>
                </div>
                <h4>Global Endpoints</h4>
                <Table rowSelection={rowSelection} loading={endpoints === null} columns={columns}
                       dataSource={endpoints}
                       rowKey="id"
                       size="middle"/>
            </div>
        );
    }
}