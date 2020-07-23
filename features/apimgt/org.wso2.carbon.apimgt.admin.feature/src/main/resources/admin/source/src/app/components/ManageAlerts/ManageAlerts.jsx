/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect, useState } from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { Link } from 'react-router-dom';
import Joi from '@hapi/joi';
import { withStyles } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Checkbox from '@material-ui/core/Checkbox';
import CircularProgress from '@material-ui/core/CircularProgress';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import DescriptionIcon from '@material-ui/icons/Description';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import Alert from 'AppComponents/Shared/Alert';
import API from 'AppData/api';
import Configurations from 'Config';
import ChipInput from 'material-ui-chip-input';
import PropTypes from 'prop-types';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';

const styles = (theme) => ({
    alertsWrapper: {
        padding: theme.spacing(2),
        '& span, & h5, & label, & input, & td, & li': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
        marginBottom: theme.spacing(15),
    },
    manageAlertHeading: {
        marginBottom: theme.spacing(),
    },
    chipInput: {
        width: '100%',
        marginTop: theme.spacing(2),
        marginBottom: theme.spacing(2),
    },
    alertConfigDialog: {
        width: '60%',
    },
    configDialogHeading: {
        fontWeight: '600',
    },
    listItem: {
        marginLeft: theme.spacing(1),
    },
});

/**
 * Alerts management component.
 *
 * @param {any} props The Input props.
 * @return {any} HTML representation of the component.
 * */
const ManageAlerts = (props) => {
    const {
        classes,
        intl,
    } = props;
    const [emails, setEmailsList] = useState([]);
    const [supportedAlerts, setSupportedAlerts] = useState();
    const [subscribedAlerts, setSubscribedAlerts] = useState([]);
    const [isAnalyticsEnabled, setAnalyticsEnabled] = useState();
    const [isInProgress, setInProgress] = useState({ subscribing: false, unSubscribing: false });
    const [unsubscribeAll, setUnsubscribeAll] = useState(false);
    const api = new API();

    const alertIdMapping = {
        1: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.response.time.name',
                defaultMessage: 'Abnormal Response Time',
            }),
            displayName: 'AbnormalResponseTime',
            description: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.response.time.description',
                defaultMessage: 'This alert gets triggered if the response time of a particular API '
                + 'is higher than the predefined value. These alerts could be treated as an indication '
                + 'of a slow WSO2 API Manager runtime or a slow backend.',
            }),
        },
        2: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.backend.time.name',
                defaultMessage: 'Abnormal Backend Time',
            }),
            displayName: 'AbnormalBackendTime',
            description: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.backend.time.description',
                defaultMessage: 'This alert gets triggered if the backend time corresponding to a '
                + 'particular API is higher than the predefined value. These alerts could be treated '
                + 'as an indication of a slow backend. In technical terms, if the backend time of a '
                + 'particular API of a tenant lies outside the predefined value, an alert will be sent out.',
            }),
        },
        3: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.request.count.name',
                defaultMessage: 'Abnormal Request Count',
            }),
            displayName: 'AbnormalRequestsPerMin',
            description: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.request.count.description',
                defaultMessage: 'This alert is triggered if there is a sudden spike the request count within '
                + 'a period of one minute by default for a particular API for an application. '
                + 'These alerts could be treated as an indication of a possible high traffic, '
                + 'suspicious activity, possible malfunction of the client application, etc.',
            }),
        },
        4: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.request.access.name',
                defaultMessage: 'Abnormal Resource Access',
            }),
            displayName: 'AbnormalRequestPattern',
            description: intl.formatMessage({
                id: 'Manage.Alerts.abnormal.request.access.description',
                defaultMessage: 'This alert is triggered if there is a change in the resource access pattern '
                + 'of a user of a particular application. These alerts could be treated as an indication of a '
                + 'suspicious activity by a user over your application.',
            }),
        },
        5: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.unseen.source.ip.address.name',
                defaultMessage: 'Unseen Source IP Access',
            }),
            displayName: 'UnusualIPAccess',
            description: intl.formatMessage({
                id: 'Manage.Alerts.unseen.source.ip.address.description',
                defaultMessage: 'This alert is triggered if there is either a change in the request source IP '
                + 'for a particular application by a user or if the request is from an IP used before a '
                + 'time period of 30 days (default). These alerts could be treated as an indication of a '
                + 'suspicious activity by a user over an application.',
            }),
        },
        6: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.tier.crossing.name',
                defaultMessage: 'Tier Crossing',
            }),
            displayName: 'FrequentTierLimitHitting',
            description: intl.formatMessage({
                id: 'Manage.Alerts.tier.crossing.description',
                defaultMessage: 'This alert is triggered if at least one of the two cases below are '
                + 'satisfied; if a particular application gets throttled out for hitting the '
                + 'subscribed tier limit of that application more than 10 times (by default) '
                + 'within an hour (by default) or if a particular user of an application gets '
                + 'throttled out for hitting the subscribed tier limit of a particular API more '
                + 'than 10 times (by default) within a day (by default)',
            }),
        },
        7: {
            name: intl.formatMessage({
                id: 'Manage.Alerts.health.availability.name',
                defaultMessage: 'Health Availability',
            }),
            displayName: 'ApiHealthMonitor',
            description: intl.formatMessage({
                id: 'Manage.Alerts.health.availability.description',
                defaultMessage: 'This alert gets triggered if at least one of the two cases below are '
                + 'satisfied; Response time of an API greater than Threshold response time value defined '
                + 'for that particular API or Response status code is in between 500 and 600 (By Default)',
            }),
        },
    };

    /**
     * Checks whether the provided alert is aubscribed.
     *
     * @param {number} alertId : The id of the alert.
     * @return {boolean} True if the alert is subscribed. False otherwise.
     * */
    const isAlertSubscribed = (alertId) => {
        return subscribedAlerts.some((alert) => { return alert.id === alertId; });
    };

    /**
     * Handles the alert type select event.
     *
     * @param {obj} alert : The selected alert.
     * */
    const handleCheckAlert = (alert) => {
        const alertId = alert.id;
        let tmpSubscribedAlerts = [...subscribedAlerts];
        if (isAlertSubscribed(alertId)) {
            tmpSubscribedAlerts = tmpSubscribedAlerts.filter((sub) => {
                return sub.id !== alertId;
            });
        } else {
            const newAlert = { id: alertId, name: alertIdMapping[alertId].displayName };
            tmpSubscribedAlerts.push(newAlert);
        }
        setSubscribedAlerts(tmpSubscribedAlerts);
    };

    const validateEmail = (value) => {
        const schema = Joi.string().email().empty();
        const validationError = schema.validate(value).error;

        if (validationError) {
            const errorType = validationError.details[0].type;
            if (errorType === 'string.email') {
                return 'Invalid Email: ' + value;
            }
        }
        return false;
    };

    const validateEmailList = (values) => {
        for (const email in values) {
            if (validateEmail(values[email])) {
                return validateEmail(values[email]);
            }
        }
        return false;
    };

    /**
     * Handles the add email event.
     *
     * @param {string} email The email address that is being added.
     * */
    const handleAddEmail = (email) => {
        setEmailsList(email);
    };

    /**
     * Handles the email deletion event.
     *
     * @param {string} email : The email that is being deleted.
     * */
    const handleEmailDeletion = (email) => {
        const newEmails = emails.filter((oldEmail) => {
            return oldEmail !== email;
        });
        setEmailsList(newEmails);
    };

    useEffect(() => {
        const supportedAlertsPromise = api.getSupportedAlertTypes();
        const subscribedAlertsPromise = api.getSubscribedAlertTypesByUser();
        Promise.all([supportedAlertsPromise, subscribedAlertsPromise]).then((response) => {
            const [supportedAlertsResponse, subscribedAlertsResponse] = response;
            if (supportedAlertsResponse === 204 || subscribedAlertsResponse === 204) {
                setAnalyticsEnabled(false);
            } else {
                setAnalyticsEnabled(true);
                setSubscribedAlerts(subscribedAlertsResponse.body.alerts);
                setEmailsList(subscribedAlertsResponse.body.emailList);
                setSupportedAlerts(supportedAlertsResponse.body.alerts);
            }
        }).catch((err) => {
            setAnalyticsEnabled(false);
            setSubscribedAlerts({});
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Manage.Alerts.loading.error.msg',
                defaultMessage: 'Error occurred while loading alerts',
            }));
        });
    }, []);

    /**
     * Handles the subscribe button click action.
     * */
    const handleSubscribe = () => {
        const validationErrors = validateEmailList(emails);
        if (validationErrors) {
            Alert.error(validationErrors);
            return;
        }
        setInProgress({ subscribing: true });
        const alertsToSubscribe = { alerts: subscribedAlerts, emailList: emails };
        api.subscribeAlerts(alertsToSubscribe).then(() => {
            Alert.success(intl.formatMessage({
                id: 'Manage.Alerts.subscribe.success.msg',
                defaultMessage: 'Subscribed to Alerts Successfully.',
            }));
        }).catch((err) => {
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Manage.Alerts.subscribe.error.msg',
                defaultMessage: 'Error occurred while subscribing to alerts.',
            }));
        }).finally(() => setInProgress({ subscribing: false }));
    };

    /**
     * Handles unsubscribe button click action.
     * */
    const handleUnSubscribe = () => {
        setInProgress({ unSubscribing: true });
        api.unsubscribeAlerts().then(() => {
            setSubscribedAlerts([]);
            setEmailsList([]);
            Alert.success(intl.formatMessage({
                id: 'Manage.Alerts.unsubscribe.success.msg',
                defaultMessage: 'Unsubscribed from all alerts successfully.',
            }));
        }).catch((err) => {
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Manage.Alerts.unsubscribe.error.msg',
                defaultMessage: 'Error occurred while unsubscribing.',
            }));
        }).finally(() => setInProgress({ unSubscribing: false }));
    };

    const pageProps = {
        help: (
            <HelpBase>
                <List component='nav' aria-label='main mailbox folders'>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <a
                            rel='noopener noreferrer'
                            href={
                                Configurations.app.docUrl
                                + 'learn/analytics/managing-alerts-with-real-time-analytics/'
                                + 'subscribing-for-alerts/#system-administrators'
                            }
                            target='_blank'
                        >
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        id='Manage.Alerts.help.link.one'
                                        defaultMessage='Subscribe to alerts'
                                    />
                                )}
                            />
                        </a>
                    </ListItem>
                </List>
            </HelpBase>
        ),
        pageStyle: 'full',
        title: intl.formatMessage({
            id: 'Manage.Alerts.title',
            defaultMessage: 'Manage Alert Subscriptions',
        }),
    };

    const analyticsDisabledEmptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Manage.Alerts.analytics.disabled.empty.content'
                    values={{
                        breakingLine: <br />,
                    }}
                    defaultMessage={'A system administrator is allowed to select '
                    + 'one or more alert types to subscribe for, as well as specify a '
                    + 'list of email addresses to which the alerts should be sent. '
                    + '{breakingLine}{breakingLine}'
                    + 'Please enable Analytics for subscribing to Alerts'}
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Manage.Alerts.analytics.disabled.empty.title'
                    defaultMessage='Analytics disabled!'
                />
            </Typography>
        ),
    };

    if (isAnalyticsEnabled === undefined) {
        return (
            <ContentBase pageStyle='paperLess'>
                <InlineProgress />
            </ContentBase>
        );
    }

    return (
        <>
            {!isAnalyticsEnabled
                ? (
                    <ContentBase
                        {...pageProps}
                        pageStyle='small'
                    >
                        <Card>
                            <CardContent>
                                {analyticsDisabledEmptyBoxProps.title}
                                {analyticsDisabledEmptyBoxProps.content}
                            </CardContent>
                        </Card>
                    </ContentBase>
                )
                : (
                    <ContentBase
                        {...pageProps}
                    >
                        <div className={classes.alertsWrapper}>
                            {!supportedAlerts
                                ? <CircularProgress />
                                : (
                                    <>
                                        <Typography variant='caption'>
                                            <FormattedMessage
                                                id='Manage.Alerts.subscribe.to.alerts.subheading'
                                                defaultMessage={'Select the Alert types to subscribe/ unsubscribe'
                                        + ' and click Save.'}
                                            />
                                        </Typography>
                                        <List>
                                            {supportedAlerts && supportedAlerts.map((alert) => {
                                                return (
                                                    <ListItem key={alert.id} divider>
                                                        <ListItemIcon>
                                                            <Checkbox
                                                                edge='start'
                                                                tabIndex={-1}
                                                                value={alert.id}
                                                                checked={isAlertSubscribed(alert.id)}
                                                                onChange={() => handleCheckAlert(alert)}
                                                                inputProps={{ 'aria-labelledby': alert.name }}
                                                                color='primary'
                                                            />
                                                        </ListItemIcon>
                                                        <ListItemText
                                                            id={alert.id}
                                                            primary={alertIdMapping[alert.id].name}
                                                            secondary={alertIdMapping[alert.id].description}
                                                            className={classes.listItem}
                                                        />
                                                    </ListItem>
                                                );
                                            })}
                                        </List>
                                        <ChipInput
                                            label='Emails'
                                            variant='outlined'
                                            className={classes.chipInput}
                                            placeholder={emails.length === 0
                                                ? 'Enter email address and press Enter' : ''}
                                            defaultValue={emails}
                                            required
                                            helperText={validateEmailList(emails) || 'Email address to receive '
                                                + 'alerts of selected Alert types.'
                                                + ' Type email address and press Enter to add'}
                                            error={validateEmailList(emails)}
                                            onChange={(chip) => {
                                                handleAddEmail(chip);
                                            }}
                                            onDelete={(chip) => {
                                                handleEmailDeletion(chip);
                                            }}
                                        />
                                        <Box mt={2}>
                                            <Grid
                                                container
                                                direction='row'
                                                spacing={2}
                                            >
                                                <Grid item>
                                                    <Button
                                                        disabled={emails.length === 0 || subscribedAlerts.length === 0}
                                                        onClick={handleSubscribe}
                                                        variant='contained'
                                                        color='primary'
                                                    >
                                                        {isInProgress.subscribing && <CircularProgress size={15} />}
                                                        Save
                                                    </Button>
                                                </Grid>
                                                <Grid item>
                                                    <Button
                                                        disabled={isInProgress.subscribing}
                                                        color='primary'
                                                        variant='contained'
                                                        onClick={() => setUnsubscribeAll(true)}
                                                    >
                                                        {isInProgress.unSubscribing && <CircularProgress size={15} />}
                                                        Unsubscribe All
                                                    </Button>
                                                </Grid>
                                                <Grid item>
                                                    <Link to='/'>
                                                        <Button
                                                            disabled={isInProgress.subscribing}
                                                            variant='contained'
                                                            color='default'
                                                        >
                                                            {isInProgress.unSubscribing
                                                            && <CircularProgress size={15} />}
                                                            Cancel
                                                        </Button>
                                                    </Link>
                                                </Grid>
                                            </Grid>
                                        </Box>
                                    </>
                                )}
                        </div>
                    </ContentBase>
                )}
            <Dialog open={unsubscribeAll}>
                <DialogTitle>
                    <Typography className={classes.configDialogHeading}>
                        <FormattedMessage
                            id='Manage.Alerts.unsubscribe.confirm.dialog.heading'
                            defaultMessage='Confirm unsubscription from All Alerts'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        <FormattedMessage
                            id='Manage.Alerts.unsubscribe.confirm.dialog.message'
                            defaultMessage={'This will remove all the existing alert subscriptions and emails. This'
                            + ' action cannot be undone.'}
                        />
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button
                        color='primary'
                        size='small'
                        onClick={() => {
                            handleUnSubscribe();
                            setUnsubscribeAll(false);
                        }}
                    >
                        <Typography>
                            <FormattedMessage
                                id='Manage.Alerts.confirm.btn'
                                defaultMessage='Unsubscribe All'
                            />
                        </Typography>
                    </Button>
                    <Button
                        color='secondary'
                        size='small'
                        onClick={() => setUnsubscribeAll(false)}
                    >
                        <Typography>
                            <FormattedMessage id='Manage.Alerts.cancel.btn' defaultMessage='Cancel' />
                        </Typography>
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

ManageAlerts.propTypes = {
    classes: PropTypes.shape({
        configDialogHeading: PropTypes.string.isRequired,
        chipInput: PropTypes.string.isRequired,
        btnContainer: PropTypes.string.isRequired,
        alertsWrapper: PropTypes.string.isRequired,
        manageAlertHeading: PropTypes.string.isRequired,
    }).isRequired,
    intl: PropTypes.shape({
        formatMessage: PropTypes.func.isRequired,
    }).isRequired,
};

export default injectIntl(withStyles(styles)(ManageAlerts));
