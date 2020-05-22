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
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import EditApplication from 'AppComponents/ApplicationSettings/EditApplication';
import DescriptionIcon from '@material-ui/icons/Description';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import EditIcon from '@material-ui/icons/Edit';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListApplications() {
    const intl = useIntl();
    const [applicationList, setApplicationList] = useState([]);
    /**
    * API call to get application list
    * @returns {Promise}.
    */
    function apiCall() {
        return new Promise((resolve, reject) => {
            const restApi = new API();
            restApi
                .getApplicationList()
                .then((result) => {
                    setApplicationList(result.body.list);
                    resolve(result.body.list);
                })
                .catch((error) => {
                    reject(error);
                });
        });
    }
    const columProps = [
        { name: 'applicationId', options: { display: false } },
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'AdminPages.ApplicationSettings.table.header.application.name',
                defaultMessage: 'Application Name',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: 'owner',
            label: intl.formatMessage({
                id: 'AdminPages.ApplicationSettings.table.header.application.owner',
                defaultMessage: 'Owner',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
    ];
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.ApplicationSettings.List.search.default',
            defaultMessage: 'Search by Application Name or Owner',
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
                            href={
                                Configurations.app.docUrl
                                + 'learn/consume-api/manage-application/advanced-topics/'
                                + 'changing-the-owner-of-an-application/'
                            }
                        >
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        id='AdminPages.ApplicationSettings.List.help.link.one'
                                        defaultMessage='Changing the Owner of an Application'
                                    />
                                )}
                            />
                        </Link>
                    </ListItem>
                </List>
            </HelpBase>
        ),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'AdminPages.ApplicationSettings.List.title.application.settings',
            defaultMessage: 'Application Settings',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.ApplicationSettings.List.empty.content.application.settings'
                    values={{
                        breakingLine: <br />,
                    }}
                    defaultMessage={
                        'If required, you can transfer the ownership of your application to another user '
                        + 'in your organization. Thereby, when transferring ownership, the new owner '
                        + 'will have the required permission to delete or edit the respective application.'
                        + '{breakingLine}{breakingLine}'
                        + 'Create an application with Devportal to change ownership.'
                    }
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.ApplicationSettings.List.empty.title.change.application.ownership'
                    defaultMessage='Application Ownership Change'
                />
            </Typography>
        ),
    };

    return (
        <ListBase
            columProps={columProps}
            pageProps={pageProps}
            addButtonProps={() => <span />}
            searchProps={searchProps}
            emptyBoxProps={emptyBoxProps}
            apiCall={apiCall}
            EditComponent={EditApplication}
            editComponentProps={{
                icon: <EditIcon />,
                title: 'Change Application Owner',
                applicationList,
            }}
            DeleteComponent={() => <span />}
        />
    );
}
