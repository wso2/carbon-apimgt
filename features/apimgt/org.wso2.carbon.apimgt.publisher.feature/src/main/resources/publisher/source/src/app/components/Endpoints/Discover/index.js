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
import {Table, Icon, Button, Dropdown, Menu, message} from 'antd';

import API from '../../../data/api'

export default class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
            selectedRowKeys: [],
            endpointBeingAdded: false,
        };
        //this.onSelectChange = this.onSelectChange.bind(this);
    }

    handleAddEndpointToDB = (endpointUuid, serviceName, endpointType, config, security,
        maximumTps) => {
        //Todo check if exists in DB
        let endpointDefinition = {
            name: serviceName + "-" + endpointType + "-" + JSON.parse(config).serviceType,
            type: endpointType,
            endpointConfig: config,
            endpointSecurity: security,
            maxTps: maximumTps
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint.then(
            response => {
                const {name, id} = response.obj;
                message.success("New endpoint " + name + " created successfully");
            }
        ).catch(
            error => {
                console.error(error);
                message.error("Error occurred while creating the endpoint!");
                this.setState({endpointBeingAdded: false});
            }
        )
    }

    componentDidMount() {
        const api = new API();
        const promised_discoveredEndpoints = api.discoverEndpoints();
        // TODO: Handle catch case , auth errors and ect ~tmkb ------copied from EndpointListing class
        promised_discoveredEndpoints.then(
            response => {
                this.setState({endpoints: response.obj.list});
            }
        );
    }

    onSelectChange(selectedRowKeys) {
        this.setState({selectedRowKeys: selectedRowKeys});
    }

    checkIfEndpointAlreadyAdded(record, api) {
        let alreadyAdded = false;
        debugger;
        let endpointName = record.name+"-"+record.type+"-"+JSON.parse(record.endpointConfig).serviceType;
        let promised_endpointAlreadyInDB = api.checkIfEndpointExists(endpointName);;
        promised_endpointAlreadyInDB.then(
            response => {
                console.log("done");
                alreadyAdded = true;
            }
        ).catch(
            error => {
                if(error.response.status==404){
                    console.log(endpointName+" not in database")
                }

            }
        )
        return alreadyAdded;
    }

    render() {
        const {selectedRowKeys, endpoints} = this.state;
        const api = new API();
        const columns = [{
            title: 'Name',
            dataIndex: 'name',
            key: 'age',
            width: '30%',
            sorter: (a, b) => a.name.length - b.name.length,
            render: (text, record) => <Link to={"/endpoints/" + record.id}>{text}</Link>
        }, {
            title: 'Type',
            dataIndex: 'type',
            width: '10%'
        }, {
            title: 'Service URL',
            dataIndex: 'endpointConfig',
            width: '30%',
            render: (text, record) => (
            <span>
                {JSON.parse(text).serviceUrl}
                <span className="ant-divider" />
                {JSON.parse(text).serviceType}
            </span>
            )
        }, {
            title: 'Max TPS',
            dataIndex: 'maxTps',
            width: '10%',
            sorter: (a, b) => a.maxTps - b.maxTps,
        }, {
            title: 'Action',
            key: 'action',
            dataIndex: 'id',
            width: '20%',
            render: (text, record) => {
                let alreadyInDB = this.checkIfEndpointAlreadyAdded(record,api);
                console.log(alreadyInDB);
                if(alreadyInDB){
                    return <Button> Update Database </Button>;
                }else{
                    return <span>
                      <Button type="primary" onClick={() =>this.handleAddEndpointToDB(record.id,
                      record.name, record.type, record.endpointConfig, record.endpointSecurity,
                      record.maxTps)}>
                        Add to Database</Button>
                    </span>;
                }
            }
        }];
        const rowSelection = {
            selectedRowKeys,
            onChange: this.onSelectChange,
        };
        const endpointListAndCreatMenu = (
            <Menu>
                <Menu.Item key="0">
                    <Link to="/endpoints">List Endpoints</Link>
                </Menu.Item>
                <Menu.Item key="1">
                    <Link to="/endpoints/create">Create New Endpoint</Link>
                </Menu.Item>
            </Menu>
        );
        return (
            <div>
                <Dropdown overlay={endpointListAndCreatMenu}>
                    <Button icon="left" />
                </Dropdown>
                <h3>Discovered Service Endpoints</h3>
                <Table rowSelection={rowSelection} loading={endpoints === null} columns={columns}
                       dataSource={endpoints}
                       rowKey="id"
                       size="middle"/>
            </div>
        );
    }
}