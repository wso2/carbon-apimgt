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
import Link from '@material-ui/core/Link';
import Configurations from 'Config';
import AddEdit from 'AppComponents/Throttling/Blacklist/AddEdit';
import Delete from 'AppComponents/Throttling/Blacklist/Delete';
import API from 'AppData/api';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */
export default function ListBlacklistThrottlingPolicies() {
    const intl = useIntl();
    const restApi = new API();
    let blacklistPolicyList;
    const [
        blacklistPoliciesList,
        setBlacklistPoliciesList,
    ] = useState([]);

    // const [state, setState] = React.useState({
    //     checkedA: true,
    //     checkedB: true,
    // });

    // const handleChange = (event) => {
    //     setState({ ...state, [event.target.name]: event.target.checked });
    // };

    const addButtonProps = {
        triggerButtonText: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.addButtonProps.triggerButtonText',
            defaultMessage: 'Add Policy',
        }),
        /* This title is what as the title of the popup dialog box */
        title: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.addButtonProps.title',
            defaultMessage: 'Select Item to Blacklist ',
        }),
    };
    const searchProps = {
        searchPlaceholder: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.List.search.default',
            defaultMessage: 'Search by Blacklist Policy name',
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
                                    defaultMessage='Blacklisting requests'
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
            defaultMessage: 'Blacklist Policies',
        }),
        EditTitle: intl.formatMessage({
            id: 'Throttling.Blacklist.Policy.search.default',
            defaultMessage: 'Blacklist Policies',
        }),
    };

    const columProps = [
        {
            name: 'conditionId',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Blacklist.Throttling.policy.table.header.condition.id',
                defaultMessage: 'Condition ID',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
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
        // {
        //     name: 'conditionStatus',
        //     label: intl.formatMessage({
        //         id: 'Admin.Throttling.Blacklist.Throttling.policy.table.header.condition.status',
        //         defaultMessage: 'Condition Status',
        //     }),
        //     customBodyRender: (value, tableMeta) => {
        //         // const dataRow = applicationThrottlingPolicyList[tableMeta.rowIndex];
        //         return (
        //             <Switch
        //                 checked={state.checkedB}
        //                 onChange={handleChange}
        //                 color='primary'
        //                 name='checkedB'
        //                 inputProps={{ 'aria-label': 'primary checkbox' }}
        //             />
        //         );
        //     },
        //     options: {
        //         filter: true,
        //         sort: false,
        //     },
        // },
    ];

    const emptyBoxProps = {
        content: (
            <Typography variant='body2' color='textSecondary' component='p'>
                <FormattedMessage
                    id='Throttling.Blacklist.Policy.List.empty.content.blacklist.policies and abuse by'
                    defaultMessage={'By blacklisting requests, you can protect servers from common attacks'
                    + ' users.'}
                />
            </Typography>),
        title: (
            <Typography gutterBottom variant='h5' component='h2'>
                <FormattedMessage
                    id='Throttling.Blacklist.Policy.List.empty.title.blacklist.policies'
                    defaultMessage='Blacklist Policies'
                />

            </Typography>),
    };

    /**
 * Mock API call
 * @returns {Promise}.
 */
    function apiCall() {
        let policyList;
        let a = 0;
        return new Promise(((resolve) => {
            restApi.blacklistPoliciesGet().then((result) => {
                policyList = result.body.list;
                console.log('policyList', policyList);
                blacklistPolicyList = policyList.map((obj) => {
                    let array = [];
                    a++;
                    if (obj.conditionValue === Object(obj.conditionValue)) {
                        Object.keys(obj.conditionValue);
                        Object.values(obj.conditionValue);
                        array = Object.entries(obj.conditionValue);
                    } else {
                        array.push(obj.conditionValue);
                    }
                    return {
                        conditionId: a,
                        conditionUUID: obj.conditionId,
                        conditionType: obj.conditionType,
                        conditionValue: array,
                    };
                });
                setBlacklistPoliciesList(blacklistPolicyList);
            });
            setTimeout(() => {
                resolve(blacklistPolicyList);
            }, 1000);
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
            deleteComponentProps={{
                blacklistPoliciesList,
            }}
            DeleteComponent={Delete}
            EditComponent={AddEdit}
            isBlacklist
        />
    );
}
