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
import classnames from 'classnames';
import { Link } from 'react-router-dom';

import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Select from '@material-ui/core/Select';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Slide from '@material-ui/core/Slide';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import Icon from '@material-ui/core/Icon';
import Grid from '@material-ui/core/Grid';
import PropTypes from 'prop-types';
import Collapse from '@material-ui/core/Collapse';
import { FormattedMessage, injectIntl } from 'react-intl';
import Api from '../../../data/api';
import ResourceNotFound from '../../Base/Errors/ResourceNotFound';
import Loading from '../../Base/Loading/Loading';

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
        const { intl } = this.props;
        this.refs.notificationSystem.addNotification({
            message: intl.formatMessage({
                defaultMessage: 'Subscribe to API successfully',
                id: 'Apis.Details.Subscribe.subscribe.successfull',
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
                        id: 'Apis.Details.Subscribe.created.successfully.with.id',
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
                        id: 'Apis.Details.Subscribe.error',
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

        return this.state.api ? (
            <div className={classes.root}>
                <IconButton
                    className={classnames(classes.expand, {
                        [classes.expandOpen]: this.state.expanded,
                    })}
                    onClick={this.handleExpandClick}
                    aria-expanded={this.state.expanded}
                    aria-label={intl.formatMessage({
                        defaultMessage: 'Show more',
                        id: 'Apis.Details.Subscribe.show.more',
                    })}
                >
                    <Icon>expand_more</Icon>
                </IconButton>
                <Collapse in={this.state.expanded} timeout='auto' unmountOnExit>
                    asdfsdf
                </Collapse>

                {this.state.applications && this.state.applications.length > 0 ? (
                    <div>
                        <Typography variant='h5' className={classes.headline}>
                            <FormattedMessage
                                id='Apis.Details.Subscribe.test.this.api'
                                defaultMessage='Test this API?'
                            />
                        </Typography>

                        <Typography gutterBottom>
                            <FormattedMessage
                                id='Apis.Details.Subscribe.test.description'
                                defaultMessage={`Create an application and subscribe to this API.
                                                An application is a logical collection of APIs.
                                                Applications allow you to use a single access token to invoke a collection
                                                of APIs and to subscribe to one API multiple times with different SLA levels.
                                                The DefaultApplication is pre-created and allows unlimited access by default.`}
                            />
                        </Typography>

                        <Button
                            onClick={this.handleAppDialogOpen}
                            color='primary'
                            variant='raised'
                            className='form-buttons full-width'
                        >
                            {' '}
                            <FormattedMessage
                                id='Apis.Details.Subscribe.create.new.application'
                                defaultMessage='Create New Application'
                            />
                        </Button>
                    </div>
                ) : (
                    <div>
                        <Typography variant='h5' className={classes.headline}>
                            <FormattedMessage
                                id='Apis.Details.Subscribe.subscriptions.title'
                                defaultMessage='Subscriptions'
                            />
                        </Typography>

                        {this.state.applicationsAvailable && this.state.applicationsAvailable.length > 0 ? (
                            <div>
                                <FormControl className={classes.formControl}>
                                    <InputLabel>
                                        <FormattedMessage
                                            id='Apis.Details.Subscribe.applications.title'
                                            defaultMessage='Applications'
                                        />
                                    </InputLabel>
                                    <Select
                                        value={this.state.applicationId}
                                        onChange={this.handleChange('applicationId')}
                                    >
                                        {this.state.applicationsAvailable.map(app => (
                                            <option value={app.value} key={app.value}>
                                                {app.label}
                                            </option>
                                        ))}
                                    </Select>
                                </FormControl>
                                {this.state.tiers && (
                                    <FormControl className={classes.formControl}>
                                        <Select
                                            value={this.state.policyName}
                                            onChange={this.handleChange('policyName')}
                                        >
                                            {this.state.tiers.map(tier => (
                                                <option value={tier.value} key={tier.value}>{tier.label}</option>
                                            ))}
                                        </Select>
                                    </FormControl>
                                )}
                                <Button
                                    onClick={this.createSubscription}
                                    color='primary'
                                    variant='raised'
                                    className='form-buttons full-width'
                                >
                                    {' '}
                                    <FormattedMessage
                                        id='Apis.Details.Subscribe.subscribe.to.this'
                                        defaultMessage='Subscribed to this API'
                                    />
                                </Button>
                            </div>
                        ) : (
                            <div>
                                <Typography gutterBottom>
                                    <FormattedMessage
                                        id='Apis.Details.Subscribe.subscribed.to.all'
                                        defaultMessage='You have subscribed to all the available applications. You need to create a
                                                                        new application to subscribe again to this API.'
                                    />
                                </Typography>
                                <Button
                                    onClick={this.handleAppDialogOpen}
                                    color='primary'
                                    variant='raised'
                                    className='form-buttons full-width'
                                >
                                    <FormattedMessage
                                        id='Apis.Details.Subscribe.create.new'
                                        defaultMessage='Create New Application'
                                    />
                                </Button>
                            </div>
                        )}

                        {this.state.subscribedApplications && this.state.subscribedApplications.length > 0 && (
                            <div>
                                <Typography variant='caption' className={classes.subtitle}>
                                    <FormattedMessage
                                        id='Apis.Details.Subscribe.showing.two.subscribed.applications'
                                        defaultMessage='Subscribed Applications'
                                    />
                                    {this.state.subscribedApplications && this.state.subscribedApplications.length > 2 && (
                                        <span>
                                            -
                                            <FormattedMessage
                                                id='Apis.Details.Subscribe.showing.two.out.of'
                                                defaultMessage='Showing 2 out of'
                                            />
                                            {this.state.subscribedApplications.length}
-
                                            <Link to='/applications' className={classes.viewAllLink}>
                                                <FormattedMessage
                                                    id='Apis.Details.Subscribe.view.all.link'
                                                    defaultMessage='View All'
                                                />
                                            </Link>
                                        </span>
                                    )}
                                </Typography>
                                {this.state.subscribedApplications
                                    .slice(0)
                                    .reverse()
                                    .map(
                                        (app, index) => index < 2 && (
                                            <div className={classes.appListWrapper} key={index}>
                                                <Link
                                                    to={'/applications/' + app.value}
                                                    key={app.value}
                                                    className={classes.appLink}
                                                >
                                                    <span className={classes.applicationName}>{app.label}</span>
                                                    <span className={classes.caption}>
                                                        - (
                                                        {app.policy}
                                                        )
                                                    </span>
                                                </Link>
                                            </div>
                                        ),
                                    )}
                            </div>
                        )}
                    </div>
                )}

                {/* Application creation container */}
                <Dialog fullScreen open={this.state.createAppOpen} onClose={this.handleClose} transition={Transition}>
                    <Grid container>
                        <Grid item xs={1} className={classes.closeButton}>
                            <IconButton
                                color='inherit'
                                onClick={this.handleAppDialogClose}
                                aria-label={intl.formatMessage({
                                    defaultMessage: 'Close',
                                    id: 'Apis.Details.Subscribe.close.label',
                                })}
                            >
                                <Icon>close</Icon>
                            </IconButton>
                        </Grid>
                        <Grid item xs={11} className={classes.applicationCreateRoot} />
                    </Grid>
                </Dialog>

                {/* Dialog to show once user have subscribed. */}
                <Dialog
                    open={this.state.openSubsConfirm}
                    onClose={this.handleCloseSubsConfirm}
                    aria-labelledby='alert-dialog-title'
                    aria-describedby='alert-dialog-description'
                >
                    <DialogTitle id='alert-dialog-title'>
                        <FormattedMessage
                            id='Apis.Details.Subscribe.use.google.location.service'
                            defaultMessage="Use Google's location service?"
                        />
                    </DialogTitle>
                    <DialogContent>
                        <DialogContentText id='alert-dialog-description'>
                            <FormattedMessage
                                id='Apis.Details.Subscribe.created.success'
                                defaultMessage='Successfully created application.'
                            />
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Link to={'/applications/' + this.state.applicationId}>
                            <Button color='primary'>
                                <FormattedMessage
                                    id='Apis.Details.Subscribe.go.application'
                                    defaultMessage='Go to application page.'
                                />
                            </Button>
                        </Link>
                        <Button onClick={this.handleCloseSubsConfirm} color='primary' autoFocus>
                            <FormattedMessage
                                id='Apis.Details.Subscribe.stay.on.the.api.details.page'
                                defaultMessage='Stay on the API details page.'
                            />
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        ) : (
            <Loading />
        );
    }
}

Subscribe.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default injectIntl(withStyles(styles)(Subscribe));
