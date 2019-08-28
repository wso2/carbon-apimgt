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

import React from 'react';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import Divider from '@material-ui/core/Divider';
import Alert from '../../Shared/Alert';
import SubscriptionTableData from './SubscriptionTableData';
import APIList from '../../Apis/Listing/APIList';
import Subscription from '../../../data/Subscription';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    root: {
        padding: theme.spacing.unit * 3,
    },
    keyTitle: {
        textTransform: 'uppercase',
        marginBottom: theme.spacing.unit * 2,
    },
    firstCell: {
        paddingLeft: 0,
    },
    cardTitle: {
        paddingLeft: theme.spacing.unit * 2,
    },
    cardContent: {
        minHeight: 200,
    },
});
/**
 *
 *
 * @class Subscriptions
 * @extends {React.Component}
 */
class Subscriptions extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            subscriptions: null,
        };
        this.handleSubscriptionDelete = this.handleSubscriptionDelete.bind(this);
        this.updateSubscriptions = this.updateSubscriptions.bind(this);
    }

    /**
     *
     *
     * @param {*} applicationId
     * @memberof Subscriptions
     */
    updateSubscriptions(applicationId) {
        const client = new Subscription();
        const promisedSubscriptions = client.getSubscriptions(null, applicationId);
        promisedSubscriptions
            .then((response) => {
                this.setState({ subscriptions: response.body.list });
            })
            .catch((error) => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @memberof Subscriptions
     */
    componentDidMount() {
        const { applicationId } = this.props.match.params;
        this.updateSubscriptions(applicationId);
    }

    /**
     *
     *
     * @param {*} subscriptionId
     * @memberof Subscriptions
     */
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
                if (Object.prototype.hasOwnProperty.call(subscriptions, endpointIndex) && subscriptions[endpointIndex].subscriptionId === subscriptionId) {
                    subscriptions.splice(endpointIndex, 1);
                    break;
                }
            }
        });
    }

    /**
     *
     *
     * @returns
     * @memberof Subscriptions
     */
    render() {
        const { subscriptions } = this.state;
        const { applicationId } = this.props.match.params;
        const { classes } = this.props;
        if (subscriptions) {
            return (
                <div className={classes.root}>
                    <Typography variant='headline' className={classes.keyTitle}>
                        Subscription Management
                    </Typography>

                    <Grid container className='tab-grid' spacing={16}>
                        <Grid item xs={6}>
                            <Card className={classes.card}>
                                <CardActions>
                                    <Typography variant='h6' gutterBottom className={classes.cardTitle}>
                                        Subscriptions
                                    </Typography>
                                </CardActions>
                                <Divider />
                                <CardContent className={classes.cardContent}>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell className={classes.firstCell}>API Name</TableCell>
                                                <TableCell>Subscription Tier</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Action</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {subscriptions
                                                && subscriptions.map((subscription) => {
                                                    return <SubscriptionTableData subscription={subscription} key={subscription.subscriptionId} handleSubscriptionDelete={this.handleSubscriptionDelete} />;
                                                })}
                                        </TableBody>
                                    </Table>
                                </CardContent>
                            </Card>
                        </Grid>
                        <Grid item xs={6} className={classes.cardGrid}>
                            <APIList subscriptions={subscriptions} applicationId={applicationId} updateSubscriptions={this.updateSubscriptions} />
                        </Grid>
                    </Grid>
                </div>
            );
        } else {
            return 'Loading . . . ';
        }
    }
}
Subscriptions.propTypes = {
    classes: PropTypes.object,
};

export default withStyles(styles)(Subscriptions);
