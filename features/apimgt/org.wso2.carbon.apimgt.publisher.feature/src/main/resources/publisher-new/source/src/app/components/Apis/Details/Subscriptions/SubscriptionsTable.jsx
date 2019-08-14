import React, { Component } from 'react';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl, defineMessages } from 'react-intl';
import withStyles from '@material-ui/core/styles/withStyles';
import PropTypes from 'prop-types';

import API from 'AppData/api';
import ApiPermissionValidation from 'AppData/ApiPermissionValidation';
import Alert from 'AppComponents/Shared/Alert';

const styles = theme => ({
    button: {
        margin: theme.spacing.unit,
    },
    headline: { paddingTop: theme.spacing.unit * 1.25, paddingLeft: theme.spacing.unit * 2.5 },
    table: {
        '& td': {
            fontSize: theme.typography.fontSize,
        },
        '& th': {
            fontSize: theme.typography.fontSize * 1.2,
        },
    },
});

/**
 * Lists all subscriptions.
 *
 * @class SubscriptionsTable
 * @extends {React.Component}
 */
class SubscriptionsTable extends Component {
    constructor(props) {
        super(props);
        this.api = props.api;
        this.state = {
            subscriptions: null,
        };
        this.formatSubscriptionStateString = this.formatSubscriptionStateString.bind(this);
        this.blockSubscription = this.blockSubscription.bind(this);
        this.blockProductionOnly = this.blockProductionOnly.bind(this);
        this.unblockSubscription = this.unblockSubscription.bind(this);
    }

    componentDidMount() {
        this.fetchSubscriptionData();
    }

    /**
     * Returns the set of action buttons based on the current subscription state
     *
     * @param {*} state State of the subscription (PROD_ONLY_BLOCKED/BLOCKED/ACTIVE)
     * @param {*} subscriptionId Subscription ID
     * @returns {JSX} Action buttons in JSX
     * @memberof SubscriptionsTable
     */
    getSubscriptionBlockingButtons(state, subscriptionId) {
        const { classes } = this.props;
        if (state === 'PROD_ONLY_BLOCKED') {
            return (
                <dev>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockProductionOnly(subscriptionId)}
                        className={classes.button}
                        disabled='true'
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.production.only.state.prod.only'
                            defaultMessage='Block Production Only'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockSubscription(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.all.state.prod.only'
                            defaultMessage='Block All'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.unblockSubscription(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.unblock.state.prod.only'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>
            );
        } else if (state === 'BLOCKED') {
            return (
                <dev>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockProductionOnly(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.production.only.state.blocked'
                            defaultMessage='Block Production Only'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockSubscription(subscriptionId)}
                        className={classes.button}
                        disabled='true'
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.all.state.blocked'
                            defaultMessage='Block All'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.unblockSubscription(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.unblock.state.blocked'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>
            );
        } else {
            return (
                <dev>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockProductionOnly(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.production.only.active'
                            defaultMessage='Block Production Only'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockSubscription(subscriptionId)}
                        className={classes.button}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.block.all.active'
                            defaultMessage='Block All'
                        />
                    </Button>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.unblockSubscription(subscriptionId)}
                        className={classes.button}
                        disabled='true'
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.unblock.active'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>
            );
        }
    }

    /**
     * Returns subscription state string based on te current subscription state
     *
     * @param {*} state The current state of subscription
     * @returns {JSX} Subscription state string
     * @memberof SubscriptionsTable
     */
    formatSubscriptionStateString(state) {
        if (state === 'PROD_ONLY_BLOCKED') {
            return (<FormattedMessage
                id='Apis.Details.Subscriptions.SubscriptionsTable.blocked.production.only.subs.state'
                defaultMessage='Blocked Production Only'
            />);
        } else if (state === 'BLOCKED') {
            return (<FormattedMessage
                id='Apis.Details.Subscriptions.SubscriptionsTable.blocked.subs.state'
                defaultMessage='Blocked'
            />);
        } else {
            return (<FormattedMessage
                id='Apis.Details.Subscriptions.SubscriptionsTable.active.subs.state'
                defaultMessage='Active'
            />);
        }
    }

    /**
     * Blocks the given subscription
     *
     * @param {*} subscriptionId Subscription ID
     * @memberof SubscriptionsTable
     */
    blockSubscription(subscriptionId) {
        const { intl } = this.props;
        const api = new API();
        const promisedSubscriptionUpdate = api.blockSubscriptions(subscriptionId, 'BLOCKED');
        promisedSubscriptionUpdate
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscription.blocked',
                    defaultMessage: 'Subscription was blocked.',
                }));
                this.fetchSubscriptionData();
            })
            .catch((errorResponse) => {
                console.log(errorResponse);
                const { message } = errorResponse.response.body;
                const messages = defineMessages({
                    errorMessage: {
                        id: 'Apis.Details.Subscriptions.SubscriptionsTable.error.subscription.block',
                        defaultMessage: 'Error: Unable to block subscription. (Reason: {message})',
                    },
                });
                Alert.error(intl.formatMessage(messages.errorMessage, { message }));
            });
    }

    /**
     * Blocks production only for the given subscription
     *
     * @param {*} subscriptionId Subscription ID
     * @memberof SubscriptionsTable
     */
    blockProductionOnly(subscriptionId) {
        const { intl } = this.props;
        const api = new API();
        const promisedSubscriptionUpdate = api.blockSubscriptions(subscriptionId, 'PROD_ONLY_BLOCKED');
        promisedSubscriptionUpdate
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscription.blocked.prod.only',
                    defaultMessage: 'Subscription was blocked for production only.',
                }));
                this.fetchSubscriptionData();
            })
            .catch((errorResponse) => {
                console.log(errorResponse);
                const { message } = errorResponse.response.body;
                const messages = defineMessages({
                    errorMessage: {
                        id: 'Apis.Details.Subscriptions.SubscriptionsTable.error.subscription.block.prod.only',
                        defaultMessage: 'Error: Unable to block subscription. (Reason: {message})',
                    },
                });
                Alert.error(intl.formatMessage(messages.errorMessage, { message }));
            });
    }

    /**
     * Unblocks the given subscription
     *
     * @param {*} subscriptionId Subscription ID
     * @memberof SubscriptionsTable
     */
    unblockSubscription(subscriptionId) {
        const { intl } = this.props;
        const api = new API();
        const promisedSubscriptionUpdate = api.unblockSubscriptions(subscriptionId);
        promisedSubscriptionUpdate
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscription.unblocked',
                    defaultMessage: 'Subscription was unblocked.',
                }));
                this.fetchSubscriptionData();
            })
            .catch((errorResponse) => {
                console.log(errorResponse);
                const { message } = errorResponse.response.body;
                const messages = defineMessages({
                    errorMessage: {
                        id: 'Apis.Details.Subscriptions.SubscriptionsTable.error.subscription.unblock',
                        defaultMessage: 'Error: Unable to unblock subscription. (Reason: {message})',
                    },
                });
                Alert.error(intl.formatMessage(messages.errorMessage, { message }));
            });
    }

    /**
     * Fetches subscription data
     *
     * @memberof SubscriptionsTable
     */
    fetchSubscriptionData() {
        const api = new API();
        const promisedSubscriptions = api.subscriptions(this.api.id);
        promisedSubscriptions
            .then((response) => {
                this.setState({ subscriptions: response.obj.list });
            })
            .catch((errorMessage) => {
                console.log(errorMessage);
                Alert.error(JSON.stringify(errorMessage));
            });
    }

    render() {
        const { subscriptions } = this.state;
        const { api } = this;
        const { classes } = this.props;
        console.info(subscriptions);
        console.info(api);
        if (subscriptions != null) {
            return (
                <dev>
                    <Typography className={classes.headline} gutterBottom variant='h5' component='h2'>
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.manage.subscriptions'
                            defaultMessage='Manage Subscriptions'
                        />
                    </Typography>
                    <Table className={classes.table}>
                        <colgroup>
                            <col style={{ width: '15%' }} />
                            <col style={{ width: '15%' }} />
                            <col style={{ width: '15%' }} />
                            <col style={{ width: '15%' }} />
                            <col style={{ width: '40%' }} />
                        </colgroup>
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    <FormattedMessage
                                        id='Apis.Details.Subscriptions.SubscriptionsTable.subscriber'
                                        defaultMessage='Subscriber'
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id='Apis.Details.Subscriptions.SubscriptionsTable.application'
                                        defaultMessage='Application'
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id='Apis.Details.Subscriptions.SubscriptionsTable.tier'
                                        defaultMessage='Tier'
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id='Apis.Details.Subscriptions.SubscriptionsTable.status'
                                        defaultMessage='Status'
                                    />
                                </TableCell>
                                <TableCell>
                                    <FormattedMessage
                                        id='Apis.Details.Subscriptions.SubscriptionsTable.actions'
                                        defaultMessage='Actions'
                                    />
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody className={classes.body}>
                            {subscriptions
                                .sort((a, b) => {
                                    const x = a.subscriptionId;
                                    const y = b.subscriptionId;
                                    if (x < y) {
                                        return -1;
                                    }
                                    if (x > y) {
                                        return 1;
                                    }
                                    return 0;
                                })
                                .map((row) => {
                                    return (
                                        <TableRow key={row.subscriptionId}>
                                            <TableCell>{row.applicationInfo.subscriber}</TableCell>
                                            <TableCell>{row.applicationInfo.name}</TableCell>
                                            <TableCell>{row.policy}</TableCell>
                                            <TableCell>
                                                {this.formatSubscriptionStateString(row.subscriptionStatus)}
                                            </TableCell>
                                            <TableCell>
                                                <div>
                                                    <ApiPermissionValidation
                                                        userPermissions={api.userPermissionsForApi}
                                                    >
                                                        <ApiPermissionValidation
                                                            checkingPermissionType={
                                                                ApiPermissionValidation.permissionType
                                                                    .MANAGE_SUBSCRIPTION
                                                            }
                                                            userPermissions={api.userPermissionsForApi}
                                                        >
                                                            {this.getSubscriptionBlockingButtons(
                                                                row.subscriptionStatus,
                                                                row.subscriptionId,
                                                            )}
                                                        </ApiPermissionValidation>
                                                    </ApiPermissionValidation>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                        </TableBody>
                    </Table>
                </dev>
            );
        } else {
            return null;
        }
    }
}

SubscriptionsTable.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    api: PropTypes.shape({
        id: PropTypes.string,
    }).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(SubscriptionsTable));
