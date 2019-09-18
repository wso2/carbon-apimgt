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
import Dialog from '@material-ui/core/Dialog';
import Slide from '@material-ui/core/Slide';
import Subscription from 'AppData/Subscription';
import GenericDisplayDialog from 'AppComponents/Shared/GenericDisplayDialog';
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import { FormattedMessage, injectIntl } from 'react-intl';
import { ApiContext } from '../ApiContext';
import Wizard from './Wizard/Wizard';
import SubscriptionTableRow from './SubscriptionTableRow';
import SubscribeToApps from './SubscribeToApps';
import SubscibeButtonPanel from './SubscibeButtonPanel';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing.unit * 3,
        paddingTop: theme.spacing.unit * 3,
    },
    titleSub: {
        cursor: 'pointer',
    },
    tableMain: {
        width: '100%',
        borderCollapse: 'collapse',
        marginTop: theme.spacing.unit * 3,
    },
    th: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        borderTop: 'solid 1px ' + theme.palette.grey.A200,
        textAlign: 'left',
        fontSize: '11px',
        paddingLeft: theme.spacing.unit,
        paddingTop: theme.spacing.unit,
        paddingBottom: theme.spacing.unit,
    },
    expansion: {
        background: 'transparent',
        boxShadow: 'none',
    },
    summary: {
        alignItems: 'center',
    },
    subscribeRoot: {
        paddingLeft: theme.spacing.unit * 2,
    },
    activeLink: {
        background: theme.palette.grey.A100,
    },
    appBar: {
        background: theme.palette.background.paper,
        color: theme.palette.getContrastText(theme.palette.background.paper),
    },
    toolbar: {
        marginLeft: theme.spacing.unit * 2,
    },
    subscribeTitle: {
        flex: 1,
    },
});

/**
 * @param {*} props properties
 * @returns {Component}
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 * @class Credentials
 * @extends {React.Component}
 */
class Credentials extends React.Component {
    static contextType = ApiContext;

    state = {
        value: 0,
        expanded: true,
        wizardOn: false,
        openAvailable: false,
        openNew: false,
        selectedAppId: false,
        selectedKeyType: false,
        subscriptionRequest: {
            applicationId: '',
            apiId: '',
            throttlingPolicy: '',
        },
        throttlingPolicyList: [],
    };

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
        const { history: { location: { state, pathname }, replace } } = this.props;
        if (state) {
            const { openWizard } = state;
            if (openWizard) {
                this.setState({ wizardOn: true, openNew: true });
                replace({
                    pathname,
                    state: {},
                });
            }
        }
    }

    updateData = () => {
        const { api, applicationsAvailable } = this.context;
        const { subscriptionRequest } = this.state;
        const newSubscriptionRequest = { ...subscriptionRequest, apiId: api.id };
        const throttlingPolicyList = api.tiers;
        if (throttlingPolicyList) {
            [newSubscriptionRequest.throttlingPolicy] = throttlingPolicyList;
        }
        if (applicationsAvailable && applicationsAvailable[0]) {
            newSubscriptionRequest.applicationId = applicationsAvailable[0].value;
        }
        this.setState({ subscriptionRequest: newSubscriptionRequest, throttlingPolicyList });
    }

    /**
     * @memberof Credentials
     */
    handleExpandClick = () => {
        this.setState(state => ({ expanded: !state.expanded }));
    };

    /**
     * @memberof Credentials
     */
    startStopWizard = () => {
        this.setState(state => ({ wizardOn: !state.wizardOn }));
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
        api.subscribe(subscriptionRequest.apiId, subscriptionRequest.applicationId,
            subscriptionRequest.throttlingPolicy, apiType)
            .then((response) => {
                console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);
                Alert.info(intl.formatMessage({
                    defaultMessage: 'Subscribed successfully',
                    id: 'Apis.Details.Credentials.Credentials.subscribed.successfully',
                }));
                if (updateSubscriptionData) updateSubscriptionData();
                this.setState({ openAvailable: false });
            })
            .catch((error) => {
                console.log('Error while creating the subscription.');
                console.error(error);
            });
    };

    /**
     * @inheritdoc
     * @memberof Credentials
     */
    handleClickToggle = (name, updateSubscriptionData) => {
        this.setState((prevState) => {
            return { [name]: !prevState[name] };
        });
        if (updateSubscriptionData) {
            updateSubscriptionData();
        }
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
    };

    /**
     * Update subscription Request state
     * @param {Object} subscriptionRequest parameters requried for subscription
     */
    updateSubscriptionRequest = (subscriptionRequest) => {
        this.setState({ subscriptionRequest });
    }

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
            if (updateSubscriptionData) updateSubscriptionData();
        });
    }


    /**
     * @inheritdoc
     */
    render() {
        const { classes, intl } = this.props;
        const {
            api, updateSubscriptionData, applicationsAvailable, subscribedApplications,
        } = this.context;
        const {
            selectedKeyType, selectedAppId, wizardOn, openAvailable, subscriptionRequest,
            throttlingPolicyList, openNew,
        } = this.state;
        return (
            <div className={classes.contentWrapper}>
                <Typography onClick={this.handleExpandClick} variant='display1' className={classes.titleSub}>
                    <FormattedMessage
                        id='Apis.Details.Credentials.Credentials.api.credentials'
                        defaultMessage='API Credentials'
                    />
                </Typography>
                <Typography variant='body1' gutterBottom>
                    <FormattedMessage
                        id='Apis.Details.Credentials.Credentials.'
                        defaultMessage={`API Credentials are grouped in to applications. An application is 
                        primarily used to decouple the consumer from the APIs. It allows you to Generate 
                        and use a single key for multiple APIs and subscribe multiple times to a single 
                        API with different SLA levels.`}
                    />
                </Typography>
                {applicationsAvailable.length === 0 && subscribedApplications.length === 0 ? (
                    !wizardOn && (
                        <GenericDisplayDialog
                            classes={classes}
                            handleClick={this.startStopWizard}
                            heading={intl.formatMessage({
                                defaultMessage: 'Generate Credentials',
                                id: 'Apis.Details.Credentials.Credentials.generate.credentials',
                            })}
                            caption={intl.formatMessage({
                                defaultMessage: 'You need to generate credentials to access this API',
                                id: 'Apis.Details.Credentials.Credentials.you.need.to'
                                + '.generate.credentials.to.access.this.api',
                            })}
                            buttonText={intl.formatMessage({
                                defaultMessage: 'GENERATE',
                                id: 'Apis.Details.Credentials.Credentials.generate',
                            })}
                        />
                    )
                ) : (
                    <React.Fragment>
                        <SubscibeButtonPanel
                            avalibleAppsLength={applicationsAvailable.length}
                            subscribedAppsLength={subscribedApplications.length}
                            handleClickToggle={this.handleClickToggle}
                        />
                        {/*
                                ****************************
                                Subscription List
                                ***************************
                                */}
                        <table className={classes.tableMain}>
                            <tr>
                                <th className={classes.th}>Application Name</th>
                                <th className={classes.th}>Throttling Tier</th>
                                <th className={classes.th}>Application Status</th>
                                <th className={classes.th} />
                            </tr>
                            {subscribedApplications.map((app, index) => (
                                <SubscriptionTableRow
                                    loadInfo={this.loadInfo}
                                    handleSubscriptionDelete={this.handleSubscriptionDelete}
                                    selectedAppId={selectedAppId}
                                    updateSubscriptionData={updateSubscriptionData}
                                    selectedKeyType={selectedKeyType}
                                    app={app}
                                    index={index}
                                />
                            ))}
                        </table>
                        {/*
                                ****************************
                                Subscribe to apps available
                                ***************************
                                */}
                        {applicationsAvailable.length > 0 && (
                            <SubscribeToApps
                                api={api}
                                openAvailable={openAvailable}
                                handleClickToggle={this.handleClickToggle}
                                Transition={Transition}
                                applicationsAvailable={applicationsAvailable}
                                handleSubscribe={this.handleSubscribe}
                                subscriptionRequest={subscriptionRequest}
                                updateSubscriptionRequest={this.updateSubscriptionRequest}
                                throttlingPolicyList={throttlingPolicyList}
                            />
                        )}
                        {/*
                                ***************************************
                                Subscribe with new Mode
                                ***************************************
                                */}
                        <Dialog
                            fullScreen
                            open={openNew}
                            onClose={() => this.handleClickToggle('openNew', updateSubscriptionData)}
                            TransitionComponent={Transition}
                        >
                            <Wizard
                                updateSubscriptionData={updateSubscriptionData}
                                apiId={api.id}
                                handleClickToggle={this.handleClickToggle}
                                throttlingPolicyList={throttlingPolicyList}
                            />
                        </Dialog>
                    </React.Fragment>
                )}
            </div>
        );
    }
}

Credentials.propTypes = {
    classes: PropTypes.shape({
        contentWrapper: PropTypes.shape({}),
        titleSub: PropTypes.shape({}),
        tableMain: PropTypes.shape({}),
        th: PropTypes.shape({}),
    }).isRequired,
    history: PropTypes.shape({
        location: PropTypes.shape({
            state: PropTypes.shape({
                openWizard: PropTypes.bool.isRequired,
            }).isRequired,
            pathname: PropTypes.string.isRequired,
        }).isRequired,
        replace: PropTypes.func.isRequired,
    }).isRequired,
    intl: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Credentials));
