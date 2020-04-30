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
import { FormattedMessage } from 'react-intl';
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
import AddEdit from 'AppComponents/AdminPages/Microgateways/AddEdit';
import Alert from 'AppComponents/Shared/Alert';
import Delete from 'AppComponents/AdminPages/MicroGateways/Delete';

/**
 * Mock API call
 * @returns {Promise}.
 */
function apiCall() {
    return new Promise(((resolve) => {
        setTimeout(() => {
            resolve([
                { id: '1', label: 'West Wing', description: "It's somewhat hot" },
                { id: '2', label: 'East Wing', description: "It's cool" },
                { id: '3', label: 'South Wing', description: "It's red zone" },
                { id: '4', label: 'Noth Wing', description: "It's blue zone" },
            ]);
        }, 1000);
    }));
}

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
 * @returns {JSX} Header AppBar components.
 */
function ListLabels(props) {
    const { classes } = props;
    const [data, setData] = useState(null);
    const [searchText, setSearchText] = useState('');

    const filterData = (event) => {
        setSearchText(event.target.value);
    };
    const fetchData = () => {
    // Fetch data from backend
        setData(null);
        const promiseAPICall = apiCall();
        promiseAPICall.then((LocalData) => {
            setData(LocalData);
        })
            .catch((e) => {
                Alert.error(e);
            });
    };
    useEffect(() => {
        fetchData();
    }, []);
    const columns = [
        {
            name: 'label',
            label: 'Label',
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: 'description',
            label: 'Description',
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
                                updateList={() => fetchData()}
                                icon={<EditIcon />}
                                title='Edit Microgateway'
                            />
                            <Delete dataRow={dataRow} updateList={() => fetchData()} />
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
            {/*
                without pageStyle attribute = This is the default case where it
                takes fixed size center part of the screen.
                pageStyle='full-page' = Take the full content area.
                pageStyle='no-paper' = Avoid from displaying background paper. ( For dashbord we need this )
             */}
            <ContentBase title='Microgateways' subtitle='Microgateways' help={help} pageStyle='full-page'>
                <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                    <Toolbar>
                        <Grid container spacing={2} alignItems='center'>
                            <Grid item>
                                <SearchIcon className={classes.block} color='inherit' />
                            </Grid>
                            <Grid item xs>
                                <TextField
                                    fullWidth
                                    placeholder={(
                                        <FormattedMessage
                                            id='AdminPages.Microgateways.List.search.default'
                                            defaultMessage='Search by Microgateway label'
                                        />
                                    )}
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
                                    triggerButtonText='Add Microgateway'
                                    title='Add Microgateway'
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
                                id='AdminPages.Microgateways.List.nodata.message'
                                defaultMessage='No Gateway Labels Yet'
                            />
                        </Typography>
                    </div>
                )}
                {!data && (<InlineProgress />)}
            </ContentBase>
        </>
    );
}

ListLabels.propTypes = {
    classes: PropTypes.shape({}).isRequired,
};

export default withStyles(styles)(ListLabels);
