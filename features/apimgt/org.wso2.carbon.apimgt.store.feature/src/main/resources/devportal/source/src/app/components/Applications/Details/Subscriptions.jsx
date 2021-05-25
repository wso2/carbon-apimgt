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
import Icon from '@material-ui/core/Icon';
import IconButton from '@material-ui/core/IconButton';
import Divider from '@material-ui/core/Divider';
import Dialog from '@material-ui/core/Dialog';
import Button from '@material-ui/core/Button';
import MuiDialogTitle from '@material-ui/core/DialogTitle';
import Box from '@material-ui/core/Box';
import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import HighlightOffIcon from '@material-ui/icons/HighlightOff';
import SearchIcon from '@material-ui/icons/Search';
import { FormattedMessage, injectIntl } from 'react-intl';
import Progress from 'AppComponents/Shared/Progress';
import Alert from 'AppComponents/Shared/Alert';
import APIList from 'AppComponents/Apis/Listing/APICardView';
import ResourceNotFound from 'AppComponents/Base/Errors/ResourceNotFound';
import Subscription from 'AppData/Subscription';
import Api from 'AppData/api';
import { app } from 'Settings';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import SubscriptionTableData from './SubscriptionTableData';

/**
 *
 * @inheritdoc
 * @param {*} theme theme
 */
const styles = (theme) => ({
    searchRoot: {
        padding: '2px 4px',
        display: 'flex',
        alignItems: 'center',
        width: 400,
        flex: 1,
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2),
    },
    input: {
        marginLeft: theme.spacing(1),
        flex: 1,
    },
    iconButton: {
        padding: 10,
    },
    divider: {
        height: 28,
        margin: 4,
    },
    root: {
        padding: theme.spacing(3),
        '& h5': {
            color: theme.palette.getContrastText(theme.palette.background.default),
        },
    },
    subscribePop: {
        '& span, & h5, & label, & input, & td, & li': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    firstCell: {
        paddingLeft: 0,
    },
    cardTitle: {
        paddingLeft: theme.spacing(2),
    },
    cardContent: {
        '& table tr td':{
            paddingLeft: theme.spacing(1),
        },
        '& table tr:nth-child(even)': {
            backgroundColor: theme.custom.listView.tableBodyEvenBackgrund,
            '& td, & a': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
            },
        },
        '& table tr:nth-child(odd)': {
            backgroundColor: theme.custom.listView.tableBodyOddBackgrund,
            '& td, & a': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyOddBackgrund),
            },
        },
        '& table th': {
            backgroundColor: theme.custom.listView.tableHeadBackground,
            color: theme.palette.getContrastText(theme.custom.listView.tableHeadBackground),
            paddingLeft: theme.spacing(1),
        },

    },
    titleWrapper: {
        display: 'flex',
        alignItems: 'center',
        paddingBottom: theme.spacing(2),
        '& h5': {
            marginRight: theme.spacing(1),
        },
    },
    dialogTitle: {
        display: 'flex',
        alignItems: 'flex-start',
        padding: theme.spacing(1),
    },
    genericMessageWrapper: {
        margin: theme.spacing(2),
    },
    searchWrapper: {
        flex: 1,
    },
    searchResults: {
        height: 30,
        display: 'flex',
        paddingTop: theme.spacing(1),
        paddingRight: 0,
        paddingBottom: 0,
        paddingLeft: theme.spacing(2),
    },
    clearSearchIcon: {
        cursor: 'pointer',
    },
    subsTable: {
        '& td': {
            padding: '4px 8px',
        },
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
            openDialog: false,
            searchText: '',
        };
        this.handleSubscriptionDelete = this.handleSubscriptionDelete.bind(this);
        this.handleSubscriptionUpdate = this.handleSubscriptionUpdate.bind(this);
        this.updateSubscriptions = this.updateSubscriptions.bind(this);
        this.handleSubscribe = this.handleSubscribe.bind(this);
        this.handleOpenDialog = this.handleOpenDialog.bind(this);
        this.handleSearchTextChange = this.handleSearchTextChange.bind(this);
        this.handleSearchTextTmpChange = this.handleSearchTextTmpChange.bind(this);
        this.handleClearSearch = this.handleClearSearch.bind(this);
        this.handleEnterPress = this.handleEnterPress.bind(this);
        this.searchTextTmp = '';
    }

    /**
     *
     *
     * @memberof Subscriptions
     */
    componentDidMount() {
        const { applicationId } = this.props.application;
        this.updateSubscriptions(applicationId);
    }

    handleOpenDialog() {
        this.setState((prevState) => ({ openDialog: !prevState.openDialog, searchText: '' }));
    }

    /**
     *
     * Update subscriptions list of Application
     * @param {*} applicationId application id
     * @memberof Subscriptions
     */
    updateSubscriptions(applicationId) {
        const client = new Subscription();
        const subscriptionLimit = app.subscriptionLimit || 1000;
        const promisedSubscriptions = client.getSubscriptions(null, applicationId, subscriptionLimit);
        promisedSubscriptions
            .then((response) => {
                this.setState({ subscriptions: response.body.list });
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
                        Object.prototype.hasOwnProperty.call(subscriptions, endpointIndex)
                        && subscriptions[endpointIndex].subscriptionId === subscriptionId
                    ) {
                        subscriptions.splice(endpointIndex, 1);
                        break;
                    }
                }
                this.setState({ subscriptions });
                this.props.getApplication();
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
     * Handle subscription update of application
     *
     * @param {*} apiId API id
     * @param {*} subscriptionId subscription id
     * @param {*} throttlingPolicy throttling tier
     * @param {*} status subscription status
     * @memberof Subscriptions
     */
    handleSubscriptionUpdate(apiId, subscriptionId, currentThrottlingPolicy, status, requestedThrottlingPolicy) {
        const { applicationId } = this.props.application;
        const client = new Subscription();
        const promisedUpdate = client.updateSubscription(applicationId, apiId, subscriptionId, currentThrottlingPolicy, status, requestedThrottlingPolicy);

        promisedUpdate
            .then((response) => {
                if (response.status !== 200 && response.status !== 201) {
                    console.log(response);
                    Alert.info('Something went wrong while updating the Subscription!');
                    return;
                }
                Alert.info('Subscription Tier updated successfully!');
                this.updateSubscriptions(applicationId);
            })
            .catch((error) => {
                const { status } = error;
                if (status === 401) {
                    this.setState({ isAuthorize: false });
                }
                Alert.error('Error occurred when updating subscription');
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
                            defaultMessage: 'Your subscription request has been submitted and is now awaiting '
                                + 'approval.',
                            id: 'subscription.pending',
                        }));
                    } else {
                        Alert.info(intl.formatMessage({
                            id: 'Applications.Details.Subscriptions.subscription.successful',
                            defaultMessage: 'Subscription successful',
                        }));
                    }
                    this.updateSubscriptions(applicationId);
                    this.props.getApplication();
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
    handleSearchTextChange() {
        this.setState({ searchText: this.searchTextTmp });
    };
    handleSearchTextTmpChange(event) {
        this.searchTextTmp = event.target.value;
    };
    handleClearSearch() {
        this.setState({ searchText: '' });
    };
    handleEnterPress(e) {
        if (e.keyCode === 13) {
            e.preventDefault();
            this.handleSearchTextChange();
        }
    }
    /**
     * @inheritdoc
     * @memberof Subscriptions
     */
    render() {
        const { isAuthorize, openDialog, searchText } = this.state;

        if (!isAuthorize) {
            window.location = app.context + '/services/configs';
        }

        const {
            subscriptions, apisNotFound, subscriptionsNotFound,
        } = this.state;
        const { applicationId } = this.props.application;
        const { classes, intl } = this.props;

        if (subscriptions) {
            return (
                <div className={classes.root}>
                    <div className={classes.titleWrapper}>
                        <Typography variant='h5' className={classes.keyTitle}>
                            <FormattedMessage
                                id='Applications.Details.Subscriptions.subscription.management'
                                defaultMessage='Subscription Management'
                            />
                        </Typography>
                        <Button
                            color='secondary'
                            className={classes.buttonElm}
                            size='small'
                            onClick={this.handleOpenDialog}
                        >
                            <Icon>add_circle_outline</Icon>
                            <FormattedMessage
                                id='Applications.Details.Subscriptions.subscription.management.add'
                                defaultMessage='Subscribe APIs'
                            />
                        </Button>
                    </div>
                    <Grid container className='tab-grid' spacing={2}>
                        <Grid item xs={12} xl={11}>
                            {(subscriptions && subscriptions.length === 0)
                                ? (
                                    <div className={classes.genericMessageWrapper}>
                                        <InlineMessage type='info' className={classes.dialogContainer}>
                                            <Typography variant='h5' component='h3'>
                                                <FormattedMessage
                                                    id='Applications.Details.Subscriptions.no.subscriptions'
                                                    defaultMessage='No Subscriptions Available'
                                                />
                                            </Typography>
                                            <Typography component='p'>
                                                <FormattedMessage
                                                    id='Applications.Details.Subscriptions.no.subscriptions.content'
                                                    defaultMessage='No subscriptions are available for this Application'
                                                />
                                            </Typography>
                                        </InlineMessage>
                                    </div>
                                )
                                : (
                                    <div className={classes.cardContent}>
                                            {subscriptionsNotFound ? (
                                                <ResourceNotFound />
                                            ) : (
                                                    <Table className={classes.subsTable}>
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
                                                                        .subscription.state`}
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
                                                            </TableRow>
                                                        </TableHead>
                                                        <TableBody>
                                                            {subscriptions
                                                                && subscriptions.map((subscription) => {
                                                                    return (
                                                                        <SubscriptionTableData
                                                                            key={subscription.subscriptionId}
                                                                            subscription={subscription}
                                                                            handleSubscriptionDelete={this.handleSubscriptionDelete}
                                                                            handleSubscriptionUpdate={this.handleSubscriptionUpdate}
                                                                        />
                                                                    );
                                                                })}
                                                        </TableBody>
                                                    </Table>
                                                )}
                                        </div>
                                )}
                        </Grid>
                    </Grid>
                    <Dialog
                        onClose={this.handleOpenDialog}
                        aria-labelledby='simple-dialog-title'
                        open={openDialog}
                        maxWidth='lg'
                        className={classes.subscribePop}
                    >
                        <MuiDialogTitle disableTypography className={classes.dialogTitle}>
                            <Typography variant='h6'>
                                <FormattedMessage
                                    id='Applications.Details.Subscriptions.subscription.management.add'
                                    defaultMessage='Subscribe APIs'
                                />
                            </Typography>
                            <Box className={classes.searchWrapper}>
                                <Paper component="form" className={classes.searchRoot}>
                                    {searchText && <HighlightOffIcon className={classes.clearSearchIcon}
                                        onClick={this.handleClearSearch}
                                    />}
                                    <InputBase
                                        className={classes.input}
                                        placeholder={intl.formatMessage({ defaultMessage: 'Search APIs', id: 'Applications.Details.Subscriptions.search' })}
                                        inputProps={{ 'aria-label': intl.formatMessage({ defaultMessage: 'Search APIs', id: 'Applications.Details.Subscriptions.search' }) }}
                                        onChange={this.handleSearchTextTmpChange}
                                        onKeyDown={this.handleEnterPress}
                                    />
                                    <IconButton className={classes.iconButton} aria-label="search" onClick={this.handleSearchTextChange}>
                                        <SearchIcon />
                                    </IconButton>
                                </Paper>
                                <Box className={classes.searchResults}>
                                    {(searchText && searchText !== '') ? <>
                                        <Typography variant='caption'>
                                            <FormattedMessage
                                                id='Applications.Details.Subscriptions.filter.msg'
                                                defaultMessage='Filtered APIs for '
                                            />{searchText}</Typography>
                                    </> : (<Typography variant='caption'>
                                        <FormattedMessage
                                            id='Applications.Details.Subscriptions.filter.msg.all.apis'
                                            defaultMessage='Displaying all APIs'
                                        /></Typography>)}
                                </Box>

                            </Box>

                            <IconButton aria-label='close' className={classes.closeButton} onClick={this.handleOpenDialog}>
                                <Icon>cancel</Icon>
                            </IconButton>
                        </MuiDialogTitle>
                        <Box padding={2}>
                            <APIList
                                apisNotFound={apisNotFound}
                                subscriptions={subscriptions}
                                applicationId={applicationId}
                                handleSubscribe={(app, api, policy) => this.handleSubscribe(app, api, policy)}
                                searchText={searchText}
                            />
                        </Box>
                    </Dialog>
                </div>
            );
        } else {
            return (
              <Progress />  
            );
        }
    }
}
Subscriptions.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(Subscriptions));
