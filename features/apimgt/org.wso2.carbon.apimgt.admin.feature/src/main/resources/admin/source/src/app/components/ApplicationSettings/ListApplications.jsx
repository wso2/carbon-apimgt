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
import { makeStyles } from '@material-ui/core/styles';
import API from 'AppData/api';
import { useIntl, FormattedMessage } from 'react-intl';
import EditApplication from 'AppComponents/ApplicationSettings/EditApplication';
import AppsTableContent from 'AppComponents/ApplicationSettings/AppsTableContent';
import ApplicationTableHead from 'AppComponents/ApplicationSettings/ApplicationTableHead';
import EditIcon from '@material-ui/icons/Edit';
import Table from '@material-ui/core/Table';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import TableFooter from '@material-ui/core/TableFooter';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Grid from '@material-ui/core/Grid';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import HighlightOffRoundedIcon from '@material-ui/icons/HighlightOffRounded';
import Tooltip from '@material-ui/core/Tooltip';
import TextField from '@material-ui/core/TextField';
import SearchIcon from '@material-ui/icons/Search';

/**
 * Render a list
 * @returns {JSX} Header AppBar components.
 */

const useStyles = makeStyles((theme) => ({
    searchBar: {
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
    },
    block: {
        display: 'block',
    },
    clearSearch: {
        position: 'absolute',
        right: 111,
        top: 13,
    },
    addUser: {
        marginRight: theme.spacing(1),
    },
}));

export default function ListApplications() {
    const intl = useIntl();
    const classes = useStyles();
    const [applicationList, setApplicationList] = useState([]);
    const [totalApps, setTotalApps] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(10);
    const [page, setPage] = useState(0);
    const [owner, setOwner] = useState('');

    /**
    * API call to get application list
    * @returns {Promise}.
    */
    function apiCall(pageNo) {
        const restApi = new API();
        return restApi
            .getApplicationList({ limit: rowsPerPage, offset: pageNo * rowsPerPage, user: owner })
            .then((result) => {
                setApplicationList(result.body.list);
                const { pagination: { total } } = result.body;
                setTotalApps(total);
                return result.body.list;
            })
            .catch((error) => {
                throw error;
            });
    }

    useEffect(() => {
        apiCall(page).then((result) => {
            setApplicationList(result);
        });
    }, [page]);

    useEffect(() => {
        apiCall(page).then((result) => {
            setApplicationList(result);
        });
    }, [rowsPerPage]);

    function handleChangePage(event, pageNo) {
        setPage(pageNo);
        apiCall(pageNo).then((result) => {
            setApplicationList(result);
        });
    }

    function handleChangeRowsPerPage(event) {
        const nextRowsPerPage = event.target.value;
        const rowsPerPageRatio = rowsPerPage / nextRowsPerPage;
        const nextPage = Math.floor(page * rowsPerPageRatio);
        setPage(nextPage);
        setRowsPerPage(nextRowsPerPage);
        apiCall(page).then((result) => {
            setApplicationList(result);
        });
    }

    function clearSearch() {
        setPage(0);
        setOwner('');
        apiCall(page).then((result) => {
            setApplicationList(result);
        });
    }

    function setQuery(event) {
        const newQuery = event.target.value;
        if (newQuery === '') {
            clearSearch();
        } else {
            setOwner(newQuery);
        }
    }

    function filterApps() {
        setPage(0);
        apiCall(page).then((result) => {
            setApplicationList(result);
        });
    }

    return (
        <ContentBase>
            <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                <Toolbar>
                    <Grid container spacing={2} alignItems='center'>
                        <Grid item>
                            <SearchIcon className={classes.block} color='inherit' />
                        </Grid>
                        <Grid item xs>
                            <TextField
                                fullWidth
                                id='search-label'
                                label={intl.formatMessage({
                                    defaultMessage: 'Search',
                                    id: 'Applications.Listing.Listing.applications.search.label',
                                })}
                                placeholder='Search application by owner'
                                InputProps={{
                                    disableUnderline: true,
                                    className: classes.searchInput,
                                }}
                                value={owner}
                                onChange={setQuery}
                                // onKeyPress={this.handleSearchKeyPress}
                            />
                            { owner.length > 0
                                && (
                                    <Tooltip
                                        title={
                                            intl.formatMessage({
                                                defaultMessage: 'Clear Search',
                                                id: 'Applications.Listing.Listing.clear.search',
                                            })
                                        }
                                    >
                                        <IconButton
                                            aria-label='delete'
                                            className={classes.clearSearch}
                                            onClick={clearSearch}
                                        >
                                            <HighlightOffRoundedIcon />
                                        </IconButton>
                                    </Tooltip>
                                )}
                        </Grid>
                        <Grid item>
                            <Button variant='contained' className={classes.addUser} onClick={filterApps}>
                                <FormattedMessage
                                    id='Applications.Listing.Listing.applications.search'
                                    defaultMessage='Search'
                                />
                            </Button>

                        </Grid>
                    </Grid>
                </Toolbar>
            </AppBar>
            <Table id='itest-application-list-table'>
                <ApplicationTableHead />
                <AppsTableContent
                    apps={applicationList}
                    page={page}
                    rowsPerPage={rowsPerPage}
                    editComponentProps={{
                        icon: <EditIcon />,
                        title: 'Change Application Owner',
                        applicationList,
                    }}
                    EditComponent={EditApplication}
                    apiCall={apiCall}
                />
                <TableFooter>
                    <TableRow>
                        <TablePagination
                            component='td'
                            count={totalApps}
                            rowsPerPage={rowsPerPage}
                            rowsPerPageOptions={[5, 10, 15]}
                            labelRowsPerPage='Show'
                            page={page}
                            backIconButtonProps={{
                                'aria-label': 'Previous Page',
                            }}
                            nextIconButtonProps={{
                                'aria-label': 'Next Page',
                            }}
                            onChangePage={handleChangePage}
                            onChangeRowsPerPage={handleChangeRowsPerPage}
                        />
                    </TableRow>
                </TableFooter>
            </Table>
        </ContentBase>
    );
}
