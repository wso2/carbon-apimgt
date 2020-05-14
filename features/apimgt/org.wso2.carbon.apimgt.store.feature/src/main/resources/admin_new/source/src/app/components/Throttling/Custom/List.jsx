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

import React from 'react';
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
import AddEdit from 'AppComponents/Throttling/Blacklist/AddEdit';
import Delete from 'AppComponents/Throttling/Blacklist/Delete';
import API from 'AppData/api';
import EditIcon from '@material-ui/icons/Edit';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListCustomThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Custome.Policy.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Custom.Policy.List.addButtonProps.title',
            defaultMessage: 'Add Custom Policy ',
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
                filter: true,
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
        return new Promise(((resolve, reject) => {
            restApi.customPoliciesGet().then((result) => {
                const customPolicies = result.body.list.map((obj) => {
                    return {
                        policyId: obj.policyId,
                        policyName: obj.policyName,
                        description: obj.description,
                        keyTemplate: obj.keyTemplate,
                    };
                });
                resolve(customPolicies);
            }).catch((error) => {
                reject(error);
            });
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
            DeleteComponent={Delete}
            EditComponent={AddEdit}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Edit Custom Policy',
            }}
        />
    );
}
