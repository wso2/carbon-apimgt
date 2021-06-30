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
import merge from 'lodash.merge';
import cloneDeep from 'lodash.clonedeep';
import queryString from 'query-string';
import API from 'AppData/api';
import { withTheme } from '@material-ui/styles';
import Typography from '@material-ui/core/Typography';
import Configurations from 'Config';
import StarRatingBar from 'AppComponents/Apis/Listing/StarRatingBar';
import withSettings from 'AppComponents/Shared/withSettingsContext';
import Loading from 'AppComponents/Base/Loading/Loading';
import Alert from 'AppComponents/Shared/Alert';
import Icon from '@material-ui/core/Icon';
import CustomIcon from 'AppComponents/Shared/CustomIcon';
import DefaultConfigurations from 'AppData/defaultTheme';
import ImageGenerator from './APICards/ImageGenerator';
import ApiThumb from './ApiThumb';
import DocThumb from './APICards/DocThumb';
import { ApiContext } from '../Details/ApiContext';
import NoApi from './NoApi';

const styles = (theme) => ({
    apiNameLink: {
        display: 'flex',
        alignItems: 'center',
        '& span': {
            marginLeft: theme.spacing(1),
        },
        color: theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund),
        '& .material-icons': {
            marginTop: 5,
            color: `${theme.custom.thumbnail.iconColor} !important`,
            fontSize: `${theme.custom.thumbnail.listViewIconSize}px !important`,
        },
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
        this.rowsPerPage = localStorage.getItem('portal.numberOfRows') || 10;
        this.pageType = null;
    }

    /**
     * Component mount call back
     * @returns {void}
     */
    componentDidMount() {
        this.apiType = this.context.apiType;
        this.getData();
    }

    /**
     * Component update call back
     * @param {JSON} prevProps properties from previous state of the component
     * @returns {void}
     */
    componentDidUpdate(prevProps) {
        const { query, selectedTag } = this.props;
        if (
            this.apiType !== this.context.apiType
            || query !== prevProps.query
            || prevProps.selectedTag !== selectedTag
        ) {
            this.page = 0;
            this.apiType = this.context.apiType;
            this.getData();
        }
    }

    getMuiTheme = () => {
        const { gridView, theme } = this.props;
        let themeAdditions = {};
        const muiTheme = {
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
                            lineHeight: 1,
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
                MUIDataTablePagination: {
                    root: {
                        color: theme.palette.getContrastText(theme.palette.background.default),

                    },
                },
                MuiMenuItem: {
                    root: {
                        color: theme.palette.getContrastText(theme.palette.background.default),
                    },
                },
                MUIDataTableToolbar: {
                    root: {
                        '& svg': {
                            color: theme.palette.getContrastText(theme.palette.background.default),
                        },
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
                            border: 'none',
                            '& tbody': {
                                display: 'flex',
                                flexWrap: 'wrap',
                                marginLeft: 0,
                            },
                            '& thead': {
                                display: 'none',
                            },
                            '& tr:nth-child(odd),& tr:nth-child(even)': {
                                display: 'block',
                                marginRight: 5,
                                marginBottom: 5,
                                backgroundColor: 'transparent',
                            },
                            '& td': {
                                display: 'block',
                                backgroundColor: 'transparent',
                            },
                        },
                        paper: {
                            boxShadow: 'none',
                            backgroundColor: 'transparent',
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
        }
        const systemTheme = merge({}, DefaultConfigurations, Configurations, { custom: cloneDeep(theme.custom) });
        const dataTableTheme = merge({}, muiTheme, systemTheme, themeAdditions);
        return createMuiTheme(dataTableTheme);
    };

    // get data
    getData = () => {
        const { intl } = this.props;
        this.setState({ loading: true });
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
            .catch(() => {
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
                        return <span />;
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
                                const strokeColor = theme.palette.getContrastText(theme.custom.listView.tableBodyEvenBackgrund);
                                return (
                                    <Link
                                        to={'/apis/' + apiId + '/overview'}
                                        className={classes.apiNameLink}
                                    >
                                        <CustomIcon width={16} height={16} icon='api' strokeColor={strokeColor} />

                                        <span>{apiName}</span>
                                    </Link>
                                );
                            }
                        }
                        return <span />;
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
                    defaultMessage: 'Provider/Business Owner',
                }),
                options: {
                    sort: false,
                    customBodyRender: (value, tableMeta) => {
                        if (tableMeta.rowData) {
                            if (
                                tableMeta.rowData[9] && tableMeta.rowData[9].businessOwner
                            ) {
                                return (
                                    <>
                                        <div>{tableMeta.rowData[9].businessOwner}</div>
                                        <Typography variant='caption'>
                                            <FormattedMessage
                                                defaultMessage='(Business Owner)'
                                                id='Apis.Listing.ApiTableView.business.owner.caption'
                                            />
                                        </Typography>
                                    </>
                                );
                            } else {
                                return (
                                    <>
                                        {value
                                        && (
                                            <>
                                                <div>{value}</div>
                                                <Typography variant='caption'>
                                                    <FormattedMessage
                                                        defaultMessage='(Provider)'
                                                        id='Apis.Listing.ApiTableView.provider.caption'
                                                    />
                                                </Typography>
                                            </>
                                        )}
                                    </>
                                );
                            }
                        }
                        return <span />;
                    },
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
                        return <span />;
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
            {
                name: 'businessInformation',
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
                    default:
                        break;
                }
            },
            selectableRows: 'none',
            rowsPerPage,
            onChangeRowsPerPage: (numberOfRows) => {
                const { page: pageInner, count: countInner } = this;
                if (pageInner * numberOfRows > countInner) {
                    this.page = 0;
                }
                this.rowsPerPage = numberOfRows;
                localStorage.setItem('portal.numberOfRows', numberOfRows);
                this.getData();
            },
            textLabels: {
                pagination: {
                    rowsPerPage: intl.formatMessage({
                        id: 'Apis.Listing.ApiTableView.items.per.page',
                        defaultMessage: 'Items per page',
                    }),
                },
            },
        };
        if (gridView) {
            options.customRowRender = (_data, dataIndex, rowIndex, tableViewObj = this) => {
                const artifact = tableViewObj.state.data[dataIndex];
                if (artifact) {
                    if (artifact.type === 'DOC') {
                        return <tr key={rowIndex}><td><DocThumb doc={artifact} /></td></tr>;
                    } else {
                        return (
                            <tr key={rowIndex}>
                                <td>
                                    <ApiThumb
                                        api={artifact}
                                        customHeight={theme.custom.thumbnail.height}
                                        customWidth={theme.custom.thumbnail.width}
                                    />
                                </td>
                            </tr>
                        );
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
        if (page === 0 && this.count <= rowsPerPage && rowsPerPage === 10) {
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
