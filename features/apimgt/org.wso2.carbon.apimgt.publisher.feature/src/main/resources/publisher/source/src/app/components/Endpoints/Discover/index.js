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
import {message} from 'antd';
import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import ArrowDropDown from 'material-ui-icons/ArrowDropDown';
import { MenuItem } from 'material-ui/Menu';
import Popover from 'material-ui/Popover';
import { CircularProgress } from 'material-ui/Progress';
import Table, { TableBody, TableCell, TableHead, TableRow, TableFooter, TablePagination} from 'material-ui/Table';

import './discover.css'
import API from '../../../data/api'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';


export default class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            anchorElEndpointsMenu: null,
            discoveredEndpoints: null,
            viewableEndpoints: null,
            filterType: "namespace",
            filterText: "",
            storedEndpoints: null,
            endpointBeingAdded: false,
            page: 0,
            rowsPerPage: 5,
        };
        this.handleFilterTextInputChange = this.handleFilterTextInputChange.bind(this);
        this.handleRadioButtonChange = this.handleRadioButtonChange.bind(this);
    }

    handleGlobalEndpointButtonClick = event => {
        this.setState({ anchorElEndpointsMenu: event.target });
    };

    handleCloseGlEndpointMenu = () => {
        this.setState({ anchorElEndpointsMenu: null });
      };

    handleRadioButtonChange(e) {
        this.setState({
            filterType: e.target.value,
            viewableEndpoints: this.filterEndpoints(e.target.value, this.state.filterText)
        })
    }

    handleFilterTextInputChange(e) {
        this.setState({
            filterText: e.target.value,
            viewableEndpoints: this.filterEndpoints(this.state.filterType, e.target.value)
        })
    }

    filterEndpoints(filterType, filterText) {
        const {discoveredEndpoints} = this.state;
        switch(filterType) {
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
                message.error("Error while retrieving stored endpoints");
            }
        );
    }

    componentDidMount() {
        this.discoverServices();
        this.getStoredEndpoints();
    }

    handleChangePage = (event, page) => {
        this.setState({ page });
    };

    handleChangeRowsPerPage = event => {
        this.setState({ rowsPerPage: event.target.value });
    };

    render() {
        const {viewableEndpoints, rowsPerPage, page} = this.state;
        const globalEndpointMenuOpen = !!this.state.anchorElEndpointsMenu;

        if (viewableEndpoints === null) {
            return (
                <div className="ed-loading"><CircularProgress /></div>
            );
        }

        return (
            <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY} resourceMethod={resourceMethod.GET}>
                <div className="ed-body">
                    <div className="ed-top-section">
                        <Typography className="page-title ed-title" type="title">
                             Discovered Service Endpoints
                        </Typography>
                        <div className="ed-global-endpoints-button-div">
                            <Button raised
                                className="ed-global-endpoints-button"
                                onClick={this.handleGlobalEndpointButtonClick}
                            >
                                Global Endpoints
                                <span className="ed-drop-down-arrow">  <ArrowDropDown className="more-arrow-shift ed-drop-down-arrow"/></span>
                            </Button>
                            <Popover
                                open={globalEndpointMenuOpen}
                                anchorEl={this.state.anchorElEndpointsMenu}
                                anchorOrigin={{horizontal: 'right', vertical: 'top'}}
                                transformOrigin={{horizontal: 'left', vertical: 'top'}}
                                onRequestClose={this.handleCloseGlEndpointMenu}
                            >
                                <div>
                                    <MenuItem component={Link} to='/endpoints'>View all</MenuItem>
                                    <MenuItem component={Link} to='/endpoints/create'>Create custom endpoint</MenuItem>
                                </div>
                            </Popover>
                        </div>
                        <div className="ed-filter-area">
                            <input
                              type="text"
                              className="ed-filter-input"
                              placeholder="Type here to filter.."
                              onChange={this.handleFilterTextInputChange}
                            />
                            <span className="ed-filter-area-inline-text"> Filter by</span>
                            <ul className="ed-radio">
                                <li>
                                    <input type="radio" name="amount" id="namespace"
                                        value="namespace"
                                        checked={this.state.filterType === 'namespace'}
                                        onChange={this.handleRadioButtonChange}
                                    />
                                    <label htmlFor="namespace">Namespace</label>
                                </li>
                                |
                                <li>
                                    <input type="radio" name="amount" id="criteria"
                                        value="criteria"
                                        checked={this.state.filterType === 'criteria'}
                                        onChange={this.handleRadioButtonChange}
                                        />
                                    <label htmlFor="criteria">Criteria</label>
                                </li>
                                |
                                <li>
                                    <input type="radio" name="amount" id="name"
                                        value="name"
                                        checked={this.state.filterType === 'name'}
                                        onChange={this.handleRadioButtonChange}
                                        />
                                    <label htmlFor="name">Service Name</label>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <Table>
                        <TableHead className="ed-table-head">
                            <TableRow>
                                <TableCell padding='dense' className="ed-regular-column">Name</TableCell>
                                <TableCell padding='dense' className="ed-regular-column">Namespace</TableCell>
                                <TableCell padding='dense' className="ed-regular-column">Criteria</TableCell>
                                <TableCell padding='dense' className="ed-fixed-slim-column">Type</TableCell>
                                <TableCell padding='dense' >Service URL</TableCell>
                                <TableCell padding='dense' className="ed-slim-column">Max TPS</TableCell>
                                <TableCell padding='dense' className="ed-button-column">Action</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {viewableEndpoints.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map(record => {
                                return (
                                    <TableRow key={record.id}>
                                        <TableCell padding='dense' className="ed-regular-column">{record.name}</TableCell>
                                        <TableCell padding='dense' className="ed-regular-column">{JSON.parse(record.endpointConfig).namespace}</TableCell>
                                        <TableCell padding='dense' className="ed-regular-column">{JSON.parse(record.endpointConfig).criteria}</TableCell>
                                        <TableCell padding='dense' className="ed-fixed-slim-column">{record.type}</TableCell>
                                        <TableCell padding='dense' >
                                            <span>
                                                {JSON.parse(record.endpointConfig).serviceUrl}
                                                &emsp;|&emsp;
                                                {JSON.parse(record.endpointConfig).urlType}
                                            </span>
                                        </TableCell>
                                        <TableCell padding='dense' className="ed-slim-column">{record.maxTps}</TableCell>
                                        <TableCell padding='dense' className="ed-button-column"><ButtonCell record={record} storedEndpoints={this.state.storedEndpoints}/></TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                        <TableFooter>
                            <TableRow>
                                <TablePagination
                                  count={viewableEndpoints.length}
                                  rowsPerPage={rowsPerPage}
                                  page={page}
                                  onChangePage={this.handleChangePage}
                                  onChangeRowsPerPage={this.handleChangeRowsPerPage}
                                />
                            </TableRow>
                        </TableFooter>
                      </Table>
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
            actionButton: <Button raised color="primary" className="ed-button">Loading...</Button>
        };
    }

    getAddButton() {
        return (
            <Button raised color="primary" className="ed-button" onClick={() =>this.handleAddEndpointToDB()}>
            Add as Global Endpoint </Button>
        );
    }

    getUpdateButton() {
        return (
            <Button raised color="secondary" className="ed-button" onClick={() =>this.handleUpdateEndpoint()}>
            Update added Endpoint </Button>
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
                hideMessage();
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

