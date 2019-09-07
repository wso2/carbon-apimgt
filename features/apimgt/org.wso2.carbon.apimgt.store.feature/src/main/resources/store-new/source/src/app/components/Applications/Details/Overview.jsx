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
import PropTypes from 'prop-types';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import { FormattedMessage } from 'react-intl';
import Loading from 'AppComponents/Base/Loading/Loading';
import API from 'AppData/data/api';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';

/**
 *
 *
 * @class Overview
 * @extends {Component}
 */
class Overview extends Component {
    /**
     *Creates an instance of Overview.
     * @param {*} props properties
     * @memberof Overview
     */
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
        const { match } = this.props;
        // Get application
        const promisedApplication = client.getApplication(match.params.applicationId);
        promisedApplication
            .then((response) => {
                const promisedTier = client.getTierByName(response.obj.throttlingTier, 'application');
                return Promise.all([response, promisedTier]);
            })
            .then((response) => {
                const [application, tier] = response.map(data => data.obj);
                this.setState({ application, tierDescription: tier.description });
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const { status } = error;
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
                                    <TableCell>
                                        <FormattedMessage
                                            id='Applications.Details.Overview.less'
                                            defaultMessage='Throttling Tier'
                                        />
                                    </TableCell>
                                    <TableCell>
                                        {application.throttlingTier}
                                        {' '}
                                        {tierDescription}
                                    </TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Applications.Details.Overview.life.cycle.state'
                                            defaultMessage='Life Cycle State'
                                        />
                                    </TableCell>
                                    <TableCell>{application.lifeCycleStatus}</TableCell>
                                </TableRow>
                                <TableRow>
                                    <TableCell>
                                        <FormattedMessage
                                            id='Applications.Details.Overview.application.description'
                                            defaultMessage='Application Description'
                                        />
                                    </TableCell>
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

Overview.propTypes = {
    match: PropTypes.shape({
        params: PropTypes.shape({
            application_uuid: PropTypes.string.isRequired,
        }).isRequired,
    }).isRequired,
};
export default Overview;
