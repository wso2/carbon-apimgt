/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Link } from 'react-router-dom';
import { createMuiTheme, MuiThemeProvider, withStyles } from '@material-ui/core/styles';
import MUIDataTable from 'mui-datatables';
import { FormattedMessage, injectIntl } from 'react-intl';
import queryString from 'query-string';
import API from 'AppData/api';
import { withTheme } from '@material-ui/styles';
import Configurations from 'Config';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Loading from 'AppComponents/Base/Loading/Loading';
import Alert from 'AppComponents/Shared/Alert';
import Icon from '@material-ui/core/Icon';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import ImageGenerator from './ImageGenerator';
import ApiThumb from './ApiThumb';
import DocThumb from './DocThumb';
import { ApiContext } from '../Details/ApiContext';
import NoApi from './NoApi';

const styles = (theme) => ({
    rowImageOverride: {
        '& .material-icons': {
            marginTop: 5,
            color: `${theme.custom.thumbnail.iconColor} !important`,
            fontSize: `${theme.custom.thumbnail.listViewIconSize}px !important`,
        },
    },
    apiNameLink: {
        display: 'flex',
        alignItems: 'center',
        '& span': {
            marginLeft: theme.spacing(1),
        },
        color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
    },
});
/**
 * Table view for api listing
 *
 * @class ApiTableView
 * @extends {React.Component}
 */
class ApiTableView extends React.Component {
    /**
     * @inheritdoc
     * @param {*} props properties
     * @memberof ApiTableView
     */
    constructor(props) {
        super(props);
        this.state = {
            data: null,
            loading: true,
        };
        this.page = 0;
        this.count = 100;
        this.rowsPerPage = 10;
        this.pageType = null;
    }

    getMuiTheme = () => {
        const { gridView, theme } = this.props;
        let themeAdditions = {};
        let muiTheme = {
            overrides: {
                MUIDataTable: {
                    root: {
                        backgroundColor: 'transparent',
                        marginLeft: 40,
                        marginBottom: 20,
                        width: '100%',
                    },
                    paper: {
                        boxShadow: 'none',
                        backgroundColor: 'transparent',
                        width: '100%',
                    },
                    tableRoot: {
                        border: 'solid 1px #fff',
                        '& a': {
                            display: 'flex',
                            alignItems: 'center',
                        },
                        '& a > div': {
                            paddingRight: 10,
                        },
                        '& td': {
                            whiteSpace: 'nowrap',
                        },
                        '& tr:nth-child(even)': {
                            backgroundColor: theme.custom.listView.tableBodyEvenBackgrund,
                            '& td': {
                                color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
                            },
                        },
                        '& tr:nth-child(odd)': {
                            backgroundColor: theme.custom.listView.tableBodyOddBackgrund,
                            '& td': {
                                color: theme.palette.getContrastText(theme.custom.listView.tableBodyOddBackgrund),
                            },
                        },
                        '& th': {
                            backgroundColor: theme.custom.listView.tableHeadBackground,
                            color: theme.palette.getContrastText(theme.custom.listView.tableHeadBackground),
                        },
                    },
                },
                MUIDataTableBodyCell: {
                    root: {
                        backgroundColor: 'transparent',
                        width: '100%',
                    },
                },
            },
        };
        if (gridView) {
            themeAdditions = {
                overrides: {
                    MUIDataTable: {
                        tableRoot: {
                            display: 'block',
                            '& tbody': {
                                display: 'flex',
                                flexWrap: 'wrap',
                                marginLeft: 0,
                            },
                            '& thead': {
                                display: 'none',
                            },
                        },
                        paper: {
                            boxShadow: 'none',
                            backgroundColor: 'transparent',
                        },
                    },
                },
            };
        }
        muiTheme = Object.assign(muiTheme, themeAdditions, Configurations.themes.light);
        return createMuiTheme(muiTheme);
    };

    componentDidMount() {
        this.apiType = this.context.apiType;
        this.getData();
    }

    componentDidUpdate(prevProps) {
        const { query, selectedTag } = this.props;
        if (
            this.apiType !== this.context.apiType
            || query !== prevProps.query
            || prevProps.selectedTag !== selectedTag
        ) {
            this.apiType = this.context.apiType;
            this.getData();
        }
    }

    // get data
    getData = () => {
        const { intl } = this.props;
        this.xhrRequest()
            .then((data) => {
                const { body } = data;
                const { list, pagination } = body;
                const { total } = pagination;
                this.count = total;
                this.setState({ data: list });
            })
            .catch((error) => {
                const { response } = error;
                const { setTenantDomain } = this.props;
                if (response && response.body.code === 901300) {
                    setTenantDomain('INVALID');
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Invalid tenant domain',
                        id: 'Apis.Listing.ApiTableView.invalid.tenant.domain',
                    }));
                } else {
                    Alert.error(intl.formatMessage({
                        defaultMessage: 'Error While Loading APIs',
                        id: 'Apis.Listing.ApiTableView.error.loading',
                    }));
                }
            })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    xhrRequest = () => {
        const { query, selectedTag } = this.props;
        const { page, rowsPerPage } = this;
        const { apiType } = this.context;
        const api = new API();
        const searchParam = new URLSearchParams(query);
        const searchQuery = searchParam.get('query');
        if (query && searchQuery !== null) {
            const composeQuery = queryString.parse(query);
            composeQuery.limit = this.rowsPerPage;
            composeQuery.offset = page * rowsPerPage;
            return api.search(composeQuery);
        }

        if (selectedTag) {
            return api.getAllAPIs({ query: 'tag:' + selectedTag, limit: this.rowsPerPage, offset: page * rowsPerPage });
        } else {
            return api.getAllAPIs({ limit: this.rowsPerPage, offset: page * rowsPerPage });
        }
    };

    changePage = (page) => {
        const { intl } = this.props;
        this.page = page;
        this.setState({ loading: true });
        this.xhrRequest()
            .then((data) => {
                const { body } = data;
                const { list } = body;
                this.setState({
                    data: list,
                });
            })
            .catch((e) => {
                Alert.error(intl.formatMessage({
                    defaultMessage: 'Error While Loading APIs',
                    id: 'Apis.Listing.ApiTableView.error.loading',
                }));
            })
            .finally(() => {
                this.setState({ loading: false });
            });
    };

    /**
     * @inheritdoc
     * @returns {Component}x
     * @memberof ApiTableView
     */
    render() {
        const { intl, gridView, theme } = this.props;
        const { custom: { social: { showRating } } } = theme;
        const { loading } = this.state;
        const columns = [
            {
                name: 'id',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
            {
                name: 'name',
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            return <ImageGenerator api={artifact} width={30} height={30} />;
                        }
                    },
                    sort: false,
                    filter: false,
                    display: 'excluded',
                },
            },
            {
                name: 'name',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.name',
                    defaultMessage: 'Name',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            const apiName = tableMeta.rowData[2];
                            const apiId = tableMeta.rowData[0];
                            const { classes } = this.props;

                            if (artifact) {
                                if (artifact.type === 'DOC') {
                                    return (
                                        <Link
                                            to={'/apis/' + artifact.apiUUID + '/documents'}
                                            className={classes.apiNameLink}
                                        >
                                            <Icon>library_books</Icon>

                                            <span>
                                                {' '}
                                                <FormattedMessage
                                                    id='Apis.Listing.TableView.TableView.doc.flag'
                                                    defaultMessage='[Doc] '
                                                />
                                                {' '}
                                                {apiName}
                                            </span>
                                        </Link>
                                    );
                                }
                                return (
                                    <Link
                                        to={'/apis/' + apiId + '/overview'}
                                        className={classes.rowImageOverride}
                                        className={classes.apiNameLink}
                                    >
                                        <CustomIcon width={16} height={16} icon='api' strokeColor='#444444' />

                                        <span>{apiName}</span>
                                    </Link>
                                );
                            }
                        }
                    },
                    sort: false,
                    filter: false,
                },
            },
            {
                name: 'version',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.version',
                    defaultMessage: 'Version',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'context',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.context',
                    defaultMessage: 'Context',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'provider',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.provider',
                    defaultMessage: 'Provider',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'type',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.type',
                    defaultMessage: 'Type',
                }),
                options: {
                    sort: false,
                },
            },
            {
                name: 'rating',
                label: intl.formatMessage({
                    id: 'Apis.Listing.ApiTableView.rating',
                    defaultMessage: 'Rating',
                }),
                options: {
                    customBodyRender: (value, tableMeta, updateValue, tableViewObj = this) => {
                        if (tableMeta.rowData) {
                            const artifact = tableViewObj.state.data[tableMeta.rowIndex];
                            if (artifact) {
                                if (artifact.type !== 'DOC') {
                                    const apiId = tableMeta.rowData[0];
                                    const avgRating = tableMeta.rowData[8];
                                    return (
                                        <StarRatingBar
                                            apiRating={avgRating}
                                            apiId={apiId}
                                            isEditable={false}
                                            showSummary={false}
                                        />
                                    );
                                }
                            }
                        }
                    },
                    sort: false,
                    display: showRating ? 'true' : 'excluded',
                },
            },
            {
                name: 'avgRating',
                options: {
                    display: 'excluded',
                    filter: false,
                },
            },
        ];
        const { page, count, rowsPerPage } = this;
        const { data } = this.state;
        const options = {
            filterType: 'dropdown',
            responsive: 'stacked',
            serverSide: true,
            search: false,
            count,
            page,
            onTableChange: (action, tableState) => {
                switch (action) {
                    case 'changePage':
                        this.changePage(tableState.page);
                        break;
                }
            },
            selectableRows: 'none',
            rowsPerPage,
            onChangeRowsPerPage: (numberOfRows) => {
                const { page, count } = this;
                if (page * numberOfRows > count) {
                    this.page = 0;
                }
                this.rowsPerPage = numberOfRows;
                this.getData();
            },
        };
        if (gridView) {
            options.customRowRender = (data, dataIndex, rowIndex, tableViewObj = this) => {
                const artifact = tableViewObj.state.data[dataIndex];
                if (artifact) {
                    if (artifact.type === 'DOC') {
                        return <tr key={rowIndex}><td><DocThumb doc={artifact} /></td></tr>;
                    } else {
                        return <tr key={rowIndex}><td><ApiThumb api={artifact} /></td></tr>;
                    }
                }
                return <span />;
            };
            options.title = false;
            options.filter = false;
            options.print = false;
            options.download = false;
            options.viewColumns = false;
            options.customToolbar = false;
        } else {
            options.filter = false;
        }
        if (page === 0 && this.count <= rowsPerPage) {
            options.pagination = false;
        } else {
            options.pagination = true;
        }
        if (loading) {
            return <Loading />;
        }
        if ((data && data.length === 0) || !data) {
            return <NoApi />;
        }
        return (
            <MuiThemeProvider theme={this.getMuiTheme()}>
                <MUIDataTable title='' data={data} columns={columns} options={options} />
            </MuiThemeProvider>
        );
    }
}

ApiTableView.contextType = ApiContext;

export default withSettings(injectIntl(withTheme(withStyles(styles)(ApiTableView))));
