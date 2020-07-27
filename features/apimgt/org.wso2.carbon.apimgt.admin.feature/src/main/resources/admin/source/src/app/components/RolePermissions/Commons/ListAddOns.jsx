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
import { FormattedMessage } from 'react-intl';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import { makeStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';


const useStyles = makeStyles((theme) => ({
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
}));


/**
 *
 *
 * @param {*} props
 * @returns
 */
function ListAddOns(props) {
    const {
        searchActive, filterData, onRefresh, searchPlaceholder, children,
    } = props;
    const classes = useStyles();
    return (
        <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
            <Toolbar>
                <Grid container spacing={2} direction='row' justify='flex-end' alignItems='center'>
                    <Grid item>
                        {searchActive && (<SearchIcon className={classes.block} color='inherit' />)}
                    </Grid>
                    <Grid item xs>
                        {searchActive && (
                            <TextField
                                fullWidth
                                placeholder={searchPlaceholder}
                                InputProps={{
                                    disableUnderline: true,
                                    className: classes.searchInput,
                                }}
                                onChange={filterData}
                            />
                        )}
                    </Grid>
                    {children}
                    <Grid item>
                        <Tooltip title={(
                            <FormattedMessage
                                id='AdminPages.Addons.ListBase.reload'
                                defaultMessage='Reload'
                            />
                        )}
                        >
                            <IconButton onClick={onRefresh}>
                                <RefreshIcon className={classes.block} color='inherit' />
                            </IconButton>
                        </Tooltip>
                    </Grid>
                </Grid>
            </Toolbar>
        </AppBar>
    );
}

export default ListAddOns;
