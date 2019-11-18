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
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';
import APIList from 'AppComponents/Apis/Listing/APICardView';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Subscription from 'AppData/Subscription';
import Api from 'AppData/api';
import { app } from 'Settings';
import SubscriptionTableData from './SubscriptionTableData';

/**
 *
 * @inheritdoc
 * @param {*} theme theme
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
    /**
     *Creates an instance of Subscriptions.
     * @param {*} props properties
     * @memberof Subscriptions
     */
    constructor(props) {
        super(props);
        this.state = {
            subscriptions: null,
            unsubscribedAPIList: [],
            apisNotFound: false,
            subscriptionsNotFound: false,
            isAuthorize: true,
        };
        this.handleSubscriptionDelete = this.handleSubscriptionDelete.bind(this);
        this.updateSubscriptions = this.updateSubscriptions.bind(this);
        this.updateUnsubscribedAPIsList = this.updateUnsubscribedAPIsList.bind(this);
        this.handleSubscribe = this.handleSubscribe.bind(this);
        this.getIdsOfSubscribedEntities = this.getIdsOfSubscribedEntities.bind(this);
    }

    /**
     *
     *
     * @memberof Subscriptions
     */
    componentDidMount() {
        const {
            match: {
                params: { applicationId },
            },
        } = this.props;
        this.updateSubscriptions(applicationId);
        this.updateUnsubscribedAPIsList();
    }

    /**
     *
     * Get List of the Ids of all APIs that have been already subscribed
     *
     * @returns {*} Ids of respective APIs
     * @memberof Subscriptions
     */
    getIdsOfSubscribedEntities() {
        const { subscriptions } = this.state;

        // Get arrays of the API Ids and remove all null/empty references by executing 'fliter(Boolean)'
        const subscribedAPIIds = subscriptions.map(sub => sub.apiId).filter(Boolean);

        return subscribedAPIIds;
    }

    /**
     *
     * Update subscriptions list of Application
     * @param {*} applicationId application id
     * @memberof Subscriptions
     */
    updateSubscriptions(applicationId) {
        const client = new Subscription();
        const promisedSubscriptions = client.getSubscriptions(null, applicationId);
        promisedSubscriptions
            .then((response) => {
                this.setState({ subscriptions: response.body.list }, this.updateUnsubscribedAPIsList());
            })
            .catch((error) => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ subscriptionsNotFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }

    /**
     *
     * Handle subscription deletion of application
     * @param {*} subscriptionId subscription id
     * @memberof Subscriptions
     */
    handleSubscriptionDelete(subscriptionId) {
        const client = new Subscription();
        const promisedDelete = client.deleteSubscription(subscriptionId);

        promisedDelete
            .then((response) => {
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
                this.setState({ subscriptions }, this.updateUnsubscribedAPIsList());
            })
            .catch((error) => {
                const { status } = error;
                if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
                Alert.error('Error occurred when deleting subscription');
            });
    }

    /**
     *
     * Update list of unsubscribed APIs
     * @memberof Subscriptions
     */
    updateUnsubscribedAPIsList() {
        const apiClient = new Api();
        const promisedGetApis = apiClient.getAllAPIs({ query: 'status:published' });

        promisedGetApis
            .then((response) => {
                const { list } = response.obj;
                const subscribedIds = this.getIdsOfSubscribedEntities();
                const unsubscribedAPIList = list
                    .filter(api => (!subscribedIds.includes(api.id) && !api.advertiseInfo.advertised)
                     && api.isSubscriptionAvailable)
                    .map((filteredApi) => {
                        return {
                            Id: filteredApi.id,
                            Policy: filteredApi.throttlingPolicies,
                            Name: filteredApi.name,
                        };
                    });
                this.setState({ unsubscribedAPIList });
            })
            .catch((error) => {
                const { status } = error;
                if (status === 404) {
                    this.setState({ apisNotFound: true });
                } else if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
            });
    }

    /**
     * Handle onClick of subscribing to an API
     * @param {*} applicationId application id
     * @param {*} apiId api id
     * @param {*} policy policy
     * @memberof Subscriptions
     */
    handleSubscribe(applicationId, apiId, policy) {
        const api = new Api();
        const { intl } = this.props;
        if (!policy) {
            Alert.error(intl.formatMessage({
                id: 'Applications.Details.Subscriptions.select.a.subscription.policy',
                defaultMessage: 'Select a subscription policy',
            }));
            return;
        }

        const promisedSubscribe = api.subscribe(apiId, applicationId, policy);
        promisedSubscribe
            .then((response) => {
                if (response.status !== 201) {
                    Alert.error(intl.formatMessage({
                        id: 'Applications.Details.Subscriptions.error.occurred.during.subscription.not.201',
                        defaultMessage: 'Error occurred during subscription',
                    }));
                } else {
                    if (response.body.status === 'ON_HOLD') {
                        Alert.info(intl.formatMessage({
                            defaultMessage: 'Your subscription request has been submitted and is now awaiting ' +
                            'approval.',
                            id: 'subscription.pending',
                        }));
                    } else {
                        Alert.info(intl.formatMessage({
                            id: 'Applications.Details.Subscriptions.subscription.successful',
                            defaultMessage: 'Subscription successful',
                        }));
                    }
                    this.updateSubscriptions(applicationId);
                }
            })
            .catch((error) => {
                const { status } = error;
                if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
                Alert.error(intl.formatMessage({
                    id: 'Applications.Details.Subscriptions.error.occurred.during.subscription',
                    defaultMessage: 'Error occurred during subscription',
                }));
            });
    }

    /**
     * @inheritdoc
     * @memberof Subscriptions
     */
    render() {
        const { isAuthorize } = this.state;

        if (!isAuthorize) {
            window.location = app.context + '/services/configs';
        }

        const {
            subscriptions, unsubscribedAPIList, apisNotFound, subscriptionsNotFound,
        } = this.state;
        const {
            match: {
                params: { applicationId },
            },
        } = this.props;
        const { classes } = this.props;

        if (subscriptions) {
            return (
                <div className={classes.root}>
                    <Typography variant='h5' className={classes.keyTitle}>
                        <FormattedMessage
                            id='Applications.Details.Subscriptions.subscription.management'
                            defaultMessage='Subscription Management'
                        />
                    </Typography>

                    <Grid container className='tab-grid' spacing={2}>
                        <Grid item xs={4} className={classes.cardGrid}>
                            <APIList
                                apisNotFound={apisNotFound}
                                unsubscribedAPIList={unsubscribedAPIList}
                                applicationId={applicationId}
                                handleSubscribe={(app, api, policy) => this.handleSubscribe(app, api, policy)}
                            />
                        </Grid>
                        <Grid item xs={8} xl={11}>
                            <Card className={classes.card}>
                                <CardActions>
                                    <Typography variant='h6' gutterBottom className={classes.cardTitle}>
                                        <FormattedMessage
                                            id='Applications.Details.Subscriptions.subscriptions'
                                            defaultMessage='Subscriptions'
                                        />
                                    </Typography>
                                </CardActions>
                                <Divider />
                                <CardContent className={classes.cardContent}>
                                    {subscriptionsNotFound ? (
                                        <ResourceNotFound />
                                    ) : (
                                        <Table>
                                            <TableHead>
                                                <TableRow>
                                                    <TableCell className={classes.firstCell}>
                                                        <FormattedMessage
                                                            id='Applications.Details.Subscriptions.api.name'
                                                            defaultMessage='API'
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormattedMessage
                                                            id={`Applications.Details.Subscriptions
                                                                    .subscription.tier`}
                                                            defaultMessage='Lifecycle State'
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormattedMessage
                                                            id={`Applications.Details.Subscriptions
                                                                    .subscription.tier`}
                                                            defaultMessage='Subscription Tier'
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormattedMessage
                                                            id='Applications.Details.Subscriptions.Status'
                                                            defaultMessage='Subscription Status'
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormattedMessage
                                                            id='Applications.Details.Subscriptions.action'
                                                            defaultMessage='Action'
                                                        />
                                                    </TableCell>
                                                    <TableCell>
                                                        <FormattedMessage
                                                            id='Applications.Details.Subscriptions.invoice'
                                                            defaultMessage='Invoice'
                                                        />
                                                    </TableCell>
                                                </TableRow>
                                            </TableHead>
                                            <TableBody>
                                                {subscriptions &&
                                                    subscriptions.map((subscription) => {
                                                        return (
                                                            <SubscriptionTableData
                                                                key={subscription.subscriptionId}
                                                                subscription={subscription}
                                                                handleSubscriptionDelete={this.handleSubscriptionDelete}
                                                            />
                                                        );
                                                    })}
                                            </TableBody>
                                        </Table>
                                    )}
                                </CardContent>
                            </Card>
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
    classes: PropTypes.shape({}).isRequired,
    match: PropTypes.shape({
        params: PropTypes.shape({
            application_uuid: PropTypes.string.isRequired,
        }).isRequired,
    }).isRequired,
    intl: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles)(Subscriptions));
