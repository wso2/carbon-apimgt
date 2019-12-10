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
import Dialog from '@material-ui/core/Dialog';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton';
import Grid from '@material-ui/core/Grid';
import Icon from '@material-ui/core/Icon';
import { Typography } from '@material-ui/core';
import Button from '@material-ui/core/Button';
import SubscribeToApi from 'AppComponents/Shared/AppsAndKeys/SubscribeToApi';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { FormattedMessage, injectIntl } from 'react-intl';

/**
 * @inheritdoc
 * @param {*} theme theme object
 */
const styles = theme => ({
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
    plainContent: {
        paddingTop: 80,
        paddingLeft: theme.spacing(2),
    },
    button: {
        marginTop: theme.spacing(2),
        marginRight: theme.spacing(1),
    },
});

const subscrbeToApps = (props) => {
    const {
        classes, api, openAvailable, handleClickToggle,
        Transition, applicationsAvailable, handleSubscribe, subscriptionRequest,
        throttlingPolicyList, updateSubscriptionRequest, intl,
    } = props;
    const appLength = applicationsAvailable.length;
    const appPlaceholder = appLength > 0
        ? intl.formatMessage({
            defaultMessage: 'Applications',
            id: 'Apis.Details.Credentials.SubscibeToApps.applications',
        }) : intl.formatMessage({
            defaultMessage: 'Application',
            id: 'Apis.Details.Credentials.SubscibeToApps.application',
        });
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
                                <Icon>close</Icon>
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
                                onClick={handleSubscribe}
                            >
                                <FormattedMessage
                                    id='Apis.Details.Credentials.SubscibeToApps.subscribe'
                                    defaultMessage='Subscribe'
                                />
                            </Button>
                        </Toolbar>
                    </Grid>
                </Grid>
            </AppBar>
            <div className={classes.plainContent}>
                <SubscribeToApi
                    applicationsAvailable={applicationsAvailable}
                    subscriptionRequest={subscriptionRequest}
                    throttlingPolicyList={throttlingPolicyList}
                    updateSubscriptionRequest={updateSubscriptionRequest}
                />
            </div>
        </Dialog>
    );
};
subscrbeToApps.propTypes = {
    classes: PropTypes.shape({
        appBar: PropTypes.string,
        toolbar: PropTypes.string,
        subscribeTitle: PropTypes.string,
        button: PropTypes.string,
        plainContent: PropTypes.string,
    }).isRequired,
    handleClickToggle: PropTypes.func.isRequired,
    openAvailable: PropTypes.bool.isRequired,
    handleSubscribe: PropTypes.func.isRequired,
    intl: PropTypes.func.isRequired,
    updateSubscriptionRequest: PropTypes.func.isRequired,
    subscriptionRequest: PropTypes.shape({}).isRequired,
    applicationsAvailable: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
    throttlingPolicyList: PropTypes.arrayOf(PropTypes.string).isRequired,
    api: PropTypes.shape({
        name: PropTypes.string,
    }).isRequired,
    Transition: PropTypes.func.isRequired,
};

export default injectIntl(withStyles(styles, { withTheme: true })(subscrbeToApps));
