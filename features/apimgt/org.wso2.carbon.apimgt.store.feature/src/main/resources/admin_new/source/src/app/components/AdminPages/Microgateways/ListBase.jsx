/* eslint-disable react/jsx-props-no-spreading */
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
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import TextField from '@material-ui/core/TextField';
import Tooltip from '@material-ui/core/Tooltip';
import IconButton from '@material-ui/core/IconButton';
import { makeStyles } from '@material-ui/core/styles';
import SearchIcon from '@material-ui/icons/Search';
import RefreshIcon from '@material-ui/icons/Refresh';
import EditIcon from '@material-ui/icons/Edit';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import MUIDataTable from 'mui-datatables';
import ContentBase from 'AppComponents/AdminPages/Addons/ContentBase';
import InlineProgress from 'AppComponents/AdminPages/Addons/InlineProgress';
import Delete from 'AppComponents/AdminPages/Microgateways/Delete';
import AddEdit from 'AppComponents/AdminPages/Microgateways/AddEdit';
import Alert from 'AppComponents/Shared/Alert';

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
 * Render a list
 * @param {JSON} props props passed from parent
 * @returns {JSX} Header AppBar components.
 */
function ListLabels(props) {
    const {
        columProps, pageProps, addButtonProps,
        searchProps, apiCall, emptyBoxProps: {
            title: emptyBoxTitle,
            content: emptyBoxContent,
        },
        actionColumnProps: {
            editIconShow, editIconOverride, deleteIconShow,
        },
    } = props;

    const classes = useStyles();
    const [searchText, setSearchText] = useState('');
    const [data, setData] = useState(null);

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
        ...columProps,
    ];
    if (deleteIconShow || editIconShow) {
        columns.push(
            {
                name: '',
                label: 'Actions',
                options: {
                    filter: false,
                    sort: false,
                    customBodyRender: (value, tableMeta) => {
                        const dataRow = data[tableMeta.rowIndex];
                        return (
                            <>
                                {editIconShow && !editIconOverride && (
                                    <AddEdit
                                        dataRow={dataRow}
                                        updateList={fetchData}
                                        icon={<EditIcon />}
                                        title='Edit Microgateway'
                                    />
                                )}
                                {editIconShow && editIconOverride && (
                                    editIconOverride
                                )}
                                {deleteIconShow && (<Delete dataRow={dataRow} updateList={fetchData} />)}
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
        );
    }
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
    if (data && data.length === 0) {
        return (
            <ContentBase
                pageStyle='small'
                title={(
                    <FormattedMessage
                        id='AdminPages.Microgateways.ListBase.nodata.title'
                        defaultMessage='Microgateways'
                    />
                )}
            >
                <Card className={classes.root}>
                    <CardActionArea>
                        <CardContent>
                            {emptyBoxTitle}
                            {emptyBoxContent}
                        </CardContent>
                    </CardActionArea>
                    <CardActions>
                        <AddEdit
                            updateList={fetchData}
                            {...addButtonProps}
                        />
                    </CardActions>
                </Card>
            </ContentBase>
        );
    }
    if (!data) {
        return (
            <ContentBase pageStyle='paperLess'>
                <InlineProgress />
            </ContentBase>

        );
    }
    return (

        <>
            <ContentBase {...pageProps}>
                {(searchProps || addButtonProps) && (
                    <AppBar className={classes.searchBar} position='static' color='default' elevation={0}>
                        <Toolbar>
                            <Grid container spacing={2} alignItems='center'>

                                <Grid item>
                                    {searchProps && (<SearchIcon className={classes.block} color='inherit' />)}
                                </Grid>
                                <Grid item xs>
                                    {searchProps && (
                                        <TextField
                                            fullWidth
                                            placeholder={searchProps.searchHelpText}
                                            InputProps={{
                                                disableUnderline: true,
                                                className: classes.searchInput,
                                            }}
                                            onChange={filterData}
                                        />
                                    )}
                                </Grid>
                                <Grid item>
                                    {addButtonProps && (
                                        <AddEdit
                                            updateList={fetchData}
                                            {...addButtonProps}
                                        />
                                    )}
                                    <Tooltip title='Reload'>
                                        <IconButton onClick={fetchData}>
                                            <RefreshIcon className={classes.block} color='inherit' />
                                        </IconButton>
                                    </Tooltip>
                                </Grid>
                            </Grid>
                        </Toolbar>
                    </AppBar>
                )}

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
            </ContentBase>
        </>
    );
}
ListLabels.defaultProps = {
    addButtonProps: null,
    searchProps: null,
    actionColumnProps: {
        editIconShow: true,
        editIconOverride: null,
        deleteIconShow: true,
    },
};
ListLabels.propTypes = {
    columProps: PropTypes.element.isRequired,
    pageProps: PropTypes.shape({}).isRequired,
    addButtonProps: PropTypes.shape({}),
    searchProps: PropTypes.shape({}),
    apiCall: PropTypes.func.isRequired,
    emptyBoxProps: PropTypes.shape({
        title: PropTypes.element.isRequired,
        content: PropTypes.element.isRequired,
    }).isRequired,
    actionColumnProps: PropTypes.shape({
        editIconShow: PropTypes.bool,
        editIconOverride: PropTypes.element,
        deleteIconShow: PropTypes.bool,
    }),
};
export default ListLabels;
