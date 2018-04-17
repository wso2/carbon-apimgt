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

import React, { Component } from 'react';
import { Link } from 'react-router-dom';

import Typography from 'material-ui/Typography';
import Button from 'material-ui/Button';
import { MenuItem } from 'material-ui/Menu';
import { CircularProgress } from 'material-ui/Progress';
import Table, { TableBody, TableCell, TableHead, TableRow, TableFooter, TablePagination } from 'material-ui/Table';
import PropTypes from 'prop-types';
import { withStyles } from 'material-ui/styles';
import Grid from 'material-ui/Grid';
import ArrowBack from '@material-ui/icons/ArrowBack';
import { FormControl } from 'material-ui/Form';
import TextField from 'material-ui/TextField';
import Select from 'material-ui/Select';

import API from '../../../data/api';
import { ScopeValidation, resourceMethod, resourcePath } from '../../../data/ScopeValidation';
import Alert from '../../Shared/Alert';
import ButtonCell from './ButtonCell';

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
        marginRight: 50,
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
    },
});

/**
 * Endpoint discovery UI
 * @class EndpointsDiscover
 * @extends {Component}
 */
class EndpointsDiscover extends Component {
    /**
     * Creates an instance of EndpointsDiscover.
     * @param {any} props @inheritDoc
     * @memberof EndpointsDiscover
     */
    constructor(props) {
        super(props);
        this.state = {
            discoveredEndpoints: null,
            viewableEndpoints: null,
            filterType: 'namespace',
            filterText: '',
            storedEndpoints: null,
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

    getStoredEndpoints() {
        const api = new API();
        const promisedStoredEndpoints = api.getEndpoints();
        promisedStoredEndpoints
            .then((response) => {
                this.setState({
                    storedEndpoints: response.obj.list,
                });
            })
            .catch(() => {
                this.changeMessage('error', 'Error while retrieving stored endpoints');
            });
    }

    discoverServices() {
        const api = new API();
        const promisedDiscoveredEndpoints = api.discoverServices();
        promisedDiscoveredEndpoints.then((response) => {
            const { list } = response.obj;
            this.setState({
                discoveredEndpoints: list,
                viewableEndpoints: list,
            });
        });
    }

    handleRadioButtonChange = (e) => {
        this.setState({
            filterType: e.target.value,
            viewableEndpoints: this.filterEndpoints(e.target.value, this.state.filterText),
        });
    };

    handleFilterTextInputChange(e) {
        this.setState({
            filterText: e.target.value,
            viewableEndpoints: this.filterEndpoints(this.state.filterType, e.target.value),
        });
    }

    filterEndpoints(filterType, filterText) {
        const { discoveredEndpoints } = this.state;
        switch (filterType) {
            case 'namespace':
                return discoveredEndpoints.filter(el => JSON.parse(el.endpointConfig).namespace.startsWith(filterText));
            case 'criteria':
                return discoveredEndpoints.filter(el => JSON.parse(el.endpointConfig).criteria.includes(filterText));
            case 'name':
                return discoveredEndpoints.filter(el => el.name.startsWith(filterText));
            default:
                return discoveredEndpoints.filter(el => el.name.startsWith(filterText));
        }
    }

    changeMessage(typeOfMessage, stringOfMessage) {
        switch (typeOfMessage) {
            case 'success':
                Alert.success(stringOfMessage);
                break;
            case 'info':
                Alert.info(stringOfMessage);
                break;
            case 'error':
                Alert.error(stringOfMessage);
                break;
            case 'loading':
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

    handleChangeRowsPerPage = (event) => {
        this.setState({ rowsPerPage: event.target.value });
    };

    /**
     * @inheritDoc
     * @returns {React.Component} Endpoint discovery component
     * @memberof EndpointsDiscover
     */
    render() {
        const { viewableEndpoints, rowsPerPage, page } = this.state;

        if (viewableEndpoints === null) {
            return (
                <div className='ed-loading'>
                    <CircularProgress />
                </div>
            );
        }
        const { classes } = this.props;
        return (
            <ScopeValidation resourcePath={resourcePath.SERVICE_DISCOVERY} resourceMethod={resourceMethod.GET}>
                <Grid container spacing={0} justify='center'>
                    <Grid item xs={12} className={classes.titleBar}>
                        <div className={classes.buttonLeft}>
                            <Link to='/endpoints/'>
                                <Button variant='raised' size='small' className={classes.buttonBack} color='default'>
                                    <ArrowBack />
                                </Button>
                            </Link>
                            <div className={classes.title}>
                                <Typography variant='display2'>Service Discovery</Typography>
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
                                <MenuItem value='namespace'>Namespace</MenuItem>
                                <MenuItem value='criteria'>Criteria</MenuItem>
                                <MenuItem value='name'>Name</MenuItem>
                            </Select>
                        </FormControl>
                        <TextField
                            id='full-width'
                            InputLabelProps={{
                                shrink: true,
                            }}
                            placeholder='Type here to filter..'
                            helperText='Enter the filter criteria'
                            fullWidth
                            margin='normal'
                            onChange={this.handleFilterTextInputChange}
                            className={classes.textField}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <Table>
                            <TableHead className='ed-table-head'>
                                <TableRow>
                                    <TableCell padding='dense' className='ed-regular-column'>
                                        Name
                                    </TableCell>
                                    <TableCell padding='dense' className='ed-regular-column'>
                                        Namespace
                                    </TableCell>
                                    <TableCell padding='dense'>Criteria</TableCell>
                                    <TableCell padding='dense' className='ed-slim-column'>
                                        Type
                                    </TableCell>
                                    <TableCell padding='dense'>Service URL</TableCell>
                                    <TableCell padding='dense' className='ed-slim-column'>
                                        Max TPS
                                    </TableCell>
                                    <TableCell padding='dense' className='ed-button-column'>
                                        Action
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {viewableEndpoints
                                    .slice(page * rowsPerPage, (page * rowsPerPage) + rowsPerPage)
                                    .map((record) => {
                                        return (
                                            <TableRow key={record.id}>
                                                <TableCell padding='dense' className='ed-regular-column'>
                                                    {record.name}
                                                </TableCell>
                                                <TableCell padding='dense' className='ed-regular-column'>
                                                    {JSON.parse(record.endpointConfig).namespace}
                                                </TableCell>
                                                <TableCell padding='dense'>
                                                    {JSON.parse(record.endpointConfig).criteria}
                                                </TableCell>
                                                <TableCell padding='dense' className='ed-slim-column'>
                                                    {record.type}
                                                </TableCell>
                                                <TableCell padding='dense'>
                                                    <span>
                                                        {JSON.parse(record.endpointConfig).serviceUrl}
                                                        &emsp;|&emsp;
                                                        {JSON.parse(record.endpointConfig).urlType}
                                                    </span>
                                                </TableCell>
                                                <TableCell padding='dense' className='ed-slim-column'>
                                                    {record.maxTps}
                                                </TableCell>
                                                <TableCell padding='dense' className='ed-button-column'>
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
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(EndpointsDiscover);
