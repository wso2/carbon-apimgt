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
import { useAppContext } from 'AppComponents/Shared/AppContext';
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
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Configurations from 'Config';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';
import WarningBase from "AppComponents/AdminPages/Addons/WarningBase";

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListDetectedBotData() {
    const { settings } = useAppContext();
    const intl = useIntl();
    const isAnalyticsEnabled = settings.analyticsEnabled;
    const restApi = new API();
    const [hasListBotDetectionDataPermission, setHasListBotDetectionDataPermission] = useState(true);

    /**
     * API call to get Detected Data
     * @returns {Promise}.
     */
    function apiCall() {
        return restApi
            .getDetectedBotData()
            .then((result) => {
                return (result.body.list);
            })
            .catch((error) => {
                if (error.statusCode === 401) {
                    setHasListBotDetectionDataPermission(false);
                }
                throw error;
            });
    }

    const columProps = [
        {
            name: 'recordedTime',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.record.time',
                defaultMessage: 'Record Time',
            }),
            options: {
                sort: true,
                customBodyRender: (value) => {
                    const date = new Date(value);
                    return date.toUTCString();
                },
            },
        },
        {
            name: 'messageID',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.message.id',
                defaultMessage: 'Message ID',
            }),
            options: {
                sort: true,
            },
        },
        {
            name: 'apiMethod',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.api.method',
                defaultMessage: 'API method',
            }),
            options: {
                sort: true,
            },
        },
        {
            name: 'headerSet',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.headers.set',
                defaultMessage: 'Headers Set',
            }),
            options: {
                sort: true,
            },
        },
        {
            name: 'messageBody',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.message.body',
                defaultMessage: 'Message Body',
            }),
            options: {
                sort: true,
                customBodyRender: (value) => {
                    const emptyMessageBodyText = intl.formatMessage({
                        id: 'AdminPages.BotDetection.detected.data.table.row.empty.message.body.default.message',
                        defaultMessage: 'Empty Message Body',
                    });
                    if (value) {
                        if (value === '') {
                            return emptyMessageBodyText;
                        }
                        return value;
                    }
                    return emptyMessageBodyText;
                },
            },
        },
        {
            name: 'clientIp',
            label: intl.formatMessage({
                id: 'AdminPages.BotDetection.detected.data.table.header.label.client.ip',
                defaultMessage: 'Client IP',
            }),
            options: {
                sort: true,
            },
        },
    ];

    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'AdminPages.BotDetection.detected.data.List.search.default',
            defaultMessage: 'Search Bot data',
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
                                        id='AdminPages.BotDetection.detected.data.List.help.link.one'
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
                                        id='AdminPages.BotDetection.detected.data.List.help.link.two'
                                        defaultMessage='Viewing bot detection data via the Admin Portal'
                                    />
                                )}
                            />
                        </Link>
                    </ListItem>
                </List>
            </HelpBase>
        ),
        pageStyle: 'full',
        title: intl.formatMessage({
            id: 'AdminPages.BotDetection.detected.data.List.title',
            defaultMessage: 'Detected Data',
        }),
    };

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.BotDetection.detected.data.List.empty.content.microgateways'
                    values={{
                        breakingLine: <br />,
                    }}
                    defaultMessage={'Bot detection is enabled. There is no detected bot data. '
                        + 'When a bot attack is detected, you will be informed via email. '
                        + 'Set the email to be informed with, '
                        + '{breakingLine}{breakingLine}'
                        + 'Bot Detection Data > Configure Emails '
                        + '{breakingLine}{breakingLine}'
                        + 'on the left side menu.'}
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.BotDetection.detected.data.List.empty.title'
                    defaultMessage='No Bots detected!'
                />
            </Typography>
        ),
    };
    const analyticsDisabledEmptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='AdminPages.BotDetection.detected.data.List.analytics.disabled.empty.content'
                    values={{
                        breakingLine: <br />,
                    }}
                    defaultMessage={
                        'If you enable WSO2 API Manager Analytics with WSO2 API Manager, '
                        + 'you can enable email notifications for all unauthorized API calls that '
                        + 'you receive and also view the bot detection data easily via the Admin Portal.'
                        + '{breakingLine}{breakingLine}'
                        + 'Follow documentations on help to enable Analytics and get started.'
                    }
                />
            </Typography>
        ),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='AdminPages.BotDetection.detected.data.List.analytics.disabled.empty.title'
                    defaultMessage='Analytics disabled!'
                />
            </Typography>
        ),
    };
    if (isAnalyticsEnabled === undefined) {
        return (
            <ContentBase pageStyle='paperLess'>
                <InlineProgress />
            </ContentBase>
        );
    }

    if (!hasListBotDetectionDataPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'BotDetection.detected.data.List.title.detected.data',
                        defaultMessage: 'Detected Data',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='BotDetection.detected.data.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='BotDetection.detected.data.List.permission.denied.content'
                        defaultMessage={'You don\'t have sufficient permission to view detected data.'
                        + ' Please contact the site administrator.'}
                    />
                )}
            />
        );
    } else {
        return (
            isAnalyticsEnabled ? (
                <ListBase
                    columProps={columProps}
                    pageProps={pageProps}
                    searchProps={searchProps}
                    emptyBoxProps={emptyBoxProps}
                    apiCall={apiCall}
                    showActionColumn={false}
                />
            ) : (
                <ContentBase
                    {...pageProps}
                    pageStyle='small'
                >
                    <Card>
                        <CardContent>
                            {analyticsDisabledEmptyBoxProps.title}
                            {analyticsDisabledEmptyBoxProps.content}
                        </CardContent>
                    </Card>
                </ContentBase>
            ));
    }
}
