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
import IconButton from 'material-ui/IconButton';
import Tooltip from 'material-ui/Tooltip';
import {CircularProgress} from 'material-ui/Progress';
import Subscription from "../../../data/Subscription";
import ResourceNotFound from "../../Base/Errors/ResourceNotFound";

function getSorting(order, orderBy) {
    return order === 'desc'
        ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1)
        : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}

class AppsTableBody extends Component {
    constructor(props){
        super(props);
        this.state = {
            subscriptions: false,
            notFound :  false
        }
    }
    componentDidMount(){
        let client = new Subscription();
        let {apps} = this.props;
        let appIds = [...apps.keys()];
        let promises = appIds.map(appId => client.getSubscriptions(null, appId)
            .then(response => { response.appId = appId; return response}));

        Promise.all(promises).then(response => {
            response.map(data => {
                let app = apps.get(data.appId);
                app["subscriptions"] = data.body.count;
                apps.set(app.applicationId, app);
            });
            this.setState({ subscriptions: true})
        }).catch(error => {
            this.setState({notFound : true});
            console.error(error);
        });
    }
    render() {
        const {apps, handleAppDelete, page, rowsPerPage, order, orderBy} = this.props;
        const {subscriptions, notFound} = this.state;
        const emptyRowsPerPage = rowsPerPage - Math.min(rowsPerPage, apps.size - page * rowsPerPage);
        let appsTableData = [];

        if (subscriptions) {
            appsTableData = [...apps.values()].map(app => {
                app["deleting"] = false;
                return app;
            });
        }
        if (notFound) {
            return <ResourceNotFound/>
        }
        return (
            <TableBody>
                {appsTableData
                    .sort(getSorting(order, orderBy))
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map(app => {
                        return (
                            <TableRow key={app.applicationId}>
                                <TableCell>
                                    <Link to={"/applications/" + app.applicationId}>
                                        {app.name}
                                    </Link>
                                </TableCell>
                                <TableCell>{app.throttlingTier}</TableCell>
                                <TableCell>{app.lifeCycleStatus}</TableCell>
                                <TableCell>{app.subscriptions}</TableCell>
                                <TableCell>
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
                                            <DeleteIcon/>
                                        </IconButton>
                                    </Tooltip>
                                    {app.deleting && <CircularProgress size={24}/>}
                                </TableCell>
                            </TableRow>
                        );
                    })
                }
                {emptyRowsPerPage > 0 && (
                    <TableRow style={{height: 49 * emptyRowsPerPage}}>
                        <TableCell colSpan={6}/>
                    </TableRow>
                )}
            </TableBody>
        )
    };
}
export default AppsTableBody;
