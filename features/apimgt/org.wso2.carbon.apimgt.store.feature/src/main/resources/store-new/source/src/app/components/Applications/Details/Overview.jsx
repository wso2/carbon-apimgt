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
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Loading from '../../Base/Loading/Loading';
import API from '../../../data/api';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
/**
 *
 *
 * @class Overview
 * @extends {Component}
 */
class Overview extends Component {
    constructor(props) {
        super(props);
        this.state = {
            application: null,
            notFound: false,
            tierDescription: null,
        };
    }

    /**
     *
     *
     * @memberof Overview
     */
    componentDidMount() {
        const client = new API();
        // Get application
        const promised_application = client.getApplication(this.props.match.params.applicationId);
        promised_application
            .then((response) => {
                const promised_tier = client.getTierByName(response.obj.throttlingTier, 'application');
                return Promise.all([response, promised_tier]);
            })
            .then((response) => {
                const [application, tier] = response.map(data => data.obj);
                this.setState({ application, tierDescription: tier.description });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @returns
     * @memberof Overview
     */
    render() {
        const redirect_url = '/applications/' + this.props.match.params.application_uuid + '/overview';
        const { application, tierDescription, notFound } = this.state;
        if (notFound) {
            return <ResourceNotFound />;
        }
        if (!application) {
            return <Loading />;
        }
        return (
            <Paper>
                <Grid container className='tab-grid' spacing={0}>
                    <Grid>
                        <Table>
                            <TableBody>
                                <TableRow>
                                    <TableCell>Throttling Tier</TableCell>
                                    <TableCell>
                                        {application.throttlingTier}
                                        {' '}
                                        {tierDescription}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Life Cycle State</TableCell>
                                    <TableCell>{application.lifeCycleStatus}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>Application Description</TableCell>
                                    <TableCell>{application.description}</TableCell>
                                </TableRow>
                            </TableBody>
                        </Table>
                    </Grid>
                </Grid>
            </Paper>
        );
    }
}
export default Overview;
