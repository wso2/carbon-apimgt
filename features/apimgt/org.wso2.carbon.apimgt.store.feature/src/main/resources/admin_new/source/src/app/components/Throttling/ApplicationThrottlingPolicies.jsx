/*
 * Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React, { useEffect, useState } from 'react';

import MUIDataTable from 'mui-datatables';
import {
    Box, Typography, Button, Icon,
} from '@material-ui/core';
import { FormattedMessage, injectIntl } from 'react-intl';
import { makeStyles } from '@material-ui/core/styles';
import API from 'AppData/api';

const useStyles = makeStyles((theme) => ({
    mainTitle: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(3),
    },
    editButton: {
        paddingTop: theme.spacing(2),
        paddingBottom: theme.spacing(3),
    },
}));


/**
 * Displays Application Throttling Policies
 */
function ApplicationThrottlingPolicies(props) {
    const classes = useStyles();
    const restApi = new API();
    const [data, setData] = useState(null);
    const { intl } = props;

    const columns = [
        intl.formatMessage({
            id: 'Admin.Throttling.Application.Throttling.policy.table.header.name',
            defaultMessage: 'Name',
        }),
        intl.formatMessage({
            id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota.policy',
            defaultMessage: 'Quota Policy',
        }),
        intl.formatMessage({
            id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota',
            defaultMessage: 'Quota',
        }),
        intl.formatMessage({
            id: 'Admin.Throttling.Application.Throttling.policy.table.header.unit.time',
            defaultMessage: 'Unit Time',
        }),
        {
            options: {
                filter: false,
                sort: false,
                empty: true,
                customBodyRender: () => {
                    return (
                        <>
                            <Button>
                                <Icon>edit</Icon>
                                <FormattedMessage
                                    id='Admin.Throttling.Application.Throttling.policy.edit'
                                    defaultMessage='Edit'
                                />
                            </Button>
                            <Button>
                                <Icon>delete</Icon>
                                <FormattedMessage
                                    id='Admin.Throttling.Application.Throttling.policy.edit'
                                    defaultMessage='Delete'
                                />
                            </Button>
                        </>
                    );
                },
            },
        },
    ];

    const options = {
        sort: true,
        search: true,
        viewColumns: true,
        filter: true,
        selectableRowsHeader: false,
        selectableRows: 'none',
        pagination: true,
        download: true,
    };


    useEffect(() => {
        restApi.applicationThrottlingPoliciesGet().then((result) => {
            const applicationPolicies = result.body.list.map((obj) => {
                return {
                    policyName: obj.policyName,
                    quotaPolicy: obj.defaultLimit.type,
                    quota: obj.defaultLimit.requestCount,
                    unitTime: (obj.defaultLimit.unitTime + ' ' + obj.defaultLimit.timeUnit),
                };
            });

            const applicationThrottlingvalues = applicationPolicies.filter((policy) => policy.policyName
            !== 'Unlimited').map((obj) => {
                return Object.values(obj);
            });
            setData(applicationThrottlingvalues);
        });
    }, []);

    return (
        <>
            <Box className={classes.mainTitle}>
                <Typography variant='h4'>
                    <FormattedMessage
                        id='Applications.Listing.Listing.applications'
                        defaultMessage='Application Throttling Policies'
                    />
                </Typography>

            </Box>
            {data && (
                <MUIDataTable
                    data={data}
                    columns={columns}
                    options={options}
                />
            )}
        </>

    );
}
export default injectIntl(ApplicationThrottlingPolicies);
