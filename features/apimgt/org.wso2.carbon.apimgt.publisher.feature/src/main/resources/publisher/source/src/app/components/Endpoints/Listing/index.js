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
import Grid from 'material-ui/Grid';
import Table, {TableBody, TableCell, TableRow, TableHead} from 'material-ui/Table';

import API from '../../../data/api'
import NotificationSystem from 'react-notification-system';
import EndpointTableRows from "../Create/EndpointTableRows";
import Typography from 'material-ui/Typography';
import { Manager, Target } from 'react-popper';
import AddNewMenu from './AddNewMenu'
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';

const styles = theme => ({
    root: {
        width: '100%',
        marginTop: theme.spacing.unit * 3,
        overflowX: 'auto',
    },
    table: {
        minWidth: 700,
    },
    titleBar: {
        display: 'flex',
        justifyContent: 'space-between',
        borderBottomWidth: '1px',
        borderBottomStyle: 'solid',
        borderColor: theme.palette.text.secondary,
        marginBottom: 20,
    },
    buttonLeft: {
        alignSelf: 'flex-start',
        display: 'flex',
    },
    buttonRight: {
        alignSelf: 'flex-end',
        display: 'flex',
    },
    title: {
        display: 'inline-block',
        marginRight: 50
    },
    addButton: {
        display: 'inline-block',
        marginBottom: 20,
        zIndex: 1,
    },
    popperClose: {
        pointerEvents: 'none',
    }
});


class EndpointsListing extends Component {
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
        const { classes } = this.props;
        return (
        <Grid container spacing={0} justify="center">
            <Grid item xs={12} className={classes.titleBar}>
                <div className={classes.buttonLeft}>
                    <div className={classes.title}>
                        <Typography variant="display2" gutterBottom>
                            Global Endpoints
                        </Typography>
                    </div>
                    <AddNewMenu />
                    <NotificationSystem ref="notificationSystem"/>
                </div>
            </Grid>

            <Grid item xs={12}>

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
            </Grid>
        </Grid>
        )
    }
}
EndpointsListing.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointsListing);