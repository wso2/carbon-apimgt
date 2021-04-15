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
import Box from '@material-ui/core/Box';
import FirstPageIcon from '@material-ui/icons/FirstPage';
import KeyboardArrowLeft from '@material-ui/icons/KeyboardArrowLeft';
import KeyboardArrowRight from '@material-ui/icons/KeyboardArrowRight';
import LastPageIcon from '@material-ui/icons/LastPage';
import Paper from '@material-ui/core/Paper';
import CircularProgress from '@material-ui/core/CircularProgress';
import Grid from '@material-ui/core/Grid';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import TablePagination from '@material-ui/core/TablePagination';
import Tooltip from '@material-ui/core/Tooltip';
import withStyles from '@material-ui/core/styles/withStyles';
import Typography from '@material-ui/core/Typography';
import PropTypes from 'prop-types';
import MUIDataTable from 'mui-datatables';
import InfoIcon from '@material-ui/icons/Info';
import UserIcon from '@material-ui/icons/Person';


import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import { ScopeValidation, resourceMethod, resourcePath } from 'AppData/ScopeValidation';
import AuthManager from 'AppData/AuthManager';
import Invoice from './Invoice';

const styles = (theme) => ({
    heading: {
        marginTop: theme.spacing(3),
        marginBottom: theme.spacing(2),
    },
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
    typography: {
        padding: theme.spacing(2),
    },
    root: {
        flexGrow: 1,
    },
    InfoToolTip: {
        backgroundColor: theme.custom.disableColor,
        color: theme.palette.getContrastText(theme.custom.disableColor),
        fontSize: theme.typography.fontSize,
        fontWeight: theme.typography.h6.fontWeight,
        border: 'solid 1px ' + theme.palette.grey,
        borderRadius: theme.shape.borderRadius,
        padding: theme.spacing(2),
    },
    subscriberHeader: {
        fontSize: theme.typography.h6.fontSize,
        color: theme.typography.h6.color,
        fontWeight: theme.typography.h6.fontWeight,
    },
});

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
            subscriberClaims: null,
        };
        this.formatSubscriptionStateString = this.formatSubscriptionStateString.bind(this);
        this.blockSubscription = this.blockSubscription.bind(this);
        this.blockProductionOnly = this.blockProductionOnly.bind(this);
        this.unblockSubscription = this.unblockSubscription.bind(this);
        this.handleChangePage = this.handleChangePage.bind(this);
        this.handleChangeRowsPerPage = this.handleChangeRowsPerPage.bind(this);
        this.filterSubscriptions = this.filterSubscriptions.bind(this);
        this.isMonetizedPolicy = this.isMonetizedPolicy.bind(this);
        this.renderClaims = this.renderClaims.bind(this);
        this.isNotCreator = AuthManager.isNotCreator();
        this.isNotPublisher = AuthManager.isNotPublisher();
    }

    componentDidMount() {
        this.fetchSubscriptionData();
    }

    // TODO: This is a React anti-pattern, have to move this to a component ~tmkb
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
                        disabled={this.api.isRevision}
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
                        disabled={this.api.isRevision}
                    >
                        <FormattedMessage
                            id='unblock'
                            defaultMessage='Unblock'
                        />
                    </Button>
                </dev>
            );
        } else if (state === subscriptionStatus.BLOCKED) {
            return (
                <dev>
                    <Button
                        size='small'
                        variant='outlined'
                        color='primary'
                        onClick={() => this.blockProductionOnly(subscriptionId)}
                        className={classes.button}
                        disabled={this.api.isRevision}
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
                        disabled={this.api.isRevision}
                    >
                        <FormattedMessage
                            id='unblock'
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
                        disabled={this.api.isRevision}
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
                        disabled={this.api.isRevision}
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
                </dev>
            );
        }
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
     * handleChangeRowsPerPage handle change in rows per page
     *
     * @param event rows per page change event
     * */
    handleChangeRowsPerPage(event) {
        this.setState({ rowsPerPage: event.target.value, page: 0 }, this.fetchSubscriptionData);
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
            return (
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionsTable.blocked.production.only.subs.state'
                    defaultMessage='Blocked Production Only'
                />
            );
        } else if (state === subscriptionStatus.BLOCKED) {
            return (
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionsTable.blocked.subs.state'
                    defaultMessage='Blocked'
                />
            );
        } else {
            return (
                <FormattedMessage
                    id='Apis.Details.Subscriptions.SubscriptionsTable.active.subs.state'
                    defaultMessage='Active'
                />
            );
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
        const { intl } = this.props;
        const promisedSubscriptions = api.subscriptions(this.api.id, page * rowsPerPage, rowsPerPage, searchQuery);
        promisedSubscriptions
            .then((response) => {
                for (let i = 0; i < response.body.list.length; i++) {
                    const { subscriptionId } = response.body.list[i];
                    response.body.list[i].name = response.body.list[i].applicationInfo.name;
                    const promisedInfo = api.getSubscriberInfo(subscriptionId);
                    promisedInfo
                        .then((resp) => {
                            this.setState((prevState) => ({
                                subscriberClaims: {
                                    ...prevState.subscriberClaims,
                                    [subscriptionId]: resp.body,
                                },
                            }));
                        })
                        .catch((errorMessage) => {
                            console.error(errorMessage);
                            Alert.error(intl.formatMessage({
                                id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscriber.info.error',
                                defaultMessage: 'Error while retrieving the subscriber information',
                            }));
                        });
                }
                this.setState({
                    subscriptions: response.body.list,
                    totalSubscription: response.body.pagination.total,
                });
            })
            .catch((errorMessage) => {
                console.error(errorMessage);
                Alert.error(intl.formatMessage({
                    id: 'Apis.Details.Subscriptions.SubscriptionsTable.subscriptions.error',
                    defaultMessage: 'Error while retrieving the subscriptions',
                }));
            });
        api.getMonetization(this.props.api.id).then((status) => {
            this.setState({ monetizationStatus: status.enabled });
        });
        api.getSubscriptionPolicies(this.api.id).then((policies) => {
            const filteredPolicies = policies.filter((policy) => policy.tierPlan === 'COMMERCIAL');
            this.setState({ policies: filteredPolicies });
        });
    }

    /**
     * Checks whether the policy is a usage based monetization plan
     *
     * */
    isMonetizedPolicy(policyName) {
        const { policies, monetizationStatus } = this.state;
        if (policies.length > 0) {
            const filteredPolicies = policies.filter(
                (policy) => policy.name === policyName && policy.monetizationAttributes.pricePerRequest != null,
            );
            return (filteredPolicies.length > 0 && monetizationStatus);
        } else {
            return false;
        }
    }

    /**
     * Filter subscriptions based on user search value
     *
     * @param event onChange event of user search
     */
    filterSubscriptions(event) {
        this.setState({ searchQuery: event.target.value }, this.fetchSubscriptionData);
    }

    /**
     * Render claims based on the claim object
     */
    renderClaims(claimsObject) {
        const { classes } = this.props;
        if (claimsObject) {
            return (
                <div className={classes.root}>
                    {claimsObject.name}
                    <Grid container spacing={1}>
                        <Grid item>
                            <UserIcon color='primary' />
                        </Grid>
                        <Grid item>
                            {claimsObject.name}
                        </Grid>
                    </Grid>
                    {claimsObject.claims && (
                        <div>
                            <Table className={classes.table}>
                                <TableBody>
                                    {claimsObject.claims.map((claim) => (
                                        <TableRow hover>
                                            <TableCell>{claim.name}</TableCell>
                                            {claim.value ? (
                                                <TableCell>{claim.value}</TableCell>
                                            ) : (
                                                <TableCell>Not Available</TableCell>
                                            )}
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </div>
            );
        }
        return (
            <div>
                <Typography className={classes.typography}>
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Subscriber.no.claims'
                        defaultMessage='No subscriber claims data available'
                    />
                </Typography>
            </div>
        );
    }

    /**
     *
     */
    render() {
        const {
            subscriptions, page, rowsPerPage, totalSubscription, rowsPerPageOptions, emptyColumnHeight,
            subscriberClaims,
        } = this.state;
        const { classes, api } = this.props;
        if (!subscriptions) {
            return (
                <Grid container direction='row' justify='center' alignItems='center'>
                    <Grid item>
                        <CircularProgress />
                    </Grid>
                </Grid>
            );
        }
        const columns = [
            {
                name: 'subscriptionId',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'applicationInfo.applicationId',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'applicationInfo.subscriber',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.subscriber'
                        defaultMessage='Subscriber'
                    />
                ),
                options: {
                    sort: false,
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            let claimsObject;
                            if (subscriberClaims) {
                                claimsObject = subscriberClaims[tableMeta.rowData[0]];
                            }
                            return (
                                <Box display='flex'>
                                    <Box pr={1}>
                                        {subscriberClaims && claimsObject && claimsObject.name}
                                    </Box>
                                    <Tooltip
                                        interactive
                                        placement='top'
                                        classes={{
                                            tooltip: classes.InfoToolTip,
                                        }}
                                        title={(
                                            <>
                                                {subscriberClaims && (
                                                    <div>
                                                        {this.renderClaims(claimsObject)}
                                                    </div>
                                                )}
                                            </>
                                        )}
                                    >
                                        <Grid container direction='row' alignItems='center' spacing={1}>
                                            <Grid item>
                                                <Typography>
                                                    <InfoIcon color='action' />
                                                </Typography>
                                            </Grid>
                                            <Grid item>
                                                {value}
                                            </Grid>
                                        </Grid>
                                    </Tooltip>
                                </Box>
                            );
                        }
                        return null;
                    },
                },
            },
            {
                name: 'name',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.application'
                        defaultMessage='Application'
                    />
                ),
                options: {
                    sort: false,
                },
            },
            {
                name: 'applicationInfo.description',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'applicationInfo.subscriptionCount',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'throttlingPolicy',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.throttling.tier'
                        defaultMessage='Tier'
                    />
                ),
                options: {
                    sort: false,
                },
            },
            {
                name: 'subscriptionStatus',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.subscription.status'
                        defaultMessage='Status'
                    />
                ),
                options: {
                    sort: false,
                },
            },
            {
                name: 'actions',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.subscription.actions'
                        defaultMessage='Actions'
                    />
                ),
                options: {
                    sort: false,
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const status = tableMeta.rowData[7];
                            const subscriptionId = tableMeta.rowData[0];
                            return (
                                <ScopeValidation
                                    resourceMethod={resourceMethod.POST}
                                    resourcePath={resourcePath.BLOCK_SUBSCRIPTION}
                                >
                                    {
                                        this.getSubscriptionBlockingButtons(
                                            status,
                                            subscriptionId,
                                        )
                                    }
                                </ScopeValidation>
                            );
                        }
                        return null;
                    },
                },
            },
            {
                name: 'invoice',
                label: (
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.Listing.column.header.subscription.invoice'
                        defaultMessage='Invoice'
                    />
                ),
                options: {
                    sort: false,
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            const throttlingPolicy = tableMeta.rowData[6];
                            const subscriptionId = tableMeta.rowData[0];
                            return (
                                <Invoice
                                    subscriptionId={subscriptionId}
                                    isNotAuthorized={this.isNotCreator && this.isNotPublisher}
                                    isMonetizedUsagePolicy={
                                        this.isMonetizedPolicy(throttlingPolicy)
                                    }
                                    api={api}
                                />
                            );
                        }
                        return null;
                    },
                },
            },
        ];

        const options = {
            title: false,
            print: false,
            download: false,
            viewColumns: false,
            elevation: 1,
            customToolbar: false,
            search: false,
            selectableRows: 'none',
            rowsPerPageOptions: [5, 10, 25, 50, 100],
            customFooter: () => {
                return (
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
                );
            },
        };
        const subMails = {};
        const emails = subscriberClaims && Object.entries(subscriberClaims).map(([, v]) => {
            let email = null;
            if (!subMails[v.name]) {
                email = v.claims.find((claim) => claim.uri === 'http://wso2.org/claims/emailaddress').value;
                subMails[v.name] = email;
            }
            return email;
        }).reduce((a, b) => {
            return b !== null ? `${a || ''},${b}` : a;
        });
        let names = null;
        if (subMails) {
            Object.entries(subMails).map(([k, v]) => {
                if (v) {
                    if (names === null) {
                        names = k;
                    } else {
                        names = `${names}, ${k}`;
                    }
                }
                return null;
            });
        }
        const Tip = names ? React.Fragment : Tooltip;
        return (
            <>
                <div className={classes.heading}>
                    <Typography variant='h4'>
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.manage.subscriptions'
                            defaultMessage='Manage Subscriptions'
                        />
                        {'   '}
                        {subscriptions.length > 0 && (
                            <Tip title='No contact details' placement='top'>
                                <span>
                                    <Button
                                        target='_blank'
                                        rel='noopener'
                                        href={`mailto:?subject=Message from the API Publisher&cc=${emails}`
                                            + `&body=Hi ${names},`}
                                        size='small'
                                        disabled={!names}
                                        variant='outlined'
                                    >
                                        Contact Subscribers
                                    </Button>
                                </span>
                            </Tip>
                        )}
                    </Typography>
                    <Typography variant='caption' gutterBottom>
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.SubscriptionsTable.sub.heading'
                            defaultMessage='Manage subscriptions of the API'
                        />
                    </Typography>
                </div>
                <Paper elevation={0}>
                    {subscriptions.length > 0 ? (
                        <div>
                            <MUIDataTable title='' data={subscriptions} columns={columns} options={options} />
                        </div>
                    )
                        : (
                            <div className={classes.noDataMessage} style={{ height: rowsPerPage * emptyColumnHeight }}>
                                <FormattedMessage
                                    id='Apis.Details.Subscriptions.SubscriptionsTable.no.subscriptions'
                                    defaultMessage='No subscriptions data available'
                                />
                            </div>
                        )}
                </Paper>
            </>
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
