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

import React, { useState } from 'react';
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
import AddEdit from 'AppComponents/Throttling/Application/AddEdit';
import Delete from 'AppComponents/Throttling/Application/Delete';
import API from 'AppData/api';
import EditIcon from '@material-ui/icons/Edit';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListApplicationThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();
    const [
        applicationThrottlingPolicyList,
        setApplicationThrottlingPolicyList,
    ] = useState(null);

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Application.Policy.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Application.Policy.List.addButtonProps.title',
            defaultMessage: 'Add Policy',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Application.Policy..List.search.default',
            defaultMessage: 'Search by Application Policy name',
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
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.one'
                                    defaultMessage='Create a Microgateway label'
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
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.two'
                                    defaultMessage='Assign the Microgateway label to an API'
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
                        + 'learn/api-microgateway/grouping-apis-with-labels/#grouping-apis-with-microgateway-labels'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='AdminPages.Microgateways.List.help.link.three'
                                    defaultMessage='View the Microgateway labels'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Application.Policy.search.default',
            defaultMessage: 'Application Rate Limiting Policies',
        }),
        EditTitle: intl.formatMessage({
            id: 'Throttling.Application.Policy.search.default',
            defaultMessage: 'Application Rate Limiting Policies',
        }),
    };

    const columProps = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.name',
                defaultMessage: 'Name',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: 'quotaPolicy',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota.policy',
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
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota',
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
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.unit.time',
                defaultMessage: 'Unit Time',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
    ];

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.Microgateways.List.empty.content.microgateways'
                    defaultMessage={'It is possible to create a Microgateway distribution '
                    + 'for a group of APIs. In order to group APIs, a label needs to be created'
                    + ' and attached to the APIs that need to be in a single group.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.Microgateways.List.empty.title.microgateways'
                    defaultMessage='Microgateways'
                />

            </Typography>),
    };
    /*
    If the add button wants to route to a new page, we need to override the Button component completely.
    Send the following prop to ListBase component.
    import { Link as RouterLink } from 'react-router-dom';
    import Button from '@material-ui/core/Button';

    const addButtonOverride = (
        <RouterLink to='/'>
            <Button variant='contained' color='primary'>
                <FormattedMessage
                    id='AdminPages.Microgateways.List.help.link.one'
                    defaultMessage='Create a Microgateway label'
                />
            </Button>
        </RouterLink>
    );
    */
    /* *************************************************************** */
    /* To override the no data message send the following with the props to ListBase
    const noDataMessage = (
        <FormattedMessage
            id='AdminPages.Addons.ListBase.nodata.message'
            defaultMessage='No items yet'
        />
    )
    /* **************************************************************** */
    /*
    Send the following props to ListBase to override the action column.

    Following imports needs to go to the header.
    import { Link as RouterLink } from 'react-router-dom';
    import EditIcon from '@material-ui/icons/Edit';

    const actionColumnProps = {
        editIconShow: true,
        editIconOverride: (
            <RouterLink to='/'>
                <EditIcon />
            </RouterLink>
        ),
        deleteIconShow: false,
    }; */

    /**
 * Mock API call
 * @returns {Promise}.
 */
    function apiCall() {
        let applicationThrottlingvalues;
        return new Promise(((resolve) => {
            restApi.applicationThrottlingPoliciesGet().then((result) => {
                setApplicationThrottlingPolicyList(result.body.list);
                const applicationPolicies = result.body.list.map((obj) => {
                    return {
                        policyName: obj.policyName,
                        quotaPolicy: obj.defaultLimit.type,
                        quota: obj.defaultLimit.requestCount
                || obj.defaultLimit.dataAmount + ' ' + obj.defaultLimit.dataUnit,
                        unitTime: obj.defaultLimit.unitTime + ' ' + obj.defaultLimit.timeUnit,
                    };
                });

                applicationThrottlingvalues = applicationPolicies
                    .filter((policy) => policy.policyName !== 'Unlimited')
                    .map((obj) => {
                        return Object.values(obj);
                    });
            });

            setTimeout(() => {
                resolve(applicationThrottlingvalues);
            }, 1000);
        }));
    }

    return (
        <ListBase
            columProps={columProps}
            pageProps={pageProps}
            addButtonProps={addButtonProps}
            searchProps={searchProps}
            emptyBoxProps={emptyBoxProps}
            apiCall={apiCall}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Edit Microgateway',
                applicationThrottlingPolicyList,
            }}
            deleteComponentProps={{
                applicationThrottlingPolicyList,
            }}
            DeleteComponent={Delete}
            EditComponent={AddEdit}
        />
    );
}
