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
import { useIntl, FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import Delete from 'AppComponents/KeyManagers/DeleteKeyManager';
import { Link as RouterLink } from 'react-router-dom';
import Button from '@material-ui/core/Button';

/**
 * API call to get microgateway labels
 * @returns {Promise}.
 */
function apiCall() {
    const restApi = new API();
    return restApi
        .getKeyManagersList()
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
export default function ListKeyManagers() {
    const intl = useIntl();
    const columProps = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'AdminPages.KeyManagers.table.header.label.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                        return <RouterLink to={`/settings/key-managers/${artifactId}`}>{value}</RouterLink>;
                    } else {
                        return <div />;
                    }
                },
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'AdminPages.KeyManagers.table.header.label.description',
                defaultMessage: 'Description',
            }),
            options: {
                sort: false,
            },
        },
        {
            name: 'type',
            label: intl.formatMessage({
                id: 'AdminPages.KeyManagers.table.header.label.type',
                defaultMessage: 'Type',
            }),
            options: {
                sort: false,
            },
        },
        { name: 'id', options: { display: false } },
    ];
    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'AdminPages.KeyManagers.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add KeyManager',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'AdminPages.KeyManagers.List.addButtonProps.title',
            defaultMessage: 'Add KeyManager',
        }),
    };
    const pageProps = {
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'AdminPages.KeyManagers.List.title',
            defaultMessage: 'Key Managers',
        }),
    };
    const addButtonOverride = (
        <RouterLink to='/settings/key-managers/create'>
            <Button variant='contained' color='primary' size='small'>
                <FormattedMessage
                    id='AdminPages.KeyManagers.List.addButtonProps.triggerButtonText'
                    defaultMessage='Add KeyManager'
                />
            </Button>
        </RouterLink>
    );
    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.KeyManagers.List.empty.content.keymanagers'
                    defaultMessage='It is possible to register an OAuth Provider.'
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.KeyManagers.List.empty.title'
                    defaultMessage='Key Managers'
                />
            </Typography>
        ),
    };
    return (
        <ListBase
            columProps={columProps}
            pageProps={pageProps}
            addButtonProps={addButtonProps}
            addButtonOverride={addButtonOverride}
            emptyBoxProps={emptyBoxProps}
            apiCall={apiCall}
            DeleteComponent={Delete}
        />
    );
}
