import React from 'react';
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import CloseIcon from '@material-ui/icons/Close';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';

const subscrbeToApps = (props) => {
    const {
        classes, api, openAvailable, handleClickToggle,
        Transition, applicationsAvailable, handleSubscribe, subscriptionRequest,
        throttlingPolicyList, updateSubscriptionRequest,
    } = props;
    const appLength = applicationsAvailable.length;
    const appPlaceholder = appLength > 0 ? 'Applications' : 'Application';
    return (
        <Dialog
            fullScreen
            open={openAvailable}
            onClose={() => handleClickToggle('openAvailable')}
            TransitionComponent={Transition}
        >
            {' '}
            <AppBar className={classes.appBar}>
                <Grid container spacing={0}>
                    <Grid item xs={6}>
                        <Toolbar className={classes.toolbar}>
                            <IconButton
                                color='inherit'
                                onClick={() => handleClickToggle('openAvailable')}
                                aria-label='Close'
                            >
                                <CloseIcon />
                            </IconButton>
                            <div className={classes.subscribeTitle}>
                                <Typography variant='h6'>
                                    {`Subscribe ${api.name} to ${appPlaceholder}`}
                                </Typography>
                                <Typography variant='caption'>
                                    {`(${appLength} ${appPlaceholder} )`}
                                </Typography>
                            </div>
                            <Button
                                variant='contained'
                                color='primary'
                                className={classes.button}
                                onClick={() => handleSubscribe()}
                            >
                                Subscribe
                            </Button>
                        </Toolbar>
                    </Grid>
                </Grid>
            </AppBar>
            <div className={classes.plainContent}>
                <SubscribeToApi
                    api={api}
                    applicationsAvailable={applicationsAvailable}
                    rootClass={classes.subscribeRoot}
                    subscriptionRequest={subscriptionRequest}
                    throttlingPolicyList={throttlingPolicyList}
                    updateSubscriptionRequest={updateSubscriptionRequest}
                />
            </div>
        </Dialog>
    );
};

export default subscrbeToApps;
