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
import DeleteEmail from 'AppComponents/BotDetection/EmailConfig/DeleteEmail';
import AddEmails from 'AppComponents/BotDetection/EmailConfig/AddEmail';

/**
 * API call to get all emails
 * @returns {Promise}.
 */
function apiCall() {
    const restApi = new API();
    return restApi
        .botDetectionNotifyingEmailsGet()
        .then((result) => {
            return result.body;
        })
        .catch((error) => {
            throw error;
        });
}

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListEmails() {
    const intl = useIntl();
    const columProps = [
        { name: 'uuid', options: { display: false } },
        {
            name: 'email',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.Email.List.table.header.email',
                defaultMessage: 'Email',
            }),
            options: {
                sort: true,
            },
        },
    ];
    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'AdminPages.BotDetection.Email.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Email',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'AdminPages.BotDetection.Email.List.addButtonProps.title',
            defaultMessage: 'Add Email',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.BotDetection.Email.List.search.default',
            defaultMessage: 'Search by Email',
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
                                + 'learn/api-security/threat-protection/bot-detection/'
                                + '#enabling-email-notifications-for-bot-detection'
                            }
                        >
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        id='AdminPages.BotDetection.Email.List.help.link.one'
                                        defaultMessage='Enabling email notifications for bot detection'
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
                            href={
                                Configurations.app.docUrl
                                + 'learn/api-security/threat-protection/bot-detection/'
                                + '#viewing-bot-detection-data-via-the-admin-portal'
                            }
                        >
                            <ListItemText
                                primary={(
                                    <FormattedMessage
                                        id='AdminPages.BotDetection.Email.List.help.link.two'
                                        defaultMessage='Viewing bot detection data via the Admin Portal'
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
            id: 'AdminPages.BotDetection.Email.List.title',
            defaultMessage: 'Emails',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.BotDetection.Email.List.empty.content'
                    values={{
                        breakingLine: <br />,
                    }}
                    defaultMessage={
                        'After a Publisher publishes APIs in the API Developer Portal, '
                        + 'hackers can invoke the APIs without an access token by scanning the '
                        + 'open ports of a system. Therefore, WSO2 API Manager has a '
                        + 'bot detection mechanism in place to prevent such attacks by '
                        + 'identifying who tried to enter and invoke resources without proper '
                        + 'authorization. WSO2 API Manager\'s bot detection mechanism traces and '
                        + 'logs details of such unauthorized API calls and sends notifications '
                        + 'in this regard via emails. Thereby this helps Publishers to protect '
                        + 'their data from bot attackers and improve the security of their data.'
                        + '{breakingLine}{breakingLine}'
                        + 'You can add, delete and see the list of registered emails here.'
                    }
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.BotDetection.Email.List.empty.title'
                    defaultMessage='Notification receiving Emails'
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
            EditComponent={AddEmails}
            editComponentProps={null}
            DeleteComponent={DeleteEmail}
        />
    );
}
