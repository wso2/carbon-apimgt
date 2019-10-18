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
import Api from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import { FormattedMessage, injectIntl } from 'react-intl';
import Application from 'AppData/Application';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';
import { ScopeValidation, resourceMethods, resourcePaths } from 'AppComponents/Shared/ScopeValidation';
import { ApiContext } from '../ApiContext';
import SubscriptionTableRow from './SubscriptionTableRow';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
    contentWrapper: {
        maxWidth: theme.custom.contentAreaWidth,
        paddingLeft: theme.spacing(3),
        paddingTop: theme.spacing(3),
    },
    titleSub: {
        marginLeft: theme.spacing(2),
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    generateCredentialWrapper: {
        marginLeft: 0,
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(2),
    },
    tableMain: {
        width: '100%',
        borderCollapse: 'collapse',
        marginTop: theme.spacing(3),
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(1),

    },
    th: {
        color: theme.palette.getContrastText(theme.palette.background.default),
        borderBottom: 'solid 1px ' + theme.palette.grey.A200,
        borderTop: 'solid 1px ' + theme.palette.grey.A200,
        textAlign: 'left',
        fontSize: '11px',
        paddingLeft: theme.spacing(1),
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
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
        marginLeft: theme.spacing(2),
        padding: theme.spacing(2),
    },
    descWrapper: {
        marginBottom: theme.spacing(2),
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
});

/**
 * @class Credentials
 * @extends {React.Component}
 */
class Credentials extends React.Component {
    static contextType = ApiContext;

    state = {
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
        this.setState(state => ({ expanded: !state.expanded }));
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
        api.subscribe(
            subscriptionRequest.apiId,
            subscriptionRequest.applicationId,
            subscriptionRequest.throttlingPolicy,
            apiType,
        )
            .then((response) => {
                console.log('Subscription created successfully with ID : ' + response.body.subscriptionId);
                Alert.info(intl.formatMessage({
                    defaultMessage: 'Subscribed successfully',
                    id: 'Apis.Details.Credentials.Credentials.subscribed.successfully',
                }));
                if (updateSubscriptionData) updateSubscriptionData(this.updateData);
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
                this.setState({ applicationOwner: result.owner });
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
            if (updateSubscriptionData) updateSubscriptionData();
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
        } = this.state;
        return (
            <Grid container>
                <Grid item md={12} lg={11}>
                    <Grid container spacing={5}>
                        <Grid item md={12}>
                            <Typography onClick={this.handleExpandClick} variant='h4' className={classes.titleSub}>
                                <FormattedMessage
                                    id='Apis.Details.Credentials.Credentials.api.credentials'
                                    defaultMessage='API Credentials'
                                />
                            </Typography>
                            <Paper elevation={0} className={classes.paper}>
                                <Typography variant='body2' className={classes.descWrapper}>
                                    <FormattedMessage
                                        id='Apis.Details.Credentials.Credentials.'
                                        defaultMessage={`API Credentials are grouped in to applications. An application 
                                        is primarily used to decouple the consumer from the APIs. It allows you to 
                                        generate and use a single key for multiple APIs and subscribe multiple times to 
                                        a single API with different SLA levels.`}
                                    />
                                </Typography>

                                {applicationsAvailable.length === 0 && subscribedApplications.length === 0 ? (
                                    <GenericDisplayDialog
                                        classes={classes}
                                        handleClick={this.goToWizard}
                                        heading={intl.formatMessage({
                                            defaultMessage: 'Generate Credentials',
                                            id: 'Apis.Details.Credentials.Credentials.generate.credentials',
                                        })}
                                        caption={intl.formatMessage({
                                            defaultMessage: 'You need to generate credentials to access this API',
                                            id:
                                                    'Apis.Details.Credentials.Credentials.you.need.to'
                                                    + '.generate.credentials.to.access.this.api',
                                        })}
                                        buttonText={intl.formatMessage({
                                            defaultMessage: 'GENERATE',
                                            id: 'Apis.Details.Credentials.Credentials.generate',
                                        })}
                                    />
                                ) : (
                                    <React.Fragment>
                                        <div className={classes.generateCredentialWrapper}>
                                            <ScopeValidation
                                                resourcePath={resourcePaths.SUBSCRIPTIONS}
                                                resourceMethod={resourceMethods.POST}
                                            >
                                                <Typography variant='h5'>
                                                    <FormattedMessage
                                                        id={'Apis.Details.Credentials.Credentials'
                                                        + '.api.credentials.generate'}
                                                        defaultMessage='Generate Credentials'
                                                    />
                                                </Typography>
                                                <div className={classes.credentialBoxWrapper}>
                                                    <div className={classes.credentialBox}>
                                                        <Typography variant='body2'>
                                                            <FormattedMessage
                                                                id={'Apis.Details.Credentials.Credentials.'
                                                                + 'api.credentials.with.wizard.message'}
                                                                defaultMessage={
                                                                    'Use the Key Generation Wizard. '
                                                                    + 'Create a new application -> '
                                                                    + 'Subscribe -> Generate keys and '
                                                                    + 'Access Token to invoke this API.'
                                                                }
                                                            />
                                                        </Typography>
                                                        <Link
                                                            to={`/apis/${api.id}/credentials/wizard`}
                                                            style={!api.isSubscriptionAvailable ? { pointerEvents: 'none' } : null}
                                                        >
                                                            <Button
                                                                variant='contained'
                                                                color='primary'
                                                                className={classes.buttonElm}
                                                                disabled={!api.isSubscriptionAvailable}
                                                            >
                                                                <FormattedMessage
                                                                    id={'Apis.Details.Credentials.' +
                                                                'SubscibeButtonPanel.subscribe.wizard'}
                                                                    defaultMessage='Wizard'
                                                                />
                                                            </Button>
                                                        </Link>
                                                    </div>
                                                    {applicationsAvailable.length > 0 && (
                                                        <div className={classes.credentialBox}>
                                                            <Typography variant='body2'>
                                                                <FormattedMessage
                                                                    id={'Apis.Details.Credentials.Credentials' +
                                                                    '.api.credentials.with.subscribe.message'}
                                                                    defaultMessage={'Subscribe to an application' +
                                                                    ' and generate credentials'}
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
                                                                disabled={!api.isSubscriptionAvailable}
                                                            >
                                                                <FormattedMessage
                                                                    id={'Apis.Details.Credentials.'
                                                                    + 'SubscibeButtonPanel.subscribe.btn'}
                                                                    defaultMessage='Subscribe'
                                                                />
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
                                            <React.Fragment>
                                                <Typography variant='h5'>
                                                    <FormattedMessage
                                                        id={'Apis.Details.Credentials.Credentials.' +
                                                        'api.credentials.subscribed.apps.title'}
                                                        defaultMessage='View Credentials'
                                                    />
                                                </Typography>
                                                <Typography variant='body2'>
                                                    <FormattedMessage
                                                        id={'Apis.Details.Credentials.Credentials.' +
                                                        'api.credentials.subscribed.apps.description'}
                                                        defaultMessage='( Subscribed Applications )'
                                                    />
                                                </Typography>
                                                <table className={classes.tableMain}>
                                                    <tr>
                                                        <th className={classes.th}>
                                                            <FormattedMessage
                                                                id={'Apis.Details.Credentials.Credentials.' +
                                                                'api.credentials.subscribed.apps.name'}
                                                                defaultMessage='Application Name'
                                                            />
                                                        </th>
                                                        <th className={classes.th}>
                                                            <FormattedMessage
                                                                id={'Apis.Details.Credentials.Credentials.api.' +
                                                                'credentials.subscribed.apps.tier'}
                                                                defaultMessage='Throttling Tier'
                                                            />
                                                        </th>
                                                        <th className={classes.th}>
                                                            <FormattedMessage
                                                                id={'Apis.Details.Credentials.Credentials.' +
                                                                'api.credentials.subscribed.apps.status'}
                                                                defaultMessage='Application Status'
                                                            />
                                                        </th>
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
                                                            applicationOwner={applicationOwner}
                                                        />
                                                    ))}
                                                </table>
                                            </React.Fragment>
                                        )}
                                    </React.Fragment>
                                )}
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
    }).isRequired,
    history: PropTypes.shape({
        location: PropTypes.shape({
            state: PropTypes.shape({
                openWizard: PropTypes.bool.isRequired,
            }).isRequired,
            pathname: PropTypes.string.isRequired,
        }).isRequired,
        replace: PropTypes.func.isRequired,
        push: PropTypes.func.isRequired,
    }).isRequired,
    location: PropTypes.shape({
        state: PropTypes.shape({
            openWizard: PropTypes.bool.isRequired,
        }).isRequired,
        pathname: PropTypes.string.isRequired,
    }).isRequired,
    intl: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(Credentials));
