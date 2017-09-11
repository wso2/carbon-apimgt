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

class ApplicationTable extends Component {

    componentDidMount() {
        let applicationResponseObj = JSON.parse('{' +
            '"count":4,' +
            '"next":"",' +
            '"previous":"",' +
            '"list":[' +
            '{' +
            '"applicationId":"6fabb2fc-cece-47b1-9509-7d40d7c91f06",' +
            '"name":"sampleapp1",' +
            '"subscriber":"admin",' +
            '"throttlingTier":"Unlimited",' +
            '"description":"sample app description",' +
            '"lifeCycleStatus":"APPROVED"' +
            '},' +
            '{' +
            '"applicationId":"5bc78bf8-711c-4866-ab03-265d1c61c58d",' +
            '"name":"App2",' +
            '"subscriber":"admin",' +
            '"throttlingTier":"Unlimited",' +
            '"description":"sample app description",' +
            '"lifeCycleStatus":"APPROVED"' +
            '},' +
            '{"applicationId":"2f061596-df6f-4280-9a7a-a4b69f56ae7e",' +
            '"name":"HelloApp",' +
            '"subscriber":"admin",' +
            '"throttlingTier":"50PerMin",' +
            '"description":"sample app description",' +
            '"lifeCycleStatus":"APPROVED"' +
            '},' +
            '{' +
            '"applicationId":"3753f1a0-64d4-4251-b52f-b6cccdadb6f5",' +
            '"name":"TenPerMinApp",' +
            '"subscriber":"admin",' +
            '"throttlingTier":"10PerMin",' +
            '"description":"10 per min description",' +
            '"lifeCycleStatus":"APPROVED"' +
            '}]}');
        let applicationData = [];
        applicationResponseObj.list.map(item => applicationData.push(item));
        this.setState({data: applicationData});
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
            <Paper className={classes.paper}>
                <Table>
                    <ApplicationTableHead order={order} orderBy={orderBy} onRequestSort={this.handleRequestSort}/>
                    <TableBody>
                        {data.map(n => {
                            return (
                                <TableRow hover tabIndex="-1" key={n.applicationId}>
                                    <TableCell disablePadding>
                                        {n.name}
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
        );
    }
}

ApplicationTable.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styleSheet)(ApplicationTable);