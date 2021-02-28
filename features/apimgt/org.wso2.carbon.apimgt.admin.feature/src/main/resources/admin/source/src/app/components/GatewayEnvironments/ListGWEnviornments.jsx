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
import API from 'AppData/api';
import { FormattedMessage, useIntl } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import Delete from 'AppComponents/GatewayEnvironments/DeleteGWEnvironment';
import AddEdit from 'AppComponents/GatewayEnvironments/AddEditGWEnvironment';
import EditIcon from '@material-ui/icons/Edit';

/**
 * API call to get Gateway labels
 * @returns {Promise}.
 */
function apiCall() {
    const restApi = new API();
    return restApi
        .getGatewayEnvironmentList()
        .then((result) => {
            return result.body.list;
        })
        .catch((error) => {
            throw error;
        });
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListGWEnviornments() {
    const intl = useIntl();
    const columProps = [
        { name: 'id', options: { display: false } },
        { name: 'name', options: { display: false } },
        {
            name: 'displayName',
            label: intl.formatMessage({
                id: 'AdminPages.Gateways.table.header.displayName',
                defaultMessage: 'Name',
            }),
            options: {
                sort: true,
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'AdminPages.Gateways.table.header.description',
                defaultMessage: 'Description',
            }),
            options: {
                sort: false,
            },
        },
        {
            name: 'vhosts',
            label: intl.formatMessage({
                id: 'AdminPages.Gateways.table.header.vhosts',
                defaultMessage: 'Virtual Host(s)',
            }),
            options: {
                sort: false,
                customBodyRender: (vhosts) => {
                    return (
                        vhosts.map((vhost) => (
                            <div>
                                {
                                    'https://' + vhost.host + (vhost.httpsPort === 443 ? '' : ':' + vhost.httpsPort)
                                    + (vhost.httpContext ? '/' + vhost.httpContext.replace(/^\//g, '') : '')
                                }
                            </div>
                        ))
                    );
                },
            },
        },
    ];
    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'AdminPages.Gateways.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Gateway Environment',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'AdminPages.Gateways.List.addButtonProps.title',
            defaultMessage: 'Add Gateway Environment',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.Gateways.List.search.default',
            defaultMessage: 'Search Gateway by Name, Description or Host',
        }),
        active: true,
    };
    const pageProps = {
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'AdminPages.Gateways.List.title',
            defaultMessage: 'Gateway Environments',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.Gateways.List.empty.content.Gateways'
                    defaultMessage={'It is possible to create a Gateway environment with virtual hosts to access APIs.'
                    + ' API revisions can be attached with Vhost to access it.'}
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.Gateways.List.empty.title'
                    defaultMessage='Gateway Environments'
                />
            </Typography>
        ),
    };
    return (
        <ListBase
            columProps={columProps}
            pageProps={pageProps}
            addButtonProps={addButtonProps}
            searchProps={searchProps}
            emptyBoxProps={emptyBoxProps}
            apiCall={apiCall}
            EditComponent={AddEdit}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Edit Gateway Label',
            }}
            DeleteComponent={Delete}
        />
    );
}
