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

import React, {Component} from 'react';
import {Link} from 'react-router-dom';
import Button from 'material-ui/Button';
import API from '../../../data/api'
import Application from '../../../data/Application'

import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import {withStyles} from 'material-ui/styles';
import PropTypes from 'prop-types';
import Table, {
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TableSortLabel,
} from 'material-ui/Table';
import Paper from 'material-ui/Paper';
import IconButton from 'material-ui/IconButton';
import DeleteIcon from 'material-ui-icons/Delete';
import AddIcon from 'material-ui-icons/Add';

const columnData = [
    {id: 'name', numeric: false, disablePadding: true, label: 'Name'},
    {id: 'throttlingTier', numeric: false, disablePadding: false, label: 'Tier'},
    {id: 'lifeCycleStatus', numeric: false, disablePadding: false, label: 'Workflow Status'},
    {id: 'subscriptions', numeric: true, disablePadding: false, label: 'Subscriptions'},
    {id: 'actions', numeric: false, disablePadding: false, label: 'Actions'},
];

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
    fullWidth: {
        width: "100%",
        "margin-top": "1%"
    }
});

class ApplicationTableHead extends Component {
    static propTypes = {
        onRequestSort: PropTypes.func.isRequired,
        order: PropTypes.string.isRequired,
        orderBy: PropTypes.string.isRequired,
    };

    createSortHandler = property => event => {
        this.props.onRequestSort(event, property);
    };

    render() {
        const {order, orderBy} = this.props;
        return (
            <TableHead>
                <TableRow>
                    {columnData.map(column => {
                        return (
                            <TableCell
                                key={column.id}
                                numeric={column.numeric}
                            >
                                <TableSortLabel
                                    active={orderBy === column.id}
                                    direction={order}
                                    onClick={this.createSortHandler(column.id)}
                                >
                                    {column.label}
                                </TableSortLabel>
                            </TableCell>
                        );
                    }, this)}
                </TableRow>
            </TableHead>
        );
    }
}


class Listing extends Component {

    constructor(props) {
        super(props);

    }

    componentDidMount() {
        let applicationApi = new API();
        let promised_applications = applicationApi.getAllApplications();
        promised_applications.then((response) => {
            let applicationResponseObj = response.body;
            let applicationData = [];
            applicationResponseObj.list.map(item => applicationData.push(item));
            this.setState({data: applicationData});
        }).catch(error => {
            if (process.env.NODE_ENV !== "production")
                console.log(error);
            let status = error.status;
            if (status === 404) {
                this.setState({notFound: true});
            } else if (status === 401) {
                this.setState({isAuthorize: false});
                let params = qs.stringify({reference: this.props.location.pathname});
                this.props.history.push({pathname: "/login", search: params});
            }
        });
    }

    state = {
        order: 'asc',
        orderBy: 'name',
        selected: [],
        data: []
    };

    handleAppDelete(event) {
        const id = "";
        Application.delete(id);
    }

    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';
        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }
        const data = this.state.data.sort(
            (a, b) => (order === 'desc' ? b[orderBy] > a[orderBy] : a[orderBy] > b[orderBy]),
        );
        this.setState({data, order, orderBy});
    };

    render() {
        const {data, order, orderBy, selected} = this.state;
        const {classes} = this.props;
        return (
            <Grid className={classes.fullWidth} container justify="center" alignItems="center">
                <Grid item xs={11}>
                    <Paper>
                        <Grid item xs={10}>
                            <Typography type="display1" className="page-title">
                                Applications
                            </Typography>
                            <Typography type="caption" className="page-title" paragraph={true}>
                                An application is a logical collection of APIs. Applications allow you to use a single
                                access
                                token to invoke a collection of APIs and to subscribe to one API multiple times with
                                different
                                SLA levels. The DefaultApplication is pre-created and allows unlimited access by
                                default.
                            </Typography>
                        </Grid>
                        <hr/>
                        <Table>
                            <ApplicationTableHead order={order} orderBy={orderBy}
                                                  onRequestSort={this.handleRequestSort}/>
                            <TableBody>
                                {data.map(n => {
                                    return (
                                        <TableRow hover tabIndex="-1" key={n.applicationId}>
                                            <TableCell>
                                                <Link to={"/applications/" + n.applicationId}>
                                                    {n.name}
                                                </Link>
                                            </TableCell>
                                            <TableCell>
                                                {n.throttlingTier}
                                            </TableCell>
                                            <TableCell>
                                                {n.lifeCycleStatus}
                                            </TableCell>
                                            <TableCell>
                                                0
                                            </TableCell>
                                            <TableCell>
                                                <IconButton color="accent" className={classes.button}
                                                            aria-label="Delete">
                                                    <DeleteIcon />
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </Paper>
                </Grid>
                <Grid className={classes.fullWidth} container justify="flex-end" alignItems="center">
                    <Grid>
                        <Link to={"/application/create"}>
                            <Button fab color="accent" aria-label="add" className={classes.button}>
                                <AddIcon />
                            </Button>
                        </Link>
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}


export default withStyles(styles)(Listing);
