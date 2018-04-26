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

import React from 'react'
import Paper from 'material-ui/Paper';
import Grid from 'material-ui/Grid';
import Typography from 'material-ui/Typography';
import Table, { TableCell, TableHead, TableRow, TableSortLabel, TableBody } from 'material-ui/Table';
import PropTypes from 'prop-types';
import SubscriptionTableData from './SubscriptionTableData'
import Alert from '../../Shared/Alert';

import Subscription from '../../../data/Subscription';

class Subscriptions extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            subscriptions: null
        }
        this.handleSubscriptionDelete = this.handleSubscriptionDelete.bind(this);
    }

    componentDidMount() {
        const client = new Subscription();
        const {applicationId} = this.props.match.params;
        let promisedSubscriptions = client.getSubscriptions(null, applicationId);
        promisedSubscriptions.then((response) => {
            this.setState({ subscriptions: response.body.list });
        }
        ).catch(
            error => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    handleSubscriptionDelete(subscriptionId) {
        const client = new Subscription();
        const promisedDelete = client.deleteSubscription(subscriptionId);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info('Something went wrong while deleting the Subscription!');
                return;
            }
            Alert.info('Subscription deleted successfully!');
            const { subscriptions } = this.state;
            for (const endpointIndex in subscriptions) {
                if (
                    Object.prototype.hasOwnProperty.call(subscriptions, endpointIndex) &&
                    subscriptions[endpointIndex].subscriptionId === subscriptionId
                ) {
                    subscriptions.splice(endpointIndex, 1);
                    break;
                }
            }
        });
    }

    render() {
        const { subscriptions } = this.state;
        if (subscriptions) {
            return (
                <Paper>
                    <Grid container className="tab-grid" spacing={0} >
                        <Grid item xs={12}>
                            <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>API Name</TableCell>
                                        <TableCell>Subscription Tier</TableCell>
                                        <TableCell>Status</TableCell>
                                        <TableCell>Action</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {subscriptions &&
                                        subscriptions.map((subscription) => {
                                            return (
                                                <SubscriptionTableData
                                                    subscription={subscription}
                                                    key={subscription.subscriptionId}
                                                    handleSubscriptionDelete={this.handleSubscriptionDelete}
                                                />
                                            );
                                        })}
                                </TableBody>
                            </Table>
                        </Grid>
                    </Grid>
                </Paper>
            );
        } else {
            return ("Loading . . . ");
        }

    }
}
export default Subscriptions

