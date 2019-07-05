/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete';
import EditIcon from '@material-ui/icons/Edit';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import CircularProgress from '@material-ui/core/CircularProgress';
import Subscription from '../../../data/Subscription';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import { ScopeValidation, resourceMethods, resourcePaths } from '../../Shared/ScopeValidation';
/**
 *
 *
 * @param {*} order
 * @param {*} orderBy
 * @returns
 */
function getSorting(order, orderBy) {
    return order === 'desc' ? (a, b) => (b[orderBy] < a[orderBy] ? -1 : 1)
        : (a, b) => (a[orderBy] < b[orderBy] ? -1 : 1);
}
/**
 *
 *
 * @class AppsTableContent
 * @extends {Component}
 */
class AppsTableContent extends Component {
    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            subscriptions: false,
            notFound: false,
        };
        this.APPLICATION_STATES = {
            CREATED: 'CREATED',
            APPROVED: 'APPROVED',
            REJECTED: 'REJECTED',
        };
    }

    // /**
    //  * Get all applications
    //  * @memberof AppsTableContent
    //  */
    // componentDidMount() {
    //     const client = new Subscription();
    //     const { apps } = this.props;
    //     const appIds = [...apps.keys()];
    //     const promises = appIds.map(appId => client.getSubscriptions(null, appId).then((response) => {
    //         response.appId = appId;
    //         return response;
    //     }));

    //     Promise.all(promises)
    //         .then((response) => {
    //             response.map((data) => {
    //                 const app = apps.get(data.appId);
    //                 app.subscriptions = data.body.count;
    //                 apps.set(app.applicationId, app);
    //             });
    //             this.setState({ subscriptions: true });
    //         })
    //         .catch((error) => {
    //             this.setState({ notFound: true });
    //             console.error(error);
    //         });
    // }

    /**
     *
     *
     * @returns
     * @memberof AppsTableContent
     */
    render() {
        const {
            apps, handleAppDelete, page, rowsPerPage, order, orderBy,
        } = this.props;
        const { notFound } = this.state;
        const emptyRowsPerPage = rowsPerPage - Math.min(rowsPerPage, apps.size - page * rowsPerPage);
        let appsTableData = [];

        if (apps) {
            appsTableData = [...apps.values()].map((app) => {
                app.deleting = false;
                return app;
            });
        }
        if (notFound) {
            return <ResourceNotFound />;
        }
        return (
            <TableBody>
                {appsTableData
                    .sort(getSorting(order, orderBy))
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((app) => {
                        return (
                            <TableRow key={app.applicationId}>
                                <TableCell>
                                    {app.status === this.APPLICATION_STATES.APPROVED ? (
                                        <Link to={'/applications/' + app.applicationId}>{app.name}</Link>
                                    ) : (
                                        app.name
                                    )
                                    }
                                </TableCell>
                                <TableCell>{app.throttlingPolicy}</TableCell>
                                <TableCell>
                                    {app.status === this.APPLICATION_STATES.APPROVED && (
                                        <Typography variant='subheading' gutterBottom>ACTIVE</Typography>
                                    )}
                                    {app.status === this.APPLICATION_STATES.CREATED && (
                                        <Typography variant='subheading' gutterBottom>
                                            INACTIVE
                                            <Typography variant='caption'>
                                            waiting for approval
                                            </Typography>
                                        </Typography>
                                    )}
                                    {app.status === this.APPLICATION_STATES.REJECTED && (
                                        <Typography variant='subheading' gutterBottom>REJECTED</Typography>
                                    )}
                                </TableCell>
                                <TableCell>{app.subscriptionCount}</TableCell>
                                <TableCell>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.PUT}
                                    >
                                        {app.status === this.APPLICATION_STATES.APPROVED && (
                                            <Tooltip title='Edit'>
                                                <Link to={'application/edit/' + app.applicationId}>
                                                    <IconButton>
                                                        <EditIcon aria-label='Edit' />
                                                    </IconButton>
                                                </Link>
                                            </Tooltip>
                                        )}
                                    </ScopeValidation>
                                    <ScopeValidation
                                        resourcePath={resourcePaths.SINGLE_APPLICATION}
                                        resourceMethod={resourceMethods.DELETE}
                                    >
                                        <Tooltip title='Delete'>
                                            <IconButton
                                                disabled={app.deleting}
                                                data-appId={app.applicationId}
                                                onClick={handleAppDelete}
                                                color='default'
                                                aria-label='Delete'
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        </Tooltip>
                                    </ScopeValidation>
                                    {app.deleting && <CircularProgress size={24} />}
                                </TableCell>
                            </TableRow>
                        );
                    })}
                {emptyRowsPerPage > 0 && (
                    <TableRow style={{ height: 49 * emptyRowsPerPage }}>
                        <TableCell colSpan={6} />
                    </TableRow>
                )}
            </TableBody>
        );
    }
}
export default AppsTableContent;
