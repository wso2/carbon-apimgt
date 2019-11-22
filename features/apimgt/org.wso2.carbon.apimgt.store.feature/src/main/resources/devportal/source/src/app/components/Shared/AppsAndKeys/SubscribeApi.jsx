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
import { withStyles } from '@material-ui/core/styles';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Slide from '@material-ui/core/Slide';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import Api from '../../../data/api';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
/**
 *
 *
 * @param {*} theme
 */
const styles = theme => ({
    media: {
        height: 200,
    },
    root: {
        paddingLeft: 20,
        width: '100%',
        flex: 1,
    },
    listApps: {
        width: '100%',
        backgroundColor: theme.palette.background.paper,
        position: 'relative',
        overflow: 'auto',
        maxHeight: 300,
    },
    listSection: {
        backgroundColor: 'inherit',
    },
    ul: {
        backgroundColor: 'inherit',
        padding: 0,
    },
    appListItem: {
        paddingLeft: 0,
        marginLeft: 20,
    },
    appBar: {
        position: 'relative',
    },
    flex: {
        flex: 1,
    },
    applicationCreateRoot: {
        marginLeft: 40,
        marginRight: 40,
    },
    closeButton: {
        marginLeft: 10,
        marginRight: 10,
    },
    caption: {
        color: theme.palette.text.secondary,
    },
    applicationName: {
        color: theme.palette.text.primary,
    },
    formControl: {
        paddingRight: 20,
        marginBottom: 10,
        width: 200,
    },
    subtitle: {
        marginTop: 20,
    },
    appLink: {
        textDecoration: 'none',
    },
    viewAllLink: {
        color: theme.palette.text.secondary,
    },
    expand: {
        transform: 'rotate(0deg)',
        transition: theme.transitions.create('transform', {
            duration: theme.transitions.duration.shortest,
        }),
        marginLeft: 'auto',
        [theme.breakpoints.up('sm')]: {
            marginRight: -8,
        },
    },
    expandOpen: {
        transform: 'rotate(180deg)',
    },
});
/**
 *
 *
 * @param {*} props
 * @returns
 */
function Transition(props) {
    return <Slide direction='up' {...props} />;
}
/**
 *
 *
 * @class Subscribe
 * @extends {Component}
 */
class Subscribe extends Component {
    /**
     *Creates an instance of Subscribe.
     * @param {*} props
     * @memberof Subscribe
     */
    constructor(props) {
        super(props);
        this.state = {
            api: null,
            applications: null,
            policies: null,
            dropDownApplications: null,
            dropDownPolicies: null,
            notFound: false,
            openSubscribeMenu: false,
            matDropVisible: false,
            matDropValue: 'one',
            subscribedApplications: [],
            applicationsAvailable: [],
            tiers: [],
            applicationId: '',
            policyName: '',
            openPopup: false,
            anchorEl: null,
            createAppOpen: false,
            openSubsConfirm: false,
            expaned: false,
        };
        this.api_uuid = this.props.api_uuid;
        this.logChange = this.logChange.bind(this);
        this.openSubscribeMenu = this.openSubscribeMenu.bind(this);
        this.closeSubscribeMenu = this.closeSubscribeMenu.bind(this);
        this.handlePopupOpen = this.handlePopupOpen.bind(this);
        this.handlePopupClose = this.handlePopupClose.bind(this);
        this.updateSubscriptionData = this.updateSubscriptionData.bind(this);
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    updateSubscriptionData() {
        const api = new Api();
        const promised_api = api.getAPIById(this.api_uuid);
        const existing_subscriptions = api.getSubscriptions(this.api_uuid, null);
        const promised_applications = api.getAllApplications();

        Promise.all([promised_api, existing_subscriptions, promised_applications])
            .then((response) => {
                const [api, subscriptions, applications] = response.map(data => data.obj);

                // Getting the policies from api details
                this.setState({ api });
                if (api && api.policies) {
                    const apiTiers = api.policies;
                    const tiers = [];
                    for (let i = 0; i < apiTiers.length; i++) {
                        const tierName = apiTiers[i];
                        tiers.push({ value: tierName, label: tierName });
                    }
                    this.setState({ tiers });
                    if (tiers.length > 0) {
                        this.setState({ policyName: tiers[0].value });
                    }
                }

                const subscribedApplications = [];
                // get the application IDs of existing subscriptions
                subscriptions.list.map(element => subscribedApplications.push({ value: element.applicationId, policy: element.policy }));
                this.setState({ subscribedApplications });

                // Removing subscribed applications from all the applications and get the available applications to subscribe
                const applicationsAvailable = [];
                for (let i = 0; i < applications.list.length; i++) {
                    const applicationId = applications.list[i].applicationId;
                    const applicationName = applications.list[i].name;
                    // include the application only if it does not has an existing subscriptions
                    let applicationSubscribed = false;
                    for (let j = 0; j < subscribedApplications.length; j++) {
                        if (subscribedApplications[j].value === applicationId) {
                            applicationSubscribed = true;
                            subscribedApplications[j].label = applicationName;
                        }
                    }
                    if (!applicationSubscribed) {
                        applicationsAvailable.push({ value: applicationId, label: applicationName });
                    }
                }
                this.setState({ applicationsAvailable });
                if (applicationsAvailable && applicationsAvailable.length > 0) {
                    this.setState({ applicationId: applicationsAvailable[0].value });
                }
            })
            .catch((error) => {
                if (process.env.NODE_ENV !== 'production') {
                    console.log(error);
                }
                const status = error.status;
                if (status === 404) {
                    this.setState({ notFound: true });
                }
            });
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    componentDidMount() {
        this.updateSubscriptionData();
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    handleChange = name => (event) => {
        this.setState({ [name]: event.target.value });
    };

    /**
     *
     *
     * @memberof Subscribe
     */
    addNotifications() {
        const intl = this.props;
        this.refs.notificationSystem.addNotification({
            message: intl.formatMessage({
                defaultMessage: 'Subscribe to API successfully',
                id: 'Shared.AppsAndKeys.SubscribeApi.subscribe.to.api',
            }),
            position: 'tc',
            level: 'success',
        });
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    handleExpandClick = () => {
        this.setState(state => ({ expanded: !state.expanded }));
    };

    /**
     *
     *
     * @memberof Subscribe
     */
    createSubscription = (e) => {
        e.preventDefault();
        const { intl } = this.props;
        const apiId = this.api_uuid;
        const applicationId = this.state.applicationId;
        const policy = this.state.policyName;
        const api = new Api();
        const promised_subscribe = api.subscribe(apiId, applicationId, policy);
        promised_subscribe
            .then((response) => {
                console.log(
                    intl.formatMessage({
                        defaultMessage: 'Subscription created successfully with ID : ',
                        id: 'Shared.AppsAndKeys.SubscribeApi.subscription.created',
                    }) + response.body.subscriptionId,
                );
                // this.addNotifications();
                this.updateSubscriptionData();
                this.setState({ openSubsConfirm: true });
            })
            .catch((error) => {
                console.log(
                    intl.formatMessage({
                        defaultMessage: 'Error while creating the subscription.',
                        id: 'Shared.AppsAndKeys.SubscribeApi.error.while.creating',
                    }),
                );
                console.error(error);
            });
    };

    /**
     *
     *
     * @memberof Subscribe
     */
    handleClick() {
        this.setState({ redirect: true });
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    openSubscribeMenu() {
        this.setState({ openSubscribeMenu: true });
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    closeSubscribeMenu() {
        this.setState({ openSubscribeMenu: false });
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    handlePopupClose() {
        this.setState({ openPopup: false });
    }

    /**
     *
     *
     * @param {*} event
     * @memberof Subscribe
     */
    handlePopupOpen(event) {
        this.setState({ openPopup: true, anchorEl: event.currentTarget });
    }

    /**
     *
     *
     * @param {*} e
     * @memberof Subscribe
     */
    onBlur(e) {
        if (!e.currentTarget.contains(document.activeElement)) {
            this.setState({ matDropVisible: false });
        }
    }

    /**
     *
     *
     * @param {*} val
     * @memberof Subscribe
     */
    logChange(val) {
        this.setState({ matDropValue: val.value });
        console.log('Selected: ' + JSON.stringify(val));
    }

    /**
     *
     *
     * @memberof Subscribe
     */
    handleAppDialogOpen = () => {
        this.setState({ createAppOpen: true });
    };

    /**
     *
     *
     * @memberof Subscribe
     */
    handleAppDialogClose = () => {
        this.setState({ createAppOpen: false });
    };

    /**
     *
     *
     * @memberof Subscribe
     */
    handleCloseSubsConfirm = () => {
        this.setState({ openSubsConfirm: false });
    };

    /**
     *
     *
     * @returns
     * @memberof Subscribe
     */
    render() {
        if (this.state.notFound) {
            return <ResourceNotFound />;
        }
        if (this.state.redirect) {
            return <Redirect push to='/application-create' />;
        }
        const { classes } = this.props;
        const api = this.state.api;

        return (
            this.state.applicationsAvailable
            && this.state.applicationsAvailable.length > 0 && (
                <Grid container spacing={3} className={classes.root}>
                    <Grid item xs={12} md={6}>
                        <FormControl className={classes.FormControl}>
                            <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                                <FormattedMessage
                                    defaultMessage='Application'
                                    id='Shared.AppsAndKeys.SubscribeApi.application'
                                />
                            </InputLabel>
                            <Select value={this.state.applicationId} onChange={this.handleChange('applicationId')}>
                                {this.state.applicationsAvailable.map(app => (
                                    <option value={app.value} key={app.value}>
                                        {app.label}
                                    </option>
                                ))}
                            </Select>
                            <FormattedMessage
                                defaultMessage='Label + placeholder'
                                id='Shared.AppsAndKeys.SubscribeApi.label.placeholder'
                            />
                            <FormHelperText />
                        </FormControl>
                        {this.state.tiers && (
                            <FormControl className={classes.FormControlOdd}>
                                <InputLabel shrink htmlFor='age-label-placeholder' className={classes.quotaHelp}>
                                    <FormattedMessage
                                        defaultMessage='Throttling Tier'
                                        id='Shared.AppsAndKeys.SubscribeApi.throttling.tier'
                                    />
                                </InputLabel>
                                <Select value={this.state.policyName} onChange={this.handleChange('policyName')}>
                                    {this.state.tiers.map(tier => (
                                        <option value={tier.value} key={tier.value}>{tier.label}</option>
                                    ))}
                                </Select>
                                <FormattedMessage
                                    defaultMessage='Label + placeholder'
                                    id='Shared.AppsAndKeys.SubscribeApi.label.placeholder.other'
                                />
                                <FormHelperText />
                            </FormControl>
                        )}
                    </Grid>
                </Grid>
            )
        );
    }
}

Subscribe.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(Subscribe);
