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
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import Button from '@material-ui/core/Button';
import DialogActions from '@material-ui/core/DialogActions';
import Paper from '@material-ui/core/Paper';
import { FormattedMessage, injectIntl } from 'react-intl';
import Alert from 'AppComponents/Shared/Alert';
import APIList from 'AppComponents/Apis/Listing/APICardView';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Subscription from 'AppData/Subscription';
import Api from 'AppData/api';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormControl from '@material-ui/core/FormControl';
import FormLabel from '@material-ui/core/FormLabel';
import APIProduct from 'AppData/APIProduct';
import CONSTS from 'AppData/Constants';
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
            apiType: CONSTS.API_TYPE,
            invoice: {},
            showPopup: false,
        };
        this.handleSubscriptionDelete = this.handleSubscriptionDelete.bind(this);
        this.handleGetPendingInvoice = this.handleGetPendingInvoice.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.updateSubscriptions = this.updateSubscriptions.bind(this);
        this.updateUnsubscribedAPIsList = this.updateUnsubscribedAPIsList.bind(this);
        this.handleSubscribe = this.handleSubscribe.bind(this);
        this.getIdsOfSubscribedEntities = this.getIdsOfSubscribedEntities.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    /**
     *
     *
     * @memberof Subscriptions
     */
    componentDidMount() {
        const { match: { params: { applicationId } } } = this.props;
        const { apiType } = this.state;
        this.updateSubscriptions(applicationId);
        this.updateUnsubscribedAPIsList(apiType);
    }

    /**
     *
     * Get List of the Ids of all APIs and API Products that have been already subscribed
     *
     * @returns {*} Ids of respective APIs and API Products
     * @memberof Subscriptions
     */
    getIdsOfSubscribedEntities() {
        const { subscriptions } = this.state;

        // Get arrays of the API and API Product Ids and remove all null/empty references by executing 'fliter(Boolean)'
        const subscribedAPIIds = subscriptions.map(sub => sub.apiId).filter(Boolean);
        const subscribedAPIProductIds = subscriptions.map(sub => sub.apiProductId).filter(Boolean);

        // We want to treat both API and API Product Ids as a single array of Ids
        const subscribedIds = subscribedAPIIds.concat(subscribedAPIProductIds);

        return subscribedIds;
    }

    handleChange = (event) => {
        const { value } = event.target;
        this.setState({ apiType: value });
        this.updateUnsubscribedAPIsList(value);
    };

    /**
     *
     * Update subscriptions list of Application
     * @param {*} applicationId application id
     * @memberof Subscriptions
     */
    updateSubscriptions(applicationId) {
        const client = new Subscription();
        const promisedSubscriptions = client.getSubscriptions(null, applicationId);
        const { apiType } = this.state;
        promisedSubscriptions
            .then((response) => {
                this.setState({ subscriptions: response.body.list }, this.updateUnsubscribedAPIsList(apiType));
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
                const { subscriptions, apiType } = this.state;
                for (const endpointIndex in subscriptions) {
                    if (Object.prototype.hasOwnProperty.call(subscriptions, endpointIndex)
                        && subscriptions[endpointIndex].subscriptionId === subscriptionId) {
                        subscriptions.splice(endpointIndex, 1);
                        break;
                    }
                }
                this.setState({ subscriptions }, this.updateUnsubscribedAPIsList(apiType));
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
     * Handle closing the popup
     *
     * */
    handleClose() {
        this.setState({ showPopup: false });
    }

    /**
     * Handle view invoice for metered billing
     * @param {*} subscriptionId subscription id
     * @memberof Subscriptions
     */
    handleGetPendingInvoice(subscriptionId) {
        this.setState({ showPopup: true, invoice: {} });
        const client = new Subscription();
        const promisedInvoice = client.getPendingInvoice(subscriptionId);
        promisedInvoice.then((response) => {
            if (response.status !== 200) {
                this.setState({ invoice: response.properties });
            }
            this.setState({ invoice: response.properties });
        }).catch((error) => {
            this.setState({showPopup: true})
        });
    }
    
    /**
     *
     * Update list of unsubscribed APIs
     * @param {string} apiType The type of API being dealt with(API or API Product)
     * @memberof Subscriptions
     */
    updateUnsubscribedAPIsList(apiType) {
        let promisedGetApis = null;

        if (apiType === CONSTS.API_TYPE) {
            const apiClient = new Api();
            promisedGetApis = apiClient.getAllAPIs();
        } else if (apiType === CONSTS.API_PRODUCT_TYPE) {
            const apiClient = new APIProduct();
            promisedGetApis = apiClient.getAllAPIProducts();
        }

        promisedGetApis
            .then((response) => {
                const { list } = response.obj;
                const subscribedIds = this.getIdsOfSubscribedEntities();
                const unsubscribedAPIList = list
                    .filter(api => !subscribedIds.includes(api.id) && !api.advertiseInfo.advertised)
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

        const { apiType } = this.state;

        const promisedSubscribe = api.subscribe(apiId, applicationId, policy, apiType);
        promisedSubscribe
            .then((response) => {
                if (response.status !== 201) {
                    Alert.error(intl.formatMessage({
                        id: 'Applications.Details.Subscriptions.error.occurred.during.subscription.not.201',
                        defaultMessage: 'Error occurred during subscription',
                    }));
                } else {
                    Alert.info(intl.formatMessage({
                        id: 'Applications.Details.Subscriptions.subscription.successful',
                        defaultMessage: 'Subscription successful',
                    }));
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
        const { isAuthorize, showPopup, invoice } = this.state;
        if (!isAuthorize) {
            window.location = '/store-new/services/configs';
        }

        const {
            subscriptions, unsubscribedAPIList, apisNotFound, subscriptionsNotFound, apiType,
        } = this.state;
        const { match: { params: { applicationId } } } = this.props;
        const { classes } = this.props;

        if (subscriptions) {
            return (
                <div className={classes.root}>
                    <Typography variant='headline' className={classes.keyTitle}>
                        <FormattedMessage
                            id='Applications.Details.Subscriptions.subscription.management'
                            defaultMessage='Subscription Management'
                        />
                    </Typography>

                    <div className='apiTypeSelection' align='center'>
                        <FormControl component='fieldset' className={classes.formControl}>
                            <FormLabel component='legend'>API Type</FormLabel>
                            <RadioGroup
                                aria-label='API Type'
                                name='apiType1'
                                className={classes.group}
                                value={apiType}
                                onChange={this.handleChange}
                                row
                            >
                                <FormControlLabel value={CONSTS.API_TYPE} control={<Radio />} label='API' />
                                <FormControlLabel value={CONSTS.API_PRODUCT_TYPE} control={<Radio />} label='API Product' />
                            </RadioGroup>
                        </FormControl>
                    </div>

                    <Grid container className='tab-grid' spacing={16}>
                        <Grid item xs={5} className={classes.cardGrid}>
                            <APIList
                                apisNotFound={apisNotFound}
                                unsubscribedAPIList={unsubscribedAPIList}
                                applicationId={applicationId}
                                handleSubscribe={(app, api, policy) => this.handleSubscribe(app, api, policy)}
                            />
                        </Grid>
                        <Grid item xs={7} xl={10}>
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
                                    {
                                        subscriptionsNotFound
                                            ? (<ResourceNotFound />)
                                            : (
                                                <Table>
                                                    <TableHead>
                                                        <TableRow>
                                                            <TableCell className={classes.firstCell}>
                                                                <FormattedMessage
                                                                    id='Applications.Details.Subscriptions.api.name'
                                                                    defaultMessage='API Name'
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
                                                                    defaultMessage='Status'
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
                                                                <Typography component='p' variant='body1'>
                                                                    <FormattedMessage
                                                                        id='Applications.Details.Subscriptions.SubscriptionsTable.invoice.sub'
                                                                        defaultMessage='(metered billing)'
                                                                    />
                                                                </Typography>
                                                            </TableCell>
                                                        </TableRow>
                                                    </TableHead>
                                                    <TableBody>
                                                        {subscriptions && subscriptions.map((subscription) => {
                                                            return (
                                                                <SubscriptionTableData
                                                                    key={subscription.subscriptionId}
                                                                    subscription={subscription}
                                                                    handleSubscriptionDelete={
                                                                        this.handleSubscriptionDelete
                                                                    }
                                                                    handleGetPendingInvoice={
                                                                        this.handleGetPendingInvoice
                                                                    }
                                                                />
                                                            );
                                                        })}
                                                    </TableBody>
                                                </Table>
                                            )
                                    }
                                </CardContent>
                            </Card>
                        </Grid>
                    </Grid>
                    <Dialog
                        open={showPopup}
                        keepMounted
                        onClose={this.handleClose}
                        aria-labelledby="alert-dialog-slide-title"
                        aria-describedby="alert-dialog-slide-description"
                    >
                        <DialogTitle id="alert-dialog-slide-title">
                            Error
                        </DialogTitle>
                        <DialogContent>
                            <DialogContentText id="alert-dialog-description">
                                Please configure monetization to view invoice!
                            </DialogContentText>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={this.handleClose} color="primary">
                                Close
                            </Button>
                        </DialogActions>
                    </Dialog>
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
