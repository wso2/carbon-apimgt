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
import Button from '@material-ui/core/Button';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import Delete from 'AppComponents/Throttling/Custom/Delete';
import API from 'AppData/api';
import EditIcon from '@material-ui/icons/Edit';
import { Link as RouterLink } from 'react-router-dom';
import WarningBase from 'AppComponents/AdminPages/Addons/WarningBase';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListCustomThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();
    const [hasListCustomThrottlingPoliciesPermission, setHasListCustomThrottlingPoliciesPermission] = useState(true);

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Custom.Policy.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Define Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Custom.Policy.List.addButtonProps.title',
            defaultMessage: 'Define Custom Policy ',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Custom.Policy.List.search.default',
            defaultMessage: 'Search by Custom Policy name',
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
            + 'learn/rate-limiting/advanced-topics/custom-throttling/'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Custom.Policy.List.help.link.one'
                                    defaultMessage='Custom Throttling Policy'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Custom.Policy.search.default',
            defaultMessage: 'Custom Rate Limiting Policies',
        }),
    };

    const columProps = [
        {
            name: 'policyName',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Custom.Throttling.policy.table.header.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                        return <RouterLink to={`/throttling/custom/${artifactId}`}>{value}</RouterLink>;
                    } else {
                        return <div />;
                    }
                },
                filter: false,
                sort: true,
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Custom.Throttling.policy.table.header.description',
                defaultMessage: 'Description',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'keyTemplate',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Custom.Throttling.policy.table.header.key.template',
                defaultMessage: 'Key Template',
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
                    id='Throttling.Custom.Policy.List.empty.content.custom.policies and abuse by'
                    defaultMessage={'Custom throttling allows system administrators to define dynamic'
                    + ' rules for specific use cases, which are applied globally across all tenants.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Custom.Policy.List.empty.title.custom.policies'
                    defaultMessage='Custom Policies'
                />

            </Typography>),
    };

    /**
 * Mock API call
 * @returns {Promise}.
 */
    function apiCall() {
        return restApi.customPoliciesGet().then((result) => {
            const customPolicies = result.body.list.map((obj) => {
                return {
                    policyId: obj.policyId,
                    policyName: obj.policyName,
                    description: obj.description,
                    keyTemplate: obj.keyTemplate,
                };
            });
            return (customPolicies);
        }).catch((error) => {
            if (error.statusCode === 401) {
                setHasListCustomThrottlingPoliciesPermission(false);
            } else {
                setHasListCustomThrottlingPoliciesPermission(true);
                throw error;
            }
        });
    }

    const addButtonOverride = (
        <RouterLink to='/throttling/custom/create'>
            <Button variant='contained' color='primary' size='small'>
                <FormattedMessage
                    id='Throttling.Custom.List.add.new.polcy'
                    defaultMessage='Define Policy'
                />
            </Button>
        </RouterLink>
    );

    if (!hasListCustomThrottlingPoliciesPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'Throttling.Custom.Policy.List.title.custom.rate.limiting.policies',
                        defaultMessage: 'Custom Rate Limiting Policies',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='Throttling.Custom.Policy.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='Throttling.Custom.Policy.List.permission.denied.content'
                        defaultMessage={'You don\'t have sufficient permission to view Custom Rate Limiting Policies.'
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
                addButtonProps={addButtonProps}
                searchProps={searchProps}
                emptyBoxProps={emptyBoxProps}
                apiCall={apiCall}
                DeleteComponent={Delete}
                editComponentProps={{
                    icon: <EditIcon/>,
                    title: 'Edit Policy',
                    routeTo: '/throttling/custom/',
                }}
                addButtonOverride={addButtonOverride}
            />
        );
    }
}
