/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import React, { useEffect, useState } from 'react';
import {
    Paper,
    ListItem,
    Checkbox,
    ListItemText,
    ListItemIcon,
    ListItemSecondaryAction,
    IconButton,
    Icon,
    withStyles,
    Typography,
    List,
    Dialog,
    DialogTitle,
    DialogContent,
    Button,
    DialogActions,
    CircularProgress,
    DialogContentText,
} from '@material-ui/core';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import ChipInput from 'material-ui-chip-input';
import Grid from '@material-ui/core/Grid';
import { Link } from 'react-router-dom';
import API from 'AppData/api';
import Alert from 'AppComponents/Shared/Alert';
import AlertConfiguration from './AlertConfiguration';


const styles = theme => ({
    alertsWrapper: {
        padding: theme.spacing(2),
        '& span, & h5, & label, & input, & td, & li': {
            color: theme.palette.getContrastText(theme.palette.background.paper),
        },
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
    btnContainer: {
        marginTop: theme.spacing(),
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
const Alerts = (props) => {
    const {
        classes,
        intl,
    } = props;
    const [openDialog, setOpenDialog] = useState({ open: false, alertType: '', name: '' });
    const [emails, setEmailsList] = useState([]);
    const [supportedAlerts, setSupportedAlerts] = useState();
    const [subscribedAlerts, setSubscribedAlerts] = useState([]);
    const [isAnalyticsEnabled, setAnalyticsEnabled] = useState(false);
    const [isInProgress, setInProgress] = useState({ subscribing: false, unSubscribing: false });
    const [unsubscribeAll, setUnsubscribeAll] = useState(false);
    const [isWorkerNodeDown, setIsWorkerNodeDown] = useState(false);
    const api = new API();

    const alertIdMapping =
        {
            3: {
                name: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.abnormal.response.time',
                    defaultMessage: 'Abnormal Requests per Minute',
                }),
                displayName: 'AbnormalRequestsPerMin',
                description: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.abnormal.request.per.min.description',
                    defaultMessage: 'This alert is triggered if there is a sudden spike the request count within a ' +
                            'period of one minute by default for a particular API for an application. These alerts ' +
                            'could be treated as an indication of a possible high traffic, suspicious activity, ' +
                            'possible malfunction of the client application, etc.',
                }),
            },
            4: {
                name: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.abnormal.backend.time',
                    defaultMessage: 'Abnormal Resource Access',
                }),
                displayName: 'AbnormalRequestPattern',
                description: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.abnormal.request.pattern.description',
                    defaultMessage: 'This alert is triggered if there is a change in the resource access pattern of ' +
                        'a user of a particular application. These alerts could be treated as an indication of a ' +
                        'suspicious activity by a user over your application.',
                }),
            },
            5: {
                name: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.numusual.ip',
                    defaultMessage: 'Unusual IP Access',
                }),
                displayName: 'UnusualIPAccess',
                description: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.unusual.ip.access.description',
                    defaultMessage: 'This alert is triggered if there is either a change in the request source IP ' +
                        'for a particular application by a user or if the request is from an IP used before a ' +
                        'time period of 30 days (default). These alerts could be treated as an indication of a ' +
                        'suspicious activity by a user over an application.',
                }),
            },
            6: {
                name: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.frequent.tier',
                    defaultMessage: 'Frequent Tier Limit Hitting',
                }),
                displayName: 'FrequentTierLimitHitting',
                description: intl.formatMessage({
                    id: 'Settings.Alerts.Alerts.tier.limit.hitting.description',
                    defaultMessage: 'This alert is triggered if at least one of the two cases below are satisfied.' +
                        ' If a particular application gets throttled out for hitting the subscribed tier limit of ' +
                        'that application more than 10 times (by default) within an hour (by default) or if a ' +
                        'particular user of an application gets throttled out for hitting the subscribed tier limit ' +
                        'of a particular API more than 10 times (by default) within a day (by default)',
                }),
            },
        };
    /**
     * Set the configuration dialog open property for provided alert type.
     *
     * @param {number} id The alert type id
     * */
    const setConfigOpen = (id) => {
        setOpenDialog({
            open: true,
            alertType: alertIdMapping[id].displayName,
            name: alertIdMapping[id].name,
        });
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
     * Check whether the provided alert is configured.
     *
     * @param {string} selectedType : The alert type which needs to be check.
     * */
    const isAlertConfigured = (selectedType) => {
        api.getAlertConfigurations(selectedType.displayName).then((res) => {
            const data = res.body;
            if (data.length === 0) {
                setOpenDialog({ open: true, alertType: selectedType.displayName, name: selectedType.name });
            }
        }).catch(err => console.log(err));
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
            const newAlert = { id: alertId, name: alertIdMapping[alertId].displayName, configuration: [] };
            tmpSubscribedAlerts.push(newAlert);
        }
        // Check whether the alert is configurable and check configuration exists.
        if (alert.requireConfiguration) {
            isAlertConfigured(alertIdMapping[alertId]);
        }
        setSubscribedAlerts(tmpSubscribedAlerts);
    };

    /**
     * Handles the add email event.
     *
     * @param {string} email The email address that is being added.
     * */
    const handleAddEmail = (email) => {
        const newEmail = [...emails].push(email);
        setEmailsList(newEmail);
    };

    /**
     * Handles the email deletion event.
     *
     * @param {string} email : The email that is being deleted.
     * */
    const handleEmailDeletion = (email) => {
        setEmailsList([...emails].filter((oldEmail) => oldEmail !== email));
    };

    useEffect(() => {
        const supportedAlertsPromise = api.getSupportedAlertTypes();
        const subscribedAlertsPromise = api.getSubscribedAlertTypesByUser();
        Promise.all([supportedAlertsPromise, subscribedAlertsPromise]).then((response) => {
            if (response[0].status === 204 || response[1].status === 204) {
                setAnalyticsEnabled(false);
            } else {
                setAnalyticsEnabled(true);
                setSubscribedAlerts(response[1].body.alerts);
                setEmailsList(response[1].body.emailList);
                setSupportedAlerts(response[0].body.alerts);
            }
        }).catch((err) => {
            setAnalyticsEnabled(false);
            setSubscribedAlerts({});
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Settings.Alerts.Alerts.loading.error.msg',
                defaultMessage: 'Error occurred while loading alerts',
            }));
        });
    }, []);

    /**
     * Handles the subscribe button click action.
     * */
    const handleSubscribe = () => {
        setInProgress({ subscribing: true });
        const alertsToSubscribe = { alerts: subscribedAlerts, emailList: emails };
        api.subscribeAlerts(alertsToSubscribe).then(() => {
            Alert.success(intl.formatMessage({
                id: 'Settings.Alerts.Alerts.subscribe.success.msg',
                defaultMessage: 'Subscribed to Alerts Successfully.',
            }));
        }).catch((err) => {
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Settings.Alerts.Alerts.subscribe.error.msg',
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
                id: 'Settings.Alerts.Alerts.unsubscribe.success.msg',
                defaultMessage: 'Unsubscribed from all alerts successfully.',
            }));
        }).catch((err) => {
            console.error(err);
            Alert.error(intl.formatMessage({
                id: 'Settings.Alerts.Alerts.unsubscribe.error.msg',
                defaultMessage: 'Error occurred while unsubscribing.',
            }));
        }).finally(() => setInProgress({ unSubscribing: false }));
    };
    // if (!supportedAlerts) {
    //     return <CircularProgress />;
    // }
    return (
        <React.Fragment>
            <div className={classes.alertsWrapper}>
                {!isAnalyticsEnabled ?
                    <React.Fragment>
                        <InlineMessage type='info' height={100}>
                            <div>
                                <Typography>
                                    <FormattedMessage
                                        id='Settings.Alerts.Alerts.enable.analytics.message'
                                        defaultMessage='Enable Analytics to Configure Alerts'
                                    />
                                </Typography>
                            </div>
                        </InlineMessage>
                    </React.Fragment> :
                    <React.Fragment>
                        {!supportedAlerts ?
                            <CircularProgress /> :
                            <React.Fragment>
                                <Typography variant='h6' className={classes.manageAlertHeading}>
                                    <FormattedMessage
                                        id='Settings.Alerts.Alerts.subscribe.to.alerts.heading'
                                        defaultMessage='Manage Alert Subscriptions'
                                    />
                                </Typography>
                                <Typography variant='caption'>
                                    <FormattedMessage
                                        id='Settings.Alerts.Alerts.subscribe.to.alerts.subheading'
                                        defaultMessage={'Select the Alert types to subscribe/ unsubscribe and click' +
                                        ' Save.'}
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
                                                {alert.requireConfiguration === true ?
                                                    <ListItemSecondaryAction>
                                                        <IconButton
                                                            onClick={() => setConfigOpen(alert.id)}
                                                        >
                                                            <Icon>
                                                                settings
                                                            </Icon>
                                                        </IconButton>
                                                    </ListItemSecondaryAction> :
                                                    <div />}
                                            </ListItem>
                                        );
                                    })}
                                </List>
                                <ChipInput
                                    label='Emails'
                                    variant='outlined'
                                    className={classes.chipInput}
                                    value={emails}
                                    placeholder='Enter email address and press Enter'
                                    required
                                    helperText={'Email address to receive alerts of selected Alert types. Type email' +
                                    ' address and press Enter to add'}
                                    onAdd={(chip) => {
                                        handleAddEmail(chip);
                                    }}
                                    onDelete={(chip) => {
                                        handleEmailDeletion(chip);
                                    }}
                                />
                                <Grid container direction='row' spacing={2} className={classes.btnContainer}>
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
                                        <Link to='/apis/'>
                                            <Button
                                                disabled={isInProgress.subscribing}
                                                variant='contained'
                                                color='default'
                                            >
                                                {isInProgress.unSubscribing && <CircularProgress size={15} />}
                                                    Cancel
                                            </Button>
                                        </Link>
                                    </Grid>
                                </Grid>
                            </React.Fragment>}
                    </React.Fragment>}
            </div>
            <Dialog fullWidth maxWidth="md" open={openDialog.open}>
                <DialogTitle>
                    <FormattedMessage
                        id='Settings.Alerts.Alerts.configure.alert'
                        defaultMessage='Configurations'
                    />
                </DialogTitle>
                {isWorkerNodeDown ? (
                    <DialogContent>
                        <DialogContentText id='analytics-dialog-description'>
                            <FormattedMessage
                                id='Apis.Settings.Alerts.connection.error'
                                defaultMessage='Could not connect to analytics server. Please check the connectivity.'
                            />
                        </DialogContentText>
                    </DialogContent>
                ) : (
                    <DialogContent>
                        <AlertConfiguration
                            alertType={openDialog.alertType}
                            alertName={openDialog.name}
                            api={api}
                            setIsWorkerNodeDown={setIsWorkerNodeDown}
                        />
                    </DialogContent>)}
                <DialogActions>
                    <Button
                        color='primary'
                        variant='outlined'
                        onClick={() => setOpenDialog({ open: false })}
                    >
                        <Typography>
                            <FormattedMessage id='Settings.Alerts.Alerts.close.btn' defaultMessage='Close' />
                        </Typography>
                    </Button>
                </DialogActions>
            </Dialog>
            <Dialog open={unsubscribeAll}>
                <DialogTitle>
                    <Typography className={classes.configDialogHeading}>
                        <FormattedMessage
                            id='Settings.Alerts.Alerts.unsubscribe.confirm.dialog.heading'
                            defaultMessage='Confirm unsubscription from All Alerts'
                        />
                    </Typography>
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        <FormattedMessage
                            id='Settings.Alerts.Alerts.unsubscribe.confirm.dialog.message'
                            defaultMessage={'This will remove all the existing alert subscriptions and emails. This' +
                            ' action cannot be undone.'}
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
                                id='Settings.Alerts.Alerts.confirm.btn'
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
                            <FormattedMessage id='Settings.Alerts.Alerts.cancel.btn' defaultMessage='Cancel' />
                        </Typography>
                    </Button>
                </DialogActions>
            </Dialog>
        </React.Fragment>
    );
};

Alerts.propTypes = {
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

export default injectIntl(withStyles(styles)(Alerts));
