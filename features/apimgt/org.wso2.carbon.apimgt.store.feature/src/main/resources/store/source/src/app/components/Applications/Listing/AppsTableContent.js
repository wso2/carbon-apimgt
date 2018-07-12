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
"use strict";
import React, {Component} from 'react';
import {Link} from 'react-router-dom';
import {TableBody, TableCell, TableRow} from 'material-ui/Table';
import DeleteIcon from 'material-ui-icons/Delete';
import EditIcon from 'material-ui-icons/Edit';
import ViewIcon from 'material-ui-icons/RemoveRedEye';
import IconButton from 'material-ui/IconButton';
import Tooltip from 'material-ui/Tooltip';
import {CircularProgress} from 'material-ui/Progress';
import Subscription from "../../../data/Subscription";

function getSorting(order, orderBy) {
    return order === 'desc'
        ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1)
        : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}

const AppsTableBody = (props) => {
    const {apps, handleAppDelete, classes, page, rowsPerPage, order, orderBy} = props;
    const emptyRowsPerPage = rowsPerPage - Math.min(rowsPerPage, apps.size - page*rowsPerPage);
    let appsTableData = [];
    apps.forEach(app => {
        appsTableData.push(
            <TableRow hover tabIndex="-1" key={app.applicationId}>
                <TableCell>
                    <Link to={"/applications/" + app.applicationId}>
                        {app.name}
                    </Link>
                </TableCell>
                <TableCell>
                    {app.throttlingTier}
                </TableCell>
                <TableCell>
                    {app.lifeCycleStatus}
                </TableCell>
                <TableCell>
                    <Subscribers applicationId={app.applicationId}/>
                </TableCell>
                <TableCell>
                    <Tooltip title="View">
                        <Link to={"/applications/" + app.applicationId}>
                            <IconButton>
                                <ViewIcon aria-label="View"/>
                            </IconButton>
                        </Link>
                    </Tooltip>
                    <Tooltip title="Edit">
                        <Link to={"application/edit/" + app.applicationId}>
                            <IconButton>
                                <EditIcon aria-label="Edit"/>
                            </IconButton>
                        </Link>
                    </Tooltip>
                    <Tooltip title="Delete">
                        <IconButton disabled={app.deleting} data-appId={app.applicationId}
                                    onClick={handleAppDelete} color="default"
                                    aria-label="Delete">
                            <DeleteIcon />
                        </IconButton>
                    </Tooltip>
                    { app.deleting && <CircularProgress size={24}/>}
                </TableCell>
            </TableRow>
        );
    });
    return (
        <TableBody>
            {appsTableData
                .sort(getSorting(order,orderBy))
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
            }
            {emptyRowsPerPage > 0 && (
                <TableRow style={{ height: 49 * emptyRowsPerPage }}>
                    <TableCell colSpan={6} />
                </TableRow>
            )}
        </TableBody>
    )
};

class Subscribers extends Component {
    constructor(props) {
        super(props);
        this.state = {
            subscriptions:0
        }
    }
    componentDidMount() {
        let client = new Subscription();
        const {applicationId} = this.props;
        let promised_subscriptions = client.getSubscriptions(null, applicationId);
        promised_subscriptions.then ( (response) => {
            this.setState({subscriptions:response.obj.count});

        }).catch(
            error => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }
    render(){
        return this.state.subscriptions;
    }
}

export default AppsTableBody;