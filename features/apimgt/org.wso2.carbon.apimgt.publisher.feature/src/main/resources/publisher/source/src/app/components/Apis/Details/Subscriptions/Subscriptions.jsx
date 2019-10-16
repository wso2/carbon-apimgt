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
import PropTypes from 'prop-types';
import withStyles from '@material-ui/core/styles/withStyles';
import API from 'AppData/api';
import CONSTS from 'AppData/Constants';
import { FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import InlineMessage from 'AppComponents/Shared/InlineMessage';
import SubscriptionsTable from './SubscriptionsTable';
import SubscriptionPoliciesManage from './SubscriptionPoliciesManage';
import SubscriptionAvailability from './SubscriptionAvailability';

const useStyles = makeStyles(theme => ({
    button: {
        margin: theme.spacing.unit,
    },
    emptyBox: {
        marginTop: theme.spacing.unit * 2,
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
    const { api, updateAPI } = props;
    const restApi = new API();
    const [tenants, setTenants] = useState([]);
    const [subscriptions, setSubsriptions] = useState([]);
    useEffect(() => {
        restApi.getTenantsByState(CONSTS.TENANT_STATE_ACTIVE)
            .then((result) => {
                setTenants(result.body.count);
            });
    }, []);

    useEffect(() => {
        restApi.subscriptions(api.id)
            .then((result) => {
                setSubsriptions(result.body.count);
            });
    }, []);

    return (
        <React.Fragment>
            <SubscriptionPoliciesManage api={api} updateAPI={updateAPI} />
            {tenants !== 0 && (
                <SubscriptionAvailability api={api} updateAPI={updateAPI} />
            )}
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
    api: PropTypes.shape({}).isRequired,
    updateAPI: PropTypes.func.isRequired,
};

export default withStyles(makeStyles)(Subscriptions);
