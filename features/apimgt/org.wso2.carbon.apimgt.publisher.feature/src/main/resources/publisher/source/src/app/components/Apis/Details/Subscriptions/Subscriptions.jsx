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

import React, { useEffect, useState } from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { CircularProgress, Typography, Grid } from '@material-ui/core';
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import { useAPI } from 'AppComponents/Apis/Details/components/ApiContext';
import API from 'AppData/api';
import CONSTS from 'AppData/Constants';
import Progress from 'AppComponents/Shared/Progress';
import { FormattedMessage } from 'react-intl';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import SubscriptionsTable from './SubscriptionsTable';
import SubscriptionPoliciesManage from './SubscriptionPoliciesManage';
import SubscriptionAvailability from './SubscriptionAvailability';

const useStyles = makeStyles(theme => ({
    buttonSection: {
        marginTop: theme.spacing(2),
    },
    emptyBox: {
        marginTop: theme.spacing(2),
    },
    heading: {
        marginTop: theme.spacing(3),
        marginBottom: theme.spacing(2),
    },
}));

/**
 * Subscriptions component
 *
 * @class Subscriptions
 * @extends {Component}
 */
function Subscriptions(props) {
    const classes = useStyles();
    const [api] = useAPI();
    const { updateAPI } = props;
    const restApi = new API();
    const [tenants, setTenants] = useState(null);
    const [policies, setPolices] = useState({});
    const [availability, setAvailability] = useState({ subscriptionAvailability: api.subscriptionAvailability });
    const [tenantList, setTenantList] = useState(api.subscriptionAvailableTenants);
    const [subscriptions, setSubscriptions] = useState(null);
    const [updateInProgress, setUpdateInProgress] = useState(false);

    /**
     * Save subscription information (policies, subscriptionAvailability, subscriptionAvailableTenants)
     */
    function saveAPI() {
        setUpdateInProgress(true);
        const { subscriptionAvailability } = availability;
        const newApi = {
            policies,
            subscriptionAvailability,
            subscriptionAvailableTenants: tenantList,
        };
        updateAPI(newApi)
            .then(() => {
                Alert.info('Subscription configurations updated successfully');
            })
            .catch((error) => {
                console.error(error);
                Alert.error('Error occurred while updating subscription configurations');
            }).finally(() => {
                setUpdateInProgress(false);
            });
    }

    useEffect(() => {
        restApi.getTenantsByState(CONSTS.TENANT_STATE_ACTIVE)
            .then((result) => {
                setTenants(result.body.count);
            });
        restApi.subscriptions(api.id)
            .then((result) => {
                setSubscriptions(result.body.count);
            });
        setPolices([...api.policies]);
    }, []);

    if (typeof tenants !== 'number' || typeof subscriptions !== 'number') {
        return (
            <Grid container direction='row' justify='center' alignItems='center'>
                <Grid item>
                    <CircularProgress />
                </Grid>
            </Grid>
        );
    }
    return (
        <React.Fragment>
            <SubscriptionPoliciesManage api={api} policies={policies} setPolices={setPolices} />
            {tenants !== 0 && (
                <SubscriptionAvailability
                    api={api}
                    availability={availability}
                    setAvailability={setAvailability}
                    tenantList={tenantList}
                    setTenantList={setTenantList}
                />
            )}
            { updateInProgress && <Progress /> }
            <Grid
                container
                direction='row'
                alignItems='flex-start'
                spacing={1}
                className={classes.buttonSection}
            >
                <Grid item>
                    <Button
                        type='submit'
                        variant='contained'
                        color='primary'
                        onClick={() => saveAPI()}
                    >
                        <FormattedMessage
                            id='Apis.Details.Subscriptions.Subscriptions.save'
                            defaultMessage='Save'
                        />
                    </Button>
                </Grid>
                <Grid item>
                    <Link to={'/apis/' + api.id + '/overview'}>
                        <Button>
                            <FormattedMessage
                                id='Apis.Details.Subscriptions.Subscriptions.cancel'
                                defaultMessage='Cancel'
                            />
                        </Button>
                    </Link>
                </Grid>
            </Grid>
            <div className={classes.heading}>
                <Typography variant='h4'>
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.SubscriptionsTable.manage.subscriptions'
                        defaultMessage='Manage Subscriptions'
                    />
                </Typography>
                <Typography variant='caption' gutterBottom >
                    <FormattedMessage
                        id='Apis.Details.Subscriptions.SubscriptionsTable.sub.heading'
                        defaultMessage='Manage subscriptions of the API'
                    />
                </Typography>
            </div>
            {subscriptions !== 0 ? (
                <SubscriptionsTable api={api} />
            ) : (
                <InlineMessage type='info' height={80} className={classes.emptyBox}>
                    <div className={classes.contentWrapper}>
                        <Typography component='p' className={classes.content}>
                            <FormattedMessage
                                id='Apis.Details.Subscriptions.table.empty'
                                defaultMessage='No subscription data available.'
                            />
                        </Typography>
                    </div>
                </InlineMessage>
            )}
        </React.Fragment>
    );
}

Subscriptions.propTypes = {
    updateAPI: PropTypes.func.isRequired,
};

export default withStyles(makeStyles)(Subscriptions);
