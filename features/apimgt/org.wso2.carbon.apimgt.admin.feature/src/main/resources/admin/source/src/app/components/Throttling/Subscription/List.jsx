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

import React, {useState} from 'react';
import { useIntl, FormattedMessage } from 'react-intl';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import Delete from 'AppComponents/Throttling/Subscription/Delete';
import API from 'AppData/api';
import EditIcon from '@material-ui/icons/Edit';
import { Link as RouterLink } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import WarningBase from 'AppComponents/AdminPages/Addons/WarningBase';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListSubscriptionThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();
    const [hasListSubscriptionThrottlingPoliciesPermission, setHasListSubscriptionThrottlingPoliciesPermission] = useState(true);

    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Subscription.Policy..List.search.default',
            defaultMessage: 'Search by Subscription Policy name',
        }),
        active: true,
    };
    const pageProps = {
        help: (
            <HelpBase>
                <List component='nav' aria-label='main mailbox folders'>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
            + 'learn/rate-limiting/adding-new-throttling-policies/#adding-a-new-subscription-level-throttling-tier'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Subscription.Policy.List.help.link.one'
                                    defaultMessage='Creating a Subscription Rate Limiting Policy'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                + 'learn/rate-limiting/setting-throttling-limits/#subscription-level-throttling-api-publisher'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Subscription.Policy.List.help.link.two'
                                    defaultMessage='Setting a Subscription Rate Limiting Policy as an API Publisher'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                    <ListItem button>
                        <ListItemIcon>
                            <DescriptionIcon />
                        </ListItemIcon>
                        <Link
                            target='_blank'
                            href={Configurations.app.docUrl
                + 'learn/rate-limiting/setting-throttling-limits/#subscription-level-throttling-api-subscriber'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Subscription.Policy.List.help.link.three'
                                    defaultMessage='Setting a Subscription Rate Limiting Policy as an API Subscriber'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Subscription.Policy.search.default',
            defaultMessage: 'Subscription Rate Limiting Policies',
        }),
        EditTitle: intl.formatMessage({
            id: 'Throttling.Subscription.Policy.search.default',
            defaultMessage: 'Subscription Rate Limiting Policies',
        }),
    };

    const columProps = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                        return <RouterLink to={`/throttling/subscription/${artifactId}`}>{value}</RouterLink>;
                    } else {
                        return <div />;
                    }
                },
                filter: false,
                sort: true,
            },
        },
        {
            name: 'quotaPolicy',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.quota.policy',
                defaultMessage: 'Quota Policy',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'quota',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.quota',
                defaultMessage: 'Quota',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'unitTime',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.unit.time',
                defaultMessage: 'Unit Time',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'rateLimit',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.rate.limit',
                defaultMessage: 'Rate Limit',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'timeUnit',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Subscription.Throttling.policy.table.header.time.unit',
                defaultMessage: 'Time Unit',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        { // Id column has to be always the last.
            name: 'policyId',
            options: {
                display: false,
            },
        },
    ];

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Throttling.Subsription.Policy.List.empty.content.subscription.policies'
                    defaultMessage={'Subscription-level throttling policies are applicable per access '
                    + 'token generated for an application.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Subscription.Policy.List.empty.title.subscription.policies'
                    defaultMessage='Subscription Policies'
                />

            </Typography>),
    };

    const addButtonOverride = (
        <RouterLink to='/throttling/subscription/add'>
            <Button variant='contained' color='primary'>
                <FormattedMessage
                    id='Throttling.Subscription.Policy.List.addButtonProps.title'
                    defaultMessage='Add Policy'
                />
            </Button>
        </RouterLink>
    );

    /**
     * Populate subscription policies
     * @returns {Promise} The list of subscription policies
     */
    function apiCall() {
        let subscriptionThrottlingvalues;
        return restApi.getSubscritionPolicyList().then((result) => {
            const subscriptionPolicies = result.body.list.map((obj) => {
                if (obj.defaultLimit.requestCount !== null) {
                    return {
                        policyName: obj.policyName,
                        quotaPolicy: 'Request Count',
                        quota: obj.defaultLimit.requestCount.requestCount,
                        unitTime: obj.defaultLimit.requestCount.unitTime + ' '
                        + obj.defaultLimit.requestCount.timeUnit,
                        rateLimit: (obj.rateLimitCount === 0) ? 'NA' : obj.rateLimitCount,
                        timeUnit: (obj.rateLimitCount === 0) ? 'NA' : obj.rateLimitTimeUnit,
                        policyId: obj.policyId,
                    };
                } else {
                    return {
                        policyName: obj.policyName,
                        quotaPolicy: 'Bandwidth Volume',
                        quota: obj.defaultLimit.bandwidth.dataAmount + ' '
                        + obj.defaultLimit.bandwidth.dataUnit,
                        unitTime: obj.defaultLimit.bandwidth.unitTime + ' '
                        + obj.defaultLimit.bandwidth.timeUnit,
                        rateLimit: (obj.rateLimitCount === 0) ? 'NA' : obj.rateLimitCount,
                        timeUnit: (obj.rateLimitCount === 0) ? 'NA' : obj.rateLimitTimeUnit,
                        policyId: obj.policyId,
                    };
                }
            });

            subscriptionThrottlingvalues = subscriptionPolicies
                .map(Object.values);
            return (subscriptionThrottlingvalues);
        }).catch((error) => {
            if (error.statusCode === 401) {
                setHasListSubscriptionThrottlingPoliciesPermission(false);
            }
            throw error;
        });
    }

    if (!hasListSubscriptionThrottlingPoliciesPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'Throttling.Subscription.Policy.List.title.subscription.rate.limiting.policies',
                        defaultMessage: 'Subscription Rate Limiting Policies',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='Throttling.Subscription.Policy.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='Throttling.Subscription.Policy.List.permission.denied.content'
                        defaultMessage={'You don\'t have sufficient permission to view Subscription Rate Limiting Policies.'
                        + ' Please contact the site administrator.'}
                    />
                )}
            />
        );
    } else {
        return (
            <ListBase
                columProps={columProps}
                pageProps={pageProps}
                addButtonOverride={addButtonOverride}
                searchProps={searchProps}
                emptyBoxProps={emptyBoxProps}
                apiCall={apiCall}
                editComponentProps={{
                    icon: <EditIcon/>,
                    title: 'Edit Subscription Policy',
                    routeTo: '/throttling/subscription/',
                }}
                DeleteComponent={Delete}
            />
        );
    }
}
