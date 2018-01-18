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
import Menu, {MenuItem} from 'material-ui/Menu';
import Grid from 'material-ui/Grid';
import Button from 'material-ui/Button';
import AddIcon from 'material-ui-icons/Add';
import Table, {TableBody, TableCell, TableRow, TableHead} from 'material-ui/Table';

import API from '../../../data/api'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';
import NotificationSystem from 'react-notification-system';
import Paper from 'material-ui/Paper';
import EndpointTableRows from "../Create/EndpointTableRows";

export default class EndpointsListing extends Component {
    constructor(props) {
        super(props);
        this.state = {
            endpoints: null,
            openAddMenu: false,
            openMenu: false,
            anchorEl: null,
            selectedRowKeys: []
        };
        this.onSelectChange = this.onSelectChange.bind(this);
        this.handleEndpointDelete = this.handleEndpointDelete.bind(this);
        this.handleRequestOpenAddMenu = this.handleRequestOpenAddMenu.bind(this);
        this.handleRequestCloseAddMenu = this.handleRequestCloseAddMenu.bind(this);
    }

    handleEndpointDelete(endpointUuid, name) {
        const api = new API();
        let promised_delete = api.deleteEndpoint(endpointUuid);
        promised_delete.then(
            response => {
                if (response.status !== 200) {
                    console.log(response);
                    this.refs.notificationSystem.addNotification({
                        message: 'Something went wrong while deleting the ' + name + ' Endpoint!', position: 'tc',
                        level: 'error'
                    });
                    return;
                }
                this.refs.notificationSystem.addNotification({
                    message: name + ' Endpoint deleted successfully!', position: 'tc',
                    level: 'success'
                });
                let endpoints = this.state.endpoints;
                for (let endpointIndex in endpoints) {
                    if (endpoints.hasOwnProperty(endpointIndex) && endpoints[endpointIndex].id === endpointUuid) {
                        endpoints.splice(endpointIndex, 1);
                        break;
                    }
                }
                this.setState({active: false, endpoints: endpoints, openMenu: false});
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

    handleRequestCloseAddMenu() {
        this.setState({openAddMenu: false});
    };

    handleRequestOpenAddMenu(event) {
        this.setState({openAddMenu: true, anchorEl: event.currentTarget});
    };

    render() {
        const {anchorEl} = this.state;

        return (
            <div>
                <span>
                <h3>Global Endpoints</h3>
                </span>
                <NotificationSystem ref="notificationSystem"/>
                <Grid container>
                    <Grid item xs>
                        <Paper>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>Name</TableCell>
                                        <TableCell>Type</TableCell>
                                        <TableCell>Service URL</TableCell>
                                        <TableCell>Max TPS</TableCell>
                                        <TableCell>Action</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {this.state.endpoints && this.state.endpoints.map(endpoint => {
                                        return <EndpointTableRows endpoint={endpoint} key={endpoint.id}
                                                                  handleEndpointDelete={this.handleEndpointDelete}
                                        />
                                    })}
                                </TableBody>
                            </Table>
                        </Paper>
                    </Grid>
                </Grid>
                <div className="api-add-links">
                    <Grid container justify="flex-end" alignItems="center">
                        <Grid item xs={1}>
                            <Menu open={this.state.openAddMenu}
                                  onClose={this.handleRequestCloseAddMenu} id="simple-menu"
                                  anchorEl={anchorEl}>
                                <MenuItem onClick={this.handleRequestCloseAddMenu}>
                                    <Link to="/endpoints/create">Create new Endpoint</Link>
                                </MenuItem>
                                <MenuItem onClick={this.handleRequestCloseAddMenu}>
                                    <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY}
                                                     resourceMethod={resourceMethod.GET}>
                                        <Link to="/endpoints/discover">Discover Endpoints</Link>
                                    </ScopeValidation>
                                </MenuItem>
                            </Menu>
                            <Button aria-owns={anchorEl ? 'simple-menu' : null}
                                    aria-haspopup="true" fab color="accent"
                                    aria-label="add" onClick={this.handleRequestOpenAddMenu}>
                                <AddIcon/>
                            </Button>
                        </Grid>
                    </Grid>
                </div>
            </div>
        );
    }
}