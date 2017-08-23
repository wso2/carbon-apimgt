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
import Button from 'material-ui/Button';
import API from '../../../data/api.js'

import PropTypes from 'prop-types';
import { withStyles, createStyleSheet } from 'material-ui/styles';
import Table, {
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TableSortLabel,
} from 'material-ui/Table';
import Paper from 'material-ui/Paper';

const columnData = [
    { id: 'name', numeric: false, disablePadding: true, label: 'Name' },
    { id: 'throttlingTier', numeric: false, disablePadding: false, label: 'Tier' },
    { id: 'lifeCycleStatus', numeric: false, disablePadding: false, label: 'Workflow Status' },
    { id: 'subscriptions', numeric: true, disablePadding: false, label: 'Subscriptions' },
    { id: 'actions', numeric: false, disablePadding: false, label: 'Actions' },
];

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
        const {  order, orderBy } = this.props;
        return (
            <TableHead>
                <TableRow>
                    {columnData.map(column => {
                        return (
                            <TableCell
                                key={column.id}
                                numeric={column.numeric}
                                disablePadding={column.disablePadding}
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

const toolbarStyleSheet = createStyleSheet(theme => ({
    root: {
        paddingRight: 2,
    },
    highlight:
        theme.palette.type === 'light'
            ? {
            color: theme.palette.accent.A700,
            backgroundColor: theme.palette.accent.A100,
        }
            : {
            color: theme.palette.accent.A100,
            backgroundColor: theme.palette.accent.A700,
        },
    spacer: {
        flex: '1 1 100%',
    },
    actions: {
        color: theme.palette.text.secondary,
    },
    title: {
        flex: '0 0 auto',
    },
}));

const styleSheet = createStyleSheet(theme => ({
    paper: {
        width: '100%',
        marginTop: theme.spacing.unit * 3,
        overflowX: 'auto',
    },
}));

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
        data : []
    };

    handleRequestSort = (event, property) => {
        const orderBy = property;
        let order = 'desc';
        if (this.state.orderBy === property && this.state.order === 'desc') {
            order = 'asc';
        }
        const data = this.state.data.sort(
            (a, b) => (order === 'desc' ? b[orderBy] > a[orderBy] : a[orderBy] > b[orderBy]),
        );
        this.setState({ data, order, orderBy });
    };

    render() {
        const classes = this.props.classes;
        const { data, order, orderBy, selected } = this.state;

        return (
            <div>
                <div className="page-header" id="Message">
                    <h2>Applications</h2>
                </div>

                <Link to={"/application/create"}>
                    <Button raised>
                            Add Application
                    </Button>
                </Link>
                <div>
                    <p>An application is a logical collection of APIs. Applications allow you to use a single access
                       token
                       to invoke a collection of APIs and to subscribe to one API multiple times with different SLA
                       levels.
                       The DefaultApplication is pre-created and allows unlimited access by default.
                    </p>
                </div>
                <Paper className={classes.paper}>
                    <Table>
                        <ApplicationTableHead order={order} orderBy={orderBy} onRequestSort={this.handleRequestSort}/>
                        <TableBody>
                            {data.map(n => {
                                return (
                                    <TableRow hover tabIndex="-1" key={n.applicationId}>
                                        <TableCell disablePadding>
                                            <Link to={"/applications/" + n.applicationId}>
                                                {n.name}
                                            </Link>
                                        </TableCell>
                                        <TableCell disablePadding>
                                            {n.throttlingTier}
                                        </TableCell>
                                        <TableCell disablePadding>
                                            {n.lifeCycleStatus}
                                        </TableCell>
                                        <TableCell disablePadding>
                                            0
                                        </TableCell>
                                        <TableCell disablePadding>
                                            View Edit Delete
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </Paper>
            </div>
        );
    }
}

Listing.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styleSheet)(Listing);