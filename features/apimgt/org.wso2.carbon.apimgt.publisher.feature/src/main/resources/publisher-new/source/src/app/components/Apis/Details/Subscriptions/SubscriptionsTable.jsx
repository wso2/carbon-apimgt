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
import IconButton from '@material-ui/core/IconButton';
import InputBase from '@material-ui/core/InputBase';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import Search from '@material-ui/icons/Search';
import LastPageIcon from '@material-ui/icons/LastPage';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableFooter from '@material-ui/core/TableFooter';
import TableHead from '@material-ui/core/TableHead';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import Tooltip from '@material-ui/core/Tooltip';
import Typography from '@material-ui/core/Typography';
import { FormattedMessage, injectIntl, defineMessages } from 'react-intl';
import withStyles from '@material-ui/core/styles/withStyles';
import PropTypes from 'prop-types';

import Alert from 'AppComponents/Shared/Alert';
import SubscriptionsBlock from 'AppComponents/Apis/Details/Subscriptions/SubscriptionsBlock';
import API from 'AppData/api';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';

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
    searchDiv: {
        float: 'right',
        paddingTop: theme.spacing.unit * 1.25,
        paddingRight: theme.spacing.unit * 1.25,
    },
    searchRoot: {
        paddingTop: theme.spacing.unit * 0.25,
        paddingBottom: theme.spacing.unit * 0.25,
        paddingRight: theme.spacing.unit * 0.5,
        paddingLeft: theme.spacing.unit * 0.5,
        display: 'flex',
        alignItems: 'right',
        width: theme.spacing.unit * 50,
        borderBottom: '1px solid #E8E8E8',
    },
    searchInput: {
        marginLeft: theme.spacing.unit,
        flex: 1,
    },
    searchIconButton: {
        padding: theme.spacing.unit * 1.25,
    },
    noDataMessage: {
        height: theme.spacing.unit * 50,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#888888',
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
                onClick={() => handleFirstPageButtonClick()}
                disabled={page === 0}
            >
                <FirstPageIcon />
            </IconButton>
            <IconButton
                onClick={() => handleBackButtonClick()}
                disabled={page === 0}
            >
                <KeyboardArrowLeft />
            </IconButton>
            <IconButton
                onClick={() => handleNextButtonClick()}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
            >
                <KeyboardArrowRight />
            </IconButton>
            <IconButton
                onClick={() => handleLastPageButtonClick()}
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
            subscriptions: [],
            selectedSubscriptions: [],
            page: 0,
            rowsPerPage: 5,
        };
        this.formatSubscriptionStateString = this.formatSubscriptionStateString.bind(this);
        this.blockSubscription = this.blockSubscription.bind(this);
        this.blockProductionOnly = this.blockProductionOnly.bind(this);
        this.unblockSubscription = this.unblockSubscription.bind(this);
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
        this.filterSubscriptions = this.filterSubscriptions.bind(this);
    }

    componentDidMount() {
        this.fetchSubscriptionData();
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
                const subscriptions = response.obj.list.sort((a, b) => {
                    const x = a.subscriptionId;
                    const y = b.subscriptionId;
                    if (x < y) {
                        return -1;
                    }
                    if (x > y) {
                        return 1;
                    }
                    return 0;
                });
                this.setState({ subscriptions, selectedSubscriptions: subscriptions });
            })
            .catch((errorMessage) => {
                console.log(errorMessage);
                Alert.error(JSON.stringify(errorMessage));
            });
    }

    /**
     * handleChangePage handle change in selected page
     *
     * @param page selected page
     * */
    handleChangePage(page) {
        this.setState({ page });
    }

    /**
     * handleChangeRowsPerPage handle change in rows per page
     *
     * @param event rows per page change event
     * */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value });
    }

    /**
     * Filter subscriptions based on user search value
     *
     * @param event onChange event of user search
     */
    filterSubscriptions(event) {
        const { subscriptions } = this.state;
        let { value } = event.target;
        if (value) {
            value = value.toLowerCase();
            const selectedSubscriptions = subscriptions.filter(sub => (
                (sub.applicationInfo.subscriber).toLowerCase()).includes(value)
                || ((sub.applicationInfo.name).toLowerCase()).includes(value)
                || ((sub.throttlingPolicy).toLowerCase()).includes(value));
            this.setState({ selectedSubscriptions });
        } else {
            this.setState({ selectedSubscriptions: subscriptions });
        }
    }

    render() {
        const {
            subscriptions, selectedSubscriptions, page, rowsPerPage,
        } = this.state;
        const { classes, intl } = this.props;

        if (subscriptions.length > 0) {
            return (
                <div>
                    <Typography className={classes.headline} gutterBottom variant='headline' component='h2'>
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.manage.subscriptions'
                            defaultMessage='Manage Subscriptions'
                        />
                    </Typography>
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
                        <div>
                            <Table className={classes.table}>
                                <colgroup>
                                    <col style={{ width: '15%' }} />
                                    <col style={{ width: '15%' }} />
                                    <col style={{ width: '15%' }} />
                                    <col style={{ width: '15%' }} />
                                    <col style={{ width: '40%' }} />
                                </colgroup>
                                <TableHead>
                                    { tableHeaders }
                                </TableHead>
                                <TableBody className={classes.body}>
                                    {
                                        selectedSubscriptions
                                            .slice(page * rowsPerPage, (page * rowsPerPage) + rowsPerPage)
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
                                                            <SubscriptionsBlock
                                                                subscriptionId={sub.subscriptionId}
                                                                subscriptionStatus={sub.subscriptionStatus}
                                                                blockProductionSubs={this.blockProductionOnly}
                                                                blockAllSubs={this.blockSubscription}
                                                                unblockSubs={this.unblockSubscription}
                                                            />
                                                        </ScopeValidation>
                                                    </TableCell>
                                                </TableRow>
                                            ))
                                    }
                                </TableBody>
                                <TableFooter>
                                    <TableRow>
                                        <TablePagination
                                            rowsPerPageOptions={[5, 10, 25, 50, 100]}
                                            colSpan={5}
                                            count={selectedSubscriptions.length}
                                            rowsPerPage={rowsPerPage}
                                            page={page}
                                            onChangePage={this.handleChangePage}
                                            onChangeRowsPerPage={this.handleChangeRowsPerPage}
                                            ActionsComponent={SubscriptionTablePagination}
                                        />
                                    </TableRow>
                                </TableFooter>
                            </Table>
                        </div>
                    </Paper>
                </div>
            );
        } else {
            return (
                <div>
                    <Paper className={classes.noDataMessage}>
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.no.subscriptions'
                            defaultMessage='No subscriptions available'
                        />
                    </Paper>
                </div>
            );
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
