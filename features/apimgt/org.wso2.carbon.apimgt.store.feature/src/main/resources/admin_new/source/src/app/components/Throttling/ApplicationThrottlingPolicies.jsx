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
import Alert from 'AppComponents/Shared/Alert';
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
    const [isUpdated, setIsUpdated] = useState(false);
    const [applicationThrottlingPolicyList, setApplicationThrottlingPolicyList] = useState(null);

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

    /**
     * Delete an Application Policy
     * @param {string} selectedPolicyName selected policy name
     */
    function deleteApplicationPolicy(selectedPolicyName) {
        const selectedPolicy = applicationThrottlingPolicyList
            .filter((policy) => policy.policyName === selectedPolicyName);

        const policyId = selectedPolicy.length !== 0 && selectedPolicy[0].policyId;
        restApi.deleteApplicationThrottlingPolicy(policyId).then(() => {
            setIsUpdated(true);
            Alert.info(`${intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.Policy.policy.delete.succesful',
                defaultMessage: 'deleted successfully.',
            })}`);
        })
            .catch(() => {
                setIsUpdated(false);
                Alert.error(`${intl.formatMessage({
                    id: 'Admin.Throttling.Application.Throttling.Policy.policy.delete.error',
                    defaultMessage: 'Policy could not be deleted.',
                })}`);
            });
    }

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
            name: 'Actions',
            options: {
                customBodyRender: (value, tableMeta) => {
                    if (tableMeta.rowData) {
                        const row = tableMeta.rowData;
                        return (
                            <table className={classes.actionTable}>
                                <tr>
                                    <td>
                                        <Button>
                                            <Icon>edit</Icon>
                                            <FormattedMessage
                                                id='Admin.Throttling.Application.Throttling.Policy.edit'
                                                defaultMessage='Edit'
                                            />
                                        </Button>
                                    </td>
                                    <td>
                                        <Button
                                            onClick={() => deleteApplicationPolicy(row[0])}
                                        >
                                            <Icon>delete_forever</Icon>
                                            <FormattedMessage
                                                id='Admin.Throttling.Application.Throttling.Policy.delete'
                                                defaultMessage='Delete'
                                            />
                                        </Button>
                                    </td>
                                </tr>
                            </table>
                        );
                    }
                    return false;
                },
            },
        },
    ];

    useEffect(() => {
        restApi.applicationThrottlingPoliciesGet().then((result) => {
            setApplicationThrottlingPolicyList(result.body.list);
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
    }, [isUpdated]);

    return (
        <>
            <Box className={classes.mainTitle}>
                <Typography variant='h4'>
                    <FormattedMessage
                        id='Admin.Throttling.Application.Throttling.Policy.title'
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
