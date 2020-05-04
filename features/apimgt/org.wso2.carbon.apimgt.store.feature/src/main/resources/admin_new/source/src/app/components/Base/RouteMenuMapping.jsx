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
import PeopleIcon from '@material-ui/icons/People';
import DnsRoundedIcon from '@material-ui/icons/DnsRounded';
import PermMediaOutlinedIcon from '@material-ui/icons/PhotoSizeSelectActual';
import PublicIcon from '@material-ui/icons/Public';
import SettingsEthernetIcon from '@material-ui/icons/SettingsEthernet';
import TimerIcon from '@material-ui/icons/Timer';
import SettingsIcon from '@material-ui/icons/Settings';
import PhonelinkSetupIcon from '@material-ui/icons/PhonelinkSetup';
import HomeIcon from '@material-ui/icons/Home';
import Dashboard from 'AppComponents/AdminPages/Dashboard/Dashboard';
import DemoTable from 'AppComponents/AdminPages/Microgateways/List';
import ApplicationThrottlingPolicies from 'AppComponents/Throttling/Application/List';
import APICategories from 'AppComponents/APICategories/ListApiCategories';


const RouteMenuMapping = (intl) => [
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.dashboard',
            defaultMessage: 'Admin Dashboard',
        }),
        icon: <HomeIcon />,
        path: '/dashboard',
        component: <Dashboard />,
        exact: true,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.tasks',
            defaultMessage: 'Tasks',
        }),
        children: [
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.user.creation',
                    defaultMessage: 'User Creation',
                }),
                path: '/tasks/user-creation',
                component: () => <DemoTable />,
                icon: <PeopleIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.application.creation',
                    defaultMessage: 'Application Creation',
                }),
                path: '/tasks/application-creation',
                component: () => <DemoTable />,
                icon: <DnsRoundedIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.subscription.creation',
                    defaultMessage: 'Subscription Creation',
                }),
                path: '/tasks/subscription-creation',
                component: () => <DemoTable />,
                icon: <PermMediaOutlinedIcon />,
            },
            {
                id: 'Application Registration',
                path: '/tasks/application-registration',
                component: () => <DemoTable />,
                icon: <PublicIcon />,
            },
            {
                id: 'API State Change',
                path: '/tasks/api-state-change',
                component: () => <DemoTable />,
                icon: <SettingsEthernetIcon />,
            },
        ],
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.microgateways',
            defaultMessage: 'Microgateways',
        }),
        path: '/settings/mg-labels',
        component: () => <DemoTable />,
        icon: <PhonelinkSetupIcon />,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.api.categories',
            defaultMessage: 'API Categories',
        }),
        path: '/settings/api-categories',
        component: () => <APICategories />,
        icon: <PhonelinkSetupIcon />,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.bot.detection',
            defaultMessage: 'Bot Detection',
        }),
        path: '/settings/bot-detection',
        component: () => <DemoTable />,
        icon: <PhonelinkSetupIcon />,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.settings',
            defaultMessage: 'Settings',
        }),
        children: [
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.applications',
                    defaultMessage: 'Applications',
                }),
                path: '/settings/applications',
                component: () => <DemoTable />,
                icon: <SettingsIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.scope.mapping',
                    defaultMessage: 'Scope Mapping',
                }),
                path: '/settings/scope-mapping',
                component: () => <DemoTable />,
                icon: <TimerIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.devportal.theme',
                    defaultMessage: 'Devportal Theme',
                }),
                path: '/settings/devportal-theme',
                component: () => <DemoTable />,
                icon: <PhonelinkSetupIcon />,
            },
        ],
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.throttling.policies',
            defaultMessage: 'Rate Limiting Policies',
        }),
        children: [
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.advanced.throttling.policies',
                    defaultMessage: 'Advanced Policies',
                }),
                path: '/throttling/advanced',
                component: () => <DemoTable />,
                icon: <SettingsIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.application.throttling.policies',
                    defaultMessage: 'Application Policies',
                }),
                path: '/throttling/application',
                component: () => <ApplicationThrottlingPolicies />,
                icon: <TimerIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.subscription.throttling.policies',
                    defaultMessage: 'Subscription Policies',
                }),
                path: '/throttling/subscription',
                component: () => <DemoTable />,
                icon: <PhonelinkSetupIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.custom.throttling.policies',
                    defaultMessage: 'Custom Policies',
                }),
                path: '/throttling/custom',
                component: () => <DemoTable />,
                icon: <PhonelinkSetupIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.blacklisted.items',
                    defaultMessage: 'Blacklist Policies',
                }),
                path: '/throttling/blacklisted',
                component: () => <DemoTable />,
                icon: <PhonelinkSetupIcon />,
            },
        ],
    },

];

export default RouteMenuMapping;
