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

import './discover.css'
import API from '../../../data/api'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';


export default class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            discoveredEndpoints: null,
            viewableEndpoints: null,
            storedEndpoints: null,
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
        const { discoveredEndpoints } = this.state;
        switch(this.state.filterType) {
            case "namespace":
                return discoveredEndpoints.filter(el => JSON.parse(el.endpointConfig).namespace.startsWith(filterText))
            case "criteria":
                return discoveredEndpoints.filter(el => JSON.parse(el.endpointConfig).criteria.includes(filterText))
            case "name":
                return discoveredEndpoints.filter(el => el.name.startsWith(filterText))
            default :
                return discoveredEndpoints.filter(el => el.name.startsWith(filterText))
        }
    }

    discoverServices() {
        const api = new API();
        const promised_discoveredEndpoints = api.discoverServices();
        promised_discoveredEndpoints.then(
            response => {
                const list = response.obj.list;
                this.setState({
                    discoveredEndpoints: list,
                    viewableEndpoints: list,
                });
            }
        )
    }

    getStoredEndpoints() {
        const api = new API();
        const promised_storedEndpoints = api.getEndpoints();
        promised_storedEndpoints.then(
            response => {
                this.setState({
                    storedEndpoints: response.obj.list,
                    databaseChecked: true,
                });
            }
        ).catch(
            error => {
                console.error("Error while retrieving stored endpoints");
                message.error("Error while retrieving stored endpoints");
            }
        );
    }

    componentDidMount() {
        this.discoverServices();
        this.getStoredEndpoints();
    }

    render() {
        const {viewableEndpoints} = this.state;
        const columns = [{
            title: 'Name  |  Namespace  |  Criteria',
            dataIndex: 'name',
            key: 'age',
            width: '40%',
            sorter: (a, b) => a.name.length - b.name.length,
            className: 'ed-table-first-column',
            render: (text, record) => (
            <span>
                {text}
                <span className="ant-divider" />
                {JSON.parse(record.endpointConfig).namespace}
                <span className="ant-divider" />
                {JSON.parse(record.endpointConfig).criteria}
            </span>
            )
        }, {
            title: 'Type',
            dataIndex: 'type',
            className: 'ed-font'
        }, {
            title: 'Service URL',
            dataIndex: 'endpointConfig',
            className: 'ed-font',
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
            className: 'ed-font',
            sorter: (a, b) => a.maxTps - b.maxTps,
        }, {
            title: 'Action',
            key: 'action',
            dataIndex: 'id',
            className: 'ed-font',
            render: (text, record) =><ButtonCell record={record} storedEndpoints={this.state.storedEndpoints}/>
        }];
        const serviceEndpointsListAndCreateMenu = (
            <Menu>
                <Menu.Item key="0">
                    <Link to="/endpoints">View stored endpoints</Link>
                </Menu.Item>
                <Menu.Item key="1">
                    <Link to="/endpoints/create">Create custom endpoint</Link>
                </Menu.Item>
            </Menu>
        );
        return (
            <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY} resourceMethod={resourceMethod.GET}>
                <div className="ed-body">
                    <div className="ed-top-section">
                        <Typography className="page-title ed-title" type="title">
                             Discover Service Endpoints
                        </Typography>
                        <div className="ed-global-endpoints-button-div">
                            <Dropdown overlay={serviceEndpointsListAndCreateMenu}>
                                <Button size='large' className="ed-global-endpoints-button" icon="down">Global Endpoints</Button>
                            </Dropdown>
                        </div>
                        <div className="ed-filter-area">
                            <input
                              type="text"
                              className="ed-filter-input"
                              placeholder="Type here to filter.."
                              onChange={this.handleFilterTextInputChange}
                            />
                            <div className="ed-filter-area-radio-buttons-div">
                                <span className="ed-filter-area-inline-text"> Filter by</span>
                                <RadioGroup size='large' onChange={this.handleRadioButtonChange} defaultValue="name">
                                  <RadioButton className="ed-font" value="namespace">Namespace</RadioButton>
                                  <RadioButton className="ed-font" value="criteria">Criteria</RadioButton>
                                  <RadioButton className="ed-font" value="name">Service Name</RadioButton>
                                </RadioGroup>
                            </div>
                        </div>
                    </div>
                    <Table loading={viewableEndpoints === null || this.state.storedEndpoints === null}
                        columns={columns}
                        dataSource={viewableEndpoints}
                        rowKey="id"
                        size="large"/>
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
            storedEndpoints: this.props.storedEndpoints,
            actionButton: <Button type="primary" loading>Loading...</Button>
        };
    }

    getAddButton(){
        return (
            <Button type="primary" onClick={() =>this.handleAddEndpointToDB()}>
            Add to Database </Button>
        );
    }

    getUpdateButton(){
        return (
            <Button type="secondary" onClick={() =>this.handleUpdateEndpoint()}>
            Update Database </Button>
        );
    }

    handleAddEndpointToDB = () => {
        let record = this.state.record;
        let configObject = JSON.parse(record.endpointConfig);
        let endpointDefinition = {
            name: configObject.namespace + "-" + record.name + "-"
                    + record.type + "-" + configObject.urlType,
            type: record.type,
            endpointConfig: record.endpointConfig,
            endpointSecurity: record.endpointSecurity,
            maxTps: record.maxTps
        };
        const api = new API();
        const promisedEndpoint = api.addEndpoint(endpointDefinition);
        return promisedEndpoint.then(
            response => {
                const {name, id} = response.obj;
                this.state.storedEndpoints.push(response.obj);
                message.success("New endpoint " + name + " created successfully");
                this.setState({
                    actionButton: this.getUpdateButton()
                });
            }
        ).catch(
            error => {
                console.error(error);
                message.error("Error occurred while creating the endpoint!");
                this.setState({
                    endpointBeingAdded: false
                });
            }
        )
    }

    handleUpdateEndpoint = () => {
        const hideMessage = message.loading("Updating the Endpoint ...", 0);
        let record = this.state.record;
        let configObject = JSON.parse(record.endpointConfig);
        let endpointName = configObject.namespace + "-" + record.name + "-"
                            + record.type + "-" + configObject.urlType;
        let storedEndpoint = this.state.storedEndpoints.find(el => el.name === endpointName);
        if (storedEndpoint === null) {
            message.error("Error while updating. Could not find the " + endpointName + " Endpoint!");
            hideMessage();
            return;
        }
        let endpointDefinition = {
            id: storedEndpoint.id,
            name: endpointName,
            type: record.type,
            endpointConfig: record.endpointConfig,
            endpointSecurity: record.endpointSecurity,
            maxTps: record.maxTps
        };
        const api = new API();
        let promised_update = api.updateEndpoint(endpointDefinition);
        promised_update.then(
            response => {
                if (response.status !== 200) {
                    console.log("logging");
                    console.log(response);
                    message.error("Something went wrong while updating the " + endpointName + " Endpoint!");
                    hideMessage();
                    return;
                }
                message.success(endpointName + " Endpoint updated successfully!");
                hideMessage()
            }
        );
    }

    checkIfEndpointExists = () => {
        let record = this.state.record;
        let endpointConfig = JSON.parse(record.endpointConfig);
        let endpointName = endpointConfig.namespace + "-" + record.name + "-"
                            + record.type + "-" + endpointConfig.urlType;
        return this.state.storedEndpoints.some(el => el.name === endpointName);
    }

    componentDidMount() {
        if(this.state.storedEndpoints != null){
            if(this.checkIfEndpointExists()) {
                this.setState({
                    actionButton: this.getUpdateButton()
                });
            } else {
                this.setState({
                    actionButton: this.getAddButton()
                });
            }
        }
    }

    render() {
        return (this.state.actionButton);
    }
}

