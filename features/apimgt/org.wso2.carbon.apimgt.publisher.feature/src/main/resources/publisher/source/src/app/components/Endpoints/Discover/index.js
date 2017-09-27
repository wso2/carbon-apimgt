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
import {Table, Icon, Dropdown, Button, Menu, message, Radio} from 'antd';
const RadioGroup = Radio.Group;
const RadioButton = Radio.Button;
import Typography from 'material-ui/Typography';

import API from '../../../data/api'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';


export default class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
            viewableEndpoints: null,
            filterType: "",
            endpointBeingAdded: false,
        };
        this.handleFilterTextInputChange = this.handleFilterTextInputChange.bind(this);
        this.handleRadioButtonChange = this.handleRadioButtonChange.bind(this);
    }

    handleRadioButtonChange(e) {
        this.setState({
            filterType: e.target.value
        })
    }

    handleFilterTextInputChange(e) {
        this.setState({
            viewableEndpoints: this.filterEndpoints(e.target.value)
        })
    }

    filterEndpoints(filterText){
        const { endpoints } = this.state;
        switch(this.state.filterType) {
            case "namespace":
                console.log("namespace");
                return endpoints.filter(el => JSON.parse(el.endpointConfig).namespace.startsWith(filterText))
            case "criteria":
            case "name":
                return endpoints.filter(el => el.name.startsWith(filterText))
            default :

                return endpoints.filter(el => el.name.startsWith(filterText))
        }
    }

    componentDidMount() {
        const api = new API();
        const promised_discoveredEndpoints = api.discoverServices();
        // TODO: Handle catch case , auth errors and ect ~tmkb ------copied from EndpointListing class
        promised_discoveredEndpoints.then(
            response => {
                const list = response.obj.list;
                this.setState({
                    endpoints: list,
                    viewableEndpoints: list
                });
            }
        );
    }

    render() {
        const {viewableEndpoints} = this.state;
        const api = new API();
        const columns = [{
            title: 'Name | Namespace | Criteria',
            dataIndex: 'name',
            key: 'age',
            width: '20%',
            sorter: (a, b) => a.name.length - b.name.length,
            render: (text, record) => (
            <span>
                <Link to={"/endpoints/" + record.id}>{text}</Link>
                <span className="ant-divider" />
                {JSON.parse(record.endpointConfig).namespace}
            </span>
            )
        }, {
            title: 'Type',
            dataIndex: 'type'
        }, {
            title: 'Service URL',
            dataIndex: 'endpointConfig',
            render: (text, record) => (
                <span>
                    {JSON.parse(text).serviceUrl}
                    <span className="ant-divider" />
                    {JSON.parse(text).urlType}
                </span>
            )
        }, {
            title: 'Max TPS',
            dataIndex: 'maxTps',
            sorter: (a, b) => a.maxTps - b.maxTps,
        }, {
            title: 'Action',
            key: 'action',
            dataIndex: 'id',
            render: (text, record) =><ButtonCell record={record} api={api}/>
        }];
        const serviceEndpointsListAndCreateMenu = (
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
            <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY} resourceMethod={resourceMethod.GET}>
                <div>
                    <Dropdown overlay={serviceEndpointsListAndCreateMenu}>
                        <Button icon="left" />
                    </Dropdown>
                    <Typography className="page-title" type="display1">
                        Discover Service Endpoints
                    </Typography>
                    <div style={{ margin:16 }}>
                        <input
                          type="text"
                          placeholder="Search..."
                          onChange={this.handleFilterTextInputChange}
                        />
                        <div style={{ marginLeft: 16, display: 'inline-block' }}>
                            <span style={{ marginRight: 16 }}> Filter by</span>
                            <RadioGroup onChange={this.handleRadioButtonChange} defaultValue="name">
                              <RadioButton value="namespace">Namespace</RadioButton>
                              <RadioButton value="criteria">Prior Defined Key:Value Pairs</RadioButton>
                              <RadioButton value="name">Service Name</RadioButton>
                            </RadioGroup>
                        </div>
                    </div>
                    <Table loading={viewableEndpoints === null }
                        columns={columns}
                        dataSource={viewableEndpoints}
                        rowKey="id"
                        size="middle"/>
                </div>
            </ScopeValidation>
        );
    }
}



class ButtonCell extends Component {
    constructor(props) {
        super(props);
        this.state = {
            record: this.props.record,
            api: this.props.api,
            actionButton: <Button type="primary" loading>Loading...</Button>
        };
    }
    getUpdateButton(){
        return (
            <Button> Update Database </Button>
        );
    }
    getAddButton(){
        let record = this.state.record;
        return (
            <Button type="primary" onClick={() =>this.handleAddEndpointToDB(record.id,
            record.name, record.type, record.endpointConfig, record.endpointSecurity,
            record.maxTps)}>
            Add to Database</Button>
        );
    }
    handleAddEndpointToDB = (endpointUuid, serviceName, endpointType, config, security,
        maximumTps) => {
        let endpointDefinition = {
            name: JSON.parse(config).namespace + "-" + serviceName + "-"
                    + endpointType + "-" + JSON.parse(config).urlType,
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
                this.setState({
                    actionButton: this.getUpdateButton()
                });
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
        let record = this.state.record;
        let api = this.state.api;
        let endpointConfig = JSON.parse(record.endpointConfig);
        let endpointName = endpointConfig.namespace + "-" + record.name + "-"
                            + record.type + "-" + endpointConfig.urlType;
        let promised_endpointAlreadyInDB = api.checkIfEndpointExists(endpointName);
        promised_endpointAlreadyInDB.then(
            response => {
                this.setState({
                    actionButton: this.getUpdateButton()
                });
            }
        ).catch(
            error => {
                if(error.response.status==404){
                     this.setState({
                         actionButton: this.getAddButton()
                     });
                }
            }
        )
     }
    render() {
        return (this.state.actionButton);
    }
}

