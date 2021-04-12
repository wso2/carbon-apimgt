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
import SettingsEthernetIcon from '@material-ui/icons/SettingsEthernet';
import SettingsIcon from '@material-ui/icons/Settings';
import PhonelinkSetupIcon from '@material-ui/icons/PhonelinkSetup';
import HomeIcon from '@material-ui/icons/Home';
import Dashboard from 'AppComponents/AdminPages/Dashboard/Dashboard';
import ApplicationThrottlingPolicies from 'AppComponents/Throttling/Application/List';
import SubscriptionThrottlingPolicies from 'AppComponents/Throttling/Subscription/index';
import APICategories from 'AppComponents/APICategories/ListApiCategories';
import BlacklistThrottlingPolicies from 'AppComponents/Throttling/Blacklist/List';
import ListApplications from 'AppComponents/ApplicationSettings/ListApplications';
import GatewayEnvironments from 'AppComponents/GatewayEnvironments/ListGWEnviornments';
import AdvancedThrottlePolicies from 'AppComponents/Throttling/Advanced';
import CustomThrottlingPolicies from 'AppComponents/Throttling/Custom';
import TenantTheme from 'AppComponents/TenantTheme/UploadTheme';
import KeyManagers from 'AppComponents/KeyManagers';
import ListRoles from 'AppComponents//RolePermissions/ListRoles.jsx';

import GamesIcon from '@material-ui/icons/Games';
import CategoryIcon from '@material-ui/icons/Category';
import PolicyIcon from '@material-ui/icons/Policy';
import BlockIcon from '@material-ui/icons/Block';
import AssignmentIcon from '@material-ui/icons/Assignment';
import ApplicationCreation from 'AppComponents/Workflow/ApplicationCreation';
import SubscriptionCreation from 'AppComponents/Workflow/SubscriptionCreation';
import SubscriptionUpdate from 'AppComponents/Workflow/SubscriptionUpdate';
import RegistrationCreation from 'AppComponents/Workflow/RegistrationCreation';
import APIStateChange from 'AppComponents/Workflow/APIStateChange';
import UserCreation from 'AppComponents/Workflow/UserCreation';
import AssignmentTurnedInIcon from '@material-ui/icons/AssignmentTurnedIn';
import SecurityIcon from '@material-ui/icons/Security';
import TouchAppIcon from '@material-ui/icons/TouchApp';
import VpnKeyIcon from '@material-ui/icons/VpnKey';
import AccountTreeIcon from '@material-ui/icons/AccountTree';

const RouteMenuMapping = (intl) => [
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.dashboard',
            defaultMessage: 'Dashboard',
        }),
        icon: <HomeIcon />,
        path: '/dashboard',
        component: () => <Dashboard />,
        exact: true,
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
                component: AdvancedThrottlePolicies,
                icon: <PolicyIcon />,
                addEditPageDetails: [
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.advanced.throttling.policies.Adding',
                            defaultMessage: 'Add Advanced Policy',
                        }),
                        path: '/throttling/advanced/create',
                    },
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.advanced.throttling.policies.Editing',
                            defaultMessage: 'Edit Advanced Policy',
                        }),
                        path: '/throttling/advanced/(.*?)$',
                    },
                ],
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.application.throttling.policies',
                    defaultMessage: 'Application Policies',
                }),
                path: '/throttling/application',
                component: ApplicationThrottlingPolicies,
                icon: <PhonelinkSetupIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.subscription.throttling.policies',
                    defaultMessage: 'Subscription Policies',
                }),
                path: '/throttling/subscription',
                component: SubscriptionThrottlingPolicies,
                icon: <AssignmentTurnedInIcon />,
                addEditPageDetails: [
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.subscription.throttling.policies.Adding',
                            defaultMessage: 'Add Subscription Policy',
                        }),
                        path: '/throttling/subscription/add',
                    },
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.subscription.throttling.policies.Editing',
                            defaultMessage: 'Edit Subscription Policy',
                        }),
                        path: '/throttling/subscription/(.*?)$',
                    },
                ],
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.custom.throttling.policies',
                    defaultMessage: 'Custom Policies',
                }),
                path: '/throttling/custom',
                component: CustomThrottlingPolicies,
                icon: <AssignmentIcon />,
                addEditPageDetails: [
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.custom.throttling.policies.items.Adding',
                            defaultMessage: 'Add Custom Policy',
                        }),
                        path: '/throttling/custom/create',
                    },
                    {
                        id: intl.formatMessage({
                            id: 'Base.RouteMenuMapping.custom.throttling.policies.items.Editing',
                            defaultMessage: 'Edit Custom Policy',
                        }),
                        path: '/throttling/custom/(.*?)$',
                    },
                ],
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.blacklisted.items',
                    defaultMessage: 'Deny Policies',
                }),
                path: '/throttling/deny-policies',
                component: BlacklistThrottlingPolicies,
                icon: <BlockIcon />,
            },
        ],
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.gateways',
            defaultMessage: 'Gateways',
        }),
        path: '/settings/environments',
        component: GatewayEnvironments,
        icon: <GamesIcon />,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.api.categories',
            defaultMessage: 'API Categories',
        }),
        path: '/settings/api-categories',
        component: APICategories,
        icon: <CategoryIcon />,
    },
    {
        id: intl.formatMessage({
            id: 'Base.RouteMenuMapping.keymanagers',
            defaultMessage: 'Key Managers',
        }),
        path: '/settings/key-managers',
        component: KeyManagers,
        icon: <SecurityIcon />,
        addEditPageDetails: [
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.keymanagers.items.Adding',
                    defaultMessage: 'Add Key Manager',
                }),
                path: '/settings/key-managers/create',
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.keymanagers.items.Editing',
                    defaultMessage: 'Edit Key Manager',
                }),
                path: '/settings/key-managers/(.*?)$',
            },
        ],
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
                component: UserCreation,
                icon: <PeopleIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.application.creation',
                    defaultMessage: 'Application Creation',
                }),
                path: '/tasks/application-creation',
                component: ApplicationCreation,
                icon: <DnsRoundedIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.subscription.creation',
                    defaultMessage: 'Subscription Creation',
                }),
                path: '/tasks/subscription-creation',
                component: SubscriptionCreation,
                icon: <TouchAppIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.subscription.update',
                    defaultMessage: 'Subscription Update',
                }),
                path: '/tasks/subscription-update',
                component: SubscriptionUpdate,
                icon: <TouchAppIcon />,
            },
            {
                id: 'Application Registration',
                path: '/tasks/application-registration',
                component: RegistrationCreation,
                icon: <VpnKeyIcon />,
            },
            {
                id: 'API State Change',
                path: '/tasks/api-state-change',
                component: APIStateChange,
                icon: <SettingsEthernetIcon />,
            },
        ],
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
                component: ListApplications,
                icon: <SettingsIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.role.permissions',
                    defaultMessage: 'Scope Assignments',
                }),
                path: '/settings/scope-mapping',
                component: ListRoles,
                icon: <AccountTreeIcon />,
            },
            {
                id: intl.formatMessage({
                    id: 'Base.RouteMenuMapping.tenant.theme',
                    defaultMessage: 'Tenant Theme',
                }),
                path: '/settings/devportal-theme',
                component: TenantTheme,
                icon: <PhonelinkSetupIcon />,
            },
        ],
    },

];

export default RouteMenuMapping;
