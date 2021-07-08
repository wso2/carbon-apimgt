/* eslint-disable no-param-reassign */
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
import Typography from '@material-ui/core/Typography';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import Delete from 'AppComponents/Throttling/Advanced/Delete';
import EditIcon from '@material-ui/icons/Edit';
import { Link as RouterLink } from 'react-router-dom';
import HelpLinks from 'AppComponents/Throttling/Advanced/HelpLinks';
import Button from '@material-ui/core/Button';
import API from 'AppData/api';
import WarningBase from "AppComponents/AdminPages/Addons/WarningBase";

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListMG() {
    const intl = useIntl();
    const [hasListAdvancedThrottlingPoliciesPermission, setHasListAdvancedThrottlingPoliciesPermission] = useState(true);

    /**
     * Fetch policy list from backend
     * @returns {Promise}.
     */
    function apiCall() {
        const restApi = new API();
        return restApi
            .getThrottlingPoliciesAdvanced()
            .then((result) => {
                const { body: { list } } = result;
                list.forEach((item) => {
                    if (item.defaultLimit.bandwidth) {
                        item.quotaPolicy = 'Bandwidth Volume';
                        item.quota = item.defaultLimit.bandwidth.dataAmount
                            + item.defaultLimit.bandwidth.dataUnit;
                        item.unitTime = item.defaultLimit.bandwidth.unitTime
                            + item.defaultLimit.bandwidth.timeUnit;
                    } else {
                        item.quotaPolicy = 'Request Count';
                        item.quota = item.defaultLimit.requestCount.requestCount;
                        item.unitTime = item.defaultLimit.requestCount.unitTime
                            + item.defaultLimit.requestCount.timeUnit;
                    }
                });
                return list;
            })
            .catch((error) => {
                if (error.statusCode === 401) {
                    setHasListAdvancedThrottlingPoliciesPermission(false);
                } else {
                    setHasListAdvancedThrottlingPoliciesPermission(true);
                    throw error;
                }
            });
    }

    const columProps = [
        {
            name: 'policyName',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Advanced.Throttling.policy.table.header.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                        return <RouterLink to={`/throttling/advanced/${artifactId}`}>{value}</RouterLink>;
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
                id: 'Admin.Throttling.Advanced.Throttling.policy.table.header.quota.policy',
                defaultMessage: 'Quota Policy',
            }),
        },
        {
            name: 'quota',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Advanced.Throttling.policy.table.header.quota',
                defaultMessage: 'Quota',
            }),
        },
        {
            name: 'unitTime',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Advanced.Throttling.policy.table.header.unit.time',
                defaultMessage: 'Unit Time',
            }),
        },
        { // Id column has to be always the last.
            name: 'policyId',
            options: {
                display: false,
            },
        },
    ];

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Advanced.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Advanced.List.addButtonProps.title',
            defaultMessage: 'Add Policy',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Advanced.List.search.default',
            defaultMessage: 'Search by Advanced Policy name',
        }),
        active: true,
    };
    const pageProps = {
        help: (<HelpLinks />),
        /*
        pageStyle='half' center part of the screen.
        pageStyle='full' = Take the full content area.
        pageStyle='paperLess' = Avoid from displaying background paper. ( For dashbord we need this )
        */
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Advanced.List.title.main',
            defaultMessage: 'Advanced Rate Limiting Policies',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Throttling.Advanced.List.empty.content'
                    defaultMessage={'It is possible to create a Microgateway distribution '
                        + 'for a group of APIs. In order to group APIs, a label needs to be created'
                        + ' and attached to the APIs that need to be in a single group.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Advanced.List.empty.title'
                    defaultMessage='Advanced Throttling Policies'
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
                    id='Throttling.Advanced.List.help.link.one'
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


    To disable the Edit button pass an empty component. Ex EditComponent={() => <span />}
    To disable the Delete button pass an empty component. Ex DeleteComponent={() => <span />}
    To make the edit link go to a new page send a react-router-dom as the EditComponent.
    Ex:
    import { Link as RouterLink } from 'react-router-dom';
    import EditIcon from '@material-ui/icons/Edit';
    .....
    .....
    EditComponent={() => <RouterLink to='/'>
                <EditIcon />
            </RouterLink> }
    .....
    /* **************************************************************** */
    /*
    Passing additional actions to the action column.
    const addedActions = [
        {
            component: <Button>test</Button>,
            componentProps: {
                onClick={}
            }
        }
    ]

    */
    /* ====================
    Uncomment following to add an action which can do an action which can perform on a given item
    const addedActions = [
        (props) => {
            const { rowData, updateList } = props;
            const updateSomething = () => {
                alert(`Do something with ${JSON.stringify(rowData)}`);
                updateList();
            };
            return (
                <Button variant='contained' size='small' onClick={updateSomething}>
                    <FormattedMessage
                        id='Throttling.Advanced.List.custom.action'
                        defaultMessage='Some Action'
                    />
                </Button>
            );
        },
    ];
    =========== */
    const addButtonOverride = (
        <RouterLink to='/throttling/advanced/create'>
            <Button variant='contained' color='primary' size='small'>
                <FormattedMessage
                    id='Throttling.Advanced.List.add.new.polcy'
                    defaultMessage='Add New Policy'
                />
            </Button>
        </RouterLink>
    );

    if (!hasListAdvancedThrottlingPoliciesPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'Throttling.Advanced.List.title.advanced.rate.limiting.policies',
                        defaultMessage: 'Advanced Rate Limiting Policies',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='Throttling.Advanced.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='Throttling.Advanced.List.permission.denied.content'
                        defaultMessage={'You dont have enough permission to view Advanced Rate Limiting Policies.'
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
                editComponentProps={{
                    icon: <EditIcon/>,
                    title: 'Edit Policy',
                    routeTo: '/throttling/advanced/',
                }}
                DeleteComponent={Delete}
                addButtonOverride={addButtonOverride}
                // addedActions={addedActions}
            />
        );
    }
}
