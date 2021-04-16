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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { Typography } from '@material-ui/core';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Subscription from 'AppData/Subscription';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import CircularProgress from '@material-ui/core/CircularProgress';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import Icon from '@material-ui/core/Icon';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import { FormattedMessage, injectIntl } from 'react-intl';
import Application from 'AppData/Application';
import AuthManager from 'AppData/AuthManager';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import { ApiContext } from '../ApiContext';
import SubscriptionTableRow from './SubscriptionTableRow';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = (theme) => ({
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
        paddingTop: theme.spacing(3),
    },
    titleSub: {
        marginLeft: theme.spacing(3),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.default),
    },
    generateCredentialWrapper: {
        marginLeft: 0,
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
        '& span, & h5, & label, & td, & li, & div': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
    },
    tableMain: {
        '& > table': {
            width: '100%',
            borderCollapse: 'collapse',
            marginTop: theme.spacing(3),
            marginLeft: theme.spacing(2),
            marginRight: theme.spacing(1),
        },
        '& table > tr td': {
            paddingLeft: theme.spacing(1),
        },
        '&  table > tr:nth-child(even)': {
            backgroundColor: theme.custom.listView.tableBodyEvenBackgrund,
            '& td, & a, & .material-icons': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
            },
        },
        '&  table > tr:nth-child(odd)': {
            backgroundColor: theme.custom.listView.tableBodyOddBackgrund,
            '& td, & a, & .material-icons': {
                color: theme.palette.getContrastText(theme.custom.listView.tableBodyOddBackgrund),
            },
        },
        '&  table > tr > th': {
            backgroundColor: theme.custom.listView.tableHeadBackground,
            color: theme.palette.getContrastText(theme.custom.listView.tableHeadBackground),
            paddingLeft: theme.spacing(1),
            borderBottom: 'solid 1px ' + theme.palette.grey.A200,
            borderTop: 'solid 1px ' + theme.palette.grey.A200,
            textAlign: 'left',
            fontSize: '11px',
            paddingTop: theme.spacing(1),
            paddingBottom: theme.spacing(1),
        },
        '& table > tr > th:last-child': {
            textAlign: 'right',
        },

    },
    expansion: {
        background: 'transparent',
        boxShadow: 'none',
    },
    summary: {
        alignItems: 'center',
    },
    subscribeRoot: {
        paddingLeft: theme.spacing(2),
    },
    activeLink: {
        background: theme.palette.grey.A100,
    },
    appBar: {
        background: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    toolbar: {
        marginLeft: theme.spacing(2),
    },
    subscribeTitle: {
        flex: 1,
    },
    paper: {
        marginLeft: theme.spacing(3),
        padding: theme.spacing(2),
    },
    descWrapper: {
        marginBottom: theme.spacing(2),
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    credentialBoxWrapper: {
        paddingLeft: theme.spacing(2),
    },
    credentialBox: {
        padding: theme.spacing(1),
        border: 'solid 1px #ccc',
        borderRadius: 5,
        marginBottom: theme.spacing(2),
        marginTop: theme.spacing(2),
    },
    addLinkWrapper: {
        marginLeft: theme.spacing(2),
    },
    subsListTitle: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    subsListDesc: {
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    buttonElm: {
        '& span': {
            color: theme.palette.getContrastText(theme.palette.primary.main),
        },
    },
});

/**
 * @class Credentials
 * @extends {React.Component}
 */
class Credentials extends React.Component {
    /**
     *Creates an instance of Credentials.
     * @param JSON props
     * @memberof Credentials
     */
    constructor(props) {
        super(props);
        this.state = {
            expanded: true,
            selectedAppId: false,
            selectedKeyType: false,
            subscriptionRequest: {
                applicationId: '',
                apiId: '',
                throttlingPolicy: '',
            },
            throttlingPolicyList: [],
            applicationOwner: '',
            hashEnabled: false,
            isSubscribing: false,
        };
        this.api = new Api();
    }


    /**
     *  Set the initial values for subscription request
     */
    componentDidMount() {
        const { api, updateSubscriptionData } = this.context;
        if (api) {
            this.updateData();
        } else {
            updateSubscriptionData(this.updateData);
        }
    }

    updateData = () => {
        const { api, applicationsAvailable } = this.context;
        const { subscriptionRequest } = this.state;
        const newSubscriptionRequest = { ...subscriptionRequest, apiId: api.id };
        const throttlingPolicyList = api.tiers;
        if (throttlingPolicyList && throttlingPolicyList[0]) {
            newSubscriptionRequest.throttlingPolicy = throttlingPolicyList[0].tierName;
        }
        if (applicationsAvailable && applicationsAvailable[0]) {
            newSubscriptionRequest.applicationId = applicationsAvailable[0].value;
        }
        this.setState({ subscriptionRequest: newSubscriptionRequest, throttlingPolicyList });
    };

    /**
     * @memberof Credentials
     */
    handleExpandClick = () => {
        this.setState((state) => ({ expanded: !state.expanded }));
    };

    /**
     * @param {*} updateSubscriptionData method to update global subscription data
     * @memberof Credentials
     */
    handleSubscribe = () => {
        const { updateSubscriptionData, apiType } = this.context;
        const { subscriptionRequest } = this.state;
        const { intl } = this.props;
        const api = new Api();
        this.setState({ isSubscribing: true });
        api.subscribe(
            subscriptionRequest.apiId,
            subscriptionRequest.applicationId,
            subscriptionRequest.throttlingPolicy,
            apiType,
        )
            .then((response) => {
                if (response.body.status === 'ON_HOLD') {
                    Alert.info(intl.formatMessage({
                        defaultMessage: 'Your subscription request has been submitted and is now awaiting approval.',
                        id: 'subscription.pending',
                    }));
                } else {
                    console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);
                    Alert.info(intl.formatMessage({
                        defaultMessage: 'Subscribed successfully',
                        id: 'Apis.Details.Credentials.Credentials.subscribed.successfully',
                    }));
                }
                if (updateSubscriptionData) updateSubscriptionData(this.updateData);
                this.setState({ isSubscribing: false });
            })
            .catch((error) => {
                Alert.error(intl.formatMessage({
                    id: 'Applications.Details.Subscriptions.error.occurred.during.subscription.not.201',
                    defaultMessage: 'Error occurred during subscription',
                }));
                console.log('Error while creating the subscription.');
                console.error(error);
                this.setState({ isSubscribing: false });
            });
    };

    /**
     * @inheritdoc
     * @memberof Credentials
     */
    goToWizard = () => {
        const { history } = this.props;
        history.push('credentials/wizard');
    };

    /**
     * used to load the token manager component when
     * key type is selected in the applicaiton list
     * @param {*} selectedKeyType key type
     * @param {*} selectedAppId  application id
     * @memberof Credentials
     */
    loadInfo = (selectedKeyType, selectedAppId) => {
        this.setState({ selectedKeyType, selectedAppId });

        Application.get(selectedAppId)
            .then((result) => {
                this.setState({ applicationOwner: result.owner, hashEnabled: result.hashEnabled });
            });
    };

    /**
     * Update subscription Request state
     * @param {Object} subscriptionRequest parameters requried for subscription
     */
    updateSubscriptionRequest = (subscriptionRequest) => {
        this.setState({ subscriptionRequest });
    };

    /**
     *
     * @param {*} subscriptionId subscription id
     * @param {*} updateSubscriptionData method to update global subscription data
     * @memberof Subscriptions
     */
    handleSubscriptionDelete = (subscriptionId, updateSubscriptionData) => {
        const { intl } = this.props;
        const client = new Subscription();
        const promisedDelete = client.deleteSubscription(subscriptionId);
        promisedDelete.then((response) => {
            if (response.status !== 200) {
                console.log(response);
                Alert.info(intl.formatMessage({
                    defaultMessage: 'Something went wrong while deleting the Subscription!',
                    id: 'Apis.Details.Credentials.Credentials.something.went.wrong.with.subscription',
                }));
                return;
            }
            Alert.info(intl.formatMessage({
                defaultMessage: 'Subscription deleted successfully!',
                id: 'Apis.Details.Credentials.Credentials.subscription.deleted.successfully',
            }));
            if (updateSubscriptionData) updateSubscriptionData(this.updateData);
        });
    };

    /**
     * @inheritdoc
     */
    render() {
        const { classes, intl } = this.props;
        const {
            api, updateSubscriptionData, applicationsAvailable, subscribedApplications,
        } = this.context;
        const {
            selectedKeyType,
            selectedAppId,
            subscriptionRequest,
            throttlingPolicyList,
            applicationOwner,
            hashEnabled,
            isSubscribing,
        } = this.state;
        const user = AuthManager.getUser();
        const isOnlyMutualSSL = api.securityScheme.includes('mutualssl') && !api.securityScheme.includes('oauth2')
        && !api.securityScheme.includes('api_key') && !api.securityScheme.includes('basic_auth');
        const isOnlyBasicAuth = api.securityScheme.includes('basic_auth') && !api.securityScheme.includes('oauth2')
         && !api.securityScheme.includes('api_key');
        const isPrototypedAPI = api.lifeCycleStatus && api.lifeCycleStatus.toLowerCase() === 'prototyped';
        const isSetAllorResidentKeyManagers = (api.keyManagers && api.keyManagers.includes('all'))
            || (api.keyManagers && api.keyManagers.includes('Resident Key Manager'));
        const renderCredentialInfo = () => {
            if (isPrototypedAPI) {
                return (
                    <>
                        <InlineMessage type='info' className={classes.dialogContainer}>
                            <Typography component='p'>
                                <FormattedMessage
                                    id={'Apis.Details.Credentials.Credentials.you.do.not.need'
                                        + '.credentials.to.access.prototyped.api'}
                                    defaultMessage='You do not need credentials to access Prototyped APIs'
                                />
                            </Typography>
                        </InlineMessage>
                    </>
                );
            } else if (isOnlyMutualSSL || isOnlyBasicAuth) {
                return (
                    <InlineMessage type='info' className={classes.dialogContainer}>
                        <Typography component='p'>
                            <FormattedMessage
                                id='Apis.Details.Creadentials.credetials.mutualssl'
                                defaultMessage={'Subscription is not required for Mutual SSL APIs'
                                        + ' or APIs with only Basic Authentication.'}
                            />
                        </Typography>
                    </InlineMessage>
                );
            } else if (applicationsAvailable.length === 0 && subscribedApplications.length === 0) {
                return (
                    <GenericDisplayDialog
                        classes={classes}
                        handleClick={this.goToWizard}
                        heading={user ? intl.formatMessage({
                            defaultMessage: 'Subscribe',
                            id: 'Apis.Details.Credentials.Credentials.subscribe.to.application',
                        })
                            : intl.formatMessage({
                                defaultMessage: 'Sign In to Subscribe',
                                id: 'Apis.Details.Credentials.Credentials.subscribe.to.application.sign.in',
                            })}
                        caption={intl.formatMessage({
                            defaultMessage: 'You need to subscribe to an application to access this API',
                            id:
                            'Apis.Details.Credentials.Credentials.subscribe.to.application.msg',
                        })}
                        buttonText={intl.formatMessage({
                            defaultMessage: 'Subscribe',
                            id: 'Apis.Details.Credentials.Credentials.generate',
                        })}
                    />
                );
            } else {
                return (
                    <>
                        <div className={classes.generateCredentialWrapper}>
                            <ScopeValidation
                                resourcePath={resourcePaths.SUBSCRIPTIONS}
                                resourceMethod={resourceMethods.POST}
                            >
                                <Typography variant='h5' component='h2'>
                                    <FormattedMessage
                                        id={'Apis.Details.Credentials.Credentials.'
                                        + 'subscribe.to.application'}
                                        defaultMessage='Subscribe'
                                    />
                                </Typography>
                                <div className={classes.credentialBoxWrapper}>
                                    {applicationsAvailable.length === 0 && (
                                        <div className={classes.credentialBox}>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials.'
                                                    + 'api.credentials.with.wizard.message'}
                                                    defaultMessage={
                                                        'Use the Subscription and Key Generation Wizard. '
                                                        + 'Create a new application -> '
                                                        + 'Subscribe -> Generate keys and '
                                                        + 'Access Token to invoke this API.'
                                                    }
                                                />
                                            </Typography>
                                            <Link
                                                to={(isOnlyMutualSSL || isOnlyBasicAuth
                                                    || !isSetAllorResidentKeyManagers) ? null
                                                    : `/apis/${api.id}/credentials/wizard`}
                                                style={!api.isSubscriptionAvailable
                                                    ? { pointerEvents: 'none' } : null}
                                            >
                                                <Button
                                                    variant='contained'
                                                    color='primary'
                                                    className={classes.buttonElm}
                                                    disabled={!api.isSubscriptionAvailable || isOnlyMutualSSL
                                                        || isOnlyBasicAuth || !isSetAllorResidentKeyManagers}
                                                >
                                                    <FormattedMessage
                                                        id={'Apis.Details.Credentials.'
                                                        + 'SubscibeButtonPanel.subscribe.wizard.with.new.app'}
                                                        defaultMessage='Subscription &amp; Key Generation Wizard'
                                                    />
                                                </Button>
                                            </Link>
                                        </div>
                                    ) }
                                    {applicationsAvailable.length > 0 && (
                                        <div className={classes.credentialBox}>
                                            <Typography variant='body2'>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials'
                                                    + '.api.credentials.with.subscribe.message'}
                                                    defaultMessage={'Subscribe to an application'
                                                    + ' and generate credentials'}
                                                />
                                            </Typography>
                                            <SubscribeToApi
                                                applicationsAvailable={applicationsAvailable}
                                                subscriptionRequest={subscriptionRequest}
                                                throttlingPolicyList={throttlingPolicyList}
                                                updateSubscriptionRequest={
                                                    this.updateSubscriptionRequest
                                                }
                                                renderSmall
                                            />
                                            <Button
                                                variant='contained'
                                                color='primary'
                                                className={classes.buttonElm}
                                                onClick={() => this.handleSubscribe()}
                                                disabled={!api.isSubscriptionAvailable || isSubscribing}
                                            >
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.'
                                                    + 'SubscibeButtonPanel.subscribe.btn'}
                                                    defaultMessage='Subscribe'
                                                />
                                                {isSubscribing && <CircularProgress size={24} />}
                                            </Button>
                                        </div>
                                    )}
                                </div>
                            </ScopeValidation>
                        </div>
                        {/*
                                    ****************************
                                    Subscription List
                                    ***************************
                                    */}
                        {subscribedApplications && subscribedApplications.length > 0 && (
                            <>
                                <Typography variant='h5' component='h2' className={classes.subsListTitle}>
                                    <FormattedMessage
                                        id={'Apis.Details.Credentials.Credentials.'
                                        + 'api.credentials.subscribed.apps.title'}
                                        defaultMessage='Subscriptions'
                                    />
                                </Typography>
                                <Typography variant='body2' className={classes.subsListDesc}>
                                    <FormattedMessage
                                        id={'Apis.Details.Credentials.Credentials.'
                                        + 'api.credentials.subscribed.apps.description'}
                                        defaultMessage='( Applications Subscribed to this Api )'
                                    />
                                </Typography>
                                <div className={classes.tableMain}>
                                    <table>
                                        <tr>
                                            <th className={classes.th}>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials.'
                                                    + 'api.credentials.subscribed.apps.name'}
                                                    defaultMessage='Application Name'
                                                />
                                            </th>
                                            <th className={classes.th}>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials.api.'
                                                    + 'credentials.subscribed.apps.tier'}
                                                    defaultMessage='Throttling Tier'
                                                />
                                            </th>
                                            <th className={classes.th}>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials.'
                                                    + 'api.credentials.subscribed.apps.status'}
                                                    defaultMessage='Application Status'
                                                />
                                            </th>
                                            <th className={classes.th}>
                                                <FormattedMessage
                                                    id={'Apis.Details.Credentials.Credentials.'
                                                    + 'api.credentials.subscribed.apps.action'}
                                                    defaultMessage='Actions'
                                                />
                                            </th>
                                        </tr>
                                        {subscribedApplications.map((app, index) => (
                                            <SubscriptionTableRow
                                                key={app.id}
                                                loadInfo={this.loadInfo}
                                                handleSubscriptionDelete={this.handleSubscriptionDelete}
                                                selectedAppId={selectedAppId}
                                                updateSubscriptionData={updateSubscriptionData}
                                                selectedKeyType={selectedKeyType}
                                                app={app}
                                                index={index}
                                                applicationOwner={applicationOwner}
                                                hashEnabled={hashEnabled}
                                            />
                                        ))}
                                    </table>
                                </div>
                            </>
                        )}
                    </>
                );
            }
        };
        return (
            <Grid container>
                <Grid item md={12} lg={11}>
                    <Grid container spacing={2}>
                        <Grid item md={12}>
                            <Typography onClick={this.handleExpandClick} variant='h4' component='div' className={classes.titleSub}>
                                {applicationsAvailable.length > 0 && (
                                    <Link
                                        to={(isOnlyMutualSSL || isOnlyBasicAuth || isPrototypedAPI
                                            || !isSetAllorResidentKeyManagers) ? null
                                            : `/apis/${api.id}/credentials/wizard`}
                                        style={!api.isSubscriptionAvailable
                                            ? { pointerEvents: 'none' } : null}
                                        className={classes.addLinkWrapper}
                                    >
                                        <Button
                                            color='secondary'
                                            disabled={!api.isSubscriptionAvailable || isOnlyMutualSSL
                                                 || isOnlyBasicAuth || isPrototypedAPI
                                                 || !isSetAllorResidentKeyManagers}
                                            size='small'
                                        >
                                            <Icon>add_circle_outline</Icon>
                                            <FormattedMessage
                                                id={'Apis.Details.Credentials.'
                                                + 'SubscibeButtonPanel.subscribe.wizard.with.new.app'}
                                                defaultMessage='Subscription &amp; Key Generation Wizard'
                                            />
                                        </Button>
                                    </Link>
                                )}
                            </Typography>
                            <Paper elevation={0} className={classes.paper}>
                                <Typography variant='body2' className={classes.descWrapper}>
                                    <FormattedMessage
                                        id='Apis.Details.Credentials.Credentials.'
                                        defaultMessage={`An application 
                                        is primarily used to decouple the consumer from the APIs. It allows you to 
                                        generate and use a single key for multiple APIs and subscribe multiple times to 
                                        a single API with different SLA levels.`}
                                    />
                                </Typography>
                                {renderCredentialInfo()}
                            </Paper>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        );
    }
}

Credentials.propTypes = {
    classes: PropTypes.shape({
        contentWrapper: PropTypes.string,
        titleSub: PropTypes.string,
        tableMain: PropTypes.string,
        th: PropTypes.string,
        paper: PropTypes.string,
        descWrapper: PropTypes.string,
        generateCredentialWrapper: PropTypes.string,
        credentialBoxWrapper: PropTypes.string,
        credentialBox: PropTypes.string,
        buttonElm: PropTypes.string,
        dialogContainer: PropTypes.string,
    }).isRequired,
    history: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};
Credentials.contextType = ApiContext;

export default injectIntl(withStyles(styles, { withTheme: true })(Credentials));
