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

import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl } from 'react-intl';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import { withStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import EditIcon from '@material-ui/icons/Edit';
import MUIDataTable from 'mui-datatables';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import HelpBase from 'AppComponents/AdminPages/Addons/HelpBase';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';
import AddEdit from 'AppComponents/Throttling/Application/AddEdit';
import Delete from 'AppComponents/Throttling/Application/Delete';
import API from 'AppData/api';

const styles = (theme) => ({
    searchBar: {
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
    },
    searchInput: {
        fontSize: theme.typography.fontSize,
    },
    block: {
        display: 'block',
    },
    contentWrapper: {
        margin: '40px 16px',
    },
    button: {
        borderColor: 'rgba(255, 255, 255, 0.7)',
    },
});

/**
 * Render a list
 * @param {JSON} props .
 * @returns {JSX} Header AppBar components .
 */
function ApplicationThrottlingPolicies(props) {
    const { classes, intl } = props;
    const [data, setData] = useState(null);
    const [searchText, setSearchText] = useState('');
    const restApi = new API();
    const [
        applicationThrottlingPolicyList,
        setApplicationThrottlingPolicyList,
    ] = useState(null);
    const filterData = (event) => {
        setSearchText(event.target.value);
    };
    const [selectedRow, setSelectedRow] = useState(null);

    const fetchData = () => {
        restApi.applicationThrottlingPoliciesGet().then((result) => {
            setApplicationThrottlingPolicyList(result.body.list);
            const applicationPolicies = result.body.list.map((obj) => {
                return {
                    policyName: obj.policyName,
                    quotaPolicy: obj.defaultLimit.type,
                    quota: obj.defaultLimit.requestCount
                    || obj.defaultLimit.dataAmount + ' ' + obj.defaultLimit.dataUnit,
                    unitTime: obj.defaultLimit.unitTime + ' ' + obj.defaultLimit.timeUnit,
                };
            });

            const applicationThrottlingvalues = applicationPolicies
                .filter((policy) => policy.policyName !== 'Unlimited')
                .map((obj) => {
                    return Object.values(obj);
                });
            setData(applicationThrottlingvalues);
        });
    };

    useEffect(() => {
        fetchData();
    }, []);

    const columns = [
        {
            name: 'name',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.name',
                defaultMessage: 'Name',
            }),
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: 'quotaPolicy',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota.policy',
                defaultMessage: 'Quota Policy',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'quota',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.quota',
                defaultMessage: 'Quota',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'unitTime',
            label: intl.formatMessage({
                id: 'Admin.Throttling.Application.Throttling.policy.table.header.unit.time',
                defaultMessage: 'Unit Time',
            }),
            options: {
                filter: true,
                sort: false,
            },
        },
        {
            name: 'id',
            label: 'Actions',
            options: {
                filter: false,
                sort: false,
                customBodyRender: (value, tableMeta) => {
                    const dataRow = data[tableMeta.rowIndex];
                    return (
                        <>
                            <AddEdit
                                dataRow={dataRow}
                                applicationThrottlingPolicyList={applicationThrottlingPolicyList}
                                updateList={() => fetchData()}
                                icon={<EditIcon />}
                                title='Edit Application Throttling Policy'
                                selectedRow={selectedRow}
                            />
                            <Delete
                                selectedPolicyName={dataRow[0]}
                                applicationThrottlingPolicyList={applicationThrottlingPolicyList}
                                updateList={() => fetchData()}
                            />
                        </>
                    );
                },
                setCellProps: () => {
                    return {
                        style: { width: 200 },
                    };
                },
            },
        },
    ];

    const handleRowClick = (rowData) => {
        setSelectedRow(rowData);
    };

    const options = {
        filterType: 'checkbox',
        selectableRows: 'none',
        filter: false,
        search: false,
        print: false,
        download: false,
        viewColumns: false,
        customToolbar: null,
        responsive: 'stacked',
        searchText,
        onRowClick: handleRowClick,
    };


    const help = (
        <HelpBase>
            <List component='nav' aria-label='main mailbox folders'>
                <ListItem button>
                    <ListItemIcon>
                        <RefreshIcon />
                    </ListItemIcon>
                    <ListItemText primary='Inbox' />
                </ListItem>
                <ListItem button>
                    <ListItemIcon>
                        <RefreshIcon />
                    </ListItemIcon>
                    <ListItemText primary='Drafts' />
                </ListItem>
            </List>
        </HelpBase>
    );

    return (

        <>
            <ContentBase
                title='Application Throttling Policies'
                subtitle='Application Throttling Policies'
                help={help}
                pageStyle='full-page'
            >
                <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                    <Toolbar>
                        <Grid container spacing={2} alignItems='center'>
                            <Grid item>
                                <SearchIcon className={classes.block} color='inherit' />
                            </Grid>
                            <Grid item xs>
                                <TextField
                                    fullWidth
                                    placeholder={intl.formatMessage({
                                        id: 'Throttling.Application.Policy.search.default',
                                        defaultMessage: 'Search by Application Policy name',
                                    })}
                                    InputProps={{
                                        disableUnderline: true,
                                        className: classes.searchInput,
                                    }}
                                    onChange={filterData}
                                />
                            </Grid>
                            <Grid item>
                                <AddEdit
                                    updateList={() => fetchData()}
                                    triggerButtonText='Add Policy'
                                    title='Add Policy'
                                />
                                <Tooltip title='Reload'>
                                    <IconButton onClick={fetchData}>
                                        <RefreshIcon className={classes.block} color='inherit' />
                                    </IconButton>
                                </Tooltip>
                            </Grid>
                        </Grid>
                    </Toolbar>
                </AppBar>

                {data && data.length > 0 && (
                    <MUIDataTable
                        title={null}
                        data={data}
                        columns={columns}
                        options={options}
                    />
                )}
                {data && data.length === 0 && (
                    <div className={classes.contentWrapper}>
                        <Typography color='textSecondary' align='center'>
                            <FormattedMessage
                                id='Throttling.Application.Policy.List.nodata.message'
                                defaultMessage='No Application Polcies'
                            />
                        </Typography>
                    </div>
                )}
                {!data && (<InlineProgress />)}
            </ContentBase>
        </>
    );
}

ApplicationThrottlingPolicies.propTypes = {
    classes: PropTypes.shape({}).isRequired,
    intl: PropTypes.shape({}).isRequired,
};

export default injectIntl(withStyles(styles)(ApplicationThrottlingPolicies));
