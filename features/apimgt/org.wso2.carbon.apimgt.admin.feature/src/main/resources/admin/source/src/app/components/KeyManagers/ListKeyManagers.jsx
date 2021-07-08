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
import API from 'AppData/api';
import { useIntl, FormattedMessage } from 'react-intl';
import Typography from '@material-ui/core/Typography';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import Delete from 'AppComponents/KeyManagers/DeleteKeyManager';
import { Link as RouterLink } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import Alert from 'AppComponents/Shared/Alert';
import Switch from '@material-ui/core/Switch';
import WarningBase from "AppComponents/AdminPages/Addons/WarningBase";

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListKeyManagers() {
    // eslint-disable-next-line no-unused-vars
    const [saving, setSaving] = useState(false);
    const intl = useIntl();
    const [hasListKeyManagersPermission, setHasListKeyManagersPermission] = useState(true);

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
                if (error.statusCode === 401) {
                    setHasListKeyManagersPermission(false);
                } else {
                    setHasListKeyManagersPermission(true);
                    throw error;
                }
            });
    }

    const columProps = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.name',
                defaultMessage: 'Name',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (typeof tableMeta.rowData === 'object') {
                        const artifactId = tableMeta.rowData[tableMeta.rowData.length - 2];
                        return (
                            <RouterLink to={`/settings/key-managers/${artifactId}`}>
                                {value}
                            </RouterLink>
                        );
                    } else {
                        return <div />;
                    }
                },
            },
        },
        {
            name: 'description',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.description',
                defaultMessage: 'Description',
            }),
            options: {
                sort: false,
            },
        },
        {
            name: 'type',
            label: intl.formatMessage({
                id: 'KeyManagers.ListKeyManagers.table.header.label.type',
                defaultMessage: 'Type',
            }),
            options: {
                sort: false,
            },
        },
        { name: 'enabled', options: { display: false } },
        { name: 'id', options: { display: false } },
    ];
    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'KeyManagers.ListKeyManagers.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Key Manager',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'KeyManagers.ListKeyManagers.List.addButtonProps.title',
            defaultMessage: 'Add Key Manager',
        }),
    };
    const pageProps = {
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'KeyManagers.ListKeyManagers.List.title',
            defaultMessage: 'Key Managers',
        }),
    };
    const addButtonOverride = (
        <RouterLink to='/settings/key-managers/create'>
            <Button variant='contained' color='primary' size='small'>
                <FormattedMessage
                    id='KeyManagers.ListKeyManagers.addButtonProps.triggerButtonText'
                    defaultMessage='Add Key Manager'
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
                    id='KeyManagers.ListKeyManagers.empty.title'
                    defaultMessage='Key Managers'
                />
            </Typography>
        ),
    };
    const addedActions = [
        (props) => {
            const { rowData, updateList } = props;
            const updateSomething = () => {
                const restApi = new API();
                const kmName = rowData[0];
                const kmId = rowData[4];
                restApi.keyManagerGet(kmId).then((result) => {
                    let editState;
                    if (result.body.name !== null) {
                        editState = {
                            ...result.body,
                        };
                    }
                    editState.enabled = !editState.enabled;
                    restApi.updateKeyManager(kmId, editState).then(() => {
                        Alert.success(` ${kmName} ${intl.formatMessage({
                            id: 'KeyManagers.ListKeyManagers.edit.success',
                            defaultMessage: ' Key Manager updated successfully.',
                        })}`);
                        setSaving(false);
                        updateList();
                    }).catch((e) => {
                        const { response } = e;
                        if (response.body) {
                            Alert.error(response.body.description);
                        }
                        setSaving(false);
                        updateList();
                    });
                });
            };
            const kmEnabled = rowData[3];

            return (
                <Switch
                    checked={kmEnabled}
                    onChange={updateSomething}
                    color='primary'
                    size='small'
                />
            );
        },
    ];

    if (!hasListKeyManagersPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'KeyManagers.ListKeyManagers.title.keyManagers',
                        defaultMessage: 'Key Managers',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='KeyManagers.ListKeyManagers.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='KeyManagers.ListKeyManagers.permission.denied.content'
                        defaultMessage={'You don\'t have sufficient permission to view Key Managers.'
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
                addButtonOverride={addButtonOverride}
                emptyBoxProps={emptyBoxProps}
                apiCall={apiCall}
                DeleteComponent={Delete}
                addedActions={addedActions}
            />
        );
    }
}
