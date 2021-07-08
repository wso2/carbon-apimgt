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
import { useIntl, FormattedMessage } from 'react-intl';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Typography from '@material-ui/core/Typography';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import ListBase from 'AppComponents/AdminPages/Addons/ListBase';
import DescriptionIcon from '@material-ui/icons/Description';
import Switch from '@material-ui/core/Switch';
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import AddEdit from 'AppComponents/Throttling/Blacklist/AddEdit';
import Delete from 'AppComponents/Throttling/Blacklist/Delete';
import API from 'AppData/api';
import cloneDeep from 'lodash.clonedeep';
import Alert from 'AppComponents/Shared/Alert';
import WarningBase from "AppComponents/AdminPages/Addons/WarningBase";

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListBlacklistThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();
    const [blacklistPolicyList, setBlacklistPolicyList] = useState([]);
    const [hasListBlacklistThrottlingPoliciesPermission, setHasListBlacklistThrottlingPoliciesPermission] = useState(true);

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.addButtonProps.title',
            defaultMessage: 'Select Item to Deny ',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.search.default',
            defaultMessage: 'Search by Deny Policy name',
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
            + 'learn/rate-limiting/blacklisting-whitelisting/#blacklisting-requests'}
                        >
                            <ListItemText primary={(
                                <FormattedMessage
                                    id='Throttling.Blacklist.Policy.List.help.link.one'
                                    defaultMessage='Denying requests'
                                />
                            )}
                            />

                        </Link>
                    </ListItem>
                </List>
            </HelpBase>),
        pageStyle: 'half',
        title: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.search.default',
            defaultMessage: 'Deny Policies',
        }),
        EditTitle: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.search.default',
            defaultMessage: 'Deny Policies',
        }),
    };

    /**
 * Mock API call
 * @returns {Promise}.
 */
    function apiCall() {
        return restApi.blacklistPoliciesGet()
            .then((result) => {
                const policyList = result.body.list;
                const filteredPolicyList = policyList.filter((item) => item.conditionType !== 'SUBSCRIPTION');
                const blacklistPolicies = filteredPolicyList.map((obj) => {
                    let array = [];
                    let conditionTypeValue;
                    if (obj.conditionValue === Object(obj.conditionValue)) {
                        array = Object.entries(obj.conditionValue);
                    } else {
                        array.push(obj.conditionValue);
                    }
                    switch (obj.conditionType) {
                        case 'APPLICATION':
                            conditionTypeValue = 'Application';
                            break;
                        case 'IPRANGE':
                            conditionTypeValue = 'IP Range';
                            break;
                        default:
                            conditionTypeValue = obj.conditionType;
                            break;
                    }
                    return {
                        conditionId: obj.conditionId,
                        conditionType: conditionTypeValue,
                        conditionValue: array,
                        conditionStatus: obj.conditionStatus,
                    };
                });
                setBlacklistPolicyList(blacklistPolicies);
                return (blacklistPolicies);
            })
            .catch((error) => {
                if (error.statusCode === 401) {
                    setHasListBlacklistThrottlingPoliciesPermission(false);
                } else {
                    setHasListBlacklistThrottlingPoliciesPermission(true);
                    throw error;
                }
            });
    }

    const handleConditionStatus = (event) => {
        const blacklistPolicyListNew = cloneDeep(blacklistPolicyList);
        blacklistPolicyListNew.map((res) => {
            if (res.conditionId === event.target.id) {
                res.conditionStatus = event.target.checked;
            }
            return res.conditionStatus;
        });
        setBlacklistPolicyList(blacklistPolicyListNew);
        const promisedUpdateBlacklistPolicy = restApi.updateBlacklistPolicy(
            event.target.id, event.target.checked,
        );
        return promisedUpdateBlacklistPolicy
            .then(() => {
                Alert.success(intl.formatMessage({
                    id: 'Throttling.Blacklist.Policy.policy.update.success',
                    defaultMessage: 'Condition status has been updated successfully.',
                }));
            })
            .catch((error) => {
                const { response } = error;
                if (response.body) {
                    Alert.error(response.body.description);
                }
                return null;
            })
            .finally(() => {
                apiCall();
            });
    };

    const columProps = [
        {
            name: 'conditionType',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Blacklist.Throttling.policy.table.header.condition.type',
                defaultMessage: 'Condition Type',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'conditionValue',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Blacklist.Throttling.policy.table.header.conditional.value',
                defaultMessage: 'Conditional Value',
            }),
            options: {
                customBodyRender: (value) => (
                    <div>{value.length > 1 ? value.map((child) => <div>{child.join(' : ')}</div>) : value}</div>
                ),
                filter: true,
                sort: false,
            },
        },
        {
            name: 'conditionStatus',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Blacklist.Throttling.policy.table.header.condition.status',
                defaultMessage: 'Condition Status',
            }),
            options: {
                customBodyRender: (value, tableMeta) => {
                    const dataRow = blacklistPolicyList[tableMeta.rowIndex];
                    const { conditionId } = dataRow;
                    return (
                        <div>
                            <Switch
                                checked={blacklistPolicyList.find((x) => x.conditionId === conditionId)
                                    .conditionStatus}
                                onChange={handleConditionStatus}
                                id={conditionId}
                                name='conditionStatus'
                                color='primary'
                            />
                        </div>
                    );
                },
                filter: true,
                sort: false,
            },
        },
    ];

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Throttling.Blacklist.Policy.List.empty.content.blacklist.policies and abuse by'
                    defaultMessage='Denying requests from malicious entities helps you to keep your servers safe'
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Blacklist.Policy.List.empty.title.blacklist.policies'
                    defaultMessage='Deny Policies'
                />
            </Typography>),
    };

    if (!hasListBlacklistThrottlingPoliciesPermission) {
        return (
            <WarningBase
                pageProps={{
                    help: null,

                    pageStyle: 'half',
                    title: intl.formatMessage({
                        id: 'Throttling.Blacklist.Policy.List.title.deny.policies',
                        defaultMessage: 'Deny Policies',
                    }),
                }}
                title={(
                    <FormattedMessage
                        id='Throttling.Blacklist.Policy.List.permission.denied.title'
                        defaultMessage='Permission Denied'
                    />
                )}
                content={(
                    <FormattedMessage
                        id='Throttling.Blacklist.Policy.List.permission.denied.content'
                        defaultMessage={'You dont have enough permission to view Deny Policies.'
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
                DeleteComponent={Delete}
                EditComponent={AddEdit}
                isBlacklist
            />
        );
    }
}
