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

import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import AddIcon from 'material-ui-icons/Add';
import UpdateIcon from 'material-ui-icons/Update';
import { MenuItem } from 'material-ui/Menu';
import { CircularProgress } from 'material-ui/Progress';
import Table, { TableBody, TableCell, TableHead, TableRow, TableFooter, TablePagination} from 'material-ui/Table';

import API from '../../../data/api'
import {ScopeValidation, resourceMethod, resourcePath} from '../../../data/ScopeValidation';
import { Manager, Target } from 'react-popper';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Grid from 'material-ui/Grid';
import ArrowBack from 'material-ui-icons/ArrowBack'
import {  FormControl } from 'material-ui/Form';
import Alert from '../../Shared/Alert'
import TextField from 'material-ui/TextField';
import Select from 'material-ui/Select';


const styles = theme => ({
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
        buttonBack: {
            marginRight: 20,
        },
        filterWrapper: {
            display: 'flex',
        },
        formControl: {
            marginTop: 21,
        },
        textField: {
            marginLeft: 20,
        }
    }
);
class EndpointsDiscover extends Component {
    constructor(props) {
        super(props);
        this.state = {
            anchorElEndpointsMenu: null,
            discoveredEndpoints: null,
            viewableEndpoints: null,
            filterType: "namespace",
            filterText: "",
            storedEndpoints: null,
            messageType: "info",
            messageString: "Listing discovered endpoints",
            page: 0,
            rowsPerPage: 5,
        };
        this.handleFilterTextInputChange = this.handleFilterTextInputChange.bind(this);
        this.changeMessage = this.changeMessage.bind(this);
    }

    componentDidMount() {
        this.discoverServices();
        this.getStoredEndpoints();
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
                this.changeMessage("error", "Error while retrieving stored endpoints");
            }
        );
    }

    handleRadioButtonChange = (e) => {
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

    changeMessage(typeOfMessage, stringOfMessage) {
        switch ( typeOfMessage ) {
            case "success" :
                Alert.success(stringOfMessage);
                break;
            case "info" :
                Alert.info(stringOfMessage);
                break;
            case "error" :
                Alert.error(stringOfMessage);
                break;
            case "loading" :
                Alert.error(stringOfMessage);
                break;
            default:
                Alert.info(stringOfMessage);
                break;
        }
    }

    handleChangePage = (event, page) => {
        this.setState({ page });
    };

    handleChangeRowsPerPage = event => {
        this.setState({ rowsPerPage: event.target.value });
    };

    render() {
        const {viewableEndpoints, rowsPerPage, page} = this.state;

        if (viewableEndpoints === null) {
            return (
                <div className="ed-loading"><CircularProgress /></div>
            );
        }
        const {classes} = this.props;
        return (
            <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY} resourceMethod={resourceMethod.GET}>

                    <Grid container spacing={0} justify="center">
                        <Grid item xs={12} className={classes.titleBar}>
                            <div className={classes.buttonLeft}>
                                <Link to={"/endpoints/"}>
                                    <Button  variant="raised" size="small" className={classes.buttonBack}
                                             color="default">
                                        <ArrowBack />
                                    </Button>
                                </Link>
                                <div className={classes.title}>
                                    <Typography variant="display2">
                                        Service Discovery
                                    </Typography>
                                </div>
                            </div>
                        </Grid>
                        <Grid item xs={12} className={classes.filterWrapper}>
                            <FormControl className={classes.formControl}>
                                <Select
                                    value={this.state.filterType}
                                    onChange={this.handleRadioButtonChange}
                                    inputProps={{
                                        name: 'filter',
                                        id: 'filter',
                                    }}
                                >
                                    <MenuItem value="namespace">Namespace</MenuItem>
                                    <MenuItem value="criteria">Criteria</MenuItem>
                                    <MenuItem value="name">Name</MenuItem>
                                </Select>
                            </FormControl>
                            <TextField
                                id="full-width"
                                InputLabelProps={{
                                    shrink: true,
                                }}
                                placeholder="Type here to filter.."
                                helperText="Enter the filter criteria"
                                fullWidth
                                margin="normal"
                                onChange={this.handleFilterTextInputChange}
                                className={classes.textField}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Table>
                                <TableHead className="ed-table-head">
                                    <TableRow>
                                        <TableCell padding='dense' className="ed-regular-column">Name</TableCell>
                                        <TableCell padding='dense' className="ed-regular-column">Namespace</TableCell>
                                        <TableCell padding='dense' >Criteria</TableCell>
                                        <TableCell padding='dense' className="ed-slim-column">Type</TableCell>
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
                                                <TableCell padding='dense' >{JSON.parse(record.endpointConfig).criteria}</TableCell>
                                                <TableCell padding='dense' className="ed-slim-column">{record.type}</TableCell>
                                                <TableCell padding='dense' >
                                                <span>
                                                    {JSON.parse(record.endpointConfig).serviceUrl}
                                                    &emsp;|&emsp;
                                                    {JSON.parse(record.endpointConfig).urlType}
                                                </span>
                                                </TableCell>
                                                <TableCell padding='dense' className="ed-slim-column">{record.maxTps}</TableCell>
                                                <TableCell padding='dense' className="ed-button-column">
                                                    <ButtonCell
                                                        record={record}
                                                        storedEndpoints={this.state.storedEndpoints}
                                                        changeMessage={this.changeMessage}
                                                    />
                                                </TableCell>
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
                        </Grid>
                    </Grid>
            </ScopeValidation>
        );
    }
}
EndpointsDiscover.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EndpointsDiscover);

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
            <AddIcon />&nbsp; Add</Button>
        );
    }

    getUpdateButton() {
        return (
            <Button raised className="ed-button" onClick={() =>this.handleUpdateEndpoint()}>
            <UpdateIcon />&nbsp; Update</Button>
        );
    }

    handleAddEndpointToDB = () => {
        this.props.changeMessage("loading", "Adding the Endpoint ...");
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
                debugger;
                this.setState({
                    actionButton: this.getUpdateButton()
                });
                this.props.changeMessage("success", "New endpoint " + name + " created successfully");
            }
        ).catch(
            error => {
                this.props.changeMessage("error", "Error occurred while creating the endpoint!");
            }
        )
    }

    handleUpdateEndpoint = () => {
        this.props.changeMessage("loading", "Updating the Endpoint ...");
        let record = this.state.record;
        let configObject = JSON.parse(record.endpointConfig);
        let endpointName = configObject.namespace + "-" + record.name + "-"
                            + record.type + "-" + configObject.urlType;
        let storedEndpoint = this.state.storedEndpoints.find(el => el.name === endpointName);
        if (storedEndpoint === null) {
            this.props.changeMessage("error",
                "Error while updating. Could not find the " + endpointName + " Endpoint!");
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
                    this.props.changeMessage("error",
                        "Something went wrong while updating the " + endpointName + " Endpoint!");
                    return;
                }
                this.props.changeMessage("success", "Endpoint " + endpointName + " updated successfully!");
            }
        ).catch(
            error => {
                console.error(error);
                this.props.changeMessage("error", "Error occurred while trying to update the endpoint!");
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
