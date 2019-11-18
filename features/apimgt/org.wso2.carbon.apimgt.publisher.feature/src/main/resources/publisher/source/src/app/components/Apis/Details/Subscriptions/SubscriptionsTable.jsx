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
import { FormattedMessage, injectIntl, defineMessages } from 'react-intl';
import IconButton from '@material-ui/core/IconButton';
import Button from '@material-ui/core/Button';
import InputBase from '@material-ui/core/InputBase';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import Search from '@material-ui/icons/Search';
import LastPageIcon from '@material-ui/icons/LastPage';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableFooter from '@material-ui/core/TableFooter';
import TableHead from '@material-ui/core/TableHead';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import Tooltip from '@material-ui/core/Tooltip';
import withStyles from '@material-ui/core/styles/withStyles';
import PropTypes from 'prop-types';

import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import AuthManager from 'AppData/AuthManager';
import Invoice from './Invoice';

const styles = theme => ({
    button: {
        margin: theme.spacing(1),
    },
    headline: { paddingTop: theme.spacing(1.25), paddingLeft: theme.spacing(2.5) },
    popupHeadline: {
        alignItems: 'center',
        borderBottom: '2px solid #40E0D0',
        textAlign: 'center',
    },
    table: {
        '& td': {
            fontSize: theme.typography.fontSize,
        },
        '& th': {
            fontSize: theme.typography.fontSize * 1.2,
        },
    },
    searchDiv: {
        float: 'right',
        paddingTop: theme.spacing(1.25),
        paddingRight: theme.spacing(1.25),
    },
    searchRoot: {
        paddingTop: theme.spacing(0.25),
        paddingBottom: theme.spacing(0.25),
        paddingRight: theme.spacing(0.5),
        paddingLeft: theme.spacing(0.5),
        display: 'flex',
        alignItems: 'right',
        width: theme.spacing(50),
        borderBottom: '1px solid #E8E8E8',
    },
    searchInput: {
        marginLeft: theme.spacing(1),
        flex: 1,
    },
    searchIconButton: {
        padding: theme.spacing(1.25),
    },
    noDataMessage: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#888888',
        width: '100%',
    },
    tableColumnSize: {
        width: '14%',
    },
    tableColumnSize2: {
        width: '30%',
    },
    dialogColumnSize: {
        width: '50%',
    },
    dialog: {
        float: 'center',
        alignItems: 'center',
    },
    invoiceTable: {
        '& td': {
            fontSize: theme.typography.fontSize * 1.5,
        },
    },
    uniqueCell: {
        borderTop: '1px solid #000000',
        fontWeight: 'bold',
    },
    mainTitle: {
        paddingLeft: 0,
        marginTop: theme.spacing(3),
    },
    titleWrapper: {
        marginBottom: theme.spacing(3),
    },
});

const tableHeaders = (
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
            <ScopeValidation
                resourceMethod={resourceMethod.POST}
                resourcePath={resourcePath.BLOCK_SUBSCRIPTION}
            >
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionsTable.actions'
                    defaultMessage='Actions'
                />
            </ScopeValidation>
        </TableCell>
        <Tooltip title='Only for Usage based plans'>
            <TableCell>
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionsTable.invoice.heading'
                    defaultMessage='Invoice'
                />
            </TableCell>
        </Tooltip>
    </TableRow>
);

const subscriptionStatus = {
    BLOCKED: 'BLOCKED',
    PROD_BLOCKED: 'PROD_ONLY_BLOCKED',
};

/**
 * Table pagination for subscriptions table
 *
 * @param props props used for SubscriptionTablePagination
 * @returns {*}
 */
function SubscriptionTablePagination(props) {
    const {
        count, page, rowsPerPage, onChangePage,
    } = props;

    /**
     * handleFirstPageButtonClick loads data of the first page
     * */
    function handleFirstPageButtonClick() {
        if (onChangePage) {
            onChangePage(0);
        }
    }

    /**
     * handleBackButtonClick load data of the prev page
     * */
    function handleBackButtonClick() {
        if (onChangePage) {
            onChangePage(page - 1);
        }
    }

    /**
     * handleNextButtonClick load data of the next page
     * */
    function handleNextButtonClick() {
        if (onChangePage) {
            onChangePage(page + 1);
        }
    }

    /**
     * handleLastPageButtonClick load data of the last page
     * */
    function handleLastPageButtonClick() {
        if (onChangePage) {
            onChangePage(Math.max(0, Math.ceil(count / rowsPerPage) - 1));
        }
    }

    return (
        <div
            style={{ display: 'flex' }}
        >
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
            >
                <FirstPageIcon />
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
            >
                <KeyboardArrowLeft />
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
            >
                <KeyboardArrowRight />
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
            >
                <LastPageIcon />
            </IconButton>
        </div>
    );
}

SubscriptionTablePagination.propTypes = {
    count: PropTypes.number.isRequired,
    page: PropTypes.number.isRequired,
    rowsPerPage: PropTypes.number.isRequired,
    onChangePage: PropTypes.func.isRequired,
};

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
            totalSubscription: 0,
            page: 0,
            rowsPerPage: 5,
            searchQuery: null,
            rowsPerPageOptions: [5, 10, 25, 50, 100],
            emptyColumnHeight: 60,
            policies: [],
        };
        this.formatSubscriptionStateString = this.formatSubscriptionStateString.bind(this);
        this.blockSubscription = this.blockSubscription.bind(this);
        this.blockProductionOnly = this.blockProductionOnly.bind(this);
        this.unblockSubscription = this.unblockSubscription.bind(this);
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
        this.filterSubscriptions = this.filterSubscriptions.bind(this);
        this.isMonetizedPolicy = this.isMonetizedPolicy.bind(this);
        this.isNotCreator = AuthManager.isNotCreator();
        this.isNotPublisher = AuthManager.isNotPublisher();
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
        if (state === subscriptionStatus.PROD_BLOCKED) {
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
                            id='block.production.only'
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
                            id='block.all'
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
                            id='unblock'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>);
        } else if (state === subscriptionStatus.BLOCKED) {
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
                            id='block.production.only'
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
                            id='block.all'
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
                            id='unblock'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>);
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
                            id='block.production.only'
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
                            id='block.all'
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
                            id='unblock'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>);
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
        if (state === subscriptionStatus.PROD_BLOCKED) {
            return (<FormattedMessage
                id='Apis.Details.Subscriptions.SubscriptionsTable.blocked.production.only.subs.state'
                defaultMessage='Blocked Production Only'
            />);
        } else if (state === subscriptionStatus.BLOCKED) {
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
        const promisedSubscriptionUpdate = api.blockSubscriptions(subscriptionId, subscriptionStatus.BLOCKED);
        promisedSubscriptionUpdate
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscription.blocked',
                    defaultMessage: 'Subscription was blocked.',
                }));
                this.fetchSubscriptionData();
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
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
        const promisedSubscriptionUpdate = api.blockSubscriptions(subscriptionId, subscriptionStatus.PROD_BLOCKED);
        promisedSubscriptionUpdate
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscription.blocked.prod.only',
                    defaultMessage: 'Subscription was blocked for production only.',
                }));
                this.fetchSubscriptionData();
            })
            .catch((errorResponse) => {
                console.error(errorResponse);
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
                console.error(errorResponse);
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
        const { page, rowsPerPage, searchQuery } = this.state;
        const promisedSubscriptions = api.subscriptions(this.api.id, page * rowsPerPage, rowsPerPage, searchQuery);
        promisedSubscriptions
            .then(response =>
                this.setState({
                    subscriptions: response.body.list,
                    totalSubscription: response.body.pagination.total,
                }))
            .catch((errorMessage) => {
                console.error(errorMessage);
                Alert.error(JSON.stringify(errorMessage));
            });
        api.getMonetization(this.props.api.id).then((status) => {
            this.setState({ monetizationStatus: status.enabled });
        });
        api.getSubscriptionPolicies(this.api.id).then((policies) => {
            const filteredPolicies = policies.filter(policy => policy.tierPlan === 'COMMERCIAL');
            this.setState({ policies: filteredPolicies });
        });
    }

    /**
     * handleChangePage handle change in selected page
     *
     * @param page selected page
     * */
    handleChangePage(page) {
        this.setState({ page }, this.fetchSubscriptionData);
    }

    /**
     * Checks whether the policy is a usage based monetization plan
     *
     * */
    isMonetizedPolicy(policyName) {
        const { policies, monetizationStatus } = this.state;
        if (policies.length > 0) {
            const filteredPolicies = policies.filter(policy =>
                policy.name === policyName && policy.monetizationAttributes.pricePerRequest != null);
            return (filteredPolicies.length > 0 && monetizationStatus);
        } else {
            return false;
        }
    }

    /**
     * handleChangeRowsPerPage handle change in rows per page
     *
     * @param event rows per page change event
     * */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value, page: 0 }, this.fetchSubscriptionData);
    }

    /**
     * Filter subscriptions based on user search value
     *
     * @param event onChange event of user search
     */
    filterSubscriptions(event) {
        this.setState({ searchQuery: event.target.value }, this.fetchSubscriptionData);
    }

    render() {
        const {
            subscriptions, page, rowsPerPage, totalSubscription, rowsPerPageOptions, emptyColumnHeight,
        } = this.state;
        const { classes, intl, api } = this.props;
        if (!subscriptions) {
            return (
                <Grid container direction='row' justify='center' alignItems='center'>
                    <Grid item>
                        <CircularProgress />
                    </Grid>
                </Grid>
            );
        }
        return (
            <React.Fragment>
                <Paper>
                    <Tooltip
                        title={intl.formatMessage({
                            id: 'Apis.Details.Subscriptions.SubscriptionsTable.search.tooltip',
                            defaultMessage: 'Search subscriptions by Subscriber, Application and Tier',
                        })}
                        aria-label='Search tooltip'
                    >
                        <div className={classes.searchDiv}>
                            <div className={classes.searchRoot}>
                                <InputBase
                                    className={classes.searchInput}
                                    placeholder={intl.formatMessage({
                                        id: 'Apis.Details.Subscriptions.SubscriptionsTable.search',
                                        defaultMessage: 'Search',
                                    })}
                                    inputProps={{ 'aria-label': 'Search' }}
                                    onChange={e => this.filterSubscriptions(e)}
                                />
                                <IconButton className={classes.searchIconButton} aria-label='Search' disabled>
                                    <Search />
                                </IconButton>
                            </div>
                        </div>
                    </Tooltip>
                    {subscriptions.length > 0 ? (
                        <div>
                            <Table className={classes.table}>
                                <colgroup>
                                    <col className={classes.tableColumnSize} />
                                    <col className={classes.tableColumnSize} />
                                    <col className={classes.tableColumnSize} />
                                    <col className={classes.tableColumnSize} />
                                    <col className={classes.tableColumnSize2} />
                                    <col className={classes.tableColumnSize} />
                                </colgroup>
                                <TableHead>
                                    {tableHeaders}
                                </TableHead>
                                <TableBody>
                                    {subscriptions
                                        .map(sub => (
                                            <TableRow key={sub.subscriptionId}>
                                                <TableCell>{sub.applicationInfo.subscriber}</TableCell>
                                                <TableCell>{sub.applicationInfo.name}</TableCell>
                                                <TableCell>{sub.throttlingPolicy}</TableCell>
                                                <TableCell>
                                                    {this.formatSubscriptionStateString(sub.subscriptionStatus)}
                                                </TableCell>
                                                <TableCell>
                                                    <ScopeValidation
                                                        resourceMethod={resourceMethod.POST}
                                                        resourcePath={resourcePath.BLOCK_SUBSCRIPTION}
                                                    >
                                                        {
                                                            this.getSubscriptionBlockingButtons(
                                                                sub.subscriptionStatus,
                                                                sub.subscriptionId,
                                                            )
                                                        }
                                                    </ScopeValidation>
                                                </TableCell>
                                                <TableCell>
                                                    <Invoice
                                                        subscriptionId={sub.subscriptionId}
                                                        isNotAuthorized={this.isNotCreator && this.isNotPublisher}
                                                        isMonetizedUsagePolicy={
                                                            this.isMonetizedPolicy(sub.throttlingPolicy)}
                                                        api={api}
                                                    />
                                                </TableCell>
                                            </TableRow>
                                        ))
                                    }
                                </TableBody>
                                <TableFooter>
                                    <TableRow>
                                        <TablePagination
                                            rowsPerPageOptions={rowsPerPageOptions}
                                            colSpan={6}
                                            count={totalSubscription}
                                            rowsPerPage={rowsPerPage}
                                            page={page}
                                            onChangePage={this.handleChangePage}
                                            onChangeRowsPerPage={this.handleChangeRowsPerPage}
                                            ActionsComponent={SubscriptionTablePagination}
                                        />
                                    </TableRow>
                                </TableFooter>
                            </Table>
                        </div>) :
                        (
                            <div className={classes.noDataMessage} style={{ height: rowsPerPage * emptyColumnHeight }}>
                                <FormattedMessage
                                    id='Apis.Details.Subscriptions.SubscriptionsTable.no.subscriptions'
                                    defaultMessage='No subscriptions data available'
                                />
                            </div>
                        )
                    }
                </Paper>
            </React.Fragment>
        );
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
